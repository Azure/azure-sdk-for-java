# Release History

## 1.0.0-beta.4 (2021-08-10)
- Update to 2020-12-01 API version

## 1.0.0-beta.3 (2021-04-06)

### Breaking Changes
- `listRoleAssignmentsWithResponse()` now returns `RoleAssignmentsListRoleAssignmentsResponse`

### Dependency Updates
- Update azure-core to 1.15.0

## 1.0.0-beta.2 (2021-02-09)

- Support specifying the service API version. (AutoRest update)
- Send missing "Accept" request headers

## 1.0.0-beta.1 (2020-12-08)

Version 1.0.0-beta.1 is a beta of our efforts in creating an Azure Synapse Access Control client library that is developer-friendly, idiomatic to
the Java ecosystem, and as consistent across different languages and platforms as possible. The principles that guide
our efforts can be found in the
[Azure SDK Design Guidelines for Java](https://azure.github.io/azure-sdk/java_introduction.html).

### New Features

- It uses Azure Synapse 2019-08-01-preview API
- Two client design:
    - `RoleDefinitionsClient` to manage role definitions in Synapse
    - `RoleAssignmentsClient` to manage role assignments in Synapse
- Reactive streams support using [Project Reactor](https://projectreactor.io/)
