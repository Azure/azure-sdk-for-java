# Release History

## 1.0.0-beta.4 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
