# Release History

## 2.58.0-beta.2 (2026-03-20)

### Breaking Changes

#### `models.OperationListResult` was removed

#### `models.AgentPoolBlueGreenUpgradeSettings` was removed

#### `models.UpgradeStrategy` was removed

#### `models.MeshMembershipProperties` was removed

#### `models.SeccompDefault` was removed

#### `models.JwtAuthenticatorProvisioningState` was removed

#### `models.ManagedClusterSecurityProfileImageIntegrity` was removed

#### `models.ManagedClusterListResult` was removed

#### `models.ComponentsByRelease` was removed

#### `models.VmState` was removed

#### `models.JwtAuthenticatorClaimMappings` was removed

#### `models.ManagedClusterSecurityProfileNodeRestriction` was removed

#### `models.AgentPoolListResult` was removed

#### `models.MachineOSProfileLinuxProfile` was removed

#### `models.GuardrailsAvailableVersionsProperties` was removed

#### `models.ManagedClusterSecurityProfileDefenderSecurityGating` was removed

#### `models.SchedulerInstanceProfile` was removed

#### `models.MachineOSProfile` was removed

#### `models.Operator` was removed

#### `models.ClusterServiceLoadBalancerHealthProbeMode` was removed

#### `models.ContainerServiceNetworkProfileKubeProxyConfigIpvsConfig` was removed

#### `models.GuardrailsSupport` was removed

#### `models.MeshMembershipsListResult` was removed

#### `models.SchedulerProfileSchedulerInstanceProfiles` was removed

#### `models.GuardrailsAvailableVersionsList` was removed

#### `models.SafeguardsAvailableVersionsList` was removed

#### `models.DriftAction` was removed

#### `models.IdentityBindingProperties` was removed

#### `models.SafeguardsSupport` was removed

#### `models.ManagedClusterAzureMonitorProfileContainerInsights` was removed

#### `models.InfrastructureEncryption` was removed

#### `models.SchedulerConfigMode` was removed

#### `models.SchedulerProfile` was removed

#### `models.MeshUpgradeProfileList` was removed

#### `models.MachineKubernetesProfile` was removed

#### `models.JwtAuthenticatorIssuer` was removed

#### `models.IdentityBindingOidcIssuerProfile` was removed

#### `models.ManagedClusterAzureMonitorProfileAppMonitoringOpenTelemetryMetrics` was removed

#### `models.TrustedAccessRoleListResult` was removed

#### `models.OperationStatusResultList` was removed

#### `models.JwtAuthenticatorExtraClaimMappingExpression` was removed

#### `models.SafeguardsAvailableVersionsProperties` was removed

#### `models.MachineSecurityProfile` was removed

#### `models.ManagedNamespaceListResult` was removed

#### `models.LabelSelectorRequirement` was removed

#### `models.TrustedAccessRoleBindingListResult` was removed

#### `models.ManagedClusterIngressProfileGatewayConfiguration` was removed

#### `models.ContainerServiceNetworkProfileKubeProxyConfig` was removed

#### `models.AgentPoolRecentlyUsedVersion` was removed

#### `models.MachineHardwareProfile` was removed

#### `models.IdentityBindingManagedIdentityProfile` was removed

#### `models.JwtAuthenticatorValidationRule` was removed

#### `models.DriverType` was removed

#### `models.ManagedClusterAzureMonitorProfileAppMonitoring` was removed

#### `models.ManagedClusterPropertiesForSnapshot` was removed

#### `models.MeshMembershipProvisioningState` was removed

#### `models.OutboundEnvironmentEndpointCollection` was removed

#### `models.IdentityBindingListResult` was removed

#### `models.ManagedClusterIngressDefaultDomainProfile` was removed

#### `models.IpvsScheduler` was removed

#### `models.ManagedClusterIngressProfileApplicationLoadBalancer` was removed

#### `models.AgentPoolArtifactStreamingProfile` was removed

#### `models.NodeCustomizationProfile` was removed

#### `models.LabelSelector` was removed

#### `models.KubernetesResourceObjectEncryptionProfile` was removed

#### `models.Component` was removed

#### `models.LoadBalancerListResult` was removed

#### `models.NodeImageVersionsListResult` was removed

#### `models.SnapshotListResult` was removed

#### `models.JwtAuthenticatorClaimMappingExpression` was removed

#### `models.MachineStatus` was removed

#### `models.AutoScaleProfile` was removed

#### `models.RebalanceLoadBalancersRequestBody` was removed

#### `models.ManagedClusterAzureMonitorProfileAppMonitoringOpenTelemetryLogs` was removed

#### `models.ManagedClusterSnapshotListResult` was removed

#### `models.IdentityBindingProvisioningState` was removed

#### `models.JwtAuthenticatorListResult` was removed

#### `models.ManagedClusterSecurityProfileDefenderSecurityGatingIdentitiesItem` was removed

#### `models.MeshRevisionProfileList` was removed

#### `models.MachineListResult` was removed

#### `models.Mode` was removed

#### `models.AddonAutoscaling` was removed

#### `models.NetworkProfileForSnapshot` was removed

#### `models.ManagedClusterAzureMonitorProfileAppMonitoringAutoInstrumentation` was removed

#### `models.ManagedClusterHostedSystemProfile` was removed

#### `models.MaintenanceConfigurationListResult` was removed

#### `models.ManagedGatewayType` was removed

#### `models.JwtAuthenticatorProperties` was removed

#### `models.PodLinkLocalAccess` was removed

#### `models.ScaleProfile` was modified

* `autoscale()` was removed
* `withAutoscale(models.AutoScaleProfile)` was removed

#### `models.ManagedClusterSecurityProfileDefender` was modified

* `withSecurityGating(models.ManagedClusterSecurityProfileDefenderSecurityGating)` was removed
* `securityGating()` was removed

#### `models.LocalDnsProfile` was modified

* `withVnetDnsOverrides(java.util.Map)` was removed
* `kubeDnsOverrides()` was removed
* `withKubeDnsOverrides(java.util.Map)` was removed
* `vnetDnsOverrides()` was removed

#### `models.ManagedClustersGetCommandResultHeaders` was modified

* `withLocation(java.lang.String)` was removed

#### `models.ManagedClusterPodIdentityProvisioningErrorBody` was modified

* `ManagedClusterPodIdentityProvisioningErrorBody()` was changed to private access
* `withDetails(java.util.List)` was removed
* `withMessage(java.lang.String)` was removed
* `withTarget(java.lang.String)` was removed
* `withCode(java.lang.String)` was removed

#### `models.ManagedClusterIngressProfile` was modified

* `withApplicationLoadBalancer(models.ManagedClusterIngressProfileApplicationLoadBalancer)` was removed
* `applicationLoadBalancer()` was removed
* `withGatewayApi(models.ManagedClusterIngressProfileGatewayConfiguration)` was removed
* `gatewayApi()` was removed

#### `models.ContainerServiceNetworkProfile` was modified

* `podLinkLocalAccess()` was removed
* `withPodLinkLocalAccess(models.PodLinkLocalAccess)` was removed
* `kubeProxyConfig()` was removed
* `withKubeProxyConfig(models.ContainerServiceNetworkProfileKubeProxyConfig)` was removed

#### `models.AgentPoolMode` was modified

* `MACHINES` was removed
* `MANAGED_SYSTEM` was removed

#### `models.ManagedClusterHttpProxyConfig` was modified

* `effectiveNoProxy()` was removed

#### `models.SnapshotType` was modified

* `MANAGED_CLUSTER` was removed

#### `models.AgentPoolAvailableVersionsPropertiesAgentPoolVersionsItem` was modified

* `AgentPoolAvailableVersionsPropertiesAgentPoolVersionsItem()` was changed to private access
* `withDefaultProperty(java.lang.Boolean)` was removed
* `withKubernetesVersion(java.lang.String)` was removed
* `withIsPreview(java.lang.Boolean)` was removed

#### `models.ManagedClusterWorkloadAutoScalerProfileVerticalPodAutoscaler` was modified

* `addonAutoscaling()` was removed
* `withAddonAutoscaling(models.AddonAutoscaling)` was removed

#### `models.ManagedClusterAgentPoolProfile` was modified

* `etag()` was removed
* `withUpgradeSettingsBlueGreen(models.AgentPoolBlueGreenUpgradeSettings)` was removed
* `withArtifactStreamingProfile(models.AgentPoolArtifactStreamingProfile)` was removed
* `withNodeInitializationTaints(java.util.List)` was removed
* `withNodeImageVersion(java.lang.String)` was removed
* `withNodeCustomizationProfile(models.NodeCustomizationProfile)` was removed
* `withLocalDnsProfile(models.LocalDnsProfile)` was removed
* `withUpgradeStrategy(models.UpgradeStrategy)` was removed

#### `models.ManagedClusterSecurityProfile` was modified

* `withKubernetesResourceObjectEncryptionProfile(models.KubernetesResourceObjectEncryptionProfile)` was removed
* `withImageIntegrity(models.ManagedClusterSecurityProfileImageIntegrity)` was removed
* `imageIntegrity()` was removed
* `nodeRestriction()` was removed
* `withNodeRestriction(models.ManagedClusterSecurityProfileNodeRestriction)` was removed
* `kubernetesResourceObjectEncryptionProfile()` was removed

#### `models.ManagedClusterStorageProfile` was modified

* `blobCsiDriver()` was removed
* `diskCsiDriver()` was removed
* `withDiskCsiDriver(models.ManagedClusterStorageProfileDiskCsiDriver)` was removed
* `withFileCsiDriver(models.ManagedClusterStorageProfileFileCsiDriver)` was removed
* `withBlobCsiDriver(models.ManagedClusterStorageProfileBlobCsiDriver)` was removed
* `fileCsiDriver()` was removed

#### `models.ManagedClusterIngressProfileWebAppRouting` was modified

* `defaultDomain()` was removed
* `withDefaultDomain(models.ManagedClusterIngressDefaultDomainProfile)` was removed

#### `models.ManagedClusterPodIdentityProvisioningInfo` was modified

* `ManagedClusterPodIdentityProvisioningInfo()` was changed to private access
* `withError(models.ManagedClusterPodIdentityProvisioningError)` was removed

#### `models.ManagedClusterLoadBalancerProfile` was modified

* `withClusterServiceLoadBalancerHealthProbeMode(models.ClusterServiceLoadBalancerHealthProbeMode)` was removed
* `outboundIPs()` was removed
* `managedOutboundIPs()` was removed
* `clusterServiceLoadBalancerHealthProbeMode()` was removed
* `withOutboundIPs(models.ManagedClusterLoadBalancerProfileOutboundIPs)` was removed
* `withManagedOutboundIPs(models.ManagedClusterLoadBalancerProfileManagedOutboundIPs)` was removed
* `effectiveOutboundIPs()` was removed

#### `models.ManagedClusterApiServerAccessProfile` was modified

* `withEnablePrivateClusterPublicFqdn(java.lang.Boolean)` was removed
* `enablePrivateClusterPublicFqdn()` was removed

#### `models.PublicNetworkAccess` was modified

* `SECURED_BY_PERIMETER` was removed

#### `models.MeshRevision` was modified

* `models.MeshRevision withCompatibleWith(java.util.List)` -> `models.MeshRevision withCompatibleWith(java.util.List)`
* `models.MeshRevision withUpgrades(java.util.List)` -> `models.MeshRevision withUpgrades(java.util.List)`
* `models.MeshRevision withRevision(java.lang.String)` -> `models.MeshRevision withRevision(java.lang.String)`

#### `models.AgentPoolSshAccess` was modified

* `ENTRA_ID` was removed

#### `models.KubernetesVersion` was modified

* `KubernetesVersion()` was changed to private access
* `withCapabilities(models.KubernetesVersionCapabilities)` was removed
* `withIsDefault(java.lang.Boolean)` was removed
* `withPatchVersions(java.util.Map)` was removed
* `withVersion(java.lang.String)` was removed
* `withIsPreview(java.lang.Boolean)` was removed

#### `models.EndpointDependency` was modified

* `EndpointDependency()` was changed to private access
* `withDomainName(java.lang.String)` was removed
* `withEndpointDetails(java.util.List)` was removed

#### `models.KubernetesPatchVersion` was modified

* `KubernetesPatchVersion()` was changed to private access
* `withUpgrades(java.util.List)` was removed

#### `models.ManagedClusterAddonProfileIdentity` was modified

* `ManagedClusterAddonProfileIdentity()` was changed to private access
* `withObjectId(java.lang.String)` was removed
* `withClientId(java.lang.String)` was removed
* `withResourceId(java.lang.String)` was removed

#### `models.AgentPoolSecurityProfile` was modified

* `enableVtpm()` was removed
* `withEnableVtpm(java.lang.Boolean)` was removed

#### `models.EndpointDetail` was modified

* `EndpointDetail()` was changed to private access
* `withProtocol(java.lang.String)` was removed
* `withPort(java.lang.Integer)` was removed
* `withDescription(java.lang.String)` was removed
* `withIpAddress(java.lang.String)` was removed

#### `models.ManagedClusterStorageProfileDiskCsiDriver` was modified

* `withVersion(java.lang.String)` was removed
* `version()` was removed

#### `models.MeshRevisionProfileProperties` was modified

* `MeshRevisionProfileProperties()` was changed to private access
* `withMeshRevisions(java.util.List)` was removed

#### `models.ManagedClusterPoolUpgradeProfileUpgradesItem` was modified

* `ManagedClusterPoolUpgradeProfileUpgradesItem()` was changed to private access
* `withIsPreview(java.lang.Boolean)` was removed
* `withIsOutOfSupport(java.lang.Boolean)` was removed
* `isOutOfSupport()` was removed
* `withKubernetesVersion(java.lang.String)` was removed

#### `models.ManagedClusterWindowsProfile` was modified

* `withEnableCsiProxy(java.lang.Boolean)` was removed
* `enableCsiProxy()` was removed

#### `models.MachineNetworkProperties` was modified

* `MachineNetworkProperties()` was changed to private access
* `withEnableNodePublicIp(java.lang.Boolean)` was removed
* `withPodSubnetId(java.lang.String)` was removed
* `withNodePublicIpPrefixId(java.lang.String)` was removed
* `nodePublicIpTags()` was removed
* `podSubnetId()` was removed
* `enableNodePublicIp()` was removed
* `nodePublicIpPrefixId()` was removed
* `vnetSubnetId()` was removed
* `withNodePublicIpTags(java.util.List)` was removed
* `withVnetSubnetId(java.lang.String)` was removed

#### `models.WorkloadRuntime` was modified

* `KATA_MSHV_VM_ISOLATION` was removed

#### `models.OSSku` was modified

* `MARINER` was removed
* `FLATCAR` was removed
* `WINDOWS2025` was removed
* `WINDOWS_ANNUAL` was removed

#### `models.AgentPoolUpgradeProfilePropertiesUpgradesItem` was modified

* `AgentPoolUpgradeProfilePropertiesUpgradesItem()` was changed to private access
* `withKubernetesVersion(java.lang.String)` was removed
* `withIsPreview(java.lang.Boolean)` was removed
* `withIsOutOfSupport(java.lang.Boolean)` was removed
* `isOutOfSupport()` was removed

#### `models.ManagedClusterLoadBalancerProfileOutboundIPs` was modified

* `withPublicIPs(java.util.List)` was removed
* `publicIPs()` was removed

#### `models.ManagedClusterPodIdentityProvisioningError` was modified

* `ManagedClusterPodIdentityProvisioningError()` was changed to private access
* `withError(models.ManagedClusterPodIdentityProvisioningErrorBody)` was removed

#### `models.MachineIpAddress` was modified

* `MachineIpAddress()` was changed to private access

#### `models.GpuProfile` was modified

* `driverType()` was removed
* `withDriverType(models.DriverType)` was removed

#### `models.KubernetesVersionCapabilities` was modified

* `KubernetesVersionCapabilities()` was changed to private access
* `withSupportPlan(java.util.List)` was removed

#### `models.AgentPoolUpgradeSettings` was modified

* `maxBlockedNodes()` was removed
* `withMaxBlockedNodes(java.lang.String)` was removed

#### `models.CompatibleVersions` was modified

* `CompatibleVersions()` was changed to private access
* `withName(java.lang.String)` was removed
* `withVersions(java.util.List)` was removed

#### `models.ManagedClusterAzureMonitorProfile` was modified

* `appMonitoring()` was removed
* `withContainerInsights(models.ManagedClusterAzureMonitorProfileContainerInsights)` was removed
* `containerInsights()` was removed
* `withAppMonitoring(models.ManagedClusterAzureMonitorProfileAppMonitoring)` was removed

#### `models.ManagedClusterNatGatewayProfile` was modified

* `effectiveOutboundIPs()` was removed

#### `models.MeshUpgradeProfileProperties` was modified

* `MeshUpgradeProfileProperties()` was changed to private access
* `withRevision(java.lang.String)` was removed
* `withCompatibleWith(java.util.List)` was removed
* `withUpgrades(java.util.List)` was removed

#### `models.ManagedClusterPoolUpgradeProfile` was modified

* `ManagedClusterPoolUpgradeProfile()` was changed to private access
* `withComponentsByReleases(java.util.List)` was removed
* `withOsType(models.OSType)` was removed
* `componentsByReleases()` was removed
* `withUpgrades(java.util.List)` was removed
* `withName(java.lang.String)` was removed
* `withKubernetesVersion(java.lang.String)` was removed

#### `models.TrustedAccessRoleRule` was modified

* `TrustedAccessRoleRule()` was changed to private access

#### `models.KubeletConfig` was modified

* `withSeccompDefault(models.SeccompDefault)` was removed
* `seccompDefault()` was removed

#### `models.MachineProperties` was modified

* `MachineProperties()` was changed to private access
* `withNetwork(models.MachineNetworkProperties)` was removed
* `mode()` was removed
* `security()` was removed
* `withOperatingSystem(models.MachineOSProfile)` was removed
* `priority()` was removed
* `withPriority(models.ScaleSetPriority)` was removed
* `withHardware(models.MachineHardwareProfile)` was removed
* `withNodeImageVersion(java.lang.String)` was removed
* `tags()` was removed
* `status()` was removed
* `kubernetes()` was removed
* `hardware()` was removed
* `operatingSystem()` was removed
* `withSecurity(models.MachineSecurityProfile)` was removed
* `etag()` was removed
* `nodeImageVersion()` was removed
* `provisioningState()` was removed
* `withTags(java.util.Map)` was removed
* `withKubernetes(models.MachineKubernetesProfile)` was removed
* `withMode(models.AgentPoolMode)` was removed

#### `models.CredentialResult` was modified

* `CredentialResult()` was changed to private access

### Features Added

#### `models.LocalDnsProfile` was modified

* `withKubeDNSOverrides(java.util.Map)` was added
* `withVnetDNSOverrides(java.util.Map)` was added
* `kubeDNSOverrides()` was added
* `vnetDNSOverrides()` was added

#### `models.ManagedClusterAgentPoolProfile` was modified

* `withLocalDNSProfile(models.LocalDnsProfile)` was added
* `nodeImageVersion()` was added
* `eTag()` was added

#### `models.ManagedClusterStorageProfile` was modified

* `withFileCSIDriver(models.ManagedClusterStorageProfileFileCsiDriver)` was added
* `withBlobCSIDriver(models.ManagedClusterStorageProfileBlobCsiDriver)` was added
* `diskCSIDriver()` was added
* `withDiskCSIDriver(models.ManagedClusterStorageProfileDiskCsiDriver)` was added
* `blobCSIDriver()` was added
* `fileCSIDriver()` was added

#### `models.ManagedClusterLoadBalancerProfile` was modified

* `outboundIps()` was added
* `managedOutboundIps()` was added
* `effectiveOutboundIps()` was added
* `withOutboundIps(models.ManagedClusterLoadBalancerProfileOutboundIPs)` was added
* `withManagedOutboundIps(models.ManagedClusterLoadBalancerProfileManagedOutboundIPs)` was added

#### `models.ManagedClusterApiServerAccessProfile` was modified

* `enablePrivateClusterPublicFQDN()` was added
* `withEnablePrivateClusterPublicFQDN(java.lang.Boolean)` was added

#### `models.AgentPoolSecurityProfile` was modified

* `withEnableVTPM(java.lang.Boolean)` was added
* `enableVTPM()` was added

#### `models.ManagedClusterWindowsProfile` was modified

* `withEnableCSIProxy(java.lang.Boolean)` was added
* `enableCSIProxy()` was added

#### `models.ManagedClusterLoadBalancerProfileOutboundIPs` was modified

* `withPublicIps(java.util.List)` was added
* `publicIps()` was added

#### `ContainerServiceManager` was modified

* `orchestratorClient()` was added
* `openShiftClient()` was added

#### `models.ManagedClusterNatGatewayProfile` was modified

* `effectiveOutboundIps()` was added

#### `models.MeshUpgradeProfileProperties` was modified

* `upgrades()` was added
* `compatibleWith()` was added
* `revision()` was added

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
