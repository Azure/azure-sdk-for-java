# Azure File Data Lake client library for Java

Azure Data Lake Storage is Microsoft's optimized storage solution for for big 
data analytics workloads. A fundamental part of Data Lake Storage Gen2 is the
addition of a hierarchical namespace to Blob storage. The hierarchical 
namespace organizes objects/files into a hierarchy of directories for 
efficient data access.

[Source code][source] | [API reference documentation][docs] | [REST API documentation][rest_docs] | [Product documentation][product_docs] | [Samples][samples]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]
- [Create Storage Account][storage_account]

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-storage-file-datalake;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-storage-file-datalake</artifactId>
    <version>12.0.0-preview.5</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Default HTTP Client
All client libraries, by default, use the Netty HTTP client. Adding the above dependency will automatically configure 
Storage Data Lake to use the Netty HTTP client. 

### Alternate HTTP client
If, instead of Netty it is preferable to use OkHTTP, there is an HTTP client available for that too. Exclude the default
Netty and include the OkHTTP client in your pom.xml.

[//]: # ({x-version-update-start;com.azure:azure-storage-file-datalake;current})
```xml
<!-- Add the Storage Data Lake dependency without the Netty HTTP client -->
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-storage-file-datalake</artifactId>
    <version>12.0.0-preview.5</version>
    <exclusions>
        <exclusion>
            <groupId>com.azure</groupId>
            <artifactId>azure-core-http-netty</artifactId>
        </exclusion>
    </exclusions>
</dependency>
```
[//]: # ({x-version-update-end})
[//]: # ({x-version-update-start;com.azure:azure-core-http-okhttp;current})
```xml
<!-- Add the OkHTTP client to use with Storage Data Lake -->
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-core-http-okhttp</artifactId>
    <version>1.0.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Configuring HTTP Clients
When an HTTP client is included on the classpath, as shown above, it is not necessary to specify it in the client library [builders](#create-datalakeserviceclient) unless you want to customize the HTTP client in some fashion. If this is desired, the `httpClient` builder method is often available to achieve just this by allowing users to provide custom (or customized) `com.azure.core.http.HttpClient` instances.

For starters, by having the Netty or OkHTTP dependencies on your classpath, as shown above, you can create new instances of these `HttpClient` types using their builder APIs. For example, here is how you would create a Netty HttpClient instance:

```java
HttpClient client = new NettyAsyncHttpClientBuilder()
        .port(8080)
        .wiretap(true)
        .build();
```

### Create a Storage Account
To create a Storage Account you can use the [Azure Portal][storage_account_create_portal] or [Azure CLI][storage_account_create_cli].
Note: To use data lake, your account must have hierarchical namespace enabled.

```bash
az storage account create \
    --resource-group <resource-group-name> \
    --name <storage-account-name> \
    --location <location>
```

### Authenticate the client

In order to interact with the Storage Service you'll need to create an instance of the Service Client class.
To make this possible you'll need the Account SAS (shared access signature) string of the Storage Account. Learn more at [SAS Token][sas_token]

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

a. Use Account name and Account key. Account name is your Storage Account name.

1. Go to your Storage Account
2. Select `Access keys` from the menu on the left
3. Under `key1`/`key2` copy the contents of the `Key` field

or

b. Use the connection string.

1. Go to your Storage Account
2. Select `Access keys` from the menu on the left
3. Under `key1`/`key2` copy the contents of the `Connection string` field

## Key concepts

This preview package for Java includes ADLS Gen2 specific API support made available in Blob SDK. This includes:
1. New directory level operations (Create, Rename/Move, Delete) for both hierarchical namespace enabled (HNS) storage accounts and HNS disabled storage accounts. For HNS enabled accounts, the rename/move operations are atomic.
2. Permission related operations (Get/Set ACLs) for hierarchical namespace enabled (HNS) accounts. 

HNS enabled accounts in ADLS Gen2 can also now leverage all of the operations available in Blob SDK. Support for File level semantics for ADLS Gen2 is planned to be made available in Blob SDK in a later release. In the meantime, please find below mapping for ADLS Gen2 terminology to Blob terminology

|ADLS Gen2 	 | Blob       |
| ---------- | ---------- |
|Filesystem	 | Container  | 
|Folder	   	 | Directory  |
|File		 | Blob       |

## Examples

The following sections provide several code snippets covering some of the most common Azure Storage Blob tasks, including:

- [Create a `DataLakeServiceClient`](#create-a-datalakeserviceclient)
- [Create a `FileSystemClient`](#create-a-filesystemclient)
- [Create a `FileClient`](#create-a-fileclient)
- [Create a `DirectoryClient`](#create-a-directoryclient)
- [Create a file system](#create-a-filesystem)
- [Upload a file from a stream](#upload-a-file-from-a-stream)
- [Read a file to a stream](#read-a-file-to-a-stream)
- [Enumerate paths](#enumerate-paths)
- [Authenticate with Azure Identity](#authenticate-with-azure-identity)

### Create a `DataLakeServiceClient`

Create a `DataLakeServiceClient` using the [`sasToken`](#get-credentials) generated above.

```java
DataLakeServiceClient dataLakeServiceClient = new DataLakeServiceClientBuilder()
        .endpoint("<your-storage-dfs-url>")
        .sasToken("<your-sasToken>")
        .buildClient();
```

### Create a `FileSystemClient`

Create a `FileSystemClient` using a `DataLakeServiceClient`.

```java
FileSystemClient fileSystemClient = dataLakeServiceClient.getFileSystemClient("myfilesystem");
```

or

Create a `FileSystemClient` from the builder [`sasToken`](#get-credentials) generated above.

```java
FileSystemClient fileSystemClient = new FileSystemClientBuilder()
        .endpoint("<your-storage-dfs-url>")
        .sasToken("<your-sasToken>")
        .containerName("myfilesystem")
        .buildClient();
```

### Create a `FileClient`

Create a `FileClient` using a `FileSystemClient`.

```java
FileClient fileClient = fileSystemClient.getFileClient("myfile");
```

or

Create a `FileClient` from the builder [`sasToken`](#get-credentials) generated above.

```java
DataLakeFileClient fileClient = new DataLakePathClientBuilder()
        .endpoint("<your-storage-dfs-url>")
        .sasToken("<your-sasToken>")
        .fileSystemName("myfilesystem")
        .pathName("myfile")
        .buildClient();
```

### Create a file system

Create a file system using a `DataLakeServiceClient`.

```java
dataLakeServiceClient.createFileSystem("myfilesystem");
```

or

Create a container using a `FileSystemClient`.

```java
fileSystemClient.create();
```

### Upload a file from a stream

Upload from an `InputStream` to a blob using a `DataLakeFileClient` generated from a `FileSystemClient`.

```java
DataLakeFileClient fileClient = fileSystemClient.getFileClient("myfile");
fileClient.create();
String dataSample = "samples";
try (ByteArrayInputStream dataStream = new ByteArrayInputStream(dataSample.getBytes())) {
    fileClient.append(dataStream, 0, dataSample.length());
}
fileClient.flush(dataSample.length());
```

### Download a file to a stream

Download a file to an `OutputStream` using a `FileClient`.

```java
try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
    fileClient.read(outputStream);
}
```

### Enumerate paths

Enumerating all paths using a `FileSystemClient`.

```java
fileSystemClient.listPaths()
        .forEach(
            pathItem -> System.out.println("This is the path name: " + pathItem.getName())
        );
```

### Authenticate with Azure Identity

The [Azure Identity library][identity] provides Azure Active Directory support for authenticating with Azure Storage.

```java
DataLakeServiceClient storageClient = new DataLakeServiceClientBuilder()
        .endpoint(endpoint)
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildClient();
```

## Troubleshooting

When interacting with data lake using this Java client library, errors returned by the service correspond to the same HTTP
status codes returned for [REST API][error_codes] requests. For example, if you try to retrieve a file system or path that
doesn't exist in your Storage Account, a `404` error is returned, indicating `Not Found`.

## Next steps

Several Storage datalake  Java SDK samples are available to you in the SDK's GitHub repository. 

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a [Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

<!-- LINKS -->
[source]: src
[samples_readme]: src/samples/README.md
[docs]: http://azure.github.io/azure-sdk-for-java/
[rest_docs]: https://docs.microsoft.com/en-us/rest/api/storageservices/data-lake-storage-gen2
[product_docs]: https://docs.microsoft.com/en-us/azure/storage/blobs/data-lake-storage-introduction
[sas_token]: https://docs.microsoft.com/azure/storage/common/storage-dotnet-shared-access-signature-part-1
[jdk]: https://docs.microsoft.com/java/azure/jdk/
[azure_subscription]: https://azure.microsoft.com/free/
[storage_account]: https://docs.microsoft.com/azure/storage/common/storage-quickstart-create-account?tabs=azure-portal
[storage_account_create_cli]: https://docs.microsoft.com/azure/storage/common/storage-quickstart-create-account?tabs=azure-cli
[storage_account_create_portal]: https://docs.microsoft.com/azure/storage/common/storage-quickstart-create-account?tabs=azure-portal
[identity]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/identity/azure-identity/README.md
[samples]: src/samples
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java/sdk/storage/azure-storage-file-data-lake/README.png)
