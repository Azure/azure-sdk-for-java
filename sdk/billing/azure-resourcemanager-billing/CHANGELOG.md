# Release History

## 1.1.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0 (2024-09-05)

- Azure Resource Manager Billing client library for Java. This package contains Microsoft Azure SDK for Billing Management SDK. Billing Client. Package tag package-2024-04. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.EnrollmentPolicies` was removed

* `models.ReservationPropertyUtilization` was removed

* `models.BillingProfileSpendingLimit` was removed

* `models.TransactionTypeKind` was removed

* `models.ProductTransferValidationErrorCode` was removed

* `models.ValidateProductTransferEligibilityResult` was removed

* `models.TransferBillingSubscriptionRequestProperties` was removed

* `models.ProductsMoveResponse` was removed

* `models.ViewCharges` was removed

* `models.SpendingLimitForBillingProfile` was removed

* `models.DocumentType` was removed

* `models.StatusReasonCodeForBillingProfile` was removed

* `models.ValidateAddressResponse` was removed

* `models.BillingPermissionsProperties` was removed

* `models.TargetCloud` was removed

* `models.ValidateSubscriptionTransferEligibilityResult` was removed

* `models.ValidateSubscriptionTransferEligibilityError` was removed

* `models.ValidateProductTransferEligibilityError` was removed

* `models.InstructionListResult` was removed

* `models.BillingPeriods` was removed

* `models.Document` was removed

* `models.BillingProfilesOnExpand` was removed

* `models.Policy` was removed

* `models.BillingPeriod` was removed

* `models.ReservationType` was removed

* `models.DownloadUrl` was removed

* `models.BillingPermissionsListResult` was removed

* `models.ProductsMoveHeaders` was removed

* `models.PaymentProperties` was removed

* `models.BillingSubscriptionStatusType` was removed

* `models.Instructions` was removed

* `models.TransferProductRequestProperties` was removed

* `models.Enrollment` was removed

* `models.BillingSubscriptionsListResult` was removed

* `models.EnrollmentAccountSummary` was removed

* `models.InvoiceSectionsOnExpand` was removed

* `models.BillingFrequency` was removed

* `models.Participants` was removed

* `models.ProductsListResult` was removed

* `models.BillingAccountUpdateRequest` was removed

* `models.BillingPeriodsListResult` was removed

* `models.StatusReasonCode` was removed

* `models.Instruction` was removed

* `models.InvoiceSectionListWithCreateSubPermissionResult` was removed

* `models.ProductStatusType` was removed

#### `models.InvoiceSectionListResult` was modified

* `totalCount()` was removed

#### `models.AvailableBalance` was modified

* `amount()` was removed

#### `models.Reservations` was modified

* `listByBillingProfile(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `listByBillingAccount(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.BillingPermissions` was modified

* `listByCustomer(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `listByInvoiceSections(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `listByInvoiceSections(java.lang.String,java.lang.String,java.lang.String)` was removed
* `listByCustomer(java.lang.String,java.lang.String)` was removed

#### `models.BillingSubscription` was modified

* `java.util.UUID subscriptionId()` -> `java.lang.String subscriptionId()`
* `costCenter()` was removed
* `subscriptionBillingStatus()` was removed

#### `models.Product` was modified

* `billingFrequency()` was removed
* `customerDisplayName()` was removed
* `lastChargeDate()` was removed
* `customerId()` was removed
* `endDate()` was removed
* `purchaseDate()` was removed
* `displayName()` was removed
* `productTypeId()` was removed
* `invoiceSectionId()` was removed
* `lastCharge()` was removed
* `productType()` was removed
* `quantity()` was removed
* `tenantId()` was removed
* `billingProfileDisplayName()` was removed
* `reseller()` was removed
* `status()` was removed
* `billingProfileId()` was removed
* `invoiceSectionDisplayName()` was removed
* `autoRenew()` was removed
* `skuDescription()` was removed
* `availabilityId()` was removed
* `skuId()` was removed

#### `models.Customer` was modified

* `enabledAzurePlans()` was removed
* `billingProfileId()` was removed
* `displayName()` was removed
* `resellers()` was removed
* `billingProfileDisplayName()` was removed

#### `models.BillingProfileListResult` was modified

* `totalCount()` was removed

#### `models.Policies` was modified

* `updateCustomerWithResponse(java.lang.String,java.lang.String,fluent.models.CustomerPolicyInner,com.azure.core.util.Context)` was removed
* `updateWithResponse(java.lang.String,java.lang.String,fluent.models.PolicyInner,com.azure.core.util.Context)` was removed
* `update(java.lang.String,java.lang.String,fluent.models.PolicyInner)` was removed
* `updateCustomer(java.lang.String,java.lang.String,fluent.models.CustomerPolicyInner)` was removed
* `getByCustomerWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `getByCustomer(java.lang.String,java.lang.String)` was removed
* `models.Policy getByBillingProfile(java.lang.String,java.lang.String)` -> `models.BillingProfilePolicy getByBillingProfile(java.lang.String,java.lang.String)`

#### `models.BillingSubscriptions` was modified

* `listByBillingAccount(java.lang.String,com.azure.core.util.Context)` was removed
* `listByBillingProfile(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `get(java.lang.String)` was removed
* `listByInvoiceSection(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `validateMove(java.lang.String,models.TransferBillingSubscriptionRequestProperties)` was removed
* `getWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `update(java.lang.String,fluent.models.BillingSubscriptionInner)` was removed
* `listByCustomer(java.lang.String,java.lang.String)` was removed
* `move(java.lang.String,models.TransferBillingSubscriptionRequestProperties)` was removed
* `listByCustomer(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `move(java.lang.String,models.TransferBillingSubscriptionRequestProperties,com.azure.core.util.Context)` was removed
* `validateMoveWithResponse(java.lang.String,models.TransferBillingSubscriptionRequestProperties,com.azure.core.util.Context)` was removed
* `updateWithResponse(java.lang.String,fluent.models.BillingSubscriptionInner,com.azure.core.util.Context)` was removed

#### `models.CustomerListResult` was modified

* `totalCount()` was removed

#### `models.CustomerPolicy` was modified

* `viewCharges()` was removed

#### `models.BillingProfiles` was modified

* `listByBillingAccount(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.AvailableBalances` was modified

* `getWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `get(java.lang.String,java.lang.String)` was removed

#### `models.BillingRoleDefinition` was modified

* `permissions()` was removed
* `roleName()` was removed
* `description()` was removed

#### `models.InvoiceSections` was modified

* `listByBillingProfile(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Products` was modified

* `moveWithResponse(java.lang.String,java.lang.String,models.TransferProductRequestProperties,com.azure.core.util.Context)` was removed
* `validateMoveWithResponse(java.lang.String,java.lang.String,models.TransferProductRequestProperties,com.azure.core.util.Context)` was removed
* `update(java.lang.String,java.lang.String,fluent.models.ProductInner)` was removed
* `listByCustomer(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `updateWithResponse(java.lang.String,java.lang.String,fluent.models.ProductInner,com.azure.core.util.Context)` was removed
* `validateMove(java.lang.String,java.lang.String,models.TransferProductRequestProperties)` was removed
* `listByBillingProfile(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `listByInvoiceSection(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `move(java.lang.String,java.lang.String,models.TransferProductRequestProperties)` was removed
* `listByBillingAccount(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.TransactionListResult` was modified

* `totalCount()` was removed

#### `models.EnrollmentAccount` was modified

* `accountName()` was removed
* `endDate()` was removed
* `withDepartment(models.Department)` was removed
* `withAccountName(java.lang.String)` was removed
* `accountOwnerEmail()` was removed
* `status()` was removed
* `withCostCenter(java.lang.String)` was removed
* `accountOwner()` was removed
* `costCenter()` was removed
* `withAccountOwner(java.lang.String)` was removed
* `withStatus(java.lang.String)` was removed
* `withEndDate(java.time.OffsetDateTime)` was removed
* `validate()` was removed
* `startDate()` was removed
* `withAccountOwnerEmail(java.lang.String)` was removed
* `withStartDate(java.time.OffsetDateTime)` was removed
* `department()` was removed

#### `models.InvoiceSectionWithCreateSubPermission` was modified

* `models.StatusReasonCodeForBillingProfile billingProfileStatusReasonCode()` -> `models.BillingProfileStatusReasonCode billingProfileStatusReasonCode()`
* `models.SpendingLimitForBillingProfile billingProfileSpendingLimit()` -> `models.SpendingLimit billingProfileSpendingLimit()`

#### `models.EnrollmentAccounts` was modified

* `list(com.azure.core.util.Context)` was removed
* `list()` was removed
* `get(java.lang.String)` was removed
* `getWithResponse(java.lang.String,com.azure.core.util.Context)` was removed

#### `models.BillingAccounts` was modified

* `update(java.lang.String,models.BillingAccountUpdateRequest,com.azure.core.util.Context)` was removed
* `list(java.lang.String,com.azure.core.util.Context)` was removed
* `getWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `update(java.lang.String,models.BillingAccountUpdateRequest)` was removed
* `listInvoiceSectionsByCreateSubscriptionPermission(java.lang.String,com.azure.core.util.Context)` was removed

#### `models.BillingRoleAssignments` was modified

* `listByBillingProfile(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `models.BillingRoleAssignment deleteByInvoiceSection(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` -> `void deleteByInvoiceSection(java.lang.String,java.lang.String,java.lang.String,java.lang.String)`
* `models.BillingRoleAssignment deleteByBillingProfile(java.lang.String,java.lang.String,java.lang.String)` -> `void deleteByBillingProfile(java.lang.String,java.lang.String,java.lang.String)`
* `listByBillingAccount(java.lang.String,com.azure.core.util.Context)` was removed
* `listByInvoiceSection(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `models.BillingRoleAssignment deleteByBillingAccount(java.lang.String,java.lang.String)` -> `void deleteByBillingAccount(java.lang.String,java.lang.String)`

#### `models.InvoiceSection` was modified

* `displayName()` was removed
* `state()` was removed
* `targetCloud()` was removed
* `systemId()` was removed
* `labels()` was removed

#### `models.Invoices` was modified

* `downloadMultipleBillingSubscriptionInvoices(java.util.List,com.azure.core.util.Context)` was removed
* `downloadBillingSubscriptionInvoice(java.lang.String,java.lang.String)` was removed
* `downloadMultipleBillingProfileInvoices(java.lang.String,java.util.List)` was removed
* `listByBillingProfile(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was removed
* `listByBillingSubscription(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `downloadInvoice(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `listByBillingAccount(java.lang.String,java.lang.String,java.lang.String)` was removed
* `listByBillingSubscription(java.lang.String,java.lang.String)` was removed
* `downloadBillingSubscriptionInvoice(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `downloadInvoice(java.lang.String,java.lang.String,java.lang.String)` was removed
* `getBySubscriptionAndInvoiceId(java.lang.String)` was removed
* `get(java.lang.String,java.lang.String)` was removed
* `getBySubscriptionAndInvoiceIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed
* `getById(java.lang.String)` was removed
* `listByBillingProfile(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `listByBillingAccount(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `downloadMultipleBillingSubscriptionInvoices(java.util.List)` was removed
* `getWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `downloadMultipleBillingProfileInvoices(java.lang.String,java.util.List,com.azure.core.util.Context)` was removed
* `getByIdWithResponse(java.lang.String,com.azure.core.util.Context)` was removed

#### `BillingManager` was modified

* `billingPeriods()` was removed
* `instructions()` was removed

#### `models.BillingRoleAssignment` was modified

* `createdOn()` was removed
* `createdByPrincipalId()` was removed
* `principalTenantId()` was removed
* `createdByPrincipalTenantId()` was removed
* `roleDefinitionId()` was removed
* `createdByUserEmailAddress()` was removed
* `scope()` was removed
* `userEmailAddress()` was removed
* `principalId()` was removed
* `userAuthenticationType()` was removed

#### `models.Agreement` was modified

* `effectiveDate()` was removed
* `category()` was removed
* `participants()` was removed
* `acceptanceMode()` was removed
* `status()` was removed
* `agreementLink()` was removed
* `billingProfileInfo()` was removed
* `expirationDate()` was removed

#### `models.Agreements` was modified

* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Department` was modified

* `withDepartmentName(java.lang.String)` was removed
* `departmentName()` was removed
* `status()` was removed
* `enrollmentAccounts()` was removed
* `costCenter()` was removed
* `validate()` was removed
* `withCostCenter(java.lang.String)` was removed
* `withStatus(java.lang.String)` was removed
* `withEnrollmentAccounts(java.util.List)` was removed

#### `models.Transactions` was modified

* `listByInvoice(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Customers` was modified

* `get(java.lang.String,java.lang.String)` was removed
* `listByBillingAccount(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `listByBillingProfile(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.Amount` was modified

* `models.Amount withValue(java.lang.Float)` -> `models.Amount withValue(java.lang.Float)`

#### `models.BillingProfile` was modified

* `systemId()` was removed
* `enabledAzurePlans()` was removed
* `invoiceEmailOptIn()` was removed
* `invoiceSections()` was removed
* `targetClouds()` was removed
* `status()` was removed
* `currency()` was removed
* `statusReasonCode()` was removed
* `indirectRelationshipInfo()` was removed
* `hasReadAccess()` was removed
* `billTo()` was removed
* `spendingLimit()` was removed
* `poNumber()` was removed
* `billingRelationshipType()` was removed
* `displayName()` was removed
* `invoiceDay()` was removed

#### `models.BillingRoleAssignmentListResult` was modified

* `java.util.List value()` -> `java.util.List value()`
* `validate()` was removed
* `java.lang.String nextLink()` -> `java.lang.String nextLink()`

#### `models.InvoiceListResult` was modified

* `totalCount()` was removed

#### `models.RebillDetails` was modified

* `java.util.Map rebillDetails()` -> `models.RebillDetails rebillDetails()`

#### `models.BillingAccount` was modified

* `enrollmentAccounts()` was removed
* `enrollmentDetails()` was removed
* `billingProfiles()` was removed
* `agreementType()` was removed
* `displayName()` was removed
* `departments()` was removed
* `notificationEmailAddress()` was removed
* `soldTo()` was removed
* `accountStatus()` was removed
* `accountType()` was removed
* `hasReadAccess()` was removed

#### `models.Address` was modified

* `models.ValidateAddressResponse validate(models.AddressDetails)` -> `models.AddressValidationResponse validate(models.AddressDetails)`

#### `models.BillingProperty` was modified

* `isAccountAdmin()` was removed
* `billingAccountDisplayName()` was removed
* `skuDescription()` was removed
* `billingAccountId()` was removed
* `billingProfileSpendingLimit()` was removed
* `billingProfileStatusReasonCode()` was removed
* `billingTenantId()` was removed
* `invoiceSectionDisplayName()` was removed
* `invoiceSectionId()` was removed
* `costCenter()` was removed
* `billingProfileStatus()` was removed
* `productName()` was removed
* `skuId()` was removed
* `billingProfileId()` was removed
* `billingProfileDisplayName()` was removed
* `accountAdminNotificationEmailAddress()` was removed
* `productId()` was removed

#### `models.Invoice` was modified

* `documents()` was removed
* `dueDate()` was removed
* `taxAmount()` was removed
* `subTotal()` was removed
* `freeAzureCreditApplied()` was removed
* `amountDue()` was removed
* `invoiceDate()` was removed
* `invoicePeriodEndDate()` was removed
* `totalAmount()` was removed
* `creditForDocumentId()` was removed
* `isMonthlyInvoice()` was removed
* `creditAmount()` was removed
* `invoiceType()` was removed
* `billedAmount()` was removed
* `payments()` was removed
* `billingProfileId()` was removed
* `purchaseOrderNumber()` was removed
* `invoicePeriodStartDate()` was removed
* `billingProfileDisplayName()` was removed
* `documentType()` was removed
* `azurePrepaymentApplied()` was removed
* `subscriptionId()` was removed
* `billedDocumentId()` was removed
* `rebillDetails()` was removed
* `status()` was removed

#### `models.BillingProperties` was modified

* `getWithResponse(com.azure.core.util.Context)` was removed

#### `models.Transaction` was modified

* `productDescription()` was removed
* `invoice()` was removed
* `units()` was removed
* `exchangeRate()` was removed
* `invoiceSectionId()` was removed
* `billingProfileDisplayName()` was removed
* `date()` was removed
* `azurePlan()` was removed
* `transactionAmount()` was removed
* `discount()` was removed
* `subscriptionId()` was removed
* `billingProfileId()` was removed
* `customerDisplayName()` was removed
* `marketPrice()` was removed
* `servicePeriodEndDate()` was removed
* `billingCurrency()` was removed
* `tax()` was removed
* `unitType()` was removed
* `servicePeriodStartDate()` was removed
* `transactionType()` was removed
* `unitOfMeasure()` was removed
* `invoiceSectionDisplayName()` was removed
* `azureCreditApplied()` was removed
* `productType()` was removed
* `pricingCurrency()` was removed
* `subscriptionName()` was removed
* `customerId()` was removed
* `productTypeId()` was removed
* `invoiceId()` was removed
* `effectivePrice()` was removed
* `orderId()` was removed
* `productFamily()` was removed
* `kind()` was removed
* `orderName()` was removed
* `quantity()` was removed
* `subTotal()` was removed

#### `models.Reservation` was modified

* `utilization()` was removed
* `java.lang.String effectiveDateTime()` -> `java.time.OffsetDateTime effectiveDateTime()`

### Features Added

* `models.AccessDecision` was added

* `models.InvoicePropertiesSubTotal` was added

* `models.CancelSubscriptionRequest` was added

* `models.AssociatedTenantProperties` was added

* `models.BillingRequestProperties` was added

* `models.BillingPermissionListResult` was added

* `models.PaymentMethod` was added

* `models.SpecialTaxationType` was added

* `models.EnrollmentDetails` was added

* `models.DepartmentListResult` was added

* `models.ExtendedTermOption` was added

* `models.DeleteBillingProfileEligibilityDetail` was added

* `models.MarkupStatus` was added

* `models.RegistrationNumber` was added

* `models.InvoiceProperties` was added

* `models.ProductPropertiesLastCharge` was added

* `models.TransactionPropertiesTransactionAmount` was added

* `models.SubscriptionEnrollmentAccountStatus` was added

* `models.PaymentTermsEligibilityResult` was added

* `models.ReservationOrders` was added

* `models.BillingRequestStatus` was added

* `models.PaymentStatus` was added

* `models.DeleteInvoiceSectionEligibilityResult` was added

* `models.BillingPlanInformation` was added

* `models.Commitment` was added

* `models.FailedPaymentReason` was added

* `models.BillingPermission` was added

* `models.PolicySummary` was added

* `models.AppliedScopeProperties` was added

* `models.BillingSubscriptionAlias` was added

* `models.CustomerStatus` was added

* `models.ProvisioningTenantState` was added

* `models.ProductType` was added

* `models.PaymentAmount` was added

* `models.PaymentMethodsListResult` was added

* `models.TransactionPropertiesSubTotal` was added

* `models.PartnerTransfers` was added

* `models.BillingPlan` was added

* `models.PurchaseRequest` was added

* `models.DeleteBillingProfileEligibilityCode` was added

* `models.SavingsPlanOrderModelList` was added

* `models.SavingsPlanValidResponseProperty` was added

* `models.PaymentTermsEligibilityDetail` was added

* `models.BillingSubscriptionSplitRequest` was added

* `models.AvailableBalancePropertiesTotalPaymentsOnAccount` was added

* `models.TransactionType` was added

* `models.ProductTransferStatus` was added

* `models.ReservationList` was added

* `models.BillingAccountProperties` was added

* `models.ProxyResourceWithTags` was added

* `models.SubscriptionStatusReason` was added

* `models.MoveProductEligibilityResultErrorDetails` was added

* `models.EnrollmentDepartmentAdminViewCharges` was added

* `models.InvoiceSectionLabelManagementPolicy` was added

* `models.SavingsPlanValidateResponse` was added

* `models.EligibleProductType` was added

* `models.CheckAccessRequest` was added

* `models.TransactionPropertiesAzureCreditApplied` was added

* `models.ReservationOrder` was added

* `models.DeleteInvoiceSectionEligibilityCode` was added

* `models.SavingsPlanPurchasesPolicy` was added

* `models.SubscriptionPolicyProperties` was added

* `models.ProductDetails` was added

* `models.BillingAccountPolicy` was added

* `models.PartnerInitiateTransferRequest` was added

* `models.TransactionPropertiesMarketPrice` was added

* `models.EnrollmentDetailsIndirectRelationshipInfo` was added

* `models.TransactionPropertiesRefundTransactionDetails` was added

* `models.PaymentTermsEligibilityStatus` was added

* `models.PaymentMethodLink` was added

* `models.SavingsPlanUpdateRequest` was added

* `models.SavingsPlanSummaryCount` was added

* `models.BillingSubscriptionAliasListResult` was added

* `models.BillingProfilePolicy` was added

* `models.TaxIdentifierType` was added

* `models.RefundDetailsSummaryAmountRequested` was added

* `models.AppliedScopeType` was added

* `models.EnrollmentAuthLevelState` was added

* `models.BillingSubscriptionsAliases` was added

* `models.BillingProfilePropertiesCurrentPaymentTerm` was added

* `models.ProductStatus` was added

* `models.BillingRequestPropertiesCreatedBy` was added

* `models.MoveBillingSubscriptionEligibilityResult` was added

* `models.BillingAccountPropertiesRegistrationNumber` was added

* `models.InvoiceSectionWithCreateSubPermissionListResult` was added

* `models.RecipientTransferDetailsListResult` was added

* `models.SavingsPlanUpdateValidateRequest` was added

* `models.BillingRequests` was added

* `models.TransferDetailsListResult` was added

* `models.SupportLevel` was added

* `models.ExtendedStatusDefinitionProperties` was added

* `models.ReservationOrderList` was added

* `models.SavingsPlanOrderModel` was added

* `models.PartnerTransferDetailsListResult` was added

* `models.BillingProfileProperties` was added

* `models.SystemOverrides` was added

* `models.EnrollmentAccountProperties` was added

* `models.BillingAccountPatch` was added

* `models.RefundDetailsSummary` was added

* `models.AcceptTransferRequest` was added

* `models.BillingProfilePolicyPropertiesEnterpriseAgreementPolicies` was added

* `models.ReservationOrderBillingPlanInformation` was added

* `models.BillingProfilePolicyProperties` was added

* `models.Participant` was added

* `models.DeleteBillingProfileEligibilityResult` was added

* `models.AddressValidationResponse` was added

* `models.SavingsPlanModelList` was added

* `models.BillingSubscriptionStatusDetails` was added

* `models.SubscriptionPolicy` was added

* `models.DocumentDownloadRequest` was added

* `models.SkuName` was added

* `models.DepartmentProperties` was added

* `models.BillingSubscriptionStatus` was added

* `models.RenewalTermDetails` was added

* `models.ReservationExtendedStatusInfo` was added

* `models.DeleteInvoiceSectionEligibilityStatus` was added

* `models.TransferError` was added

* `models.ProductPatch` was added

* `models.TransactionSummary` was added

* `models.PaymentOnAccountAmount` was added

* `models.SupportedAccountType` was added

* `models.BillingRequestPropertiesReviewedBy` was added

* `models.SubscriptionEnrollmentDetails` was added

* `models.Payment` was added

* `models.BillingProfilePropertiesIndirectRelationshipInfo` was added

* `models.BillingAccountPropertiesEnrollmentDetails` was added

* `models.BillingRoleDefinitionProperties` was added

* `models.UtilizationAggregates` was added

* `models.InitiatorCustomerType` was added

* `models.TransactionProperties` was added

* `models.BillingPropertyProperties` was added

* `models.AvailableBalanceProperties` was added

* `models.PaymentMethodStatus` was added

* `models.SubscriptionWorkloadType` was added

* `models.PolicyType` was added

* `models.AssociatedTenants` was added

* `models.BillingRoleAssignmentProperties` was added

* `models.InvoicePropertiesRefundDetails` was added

* `models.ServiceDefinedResourceName` was added

* `models.BillingAccountPolicyProperties` was added

* `models.Principal` was added

* `models.CancellationReason` was added

* `models.InvoicePropertiesTaxAmount` was added

* `models.CustomerProperties` was added

* `models.SavingsPlanModelListResult` was added

* `models.BillingProfilePropertiesBillTo` was added

* `models.BillingProfilePropertiesSoldTo` was added

* `models.PatchModel` was added

* `models.RecipientTransferDetails` was added

* `models.BillingSubscriptionPatch` was added

* `models.BillingProfilePropertiesShipTo` was added

* `models.ReservationSplitProperties` was added

* `models.ProductProperties` was added

* `models.RefundTransactionDetailsAmountRequested` was added

* `models.Beneficiary` was added

* `models.MoveProductEligibilityResult` was added

* `models.ExtendedStatusInfo` was added

* `models.MoveProductErrorDetails` was added

* `models.PaymentMethodLogo` was added

* `models.AssociatedTenant` was added

* `models.AvailableBalancePropertiesAmount` was added

* `models.ReservationPaymentDetail` was added

* `models.MoveProductRequest` was added

* `models.ProductPropertiesReseller` was added

* `models.InitiateTransferRequest` was added

* `models.EnrollmentAccountOwnerViewCharges` was added

* `models.ReservationPurchaseRequest` was added

* `models.Transfers` was added

* `models.DetailedTransferStatus` was added

* `models.BillingAccountStatusReasonCode` was added

* `models.PaymentTermsEligibilityCode` was added

* `models.PaymentTerm` was added

* `models.SavingsPlanOrders` was added

* `models.AssociatedTenantListResult` was added

* `models.PrincipalType` was added

* `models.ReservationAppliedScopeProperties` was added

* `models.BillingSubscriptionMergeRequest` was added

* `models.InstanceFlexibility` was added

* `models.PaymentMethods` was added

* `models.SavingsPlans` was added

* `models.MoveBillingSubscriptionErrorDetails` was added

* `models.InvoicePropertiesRebillDetails` was added

* `models.InvoiceDocument` was added

* `models.TransactionKind` was added

* `models.SubscriptionBillingType` was added

* `models.RenewPropertiesResponse` was added

* `models.SpendingLimitStatus` was added

* `models.TransferStatus` was added

* `models.BillingManagementTenantState` was added

* `models.DeleteBillingProfileEligibilityStatus` was added

* `models.BillingAccountPropertiesSoldTo` was added

* `models.BillingSubscriptionOperationStatus` was added

* `models.CreditType` was added

* `models.SavingsPlanTerm` was added

* `models.InvoicePropertiesFreeAzureCreditApplied` was added

* `models.TransactionPropertiesConsumptionCommitmentDecremented` was added

* `models.AccountSubType` was added

* `models.CheckAccessResponse` was added

* `models.RefundTransactionDetailsAmountRefunded` was added

* `models.InvoicePropertiesTotalAmount` was added

* `models.InvoiceSectionStateReasonCode` was added

* `models.PartnerTransferDetails` was added

* `models.RenewProperties` was added

* `models.TransactionPropertiesEffectivePrice` was added

* `models.BillingAccountPolicyPropertiesEnterpriseAgreementPolicies` was added

* `models.InvoiceSectionProperties` was added

* `models.ReservationSwapProperties` was added

* `models.TaxIdentifier` was added

* `models.NextBillingCycleDetails` was added

* `models.ProductListResult` was added

* `models.InvoicePropertiesCreditAmount` was added

* `models.BillingRequestListResult` was added

* `models.MoveBillingSubscriptionRequest` was added

* `models.CommitmentGrain` was added

* `models.PaymentDetail` was added

* `models.RefundDetailsSummaryAmountRefunded` was added

* `models.RefundReasonCode` was added

* `models.ReservationBillingPlan` was added

* `models.BillingRequestType` was added

* `models.AgreementProperties` was added

* `models.Utilization` was added

* `models.ValidationResultProperties` was added

* `models.Price` was added

* `models.DeleteInvoiceSectionEligibilityDetail` was added

* `models.InvoicePropertiesBilledAmount` was added

* `models.EnterpriseAgreementPolicies` was added

* `models.BillingPropertyPropertiesEnrollmentDetails` was added

* `models.ProvisioningState` was added

* `models.BillingRequest` was added

* `models.PaymentOnAccount` was added

* `models.RefundTransactionDetails` was added

* `models.ReservationMergeProperties` was added

* `models.ValidateTransferResponse` was added

* `models.BillingRequestPropertiesLastUpdatedBy` was added

* `models.Cancellation` was added

* `models.FailedPayment` was added

* `models.SpendingLimitType` was added

* `models.RefundStatus` was added

* `models.ReservationStatusCode` was added

* `models.Sku` was added

* `models.BillingSubscriptionListResult` was added

* `models.SavingsPlanModel` was added

* `models.CustomerPolicyProperties` was added

* `models.InvoicePropertiesAmountDue` was added

* `models.InvoicePropertiesAzurePrepaymentApplied` was added

* `models.MoveValidationErrorCode` was added

* `models.Departments` was added

* `models.SavingsPlanUpdateRequestProperties` was added

* `models.BillingPropertyPropertiesSubscriptionServiceUsageAddress` was added

* `models.TransactionPropertiesTax` was added

* `models.TaxIdentifierStatus` was added

* `models.DocumentDownloadResult` was added

* `models.TransferDetails` was added

* `models.TransitionDetails` was added

* `models.ValidateTransferListResponse` was added

* `models.RecipientTransfers` was added

* `models.SpendingLimitDetails` was added

* `models.PaymentMethodLinksListResult` was added

#### `models.InvoiceSectionListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ReservationSummary` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `noBenefitCount()` was added
* `processingCount()` was added
* `warningCount()` was added

#### `models.AvailableBalance` was modified

* `systemData()` was added
* `tags()` was added
* `properties()` was added

#### `models.IndirectRelationshipInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AddressDetails` was modified

* `isValidAddress()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withIsValidAddress(java.lang.Boolean)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Reservations` was modified

* `listByReservationOrder(java.lang.String,java.lang.String)` was added
* `listByReservationOrder(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getByReservationOrderWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listByBillingProfile(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Float,java.lang.String,java.lang.String,java.lang.Float,com.azure.core.util.Context)` was added
* `updateByBillingAccount(java.lang.String,java.lang.String,java.lang.String,models.PatchModel,com.azure.core.util.Context)` was added
* `updateByBillingAccount(java.lang.String,java.lang.String,java.lang.String,models.PatchModel)` was added
* `getByReservationOrder(java.lang.String,java.lang.String,java.lang.String)` was added
* `listByBillingAccount(java.lang.String,java.lang.String,java.lang.String,java.lang.Float,java.lang.String,java.lang.String,java.lang.Float,com.azure.core.util.Context)` was added

#### `models.AzurePlan` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `productId()` was added
* `withSkuDescription(java.lang.String)` was added
* `withProductId(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BillingPermissions` was modified

* `listByDepartment(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `checkAccessByInvoiceSectionWithResponse(java.lang.String,java.lang.String,java.lang.String,models.CheckAccessRequest,com.azure.core.util.Context)` was added
* `listByCustomerAtBillingAccount(java.lang.String,java.lang.String)` was added
* `checkAccessByBillingProfileWithResponse(java.lang.String,java.lang.String,models.CheckAccessRequest,com.azure.core.util.Context)` was added
* `checkAccessByBillingAccountWithResponse(java.lang.String,models.CheckAccessRequest,com.azure.core.util.Context)` was added
* `checkAccessByCustomer(java.lang.String,java.lang.String,java.lang.String,models.CheckAccessRequest)` was added
* `checkAccessByDepartment(java.lang.String,java.lang.String,models.CheckAccessRequest)` was added
* `checkAccessByCustomerWithResponse(java.lang.String,java.lang.String,java.lang.String,models.CheckAccessRequest,com.azure.core.util.Context)` was added
* `checkAccessByBillingProfile(java.lang.String,java.lang.String,models.CheckAccessRequest)` was added
* `checkAccessByDepartmentWithResponse(java.lang.String,java.lang.String,models.CheckAccessRequest,com.azure.core.util.Context)` was added
* `checkAccessByInvoiceSection(java.lang.String,java.lang.String,java.lang.String,models.CheckAccessRequest)` was added
* `listByDepartment(java.lang.String,java.lang.String)` was added
* `checkAccessByBillingAccount(java.lang.String,models.CheckAccessRequest)` was added
* `listByCustomer(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listByInvoiceSection(java.lang.String,java.lang.String,java.lang.String)` was added
* `checkAccessByEnrollmentAccount(java.lang.String,java.lang.String,models.CheckAccessRequest)` was added
* `listByCustomer(java.lang.String,java.lang.String,java.lang.String)` was added
* `listByCustomerAtBillingAccount(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `checkAccessByEnrollmentAccountWithResponse(java.lang.String,java.lang.String,models.CheckAccessRequest,com.azure.core.util.Context)` was added
* `listByEnrollmentAccount(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listByInvoiceSection(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listByEnrollmentAccount(java.lang.String,java.lang.String)` was added

#### `models.BillingSubscription` was modified

* `termEndDate()` was added
* `productCategory()` was added
* `renewalTermDetails()` was added
* `quantity()` was added
* `termStartDate()` was added
* `offerId()` was added
* `systemData()` was added
* `beneficiary()` was added
* `customerName()` was added
* `nextBillingCycleDetails()` was added
* `resourceUri()` was added
* `beneficiaryTenantId()` was added
* `enrollmentAccountId()` was added
* `autoRenew()` was added
* `suspensionReasonDetails()` was added
* `provisioningState()` was added
* `enrollmentAccountStartDate()` was added
* `billingProfileName()` was added
* `enrollmentAccountDisplayName()` was added
* `productTypeId()` was added
* `provisioningTenantId()` was added
* `productType()` was added
* `systemOverrides()` was added
* `status()` was added
* `termDuration()` was added
* `subscriptionEnrollmentAccountStatus()` was added
* `operationStatus()` was added
* `purchaseDate()` was added
* `invoiceSectionName()` was added
* `billingFrequency()` was added
* `billingPolicies()` was added
* `tags()` was added
* `consumptionCostCenter()` was added

#### `models.ReservationsListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Product` was modified

* `systemData()` was added
* `tags()` was added
* `properties()` was added

#### `models.BillingRoleDefinitionListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Customer` was modified

* `properties()` was added
* `systemData()` was added
* `tags()` was added

#### `models.BillingProfileListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Policies` was modified

* `getByCustomerAtBillingAccount(java.lang.String,java.lang.String)` was added
* `createOrUpdateByCustomer(java.lang.String,java.lang.String,java.lang.String,fluent.models.CustomerPolicyInner,com.azure.core.util.Context)` was added
* `getBySubscription()` was added
* `createOrUpdateByBillingProfile(java.lang.String,java.lang.String,fluent.models.BillingProfilePolicyInner,com.azure.core.util.Context)` was added
* `getByCustomerAtBillingAccountWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `createOrUpdateByBillingAccount(java.lang.String,fluent.models.BillingAccountPolicyInner,com.azure.core.util.Context)` was added
* `createOrUpdateByCustomerAtBillingAccount(java.lang.String,java.lang.String,fluent.models.CustomerPolicyInner)` was added
* `createOrUpdateByCustomer(java.lang.String,java.lang.String,java.lang.String,fluent.models.CustomerPolicyInner)` was added
* `getByBillingAccount(java.lang.String)` was added
* `getByBillingAccountWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `getBySubscriptionWithResponse(com.azure.core.util.Context)` was added
* `getByCustomerWithResponse(java.lang.String,java.lang.String,java.lang.String,models.ServiceDefinedResourceName,com.azure.core.util.Context)` was added
* `getByCustomer(java.lang.String,java.lang.String,java.lang.String,models.ServiceDefinedResourceName)` was added
* `createOrUpdateByBillingAccount(java.lang.String,fluent.models.BillingAccountPolicyInner)` was added
* `createOrUpdateByCustomerAtBillingAccount(java.lang.String,java.lang.String,fluent.models.CustomerPolicyInner,com.azure.core.util.Context)` was added
* `createOrUpdateByBillingProfile(java.lang.String,java.lang.String,fluent.models.BillingProfilePolicyInner)` was added

#### `models.BillingSubscriptions` was modified

* `update(java.lang.String,java.lang.String,models.BillingSubscriptionPatch,com.azure.core.util.Context)` was added
* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `move(java.lang.String,java.lang.String,models.MoveBillingSubscriptionRequest)` was added
* `getByBillingProfileWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listByBillingProfile(java.lang.String,java.lang.String,java.lang.Boolean,java.lang.String,java.lang.String,java.lang.String,java.lang.Long,java.lang.Long,java.lang.Boolean,java.lang.String,com.azure.core.util.Context)` was added
* `merge(java.lang.String,java.lang.String,models.BillingSubscriptionMergeRequest,com.azure.core.util.Context)` was added
* `validateMoveEligibilityWithResponse(java.lang.String,java.lang.String,models.MoveBillingSubscriptionRequest,com.azure.core.util.Context)` was added
* `listByCustomer(java.lang.String,java.lang.String,java.lang.String)` was added
* `getByBillingProfile(java.lang.String,java.lang.String,java.lang.String)` was added
* `update(java.lang.String,java.lang.String,models.BillingSubscriptionPatch)` was added
* `listByBillingAccount(java.lang.String,java.lang.Boolean,java.lang.Boolean,java.lang.Boolean,java.lang.String,java.lang.String,java.lang.String,java.lang.Long,java.lang.Long,java.lang.Boolean,java.lang.String,com.azure.core.util.Context)` was added
* `listByCustomerAtBillingAccount(java.lang.String,java.lang.String,java.lang.Boolean,java.lang.String,java.lang.String,java.lang.String,java.lang.Long,java.lang.Long,java.lang.Boolean,java.lang.String,com.azure.core.util.Context)` was added
* `move(java.lang.String,java.lang.String,models.MoveBillingSubscriptionRequest,com.azure.core.util.Context)` was added
* `split(java.lang.String,java.lang.String,models.BillingSubscriptionSplitRequest,com.azure.core.util.Context)` was added
* `deleteByResourceGroup(java.lang.String,java.lang.String)` was added
* `listByEnrollmentAccount(java.lang.String,java.lang.String)` was added
* `listByCustomerAtBillingAccount(java.lang.String,java.lang.String)` was added
* `listByInvoiceSection(java.lang.String,java.lang.String,java.lang.String,java.lang.Boolean,java.lang.String,java.lang.String,java.lang.String,java.lang.Long,java.lang.Long,java.lang.Boolean,java.lang.String,com.azure.core.util.Context)` was added
* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listByCustomer(java.lang.String,java.lang.String,java.lang.String,java.lang.Boolean,java.lang.String,java.lang.String,java.lang.String,java.lang.Long,java.lang.Long,java.lang.Boolean,java.lang.String,com.azure.core.util.Context)` was added
* `validateMoveEligibility(java.lang.String,java.lang.String,models.MoveBillingSubscriptionRequest)` was added
* `cancel(java.lang.String,java.lang.String,models.CancelSubscriptionRequest,com.azure.core.util.Context)` was added
* `get(java.lang.String,java.lang.String)` was added
* `listByEnrollmentAccount(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Long,java.lang.Long,java.lang.Boolean,java.lang.String,com.azure.core.util.Context)` was added
* `cancel(java.lang.String,java.lang.String,models.CancelSubscriptionRequest)` was added
* `split(java.lang.String,java.lang.String,models.BillingSubscriptionSplitRequest)` was added
* `merge(java.lang.String,java.lang.String,models.BillingSubscriptionMergeRequest)` was added

#### `models.CustomerListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CustomerPolicy` was modified

* `tags()` was added
* `systemData()` was added
* `properties()` was added

#### `models.BillingProfiles` was modified

* `listByBillingAccount(java.lang.String,java.lang.Boolean,java.lang.String,java.lang.String,java.lang.Long,java.lang.Long,java.lang.Boolean,java.lang.String,com.azure.core.util.Context)` was added
* `getWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `deleteByResourceGroup(java.lang.String,java.lang.String)` was added
* `validateDeleteEligibility(java.lang.String,java.lang.String)` was added
* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `validateDeleteEligibilityWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.AvailableBalances` was modified

* `getByBillingProfile(java.lang.String,java.lang.String)` was added
* `getByBillingAccount(java.lang.String)` was added
* `getByBillingAccountWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `getByBillingProfileWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.BillingRoleDefinition` was modified

* `tags()` was added
* `properties()` was added
* `systemData()` was added

#### `models.InvoiceSections` was modified

* `listByBillingProfile(java.lang.String,java.lang.String,java.lang.Boolean,java.lang.String,java.lang.String,java.lang.Long,java.lang.Long,java.lang.Boolean,java.lang.String,com.azure.core.util.Context)` was added
* `delete(java.lang.String,java.lang.String,java.lang.String)` was added
* `validateDeleteEligibilityWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `delete(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `validateDeleteEligibility(java.lang.String,java.lang.String,java.lang.String)` was added

#### `models.EnrollmentAccountListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Products` was modified

* `move(java.lang.String,java.lang.String,models.MoveProductRequest)` was added
* `validateMoveEligibilityWithResponse(java.lang.String,java.lang.String,models.MoveProductRequest,com.azure.core.util.Context)` was added
* `updateWithResponse(java.lang.String,java.lang.String,models.ProductPatch,com.azure.core.util.Context)` was added
* `move(java.lang.String,java.lang.String,models.MoveProductRequest,com.azure.core.util.Context)` was added
* `update(java.lang.String,java.lang.String,models.ProductPatch)` was added
* `listByCustomer(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Long,java.lang.Long,java.lang.Boolean,java.lang.String,com.azure.core.util.Context)` was added
* `listByBillingAccount(java.lang.String,java.lang.String,java.lang.String,java.lang.Long,java.lang.Long,java.lang.Boolean,java.lang.String,com.azure.core.util.Context)` was added
* `listByInvoiceSection(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Long,java.lang.Long,java.lang.Boolean,java.lang.String,com.azure.core.util.Context)` was added
* `validateMoveEligibility(java.lang.String,java.lang.String,models.MoveProductRequest)` was added
* `listByBillingProfile(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Long,java.lang.Long,java.lang.Boolean,java.lang.String,com.azure.core.util.Context)` was added

#### `models.TransactionListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.EnrollmentAccount` was modified

* `id()` was added
* `name()` was added
* `innerModel()` was added
* `type()` was added
* `tags()` was added
* `properties()` was added
* `systemData()` was added

#### `models.BillingRoleDefinitions` was modified

* `listByDepartment(java.lang.String,java.lang.String)` was added
* `listByCustomer(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getByCustomer(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added
* `listByEnrollmentAccount(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listByCustomer(java.lang.String,java.lang.String,java.lang.String)` was added
* `getByDepartment(java.lang.String,java.lang.String,java.lang.String)` was added
* `getByEnrollmentAccount(java.lang.String,java.lang.String,java.lang.String)` was added
* `getByCustomerWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getByDepartmentWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getByEnrollmentAccountWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listByEnrollmentAccount(java.lang.String,java.lang.String)` was added
* `listByDepartment(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.EnrollmentAccounts` was modified

* `getByDepartmentWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getByDepartment(java.lang.String,java.lang.String,java.lang.String)` was added
* `listByDepartment(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Long,java.lang.Long,java.lang.Boolean,java.lang.String,com.azure.core.util.Context)` was added
* `getWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listByBillingAccount(java.lang.String)` was added
* `listByDepartment(java.lang.String,java.lang.String)` was added
* `listByBillingAccount(java.lang.String,java.lang.String,java.lang.String,java.lang.Long,java.lang.Long,java.lang.Boolean,java.lang.String,com.azure.core.util.Context)` was added
* `get(java.lang.String,java.lang.String)` was added

#### `models.BillingAccounts` was modified

* `addPaymentTerms(java.lang.String,java.util.List)` was added
* `addPaymentTerms(java.lang.String,java.util.List,com.azure.core.util.Context)` was added
* `cancelPaymentTerms(java.lang.String,java.time.OffsetDateTime,com.azure.core.util.Context)` was added
* `validatePaymentTermsWithResponse(java.lang.String,java.util.List,com.azure.core.util.Context)` was added
* `confirmTransition(java.lang.String)` was added
* `list(java.lang.Boolean,java.lang.Boolean,java.lang.Boolean,java.lang.Boolean,java.lang.Boolean,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Long,java.lang.Long,java.lang.String,com.azure.core.util.Context)` was added
* `update(java.lang.String,models.BillingAccountPatch,com.azure.core.util.Context)` was added
* `cancelPaymentTerms(java.lang.String,java.time.OffsetDateTime)` was added
* `update(java.lang.String,models.BillingAccountPatch)` was added
* `validatePaymentTerms(java.lang.String,java.util.List)` was added
* `confirmTransitionWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `getWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `listInvoiceSectionsByCreateSubscriptionPermission(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.BillingRoleAssignments` was modified

* `createByBillingProfile(java.lang.String,java.lang.String,models.BillingRoleAssignmentProperties)` was added
* `createOrUpdateByEnrollmentAccount(java.lang.String,java.lang.String,java.lang.String,fluent.models.BillingRoleAssignmentInner,com.azure.core.util.Context)` was added
* `getByEnrollmentAccount(java.lang.String,java.lang.String,java.lang.String)` was added
* `resolveByInvoiceSection(java.lang.String,java.lang.String,java.lang.String,java.lang.Boolean,java.lang.String,com.azure.core.util.Context)` was added
* `createOrUpdateByDepartment(java.lang.String,java.lang.String,java.lang.String,fluent.models.BillingRoleAssignmentInner)` was added
* `resolveByBillingAccount(java.lang.String,java.lang.Boolean,java.lang.String,com.azure.core.util.Context)` was added
* `listByCustomer(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Long,java.lang.Long,com.azure.core.util.Context)` was added
* `createOrUpdateByEnrollmentAccount(java.lang.String,java.lang.String,java.lang.String,fluent.models.BillingRoleAssignmentInner)` was added
* `createByCustomer(java.lang.String,java.lang.String,java.lang.String,models.BillingRoleAssignmentProperties)` was added
* `createByCustomer(java.lang.String,java.lang.String,java.lang.String,models.BillingRoleAssignmentProperties,com.azure.core.util.Context)` was added
* `resolveByCustomer(java.lang.String,java.lang.String,java.lang.String,java.lang.Boolean,java.lang.String,com.azure.core.util.Context)` was added
* `resolveByBillingAccount(java.lang.String)` was added
* `deleteByCustomerWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `deleteByCustomer(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added
* `deleteByEnrollmentAccount(java.lang.String,java.lang.String,java.lang.String)` was added
* `deleteByDepartmentWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `deleteByEnrollmentAccountWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `createByInvoiceSection(java.lang.String,java.lang.String,java.lang.String,models.BillingRoleAssignmentProperties)` was added
* `resolveByBillingProfile(java.lang.String,java.lang.String,java.lang.Boolean,java.lang.String,com.azure.core.util.Context)` was added
* `resolveByBillingProfile(java.lang.String,java.lang.String)` was added
* `listByBillingAccount(java.lang.String,java.lang.String,java.lang.Long,java.lang.Long,com.azure.core.util.Context)` was added
* `createByInvoiceSection(java.lang.String,java.lang.String,java.lang.String,models.BillingRoleAssignmentProperties,com.azure.core.util.Context)` was added
* `getByDepartmentWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listByDepartment(java.lang.String,java.lang.String)` was added
* `listByEnrollmentAccount(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listByCustomer(java.lang.String,java.lang.String,java.lang.String)` was added
* `listByDepartment(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getByCustomerWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `createOrUpdateByDepartment(java.lang.String,java.lang.String,java.lang.String,fluent.models.BillingRoleAssignmentInner,com.azure.core.util.Context)` was added
* `listByInvoiceSection(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Long,java.lang.Long,com.azure.core.util.Context)` was added
* `createOrUpdateByBillingAccount(java.lang.String,java.lang.String,fluent.models.BillingRoleAssignmentInner)` was added
* `listByBillingProfile(java.lang.String,java.lang.String,java.lang.String,java.lang.Long,java.lang.Long,com.azure.core.util.Context)` was added
* `createByBillingAccount(java.lang.String,models.BillingRoleAssignmentProperties,com.azure.core.util.Context)` was added
* `getByEnrollmentAccountWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `deleteByDepartment(java.lang.String,java.lang.String,java.lang.String)` was added
* `listByEnrollmentAccount(java.lang.String,java.lang.String)` was added
* `getByDepartment(java.lang.String,java.lang.String,java.lang.String)` was added
* `resolveByCustomer(java.lang.String,java.lang.String,java.lang.String)` was added
* `createOrUpdateByBillingAccount(java.lang.String,java.lang.String,fluent.models.BillingRoleAssignmentInner,com.azure.core.util.Context)` was added
* `getByCustomer(java.lang.String,java.lang.String,java.lang.String,java.lang.String)` was added
* `createByBillingAccount(java.lang.String,models.BillingRoleAssignmentProperties)` was added
* `createByBillingProfile(java.lang.String,java.lang.String,models.BillingRoleAssignmentProperties,com.azure.core.util.Context)` was added
* `resolveByInvoiceSection(java.lang.String,java.lang.String,java.lang.String)` was added

#### `models.ReservationSkuProperty` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.InvoiceSection` was modified

* `systemData()` was added
* `properties()` was added

#### `models.Invoices` was modified

* `listByBillingAccount(java.lang.String,java.time.LocalDate,java.time.LocalDate,java.lang.String,java.lang.String,java.lang.Long,java.lang.Long,java.lang.Boolean,java.lang.String,com.azure.core.util.Context)` was added
* `downloadByBillingAccount(java.lang.String,java.lang.String)` was added
* `getWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `getByBillingAccount(java.lang.String,java.lang.String)` was added
* `getByBillingSubscriptionWithResponse(java.lang.String,com.azure.core.util.Context)` was added
* `downloadDocumentsByBillingSubscription(java.util.List,com.azure.core.util.Context)` was added
* `listByBillingProfile(java.lang.String,java.lang.String)` was added
* `listByBillingSubscription()` was added
* `downloadByBillingSubscription(java.lang.String)` was added
* `downloadDocumentsByBillingAccount(java.lang.String,java.util.List)` was added
* `getByBillingSubscription(java.lang.String)` was added
* `downloadSummaryByBillingAccount(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listByBillingAccount(java.lang.String)` was added
* `downloadByBillingSubscription(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listByBillingProfile(java.lang.String,java.lang.String,java.time.LocalDate,java.time.LocalDate,java.lang.String,java.lang.String,java.lang.Long,java.lang.Long,java.lang.Boolean,java.lang.String,com.azure.core.util.Context)` was added
* `downloadDocumentsByBillingAccount(java.lang.String,java.util.List,com.azure.core.util.Context)` was added
* `downloadDocumentsByBillingSubscription(java.util.List)` was added
* `downloadSummaryByBillingAccount(java.lang.String,java.lang.String)` was added
* `getByBillingAccountWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `downloadByBillingAccount(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `amend(java.lang.String,java.lang.String)` was added
* `listByBillingSubscription(java.time.LocalDate,java.time.LocalDate,java.lang.String,java.lang.String,java.lang.Long,java.lang.Long,java.lang.Boolean,java.lang.String,com.azure.core.util.Context)` was added
* `get(java.lang.String)` was added
* `amend(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `BillingManager` was modified

* `departments()` was added
* `billingSubscriptionsAliases()` was added
* `savingsPlans()` was added
* `partnerTransfers()` was added
* `transfers()` was added
* `paymentMethods()` was added
* `recipientTransfers()` was added
* `reservationOrders()` was added
* `billingRequests()` was added
* `savingsPlanOrders()` was added
* `associatedTenants()` was added

#### `models.BillingRoleAssignment` was modified

* `properties()` was added
* `systemData()` was added
* `tags()` was added

#### `models.Agreement` was modified

* `properties()` was added
* `tags()` was added
* `systemData()` was added

#### `models.Agreements` was modified

* `getWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.ReservationUtilizationAggregates` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Department` was modified

* `innerModel()` was added
* `name()` was added
* `tags()` was added
* `type()` was added
* `id()` was added
* `systemData()` was added
* `properties()` was added

#### `models.Transactions` was modified

* `listByCustomer(java.lang.String,java.lang.String,java.lang.String,java.time.LocalDate,java.time.LocalDate,models.TransactionType)` was added
* `transactionsDownloadByInvoice(java.lang.String,java.lang.String)` was added
* `listByInvoice(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Long,java.lang.Long,java.lang.Boolean,java.lang.String,com.azure.core.util.Context)` was added
* `listByInvoiceSection(java.lang.String,java.lang.String,java.lang.String,java.time.LocalDate,java.time.LocalDate,models.TransactionType)` was added
* `transactionsDownloadByInvoice(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listByBillingProfile(java.lang.String,java.lang.String,java.time.LocalDate,java.time.LocalDate,models.TransactionType)` was added
* `getTransactionSummaryByInvoiceWithResponse(java.lang.String,java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `listByCustomer(java.lang.String,java.lang.String,java.lang.String,java.time.LocalDate,java.time.LocalDate,models.TransactionType,java.lang.String,java.lang.String,java.lang.Long,java.lang.Long,java.lang.Boolean,java.lang.String,com.azure.core.util.Context)` was added
* `listByInvoiceSection(java.lang.String,java.lang.String,java.lang.String,java.time.LocalDate,java.time.LocalDate,models.TransactionType,java.lang.String,java.lang.String,java.lang.Long,java.lang.Long,java.lang.Boolean,java.lang.String,com.azure.core.util.Context)` was added
* `getTransactionSummaryByInvoice(java.lang.String,java.lang.String)` was added
* `listByBillingProfile(java.lang.String,java.lang.String,java.time.LocalDate,java.time.LocalDate,models.TransactionType,java.lang.String,java.lang.String,java.lang.Long,java.lang.Long,java.lang.Boolean,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Customers` was modified

* `listByBillingAccount(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Long,java.lang.Long,java.lang.Boolean,java.lang.String,com.azure.core.util.Context)` was added
* `getByBillingAccountWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added
* `getByBillingAccount(java.lang.String,java.lang.String)` was added
* `get(java.lang.String,java.lang.String,java.lang.String)` was added
* `listByBillingProfile(java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.String,java.lang.Long,java.lang.Long,java.lang.Boolean,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Amount` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.BillingProfile` was modified

* `properties()` was added
* `systemData()` was added

#### `models.OperationDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BillingProfileInfo` was modified

* `billingAccountId()` was added
* `billingProfileSystemId()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `withBillingAccountId(java.lang.String)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `withBillingProfileSystemId(java.lang.String)` was added

#### `models.BillingRoleAssignmentListResult` was modified

* `innerModel()` was added

#### `models.BillingAccountListResult` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.InvoiceListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.RebillDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BillingAccount` was modified

* `systemData()` was added
* `properties()` was added
* `tags()` was added

#### `models.BillingProperty` was modified

* `systemData()` was added
* `tags()` was added
* `properties()` was added

#### `models.Invoice` was modified

* `systemData()` was added
* `properties()` was added
* `tags()` was added

#### `models.BillingProperties` was modified

* `getWithResponse(java.lang.Boolean,java.lang.Boolean,com.azure.core.util.Context)` was added

#### `models.Transaction` was modified

* `systemData()` was added
* `tags()` was added
* `properties()` was added

#### `models.Reseller` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AgreementListResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.Reservation` was modified

* `etag()` was added
* `renewProperties()` was added
* `trend()` was added
* `expiryDateTime()` was added
* `billingPlan()` was added
* `archived()` was added
* `productCode()` was added
* `purchaseDate()` was added
* `tags()` was added
* `benefitStartTime()` was added
* `swapProperties()` was added
* `purchaseDateTime()` was added
* `aggregates()` was added
* `mergeProperties()` was added
* `instanceFlexibility()` was added
* `renewDestination()` was added
* `appliedScopeProperties()` was added
* `billingScopeId()` was added
* `capabilities()` was added
* `extendedStatusInfo()` was added
* `lastUpdatedDateTime()` was added
* `splitProperties()` was added
* `reviewDateTime()` was added
* `systemData()` was added

## 1.0.0-beta.3 (2023-01-12)

- Azure Resource Manager Billing client library for Java. This package contains Microsoft Azure SDK for Billing Management SDK. Billing client provides access to billing resources for Azure subscriptions. Package tag package-2020-05. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.BillingSubscriptionsMoveHeaders` was removed

* `models.InvoicesDownloadBillingSubscriptionInvoiceResponse` was removed

* `models.InvoicesDownloadMultipleBillingProfileInvoicesResponse` was removed

* `models.InvoicesDownloadMultipleBillingSubscriptionInvoicesHeaders` was removed

* `models.InvoicesDownloadInvoiceResponse` was removed

* `models.InvoiceSectionsCreateOrUpdateHeaders` was removed

* `models.BillingProfilesCreateOrUpdateResponse` was removed

* `models.InvoicesDownloadMultipleBillingProfileInvoicesHeaders` was removed

* `models.InvoicesDownloadBillingSubscriptionInvoiceHeaders` was removed

* `models.BillingSubscriptionsMoveResponse` was removed

* `models.InvoiceSectionsCreateOrUpdateResponse` was removed

* `models.BillingProfilesCreateOrUpdateHeaders` was removed

* `models.InvoicesDownloadInvoiceHeaders` was removed

* `models.InvoicesDownloadMultipleBillingSubscriptionInvoicesResponse` was removed

### Features Added

* `models.BillingProfileInfo` was added

#### `models.Agreement` was modified

* `billingProfileInfo()` was added

#### `models.InvoiceListResult` was modified

* `totalCount()` was added

#### `BillingManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added

#### `models.BillingProfileListResult` was modified

* `totalCount()` was added

#### `BillingManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

## 1.0.0-beta.2 (2021-10-09)

- Azure Resource Manager Billing client library for Java. This package contains Microsoft Azure SDK for Billing Management SDK. Billing client provides access to billing resources for Azure subscriptions. Package tag package-2020-05. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.Instruction` was modified

* `java.lang.Float amount()` -> `float amount()`

### Features Added

#### `models.ProductsListResult` was modified

* `totalCount()` was added

#### `models.TransactionListResult` was modified

* `totalCount()` was added

#### `models.BillingSubscription` was modified

* `suspensionReasons()` was added

#### `BillingManager$Configurable` was modified

* `withScope(java.lang.String)` was added

#### `models.InvoiceSection` was modified

* `tags()` was added

## 1.0.0-beta.1 (2021-04-13)

- Azure Resource Manager Billing client library for Java. This package contains Microsoft Azure SDK for Billing Management SDK. Billing client provides access to billing resources for Azure subscriptions. Package tag package-2020-05. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
