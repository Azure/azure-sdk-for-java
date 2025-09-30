# Release History

## 1.2.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.1.0 (2025-09-23)

- Azure Resource Manager DataMigration client library for Java. This package contains Microsoft Azure SDK for DataMigration Management SDK. Data Migration Client. Package tag package-2025-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ServiceOperationList` was removed

#### `models.ServiceOperation` was removed

#### `models.ServiceOperationDisplay` was removed

#### `models.ProjectTaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.MigratePostgreSqlAzureDbForPostgreSqlSyncTaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.GetUserTablesSqlSyncTaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.GetUserTablesSqlTaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.ConnectToTargetSqlDbTaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.ValidateMigrationInputSqlServerSqlMISyncTaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.ConnectToSourcePostgreSqlSyncTaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.ConnectToSourceSqlServerSyncTaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.ConnectToTargetSqlMISyncTaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.MigrateSqlServerSqlMITaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.ConnectToTargetSqlMITaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.ConnectToTargetAzureDbForPostgreSqlSyncTaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.DataMigrationService` was modified

* `nestedCheckNameAvailability(models.NameAvailabilityRequest)` was removed
* `nestedCheckNameAvailabilityWithResponse(models.NameAvailabilityRequest,com.azure.core.util.Context)` was removed

#### `models.Projects` was modified

* `listByResourceGroup(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `listByResourceGroup(java.lang.String,java.lang.String)` was removed

#### `models.ValidateMigrationInputSqlServerSqlDbSyncTaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.CommandProperties` was modified

* `java.util.List errors()` -> `java.util.List errors()`
* `fromJson(com.azure.json.JsonReader)` was removed
* `models.CommandState state()` -> `models.CommandState state()`
* `validate()` was removed
* `java.lang.String commandType()` -> `models.CommandType commandType()`
* `toJson(com.azure.json.JsonWriter)` was removed

#### `models.ValidateMigrationInputSqlServerSqlMITaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.MigrateSqlServerSqlDbTaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.ConnectToTargetAzureDbForMySqlTaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.MigrateSyncCompleteCommandProperties` was modified

* `java.lang.String commandType()` -> `models.CommandType commandType()`

#### `models.ConnectToTargetSqlDbSyncTaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.GetTdeCertificatesSqlTaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.MigrateMISyncCompleteCommandProperties` was modified

* `java.lang.String commandType()` -> `models.CommandType commandType()`

#### `models.ConnectToSourceMySqlTaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.Services` was modified

* `nestedCheckNameAvailability(java.lang.String,java.lang.String,models.NameAvailabilityRequest)` was removed
* `nestedCheckNameAvailabilityWithResponse(java.lang.String,java.lang.String,models.NameAvailabilityRequest,com.azure.core.util.Context)` was removed

#### `models.MigrateSqlServerSqlMISyncTaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.MigrateSqlServerSqlDbTaskOutputMigrationLevel` was modified

* `migrationReport()` was removed

#### `models.ConnectToSourceSqlServerTaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.MigrateMySqlAzureDbForMySqlSyncTaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.MigrateSqlServerSqlDbSyncTaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

### Features Added

* `models.ScenarioTarget` was added

* `models.OperationListResult` was added

* `models.MigrateSchemaSqlServerSqlDbTaskProperties` was added

* `models.DeleteNode` was added

* `models.DatabaseMigrationSqlMi$DefinitionStages` was added

* `models.SqlDbMigrationStatusDetails` was added

* `models.SqlBackupSetInfo` was added

* `models.UploadOciDriverTaskInput` was added

* `models.GetUserTablesMySqlTaskOutput` was added

* `models.ReplicateMigrationState` was added

* `models.UserAssignedIdentity` was added

* `models.DatabaseMigrationSqlMi$Definition` was added

* `models.MigrateSchemaSqlServerSqlDbTaskOutputMigrationLevel` was added

* `models.SqlMigrationService$UpdateStages` was added

* `models.DatabaseMigrationsMongoToCosmosDbvCoreMongoes` was added

* `models.MigrateSsisTaskOutput` was added

* `models.MigrateOracleAzureDbPostgreSqlSyncTaskOutput` was added

* `models.DatabaseMigrationSqlVm$Update` was added

* `models.MigrateSchemaSqlServerSqlDbTaskOutputDatabaseLevel` was added

* `models.MigrationValidationResult` was added

* `models.MongoDbReplication` was added

* `models.MigrationServiceUpdate` was added

* `models.DatabaseMigrationSqlDb$Update` was added

* `models.MongoDbDatabaseInfo` was added

* `models.DatabaseMigrationSqlMi$UpdateStages` was added

* `models.DatabaseMigrationBaseProperties` was added

* `models.ScenarioSource` was added

* `models.InstallOciDriverTaskInput` was added

* `models.ConnectToSourceOracleSyncTaskInput` was added

* `models.DatabaseMigrationCosmosDbMongo` was added

* `models.DatabaseMigration` was added

* `models.MigrationService$Definition` was added

* `models.DatabaseMigrationPropertiesSqlDb` was added

* `models.GetUserTablesPostgreSqlTaskProperties` was added

* `models.ProjectFileProperties` was added

* `models.MongoDbShardKeySetting` was added

* `models.BackupConfiguration` was added

* `models.DatabaseMigrationSqlMi` was added

* `models.CreatedByType` was added

* `models.MongoDbThrottlingSettings` was added

* `models.ProjectFile$Update` was added

* `models.MigrationServices` was added

* `models.SsisMigrationInfo` was added

* `models.MigrateSchemaSqlServerSqlDbTaskInput` was added

* `models.ErrorInfo` was added

* `models.MigrateSsisTaskOutputProjectLevel` was added

* `models.MigrationValidationDatabaseLevelResult` was added

* `models.DatabaseMigrationProperties` was added

* `models.DatabaseMigrationsSqlDbs` was added

* `models.MigrationOperationInput` was added

* `models.ValidateOracleAzureDbForPostgreSqlSyncTaskProperties` was added

* `models.MigratePostgreSqlAzureDbForPostgreSqlSyncDatabaseTableInput` was added

* `models.SqlMigrationServices` was added

* `models.MigrateMySqlAzureDbForMySqlOfflineTaskOutputDatabaseLevel` was added

* `models.InstallOciDriverTaskProperties` was added

* `models.MigrationStatusDetails` was added

* `models.MigrateSsisTaskOutputMigrationLevel` was added

* `models.DatabaseMigrationSqlVm$DefinitionStages` was added

* `models.MigrationServiceListResult` was added

* `models.ValidateMongoDbTaskProperties` was added

* `models.MongoDbDatabaseProgress` was added

* `models.MongoDbShardKeyInfo` was added

* `models.ConnectToSourceOracleSyncTaskOutput` was added

* `models.OperationsDisplayDefinition` was added

* `models.ConnectToTargetOracleAzureDbForPostgreSqlSyncTaskProperties` was added

* `models.MongoDbClusterInfo` was added

* `models.DatabaseMigrationBase` was added

* `models.MigrateMySqlAzureDbForMySqlOfflineTaskInput` was added

* `models.MongoDbMigrationProgress` was added

* `models.MigrateOracleAzureDbForPostgreSqlSyncTaskProperties` was added

* `models.MongoDbCollectionInfo` was added

* `models.MigrateOracleAzureDbPostgreSqlSyncDatabaseInput` was added

* `models.ServiceTasks` was added

* `models.UploadOciDriverTaskProperties` was added

* `models.DatabaseMigrationBaseListResult` was added

* `models.MigrateOracleAzureDbPostgreSqlSyncTaskOutputDatabaseLevel` was added

* `models.CheckOciDriverTaskProperties` was added

* `models.ProvisioningState` was added

* `models.Files` was added

* `models.GetUserTablesMySqlTaskInput` was added

* `models.MongoDbProgress` was added

* `models.ProjectFile$Definition` was added

* `models.SqlBackupFileInfo` was added

* `models.DatabaseMigrationSqlVm` was added

* `models.SqlMigrationService$Update` was added

* `models.SourceLocation` was added

* `models.GetUserTablesPostgreSqlTaskInput` was added

* `models.SqlDbOfflineConfiguration` was added

* `models.RegenAuthKeys` was added

* `models.MigrationService$Update` was added

* `models.AzureBlob` was added

* `models.DatabaseMigrationSqlVm$Definition` was added

* `models.MigrateSchemaSqlServerSqlDbTaskOutputError` was added

* `models.MigrateSchemaSqlTaskOutputError` was added

* `models.MongoDbDatabaseSettings` was added

* `models.GetUserTablesPostgreSqlTaskOutput` was added

* `models.MigrationService$UpdateStages` was added

* `models.DatabaseMigrationSqlMi$Update` was added

* `models.MongoMigrationCollection` was added

* `models.OperationsDefinition` was added

* `models.GetUserTablesMySqlTaskProperties` was added

* `models.OperationOrigin` was added

* `models.OfflineConfiguration` was added

* `models.MongoDbFinishCommand` was added

* `models.DatabaseMigrationsSqlMis` was added

* `models.MongoDbCollectionSettings` was added

* `models.MongoMigrationProgressDetails` was added

* `models.MigrateSsisTaskInput` was added

* `models.SsisStoreType` was added

* `models.MongoDbMigrationSettings` was added

* `models.DatabaseMigrationCosmosDbMongo$Definition` was added

* `models.MigrationService` was added

* `models.InstallOciDriverTaskOutput` was added

* `models.DatabaseMigrationListResult` was added

* `models.MigrateMySqlAzureDbForMySqlOfflineTaskOutput` was added

* `models.MongoDbShardKeyField` was added

* `models.FileStorageInfo` was added

* `models.ConnectToTargetOracleAzureDbForPostgreSqlSyncTaskOutput` was added

* `models.MigrateOracleAzureDbPostgreSqlSyncTaskOutputDatabaseError` was added

* `models.IntegrationRuntimeMonitoringData` was added

* `models.ConnectToTargetOracleAzureDbForPostgreSqlSyncTaskOutputDatabaseSchemaMapItem` was added

* `models.OracleOciDriverInfo` was added

* `models.GetUserTablesOracleTaskProperties` was added

* `models.TaskType` was added

* `models.MongoDbError` was added

* `models.NodeMonitoringData` was added

* `models.DatabaseMigrationPropertiesCosmosDbMongo` was added

* `models.ConnectToSourceOracleSyncTaskProperties` was added

* `models.ProjectFile$UpdateStages` was added

* `models.SqlMigrationServiceUpdate` was added

* `models.UploadOciDriverTaskOutput` was added

* `models.DatabaseTable` was added

* `models.ProjectFile` was added

* `models.SqlFileShare` was added

* `models.GetUserTablesOracleTaskInput` was added

* `models.MongoDbFinishCommandInput` was added

* `models.MigrateSchemaSqlServerSqlDbTaskOutput` was added

* `models.CommandType` was added

* `models.MongoDbCancelCommand` was added

* `models.SqlConnectionInformation` was added

* `models.MongoDbCollectionProgress` was added

* `models.MigrateMongoDbTaskProperties` was added

* `models.MigrationService$DefinitionStages` was added

* `models.DatabaseMigrationPropertiesSqlVm` was added

* `models.ConnectToMongoDbTaskProperties` was added

* `models.SchemaMigrationSetting` was added

* `models.MigrateMySqlAzureDbForMySqlOfflineTaskOutputMigrationLevel` was added

* `models.MongoDbConnectionInfo` was added

* `models.CheckOciDriverTaskInput` was added

* `models.AuthType` was added

* `models.MigrateOracleAzureDbPostgreSqlSyncTaskOutputTableLevel` was added

* `models.MongoDbClusterType` was added

* `models.MigrateMySqlAzureDbForMySqlOfflineTaskOutputTableLevel` was added

* `models.FileList` was added

* `models.MigrateSsisTaskProperties` was added

* `models.SchemaMigrationStage` was added

* `models.ProjectFile$DefinitionStages` was added

* `models.DatabaseMigrationSqlVm$UpdateStages` was added

* `models.SqlMigrationService$Definition` was added

* `models.ManagedServiceIdentity` was added

* `models.SqlMigrationService$DefinitionStages` was added

* `models.ResourceType` was added

* `models.MongoMigrationStatus` was added

* `models.MongoDbErrorType` was added

* `models.SsisMigrationOverwriteOption` was added

* `models.MongoConnectionInformation` was added

* `models.MigrateSchemaSqlServerSqlDbDatabaseInput` was added

* `models.SqlMigrationListResult` was added

* `models.ManagedServiceIdentityType` was added

* `models.MigrateMySqlAzureDbForMySqlOfflineDatabaseInput` was added

* `models.MongoDbCommandInput` was added

* `models.SqlMigrationService` was added

* `models.MigrateOracleAzureDbPostgreSqlSyncTaskOutputMigrationLevel` was added

* `models.DatabaseMigrationCosmosDbMongo$DefinitionStages` was added

* `models.GetUserTablesOracleTaskOutput` was added

* `models.DatabaseMigrationSqlDb$UpdateStages` was added

* `models.CopyProgressDetails` was added

* `models.MongoDbShardKeyOrder` was added

* `models.DatabaseMigrationsMongoToCosmosDbRUMongoes` was added

* `models.SchemaMigrationOption` was added

* `models.MigrateMySqlAzureDbForMySqlOfflineTaskOutputError` was added

* `models.DatabaseMigrationSqlDb$DefinitionStages` was added

* `models.SystemDataAutoGenerated` was added

* `models.MigrateMySqlAzureDbForMySqlOfflineTaskProperties` was added

* `models.AuthenticationKeys` was added

* `models.MongoDbObjectInfo` was added

* `models.DatabaseMigrationCosmosDbMongoListResult` was added

* `models.OracleConnectionInfo` was added

* `models.TargetLocation` was added

* `models.DatabaseMigrationPropertiesSqlMi` was added

* `models.MigrateOracleAzureDbPostgreSqlSyncTaskOutputError` was added

* `models.MongoDbMigrationState` was added

* `models.DatabaseMigrationsSqlVms` was added

* `models.MigrateOracleAzureDbPostgreSqlSyncTaskInput` was added

* `models.ValidateOracleAzureDbPostgreSqlSyncTaskOutput` was added

* `models.DatabaseMigrationSqlDb` was added

* `models.MongoDbRestartCommand` was added

* `models.SsisMigrationStage` was added

* `models.DatabaseMigrationSqlDb$Definition` was added

* `models.MongoDbProgressResultType` was added

* `models.ConnectToTargetOracleAzureDbForPostgreSqlSyncTaskInput` was added

* `models.CheckOciDriverTaskOutput` was added

#### `models.MigrateSqlServerSqlMITaskInput` was modified

* `encryptedKeyForSecureFields()` was added
* `withAadDomainName(java.lang.String)` was added
* `withStartedOn(java.lang.String)` was added
* `withEncryptedKeyForSecureFields(java.lang.String)` was added
* `startedOn()` was added
* `aadDomainName()` was added

#### `models.MigrateSqlServerSqlDbTaskInput` was modified

* `startedOn()` was added
* `withEncryptedKeyForSecureFields(java.lang.String)` was added
* `encryptedKeyForSecureFields()` was added
* `withStartedOn(java.lang.String)` was added

#### `models.MigrateSqlServerSqlDbTaskOutputValidationResult` was modified

* `id()` was added

#### `models.QueryAnalysisValidationResult` was modified

* `withQueryResults(models.QueryExecutionResult)` was added
* `withValidationErrors(models.ValidationError)` was added

#### `models.ProjectTaskProperties` was modified

* `withClientData(java.util.Map)` was added
* `clientData()` was added

#### `models.MigratePostgreSqlAzureDbForPostgreSqlSyncTaskProperties` was modified

* `withCreatedOn(java.lang.String)` was added
* `withClientData(java.util.Map)` was added
* `taskId()` was added
* `createdOn()` was added
* `isCloneable()` was added
* `withTaskId(java.lang.String)` was added
* `withIsCloneable(java.lang.Boolean)` was added

#### `models.GetUserTablesSqlSyncTaskProperties` was modified

* `withClientData(java.util.Map)` was added

#### `models.SqlConnectionInfo` was modified

* `withServerBrandVersion(java.lang.String)` was added
* `resourceId()` was added
* `withServerName(java.lang.String)` was added
* `serverVersion()` was added
* `serverName()` was added
* `withResourceId(java.lang.String)` was added
* `serverBrandVersion()` was added
* `port()` was added
* `withServerVersion(java.lang.String)` was added
* `withPort(java.lang.Integer)` was added

#### `models.MigratePostgreSqlAzureDbForPostgreSqlSyncDatabaseInput` was modified

* `id()` was added
* `selectedTables()` was added
* `withSelectedTables(java.util.List)` was added

#### `models.MigrateSqlServerSqlMIDatabaseInput` was modified

* `withId(java.lang.String)` was added
* `id()` was added

#### `models.GetUserTablesSqlTaskProperties` was modified

* `withClientData(java.util.Map)` was added
* `withTaskId(java.lang.String)` was added
* `taskId()` was added

#### `models.ConnectToTargetAzureDbForMySqlTaskInput` was modified

* `isOfflineMigration()` was added
* `withIsOfflineMigration(java.lang.Boolean)` was added

#### `models.ConnectToTargetSqlDbTaskProperties` was modified

* `withClientData(java.util.Map)` was added
* `withCreatedOn(java.lang.String)` was added
* `createdOn()` was added

#### `models.MySqlConnectionInfo` was modified

* `withAdditionalSettings(java.lang.String)` was added
* `encryptConnection()` was added
* `withDataSource(java.lang.String)` was added
* `authentication()` was added
* `dataSource()` was added
* `withAuthentication(models.AuthenticationType)` was added
* `withEncryptConnection(java.lang.Boolean)` was added
* `additionalSettings()` was added

#### `models.ValidationError` was modified

* `withText(java.lang.String)` was added
* `withSeverity(models.Severity)` was added

#### `models.ValidateMigrationInputSqlServerSqlMISyncTaskProperties` was modified

* `withClientData(java.util.Map)` was added

#### `models.ConnectToSourcePostgreSqlSyncTaskProperties` was modified

* `withClientData(java.util.Map)` was added

#### `models.ConnectToSourceSqlServerSyncTaskProperties` was modified

* `withClientData(java.util.Map)` was added

#### `models.ConnectToTargetSqlMISyncTaskProperties` was modified

* `withClientData(java.util.Map)` was added

#### `models.MigrateSqlServerSqlDbTaskOutputDatabaseLevelValidationResult` was modified

* `id()` was added

#### `models.MigrationReportResult` was modified

* `withId(java.lang.String)` was added
* `withReportUrl(java.lang.String)` was added

#### `models.MigrateSqlServerSqlMITaskProperties` was modified

* `withTaskId(java.lang.String)` was added
* `withClientData(java.util.Map)` was added
* `isCloneable()` was added
* `createdOn()` was added
* `parentTaskId()` was added
* `taskId()` was added
* `withIsCloneable(java.lang.Boolean)` was added
* `withParentTaskId(java.lang.String)` was added
* `withCreatedOn(java.lang.String)` was added

#### `models.ConnectToTargetSqlMITaskProperties` was modified

* `withClientData(java.util.Map)` was added

#### `models.ConnectToTargetAzureDbForPostgreSqlSyncTaskProperties` was modified

* `withClientData(java.util.Map)` was added

#### `models.ConnectToSourceSqlServerTaskOutputTaskLevel` was modified

* `databaseTdeCertificateMapping()` was added

#### `models.DataMigrationService$Update` was modified

* `withVirtualNicId(java.lang.String)` was added
* `withDeleteResourcesOnStop(java.lang.Boolean)` was added
* `withAutoStopDelay(java.lang.String)` was added

#### `models.DataMigrationService` was modified

* `checkChildrenNameAvailabilityWithResponse(models.NameAvailabilityRequest,com.azure.core.util.Context)` was added
* `autoStopDelay()` was added
* `systemData()` was added
* `checkChildrenNameAvailability(models.NameAvailabilityRequest)` was added
* `deleteResourcesOnStop()` was added
* `virtualNicId()` was added

#### `models.Projects` was modified

* `list(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `list(java.lang.String,java.lang.String)` was added

#### `models.DataMigrationServiceStatusResponse` was modified

* `agentConfiguration()` was added

#### `models.ReportableException` was modified

* `withStackTrace(java.lang.String)` was added
* `withMessage(java.lang.String)` was added
* `withFilePath(java.lang.String)` was added
* `withLineNumber(java.lang.String)` was added
* `withHResult(java.lang.Integer)` was added

#### `models.DataIntegrityValidationResult` was modified

* `withValidationErrors(models.ValidationError)` was added
* `withFailedObjects(java.util.Map)` was added

#### `models.MigratePostgreSqlAzureDbForPostgreSqlSyncTaskInput` was modified

* `startedOn()` was added
* `withEncryptedKeyForSecureFields(java.lang.String)` was added
* `encryptedKeyForSecureFields()` was added

#### `models.ConnectToTargetSqlDbTaskInput` was modified

* `withQueryObjectCounts(java.lang.Boolean)` was added
* `queryObjectCounts()` was added

#### `DataMigrationManager` was modified

* `databaseMigrationsMongoToCosmosDbRUMongoes()` was added
* `migrationServices()` was added
* `databaseMigrationsMongoToCosmosDbvCoreMongoes()` was added
* `databaseMigrationsSqlVms()` was added
* `files()` was added
* `databaseMigrationsSqlDbs()` was added
* `sqlMigrationServices()` was added
* `databaseMigrationsSqlMis()` was added
* `serviceTasks()` was added

#### `models.ValidateMigrationInputSqlServerSqlDbSyncTaskProperties` was modified

* `withClientData(java.util.Map)` was added

#### `models.CommandProperties` was modified

* `innerModel()` was added

#### `models.MigrateSqlServerSqlDbDatabaseInput` was modified

* `withSchemaSetting(java.lang.Object)` was added
* `id()` was added
* `withId(java.lang.String)` was added
* `schemaSetting()` was added

#### `models.Project` was modified

* `etag()` was added
* `azureAuthenticationInfo()` was added
* `systemData()` was added

#### `models.DataMigrationService$Definition` was modified

* `withAutoStopDelay(java.lang.String)` was added
* `withDeleteResourcesOnStop(java.lang.Boolean)` was added
* `withVirtualNicId(java.lang.String)` was added

#### `models.QueryExecutionResult` was modified

* `withQueryText(java.lang.String)` was added
* `withStatementsInBatch(java.lang.Long)` was added
* `withSourceResult(models.ExecutionStatistics)` was added
* `withTargetResult(models.ExecutionStatistics)` was added

#### `models.MigrateMySqlAzureDbForMySqlSyncDatabaseInput` was modified

* `withTableMap(java.util.Map)` was added
* `tableMap()` was added

#### `models.SchemaComparisonValidationResult` was modified

* `withValidationErrors(models.ValidationError)` was added
* `withSchemaDifferences(models.SchemaComparisonValidationResultType)` was added

#### `models.ValidateMigrationInputSqlServerSqlMITaskProperties` was modified

* `withClientData(java.util.Map)` was added

#### `models.ConnectToSourceSqlServerTaskInput` was modified

* `withCollectDatabases(java.lang.Boolean)` was added
* `withCollectTdeCertificateInfo(java.lang.Boolean)` was added
* `validateSsisCatalogOnly()` was added
* `encryptedKeyForSecureFields()` was added
* `withValidateSsisCatalogOnly(java.lang.Boolean)` was added
* `withEncryptedKeyForSecureFields(java.lang.String)` was added
* `collectTdeCertificateInfo()` was added
* `collectDatabases()` was added

#### `models.MigrateSqlServerSqlDbTaskProperties` was modified

* `withIsCloneable(java.lang.Boolean)` was added
* `withClientData(java.util.Map)` was added
* `withTaskId(java.lang.String)` was added
* `createdOn()` was added
* `isCloneable()` was added
* `taskId()` was added
* `withCreatedOn(java.lang.String)` was added

#### `models.ConnectToTargetAzureDbForMySqlTaskProperties` was modified

* `withClientData(java.util.Map)` was added

#### `models.GetUserTablesSqlTaskInput` was modified

* `withEncryptedKeyForSecureFields(java.lang.String)` was added
* `encryptedKeyForSecureFields()` was added

#### `models.MigrateSqlServerSqlMISyncTaskInput` was modified

* `numberOfParallelDatabaseMigrations()` was added
* `withNumberOfParallelDatabaseMigrations(java.lang.Float)` was added

#### `models.MigratePostgreSqlAzureDbForPostgreSqlSyncTaskOutputMigrationLevel` was modified

* `state()` was added
* `withDatabaseCount(java.lang.Float)` was added
* `databaseCount()` was added
* `sourceServerType()` was added
* `targetServerType()` was added

#### `models.MigrateSyncCompleteCommandProperties` was modified

* `withCommandId(java.lang.String)` was added
* `errors()` was added
* `commandId()` was added
* `state()` was added

#### `models.ConnectToTargetSqlDbSyncTaskProperties` was modified

* `withClientData(java.util.Map)` was added

#### `models.SchemaComparisonValidationResultType` was modified

* `withUpdateAction(models.UpdateActionType)` was added
* `withObjectType(models.ObjectType)` was added
* `withObjectName(java.lang.String)` was added

#### `models.GetTdeCertificatesSqlTaskProperties` was modified

* `withClientData(java.util.Map)` was added

#### `models.MigrateMISyncCompleteCommandProperties` was modified

* `state()` was added
* `errors()` was added

#### `models.ProjectTask` was modified

* `command(fluent.models.CommandPropertiesInner)` was added
* `systemData()` was added
* `commandWithResponse(fluent.models.CommandPropertiesInner,com.azure.core.util.Context)` was added

#### `models.ConnectToSourceSqlServerTaskOutputAgentJobLevel` was modified

* `validationErrors()` was added

#### `models.AzureActiveDirectoryApp` was modified

* `ignoreAzurePermissions()` was added
* `withIgnoreAzurePermissions(java.lang.Boolean)` was added

#### `models.ConnectToSourceMySqlTaskProperties` was modified

* `withClientData(java.util.Map)` was added

#### `models.Services` was modified

* `checkChildrenNameAvailabilityWithResponse(java.lang.String,java.lang.String,models.NameAvailabilityRequest,com.azure.core.util.Context)` was added
* `checkChildrenNameAvailability(java.lang.String,java.lang.String,models.NameAvailabilityRequest)` was added

#### `models.MigratePostgreSqlAzureDbForPostgreSqlSyncTaskOutputError` was modified

* `events()` was added
* `withEvents(java.util.List)` was added

#### `models.PostgreSqlConnectionInfo` was modified

* `authentication()` was added
* `trustServerCertificate()` was added
* `withAdditionalSettings(java.lang.String)` was added
* `dataSource()` was added
* `serverVersion()` was added
* `withDataSource(java.lang.String)` was added
* `withAuthentication(models.AuthenticationType)` was added
* `encryptConnection()` was added
* `withEncryptConnection(java.lang.Boolean)` was added
* `serverBrandVersion()` was added
* `additionalSettings()` was added
* `withServerBrandVersion(java.lang.String)` was added
* `withTrustServerCertificate(java.lang.Boolean)` was added
* `withServerVersion(java.lang.String)` was added

#### `models.Project$Update` was modified

* `withAzureAuthenticationInfo(models.AzureActiveDirectoryApp)` was added
* `withEtag(java.lang.String)` was added

#### `models.MigrateSqlServerSqlMISyncTaskProperties` was modified

* `withCreatedOn(java.lang.String)` was added
* `createdOn()` was added
* `withClientData(java.util.Map)` was added

#### `models.Tasks` was modified

* `command(java.lang.String,java.lang.String,java.lang.String,java.lang.String,fluent.models.CommandPropertiesInner)` was added
* `commandWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,fluent.models.CommandPropertiesInner,com.azure.core.util.Context)` was added

#### `models.ExecutionStatistics` was modified

* `withExecutionCount(java.lang.Long)` was added
* `withCpuTimeMs(java.lang.Float)` was added
* `withSqlErrors(java.util.List)` was added
* `withHasErrors(java.lang.Boolean)` was added
* `withElapsedTimeMs(java.lang.Float)` was added

#### `models.ConnectToTargetSqlMITaskInput` was modified

* `validateSsisCatalogOnly()` was added
* `withValidateSsisCatalogOnly(java.lang.Boolean)` was added
* `collectLogins()` was added
* `withCollectAgentJobs(java.lang.Boolean)` was added
* `collectAgentJobs()` was added
* `withCollectLogins(java.lang.Boolean)` was added

#### `models.ConnectToSourceMySqlTaskInput` was modified

* `isOfflineMigration()` was added
* `withIsOfflineMigration(java.lang.Boolean)` was added

#### `models.Project$Definition` was modified

* `withEtag(java.lang.String)` was added
* `withAzureAuthenticationInfo(models.AzureActiveDirectoryApp)` was added

#### `models.MigrateSqlServerSqlDbTaskOutputMigrationLevel` was modified

* `migrationValidationResult()` was added
* `migrationReportResult()` was added
* `withMigrationValidationResult(models.MigrationValidationResult)` was added
* `withMigrationReportResult(models.MigrationReportResult)` was added

#### `models.ConnectToSourceSqlServerTaskProperties` was modified

* `taskId()` was added
* `withClientData(java.util.Map)` was added
* `withTaskId(java.lang.String)` was added

#### `models.MigrateMySqlAzureDbForMySqlSyncTaskProperties` was modified

* `withClientData(java.util.Map)` was added

#### `models.WaitStatistics` was modified

* `withWaitType(java.lang.String)` was added
* `withWaitCount(java.lang.Long)` was added
* `withWaitTimeMs(java.lang.Float)` was added

#### `models.MigrateSqlServerSqlDbSyncTaskProperties` was modified

* `withClientData(java.util.Map)` was added

## 1.1.0-beta.1 (2025-07-28)

- Azure Resource Manager DataMigration client library for Java. This package contains Microsoft Azure SDK for DataMigration Management SDK. Data Migration Client. Package tag package-preview-2025-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ServiceOperationList` was removed

#### `models.ServiceOperation` was removed

#### `models.ServiceOperationDisplay` was removed

#### `models.ProjectTaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.MigratePostgreSqlAzureDbForPostgreSqlSyncTaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.GetUserTablesSqlSyncTaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.GetUserTablesSqlTaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.ConnectToTargetSqlDbTaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.ValidateMigrationInputSqlServerSqlMISyncTaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.ConnectToSourcePostgreSqlSyncTaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.ConnectToSourceSqlServerSyncTaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.ConnectToTargetSqlMISyncTaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.MigrateSqlServerSqlMITaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.ConnectToTargetSqlMITaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.ConnectToTargetAzureDbForPostgreSqlSyncTaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.DataMigrationService` was modified

* `nestedCheckNameAvailability(models.NameAvailabilityRequest)` was removed
* `nestedCheckNameAvailabilityWithResponse(models.NameAvailabilityRequest,com.azure.core.util.Context)` was removed

#### `models.Projects` was modified

* `listByResourceGroup(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `listByResourceGroup(java.lang.String,java.lang.String)` was removed

#### `models.ValidateMigrationInputSqlServerSqlDbSyncTaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.CommandProperties` was modified

* `java.util.List errors()` -> `java.util.List errors()`
* `models.CommandState state()` -> `models.CommandState state()`
* `fromJson(com.azure.json.JsonReader)` was removed
* `toJson(com.azure.json.JsonWriter)` was removed
* `java.lang.String commandType()` -> `models.CommandType commandType()`
* `validate()` was removed

#### `models.ValidateMigrationInputSqlServerSqlMITaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.MigrateSqlServerSqlDbTaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.ConnectToTargetAzureDbForMySqlTaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.MigrateSyncCompleteCommandProperties` was modified

* `java.lang.String commandType()` -> `models.CommandType commandType()`

#### `models.ConnectToTargetSqlDbSyncTaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.GetTdeCertificatesSqlTaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.MigrateMISyncCompleteCommandProperties` was modified

* `java.lang.String commandType()` -> `models.CommandType commandType()`

#### `models.ConnectToSourceMySqlTaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.Services` was modified

* `nestedCheckNameAvailability(java.lang.String,java.lang.String,models.NameAvailabilityRequest)` was removed
* `nestedCheckNameAvailabilityWithResponse(java.lang.String,java.lang.String,models.NameAvailabilityRequest,com.azure.core.util.Context)` was removed

#### `models.MigrateSqlServerSqlMISyncTaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.MigrateSqlServerSqlDbTaskOutputMigrationLevel` was modified

* `migrationReport()` was removed

#### `models.ConnectToSourceSqlServerTaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.MigrateMySqlAzureDbForMySqlSyncTaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

#### `models.MigrateSqlServerSqlDbSyncTaskProperties` was modified

* `java.lang.String taskType()` -> `models.TaskType taskType()`

### Features Added

* `models.ScenarioTarget` was added

* `models.OperationListResult` was added

* `models.MigrateSchemaSqlServerSqlDbTaskProperties` was added

* `models.DeleteNode` was added

* `models.DatabaseMigrationSqlMi$DefinitionStages` was added

* `models.SqlDbMigrationStatusDetails` was added

* `models.SqlBackupSetInfo` was added

* `models.UploadOciDriverTaskInput` was added

* `models.GetUserTablesMySqlTaskOutput` was added

* `models.ReplicateMigrationState` was added

* `models.UserAssignedIdentity` was added

* `models.DatabaseMigrationSqlMi$Definition` was added

* `models.MigrateSchemaSqlServerSqlDbTaskOutputMigrationLevel` was added

* `models.SqlMigrationService$UpdateStages` was added

* `models.DatabaseMigrationsMongoToCosmosDbvCoreMongoes` was added

* `models.MigrateSsisTaskOutput` was added

* `models.MigrateOracleAzureDbPostgreSqlSyncTaskOutput` was added

* `models.DatabaseMigrationSqlVm$Update` was added

* `models.MigrateSchemaSqlServerSqlDbTaskOutputDatabaseLevel` was added

* `models.MigrationValidationResult` was added

* `models.MongoDbReplication` was added

* `models.MigrationServiceUpdate` was added

* `models.DatabaseMigrationSqlDb$Update` was added

* `models.MongoDbDatabaseInfo` was added

* `models.DatabaseMigrationSqlMi$UpdateStages` was added

* `models.DatabaseMigrationBaseProperties` was added

* `models.ScenarioSource` was added

* `models.InstallOciDriverTaskInput` was added

* `models.ConnectToSourceOracleSyncTaskInput` was added

* `models.DatabaseMigrationCosmosDbMongo` was added

* `models.DatabaseMigration` was added

* `models.MigrationService$Definition` was added

* `models.DatabaseMigrationPropertiesSqlDb` was added

* `models.GetUserTablesPostgreSqlTaskProperties` was added

* `models.ProjectFileProperties` was added

* `models.MongoDbShardKeySetting` was added

* `models.BackupConfiguration` was added

* `models.DatabaseMigrationSqlMi` was added

* `models.CreatedByType` was added

* `models.MongoDbThrottlingSettings` was added

* `models.ProjectFile$Update` was added

* `models.MigrationServices` was added

* `models.SsisMigrationInfo` was added

* `models.MigrateSchemaSqlServerSqlDbTaskInput` was added

* `models.ErrorInfo` was added

* `models.MigrateSsisTaskOutputProjectLevel` was added

* `models.MigrationValidationDatabaseLevelResult` was added

* `models.DatabaseMigrationProperties` was added

* `models.DatabaseMigrationsSqlDbs` was added

* `models.MigrationOperationInput` was added

* `models.ValidateOracleAzureDbForPostgreSqlSyncTaskProperties` was added

* `models.MigratePostgreSqlAzureDbForPostgreSqlSyncDatabaseTableInput` was added

* `models.SqlMigrationServices` was added

* `models.MigrateMySqlAzureDbForMySqlOfflineTaskOutputDatabaseLevel` was added

* `models.InstallOciDriverTaskProperties` was added

* `models.MigrationStatusDetails` was added

* `models.MigrateSsisTaskOutputMigrationLevel` was added

* `models.DatabaseMigrationSqlVm$DefinitionStages` was added

* `models.MigrationServiceListResult` was added

* `models.ValidateMongoDbTaskProperties` was added

* `models.MongoDbDatabaseProgress` was added

* `models.MongoDbShardKeyInfo` was added

* `models.ConnectToSourceOracleSyncTaskOutput` was added

* `models.OperationsDisplayDefinition` was added

* `models.ConnectToTargetOracleAzureDbForPostgreSqlSyncTaskProperties` was added

* `models.MongoDbClusterInfo` was added

* `models.DatabaseMigrationBase` was added

* `models.MigrateMySqlAzureDbForMySqlOfflineTaskInput` was added

* `models.MongoDbMigrationProgress` was added

* `models.MigrateOracleAzureDbForPostgreSqlSyncTaskProperties` was added

* `models.MongoDbCollectionInfo` was added

* `models.MigrateOracleAzureDbPostgreSqlSyncDatabaseInput` was added

* `models.ServiceTasks` was added

* `models.UploadOciDriverTaskProperties` was added

* `models.DatabaseMigrationBaseListResult` was added

* `models.MigrateOracleAzureDbPostgreSqlSyncTaskOutputDatabaseLevel` was added

* `models.CheckOciDriverTaskProperties` was added

* `models.ProvisioningState` was added

* `models.Files` was added

* `models.GetUserTablesMySqlTaskInput` was added

* `models.MongoDbProgress` was added

* `models.ProjectFile$Definition` was added

* `models.SqlBackupFileInfo` was added

* `models.DatabaseMigrationSqlVm` was added

* `models.SqlMigrationService$Update` was added

* `models.SourceLocation` was added

* `models.GetUserTablesPostgreSqlTaskInput` was added

* `models.SqlDbOfflineConfiguration` was added

* `models.RegenAuthKeys` was added

* `models.MigrationService$Update` was added

* `models.AzureBlob` was added

* `models.DatabaseMigrationSqlVm$Definition` was added

* `models.MigrateSchemaSqlServerSqlDbTaskOutputError` was added

* `models.MigrateSchemaSqlTaskOutputError` was added

* `models.MongoDbDatabaseSettings` was added

* `models.GetUserTablesPostgreSqlTaskOutput` was added

* `models.MigrationService$UpdateStages` was added

* `models.DatabaseMigrationSqlMi$Update` was added

* `models.MongoMigrationCollection` was added

* `models.OperationsDefinition` was added

* `models.GetUserTablesMySqlTaskProperties` was added

* `models.OperationOrigin` was added

* `models.OfflineConfiguration` was added

* `models.MongoDbFinishCommand` was added

* `models.DatabaseMigrationsSqlMis` was added

* `models.MongoDbCollectionSettings` was added

* `models.MongoMigrationProgressDetails` was added

* `models.MigrateSsisTaskInput` was added

* `models.SsisStoreType` was added

* `models.MongoDbMigrationSettings` was added

* `models.DatabaseMigrationCosmosDbMongo$Definition` was added

* `models.MigrationService` was added

* `models.InstallOciDriverTaskOutput` was added

* `models.DatabaseMigrationListResult` was added

* `models.MigrateMySqlAzureDbForMySqlOfflineTaskOutput` was added

* `models.MongoDbShardKeyField` was added

* `models.FileStorageInfo` was added

* `models.ConnectToTargetOracleAzureDbForPostgreSqlSyncTaskOutput` was added

* `models.MigrateOracleAzureDbPostgreSqlSyncTaskOutputDatabaseError` was added

* `models.IntegrationRuntimeMonitoringData` was added

* `models.ConnectToTargetOracleAzureDbForPostgreSqlSyncTaskOutputDatabaseSchemaMapItem` was added

* `models.OracleOciDriverInfo` was added

* `models.GetUserTablesOracleTaskProperties` was added

* `models.TaskType` was added

* `models.MongoDbError` was added

* `models.NodeMonitoringData` was added

* `models.DatabaseMigrationPropertiesCosmosDbMongo` was added

* `models.ConnectToSourceOracleSyncTaskProperties` was added

* `models.ProjectFile$UpdateStages` was added

* `models.SqlMigrationServiceUpdate` was added

* `models.UploadOciDriverTaskOutput` was added

* `models.DatabaseTable` was added

* `models.ProjectFile` was added

* `models.SqlFileShare` was added

* `models.GetUserTablesOracleTaskInput` was added

* `models.MongoDbFinishCommandInput` was added

* `models.MigrateSchemaSqlServerSqlDbTaskOutput` was added

* `models.CommandType` was added

* `models.MongoDbCancelCommand` was added

* `models.SqlConnectionInformation` was added

* `models.MongoDbCollectionProgress` was added

* `models.MigrateMongoDbTaskProperties` was added

* `models.MigrationService$DefinitionStages` was added

* `models.DatabaseMigrationPropertiesSqlVm` was added

* `models.ConnectToMongoDbTaskProperties` was added

* `models.SchemaMigrationSetting` was added

* `models.MigrateMySqlAzureDbForMySqlOfflineTaskOutputMigrationLevel` was added

* `models.MongoDbConnectionInfo` was added

* `models.CheckOciDriverTaskInput` was added

* `models.AuthType` was added

* `models.MigrateOracleAzureDbPostgreSqlSyncTaskOutputTableLevel` was added

* `models.MongoDbClusterType` was added

* `models.MigrateMySqlAzureDbForMySqlOfflineTaskOutputTableLevel` was added

* `models.FileList` was added

* `models.MigrateSsisTaskProperties` was added

* `models.SchemaMigrationStage` was added

* `models.ProjectFile$DefinitionStages` was added

* `models.DatabaseMigrationSqlVm$UpdateStages` was added

* `models.SqlMigrationService$Definition` was added

* `models.ManagedServiceIdentity` was added

* `models.SqlMigrationService$DefinitionStages` was added

* `models.ResourceType` was added

* `models.MongoMigrationStatus` was added

* `models.MongoDbErrorType` was added

* `models.SsisMigrationOverwriteOption` was added

* `models.MongoConnectionInformation` was added

* `models.MigrateSchemaSqlServerSqlDbDatabaseInput` was added

* `models.SqlMigrationListResult` was added

* `models.ManagedServiceIdentityType` was added

* `models.MigrateMySqlAzureDbForMySqlOfflineDatabaseInput` was added

* `models.MongoDbCommandInput` was added

* `models.SqlMigrationService` was added

* `models.MigrateOracleAzureDbPostgreSqlSyncTaskOutputMigrationLevel` was added

* `models.DatabaseMigrationCosmosDbMongo$DefinitionStages` was added

* `models.GetUserTablesOracleTaskOutput` was added

* `models.DatabaseMigrationSqlDb$UpdateStages` was added

* `models.CopyProgressDetails` was added

* `models.MongoDbShardKeyOrder` was added

* `models.DatabaseMigrationsMongoToCosmosDbRUMongoes` was added

* `models.SchemaMigrationOption` was added

* `models.MigrateMySqlAzureDbForMySqlOfflineTaskOutputError` was added

* `models.DatabaseMigrationSqlDb$DefinitionStages` was added

* `models.SystemDataAutoGenerated` was added

* `models.MigrateMySqlAzureDbForMySqlOfflineTaskProperties` was added

* `models.AuthenticationKeys` was added

* `models.MongoDbObjectInfo` was added

* `models.DatabaseMigrationCosmosDbMongoListResult` was added

* `models.OracleConnectionInfo` was added

* `models.TargetLocation` was added

* `models.DatabaseMigrationPropertiesSqlMi` was added

* `models.MigrateOracleAzureDbPostgreSqlSyncTaskOutputError` was added

* `models.MongoDbMigrationState` was added

* `models.DatabaseMigrationsSqlVms` was added

* `models.MigrateOracleAzureDbPostgreSqlSyncTaskInput` was added

* `models.ValidateOracleAzureDbPostgreSqlSyncTaskOutput` was added

* `models.DatabaseMigrationSqlDb` was added

* `models.MongoDbRestartCommand` was added

* `models.SsisMigrationStage` was added

* `models.DatabaseMigrationSqlDb$Definition` was added

* `models.MongoDbProgressResultType` was added

* `models.ConnectToTargetOracleAzureDbForPostgreSqlSyncTaskInput` was added

* `models.CheckOciDriverTaskOutput` was added

#### `models.MigrateSqlServerSqlMITaskInput` was modified

* `encryptedKeyForSecureFields()` was added
* `startedOn()` was added
* `withStartedOn(java.lang.String)` was added
* `aadDomainName()` was added
* `withAadDomainName(java.lang.String)` was added
* `withEncryptedKeyForSecureFields(java.lang.String)` was added

#### `models.MigrateSqlServerSqlDbTaskInput` was modified

* `withEncryptedKeyForSecureFields(java.lang.String)` was added
* `startedOn()` was added
* `encryptedKeyForSecureFields()` was added
* `withStartedOn(java.lang.String)` was added

#### `models.MigrateSqlServerSqlDbTaskOutputValidationResult` was modified

* `id()` was added

#### `models.QueryAnalysisValidationResult` was modified

* `withQueryResults(models.QueryExecutionResult)` was added
* `withValidationErrors(models.ValidationError)` was added

#### `models.ProjectTaskProperties` was modified

* `withClientData(java.util.Map)` was added
* `clientData()` was added

#### `models.MigratePostgreSqlAzureDbForPostgreSqlSyncTaskProperties` was modified

* `withIsCloneable(java.lang.Boolean)` was added
* `withClientData(java.util.Map)` was added
* `withCreatedOn(java.lang.String)` was added
* `createdOn()` was added
* `isCloneable()` was added
* `taskId()` was added
* `withTaskId(java.lang.String)` was added

#### `models.GetUserTablesSqlSyncTaskProperties` was modified

* `withClientData(java.util.Map)` was added

#### `models.SqlConnectionInfo` was modified

* `withServerVersion(java.lang.String)` was added
* `serverBrandVersion()` was added
* `withPort(java.lang.Integer)` was added
* `resourceId()` was added
* `withResourceId(java.lang.String)` was added
* `withServerName(java.lang.String)` was added
* `port()` was added
* `serverName()` was added
* `withServerBrandVersion(java.lang.String)` was added
* `serverVersion()` was added

#### `models.MigratePostgreSqlAzureDbForPostgreSqlSyncDatabaseInput` was modified

* `withSelectedTables(java.util.List)` was added
* `selectedTables()` was added
* `id()` was added

#### `models.MigrateSqlServerSqlMIDatabaseInput` was modified

* `withId(java.lang.String)` was added
* `id()` was added

#### `models.GetUserTablesSqlTaskProperties` was modified

* `taskId()` was added
* `withClientData(java.util.Map)` was added
* `withTaskId(java.lang.String)` was added

#### `models.ConnectToTargetAzureDbForMySqlTaskInput` was modified

* `withIsOfflineMigration(java.lang.Boolean)` was added
* `isOfflineMigration()` was added

#### `models.ConnectToTargetSqlDbTaskProperties` was modified

* `withClientData(java.util.Map)` was added
* `withCreatedOn(java.lang.String)` was added
* `createdOn()` was added

#### `models.MySqlConnectionInfo` was modified

* `authentication()` was added
* `additionalSettings()` was added
* `withEncryptConnection(java.lang.Boolean)` was added
* `withDataSource(java.lang.String)` was added
* `withAuthentication(models.AuthenticationType)` was added
* `dataSource()` was added
* `encryptConnection()` was added
* `withAdditionalSettings(java.lang.String)` was added

#### `models.ValidationError` was modified

* `withSeverity(models.Severity)` was added
* `withText(java.lang.String)` was added

#### `models.ValidateMigrationInputSqlServerSqlMISyncTaskProperties` was modified

* `withClientData(java.util.Map)` was added

#### `models.ConnectToSourcePostgreSqlSyncTaskProperties` was modified

* `withClientData(java.util.Map)` was added

#### `models.ConnectToSourceSqlServerSyncTaskProperties` was modified

* `withClientData(java.util.Map)` was added

#### `models.ConnectToTargetSqlMISyncTaskProperties` was modified

* `withClientData(java.util.Map)` was added

#### `models.MigrateSqlServerSqlDbTaskOutputDatabaseLevelValidationResult` was modified

* `id()` was added

#### `models.MigrationReportResult` was modified

* `withId(java.lang.String)` was added
* `withReportUrl(java.lang.String)` was added

#### `models.MigrateSqlServerSqlMITaskProperties` was modified

* `withCreatedOn(java.lang.String)` was added
* `createdOn()` was added
* `withClientData(java.util.Map)` was added
* `withIsCloneable(java.lang.Boolean)` was added
* `withParentTaskId(java.lang.String)` was added
* `withTaskId(java.lang.String)` was added
* `parentTaskId()` was added
* `taskId()` was added
* `isCloneable()` was added

#### `models.ConnectToTargetSqlMITaskProperties` was modified

* `withClientData(java.util.Map)` was added

#### `models.ConnectToTargetAzureDbForPostgreSqlSyncTaskProperties` was modified

* `withClientData(java.util.Map)` was added

#### `models.ConnectToSourceSqlServerTaskOutputTaskLevel` was modified

* `databaseTdeCertificateMapping()` was added

#### `models.DataMigrationService$Update` was modified

* `withAutoStopDelay(java.lang.String)` was added
* `withDeleteResourcesOnStop(java.lang.Boolean)` was added
* `withVirtualNicId(java.lang.String)` was added

#### `models.DataMigrationService` was modified

* `virtualNicId()` was added
* `checkChildrenNameAvailability(models.NameAvailabilityRequest)` was added
* `systemData()` was added
* `checkChildrenNameAvailabilityWithResponse(models.NameAvailabilityRequest,com.azure.core.util.Context)` was added
* `autoStopDelay()` was added
* `deleteResourcesOnStop()` was added

#### `models.Projects` was modified

* `list(java.lang.String,java.lang.String)` was added
* `list(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.DataMigrationServiceStatusResponse` was modified

* `agentConfiguration()` was added

#### `models.ReportableException` was modified

* `withLineNumber(java.lang.String)` was added
* `withMessage(java.lang.String)` was added
* `withStackTrace(java.lang.String)` was added
* `withFilePath(java.lang.String)` was added
* `withHResult(java.lang.Integer)` was added

#### `models.DataIntegrityValidationResult` was modified

* `withValidationErrors(models.ValidationError)` was added
* `withFailedObjects(java.util.Map)` was added

#### `models.MigratePostgreSqlAzureDbForPostgreSqlSyncTaskInput` was modified

* `encryptedKeyForSecureFields()` was added
* `startedOn()` was added
* `withEncryptedKeyForSecureFields(java.lang.String)` was added

#### `models.ConnectToTargetSqlDbTaskInput` was modified

* `queryObjectCounts()` was added
* `withQueryObjectCounts(java.lang.Boolean)` was added

#### `DataMigrationManager` was modified

* `databaseMigrationsMongoToCosmosDbRUMongoes()` was added
* `sqlMigrationServices()` was added
* `databaseMigrationsSqlVms()` was added
* `migrationServices()` was added
* `databaseMigrationsSqlDbs()` was added
* `databaseMigrationsSqlMis()` was added
* `files()` was added
* `serviceTasks()` was added
* `databaseMigrationsMongoToCosmosDbvCoreMongoes()` was added

#### `models.ValidateMigrationInputSqlServerSqlDbSyncTaskProperties` was modified

* `withClientData(java.util.Map)` was added

#### `models.CommandProperties` was modified

* `innerModel()` was added

#### `models.MigrateSqlServerSqlDbDatabaseInput` was modified

* `id()` was added
* `withId(java.lang.String)` was added
* `withSchemaSetting(java.lang.Object)` was added
* `schemaSetting()` was added

#### `models.Project` was modified

* `systemData()` was added
* `etag()` was added
* `azureAuthenticationInfo()` was added

#### `models.DataMigrationService$Definition` was modified

* `withVirtualNicId(java.lang.String)` was added
* `withAutoStopDelay(java.lang.String)` was added
* `withDeleteResourcesOnStop(java.lang.Boolean)` was added

#### `models.QueryExecutionResult` was modified

* `withSourceResult(models.ExecutionStatistics)` was added
* `withStatementsInBatch(java.lang.Long)` was added
* `withQueryText(java.lang.String)` was added
* `withTargetResult(models.ExecutionStatistics)` was added

#### `models.MigrateMySqlAzureDbForMySqlSyncDatabaseInput` was modified

* `withTableMap(java.util.Map)` was added
* `tableMap()` was added

#### `models.SchemaComparisonValidationResult` was modified

* `withSchemaDifferences(models.SchemaComparisonValidationResultType)` was added
* `withValidationErrors(models.ValidationError)` was added

#### `models.ValidateMigrationInputSqlServerSqlMITaskProperties` was modified

* `withClientData(java.util.Map)` was added

#### `models.ConnectToSourceSqlServerTaskInput` was modified

* `withEncryptedKeyForSecureFields(java.lang.String)` was added
* `collectTdeCertificateInfo()` was added
* `validateSsisCatalogOnly()` was added
* `withCollectTdeCertificateInfo(java.lang.Boolean)` was added
* `withValidateSsisCatalogOnly(java.lang.Boolean)` was added
* `collectDatabases()` was added
* `encryptedKeyForSecureFields()` was added
* `withCollectDatabases(java.lang.Boolean)` was added

#### `models.MigrateSqlServerSqlDbTaskProperties` was modified

* `createdOn()` was added
* `withCreatedOn(java.lang.String)` was added
* `withIsCloneable(java.lang.Boolean)` was added
* `taskId()` was added
* `withClientData(java.util.Map)` was added
* `withTaskId(java.lang.String)` was added
* `isCloneable()` was added

#### `models.ConnectToTargetAzureDbForMySqlTaskProperties` was modified

* `withClientData(java.util.Map)` was added

#### `models.GetUserTablesSqlTaskInput` was modified

* `encryptedKeyForSecureFields()` was added
* `withEncryptedKeyForSecureFields(java.lang.String)` was added

#### `models.MigrateSqlServerSqlMISyncTaskInput` was modified

* `numberOfParallelDatabaseMigrations()` was added
* `withNumberOfParallelDatabaseMigrations(java.lang.Float)` was added

#### `models.MigratePostgreSqlAzureDbForPostgreSqlSyncTaskOutputMigrationLevel` was modified

* `state()` was added
* `targetServerType()` was added
* `withDatabaseCount(java.lang.Float)` was added
* `sourceServerType()` was added
* `databaseCount()` was added

#### `models.MigrateSyncCompleteCommandProperties` was modified

* `errors()` was added
* `commandId()` was added
* `state()` was added
* `withCommandId(java.lang.String)` was added

#### `models.ConnectToTargetSqlDbSyncTaskProperties` was modified

* `withClientData(java.util.Map)` was added

#### `models.SchemaComparisonValidationResultType` was modified

* `withUpdateAction(models.UpdateActionType)` was added
* `withObjectName(java.lang.String)` was added
* `withObjectType(models.ObjectType)` was added

#### `models.GetTdeCertificatesSqlTaskProperties` was modified

* `withClientData(java.util.Map)` was added

#### `models.MigrateMISyncCompleteCommandProperties` was modified

* `state()` was added
* `errors()` was added

#### `models.ProjectTask` was modified

* `command(fluent.models.CommandPropertiesInner)` was added
* `systemData()` was added
* `commandWithResponse(fluent.models.CommandPropertiesInner,com.azure.core.util.Context)` was added

#### `models.ConnectToSourceSqlServerTaskOutputAgentJobLevel` was modified

* `validationErrors()` was added

#### `models.AzureActiveDirectoryApp` was modified

* `withIgnoreAzurePermissions(java.lang.Boolean)` was added
* `ignoreAzurePermissions()` was added

#### `models.ConnectToSourceMySqlTaskProperties` was modified

* `withClientData(java.util.Map)` was added

#### `models.Services` was modified

* `checkChildrenNameAvailability(java.lang.String,java.lang.String,models.NameAvailabilityRequest)` was added
* `checkChildrenNameAvailabilityWithResponse(java.lang.String,java.lang.String,models.NameAvailabilityRequest,com.azure.core.util.Context)` was added

#### `models.MigratePostgreSqlAzureDbForPostgreSqlSyncTaskOutputError` was modified

* `withEvents(java.util.List)` was added
* `events()` was added

#### `models.PostgreSqlConnectionInfo` was modified

* `authentication()` was added
* `withAdditionalSettings(java.lang.String)` was added
* `additionalSettings()` was added
* `serverVersion()` was added
* `withServerVersion(java.lang.String)` was added
* `serverBrandVersion()` was added
* `withAuthentication(models.AuthenticationType)` was added
* `withServerBrandVersion(java.lang.String)` was added
* `withTrustServerCertificate(java.lang.Boolean)` was added
* `withEncryptConnection(java.lang.Boolean)` was added
* `dataSource()` was added
* `trustServerCertificate()` was added
* `withDataSource(java.lang.String)` was added
* `encryptConnection()` was added

#### `models.Project$Update` was modified

* `withAzureAuthenticationInfo(models.AzureActiveDirectoryApp)` was added
* `withEtag(java.lang.String)` was added

#### `models.MigrateSqlServerSqlMISyncTaskProperties` was modified

* `withClientData(java.util.Map)` was added
* `createdOn()` was added
* `withCreatedOn(java.lang.String)` was added

#### `models.Tasks` was modified

* `command(java.lang.String,java.lang.String,java.lang.String,java.lang.String,fluent.models.CommandPropertiesInner)` was added
* `commandWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,fluent.models.CommandPropertiesInner,com.azure.core.util.Context)` was added

#### `models.ExecutionStatistics` was modified

* `withSqlErrors(java.util.List)` was added
* `withHasErrors(java.lang.Boolean)` was added
* `withElapsedTimeMs(java.lang.Float)` was added
* `withCpuTimeMs(java.lang.Float)` was added
* `withExecutionCount(java.lang.Long)` was added

#### `models.ConnectToTargetSqlMITaskInput` was modified

* `validateSsisCatalogOnly()` was added
* `withCollectLogins(java.lang.Boolean)` was added
* `withCollectAgentJobs(java.lang.Boolean)` was added
* `collectAgentJobs()` was added
* `withValidateSsisCatalogOnly(java.lang.Boolean)` was added
* `collectLogins()` was added

#### `models.ConnectToSourceMySqlTaskInput` was modified

* `isOfflineMigration()` was added
* `withIsOfflineMigration(java.lang.Boolean)` was added

#### `models.Project$Definition` was modified

* `withEtag(java.lang.String)` was added
* `withAzureAuthenticationInfo(models.AzureActiveDirectoryApp)` was added

#### `models.MigrateSqlServerSqlDbTaskOutputMigrationLevel` was modified

* `withMigrationReportResult(models.MigrationReportResult)` was added
* `migrationReportResult()` was added
* `migrationValidationResult()` was added
* `withMigrationValidationResult(models.MigrationValidationResult)` was added

#### `models.ConnectToSourceSqlServerTaskProperties` was modified

* `withClientData(java.util.Map)` was added
* `taskId()` was added
* `withTaskId(java.lang.String)` was added

#### `models.MigrateMySqlAzureDbForMySqlSyncTaskProperties` was modified

* `withClientData(java.util.Map)` was added

#### `models.WaitStatistics` was modified

* `withWaitTimeMs(java.lang.Float)` was added
* `withWaitType(java.lang.String)` was added
* `withWaitCount(java.lang.Long)` was added

#### `models.MigrateSqlServerSqlDbSyncTaskProperties` was modified

* `withClientData(java.util.Map)` was added

## 1.0.0 (2024-12-24)

- Azure Resource Manager DataMigration client library for Java. This package contains Microsoft Azure SDK for DataMigration Management SDK. Data Migration Client. Package tag package-2018-04-19. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ValidateMigrationInputSqlServerSqlDbSyncTaskProperties` was modified

* `errors()` was removed
* `state()` was removed
* `commands()` was removed

#### `models.MigrateSqlServerSqlMISyncTaskOutputMigrationLevel` was modified

* `id()` was removed

#### `models.MigrateSqlServerSqlDbTaskOutputValidationResult` was modified

* `id()` was removed

#### `models.MigrateMySqlAzureDbForMySqlSyncTaskOutputDatabaseError` was modified

* `id()` was removed

#### `models.MigratePostgreSqlAzureDbForPostgreSqlSyncTaskProperties` was modified

* `commands()` was removed
* `errors()` was removed
* `state()` was removed

#### `models.MigrateSqlServerSqlDbSyncTaskOutputDatabaseError` was modified

* `id()` was removed

#### `models.MigrateSqlServerSqlMISyncTaskOutputDatabaseLevel` was modified

* `id()` was removed

#### `models.GetUserTablesSqlSyncTaskProperties` was modified

* `commands()` was removed
* `state()` was removed
* `errors()` was removed

#### `models.MigrateSqlServerSqlDbTaskOutputTableLevel` was modified

* `id()` was removed

#### `models.MigrateMySqlAzureDbForMySqlSyncTaskOutputTableLevel` was modified

* `id()` was removed

#### `models.ValidateMigrationInputSqlServerSqlMITaskProperties` was modified

* `state()` was removed
* `commands()` was removed
* `errors()` was removed

#### `models.GetUserTablesSqlTaskProperties` was modified

* `state()` was removed
* `errors()` was removed
* `commands()` was removed

#### `models.MigrateSqlServerSqlDbTaskProperties` was modified

* `errors()` was removed
* `state()` was removed
* `commands()` was removed

#### `models.ConnectToTargetAzureDbForMySqlTaskProperties` was modified

* `state()` was removed
* `commands()` was removed
* `errors()` was removed

#### `models.MigrateSqlServerSqlMITaskOutputMigrationLevel` was modified

* `id()` was removed

#### `models.MigratePostgreSqlAzureDbForPostgreSqlSyncTaskOutputMigrationLevel` was modified

* `id()` was removed

#### `models.MigrateSqlServerSqlDbTaskOutputError` was modified

* `id()` was removed

#### `models.ConnectToTargetSqlDbTaskProperties` was modified

* `errors()` was removed
* `commands()` was removed
* `state()` was removed

#### `models.MigrateSyncCompleteCommandProperties` was modified

* `state()` was removed
* `errors()` was removed

#### `models.ConnectToTargetSqlDbSyncTaskProperties` was modified

* `errors()` was removed
* `commands()` was removed
* `state()` was removed

#### `models.MigrateSqlServerSqlDbSyncTaskOutputTableLevel` was modified

* `id()` was removed

#### `models.ValidateMigrationInputSqlServerSqlMISyncTaskProperties` was modified

* `errors()` was removed
* `state()` was removed
* `commands()` was removed

#### `models.ConnectToSourcePostgreSqlSyncTaskProperties` was modified

* `commands()` was removed
* `errors()` was removed
* `state()` was removed

#### `models.MigrateSqlServerSqlDbSyncTaskOutputMigrationLevel` was modified

* `id()` was removed

#### `models.MigrateSqlServerSqlMITaskOutputLoginLevel` was modified

* `id()` was removed

#### `models.GetTdeCertificatesSqlTaskProperties` was modified

* `state()` was removed
* `commands()` was removed
* `errors()` was removed

#### `models.MigrateMISyncCompleteCommandProperties` was modified

* `state()` was removed
* `errors()` was removed

#### `models.ConnectToSourceSqlServerTaskOutputAgentJobLevel` was modified

* `id()` was removed

#### `models.MigrateSqlServerSqlMISyncTaskOutputError` was modified

* `id()` was removed

#### `models.ConnectToSourceSqlServerSyncTaskProperties` was modified

* `state()` was removed
* `errors()` was removed
* `commands()` was removed

#### `models.ConnectToSourceMySqlTaskProperties` was modified

* `errors()` was removed
* `state()` was removed
* `commands()` was removed

#### `models.MigrateSqlServerSqlMITaskOutputError` was modified

* `id()` was removed

#### `models.MigrateSqlServerSqlDbSyncTaskOutputDatabaseLevel` was modified

* `id()` was removed

#### `models.ConnectToTargetSqlMISyncTaskProperties` was modified

* `commands()` was removed
* `state()` was removed
* `errors()` was removed

#### `models.MigratePostgreSqlAzureDbForPostgreSqlSyncTaskOutputError` was modified

* `id()` was removed

#### `models.MigrateSqlServerSqlDbTaskOutputDatabaseLevelValidationResult` was modified

* `id()` was removed

#### `models.MigrateSqlServerSqlMITaskOutputDatabaseLevel` was modified

* `id()` was removed

#### `models.MigrateSqlServerSqlMITaskProperties` was modified

* `errors()` was removed
* `state()` was removed
* `commands()` was removed

#### `models.MigrateMySqlAzureDbForMySqlSyncTaskOutputDatabaseLevel` was modified

* `id()` was removed

#### `models.ConnectToSourceSqlServerTaskOutputLoginLevel` was modified

* `id()` was removed

#### `models.ConnectToTargetSqlMITaskProperties` was modified

* `commands()` was removed
* `state()` was removed
* `errors()` was removed

#### `models.ConnectToTargetAzureDbForPostgreSqlSyncTaskProperties` was modified

* `errors()` was removed
* `state()` was removed
* `commands()` was removed

#### `models.ConnectToSourceSqlServerTaskOutputTaskLevel` was modified

* `id()` was removed

#### `models.MigrateSqlServerSqlMISyncTaskProperties` was modified

* `errors()` was removed
* `commands()` was removed
* `state()` was removed

#### `models.MigratePostgreSqlAzureDbForPostgreSqlSyncTaskOutputDatabaseError` was modified

* `id()` was removed

#### `models.MigrateSqlServerSqlDbTaskOutputMigrationLevel` was modified

* `id()` was removed

#### `models.ConnectToSourceSqlServerTaskProperties` was modified

* `state()` was removed
* `errors()` was removed
* `commands()` was removed

#### `models.MigratePostgreSqlAzureDbForPostgreSqlSyncTaskOutputTableLevel` was modified

* `id()` was removed

#### `models.MigrateSqlServerSqlDbSyncTaskOutputError` was modified

* `id()` was removed

#### `models.MigrateMySqlAzureDbForMySqlSyncTaskOutputError` was modified

* `id()` was removed

#### `models.MigrateMySqlAzureDbForMySqlSyncTaskOutputMigrationLevel` was modified

* `id()` was removed

#### `models.MigrateMySqlAzureDbForMySqlSyncTaskProperties` was modified

* `commands()` was removed
* `errors()` was removed
* `state()` was removed

#### `models.MigrateSqlServerSqlMITaskOutputAgentJobLevel` was modified

* `id()` was removed

#### `models.ConnectToSourceSqlServerTaskOutputDatabaseLevel` was modified

* `id()` was removed

#### `models.MigrateSqlServerSqlDbSyncTaskProperties` was modified

* `state()` was removed
* `commands()` was removed
* `errors()` was removed

#### `models.MigrateSqlServerSqlDbTaskOutputDatabaseLevel` was modified

* `id()` was removed

#### `models.MigratePostgreSqlAzureDbForPostgreSqlSyncTaskOutputDatabaseLevel` was modified

* `id()` was removed

## 1.0.0-beta.3 (2024-10-17)

- Azure Resource Manager DataMigration client library for Java. This package contains Microsoft Azure SDK for DataMigration Management SDK. Data Migration Client. Package tag package-2018-04-19. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.ValidateMigrationInputSqlServerSqlDbSyncTaskProperties` was modified

* `commands()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `taskType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `errors()` was added
* `state()` was added

#### `models.GetTdeCertificatesSqlTaskOutput` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MigrateSqlServerSqlMISyncTaskOutputMigrationLevel` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `id()` was added
* `resultType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CommandProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `commandType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MigrateSqlServerSqlDbDatabaseInput` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MigrateSqlServerSqlMITaskInput` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ValidateMigrationInputSqlServerSqlMISyncTaskOutput` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MigrateSqlServerSqlDbTaskInput` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MigrateSqlServerSqlDbTaskOutputValidationResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `resultType()` was added
* `id()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ValidateSyncMigrationInputSqlServerTaskOutput` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MigrateMySqlAzureDbForMySqlSyncTaskOutputDatabaseError` was modified

* `resultType()` was added
* `id()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MigrateMySqlAzureDbForMySqlSyncTaskOutput` was modified

* `resultType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DatabaseInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.QueryExecutionResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.QueryAnalysisValidationResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MigrateMySqlAzureDbForMySqlSyncDatabaseInput` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ProjectTaskProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `taskType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MigratePostgreSqlAzureDbForPostgreSqlSyncTaskProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `errors()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `commands()` was added
* `taskType()` was added
* `state()` was added

#### `models.SchemaComparisonValidationResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MigrateSqlServerSqlDbSyncTaskOutputDatabaseError` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `resultType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `id()` was added

#### `models.MigrateSqlServerSqlMITaskOutput` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `resultType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MigrateSqlServerSqlMISyncTaskOutputDatabaseLevel` was modified

* `id()` was added
* `resultType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.GetUserTablesSqlSyncTaskProperties` was modified

* `state()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `errors()` was added
* `commands()` was added
* `taskType()` was added

#### `models.MigrateSqlServerSqlDbSyncTaskInput` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.GetUserTablesSqlSyncTaskOutput` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ServiceOperationList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SelectedCertificateInput` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ConnectToTargetSqlDbTaskOutput` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SqlConnectionInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `type()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MigrateSqlServerSqlDbTaskOutputTableLevel` was modified

* `resultType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `id()` was added

#### `models.MigratePostgreSqlAzureDbForPostgreSqlSyncDatabaseInput` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MigrateMySqlAzureDbForMySqlSyncTaskOutputTableLevel` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `id()` was added
* `resultType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ValidateMigrationInputSqlServerSqlMITaskProperties` was modified

* `commands()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `state()` was added
* `errors()` was added
* `taskType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ConnectToSourceSqlServerTaskInput` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ServiceSku` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MigrateSqlServerSqlMIDatabaseInput` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.GetUserTablesSqlTaskProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `state()` was added
* `commands()` was added
* `errors()` was added
* `taskType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ConnectToSourceSqlServerTaskOutput` was modified

* `resultType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MigrateSqlServerSqlDbTaskProperties` was modified

* `state()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `commands()` was added
* `taskType()` was added
* `errors()` was added

#### `models.ConnectToTargetAzureDbForMySqlTaskProperties` was modified

* `state()` was added
* `errors()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `taskType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `commands()` was added

#### `models.ConnectToTargetAzureDbForMySqlTaskInput` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DataItemMigrationSummaryResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.GetUserTablesSqlTaskInput` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MigrateSqlServerSqlDbTaskOutput` was modified

* `resultType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MigrateSqlServerSqlMISyncTaskInput` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ResourceSkuCapacity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MigrateSqlServerSqlMITaskOutputMigrationLevel` was modified

* `id()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `resultType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.QuotaList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MigratePostgreSqlAzureDbForPostgreSqlSyncTaskOutputMigrationLevel` was modified

* `id()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `resultType()` was added

#### `models.MigrateSqlServerSqlDbTaskOutputError` was modified

* `id()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `resultType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ResourceSkuCosts` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ConnectToTargetSqlDbTaskProperties` was modified

* `commands()` was added
* `taskType()` was added
* `errors()` was added
* `state()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DatabaseFileInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.QuotaName` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MigrateSyncCompleteCommandProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `commandType()` was added
* `errors()` was added
* `state()` was added

#### `models.ConnectToTargetSqlDbSyncTaskProperties` was modified

* `errors()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `state()` was added
* `commands()` was added
* `taskType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MySqlConnectionInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `type()` was added

#### `models.ValidationError` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SchemaComparisonValidationResultType` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MigrateSqlServerSqlDbSyncTaskOutputTableLevel` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `resultType()` was added
* `id()` was added

#### `models.BackupFileInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ConnectToSourcePostgreSqlSyncTaskOutput` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ValidateMigrationInputSqlServerSqlMISyncTaskProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `state()` was added
* `commands()` was added
* `taskType()` was added
* `errors()` was added

#### `models.ServiceSkuList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ServerProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MigrationValidationDatabaseSummaryResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DatabaseBackupInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.FileShare` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ConnectToSourcePostgreSqlSyncTaskProperties` was modified

* `commands()` was added
* `taskType()` was added
* `state()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `errors()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MigrateSqlServerSqlDbSyncTaskOutputMigrationLevel` was modified

* `resultType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `id()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MigrateSqlServerSqlMITaskOutputLoginLevel` was modified

* `resultType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `id()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ValidateMigrationInputSqlServerSqlMISyncTaskInput` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.GetTdeCertificatesSqlTaskProperties` was modified

* `commands()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `taskType()` was added
* `errors()` was added
* `state()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MigrateMISyncCompleteCommandProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `commandType()` was added
* `state()` was added
* `errors()` was added

#### `models.ConnectToTargetSqlMISyncTaskInput` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SyncMigrationDatabaseErrorEvent` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MigrateSqlServerSqlMISyncTaskOutput` was modified

* `resultType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MigratePostgreSqlAzureDbForPostgreSqlSyncTaskOutput` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `resultType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ConnectToTargetSqlDbSyncTaskInput` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MigrateSqlServerSqlDbSyncTaskOutput` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `resultType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ConnectToSourceSqlServerTaskOutputAgentJobLevel` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `id()` was added
* `resultType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AvailableServiceSkuSku` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ResourceSkuCapabilities` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ConnectToTargetAzureDbForPostgreSqlSyncTaskOutput` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MigrateSqlServerSqlMISyncTaskOutputError` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `resultType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `id()` was added

#### `models.ResourceSkuRestrictions` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ConnectToSourceSqlServerSyncTaskProperties` was modified

* `errors()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `state()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `commands()` was added
* `taskType()` was added

#### `models.AzureActiveDirectoryApp` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ConnectToSourceMySqlTaskProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `state()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `errors()` was added
* `taskType()` was added
* `commands()` was added

#### `models.MigrateSqlServerSqlMITaskOutputError` was modified

* `resultType()` was added
* `id()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MigrateSqlServerSqlDbSyncTaskOutputDatabaseLevel` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `id()` was added
* `resultType()` was added

#### `models.ConnectToTargetSqlMISyncTaskProperties` was modified

* `commands()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `state()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `errors()` was added
* `taskType()` was added

#### `models.ResourceSkusResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MigratePostgreSqlAzureDbForPostgreSqlSyncTaskOutputError` was modified

* `resultType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `id()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MigrateSqlServerSqlDbTaskOutputDatabaseLevelValidationResult` was modified

* `id()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `resultType()` was added

#### `models.MigrateSqlServerSqlMITaskOutputDatabaseLevel` was modified

* `id()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `resultType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ServiceOperationDisplay` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MigrationReportResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ConnectToSourcePostgreSqlSyncTaskInput` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MigrateSqlServerSqlDbSyncDatabaseInput` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MigrateSqlServerSqlMITaskProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `commands()` was added
* `taskType()` was added
* `state()` was added
* `errors()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PostgreSqlConnectionInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `type()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ConnectToTargetAzureDbForPostgreSqlSyncTaskInput` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MigrateMySqlAzureDbForMySqlSyncTaskOutputDatabaseLevel` was modified

* `id()` was added
* `resultType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ValidateSyncMigrationInputSqlServerTaskInput` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ConnectionInfo` was modified

* `type()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MigrationValidationOptions` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ConnectToSourceSqlServerTaskOutputLoginLevel` was modified

* `resultType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `id()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ConnectToTargetSqlMITaskProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `state()` was added
* `commands()` was added
* `errors()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `taskType()` was added

#### `models.DataMigrationServiceList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ConnectToTargetAzureDbForPostgreSqlSyncTaskProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `commands()` was added
* `state()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `errors()` was added
* `taskType()` was added

#### `models.ConnectToSourceSqlServerTaskOutputTaskLevel` was modified

* `id()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `resultType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BackupSetInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MigrateSyncCompleteCommandInput` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MigrateSqlServerSqlMISyncTaskProperties` was modified

* `errors()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `commands()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `state()` was added
* `taskType()` was added

#### `models.MigrateMySqlAzureDbForMySqlSyncTaskInput` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ReportableException` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ConnectToSourceNonSqlTaskOutput` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ExecutionStatistics` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ConnectToTargetSqlMITaskInput` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ConnectToSourceMySqlTaskInput` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DataIntegrityValidationResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.GetUserTablesSqlSyncTaskInput` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ConnectToTargetSqlMISyncTaskOutput` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.GetTdeCertificatesSqlTaskInput` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ValidateMigrationInputSqlServerSqlMITaskInput` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MigratePostgreSqlAzureDbForPostgreSqlSyncTaskInput` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MigratePostgreSqlAzureDbForPostgreSqlSyncTaskOutputDatabaseError` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `resultType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `id()` was added

#### `models.ValidateMigrationInputSqlServerSqlMITaskOutput` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MigrateMISyncCompleteCommandInput` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MigrateSqlServerSqlDbTaskOutputMigrationLevel` was modified

* `id()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `resultType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MigrateMISyncCompleteCommandOutput` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ConnectToSourceSqlServerTaskProperties` was modified

* `state()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `errors()` was added
* `taskType()` was added
* `commands()` was added

#### `models.AvailableServiceSkuCapacity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MigratePostgreSqlAzureDbForPostgreSqlSyncTaskOutputTableLevel` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `id()` was added
* `resultType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ConnectToTargetSqlDbTaskInput` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MigrateSqlServerSqlDbSyncTaskOutputError` was modified

* `resultType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `id()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MiSqlConnectionInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `type()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DatabaseSummaryResult` was modified

* `errorPrefix()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `statusMessage()` was added
* `startedOn()` was added
* `state()` was added
* `endedOn()` was added
* `name()` was added
* `itemsCompletedCount()` was added
* `itemsCount()` was added
* `resultPrefix()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MigrateMySqlAzureDbForMySqlSyncTaskOutputError` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `resultType()` was added
* `id()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MigrateMySqlAzureDbForMySqlSyncTaskOutputMigrationLevel` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `id()` was added
* `resultType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MigrateMySqlAzureDbForMySqlSyncTaskProperties` was modified

* `commands()` was added
* `errors()` was added
* `state()` was added
* `taskType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MigrateSqlServerSqlMITaskOutputAgentJobLevel` was modified

* `id()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `resultType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SqlServerSqlMISyncTaskInput` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WaitStatistics` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OrphanedUserInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SqlMigrationTaskInput` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.NameAvailabilityRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ConnectToTargetSqlMITaskOutput` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ConnectToSourceSqlServerTaskOutputDatabaseLevel` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `id()` was added
* `resultType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.GetUserTablesSqlTaskOutput` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BlobShare` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TaskList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MigrateSqlServerSqlDbSyncTaskProperties` was modified

* `state()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `taskType()` was added
* `commands()` was added
* `errors()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MigrationEligibilityInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MigrateSqlServerSqlDbTaskOutputDatabaseLevel` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `id()` was added
* `resultType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ConnectToTargetAzureDbForMySqlTaskOutput` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MigratePostgreSqlAzureDbForPostgreSqlSyncTaskOutputDatabaseLevel` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `resultType()` was added
* `id()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ProjectList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MigrateSyncCompleteCommandOutput` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

## 1.0.0-beta.2 (2023-01-17)

- Azure Resource Manager DataMigration client library for Java. This package contains Microsoft Azure SDK for DataMigration Management SDK. Data Migration Client. Package tag package-2018-04-19. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.AvailableServiceSkuAutoGenerated` was removed

* `models.NonSqlDataMigrationTable` was removed

* `models.DataMigrationResultCode` was removed

* `models.ErrorType` was removed

* `models.MigrationTableMetadata` was removed

* `models.DataMigrationError` was removed

* `models.DatabaseFileInput` was removed

#### `models.AvailableServiceSku` was modified

* `models.AvailableServiceSkuAutoGenerated sku()` -> `models.AvailableServiceSkuSku sku()`

#### `models.Services` was modified

* `delete(java.lang.String,java.lang.String,java.lang.Boolean)` was removed

### Features Added

* `models.AvailableServiceSkuSku` was added

#### `models.Project` was modified

* `resourceGroupName()` was added

#### `DataMigrationManager$Configurable` was modified

* `withScope(java.lang.String)` was added
* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.ProjectTask` was modified

* `resourceGroupName()` was added

#### `models.DataMigrationService` was modified

* `resourceGroupName()` was added

#### `DataMigrationManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

## 1.0.0-beta.1 (2021-04-14)

- Azure Resource Manager DataMigration client library for Java. This package contains Microsoft Azure SDK for DataMigration Management SDK. Data Migration Client. Package tag package-2018-04-19. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
