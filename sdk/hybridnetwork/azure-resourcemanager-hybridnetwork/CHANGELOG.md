# Release History

## 1.1.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0 (2023-11-20)

- Azure Resource Manager HybridNetwork client library for Java. This package contains Microsoft Azure SDK for HybridNetwork Management SDK. The definitions in this swagger specification will be used to manage the Hybrid Network resources. Package tag package-2023-09-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.Device$UpdateStages` was removed

* `models.VendorSkus` was removed

* `models.Vendor` was removed

* `models.PreviewSubscriptionsList` was removed

* `models.NetworkFunctionVendor` was removed

* `models.NetworkFunctionSkuRoleDetails` was removed

* `models.Vendor$DefinitionStages` was removed

* `models.ImageReference` was removed

* `models.RoleInstances` was removed

* `models.Device$DefinitionStages` was removed

* `models.DevicePropertiesFormat` was removed

* `models.Device` was removed

* `models.SkuOverview` was removed

* `models.DataDisk` was removed

* `models.OperationalState` was removed

* `models.LinuxConfiguration` was removed

* `models.VendorSku$UpdateStages` was removed

* `models.PreviewSubscription$UpdateStages` was removed

* `models.PreviewSubscription$Update` was removed

* `models.PreviewSubscription$DefinitionStages` was removed

* `models.Vendor$UpdateStages` was removed

* `models.CustomProfile` was removed

* `models.AzureStackEdgeFormat` was removed

* `models.OsProfile` was removed

* `models.NetworkFunctionRoleInstanceListResult` was removed

* `models.VendorSku$Update` was removed

* `models.NetworkFunctionRoleConfigurationType` was removed

* `models.VendorNetworkFunction$Definition` was removed

* `models.VirtualMachineSizeTypes` was removed

* `models.DeviceRegistrationKey` was removed

* `models.Devices` was removed

* `models.NetworkFunctionRoleConfiguration` was removed

* `models.VirtualHardDisk` was removed

* `models.NetworkFunctionUserConfigurationOsProfile` was removed

* `models.Vendor$Definition` was removed

* `models.SkuType` was removed

* `models.VendorListResult` was removed

* `models.PreviewSubscription` was removed

* `models.VendorProvisioningState` was removed

* `models.Vendor$Update` was removed

* `models.NetworkFunctionSkuListResult` was removed

* `models.VendorSku$DefinitionStages` was removed

* `models.OperationList` was removed

* `models.NetworkFunctionTemplate` was removed

* `models.NetworkFunctionUserConfiguration` was removed

* `models.Device$Definition` was removed

* `models.NetworkInterface` was removed

* `models.StorageProfile` was removed

* `models.VendorSku$Definition` was removed

* `models.NetworkFunctionVendorListResult` was removed

* `models.SshPublicKey` was removed

* `models.Vendors` was removed

* `models.DeviceListResult` was removed

* `models.SkuDeploymentMode` was removed

* `models.VendorSkuListResult` was removed

* `models.NetworkFunctionVendors` was removed

* `models.IpAllocationMethod` was removed

* `models.RoleInstance` was removed

* `models.Device$Update` was removed

* `models.VendorSku` was removed

* `models.VendorSkuPreviews` was removed

* `models.VendorNetworkFunctionListResult` was removed

* `models.NetworkFunctionVendorConfiguration` was removed

* `models.DeviceType` was removed

* `models.VendorNetworkFunction$DefinitionStages` was removed

* `models.VendorNetworkFunction$Update` was removed

* `models.DiskCreateOptionTypes` was removed

* `models.IpVersion` was removed

* `models.VendorNetworkFunction` was removed

* `models.VendorNetworkFunction$UpdateStages` was removed

* `models.OperatingSystemTypes` was removed

* `models.SshConfiguration` was removed

* `models.NetworkInterfaceIpConfiguration` was removed

* `models.PreviewSubscription$Definition` was removed

* `models.NetworkFunctionVendorSkus` was removed

* `models.OsDisk` was removed

* `models.VMSwitchType` was removed

* `models.NetworkFunctionSkuDetails` was removed

* `models.VendorNetworkFunctions` was removed

#### `models.NetworkFunction` was modified

* `vendorProvisioningState()` was removed
* `provisioningState()` was removed
* `vendorName()` was removed
* `networkFunctionContainerConfigurations()` was removed
* `networkFunctionUserConfigurations()` was removed
* `serviceKey()` was removed
* `managedApplicationParameters()` was removed
* `managedApplication()` was removed
* `skuName()` was removed
* `skuType()` was removed
* `device()` was removed

#### `models.OperationDisplay` was modified

* `withOperation(java.lang.String)` was removed
* `withProvider(java.lang.String)` was removed
* `withDescription(java.lang.String)` was removed
* `withResource(java.lang.String)` was removed

#### `HybridNetworkManager` was modified

* `networkFunctionVendorSkus()` was removed
* `roleInstances()` was removed
* `devices()` was removed
* `vendorNetworkFunctions()` was removed
* `networkFunctionVendors()` was removed
* `vendors()` was removed
* `vendorSkuPreviews()` was removed
* `vendorSkus()` was removed

#### `models.NetworkFunction$Definition` was modified

* `withSkuName(java.lang.String)` was removed
* `withManagedApplicationParameters(java.lang.Object)` was removed
* `withNetworkFunctionContainerConfigurations(java.lang.Object)` was removed
* `withNetworkFunctionUserConfigurations(java.util.List)` was removed
* `withDevice(com.azure.core.management.SubResource)` was removed
* `withVendorName(java.lang.String)` was removed

### Features Added

* `models.PodEventType` was added

* `models.NetworkFunctionDefinitionVersion$DefinitionStages` was added

* `models.DeploymentResourceIdReference` was added

* `models.NetworkServiceDesignGroup$DefinitionStages` was added

* `models.AzureOperatorNexusNetworkFunctionArmTemplateApplication` was added

* `models.ArtifactStores` was added

* `models.ArtifactManifest$UpdateStages` was added

* `models.NetworkServiceDesignGroupListResult` was added

* `models.ArtifactState` was added

* `models.VersionState` was added

* `models.VirtualNetworkFunctionNfviType` was added

* `models.Publisher$Definition` was added

* `models.ResourceElementTemplate` was added

* `models.PodEvent` was added

* `models.ArtifactStore$Definition` was added

* `models.AzureOperatorNexusNetworkFunctionApplication` was added

* `models.ProxyArtifactOverviewListResult` was added

* `models.NetworkFunctionDefinitionVersion` was added

* `models.NetworkFunctionDefinitionGroup$Update` was added

* `models.IdType` was added

* `models.NetworkFunctionApplication` was added

* `models.NetworkServiceDesignGroup$UpdateStages` was added

* `models.SkuName` was added

* `models.AzureOperatorNexusNetworkFunctionTemplate` was added

* `models.ConfigurationGroupSchemaListResult` was added

* `models.ArtifactManifestUpdateState` was added

* `models.NfvIs` was added

* `models.ArtifactAccessCredential` was added

* `models.PublisherScope` was added

* `models.ArtifactStore$UpdateStages` was added

* `models.AzureCoreNetworkFunctionArmTemplateApplication` was added

* `models.NetworkServiceDesignGroups` was added

* `models.AzureArcKubernetesArtifactType` was added

* `models.SiteListResult` was added

* `models.AzureCoreNetworkFunctionVhdApplication` was added

* `models.AzureCoreNetworkFunctionApplication` was added

* `models.AzureArcKubernetesArtifactProfile` was added

* `models.HttpMethod` was added

* `models.TemplateType` was added

* `models.ArtifactManifest$Update` was added

* `models.AzureCoreVhdImageDeployMappingRuleProfile` was added

* `models.ArtifactStoreType` was added

* `models.Site$Update` was added

* `models.AzureArcK8SClusterNfviDetails` was added

* `models.ArtifactChangeStateProperties` was added

* `models.ManagedServiceIdentity` was added

* `models.AzureStorageAccountContainerCredential` was added

* `models.Publishers` was added

* `models.Deployment` was added

* `models.ArtifactStore$Update` was added

* `models.ArtifactManifest$Definition` was added

* `models.ArmTemplateMappingRuleProfile` was added

* `models.AzureOperatorNexusImageArtifactProfile` was added

* `models.ApplicationEnablement` was added

* `models.NetworkFunctionDefinitionVersion$Definition` was added

* `models.SiteNetworkService$UpdateStages` was added

* `models.StatefulSet` was added

* `models.Origin` was added

* `models.ArmTemplateArtifactProfile` was added

* `models.ContainerizedNetworkFunctionDefinitionVersion` was added

* `models.HelmMappingRuleProfile` was added

* `models.SitePropertiesFormat` was added

* `models.Sites` was added

* `models.HelmInstallOptions` was added

* `models.ManagedResourceGroupConfiguration` was added

* `models.ArtifactProfile` was added

* `models.ConfigurationGroupValue$DefinitionStages` was added

* `models.ArmResourceDefinitionResourceElementTemplate` was added

* `models.NetworkServiceDesignGroup` was added

* `models.ArtifactType` was added

* `models.ArtifactStore` was added

* `models.Site$DefinitionStages` was added

* `models.ArtifactStore$DefinitionStages` was added

* `models.ComponentListResult` was added

* `models.ArtifactManifestListResult` was added

* `models.NetworkFunctionDefinitionGroup$DefinitionStages` was added

* `models.Site$Definition` was added

* `models.NfviType` was added

* `models.NsdArtifactProfile` was added

* `models.ArmResourceDefinitionResourceElementTemplateDetails` was added

* `models.NetworkFunctionDefinitionGroup$Definition` was added

* `models.VhdImageArtifactProfile` was added

* `models.ComponentProperties` was added

* `models.ArtifactManifest` was added

* `models.AzureCoreArmTemplateArtifactProfile` was added

* `models.AzureStorageAccountCredential` was added

* `models.AzureCoreVhdImageArtifactProfile` was added

* `models.SiteNetworkService$Definition` was added

* `models.NetworkServiceDesignVersion$Definition` was added

* `models.ConfigurationGroupValueListResult` was added

* `models.Type` was added

* `models.ConfigurationGroupValue` was added

* `models.NetworkFunctionPropertiesFormat` was added

* `models.UserAssignedIdentity` was added

* `models.NetworkServiceDesignVersionPropertiesFormat` was added

* `models.OpenDeploymentResourceReference` was added

* `models.PublisherListResult` was added

* `models.HelmUpgradeOptions` was added

* `models.Site` was added

* `models.Pod` was added

* `models.ConfigurationGroupValue$Definition` was added

* `models.DeploymentStatusProperties` was added

* `models.AzureCoreNfviDetails` was added

* `models.NetworkFunctionDefinitionGroupPropertiesFormat` was added

* `models.Site$UpdateStages` was added

* `models.Publisher` was added

* `models.SiteNetworkService$DefinitionStages` was added

* `models.ConfigurationGroupSchemaPropertiesFormat` was added

* `models.ManifestArtifactFormat` was added

* `models.ConfigurationGroupSchema$Update` was added

* `models.PodStatus` was added

* `models.ConfigurationGroupValuePropertiesFormat` was added

* `models.NetworkServiceDesignVersion$DefinitionStages` was added

* `models.ProxyArtifactVersionsListOverview` was added

* `models.AzureOperatorNexusArtifactType` was added

* `models.Publisher$UpdateStages` was added

* `models.NetworkFunctionDefinitionVersionPropertiesFormat` was added

* `models.ContainerizedNetworkFunctionNfviType` was added

* `models.NetworkServiceDesignGroupPropertiesFormat` was added

* `models.AzureCoreNetworkFunctionTemplate` was added

* `models.ConfigurationGroupSchemaVersionUpdateState` was added

* `models.NfviDetails` was added

* `models.PublisherPropertiesFormat` was added

* `models.ArtifactChangeState` was added

* `models.NetworkServiceDesignVersionListResult` was added

* `models.ProxyArtifactListOverview` was added

* `models.VirtualNetworkFunctionDefinitionVersion` was added

* `models.OperationListResult` was added

* `models.SiteNetworkService$Update` was added

* `models.ProxyArtifactOverviewPropertiesValue` was added

* `models.Publisher$Update` was added

* `models.Resources` was added

* `models.ReplicaSet` was added

* `models.NetworkFunctionDefinitionVersionUpdateState` was added

* `models.HelmMappingRuleProfileOptions` was added

* `models.CredentialType` was added

* `models.NetworkServiceDesignVersion$UpdateStages` was added

* `models.VhdImageMappingRuleProfile` was added

* `models.NetworkFunctionDefinitionGroup` was added

* `models.Components` was added

* `models.AzureCoreArtifactType` was added

* `models.DependsOnProfile` was added

* `models.ArtifactReplicationStrategy` was added

* `models.NetworkFunctionDefinitionVersion$UpdateStages` was added

* `models.SiteNetworkServices` was added

* `models.ArtifactStorePropertiesFormatManagedResourceGroupConfiguration` was added

* `models.NetworkFunctionValueWithoutSecrets` was added

* `models.NetworkFunctionDefinitionVersion$Update` was added

* `models.RequestMetadata` was added

* `models.ExecuteRequestParameters` was added

* `models.NetworkFunctionDefinitionGroup$UpdateStages` was added

* `models.ContainerizedNetworkFunctionTemplate` was added

* `models.ConfigurationGroupSchema$Definition` was added

* `models.MappingRuleProfile` was added

* `models.Publisher$DefinitionStages` was added

* `models.Sku` was added

* `models.AzureCoreArmTemplateDeployMappingRuleProfile` was added

* `models.SkuTier` was added

* `models.NetworkFunctionDefinitionVersions` was added

* `models.ArtifactManifestState` was added

* `models.ConfigurationValueWithSecrets` was added

* `models.SiteNetworkService` was added

* `models.NetworkServiceDesignVersionUpdateState` was added

* `models.AzureOperatorNexusArmTemplateDeployMappingRuleProfile` was added

* `models.ConfigurationValueWithoutSecrets` was added

* `models.AzureArcKubernetesDeployMappingRuleProfile` was added

* `models.ArtifactStorePropertiesFormat` was added

* `models.NetworkServiceDesignGroup$Definition` was added

* `models.AzureArcKubernetesNetworkFunctionApplication` was added

* `models.ConfigurationGroupSchema$DefinitionStages` was added

* `models.AzureOperatorNexusImageDeployMappingRuleProfile` was added

* `models.NetworkServiceDesignGroup$Update` was added

* `models.ConfigurationGroupValue$UpdateStages` was added

* `models.NetworkFunctionValueWithSecrets` was added

* `models.DaemonSet` was added

* `models.VirtualNetworkFunctionTemplate` was added

* `models.ConfigurationGroupSchema` was added

* `models.ArtifactManifestPropertiesFormat` was added

* `models.AzureArcKubernetesNetworkFunctionTemplate` was added

* `models.ActionType` was added

* `models.ProxyArtifacts` was added

* `models.ProxyArtifactVersionsOverviewListResult` was added

* `models.Component` was added

* `models.NetworkServiceDesignVersion$Update` was added

* `models.ReferencedResource` was added

* `models.AzureOperatorNexusNetworkFunctionImageApplication` was added

* `models.NetworkFunctionDefinitionVersionListResult` was added

* `models.ConfigurationGroupSchemas` was added

* `models.AzureOperatorNexusArmTemplateArtifactProfile` was added

* `models.ConfigurationGroupValueConfigurationType` was added

* `models.AzureOperatorNexusClusterNfviDetails` was added

* `models.NetworkServiceDesignVersion` was added

* `models.SiteNetworkServiceListResult` was added

* `models.SiteNetworkServicePropertiesFormat` was added

* `models.HelmArtifactProfile` was added

* `models.ConfigurationGroupSchema$UpdateStages` was added

* `models.ManagedServiceIdentityType` was added

* `models.SecretDeploymentResourceReference` was added

* `models.NetworkFunctionConfigurationType` was added

* `models.ImageMappingRuleProfile` was added

* `models.NetworkFunctionDefinitionGroupListResult` was added

* `models.ArtifactManifest$DefinitionStages` was added

* `models.ConfigurationGroupValue$Update` was added

* `models.ArtifactManifests` was added

* `models.ConfigurationGroupValues` was added

* `models.ImageArtifactProfile` was added

* `models.ArtifactStoreListResult` was added

* `models.NetworkFunctionDefinitionGroups` was added

* `models.AzureContainerRegistryScopedTokenCredential` was added

* `models.NetworkFunctionDefinitionResourceElementTemplateDetails` was added

* `models.AzureArcKubernetesHelmApplication` was added

* `models.NetworkServiceDesignVersions` was added

#### `models.Operation` was modified

* `actionType()` was added
* `isDataAction()` was added
* `origin()` was added

#### `models.NetworkFunction` was modified

* `executeRequest(models.ExecuteRequestParameters,com.azure.core.util.Context)` was added
* `identity()` was added
* `properties()` was added
* `executeRequest(models.ExecuteRequestParameters)` was added

#### `HybridNetworkManager` was modified

* `configurationGroupValues()` was added
* `sites()` was added
* `publishers()` was added
* `artifactStores()` was added
* `siteNetworkServices()` was added
* `components()` was added
* `networkFunctionDefinitionGroups()` was added
* `proxyArtifacts()` was added
* `networkServiceDesignGroups()` was added
* `networkFunctionDefinitionVersions()` was added
* `artifactManifests()` was added
* `configurationGroupSchemas()` was added
* `networkServiceDesignVersions()` was added

#### `models.NetworkFunctions` was modified

* `executeRequest(java.lang.String,java.lang.String,models.ExecuteRequestParameters,com.azure.core.util.Context)` was added
* `executeRequest(java.lang.String,java.lang.String,models.ExecuteRequestParameters)` was added

#### `models.NetworkFunction$Definition` was modified

* `withProperties(models.NetworkFunctionPropertiesFormat)` was added
* `withIdentity(models.ManagedServiceIdentity)` was added

## 1.0.0-beta.2 (2022-09-30)

- Azure Resource Manager HybridNetwork client library for Java. This package contains Microsoft Azure SDK for HybridNetwork Management SDK. The definitions in this swagger specification will be used to manage the Hybrid Network resources. Package tag package-2021-05-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Device$Definition` was modified

* `withAzureStackEdge(com.azure.core.management.SubResource)` was removed

#### `models.Device` was modified

* `status()` was removed
* `provisioningState()` was removed
* `azureStackEdge()` was removed
* `networkFunctions()` was removed

### Features Added

* `models.AzureStackEdgeFormat` was added

* `models.DevicePropertiesFormat` was added

#### `models.Device$Definition` was modified

* `withProperties(models.DevicePropertiesFormat)` was added

#### `models.Device` was modified

* `properties()` was added
* `resourceGroupName()` was added

#### `HybridNetworkManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `HybridNetworkManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.NetworkFunction` was modified

* `resourceGroupName()` was added

## 1.0.0-beta.1 (2021-08-16)

- Azure Resource Manager HybridNetwork client library for Java. This package contains Microsoft Azure SDK for HybridNetwork Management SDK. The definitions in this swagger specification will be used to manage the Hybrid Network resources. Package tag package-2021-05-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

