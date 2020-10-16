# Release History

## 1.0.0 (Unreleased)

### New Features

- Regenerate protocol layer from service API version 2020-10-31
- Update service API version to use service API version 2020-10-31 by default
- Add optional parameters for traceparent and tracestate to all service request APIs to support distributed tracing

### Breaking Changes

- Add messageId as mandatory parameter for telemetry APIs. Service API version 2020-10-31 requires this parameter.

## 1.0.0-beta.3 (2020-10-01)

- Fixed issue with pagination APIs that support max-item-count where the item count was not respected from the second page forward.

## 1.0.0-beta.2 (2020-09-24)

### Fixes and improvements

- Paging functionality for list operations has been fixed.
- `listModelOptions` is renamed to `modelsListOptions` for naming consistency.

## 1.0.0-beta.1 (2020-09-22)

### New features

- Official public preview of azure-digitalTwins-core SDK
- [Azure Digital Twins Public Repo](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/digitaltwins/azure-digitaltwins-core)
- [Azure Digital Twins Samples](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/digitaltwins/azure-digitaltwins-core/src/samples)

### Breaking changes

- N/A

### Added

- N/A

### Fixes and improvements

- N/A
