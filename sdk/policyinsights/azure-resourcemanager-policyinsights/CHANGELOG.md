# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
