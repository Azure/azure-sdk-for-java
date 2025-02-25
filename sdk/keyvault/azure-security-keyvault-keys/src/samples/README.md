---
page_type: sample
languages:
  - java
products:
  - azure
  - azure-key-vault
urlFragment: keyvault-keys-samples
---
# Azure Key Vault Keys Samples client library for Java
This document explains samples and how to use them.

## Key concepts
Key concepts are explained in detail [here][SDK_README_KEY_CONCEPTS].

## Getting started
Getting started explained in detail [here][SDK_README_GETTING_STARTED].

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
Start using KeyVault Java SDK in your solutions. Our SDK details could be found at [SDK README][KEYS_SDK_README]. 

###  Additional Documentation
For more extensive documentation on Azure Key Vault, see the [API reference documentation][azkeyvault_rest].

## Contributing
This project welcomes contributions and suggestions. Find [more contributing][SDK_README_CONTRIBUTING] details here.

<!-- LINKS -->
[KEYS_SDK_README]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-keys/README.md
[SDK_README_CONTRIBUTING]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-keys/README.md#contributing
[SDK_README_GETTING_STARTED]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-keys/README.md#getting-started
[SDK_README_KEY_CONCEPTS]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-keys/README.md#key-concepts
[azkeyvault_rest]: https://docs.microsoft.com/rest/api/keyvault/
[sample_helloWorld]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-keys/src/samples/java/com/azure/security/keyvault/keys/HelloWorld.java
[sample_helloWorldAsync]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-keys/src/samples/java/com/azure/security/keyvault/keys/HelloWorldAsync.java
[sample_list]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-keys/src/samples/java/com/azure/security/keyvault/keys/ListOperations.java
[sample_listAsync]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-keys/src/samples/java/com/azure/security/keyvault/keys/ListOperationsAsync.java
[sample_BackupRestore]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-keys/src/samples/java/com/azure/security/keyvault/keys/BackupAndRestoreOperations.java
[sample_BackupRestoreAsync]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-keys/src/samples/java/com/azure/security/keyvault/keys/BackupAndRestoreOperationsAsync.java
[sample_ManageDeleted]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-keys/src/samples/java/com/azure/security/keyvault/keys/ManagingDeletedKeys.java
[sample_ManageDeletedAsync]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-keys/src/samples/java/com/azure/security/keyvault/keys/ManagingDeletedKeysAsync.java
[sample_encryptDecrypt]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-keys/src/samples/java/com/azure/security/keyvault/keys/cryptography/EncryptDecryptOperations.java
[sample_encryptDecryptAsync]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-keys/src/samples/java/com/azure/security/keyvault/keys/cryptography/EncryptDecryptOperationsAsync.java
[sample_signVerify]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-keys/src/samples/java/com/azure/security/keyvault/keys/cryptography/SignVerifyOperations.java
[sample_signVerifyAsync]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-keys/src/samples/java/com/azure/security/keyvault/keys/cryptography/SignVerifyOperationsAsync.java
[sample_wrapUnwrap]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-keys/src/samples/java/com/azure/security/keyvault/keys/cryptography/KeyWrapUnwrapOperations.java
[sample_wrapUnwrapAsync]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/keyvault/azure-security-keyvault-keys/src/samples/java/com/azure/security/keyvault/keys/cryptography/KeyWrapUnwrapOperationsAsync.java

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fkeyvault%2Fazure-security-keyvault-keys%2Fsrc%2Fsamples%2FREADME.png)
