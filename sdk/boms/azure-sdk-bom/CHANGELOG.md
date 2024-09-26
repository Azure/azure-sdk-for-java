# Release History

## 1.2.27 (2024-08-27)

### Dependency Updates

- Updated Azure SDK dependency versions to the latest releases.

## 1.2.26 (2024-07-30)

### Dependency Updates

- Updated Azure SDK dependency versions to the latest releases.

## 1.2.25 (2024-07-02)

### Dependency Updates

- Added new library entries to the BOM:
  - `azure-messaging-eventgrid-namespaces`

- Updated Azure SDK dependency versions to the latest releases.

## 1.2.24 (2024-05-29)

### Dependency Updates

- Added new library entries to the BOM:
  - `azure-ai-translation-text`
  - `azure-developer-devcenter`

- Updated Azure SDK dependency versions to the latest releases.

## 1.2.23 (2024-04-24)

### Dependency Updates

- Updated Azure SDK dependency versions to the latest releases.

## 1.2.22 (2024-03-26)

### Dependency Updates

- Updated Azure SDK dependency versions to the latest releases.

## 1.2.21 (2024-02-26)

### Dependency Updates

- Updated Azure SDK dependency versions to the latest releases.

## 1.2.20 (2024-01-25)

### Dependency Updates

- Updated Azure SDK dependency versions to the latest releases.

## 1.2.19 (2023-12-07)

### Dependency Updates

- Updated Azure SDK dependency versions to the latest releases.

## 1.2.18 (2023-10-26)

### Dependency Updates

- Updated Azure SDK dependency versions to the latest releases.

## 1.2.17 (2023-09-28)

### Dependency Updates

- Updated Azure SDK dependency versions to the latest releases.

## 1.2.16 (2023-08-23)

### Dependency Updates

- Updated Azure SDK dependency versions to the latest releases.

## 1.2.15 (2023-07-26)

### Dependency Updates

- Updated Azure SDK dependency versions to the latest releases.

## 1.2.14 (2023-06-22)

### Dependency Updates

- Updated Azure SDK dependency versions to the latest releases.

## 1.2.13 (2023-05-23)

### Dependency Updates

- Updated Azure SDK dependency versions to the latest releases.

## 1.2.12 (2023-04-26)

### Dependency Updates

- Updated Azure SDK dependency versions to the latest releases.

## 1.2.11 (2023-03-18)

### Dependency Updates

- Updated Azure SDK dependency versions to the latest releases.

## 1.2.10 (2023-02-22)

### Dependency Updates

- Updated Azure SDK dependency versions to the latest releases.

## 1.2.9 (2023-01-25)

### Dependency Updates

- Updated Azure SDK dependency versions to the latest releases.

## 1.2.8 (2022-11-22)

### Dependency Updates

- Updated Azure SDK dependency versions to the latest releases.

## 1.2.7 (2022-10-25)

### Dependency Updates

- Updated Azure SDK dependency versions to the latest releases.

## 1.2.6 (2022-09-20)

### Dependency Updates

- Updated Azure SDK dependency versions to the latest releases.

## 1.2.5 (2022-08-24)

### Dependency Updates

- Updated Azure SDK dependency versions to the latest releases.

## 1.2.4 (2022-07-19)

### Dependency Updates

- Updated Azure SDK dependency versions to the latest releases.

## 1.2.3 (2022-06-22)

### Dependency Updates

- Updated Azure SDK dependency versions to the latest releases.

## 1.2.2 (2022-05-23)

### Dependency Updates

- Updated Azure SDK dependency versions to the latest releases.

## 1.2.1 (2022-04-19)

### Dependency Updates

- Updated Azure SDK dependency versions to the latest releases.

## 1.2.0 (2022-03-18)

### Dependency Updates

- Updated Azure SDK dependency versions to the latest releases.

#### Breaking Changes
Removed `azure-communication-networktraversal` from the BOM. If you depend on this library please take a direct dependency on it.

## 1.1.1 (2022-02-18)

### Dependency Updates

- Updated Azure SDK dependency versions to the latest releases.

## 1.1.0 (2022-01-22)

### Dependency Updates

- Updated Azure SDK dependency versions to more recent releases.
- Removed non-Azure SDK dependencies from the Azure SDK BOM.
Azure SDK BOM used to include external dependencies till now however this let to [dependency conflicts](https://github.com/Azure/azure-sdk-for-java/issues/26217)
in situations where our libraries were used alongside other frameworks. Given the above we are removing 3rd party dependencies from the BOM and letting the Azure SDK Client implementation libraries bring them in at runtime. 
We continue to ensure all our client libraries have unified set of external dependencies to prevents conflicts when used together.

#### Breaking Changes
If you depend on Azure SDK BOM to get versions for external libraries you would now see compilation errors in your project. Please explicitly add the required versions of your external dependencies to your project POM files. 
Depending on implicit version for transitive dependencies should be avoided as it is not a recommended practice. 

## 1.0.6 (2021-11-22)

### Dependency Updates

- Updated Azure SDK dependency versions to the latest releases.
- Updated non-Azure SDK dependency versions to align with the versions Azure SDK uses.

## 1.0.5 (2021-10-18)

### Dependency Updates

- Updated Azure SDK dependency versions to the latest  releases.
- Updated non-Azure SDK dependency versions to align with the versions Azure SDK uses.

## 1.0.4 (2021-09-27)

### Dependency Updates

- Updated Azure SDK dependency versions to the latest releases.
- Updated non-Azure SDK dependency versions to align with the versions Azure SDK uses.

## 1.0.3 (2021-06-02)

### Dependency Updates

- Updated Azure SDK dependency versions to the latest  releases.
- Updated non-Azure SDK dependency versions to align with the versions Azure SDK uses.

## 1.0.2 (2021-02-25)

### Dependency Updates

- Updated Azure SDK dependency versions to the latest releases.
- Transitioned loose dependencies of Jackson, Netty, and Reactor to using their BOMs.

## 1.0.1 (2020-06-19)

- Updated Azure SDK dependency versions to the latest  release.
- Added Azure SDK direct runtime dependencies to dependency management section.

## 1.0.0 (2020-04-17)

- Initial release. Please see the README and wiki for information on the new design.
