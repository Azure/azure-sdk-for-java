# Release History

## 1.0.0 (2026-09-04)

- Azure Resource Manager Policy client library for Java. This package contains Microsoft Azure SDK for Policy Management SDK. To manage and control access to your resources, you can define customized policies and assign them at a scope. Package api-version 2026-07-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.AssignmentScopeValidation` was removed

#### `models.PolicyEnrollment$Update` was removed

#### `models.Variable$Definition` was removed

#### `models.VariableValue$Update` was removed

#### `models.PolicyExemption$UpdateStages` was removed

#### `models.PolicyEnrollment` was removed

#### `models.ExemptionManagementMode` was removed

#### `models.PolicyExemptionUpdate` was removed

#### `models.Variable$UpdateStages` was removed

#### `models.PolicyVariableColumn` was removed

#### `models.VariableValues` was removed

#### `models.Variable$DefinitionStages` was removed

#### `models.PolicyEnrollmentUpdate` was removed

#### `models.VariableValue$DefinitionStages` was removed

#### `models.Variable$Update` was removed

#### `models.PolicyVariableValueColumnValue` was removed

#### `models.Variables` was removed

#### `models.PolicyEnrollment$Definition` was removed

#### `models.PolicyEnrollment$UpdateStages` was removed

#### `models.PolicyExemption$Definition` was removed

#### `models.PolicyExemption$DefinitionStages` was removed

#### `models.PolicyExemption$Update` was removed

#### `models.VariableValue$Definition` was removed

#### `models.VariableValue$UpdateStages` was removed

#### `models.PolicyExemption` was removed

#### `models.VariableValue` was removed

#### `models.Variable` was removed

#### `models.PolicyExemptions` was removed

#### `models.PolicyEnrollment$DefinitionStages` was removed

#### `models.ExemptionCategory` was removed

#### `models.PolicyEnrollments` was removed

#### `PolicyManager` was modified

* `variableValues()` was removed
* `policyEnrollments()` was removed
* `policyExemptions()` was removed
* `variables()` was removed

### Features Added

* `models.ComplianceState` was added

#### `models.ExternalEvaluationEndpointInvocationResult` was modified

* `complianceState()` was added

#### `models.SelectorKind` was modified

* `RESOURCE_ROLLOUT_PERCENTAGE` was added

#### `models.Selector` was modified

* `progress()` was added
* `withProgress(java.lang.Integer)` was added

#### `models.PolicyTokens` was modified

* `acquireAtResourceGroupWithResponse(java.lang.String,models.PolicyTokenRequest,com.azure.core.util.Context)` was added
* `acquireAtResourceGroup(java.lang.String,models.PolicyTokenRequest)` was added

## 1.0.0-beta.1 (2026-08-27)

- Azure Resource Manager Policy client library for Java. This package contains Microsoft Azure SDK for Policy Management SDK. To manage and control access to your resources, you can define customized policies and assign them at a scope. Package api-version 2026-01-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
### Features Added

- Initial release for the azure-resourcemanager-resources-policy Java SDK.

