# Release History

## 1.0.0-beta.3 (2026-08-28)

- Azure Resource Manager Compute Recommender client library for Java. This package contains Microsoft Azure SDK for Compute Recommender Management SDK. The Compute Recommender Resource Provider Client. Package api-version 2026-09-05-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.SkuMixPlacementItem` was modified

* `capacityMax()` was removed

#### `models.SkuMixPlacementDeploymentChoice` was modified

* `id()` was removed

### Features Added

* `models.SkuMixPlacementCapacityLimit` was added

* `models.SkuMixPlacementCapacityLimitReason` was added

#### `models.SkuMixPlacementResponse` was modified

* `id()` was added
* `capacityLimits()` was added

## 1.0.0-beta.2 (2026-08-03)

- Azure Resource Manager Compute Recommender client library for Java. This package contains Microsoft Azure SDK for Compute Recommender Management SDK. The Compute Recommender Resource Provider Client. Package api-version 2026-05-05-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.SkuMixPlacementBase` was added

* `models.SkuMixPlacementItem` was added

* `models.SkuMixPlacementResponse` was added

* `models.SkuMixPlacementProperties` was added

* `models.SkuMixPlacementPartialFulfillmentReason` was added

* `models.SkuMixPlacementCapacityProfile` was added

* `models.SkuMixPlacementPriority` was added

* `models.SkuMixPlacementRequest` was added

* `models.SkuMixPlacementInstanceDescription` was added

* `models.SkuMixPlacementSpotPriorityProfile` was added

* `models.SkuMixPlacementCapacityType` was added

* `models.SkuMixPlacementZoneAllocationPolicy` was added

* `models.SkuMixPlacementAllocationStrategy` was added

* `models.SkuMixPlacementScores` was added

* `models.SkuMixPlacementDeploymentChoice` was added

* `models.SkuMixPlacementVMSize` was added

* `models.SkuMixPlacementZonePreference` was added

* `models.SkuMixPlacementOSType` was added

* `models.SkuMixPlacementZonalDistributionStrategy` was added

#### `ComputeRecommenderManager` was modified

* `skuMixPlacementScores()` was added

## 1.0.0-beta.1 (2025-09-24)

- Azure Resource Manager Compute Recommender client library for Java. This package contains Microsoft Azure SDK for Compute Recommender Management SDK. The Compute Recommender Resource Provider Client. Package api-version 2025-06-05. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
### Features Added

- Initial release for the azure-resourcemanager-compute-recommender Java SDK.
