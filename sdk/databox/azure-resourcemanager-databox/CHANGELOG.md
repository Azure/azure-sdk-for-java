# Release History

## 1.2.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.1.0 (2025-03-27)

- Azure Resource Manager DataBox client library for Java. This package contains Microsoft Azure SDK for DataBox Management SDK.  Package tag package-2025-02. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

* `models.DeviceCapabilityDetails` was added

* `models.JobDelayDetails` was added

* `models.DeviceCapabilityResponse` was added

* `models.DeviceCapabilityRequest` was added

* `models.PortalDelayErrorCode` was added

* `models.DelayNotificationStatus` was added

* `models.ModelName` was added

#### `models.TransportAvailabilityRequest` was modified

* `model()` was added
* `withModel(models.ModelName)` was added

#### `models.SkuAvailabilityValidationRequest` was modified

* `model()` was added
* `withModel(models.ModelName)` was added

#### `models.DiskScheduleAvailabilityRequest` was modified

* `withModel(models.ModelName)` was added

#### `models.ScheduleAvailabilityRequest` was modified

* `model()` was added
* `withModel(models.ModelName)` was added

#### `models.HeavyScheduleAvailabilityRequest` was modified

* `withModel(models.ModelName)` was added

#### `models.RegionConfigurationRequest` was modified

* `withDeviceCapabilityRequest(models.DeviceCapabilityRequest)` was added
* `deviceCapabilityRequest()` was added

#### `models.SkuCapacity` was modified

* `individualSkuUsable()` was added

#### `models.JobStages` was modified

* `delayInformation()` was added

#### `models.DataTransferDetailsValidationRequest` was modified

* `model()` was added
* `withModel(models.ModelName)` was added

#### `models.DataBoxScheduleAvailabilityRequest` was modified

* `withModel(models.ModelName)` was added

#### `models.RegionConfigurationResponse` was modified

* `deviceCapabilityResponse()` was added

#### `models.JobResource` was modified

* `allDevicesLost()` was added
* `delayedStage()` was added

#### `models.ValidateAddress` was modified

* `withModel(models.ModelName)` was added
* `model()` was added

#### `models.DatacenterAddressRequest` was modified

* `withModel(models.ModelName)` was added
* `model()` was added

#### `models.CreateOrderLimitForSubscriptionValidationRequest` was modified

* `withModel(models.ModelName)` was added
* `model()` was added

#### `models.Sku` was modified

* `withModel(models.ModelName)` was added
* `model()` was added

#### `models.PreferencesValidationRequest` was modified

* `model()` was added
* `withModel(models.ModelName)` was added

## 1.0.0 (2024-12-24)

- Azure Resource Manager DataBox client library for Java. This package contains Microsoft Azure SDK for DataBox Management SDK.  Package tag package-2022-12. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.DataboxJobSecrets` was modified

* `error()` was removed
* `dcAccessSecurityCode()` was removed

#### `models.DataBoxHeavyJobDetails` was modified

* `actions()` was removed
* `datacenterAddress()` was removed
* `reverseShipmentLabelSasKey()` was removed
* `deviceErasureDetails()` was removed
* `jobStages()` was removed
* `deliveryPackage()` was removed
* `returnPackage()` was removed
* `lastMitigationActionOnJob()` was removed
* `chainOfCustodySasKey()` was removed
* `dataCenterCode()` was removed
* `copyLogDetails()` was removed

#### `models.DataBoxDiskJobSecrets` was modified

* `dcAccessSecurityCode()` was removed
* `error()` was removed

#### `models.PreferencesValidationResponseProperties` was modified

* `error()` was removed

#### `models.DataBoxJobDetails` was modified

* `lastMitigationActionOnJob()` was removed
* `copyLogDetails()` was removed
* `dataCenterCode()` was removed
* `actions()` was removed
* `deliveryPackage()` was removed
* `chainOfCustodySasKey()` was removed
* `datacenterAddress()` was removed
* `jobStages()` was removed
* `reverseShipmentLabelSasKey()` was removed
* `returnPackage()` was removed
* `deviceErasureDetails()` was removed

#### `models.DataBoxDiskJobDetails` was modified

* `actions()` was removed
* `chainOfCustodySasKey()` was removed
* `copyLogDetails()` was removed
* `reverseShipmentLabelSasKey()` was removed
* `jobStages()` was removed
* `deviceErasureDetails()` was removed
* `dataCenterCode()` was removed
* `deliveryPackage()` was removed
* `datacenterAddress()` was removed
* `returnPackage()` was removed
* `lastMitigationActionOnJob()` was removed

#### `models.DataTransferDetailsValidationResponseProperties` was modified

* `error()` was removed

#### `models.CustomerDiskJobSecrets` was modified

* `dcAccessSecurityCode()` was removed
* `error()` was removed

#### `models.DatacenterAddressLocationResponse` was modified

* `dataCenterAzureLocation()` was removed
* `supportedCarriersForReturnShipment()` was removed

#### `models.CreateOrderLimitForSubscriptionValidationResponseProperties` was modified

* `error()` was removed

#### `models.SkuAvailabilityValidationResponseProperties` was modified

* `error()` was removed

#### `models.AddressValidationProperties` was modified

* `error()` was removed

#### `models.DataBoxCustomerDiskJobDetails` was modified

* `returnPackage()` was removed
* `deliveryPackage()` was removed
* `actions()` was removed
* `reverseShipmentLabelSasKey()` was removed
* `deviceErasureDetails()` was removed
* `dataCenterCode()` was removed
* `chainOfCustodySasKey()` was removed
* `datacenterAddress()` was removed
* `jobStages()` was removed
* `copyLogDetails()` was removed
* `lastMitigationActionOnJob()` was removed

#### `models.DatacenterAddressInstructionResponse` was modified

* `supportedCarriersForReturnShipment()` was removed
* `dataCenterAzureLocation()` was removed

#### `models.SubscriptionIsAllowedToCreateJobValidationResponseProperties` was modified

* `error()` was removed

#### `models.DataBoxHeavyJobSecrets` was modified

* `error()` was removed
* `dcAccessSecurityCode()` was removed

## 1.0.0-beta.4 (2024-10-31)

- Azure Resource Manager DataBox client library for Java. This package contains Microsoft Azure SDK for DataBox Management SDK.  Package tag package-2022-12. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Features Added

#### `models.DataBoxAccountCopyLogDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `copyLogDetailsType()` was added

#### `models.TransportAvailabilityRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DataBoxDiskCopyProgress` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TransferConfiguration` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UserAssignedIdentity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TransferConfigurationTransferAllDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AccountCredentialDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UpdateJobDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DataBoxCustomerDiskCopyLogDetails` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `copyLogDetailsType()` was added

#### `models.DataBoxDiskGranularCopyProgress` was modified

* `filesErroredOut()` was added
* `invalidDirectoriesProcessed()` was added
* `renamedContainerCount()` was added
* `directoriesErroredOut()` was added
* `bytesProcessed()` was added
* `transferType()` was added
* `invalidFilesProcessed()` was added
* `actions()` was added
* `storageAccountName()` was added
* `isEnumerationInProgress()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `error()` was added
* `totalFilesToProcess()` was added
* `filesProcessed()` was added
* `totalBytesToProcess()` was added
* `invalidFileBytesUploaded()` was added
* `accountId()` was added
* `dataAccountType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DataboxJobSecrets` was modified

* `dcAccessSecurityCode()` was added
* `jobSecretsType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `error()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DataBoxHeavyJobDetails` was modified

* `copyLogDetails()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `datacenterAddress()` was added
* `chainOfCustodySasKey()` was added
* `returnPackage()` was added
* `jobDetailsType()` was added
* `actions()` was added
* `jobStages()` was added
* `reverseShipmentLabelSasKey()` was added
* `dataCenterCode()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `deviceErasureDetails()` was added
* `deliveryPackage()` was added
* `lastMitigationActionOnJob()` was added

#### `models.JobResourceUpdateParameter` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.JobDetails` was modified

* `jobDetailsType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DataBoxHeavyAccountCopyLogDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `copyLogDetailsType()` was added

#### `models.PackageShippingDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.FilterFileDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DataImportDetails` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DataBoxDiskJobSecrets` was modified

* `error()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `jobSecretsType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `dcAccessSecurityCode()` was added

#### `models.IdentityProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TransferConfigurationTransferFilterDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.UnencryptedCredentialsList` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.JobSecrets` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `jobSecretsType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SkuAvailabilityValidationRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `validationType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TransportAvailabilityDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DataAccountDetails` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `dataAccountType()` was added

#### `models.TransferFilterDetails` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DiskScheduleAvailabilityRequest` was modified

* `skuName()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PreferencesValidationResponseProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `validationType()` was added
* `error()` was added

#### `models.ShareCredentialDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ScheduleAvailabilityRequest` was modified

* `skuName()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ContactDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.StorageAccountDetails` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `dataAccountType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DataBoxJobDetails` was modified

* `jobStages()` was added
* `actions()` was added
* `copyLogDetails()` was added
* `returnPackage()` was added
* `jobDetailsType()` was added
* `deliveryPackage()` was added
* `dataCenterCode()` was added
* `reverseShipmentLabelSasKey()` was added
* `datacenterAddress()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `lastMitigationActionOnJob()` was added
* `chainOfCustodySasKey()` was added
* `deviceErasureDetails()` was added

#### `models.ResourceIdentity` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CancellationReason` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.OperationDisplay` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AdditionalErrorInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DcAccessSecurityCode` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ManagedDiskDetails` was modified

* `dataAccountType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.BlobFilterDetails` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DataBoxDiskJobDetails` was modified

* `returnPackage()` was added
* `copyLogDetails()` was added
* `chainOfCustodySasKey()` was added
* `jobStages()` was added
* `lastMitigationActionOnJob()` was added
* `actions()` was added
* `dataCenterCode()` was added
* `jobDetailsType()` was added
* `deviceErasureDetails()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `datacenterAddress()` was added
* `deliveryPackage()` was added
* `reverseShipmentLabelSasKey()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DataTransferDetailsValidationResponseProperties` was modified

* `error()` was added
* `validationType()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DataExportDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CustomerDiskJobSecrets` was modified

* `dcAccessSecurityCode()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `error()` was added
* `jobSecretsType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.HeavyScheduleAvailabilityRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `skuName()` was added

#### `models.Preferences` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SubscriptionIsAllowedToCreateJobValidationRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `validationType()` was added

#### `models.JobDeliveryInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DataBoxCustomerDiskCopyProgress` was modified

* `invalidDirectoriesProcessed()` was added
* `storageAccountName()` was added
* `directoriesErroredOut()` was added
* `isEnumerationInProgress()` was added
* `invalidFileBytesUploaded()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `totalFilesToProcess()` was added
* `transferType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `totalBytesToProcess()` was added
* `dataAccountType()` was added
* `renamedContainerCount()` was added
* `filesErroredOut()` was added
* `filesProcessed()` was added
* `actions()` was added
* `bytesProcessed()` was added
* `error()` was added
* `accountId()` was added
* `invalidFilesProcessed()` was added

#### `models.ValidationInputResponse` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `validationType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.PackageCarrierInfo` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PackageCarrierDetails` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ScheduleAvailabilityResponse` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ImportDiskDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.TransferAllDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ExportDiskDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.LastMitigationActionOnJob` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.RegionConfigurationRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SkuCapacity` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.OperationList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CloudError` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.MarkDevicesShippedRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DatacenterAddressResponse` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `datacenterAddressType()` was added

#### `models.JobStages` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.EncryptionPreferences` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ApplianceNetworkConfiguration` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DataTransferDetailsValidationRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `validationType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ContactInfo` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DataBoxDiskCopyLogDetails` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `copyLogDetailsType()` was added

#### `models.SkuCost` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DatacenterAddressLocationResponse` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `supportedCarriersForReturnShipment()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `dataCenterAzureLocation()` was added
* `datacenterAddressType()` was added

#### `models.CreateOrderLimitForSubscriptionValidationResponseProperties` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `error()` was added
* `validationType()` was added

#### `models.DeviceErasureDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.AvailableSkuRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DataBoxScheduleAvailabilityRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `skuName()` was added

#### `models.DiskSecret` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.SkuAvailabilityValidationResponseProperties` was modified

* `validationType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `error()` was added

#### `models.CreateJobValidations` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `validationCategory()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.TransportPreferences` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ValidationInputRequest` was modified

* `validationType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ShippingAddress` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AddressValidationProperties` was modified

* `error()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `validationType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.MitigateJobRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.NotificationPreference` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.ShipmentPickUpRequest` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DataBoxCustomerDiskJobDetails` was modified

* `copyLogDetails()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `jobStages()` was added
* `actions()` was added
* `datacenterAddress()` was added
* `chainOfCustodySasKey()` was added
* `lastMitigationActionOnJob()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `jobDetailsType()` was added
* `reverseShipmentLabelSasKey()` was added
* `deviceErasureDetails()` was added
* `returnPackage()` was added
* `deliveryPackage()` was added
* `dataCenterCode()` was added

#### `models.DatacenterAddressInstructionResponse` was modified

* `supportedCarriersForReturnShipment()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `dataCenterAzureLocation()` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `datacenterAddressType()` was added

#### `models.KeyEncryptionKey` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.GranularCopyProgress` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ValidateAddress` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `validationType()` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DataLocationToServiceLocationMap` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DatacenterAddressRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ReverseShippingDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.SubscriptionIsAllowedToCreateJobValidationResponseProperties` was modified

* `error()` was added
* `validationType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.CreateOrderLimitForSubscriptionValidationRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `validationType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.GranularCopyLogDetails` was modified

* `copyLogDetailsType()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.Sku` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.ValidationRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `validationCategory()` was added

#### `models.DataBoxSecret` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DataBoxHeavySecret` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.JobResourceList` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.DataBoxHeavyJobSecrets` was modified

* `error()` was added
* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `dcAccessSecurityCode()` was added
* `jobSecretsType()` was added

#### `models.TransportAvailabilityResponse` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AvailableSkusResult` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CopyProgress` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.DataBoxDiskGranularCopyLogDetails` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `copyLogDetailsType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.AzureFileFilterDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added

#### `models.PreferencesValidationRequest` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `validationType()` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.UserAssignedProperties` was modified

* `toJson(com.azure.json.JsonWriter)` was added
* `fromJson(com.azure.json.JsonReader)` was added

#### `models.CopyLogDetails` was modified

* `fromJson(com.azure.json.JsonReader)` was added
* `toJson(com.azure.json.JsonWriter)` was added
* `copyLogDetailsType()` was added

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
