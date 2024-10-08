# Release History

## 1.0.0-beta.5 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.4 (2024-10-04)

- Azure Resource Manager Consumption client library for Java. This package contains Microsoft Azure SDK for Consumption Management SDK. Consumption management client provides access to consumption resources for Azure Enterprise Subscriptions. Package tag package-2021-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.BudgetFilter` was modified

* `withNot(models.BudgetFilterProperties)` was removed
* `not()` was removed

#### `models.LegacyChargeSummary` was modified

* `marketplaceCharges()` was removed

#### `models.ReservationRecommendationDetails` was modified

* `getWithResponse(java.lang.String,java.lang.String,models.Term,models.LookBackPeriod,java.lang.String,com.azure.core.util.Context)` was removed
* `get(java.lang.String,java.lang.String,models.Term,models.LookBackPeriod,java.lang.String)` was removed

#### `models.Budgets` was modified

* `deleteWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.LegacyReservationRecommendation` was modified

* `lookBackPeriod()` was removed
* `term()` was removed
* `recommendedQuantity()` was removed
* `totalCostWithReservedInstances()` was removed
* `instanceFlexibilityGroup()` was removed
* `netSavings()` was removed
* `costWithNoReservedInstances()` was removed
* `instanceFlexibilityRatio()` was removed
* `firstUsageDate()` was removed
* `normalizedSize()` was removed
* `resourceType()` was removed
* `recommendedQuantityNormalized()` was removed
* `skuProperties()` was removed
* `meterId()` was removed

#### `models.ModernReservationRecommendation` was modified

* `instanceFlexibilityRatio()` was removed
* `firstUsageDate()` was removed
* `netSavings()` was removed
* `meterId()` was removed
* `skuProperties()` was removed
* `lookBackPeriod()` was removed
* `recommendedQuantity()` was removed
* `instanceFlexibilityGroup()` was removed
* `term()` was removed
* `recommendedQuantityNormalized()` was removed
* `skuName()` was removed
* `normalizedSize()` was removed
* `costWithNoReservedInstances()` was removed
* `locationPropertiesLocation()` was removed
* `scope()` was removed
* `totalCostWithReservedInstances()` was removed

#### `models.CreditSummary` was modified

* `tags()` was removed

### Features Added

* `models.ModernSingleScopeReservationRecommendationProperties` was added

* `models.ModernSharedScopeReservationRecommendationProperties` was added

* `models.ModernReservationRecommendationProperties` was added

* `models.Scope` was added

* `models.LegacyReservationRecommendationProperties` was added

#### `models.ForecastSpend` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ReservationRecommendationDetailsUsageProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BudgetTimePeriod` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ModernChargeSummary` was modified

* `name()` was added
* `id()` was added
* `kind()` was added
* `subscriptionId()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `type()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MarketplacesListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ChargeSummary` was modified

* `id()` was added
* `kind()` was added
* `name()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `type()` was added

#### `models.ReservationRecommendationDetailsSavingsProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BudgetsListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BudgetFilter` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.LotsOperations` was modified

* `listByCustomer(java.lang.String,java.lang.String)` was added
* `listByCustomer(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.BalancePropertiesAdjustmentDetailsItem` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MeterDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Amount` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ReservationDetailsListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.LegacyReservationTransaction` was modified

* `reservationOrderId()` was added
* `currency()` was added
* `purchasingEnrollment()` was added
* `eventDate()` was added
* `amount()` was added
* `purchasingSubscriptionGuid()` was added
* `region()` was added
* `billingFrequency()` was added
* `accountOwnerEmail()` was added
* `description()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `id()` was added
* `name()` was added
* `currentEnrollment()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `billingMonth()` was added
* `reservationOrderName()` was added
* `quantity()` was added
* `type()` was added
* `term()` was added
* `accountName()` was added
* `costCenter()` was added
* `armSkuName()` was added
* `monetaryCommitment()` was added
* `departmentName()` was added
* `eventType()` was added
* `overage()` was added
* `tags()` was added
* `purchasingSubscriptionName()` was added

#### `models.LegacyUsageDetail` was modified

* `etag()` was added
* `benefitName()` was added
* `kind()` was added
* `id()` was added
* `name()` was added
* `type()` was added
* `tags()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `benefitId()` was added

#### `models.PriceSheetProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Events` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ResourceAttributes` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ModernUsageDetail` was modified

* `tags()` was added
* `type()` was added
* `kind()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `id()` was added
* `etag()` was added
* `name()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BudgetFilterProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.LegacyChargeSummary` was modified

* `azureMarketplaceCharges()` was added
* `type()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `id()` was added
* `name()` was added
* `kind()` was added

#### `models.ReservationRecommendationDetails` was modified

* `getWithResponse(java.lang.String,models.Scope,java.lang.String,models.Term,models.LookBackPeriod,java.lang.String,com.azure.core.util.Context)` was added
* `get(java.lang.String,models.Scope,java.lang.String,models.Term,models.LookBackPeriod,java.lang.String)` was added

#### `models.ReservationRecommendationDetailsResourceProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AmountWithExchangeRate` was modified

* `value()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `currency()` was added

#### `ConsumptionManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.SkuProperty` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ReservationRecommendationsListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MeterDetailsResponse` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BalancePropertiesNewPurchasesDetailsItem` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.LegacySharedScopeReservationRecommendationProperties` was modified

* `recommendedQuantity()` was added
* `recommendedQuantityNormalized()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `instanceFlexibilityRatio()` was added
* `skuProperties()` was added
* `term()` was added
* `instanceFlexibilityGroup()` was added
* `scope()` was added
* `meterId()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `totalCostWithReservedInstances()` was added
* `costWithNoReservedInstances()` was added
* `lookBackPeriod()` was added
* `resourceType()` was added
* `netSavings()` was added
* `firstUsageDate()` was added
* `normalizedSize()` was added

#### `models.UsageDetailsListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BudgetComparisonExpression` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CreditBalanceSummary` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ModernReservationTransactionsListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Budgets` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.CurrentSpend` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ReservationRecommendation` was modified

* `kind()` was added

#### `models.ReservationRecommendationDetailsCalculatedSavingsProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ReservationTransactionResource` was modified

* `type()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `id()` was added
* `name()` was added

#### `ConsumptionManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.LegacyReservationRecommendation` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `location()` was added
* `withProperties(models.LegacyReservationRecommendationProperties)` was added
* `kind()` was added
* `tags()` was added
* `properties()` was added
* `type()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `etag()` was added
* `id()` was added
* `name()` was added
* `sku()` was added

#### `models.Reseller` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Notification` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ReservationTransactionsListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Lots` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UsageDetail` was modified

* `kind()` was added

#### `models.ReservationSummariesListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ModernReservationRecommendation` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `properties()` was added
* `location()` was added
* `sku()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `kind()` was added
* `name()` was added
* `etag()` was added
* `type()` was added
* `id()` was added
* `withProperties(models.ModernReservationRecommendationProperties)` was added
* `tags()` was added

#### `models.LegacySingleScopeReservationRecommendationProperties` was modified

* `meterId()` was added
* `instanceFlexibilityRatio()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `instanceFlexibilityGroup()` was added
* `term()` was added
* `scope()` was added
* `totalCostWithReservedInstances()` was added
* `netSavings()` was added
* `recommendedQuantityNormalized()` was added
* `firstUsageDate()` was added
* `lookBackPeriod()` was added
* `normalizedSize()` was added
* `costWithNoReservedInstances()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `skuProperties()` was added
* `resourceType()` was added
* `recommendedQuantity()` was added

#### `models.Tag` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationDisplay` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

## 1.0.0-beta.3 (2021-12-14)

- Azure Resource Manager Consumption client library for Java. This package contains Microsoft Azure SDK for Consumption Management SDK. Consumption management client provides access to consumption resources for Azure Enterprise Subscriptions. Package tag package-2021-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.Bound` was removed

* `models.ForecastsListResult` was removed

* `models.Grain` was removed

* `models.Forecasts` was removed

* `models.Scope` was removed

* `models.ForecastPropertiesConfidenceLevelsItem` was removed

* `models.Forecast` was removed

* `models.ChargeType` was removed

#### `models.LotSummary` was modified

* `tags()` was removed

#### `models.ChargeSummary` was modified

* `tags()` was removed

#### `models.LotsOperations` was modified

* `list(java.lang.String,com.azure.core.util.Context)` was removed
* `list(java.lang.String)` was removed

#### `models.ModernUsageDetail` was modified

* `java.lang.String meterId()` -> `java.util.UUID meterId()`

#### `models.ReservationRecommendationDetails` was modified

* `get(java.lang.String,models.Scope,java.lang.String,models.Term,models.LookBackPeriod,java.lang.String)` was removed
* `getWithResponse(java.lang.String,models.Scope,java.lang.String,models.Term,models.LookBackPeriod,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.EventSummary` was modified

* `tags()` was removed

#### `models.AmountWithExchangeRate` was modified

* `java.lang.Float exchangeRateMonth()` -> `java.lang.Integer exchangeRateMonth()`

#### `ConsumptionManager` was modified

* `forecasts()` was removed

#### `models.ReservationRecommendationsListResult` was modified

* `totalCost()` was removed

#### `models.CreditBalanceSummary` was modified

* `currentBalanceInBillingCurrency()` was removed

#### `models.LegacyReservationRecommendation` was modified

* `scope()` was removed

#### `models.Credits` was modified

* `get(java.lang.String)` was removed
* `getWithResponse(java.lang.String,com.azure.core.util.Context)` was removed

#### `models.EventsOperations` was modified

* `list(java.lang.String,java.lang.String,java.lang.String)` was removed
* `list(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.ModernReservationRecommendation` was modified

* `etag()` was removed
* `resourceType()` was removed
* `subscriptionId()` was removed

### Features Added

* `models.PricingModelType` was added

* `models.LegacySharedScopeReservationRecommendationProperties` was added

* `models.LegacySingleScopeReservationRecommendationProperties` was added

* `models.Status` was added

#### `models.ModernChargeSummary` was modified

* `withEtag(java.lang.String)` was added
* `withEtag(java.lang.String)` was added

#### `models.LotSummary` was modified

* `etagPropertiesEtag()` was added
* `status()` was added
* `purchasedDate()` was added

#### `models.ChargeSummary` was modified

* `withEtag(java.lang.String)` was added

#### `models.LotsOperations` was modified

* `listByBillingAccount(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listByBillingAccount(java.lang.String)` was added
* `listByBillingProfile(java.lang.String,java.lang.String)` was added
* `listByBillingProfile(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.LegacyUsageDetail` was modified

* `pricingModel()` was added
* `payGPrice()` was added

#### `models.ModernUsageDetail` was modified

* `pricingModel()` was added
* `effectivePrice()` was added
* `benefitName()` was added
* `provider()` was added
* `benefitId()` was added
* `costAllocationRuleName()` was added

#### `models.LegacyChargeSummary` was modified

* `withEtag(java.lang.String)` was added
* `withEtag(java.lang.String)` was added

#### `models.ReservationRecommendationDetails` was modified

* `getWithResponse(java.lang.String,java.lang.String,models.Term,models.LookBackPeriod,java.lang.String,com.azure.core.util.Context)` was added
* `get(java.lang.String,java.lang.String,models.Term,models.LookBackPeriod,java.lang.String)` was added

#### `models.EventSummary` was modified

* `billingProfileDisplayName()` was added
* `canceledCredit()` was added
* `lotId()` was added
* `etagPropertiesEtag()` was added
* `lotSource()` was added
* `billingProfileId()` was added

#### `models.ReservationRecommendation` was modified

* `name()` was added
* `type()` was added
* `id()` was added

#### `models.Credits` was modified

* `getWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `get(java.lang.String,java.lang.String)` was added

#### `models.ReservationTransaction` was modified

* `overage()` was added
* `billingMonth()` was added
* `monetaryCommitment()` was added

#### `models.EventsOperations` was modified

* `listByBillingProfile(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listByBillingProfile(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added
* `listByBillingAccount(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listByBillingAccount(java.lang.String)` was added

#### `models.CreditSummary` was modified

* `etagPropertiesEtag()` was added

## 1.0.0-beta.2 (2021-07-09)

- Azure Resource Manager Consumption client library for Java. This package contains Microsoft Azure SDK for Consumption Management SDK. Consumption management client provides access to consumption resources for Azure Enterprise Subscriptions. Package tag package-2019-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.ReservationRecommendationsListResult` was modified

* `totalCost()` was added

#### `ConsumptionManager$Configurable` was modified

* `withScope(java.lang.String)` was added

#### `models.ModernReservationRecommendation` was modified

* `resourceType()` was added
* `etag()` was added
* `subscriptionId()` was added

## 1.0.0-beta.1 (2021-04-13)

- Azure Resource Manager Consumption client library for Java. This package contains Microsoft Azure SDK for Consumption Management SDK. Consumption management client provides access to consumption resources for Azure Enterprise Subscriptions. Package tag package-2019-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
