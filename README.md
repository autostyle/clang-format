# Autostyle-clang-format: execute clang-format from Java

[clang-format](https://clang.llvm.org/docs/ClangFormat.html) is a tool that can automatically format
source files. As of version 11 it supports C/C++/Java/JavaScript/Objective-C/Protobuf/C#.

`autostyle-clang-format` publishes `clang-format` to Maven repository, so it is accessible
from Java projects.

## Maven coordinates

The jar with binary has classifier `${operatingSystem}_${architecture}`.
The binary inside the jar is named as `com/github/autostyle/autostyle-clang-format-${version}-${operatingSystem}_${architecture}`.

- `com.github.autostyle:autostyle-clang-format:1.0:macos_x86-64`
- `com.github.autostyle:autostyle-clang-format:1.0:linux_x86-64`
- `com.github.autostyle:autostyle-clang-format:1.0:windows_x86`

## Author

`autostyle-clang-format` was created by Vladimir Sitnikov <sitnikov.vladimir@gmail.com>

## License

`autostyle-clang-format` comes under Apache 2.0
`clang-format` is a part of `llvm suite` which is licensed under [LLVM license](https://releases.llvm.org/10.0.0/LICENSE.TXT)

## Acknowledgements

- Thanks to https://github.com/angular/clang-format for keeping up to date binaries
- Built by [Gradle](https://gradle.org/)
