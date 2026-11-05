# Release History

## 1.2.0-beta.1 (Unreleased)

### Features Added

- Added opt-in JUnit 6 support through `junitVersion=6`, requiring JDK 17 or later to build and run tests. JUnit 5 remains the default.

### Breaking Changes

### Bugs Fixed

- Configured Maven Surefire for generated JUnit Jupiter projects so `mvn test` discovers and runs their tests.
- Fixed post-generation status output on Windows, where `echo` cannot be launched as an executable.

### Other Changes

## 1.0.0 (2021-11-23)

### Features Added
- A dependency on the latest `azure-sdk-bom` BOM release, to ensure that all Azure SDK for Java dependencies are aligned
  and give you the best developer experience possible.
- Built-in support for GraalVM native image compilation.
- Support for generating a new project with a specified set of Azure SDK for Java client libraries.
