---
page_type: sample
languages:
  - java
products:
  - azure
  - azure-key-vault
urlFragment: keyvault-certificates-samples
---

# Azure Key Vault Certificate Samples client library for Java
This document explains samples and how to use them.

## Key concepts
Key concepts are explained in detail [here][SDK_README_KEY_CONCEPTS].

## Getting started

Getting started explained in detail [here][SDK_README_GETTING_STARTED].

## Examples
   Following section document various examples.
   
### Hello World Samples
* [HelloWorld.java][sample_helloWorld] - and [HelloWorldAsync.java][sample_helloWorldAsync] - Contains samples for following scenarios:
    * Create a Certificate & Certificate Issuer
    * Retrieve a Certificate & Certificate Issuer
    * Update a Certificate
    * Delete a Certificate

### List Operations Samples
* [ListOperations.java][sample_list] and [ListOperationsAsync.java][sample_listAsync] - Contains samples for following scenarios:
    * Create a Certificate, Certificate Issuer & Certificate Contact
    * List Certificates, Certificate Issuers & Certificate Contacts
    * Create new version of existing certificate.
    * List versions of an existing certificate.

### Backup And Restore Operations Samples
* [BackupAndRestoreOperations.java][sample_BackupRestore] and [BackupAndRestoreOperationsAsync.java][sample_BackupRestoreAsync] - Contains samples for following scenarios:
    * Create a Certificate
    * Backup a Certificate -- Write it to a file.
    * Delete a certificate
    * Restore a certificate

### Managing Deleted Certificates Samples:
* [ManagingDeletedCertificates.java][sample_ManageDeleted] and [ManagingDeletedCertificatesAsync.java][sample_ManageDeletedAsync] - Contains samples for following scenarios:
    * Create a Certificate
    * Delete a certificate
    * List deleted certificates
    * Recover a deleted certificate
    * Purge Deleted certificate

## Troubleshooting
### General
Certificate Vault clients raise exceptions. For example, if you try to retrieve a certificate after it is deleted a `404` error is returned, indicating resource not found. In the following snippet, the error is handled gracefully by catching the exception and displaying additional information about the error.
```java
try {
    certificateClient.getCertificate("certificateName")
} catch (ResourceNotFoundException e) {
    System.out.println(e.getMessage());
}
```

## Next steps
Start using KeyVault Java SDK in your solutions. Our SDK details could be found at [SDK README][CERT_SDK_README]. 

###  Additional Documentation
For more extensive documentation on Azure Key Vault, see the [API reference documentation][azkeyvault_rest].

## Contributing
This project welcomes contributions and suggestions. Find [more contributing][SDK_README_CONTRIBUTING] details here.

<!-- LINKS -->
[CERT_SDK_README]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-certificates/README.md
[SDK_README_CONTRIBUTING]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-certificates/README.md#contributing
[SDK_README_GETTING_STARTED]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-certificates/README.md#getting-started
[SDK_README_KEY_CONCEPTS]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-certificates/README.md#key-concepts
[azkeyvault_rest]: https://docs.microsoft.com/rest/api/keyvault/
[sample_helloWorld]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-certificates/src/samples/java/com/azure/security/keyvault/certificates/HelloWorld.java
[sample_helloWorldAsync]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-certificates/src/samples/java/com/azure/security/keyvault/certificates/HelloWorldAsync.java
[sample_list]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-certificates/src/samples/java/com/azure/security/keyvault/certificates/ListOperations.java
[sample_listAsync]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-certificates/src/samples/java/com/azure/security/keyvault/certificates/ListOperationsAsync.java
[sample_BackupRestore]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-certificates/src/samples/java/com/azure/security/keyvault/certificates/BackupAndRestoreOperations.java
[sample_BackupRestoreAsync]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-certificates/src/samples/java/com/azure/security/keyvault/certificates/BackupAndRestoreOperationsAsync.java
[sample_ManageDeleted]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-certificates/src/samples/java/com/azure/security/keyvault/certificates/ManagingDeletedCertificates.java
[sample_ManageDeletedAsync]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-certificates/src/samples/java/com/azure/security/keyvault/certificates/ManagingDeletedCertificatesAsync.java

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fkeyvault%2Fazure-security-keyvault-certificates%2Fsrc%2Fsamples%2FREADME.png)
