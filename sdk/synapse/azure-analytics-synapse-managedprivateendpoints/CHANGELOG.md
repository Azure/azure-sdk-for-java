# Release History

## 1.0.0-beta.5 (Unreleased)


## 1.0.0-beta.4 (2021-08-10)
- Update API version to 2021-06-01-preview
- Added missing properties for managed private endpoints in stable version

## 1.0.0-beta.3 (2021-04-06)

### Dependency Updates
- Update azure-core to 1.15.0

## 1.0.0-beta.2 (2021-02-09)

- Support specifying the service API version. (AutoRest update)
- Send missing "Accept" request headers

## 1.0.0-beta.1 (2020-12-15)

Version 1.0.0-beta.1 is a beta of our efforts in creating an Azure Synapse Managed Private Endpoints client library that is developer-friendly, idiomatic to
the Java ecosystem, and as consistent across different languages and platforms as possible. The principles that guide
our efforts can be found in the
[Azure SDK Design Guidelines for Java](https://azure.github.io/azure-sdk/java_introduction.html).

### New Features

- It uses Azure Synapse 2019-06-01-preview API
- One client design:
    - `ManagedPrivateEndpointsClient` to manage private endpoints for Azure Synapse
- Reactive streams support using [Project Reactor](https://projectreactor.io/)
