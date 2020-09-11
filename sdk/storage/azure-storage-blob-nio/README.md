# Azure Storage Blob NIO FileSystemProvider

This package allows you to interact with Azure Blob Storage through the standard Java NIO Filesystem APIs.

[Source code][source] | [API reference documentation][docs] | [REST API documentation][rest_docs] | [Product documentation][product_docs] | [Samples][samples]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]
- [Create Storage Account][storage_account]

### Include the package

[//]: # ({x-version-update-start;com.azure:azure-storage-blob-nio;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-storage-blob-nio</artifactId>
    <version>12.0.0-beta.2</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Create a Storage Account
To create a Storage Account you can use the [Azure Portal][storage_account_create_portal] or [Azure CLI][storage_account_create_cli].

```bash
az storage account create \
    --resource-group <resource-group-name> \
    --name <storage-account-name> \
    --location <location>
```

### Authenticate the client

The simplest way to interact with the Storage Service is to create an instance of the [FileSystem][file_system] class using the [FileSystems][file_systems] API. 
To make this possible you'll need the Account SAS (shared access signature) string of the Storage Account or a Shared Key. Learn more at [SAS Token][sas_token] and [Shared Key][shared_key]

#### Get credentials

##### SAS Token

a. Use the Azure CLI snippet below to get the SAS token from the Storage Account.

```bash
az storage blob generate-sas \
    --account-name {Storage Account name} \
    --container-name {container name} \
    --name {blob name} \
    --permissions {permissions to grant} \
    --expiry {datetime to expire the SAS token} \
    --services {storage services the SAS allows} \
    --resource-types {resource types the SAS allows}
```

Example:

```bash
CONNECTION_STRING=<connection-string>

az storage blob generate-sas \
    --account-name MyStorageAccount \
    --container-name MyContainer \
    --name MyBlob \
    --permissions racdw \
    --expiry 2020-06-15
```

b. Alternatively, get the Account SAS Token from the Azure Portal.

1. Go to your Storage Account
2. Select `Shared access signature` from the menu on the left
3. Click on `Generate SAS and connection string` (after setup)

##### **Shared Key Credential**

Use Account name and Account key. Account name is your Storage Account name.

1. Go to your Storage Account
2. Select `Access keys` from the menu on the left
3. Under `key1`/`key2` copy the contents of the `Key` field

## Key concepts

NIO on top of Blob Storage is designed for:

- Working with Blob Storage as though it were a local file system
- Random access reads on large blobs without downloading the entire blob
- Uploading full files as blobs 
- Creating and navigating a directory structure within an account
- Reading and setting attributes on blobs

## Design Notes
It is important to recognize that Azure Blob Storage is not a true FileSystem, nor is it the goal of this project to 
force Azure Blob Storage to act like a full-fledged FileSystem. While providing FileSystem APIs on top of Azure Blob 
Storage can offer convenience and ease of access in certain cases, trying to force the Storage service to work in 
scenarios it is not designed for is bound to introduce performance and stability problems. 

To that end, this project will only offer APIs that can be sensibly and cleanly built on top of Azure Blob Storage APIs. 
We recognize that this will leave some scenarios unsupported indefinitely, but we would rather offer a product that 
works predictably and reliably in its well defined scenarios than eagerly support all possible scenarios at the expense 
of quality. Even still, supporting some fundamentally required use cases, such as directories, can result in unexpected 
behavior due to the difference between blob storage and a file system. The javadocs on each type and method should
therefore be read and understood for ways in which they may diverge from the standard specified by the JDK. 

Moreover, even from within a given application, it should be remembered that using a remote FileSystem introduces higher 
latency. Because of this, particular care must be taken when managing concurrency. Race conditions are more likely to 
manifest, network failures occur more frequently than disk failures, and other such distributed application scenarios 
must be considered when working with this FileSystem. While the AzureFileSystem will ensure it takes appropriate steps 
towards robustness and reliability, the application developer must also design around these failure scenarios and have 
fallback and retry options available.

The view of the FileSystem from within an instance of the JVM will be consistent, but the AzureFileSystem makes no 
guarantees on behavior or state should other processes operate on the same data. The AzureFileSystem will assume that it 
has exclusive access to the resources stored in Azure Blob Storage and will behave without regard for potential 
interfering applications.

Finally, this implementation has currently chosen to always read/write directly to/from Azure Storage without a local 
cache. Our team has determined that with the tradeoffs of complexity, correctness, safety, performance, debuggability, 
etc. one option is not inherently better than the other and that this choice most directly addresses the current known
use cases for this project. While this has consequences for every API, of particular note is the limitations on writing
data. Data may only be written as an entire file (i.e. random IO or appends are not supported), and data is not 
committed or available to be read until the write stream is closed. 

## Examples

The following sections provide several code snippets covering some of the most common Azure Storage Blob NIO tasks, including:

- [Create a `FileSystem`](#create-a-filesystem)
- [Create a directory](#create-a-directory)
- [Iterate over directory contents](#iterate-over-directory-contents)
- [Read a file](#read-a-file)
- [Write to a file](#write-to-a-file)
- [Copy a file](#copy-a-file)
- [Delete a file](#delete-a-file)
- [Read attributes on a file](#read-attributes-on-a-file)
- [Write attributes to a file](#write-attributes-to-a-file)

### Create a `FileSystem`

Create a `FileSystem` using the [`shared key`](#get-credentials) retrieved above.

Note that you can further configure the file system using constants available in `AzureFileSystem`.
Please see the docs for `AzureFileSystemProvider` for a full explanation of initializing and configuring a filesystem

<!-- embedme ./src/samples/java/com/azure/storage/blob/nio/ReadmeSamples.java#L39-L43 -->
```java
Map<String, Object> config = new HashMap<>();
String[] stores = {"<your_container_name", "another_container_name"}; // Any iterable is acceptable
config.put(AzureFileSystem.AZURE_STORAGE_ACCOUNT_KEY, "<your_account_key>");
config.put(AzureFileSystem.AZURE_STORAGE_FILE_STORES, stores);
FileSystem myFs = FileSystems.newFileSystem(new URI("azb://?account=<your_account_name"), config);
```

### Create a directory

Create a directory using the `Files` api

<!-- embedme ./src/samples/java/com/azure/storage/blob/nio/ReadmeSamples.java#L47-L48 -->
```java
Path dirPath = myFs.getPath("dir");
Files.createDirectory(dirPath);
```

### Iterate over directory contents

Iterate over a directory using a `DirectoryStream`

<!-- embedme ./src/samples/java/com/azure/storage/blob/nio/ReadmeSamples.java#L52-L54 -->
```java
for (Path p : Files.newDirectoryStream(dirPath)) {
    System.out.println(p.toString());
}
```

### Read a file

Read the contents of a file using an `InputStream`. Skipping, marking, and resetting are all supported.

<!-- embedme ./src/samples/java/com/azure/storage/blob/nio/ReadmeSamples.java#L58-L61 -->
```java
Path filePath = myFs.getPath("file");
InputStream is = Files.newInputStream(filePath);
is.read();
is.close();
```

### Write to a file

Write to a file. Only writing whole files is supported. Random IO is not supported. The stream must be closed in order 
to guarantee that the data is available to be read.

<!-- embedme ./src/samples/java/com/azure/storage/blob/nio/ReadmeSamples.java#L65-L67 -->
```java
OutputStream os = Files.newOutputStream(filePath);
os.write(0);
os.close();
``` 

### Copy a file

<!-- embedme ./src/samples/java/com/azure/storage/blob/nio/ReadmeSamples.java#L71-L72 -->
```java
Path destinationPath = myFs.getPath("destinationFile");
Files.copy(filePath, destinationPath, StandardCopyOption.COPY_ATTRIBUTES);
```

### Delete a file

<!-- embedme ./src/samples/java/com/azure/storage/blob/nio/ReadmeSamples.java#L76-L76 -->
```java
Files.delete(filePath);
```

### Read attributes on a file

Read attributes of a file through the `AzureBlobFileAttributes`.

<!-- embedme ./src/samples/java/com/azure/storage/blob/nio/ReadmeSamples.java#L80-L81 -->
```java
AzureBlobFileAttributes attr = Files.readAttributes(filePath, AzureBlobFileAttributes.class);
BlobHttpHeaders headers = attr.blobHttpHeaders();
```

Or read attributes dynamically by specifying a string of desired attributes. This will not improve performance as a call 
to retrieve any attribute will always retrieve all of them as an atomic bulk operation. You may specify "*" instead of a 
list of specific attributes to have all attributes returned in the map.

<!-- embedme ./src/samples/java/com/azure/storage/blob/nio/ReadmeSamples.java#L85-L85 -->
```java
Map<String, Object> attributes = Files.readAttributes(filePath, "azureBlob:metadata,headers");
```

### Write attributes to a file

Set attributes of a file through the `AzureBlobFileAttributeView`.

<!-- embedme ./src/samples/java/com/azure/storage/blob/nio/ReadmeSamples.java#L89-L90 -->
```java
AzureBlobFileAttributeView view = Files.getFileAttributeView(filePath, AzureBlobFileAttributeView.class);
view.setMetadata(Collections.EMPTY_MAP);
```

Or set an attribute dynamically by specifying the attribute as a string.

<!-- embedme ./src/samples/java/com/azure/storage/blob/nio/ReadmeSamples.java#L94-L94 -->
```java
Files.setAttribute(filePath, "azureBlob:blobHttpHeaders", new BlobHttpHeaders());
```

## Troubleshooting

When using the NIO implementation for Azure Blob Storage, errors returned by the service are manifested as an 
`IOException` which wraps a `BlobStorageException` having the same HTTP status codes returned for 
[REST API][error_codes] requests. For example, if you try to read a file that doesn't exist in your Storage Account, a 
`404` error is returned, indicating `Not Found`.

### Default HTTP Client
All client libraries by default use the Netty HTTP client. Adding the above dependency will automatically configure 
the client library to use the Netty HTTP client. Configuring or changing the HTTP client is detailed in the
[HTTP clients wiki](https://github.com/Azure/azure-sdk-for-java/wiki/HTTP-clients).

### Default SSL library
All client libraries, by default, use the Tomcat-native Boring SSL library to enable native-level performance for SSL 
operations. The Boring SSL library is an uber jar containing native libraries for Linux / macOS / Windows, and provides 
better performance compared to the default SSL implementation within the JDK. For more information, including how to 
reduce the dependency size, refer to the [performance tuning][performance_tuning] section of the wiki.

## Continued development

This project is still actively being developed in an effort to move from preview to GA. Below is a list of features that
are not currently supported but are under consideration and may be added before GA. We welcome feedback and input on 
which of these may be most useful and are open to suggestions for items not included in this list. While all of these 
items are being considered, they have not been investigated and designed and therefore we cannot confirm their 
feasibility within Azure Blob Storage. Therefore, it may be the case that further investigation reveals a feature may 
not be possible or otherwise may conflict with established design goals and therefor will not ultimately be supported. 

- Symbolic links
- Hard links
- Hidden files
- Random writes
- File locks
- Read only files or file stores
- Watches on directory events
- Support for other Azure Storage services such as ADLS Gen 2 (Datalake) and Azure Files (shares)
- Token authentication
- Multi-account filesystems
- Delegating access to single files
- Normalizing directory structure of data upon loading a FileSystem
- Local caching
- Other `OpenOptions` such as append or dsync
- Flags to toggle certain behaviors such as FileStore (container) creation, etc.

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a [Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

<!-- LINKS -->
[source]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/storage/azure-storage-blob-nio/src
[samples_readme]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/storage/azure-storage-blob-nio/src/samples/README.md
[docs]: http://azure.github.io/azure-sdk-for-java/
[rest_docs]: https://docs.microsoft.com/rest/api/storageservices/blob-service-rest-api
[product_docs]: https://docs.microsoft.com/azure/storage/blobs/storage-blobs-overview
[sas_token]: https://docs.microsoft.com/azure/storage/common/storage-dotnet-shared-access-signature-part-1
[shared_key]: https://docs.microsoft.com/en-us/rest/api/storageservices/authorize-with-shared-key
[jdk]: https://docs.microsoft.com/java/azure/jdk/
[azure_subscription]: https://azure.microsoft.com/free/
[storage_account]: https://docs.microsoft.com/azure/storage/common/storage-quickstart-create-account?tabs=azure-portal
[storage_account_create_cli]: https://docs.microsoft.com/azure/storage/common/storage-quickstart-create-account?tabs=azure-cli
[storage_account_create_portal]: https://docs.microsoft.com/azure/storage/common/storage-quickstart-create-account?tabs=azure-portal
[identity]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/identity/azure-identity/README.md
[error_codes]: https://docs.microsoft.com/rest/api/storageservices/blob-service-error-codes
[samples]: https://docs.oracle.com/javase/tutorial/essential/io/fileio.html
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning
[file_system]: https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystem.html
[file_systems]: https://docs.oracle.com/javase/7/docs/api/java/nio/file/FileSystems.html

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fstorage%2Fazure-storage-blob%2FREADME.png)
