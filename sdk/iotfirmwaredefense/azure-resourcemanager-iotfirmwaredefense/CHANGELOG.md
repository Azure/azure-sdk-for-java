# Release History

## 2.1.0-beta.1 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 2.0.0 (2025-08-29)

- Azure Resource Manager IoT Firmware Defense client library for Java. This package contains Microsoft Azure SDK for IoT Firmware Defense Management SDK. Firmware & IoT Security REST API. Package api-version 2025-08-02. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.SbomComponentListResult` was removed

#### `models.WorkspaceList` was removed

#### `models.BinaryHardeningListResult` was removed

#### `models.CryptoKeyListResult` was removed

#### `models.SummaryListResult` was removed

#### `models.CveListResult` was removed

#### `models.CryptoCertificateListResult` was removed

#### `models.FirmwareList` was removed

#### `models.SummaryName` was removed

#### `models.OperationListResult` was removed

#### `models.WorkspaceUpdateDefinition` was removed

#### `models.PasswordHashListResult` was removed

#### `models.PairedKey` was modified

* `id()` was removed
* `withType(java.lang.String)` was removed
* `withId(java.lang.String)` was removed

#### `models.CveResource` was modified

* `namePropertiesName()` was removed

#### `IoTFirmwareDefenseManager` was modified

* `fluent.IoTFirmwareDefense serviceClient()` -> `fluent.IoTFirmwareDefenseMgmtClient serviceClient()`

#### `models.CryptoCertificateSummaryResource` was modified

* `expired()` was removed
* `pairedKeys()` was removed
* `withPairedKeys(java.lang.Long)` was removed
* `expiringSoon()` was removed
* `totalCertificates()` was removed
* `withSelfSigned(java.lang.Long)` was removed
* `withWeakSignature(java.lang.Long)` was removed
* `weakSignature()` was removed
* `withShortKeySize(java.lang.Long)` was removed
* `withExpiringSoon(java.lang.Long)` was removed
* `shortKeySize()` was removed
* `selfSigned()` was removed
* `withTotalCertificates(java.lang.Long)` was removed
* `withExpired(java.lang.Long)` was removed

#### `models.BinaryHardeningSummaryResource` was modified

* `stripped()` was removed
* `relro()` was removed
* `withStripped(java.lang.Integer)` was removed
* `withTotalFiles(java.lang.Long)` was removed
* `withPie(java.lang.Integer)` was removed
* `pie()` was removed
* `withCanary(java.lang.Integer)` was removed
* `nx()` was removed
* `canary()` was removed
* `withNx(java.lang.Integer)` was removed
* `withRelro(java.lang.Integer)` was removed

#### `models.CryptoCertificateEntity` was modified

* `withState(java.lang.String)` was removed
* `withCountry(java.lang.String)` was removed
* `withOrganizationalUnit(java.lang.String)` was removed
* `withCommonName(java.lang.String)` was removed
* `withOrganization(java.lang.String)` was removed

#### `models.CveLink` was modified

* `withLabel(java.lang.String)` was removed
* `withHref(java.lang.String)` was removed

#### `models.Summaries` was modified

* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,models.SummaryName,com.azure.core.util.Context)` was removed
* `get(java.lang.String,java.lang.String,java.lang.String,models.SummaryName)` was removed

#### `models.FirmwareSummary` was modified

* `withRootFileSystems(java.lang.Long)` was removed
* `withComponentCount(java.lang.Long)` was removed
* `withExtractedFileCount(java.lang.Long)` was removed
* `withBinaryCount(java.lang.Long)` was removed
* `withAnalysisTimeSeconds(java.lang.Long)` was removed
* `withFileSize(java.lang.Long)` was removed
* `withExtractedSize(java.lang.Long)` was removed

#### `models.Workspaces` was modified

* `deleteByResourceGroupWithResponse(java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `com.azure.core.http.rest.Response deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)` -> `void deleteByIdWithResponse(java.lang.String,com.azure.core.util.Context)`

#### `models.Firmwares` was modified

* `generateFilesystemDownloadUrl(java.lang.String,java.lang.String,java.lang.String)` was removed
* `generateDownloadUrlWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `generateDownloadUrl(java.lang.String,java.lang.String,java.lang.String)` was removed
* `generateFilesystemDownloadUrlWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed

#### `models.CveComponent` was modified

* `withComponentId(java.lang.String)` was removed
* `withName(java.lang.String)` was removed
* `withVersion(java.lang.String)` was removed

#### `models.CveSummary` was modified

* `critical()` was removed
* `medium()` was removed
* `withUnknown(java.lang.Long)` was removed
* `withHigh(java.lang.Long)` was removed
* `withMedium(java.lang.Long)` was removed
* `low()` was removed
* `withLow(java.lang.Long)` was removed
* `withCritical(java.lang.Long)` was removed
* `high()` was removed
* `unknown()` was removed

#### `models.CryptoKeyResource` was modified

* `keySize()` was removed
* `java.lang.String keyType()` -> `models.CryptoKeyType keyType()`

#### `models.CryptoCertificateResource` was modified

* `usage()` was removed
* `keySize()` was removed
* `role()` was removed
* `keyAlgorithm()` was removed
* `namePropertiesName()` was removed

#### `models.CryptoKeySummaryResource` was modified

* `withShortKeySize(java.lang.Long)` was removed
* `withTotalKeys(java.lang.Long)` was removed
* `privateKeys()` was removed
* `totalKeys()` was removed
* `withPrivateKeys(java.lang.Long)` was removed
* `withPairedKeys(java.lang.Long)` was removed
* `withPublicKeys(java.lang.Long)` was removed
* `shortKeySize()` was removed
* `pairedKeys()` was removed
* `publicKeys()` was removed

#### `models.Firmware` was modified

* `generateFilesystemDownloadUrlWithResponse(com.azure.core.util.Context)` was removed
* `generateDownloadUrlWithResponse(com.azure.core.util.Context)` was removed
* `generateFilesystemDownloadUrl()` was removed
* `generateDownloadUrl()` was removed

#### `models.BinaryHardeningResource` was modified

* `stripped()` was removed
* `canary()` was removed
* `relro()` was removed
* `architecture()` was removed
* `classProperty()` was removed
* `pie()` was removed
* `nx()` was removed

### Features Added

* `models.UsageMetric` was added

* `models.WorkspaceUpdate` was added

* `models.SkuTier` was added

* `models.Sku` was added

* `models.UsageMetrics` was added

* `models.CvssScore` was added

* `models.CertificateUsage` was added

* `models.ExecutableClass` was added

* `models.BinaryHardeningFeatures` was added

* `models.CryptoKeyType` was added

#### `models.PairedKey` was modified

* `pairedKeyId()` was added

#### `models.SbomComponentResource` was modified

* `provisioningState()` was added

#### `models.CveResource` was modified

* `provisioningState()` was added
* `componentId()` was added
* `componentVersion()` was added
* `componentName()` was added
* `cvssScores()` was added
* `effectiveCvssVersion()` was added
* `effectiveCvssScore()` was added
* `cveName()` was added

#### `IoTFirmwareDefenseManager` was modified

* `usageMetrics()` was added

#### `models.Workspace` was modified

* `sku()` was added

#### `models.CryptoCertificateSummaryResource` was modified

* `totalCertificateCount()` was added
* `expiredCertificateCount()` was added
* `weakSignatureCount()` was added
* `expiringSoonCertificateCount()` was added
* `selfSignedCertificateCount()` was added
* `shortKeySizeCount()` was added
* `pairedKeyCount()` was added

#### `models.BinaryHardeningSummaryResource` was modified

* `strippedBinaryCount()` was added
* `notExecutableStackCount()` was added
* `relocationReadOnlyCount()` was added
* `positionIndependentExecutableCount()` was added
* `stackCanaryCount()` was added

#### `models.Summaries` was modified

* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,models.SummaryType,com.azure.core.util.Context)` was added
* `get(java.lang.String,java.lang.String,java.lang.String,models.SummaryType)` was added

#### `models.Workspace$Update` was modified

* `withTags(java.util.Map)` was added
* `withSku(models.Sku)` was added

#### `models.Workspaces` was modified

* `delete(java.lang.String,java.lang.String,com.azure.core.util.Context)` was added

#### `models.Workspace$Definition` was modified

* `withSku(models.Sku)` was added

#### `models.CveSummary` was modified

* `mediumCveCount()` was added
* `highCveCount()` was added
* `unknownCveCount()` was added
* `criticalCveCount()` was added
* `lowCveCount()` was added

#### `models.SummaryResourceProperties` was modified

* `provisioningState()` was added

#### `models.CryptoKeyResource` was modified

* `cryptoKeySize()` was added
* `provisioningState()` was added

#### `models.CryptoCertificateResource` was modified

* `certificateRole()` was added
* `provisioningState()` was added
* `certificateKeySize()` was added
* `certificateName()` was added
* `certificateUsage()` was added
* `certificateKeyAlgorithm()` was added

#### `models.CryptoKeySummaryResource` was modified

* `privateKeyCount()` was added
* `publicKeyCount()` was added
* `shortKeySizeCount()` was added
* `pairedKeyCount()` was added
* `totalKeyCount()` was added

#### `models.PasswordHashResource` was modified

* `provisioningState()` was added

#### `models.BinaryHardeningResource` was modified

* `securityHardeningFeatures()` was added
* `provisioningState()` was added
* `executableClass()` was added
* `executableArchitecture()` was added

## 1.2.0-beta.1 (2025-05-08)

- Azure Resource Manager IoT Firmware Defense client library for Java. This package contains Microsoft Azure SDK for IoT Firmware Defense Management SDK. Firmware & IoT Security REST API. Package api-version 2025-04-01-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.SbomComponentListResult` was removed

#### `models.WorkspaceList` was removed

#### `models.BinaryHardeningListResult` was removed

#### `models.CryptoKeyListResult` was removed

#### `models.SummaryListResult` was removed

#### `models.CveListResult` was removed

#### `models.CveComponent` was removed

#### `models.CryptoCertificateListResult` was removed

#### `models.FirmwareList` was removed

#### `models.SummaryName` was removed

#### `models.OperationListResult` was removed

#### `models.WorkspaceUpdateDefinition` was removed

#### `models.PasswordHashListResult` was removed

#### `models.PairedKey` was modified

* `withId(java.lang.String)` was removed
* `id()` was removed
* `withType(java.lang.String)` was removed

#### `models.CveResource` was modified

* `namePropertiesName()` was removed
* `cvssVersion()` was removed
* `cvssV2Score()` was removed
* `component()` was removed
* `cvssV3Score()` was removed
* `cvssScore()` was removed

#### `IoTFirmwareDefenseManager` was modified

* `fluent.IoTFirmwareDefense serviceClient()` -> `fluent.IoTFirmwareDefenseMgmtClient serviceClient()`

#### `models.CryptoCertificateSummaryResource` was modified

* `withSelfSigned(java.lang.Long)` was removed
* `shortKeySize()` was removed
* `withExpiringSoon(java.lang.Long)` was removed
* `withTotalCertificates(java.lang.Long)` was removed
* `selfSigned()` was removed
* `withExpired(java.lang.Long)` was removed
* `totalCertificates()` was removed
* `withPairedKeys(java.lang.Long)` was removed
* `withWeakSignature(java.lang.Long)` was removed
* `weakSignature()` was removed
* `expired()` was removed
* `expiringSoon()` was removed
* `pairedKeys()` was removed
* `withShortKeySize(java.lang.Long)` was removed

#### `models.BinaryHardeningSummaryResource` was modified

* `withTotalFiles(java.lang.Long)` was removed
* `canary()` was removed
* `withNx(java.lang.Integer)` was removed
* `relro()` was removed
* `stripped()` was removed
* `withRelro(java.lang.Integer)` was removed
* `nx()` was removed
* `withStripped(java.lang.Integer)` was removed
* `withPie(java.lang.Integer)` was removed
* `withCanary(java.lang.Integer)` was removed
* `pie()` was removed

#### `models.CryptoCertificateEntity` was modified

* `withOrganizationalUnit(java.lang.String)` was removed
* `withOrganization(java.lang.String)` was removed
* `withCountry(java.lang.String)` was removed
* `withState(java.lang.String)` was removed
* `withCommonName(java.lang.String)` was removed

#### `models.CveLink` was modified

* `withHref(java.lang.String)` was removed
* `withLabel(java.lang.String)` was removed

#### `models.Summaries` was modified

* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,models.SummaryName,com.azure.core.util.Context)` was removed
* `get(java.lang.String,java.lang.String,java.lang.String,models.SummaryName)` was removed

#### `models.FirmwareSummary` was modified

* `withRootFileSystems(java.lang.Long)` was removed
* `withComponentCount(java.lang.Long)` was removed
* `withAnalysisTimeSeconds(java.lang.Long)` was removed
* `withBinaryCount(java.lang.Long)` was removed
* `withExtractedFileCount(java.lang.Long)` was removed
* `withExtractedSize(java.lang.Long)` was removed
* `withFileSize(java.lang.Long)` was removed

#### `models.Firmwares` was modified

* `generateDownloadUrlWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `generateFilesystemDownloadUrlWithResponse(java.lang.String,java.lang.String,java.lang.String,com.azure.core.util.Context)` was removed
* `generateDownloadUrl(java.lang.String,java.lang.String,java.lang.String)` was removed
* `generateFilesystemDownloadUrl(java.lang.String,java.lang.String,java.lang.String)` was removed

#### `models.CveSummary` was modified

* `low()` was removed
* `withMedium(java.lang.Long)` was removed
* `critical()` was removed
* `medium()` was removed
* `withLow(java.lang.Long)` was removed
* `unknown()` was removed
* `withUnknown(java.lang.Long)` was removed
* `high()` was removed
* `withCritical(java.lang.Long)` was removed
* `withHigh(java.lang.Long)` was removed

#### `models.CryptoKeyResource` was modified

* `java.lang.String keyType()` -> `models.CryptoKeyType keyType()`
* `keySize()` was removed

#### `models.CryptoCertificateResource` was modified

* `namePropertiesName()` was removed
* `keyAlgorithm()` was removed
* `keySize()` was removed
* `usage()` was removed
* `role()` was removed

#### `models.CryptoKeySummaryResource` was modified

* `publicKeys()` was removed
* `withPairedKeys(java.lang.Long)` was removed
* `withPublicKeys(java.lang.Long)` was removed
* `totalKeys()` was removed
* `privateKeys()` was removed
* `pairedKeys()` was removed
* `shortKeySize()` was removed
* `withTotalKeys(java.lang.Long)` was removed
* `withShortKeySize(java.lang.Long)` was removed
* `withPrivateKeys(java.lang.Long)` was removed

#### `models.Firmware` was modified

* `generateFilesystemDownloadUrlWithResponse(com.azure.core.util.Context)` was removed
* `generateDownloadUrl()` was removed
* `generateDownloadUrlWithResponse(com.azure.core.util.Context)` was removed
* `generateFilesystemDownloadUrl()` was removed

#### `models.BinaryHardeningResource` was modified

* `pie()` was removed
* `architecture()` was removed
* `classProperty()` was removed
* `canary()` was removed
* `relro()` was removed
* `stripped()` was removed
* `nx()` was removed

### Features Added

* `models.UsageMetric` was added

* `implementation.models.FirmwareListResult` was added

* `models.WorkspaceUpdate` was added

* `models.SkuTier` was added

* `implementation.models.CveResourceListResult` was added

* `implementation.models.SbomComponentResourceListResult` was added

* `implementation.models.PasswordHashResourceListResult` was added

* `models.Sku` was added

* `implementation.models.CryptoKeyResourceListResult` was added

* `models.UsageMetrics` was added

* `implementation.models.OperationListResult` was added

* `implementation.models.BinaryHardeningResourceListResult` was added

* `models.CvssScore` was added

* `implementation.models.WorkspaceListResult` was added

* `models.CertificateUsage` was added

* `implementation.models.SummaryResourceListResult` was added

* `models.ExecutableClass` was added

* `models.BinaryHardeningFeatures` was added

* `models.CryptoKeyType` was added

* `implementation.models.UsageMetricListResult` was added

* `implementation.models.CryptoCertificateResourceListResult` was added

#### `models.PairedKey` was modified

* `pairedKeyId()` was added

#### `models.SbomComponentResource` was modified

* `provisioningState()` was added

#### `models.CveResource` was modified

* `effectiveCvssScore()` was added
* `provisioningState()` was added
* `componentId()` was added
* `cveName()` was added
* `cvssScores()` was added
* `componentVersion()` was added
* `componentName()` was added
* `effectiveCvssVersion()` was added

#### `IoTFirmwareDefenseManager` was modified

* `usageMetrics()` was added

#### `models.Workspace` was modified

* `sku()` was added

#### `models.CryptoCertificateSummaryResource` was modified

* `expiringSoonCertificateCount()` was added
* `pairedKeyCount()` was added
* `totalCertificateCount()` was added
* `shortKeySizeCount()` was added
* `expiredCertificateCount()` was added
* `selfSignedCertificateCount()` was added
* `weakSignatureCount()` was added

#### `models.BinaryHardeningSummaryResource` was modified

* `relocationReadOnlyCount()` was added
* `strippedBinaryCount()` was added
* `stackCanaryCount()` was added
* `notExecutableStackCount()` was added
* `positionIndependentExecutableCount()` was added

#### `models.Summaries` was modified

* `getWithResponse(java.lang.String,java.lang.String,java.lang.String,models.SummaryType,com.azure.core.util.Context)` was added
* `get(java.lang.String,java.lang.String,java.lang.String,models.SummaryType)` was added

#### `models.Workspace$Update` was modified

* `withSku(models.Sku)` was added
* `withTags(java.util.Map)` was added

#### `models.Workspace$Definition` was modified

* `withSku(models.Sku)` was added

#### `models.CveSummary` was modified

* `highCveCount()` was added
* `mediumCveCount()` was added
* `criticalCveCount()` was added
* `unknownCveCount()` was added
* `lowCveCount()` was added

#### `models.SummaryResourceProperties` was modified

* `provisioningState()` was added

#### `models.CryptoKeyResource` was modified

* `cryptoKeySize()` was added
* `provisioningState()` was added

#### `models.CryptoCertificateResource` was modified

* `certificateUsage()` was added
* `certificateName()` was added
* `provisioningState()` was added
* `certificateRole()` was added
* `certificateKeySize()` was added
* `certificateKeyAlgorithm()` was added

#### `models.CryptoKeySummaryResource` was modified

* `privateKeyCount()` was added
* `shortKeySizeCount()` was added
* `publicKeyCount()` was added
* `totalKeyCount()` was added
* `pairedKeyCount()` was added

#### `models.PasswordHashResource` was modified

* `provisioningState()` was added

#### `models.BinaryHardeningResource` was modified

* `executableArchitecture()` was added
* `executableClass()` was added
* `securityHardeningFeatures()` was added
* `provisioningState()` was added

## 1.1.0 (2024-12-19)

- Azure Resource Manager IoT Firmware Defense client library for Java. This package contains Microsoft Azure SDK for IoT Firmware Defense Management SDK. Firmware & IoT Security REST API. Package tag package-2024-01-10. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### Serialization/Deserialization change

- `Jackson` is removed from dependency and no longer supported.

##### Migration Guide

If you are using `Jackson`/`ObjectMapper` for manual serialization/deserialization, configure your `ObjectMapper` for backward compatibility:
```java
objectMapper.registerModule(com.azure.core.serializer.json.jackson.JacksonJsonProvider.getJsonSerializableDatabindModule());
```

### Features Added

#### `models.CveSummary` was modified

* `summaryType()` was added

#### `models.SummaryResourceProperties` was modified

* `summaryType()` was added

#### `models.CryptoCertificateSummaryResource` was modified

* `summaryType()` was added

#### `models.BinaryHardeningSummaryResource` was modified

* `summaryType()` was added

#### `models.CryptoKeySummaryResource` was modified

* `summaryType()` was added

#### `models.FirmwareSummary` was modified

* `summaryType()` was added

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
