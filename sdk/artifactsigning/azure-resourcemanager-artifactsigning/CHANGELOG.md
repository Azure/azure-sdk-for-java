# Release History

## 1.1.0-beta.2 (Unreleased)

### Features Added

### Breaking Changes

### Bugs Fixed

### Other Changes

## 1.1.0-beta.1 (2026-06-09)

- Azure Resource Manager Artifact Signing client library for Java. This package contains Microsoft Azure SDK for Artifact Signing Management SDK. Code Signing resource provider api. Package api-version 2026-05-15-preview. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).

### Breaking Changes

#### `models.CertificateProfiles` was modified

* `revokeCertificate(java.lang.String,java.lang.String,java.lang.String,models.RevokeCertificate)` was removed
* `revokeCertificateWithResponse(java.lang.String,java.lang.String,java.lang.String,models.RevokeCertificate,com.azure.core.util.Context)` was removed

#### `models.CertificateProfile` was modified

* `revokeCertificateWithResponse(models.RevokeCertificate,com.azure.core.util.Context)` was removed
* `revokeCertificate(models.RevokeCertificate)` was removed

### Features Added

* `models.RevokeCertificateList` was added

#### `models.CertificateProfiles` was modified

* `revokeCertificatesWithResponse(java.lang.String,java.lang.String,java.lang.String,models.RevokeCertificateList,com.azure.core.util.Context)` was added
* `revokeCertificates(java.lang.String,java.lang.String,java.lang.String,models.RevokeCertificateList)` was added

#### `models.CertificateProfile` was modified

* `revokeCertificatesWithResponse(models.RevokeCertificateList,com.azure.core.util.Context)` was added
* `programType()` was added
* `revokeCertificates(models.RevokeCertificateList)` was added

#### `models.CertificateProfile$Definition` was modified

* `withProgramType(java.lang.String)` was added

## 1.0.0 (2026-02-11)

- Azure Resource Manager Artifact Signing client library for Java. This package contains Microsoft Azure SDK for Artifact Signing Management SDK. Code Signing resource provider api. Package api-version 1.0.0. For documentation on how to use this package, please see [Azure Management Libraries for Java](https://aka.ms/azsdk/java/mgmt).
### Features Added

- Initial release for the azure-resourcemanager-artifactsigning Java SDK.
