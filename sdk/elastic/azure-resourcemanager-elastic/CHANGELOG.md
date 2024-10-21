# Release History

## 1.0.0 (2024-10-21)

- Azure Resource Manager elastic client library for Java. This package contains Microsoft Azure SDK for elastic Management SDK.  Package tag package-2024-03-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.ElasticOrganizationToAzureSubscriptionMappingResponseProperties` was added

* `models.ConnectedPartnerResourceProperties` was added

* `models.OpenAIIntegrationRPModel` was added

* `models.OpenAIIntegrationStatusResponse` was added

* `models.ElasticOrganizationToAzureSubscriptionMappingResponse` was added

* `models.ConnectedPartnerResourcesListFormat` was added

* `models.OpenAIIntegrationRPModel$UpdateStages` was added

* `models.BillingInfoResponse` was added

* `models.ConnectedPartnerResourcesListResponse` was added

* `models.ConnectedPartnerResources` was added

* `models.OpenAIIntegrationProperties` was added

* `models.OpenAIIntegrationRPModel$Update` was added

* `models.PartnerBillingEntity` was added

* `models.OpenAIIntegrationRPModel$DefinitionStages` was added

* `models.OpenAIs` was added

* `models.BillingInfoes` was added

* `models.OpenAIIntegrationRPModel$Definition` was added

* `models.PlanDetails` was added

* `models.OpenAIIntegrationStatusResponseProperties` was added

* `models.OpenAIIntegrationRPModelListResponse` was added

#### `models.IdentityProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ElasticProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ElasticMonitorResourceUpdateParameters` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ExternalUserInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MarketplaceSaaSInfoMarketplaceSubscription` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DeploymentInfoResponse` was modified

* `elasticsearchEndPoint()` was added

#### `models.CompanyInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UserEmailId` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ElasticCloudUser` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MonitorProperties` was modified

* `sourceCampaignId()` was added
* `withSourceCampaignId(java.lang.String)` was added
* `subscriptionState()` was added
* `withSubscriptionState(java.lang.String)` was added
* `withPlanDetails(models.PlanDetails)` was added
* `sourceCampaignName()` was added
* `saaSAzureSubscriptionStatus()` was added
* `withSourceCampaignName(java.lang.String)` was added
* `planDetails()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withSaaSAzureSubscriptionStatus(java.lang.String)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ElasticMonitorUpgrade` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ElasticVersionsListResponse` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `ElasticManager` was modified

* `openAIs()` was added
* `billingInfoes()` was added
* `connectedPartnerResources()` was added

#### `models.OperationListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MonitoringTagRulesProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MarketplaceSaaSInfo` was modified

* `subscribed()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `marketplaceStatus()` was added
* `withSubscribed(java.lang.Boolean)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `billedAzureSubscriptionId()` was added
* `withBilledAzureSubscriptionId(java.lang.String)` was added
* `withMarketplaceStatus(java.lang.String)` was added

#### `models.ElasticMonitorResourceListResponse` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UserInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ElasticTrafficFilterRule` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ElasticTrafficFilter` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Organizations` was modified

* `getElasticToAzureSubscriptionMapping()` was added
* `getElasticToAzureSubscriptionMappingWithResponse(com.azure.core.util.Context)` was added

#### `models.MonitoringTagRulesListResponse` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ElasticVersionListProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.LogRules` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.VMHostListResponse` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ResourceSku` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FilteringTag` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MonitoredResourceListResponse` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ElasticCloudDeployment` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationDisplay` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.VMCollectionUpdate` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.UserApiKeyResponseProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

## 1.0.0-beta.4 (2023-05-23)

- Azure Resource Manager elastic client library for Java. This package contains Microsoft Azure SDK for elastic Management SDK.  Package tag package-2023-02-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.ElasticVersionsListResponse` was modified

* `java.util.List value()` -> `java.util.List value()`
* `innerModel()` was removed
* `java.lang.String nextLink()` -> `java.lang.String nextLink()`

#### `models.UserApiKeyResponse` was modified

* `apiKey()` was removed

#### `models.ElasticVersions` was modified

* `models.ElasticVersionsListResponse list(java.lang.String)` -> `com.azure.core.http.rest.PagedIterable list(java.lang.String)`
* `listWithResponse(java.lang.String,com.azure.core.util.Context)` was removed

### Features Added

* `models.ElasticVersionListProperties` was added

* `models.ElasticVersionListFormat` was added

* `models.UserApiKeyResponseProperties` was added

#### `models.ElasticVersionsListResponse` was modified

* `withValue(java.util.List)` was added
* `validate()` was added
* `withNextLink(java.lang.String)` was added

#### `models.UserApiKeyResponse` was modified

* `properties()` was added

#### `models.ElasticVersions` was modified

* `list(java.lang.String,com.azure.core.util.Context)` was added

## 1.0.0-beta.3 (2023-04-18)

- Azure Resource Manager elastic client library for Java. This package contains Microsoft Azure SDK for elastic Management SDK.  Package tag package-2023-02-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.MarketplaceSaaSInfoMarketplaceSubscription` was added

* `models.Organizations` was added

* `models.UserEmailId` was added

* `models.ElasticVersionsListResponse` was added

* `models.UserApiKeyResponse` was added

* `models.MarketplaceSaaSInfo` was added

* `models.ElasticVersions` was added

#### `models.DeploymentInfoResponse` was modified

* `deploymentUrl()` was added
* `marketplaceSaasInfo()` was added

#### `models.MonitorProperties` was modified

* `generateApiKey()` was added
* `withGenerateApiKey(java.lang.Boolean)` was added

#### `ElasticManager` was modified

* `elasticVersions()` was added
* `organizations()` was added

## 1.0.0-beta.2 (2022-11-23)

- Azure Resource Manager elastic client library for Java. This package contains Microsoft Azure SDK for elastic Management SDK.  Package tag package-2022-07-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.CreateAndAssociatePLFilters` was added

* `models.MonitorOperations` was added

* `models.ElasticTrafficFilterRule` was added

* `models.ListAssociatedTrafficFilters` was added

* `models.ExternalUserInfo` was added

* `models.ExternalUserCreationResponse` was added

* `models.ElasticTrafficFilter` was added

* `models.TrafficFilters` was added

* `models.Type` was added

* `models.UpgradableVersionsList` was added

* `models.ElasticMonitorUpgrade` was added

* `models.ElasticTrafficFilterResponse` was added

* `models.AllTrafficFilters` was added

* `models.DetachTrafficFilters` was added

* `models.UpgradableVersions` was added

* `models.CreateAndAssociateIpFilters` was added

* `models.AssociateTrafficFilters` was added

* `models.DetachAndDeleteTrafficFilters` was added

* `models.ExternalUsers` was added

#### `models.ElasticMonitorResource` was modified

* `resourceGroupName()` was added

#### `models.MonitoringTagRules` was modified

* `resourceGroupName()` was added

#### `models.MonitorProperties` was modified

* `withVersion(java.lang.String)` was added
* `version()` was added

#### `ElasticManager` was modified

* `associateTrafficFilters()` was added
* `externalUsers()` was added
* `detachTrafficFilters()` was added
* `upgradableVersions()` was added
* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added
* `createAndAssociatePLFilters()` was added
* `allTrafficFilters()` was added
* `createAndAssociateIpFilters()` was added
* `monitorOperations()` was added
* `listAssociatedTrafficFilters()` was added
* `trafficFilters()` was added
* `detachAndDeleteTrafficFilters()` was added

#### `ElasticManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

## 1.0.0-beta.1 (2021-07-08)

- Azure Resource Manager elastic client library for Java. This package contains Microsoft Azure SDK for elastic Management SDK.  Package tag package-2020-07-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

