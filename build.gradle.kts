import com.github.vlsi.gradle.publishing.dsl.simplifyXml
import com.github.vlsi.gradle.publishing.dsl.versionFromResolution

plugins {
    `java-library`
    `maven-publish`
    id("com.github.vlsi.gradle-extensions")
    id("com.github.vlsi.stage-vote-release")
}

repositories {
    ivy {
        url = uri("https://github.com/angular/clang-format/raw/")
        content {
            includeModule("com.github.angular", "clang-format")
        }
        patternLayout {
            artifact("[revision]/bin/[classifier]/[module](.[ext])")
        }
        metadataSources { // skip downloading ivy.xml
            artifact()
        }
    }
}

group = "com.github.autostyle"

releaseParams {
    tlp.set("autostyle-clang-format")
    organizationName.set("autostyle")
    componentName.set("autostyle-clang-format")
    prefixForProperties.set("gh")
    svnDistEnabled.set(false)
    sitePreviewEnabled.set(false)
    nexus {
        mavenCentral()
    }
    voteText.set {
        """
        ${it.componentName} v${it.version}-rc${it.rc} is ready for preview.

        Git SHA: ${it.gitSha}
        Staging repository: ${it.nexusRepositoryUri}
        """.trimIndent()
    }
}

val String.v: String get() = rootProject.extra["$this.version"] as String

val buildVersion = "autostyle-clang-format".v + releaseParams.snapshotSuffix
version = buildVersion

println("Building autostyle-clang-format $version")

class TargetPlatform(val os: OperatingSystemFamily, val architecture: MachineArchitecture) {
    val id: String get() = "${os.name}_${architecture.name}"
}

fun targetPlatform(os: String, architecture: String): TargetPlatform =
        TargetPlatform(objects.named(os), objects.named(architecture))

val platforms = mapOf(
        targetPlatform(OperatingSystemFamily.WINDOWS, MachineArchitecture.X86) to "win32",
        targetPlatform(OperatingSystemFamily.MACOS, MachineArchitecture.X86_64) to "darwin_x64",
        targetPlatform(OperatingSystemFamily.LINUX, MachineArchitecture.X86_64) to "linux_x64"
)

dependencies {
    for ((platform, classifier) in platforms) {
        val ext = if (platform.os.isWindows) "exe" else ""
        val conf = configurations.create("clangFormatBinary_${platform.id}")
        conf("com.github.angular:clang-format:${"clang-format".v}:$classifier@$ext")
    }
}

val binaries by configurations.creating {
    isCanBeConsumed = true
    isCanBeResolved = false
    attributes {
        attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.NATIVE_RUNTIME))
    }
    outgoing {
        variants {
            for (platform in platforms.keys) {
                create(platform.id) {
                    attributes {
                        attribute(OperatingSystemFamily.OPERATING_SYSTEM_ATTRIBUTE, platform.os)
                        attribute(MachineArchitecture.ARCHITECTURE_ATTRIBUTE, platform.architecture)
                    }
                }
            }
        }
    }
}

(components["java"] as AdhocComponentWithVariants).addVariantsFromConfiguration(binaries) {
}

for (platform in platforms.keys) {
    val jarTask = tasks.register<Jar>("package_${platform.id}") {
        group = LifecycleBasePlugin.BUILD_GROUP
        description = "Package clang-format ${platform.id}"
        archiveClassifier.set(platform.id)
        from(configurations.named("clangFormatBinary_${platform.id}").map { it.singleFile }) {
            rename { "${project.name}-${platform.id}" }
        }
    }
    binaries.outgoing.variants {
        named(platform.id) {
            artifact(jarTask)
        }
    }
}


tasks {
    withType<AbstractArchiveTask>().configureEach {
        // Ensure builds are reproducible
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true
        dirMode = "775".toInt(8)
        fileMode = "664".toInt(8)
    }

    withType<JavaCompile>().configureEach {
        options.encoding = "UTF-8"
    }

    withType<Jar>().configureEach {
        manifest {
            attributes["Bundle-License"] = "Apache-2.0"
            attributes["Implementation-Title"] = "Autostyle clang-format"
            attributes["Implementation-Version"] = project.version
            attributes["Specification-Vendor"] = "Autostyle clang-format"
            attributes["Specification-Version"] = project.version
            attributes["Specification-Title"] = "Autostyle clang-format"
            attributes["Implementation-Vendor"] = "Autostyle clang-format"
            attributes["Implementation-Vendor-Id"] = "com.github.autostyle"
        }
    }

    publishing {
        publications {
            create<MavenPublication>(project.name) {
                artifactId = project.name
                version = rootProject.version.toString()
                description = project.description
                from(project.components.get("java"))
                versionFromResolution()
                pom {
                    simplifyXml()
                    name.set(
                            (project.findProperty("artifact.name") as? String)
                                    ?: "Autostyle clang-format ${project.name.capitalize()}"
                    )
                    description.set(
                            project.description
                                    ?: "Autostyle clang-format ${project.name.capitalize()}"
                    )
                    developers {
                        developer {
                            id.set("vlsi")
                            name.set("Vladimir Sitnikov")
                            email.set("sitnikov.vladimir@gmail.com")
                        }
                    }
                    inceptionYear.set("2019")
                    url.set("https://github.com/autostyle/clang-format")
                    licenses {
                        license {
                            name.set("The Apache License, Version 2.0")
                            url.set("https://www.apache.org/licenses/LICENSE-2.0.txt")
                            comments.set("A business-friendly OSS license")
                            distribution.set("repo")
                        }
                    }
                    issueManagement {
                        system.set("GitHub")
                        url.set("https://github.com/autostyle/clang-format/issues")
                    }
                    scm {
                        connection.set("scm:git:https://github.com/autostyle/clang-format.git")
                        developerConnection.set("scm:git:https://github.com/autostyle/clang-format.git")
                        url.set("https://github.com/autostyle/clang-format")
                        tag.set("HEAD")
                    }
                }
            }
        }
    }
}
