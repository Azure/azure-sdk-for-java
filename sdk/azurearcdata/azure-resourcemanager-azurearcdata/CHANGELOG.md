# Release History

## 1.1.0-beta.1 (2025-06-11)

- Azure Resource Manager AzureArcData client library for Java. This package contains Microsoft Azure SDK for AzureArcData Management SDK. The AzureArcData management API provides a RESTful set of web APIs to manage Azure Data Services on Azure Arc Resources. Package tag package-preview-2025-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ArcSqlManagedInstanceLicenseType` was modified

* `valueOf(java.lang.String)` was removed
* `models.ArcSqlManagedInstanceLicenseType[] values()` -> `java.util.Collection values()`
* `toString()` was removed

#### `models.SqlServerInstanceProperties` was modified

* `withProductId(java.lang.String)` was removed
* `withVCore(java.lang.String)` was removed
* `withTcpStaticPorts(java.lang.String)` was removed
* `withCurrentVersion(java.lang.String)` was removed
* `withAzureDefenderStatusLastUpdated(java.time.OffsetDateTime)` was removed
* `withLicenseType(models.ArcSqlServerLicenseType)` was removed
* `withTcpDynamicPorts(java.lang.String)` was removed
* `withPatchLevel(java.lang.String)` was removed
* `withStatus(models.ConnectionStatus)` was removed
* `withAzureDefenderStatus(models.DefenderStatus)` was removed
* `withCollation(java.lang.String)` was removed
* `withContainerResourceId(java.lang.String)` was removed

### Features Added

* `models.ArcSqlServerAvailabilityMode` was added

* `models.SqlServerAvailabilityGroupResource` was added

* `models.K8SSettings` was added

* `models.FailoverGroupResource$Definition` was added

* `models.SqlServerAvailabilityGroupResourceProperties` was added

* `models.AlwaysOnRole` was added

* `models.State` was added

* `models.LicenseCategory` was added

* `models.LastExecutionStatus` was added

* `models.PostgresInstances` was added

* `models.ActivationState` was added

* `models.SqlServerDatabaseResource$UpdateStages` was added

* `models.AzureManagedInstanceRole` was added

* `models.FailoverGroupListResult` was added

* `models.SkuRecommendationResultsAzureSqlManagedInstance` was added

* `models.DbFailover` was added

* `models.SqlServerLicenseProperties` was added

* `models.Version` was added

* `models.SkuRecommendationResultsAzureSqlVirtualMachineTargetSkuCategory` was added

* `models.SqlServerInstanceRunMigrationAssessmentResponse` was added

* `models.SequencerAction` was added

* `models.JobStatus` was added

* `models.SkuRecommendationResultsAzureSqlManagedInstanceTargetSku` was added

* `models.K8SSecurity` was added

* `models.SqlServerInstanceJobStatus` was added

* `models.SqlServerLicenseUpdateProperties` was added

* `models.SqlServerEsuLicenseListResult` was added

* `models.AggregationType` was added

* `models.PostgresInstanceProperties` was added

* `models.Authentication` was added

* `models.K8SActiveDirectoryConnector` was added

* `models.SqlServerAvailabilityGroupResourcePropertiesReplicas` was added

* `models.SqlServerEsuLicenseUpdateProperties` was added

* `models.SqlServerDatabaseUpdate` was added

* `models.SqlServerAvailabilityGroupResource$Definition` was added

* `models.ActiveDirectoryDomainControllers` was added

* `models.AvailabilityGroupInfo` was added

* `models.RecoveryMode` was added

* `models.SqlServerAvailabilityGroupResource$UpdateStages` was added

* `models.ActiveDirectoryConnectorListResult` was added

* `models.SqlServerAvailabilityGroups` was added

* `models.AutomatedBackupPreference` was added

* `models.ClientConnection` was added

* `models.SqlServerAvailabilityGroupResourcePropertiesDatabases` was added

* `models.SkuRecommendationResultsAzureSqlDatabaseTargetSkuCategory` was added

* `models.SqlAvailabilityGroupDatabaseReplicaResourceProperties` was added

* `models.ExecutionState` was added

* `models.ActiveDirectoryInformation` was added

* `models.Migration` was added

* `models.Role` was added

* `models.BackgroundJob` was added

* `models.SqlServerAvailabilityGroupUpdate` was added

* `models.SqlServerLicense` was added

* `models.FailureConditionLevel` was added

* `models.ServerAssessmentsPropertiesItemsItem` was added

* `models.DistributedAvailabilityGroupCreateUpdateConfiguration` was added

* `models.FailoverGroupPartnerSyncMode` was added

* `models.SqlServerDatabaseResource$Update` was added

* `models.ConnectionAuth` was added

* `models.ClusterType` was added

* `models.ProvisioningState` was added

* `models.PostgresInstance$UpdateStages` was added

* `models.SqlServerDatabaseResourcePropertiesBackupInformation` was added

* `models.ActiveDirectoryConnectorResource$DefinitionStages` was added

* `models.SqlServerInstanceManagedInstanceLinkAssessmentResponse` was added

* `models.K8StransparentDataEncryption` was added

* `models.Result` was added

* `models.DatabaseCreateMode` was added

* `models.ActiveDirectoryConnectorSpec` was added

* `models.MigrationAssessment` was added

* `models.AvailabilityGroupConfigure` was added

* `models.SqlServerInstanceTelemetryColumnType` was added

* `models.SqlServerLicense$Definition` was added

* `models.SkuRecommendationResultsAzureSqlManagedInstanceTargetSkuCategory` was added

* `models.FailoverGroupProperties` was added

* `models.SkuRecommendationResults` was added

* `models.ActiveDirectoryConnectorProperties` was added

* `models.PostgresInstance$Definition` was added

* `models.ActiveDirectoryDomainController` was added

* `models.ActiveDirectoryConnectors` was added

* `models.CommonSku` was added

* `models.FailoverGroupSpec` was added

* `models.IdentityType` was added

* `models.DbmEndpoint` was added

* `models.SqlServerLicenseUpdate` was added

* `models.ServiceType` was added

* `models.HostIpAddressInformation` was added

* `models.SqlServerDatabases` was added

* `models.SqlServerEsuLicense` was added

* `models.EncryptionAlgorithm` was added

* `models.SqlServerInstanceTelemetryColumn` was added

* `models.InstanceFailoverGroupRole` was added

* `models.SqlServerDatabaseResource` was added

* `models.SqlServerEsuLicense$DefinitionStages` was added

* `models.PostgresInstanceListResult` was added

* `models.PostgresInstanceUpdate` was added

* `models.FailoverCluster` was added

* `models.ActiveDirectoryConnectorStatus` was added

* `models.SqlServerEsuLicense$Definition` was added

* `models.BillingPlan` was added

* `models.RecommendationStatus` was added

* `models.SqlAvailabilityGroupIpV4AddressesAndMasksPropertiesItem` was added

* `models.PostgresInstance$Update` was added

* `models.SqlServerLicenses` was added

* `models.SqlServerDatabaseResourceProperties` was added

* `models.SqlServerEsuLicenseProperties` was added

* `models.FailoverGroups` was added

* `models.KeytabInformation` was added

* `models.SqlServerInstanceJobsStatusRequest` was added

* `models.SqlServerInstanceManagedInstanceLinkAssessment` was added

* `models.AssessmentStatus` was added

* `models.ActiveDirectoryConnectorResource` was added

* `models.PostgresInstance` was added

* `models.SqlServerLicense$DefinitionStages` was added

* `models.HostType` was added

* `models.Databases` was added

* `models.PostgresInstance$DefinitionStages` was added

* `models.SqlServerInstanceJobsStatusResponse` was added

* `models.ActiveDirectoryConnectorDomainDetails` was added

* `models.ArcSqlServerAvailabilityGroupListResult` was added

* `models.DatabaseState` was added

* `models.SqlServerEsuLicense$Update` was added

* `models.DataBaseMigration` was added

* `models.SequencerState` was added

* `models.ScopeType` was added

* `models.DataBaseMigrationAssessment` was added

* `models.SkuRecommendationSummary` was added

* `models.SqlServerInstanceUpdateProperties` was added

* `models.SqlServerDatabaseResource$Definition` was added

* `models.ServerAssessmentsItem` was added

* `models.DifferentialBackupHours` was added

* `models.SqlServerLicense$UpdateStages` was added

* `models.K8SActiveDirectory` was added

* `models.PostgresInstanceSku` was added

* `models.DtcSupport` was added

* `models.SqlServerDatabaseResource$DefinitionStages` was added

* `models.SqlServerInstanceTelemetryResponse` was added

* `models.EntraAuthentication` was added

* `models.AvailabilityGroupState` was added

* `models.PostgresInstanceSkuTier` was added

* `models.SqlServerEsuLicenses` was added

* `models.MiLinkCreateUpdateConfiguration` was added

* `models.AvailabilityGroupCreateUpdateConfiguration` was added

* `models.SkuRecommendationResultsAzureSqlDatabaseTargetSku` was added

* `models.AvailabilityGroupCreateUpdateReplicaConfiguration` was added

* `models.SkuRecommendationResultsAzureSqlDatabase` was added

* `models.SqlServerEsuLicenseUpdate` was added

* `models.SqlServerLicense$Update` was added

* `models.SecondaryAllowConnections` was added

* `models.FailoverGroupResource$DefinitionStages` was added

* `models.DistributedAvailabilityGroupCreateUpdateAvailabilityGroupCertificateConfiguration` was added

* `models.SqlServerEsuLicense$UpdateStages` was added

* `models.Mode` was added

* `models.SqlServerInstanceTelemetryRequest` was added

* `models.Monitoring` was added

* `models.TargetReadiness` was added

* `models.SkuRecommendationResultsAzureSqlVirtualMachine` was added

* `models.SqlServerAvailabilityGroupResource$Update` was added

* `models.FailoverMiLinkResourceId` was added

* `models.SkuRecommendationResultsAzureSqlVirtualMachineTargetSku` was added

* `models.SqlServerDatabaseResourcePropertiesDatabaseOptions` was added

* `models.DatabaseAssessmentsItem` was added

* `models.SqlAvailabilityGroupReplicaResourceProperties` was added

* `models.BackupPolicy` was added

* `models.FailoverGroupResource` was added

* `models.ManagedInstanceLinkCreateUpdateConfiguration` was added

* `models.ActiveDirectoryConnectorDnsDetails` was added

* `models.AssessmentCategory` was added

* `models.ActiveDirectoryConnectorResource$Definition` was added

* `models.ArcSqlServerDatabaseListResult` was added

* `models.PrimaryAllowConnections` was added

* `models.ReplicationPartnerType` was added

* `models.ArcSqlServerFailoverMode` was added

* `models.SqlServerInstanceManagedInstanceLinkAssessmentRequest` was added

* `models.K8SNetworkSettings` was added

* `models.SeedingMode` was added

* `models.SqlServerLicenseListResult` was added

* `models.SqlServerAvailabilityGroupResource$DefinitionStages` was added

* `models.DistributedAvailabilityGroupCreateUpdateAvailabilityGroupConfiguration` was added

* `models.AccountProvisioningMode` was added

* `models.SqlAvailabilityGroupStaticIpListenerProperties` was added

* `models.SkuRecommendationResultsMonthlyCost` was added

#### `models.DataControllerResource$Update` was modified

* `withProperties(models.DataControllerProperties)` was added

#### `models.DataControllerUpdate` was modified

* `withProperties(models.DataControllerProperties)` was added
* `properties()` was added

#### `models.SqlServerInstances` was modified

* `postUpgrade(java.lang.String,java.lang.String)` was added
* `runManagedInstanceLinkAssessment(java.lang.String,java.lang.String,models.SqlServerInstanceManagedInstanceLinkAssessmentRequest,com.azure.core.util.Context)` was added
* `runMigrationAssessment(java.lang.String,java.lang.String)` was added
* `getTelemetry(java.lang.String,java.lang.String,models.SqlServerInstanceTelemetryRequest,com.azure.core.util.Context)` was added
* `getJobsStatus(java.lang.String,java.lang.String)` was added
* `getJobsStatusWithResponse(java.lang.String,java.lang.String,models.SqlServerInstanceJobsStatusRequest,com.azure.core.util.Context)` was added
* `preUpgrade(java.lang.String,java.lang.String)` was added
* `preUpgradeWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `runMigrationAssessmentWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getTelemetry(java.lang.String,java.lang.String,models.SqlServerInstanceTelemetryRequest)` was added
* `runManagedInstanceLinkAssessment(java.lang.String,java.lang.String,models.SqlServerInstanceManagedInstanceLinkAssessmentRequest)` was added
* `postUpgradeWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.SqlManagedInstanceProperties` was modified

* `withActiveDirectoryInformation(models.ActiveDirectoryInformation)` was added
* `activeDirectoryInformation()` was added

#### `models.SqlServerInstanceProperties` was modified

* `upgradeLockedUntil()` was added
* `hostType()` was added
* `vmId()` was added
* `withAuthentication(models.Authentication)` was added
* `withBackupPolicy(models.BackupPolicy)` was added
* `withMigration(models.Migration)` was added
* `authentication()` was added
* `lastUsageUploadTime()` was added
* `alwaysOnRole()` was added
* `isMicrosoftPkiCertTrustConfigured()` was added
* `withMonitoring(models.Monitoring)` was added
* `withServiceType(models.ServiceType)` was added
* `backupPolicy()` was added
* `withClientConnection(models.ClientConnection)` was added
* `monitoring()` was added
* `maxServerMemoryMB()` was added
* `migration()` was added
* `withCores(java.lang.String)` was added
* `isHadrEnabled()` was added
* `failoverCluster()` was added
* `serviceType()` was added
* `lastInventoryUploadTime()` was added
* `withHostType(models.HostType)` was added
* `cores()` was added
* `withFailoverCluster(models.FailoverCluster)` was added
* `withUpgradeLockedUntil(java.time.OffsetDateTime)` was added
* `traceFlags()` was added
* `isDigiCertPkiCertTrustConfigured()` was added
* `clientConnection()` was added
* `dbMasterKeyExists()` was added
* `withDatabaseMirroringEndpoint(models.DbmEndpoint)` was added
* `databaseMirroringEndpoint()` was added

#### `models.SqlServerInstanceUpdate` was modified

* `withProperties(models.SqlServerInstanceUpdateProperties)` was added
* `properties()` was added

#### `models.SqlServerInstance$Update` was modified

* `withProperties(models.SqlServerInstanceUpdateProperties)` was added

#### `models.SqlManagedInstanceK8SSpec` was modified

* `settings()` was added
* `withSettings(models.K8SSettings)` was added
* `security()` was added
* `withSecurity(models.K8SSecurity)` was added

#### `AzureArcDataManager` was modified

* `sqlServerDatabases()` was added
* `sqlServerLicenses()` was added
* `sqlServerEsuLicenses()` was added
* `sqlServerAvailabilityGroups()` was added
* `postgresInstances()` was added
* `activeDirectoryConnectors()` was added
* `failoverGroups()` was added

#### `models.DataControllerProperties` was modified

* `logsDashboardCredential()` was added
* `metricsDashboardCredential()` was added
* `withLogsDashboardCredential(models.BasicLoginInformation)` was added
* `withMetricsDashboardCredential(models.BasicLoginInformation)` was added

#### `models.SqlServerInstance` was modified

* `getJobsStatusWithResponse(models.SqlServerInstanceJobsStatusRequest,com.azure.core.util.Context)` was added
* `getJobsStatus()` was added
* `postUpgradeWithResponse(com.azure.core.util.Context)` was added
* `getTelemetry(models.SqlServerInstanceTelemetryRequest)` was added
* `runMigrationAssessment()` was added
* `preUpgrade()` was added
* `getTelemetry(models.SqlServerInstanceTelemetryRequest,com.azure.core.util.Context)` was added
* `runMigrationAssessmentWithResponse(com.azure.core.util.Context)` was added
* `runManagedInstanceLinkAssessment(models.SqlServerInstanceManagedInstanceLinkAssessmentRequest)` was added
* `runManagedInstanceLinkAssessment(models.SqlServerInstanceManagedInstanceLinkAssessmentRequest,com.azure.core.util.Context)` was added
* `postUpgrade()` was added
* `preUpgradeWithResponse(com.azure.core.util.Context)` was added

## 1.0.0 (2025-01-02)

- Azure Resource Manager AzureArcData client library for Java. This package contains Microsoft Azure SDK for AzureArcData Management SDK. The AzureArcData management API provides a RESTful set of web APIs to manage Azure Data Services on Azure Arc Resources. Package tag package-2021-08-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.SqlManagedInstanceSku` was modified

* `withName(java.lang.String)` was removed

## 1.0.0-beta.4 (2024-10-14)

- Azure Resource Manager AzureArcData client library for Java. This package contains Microsoft Azure SDK for AzureArcData Management SDK. The AzureArcData management API provides a RESTful set of web APIs to manage Azure Data Services on Azure Arc Resources. Package tag package-2021-08-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.SqlManagedInstanceSku` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OnPremiseProperty` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DataControllerProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SqlServerInstanceUpdate` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DataControllerUpdate` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.K8SSchedulingOptions` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PageOfDataControllerResource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SqlManagedInstanceK8SSpec` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.K8SResourceRequirements` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.LogAnalyticsWorkspaceConfig` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ExtendedLocation` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.K8SScheduling` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SqlServerInstanceListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BasicLoginInformation` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SqlManagedInstanceUpdate` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.UploadWatermark` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UploadServicePrincipal` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SqlManagedInstanceK8SRaw` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SqlManagedInstanceListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SqlManagedInstanceProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SqlServerInstanceProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

## 1.0.0-beta.3 (2023-01-11)

- Azure Resource Manager AzureArcData client library for Java. This package contains Microsoft Azure SDK for AzureArcData Management SDK. The AzureArcData management API provides a RESTful set of web APIs to manage Azure Data Services on Azure Arc Resources. Package tag package-2021-08-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ODataError` was removed

### Features Added

#### `models.SqlServerInstance` was modified

* `resourceGroupName()` was added

#### `AzureArcDataManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `AzureArcDataManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.SqlManagedInstance` was modified

* `resourceGroupName()` was added

#### `models.DataControllerResource` was modified

* `resourceGroupName()` was added

## 1.0.0-beta.2 (2021-09-24)

- Azure Resource Manager AzureArcData client library for Java. This package contains Microsoft Azure SDK for AzureArcData Management SDK. The AzureArcData management API provides a RESTful set of web APIs to manage Azure Data Services on Azure Arc Resources. Package tag package-2021-08-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.ResourceIdentityType` was removed

#### `models.SqlManagedInstances` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `com.azure.core.http.rest.Response deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`

#### `models.SqlServerInstances` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `com.azure.core.http.rest.Response deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`

#### `models.DataControllers` was modified

* `com.azure.core.http.rest.Response deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`
* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

#### `models.SqlManagedInstances` was modified

* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.SqlServerInstances` was modified

* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.DataControllers` was modified

* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

## 1.0.0-beta.1 (2021-07-21)

- Azure Resource Manager AzureArcData client library for Java. This package contains Microsoft Azure SDK for AzureArcData Management SDK. The AzureArcData management API provides a RESTful set of web APIs to manage Azure Data Services on Azure Arc Resources. Package tag package-2021-08-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
