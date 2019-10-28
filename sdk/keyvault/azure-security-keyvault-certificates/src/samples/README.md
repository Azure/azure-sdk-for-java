
## Azure Key Vault Certificate Samples client library for Java
This document explains samples and how to use them.

## Key concepts
### Certificate
  Azure Key Vault supports certificates with secret content types(`PKCS12` & `PEM`). The certificate can be backed by keys in key vault of types(`EC` & `RSA`). In addition to the certificate policy, the following attributes may be specified:
* enabled: Specifies whether the certificate is enabled and useable.
* created: Indicates when this version of the certificate was created.
* updated: Indicates when this version of the certificate was updated.

### Certificate Client:
The Certificate client performs the interactions with the Azure Key Vault service for getting, setting, updating, deleting, and listing certificates and its versions. The client also supports CRUD operations for certificate issuers and contacts in the key vault. An asynchronous and synchronous, CertificateClient, client exists in the SDK allowing for selection of a client based on an application's use case. Once you've initialized a Certificate, you can interact with the primary resource types in Key Vault.

# Samples Azure Key Vault Keys APIs
This document describes how to use samples and what is done in each sample.

## Getting started

### Adding the package to your project

Maven dependency for Azure Key Client library. Add it to your project's pom file.
[//]: # ({x-version-update-start;com.azure:azure-security-keyvault-certificates;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-security-keyvault-certificates</artifactId>
    <version>4.0.0-preview.5</version>
</dependency>
```
[//]: # ({x-version-update-end})

For details on including this dependency in other build tools (Gradle, SBT, etc), refer [here](https://search.maven.org/artifact/com.azure/azure-core).

## How to run
These sample can be run in your IDE with default JDK.

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
Start using KeyVault Java SDK in your solutions. Our SDK details could be found at [SDK README] [CERT_SDK_README]. 

###  Additional Documentation
For more extensive documentation on Azure Key Vault, see the [API reference documentation][azkeyvault_rest].

## Contributing
This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the Code of Conduct FAQ or contact opencode@microsoft.com with any additional questions or comments.

<!-- LINKS -->
[source_code]:  src
[CERT_SDK_README]: ../../README.md
[azkeyvault_rest]: https://docs.microsoft.com/rest/api/keyvault/
[sample_helloWorld]: src/samples/java/com/azure/security/keyvault/certificates/HelloWorld.java
[sample_helloWorldAsync]: src/samples/java/com/azure/security/keyvault/certificates/HelloWorldAsync.java
[sample_list]: src/samples/java/com/azure/security/keyvault/certificates/ListOperations.java
[sample_listAsync]: src/samples/java/com/azure/security/keyvault/certificates/ListOperationsAsync.java
[sample_BackupRestore]: src/samples/java/com/azure/security/keyvault/certificates/BackupAndRestoreOperations.java
[sample_BackupRestoreAsync]: src/samples/java/com/azure/security/keyvault/certificates/BackupAndRestoreOperationsAsync.java
[sample_ManageDeleted]: src/samples/java/com/azure/security/keyvault/certificates/ManagingDeletedCertificates.java
[sample_ManageDeletedAsync]: src/samples/java/com/azure/security/keyvault/certificates/ManagingDeletedCertificatesAsync.java

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java/sdk/keyvault/azure-security-keyvault-certificates/README.png)
