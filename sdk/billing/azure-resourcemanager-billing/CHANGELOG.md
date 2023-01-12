# Release History

## 1.0.0-beta.4 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

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
