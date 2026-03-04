# Release History

## 1.0.0-beta.3 (2026-03-04)

- Azure Resource Manager BillingBenefits client library for Java. This package contains Microsoft Azure SDK for BillingBenefits Management SDK. Azure Benefits RP let users create and manage benefits like savings plan. Package api-version 2024-11-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.SavingsPlansUpdateHeaders` was removed

#### `models.SavingsPlanOrderModelList` was removed

#### `models.SavingsPlansUpdateResponse` was removed

#### `models.PricingCurrencyTotal` was removed

#### `models.PricingCurrencyDuration` was removed

#### `models.SavingsPlanModelListResult` was removed

#### `models.OperationListResult` was removed

#### `models.SavingsPlanModelList` was removed

#### `models.SavingsPlanOrderAliasModel` was modified

* `models.Sku sku()` -> `models.ResourceSku sku()`

#### `models.Utilization` was modified

* `Utilization()` was changed to private access
* `withAggregates(java.util.List)` was removed
* `validate()` was removed

#### `models.PurchaseRequest` was modified

* `validate()` was removed
* `models.Sku sku()` -> `models.ResourceSku sku()`
* `withSku(models.Sku)` was removed

#### `models.SavingsPlanUpdateRequest` was modified

* `validate()` was removed

#### `models.ReservationOrderAliasResponse` was modified

* `models.Sku sku()` -> `models.ResourceSku sku()`

#### `models.ReservationOrderAliasResponsePropertiesReservedResourceProperties` was modified

* `ReservationOrderAliasResponsePropertiesReservedResourceProperties()` was changed to private access
* `withInstanceFlexibility(models.InstanceFlexibility)` was removed
* `validate()` was removed

#### `models.SavingsPlanSummaryCount` was modified

* `SavingsPlanSummaryCount()` was changed to private access
* `java.lang.Float noBenefitCount()` -> `java.lang.Double noBenefitCount()`
* `java.lang.Float pendingCount()` -> `java.lang.Double pendingCount()`
* `java.lang.Float cancelledCount()` -> `java.lang.Double cancelledCount()`
* `java.lang.Float succeededCount()` -> `java.lang.Double succeededCount()`
* `validate()` was removed
* `java.lang.Float expiredCount()` -> `java.lang.Double expiredCount()`
* `java.lang.Float processingCount()` -> `java.lang.Double processingCount()`
* `java.lang.Float failedCount()` -> `java.lang.Double failedCount()`
* `java.lang.Float expiringCount()` -> `java.lang.Double expiringCount()`
* `java.lang.Float warningCount()` -> `java.lang.Double warningCount()`

#### `BillingBenefitsManager` was modified

* `fluent.BillingBenefitsRP serviceClient()` -> `fluent.BillingBenefitsManagementClient serviceClient()`

#### `models.SavingsPlanOrders` was modified

* `getWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.ExtendedStatusInfo` was modified

* `ExtendedStatusInfo()` was changed to private access
* `withMessage(java.lang.String)` was removed
* `validate()` was removed
* `withStatusCode(java.lang.String)` was removed

#### `models.SavingsPlanSummary` was modified

* `SavingsPlanSummary()` was changed to private access
* `validate()` was removed
* `withValue(models.SavingsPlanSummaryCount)` was removed

#### `models.ReservationOrderAliasRequest` was modified

* `models.Sku sku()` -> `models.ResourceSku sku()`
* `validate()` was removed
* `withSku(models.Sku)` was removed

#### `models.ReservationOrderAliasRequestPropertiesReservedResourceProperties` was modified

* `validate()` was removed

#### `models.SavingsPlanPurchaseValidateRequest` was modified

* `validate()` was removed

#### `models.SavingsPlanValidResponseProperty` was modified

* `SavingsPlanValidResponseProperty()` was changed to private access
* `withValid(java.lang.Boolean)` was removed
* `validate()` was removed
* `withReasonCode(java.lang.String)` was removed
* `withReason(java.lang.String)` was removed

#### `models.SavingsPlanOrderModel` was modified

* `models.Sku sku()` -> `models.ResourceSku sku()`

#### `models.BillingPlanInformation` was modified

* `BillingPlanInformation()` was changed to private access
* `withPricingCurrencyTotal(models.Price)` was removed
* `withNextPaymentDueDate(java.time.LocalDate)` was removed
* `withTransactions(java.util.List)` was removed
* `withStartDate(java.time.LocalDate)` was removed
* `java.time.LocalDate startDate()` -> `java.time.OffsetDateTime startDate()`
* `validate()` was removed
* `java.time.LocalDate nextPaymentDueDate()` -> `java.time.OffsetDateTime nextPaymentDueDate()`

#### `models.SavingsPlans` was modified

* `updateWithResponse(java.lang.String,java.lang.String,models.SavingsPlanUpdateRequest,com.azure.core.util.Context)` was removed
* `listAll(java.lang.String,java.lang.String,java.lang.String,java.lang.Float,java.lang.String,java.lang.Float,com.azure.core.util.Context)` was removed
* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Commitment` was modified

* `validate()` was removed

#### `models.OperationDisplay` was modified

* `OperationDisplay()` was changed to private access
* `validate()` was removed

#### `models.UtilizationAggregates` was modified

* `UtilizationAggregates()` was changed to private access
* `validate()` was removed
* `java.lang.Float grain()` -> `java.lang.Double grain()`
* `java.lang.Float value()` -> `java.lang.Double value()`

#### `models.SavingsPlanModel` was modified

* `models.Sku sku()` -> `models.ResourceSku sku()`

#### `models.SavingsPlanUpdateRequestProperties` was modified

* `validate()` was removed

#### `models.Sku` was modified

* `validate()` was removed

#### `models.Price` was modified

* `validate()` was removed

#### `models.SavingsPlanUpdateValidateRequest` was modified

* `validate()` was removed

#### `models.PaymentDetail` was modified

* `PaymentDetail()` was changed to private access
* `withStatus(models.PaymentStatus)` was removed
* `java.time.LocalDate paymentDate()` -> `java.time.OffsetDateTime paymentDate()`
* `java.time.LocalDate dueDate()` -> `java.time.OffsetDateTime dueDate()`
* `withPaymentDate(java.time.LocalDate)` was removed
* `withBillingAccount(java.lang.String)` was removed
* `withBillingCurrencyTotal(models.Price)` was removed
* `withPricingCurrencyTotal(models.Price)` was removed
* `withDueDate(java.time.LocalDate)` was removed
* `validate()` was removed

#### `models.AppliedScopeProperties` was modified

* `validate()` was removed

#### `models.RenewProperties` was modified

* `validate()` was removed

### Features Added

* `models.DiscountProperties` was added

* `models.DiscountType` was added

* `models.CustomPriceProperties` was added

* `models.DiscountTypeProductSku` was added

* `models.ManagedServiceIdentity` was added

* `models.DiscountTypeProductFamily` was added

* `models.ManagedServiceIdentityType` was added

* `models.Discounts` was added

* `models.Discount` was added

* `models.DiscountTypeCustomPriceMultiCurrency` was added

* `models.CatalogClaimsItem` was added

* `models.DiscountTypeProduct` was added

* `models.EntityTypeAffiliateDiscount` was added

* `models.DiscountAppliedScopeType` was added

* `models.DiscountOperations` was added

* `models.ConditionsItem` was added

* `models.ResourceSku` was added

* `models.SkuTier` was added

* `models.Plan` was added

* `models.PricingPolicy` was added

* `models.PriceGuaranteeProperties` was added

* `models.DiscountCombinationRule` was added

* `models.DiscountProvisioningState` was added

* `models.DiscountTypeProperties` was added

* `models.DiscountEntityType` was added

* `models.DiscountTypeCustomPrice` was added

* `models.ApplyDiscountOn` was added

* `models.DiscountRuleType` was added

* `models.UserAssignedIdentity` was added

* `models.Discount$DefinitionStages` was added

* `models.MarketSetPricesItems` was added

* `models.Discount$Definition` was added

* `models.DiscountStatus` was added

* `models.DiscountPatchRequest` was added

* `models.EntityTypePrimaryDiscount` was added

#### `models.PurchaseRequest` was modified

* `withSku(models.ResourceSku)` was added

#### `BillingBenefitsManager` was modified

* `discountOperations()` was added
* `discounts()` was added

#### `models.SavingsPlanOrders` was modified

* `getWithResponse(java.lang.String,com.azure.core.util.Context)` was added

#### `models.ReservationOrderAliasRequest` was modified

* `withSku(models.ResourceSku)` was added

#### `models.SavingsPlans` was modified

* `listAll(java.lang.String,java.lang.String,java.lang.String,java.lang.Double,java.lang.String,java.lang.Double,com.azure.core.util.Context)` was added
* `getWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `update(java.lang.String,java.lang.String,models.SavingsPlanUpdateRequest,com.azure.core.util.Context)` was added

#### `models.Sku` was modified

* `tier()` was added
* `withTier(models.SkuTier)` was added
* `withSize(java.lang.String)` was added
* `capacity()` was added
* `withCapacity(java.lang.Integer)` was added
* `family()` was added
* `size()` was added
* `withFamily(java.lang.String)` was added

## 1.0.0-beta.2 (2024-10-14)

- Azure Resource Manager BillingBenefits client library for Java. This package contains Microsoft Azure SDK for BillingBenefits Management SDK. Azure Benefits RP let users create and manage benefits like savings plan. Package tag package-2022-11-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.SavingsPlanOrderAliasModel` was modified

* `renew()` was added

#### `models.Utilization` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SavingsPlanSummary` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PurchaseRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SavingsPlanUpdateRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ReservationOrderAliasRequest` was modified

* `type()` was added
* `name()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `id()` was added

#### `models.SavingsPlanOrderModelList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PricingCurrencyTotal` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ReservationOrderAliasRequestPropertiesReservedResourceProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SavingsPlanPurchaseValidateRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SavingsPlanValidResponseProperty` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BillingPlanInformation` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ReservationOrderAliasResponsePropertiesReservedResourceProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SavingsPlanSummaryCount` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Commitment` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UtilizationAggregates` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SavingsPlanUpdateRequestProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Sku` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SavingsPlanModelList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Price` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SavingsPlanUpdateValidateRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ExtendedStatusInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PaymentDetail` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AppliedScopeProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.RenewProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SavingsPlanModelListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

## 1.0.0-beta.1 (2023-01-11)

- Azure Resource Manager BillingBenefits client library for Java. This package contains Microsoft Azure SDK for BillingBenefits Management SDK. Azure Benefits RP let users create and manage benefits like savings plan. Package tag package-2022-11-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
