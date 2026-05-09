# Release History

## 1.1.0 (2026-05-09)

- Azure Resource Manager Reservations client library for Java. This package contains Microsoft Azure SDK for Reservations Management SDK. Microsoft Azure Quota Resource Provider. Package api-version Quota: 2020-10-25, Reservations: 2022-11-01. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.QuotasListResponse` was removed

#### `models.QuotaLimits` was removed

#### `models.QuotaRequestSubmitResponse201` was removed

#### `models.ReservationList` was removed

#### `models.ReservationsListResult` was removed

#### `models.QuotasListHeaders` was removed

#### `models.QuotasListNextHeaders` was removed

#### `models.RefundResponse` was removed

#### `models.CatalogsResult` was removed

#### `models.OperationList` was removed

#### `models.ReservationOrderList` was removed

#### `models.QuotasListNextResponse` was removed

#### `models.QuotaRequestDetailsList` was removed

#### `models.Kind` was removed

#### `models.ExchangePolicyError` was modified

* `ExchangePolicyError()` was changed to private access
* `validate()` was removed
* `withCode(java.lang.String)` was removed
* `withMessage(java.lang.String)` was removed

#### `models.SkuProperty` was modified

* `SkuProperty()` was changed to private access
* `withName(java.lang.String)` was removed
* `withValue(java.lang.String)` was removed
* `validate()` was removed

#### `models.MergeRequest` was modified

* `validate()` was removed

#### `models.ResourceName` was modified

* `validate()` was removed

#### `models.SavingsPlanPurchaseRequest` was modified

* `validate()` was removed

#### `models.ExchangeRequestProperties` was modified

* `validate()` was removed

#### `models.ChangeDirectoryResult` was modified

* `ChangeDirectoryResult()` was changed to private access
* `validate()` was removed
* `withId(java.lang.String)` was removed
* `withIsSucceeded(java.lang.Boolean)` was removed
* `withName(java.lang.String)` was removed
* `withError(java.lang.String)` was removed

#### `models.ReservationSplitProperties` was modified

* `ReservationSplitProperties()` was changed to private access
* `withSplitSource(java.lang.String)` was removed
* `validate()` was removed
* `withSplitDestinations(java.util.List)` was removed

#### `models.ExchangeRequest` was modified

* `validate()` was removed

#### `models.ReservationSwapProperties` was modified

* `ReservationSwapProperties()` was changed to private access
* `withSwapSource(java.lang.String)` was removed
* `validate()` was removed
* `withSwapDestination(java.lang.String)` was removed

#### `models.CalculateRefundRequest` was modified

* `validate()` was removed

#### `models.CalculateExchangeResponseProperties` was modified

* `CalculateExchangeResponseProperties()` was changed to private access
* `validate()` was removed
* `withNetPayable(models.Price)` was removed
* `withReservationsToPurchase(java.util.List)` was removed
* `withPurchasesTotal(models.Price)` was removed
* `withRefundsTotal(models.Price)` was removed
* `withSavingsPlansToPurchase(java.util.List)` was removed
* `withSessionId(java.lang.String)` was removed
* `withPolicyResult(models.ExchangePolicyErrors)` was removed
* `withReservationsToExchange(java.util.List)` was removed

#### `models.ReservationSummary` was modified

* `ReservationSummary()` was changed to private access
* `validate()` was removed

#### `models.ReservationOrderBillingPlanInformation` was modified

* `ReservationOrderBillingPlanInformation()` was changed to private access
* `withPricingCurrencyTotal(models.Price)` was removed
* `withTransactions(java.util.List)` was removed
* `withNextPaymentDueDate(java.time.LocalDate)` was removed
* `withStartDate(java.time.LocalDate)` was removed
* `validate()` was removed

#### `models.ReservationUtilizationAggregates` was modified

* `ReservationUtilizationAggregates()` was changed to private access
* `validate()` was removed

#### `models.PatchPropertiesRenewProperties` was modified

* `validate()` was removed

#### `models.SubRequest` was modified

* `SubRequest()` was changed to private access
* `withUnit(java.lang.String)` was removed
* `validate()` was removed
* `withName(models.ResourceName)` was removed
* `withProvisioningState(models.QuotaRequestState)` was removed

#### `models.SkuRestriction` was modified

* `SkuRestriction()` was changed to private access
* `validate()` was removed
* `withReasonCode(java.lang.String)` was removed
* `withValues(java.util.List)` was removed
* `withType(java.lang.String)` was removed

#### `models.CalculateExchangeRequestProperties` was modified

* `validate()` was removed

#### `models.RefundPolicyError` was modified

* `RefundPolicyError()` was changed to private access
* `withCode(models.ErrorResponseCode)` was removed
* `withMessage(java.lang.String)` was removed
* `validate()` was removed

#### `models.ExchangeResponseProperties` was modified

* `ExchangeResponseProperties()` was changed to private access
* `withPurchasesTotal(models.Price)` was removed
* `withSavingsPlansToPurchase(java.util.List)` was removed
* `withSessionId(java.lang.String)` was removed
* `withPolicyResult(models.ExchangePolicyErrors)` was removed
* `withNetPayable(models.Price)` was removed
* `withRefundsTotal(models.Price)` was removed
* `withReservationsToPurchase(java.util.List)` was removed
* `validate()` was removed
* `withReservationsToExchange(java.util.List)` was removed

#### `models.RefundBillingInformation` was modified

* `RefundBillingInformation()` was changed to private access
* `validate()` was removed
* `withBillingCurrencyProratedAmount(models.Price)` was removed
* `withBillingPlan(models.ReservationBillingPlan)` was removed
* `withCompletedTransactions(java.lang.Integer)` was removed
* `withTotalTransactions(java.lang.Integer)` was removed
* `withBillingCurrencyRemainingCommitmentAmount(models.Price)` was removed
* `withBillingCurrencyTotalPaidAmount(models.Price)` was removed

#### `models.ExchangePolicyErrors` was modified

* `ExchangePolicyErrors()` was changed to private access
* `withPolicyErrors(java.util.List)` was removed
* `validate()` was removed

#### `models.ReservationMergeProperties` was modified

* `ReservationMergeProperties()` was changed to private access
* `withMergeSources(java.util.List)` was removed
* `withMergeDestination(java.lang.String)` was removed
* `validate()` was removed

#### `models.PurchaseRequestPropertiesReservedResourceProperties` was modified

* `validate()` was removed

#### `models.SavingsPlanToPurchaseExchange` was modified

* `SavingsPlanToPurchaseExchange()` was changed to private access
* `validate()` was removed
* `withSavingsPlanId(java.lang.String)` was removed
* `withBillingCurrencyTotal(models.Price)` was removed
* `withSavingsPlanOrderId(java.lang.String)` was removed
* `withProperties(models.SavingsPlanPurchaseRequest)` was removed
* `withStatus(models.OperationStatus)` was removed

#### `models.SplitRequest` was modified

* `validate()` was removed

#### `models.AvailableScopeRequest` was modified

* `validate()` was removed

#### `models.SavingsPlanToPurchaseCalculateExchange` was modified

* `SavingsPlanToPurchaseCalculateExchange()` was changed to private access
* `validate()` was removed
* `withProperties(models.SavingsPlanPurchaseRequest)` was removed
* `withBillingCurrencyTotal(models.Price)` was removed

#### `models.RefundRequest` was modified

* `validate()` was removed

#### `models.CalculateExchangeRequest` was modified

* `validate()` was removed

#### `models.RefundResponseProperties` was modified

* `RefundResponseProperties()` was changed to private access
* `withSessionId(java.lang.String)` was removed
* `validate()` was removed
* `withPolicyResult(models.RefundPolicyResult)` was removed
* `withPricingRefundAmount(models.Price)` was removed
* `withBillingInformation(models.RefundBillingInformation)` was removed
* `withBillingRefundAmount(models.Price)` was removed
* `withQuantity(java.lang.Integer)` was removed

#### `models.CatalogMsrp` was modified

* `CatalogMsrp()` was changed to private access
* `validate()` was removed
* `withP3Y(models.Price)` was removed
* `withP5Y(models.Price)` was removed
* `withP1Y(models.Price)` was removed

#### `models.ReservationToExchange` was modified

* `ReservationToExchange()` was changed to private access
* `withBillingInformation(models.BillingInformation)` was removed
* `withQuantity(java.lang.Integer)` was removed
* `withReservationId(java.lang.String)` was removed
* `validate()` was removed
* `withBillingRefundAmount(models.Price)` was removed

#### `models.RefundPolicyResult` was modified

* `RefundPolicyResult()` was changed to private access
* `withProperties(models.RefundPolicyResultProperty)` was removed
* `validate()` was removed

#### `models.QuotaProperties` was modified

* `validate()` was removed

#### `models.Price` was modified

* `validate()` was removed

#### `ReservationsManager` was modified

* `fluent.AzureReservationApi serviceClient()` -> `fluent.ReservationsManagementClient serviceClient()`

#### `models.ReservationToReturnForExchange` was modified

* `ReservationToReturnForExchange()` was changed to private access
* `withBillingRefundAmount(models.Price)` was removed
* `validate()` was removed
* `withReservationId(java.lang.String)` was removed
* `withStatus(models.OperationStatus)` was removed
* `withQuantity(java.lang.Integer)` was removed
* `withBillingInformation(models.BillingInformation)` was removed

#### `models.CalculatePriceResponseProperties` was modified

* `CalculatePriceResponseProperties()` was changed to private access
* `withGrandTotal(java.lang.Double)` was removed
* `withPricingCurrencyTotal(models.CalculatePriceResponsePropertiesPricingCurrencyTotal)` was removed
* `validate()` was removed
* `withNetTotal(java.lang.Double)` was removed
* `withSkuTitle(java.lang.String)` was removed
* `withReservationOrderId(java.lang.String)` was removed
* `withPaymentSchedule(java.util.List)` was removed
* `withTaxTotal(java.lang.Double)` was removed
* `withIsBillingPartnerManaged(java.lang.Boolean)` was removed
* `withSkuDescription(java.lang.String)` was removed
* `withBillingCurrencyTotal(models.CalculatePriceResponsePropertiesBillingCurrencyTotal)` was removed
* `withIsTaxIncluded(java.lang.Boolean)` was removed

#### `models.ReservationsProperties` was modified

* `ReservationsProperties()` was changed to private access
* `withQuantity(java.lang.Integer)` was removed
* `withRenewProperties(models.RenewPropertiesResponse)` was removed
* `validate()` was removed
* `withPurchaseDateTime(java.time.OffsetDateTime)` was removed
* `withTerm(models.ReservationTerm)` was removed
* `withRenewDestination(java.lang.String)` was removed
* `withSplitProperties(models.ReservationSplitProperties)` was removed
* `withExtendedStatusInfo(models.ExtendedStatusInfo)` was removed
* `withReviewDateTime(java.time.OffsetDateTime)` was removed
* `withExpiryDate(java.time.LocalDate)` was removed
* `withBillingScopeId(java.lang.String)` was removed
* `withRenewSource(java.lang.String)` was removed
* `withInstanceFlexibility(models.InstanceFlexibility)` was removed
* `withAppliedScopeType(models.AppliedScopeType)` was removed
* `withBenefitStartTime(java.time.OffsetDateTime)` was removed
* `withPurchaseDate(java.time.LocalDate)` was removed
* `withSkuDescription(java.lang.String)` was removed
* `withRenew(java.lang.Boolean)` was removed
* `withAppliedScopes(java.util.List)` was removed
* `withProvisioningState(models.ProvisioningState)` was removed
* `withSwapProperties(models.ReservationSwapProperties)` was removed
* `withExpiryDateTime(java.time.OffsetDateTime)` was removed
* `withDisplayName(java.lang.String)` was removed
* `withArchived(java.lang.Boolean)` was removed
* `withCapabilities(java.lang.String)` was removed
* `withBillingPlan(models.ReservationBillingPlan)` was removed
* `withReservedResourceType(models.ReservedResourceType)` was removed
* `withMergeProperties(models.ReservationMergeProperties)` was removed
* `withAppliedScopeProperties(models.AppliedScopeProperties)` was removed
* `withEffectiveDateTime(java.time.OffsetDateTime)` was removed

#### `models.QuotasGetHeaders` was modified

* `etag()` was removed
* `validate()` was removed
* `withEtag(java.lang.String)` was removed

#### `models.PurchaseRequest` was modified

* `validate()` was removed

#### `models.RenewPropertiesResponse` was modified

* `RenewPropertiesResponse()` was changed to private access
* `validate()` was removed
* `withBillingCurrencyTotal(models.RenewPropertiesResponseBillingCurrencyTotal)` was removed
* `withPricingCurrencyTotal(models.RenewPropertiesResponsePricingCurrencyTotal)` was removed
* `withPurchaseProperties(models.PurchaseRequest)` was removed

#### `models.PaymentDetail` was modified

* `PaymentDetail()` was changed to private access
* `withPaymentDate(java.time.LocalDate)` was removed
* `withDueDate(java.time.LocalDate)` was removed
* `withBillingAccount(java.lang.String)` was removed
* `validate()` was removed
* `withPricingCurrencyTotal(models.Price)` was removed
* `withExtendedStatusInfo(models.ExtendedStatusInfo)` was removed
* `withStatus(models.PaymentStatus)` was removed
* `withBillingCurrencyTotal(models.Price)` was removed

#### `models.SkuCapability` was modified

* `SkuCapability()` was changed to private access
* `validate()` was removed
* `withValue(java.lang.String)` was removed
* `withName(java.lang.String)` was removed

#### `models.ChangeDirectoryRequest` was modified

* `validate()` was removed

#### `models.AvailableScopeRequestProperties` was modified

* `validate()` was removed

#### `models.ScopeProperties` was modified

* `ScopeProperties()` was changed to private access
* `withScope(java.lang.String)` was removed
* `validate()` was removed
* `withValid(java.lang.Boolean)` was removed

#### `models.OperationResultError` was modified

* `OperationResultError()` was changed to private access
* `validate()` was removed
* `withCode(java.lang.String)` was removed
* `withMessage(java.lang.String)` was removed

#### `models.RenewPropertiesResponsePricingCurrencyTotal` was modified

* `RenewPropertiesResponsePricingCurrencyTotal()` was changed to private access
* `withAmount(java.lang.Float)` was removed
* `validate()` was removed
* `withCurrencyCode(java.lang.String)` was removed

#### `models.RefundPolicyResultProperty` was modified

* `RefundPolicyResultProperty()` was changed to private access
* `withMaxRefundLimit(models.Price)` was removed
* `withConsumedRefundsTotal(models.Price)` was removed
* `validate()` was removed
* `withPolicyErrors(java.util.List)` was removed

#### `models.CalculatePriceResponsePropertiesBillingCurrencyTotal` was modified

* `CalculatePriceResponsePropertiesBillingCurrencyTotal()` was changed to private access
* `validate()` was removed
* `withAmount(java.lang.Double)` was removed
* `withCurrencyCode(java.lang.String)` was removed

#### `models.RefundRequestProperties` was modified

* `validate()` was removed

#### `models.ReservationToPurchaseCalculateExchange` was modified

* `ReservationToPurchaseCalculateExchange()` was changed to private access
* `validate()` was removed
* `withProperties(models.PurchaseRequest)` was removed
* `withBillingCurrencyTotal(models.Price)` was removed

#### `models.SubscriptionScopeProperties` was modified

* `SubscriptionScopeProperties()` was changed to private access
* `withScopes(java.util.List)` was removed
* `validate()` was removed

#### `models.PatchModel` was modified

* `validate()` was removed

#### `models.BillingInformation` was modified

* `BillingInformation()` was changed to private access
* `withBillingCurrencyProratedAmount(models.Price)` was removed
* `withBillingCurrencyRemainingCommitmentAmount(models.Price)` was removed
* `withBillingCurrencyTotalPaidAmount(models.Price)` was removed
* `validate()` was removed

#### `models.ExtendedStatusInfo` was modified

* `ExtendedStatusInfo()` was changed to private access
* `withStatusCode(models.ReservationStatusCode)` was removed
* `validate()` was removed
* `withMessage(java.lang.String)` was removed

#### `models.ReservationResponse` was modified

* `models.Kind kind()` -> `models.ReservationResponseKind kind()`

#### `models.CalculateRefundRequestProperties` was modified

* `validate()` was removed

#### `models.ReservationsPropertiesUtilization` was modified

* `ReservationsPropertiesUtilization()` was changed to private access
* `withAggregates(java.util.List)` was removed
* `validate()` was removed

#### `models.SkuName` was modified

* `validate()` was removed

#### `models.ReservationToPurchaseExchange` was modified

* `ReservationToPurchaseExchange()` was changed to private access
* `withBillingCurrencyTotal(models.Price)` was removed
* `validate()` was removed
* `withProperties(models.PurchaseRequest)` was removed
* `withStatus(models.OperationStatus)` was removed
* `withReservationOrderId(java.lang.String)` was removed
* `withReservationId(java.lang.String)` was removed

#### `models.ReservationToReturn` was modified

* `validate()` was removed

#### `models.AppliedScopeProperties` was modified

* `validate()` was removed

#### `models.AppliedReservationList` was modified

* `AppliedReservationList()` was changed to private access
* `withNextLink(java.lang.String)` was removed
* `withValue(java.util.List)` was removed
* `validate()` was removed

#### `models.OperationDisplay` was modified

* `OperationDisplay()` was changed to private access
* `withDescription(java.lang.String)` was removed
* `withResource(java.lang.String)` was removed
* `withOperation(java.lang.String)` was removed
* `withProvider(java.lang.String)` was removed
* `validate()` was removed

#### `models.CalculatePriceResponsePropertiesPricingCurrencyTotal` was modified

* `CalculatePriceResponsePropertiesPricingCurrencyTotal()` was changed to private access
* `validate()` was removed
* `withCurrencyCode(java.lang.String)` was removed
* `withAmount(java.lang.Float)` was removed

#### `models.RenewPropertiesResponseBillingCurrencyTotal` was modified

* `RenewPropertiesResponseBillingCurrencyTotal()` was changed to private access
* `withAmount(java.lang.Float)` was removed
* `withCurrencyCode(java.lang.String)` was removed
* `validate()` was removed

#### `models.Commitment` was modified

* `validate()` was removed

### Features Added

* `models.ReservationResponseKind` was added

#### `models.QuotaRequestDetails` was modified

* `systemData()` was added

#### `models.CurrentQuotaLimitBase` was modified

* `systemData()` was added

#### `models.QuotasGetHeaders` was modified

* `eTag()` was added

## 1.0.0 (2024-12-25)

- Azure Resource Manager reservations client library for Java. This package contains Microsoft Azure SDK for reservations Management SDK. This API describe Azure Reservation. Package tag package-2022-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Other Changes

- Release for Azure Resource Manager reservations client library for Java.

## 1.0.0-beta.3 (2024-10-31)

- Azure Resource Manager reservations client library for Java. This package contains Microsoft Azure SDK for reservations Management SDK. This API describe Azure Reservation. Package tag package-2022-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.ExchangePolicyError` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SkuProperty` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MergeRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ResourceName` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SavingsPlanPurchaseRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ExchangeRequestProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.QuotaLimits` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ChangeDirectoryResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ReservationSplitProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ExchangeRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ReservationList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ReservationSwapProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CalculateRefundRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CalculateExchangeResponseProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ReservationsListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ReservationSummary` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ReservationOrderBillingPlanInformation` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ReservationUtilizationAggregates` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PatchPropertiesRenewProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SubRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SkuRestriction` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CalculateExchangeRequestProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.RefundPolicyError` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ExchangeResponseProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RefundBillingInformation` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ExchangePolicyErrors` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ReservationMergeProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PurchaseRequestPropertiesReservedResourceProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SavingsPlanToPurchaseExchange` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SplitRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AvailableScopeRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SavingsPlanToPurchaseCalculateExchange` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RefundRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CalculateExchangeRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RefundResponseProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CatalogMsrp` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ReservationToExchange` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.RefundPolicyResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.QuotaProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Price` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ReservationToReturnForExchange` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CatalogsResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CalculatePriceResponseProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ReservationsProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PurchaseRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.RenewPropertiesResponse` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PaymentDetail` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SkuCapability` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ChangeDirectoryRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AvailableScopeRequestProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ScopeProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationResultError` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.RenewPropertiesResponsePricingCurrencyTotal` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RefundPolicyResultProperty` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CalculatePriceResponsePropertiesBillingCurrencyTotal` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ReservationOrderList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.RefundRequestProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ReservationToPurchaseCalculateExchange` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SubscriptionScopeProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PatchModel` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BillingInformation` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ExtendedStatusInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CalculateRefundRequestProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ReservationsPropertiesUtilization` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.QuotaRequestDetailsList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SkuName` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ReservationToPurchaseExchange` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ReservationToReturn` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AppliedScopeProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AppliedReservationList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationDisplay` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CalculatePriceResponsePropertiesPricingCurrencyTotal` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.RenewPropertiesResponseBillingCurrencyTotal` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Commitment` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

## 1.0.0-beta.2 (2023-03-20)

- Azure Resource Manager reservations client library for Java. This package contains Microsoft Azure SDK for reservations Management SDK. This API describe Azure Reservation. Package tag package-2022-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Returns` was modified

* `models.RefundResponse post(java.lang.String,models.RefundRequest)` -> `models.ReservationOrderResponse post(java.lang.String,models.RefundRequest)`
* `models.RefundResponse post(java.lang.String,models.RefundRequest,com.azure.core.util.Context)` -> `models.ReservationOrderResponse post(java.lang.String,models.RefundRequest,com.azure.core.util.Context)`

### Features Added

#### `models.CatalogMsrp` was modified

* `p5Y()` was added
* `p3Y()` was added
* `withP5Y(models.Price)` was added
* `withP3Y(models.Price)` was added

## 1.0.0-beta.1 (2023-02-07)

- Azure Resource Manager reservations client library for Java. This package contains Microsoft Azure SDK for reservations Management SDK. This API describe Azure Reservation. Package tag package-2022-11. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
