# Release History

## 1.1.0-beta.2 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.1.0-beta.1 (2025-07-29)

- Azure Resource Manager PolicyInsights client library for Java. This package contains Microsoft Azure SDK for PolicyInsights Management SDK. Query component policy states at varying resource scopes for Resource Provider mode policies. Package tag package-2024-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.PolicyEvaluationResult` was modified

* `models.PolicyEvaluationDetails evaluationDetails()` -> `models.CheckRestrictionEvaluationDetails evaluationDetails()`

### Features Added

* `models.CheckRestrictionEvaluationDetails` was added

* `models.PolicyEffectDetails` was added

#### `models.CheckRestrictionsRequest` was modified

* `withIncludeAuditEffect(java.lang.Boolean)` was added
* `includeAuditEffect()` was added

#### `models.PolicyEvaluationResult` was modified

* `effectDetails()` was added

#### `models.FieldRestriction` was modified

* `reason()` was added
* `policyEffect()` was added

#### `models.RemediationFilters` was modified

* `resourceIds()` was added
* `withResourceIds(java.util.List)` was added

## 1.0.0 (2024-12-25)

- Azure Resource Manager PolicyInsights client library for Java. This package contains Microsoft Azure SDK for PolicyInsights Management SDK. Query component policy states at varying resource scopes for Resource Provider mode policies. Package tag package-2022-09. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Other Changes

- Release for Azure Resource Manager PolicyInsights client library for Java.

## 1.0.0-beta.4 (2024-10-14)

- Azure Resource Manager PolicyInsights client library for Java. This package contains Microsoft Azure SDK for PolicyInsights Management SDK. Query component policy states at varying resource scopes for Resource Provider mode policies. Package tag package-2022-09. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.ComponentPolicyStatesQueryResults` was added

* `models.ComponentPolicyStatesResource` was added

* `models.ComponentPolicyState` was added

* `models.ComponentPolicyEvaluationDetails` was added

* `models.ComponentExpressionEvaluationDetails` was added

* `models.ComponentPolicyStates` was added

#### `models.CheckRestrictionsResourceDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PolicyDefinitionSummary` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ComplianceDetail` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ExpressionEvaluationDetails` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CheckManagementGroupRestrictionsRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Summary` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `PolicyInsightsManager` was modified

* `componentPolicyStates()` was added

#### `models.ComponentEventDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PendingField` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CheckRestrictionsRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.RemediationPropertiesFailureThreshold` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.RemediationDeploymentsListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.IfNotExistsEvaluationDetails` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PolicyReference` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PolicyEvaluationDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PolicyAssignmentSummary` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RemediationDeploymentSummary` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PolicyGroupSummary` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PolicyStatesQueryResults` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AttestationListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.FieldRestrictions` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PolicyTrackedResourcesQueryResults` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AttestationEvidence` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PolicyEvaluationResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PolicyDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TrackedResourceModificationDetails` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ComponentStateDetails` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CheckRestrictionsResultContentEvaluationResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.FieldRestriction` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PolicyEventsQueryResults` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SummaryResults` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RemediationListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PolicyMetadataCollection` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RemediationFilters` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Operation` was modified

* `isDataAction()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withIsDataAction(java.lang.Boolean)` was added
* `toJson(com.azure.json.JsonWriter)` was added

## 1.0.0-beta.3 (2022-10-14)

- Azure Resource Manager PolicyInsights client library for Java. This package contains Microsoft Azure SDK for PolicyInsights Management SDK.  Package tag package-2022-09. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Remediations` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.PolicyStates` was modified

* `summarizeForResource(java.lang.String)` was removed
* `summarizeForResourceGroupWithResponse(java.lang.String,java.lang.String,java.lang.Integer,java.time.OffsetDateTime,java.time.OffsetDateTime,java.lang.String,com.azure.core.util.Context)` was removed
* `summarizeForResourceWithResponse(java.lang.String,java.lang.Integer,java.time.OffsetDateTime,java.time.OffsetDateTime,java.lang.String,com.azure.core.util.Context)` was removed
* `summarizeForResourceGroupLevelPolicyAssignmentWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,java.time.OffsetDateTime,java.time.OffsetDateTime,java.lang.String,com.azure.core.util.Context)` was removed
* `summarizeForPolicySetDefinitionWithResponse(java.lang.String,java.lang.String,java.lang.Integer,java.time.OffsetDateTime,java.time.OffsetDateTime,java.lang.String,com.azure.core.util.Context)` was removed
* `summarizeForResourceGroup(java.lang.String,java.lang.String)` was removed
* `summarizeForSubscriptionWithResponse(java.lang.String,java.lang.Integer,java.time.OffsetDateTime,java.time.OffsetDateTime,java.lang.String,com.azure.core.util.Context)` was removed
* `summarizeForManagementGroup(java.lang.String)` was removed
* `summarizeForResourceGroupLevelPolicyAssignment(java.lang.String,java.lang.String,java.lang.String)` was removed
* `summarizeForPolicyDefinition(java.lang.String,java.lang.String)` was removed
* `summarizeForPolicyDefinitionWithResponse(java.lang.String,java.lang.String,java.lang.Integer,java.time.OffsetDateTime,java.time.OffsetDateTime,java.lang.String,com.azure.core.util.Context)` was removed
* `summarizeForSubscription(java.lang.String)` was removed
* `summarizeForSubscriptionLevelPolicyAssignment(java.lang.String,java.lang.String)` was removed
* `summarizeForPolicySetDefinition(java.lang.String,java.lang.String)` was removed
* `summarizeForSubscriptionLevelPolicyAssignmentWithResponse(java.lang.String,java.lang.String,java.lang.Integer,java.time.OffsetDateTime,java.time.OffsetDateTime,java.lang.String,com.azure.core.util.Context)` was removed
* `summarizeForManagementGroupWithResponse(java.lang.String,java.lang.Integer,java.time.OffsetDateTime,java.time.OffsetDateTime,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.PolicyTrackedResources` was modified

* `listQueryResultsForManagementGroup(java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was removed
* `listQueryResultsForSubscription()` was removed
* `listQueryResultsForResourceGroup(java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was removed
* `listQueryResultsForResource(java.lang.String)` was removed
* `listQueryResultsForResourceGroup(java.lang.String)` was removed
* `listQueryResultsForManagementGroup(java.lang.String)` was removed
* `listQueryResultsForResource(java.lang.String,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was removed
* `listQueryResultsForSubscription(java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.PolicyEvents` was modified

* `listQueryResultsForPolicyDefinition(java.lang.String,java.lang.String)` was removed
* `listQueryResultsForResource(java.lang.String)` was removed
* `listQueryResultsForSubscriptionLevelPolicyAssignment(java.lang.String,java.lang.String)` was removed
* `listQueryResultsForManagementGroup(java.lang.String)` was removed
* `listQueryResultsForSubscription(java.lang.String,java.lang.Integer,java.lang.String,java.lang.String,java.time.OffsetDateTime,java.time.OffsetDateTime,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `listQueryResultsForManagementGroup(java.lang.String,java.lang.Integer,java.lang.String,java.lang.String,java.time.OffsetDateTime,java.time.OffsetDateTime,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `listQueryResultsForPolicySetDefinition(java.lang.String,java.lang.String)` was removed
* `listQueryResultsForResourceGroup(java.lang.String,java.lang.String)` was removed
* `listQueryResultsForSubscriptionLevelPolicyAssignment(java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,java.lang.String,java.time.OffsetDateTime,java.time.OffsetDateTime,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `listQueryResultsForResourceGroupLevelPolicyAssignment(java.lang.String,java.lang.String,java.lang.String)` was removed
* `listQueryResultsForSubscription(java.lang.String)` was removed
* `listQueryResultsForResource(java.lang.String,java.lang.Integer,java.lang.String,java.lang.String,java.time.OffsetDateTime,java.time.OffsetDateTime,java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `listQueryResultsForResourceGroup(java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,java.lang.String,java.time.OffsetDateTime,java.time.OffsetDateTime,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `listQueryResultsForResourceGroupLevelPolicyAssignment(java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,java.lang.String,java.time.OffsetDateTime,java.time.OffsetDateTime,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `listQueryResultsForPolicyDefinition(java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,java.lang.String,java.time.OffsetDateTime,java.time.OffsetDateTime,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `listQueryResultsForPolicySetDefinition(java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,java.lang.String,java.time.OffsetDateTime,java.time.OffsetDateTime,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Attestations` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

* `models.PolicyTrackedResourcesResourceType` was added

* `models.CheckManagementGroupRestrictionsRequest` was added

* `models.PolicyStatesSummaryResourceType` was added

* `models.PolicyEventsResourceType` was added

#### `PolicyInsightsManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.Attestation$Definition` was modified

* `withAssessmentDate(java.time.OffsetDateTime)` was added
* `withMetadata(java.lang.Object)` was added

#### `models.Remediation` was modified

* `resourceGroupName()` was added

#### `PolicyInsightsManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.Remediations` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.PolicyRestrictions` was modified

* `checkAtManagementGroupScope(java.lang.String,models.CheckManagementGroupRestrictionsRequest)` was added
* `checkAtManagementGroupScopeWithResponse(java.lang.String,models.CheckManagementGroupRestrictionsRequest,com.azure.core.util.Context)` was added

#### `models.PolicyStates` was modified

* `summarizeForResourceGroup(models.PolicyStatesSummaryResourceType,java.lang.String,java.lang.String)` was added
* `summarizeForResourceGroupLevelPolicyAssignmentWithResponse(models.PolicyStatesSummaryResourceType,java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,java.time.OffsetDateTime,java.time.OffsetDateTime,java.lang.String,com.azure.core.util.Context)` was added
* `summarizeForManagementGroupWithResponse(models.PolicyStatesSummaryResourceType,java.lang.String,java.lang.Integer,java.time.OffsetDateTime,java.time.OffsetDateTime,java.lang.String,com.azure.core.util.Context)` was added
* `summarizeForSubscription(models.PolicyStatesSummaryResourceType,java.lang.String)` was added
* `summarizeForResourceGroupWithResponse(models.PolicyStatesSummaryResourceType,java.lang.String,java.lang.String,java.lang.Integer,java.time.OffsetDateTime,java.time.OffsetDateTime,java.lang.String,com.azure.core.util.Context)` was added
* `summarizeForSubscriptionWithResponse(models.PolicyStatesSummaryResourceType,java.lang.String,java.lang.Integer,java.time.OffsetDateTime,java.time.OffsetDateTime,java.lang.String,com.azure.core.util.Context)` was added
* `summarizeForPolicySetDefinitionWithResponse(models.PolicyStatesSummaryResourceType,java.lang.String,java.lang.String,java.lang.Integer,java.time.OffsetDateTime,java.time.OffsetDateTime,java.lang.String,com.azure.core.util.Context)` was added
* `summarizeForPolicyDefinition(models.PolicyStatesSummaryResourceType,java.lang.String,java.lang.String)` was added
* `summarizeForPolicySetDefinition(models.PolicyStatesSummaryResourceType,java.lang.String,java.lang.String)` was added
* `summarizeForPolicyDefinitionWithResponse(models.PolicyStatesSummaryResourceType,java.lang.String,java.lang.String,java.lang.Integer,java.time.OffsetDateTime,java.time.OffsetDateTime,java.lang.String,com.azure.core.util.Context)` was added
* `summarizeForSubscriptionLevelPolicyAssignmentWithResponse(models.PolicyStatesSummaryResourceType,java.lang.String,java.lang.String,java.lang.Integer,java.time.OffsetDateTime,java.time.OffsetDateTime,java.lang.String,com.azure.core.util.Context)` was added
* `summarizeForResourceWithResponse(models.PolicyStatesSummaryResourceType,java.lang.String,java.lang.Integer,java.time.OffsetDateTime,java.time.OffsetDateTime,java.lang.String,com.azure.core.util.Context)` was added
* `summarizeForResource(models.PolicyStatesSummaryResourceType,java.lang.String)` was added
* `summarizeForResourceGroupLevelPolicyAssignment(models.PolicyStatesSummaryResourceType,java.lang.String,java.lang.String,java.lang.String)` was added
* `summarizeForManagementGroup(models.PolicyStatesSummaryResourceType,java.lang.String)` was added
* `summarizeForSubscriptionLevelPolicyAssignment(models.PolicyStatesSummaryResourceType,java.lang.String,java.lang.String)` was added

#### `models.Attestation` was modified

* `resourceGroupName()` was added
* `metadata()` was added
* `assessmentDate()` was added

#### `models.Attestation$Update` was modified

* `withAssessmentDate(java.time.OffsetDateTime)` was added
* `withMetadata(java.lang.Object)` was added

#### `models.PolicyTrackedResources` was modified

* `listQueryResultsForSubscription(models.PolicyTrackedResourcesResourceType)` was added
* `listQueryResultsForManagementGroup(java.lang.String,models.PolicyTrackedResourcesResourceType,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `listQueryResultsForResource(java.lang.String,models.PolicyTrackedResourcesResourceType,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `listQueryResultsForResourceGroup(java.lang.String,models.PolicyTrackedResourcesResourceType)` was added
* `listQueryResultsForManagementGroup(java.lang.String,models.PolicyTrackedResourcesResourceType)` was added
* `listQueryResultsForResource(java.lang.String,models.PolicyTrackedResourcesResourceType)` was added
* `listQueryResultsForSubscription(models.PolicyTrackedResourcesResourceType,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added
* `listQueryResultsForResourceGroup(java.lang.String,models.PolicyTrackedResourcesResourceType,java.lang.Integer,java.lang.String,com.azure.core.util.Context)` was added

#### `models.PolicyEvents` was modified

* `listQueryResultsForResourceGroupLevelPolicyAssignment(models.PolicyEventsResourceType,java.lang.String,java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,java.lang.String,java.time.OffsetDateTime,java.time.OffsetDateTime,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listQueryResultsForResource(models.PolicyEventsResourceType,java.lang.String)` was added
* `listQueryResultsForResourceGroup(models.PolicyEventsResourceType,java.lang.String,java.lang.String)` was added
* `listQueryResultsForManagementGroup(models.PolicyEventsResourceType,java.lang.String)` was added
* `listQueryResultsForResource(models.PolicyEventsResourceType,java.lang.String,java.lang.Integer,java.lang.String,java.lang.String,java.time.OffsetDateTime,java.time.OffsetDateTime,java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listQueryResultsForPolicyDefinition(models.PolicyEventsResourceType,java.lang.String,java.lang.String)` was added
* `listQueryResultsForSubscription(models.PolicyEventsResourceType,java.lang.String)` was added
* `listQueryResultsForSubscriptionLevelPolicyAssignment(models.PolicyEventsResourceType,java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,java.lang.String,java.time.OffsetDateTime,java.time.OffsetDateTime,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listQueryResultsForManagementGroup(models.PolicyEventsResourceType,java.lang.String,java.lang.Integer,java.lang.String,java.lang.String,java.time.OffsetDateTime,java.time.OffsetDateTime,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listQueryResultsForPolicySetDefinition(models.PolicyEventsResourceType,java.lang.String,java.lang.String)` was added
* `listQueryResultsForPolicyDefinition(models.PolicyEventsResourceType,java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,java.lang.String,java.time.OffsetDateTime,java.time.OffsetDateTime,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listQueryResultsForResourceGroupLevelPolicyAssignment(models.PolicyEventsResourceType,java.lang.String,java.lang.String,java.lang.String)` was added
* `listQueryResultsForSubscriptionLevelPolicyAssignment(models.PolicyEventsResourceType,java.lang.String,java.lang.String)` was added
* `listQueryResultsForSubscription(models.PolicyEventsResourceType,java.lang.String,java.lang.Integer,java.lang.String,java.lang.String,java.time.OffsetDateTime,java.time.OffsetDateTime,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listQueryResultsForResourceGroup(models.PolicyEventsResourceType,java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,java.lang.String,java.time.OffsetDateTime,java.time.OffsetDateTime,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listQueryResultsForPolicySetDefinition(models.PolicyEventsResourceType,java.lang.String,java.lang.String,java.lang.Integer,java.lang.String,java.lang.String,java.time.OffsetDateTime,java.time.OffsetDateTime,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Attestations` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

## 1.0.0-beta.2 (2021-12-06)

- Azure Resource Manager PolicyInsights client library for Java. This package contains Microsoft Azure SDK for PolicyInsights Management SDK.  Package tag package-2021-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.PolicyMetadataSlimProperties` was removed

* `models.PolicyMetadataProperties` was removed

### Features Added

* `models.RemediationPropertiesFailureThreshold` was added

#### `PolicyInsightsManager$Configurable` was modified

* `withScope(java.lang.String)` was added

#### `models.Remediation` was modified

* `statusMessage()` was added
* `failureThreshold()` was added
* `correlationId()` was added
* `resourceCount()` was added
* `listDeploymentsAtResourceGroup(java.lang.Integer,com.azure.core.util.Context)` was added
* `listDeploymentsAtResourceGroup()` was added
* `cancelAtResourceGroup()` was added
* `systemData()` was added
* `parallelDeployments()` was added
* `cancelAtResourceGroupWithResponse(com.azure.core.util.Context)` was added

#### `models.Remediation$Definition` was modified

* `withParallelDeployments(java.lang.Integer)` was added
* `withFailureThreshold(models.RemediationPropertiesFailureThreshold)` was added
* `withResourceCount(java.lang.Integer)` was added

#### `models.Remediation$Update` was modified

* `withParallelDeployments(java.lang.Integer)` was added
* `withFailureThreshold(models.RemediationPropertiesFailureThreshold)` was added
* `withResourceCount(java.lang.Integer)` was added

## 1.0.0-beta.1 (2021-04-22)

- Azure Resource Manager PolicyInsights client library for Java. This package contains Microsoft Azure SDK for PolicyInsights Management SDK.  Package tag package-2021-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
