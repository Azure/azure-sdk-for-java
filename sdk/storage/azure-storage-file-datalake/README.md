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

### Include the package

Add a dependency on Azure Storage File Datalake

[//]: # ({x-version-update-start;com.azure:azure-storage-file-datalake;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-storage-file-datalake</artifactId>
    <version>12.1.2</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Create a Storage Account
To create a Storage Account you can use the [Azure Portal][storage_account_create_portal] or [Azure CLI][storage_account_create_cli].
Note: To use data lake, your account must have hierarchical namespace enabled.

```bash
az storage account create \
    --resource-group <resource-group-name> \
    --name <storage-account-name> \
    --location <location>
```

Your storage account URL, subsequently identified as <your-storage-account-url>, would be formatted as follows
http(s)://<storage-account-name>.dfs.core.windows.net

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

DataLake Storage Gen2 was designed to:
- Service multiple petabytes of information while sustaining hundreds of gigabits of throughput
- Allow you to easily manage massive amounts of data

Key Features of DataLake Storage Gen2 include:
- Hadoop compatible access
- A superset of POSIX permissions
- Cost effective in terms of low-cost storage capacity and transactions
- Optimized driver for big data analytics

A fundamental part of Data Lake Storage Gen2 is the addition of a hierarchical namespace to Blob storage. The hierarchical namespace organizes objects/files into a hierarchy of directories for efficient data access.

In the past, cloud-based analytics had to compromise in areas of performance, management, and security. Data Lake Storage Gen2 addresses each of these aspects in the following ways:
- Performance is optimized because you do not need to copy or transform data as a prerequisite for analysis. The hierarchical namespace greatly improves the performance of directory management operations, which improves overall job performance.
- Management is easier because you can organize and manipulate files through directories and subdirectories.
- Security is enforceable because you can define POSIX permissions on directories or individual files.
- Cost effectiveness is made possible as Data Lake Storage Gen2 is built on top of the low-cost Azure Blob storage. The additional features further lower the total cost of ownership for running big data analytics on Azure.

Data Lake Storage Gen2 offers two types of resources:

- The `_filesystem` used via 'DataLakeFileSystemClient'
- The `_path` used via 'DataLakeFileClient' or 'DataLakeDirectoryClient'

|ADLS Gen2                  | Blob       |
| --------------------------| ---------- |
|Filesystem                 | Container  |
|Path (File or Directory)   | Blob       |

Note: This client library does not support hierarchical namespace (HNS) disabled storage accounts.

### URL format
Paths are addressable using the following URL format:
The following URL addresses a file:
https://myaccount.dfs.core.windows.net/myfilesystem/myfile

#### Resource URI Syntax
For the storage account, the base URI for datalake operations includes the name of the account only:

```
https://myaccount.dfs.core.windows.net
```

For a file system, the base URI includes the name of the account and the name of the file system:

```
https://myaccount.dfs.core.windows.net/myfilesystem
```

For a file/directory, the base URI includes the name of the account, the name of the file system and the name of the path:

```
https://myaccount.dfs.core.windows.net/myfilesystem/mypath
```

Note that the above URIs may not hold for more advanced scenarios such as custom domain names.

## Examples

The following sections provide several code snippets covering some of the most common Azure Storage Blob tasks, including:

- [Create a `DataLakeServiceClient`](#create-a-datalakeserviceclient)
- [Create a `DataLakeFileSystemClient`](#create-a-filesystemclient)
- [Create a `DataLakeFileClient`](#create-a-fileclient)
- [Create a `DataLakeDirectoryClient`](#create-a-directoryclient)
- [Create a file system](#create-a-filesystem)
- [Upload a file from a stream](#upload-a-file-from-a-stream)
- [Read a file to a stream](#read-a-file-to-a-stream)
- [Enumerate paths](#enumerate-paths)
- [Authenticate with Azure Identity](#authenticate-with-azure-identity)

### Create a `DataLakeServiceClient`

Create a `DataLakeServiceClient` using the [`sasToken`](#get-credentials) generated above.

<!-- embedme ./src/samples/java/com/azure/storage/file/datalake/ReadmeSamples.java#L25-L28 -->
```java
DataLakeServiceClient dataLakeServiceClient = new DataLakeServiceClientBuilder()
    .endpoint("<your-storage-account-url>")
    .sasToken("<your-sasToken>")
    .buildClient();
```

or

<!-- embedme ./src/samples/java/com/azure/storage/file/datalake/ReadmeSamples.java#L32-L34 -->
```java
DataLakeServiceClient dataLakeServiceClient = new DataLakeServiceClientBuilder()
    .endpoint("<your-storage-account-url>" + "?" + "<your-sasToken>")
    .buildClient();
```

### Create a `DataLakeFileSystemClient`

Create a `DataLakeFileSystemClient` using a `DataLakeServiceClient`.

<!-- embedme ./src/samples/java/com/azure/storage/file/datalake/ReadmeSamples.java#L38-L38 -->
```java
DataLakeFileSystemClient dataLakeFileSystemClient = dataLakeServiceClient.getFileSystemClient("myfilesystem");
```

or

Create a `DataLakeFileSystemClient` from the builder [`sasToken`](#get-credentials) generated above.

<!-- embedme ./src/samples/java/com/azure/storage/file/datalake/ReadmeSamples.java#L42-L46 -->
```java
DataLakeFileSystemClient dataLakeFileSystemClient = new DataLakeFileSystemClientBuilder()
    .endpoint("<your-storage-account-url>")
    .sasToken("<your-sasToken>")
    .fileSystemName("myfilesystem")
    .buildClient();
```

or

<!-- embedme ./src/samples/java/com/azure/storage/file/datalake/ReadmeSamples.java#L50-L52 -->
```java
DataLakeFileSystemClient dataLakeFileSystemClient = new DataLakeFileSystemClientBuilder()
    .endpoint("<your-storage-account-url>" + "/" + "myfilesystem" + "?" + "<your-sasToken>")
    .buildClient();
```

### Create a `DataLakeFileClient`

Create a `DataLakeFileClient` using a `DataLakeFileSystemClient`.

<!-- embedme ./src/samples/java/com/azure/storage/file/datalake/ReadmeSamples.java#L56-L56 -->
```java
DataLakeFileClient fileClient = dataLakeFileSystemClient.getFileClient("myfile");
```

or

Create a `FileClient` from the builder [`sasToken`](#get-credentials) generated above.

<!-- embedme ./src/samples/java/com/azure/storage/file/datalake/ReadmeSamples.java#L60-L65 -->
```java
DataLakeFileClient fileClient = new DataLakePathClientBuilder()
    .endpoint("<your-storage-account-url>")
    .sasToken("<your-sasToken>")
    .fileSystemName("myfilesystem")
    .pathName("myfile")
    .buildFileClient();
```

or

<!-- embedme ./src/samples/java/com/azure/storage/file/datalake/ReadmeSamples.java#L69-L71 -->
```java
DataLakeFileClient fileClient = new DataLakePathClientBuilder()
    .endpoint("<your-storage-account-url>" + "/" + "myfilesystem" + "/" + "myfile" + "?" + "<your-sasToken>")
    .buildFileClient();
```

### Create a `DataLakeDirectoryClient`

Get a `DataLakeDirectoryClient` using a `DataLakeFileSystemClient`.

<!-- embedme ./src/samples/java/com/azure/storage/file/datalake/ReadmeSamples.java#L75-L75 -->
```java
DataLakeDirectoryClient directoryClient = dataLakeFileSystemClient.getDirectoryClient("mydir");
```

or

Create a `DirectoryClient` from the builder [`sasToken`](#get-credentials) generated above.

<!-- embedme ./src/samples/java/com/azure/storage/file/datalake/ReadmeSamples.java#L79-L84 -->
```java
DataLakeDirectoryClient directoryClient = new DataLakePathClientBuilder()
    .endpoint("<your-storage-account-url>")
    .sasToken("<your-sasToken>")
    .fileSystemName("myfilesystem")
    .pathName("mydir")
    .buildDirectoryClient();
```

or

<!-- embedme ./src/samples/java/com/azure/storage/file/datalake/ReadmeSamples.java#L88-L90 -->
```java
DataLakeDirectoryClient directoryClient = new DataLakePathClientBuilder()
    .endpoint("<your-storage-account-url>" + "/" + "myfilesystem" + "/" + "mydir" + "?" + "<your-sasToken>")
    .buildDirectoryClient();
```

### Create a file system

Create a file system using a `DataLakeServiceClient`.

<!-- embedme ./src/samples/java/com/azure/storage/file/datalake/ReadmeSamples.java#L94-L94 -->
```java
dataLakeServiceClient.createFileSystem("myfilesystem");
```

or

Create a file system using a `DataLakeFileSystemClient`.

<!-- embedme ./src/samples/java/com/azure/storage/file/datalake/ReadmeSamples.java#L98-L98 -->
```java
dataLakeFileSystemClient.create();
```

### Enumerate paths

Enumerating all paths using a `DataLakeFileSystemClient`.

<!-- embedme ./src/samples/java/com/azure/storage/file/datalake/ReadmeSamples.java#L102-L104 -->
```java
for (PathItem pathItem : dataLakeFileSystemClient.listPaths()) {
    System.out.println("This is the path name: " + pathItem.getName());
}
```

### Rename a file

Rename a file using a `DataLakeFileClient`.

<!-- embedme ./src/samples/java/com/azure/storage/file/datalake/ReadmeSamples.java#L108-L110 -->
```java
DataLakeFileClient fileClient = dataLakeFileSystemClient.getFileClient("myfile");
fileClient.create();
fileClient.rename("new-file-system-name", "new-file-name");
```

### Rename a directory

Rename a directory using a `DataLakeDirectoryClient`.

<!-- embedme ./src/samples/java/com/azure/storage/file/datalake/ReadmeSamples.java#L114-L116 -->
```java
DataLakeDirectoryClient directoryClient = dataLakeFileSystemClient.getDirectoryClient("mydir");
directoryClient.create();
directoryClient.rename("new-file-system-name", "new-directory-name");
```

### Get file properties

Get properties from a file using a `DataLakeFileClient`.

<!-- embedme ./src/samples/java/com/azure/storage/file/datalake/ReadmeSamples.java#L120-L122 -->
```java
DataLakeFileClient fileClient = dataLakeFileSystemClient.getFileClient("myfile");
fileClient.create();
PathProperties properties = fileClient.getProperties();
```

### Get directory properties

Get properties from a directory using a `DataLakeDirectoryClient`.

<!-- embedme ./src/samples/java/com/azure/storage/file/datalake/ReadmeSamples.java#L126-L128 -->
```java
DataLakeDirectoryClient directoryClient = dataLakeFileSystemClient.getDirectoryClient("mydir");
directoryClient.create();
PathProperties properties = directoryClient.getProperties();
```

### Authenticate with Azure Identity

The [Azure Identity library][identity] provides Azure Active Directory support for authenticating with Azure Storage.

<!-- embedme ./src/samples/java/com/azure/storage/file/datalake/ReadmeSamples.java#L132-L135 -->
```java
DataLakeServiceClient storageClient = new DataLakeServiceClientBuilder()
    .endpoint("<your-storage-account-url>")
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

## Troubleshooting

When interacting with data lake using this Java client library, errors returned by the service correspond to the same HTTP
status codes returned for [REST API][error_codes] requests. For example, if you try to retrieve a file system or path that
doesn't exist in your Storage Account, a `404` error is returned, indicating `Not Found`.

### Default HTTP Client
All client libraries by default use the Netty HTTP client. Adding the above dependency will automatically configure
the client library to use the Netty HTTP client. Configuring or changing the HTTP client is detailed in the
[HTTP clients wiki](https://github.com/Azure/azure-sdk-for-java/wiki/HTTP-clients).

### Default SSL library
All client libraries, by default, use the Tomcat-native Boring SSL library to enable native-level performance for SSL
operations. The Boring SSL library is an uber jar containing native libraries for Linux / macOS / Windows, and provides
better performance compared to the default SSL implementation within the JDK. For more information, including how to
reduce the dependency size, refer to the [performance tuning][performance_tuning] section of the wiki.

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
[error_codes]: https://docs.microsoft.com/en-us/rest/api/storageservices/data-lake-storage-gen2
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fstorage%2Fazure-storage-file-datalake%2FREADME.png)
