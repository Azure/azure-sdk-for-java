# Release History

## 2.5.0-beta.1 (Unreleased)


## 2.4.0 (2021-04-28)

- Updated `api-version` to `2021-03-01`
- Supported Private Link in `KubernetesCluster`

## 2.3.0 (2021-03-30)

- Updated core dependency from resources

## 2.2.0 (2021-02-24)

- Updated `api-version` to `2020-11-01`
- Removed `withNodeImageVersion` method in `ManagedClusterAgentPoolProfileProperties`
- Removed unused class `Components1Q1Og48SchemasManagedclusterAllof1`
- Removed unused class `ComponentsQit0EtSchemasManagedclusterpropertiesPropertiesIdentityprofileAdditionalproperties`, it is same as its superclass `UserAssignedIdentity`

## 2.1.0 (2020-11-24)

- Updated core dependency from resources

## 2.0.0 (2020-10-19)

- Supported multi agent pools in kubernetes cluster
- Added required property `VirtualMachineCount` in agent pool
- Changed `withLatestVersion` to `withDefaultVersion` in kubernetes cluster
- Removed `KubernetesVersion` enum

## 2.0.0-beta.4 (2020-09-02)

- Updated `api-version` to `2020-06-01`
- Add `withAgentPoolMode` in `KubernetesClusterAgentPool`
