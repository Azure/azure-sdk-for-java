# Release History

## 1.0.0-beta.2 (Unreleased)

### Features Added

### Breaking Changes

- Removed `subscriptionId`, `resourceGroupName`, `workspaceName` API from `EasmClientBuilder` client builder.
  Please set all of these values into `endpoint` API, e.g. `builder.endpoint("https://<host>/subscriptions/<subscriptionId>/resourceGroups/<resourceGroupName>/workspaces/<workspaceName>/")`.

### Bugs Fixed

### Other Changes

## 1.0.0-beta.1 (2023-11-07)

- Azure EASM (External Attack Surface Management) client library for Java. This package contains Microsoft Azure EASM client library.

