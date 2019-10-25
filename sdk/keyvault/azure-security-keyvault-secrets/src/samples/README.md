
# Samples Azure Core APIs
This document describes how to use samples and what is done in each sample.

## Getting started

Typically, you will not need to install or specifically depend on Azure Core, instead it will be transitively downloaded by your build tool when you depend on of the client libraries using it. In case you want to depend on it explicitly (to implement your own client library, for example), include the following Maven dependency:

[//]: # ({x-version-update-start;com.azure:azure-core;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core</artifactId>
  <version>1.0.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

For details on including this dependency in other build tools (Gradle, SBT, etc), refer [here](https://search.maven.org/artifact/com.azure/azure-core).

## How to run
These sample can be run in your IDE with default JDK.

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

<!-- LINKS -->
[source_code]:  src
[sample_helloWorld]: src/samples/java/com/azure/security/keyvault/secrets/HelloWorld.java
[sample_helloWorldAsync]: src/samples/java/com/azure/security/keyvault/secrets/HelloWorldAsync.java
[sample_list]: src/samples/java/com/azure/security/keyvault/secrets/ListOperations.java
[sample_listAsync]: src/samples/java/com/azure/security/keyvault/secrets/ListOperationsAsync.java
[sample_BackupRestore]: src/samples/java/com/azure/security/keyvault/secrets/BackupAndRestoreOperations.java
[sample_BackupRestoreAsync]: src/samples/java/com/azure/security/keyvault/secrets/BackupAndRestoreOperationsAsync.java
[sample_ManageDeleted]: src/samples/java/com/azure/security/keyvault/secrets/ManagingDeletedSecrets.java
[sample_ManageDeletedAsync]: src/samples/java/com/azure/security/keyvault/secrets/ManagingDeletedSecretsAsync.java

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java/sdk/core/azure-core/README.png)
