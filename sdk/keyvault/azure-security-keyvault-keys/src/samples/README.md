
## Azure Key Vault Keys Samples client library for Java
This document explains samples and how to use them.

## Key concepts
### Key
  Azure Key Vault supports multiple key types(`RSA` & `EC`) and algorithms, and enables the use of Hardware Security Modules (HSM) for high value keys. In addition to the key material, the following attributes may be specified:
* enabled: Specifies whether the key is enabled and useable for cryptographic operations.
* not_before: Identifies the time before which the key must not be used for cryptographic operations.
* expires: Identifies the expiration time on or after which the key MUST NOT be used for cryptographic operation.
* created: Indicates when this version of the key was created.
* updated: Indicates when this version of the key was updated.

### Key Client:
The Key client performs the interactions with the Azure Key Vault service for getting, setting, updating, deleting, and listing keys and its versions. An asynchronous and synchronous, KeyClient, client exists in the SDK allowing for selection of a client based on an application's use case. Once you've initialized a Key, you can interact with the primary resource types in Key Vault.

### Cryptography Client:
The Cryptography client performs the cryptographic operations locally or calls the Azure Key Vault service depending on how much key information is available locally. It supports encrypting, decrypting, signing, verifying, key wrapping, key unwrapping and retrieving the configured key. An asynchronous and synchronous, CryptographyClient, client exists in the SDK allowing for selection of a client based on an application's use case.


# Samples Azure Key Vault Keys APIs
This document describes how to use samples and what is done in each sample.

## Getting started

### Adding the package to your project

Maven dependency for Azure Keys Client library. Add it to your project's pom file.

[//]: # ({x-version-update-start;com.azure:azure-security-keyvault-keys;current})
```xml
<dependency>
   <groupId>com.azure</groupId>
   <artifactId>azure-security-keyvault-keys</artifactId>
   <version>4.0.0</version>
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
    * Create a Key
    * Retrieve a Key
    * Update a Key
    * Delete a Key

### List Operations Samples
* [ListOperations.java][sample_list] and [ListOperationsAsync.java][sample_listAsync] - Contains samples for following scenarios:
    * Create a Key
    * List Keys
    * Create new version of existing key.
    * List versions of an existing key.

### Backup And Restore Operations Samples
* [BackupAndRestoreOperations.java][sample_BackupRestore] and [BackupAndRestoreOperationsAsync.java][sample_BackupRestoreAsync] - Contains samples for following scenarios:
    * Create a Key
    * Backup a Key -- Write it to a file.
    * Delete a key
    * Restore a key

### Managing Deleted Keys Samples:
* [ManagingDeletedKeys.java][sample_ManageDeleted] and [ManagingDeletedKeysAsync.java][sample_ManageDeletedAsync] - Contains samples for following scenarios:
    * Create a Key
    * Delete a key
    * List deleted keys
    * Recover a deleted key
    * Purge Deleted key
    
### Encrypt And Decrypt Operations Samples:
* [EncryptAndDecryptOperations.java][sample_encryptDecrypt] and [EncryptAndDecryptOperationsAsync.java][sample_encryptDecryptAsync] - Contains samples for following scenarios:
    * Encrypting plain text with asymmetric key
    * Decrypting plain text with asymmetric key
    * Encrypting plain text with symmetric key
    * Decrypting plain text with symmetric key
    
### Sign And Verify Operations Samples:
* [SignAndVerifyOperations.java][sample_signVerify] and [SignAndVerifyOperationsAsync.java][sample_signVerifyAsync] - Contains samples for following scenarios:
    * Signing a digest
    * Verifying signature against a digest
    * Signing raw data content
    * Verifyng signature against raw data content
    
### Key Wrap And Unwrap Operations Samples:
* [KeyWrapUnwrapOperations.java][sample_wrapUnwrap] and [KeyWrapUnwrapOperationsAsync.java][sample_wrapUnwrapAsync] - Contains samples for following scenarios:
    * Wrapping a key with asymmetric key
    * Unwrapping a key with asymmetric key
    * Wrapping a key with symmetric key
    * Unwrapping a key with symmetric key

## Troubleshooting
### General
Key Vault clients raise exceptions. For example, if you try to retrieve a key after it is deleted a `404` error is returned, indicating resource not found. In the following snippet, the error is handled gracefully by catching the exception and displaying additional information about the error.
```java
try {
    keyClient.getKey("deletedKey")
} catch (ResourceNotFoundException e) {
    System.out.println(e.getMessage());
}
```

## Next steps
Start using KeyVault Java SDK in your solutions. Our SDK details could be found at [SDK README] [KEYS_SDK_README]. 

###  Additional Documentation
For more extensive documentation on Azure Key Vault, see the [API reference documentation][azkeyvault_rest].

## Contributing
This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the Code of Conduct FAQ or contact opencode@microsoft.com with any additional questions or comments.

<!-- LINKS -->
[source_code]:  src
[KEYS_SDK_README]: ../../README.md
[azkeyvault_rest]: https://docs.microsoft.com/rest/api/keyvault/
[sample_helloWorld]: java/com/azure/security/keyvault/keys/HelloWorld.java
[sample_helloWorldAsync]: java/com/azure/security/keyvault/keys/HelloWorldAsync.java
[sample_list]: java/com/azure/security/keyvault/keys/ListOperations.java
[sample_listAsync]: java/com/azure/security/keyvault/keys/ListOperationsAsync.java
[sample_BackupRestore]: java/com/azure/security/keyvault/keys/BackupAndRestoreOperations.java
[sample_BackupRestoreAsync]: java/com/azure/security/keyvault/keys/BackupAndRestoreOperationsAsync.java
[sample_ManageDeleted]: java/com/azure/security/keyvault/keys/ManagingDeletedKeys.java
[sample_ManageDeletedAsync]: java/com/azure/security/keyvault/keys/ManagingDeletedKeysAsync.java
[sample_encryptDecrypt]: java/com/azure/security/keyvault/keys/cryptography/EncryptDecryptOperations.java
[sample_encryptDecryptAsync]: java/com/azure/security/keyvault/keys/cryptography/EncryptDecryptOperationsAsync.java
[sample_signVerify]: java/com/azure/security/keyvault/keys/cryptography/SignVerifyOperations.java
[sample_signVerifyAsync]: java/com/azure/security/keyvault/keys/cryptography/SignVerifyOperationsAsync.java
[sample_wrapUnwrap]: java/com/azure/security/keyvault/keys/cryptography/KeyWrapUnwrapOperations.java
[sample_wrapUnwrapAsync]: java/com/azure/security/keyvault/keys/cryptography/KeyWrapUnwrapOperationsAsync.java

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java/sdk/keyvault/azure-security-keyvault-keys/README.png)
