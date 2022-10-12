# Release History

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
