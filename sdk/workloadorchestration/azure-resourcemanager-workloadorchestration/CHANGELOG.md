# Release History

## 1.0.0-beta.2 (2026-09-15)

- Azure Resource Manager Workload Orchestration client library for Java. This package contains Microsoft Azure SDK for Workload Orchestration Management SDK. Microsoft.Edge Resource Provider management API. Package api-version 2026-05-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.SolutionVersionProperties` was modified

* `java.lang.String configuration()` -> `com.azure.core.util.BinaryData configuration()`
* `java.lang.String targetLevelConfiguration()` -> `com.azure.core.util.BinaryData targetLevelConfiguration()`
* `validate()` was removed

#### `models.UninstallSolutionParameter` was modified

* `validate()` was removed

#### `models.InstallSolutionParameter` was modified

* `validate()` was removed

#### `models.StageSpec` was modified

* `validate()` was removed

#### `models.ExtendedLocation` was modified

* `validate()` was removed

#### `models.SolutionDependencyParameter` was modified

* `validate()` was removed

#### `models.StageStatus` was modified

* `validate()` was removed

#### `models.AvailableSolutionTemplateVersion` was modified

* `validate()` was removed

#### `models.Schema$Update` was modified

* `withProperties(models.SchemaProperties)` was removed

#### `models.JobParameterBase` was modified

* `validate()` was removed

#### `models.Diagnostic$Update` was modified

* `withProperties(models.DiagnosticProperties)` was removed

#### `models.TargetProperties` was modified

* `validate()` was removed

#### `models.UpdateExternalValidationStatusParameter` was modified

* `validate()` was removed

#### `models.Solution$Update` was modified

* `withProperties(models.SolutionProperties)` was removed

#### `models.SolutionVersionParameter` was modified

* `validate()` was removed

#### `models.SchemaReferenceProperties` was modified

* `validate()` was removed

#### `models.SolutionProperties` was modified

* `validate()` was removed

#### `models.ComponentStatus` was modified

* `validate()` was removed

#### `models.Capability` was modified

* `validate()` was removed

#### `models.SolutionDependency` was modified

* `validate()` was removed

#### `models.SiteReferenceProperties` was modified

* `validate()` was removed

#### `models.InstanceProperties` was modified

* `validate()` was removed

#### `models.BulkDeploySolutionParameter` was modified

* `validate()` was removed

#### `models.SolutionTemplate$Update` was modified

* `withProperties(models.SolutionTemplateProperties)` was removed

#### `models.ConfigTemplate$Update` was modified

* `withProperties(models.ConfigTemplateProperties)` was removed

#### `models.DiagnosticProperties` was modified

* `validate()` was removed

#### `models.ErrorAction` was modified

* `validate()` was removed

#### `models.ReconciliationPolicyProperties` was modified

* `validate()` was removed

#### `models.DeployJobStepStatistics` was modified

* `validate()` was removed

#### `models.Hierarchy` was modified

* `validate()` was removed

#### `models.ExecutionStatus` was modified

* `validate()` was removed

#### `models.JobProperties` was modified

* `validate()` was removed

#### `models.Target$Update` was modified

* `withProperties(models.TargetProperties)` was removed

#### `models.BulkPublishSolutionParameter` was modified

* `validate()` was removed

#### `models.ConfigTemplateVersionProperties` was modified

* `java.lang.String configurations()` -> `com.azure.core.util.BinaryData configurations()`
* `withConfigurations(java.lang.String)` was removed
* `validate()` was removed

#### `models.SchemaVersionProperties` was modified

* `withValue(java.lang.String)` was removed
* `validate()` was removed
* `java.lang.String value()` -> `com.azure.core.util.BinaryData value()`

#### `models.WorkflowVersionProperties` was modified

* `validate()` was removed

#### `models.ConfigTemplateProperties` was modified

* `validate()` was removed

#### `models.RemoveRevisionParameter` was modified

* `validate()` was removed

#### `models.BulkPublishTargetDetails` was modified

* `validate()` was removed

#### `models.DynamicSchemaProperties` was modified

* `validate()` was removed

#### `models.DeploymentStatus` was modified

* `validate()` was removed

#### `models.SolutionVersionSnapshot` was modified

* `validate()` was removed

#### `models.WorkflowProperties` was modified

* `validate()` was removed

#### `models.ContextModel$Update` was modified

* `withProperties(models.ContextProperties)` was removed

#### `models.BulkDeployTargetDetails` was modified

* `validate()` was removed

#### `models.ExecutionProperties` was modified

* `validate()` was removed

#### `models.TaskSpec` was modified

* `validate()` was removed

#### `models.SolutionTemplateProperties` was modified

* `validate()` was removed

#### `models.TaskOption` was modified

* `validate()` was removed

#### `models.VersionParameter` was modified

* `validate()` was removed

#### `models.TargetStatus` was modified

* `validate()` was removed

#### `models.TargetSnapshot` was modified

* `validate()` was removed

#### `models.JobStep` was modified

* `validate()` was removed

#### `models.ContextProperties` was modified

* `validate()` was removed

#### `models.JobStepStatisticsBase` was modified

* `validate()` was removed

#### `models.SolutionTemplateVersionProperties` was modified

* `withConfigurations(java.lang.String)` was removed
* `validate()` was removed
* `java.lang.String configurations()` -> `com.azure.core.util.BinaryData configurations()`

#### `models.DeployJobParameter` was modified

* `validate()` was removed

#### `models.SchemaProperties` was modified

* `validate()` was removed

#### `models.InstanceHistoryProperties` was modified

* `validate()` was removed

#### `models.SolutionTemplateParameter` was modified

* `validate()` was removed

### Features Added

* `models.DiagnosticUpdateProperties` was added

* `models.SolutionMetadataVersion` was added

* `models.ConfigTemplateVersion$UpdateStages` was added

* `models.ConfigTemplateVersion$Definition` was added

* `models.SolutionDeploymentUpdateProperties` was added

* `models.UninstallJobStepStatistics` was added

* `models.ConfigTemplateSchemaProperties` was added

* `models.SolutionDeployment$UpdateStages` was added

* `models.SchemaReference$DefinitionStages` was added

* `models.InternalState` was added

* `models.ConfigTemplateMetadataUpdateProperties` was added

* `models.ConfigTemplateSchemas` was added

* `models.SchemaReference$Update` was added

* `models.BulkReviewTargetDetails` was added

* `models.HierarchyConfigurationMetadataVersions` was added

* `models.SolutionUpdateProperties` was added

* `models.HierarchyConfigurationMetadataVersion` was added

* `models.ConfigTemplateVersion$Update` was added

* `models.SolutionMetadataVersionProperties` was added

* `models.SchemaUpdate` was added

* `models.SolutionDeploymentProperties` was added

* `models.SolutionSchemaProperties` was added

* `models.TargetUpdateProperties` was added

* `models.AdditionalData` was added

* `models.SolutionMetadataProperties` was added

* `models.ConfigTemplateUpdate` was added

* `models.SolutionSchemas` was added

* `models.ConfigTemplateMetadata$Update` was added

* `models.SolutionTemplateVersion$UpdateStages` was added

* `models.SolutionDeployments` was added

* `models.SolutionTemplateVersion$DefinitionStages` was added

* `models.BulkReviewSolutionParameter` was added

* `models.HierarchyMetadata` was added

* `models.SolutionTemplateVersion$Definition` was added

* `models.ConfigurationState` was added

* `models.SolutionUpdate` was added

* `models.ConfigTemplateConfigurationState` was added

* `models.SolutionTemplateUpdateProperties` was added

* `models.HierarchyConfigurationMetadatas` was added

* `models.SolutionTemplateUpdate` was added

* `models.ConfigTemplateUpdateProperties` was added

* `models.ConfigTemplateVersion$DefinitionStages` was added

* `models.HierarchyConfigurationMetadata` was added

* `models.ContextUpdateProperties` was added

* `models.SchemaReference$UpdateStages` was added

* `models.ConfigTemplateSchema` was added

* `models.SolutionTemplateVersion$Update` was added

* `models.HierarchySelector` was added

* `models.ConfigTemplateMetadata$UpdateStages` was added

* `models.StateCategory` was added

* `models.HierarchyConfigurationMetadataVersionProperties` was added

* `models.StageMap` was added

* `models.DiagnosticUpdate` was added

* `models.SchemaUpdateProperties` was added

* `models.TargetMetadata` was added

* `models.SolutionMetadataVersions` was added

* `models.SolutionDeployment$Definition` was added

* `models.ConfigTemplateMetadataUpdate` was added

* `models.ConfigTemplateMetadata$Definition` was added

* `models.SolutionSchema` was added

* `models.SolutionDeployment` was added

* `models.CMStages` was added

* `models.SolutionTemplateMetadata` was added

* `models.ConfigTemplateMetadata$DefinitionStages` was added

* `models.UninstallJobParameter` was added

* `models.SchemaReference$Definition` was added

* `models.PublishJobParameter` was added

* `models.ConfigTemplateMetadatas` was added

* `models.ConfigTemplateMetadataProperties` was added

* `models.SolutionMetadata` was added

* `models.PublishJobStepStatistics` was added

* `models.HierarchyConfigurationMetadataProperties` was added

* `models.SolutionDeploymentUpdate` was added

* `models.ContextUpdate` was added

* `models.SolutionDeployment$DefinitionStages` was added

* `models.SolutionMetadatas` was added

* `models.TargetUpdate` was added

* `models.SolutionTemplateMetadataUpdate` was added

* `models.SolutionDeployment$Update` was added

* `models.ConfigTemplateMetadata` was added

#### `models.SolutionVersionProperties` was modified

* `currentStage()` was added
* `stages()` was added
* `latestActionTriggeredBy()` was added

#### `models.SchemaReferences` was modified

* `deleteById(java.lang.String)` was added
* `deleteByResourceGroup(java.lang.String,java.lang.String)` was added
* `getById(java.lang.String)` was added
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `define(java.lang.String)` was added

#### `models.SchemaReference` was modified

* `refresh()` was added
* `update()` was added
* `refresh(com.azure.core.util.Context)` was added

#### `models.ConfigTemplates` was modified

* `linkToHierarchies(java.lang.String,java.lang.String,models.HierarchySelector,com.azure.core.util.Context)` was added
* `unLinkFromHierarchies(java.lang.String,java.lang.String,models.HierarchySelector,com.azure.core.util.Context)` was added
* `linkToHierarchies(java.lang.String,java.lang.String,models.HierarchySelector)` was added
* `unLinkFromHierarchies(java.lang.String,java.lang.String,models.HierarchySelector)` was added

#### `models.Schema$Update` was modified

* `withProperties(models.SchemaUpdateProperties)` was added

#### `WorkloadOrchestrationManager` was modified

* `configTemplateSchemas()` was added
* `solutionSchemas()` was added
* `hierarchyConfigurationMetadataVersions()` was added
* `solutionDeployments()` was added
* `configTemplateMetadatas()` was added
* `hierarchyConfigurationMetadatas()` was added
* `solutionMetadatas()` was added
* `solutionMetadataVersions()` was added

#### `models.Diagnostic$Update` was modified

* `withProperties(models.DiagnosticUpdateProperties)` was added

#### `models.Solution$Update` was modified

* `withProperties(models.SolutionUpdateProperties)` was added

#### `models.Target` was modified

* `unstageSolutionVersion(models.SolutionVersionParameter)` was added
* `unstageSolutionVersion(models.SolutionVersionParameter,com.azure.core.util.Context)` was added

#### `models.SchemaReferenceProperties` was modified

* `withSchemaId(java.lang.String)` was added

#### `models.Targets` was modified

* `unstageSolutionVersion(java.lang.String,java.lang.String,models.SolutionVersionParameter,com.azure.core.util.Context)` was added
* `unstageSolutionVersion(java.lang.String,java.lang.String,models.SolutionVersionParameter)` was added

#### `models.SolutionTemplateVersion` was modified

* `bulkReviewSolution(models.BulkReviewSolutionParameter)` was added
* `bulkReviewSolution(models.BulkReviewSolutionParameter,com.azure.core.util.Context)` was added
* `refresh(com.azure.core.util.Context)` was added
* `refresh()` was added
* `bulkDeploySolution(models.BulkDeploySolutionParameter)` was added
* `bulkPublishSolution(models.BulkPublishSolutionParameter)` was added
* `resourceGroupName()` was added
* `update()` was added
* `bulkDeploySolution(models.BulkDeploySolutionParameter,com.azure.core.util.Context)` was added
* `bulkPublishSolution(models.BulkPublishSolutionParameter,com.azure.core.util.Context)` was added

#### `models.SolutionProperties` was modified

* `displayName()` was added

#### `models.SolutionTemplate$Update` was modified

* `withProperties(models.SolutionTemplateUpdateProperties)` was added

#### `models.ConfigTemplate$Update` was modified

* `withProperties(models.ConfigTemplateUpdateProperties)` was added

#### `models.JobProperties` was modified

* `additionalData()` was added

#### `models.Target$Update` was modified

* `withProperties(models.TargetUpdateProperties)` was added

#### `models.BulkPublishSolutionParameter` was modified

* `solutionConfiguration()` was added
* `withSolutionConfiguration(java.lang.String)` was added

#### `models.ConfigTemplateVersionProperties` was modified

* `withConfigurations(com.azure.core.util.BinaryData)` was added

#### `models.SchemaVersionProperties` was modified

* `withValue(com.azure.core.util.BinaryData)` was added

#### `models.State` was modified

* `NOT_APPLICABLE` was added

#### `models.ConfigTemplateProperties` was modified

* `uniqueIdentifier()` was added

#### `models.BulkPublishTargetDetails` was modified

* `withSolutionConfiguration(java.lang.String)` was added
* `solutionConfiguration()` was added
* `withSolutionVersionId(java.lang.String)` was added
* `solutionVersionId()` was added
* `withSolutionDependencies(java.util.List)` was added
* `solutionDependencies()` was added

#### `models.ConfigTemplateVersion` was modified

* `resourceGroupName()` was added
* `refresh()` was added
* `update()` was added
* `refresh(com.azure.core.util.Context)` was added

#### `models.DynamicSchemaProperties` was modified

* `displayName()` was added

#### `models.ConfigTemplate` was modified

* `linkToHierarchies(models.HierarchySelector,com.azure.core.util.Context)` was added
* `unLinkFromHierarchies(models.HierarchySelector,com.azure.core.util.Context)` was added
* `linkToHierarchies(models.HierarchySelector)` was added
* `unLinkFromHierarchies(models.HierarchySelector)` was added

#### `models.SolutionTemplateVersions` was modified

* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `getById(java.lang.String)` was added
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `bulkReviewSolution(java.lang.String,java.lang.String,java.lang.String,models.BulkReviewSolutionParameter,com.azure.core.util.Context)` was added
* `deleteById(java.lang.String)` was added
* `delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `bulkReviewSolution(java.lang.String,java.lang.String,java.lang.String,models.BulkReviewSolutionParameter)` was added
* `delete(java.lang.String,java.lang.String,java.lang.String)` was added
* `define(java.lang.String)` was added

#### `models.ContextModel$Update` was modified

* `withProperties(models.ContextUpdateProperties)` was added

#### `models.SolutionTemplateProperties` was modified

* `uniqueIdentifier()` was added

#### `models.JobType` was modified

* `PUBLISH` was added
* `UNINSTALL` was added

#### `models.ContextProperties` was modified

* `uniqueIdentifier()` was added

#### `models.ConfigTemplateVersions` was modified

* `deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `deleteById(java.lang.String)` was added
* `getById(java.lang.String)` was added
* `define(java.lang.String)` was added
* `delete(java.lang.String,java.lang.String,java.lang.String)` was added
* `delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.SolutionTemplateVersionProperties` was modified

* `withConfigurations(com.azure.core.util.BinaryData)` was added
* `internalState()` was added

## 1.0.0-beta.1 (2025-08-22)

- Azure Resource Manager Workload Orchestration client library for Java. This package contains Microsoft Azure SDK for Workload Orchestration Management SDK. Microsoft.Edge Resource Provider management API. Package api-version 2025-06-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
### Features Added

- Initial release for the azure-resourcemanager-workloadorchestration Java SDK.
