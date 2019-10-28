
## Azure Key Vault Secret Samples client library for Java
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


# Samples Azure Key Vault secrets APIs
This document describes how to use samples and what is done in each sample.

## Getting started

Typically, you will not need to install or specifically depend on Azure Core, instead it will be transitively downloaded by your build tool when you depend on of the client libraries using it. In case you want to depend on it explicitly (to implement your own client library, for example), include the following Maven dependency:

[//]: # ({x-version-update-start;com.azure:azure-core;current})
```xml
<dependency>
   <groupId>com.azure</groupId>
   <artifactId>azure-security-keyvault-secrets</artifactId>
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
Several KeyVault Java SDK samples are available to you in the SDK's GitHub repository. These samples provide example code for additional scenarios commonly encountered while working with Key Vault:

###  Additional Documentation
For more extensive documentation on Azure Key Vault, see the [API reference documentation][azkeyvault_rest].

## Contributing
This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the Code of Conduct FAQ or contact opencode@microsoft.com with any additional questions or comments.


<!-- LINKS -->
[source_code]:  src
[sample_helloWorld]: java/com/azure/security/keyvault/secrets/HelloWorld.java
[sample_helloWorldAsync]: java/com/azure/security/keyvault/secrets/HelloWorldAsync.java
[sample_list]: java/com/azure/security/keyvault/secrets/ListOperations.java
[sample_listAsync]: java/com/azure/security/keyvault/secrets/ListOperationsAsync.java
[sample_BackupRestore]: java/com/azure/security/keyvault/secrets/BackupAndRestoreOperations.java
[sample_BackupRestoreAsync]: java/com/azure/security/keyvault/secrets/BackupAndRestoreOperationsAsync.java
[sample_ManageDeleted]: java/com/azure/security/keyvault/secrets/ManagingDeletedSecrets.java
[sample_ManageDeletedAsync]: java/com/azure/security/keyvault/secrets/ManagingDeletedSecretsAsync.java

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java/sdk/core/azure-core/README.png)
