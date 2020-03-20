
## Azure Key Vault Secret Samples client library for Java
This document explains samples and how to use them.

## Key concepts
Key concepts are explained in detail [here][SDK_README_KEY_CONCEPTS].

# Samples Azure Key Vault secrets APIs
This document describes how to use samples and what is done in each sample.

## Getting started
Getting started explained in detail [here][SDK_README_GETTING_STARTED].

## How to run
These sample can be run in your IDE with default JDK.

## Examples
   Following section document various examples.
   
### Hello World Samples
* [HelloWorld.java][sample_helloWorld] - and [HelloWorldAsync.java][sample_helloWorldAsync] - Contains samples for following scenarios:
    * Create a Secret
    * Retrieve a Secret
    * Update a Secret
    * Delete a Secret

### List Operations Samples
* [ListOperations.java][sample_list] and [ListOperationsAsync.java][sample_listAsync] - Contains samples for following scenarios:
    * Create a Secret
    * List Secrets
    * Create new version of existing secret.
    * List versions of an existing secret.

### Backup And Restore Operations Samples
* [BackupAndRestoreOperations.java][sample_BackupRestore] and [BackupAndRestoreOperationsAsync.java][sample_BackupRestoreAsync] - Contains samples for following scenarios:
    * Create a Secret
    * Backup a Secret -- Write it to a file.
    * Delete a secret
    * Restore a secret

### Managing Deleted Secrets Samples:
* [ManagingDeletedSecrets.java][sample_ManageDeleted] and [ManagingDeletedSecretsAsync.java][sample_ManageDeletedAsync] - Contains samples for following scenarios:
    * Create a Secret
    * Delete a secret
    * List deleted secrets
    * Recover a deleted secret
    * Purge Deleted secret
    
## Troubleshooting
### General
Key Vault clients raise exceptions. For example, if you try to retrieve a secret after it is deleted a `404` error is returned, indicating resource not found. In the following snippet, the error is handled gracefully by catching the exception and displaying additional information about the error.
```java
try {
    SecretClient.getSecret("deletedSecret")
} catch (ResourceNotFoundException e) {
    System.out.println(e.getMessage());
}
```

## Next steps
Start using KeyVault Java SDK in your solutions. Our SDK details could be found at [SDK README][SECRETS_SDK_README]. 

###  Additional Documentation
For more extensive documentation on Azure Key Vault, see the [API reference documentation][azkeyvault_rest].

## Contributing
This project welcomes contributions and suggestions. Find [more contributing][SDK_README_CONTRIBUTING] details here.

<!-- LINKS -->
[SECRETS_SDK_README]: ../../README.md
[SDK_README_GETTING_STARTED]: ../../README.md#getting-started
[SDK_README_KEY_CONCEPTS]: ../../README.md#key-concepts
[SDK_README_CONTRIBUTING]: ../../README.md#contributing
[azkeyvault_rest]: https://docs.microsoft.com/rest/api/keyvault/
[sample_helloWorld]: java/com/azure/security/keyvault/secrets/HelloWorld.java
[sample_helloWorldAsync]: java/com/azure/security/keyvault/secrets/HelloWorldAsync.java
[sample_list]: java/com/azure/security/keyvault/secrets/ListOperations.java
[sample_listAsync]: java/com/azure/security/keyvault/secrets/ListOperationsAsync.java
[sample_BackupRestore]: java/com/azure/security/keyvault/secrets/BackupAndRestoreOperations.java
[sample_BackupRestoreAsync]: java/com/azure/security/keyvault/secrets/BackupAndRestoreOperationsAsync.java
[sample_ManageDeleted]: java/com/azure/security/keyvault/secrets/ManagingDeletedSecrets.java
[sample_ManageDeletedAsync]: java/com/azure/security/keyvault/secrets/ManagingDeletedSecretsAsync.java

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fkeyvault%2Fazure-security-keyvault-secrets%2Fsrc%2Fsamples%2FREADME.png)
