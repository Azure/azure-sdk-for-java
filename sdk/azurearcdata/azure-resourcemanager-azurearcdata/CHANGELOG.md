# Release History

## 1.1.0 (2026-07-03)

- Azure Resource Manager AzureArcData client library for Java. This package contains Microsoft Azure SDK for AzureArcData Management SDK. The AzureArcData management API provides a RESTful set of web APIs to manage Azure Data Services on Azure Arc Resources. Package api-version 2026-03-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.PageOfDataControllerResource` was removed

#### `models.SqlServerInstanceListResult` was removed

#### `models.OperationListResult` was removed

#### `models.SqlManagedInstanceListResult` was removed

#### `models.SqlManagedInstanceSku` was modified

* `validate()` was removed

#### `models.DataControllerUpdate` was modified

* `validate()` was removed

#### `models.K8SResourceRequirements` was modified

* `validate()` was removed

#### `models.BasicLoginInformation` was modified

* `validate()` was removed

#### `models.ArcSqlManagedInstanceLicenseType` was modified

* `toString()` was removed
* `models.ArcSqlManagedInstanceLicenseType[] values()` -> `java.util.Collection values()`
* `valueOf(java.lang.String)` was removed

#### `models.SqlManagedInstanceProperties` was modified

* `validate()` was removed

#### `models.SqlServerInstanceProperties` was modified

* `withVCore(java.lang.String)` was removed
* `validate()` was removed
* `withCollation(java.lang.String)` was removed
* `withPatchLevel(java.lang.String)` was removed
* `withTcpStaticPorts(java.lang.String)` was removed
* `withAzureDefenderStatusLastUpdated(java.time.OffsetDateTime)` was removed
* `withCurrentVersion(java.lang.String)` was removed
* `withTcpDynamicPorts(java.lang.String)` was removed
* `withAzureDefenderStatus(models.DefenderStatus)` was removed
* `withProductId(java.lang.String)` was removed
* `withLicenseType(models.ArcSqlServerLicenseType)` was removed
* `withContainerResourceId(java.lang.String)` was removed
* `withStatus(models.ConnectionStatus)` was removed

#### `models.OnPremiseProperty` was modified

* `java.util.UUID id()` -> `java.lang.String id()`
* `withId(java.util.UUID)` was removed
* `validate()` was removed

#### `models.SqlServerInstanceUpdate` was modified

* `validate()` was removed

#### `models.OperationDisplay` was modified

* `OperationDisplay()` was changed to private access
* `withResource(java.lang.String)` was removed
* `withOperation(java.lang.String)` was removed
* `withProvider(java.lang.String)` was removed
* `withDescription(java.lang.String)` was removed
* `validate()` was removed

#### `models.K8SScheduling` was modified

* `validate()` was removed

#### `models.SqlVersion` was modified

* `SQL_SERVER_2019` was removed
* `SQL_SERVER_2016` was removed
* `SQL_SERVER_2017` was removed

#### `models.K8SSchedulingOptions` was modified

* `validate()` was removed

#### `models.SqlManagedInstanceK8SSpec` was modified

* `validate()` was removed

#### `models.LogAnalyticsWorkspaceConfig` was modified

* `java.util.UUID workspaceId()` -> `java.lang.String workspaceId()`
* `withWorkspaceId(java.util.UUID)` was removed
* `validate()` was removed

#### `models.ExtendedLocation` was modified

* `validate()` was removed

#### `models.UploadWatermark` was modified

* `validate()` was removed

#### `models.UploadServicePrincipal` was modified

* `java.util.UUID clientId()` -> `java.lang.String clientId()`
* `withTenantId(java.util.UUID)` was removed
* `validate()` was removed
* `withClientId(java.util.UUID)` was removed
* `java.util.UUID tenantId()` -> `java.lang.String tenantId()`

#### `models.SqlManagedInstanceK8SRaw` was modified

* `validate()` was removed

#### `models.DataControllerProperties` was modified

* `validate()` was removed

#### `models.SqlManagedInstanceUpdate` was modified

* `validate()` was removed

### Features Added

* `models.ArcSqlServerAvailabilityMode` was added

* `models.SqlServerAvailabilityGroupResource` was added

* `models.CostTypeValues` was added

* `models.K8SSettings` was added

* `models.FailoverGroupResource$Definition` was added

* `models.SqlServerAvailabilityGroupResourceProperties` was added

* `models.AlwaysOnRole` was added

* `models.SqlServerInstanceJobsResponse` was added

* `models.State` was added

* `models.LicenseCategory` was added

* `models.LastExecutionStatus` was added

* `models.PostgresInstances` was added

* `models.ActivationState` was added

* `models.SqlServerDatabaseResource$UpdateStages` was added

* `models.AzureManagedInstanceRole` was added

* `models.SkuRecommendationResultsAzureSqlManagedInstance` was added

* `models.HostIPAddressInformation` was added

* `models.Schedule` was added

* `models.DbFailover` was added

* `models.SqlServerLicenseProperties` was added

* `models.Version` was added

* `models.SkuRecommendationResultsAzureSqlVirtualMachineTargetSkuCategory` was added

* `models.SqlServerInstanceRunMigrationAssessmentResponse` was added

* `models.SequencerAction` was added

* `models.JobStatus` was added

* `models.DBMEndpoint` was added

* `models.SkuRecommendationResultsAzureSqlManagedInstanceTargetSku` was added

* `models.CronTrigger` was added

* `models.K8SSecurity` was added

* `models.SqlServerInstanceJobStatus` was added

* `models.SqlServerLicenseUpdateProperties` was added

* `models.AggregationType` was added

* `models.PostgresInstanceProperties` was added

* `models.Authentication` was added

* `models.K8SActiveDirectoryConnector` was added

* `models.SqlServerAvailabilityGroupResourcePropertiesReplicas` was added

* `models.SqlServerEsuLicenseUpdateProperties` was added

* `models.SqlServerDatabaseUpdate` was added

* `models.SqlServerAvailabilityGroupResource$Definition` was added

* `models.ActiveDirectoryDomainControllers` was added

* `models.SqlServerInstanceMigrationReadinessReportResponse` was added

* `models.AvailabilityGroupInfo` was added

* `models.RecoveryMode` was added

* `models.SqlServerAvailabilityGroupResource$UpdateStages` was added

* `models.SqlServerAvailabilityGroups` was added

* `models.AutomatedBackupPreference` was added

* `models.ClientConnection` was added

* `models.InitiatedFrom` was added

* `models.SkuRecommendationResultsMonthlyCostOptionItem` was added

* `models.SqlServerAvailabilityGroupResourcePropertiesDatabases` was added

* `models.SkuRecommendationResultsAzureSqlDatabaseTargetSkuCategory` was added

* `models.SqlAvailabilityGroupDatabaseReplicaResourceProperties` was added

* `models.ExecutionState` was added

* `models.ActiveDirectoryInformation` was added

* `models.Migration` was added

* `models.Role` was added

* `models.ActiveDirectoryConnectorDNSDetails` was added

* `models.BackgroundJob` was added

* `models.SqlServerAvailabilityGroupUpdate` was added

* `models.ArcSqlServerAvailabilityGroupTypeFilter` was added

* `models.SqlServerInstanceBpaColumnType` was added

* `models.SqlServerLicense` was added

* `models.FailureConditionLevel` was added

* `models.ServerAssessmentsPropertiesItemsItem` was added

* `models.K8STransparentDataEncryption` was added

* `models.DistributedAvailabilityGroupCreateUpdateConfiguration` was added

* `models.FailoverGroupPartnerSyncMode` was added

* `models.SqlServerDatabaseResource$Update` was added

* `models.ConnectionAuth` was added

* `models.ClusterType` was added

* `models.ProvisioningState` was added

* `models.PostgresInstance$UpdateStages` was added

* `models.SqlServerDatabaseResourcePropertiesBackupInformation` was added

* `models.MigrationStatus` was added

* `models.SqlServerInstanceBpaRequest` was added

* `models.ActiveDirectoryConnectorResource$DefinitionStages` was added

* `models.SqlServerInstanceManagedInstanceLinkAssessmentResponse` was added

* `models.SqlServerInstanceBpaQueryType` was added

* `models.SqlServerInstanceRunBestPracticesAssessmentResponse` was added

* `models.Result` was added

* `models.DatabaseCreateMode` was added

* `models.MigrationAssessmentSettings` was added

* `models.ActiveDirectoryConnectorSpec` was added

* `models.DiscoverySource` was added

* `models.MigrationAssessment` was added

* `models.AvailabilityGroupConfigure` was added

* `models.SqlServerInstanceTelemetryColumnType` was added

* `models.SqlServerLicense$Definition` was added

* `models.SkuRecommendationResultsAzureSqlManagedInstanceTargetSkuCategory` was added

* `models.FailoverGroupProperties` was added

* `models.SkuRecommendationResults` was added

* `models.ImpactedObjectsInfo` was added

* `models.ActiveDirectoryConnectorProperties` was added

* `models.PostgresInstance$Definition` was added

* `models.ActiveDirectoryDomainController` was added

* `models.ActiveDirectoryConnectors` was added

* `models.CommonSku` was added

* `models.FailoverGroupSpec` was added

* `models.IdentityType` was added

* `models.SqlServerInstanceTargetRecommendationReport` was added

* `models.SqlServerLicenseUpdate` was added

* `models.ServiceType` was added

* `models.TargetType` was added

* `models.SqlServerDatabases` was added

* `models.SqlServerEsuLicense` was added

* `models.AdditionalMigrationJobAttributes` was added

* `models.EncryptionAlgorithm` was added

* `models.SqlServerInstanceTelemetryColumn` was added

* `models.AvailabilityGroupRetrievalFilters` was added

* `models.InstanceFailoverGroupRole` was added

* `models.SqlServerDatabaseResource` was added

* `models.SqlServerEsuLicense$DefinitionStages` was added

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

* `models.SkuRecommendationSummaryTargetSku` was added

* `models.SkuRecommendationResultsAzureSqlVirtualMachineTargetSkuVirtualMachineSize` was added

* `models.SqlServerInstanceJobsStatusRequest` was added

* `models.SqlServerInstanceJob` was added

* `models.SqlServerInstanceManagedInstanceLinkAssessment` was added

* `models.AssessmentStatus` was added

* `models.ActiveDirectoryConnectorResource` was added

* `models.PostgresInstance` was added

* `models.SqlServerLicense$DefinitionStages` was added

* `models.DiskSizes` was added

* `models.HostType` was added

* `models.Databases` was added

* `models.PostgresInstance$DefinitionStages` was added

* `models.SqlServerInstanceJobsStatusResponse` was added

* `models.ActiveDirectoryConnectorDomainDetails` was added

* `models.DatabaseState` was added

* `models.SqlServerEsuLicense$Update` was added

* `models.DataBaseMigration` was added

* `models.SequencerState` was added

* `models.ScopeType` was added

* `models.SqlServerInstanceRunMigrationReadinessAssessmentResponse` was added

* `models.DataBaseMigrationAssessment` was added

* `models.MiLinkAssessmentCategory` was added

* `models.SkuRecommendationSummary` was added

* `models.SqlServerInstanceUpdateProperties` was added

* `models.SqlServerDatabaseResource$Definition` was added

* `models.SqlAvailabilityGroupStaticIPListenerProperties` was added

* `models.ServerAssessmentsItem` was added

* `models.DifferentialBackupHours` was added

* `models.SqlServerInstanceTargetRecommendationReportSection` was added

* `models.SqlServerLicense$UpdateStages` was added

* `models.DatabaseMigrationJobsItem` was added

* `models.K8SActiveDirectory` was added

* `models.PostgresInstanceSku` was added

* `models.DtcSupport` was added

* `models.SqlServerDatabaseResource$DefinitionStages` was added

* `models.SqlServerInstanceTargetRecommendationReportsRequest` was added

* `models.MigrationMode` was added

* `models.EntraAuthentication` was added

* `models.AvailabilityGroupState` was added

* `models.PostgresInstanceSkuTier` was added

* `models.SqlServerEsuLicenses` was added

* `models.SqlServerInstanceBpaColumn` was added

* `models.SqlServerInstanceTargetRecommendationReportsResponse` was added

* `models.MiLinkCreateUpdateConfiguration` was added

* `models.AvailabilityGroupCreateUpdateConfiguration` was added

* `models.SkuRecommendationResultsAzureSqlDatabaseTargetSku` was added

* `models.AvailabilityGroupCreateUpdateReplicaConfiguration` was added

* `models.SkuRecommendationResultsAzureSqlDatabase` was added

* `models.SqlServerEsuLicenseUpdate` was added

* `models.SqlServerLicense$Update` was added

* `models.SecondaryAllowConnections` was added

* `models.FailoverGroupResource$DefinitionStages` was added

* `models.SqlServerInstanceTargetRecommendationReportSectionType` was added

* `models.SkuRecommendationSummaryTargetSkuCategory` was added

* `models.DistributedAvailabilityGroupCreateUpdateAvailabilityGroupCertificateConfiguration` was added

* `models.SqlServerEsuLicense$UpdateStages` was added

* `models.Mode` was added

* `models.ImpactedObjectsSuitabilitySummary` was added

* `models.SqlServerInstanceTelemetryRequest` was added

* `models.Monitoring` was added

* `models.TargetReadiness` was added

* `models.CostOptionSelectedValues` was added

* `models.SkuRecommendationResultsAzureSqlVirtualMachine` was added

* `models.SqlServerAvailabilityGroupResource$Update` was added

* `models.FailoverMiLinkResourceId` was added

* `models.SqlServerInstanceJobsRequest` was added

* `models.ResourceUpdateMode` was added

* `models.SkuRecommendationResultsAzureSqlVirtualMachineTargetSku` was added

* `models.SqlServerDatabaseResourcePropertiesDatabaseOptions` was added

* `models.DatabaseAssessmentsItem` was added

* `models.SqlAvailabilityGroupReplicaResourceProperties` was added

* `models.BackupPolicy` was added

* `models.FailoverGroupResource` was added

* `models.ManagedInstanceLinkCreateUpdateConfiguration` was added

* `models.SqlServerInstanceBpaReportType` was added

* `models.ActiveDirectoryConnectorResource$Definition` was added

* `models.PrimaryAllowConnections` was added

* `models.ReplicationPartnerType` was added

* `models.SqlServerInstanceRunTargetRecommendationJobResponse` was added

* `models.ArcSqlServerFailoverMode` was added

* `models.SqlServerInstanceManagedInstanceLinkAssessmentRequest` was added

* `models.K8SNetworkSettings` was added

* `models.SeedingMode` was added

* `models.SqlServerAvailabilityGroupResource$DefinitionStages` was added

* `models.DistributedAvailabilityGroupCreateUpdateAvailabilityGroupConfiguration` was added

* `models.AccountProvisioningMode` was added

* `models.SkuRecommendationResultsMonthlyCost` was added

* `models.BestPracticesAssessment` was added

* `models.SqlServerInstanceRunTargetRecommendationJobRequest` was added

#### `models.DataControllerResource$Update` was modified

* `withProperties(models.DataControllerProperties)` was added

#### `models.DataControllerUpdate` was modified

* `withProperties(models.DataControllerProperties)` was added
* `properties()` was added

#### `models.SqlServerInstances` was modified

* `runTargetRecommendationJob(java.lang.String,java.lang.String,models.SqlServerInstanceRunTargetRecommendationJobRequest,com.azure.core.util.Context)` was added
* `getBestPracticesAssessment(java.lang.String,java.lang.String,models.SqlServerInstanceBpaRequest,com.azure.core.util.Context)` was added
* `runManagedInstanceLinkAssessment(java.lang.String,java.lang.String,models.SqlServerInstanceManagedInstanceLinkAssessmentRequest)` was added
* `getTargetRecommendationReports(java.lang.String,java.lang.String,models.SqlServerInstanceTargetRecommendationReportsRequest,com.azure.core.util.Context)` was added
* `runMigrationReadinessAssessment(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getJobs(java.lang.String,java.lang.String)` was added
* `getTelemetry(java.lang.String,java.lang.String,models.SqlServerInstanceTelemetryRequest,com.azure.core.util.Context)` was added
* `postUpgradeWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `preUpgrade(java.lang.String,java.lang.String)` was added
* `getMigrationReadinessReport(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `runMigrationReadinessAssessment(java.lang.String,java.lang.String)` was added
* `getAllAvailabilityGroups(java.lang.String,java.lang.String)` was added
* `getJobsStatusWithResponse(java.lang.String,java.lang.String,models.SqlServerInstanceJobsStatusRequest,com.azure.core.util.Context)` was added
* `getTargetRecommendationReports(java.lang.String,java.lang.String)` was added
* `getTelemetry(java.lang.String,java.lang.String,models.SqlServerInstanceTelemetryRequest)` was added
* `runTargetRecommendationJob(java.lang.String,java.lang.String)` was added
* `runBestPracticesAssessmentWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `preUpgradeWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `runBestPracticesAssessment(java.lang.String,java.lang.String)` was added
* `getJobsStatus(java.lang.String,java.lang.String)` was added
* `getAllAvailabilityGroups(java.lang.String,java.lang.String,models.AvailabilityGroupRetrievalFilters,com.azure.core.util.Context)` was added
* `getBestPracticesAssessment(java.lang.String,java.lang.String,models.SqlServerInstanceBpaRequest)` was added
* `postUpgrade(java.lang.String,java.lang.String)` was added
* `runBestPracticeAssessment(java.lang.String,java.lang.String)` was added
* `runBestPracticeAssessment(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getJobs(java.lang.String,java.lang.String,models.SqlServerInstanceJobsRequest,com.azure.core.util.Context)` was added
* `runManagedInstanceLinkAssessment(java.lang.String,java.lang.String,models.SqlServerInstanceManagedInstanceLinkAssessmentRequest,com.azure.core.util.Context)` was added
* `runMigrationAssessment(java.lang.String,java.lang.String)` was added
* `getMigrationReadinessReport(java.lang.String,java.lang.String)` was added
* `runMigrationAssessmentWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.ArcSqlManagedInstanceLicenseType` was modified

* `ArcSqlManagedInstanceLicenseType()` was added
* `DISASTER_RECOVERY` was added

#### `models.SqlManagedInstanceProperties` was modified

* `withActiveDirectoryInformation(models.ActiveDirectoryInformation)` was added
* `activeDirectoryInformation()` was added

#### `models.SqlServerInstanceProperties` was modified

* `isDigiCertPkiCertTrustConfigured()` was added
* `traceFlags()` was added
* `migration()` was added
* `withDatabaseMirroringEndpoint(models.DBMEndpoint)` was added
* `backupPolicy()` was added
* `withCores(java.lang.String)` was added
* `vmId()` was added
* `monitoring()` was added
* `isMicrosoftPkiCertTrustConfigured()` was added
* `withClientConnection(models.ClientConnection)` was added
* `withMonitoring(models.Monitoring)` was added
* `bestPracticesAssessment()` was added
* `maxServerMemoryMB()` was added
* `withDiscoverySource(models.DiscoverySource)` was added
* `discoverySource()` was added
* `serviceType()` was added
* `withBackupPolicy(models.BackupPolicy)` was added
* `dbMasterKeyExists()` was added
* `authentication()` was added
* `withAuthentication(models.Authentication)` was added
* `withMigration(models.Migration)` was added
* `hostType()` was added
* `upgradeLockedUntil()` was added
* `lastUsageUploadTime()` was added
* `withHostType(models.HostType)` was added
* `alwaysOnRole()` was added
* `withServiceType(models.ServiceType)` was added
* `withBestPracticesAssessment(models.BestPracticesAssessment)` was added
* `withUpgradeLockedUntil(java.time.OffsetDateTime)` was added
* `cores()` was added
* `withFailoverCluster(models.FailoverCluster)` was added
* `databaseMirroringEndpoint()` was added
* `lastInventoryUploadTime()` was added
* `clientConnection()` was added
* `failoverCluster()` was added
* `isHadrEnabled()` was added

#### `models.OnPremiseProperty` was modified

* `withId(java.lang.String)` was added

#### `models.SqlServerInstanceUpdate` was modified

* `properties()` was added
* `withProperties(models.SqlServerInstanceUpdateProperties)` was added

#### `models.ConnectionStatus` was modified

* `REGISTERED` was added
* `DISCOVERED` was added

#### `models.SqlVersion` was modified

* `SQLSERVER2022` was added
* `SQLSERVER2012` was added
* `SQLSERVER2014` was added
* `SQLSERVER2025` was added
* `UNKNOWN` was added
* `SQLSERVER2016` was added
* `SQLSERVER2017` was added
* `SQLSERVER2019` was added

#### `models.SqlServerInstance$Update` was modified

* `withProperties(models.SqlServerInstanceUpdateProperties)` was added

#### `models.ArcSqlServerLicenseType` was modified

* `SERVER_CAL` was added
* `LICENSE_ONLY` was added
* `FABRIC_CAPACITY` was added
* `PAYG` was added

#### `models.SqlManagedInstanceK8SSpec` was modified

* `security()` was added
* `withSettings(models.K8SSettings)` was added
* `withSecurity(models.K8SSecurity)` was added
* `settings()` was added

#### `AzureArcDataManager` was modified

* `postgresInstances()` was added
* `activeDirectoryConnectors()` was added
* `sqlServerAvailabilityGroups()` was added
* `failoverGroups()` was added
* `sqlServerDatabases()` was added
* `sqlServerEsuLicenses()` was added
* `sqlServerLicenses()` was added

#### `models.LogAnalyticsWorkspaceConfig` was modified

* `withWorkspaceId(java.lang.String)` was added

#### `models.UploadServicePrincipal` was modified

* `withClientId(java.lang.String)` was added
* `withTenantId(java.lang.String)` was added

#### `models.EditionType` was modified

* `BUSINESS_INTELLIGENCE` was added
* `STANDARD_DEVELOPER` was added
* `UNKNOWN` was added

#### `models.DataControllerProperties` was modified

* `withMetricsDashboardCredential(models.BasicLoginInformation)` was added
* `withLogsDashboardCredential(models.BasicLoginInformation)` was added
* `metricsDashboardCredential()` was added
* `logsDashboardCredential()` was added

#### `models.SqlServerInstance` was modified

* `getJobsStatusWithResponse(models.SqlServerInstanceJobsStatusRequest,com.azure.core.util.Context)` was added
* `runBestPracticesAssessmentWithResponse(com.azure.core.util.Context)` was added
* `getJobs()` was added
* `runMigrationReadinessAssessment(com.azure.core.util.Context)` was added
* `runTargetRecommendationJob(models.SqlServerInstanceRunTargetRecommendationJobRequest,com.azure.core.util.Context)` was added
* `preUpgrade()` was added
* `runManagedInstanceLinkAssessment(models.SqlServerInstanceManagedInstanceLinkAssessmentRequest)` was added
* `getMigrationReadinessReport(com.azure.core.util.Context)` was added
* `runBestPracticeAssessment(com.azure.core.util.Context)` was added
* `preUpgradeWithResponse(com.azure.core.util.Context)` was added
* `getTargetRecommendationReports()` was added
* `getMigrationReadinessReport()` was added
* `runBestPracticesAssessment()` was added
* `getJobsStatus()` was added
* `getBestPracticesAssessment(models.SqlServerInstanceBpaRequest)` was added
* `runMigrationReadinessAssessment()` was added
* `getAllAvailabilityGroups()` was added
* `getTelemetry(models.SqlServerInstanceTelemetryRequest)` was added
* `runMigrationAssessmentWithResponse(com.azure.core.util.Context)` was added
* `getBestPracticesAssessment(models.SqlServerInstanceBpaRequest,com.azure.core.util.Context)` was added
* `runMigrationAssessment()` was added
* `runManagedInstanceLinkAssessment(models.SqlServerInstanceManagedInstanceLinkAssessmentRequest,com.azure.core.util.Context)` was added
* `runTargetRecommendationJob()` was added
* `getTargetRecommendationReports(models.SqlServerInstanceTargetRecommendationReportsRequest,com.azure.core.util.Context)` was added
* `getAllAvailabilityGroups(models.AvailabilityGroupRetrievalFilters,com.azure.core.util.Context)` was added
* `getTelemetry(models.SqlServerInstanceTelemetryRequest,com.azure.core.util.Context)` was added
* `postUpgrade()` was added
* `postUpgradeWithResponse(com.azure.core.util.Context)` was added
* `runBestPracticeAssessment()` was added
* `getJobs(models.SqlServerInstanceJobsRequest,com.azure.core.util.Context)` was added

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
