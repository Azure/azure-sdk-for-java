# Release History

## 1.1.0-beta.1 (2026-10-14)

- Azure Resource Manager Carbon Optimization client library for Java. This package contains Microsoft Azure SDK for Carbon Optimization Management SDK. Carbon Report Resource Provider query API. Package api-version 2026-05-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.CarbonEmissionOverallSummaryData` was modified

* `validate()` was removed

#### `models.OperationDisplay` was modified

* `validate()` was removed

#### `models.ResourceCarbonEmissionTopItemMonthlySummaryData` was modified

* `validate()` was removed

#### `models.TopItemsMonthlySummaryReportQueryFilter` was modified

* `validate()` was removed

#### `models.SubscriptionAccessDecision` was modified

* `validate()` was removed

#### `models.CarbonServices` was modified

* `queryCarbonEmissionReportsWithResponse(models.QueryFilter,com.azure.core.util.Context)` was removed
* `models.CarbonEmissionDataListResult queryCarbonEmissionReports(models.QueryFilter)` -> `com.azure.core.http.rest.PagedIterable queryCarbonEmissionReports(models.QueryFilter)`

#### `models.DateRange` was modified

* `validate()` was removed

#### `models.ResourceGroupCarbonEmissionTopItemMonthlySummaryData` was modified

* `validate()` was removed

#### `models.TopItemsSummaryReportQueryFilter` was modified

* `validate()` was removed

#### `models.OverallSummaryReportQueryFilter` was modified

* `validate()` was removed

#### `models.CarbonEmissionItemDetailData` was modified

* `validate()` was removed

#### `models.ResourceCarbonEmissionItemDetailData` was modified

* `validate()` was removed

#### `models.ResourceGroupCarbonEmissionItemDetailData` was modified

* `validate()` was removed

#### `models.CarbonEmissionMonthlySummaryData` was modified

* `validate()` was removed

#### `models.ResourceCarbonEmissionTopItemsSummaryData` was modified

* `validate()` was removed

#### `models.CarbonEmissionTopItemsSummaryData` was modified

* `validate()` was removed

#### `models.QueryFilter` was modified

* `validate()` was removed

#### `models.CarbonEmissionData` was modified

* `CarbonEmissionData()` was removed
* `java.lang.Double monthlyEmissionsChangeValue()` -> `java.lang.Double monthlyEmissionsChangeValue()`
* `models.ResponseDataTypeEnum dataType()` -> `models.ResponseDataTypeEnum dataType()`
* `fromJson(com.azure.json.JsonReader)` was removed
* `double latestMonthEmissions()` -> `double latestMonthEmissions()`
* `double previousMonthEmissions()` -> `double previousMonthEmissions()`
* `validate()` was removed
* `java.lang.Double monthOverMonthEmissionsChangeRatio()` -> `java.lang.Double monthOverMonthEmissionsChangeRatio()`
* `toJson(com.azure.json.JsonWriter)` was removed

#### `models.CarbonEmissionTopItemMonthlySummaryData` was modified

* `validate()` was removed

#### `models.ItemDetailsQueryFilter` was modified

* `validate()` was removed

#### `models.MonthlySummaryReportQueryFilter` was modified

* `validate()` was removed

#### `models.ResourceGroupCarbonEmissionTopItemsSummaryData` was modified

* `validate()` was removed

### Features Added

* `models.BillingAccountTopItemsSummaryReportQueryFilter` was added

* `models.BillingAccountOrderByColumnEnum` was added

* `models.BillingAccountCategoryTypeEnum` was added

* `models.BillingAccountItemDetailsReportQueryFilter` was added

* `models.BillingAccountMonthlySummaryReportQueryFilter` was added

* `models.BillingAccountTopItemsMonthlySummaryReportQueryFilter` was added

* `models.BillingAccountOverallSummaryReportQueryFilter` was added

* `models.BillingAccountQueryFilter` was added

#### `models.CarbonEmissionOverallSummaryData` was modified

* `monthlyEmissionsChangeValue()` was added
* `previousMonthEmissions()` was added
* `monthOverMonthEmissionsChangeRatio()` was added
* `latestMonthEmissions()` was added

#### `models.ResourceCarbonEmissionTopItemMonthlySummaryData` was modified

* `monthlyEmissionsChangeValue()` was added
* `monthOverMonthEmissionsChangeRatio()` was added
* `previousMonthEmissions()` was added
* `latestMonthEmissions()` was added

#### `models.CarbonServices` was modified

* `queryCarbonEmissionReportsByBillingAccountWithResponse(java.lang.String,models.BillingAccountQueryFilter,com.azure.core.util.Context)` was added
* `queryCarbonEmissionReports(models.QueryFilter,com.azure.core.util.Context)` was added
* `queryCarbonEmissionReportsByBillingAccount(java.lang.String,models.BillingAccountQueryFilter)` was added

#### `models.ResourceGroupCarbonEmissionTopItemMonthlySummaryData` was modified

* `monthOverMonthEmissionsChangeRatio()` was added
* `latestMonthEmissions()` was added
* `monthlyEmissionsChangeValue()` was added
* `previousMonthEmissions()` was added

#### `models.CarbonEmissionItemDetailData` was modified

* `monthlyEmissionsChangeValue()` was added
* `previousMonthEmissions()` was added
* `monthOverMonthEmissionsChangeRatio()` was added
* `latestMonthEmissions()` was added

#### `models.ResourceCarbonEmissionItemDetailData` was modified

* `monthOverMonthEmissionsChangeRatio()` was added
* `latestMonthEmissions()` was added
* `monthlyEmissionsChangeValue()` was added
* `previousMonthEmissions()` was added

#### `models.ResourceGroupCarbonEmissionItemDetailData` was modified

* `latestMonthEmissions()` was added
* `monthlyEmissionsChangeValue()` was added
* `monthOverMonthEmissionsChangeRatio()` was added
* `previousMonthEmissions()` was added

#### `models.CarbonEmissionMonthlySummaryData` was modified

* `latestMonthEmissions()` was added
* `previousMonthEmissions()` was added
* `monthlyEmissionsChangeValue()` was added
* `monthOverMonthEmissionsChangeRatio()` was added

#### `models.ResourceCarbonEmissionTopItemsSummaryData` was modified

* `previousMonthEmissions()` was added
* `monthlyEmissionsChangeValue()` was added
* `latestMonthEmissions()` was added
* `monthOverMonthEmissionsChangeRatio()` was added

#### `models.CarbonEmissionTopItemsSummaryData` was modified

* `monthlyEmissionsChangeValue()` was added
* `previousMonthEmissions()` was added
* `monthOverMonthEmissionsChangeRatio()` was added
* `latestMonthEmissions()` was added

#### `models.CarbonEmissionData` was modified

* `innerModel()` was added

#### `models.CarbonEmissionTopItemMonthlySummaryData` was modified

* `monthlyEmissionsChangeValue()` was added
* `monthOverMonthEmissionsChangeRatio()` was added
* `latestMonthEmissions()` was added
* `previousMonthEmissions()` was added

#### `models.ResourceGroupCarbonEmissionTopItemsSummaryData` was modified

* `previousMonthEmissions()` was added
* `monthOverMonthEmissionsChangeRatio()` was added
* `monthlyEmissionsChangeValue()` was added
* `latestMonthEmissions()` was added

## 1.0.0 (2025-07-15)

- Azure Resource Manager Carbon Optimization client library for Java. This package contains Microsoft Azure SDK for Carbon Optimization Management SDK. Carbon Report Resource Provider query API. Package api-version 2025-04-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

- First stable release for the azure-resourcemanager-carbonoptimization Java SDK.

## 1.0.0-beta.1 (2025-04-29)

- Azure Resource Manager Carbon Optimization client library for Java. This package contains Microsoft Azure SDK for Carbon Optimization Management SDK. Carbon Report Resource Provider query API. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
### Features Added

- Initial release for the azure-resourcemanager-carbonoptimization Java SDK.

