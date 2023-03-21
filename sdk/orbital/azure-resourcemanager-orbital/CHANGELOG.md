# Release History

## 1.0.0-beta.2 (2023-03-21)

- Azure Resource Manager orbital client library for Java. This package contains Microsoft Azure SDK for orbital Management SDK. Azure Orbital service. Package tag package-2022-11-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ContactProfile$DefinitionStages` was modified

* Stage 3, 4 was added

#### `models.Spacecraft$DefinitionStages` was modified

* Stage 3, 4, 5, 6 was added

#### `models.Contact$DefinitionStages` was modified

* Stage 2, 3, 4, 5 was added

#### `models.Spacecraft` was modified

* `etag()` was removed

#### `models.Contact` was modified

* `etag()` was removed

#### `models.ContactProfile` was modified

* `etag()` was removed

#### `models.AvailableGroundStations` was modified

* `getWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `get(java.lang.String)` was removed

### Features Added

* `models.ContactProfileThirdPartyConfiguration` was added

#### `models.ContactProfile$Definition` was modified

* `withProvisioningState(models.ContactProfilesPropertiesProvisioningState)` was added
* `withLinks(java.util.List)` was added
* `withEventHubUri(java.lang.String)` was added
* `withMinimumElevationDegrees(java.lang.Float)` was added
* `withNetworkConfiguration(models.ContactProfilesPropertiesNetworkConfiguration)` was added
* `withAutoTrackingConfiguration(models.AutoTrackingConfiguration)` was added
* `withThirdPartyConfigurations(java.util.List)` was added
* `withMinimumViableContactDuration(java.lang.String)` was added

#### `models.AvailableContacts` was modified

* `rxStartTime()` was added
* `startAzimuthDegrees()` was added
* `endAzimuthDegrees()` was added
* `maximumElevationDegrees()` was added
* `endElevationDegrees()` was added
* `txStartTime()` was added
* `rxEndTime()` was added
* `startElevationDegrees()` was added
* `txEndTime()` was added

#### `models.Spacecraft` was modified

* `systemData()` was added

#### `models.Contact` was modified

* `systemData()` was added

#### `models.ContactProfile` was modified

* `thirdPartyConfigurations()` was added
* `links()` was added
* `minimumViableContactDuration()` was added
* `minimumElevationDegrees()` was added
* `networkConfiguration()` was added
* `autoTrackingConfiguration()` was added
* `provisioningState()` was added
* `eventHubUri()` was added

#### `models.AvailableGroundStation` was modified

* `releaseMode()` was added
* `longitudeDegrees()` was added
* `providerName()` was added
* `city()` was added
* `altitudeMeters()` was added
* `latitudeDegrees()` was added

#### `models.OperationResult` was modified

* `value()` was added
* `nextLink()` was added

#### `models.ContactProfilesProperties` was modified

* `thirdPartyConfigurations()` was added
* `withThirdPartyConfigurations(java.util.List)` was added

## 1.0.0-beta.1 (2022-06-23)

- Azure Resource Manager orbital client library for Java. This package contains Microsoft Azure SDK for orbital Management SDK. Azure Orbital service. Package tag package-2022-03-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
