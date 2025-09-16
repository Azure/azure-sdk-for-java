# Release History

## 1.2.0-beta.2 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.2.0-beta.1 (2025-09-16)

- Azure Resource Manager ContainerAppsApi client library for Java. This package contains Microsoft Azure SDK for ContainerAppsApi Management SDK. Functions is an extension resource to revisions and the api listed is used to proxy the call from Web RP to the function app's host process, this api is not exposed to users and only Web RP is allowed to invoke functions extension resource. Package tag package-preview-2025-02-02-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.DaprComponent$DefinitionStages` was modified

* `withExistingConnectedEnvironment(java.lang.String,java.lang.String)` was removed in stage 1

#### `models.ContainerAppsSourceControls` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed

#### `models.ConnectedEnvironmentsDaprComponents` was modified

* `models.DaprComponent getById(java.lang.String)` -> `models.ConnectedEnvironmentDaprComponent getById(java.lang.String)`
* `models.DaprComponent$DefinitionStages$Blank define(java.lang.String)` -> `models.ConnectedEnvironmentDaprComponent$DefinitionStages$Blank define(java.lang.String)`
* `com.azure.core.http.rest.Response deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `deleteWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `models.DaprComponent get(java.lang.String,java.lang.String,java.lang.String)` -> `models.ConnectedEnvironmentDaprComponent get(java.lang.String,java.lang.String,java.lang.String)`

#### `models.ConnectedEnvironment$Update` was modified

* `withDaprAIConnectionString(java.lang.String)` was removed
* `withStaticIp(java.lang.String)` was removed
* `withCustomDomainConfiguration(models.CustomDomainConfiguration)` was removed
* `withExtendedLocation(models.ExtendedLocation)` was removed

#### `models.ConnectedEnvironmentsCertificates` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `com.azure.core.http.rest.Response deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`

#### `models.DaprComponents` was modified

* `createOrUpdate(java.lang.String,java.lang.String,java.lang.String,fluent.models.DaprComponentInner)` was removed
* `createOrUpdateWithResponse(java.lang.String,java.lang.String,java.lang.String,fluent.models.DaprComponentInner,com.azure.core.util.Context)` was removed

#### `models.ConnectedEnvironments` was modified

* `update(java.lang.String,java.lang.String)` was removed
* `updateWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.DaprComponent$Definition` was modified

* `withExistingConnectedEnvironment(java.lang.String,java.lang.String)` was removed

#### `models.ConnectedEnvironmentsStorages` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `com.azure.core.http.rest.Response deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`

### Features Added

* `models.ContainerAppsBuilds` was added

* `models.HttpConnectionPool` was added

* `models.IngressConfiguration` was added

* `models.LoggerSetting` was added

* `models.RuntimeJavaAgentLogging` was added

* `models.LabelHistoryRecordItem` was added

* `models.BuildProvisioningState` was added

* `models.DaprSubscriptions` was added

* `models.PatchType` was added

* `models.SessionProbe` was added

* `models.HttpRouteConfigProperties` was added

* `models.SmbStorage` was added

* `models.EnvironmentVariable` was added

* `models.DotNetComponentServiceBind` was added

* `models.AppResiliencies` was added

* `models.SessionProbeTcpSocket` was added

* `models.PrivateEndpoint` was added

* `models.LogsConfiguration` was added

* `models.PatchApplyStatus` was added

* `models.DaprSubscription$UpdateStages` was added

* `models.ConnectedEnvironmentDaprComponent` was added

* `models.DotNetComponent$DefinitionStages` was added

* `models.HttpGet` was added

* `models.ContainerAppsPatches` was added

* `models.LogicApps` was added

* `models.PrivateEndpointConnection$Definition` was added

* `models.MaintenanceConfigurationResource` was added

* `models.DotNetComponentProvisioningState` was added

* `models.DotNetComponentType` was added

* `models.FunctionsExtensions` was added

* `models.DaprAppHealth` was added

* `models.DaprComponentResiliencyPoliciesCollection` was added

* `models.ConnectedEnvironmentDaprComponent$DefinitionStages` was added

* `models.PatchCollection` was added

* `models.AppResiliency$Definition` was added

* `models.DaprComponentResiliencyPolicyHttpRetryBackOffConfiguration` was added

* `models.SessionProbeHttpGet` was added

* `models.HttpRouteConfigs` was added

* `models.ContainerAppsPatchResource` was added

* `models.DaprSubscription$DefinitionStages` was added

* `models.OtlpConfiguration` was added

* `models.MetricsConfiguration` was added

* `models.ScgRoute` was added

* `models.MaintenanceConfigurationResource$UpdateStages` was added

* `models.DaprSubscriptionBulkSubscribeOptions` was added

* `models.PrivateEndpointServiceConnectionStatus` was added

* `models.LabelHistoryCollection` was added

* `models.BuildsByBuilderResources` was added

* `models.BuildResource` was added

* `models.ContainerAppsBuildConfiguration` was added

* `models.WorkflowEnvelopeCollection` was added

* `models.TimeoutPolicy` was added

* `models.DotNetComponentConfigurationProperty` was added

* `models.ContainerAppsBuildCollection` was added

* `models.DaprSubscriptionsCollection` was added

* `models.ErrorEntity` was added

* `models.PrivateLinkServiceConnectionState` was added

* `models.DaprSubscriptionRoutes` was added

* `models.PatchProperties` was added

* `models.HttpRetryPolicy` was added

* `models.PatchDetails` was added

* `models.PrivateEndpointConnectionProvisioningState` was added

* `models.CircuitBreakerPolicy` was added

* `models.TcpRetryPolicy` was added

* `models.HeaderMatch` was added

* `models.DiskEncryptionConfiguration` was added

* `models.BuilderCollection` was added

* `models.BuildStatus` was added

* `models.DaprSubscription` was added

* `models.DaprComponentResiliencyPolicyHttpRetryPolicyConfiguration` was added

* `models.BuildToken` was added

* `models.DaprComponentResiliencyPolicyConfiguration` was added

* `models.WorkflowEnvelope` was added

* `models.OpenTelemetryConfiguration` was added

* `models.PrivateLinkResourceListResult` was added

* `models.DaprComponentResiliencyPolicy$Update` was added

* `models.LogicAppsProxyMethod` was added

* `models.HttpRouteAction` was added

* `models.ConnectedEnvironmentDaprComponentsCollection` was added

* `models.DestinationsConfiguration` was added

* `models.CertificateType` was added

* `models.BuildCollection` was added

* `models.ScheduledEntry` was added

* `models.ContainerExecutionStatus` was added

* `models.DaprComponentServiceBinding` was added

* `models.MaintenanceConfigurationCollection` was added

* `models.ContainerAppsBuildsByContainerApps` was added

* `models.WorkflowArtifacts` was added

* `models.LabelHistoryProperties` was added

* `models.PatchingMode` was added

* `models.ContainerAppsBuildResource` was added

* `models.PrivateEndpointConnection$UpdateStages` was added

* `models.ConnectedEnvironmentDaprComponent$UpdateStages` was added

* `models.HttpRouteProvisioningState` was added

* `models.ExecutionStatus` was added

* `models.ConnectedEnvironmentDaprComponent$Definition` was added

* `models.ContainerRegistryWithCustomImage` was added

* `models.RuntimeDotnet` was added

* `models.AppResiliency$UpdateStages` was added

* `models.SpringCloudGatewayComponent` was added

* `models.ConnectedEnvironmentDaprComponentProvisioningState` was added

* `models.Kind` was added

* `models.SessionProbeHttpGetHttpHeadersItem` was added

* `models.ContainerAppPropertiesPatchingConfiguration` was added

* `models.BuilderResource$Definition` was added

* `models.Header` was added

* `models.TcpConnectionPool` was added

* `models.DaprSubscription$Definition` was added

* `models.BuilderProvisioningState` was added

* `models.IngressConfigurationScale` was added

* `models.DotNetComponent$UpdateStages` was added

* `models.LabelHistory` was added

* `models.DaprComponentResiliencyPolicyTimeoutPolicyConfiguration` was added

* `models.LogicApp$Definition` was added

* `models.HttpRouteProvisioningErrors` was added

* `models.BuildResource$UpdateStages` was added

* `models.ManagedEnvironmentPrivateEndpointConnections` was added

* `models.DotNetComponents` was added

* `models.WorkflowState` was added

* `models.PrivateEndpointConnectionListResult` was added

* `models.AppResiliency` was added

* `models.BuilderResource$Update` was added

* `models.BuildConfiguration` was added

* `models.BuildResource$Update` was added

* `models.ConnectedEnvironmentPatchResource` was added

* `models.HttpRouteConfig$Definition` was added

* `models.PatchDetailsOldLayer` was added

* `models.ConnectedEnvironmentDaprComponent$Update` was added

* `models.DotNetComponent$Update` was added

* `models.Status` was added

* `models.SecretKeyVaultProperties` was added

* `models.HttpRouteConfig$DefinitionStages` was added

* `models.ConnectedEnvironmentStorageProvisioningState` was added

* `models.MaintenanceConfigurations` was added

* `models.PrivateEndpointConnection` was added

* `models.BuilderResourceUpdate` was added

* `models.AppResiliency$DefinitionStages` was added

* `models.ImageType` was added

* `models.BuildAuthTokens` was added

* `models.ContainerRegistry` was added

* `models.IngressTargetPortHttpScheme` was added

* `models.WorkflowEnvelopeProperties` was added

* `models.WorkflowHealthState` was added

* `models.HttpRouteConfig` was added

* `models.DaprServiceBindMetadata` was added

* `models.MaintenanceConfigurationResource$DefinitionStages` was added

* `models.TracesConfiguration` was added

* `models.DaprComponentResiliencyPolicyCircuitBreakerPolicyConfiguration` was added

* `models.PrivateEndpointConnection$Update` was added

* `models.LogicApp$UpdateStages` was added

* `models.MaintenanceConfigurationResource$Update` was added

* `models.AppInsightsConfiguration` was added

* `models.WorkflowHealth` was added

* `models.DaprComponentResiliencyPolicy$Definition` was added

* `models.PrivateLinkResource` was added

* `models.DaprComponentResiliencyPolicy$DefinitionStages` was added

* `models.PatchSkipConfig` was added

* `models.HttpRouteConfig$Update` was added

* `models.DaprComponentResiliencyPolicies` was added

* `models.AppResiliencyCollection` was added

* `models.DotNetComponent` was added

* `models.ContainerAppsLabelHistories` was added

* `models.HttpRouteMatch` was added

* `models.BuilderResource$DefinitionStages` was added

* `models.Builders` was added

* `models.Level` was added

* `models.BuildResource$DefinitionStages` was added

* `models.AppResiliency$Update` was added

* `models.DaprSubscription$Update` was added

* `models.WeekDay` was added

* `models.DataDogConfiguration` was added

* `models.DotNetComponentsCollection` was added

* `models.ManagedEnvironmentPrivateLinkResources` was added

* `models.MaintenanceConfigurationResource$Definition` was added

* `models.DotNetComponent$Definition` was added

* `models.HttpRouteConfig$UpdateStages` was added

* `models.DaprComponentResiliencyPolicy$UpdateStages` was added

* `models.LogicApp` was added

* `models.PatchDetailsNewLayer` was added

* `models.HttpRouteConfigCollection` was added

* `models.DetectionStatus` was added

* `models.ReplicaExecutionStatus` was added

* `models.PreBuildStep` was added

* `models.BuilderResource$UpdateStages` was added

* `models.RuntimeJavaAgent` was added

* `models.HttpRouteTarget` was added

* `models.SessionProbeType` was added

* `models.DaprComponentResiliencyPolicy` was added

* `models.DiskEncryptionConfigurationKeyVaultConfiguration` was added

* `models.JobRunningState` was added

* `models.DiskEncryptionConfigurationKeyVaultConfigurationAuth` was added

* `models.HttpRouteRule` was added

* `models.BuilderResource` was added

* `models.ResourceTags` was added

* `models.BuildResource$Definition` was added

* `models.Builds` was added

* `models.HttpRoute` was added

* `models.LogicApp$Update` was added

* `models.NacosComponent` was added

* `models.LogicApp$DefinitionStages` was added

* `models.PublicNetworkAccess` was added

* `models.DaprSubscriptionRouteRule` was added

* `models.PrivateEndpointConnection$DefinitionStages` was added

#### `models.SessionContainer` was modified

* `probes()` was added
* `withProbes(java.util.List)` was added

#### `models.ContainerAppsSourceControls` was modified

* `deleteByIdWithResponse(java.lang.String,java.lang.String,java.lang.Boolean,java.lang.Boolean,com.azure.core.util.Context)` was added
* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Boolean,java.lang.Boolean,com.azure.core.util.Context)` was added

#### `models.ServiceBind` was modified

* `withClientType(java.lang.String)` was added
* `customizedKeys()` was added
* `clientType()` was added
* `withCustomizedKeys(java.util.Map)` was added

#### `models.Job` was modified

* `resume(com.azure.core.util.Context)` was added
* `runningState()` was added
* `suspend(com.azure.core.util.Context)` was added
* `suspend()` was added
* `extendedLocation()` was added
* `resume()` was added

#### `models.Revision` was modified

* `labels()` was added

#### `models.WorkloadProfile` was modified

* `enableFips()` was added
* `withEnableFips(java.lang.Boolean)` was added

#### `models.InitContainer` was modified

* `withImageType(models.ImageType)` was added

#### `models.ConnectedEnvironmentsDaprComponents` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.ConnectedEnvironmentStorageProperties` was modified

* `withSmb(models.SmbStorage)` was added
* `deploymentErrors()` was added
* `provisioningState()` was added
* `smb()` was added

#### `models.GithubActionConfiguration` was modified

* `dockerfilePath()` was added
* `withDockerfilePath(java.lang.String)` was added
* `withBuildEnvironmentVariables(java.util.List)` was added
* `buildEnvironmentVariables()` was added

#### `models.DaprComponent` was modified

* `serviceComponentBind()` was added

#### `models.LogAnalyticsConfiguration` was modified

* `withDynamicJsonColumns(java.lang.Boolean)` was added
* `dynamicJsonColumns()` was added

#### `models.ManagedEnvironment` was modified

* `publicNetworkAccess()` was added
* `availabilityZones()` was added
* `diskEncryptionConfiguration()` was added
* `ingressConfiguration()` was added
* `privateEndpointConnections()` was added
* `privateLinkDefaultDomain()` was added
* `appInsightsConfiguration()` was added
* `openTelemetryConfiguration()` was added

#### `models.ConnectedEnvironmentsCertificates` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.DaprComponents` was modified

* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `getById(java.lang.String)` was added
* `define(java.lang.String)` was added
* `deleteById(java.lang.String)` was added

#### `models.Container` was modified

* `withImageType(models.ImageType)` was added

#### `models.CertificateProperties` was modified

* `certificateType()` was added
* `deploymentErrors()` was added
* `withCertificateType(models.CertificateType)` was added

#### `models.ContainerApp` was modified

* `kind()` was added
* `patchingConfiguration()` was added
* `deploymentErrors()` was added

#### `models.ReplicaContainer` was modified

* `debugEndpoint()` was added

#### `models.ContainerApp$Definition` was modified

* `withKind(models.Kind)` was added
* `withPatchingConfiguration(models.ContainerAppPropertiesPatchingConfiguration)` was added

#### `models.Configuration` was modified

* `withRevisionTransitionThreshold(java.lang.Integer)` was added
* `revisionTransitionThreshold()` was added
* `withTargetLabel(java.lang.String)` was added
* `targetLabel()` was added

#### `models.Runtime` was modified

* `withDotnet(models.RuntimeDotnet)` was added
* `dotnet()` was added

#### `models.DaprComponent$Definition` was modified

* `withExistingManagedEnvironment(java.lang.String,java.lang.String)` was added
* `withServiceComponentBind(java.util.List)` was added

#### `models.ManagedEnvironment$Update` was modified

* `withDiskEncryptionConfiguration(models.DiskEncryptionConfiguration)` was added
* `withOpenTelemetryConfiguration(models.OpenTelemetryConfiguration)` was added
* `withAvailabilityZones(java.util.List)` was added
* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added
* `withIngressConfiguration(models.IngressConfiguration)` was added
* `withAppInsightsConfiguration(models.AppInsightsConfiguration)` was added

#### `models.SourceControl$Definition` was modified

* `withXMsGithubAuxiliary(java.lang.String)` was added

#### `models.ConnectedEnvironmentsStorages` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Job$Definition` was modified

* `withExtendedLocation(models.ExtendedLocation)` was added

#### `models.Jobs` was modified

* `resume(java.lang.String,java.lang.String)` was added
* `suspend(java.lang.String,java.lang.String)` was added
* `resume(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `suspend(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.DaprComponent$Update` was modified

* `withServiceComponentBind(java.util.List)` was added

#### `models.ManagedEnvironment$Definition` was modified

* `withAvailabilityZones(java.util.List)` was added
* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added
* `withDiskEncryptionConfiguration(models.DiskEncryptionConfiguration)` was added
* `withOpenTelemetryConfiguration(models.OpenTelemetryConfiguration)` was added
* `withIngressConfiguration(models.IngressConfiguration)` was added
* `withAppInsightsConfiguration(models.AppInsightsConfiguration)` was added

#### `models.ContainerApp$Update` was modified

* `withPatchingConfiguration(models.ContainerAppPropertiesPatchingConfiguration)` was added

#### `models.SourceControl$Update` was modified

* `withXMsGithubAuxiliary(java.lang.String)` was added

#### `models.Job$Update` was modified

* `withExtendedLocation(models.ExtendedLocation)` was added

#### `models.AzureFileProperties` was modified

* `accountKeyVaultProperties()` was added
* `withAccountKeyVaultProperties(models.SecretKeyVaultProperties)` was added

#### `models.JobExecution` was modified

* `detailedStatus()` was added

#### `models.RuntimeJava` was modified

* `javaAgent()` was added
* `withJavaAgent(models.RuntimeJavaAgent)` was added

#### `models.JobPatchProperties` was modified

* `withExtendedLocation(models.ExtendedLocation)` was added
* `extendedLocation()` was added

#### `ContainerAppsApiManager` was modified

* `containerAppsBuilds()` was added
* `managedEnvironmentPrivateEndpointConnections()` was added
* `daprSubscriptions()` was added
* `httpRouteConfigs()` was added
* `appResiliencies()` was added
* `functionsExtensions()` was added
* `buildAuthTokens()` was added
* `dotNetComponents()` was added
* `managedEnvironmentPrivateLinkResources()` was added
* `containerAppsLabelHistories()` was added
* `builds()` was added
* `builders()` was added
* `daprComponentResiliencyPolicies()` was added
* `maintenanceConfigurations()` was added
* `logicApps()` was added
* `containerAppsPatches()` was added
* `containerAppsBuildsByContainerApps()` was added
* `buildsByBuilderResources()` was added

#### `models.BaseContainer` was modified

* `withImageType(models.ImageType)` was added
* `imageType()` was added

#### `models.BlobStorageTokenStore` was modified

* `managedIdentityResourceId()` was added
* `clientId()` was added
* `withManagedIdentityResourceId(java.lang.String)` was added
* `withBlobContainerUri(java.lang.String)` was added
* `withClientId(java.lang.String)` was added
* `blobContainerUri()` was added

#### `models.ContainerResources` was modified

* `withGpu(java.lang.Double)` was added
* `gpu()` was added

#### `models.Ingress` was modified

* `withTargetPortHttpScheme(models.IngressTargetPortHttpScheme)` was added
* `targetPortHttpScheme()` was added

#### `models.Dapr` was modified

* `withAppHealth(models.DaprAppHealth)` was added
* `withMaxConcurrency(java.lang.Integer)` was added
* `appHealth()` was added
* `maxConcurrency()` was added

## 1.1.0 (2025-04-17)

- Azure Resource Manager ContainerAppsApi client library for Java. This package contains Microsoft Azure SDK for ContainerAppsApi Management SDK.  Package tag package-2025-01-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.SessionContainer` was added

* `models.Runtime` was added

* `models.SessionPoolUpdatableProperties` was added

* `models.JavaComponentPropertiesScale` was added

* `models.ScaleConfiguration` was added

* `models.PoolManagementType` was added

* `models.CertificateKeyVaultProperties` was added

* `models.IdentitySettingsLifeCycle` was added

* `models.SessionPoolProvisioningState` was added

* `models.SpringBootAdminComponent` was added

* `models.JavaComponentServiceBind` was added

* `models.SessionPool$UpdateStages` was added

* `models.JavaComponentConfigurationProperty` was added

* `models.SpringCloudConfigComponent` was added

* `models.SessionPool$Definition` was added

* `models.LifecycleConfiguration` was added

* `models.JavaComponent$Definition` was added

* `models.SessionPool$DefinitionStages` was added

* `models.SessionIngress` was added

* `models.SessionPool` was added

* `models.NfsAzureFileProperties` was added

* `models.ContainerAppRunningStatus` was added

* `models.SessionContainerResources` was added

* `models.SessionPoolSecret` was added

* `models.SessionPool$Update` was added

* `models.JavaComponentsCollection` was added

* `models.JavaComponent` was added

* `models.SessionNetworkStatus` was added

* `models.RuntimeJava` was added

* `models.JavaComponent$UpdateStages` was added

* `models.JavaComponentProvisioningState` was added

* `models.ManagedIdentitySetting` was added

* `models.CustomContainerTemplate` was added

* `models.SessionNetworkConfiguration` was added

* `models.DynamicPoolConfiguration` was added

* `models.IdentitySettings` was added

* `models.JavaComponent$DefinitionStages` was added

* `models.ContainerType` was added

* `models.SessionRegistryCredentials` was added

* `models.JavaComponentType` was added

* `models.JavaComponentProperties` was added

* `models.LifecycleType` was added

* `models.ContainerAppsSessionPools` was added

* `models.SessionPoolCollection` was added

* `models.JavaComponent$Update` was added

* `models.JavaComponentIngress` was added

* `models.JavaComponents` was added

* `models.SpringCloudEurekaComponent` was added

#### `models.ManagedEnvironment$Update` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.QueueScaleRule` was modified

* `withAccountName(java.lang.String)` was added
* `identity()` was added
* `accountName()` was added
* `withIdentity(java.lang.String)` was added

#### `models.ManagedEnvironment$Definition` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.ManagedEnvironmentStorageProperties` was modified

* `nfsAzureFile()` was added
* `withNfsAzureFile(models.NfsAzureFileProperties)` was added

#### `models.JobConfiguration` was modified

* `identitySettings()` was added
* `withIdentitySettings(java.util.List)` was added

#### `models.TcpScaleRule` was modified

* `withIdentity(java.lang.String)` was added
* `identity()` was added

#### `models.Scale` was modified

* `pollingInterval()` was added
* `withPollingInterval(java.lang.Integer)` was added
* `cooldownPeriod()` was added
* `withCooldownPeriod(java.lang.Integer)` was added

#### `models.ManagedEnvironment` was modified

* `identity()` was added

#### `models.HttpScaleRule` was modified

* `identity()` was added
* `withIdentity(java.lang.String)` was added

#### `models.CertificateProperties` was modified

* `certificateKeyVaultProperties()` was added
* `withCertificateKeyVaultProperties(models.CertificateKeyVaultProperties)` was added

#### `models.ContainerApp` was modified

* `runningStatus()` was added

#### `models.JobScaleRule` was modified

* `withIdentity(java.lang.String)` was added
* `identity()` was added

#### `ContainerAppsApiManager` was modified

* `containerAppsSessionPools()` was added
* `javaComponents()` was added

#### `models.CustomScaleRule` was modified

* `identity()` was added
* `withIdentity(java.lang.String)` was added

#### `models.CustomDomainConfiguration` was modified

* `withCertificateKeyVaultProperties(models.CertificateKeyVaultProperties)` was added
* `certificateKeyVaultProperties()` was added

#### `models.Configuration` was modified

* `withIdentitySettings(java.util.List)` was added
* `runtime()` was added
* `identitySettings()` was added
* `withRuntime(models.Runtime)` was added

## 1.1.0-beta.1 (2024-10-17)

- Azure Resource Manager ContainerAppsApi client library for Java. This package contains Microsoft Azure SDK for ContainerAppsApi Management SDK. Functions is an extension resource to revisions and the api listed is used to proxy the call from Web RP to the function app's host process, this api is not exposed to users and only Web RP is allowed to invoke functions extension resource. Package tag package-preview-2024-08. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ContainerAppsSourceControls` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

* `models.SessionContainer` was added

* `models.ContainerAppsBuilds` was added

* `models.DotNetComponent$UpdateStages` was added

* `models.DaprComponentResiliencyPolicyTimeoutPolicyConfiguration` was added

* `models.HttpConnectionPool` was added

* `models.LogicApp$Definition` was added

* `models.Runtime` was added

* `models.SessionPoolUpdatableProperties` was added

* `models.LoggerSetting` was added

* `models.JavaComponentPropertiesScale` was added

* `models.BuildResource$UpdateStages` was added

* `models.ScaleConfiguration` was added

* `models.ManagedEnvironmentPrivateEndpointConnections` was added

* `models.DotNetComponents` was added

* `models.RuntimeJavaAgentLogging` was added

* `models.BuildProvisioningState` was added

* `models.WorkflowState` was added

* `models.PrivateEndpointConnectionListResult` was added

* `models.AppResiliency` was added

* `models.DaprSubscriptions` was added

* `models.BuilderResource$Update` was added

* `models.BuildConfiguration` was added

* `models.PoolManagementType` was added

* `models.PatchType` was added

* `models.BuildResource$Update` was added

* `models.SmbStorage` was added

* `models.CertificateKeyVaultProperties` was added

* `models.IdentitySettingsLifeCycle` was added

* `models.EnvironmentVariable` was added

* `models.SessionPoolProvisioningState` was added

* `models.DotNetComponentServiceBind` was added

* `models.SpringBootAdminComponent` was added

* `models.AppResiliencies` was added

* `models.PrivateEndpoint` was added

* `models.PatchDetailsOldLayer` was added

* `models.JavaComponentServiceBind` was added

* `models.LogsConfiguration` was added

* `models.DotNetComponent$Update` was added

* `models.PatchApplyStatus` was added

* `models.DaprSubscription$UpdateStages` was added

* `models.DotNetComponent$DefinitionStages` was added

* `models.HttpGet` was added

* `models.ContainerAppsPatches` was added

* `models.LogicApps` was added

* `models.PrivateEndpointConnection$Definition` was added

* `models.SessionPool$UpdateStages` was added

* `models.JavaComponentConfigurationProperty` was added

* `models.DotNetComponentProvisioningState` was added

* `models.DotNetComponentType` was added

* `models.SpringCloudConfigComponent` was added

* `models.PrivateEndpointConnection` was added

* `models.BuilderResourceUpdate` was added

* `models.FunctionsExtensions` was added

* `models.SessionPool$Definition` was added

* `models.AppResiliency$DefinitionStages` was added

* `models.DaprComponentResiliencyPoliciesCollection` was added

* `models.ImageType` was added

* `models.BuildAuthTokens` was added

* `models.PatchCollection` was added

* `models.ContainerRegistry` was added

* `models.IngressTargetPortHttpScheme` was added

* `models.AppResiliency$Definition` was added

* `models.DaprComponentResiliencyPolicyHttpRetryBackOffConfiguration` was added

* `models.WorkflowEnvelopeProperties` was added

* `models.WorkflowHealthState` was added

* `models.ContainerAppsPatchResource` was added

* `models.DaprSubscription$DefinitionStages` was added

* `models.OtlpConfiguration` was added

* `models.MetricsConfiguration` was added

* `models.DaprServiceBindMetadata` was added

* `models.ScgRoute` was added

* `models.TracesConfiguration` was added

* `models.DaprSubscriptionBulkSubscribeOptions` was added

* `models.DaprComponentResiliencyPolicyCircuitBreakerPolicyConfiguration` was added

* `models.JavaComponent$Definition` was added

* `models.SessionPool$DefinitionStages` was added

* `models.PrivateEndpointServiceConnectionStatus` was added

* `models.SessionIngress` was added

* `models.BuildsByBuilderResources` was added

* `models.BuildResource` was added

* `models.ContainerAppsBuildConfiguration` was added

* `models.WorkflowEnvelopeCollection` was added

* `models.TimeoutPolicy` was added

* `models.PrivateEndpointConnection$Update` was added

* `models.DotNetComponentConfigurationProperty` was added

* `models.ContainerAppsBuildCollection` was added

* `models.DaprSubscriptionsCollection` was added

* `models.ErrorEntity` was added

* `models.LogicApp$UpdateStages` was added

* `models.PrivateLinkServiceConnectionState` was added

* `models.AppInsightsConfiguration` was added

* `models.WorkflowHealth` was added

* `models.DaprSubscriptionRoutes` was added

* `models.DaprComponentResiliencyPolicy$Definition` was added

* `models.PatchProperties` was added

* `models.HttpRetryPolicy` was added

* `models.PatchDetails` was added

* `models.PrivateEndpointConnectionProvisioningState` was added

* `models.SessionPool` was added

* `models.NfsAzureFileProperties` was added

* `models.PrivateLinkResource` was added

* `models.CircuitBreakerPolicy` was added

* `models.DaprComponentResiliencyPolicy$DefinitionStages` was added

* `models.SessionContainerResources` was added

* `models.TcpRetryPolicy` was added

* `models.SessionPoolSecret` was added

* `models.PatchSkipConfig` was added

* `models.DaprComponentResiliencyPolicies` was added

* `models.HeaderMatch` was added

* `models.AppResiliencyCollection` was added

* `models.DotNetComponent` was added

* `models.BuilderCollection` was added

* `models.BuildStatus` was added

* `models.DaprSubscription` was added

* `models.SessionPool$Update` was added

* `models.DaprComponentResiliencyPolicyHttpRetryPolicyConfiguration` was added

* `models.BuildToken` was added

* `models.DaprComponentResiliencyPolicyConfiguration` was added

* `models.WorkflowEnvelope` was added

* `models.BuilderResource$DefinitionStages` was added

* `models.JavaComponentsCollection` was added

* `models.OpenTelemetryConfiguration` was added

* `models.PrivateLinkResourceListResult` was added

* `models.Builders` was added

* `models.Level` was added

* `models.DaprComponentResiliencyPolicy$Update` was added

* `models.BuildResource$DefinitionStages` was added

* `models.JavaComponent` was added

* `models.AppResiliency$Update` was added

* `models.SessionNetworkStatus` was added

* `models.LogicAppsProxyMethod` was added

* `models.DaprSubscription$Update` was added

* `models.DestinationsConfiguration` was added

* `models.CertificateType` was added

* `models.DataDogConfiguration` was added

* `models.DotNetComponentsCollection` was added

* `models.ManagedEnvironmentPrivateLinkResources` was added

* `models.RuntimeJava` was added

* `models.DotNetComponent$Definition` was added

* `models.DaprComponentResiliencyPolicy$UpdateStages` was added

* `models.BuildCollection` was added

* `models.ExecutionType` was added

* `models.JavaComponent$UpdateStages` was added

* `models.LogicApp` was added

* `models.JavaComponentProvisioningState` was added

* `models.PatchDetailsNewLayer` was added

* `models.DetectionStatus` was added

* `models.ReplicaExecutionStatus` was added

* `models.PreBuildStep` was added

* `models.ContainerExecutionStatus` was added

* `models.DaprComponentServiceBinding` was added

* `models.ContainerAppsBuildsByContainerApps` was added

* `models.CustomContainerTemplate` was added

* `models.WorkflowArtifacts` was added

* `models.SessionNetworkConfiguration` was added

* `models.BuilderResource$UpdateStages` was added

* `models.RuntimeJavaAgent` was added

* `models.DynamicPoolConfiguration` was added

* `models.PatchingMode` was added

* `models.ContainerAppsBuildResource` was added

* `models.PrivateEndpointConnection$UpdateStages` was added

* `models.IdentitySettings` was added

* `models.JavaComponent$DefinitionStages` was added

* `models.ExecutionStatus` was added

* `models.DaprComponentResiliencyPolicy` was added

* `models.JobRunningState` was added

* `models.ContainerRegistryWithCustomImage` was added

* `models.ContainerType` was added

* `models.SessionRegistryCredentials` was added

* `models.JavaComponentType` was added

* `models.RuntimeDotnet` was added

* `models.AppResiliency$UpdateStages` was added

* `models.JavaComponentProperties` was added

* `models.BuilderResource` was added

* `models.SpringCloudGatewayComponent` was added

* `models.BuildResource$Definition` was added

* `models.Kind` was added

* `models.Builds` was added

* `models.ContainerAppsSessionPools` was added

* `models.SessionPoolCollection` was added

* `models.ContainerAppPropertiesPatchingConfiguration` was added

* `models.BuilderResource$Definition` was added

* `models.LogicApp$Update` was added

* `models.NacosComponent` was added

* `models.LogicApp$DefinitionStages` was added

* `models.JavaComponent$Update` was added

* `models.JavaComponentIngress` was added

* `models.Header` was added

* `models.TcpConnectionPool` was added

* `models.PublicNetworkAccess` was added

* `models.JavaComponents` was added

* `models.DaprSubscriptionRouteRule` was added

* `models.DaprSubscription$Definition` was added

* `models.PrivateEndpointConnection$DefinitionStages` was added

* `models.BuilderProvisioningState` was added

* `models.SpringCloudEurekaComponent` was added

#### `models.ContainerAppsSourceControls` was modified

* `deleteByIdWithResponse(java.lang.String,java.lang.String,java.lang.Boolean,java.lang.Boolean,com.azure.core.util.Context)` was added
* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Boolean,java.lang.Boolean,com.azure.core.util.Context)` was added

#### `models.ServiceBind` was modified

* `clientType()` was added
* `customizedKeys()` was added
* `withCustomizedKeys(java.util.Map)` was added
* `withClientType(java.lang.String)` was added

#### `models.Job` was modified

* `suspend(com.azure.core.util.Context)` was added
* `resume()` was added
* `extendedLocation()` was added
* `suspend()` was added
* `runningState()` was added
* `resume(com.azure.core.util.Context)` was added

#### `models.DaprComponent$Definition` was modified

* `withServiceComponentBind(java.util.List)` was added

#### `models.ManagedEnvironment$Update` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added
* `withAppInsightsConfiguration(models.AppInsightsConfiguration)` was added
* `withOpenTelemetryConfiguration(models.OpenTelemetryConfiguration)` was added
* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added

#### `models.SourceControl$Definition` was modified

* `withXMsGithubAuxiliary(java.lang.String)` was added

#### `models.WorkloadProfile` was modified

* `enableFips()` was added
* `withEnableFips(java.lang.Boolean)` was added

#### `models.InitContainer` was modified

* `withImageType(models.ImageType)` was added

#### `models.Job$Definition` was modified

* `withExtendedLocation(models.ExtendedLocation)` was added

#### `models.QueueScaleRule` was modified

* `accountName()` was added
* `withAccountName(java.lang.String)` was added
* `identity()` was added
* `withIdentity(java.lang.String)` was added

#### `models.Jobs` was modified

* `resume(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `suspend(java.lang.String,java.lang.String)` was added
* `suspend(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `resume(java.lang.String,java.lang.String)` was added

#### `models.ConnectedEnvironmentStorageProperties` was modified

* `smb()` was added
* `withSmb(models.SmbStorage)` was added

#### `models.DaprComponent$Update` was modified

* `withServiceComponentBind(java.util.List)` was added

#### `models.ManagedEnvironment$Definition` was modified

* `withIdentity(models.ManagedServiceIdentity)` was added
* `withAppInsightsConfiguration(models.AppInsightsConfiguration)` was added
* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added
* `withOpenTelemetryConfiguration(models.OpenTelemetryConfiguration)` was added

#### `models.ContainerApp$Update` was modified

* `withPatchingConfiguration(models.ContainerAppPropertiesPatchingConfiguration)` was added

#### `models.ManagedEnvironmentStorageProperties` was modified

* `withNfsAzureFile(models.NfsAzureFileProperties)` was added
* `nfsAzureFile()` was added

#### `models.GithubActionConfiguration` was modified

* `dockerfilePath()` was added
* `withDockerfilePath(java.lang.String)` was added
* `buildEnvironmentVariables()` was added
* `withBuildEnvironmentVariables(java.util.List)` was added

#### `models.DaprComponent` was modified

* `serviceComponentBind()` was added

#### `models.JobConfiguration` was modified

* `identitySettings()` was added
* `withIdentitySettings(java.util.List)` was added

#### `models.TcpScaleRule` was modified

* `identity()` was added
* `withIdentity(java.lang.String)` was added

#### `models.Scale` was modified

* `pollingInterval()` was added
* `withPollingInterval(java.lang.Integer)` was added
* `cooldownPeriod()` was added
* `withCooldownPeriod(java.lang.Integer)` was added

#### `models.SourceControl$Update` was modified

* `withXMsGithubAuxiliary(java.lang.String)` was added

#### `models.LogAnalyticsConfiguration` was modified

* `withDynamicJsonColumns(java.lang.Boolean)` was added
* `dynamicJsonColumns()` was added

#### `models.Job$Update` was modified

* `withExtendedLocation(models.ExtendedLocation)` was added

#### `models.ManagedEnvironment` was modified

* `identity()` was added
* `privateEndpointConnections()` was added
* `appInsightsConfiguration()` was added
* `publicNetworkAccess()` was added
* `openTelemetryConfiguration()` was added

#### `models.HttpScaleRule` was modified

* `withIdentity(java.lang.String)` was added
* `identity()` was added

#### `models.JobExecution` was modified

* `detailedStatus()` was added

#### `models.JobPatchProperties` was modified

* `withExtendedLocation(models.ExtendedLocation)` was added
* `extendedLocation()` was added

#### `models.Container` was modified

* `withImageType(models.ImageType)` was added

#### `models.CertificateProperties` was modified

* `certificateKeyVaultProperties()` was added
* `withCertificateType(models.CertificateType)` was added
* `certificateType()` was added
* `withCertificateKeyVaultProperties(models.CertificateKeyVaultProperties)` was added

#### `models.ContainerApp` was modified

* `deploymentErrors()` was added
* `kind()` was added
* `patchingConfiguration()` was added

#### `models.JobScaleRule` was modified

* `identity()` was added
* `withIdentity(java.lang.String)` was added

#### `ContainerAppsApiManager` was modified

* `containerAppsBuilds()` was added
* `daprSubscriptions()` was added
* `dotNetComponents()` was added
* `logicApps()` was added
* `javaComponents()` was added
* `builds()` was added
* `containerAppsPatches()` was added
* `containerAppsSessionPools()` was added
* `managedEnvironmentPrivateEndpointConnections()` was added
* `appResiliencies()` was added
* `functionsExtensions()` was added
* `buildsByBuilderResources()` was added
* `daprComponentResiliencyPolicies()` was added
* `builders()` was added
* `buildAuthTokens()` was added
* `containerAppsBuildsByContainerApps()` was added
* `managedEnvironmentPrivateLinkResources()` was added

#### `models.ReplicaContainer` was modified

* `debugEndpoint()` was added

#### `models.BaseContainer` was modified

* `withImageType(models.ImageType)` was added
* `imageType()` was added

#### `models.CustomScaleRule` was modified

* `identity()` was added
* `withIdentity(java.lang.String)` was added

#### `models.ContainerApp$Definition` was modified

* `withKind(models.Kind)` was added
* `withPatchingConfiguration(models.ContainerAppPropertiesPatchingConfiguration)` was added

#### `models.Ingress` was modified

* `withTargetPortHttpScheme(models.IngressTargetPortHttpScheme)` was added
* `targetPortHttpScheme()` was added

#### `models.CustomDomainConfiguration` was modified

* `withCertificateKeyVaultProperties(models.CertificateKeyVaultProperties)` was added
* `certificateKeyVaultProperties()` was added

#### `models.Configuration` was modified

* `identitySettings()` was added
* `withIdentitySettings(java.util.List)` was added
* `runtime()` was added
* `withRuntime(models.Runtime)` was added

## 1.0.0 (2024-08-07)

- Azure Resource Manager ContainerAppsApi client library for Java. This package contains Microsoft Azure SDK for ContainerAppsApi Management SDK.  Package tag package-2024-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.Usage` was added

* `models.Usages` was added

* `models.UsageName` was added

* `models.ListUsagesResult` was added

* `models.TokenStore` was added

* `models.ManagedEnvironmentUsages` was added

* `models.ManagedEnvironmentPropertiesPeerTrafficConfiguration` was added

* `models.EncryptionSettings` was added

* `models.ManagedEnvironmentPropertiesPeerTrafficConfigurationEncryption` was added

* `models.IngressPortMapping` was added

* `models.BlobStorageTokenStore` was added

#### `models.Replica` was modified

* `systemData()` was added

#### `models.JobConfigurationScheduleTriggerConfig` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ManagedCertificatePatch` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ServiceBind` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Secret` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureActiveDirectoryValidation` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.WorkloadProfileStatesCollection` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DaprComponentsCollection` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ManagedCertificateCollection` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ManagedEnvironment$Update` was modified

* `withPeerTrafficConfiguration(models.ManagedEnvironmentPropertiesPeerTrafficConfiguration)` was added

#### `models.LoginScopes` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Revision` was modified

* `systemData()` was added

#### `models.AvailableWorkloadProfileProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withGpus(java.lang.Integer)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `gpus()` was added

#### `models.HttpSettings` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Google` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AllowedPrincipals` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ContainerAppProbeHttpGetHttpHeadersItem` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.KedaConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DiagnosticsDefinition` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.GlobalValidation` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureActiveDirectoryLogin` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WorkloadProfile` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.InitContainer` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EnvironmentVar` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CertificatePatch` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AvailableWorkloadProfilesCollection` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TrafficWeight` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.JobScale` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.IpSecurityRestrictionRule` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AppleRegistration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.QueueScaleRule` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CheckNameAvailabilityRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.JobsCollection` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OpenIdConnectLogin` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.UserAssignedIdentity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Jobs` was modified

* `listDetectors(java.lang.String,java.lang.String)` was added
* `proxyGetWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `proxyGet(java.lang.String,java.lang.String,java.lang.String)` was added
* `listDetectors(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getDetectorWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getDetector(java.lang.String,java.lang.String,java.lang.String)` was added

#### `models.ConnectedEnvironmentStorageProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AppRegistration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ContainerAppSecret` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Service` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BillingMeterProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ManagedEnvironment$Definition` was modified

* `withPeerTrafficConfiguration(models.ManagedEnvironmentPropertiesPeerTrafficConfiguration)` was added

#### `models.ManagedEnvironmentStorageProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OpenIdConnectConfig` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BillingMeter` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `name()` was added
* `id()` was added
* `systemData()` was added
* `type()` was added

#### `models.GithubActionConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.JobPatchPropertiesProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AllowedAudiencesValidation` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ScaleRule` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClientRegistration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CustomHostnameAnalysisResultCustomDomainVerificationFailureInfoDetailsItem` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ContainerAppCollection` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ForwardProxy` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DaprComponent` was modified

* `systemData()` was added

#### `models.IdentityProviders` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ContainerAppProbe` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ScaleRuleAuth` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.JobConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TcpScaleRule` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SecretVolumeItem` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationDisplay` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.JobConfigurationManualTriggerConfig` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.JobConfigurationEventTriggerConfig` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ConnectedEnvironmentCollection` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Scale` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CustomDomain` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CertificateCollection` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.IngressStickySessions` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.VnetConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ManagedEnvironmentStorage` was modified

* `systemData()` was added

#### `models.LogAnalyticsConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Mtls` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ManagedEnvironment` was modified

* `peerTrafficConfiguration()` was added

#### `models.AzureFileProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AuthPlatform` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CorsPolicy` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RegistryCredentials` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OpenIdConnectClientCredential` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.JobTemplate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureActiveDirectoryRegistration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ManagedServiceIdentity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DefaultAuthorizationPolicy` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DefaultErrorResponseError` was modified

* `getMessage()` was added
* `getCode()` was added
* `getTarget()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `getAdditionalInfo()` was added
* `getDetails()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureCredentials` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.WorkloadProfileStatesProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AvailableOperations` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.HttpScaleRule` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureActiveDirectory` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WorkloadProfileStates` was modified

* `systemData()` was added

#### `models.Login` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `tokenStore()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withTokenStore(models.TokenStore)` was added

#### `models.DiagnosticsDataApiResponse` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ManagedCertificateProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.JobExecutionContainer` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ResourceProviders` was modified

* `getCustomDomainVerificationId()` was added
* `getCustomDomainVerificationIdWithResponse(com.azure.core.util.Context)` was added

#### `models.AzureStaticWebApps` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DaprConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.JobPatchProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureStaticWebAppsRegistration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TwitterRegistration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SourceControl` was modified

* `systemData()` was added

#### `models.RevisionCollection` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Twitter` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Container` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CustomHostnameAnalysisResultCustomDomainVerificationFailureInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Facebook` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CookieExpiration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CustomOpenIdConnectProvider` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CertificateProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DiagnosticRendering` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Apple` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DiagnosticSupportTopic` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Diagnostics` was modified

* `systemData()` was added

#### `models.AvailableWorkloadProfile` was modified

* `systemData()` was added

#### `models.JwtClaimChecks` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ContainerAppProbeHttpGet` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.JobScaleRule` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.RegistryInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AuthConfig` was modified

* `encryptionSettings()` was added

#### `models.Volume` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DaprSecret` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DaprMetadata` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.JobExecutionTemplate` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DiagnosticsProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `ContainerAppsApiManager` was modified

* `managedEnvironmentUsages()` was added
* `usages()` was added

#### `models.DiagnosticsStatus` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ReplicaContainer` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BaseContainer` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VolumeMount` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DiagnosticDataProviderMetadata` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ExtendedLocation` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DiagnosticDataTableResponseObject` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ManagedEnvironmentPropertiesPeerAuthentication` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DiagnosticDataProviderMetadataPropertyBagItem` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AppLogsConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SourceControlCollection` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.GitHub` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ConnectedEnvironmentStorage` was modified

* `systemData()` was added

#### `models.ManagedEnvironmentsCollection` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CustomScaleRule` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AuthConfigCollection` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Nonce` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ContainerResources` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AuthConfig$Update` was modified

* `withEncryptionSettings(models.EncryptionSettings)` was added

#### `models.OpenIdConnectRegistration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Ingress` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `additionalPortMappings()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withAdditionalPortMappings(java.util.List)` was added

#### `models.CustomDomainConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Dapr` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.HttpSettingsRoutes` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DiagnosticDataTableResponseColumn` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Configuration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Template` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ContainerAppProbeTcpSocket` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AuthConfig$Definition` was modified

* `withEncryptionSettings(models.EncryptionSettings)` was added

#### `models.LoginRoutes` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

## 1.0.0-beta.8 (2024-07-24)

- Azure Resource Manager ContainerAppsApi client library for Java. This package contains Microsoft Azure SDK for ContainerAppsApi Management SDK. Functions is an extension resource to revisions and the api listed is used to proxy the call from Web RP to the function app's host process, this api is not exposed to users and only Web RP is allowed to invoke functions extension resource. Package tag package-preview-2024-02. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Jobs` was modified

* `listDetectorsWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `models.DiagnosticsCollection listDetectors(java.lang.String,java.lang.String)` -> `com.azure.core.http.rest.PagedIterable listDetectors(java.lang.String,java.lang.String)`

#### `models.BillingMeter` was modified

* `systemData()` was removed

#### `models.JavaComponent$Definition` was modified

* `withComponentType(models.JavaComponentType)` was removed
* `withConfigurations(java.util.List)` was removed
* `withServiceBinds(java.util.List)` was removed

#### `models.JavaComponent` was modified

* `provisioningState()` was removed
* `serviceBinds()` was removed
* `componentType()` was removed
* `configurations()` was removed

#### `models.JavaComponent$Update` was modified

* `withServiceBinds(java.util.List)` was removed
* `withComponentType(models.JavaComponentType)` was removed
* `withConfigurations(java.util.List)` was removed

### Features Added

* `models.SessionContainer` was added

* `models.ContainerAppsBuilds` was added

* `models.LogicApp$Definition` was added

* `models.Runtime` was added

* `models.SessionPoolUpdatableProperties` was added

* `models.LoggerSetting` was added

* `models.ScaleConfiguration` was added

* `models.ManagedEnvironmentPrivateEndpointConnections` was added

* `models.RuntimeJavaAgentLogging` was added

* `models.WorkflowState` was added

* `models.PrivateEndpointConnectionListResult` was added

* `models.PoolManagementType` was added

* `models.PatchType` was added

* `models.SmbStorage` was added

* `models.IdentitySettingsLifeCycle` was added

* `models.SessionPoolProvisioningState` was added

* `models.SpringBootAdminComponent` was added

* `models.PrivateEndpoint` was added

* `models.PatchDetailsOldLayer` was added

* `models.PatchApplyStatus` was added

* `models.ContainerAppsPatches` was added

* `models.LogicApps` was added

* `models.PrivateEndpointConnection$Definition` was added

* `models.SessionPool$UpdateStages` was added

* `models.SpringCloudConfigComponent` was added

* `models.PrivateEndpointConnection` was added

* `models.FunctionsExtensions` was added

* `models.SessionPool$Definition` was added

* `models.ImageType` was added

* `models.PatchCollection` was added

* `models.WorkflowEnvelopeProperties` was added

* `models.WorkflowHealthState` was added

* `models.ContainerAppsPatchResource` was added

* `models.SessionPool$DefinitionStages` was added

* `models.PrivateEndpointServiceConnectionStatus` was added

* `models.SessionIngress` was added

* `models.ContainerAppsBuildConfiguration` was added

* `models.WorkflowEnvelopeCollection` was added

* `models.PrivateEndpointConnection$Update` was added

* `models.ContainerAppsBuildCollection` was added

* `models.ErrorEntity` was added

* `models.LogicApp$UpdateStages` was added

* `models.PrivateLinkServiceConnectionState` was added

* `models.WorkflowHealth` was added

* `models.PatchProperties` was added

* `models.PatchDetails` was added

* `models.PrivateEndpointConnectionProvisioningState` was added

* `models.SessionPool` was added

* `models.PrivateLinkResource` was added

* `models.SessionContainerResources` was added

* `models.SessionPoolSecret` was added

* `models.PatchSkipConfig` was added

* `models.SessionPool$Update` was added

* `models.WorkflowEnvelope` was added

* `models.PrivateLinkResourceListResult` was added

* `models.Level` was added

* `models.SessionNetworkStatus` was added

* `models.LogicAppsProxyMethod` was added

* `models.ManagedEnvironmentPrivateLinkResources` was added

* `models.RuntimeJava` was added

* `models.ManagedEnvironmentPropertiesPeerTrafficConfiguration` was added

* `models.ExecutionType` was added

* `models.LogicApp` was added

* `models.PatchDetailsNewLayer` was added

* `models.DetectionStatus` was added

* `models.ReplicaExecutionStatus` was added

* `models.ContainerExecutionStatus` was added

* `models.ContainerAppsBuildsByContainerApps` was added

* `models.CustomContainerTemplate` was added

* `models.WorkflowArtifacts` was added

* `models.SessionNetworkConfiguration` was added

* `models.RuntimeJavaAgent` was added

* `models.DynamicPoolConfiguration` was added

* `models.ManagedEnvironmentPropertiesPeerTrafficConfigurationEncryption` was added

* `models.PatchingMode` was added

* `models.ContainerAppsBuildResource` was added

* `models.PrivateEndpointConnection$UpdateStages` was added

* `models.IdentitySettings` was added

* `models.ExecutionStatus` was added

* `models.ContainerType` was added

* `models.SessionRegistryCredentials` was added

* `models.RuntimeDotnet` was added

* `models.JavaComponentProperties` was added

* `models.Kind` was added

* `models.ContainerAppsSessionPools` was added

* `models.SessionPoolCollection` was added

* `models.ContainerAppPropertiesPatchingConfiguration` was added

* `models.LogicApp$Update` was added

* `models.NacosComponent` was added

* `models.LogicApp$DefinitionStages` was added

* `models.JavaComponentIngress` was added

* `models.PublicNetworkAccess` was added

* `models.PrivateEndpointConnection$DefinitionStages` was added

* `models.SpringCloudEurekaComponent` was added

#### `models.JobConfigurationScheduleTriggerConfig` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DaprComponentResiliencyPolicyTimeoutPolicyConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.HttpConnectionPool` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ManagedCertificatePatch` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ServiceBind` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Secret` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureActiveDirectoryValidation` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WorkloadProfileStatesCollection` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DaprComponentsCollection` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BuildConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ManagedCertificateCollection` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ManagedEnvironment$Update` was modified

* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added
* `withPeerTrafficConfiguration(models.ManagedEnvironmentPropertiesPeerTrafficConfiguration)` was added

#### `models.LoginScopes` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AvailableWorkloadProfileProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.HttpSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CertificateKeyVaultProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Google` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AllowedPrincipals` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EnvironmentVariable` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DotNetComponentServiceBind` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ContainerAppProbeHttpGetHttpHeadersItem` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.KedaConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DiagnosticsDefinition` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.JavaComponentServiceBind` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.GlobalValidation` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureActiveDirectoryLogin` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.LogsConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.WorkloadProfile` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.InitContainer` was modified

* `withImageType(models.ImageType)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EnvironmentVar` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CertificatePatch` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UsageName` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AvailableWorkloadProfilesCollection` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TrafficWeight` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.JobScale` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.IpSecurityRestrictionRule` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AppleRegistration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.QueueScaleRule` was modified

* `identity()` was added
* `accountName()` was added
* `withAccountName(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withIdentity(java.lang.String)` was added

#### `models.HttpGet` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CheckNameAvailabilityRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.JobsCollection` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OpenIdConnectLogin` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.UserAssignedIdentity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.JavaComponentConfigurationProperty` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Jobs` was modified

* `listDetectors(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.ConnectedEnvironmentStorageProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `withSmb(models.SmbStorage)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `smb()` was added

#### `models.ListUsagesResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AppRegistration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ContainerAppSecret` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BuilderResourceUpdate` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Service` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BillingMeterProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ManagedEnvironment$Definition` was modified

* `withPeerTrafficConfiguration(models.ManagedEnvironmentPropertiesPeerTrafficConfiguration)` was added
* `withPublicNetworkAccess(models.PublicNetworkAccess)` was added

#### `models.ContainerApp$Update` was modified

* `withPatchingConfiguration(models.ContainerAppPropertiesPatchingConfiguration)` was added

#### `models.ManagedEnvironmentStorageProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OpenIdConnectConfig` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BillingMeter` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `id()` was added
* `name()` was added
* `type()` was added

#### `models.DaprComponentResiliencyPoliciesCollection` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.GithubActionConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ContainerRegistry` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.JobPatchPropertiesProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AllowedAudiencesValidation` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ScaleRule` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ClientRegistration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DaprComponentResiliencyPolicyHttpRetryBackOffConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CustomHostnameAnalysisResultCustomDomainVerificationFailureInfoDetailsItem` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ContainerAppCollection` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OtlpConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ForwardProxy` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MetricsConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DaprServiceBindMetadata` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.IdentityProviders` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ContainerAppProbe` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ScaleRuleAuth` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.JobConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `identitySettings()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withIdentitySettings(java.util.List)` was added

#### `models.TcpScaleRule` was modified

* `identity()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withIdentity(java.lang.String)` was added

#### `models.TracesConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SecretVolumeItem` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.JobConfigurationManualTriggerConfig` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DaprSubscriptionBulkSubscribeOptions` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.JobConfigurationEventTriggerConfig` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ConnectedEnvironmentCollection` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DaprComponentResiliencyPolicyCircuitBreakerPolicyConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.JavaComponent$Definition` was modified

* `withProperties(models.JavaComponentProperties)` was added

#### `models.Scale` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CustomDomain` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CertificateCollection` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TimeoutPolicy` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.IngressStickySessions` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DotNetComponentConfigurationProperty` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DaprSubscriptionsCollection` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TokenStore` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.VnetConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AppInsightsConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.LogAnalyticsConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DaprSubscriptionRoutes` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Mtls` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.HttpRetryPolicy` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.NfsAzureFileProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ManagedEnvironment` was modified

* `peerTrafficConfiguration()` was added
* `privateEndpointConnections()` was added
* `publicNetworkAccess()` was added

#### `models.CircuitBreakerPolicy` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TcpRetryPolicy` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureFileProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.HeaderMatch` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AppResiliencyCollection` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AuthPlatform` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CorsPolicy` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BuilderCollection` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RegistryCredentials` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DaprComponentResiliencyPolicyHttpRetryPolicyConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DaprComponentResiliencyPolicyConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OpenIdConnectClientCredential` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.JobTemplate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureActiveDirectoryRegistration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.JavaComponentsCollection` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ManagedServiceIdentity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DefaultAuthorizationPolicy` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DefaultErrorResponseError` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureCredentials` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OpenTelemetryConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WorkloadProfileStatesProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AvailableOperations` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.JavaComponent` was modified

* `properties()` was added

#### `models.HttpScaleRule` was modified

* `withIdentity(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `identity()` was added

#### `models.AzureActiveDirectory` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Login` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DiagnosticsDataApiResponse` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.JobExecution` was modified

* `detailedStatus()` was added

#### `models.ManagedCertificateProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DestinationsConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.JobExecutionContainer` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DataDogConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DotNetComponentsCollection` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureStaticWebApps` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DaprConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.JobPatchProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureStaticWebAppsRegistration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TwitterRegistration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.RevisionCollection` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EncryptionSettings` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Twitter` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BuildCollection` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Container` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withImageType(models.ImageType)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CustomHostnameAnalysisResultCustomDomainVerificationFailureInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Facebook` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CookieExpiration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CustomOpenIdConnectProvider` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CertificateProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DiagnosticRendering` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PreBuildStep` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Apple` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DaprComponentServiceBinding` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DiagnosticSupportTopic` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ContainerApp` was modified

* `kind()` was added
* `patchingConfiguration()` was added

#### `models.JwtClaimChecks` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ContainerAppProbeHttpGet` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.JobScaleRule` was modified

* `withIdentity(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `identity()` was added

#### `models.RegistryInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Volume` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DaprSecret` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DaprMetadata` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.JobExecutionTemplate` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DiagnosticsProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `ContainerAppsApiManager` was modified

* `containerAppsBuilds()` was added
* `containerAppsSessionPools()` was added
* `managedEnvironmentPrivateEndpointConnections()` was added
* `logicApps()` was added
* `containerAppsBuildsByContainerApps()` was added
* `functionsExtensions()` was added
* `managedEnvironmentPrivateLinkResources()` was added
* `containerAppsPatches()` was added

#### `models.IngressPortMapping` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DiagnosticsStatus` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ReplicaContainer` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BaseContainer` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `imageType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withImageType(models.ImageType)` was added

#### `models.ContainerRegistryWithCustomImage` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BlobStorageTokenStore` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.VolumeMount` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DiagnosticDataProviderMetadata` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ExtendedLocation` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DiagnosticDataTableResponseObject` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ManagedEnvironmentPropertiesPeerAuthentication` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DiagnosticDataProviderMetadataPropertyBagItem` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AppLogsConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SourceControlCollection` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.GitHub` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ManagedEnvironmentsCollection` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CustomScaleRule` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `withIdentity(java.lang.String)` was added
* `identity()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AuthConfigCollection` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Nonce` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ContainerApp$Definition` was modified

* `withPatchingConfiguration(models.ContainerAppPropertiesPatchingConfiguration)` was added
* `withKind(models.Kind)` was added

#### `models.ContainerResources` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OpenIdConnectRegistration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Ingress` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CustomDomainConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.JavaComponent$Update` was modified

* `withProperties(models.JavaComponentProperties)` was added

#### `models.Dapr` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Header` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TcpConnectionPool` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DaprSubscriptionRouteRule` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.HttpSettingsRoutes` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DiagnosticDataTableResponseColumn` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Configuration` was modified

* `withIdentitySettings(java.util.List)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `runtime()` was added
* `withRuntime(models.Runtime)` was added
* `identitySettings()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Template` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ContainerAppProbeTcpSocket` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.LoginRoutes` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

## 1.0.0-beta.7 (2024-03-20)

- Azure Resource Manager ContainerAppsApi client library for Java. This package contains Microsoft Azure SDK for ContainerAppsApi Management SDK.  Package tag package-preview-2023-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.DotNetComponent$UpdateStages` was added

* `models.DaprComponentResiliencyPolicyTimeoutPolicyConfiguration` was added

* `models.HttpConnectionPool` was added

* `models.Usage` was added

* `models.BuildResource$UpdateStages` was added

* `models.DotNetComponents` was added

* `models.BuildProvisioningState` was added

* `models.AppResiliency` was added

* `models.DaprSubscriptions` was added

* `models.BuilderResource$Update` was added

* `models.BuildConfiguration` was added

* `models.BuildResource$Update` was added

* `models.CertificateKeyVaultProperties` was added

* `models.EnvironmentVariable` was added

* `models.DotNetComponentServiceBind` was added

* `models.AppResiliencies` was added

* `models.JavaComponentServiceBind` was added

* `models.LogsConfiguration` was added

* `models.Usages` was added

* `models.DotNetComponent$Update` was added

* `models.UsageName` was added

* `models.DaprSubscription$UpdateStages` was added

* `models.DotNetComponent$DefinitionStages` was added

* `models.HttpGet` was added

* `models.JavaComponentConfigurationProperty` was added

* `models.ListUsagesResult` was added

* `models.DotNetComponentProvisioningState` was added

* `models.DotNetComponentType` was added

* `models.BuilderResourceUpdate` was added

* `models.AppResiliency$DefinitionStages` was added

* `models.DaprComponentResiliencyPoliciesCollection` was added

* `models.BuildAuthTokens` was added

* `models.ContainerRegistry` was added

* `models.IngressTargetPortHttpScheme` was added

* `models.AppResiliency$Definition` was added

* `models.DaprComponentResiliencyPolicyHttpRetryBackOffConfiguration` was added

* `models.DaprSubscription$DefinitionStages` was added

* `models.OtlpConfiguration` was added

* `models.MetricsConfiguration` was added

* `models.DaprServiceBindMetadata` was added

* `models.TracesConfiguration` was added

* `models.DaprSubscriptionBulkSubscribeOptions` was added

* `models.DaprComponentResiliencyPolicyCircuitBreakerPolicyConfiguration` was added

* `models.JavaComponent$Definition` was added

* `models.BuildsByBuilderResources` was added

* `models.BuildResource` was added

* `models.TimeoutPolicy` was added

* `models.DotNetComponentConfigurationProperty` was added

* `models.DaprSubscriptionsCollection` was added

* `models.TokenStore` was added

* `models.AppInsightsConfiguration` was added

* `models.DaprSubscriptionRoutes` was added

* `models.DaprComponentResiliencyPolicy$Definition` was added

* `models.HttpRetryPolicy` was added

* `models.NfsAzureFileProperties` was added

* `models.CircuitBreakerPolicy` was added

* `models.DaprComponentResiliencyPolicy$DefinitionStages` was added

* `models.TcpRetryPolicy` was added

* `models.DaprComponentResiliencyPolicies` was added

* `models.HeaderMatch` was added

* `models.AppResiliencyCollection` was added

* `models.DotNetComponent` was added

* `models.BuilderCollection` was added

* `models.BuildStatus` was added

* `models.DaprSubscription` was added

* `models.DaprComponentResiliencyPolicyHttpRetryPolicyConfiguration` was added

* `models.BuildToken` was added

* `models.DaprComponentResiliencyPolicyConfiguration` was added

* `models.BuilderResource$DefinitionStages` was added

* `models.JavaComponentsCollection` was added

* `models.OpenTelemetryConfiguration` was added

* `models.Builders` was added

* `models.DaprComponentResiliencyPolicy$Update` was added

* `models.BuildResource$DefinitionStages` was added

* `models.JavaComponent` was added

* `models.AppResiliency$Update` was added

* `models.DaprSubscription$Update` was added

* `models.DestinationsConfiguration` was added

* `models.CertificateType` was added

* `models.DataDogConfiguration` was added

* `models.DotNetComponentsCollection` was added

* `models.ManagedEnvironmentUsages` was added

* `models.DotNetComponent$Definition` was added

* `models.EncryptionSettings` was added

* `models.DaprComponentResiliencyPolicy$UpdateStages` was added

* `models.BuildCollection` was added

* `models.JavaComponent$UpdateStages` was added

* `models.JavaComponentProvisioningState` was added

* `models.PreBuildStep` was added

* `models.DaprComponentServiceBinding` was added

* `models.BuilderResource$UpdateStages` was added

* `models.JavaComponent$DefinitionStages` was added

* `models.DaprComponentResiliencyPolicy` was added

* `models.IngressPortMapping` was added

* `models.ContainerRegistryWithCustomImage` was added

* `models.BlobStorageTokenStore` was added

* `models.JavaComponentType` was added

* `models.AppResiliency$UpdateStages` was added

* `models.BuilderResource` was added

* `models.BuildResource$Definition` was added

* `models.Builds` was added

* `models.BuilderResource$Definition` was added

* `models.JavaComponent$Update` was added

* `models.Header` was added

* `models.TcpConnectionPool` was added

* `models.JavaComponents` was added

* `models.DaprSubscriptionRouteRule` was added

* `models.DaprSubscription$Definition` was added

* `models.BuilderProvisioningState` was added

#### `models.Replica` was modified

* `systemData()` was added

#### `models.ServiceBind` was modified

* `withCustomizedKeys(java.util.Map)` was added
* `clientType()` was added
* `customizedKeys()` was added
* `withClientType(java.lang.String)` was added

#### `models.Job` was modified

* `extendedLocation()` was added

#### `models.DaprComponent$Definition` was modified

* `withServiceComponentBind(java.util.List)` was added

#### `models.ManagedEnvironment$Update` was modified

* `withOpenTelemetryConfiguration(models.OpenTelemetryConfiguration)` was added
* `withAppInsightsConfiguration(models.AppInsightsConfiguration)` was added
* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.Revision` was modified

* `systemData()` was added

#### `models.AvailableWorkloadProfileProperties` was modified

* `withGpus(java.lang.Integer)` was added
* `gpus()` was added

#### `models.Job$Definition` was modified

* `withExtendedLocation(models.ExtendedLocation)` was added

#### `models.Jobs` was modified

* `listDetectors(java.lang.String,java.lang.String)` was added
* `listDetectorsWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `proxyGet(java.lang.String,java.lang.String)` was added
* `getDetectorWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `proxyGetWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getDetector(java.lang.String,java.lang.String,java.lang.String)` was added

#### `models.DaprComponent$Update` was modified

* `withServiceComponentBind(java.util.List)` was added

#### `models.ManagedEnvironment$Definition` was modified

* `withOpenTelemetryConfiguration(models.OpenTelemetryConfiguration)` was added
* `withIdentity(models.ManagedServiceIdentity)` was added
* `withAppInsightsConfiguration(models.AppInsightsConfiguration)` was added

#### `models.ManagedEnvironmentStorageProperties` was modified

* `nfsAzureFile()` was added
* `withNfsAzureFile(models.NfsAzureFileProperties)` was added

#### `models.BillingMeter` was modified

* `systemData()` was added

#### `models.GithubActionConfiguration` was modified

* `buildEnvironmentVariables()` was added
* `withBuildEnvironmentVariables(java.util.List)` was added

#### `models.DaprComponent` was modified

* `serviceComponentBind()` was added
* `systemData()` was added

#### `models.ManagedEnvironmentStorage` was modified

* `systemData()` was added

#### `models.LogAnalyticsConfiguration` was modified

* `dynamicJsonColumns()` was added
* `withDynamicJsonColumns(java.lang.Boolean)` was added

#### `models.Job$Update` was modified

* `withExtendedLocation(models.ExtendedLocation)` was added

#### `models.ManagedEnvironment` was modified

* `appInsightsConfiguration()` was added
* `identity()` was added
* `openTelemetryConfiguration()` was added

#### `models.WorkloadProfileStates` was modified

* `systemData()` was added

#### `models.Login` was modified

* `tokenStore()` was added
* `withTokenStore(models.TokenStore)` was added

#### `models.ResourceProviders` was modified

* `getCustomDomainVerificationId()` was added
* `getCustomDomainVerificationIdWithResponse(com.azure.core.util.Context)` was added

#### `models.JobPatchProperties` was modified

* `withExtendedLocation(models.ExtendedLocation)` was added
* `extendedLocation()` was added

#### `models.SourceControl` was modified

* `systemData()` was added

#### `models.CertificateProperties` was modified

* `withCertificateType(models.CertificateType)` was added
* `certificateType()` was added
* `withCertificateKeyVaultProperties(models.CertificateKeyVaultProperties)` was added
* `certificateKeyVaultProperties()` was added

#### `models.Diagnostics` was modified

* `systemData()` was added

#### `models.AvailableWorkloadProfile` was modified

* `systemData()` was added

#### `models.AuthConfig` was modified

* `encryptionSettings()` was added

#### `ContainerAppsApiManager` was modified

* `dotNetComponents()` was added
* `builds()` was added
* `buildsByBuilderResources()` was added
* `javaComponents()` was added
* `usages()` was added
* `daprSubscriptions()` was added
* `daprComponentResiliencyPolicies()` was added
* `buildAuthTokens()` was added
* `appResiliencies()` was added
* `managedEnvironmentUsages()` was added
* `builders()` was added

#### `models.ConnectedEnvironmentStorage` was modified

* `systemData()` was added

#### `models.AuthConfig$Update` was modified

* `withEncryptionSettings(models.EncryptionSettings)` was added

#### `models.Ingress` was modified

* `targetPortHttpScheme()` was added
* `additionalPortMappings()` was added
* `withAdditionalPortMappings(java.util.List)` was added
* `withTargetPortHttpScheme(models.IngressTargetPortHttpScheme)` was added

#### `models.CustomDomainConfiguration` was modified

* `certificateKeyVaultProperties()` was added
* `withCertificateKeyVaultProperties(models.CertificateKeyVaultProperties)` was added

#### `models.AuthConfig$Definition` was modified

* `withEncryptionSettings(models.EncryptionSettings)` was added

## 1.0.0-beta.6 (2023-08-21)

- Azure Resource Manager ContainerAppsApi client library for Java. This package contains Microsoft Azure SDK for ContainerAppsApi Management SDK.  Package tag package-2023-05. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.JobExecutionNamesCollection` was removed

#### `models.Replica` was modified

* `systemData()` was removed

#### `models.Job` was modified

* `stopMultipleExecutions(models.JobExecutionNamesCollection)` was removed
* `start(models.JobExecutionTemplate)` was removed
* `stopMultipleExecutions(models.JobExecutionNamesCollection,com.azure.core.util.Context)` was removed

#### `models.Revision` was modified

* `systemData()` was removed

#### `models.Jobs` was modified

* `stopMultipleExecutions(java.lang.String,java.lang.String,models.JobExecutionNamesCollection,com.azure.core.util.Context)` was removed
* `start(java.lang.String,java.lang.String,models.JobExecutionTemplate)` was removed
* `stopMultipleExecutions(java.lang.String,java.lang.String,models.JobExecutionNamesCollection)` was removed

#### `models.BillingMeter` was modified

* `systemData()` was removed

#### `models.DaprComponent` was modified

* `systemData()` was removed

#### `models.ManagedEnvironmentStorage` was modified

* `systemData()` was removed

#### `models.WorkloadProfileStates` was modified

* `systemData()` was removed

#### `models.SourceControl` was modified

* `systemData()` was removed

#### `models.Diagnostics` was modified

* `systemData()` was removed

#### `models.AvailableWorkloadProfile` was modified

* `systemData()` was removed

#### `models.ConnectedEnvironmentStorage` was modified

* `systemData()` was removed

### Features Added

* `models.ServiceBind` was added

* `models.RevisionRunningState` was added

* `models.JobScale` was added

* `models.Service` was added

* `models.ContainerAppContainerRunningState` was added

* `models.JobConfigurationEventTriggerConfig` was added

* `models.Mtls` was added

* `models.ResourceProviders` was added

* `models.JobScaleRule` was added

* `models.ContainerAppReplicaRunningState` was added

* `models.ManagedEnvironmentPropertiesPeerAuthentication` was added

#### `models.Replica` was modified

* `runningStateDetails()` was added
* `runningState()` was added
* `initContainers()` was added

#### `models.Job` was modified

* `stopMultipleExecutions(com.azure.core.util.Context)` was added
* `start()` was added
* `stopMultipleExecutions()` was added

#### `models.ManagedEnvironment$Update` was modified

* `withPeerAuthentication(models.ManagedEnvironmentPropertiesPeerAuthentication)` was added

#### `models.Revision` was modified

* `runningState()` was added

#### `models.Jobs` was modified

* `stopMultipleExecutions(java.lang.String,java.lang.String)` was added
* `stopMultipleExecutions(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `start(java.lang.String,java.lang.String)` was added

#### `models.ManagedEnvironment$Definition` was modified

* `withPeerAuthentication(models.ManagedEnvironmentPropertiesPeerAuthentication)` was added

#### `models.GithubActionConfiguration` was modified

* `githubPersonalAccessToken()` was added
* `withGithubPersonalAccessToken(java.lang.String)` was added

#### `models.JobConfiguration` was modified

* `eventTriggerConfig()` was added
* `withEventTriggerConfig(models.JobConfigurationEventTriggerConfig)` was added

#### `models.ManagedEnvironment` was modified

* `peerAuthentication()` was added

#### `models.AzureCredentials` was modified

* `withKind(java.lang.String)` was added
* `kind()` was added

#### `models.ContainerApp` was modified

* `start()` was added
* `stop(com.azure.core.util.Context)` was added
* `start(com.azure.core.util.Context)` was added
* `stop()` was added

#### `models.Volume` was modified

* `withMountOptions(java.lang.String)` was added
* `mountOptions()` was added

#### `ContainerAppsApiManager` was modified

* `resourceProviders()` was added

#### `models.ReplicaContainer` was modified

* `runningState()` was added
* `runningStateDetails()` was added

#### `models.VolumeMount` was modified

* `subPath()` was added
* `withSubPath(java.lang.String)` was added

#### `models.ContainerApps` was modified

* `start(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `start(java.lang.String,java.lang.String)` was added
* `stop(java.lang.String,java.lang.String)` was added
* `stop(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Configuration` was modified

* `service()` was added
* `withService(models.Service)` was added

#### `models.Template` was modified

* `serviceBinds()` was added
* `withServiceBinds(java.util.List)` was added
* `terminationGracePeriodSeconds()` was added
* `withTerminationGracePeriodSeconds(java.lang.Long)` was added

## 1.0.0-beta.5 (2023-05-16)

- Azure Resource Manager ContainerAppsApi client library for Java. This package contains Microsoft Azure SDK for ContainerAppsApi Management SDK.  Package tag package-preview-2022-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ManagedEnvironmentOutBoundType` was removed

* `models.ManagedEnvironmentOutboundSettings` was removed

* `models.Category` was removed

* `models.EnvironmentSkuProperties` was removed

* `models.SkuName` was removed

#### `models.Certificate$DefinitionStages` was modified

* `withExistingManagedEnvironment(java.lang.String,java.lang.String)` was removed in stage 2

#### `models.DaprComponent$DefinitionStages` was modified

* `withExistingManagedEnvironment(java.lang.String,java.lang.String)` was removed in stage 1

#### `models.DaprComponent$Definition` was modified

* `withExistingManagedEnvironment(java.lang.String,java.lang.String)` was removed

#### `models.ManagedEnvironment$Update` was modified

* `withSku(models.EnvironmentSkuProperties)` was removed

#### `models.AvailableWorkloadProfileProperties` was modified

* `billingMeterCategory()` was removed
* `withBillingMeterCategory(models.Category)` was removed

#### `models.WorkloadProfile` was modified

* `withMaximumCount(int)` was removed
* `withMinimumCount(int)` was removed
* `int minimumCount()` -> `java.lang.Integer minimumCount()`
* `int maximumCount()` -> `java.lang.Integer maximumCount()`

#### `models.ConnectedEnvironmentsDaprComponents` was modified

* `createOrUpdateWithResponse(java.lang.String,java.lang.String,java.lang.String,fluent.models.DaprComponentInner,com.azure.core.util.Context)` was removed
* `createOrUpdate(java.lang.String,java.lang.String,java.lang.String,fluent.models.DaprComponentInner)` was removed

#### `models.BillingMeterProperties` was modified

* `models.Category category()` -> `java.lang.String category()`
* `withCategory(models.Category)` was removed

#### `models.ManagedEnvironment$Definition` was modified

* `withSku(models.EnvironmentSkuProperties)` was removed

#### `models.ContainerApp$Update` was modified

* `withWorkloadProfileType(java.lang.String)` was removed

#### `models.Certificate$Definition` was modified

* `withExistingManagedEnvironment(java.lang.String,java.lang.String)` was removed

#### `models.VnetConfiguration` was modified

* `runtimeSubnetId()` was removed
* `withRuntimeSubnetId(java.lang.String)` was removed
* `withOutboundSettings(models.ManagedEnvironmentOutboundSettings)` was removed
* `outboundSettings()` was removed

#### `models.ManagedEnvironment` was modified

* `sku()` was removed

#### `models.ManagedEnvironments` was modified

* `update(java.lang.String,java.lang.String,fluent.models.ManagedEnvironmentInner)` was removed
* `update(java.lang.String,java.lang.String,fluent.models.ManagedEnvironmentInner,com.azure.core.util.Context)` was removed

#### `models.ConnectedEnvironmentsCertificates` was modified

* `update(java.lang.String,java.lang.String,java.lang.String,models.CertificatePatch)` was removed
* `createOrUpdate(java.lang.String,java.lang.String,java.lang.String)` was removed
* `createOrUpdateWithResponse(java.lang.String,java.lang.String,java.lang.String,fluent.models.CertificateInner,com.azure.core.util.Context)` was removed
* `updateWithResponse(java.lang.String,java.lang.String,java.lang.String,models.CertificatePatch,com.azure.core.util.Context)` was removed

#### `models.DaprComponents` was modified

* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `getById(java.lang.String)` was removed
* `deleteById(java.lang.String)` was removed
* `define(java.lang.String)` was removed
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Certificates` was modified

* `define(java.lang.String)` was removed
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `deleteById(java.lang.String)` was removed
* `getById(java.lang.String)` was removed
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed

#### `models.ContainerApp` was modified

* `workloadProfileType()` was removed

#### `models.ContainerApps` was modified

* `update(java.lang.String,java.lang.String,fluent.models.ContainerAppInner)` was removed
* `update(java.lang.String,java.lang.String,fluent.models.ContainerAppInner,com.azure.core.util.Context)` was removed

#### `models.ContainerApp$Definition` was modified

* `withWorkloadProfileType(java.lang.String)` was removed

#### `models.CustomDomainConfiguration` was modified

* `byte[] certificatePassword()` -> `java.lang.String certificatePassword()`
* `withCertificatePassword(byte[])` was removed

### Features Added

* `models.Job$UpdateStages` was added

* `models.JobConfigurationScheduleTriggerConfig` was added

* `models.ManagedCertificatePatch` was added

* `models.Job` was added

* `models.ManagedCertificateCollection` was added

* `models.KedaConfiguration` was added

* `models.ManagedCertificates` was added

* `models.Job$Definition` was added

* `models.JobSecretsCollection` was added

* `models.JobsCollection` was added

* `models.Jobs` was added

* `models.JobProvisioningState` was added

* `models.JobPatchPropertiesProperties` was added

* `models.JobConfiguration` was added

* `models.ManagedCertificate$UpdateStages` was added

* `models.SecretVolumeItem` was added

* `models.JobConfigurationManualTriggerConfig` was added

* `models.ContainerAppJobExecutions` was added

* `models.IngressStickySessions` was added

* `models.Job$Update` was added

* `models.CorsPolicy` was added

* `models.JobExecutionBase` was added

* `models.JobTemplate` was added

* `models.JobsExecutions` was added

* `models.JobExecution` was added

* `models.ManagedCertificateProperties` was added

* `models.JobExecutionContainer` was added

* `models.DaprConfiguration` was added

* `models.JobPatchProperties` was added

* `models.ManagedCertificate$DefinitionStages` was added

* `models.ManagedCertificate$Update` was added

* `models.ManagedCertificate` was added

* `models.ManagedCertificate$Definition` was added

* `models.DaprSecret` was added

* `models.JobExecutionTemplate` was added

* `models.JobExecutionRunningState` was added

* `models.IngressClientCertificateMode` was added

* `models.Affinity` was added

* `models.JobExecutionNamesCollection` was added

* `models.Job$DefinitionStages` was added

* `models.ManagedCertificateDomainControlValidation` was added

* `models.TriggerType` was added

#### `models.DaprComponent$Definition` was modified

* `withExistingConnectedEnvironment(java.lang.String,java.lang.String)` was added

#### `models.Secret` was modified

* `keyVaultUrl()` was added
* `withIdentity(java.lang.String)` was added
* `withKeyVaultUrl(java.lang.String)` was added
* `identity()` was added

#### `models.ManagedEnvironment$Update` was modified

* `withDaprConfiguration(models.DaprConfiguration)` was added
* `withKind(java.lang.String)` was added
* `withKedaConfiguration(models.KedaConfiguration)` was added

#### `models.AvailableWorkloadProfileProperties` was modified

* `category()` was added
* `withCategory(java.lang.String)` was added

#### `models.WorkloadProfile` was modified

* `withMinimumCount(java.lang.Integer)` was added
* `name()` was added
* `withMaximumCount(java.lang.Integer)` was added
* `withName(java.lang.String)` was added

#### `models.ConnectedEnvironmentsDaprComponents` was modified

* `define(java.lang.String)` was added
* `getById(java.lang.String)` was added
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `deleteById(java.lang.String)` was added

#### `models.ContainerAppSecret` was modified

* `identity()` was added
* `keyVaultUrl()` was added

#### `models.BillingMeterProperties` was modified

* `withCategory(java.lang.String)` was added

#### `models.ManagedEnvironment$Definition` was modified

* `withInfrastructureResourceGroup(java.lang.String)` was added
* `withKind(java.lang.String)` was added
* `withKedaConfiguration(models.KedaConfiguration)` was added
* `withDaprConfiguration(models.DaprConfiguration)` was added

#### `models.ContainerApp$Update` was modified

* `withWorkloadProfileName(java.lang.String)` was added
* `withManagedBy(java.lang.String)` was added

#### `models.Certificate$Definition` was modified

* `withExistingConnectedEnvironment(java.lang.String,java.lang.String)` was added

#### `models.ManagedEnvironment` was modified

* `kedaConfiguration()` was added
* `kind()` was added
* `infrastructureResourceGroup()` was added
* `daprConfiguration()` was added

#### `models.ConnectedEnvironmentsCertificates` was modified

* `deleteById(java.lang.String)` was added
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `define(java.lang.String)` was added
* `getById(java.lang.String)` was added

#### `models.DaprComponents` was modified

* `createOrUpdate(java.lang.String,java.lang.String,java.lang.String,fluent.models.DaprComponentInner)` was added
* `createOrUpdateWithResponse(java.lang.String,java.lang.String,java.lang.String,fluent.models.DaprComponentInner,com.azure.core.util.Context)` was added

#### `models.Certificates` was modified

* `createOrUpdate(java.lang.String,java.lang.String,java.lang.String)` was added
* `updateWithResponse(java.lang.String,java.lang.String,java.lang.String,models.CertificatePatch,com.azure.core.util.Context)` was added
* `update(java.lang.String,java.lang.String,java.lang.String,models.CertificatePatch)` was added
* `createOrUpdateWithResponse(java.lang.String,java.lang.String,java.lang.String,fluent.models.CertificateInner,com.azure.core.util.Context)` was added

#### `models.ContainerApp` was modified

* `latestReadyRevisionName()` was added
* `managedBy()` was added
* `workloadProfileName()` was added

#### `models.Volume` was modified

* `secrets()` was added
* `withSecrets(java.util.List)` was added

#### `models.ConnectedEnvironment` was modified

* `checkNameAvailabilityWithResponse(models.CheckNameAvailabilityRequest,com.azure.core.util.Context)` was added
* `checkNameAvailability(models.CheckNameAvailabilityRequest)` was added

#### `ContainerAppsApiManager` was modified

* `jobsExecutions()` was added
* `managedCertificates()` was added
* `jobs()` was added

#### `models.ContainerApp$Definition` was modified

* `withManagedBy(java.lang.String)` was added
* `withWorkloadProfileName(java.lang.String)` was added

#### `models.Ingress` was modified

* `clientCertificateMode()` was added
* `stickySessions()` was added
* `withStickySessions(models.IngressStickySessions)` was added
* `withClientCertificateMode(models.IngressClientCertificateMode)` was added
* `corsPolicy()` was added
* `withCorsPolicy(models.CorsPolicy)` was added

#### `models.CustomDomainConfiguration` was modified

* `withCertificatePassword(java.lang.String)` was added

## 1.0.0-beta.4 (2022-10-12)

- Azure Resource Manager ContainerAppsApi client library for Java. This package contains Microsoft Azure SDK for ContainerAppsApi Management SDK.  Package tag package-preview-2022-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Container` was modified

* `env()` was removed
* `args()` was removed
* `image()` was removed
* `name()` was removed
* `command()` was removed
* `volumeMounts()` was removed
* `resources()` was removed

#### `models.CustomHostnameAnalysisResult` was modified

* `name()` was removed
* `systemData()` was removed
* `id()` was removed
* `models.DefaultErrorResponseError customDomainVerificationFailureInfo()` -> `models.CustomHostnameAnalysisResultCustomDomainVerificationFailureInfo customDomainVerificationFailureInfo()`
* `type()` was removed

### Features Added

* `models.LogLevel` was added

* `models.ConnectedEnvironmentStorage$Definition` was added

* `models.WorkloadProfileStatesCollection` was added

* `models.AvailableWorkloadProfiles` was added

* `models.WorkloadProfileStatesProperties` was added

* `models.ConnectedEnvironment$Update` was added

* `models.Category` was added

* `models.ConnectedEnvironmentsCertificates` was added

* `models.AvailableWorkloadProfileProperties` was added

* `models.ConnectedEnvironment$UpdateStages` was added

* `models.ManagedEnvironmentOutBoundType` was added

* `models.WorkloadProfileStates` was added

* `models.DiagnosticsDataApiResponse` was added

* `models.ConnectedEnvironmentsStorages` was added

* `models.ConnectedEnvironmentProvisioningState` was added

* `models.DiagnosticsDefinition` was added

* `models.ExtendedLocationTypes` was added

* `models.WorkloadProfile` was added

* `models.InitContainer` was added

* `models.CustomHostnameAnalysisResultCustomDomainVerificationFailureInfo` was added

* `models.AvailableWorkloadProfilesCollection` was added

* `models.DiagnosticRendering` was added

* `models.ConnectedEnvironmentStorage$DefinitionStages` was added

* `models.IpSecurityRestrictionRule` was added

* `models.ConnectedEnvironmentsDaprComponents` was added

* `models.EnvironmentSkuProperties` was added

* `models.ConnectedEnvironmentStorage$UpdateStages` was added

* `models.ConnectedEnvironmentStorage$Update` was added

* `models.DiagnosticSupportTopic` was added

* `models.Diagnostics` was added

* `models.AvailableWorkloadProfile` was added

* `models.ConnectedEnvironmentStorageProperties` was added

* `models.ManagedEnvironmentsDiagnostics` was added

* `models.BillingMeterCollection` was added

* `models.ContainerAppsDiagnostics` was added

* `models.ConnectedEnvironment` was added

* `models.BillingMeterProperties` was added

* `models.BillingMeter` was added

* `models.ConnectedEnvironment$Definition` was added

* `models.DiagnosticsProperties` was added

* `models.ConnectedEnvironment$DefinitionStages` was added

* `models.SkuName` was added

* `models.ConnectedEnvironments` was added

* `models.ManagedEnvironmentOutboundSettings` was added

* `models.CustomHostnameAnalysisResultCustomDomainVerificationFailureInfoDetailsItem` was added

* `models.ContainerAppAuthToken` was added

* `models.DiagnosticsStatus` was added

* `models.BaseContainer` was added

* `models.Applicability` was added

* `models.DiagnosticsCollection` was added

* `models.DiagnosticDataProviderMetadata` was added

* `models.Action` was added

* `models.TcpScaleRule` was added

* `models.EnvironmentAuthToken` was added

* `models.ExtendedLocation` was added

* `models.DiagnosticDataTableResponseObject` was added

* `models.DiagnosticDataProviderMetadataPropertyBagItem` was added

* `models.ConnectedEnvironmentCollection` was added

* `models.ConnectedEnvironmentStorage` was added

* `models.BillingMeters` was added

* `models.ManagedEnvironmentDiagnostics` was added

* `models.ConnectedEnvironmentStoragesCollection` was added

* `models.CustomDomainConfiguration` was added

* `models.DiagnosticDataTableResponseColumn` was added

#### `models.ManagedEnvironments` was modified

* `listWorkloadProfileStates(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getAuthToken(java.lang.String,java.lang.String)` was added
* `listWorkloadProfileStates(java.lang.String,java.lang.String)` was added
* `getAuthTokenWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.DaprComponent$Definition` was modified

* `withSecretStoreComponent(java.lang.String)` was added

#### `models.ManagedEnvironment$Update` was modified

* `withDaprAIConnectionString(java.lang.String)` was added
* `withCustomDomainConfiguration(models.CustomDomainConfiguration)` was added
* `withSku(models.EnvironmentSkuProperties)` was added
* `withDaprAIInstrumentationKey(java.lang.String)` was added
* `withWorkloadProfiles(java.util.List)` was added

#### `models.Revision` was modified

* `lastActiveTime()` was added

#### `models.Container` was modified

* `withVolumeMounts(java.util.List)` was added
* `withImage(java.lang.String)` was added
* `withEnv(java.util.List)` was added
* `withResources(models.ContainerResources)` was added
* `withArgs(java.util.List)` was added
* `withCommand(java.util.List)` was added
* `withName(java.lang.String)` was added

#### `models.CertificateProperties` was modified

* `subjectAlternativeNames()` was added

#### `models.ContainerApp` was modified

* `getAuthToken()` was added
* `systemData()` was added
* `eventStreamEndpoint()` was added
* `environmentId()` was added
* `workloadProfileType()` was added
* `getAuthTokenWithResponse(com.azure.core.util.Context)` was added
* `extendedLocation()` was added

#### `models.DaprComponent$Update` was modified

* `withSecretStoreComponent(java.lang.String)` was added

#### `models.CustomHostnameAnalysisResult` was modified

* `conflictWithEnvironmentCustomDomain()` was added

#### `models.AuthConfig` was modified

* `systemData()` was added

#### `models.ManagedEnvironment$Definition` was modified

* `withWorkloadProfiles(java.util.List)` was added
* `withCustomDomainConfiguration(models.CustomDomainConfiguration)` was added
* `withSku(models.EnvironmentSkuProperties)` was added

#### `models.ContainerApp$Update` was modified

* `withWorkloadProfileType(java.lang.String)` was added
* `withExtendedLocation(models.ExtendedLocation)` was added

#### `models.ScaleRule` was modified

* `tcp()` was added
* `withTcp(models.TcpScaleRule)` was added

#### `ContainerAppsApiManager` was modified

* `managedEnvironmentsDiagnostics()` was added
* `availableWorkloadProfiles()` was added
* `managedEnvironmentDiagnostics()` was added
* `connectedEnvironmentsCertificates()` was added
* `billingMeters()` was added
* `connectedEnvironments()` was added
* `connectedEnvironmentsDaprComponents()` was added
* `containerAppsDiagnostics()` was added
* `connectedEnvironmentsStorages()` was added

#### `models.ReplicaContainer` was modified

* `execEndpoint()` was added
* `logStreamEndpoint()` was added

#### `models.DaprComponent` was modified

* `secretStoreComponent()` was added

#### `models.ContainerApps` was modified

* `getAuthTokenWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getAuthToken(java.lang.String,java.lang.String)` was added

#### `models.ContainerApp$Definition` was modified

* `withExtendedLocation(models.ExtendedLocation)` was added
* `withWorkloadProfileType(java.lang.String)` was added
* `withEnvironmentId(java.lang.String)` was added

#### `models.Ingress` was modified

* `ipSecurityRestrictions()` was added
* `withExposedPort(java.lang.Integer)` was added
* `withIpSecurityRestrictions(java.util.List)` was added
* `exposedPort()` was added

#### `models.VnetConfiguration` was modified

* `outboundSettings()` was added
* `withOutboundSettings(models.ManagedEnvironmentOutboundSettings)` was added

#### `models.Dapr` was modified

* `enableApiLogging()` was added
* `httpMaxRequestSize()` was added
* `withHttpMaxRequestSize(java.lang.Integer)` was added
* `logLevel()` was added
* `withEnableApiLogging(java.lang.Boolean)` was added
* `withHttpReadBufferSize(java.lang.Integer)` was added
* `withLogLevel(models.LogLevel)` was added
* `httpReadBufferSize()` was added

#### `models.Configuration` was modified

* `maxInactiveRevisions()` was added
* `withMaxInactiveRevisions(java.lang.Integer)` was added

#### `models.Template` was modified

* `withInitContainers(java.util.List)` was added
* `initContainers()` was added

#### `models.ManagedEnvironment` was modified

* `sku()` was added
* `customDomainConfiguration()` was added
* `getAuthTokenWithResponse(com.azure.core.util.Context)` was added
* `getAuthToken()` was added
* `eventStreamEndpoint()` was added
* `workloadProfiles()` was added

## 1.0.0-beta.3 (2022-05-25)

- Azure Resource Manager ContainerAppsApi client library for Java. This package contains Microsoft Azure SDK for ContainerAppsApi Management SDK.  Package tag package-2022-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.Certificate` was modified

* `resourceGroupName()` was added

#### `models.SourceControl` was modified

* `resourceGroupName()` was added

#### `models.ContainerApp` was modified

* `resourceGroupName()` was added
* `listCustomHostnameAnalysisWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `listCustomHostnameAnalysis()` was added

#### `models.AuthConfig` was modified

* `resourceGroupName()` was added

#### `models.DaprComponent` was modified

* `resourceGroupName()` was added

#### `models.ManagedEnvironmentStorage` was modified

* `resourceGroupName()` was added

#### `models.ManagedEnvironment` was modified

* `resourceGroupName()` was added

## 1.0.0-beta.2 (2022-05-10)

- Azure Resource Manager ContainerAppsApi client library for Java. This package contains Microsoft Azure SDK for ContainerAppsApi Management SDK.  Package tag package-2022-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ManagedEnvironmentPatch` was removed

* `models.ContainerAppPatch` was removed

#### `models.ContainerAppsRevisions` was modified

* `listRevisions(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.ContainerAppProbeHttpGet` was modified

* `java.lang.String scheme()` -> `models.Scheme scheme()`
* `withScheme(java.lang.String)` was removed

#### `models.AuthConfig` was modified

* `systemData()` was removed

#### `models.GithubActionConfiguration` was modified

* `dockerfilePath()` was removed
* `withDockerfilePath(java.lang.String)` was removed

### Features Added

* `models.Namespaces` was added

* `models.CheckNameAvailabilityResponse` was added

* `models.CheckNameAvailabilityRequest` was added

* `models.DaprSecretsCollection` was added

* `models.Scheme` was added

* `models.CheckNameAvailabilityReason` was added

#### `models.ManagedEnvironments` was modified

* `update(java.lang.String,java.lang.String,fluent.models.ManagedEnvironmentInner,com.azure.core.util.Context)` was added
* `update(java.lang.String,java.lang.String,fluent.models.ManagedEnvironmentInner)` was added

#### `models.RegistryCredentials` was modified

* `withIdentity(java.lang.String)` was added
* `identity()` was added

#### `models.ManagedEnvironment$Update` was modified

* `withVnetConfiguration(models.VnetConfiguration)` was added
* `withAppLogsConfiguration(models.AppLogsConfiguration)` was added

#### `models.DaprComponents` was modified

* `listSecrets(java.lang.String,java.lang.String,java.lang.String)` was added
* `listSecretsWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.ContainerAppsRevisions` was modified

* `listRevisions(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.TrafficWeight` was modified

* `label()` was added
* `withLabel(java.lang.String)` was added

#### `models.ContainerAppProbeHttpGet` was modified

* `withScheme(models.Scheme)` was added

#### `models.CustomHostnameAnalysisResult` was modified

* `systemData()` was added

#### `models.ManagedEnvironment$Definition` was modified

* `withDaprAIConnectionString(java.lang.String)` was added
* `withZoneRedundant(java.lang.Boolean)` was added

#### `models.ContainerApp$Update` was modified

* `withConfiguration(models.Configuration)` was added
* `withTemplate(models.Template)` was added
* `withIdentity(models.ManagedServiceIdentity)` was added

#### `models.GithubActionConfiguration` was modified

* `image()` was added
* `withImage(java.lang.String)` was added
* `contextPath()` was added
* `withContextPath(java.lang.String)` was added

#### `ContainerAppsApiManager` was modified

* `namespaces()` was added

#### `models.DaprComponent` was modified

* `listSecretsWithResponse(com.azure.core.util.Context)` was added
* `listSecrets()` was added

#### `models.ContainerApps` was modified

* `update(java.lang.String,java.lang.String,fluent.models.ContainerAppInner)` was added
* `update(java.lang.String,java.lang.String,fluent.models.ContainerAppInner,com.azure.core.util.Context)` was added

#### `models.ManagedEnvironment` was modified

* `daprAIConnectionString()` was added
* `zoneRedundant()` was added

## 1.0.0-beta.1 (2022-04-28)

- Azure Resource Manager ContainerAppsApi client library for Java. This package contains Microsoft Azure SDK for ContainerAppsApi Management SDK.  Package tag package-2022-01-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
