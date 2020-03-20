# Samples, Snippets, and How-To Guides

Developers like to learn by looking at code, and so the Azure SDK comes with a myriad of code samples in the form of short code snippets, sample applications, and how-to guides. This document describes where to find all these resources.

## Structure of the Repository
The Azure SDK repository is organized in the following folder structure, with the main sample locations highlighted using **bold** font.

`/samples` (this folder)<br>
&nbsp;&nbsp;&nbsp;&nbsp;`README.md` (this file)<br>
`/sdk` (folder containing sources, samples, test for all SDK packages)<br>
&nbsp;&nbsp;&nbsp;&nbsp;`/<service>` (e.g. storage)<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`/<package>` (e.g. azure-storage-blob)<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;**`README.md`** (package READMEs contain hello world samples)<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;**`/src/samples`** (package-specific samples)<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`/src/main/java`<br>
&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;`/src/test`<br>

##  Getting Started (a.k.a. `Hello World`) Samples
Each package folder contains a package-specific `README.md` file. Most of these `README` files contain `Hello World` code samples illustrating basic usage of the the APIs contained in the package. For example, you can find `Hello World` samples for the `azure-storage-blobs` package [here](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/storage/azure-storage-blob/README.md#examples).

## Package Samples and How-To Guides
Each package folder contains a subfolder called `/src/samples` with additional code samples. These samples can be either short programs contained in `*.java` files, or more complete how-to guides (code samples and some commentary) contained in `*.md` files. You can find shortcuts to main how-to guides in the [**How-To Guides List**](#how-to-guide-list) section below.

## Sample Applications
Sometimes we want to illustrate how several APIs or even packages work together in a context of a more complete program. For these cases, we created sample applications that you can look at, download, compile, and execute. These application samples are located on 
[https://docs.microsoft.com/samples/](https://docs.microsoft.com/samples/).

## How-To Guide List
This section lists how-to guides for the most commonly used APIs and most common scenarios, i.e. this section does not attempt to be a complete directory of guides contained in this repository. 

#### General How-To Guides
- [How to create **Netty Http Client**](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/core/azure-core-http-netty#examples)
- [How to create **OK Http Client**](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/core/azure-core-http-okhttp#examples)
- [How to use **Azure Core Tracing Opencensus**](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/core/azure-core-tracing-opencensus#examples)
- [How to use **Azure Core Tracing Opentelemetry**](https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/core/azure-core-tracing-opentelemetry#examples)
- How to configure, access, and analyze **logging** information (TODO)

#### Azure.Security.KeyVault.Keys
- [How to **get, set,update and delete Keys Synchronously**](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/keyvault/azure-security-keyvault-keys/src/samples/java/com/azure/security/keyvault/keys/HelloWorld.java)
- [How to **get, set,update and delete Keys Asynchronously**](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/keyvault/azure-security-keyvault-keys/src/samples/java/com/azure/security/keyvault/keys/HelloWorldAsync.java)
- [How to **list, recover and purge deleted Keys Synchronously**](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/keyvault/azure-security-keyvault-keys/src/samples/java/com/azure/security/keyvault/keys/ManagingDeletedKeys.java) 
- [How to **list, recover and purge deleted Keys Asynchronously**](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/keyvault/azure-security-keyvault-keys/src/samples/java/com/azure/security/keyvault/keys/ManagingDeletedKeysAsync.java) 
- [How to **backup and restore Keys Synchronously**](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/keyvault/azure-security-keyvault-keys/src/samples/java/com/azure/security/keyvault/keys/BackupAndRestoreOperations.java)
- [How to **backup and restore Keys Asynchronously**](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/keyvault/azure-security-keyvault-keys/src/samples/java/com/azure/security/keyvault/keys/ManagingDeletedKeysAsync.java)

#### Azure.Security.KeyVault.Secret
- [How to **get, set,update and delete Secrets Synchronously**](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/keyvault/azure-security-keyvault-secrets/src/samples/java/com/azure/security/keyvault/secrets/HelloWorld.java)
- [How to **get, set,update and delete Secrets Asynchronously**](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/keyvault/azure-security-keyvault-secrets/src/samples/java/com/azure/security/keyvault/secrets/HelloWorldAsync.java)
- [How to **Managing Deleted Secrets Synchronously**](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/keyvault/azure-security-keyvault-secrets/src/samples/java/com/azure/security/keyvault/secrets/ManagingDeletedSecrets.java) 
- [How to **Managing Deleted Secrets Asynchronously**](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/keyvault/azure-security-keyvault-secrets/src/samples/java/com/azure/security/keyvault/secrets/ManagingDeletedSecretsAsync.java) 
- [How to **backup and restore Secrets Synchronously**](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/keyvault/azure-security-keyvault-secrets/src/samples/java/com/azure/security/keyvault/secrets/BackupAndRestoreOperations.java)
- [How to **backup and restore Secrets Asynchronously**](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/keyvault/azure-security-keyvault-secrets/src/samples/java/com/azure/security/keyvault/secrets/BackupAndRestoreOperationsAsync.java)


#### Azure.Storage.Blobs
- [How to **Upload, download and list** blob](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/storage/azure-storage-blob/src/samples/java/com/azure/storage/blob/BasicExample.java)
- [How to **Upload, download large file**](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/storage/azure-storage-blob/src/samples/java/com/azure/storage/blob/FileTransferExample.java)
- [How to **Handle the exceptions**](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/storage/azure-storage-blob/src/samples/java/com/azure/storage/blob/StorageErrorHandlingExample.java)
- [How to **Create, list and delete containers**](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/storage/azure-storage-blob/src/samples/java/com/azure/storage/blob/ListContainersExample.java)

#### Azure.Storage.Queue
- [How to **Create, list and delete** queues](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/storage/azure-storage-queue/src/samples/java/com/azure/storage/queue/QueueServiceSamples.java)
- [How to **CRUD Operations on message in** queues](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/storage/azure-storage-queue/src/samples/java/com/azure/storage/queue/MessageSamples.java)
- [How to **Handle the exceptions** queues](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/storage/azure-storage-queue/src/samples/java/com/azure/storage/queue/QueueExceptionSamples.java)
- [How to **Async Sample** queues](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/storage/azure-storage-queue/src/samples/java/com/azure/storage/queue/AsyncSamples.java)

#### Azure.Storage.Blobs.Batch

- [How to **Create Blob Client**](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/storage/azure-storage-blob-batch/src/samples/java/com/azure/storage/blob/batch/ReadmeCodeSamples.java)
- [How to **Bulk Deleting**](https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/storage/azure-storage-blob-batch/src/samples/java/com/azure/storage/blob/batch/ReadmeCodeSamples.java)
