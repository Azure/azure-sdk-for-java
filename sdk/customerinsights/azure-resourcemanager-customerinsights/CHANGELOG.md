# Release History

## 1.0.0-beta.3 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.2 (2023-01-13)

- Azure Resource Manager CustomerInsights client library for Java. This package contains Microsoft Azure SDK for CustomerInsights Management SDK. The Azure Customer Insights management API provides a RESTful set of web services that interact with Azure Customer Insights service to manage your resources. The API has entities that capture the relationship between an end user and the Azure Customer Insights service. Package tag package-2017-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.SalesforceTable` was removed

* `models.CrmConnectorEntities` was removed

* `models.InteractionTypeDefinition` was removed

* `models.SalesforceDiscoverSetting` was removed

* `models.ProfileTypeDefinition` was removed

#### `models.InteractionResourceFormat` was modified

* `namePropertiesDefaultDataSourceName()` was removed
* `idPropertiesDefaultDataSourceId()` was removed

#### `models.PredictionResourceFormat$Update` was modified

* `withAutoAnalyze(java.lang.Boolean)` was removed

#### `models.Profiles` was modified

* `delete(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was removed

#### `models.PredictionResourceFormat$Definition` was modified

* `withAutoAnalyze(java.lang.Boolean)` was removed

#### `models.PredictionResourceFormat` was modified

* `java.lang.Boolean autoAnalyze()` -> `boolean autoAnalyze()`

### Features Added

#### `models.Hub` was modified

* `resourceGroupName()` was added

#### `models.ConnectorMappingResourceFormat` was modified

* `resourceGroupName()` was added

#### `models.InteractionResourceFormat` was modified

* `resourceGroupName()` was added
* `idPropertiesId()` was added
* `namePropertiesName()` was added

#### `models.PredictionResourceFormat$Update` was modified

* `withAutoAnalyze(boolean)` was added

#### `CustomerInsightsManager$Configurable` was modified

* `withScope(java.lang.String)` was added
* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.KpiResourceFormat` was modified

* `resourceGroupName()` was added

#### `models.ProfileResourceFormat` was modified

* `resourceGroupName()` was added

#### `models.RelationshipResourceFormat` was modified

* `resourceGroupName()` was added

#### `CustomerInsightsManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.ViewResourceFormat` was modified

* `resourceGroupName()` was added

#### `models.ConnectorResourceFormat` was modified

* `resourceGroupName()` was added

#### `models.RoleAssignmentResourceFormat` was modified

* `resourceGroupName()` was added

#### `models.LinkResourceFormat` was modified

* `resourceGroupName()` was added

#### `models.PredictionResourceFormat$Definition` was modified

* `withAutoAnalyze(boolean)` was added

#### `models.PredictionResourceFormat` was modified

* `resourceGroupName()` was added

#### `models.RelationshipLinkResourceFormat` was modified

* `resourceGroupName()` was added

#### `models.AuthorizationPolicyResourceFormat` was modified

* `resourceGroupName()` was added

## 1.0.0-beta.1 (2021-04-14)

- Azure Resource Manager CustomerInsights client library for Java. This package contains Microsoft Azure SDK for CustomerInsights Management SDK. The Azure Customer Insights management API provides a RESTful set of web services that interact with Azure Customer Insights service to manage your resources. The API has entities that capture the relationship between an end user and the Azure Customer Insights service. Package tag package-2017-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
