# Release History

## 1.1.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.0.0 (2024-03-18)

- Azure Resource Manager IoT Firmware Defense client library for Java. This package contains Microsoft Azure SDK for IoT Firmware Defense Management SDK. Firmware & IoT Security REST API. Package tag package-2024-01-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

* `models.Cve` was removed

* `models.CryptoKeyList` was removed

* `models.PasswordHashList` was removed

* `models.NxFlag` was removed

* `models.CveList` was removed

* `models.CryptoKey` was removed

* `models.CryptoCertificateList` was removed

* `models.CryptoKeySummary` was removed

* `models.BinaryHardeningSummary` was removed

* `models.BinaryHardeningList` was removed

* `models.CanaryFlag` was removed

* `models.IsExpired` was removed

* `models.IsUpdateAvailable` was removed

* `models.CryptoCertificateSummary` was removed

* `models.PasswordHash` was removed

* `models.PieFlag` was removed

* `models.StrippedFlag` was removed

* `models.IsSelfSigned` was removed

* `models.CryptoCertificate` was removed

* `models.IsWeakSignature` was removed

* `models.IsShortKeySize` was removed

* `models.ComponentList` was removed

* `models.RelroFlag` was removed

* `models.Component` was removed

* `models.BinaryHardening` was removed

#### `models.PairedKey` was modified

* `withAdditionalProperties(java.lang.Object)` was removed
* `additionalProperties()` was removed

#### `models.UrlToken` was modified

* `uploadUrl()` was removed

#### `IoTFirmwareDefenseManager` was modified

* `fluent.Fist serviceClient()` -> `fluent.IoTFirmwareDefense serviceClient()`

#### `models.FirmwareSummary` was modified

* `java.lang.Long extractedFileCount()` -> `java.lang.Long extractedFileCount()`
* `java.lang.Long fileSize()` -> `java.lang.Long fileSize()`
* `innerModel()` was removed
* `java.lang.Long componentCount()` -> `java.lang.Long componentCount()`
* `java.lang.Long rootFileSystems()` -> `java.lang.Long rootFileSystems()`
* `java.lang.Long analysisTimeSeconds()` -> `java.lang.Long analysisTimeSeconds()`
* `java.lang.Long binaryCount()` -> `java.lang.Long binaryCount()`
* `java.lang.Long extractedSize()` -> `java.lang.Long extractedSize()`

#### `models.Firmwares` was modified

* `generateCveSummary(java.lang.String,java.lang.String,java.lang.String)` was removed
* `listGenerateCveList(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `generateSummaryWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `listGenerateCryptoKeyList(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `listGeneratePasswordHashList(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `listGenerateCryptoCertificateList(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `listGenerateBinaryHardeningList(java.lang.String,java.lang.String,java.lang.String)` was removed
* `listGenerateCveList(java.lang.String,java.lang.String,java.lang.String)` was removed
* `listGenerateComponentList(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `generateBinaryHardeningDetails(java.lang.String,java.lang.String,java.lang.String)` was removed
* `generateCryptoKeySummary(java.lang.String,java.lang.String,java.lang.String)` was removed
* `generateBinaryHardeningDetailsWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `generateBinaryHardeningSummary(java.lang.String,java.lang.String,java.lang.String)` was removed
* `listGenerateCryptoKeyList(java.lang.String,java.lang.String,java.lang.String)` was removed
* `generateCryptoCertificateSummary(java.lang.String,java.lang.String,java.lang.String)` was removed
* `generateCveSummaryWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `listGenerateComponentList(java.lang.String,java.lang.String,java.lang.String)` was removed
* `generateComponentDetails(java.lang.String,java.lang.String,java.lang.String)` was removed
* `listGeneratePasswordHashList(java.lang.String,java.lang.String,java.lang.String)` was removed
* `listGenerateBinaryHardeningList(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `generateBinaryHardeningSummaryWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `generateComponentDetailsWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `generateCryptoKeySummaryWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `generateSummary(java.lang.String,java.lang.String,java.lang.String)` was removed
* `generateCryptoCertificateSummaryWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `listGenerateCryptoCertificateList(java.lang.String,java.lang.String,java.lang.String)` was removed

#### `models.CveSummary` was modified

* `java.lang.Long critical()` -> `java.lang.Long critical()`
* `java.lang.Long low()` -> `java.lang.Long low()`
* `java.lang.Long high()` -> `java.lang.Long high()`
* `undefined()` was removed
* `innerModel()` was removed
* `java.lang.Long unknown()` -> `java.lang.Long unknown()`
* `java.lang.Long medium()` -> `java.lang.Long medium()`

#### `models.Firmware` was modified

* `listGeneratePasswordHashList()` was removed
* `listGenerateCveList(com.azure.core.util.Context)` was removed
* `generateComponentDetailsWithResponse(com.azure.core.util.Context)` was removed
* `generateCryptoKeySummaryWithResponse(com.azure.core.util.Context)` was removed
* `listGenerateCveList()` was removed
* `generateCveSummary()` was removed
* `listGenerateComponentList()` was removed
* `generateComponentDetails()` was removed
* `generateBinaryHardeningDetails()` was removed
* `listGenerateComponentList(com.azure.core.util.Context)` was removed
* `listGenerateBinaryHardeningList(com.azure.core.util.Context)` was removed
* `listGenerateCryptoKeyList(com.azure.core.util.Context)` was removed
* `listGenerateBinaryHardeningList()` was removed
* `listGenerateCryptoCertificateList(com.azure.core.util.Context)` was removed
* `generateCryptoKeySummary()` was removed
* `generateBinaryHardeningSummary()` was removed
* `listGenerateCryptoKeyList()` was removed
* `generateSummaryWithResponse(com.azure.core.util.Context)` was removed
* `listGeneratePasswordHashList(com.azure.core.util.Context)` was removed
* `generateSummary()` was removed
* `generateCryptoCertificateSummary()` was removed
* `generateBinaryHardeningDetailsWithResponse(com.azure.core.util.Context)` was removed
* `generateCryptoCertificateSummaryWithResponse(com.azure.core.util.Context)` was removed
* `listGenerateCryptoCertificateList()` was removed
* `generateCveSummaryWithResponse(com.azure.core.util.Context)` was removed
* `generateBinaryHardeningSummaryWithResponse(com.azure.core.util.Context)` was removed

### Features Added

* `models.SbomComponentResource` was added

* `models.CveResource` was added

* `models.SbomComponentListResult` was added

* `models.BinaryHardenings` was added

* `models.CryptoCertificateSummaryResource` was added

* `models.BinaryHardeningListResult` was added

* `models.CryptoKeyListResult` was added

* `models.SummaryListResult` was added

* `models.BinaryHardeningSummaryResource` was added

* `models.CveListResult` was added

* `models.CryptoKeys` was added

* `models.Summaries` was added

* `models.SbomComponents` was added

* `models.SummaryType` was added

* `models.SummaryResource` was added

* `models.StatusMessage` was added

* `models.CveComponent` was added

* `models.CryptoCertificateListResult` was added

* `models.SummaryResourceProperties` was added

* `models.CryptoCertificates` was added

* `models.CryptoKeyResource` was added

* `models.SummaryName` was added

* `models.CryptoCertificateResource` was added

* `models.CryptoKeySummaryResource` was added

* `models.PasswordHashes` was added

* `models.Cves` was added

* `models.PasswordHashResource` was added

* `models.PasswordHashListResult` was added

* `models.BinaryHardeningResource` was added

#### `IoTFirmwareDefenseManager` was modified

* `passwordHashes()` was added
* `binaryHardenings()` was added
* `cryptoCertificates()` was added
* `sbomComponents()` was added
* `summaries()` was added
* `cves()` was added
* `cryptoKeys()` was added

#### `models.FirmwareSummary` was modified

* `withExtractedFileCount(java.lang.Long)` was added
* `withFileSize(java.lang.Long)` was added
* `withExtractedSize(java.lang.Long)` was added
* `withComponentCount(java.lang.Long)` was added
* `withBinaryCount(java.lang.Long)` was added
* `withAnalysisTimeSeconds(java.lang.Long)` was added
* `withRootFileSystems(java.lang.Long)` was added
* `validate()` was added

#### `models.CveSummary` was modified

* `withUnknown(java.lang.Long)` was added
* `withLow(java.lang.Long)` was added
* `withMedium(java.lang.Long)` was added
* `validate()` was added
* `withHigh(java.lang.Long)` was added
* `withCritical(java.lang.Long)` was added

## 1.0.0-beta.1 (2023-07-20)

- Azure Resource Manager IoT Firmware Defense client library for Java. This package contains Microsoft Azure SDK for IoT Firmware Defense Management SDK. The definitions and parameters in this swagger specification will be used to manage the IoT Firmware Defense resources. Package tag package-2023-02-08-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
