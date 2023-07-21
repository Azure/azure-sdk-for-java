# Release History

## 2.29.0-beta.1 (Unreleased)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2023-06-01`.

## 2.28.0 (2023-06-25)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2023-05-01`.

## 2.27.0 (2023-05-25)

### Breaking Changes

- The property `dockerBridgeCidr` in class `KubernetesCluster` has no effect since 2019.

#### Dependency Updates

- Updated `api-version` to `2023-04-01`.

## 2.26.0 (2023-04-21)

### Breaking Changes

- Removed field `BASIC` from class `ManagedClusterSkuName`.
- Removed field `PAID` from class `ManagedClusterSkuTier`.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2023-03-01`.

## 2.25.0 (2023-03-24)

### Features Added
- Supported FIPS-enabled OS for agent pool machines.

## 2.24.0 (2023-02-17)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2023-01-01`.

## 2.23.0 (2023-01-27)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2022-11-01`.

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

- Updated `api-version` to `2022-09-01`.

## 2.19.0 (2022-09-23)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.18.0 (2022-08-26)

### Features Added

- Supported `beginCreateAgentPool` in `KubernetesCluster`.

### Other Changes

- Deprecated method `KubernetesClusters.listKubernetesVersions`. Use `KubernetesClusters.listOrchestrators`.

#### Dependency Updates

- Updated `api-version` to `2022-07-01`.

## 2.17.0 (2022-07-25)

### Breaking Changes

- Replaced property `azureDefender` with `defender` of type `ManagedClusterSecurityProfileDefender` 
  in `ManagedClusterSecurityProfile`.
- Removed class `ManagedClusterSecurityProfileAzureDefender`.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2022-06-01`.

## 2.16.0 (2022-06-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.15.0 (2022-05-25)

### Breaking Changes

- Removed unused class `AgentPoolsUpgradeNodeImageVersionResponse` and `AgentPoolsUpgradeNodeImageVersionHeaders`.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2022-04-01`.

## 2.14.0 (2022-04-11)

### Features Added

- Supported disabling Kubernetes Role-Based Access Control for `KubernetesCluster` during create.
- Supported enabling Azure AD integration for `KubernetesCluster`.
- Supported disabling local accounts for `KubernetesCluster`.
- Supported disk encryption set for `KubernetesCluster`.

### Bugs Fixed

- Fixed a bug that `orchestratorVersion` not initialized in agent pool.

### Other Changes

- Changed behavior that `KubernetesCluster` no longer retrieves admin and user KubeConfig during create, update, refresh.
- Changed behavior that Linux profile is not required for `KubernetesCluster` during create.

#### Dependency Updates

- Updated `api-version` to `2022-02-01`.

## 2.12.2 (2022-03-17)

### Other Changes

- Changed behavior that `KubernetesCluster` no longer retrieves admin and user KubeConfig during create, update, refresh.

## 2.13.0 (2022-03-11)

### Features Added

- Supported `format` parameter in listing user kube configs in `KubernetesCluster` and `KubernetesClusters`.

### Breaking Changes

- Renamed class `Ossku` to `OSSku`.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2022-01-01`.

## 2.13.0-beta.1 (2022-03-11)

### Features Added

- Supported `format` parameter in listing user kube configs in `KubernetesCluster` and `KubernetesClusters`.

### Breaking Changes

- Renamed class `Ossku` to `OSSku`.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2022-01-01`.

## 2.12.1 (2022-02-22)

### Bugs Fixed

- Fixed a bug that osDiskType on new agent pool is not set during `KubernetesCluster` update.

## 2.12.0 (2022-02-14)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources

## 2.11.0 (2022-01-17)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2021-10-01`.

## 2.10.0 (2021-11-22)

### Features Added

- Supported `tags` in `KubernetesClusterAgentPool` during create and update.

## 2.9.0 (2021-10-21)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2021-09-01`.

## 2.8.0 (2021-09-15)

### Dependency Updates

- Updated `api-version` to `2021-08-01`.

## 2.7.0 (2021-08-12)

### Dependency Updates

- Updated `api-version` of features to `2021-07-01`.

### Features Added

- Supported `start` and `stop` for `KubernetesCluster`.
- Supported `listOrchestrators` for `KubernetesCluster`.
- Supported `osDiskType` and `kubeletDiskType` for agent pool of `KubernetesCluster`.

### Breaking Changes

- Removed class `ManagedClusterIdentityUserAssignedIdentities`.
- Removed unused classes.

## 2.6.0 (2021-06-18)

- Updated `api-version` to `2021-05-01`
- Supported spot virtual machine for agent pool of `KubernetesCluster`.

## 2.5.0 (2021-05-28)
- Supported system-assigned managed identity and auto-scaler profile for `KubernetesCluster`.
- Supported auto-scaling, availability zones, node labels and taints for agent pool of `KubernetesCluster`.

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
