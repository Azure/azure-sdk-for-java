# Release History

## 1.0.0-beta.7 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
