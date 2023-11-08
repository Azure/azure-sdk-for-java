# Release History

## 1.0.0-beta.5 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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

