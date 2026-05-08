# Release History

## 1.1.0 (2026-05-08)

- Azure Resource Manager Automation client library for Java. This package contains Microsoft Azure SDK for Automation Management SDK. Automation Client. Package api-version 2024-10-23. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.PrivateEndpointConnectionListResult` was removed

#### `models.ConnectionTypeListResult` was removed

#### `models.JobListResultV2` was removed

#### `models.ModuleListResult` was removed

#### `models.CredentialListResult` was removed

#### `models.DscCompilationJob$DefinitionStages` was removed

#### `models.SourceControlSyncJobListResult` was removed

#### `models.DscCompilationJobs` was removed

#### `models.JobScheduleListResult` was removed

#### `models.PrivateLinkResourceListResult` was removed

#### `models.CertificateListResult` was removed

#### `models.DscNodeListResult` was removed

#### `models.DscConfigurationListResult` was removed

#### `models.VariableListResult` was removed

#### `models.HybridRunbookWorkersListResult` was removed

#### `models.ScheduleListResult` was removed

#### `models.SourceControlSyncJobStreamsListBySyncJob` was removed

#### `models.ConnectionListResult` was removed

#### `models.JobStreamListResult` was removed

#### `models.DscCompilationJobCreateParameters` was removed

#### `models.WatcherListResult` was removed

#### `models.DscNodeReportListResult` was removed

#### `models.OperationListResult` was removed

#### `models.DscCompilationJob$Definition` was removed

#### `models.DscCompilationJobStreams` was removed

#### `models.AutomationAccountListResult` was removed

#### `models.WebhookListResult` was removed

#### `models.DscCompilationJob` was removed

#### `models.DscNodeConfigurationListResult` was removed

#### `models.StatisticsListResult` was removed

#### `models.RunbookListResult` was removed

#### `models.ActivityListResult` was removed

#### `models.UsageListResult` was removed

#### `models.DscCompilationJobListResult` was removed

#### `models.SourceControlListResult` was removed

#### `models.TypeFieldListResult` was removed

#### `models.HybridRunbookWorkerGroupsListResult` was removed

#### `models.SoftwareUpdateConfigurationTasks` was modified

* `validate()` was removed

#### `models.ContentSource` was modified

* `validate()` was removed

#### `models.PythonPackageCreateParameters` was modified

* `validate()` was removed

#### `models.SourceControlSyncJobCreateParameters` was modified

* `validate()` was removed

#### `models.TagSettingsProperties` was modified

* `validate()` was removed

#### `models.JobCreateParameters` was modified

* `validate()` was removed

#### `models.ActivityParameterSet` was modified

* `ActivityParameterSet()` was changed to private access
* `withParameters(java.util.List)` was removed
* `withName(java.lang.String)` was removed
* `validate()` was removed

#### `models.EncryptionProperties` was modified

* `validate()` was removed

#### `models.Identity` was modified

* `validate()` was removed

#### `models.OperationDisplay` was modified

* `OperationDisplay()` was changed to private access
* `withOperation(java.lang.String)` was removed
* `validate()` was removed
* `withResource(java.lang.String)` was removed
* `withProvider(java.lang.String)` was removed

#### `models.DeletedAutomationAccount` was modified

* `DeletedAutomationAccount()` was changed to private access
* `withAutomationAccountResourceId(java.lang.String)` was removed
* `withId(java.lang.String)` was removed
* `withName(java.lang.String)` was removed
* `validate()` was removed
* `withLocation(java.lang.String)` was removed
* `withAutomationAccountId(java.lang.String)` was removed
* `withLocationPropertiesLocation(java.lang.String)` was removed
* `withType(java.lang.String)` was removed

#### `models.CredentialUpdateParameters` was modified

* `validate()` was removed

#### `models.HybridRunbookWorkerMoveParameters` was modified

* `validate()` was removed

#### `models.IdentityUserAssignedIdentities` was modified

* `validate()` was removed

#### `models.PrivateEndpointProperty` was modified

* `validate()` was removed

#### `models.ConnectionUpdateParameters` was modified

* `validate()` was removed

#### `models.KeyVaultProperties` was modified

* `validate()` was removed

#### `models.Sku` was modified

* `validate()` was removed

#### `models.WebhookCreateOrUpdateParameters` was modified

* `validate()` was removed

#### `models.AutomationAccountUpdateParameters` was modified

* `validate()` was removed

#### `models.DscReportError` was modified

* `DscReportError()` was changed to private access
* `withErrorSource(java.lang.String)` was removed
* `withErrorCode(java.lang.String)` was removed
* `withResourceId(java.lang.String)` was removed
* `withErrorMessage(java.lang.String)` was removed
* `validate()` was removed
* `withErrorDetails(java.lang.String)` was removed
* `withLocale(java.lang.String)` was removed

#### `models.ModuleProvisioningState` was modified

* `CANCELLED` was removed
* `models.ModuleProvisioningState[] values()` -> `java.util.Collection values()`
* `toString()` was removed
* `valueOf(java.lang.String)` was removed

#### `models.JobScheduleCreateParameters` was modified

* `validate()` was removed

#### `models.UpdateConfigurationNavigation` was modified

* `UpdateConfigurationNavigation()` was changed to private access
* `validate()` was removed

#### `models.ScheduleCreateOrUpdateParameters` was modified

* `validate()` was removed

#### `models.HybridRunbookWorker$Definition` was modified

* `withName(java.lang.String)` was removed

#### `models.RawGraphicalRunbookContent` was modified

* `validate()` was removed

#### `models.TestJobCreateParameters` was modified

* `validate()` was removed

#### `models.ScheduleAssociationProperty` was modified

* `validate()` was removed

#### `models.ModuleCreateOrUpdateParameters` was modified

* `validate()` was removed

#### `models.WatcherUpdateParameters` was modified

* `validate()` was removed

#### `models.PrivateLinkServiceConnectionStateProperty` was modified

* `validate()` was removed

#### `models.DscMetaConfiguration` was modified

* `DscMetaConfiguration()` was changed to private access
* `validate()` was removed
* `withCertificateId(java.lang.String)` was removed
* `withAllowModuleOverwrite(java.lang.Boolean)` was removed
* `withRefreshFrequencyMins(java.lang.Integer)` was removed
* `withActionAfterReboot(java.lang.String)` was removed
* `withConfigurationMode(java.lang.String)` was removed
* `withRebootNodeIfNeeded(java.lang.Boolean)` was removed
* `withConfigurationModeFrequencyMins(java.lang.Integer)` was removed

#### `models.AutomationAccountCreateOrUpdateParameters` was modified

* `validate()` was removed

#### `models.SucScheduleProperties` was modified

* `validate()` was removed

#### `models.ScheduleUpdateParameters` was modified

* `validate()` was removed

#### `models.SourceControlSecurityTokenProperties` was modified

* `validate()` was removed

#### `models.SoftwareUpdateConfigurationCollectionItem` was modified

* `SoftwareUpdateConfigurationCollectionItem()` was changed to private access
* `withNextRun(java.time.OffsetDateTime)` was removed
* `withFrequency(models.ScheduleFrequency)` was removed
* `withTasks(models.SoftwareUpdateConfigurationTasks)` was removed
* `withStartTime(java.time.OffsetDateTime)` was removed
* `validate()` was removed
* `withUpdateConfiguration(models.UpdateConfiguration)` was removed

#### `models.DscReportResource` was modified

* `DscReportResource()` was changed to private access
* `withDurationInSeconds(java.lang.Double)` was removed
* `validate()` was removed
* `withDependsOn(java.util.List)` was removed
* `withResourceId(java.lang.String)` was removed
* `withStartDate(java.time.OffsetDateTime)` was removed
* `withError(java.lang.String)` was removed
* `withResourceName(java.lang.String)` was removed
* `withSourceInfo(java.lang.String)` was removed
* `withStatus(java.lang.String)` was removed
* `withModuleVersion(java.lang.String)` was removed
* `withModuleName(java.lang.String)` was removed

#### `models.PythonPackageUpdateParameters` was modified

* `validate()` was removed

#### `models.SourceControlSyncJobs` was modified

* `get(java.lang.String,java.lang.String,java.lang.String,java.util.UUID)` was removed
* `define(java.util.UUID)` was removed
* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,java.util.UUID,com.azure.core.util.Context)` was removed

#### `models.RunbookParameter` was modified

* `validate()` was removed

#### `models.UpdateConfiguration` was modified

* `validate()` was removed

#### `models.DscConfigurationAssociationProperty` was modified

* `validate()` was removed

#### `models.TaskProperties` was modified

* `validate()` was removed

#### `models.WebhookUpdateParameters` was modified

* `validate()` was removed

#### `models.LinuxProperties` was modified

* `validate()` was removed

#### `models.NodeCountProperties` was modified

* `NodeCountProperties()` was changed to private access
* `withCount(java.lang.Integer)` was removed
* `validate()` was removed

#### `models.ActivityOutputType` was modified

* `ActivityOutputType()` was changed to private access
* `withType(java.lang.String)` was removed
* `withName(java.lang.String)` was removed
* `validate()` was removed

#### `models.DscNodeConfigurationCreateOrUpdateParameters` was modified

* `validate()` was removed

#### `models.ActivityParameterValidationSet` was modified

* `ActivityParameterValidationSet()` was changed to private access
* `withMemberValue(java.lang.String)` was removed
* `validate()` was removed

#### `models.JobCollectionItem` was modified

* `java.util.UUID jobId()` -> `java.lang.String jobId()`

#### `models.ContentLink` was modified

* `validate()` was removed

#### `models.HybridRunbookWorkerGroupCreateOrUpdateParameters` was modified

* `validate()` was removed

#### `models.VariableCreateOrUpdateParameters` was modified

* `validate()` was removed

#### `models.ConnectionCreateOrUpdateParameters` was modified

* `validate()` was removed

#### `models.SoftwareUpdateConfigurationMachineRuns` was modified

* `getByIdWithResponse(java.lang.String,java.lang.String,java.util.UUID,java.lang.String,com.azure.core.util.Context)` was removed
* `getById(java.lang.String,java.lang.String,java.util.UUID)` was removed

#### `models.ModuleUpdateParameters` was modified

* `validate()` was removed
* `withLocation(java.lang.String)` was removed
* `withName(java.lang.String)` was removed

#### `models.VariableUpdateParameters` was modified

* `validate()` was removed

#### `models.JobNavigation` was modified

* `JobNavigation()` was changed to private access
* `validate()` was removed

#### `models.Key` was modified

* `Key()` was changed to private access
* `validate()` was removed

#### `models.TargetProperties` was modified

* `validate()` was removed

#### `models.RunAsCredentialAssociationProperty` was modified

* `validate()` was removed

#### `models.RunbookUpdateParameters` was modified

* `validate()` was removed

#### `models.SoftwareUpdateConfigurationMachineRun` was modified

* `java.util.UUID sourceComputerId()` -> `java.lang.String sourceComputerId()`
* `java.util.UUID correlationId()` -> `java.lang.String correlationId()`

#### `models.AdvancedScheduleMonthlyOccurrence` was modified

* `validate()` was removed

#### `models.DscNodeUpdateParametersProperties` was modified

* `validate()` was removed

#### `models.ModuleErrorInfo` was modified

* `ModuleErrorInfo()` was changed to private access
* `validate()` was removed
* `withMessage(java.lang.String)` was removed
* `withCode(java.lang.String)` was removed

#### `models.JobSchedules` was modified

* `getWithResponse(java.lang.String,java.lang.String,java.util.UUID,com.azure.core.util.Context)` was removed
* `define(java.util.UUID)` was removed
* `delete(java.lang.String,java.lang.String,java.util.UUID)` was removed
* `deleteWithResponse(java.lang.String,java.lang.String,java.util.UUID,com.azure.core.util.Context)` was removed
* `get(java.lang.String,java.lang.String,java.util.UUID)` was removed

#### `models.CredentialCreateOrUpdateParameters` was modified

* `validate()` was removed

#### `models.DscNodeUpdateParameters` was modified

* `validate()` was removed

#### `AutomationManager` was modified

* `fluent.AutomationClient serviceClient()` -> `fluent.AutomationManagementClient serviceClient()`
* `dscCompilationJobs()` was removed
* `dscCompilationJobStreams()` was removed

#### `models.NonAzureQueryProperties` was modified

* `validate()` was removed

#### `models.ConnectionTypeCreateOrUpdateParameters` was modified

* `validate()` was removed

#### `models.SoftwareUpdateConfigurationRunTaskProperties` was modified

* `SoftwareUpdateConfigurationRunTaskProperties()` was changed to private access
* `withJobId(java.lang.String)` was removed
* `validate()` was removed
* `withSource(java.lang.String)` was removed
* `withStatus(java.lang.String)` was removed

#### `models.CertificateUpdateParameters` was modified

* `validate()` was removed

#### `models.FieldDefinition` was modified

* `validate()` was removed

#### `models.AgentRegistrationKeys` was modified

* `AgentRegistrationKeys()` was changed to private access
* `withSecondary(java.lang.String)` was removed
* `validate()` was removed
* `withPrimary(java.lang.String)` was removed

#### `models.RunbookDrafts` was modified

* `reactor.core.publisher.Flux getContent(java.lang.String,java.lang.String,java.lang.String)` -> `java.lang.String getContent(java.lang.String,java.lang.String,java.lang.String)`
* `replaceContent(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.BinaryData,long,com.azure.core.util.Context)` was removed
* `replaceContent(java.lang.String,java.lang.String,java.lang.String,reactor.core.publisher.Flux,long)` was removed
* `replaceContent(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.BinaryData,long)` was removed
* `replaceContent(java.lang.String,java.lang.String,java.lang.String,reactor.core.publisher.Flux,long,com.azure.core.util.Context)` was removed

#### `models.UsageCounterName` was modified

* `UsageCounterName()` was changed to private access
* `withLocalizedValue(java.lang.String)` was removed
* `withValue(java.lang.String)` was removed
* `validate()` was removed

#### `models.ContentHash` was modified

* `validate()` was removed

#### `models.Job` was modified

* `java.util.UUID jobId()` -> `java.lang.String jobId()`

#### `models.SourceControlUpdateParameters` was modified

* `validate()` was removed

#### `models.AdvancedSchedule` was modified

* `validate()` was removed

#### `models.EncryptionPropertiesIdentity` was modified

* `validate()` was removed

#### `models.AzureQueryProperties` was modified

* `validate()` was removed

#### `models.WindowsProperties` was modified

* `validate()` was removed

#### `models.SoftwareUpdateConfigurationRuns` was modified

* `getById(java.lang.String,java.lang.String,java.util.UUID)` was removed
* `getByIdWithResponse(java.lang.String,java.lang.String,java.util.UUID,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.NodeReports` was modified

* `java.lang.Object getContent(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` -> `java.lang.String getContent(java.lang.String,java.lang.String,java.lang.String,java.lang.String)`

#### `models.NodeCount` was modified

* `NodeCount()` was changed to private access
* `validate()` was removed
* `withProperties(models.NodeCountProperties)` was removed
* `withName(java.lang.String)` was removed

#### `models.Runbooks` was modified

* `reactor.core.publisher.Flux getContent(java.lang.String,java.lang.String,java.lang.String)` -> `java.lang.String getContent(java.lang.String,java.lang.String,java.lang.String)`

#### `models.DscNodeExtensionHandlerAssociationProperty` was modified

* `DscNodeExtensionHandlerAssociationProperty()` was changed to private access
* `withName(java.lang.String)` was removed
* `withVersion(java.lang.String)` was removed
* `validate()` was removed

#### `models.DscConfigurations` was modified

* `updateWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `createOrUpdateWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `createOrUpdate(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was removed

#### `models.SoftwareUpdateConfigurationRunTasks` was modified

* `SoftwareUpdateConfigurationRunTasks()` was changed to private access
* `validate()` was removed
* `withPreTask(models.SoftwareUpdateConfigurationRunTaskProperties)` was removed
* `withPostTask(models.SoftwareUpdateConfigurationRunTaskProperties)` was removed

#### `models.ConnectionTypeAssociationProperty` was modified

* `validate()` was removed

#### `models.DscConfigurationParameter` was modified

* `validate()` was removed

#### `models.DscConfigurationCreateOrUpdateParameters` was modified

* `validate()` was removed

#### `models.AgentRegistrationRegenerateKeyParameter` was modified

* `validate()` was removed

#### `models.DscConfigurationUpdateParameters` was modified

* `validate()` was removed

#### `models.HybridRunbookWorkerCreateParameters` was modified

* `validate()` was removed
* `withName(java.lang.String)` was removed

#### `models.ActivityParameter` was modified

* `ActivityParameter()` was changed to private access
* `withPosition(java.lang.Long)` was removed
* `withDescription(java.lang.String)` was removed
* `withType(java.lang.String)` was removed
* `withValidationSet(java.util.List)` was removed
* `withValueFromRemainingArguments(java.lang.Boolean)` was removed
* `validate()` was removed
* `withName(java.lang.String)` was removed
* `withIsDynamic(java.lang.Boolean)` was removed
* `withValueFromPipeline(java.lang.Boolean)` was removed
* `withValueFromPipelineByPropertyName(java.lang.Boolean)` was removed
* `withIsMandatory(java.lang.Boolean)` was removed

#### `models.CertificateCreateOrUpdateParameters` was modified

* `validate()` was removed

#### `models.SourceControlSyncJobStreams` was modified

* `get(java.lang.String,java.lang.String,java.lang.String,java.util.UUID,java.lang.String)` was removed
* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,java.util.UUID,java.lang.String,com.azure.core.util.Context)` was removed
* `listBySyncJob(java.lang.String,java.lang.String,java.lang.String,java.util.UUID,java.lang.String,com.azure.core.util.Context)` was removed
* `listBySyncJob(java.lang.String,java.lang.String,java.lang.String,java.util.UUID)` was removed

#### `models.DscReportResourceNavigation` was modified

* `DscReportResourceNavigation()` was changed to private access
* `withResourceId(java.lang.String)` was removed
* `validate()` was removed

#### `models.SourceControlCreateOrUpdateParameters` was modified

* `validate()` was removed

#### `models.RunbookAssociationProperty` was modified

* `validate()` was removed

### Features Added

* `models.Dimension` was added

* `models.PackageErrorInfo` was added

* `models.JobRuntimeEnvironment` was added

* `models.TrackedResource` was added

* `models.Packages` was added

* `models.Package$Update` was added

* `models.PackageProvisioningState` was added

* `models.Package$Definition` was added

* `models.RuntimeEnvironments` was added

* `models.Package$UpdateStages` was added

* `models.HybridRunbookWorker$Update` was added

* `models.HybridRunbookWorker$UpdateStages` was added

* `models.RuntimeEnvironment$UpdateStages` was added

* `models.LogSpecification` was added

* `models.RuntimeEnvironment$DefinitionStages` was added

* `models.Package$DefinitionStages` was added

* `models.Package` was added

* `models.RuntimeEnvironment` was added

* `models.PackageUpdateParameters` was added

* `models.DeletedRunbook` was added

* `models.RuntimeEnvironment$Definition` was added

* `models.RuntimeEnvironment$Update` was added

* `models.OperationPropertiesFormatServiceSpecification` was added

* `models.Python3Packages` was added

* `models.MetricSpecification` was added

* `models.PackageCreateOrUpdateParameters` was added

* `models.RuntimeEnvironmentUpdateParameters` was added

#### `models.DscNodeConfiguration` was modified

* `systemData()` was added

#### `models.Variable` was modified

* `systemData()` was added

#### `models.OperationDisplay` was modified

* `description()` was added

#### `models.SoftwareUpdateConfiguration` was modified

* `systemData()` was added

#### `models.RunbookProperties` was modified

* `runtimeEnvironment()` was added

#### `models.Operation` was modified

* `serviceSpecification()` was added
* `origin()` was added

#### `models.Webhook` was modified

* `systemData()` was added

#### `models.DscConfiguration` was modified

* `systemData()` was added

#### `models.DscNode` was modified

* `systemData()` was added

#### `models.ConnectionType` was modified

* `systemData()` was added

#### `models.ModuleProvisioningState` was modified

* `ModuleProvisioningState()` was added
* `CANCELED` was added

#### `models.TestJobCreateParameters` was modified

* `runtimeEnvironment()` was added
* `withRuntimeEnvironment(java.lang.String)` was added

#### `models.Certificate` was modified

* `systemData()` was added

#### `models.Watcher` was modified

* `systemData()` was added

#### `models.Connection` was modified

* `systemData()` was added

#### `models.HybridRunbookWorker` was modified

* `tags()` was added
* `region()` was added
* `regionName()` was added
* `resourceGroupName()` was added
* `update()` was added
* `location()` was added

#### `models.SourceControlSyncJobs` was modified

* `get(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added
* `define(java.lang.String)` was added
* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.JobSchedule` was modified

* `systemData()` was added

#### `models.AutomationAccounts` was modified

* `listDeletedRunbooks(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listDeletedRunbooks(java.lang.String,java.lang.String)` was added

#### `models.RunbookCreateOrUpdateParameters` was modified

* `runtimeEnvironment()` was added

#### `models.JobCollectionItem` was modified

* `startedBy()` was added
* `jobRuntimeEnvironment()` was added
* `systemData()` was added

#### `models.SoftwareUpdateConfigurationMachineRuns` was modified

* `getById(java.lang.String,java.lang.String,java.lang.String)` was added
* `getByIdWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Runbook` was modified

* `systemData()` was added
* `runtimeEnvironment()` was added

#### `models.AutomationAccount` was modified

* `listDeletedRunbooks(com.azure.core.util.Context)` was added
* `listDeletedRunbooks()` was added

#### `models.RunbookTypeEnum` was modified

* `POWER_SHELL72` was added
* `PYTHON` was added

#### `models.HybridRunbookWorkerGroup` was modified

* `region()` was added
* `location()` was added
* `tags()` was added
* `regionName()` was added

#### `models.PrivateEndpointConnection` was modified

* `systemData()` was added

#### `models.JobSchedules` was modified

* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `define(java.lang.String)` was added
* `get(java.lang.String,java.lang.String,java.lang.String)` was added
* `delete(java.lang.String,java.lang.String,java.lang.String)` was added
* `deleteWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `AutomationManager` was modified

* `python3Packages()` was added
* `runtimeEnvironments()` was added
* `packages()` was added

#### `models.Runbook$Definition` was modified

* `withRuntimeEnvironment(java.lang.String)` was added

#### `models.PrivateLinkResource` was modified

* `systemData()` was added

#### `models.RunbookDrafts` was modified

* `replaceContent(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `replaceContent(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added

#### `models.RunbookCreateOrUpdateProperties` was modified

* `runtimeEnvironment()` was added

#### `models.Job` was modified

* `systemData()` was added
* `jobRuntimeEnvironment()` was added

#### `models.Schedule` was modified

* `systemData()` was added

#### `models.Credential` was modified

* `systemData()` was added

#### `models.SoftwareUpdateConfigurationRuns` was modified

* `getById(java.lang.String,java.lang.String,java.lang.String)` was added
* `getByIdWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.SourceControl` was modified

* `systemData()` was added

#### `models.Module` was modified

* `systemData()` was added

#### `models.SourceControlSyncJobStreams` was modified

* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listBySyncJob(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listBySyncJob(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added
* `get(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added

## 1.0.0 (2024-12-23)

- Azure Resource Manager Automation client library for Java. This package contains Microsoft Azure SDK for Automation Management SDK. Automation Client. Package tag package-2022-02-22. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Other Changes

- Release for Azure Resource Manager Automation client library for Java.

## 1.0.0-beta.3 (2024-10-10)

- Azure Resource Manager Automation client library for Java. This package contains Microsoft Azure SDK for Automation Management SDK. Automation Client. Package tag package-2022-02-22. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.AutomationAccounts` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.RunbookDrafts` was modified

* `java.io.InputStream replaceContent(java.lang.String,java.lang.String,java.lang.String,reactor.core.publisher.Flux,long)` -> `com.azure.core.util.BinaryData replaceContent(java.lang.String,java.lang.String,java.lang.String,reactor.core.publisher.Flux,long)`
* `java.io.InputStream replaceContent(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.BinaryData,long)` -> `com.azure.core.util.BinaryData replaceContent(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.BinaryData,long)`
* `replaceContentWithResponse(java.lang.String,java.lang.String,java.lang.String,reactor.core.publisher.Flux,long,com.azure.core.util.Context)` was removed
* `replaceContentWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.BinaryData,long,com.azure.core.util.Context)` was removed

### Features Added

#### `models.SoftwareUpdateConfigurationTasks` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ContentSource` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateEndpointConnectionListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PythonPackageCreateParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ConnectionTypeListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SourceControlSyncJobCreateParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TagSettingsProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.JobCreateParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ActivityParameterSet` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EncryptionProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Identity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DeletedAutomationAccount` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.JobListResultV2` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ModuleListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CredentialListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CredentialUpdateParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.HybridRunbookWorkerMoveParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.IdentityUserAssignedIdentities` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateEndpointProperty` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ConnectionUpdateParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.KeyVaultProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Sku` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.WebhookCreateOrUpdateParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AutomationAccountUpdateParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SourceControlSyncJobListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DscReportError` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.JobScheduleCreateParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UpdateConfigurationNavigation` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ScheduleCreateOrUpdateParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.RawGraphicalRunbookContent` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TestJobCreateParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ScheduleAssociationProperty` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ModuleCreateOrUpdateParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WatcherUpdateParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.JobScheduleListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateLinkServiceConnectionStateProperty` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DscMetaConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AutomationAccountCreateOrUpdateParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SucScheduleProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ScheduleUpdateParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SourceControlSecurityTokenProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SoftwareUpdateConfigurationCollectionItem` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PrivateLinkResourceListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DscReportResource` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PythonPackageUpdateParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CertificateListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RunbookParameter` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DscNodeListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.UpdateConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DscConfigurationAssociationProperty` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DscConfigurationListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TaskProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WebhookUpdateParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AutomationAccounts` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.LinuxProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VariableListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.NodeCountProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ActivityOutputType` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DscNodeConfigurationCreateOrUpdateParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ActivityParameterValidationSet` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.HybridRunbookWorkersListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ContentLink` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ScheduleListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SourceControlSyncJobStreamsListBySyncJob` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.HybridRunbookWorkerGroupCreateOrUpdateParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VariableCreateOrUpdateParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ConnectionCreateOrUpdateParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ModuleUpdateParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ConnectionListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.VariableUpdateParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DscCompilationJobCreateParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.WatcherListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DscNodeReportListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.JobNavigation` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Key` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TargetProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.RunAsCredentialAssociationProperty` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RunbookUpdateParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AdvancedScheduleMonthlyOccurrence` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DscNodeUpdateParametersProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ModuleErrorInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CredentialCreateOrUpdateParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AutomationAccountListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DscNodeUpdateParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.NonAzureQueryProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.WebhookListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ConnectionTypeCreateOrUpdateParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SoftwareUpdateConfigurationRunTaskProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CertificateUpdateParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.FieldDefinition` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AgentRegistrationKeys` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RunbookDrafts` was modified

* `replaceContent(java.lang.String,java.lang.String,java.lang.String,reactor.core.publisher.Flux,long,com.azure.core.util.Context)` was added
* `replaceContent(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.BinaryData,long,com.azure.core.util.Context)` was added

#### `models.UsageCounterName` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DscNodeConfigurationListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ContentHash` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.StatisticsListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SourceControlUpdateParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RunbookListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AdvancedSchedule` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.EncryptionPropertiesIdentity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ActivityListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AzureQueryProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UsageListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.WindowsProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.NodeCount` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DscNodeExtensionHandlerAssociationProperty` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SoftwareUpdateConfigurationRunTasks` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ConnectionTypeAssociationProperty` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DscConfigurationParameter` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DscConfigurationCreateOrUpdateParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AgentRegistrationRegenerateKeyParameter` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DscConfigurationUpdateParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.HybridRunbookWorkerCreateParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DscCompilationJobListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ActivityParameter` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SourceControlListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TypeFieldListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CertificateCreateOrUpdateParameters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DscReportResourceNavigation` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.HybridRunbookWorkerGroupsListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SourceControlCreateOrUpdateParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.RunbookAssociationProperty` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

## 1.0.0-beta.2 (2022-08-12)

- Azure Resource Manager Automation client library for Java. This package contains Microsoft Azure SDK for Automation Management SDK. Automation Client. Package tag package-2022-02-22. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.RunbookDraftsReplaceContentResponse` was removed

* `models.HybridRunbookWorkerGroupUpdateParameters` was removed

* `models.RunbooksPublishHeaders` was removed

* `models.RunbookDraftsReplaceContentHeaders` was removed

* `models.RunbooksPublishResponse` was removed

#### `models.DscNode` was modified

* `namePropertiesNodeConfigurationName()` was removed

#### `models.HybridRunbookWorker` was modified

* `java.lang.String ip()` -> `java.lang.String ip()`
* `validate()` was removed
* `withLastSeenDateTime(java.time.OffsetDateTime)` was removed
* `java.lang.String name()` -> `java.lang.String name()`
* `java.time.OffsetDateTime lastSeenDateTime()` -> `java.time.OffsetDateTime lastSeenDateTime()`
* `withRegistrationTime(java.time.OffsetDateTime)` was removed
* `withName(java.lang.String)` was removed
* `registrationTime()` was removed
* `withIp(java.lang.String)` was removed

#### `models.HybridRunbookWorkerGroup` was modified

* `hybridRunbookWorkers()` was removed

#### `models.HybridRunbookWorkerGroups` was modified

* `update(java.lang.String,java.lang.String,java.lang.String,models.HybridRunbookWorkerGroupUpdateParameters)` was removed
* `updateWithResponse(java.lang.String,java.lang.String,java.lang.String,models.HybridRunbookWorkerGroupUpdateParameters,com.azure.core.util.Context)` was removed

#### `models.RunbookDrafts` was modified

* `java.lang.String getContent(java.lang.String,java.lang.String,java.lang.String)` -> `reactor.core.publisher.Flux getContent(java.lang.String,java.lang.String,java.lang.String)`
* `replaceContent(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was removed
* `void undoEdit(java.lang.String,java.lang.String,java.lang.String)` -> `models.RunbookDraftUndoEditResult undoEdit(java.lang.String,java.lang.String,java.lang.String)`
* `replaceContent(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Runbooks` was modified

* `java.lang.String getContent(java.lang.String,java.lang.String,java.lang.String)` -> `reactor.core.publisher.Flux getContent(java.lang.String,java.lang.String,java.lang.String)`

#### `models.DscConfigurations` was modified

* `update(java.lang.String,java.lang.String,java.lang.String)` was removed

### Features Added

* `models.PrivateEndpointConnectionListResult` was added

* `models.EncryptionProperties` was added

* `models.Identity` was added

* `models.DeletedAutomationAccount` was added

* `models.GraphicalRunbookContent` was added

* `models.RunbookProperties` was added

* `models.HybridRunbookWorkerGroup$DefinitionStages` was added

* `models.HybridRunbookWorkerMoveParameters` was added

* `models.IdentityUserAssignedIdentities` was added

* `models.PrivateEndpointProperty` was added

* `models.KeyVaultProperties` was added

* `models.RunbookDraftUndoEditResult` was added

* `models.ResourceProviders` was added

* `models.HybridRunbookWorker$Definition` was added

* `models.RawGraphicalRunbookContent` was added

* `models.PrivateLinkServiceConnectionStateProperty` was added

* `models.EncryptionKeySourceType` was added

* `models.PrivateLinkResourceListResult` was added

* `models.WorkerType` was added

* `models.PrivateEndpointConnection$Update` was added

* `models.HybridRunbookWorker$DefinitionStages` was added

* `models.HybridRunbookWorkersListResult` was added

* `models.HybridRunbookWorkerGroupCreateOrUpdateParameters` was added

* `models.PrivateLinkResources` was added

* `models.PrivateEndpointConnection$DefinitionStages` was added

* `models.HybridRunbookWorkerGroup$Definition` was added

* `models.HybridRunbookWorkers` was added

* `models.ResourceIdentityType` was added

* `models.PrivateEndpointConnection` was added

* `models.GraphRunbookType` was added

* `models.PrivateEndpointConnection$UpdateStages` was added

* `models.DeletedAutomationAccountListResult` was added

* `models.PrivateLinkResource` was added

* `models.RunbookCreateOrUpdateProperties` was added

* `models.PrivateEndpointConnection$Definition` was added

* `models.EncryptionPropertiesIdentity` was added

* `models.PrivateEndpointConnections` was added

* `models.HybridRunbookWorkerGroup$UpdateStages` was added

* `models.DeletedAutomationAccounts` was added

* `models.HybridRunbookWorkerGroup$Update` was added

* `models.HybridRunbookWorkerCreateParameters` was added

#### `models.DscNodeConfiguration` was modified

* `resourceGroupName()` was added

#### `models.Variable` was modified

* `resourceGroupName()` was added

#### `models.Webhook` was modified

* `resourceGroupName()` was added

#### `models.DscConfiguration` was modified

* `resourceGroupName()` was added

#### `models.DscNode` was modified

* `namePropertiesName()` was added

#### `models.ConnectionType` was modified

* `resourceGroupName()` was added

#### `models.AutomationAccountUpdateParameters` was modified

* `publicNetworkAccess()` was added
* `disableLocalAuth()` was added
* `identity()` was added
* `encryption()` was added
* `withEncryption(models.EncryptionProperties)` was added
* `withDisableLocalAuth(java.lang.Boolean)` was added
* `withIdentity(models.Identity)` was added
* `withPublicNetworkAccess(java.lang.Boolean)` was added

#### `models.Certificate` was modified

* `resourceGroupName()` was added

#### `models.AutomationAccount$Update` was modified

* `withEncryption(models.EncryptionProperties)` was added
* `withPublicNetworkAccess(java.lang.Boolean)` was added
* `withIdentity(models.Identity)` was added
* `withDisableLocalAuth(java.lang.Boolean)` was added

#### `models.Watcher` was modified

* `stop()` was added
* `startWithResponse(com.azure.core.util.Context)` was added
* `resourceGroupName()` was added
* `start()` was added
* `stopWithResponse(com.azure.core.util.Context)` was added

#### `models.AutomationAccountCreateOrUpdateParameters` was modified

* `withIdentity(models.Identity)` was added
* `encryption()` was added
* `identity()` was added
* `withEncryption(models.EncryptionProperties)` was added
* `disableLocalAuth()` was added
* `withPublicNetworkAccess(java.lang.Boolean)` was added
* `publicNetworkAccess()` was added
* `withDisableLocalAuth(java.lang.Boolean)` was added

#### `models.Connection` was modified

* `resourceGroupName()` was added

#### `models.HybridRunbookWorker` was modified

* `refresh(com.azure.core.util.Context)` was added
* `workerName()` was added
* `registeredDateTime()` was added
* `moveWithResponse(models.HybridRunbookWorkerMoveParameters,com.azure.core.util.Context)` was added
* `vmResourceId()` was added
* `refresh()` was added
* `workerType()` was added
* `innerModel()` was added
* `id()` was added
* `systemData()` was added
* `move(models.HybridRunbookWorkerMoveParameters)` was added
* `type()` was added

#### `models.Runbook` was modified

* `publish(com.azure.core.util.Context)` was added
* `resourceGroupName()` was added
* `publish()` was added

#### `models.AutomationAccount` was modified

* `systemData()` was added
* `automationHybridServiceUrl()` was added
* `privateEndpointConnections()` was added
* `publicNetworkAccess()` was added
* `disableLocalAuth()` was added
* `resourceGroupName()` was added
* `encryption()` was added
* `identity()` was added

#### `AutomationManager$Configurable` was modified

* `withScope(java.lang.String)` was added
* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.HybridRunbookWorkerGroup` was modified

* `type()` was added
* `systemData()` was added
* `resourceGroupName()` was added
* `refresh(com.azure.core.util.Context)` was added
* `update()` was added
* `refresh()` was added

#### `models.HybridRunbookWorkerGroups` was modified

* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `define(java.lang.String)` was added
* `deleteById(java.lang.String)` was added
* `getById(java.lang.String)` was added
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added

#### `AutomationManager` was modified

* `hybridRunbookWorkers()` was added
* `resourceProviders()` was added
* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added
* `deletedAutomationAccounts()` was added
* `privateLinkResources()` was added
* `privateEndpointConnections()` was added

#### `models.RunbookDrafts` was modified

* `replaceContentWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.BinaryData,long,com.azure.core.util.Context)` was added
* `replaceContent(java.lang.String,java.lang.String,java.lang.String,reactor.core.publisher.Flux,long)` was added
* `replaceContent(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.BinaryData,long)` was added
* `replaceContentWithResponse(java.lang.String,java.lang.String,java.lang.String,reactor.core.publisher.Flux,long,com.azure.core.util.Context)` was added

#### `models.Job` was modified

* `stop()` was added
* `resume()` was added
* `resumeWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `suspend()` was added
* `suspendWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `stopWithResponse(java.lang.String,com.azure.core.util.Context)` was added

#### `models.Schedule` was modified

* `resourceGroupName()` was added

#### `models.Credential` was modified

* `resourceGroupName()` was added

#### `models.AutomationAccount$Definition` was modified

* `withEncryption(models.EncryptionProperties)` was added
* `withDisableLocalAuth(java.lang.Boolean)` was added
* `withIdentity(models.Identity)` was added
* `withPublicNetworkAccess(java.lang.Boolean)` was added

#### `models.SourceControl` was modified

* `resourceGroupName()` was added

#### `models.Module` was modified

* `resourceGroupName()` was added

## 1.0.0-beta.1 (2021-04-09)

- Azure Resource Manager Automation client library for Java. This package contains Microsoft Azure SDK for Automation Management SDK. Automation Client. Package tag package-2019-06. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
