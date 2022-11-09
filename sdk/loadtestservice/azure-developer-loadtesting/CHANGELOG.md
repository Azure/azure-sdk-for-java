# Release History

## 1.0.0 (Unreleased)

- First stable release of Azure Load Testing client library for Java with single top-level `LoadTestingClientBuilder` which can build `LoadTestAdministration` and `LoadTestRun` clients.

### Features Added

- Metric namespaces, metric dimensions and metric values

### Breaking Changes

- There is a single builder which builds `LoadTestAdministration` and `LoadTestRun` clients, rather than a client which provides them via accessor methods previously
- Significant changes in metrics API, introduction of metric namespaces and metric dimensions
- File upload now uses `application/octet-stream` instead of `multipart/form-data`
- File upload now uses file name as primary identifier instead of `fileId`

### Bugs Fixed

### Other Changes

## 1.0.0-beta.1 (2022-10-22)

- Initial preview release of Azure Load Testing client library for Java with single top-level `LoadTesting` client, and `LoadTestAdministration` and `TestRun` subclients.
