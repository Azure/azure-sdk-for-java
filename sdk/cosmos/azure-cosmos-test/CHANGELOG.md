## Release History

### 1.0.0-beta.5 (Unreleased)

#### Features Added

#### Breaking Changes

#### Bugs Fixed

#### Other Changes

### 1.0.0-beta.4 (2023-07-18)

#### Features Added
* Added fault injection support for Gateway connection mode - See [PR 35378](https://github.com/Azure/azure-sdk-for-java/pull/35378)

#### Bugs Fixed
* Fixed an issue where `FaultInjectionServerErrorType.TIMEOUT` is not injecting the correct error response - See [PR 34723](https://github.com/Azure/azure-sdk-for-java/pull/34723)
* Fixed an issue where connection error is not being injected when FaultInjectionEndpoints is not configured - See [PR 35034](https://github.com/Azure/azure-sdk-for-java/pull/35034)

### 1.0.0-beta.3 (2023-04-21)

#### Features Added
* Added `getHitCountDetails` in `FaultInjectionRule` - See [PR 34581](https://github.com/Azure/azure-sdk-for-java/pull/34581)

#### Other Changes
* Added `faultInjectionEvaluationResults` in `CosmosDiagnostics` - See [PR 34581](https://github.com/Azure/azure-sdk-for-java/pull/34581)

### 1.0.0-beta.2 (2023-04-06)

#### Bugs Fixed
* Fixed an issue where `CONNECTION_DELAY` fault injection rule is not applied during `openConnectionsAndInitCaches` - See [PR 34096](https://github.com/Azure/azure-sdk-for-java/pull/34096)
* Fix an issue where `hitCount` is not being tracked properly for connection error type rules - See [PR 34295](https://github.com/Azure/azure-sdk-for-java/pull/34295)

### 1.0.0-beta.1 (2023-03-17)

#### Features Added
* Added fault injection support - See [PR 33329](https://github.com/Azure/azure-sdk-for-java/pull/33329) 

