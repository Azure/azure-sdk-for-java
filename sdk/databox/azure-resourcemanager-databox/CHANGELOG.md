# Release History

## 1.0.0-beta.4 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0-beta.3 (2023-05-23)

- Azure Resource Manager DataBox client library for Java. This package contains Microsoft Azure SDK for DataBox Management SDK.  Package tag package-2022-12. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.StageName` was modified

* `valueOf(java.lang.String)` was removed
* `toString()` was removed
* `models.StageName[] values()` -> `java.util.Collection values()`

#### `models.NotificationStageName` was modified

* `toString()` was removed
* `models.NotificationStageName[] values()` -> `java.util.Collection values()`
* `valueOf(java.lang.String)` was removed

#### `models.CopyStatus` was modified

* `toString()` was removed
* `valueOf(java.lang.String)` was removed
* `models.CopyStatus[] values()` -> `java.util.Collection values()`

### Features Added

* `models.ReverseTransportPreferenceEditStatus` was added

* `models.DatacenterAddressType` was added

* `models.DataBoxCustomerDiskCopyLogDetails` was added

* `models.DataBoxDiskGranularCopyProgress` was added

* `models.CustomerDiskJobSecrets` was added

* `models.DataCenterCode` was added

* `models.DataBoxCustomerDiskCopyProgress` was added

* `models.PackageCarrierInfo` was added

* `models.PackageCarrierDetails` was added

* `models.ImportDiskDetails` was added

* `models.ExportDiskDetails` was added

* `models.MarkDevicesShippedRequest` was added

* `models.DatacenterAddressResponse` was added

* `models.ContactInfo` was added

* `models.DatacenterAddressLocationResponse` was added

* `models.DeviceErasureDetails` was added

* `models.HardwareEncryption` was added

* `models.DataBoxCustomerDiskJobDetails` was added

* `models.DatacenterAddressInstructionResponse` was added

* `models.GranularCopyProgress` was added

* `models.DatacenterAddressRequest` was added

* `models.ReverseShippingDetails` was added

* `models.GranularCopyLogDetails` was added

* `models.DataBoxDiskGranularCopyLogDetails` was added

* `models.ReverseShippingDetailsEditStatus` was added

#### `models.DataBoxDiskCopyProgress` was modified

* `actions()` was added
* `error()` was added

#### `models.UpdateJobDetails` was modified

* `returnToCustomerPackageDetails()` was added
* `preferences()` was added
* `withReturnToCustomerPackageDetails(models.PackageCarrierDetails)` was added
* `withReverseShippingDetails(models.ReverseShippingDetails)` was added
* `withPreferences(models.Preferences)` was added
* `reverseShippingDetails()` was added

#### `models.DataBoxHeavyJobDetails` was modified

* `withReverseShippingDetails(models.ReverseShippingDetails)` was added

#### `models.JobDetails` was modified

* `deviceErasureDetails()` was added
* `withReverseShippingDetails(models.ReverseShippingDetails)` was added
* `dataCenterCode()` was added
* `reverseShippingDetails()` was added
* `datacenterAddress()` was added

#### `models.DataImportDetails` was modified

* `logCollectionLevel()` was added
* `withLogCollectionLevel(models.LogCollectionLevel)` was added

#### `models.DataBoxJobDetails` was modified

* `withReverseShippingDetails(models.ReverseShippingDetails)` was added

#### `models.DataBoxDiskJobDetails` was modified

* `withReverseShippingDetails(models.ReverseShippingDetails)` was added
* `granularCopyLogDetails()` was added
* `granularCopyProgress()` was added

#### `models.Preferences` was modified

* `reverseTransportPreferences()` was added
* `withReverseTransportPreferences(models.TransportPreferences)` was added
* `withStorageAccountAccessTierPreferences(java.util.List)` was added
* `storageAccountAccessTierPreferences()` was added

#### `models.RegionConfigurationRequest` was modified

* `withDatacenterAddressRequest(models.DatacenterAddressRequest)` was added
* `datacenterAddressRequest()` was added

#### `models.EncryptionPreferences` was modified

* `withHardwareEncryption(models.HardwareEncryption)` was added
* `hardwareEncryption()` was added

#### `models.TransportPreferences` was modified

* `isUpdated()` was added

#### `models.ShippingAddress` was modified

* `skipAddressValidation()` was added
* `withSkipAddressValidation(java.lang.Boolean)` was added
* `withTaxIdentificationNumber(java.lang.String)` was added
* `taxIdentificationNumber()` was added

#### `models.RegionConfigurationResponse` was modified

* `datacenterAddressResponse()` was added

#### `models.MitigateJobRequest` was modified

* `serialNumberCustomerResolutionMap()` was added
* `withSerialNumberCustomerResolutionMap(java.util.Map)` was added

#### `models.JobResource` was modified

* `markDevicesShippedWithResponse(models.MarkDevicesShippedRequest,com.azure.core.util.Context)` was added
* `markDevicesShipped(models.MarkDevicesShippedRequest)` was added
* `reverseShippingDetailsUpdate()` was added
* `reverseTransportPreferenceUpdate()` was added

#### `models.Jobs` was modified

* `markDevicesShippedWithResponse(java.lang.String,java.lang.String,models.MarkDevicesShippedRequest,com.azure.core.util.Context)` was added
* `markDevicesShipped(java.lang.String,java.lang.String,models.MarkDevicesShippedRequest)` was added

#### `models.SkuInformation` was modified

* `countriesWithinCommerceBoundary()` was added

#### `models.CopyProgress` was modified

* `actions()` was added
* `error()` was added

## 1.0.0-beta.2 (2023-01-13)

- Azure Resource Manager DataBox client library for Java. This package contains Microsoft Azure SDK for DataBox Management SDK.  Package tag package-2021-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.JobResource$DefinitionStages` was modified

* `withTransferType(models.TransferType)` was removed in stage 3
* `withSku(models.Sku)` was removed in stage 4

#### `models.AddressValidationOutput` was modified

* `alternateAddresses()` was removed
* `error()` was removed
* `validationStatus()` was removed

### Features Added

#### `models.AddressValidationOutput` was modified

* `properties()` was added

#### `DataBoxManager$Configurable` was modified

* `withRetryOptions(com.azure.core.http.policy.RetryOptions)` was added
* `withScope(java.lang.String)` was added

#### `DataBoxManager` was modified

* `authenticate(com.azure.core.http.HttpPipeline,com.azure.core.management.profile.AzureProfile)` was added

#### `models.JobResource` was modified

* `resourceGroupName()` was added

## 1.0.0-beta.1 (2021-04-14)

- Azure Resource Manager DataBox client library for Java. This package contains Microsoft Azure SDK for DataBox Management SDK.  Package tag package-2021-03. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
