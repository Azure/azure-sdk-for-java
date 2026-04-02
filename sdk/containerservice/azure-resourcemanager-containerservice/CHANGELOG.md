# Release History

## 2.59.0-beta.1 (2026-03-30)

### Features Added

* `models.AgentPoolBlueGreenUpgradeSettings` was added

* `models.UpgradeStrategy` was added

* `models.MeshMembershipProperties` was added

* `models.SeccompDefault` was added

* `models.ManagedClusterSecurityProfileImageIntegrity` was added

* `models.MeshMembershipPrivateConnectProfile` was added

* `models.ComponentsByRelease` was added

* `models.VmState` was added

* `models.ResourceSkuCosts` was added

* `models.ManagedClusterSecurityProfileNodeRestriction` was added

* `models.ResourceSkuRestrictionsType` was added

* `models.MigStrategy` was added

* `models.MachineOSProfileLinuxProfile` was added

* `models.GuardrailsAvailableVersionsProperties` was added

* `models.ManagedClusterSecurityProfileDefenderSecurityGating` was added

* `models.ResourceSkuRestrictions` was added

* `models.SchedulerInstanceProfile` was added

* `models.MachineOSProfile` was added

* `models.Operator` was added

* `models.ClusterServiceLoadBalancerHealthProbeMode` was added

* `models.ManagedClusterNATGatewayProfileOutboundIPs` was added

* `models.ResourceSkuCapacity` was added

* `models.JWTAuthenticatorExtraClaimMappingExpression` was added

* `models.ContainerServiceNetworkProfileKubeProxyConfigIpvsConfig` was added

* `models.GuardrailsSupport` was added

* `models.SchedulerProfileSchedulerInstanceProfiles` was added

* `models.ServiceAccountImagePullProfile` was added

* `models.MachineBillingProfile` was added

* `models.ResourceSkuRestrictionInfo` was added

* `models.DriftAction` was added

* `models.IdentityBindingProperties` was added

* `models.NvidiaGPUProfile` was added

* `models.SafeguardsSupport` was added

* `models.ManagedClusterAzureMonitorProfileContainerInsights` was added

* `models.InfrastructureEncryption` was added

* `models.SchedulerConfigMode` was added

* `models.SchedulerProfile` was added

* `models.ResourceSkuCapabilities` was added

* `models.MachineKubernetesProfile` was added

* `models.ManagedClusterHealthMonitorProfile` was added

* `models.IdentityBindingOidcIssuerProfile` was added

* `models.ManagedClusterAzureMonitorProfileAppMonitoringOpenTelemetryMetrics` was added

* `models.ContainerNetworkLogs` was added

* `models.JWTAuthenticatorProperties` was added

* `models.JWTAuthenticatorClaimMappings` was added

* `models.ManagedClusterNATGatewayProfileOutboundIpPrefixes` was added

* `models.SafeguardsAvailableVersionsProperties` was added

* `models.MachineSecurityProfile` was added

* `models.LabelSelectorRequirement` was added

* `models.GatewayApiIstioEnabled` was added

* `models.ManagedClusterAppRoutingIstio` was added

* `models.ManagedClusterIngressProfileGatewayConfiguration` was added

* `models.ContainerServiceNetworkProfileKubeProxyConfig` was added

* `models.AgentPoolRecentlyUsedVersion` was added

* `models.ResourceSkuLocationInfo` was added

* `models.MachineHardwareProfile` was added

* `models.ResourceSkuRestrictionsReasonCode` was added

* `models.IdentityBindingManagedIdentityProfile` was added

* `models.JWTAuthenticatorIssuer` was added

* `models.DriverType` was added

* `models.ManagedClusterAzureMonitorProfileAppMonitoring` was added

* `models.ManagementMode` was added

* `models.JWTAuthenticatorValidationRule` was added

* `models.ManagedClusterPropertiesForSnapshot` was added

* `models.ManagedClusterWebAppRoutingGatewayApiImplementations` was added

* `models.ResourceSkuZoneDetails` was added

* `models.MeshMembershipProvisioningState` was added

* `models.ResourceSkuCapacityScaleType` was added

* `models.ManagedClusterIngressDefaultDomainProfile` was added

* `models.IpvsScheduler` was added

* `models.ManagedClusterIngressProfileApplicationLoadBalancer` was added

* `models.AgentPoolArtifactStreamingProfile` was added

* `models.NodeCustomizationProfile` was added

* `models.LabelSelector` was added

* `models.KubernetesResourceObjectEncryptionProfile` was added

* `models.Component` was added

* `models.JWTAuthenticatorClaimMappingExpression` was added

* `models.MachineStatus` was added

* `models.AutoScaleProfile` was added

* `models.RebalanceLoadBalancersRequestBody` was added

* `models.ManagedClusterAzureMonitorProfileAppMonitoringOpenTelemetryLogs` was added

* `models.IdentityBindingProvisioningState` was added

* `models.ManagedClusterSecurityProfileDefenderSecurityGatingIdentitiesItem` was added

* `models.Mode` was added

* `models.AddonAutoscaling` was added

* `models.NetworkProfileForSnapshot` was added

* `models.ManagedClusterAzureMonitorProfileAppMonitoringAutoInstrumentation` was added

* `models.ManagedClusterHostedSystemProfile` was added

* `models.JWTAuthenticatorProvisioningState` was added

* `models.ManagedGatewayType` was added

* `models.PodLinkLocalAccess` was added

#### `models.ScaleProfile` was modified

* `withAutoscale(models.AutoScaleProfile)` was added
* `autoscale()` was added

#### `models.ManagedClusterSecurityProfileDefender` was modified

* `securityGating()` was added
* `withSecurityGating(models.ManagedClusterSecurityProfileDefenderSecurityGating)` was added

#### `models.ManagedClusterIngressProfile` was modified

* `withApplicationLoadBalancer(models.ManagedClusterIngressProfileApplicationLoadBalancer)` was added
* `applicationLoadBalancer()` was added
* `withGatewayApi(models.ManagedClusterIngressProfileGatewayConfiguration)` was added
* `gatewayApi()` was added

#### `models.ContainerServiceNetworkProfile` was modified

* `kubeProxyConfig()` was added
* `withPodLinkLocalAccess(models.PodLinkLocalAccess)` was added
* `withKubeProxyConfig(models.ContainerServiceNetworkProfileKubeProxyConfig)` was added
* `podLinkLocalAccess()` was added

#### `models.AgentPoolMode` was modified

* `MACHINES` was added
* `MANAGED_SYSTEM` was added

#### `models.ManagedClusterHttpProxyConfig` was modified

* `effectiveNoProxy()` was added

#### `models.SnapshotType` was modified

* `MANAGED_CLUSTER` was added

#### `models.OutboundType` was modified

* `MANAGED_NATGATEWAY_V2` was added

#### `models.ManagedClusterWorkloadAutoScalerProfileVerticalPodAutoscaler` was modified

* `withAddonAutoscaling(models.AddonAutoscaling)` was added
* `addonAutoscaling()` was added

#### `models.ManagedClusterAgentPoolProfile` was modified

* `withArtifactStreamingProfile(models.AgentPoolArtifactStreamingProfile)` was added
* `withNodeCustomizationProfile(models.NodeCustomizationProfile)` was added
* `withUpgradeStrategy(models.UpgradeStrategy)` was added
* `withNodeInitializationTaints(java.util.List)` was added
* `withEnableOSDiskFullCaching(java.lang.Boolean)` was added
* `withUpgradeSettingsBlueGreen(models.AgentPoolBlueGreenUpgradeSettings)` was added

#### `models.ManagedClusterSecurityProfile` was modified

* `withNodeRestriction(models.ManagedClusterSecurityProfileNodeRestriction)` was added
* `nodeRestriction()` was added
* `withServiceAccountImagePullProfile(models.ServiceAccountImagePullProfile)` was added
* `imageIntegrity()` was added
* `kubernetesResourceObjectEncryptionProfile()` was added
* `withImageIntegrity(models.ManagedClusterSecurityProfileImageIntegrity)` was added
* `serviceAccountImagePullProfile()` was added
* `withKubernetesResourceObjectEncryptionProfile(models.KubernetesResourceObjectEncryptionProfile)` was added

#### `models.ManagedClusterIngressProfileWebAppRouting` was modified

* `withGatewayApiImplementations(models.ManagedClusterWebAppRoutingGatewayApiImplementations)` was added
* `gatewayApiImplementations()` was added
* `withDefaultDomain(models.ManagedClusterIngressDefaultDomainProfile)` was added
* `defaultDomain()` was added

#### `models.ManagedClusterLoadBalancerProfile` was modified

* `withClusterServiceLoadBalancerHealthProbeMode(models.ClusterServiceLoadBalancerHealthProbeMode)` was added
* `clusterServiceLoadBalancerHealthProbeMode()` was added

#### `models.PublicNetworkAccess` was modified

* `SECURED_BY_PERIMETER` was added

#### `models.AgentPoolSshAccess` was modified

* `ENTRA_ID` was added

#### `models.TransitEncryptionType` was modified

* `M_TLS` was added

#### `models.ManagedClusterStorageProfileDiskCsiDriver` was modified

* `withVersion(java.lang.String)` was added
* `version()` was added

#### `models.ManagedClusterPoolUpgradeProfileUpgradesItem` was modified

* `isOutOfSupport()` was added

#### `models.ManagedClusterManagedOutboundIpProfile` was modified

* `withCountIPv6(java.lang.Integer)` was added
* `countIPv6()` was added

#### `models.MachineNetworkProperties` was modified

* `nodePublicIpTags()` was added
* `podSubnetId()` was added
* `nodePublicIpPrefixId()` was added
* `enableNodePublicIp()` was added
* `vnetSubnetId()` was added

#### `models.WorkloadRuntime` was modified

* `KATA_MSHV_VM_ISOLATION` was added

#### `models.OSSku` was modified

* `MARINER` was added
* `FLATCAR` was added
* `WINDOWS2025` was added
* `WINDOWS_ANNUAL` was added

#### `models.AgentPoolUpgradeProfilePropertiesUpgradesItem` was modified

* `isOutOfSupport()` was added

#### `models.GpuProfile` was modified

* `nvidia()` was added
* `driverType()` was added
* `withNvidia(models.NvidiaGPUProfile)` was added
* `withDriverType(models.DriverType)` was added

#### `models.AgentPoolUpgradeSettings` was modified

* `maxBlockedNodes()` was added
* `withMaxBlockedNodes(java.lang.String)` was added

#### `models.ManagedClusterAzureMonitorProfile` was modified

* `containerInsights()` was added
* `appMonitoring()` was added
* `withAppMonitoring(models.ManagedClusterAzureMonitorProfileAppMonitoring)` was added
* `withContainerInsights(models.ManagedClusterAzureMonitorProfileContainerInsights)` was added

#### `models.ManagedClusterNatGatewayProfile` was modified

* `withOutboundIps(models.ManagedClusterNATGatewayProfileOutboundIPs)` was added
* `outboundIps()` was added
* `withOutboundIpPrefixes(models.ManagedClusterNATGatewayProfileOutboundIpPrefixes)` was added
* `outboundIpPrefixes()` was added

#### `models.ManagedClusterPoolUpgradeProfile` was modified

* `componentsByReleases()` was added

#### `models.KubeletConfig` was modified

* `seccompDefault()` was added
* `withSeccompDefault(models.SeccompDefault)` was added

#### `models.MachineProperties` was modified

* `status()` was added
* `hardware()` was added
* `security()` was added
* `priority()` was added
* `localDNSProfile()` was added
* `evictionPolicy()` was added
* `withTags(java.util.Map)` was added
* `operatingSystem()` was added
* `eTag()` was added
* `withKubernetes(models.MachineKubernetesProfile)` was added
* `billing()` was added
* `withOperatingSystem(models.MachineOSProfile)` was added
* `withPriority(models.ScaleSetPriority)` was added
* `withEvictionPolicy(models.ScaleSetEvictionPolicy)` was added
* `withSecurity(models.MachineSecurityProfile)` was added
* `nodeImageVersion()` was added
* `provisioningState()` was added
* `mode()` was added
* `withHardware(models.MachineHardwareProfile)` was added
* `tags()` was added
* `withBilling(models.MachineBillingProfile)` was added
* `withMode(models.AgentPoolMode)` was added
* `kubernetes()` was added
* `withLocalDNSProfile(models.LocalDnsProfile)` was added

## 2.58.0 (2026-03-20)

### Breaking Changes

- Moved `ContainerServiceManager.serviceClient().getOpenShiftManagedClusters()` to `ContainerServiceManager.openShiftClient().getOpenShiftManagedClusters()`.
- Moved `ContainerServiceManager.serviceClient().getContainerServices()` to `ContainerServiceManager.orchestratorClient().getContainerServices()`.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2026-01-01`.

## 2.57.1 (2026-01-29)

### Other Changes

#### Dependency Updates

- Upgraded core dependencies.

## 2.58.0-beta.1 (2025-12-15)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2025-10-02-preview`.

## 2.57.0 (2025-12-15)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2025-10-01`.

## 2.57.0-beta.1 (2025-12-01)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2025-09-01-preview`.

## 2.56.1 (2025-11-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.56.0 (2025-11-10)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2025-09-01`.

## 2.55.1 (2025-10-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.56.0-beta.1 (2025-10-15)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2025-08-02-preview`.

## 2.55.0 (2025-10-13)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2025-08-01`.

## 2.54.1 (2025-09-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.54.0 (2025-09-23)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2025-07-01`.

## 2.53.2 (2025-08-27)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.53.1 (2025-08-05)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.53.0 (2025-07-25)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2025-05-01`.

## 2.52.0 (2025-06-27)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2025-04-01`.

## 2.51.0 (2025-05-26)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.50.0 (2025-04-24)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2025-02-01`.

## 2.49.0 (2025-03-28)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2025-01-01`.

## 2.48.0 (2025-02-28)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2024-10-01`.

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

- Updated `api-version` to `2024-09-01`.

## 2.44.0 (2024-10-25)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2024-08-01`.

## 2.43.0 (2024-09-27)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2024-07-01`.

## 2.42.0 (2024-08-23)

### Other Changes

- Replaced `Jackson` with `azure-json` for serialization/deserialization.

## 2.41.0 (2024-07-25)

### Breaking Changes

- Removed non-functional API `getOsOptions` and related models `OSOptionProperty`.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2024-05-01`.

## 2.40.0 (2024-06-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.39.0 (2024-05-24)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.38.0 (2024-04-16)

### Features Added

- Supported disabling public network access in `KubernetesCluster` via `disablePublicNetworkAccess()`, for private link feature.
- Supported specifying network mode of Azure CNI configuration for `KubernetesCluster` during create.
- Supported specifying network plugin mode for `KubernetesCluster` during create.
- Supported specifying network data plane for `KubernetesCluster` during create.

### Breaking Changes

- `nodeSelector` is removed from `IstioEgressGateway` class.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2024-02-01`.

## 2.37.0 (2024-03-22)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.36.0 (2024-02-29)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2024-01-01`.

## 2.35.0 (2024-01-26)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2023-11-01`.

## 2.34.0 (2023-12-22)

### Other Changes

#### Dependency Updates

- Updated core dependency from resources.

## 2.33.0 (2023-11-24)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2023-10-01`.

## 2.32.0 (2023-10-27)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2023-09-01`.

## 2.31.0 (2023-09-28)

### Features Added

- Supported specifying the resource group for agent pool nodes when creating `KubernetesCluster`.

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2023-08-01`.

## 2.30.0 (2023-08-25)

### Other Changes

#### Dependency Updates

- Updated `api-version` to `2023-07-01`.

## 2.29.0 (2023-07-28)

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
