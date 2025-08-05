# Release History

## 2.53.1 (2025-08-05)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.53.0 (2025-07-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.52.0 (2025-06-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.51.0 (2025-05-26)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.50.0 (2025-04-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.49.0 (2025-03-28)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.48.0 (2025-02-28)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.47.0 (2025-01-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.46.0 (2024-12-23)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.45.0 (2024-11-28)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.44.0 (2024-10-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.43.0 (2024-09-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.42.0 (2024-08-23)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.42.0-beta.1 (2024-08-15)

### Other Changes

- Replaced `Jackson` with `azure-json` for serialization/deserialization.

### Dependency Updates

- Updated `api-version` of `FailoverGroup` and `ReplicationLink` to `2023-05-01-preview`.

## 2.41.0 (2024-07-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.40.0 (2024-06-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.39.0 (2024-05-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.39.0-beta.1 (2024-05-15)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `composite-v5`.

## 2.38.0 (2024-04-16)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.37.0 (2024-03-22)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.36.0 (2024-02-29)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.35.0 (2024-01-26)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.34.0-beta.1 (2023-12-25)

- Preview release for `api-version` `2023-05-01-preview`.

## 2.34.0 (2023-12-22)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.33.0 (2023-11-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.32.0 (2023-10-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.32.0-beta.1 (2023-10-09)

- Preview release for `api-version` `2023-02-01-preview`.

## 2.31.0 (2023-09-28)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.30.0 (2023-08-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.29.0 (2023-07-28)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.29.0-beta.1 (2023-07-21)

- Preview release for `api-version` `2022-11-01-preview`.

## 2.28.0 (2023-06-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.27.0 (2023-05-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.26.0 (2023-04-21)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.25.0 (2023-03-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.24.0 (2023-02-17)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.23.0 (2023-01-27)

### Features Added

- Added `defineThreatDetectionPolicy(SecurityAlertPolicyName policyName)` in `SqlDatabase` to replace deprecated `String` parameter overload.

### Breaking Changes

- Removed `NEW` from `SecurityAlertPolicyState`. The constant is non-functional. 
- Removed `withPolicyNew` method from `SqlDatabaseThreatDetectionPolicy` since `NEW` is no longer supported in `SecurityAlertPolicyState`.
- Removed `nextResetTime` and `resourceName` methods from `ServerMetric` and `SqlDatabase`. The methods are non-functional.
- Removed `listMetricsDefinitions` and `listMetrics` methods from `SqlDatabase`. Metrics in SQL have been replaced by the Azure monitor shoebox metrics API. Not in SQL any more.
- Removed `listServiceTierAdvisors` method from `SqlDatabase`. It's no longer supported.
- Removed class `ElasticPoolDatabaseActivity`. It's removed from service definition.
- Removed `listDatabaseActivities`, `listDatabaseMetricDefinitions` and `listDatabaseMetrics` methods from `SqlElasticPool`. 
- Removed `elasticPoolName` and `serviceLevelObjective` methods from `SqlRestorableDroppedDatabase`. 
- Removed `getServiceObjective`, `listRecommendedElasticPools`, `listServiceObjectives` methods from `SqlServer`.
- Removed `withCreationDate` and `withThumbprint` from SqlServerKeyOperations. The properties are no longer mutable.
- Renamed class from `TransparentDataEncryptionInner` to `LogicalDatabaseTransparentDataEncryptionInner`.
- Removed class `TransparentDataEncryptionActivity`. The class is no longer functional.
- Removed `listActivities` from `TransparentDataEncryption` since `TransparentDataEncryptionActivity` is removed.
- Renamed `TransparentDataEncryptionStatus` to `TransparentDataEncryptionState`.
- Removed `location`, `requestedDatabaseDtuCap`, `requestedDatabaseDtuGuarantee`, `requestedDatabaseDtuMax`, `requestedDatabaseDtuMin`, 
  `requestedDtu`, `requestedDtuGuarantee` and `requestedElasticPoolName`, `requestedStorageLimitInGB` and `requestedStorageLimitInMB` methods from `ElasticPoolActivity`. The properties are no longer functional.
- Renamed class from `ElasticPoolActivityInner` to `ElasticPoolOperationInner`.
- Removed `readReplicaCount` and `withReadReplicaCount` from `DatabaseUpdate`. The property is non-functional.
- Changed return type of method `actionsRequired` in `PrivateLinkServiceConnectionStateProperty` in `PrivateLinkServiceConnectionStateProperty` from `String` to `PrivateLinkServiceConnectionStateActionsRequire`.
- Changed return type of method `status` and parameter type of method `withStatus` in `PrivateLinkServiceConnectionStateProperty` from `String` to `PrivateLinkServiceConnectionStateStatus`.
- Changed type of `ServerConnectionType` from `enum` to `ExpandableStringEnum`.
- Renamed class `PrivateEndpointConnectionPropertiesAutoGenerated` to `PrivateEndpointConnectionProperties`.
- Renamed class `ServerPublicNetworkAccess` to `ServerNetworkAccessFlag`.
- Renamed class `ExportRequest` to `ExportDatabaseDefinition`.
- Renamed class `ImportExportResponseInner` to `ImportExportOperationResultInner`.
- Renamed class `ImportExtensionRequest` to `ImportExistingDatabaseDefinition`.
- Changed type of `StorageKeyType` from `enum` to `ExpandableStringEnum`.


## 2.22.0 (2022-12-23)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.21.0 (2022-11-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.20.0 (2022-10-26)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.19.0 (2022-09-23)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.18.0 (2022-08-26)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.17.0 (2022-07-25)

### Breaking Changes

- Removed unused classes.

## 2.16.0 (2022-06-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.15.0 (2022-05-25)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.14.0 (2022-04-11)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.13.0 (2022-03-11)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.12.0 (2022-02-14)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources

## 2.11.0 (2022-01-17)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources

## 2.10.0 (2021-11-22)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources

## 2.9.0 (2021-10-21)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources

## 2.8.0 (2021-09-15)

### Dependency Updates

- Updated core dependency from resources

## 2.7.0 (2021-08-12)

### Dependency Updates

- Updated core dependency from resources

## 2.6.0 (2021-06-18)

- Updated core dependency from resources

## 2.5.0 (2021-05-28)
- Updated core dependency from resources

## 2.4.0 (2021-04-28)

- Updated core dependency from resources

## 2.3.0 (2021-03-30)

- Updated core dependency from resources

## 2.2.0 (2021-02-24)

- Updated core dependency from resources

## 2.1.0 (2020-11-24)

- Updated core dependency from resources

## 2.0.0-beta.5 (2020-10-19)

- Refactored deprecated definition and update flow
- Add `DatabaseSku` and `ElasticPoolSku` generated from API

## 2.0.0-beta.4 (2020-09-02)

- Updated core dependency from resources
