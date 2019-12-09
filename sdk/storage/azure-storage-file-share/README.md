# Azure File Share client library for Java
The Server Message Block (SMB) protocol is the preferred file share protocol used on-premises today.
The Microsoft Azure File Share service enables customers to leverage the availability and scalability of Azure's Cloud Infrastructure as a Service (IaaS) SMB without having to rewrite SMB client applications.

Files stored in Azure File Share service shares are accessible via the SMB protocol, and also via REST APIs.
The File Share service offers the following four resources: the storage account, shares, directories, and files.
Shares provide a way to organize sets of files and also can be mounted as an SMB file share that is hosted in the cloud.

[Source code][source_code] | [API reference documentation][reference_docs] | [REST API documentation][rest_api_documentation] | [Product documentation][storage_docs] |
[Samples][samples]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]
- [Create Storage Account][storage_account]

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-storage-file-share;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-storage-file-share</artifactId>
  <version>12.0.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Default HTTP Client
All client libraries, by default, use Netty HTTP client. Adding the above dependency will automatically configure 
Storage File Share to use Netty HTTP client. 

### Alternate HTTP client
If, instead of Netty it is preferable to use OkHTTP, there is a HTTP client available for that too. Exclude the default
Netty and include OkHTTP client in your pom.xml.

[//]: # ({x-version-update-start;com.azure:azure-storage-file-share;current})
```xml
<!-- Add Storage File Share dependency without Netty HTTP client -->
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-storage-file-share</artifactId>
      <version>12.0.0</version>
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
<!-- Add OkHTTP client to use with Storage File Share-->
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core-http-okhttp</artifactId>
  <version>1.1.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Configuring HTTP Clients
When an HTTP client is included on the classpath, as shown above, it is not necessary to specify it in the client library [builders](#file-services), unless you want to customize the HTTP client in some fashion. If this is desired, the `httpClient` builder method is often available to achieve just this, by allowing users to provide a custom (or customized) `com.azure.core.http.HttpClient` instances.

For starters, by having the Netty or OkHTTP dependencies on your classpath, as shown above, you can create new instances of these `HttpClient` types using their builder APIs. For example, here is how you would create a Netty HttpClient instance:

### Default SSL library
All client libraries, by default, use the Tomcat-native Boring SSL library to enable native-level performance for SSL operations. The Boring SSL library is an uber jar containing native libraries for Linux / macOS / Windows, and provides better performance compared to the default SSL implementation within the JDK. For more information, including how to reduce the dependency size, refer to the [performance tuning][performance_tuning] section of the wiki.

```java
HttpClient client = new NettyAsyncHttpClientBuilder()
    .port(8080)
    .wiretap(true)
    .build();
```

### Create a Storage Account
To create a Storage Account you can use the Azure Portal or [Azure CLI][azure_cli].

```powershell
az group create \
    --name storage-resource-group \
    --location westus
```

### Authenticate the client

In order to interact with the Storage service (File Share Service, Share, Directory, MessageId, File) you'll need to create an instance of the Service Client class.
To make this possible you'll need the Account SAS (shared access signature) string of Storage account. Learn more at [SAS Token][sas_token]

#### Get Credentials

- **SAS Token**
    * Use the [Azure CLI][azure_cli] snippet below to get the SAS token from the Storage account.

        ```powershell
        az storage file generate-sas
            --name {account name}
            --expiry {date/time to expire SAS token}
            --permission {permission to grant}
            --connection-string {connection string of the storage account}
        ```

        ```powershell
        CONNECTION_STRING=<connection-string>

        az storage file generate-sas
            --name javasdksas
            --expiry 2019-06-05
            --permission rpau
            --connection-string $CONNECTION_STRING
        ```

    * Alternatively, get the Account SAS Token from the Azure Portal.
        1. Go to your storage account.
        1. Click on "Shared access signature".
        1. Click on "Generate SAS and connection string".

- **Shared Key Credential**
    * There are two ways to create a shared key credential, the first is using the storage account name and account key. The second is using the storage connection string.
        1. Use account name and account key.
            1. The account name is your storage account name.
            1. Go to your storage account.
            1. Select "Access keys" tab.
            1. Copy the "Key" value for either Key 1 or Key 2.
        1. Use the connection string
            1. Go to your storage account.
            1. Select "Access keys" tab.
            1. Copy the "Connection string" value for either Key 1 or Key 2.

## Key concepts

### URL format
File Shares are addressable using the following URL format:

```
https://<storage account>.file.core.windows.net/<share>
```

The following URL addresses a queue in the diagram:

```
https://myaccount.file.core.windows.net/images-to-download
```

#### Resource URI Syntax
For the storage account, the base URI for queue operations includes the name of the account only:

```
https://myaccount.file.core.windows.net
```

For file, the base URI includes the name of the account and the name of the directory/file:

```
https://myaccount.file.core.windows.net/myshare/mydirectorypath/myfile
```

### Handling Exceptions
Uses the `fileServiceClient` generated from [File Share Service Client](#share-service) section below.

```java
try {
    shareServiceClient.createShare("myShare");
} catch (StorageException e) {
    logger.error("Failed to create a share with error code: " + e.getErrorCode());
}
```

### Resource Names
The URI to reference a share, directory or file must be unique. Within a given storage account, every share must have a unique name. Every file within a given share or directory must also have a unique name within that share or directory.

If you attempt to create a share, directory, or file with a name that violates naming rules, the request will fail with status code 400 (Bad Request).

### Share Names
The rules for File Share service names are more restrictive than what is prescribed by the SMB protocol for SMB share names, so that the Blob and File services can share similar naming conventions for containers and shares. The naming restrictions for shares are as follows:

1. A share name must be a valid DNS name.
1. Share names must start with a letter or number, and can contain only letters, numbers, and the dash (-) character.
1. Every dash (-) character must be immediately preceded and followed by a letter or number; consecutive dashes are not permitted in share names.
1. All letters in a share name must be lowercase.
1. Share names must be from 3 through 63 characters long.

### Directory and File Names
The Azure File Share service naming rules for directory and file names are as follows:

1. Share Directory and file names are case-preserving and case-insensitive.
1. Share Directory and file component names must be no more than 255 characters in length.
1. Share Directory names cannot end with the forward slash character (/). If provided, it will be automatically removed.
1. Share File names must not end with the forward slash character (/).
1. Reserved URL characters must be properly escaped.
1. The following characters are not allowed: `" \ / : | < > * ?`
1. Illegal URL path characters not allowed. Code points like \uE000, while valid in NTFS filenames, are not valid Unicode characters. In addition, some ASCII or Unicode characters, like control characters (0x00 to 0x1F, \u0081, etc.), are also not allowed. For rules governing Unicode strings in HTTP/1.1 see [RFC 2616, Section 2.2: Basic Rules][RFC_URL_1] and [RFC 3987][RFL_URL_2].
1. The following file names are not allowed: LPT1, LPT2, LPT3, LPT4, LPT5, LPT6, LPT7, LPT8, LPT9, COM1, COM2, COM3, COM4, COM5, COM6, COM7, COM8, COM9, PRN, AUX, NUL, CON, CLOCK$, dot character (.), and two dot characters (..).

### Metadata Names
Metadata for a share or file resource is stored as name-value pairs associated with the resource. Directories do not have metadata. Metadata names must adhere to the naming rules for [C# identifiers][csharp_identifiers].

Note that metadata names preserve the case with which they were created, but are case-insensitive when set or read. If two or more metadata headers with the same name are submitted for a resource, the Azure File service returns status code 400 (Bad Request).

### Share Services
The File Share Service REST API provides operations on accounts and manage file service properties. It allows the operations of listing and deleting shares, getting and setting file service properties.
Once you have the SASToken, you can construct the file service client with `${accountName}`, `${sasToken}`

```java
String shareServiceURL = String.format("https://%s.file.core.windows.net", accountName);
ShareServiceClient shareServiceClient = new ShareServiceClientBuilder().endpoint(fileServiceURL)
    .sasToken(sasToken).buildClient();
```

### Share
The share resource includes metadata and properties for that share. It allows the opertions of creating, creating snapshot, deleting shares, getting share properties, setting metadata, getting and setting ACL (Access policy).
Once you have the SASToken, you can construct the file service client with `${accountName}`, `${shareName}`, `${sasToken}`

```java
String shareURL = String.format("https://%s.file.core.windows.net", accountName);
ShareClient shareClient = new ShareClientBuilder().endpoint(shareURL)
    .sasToken(sasToken).shareName(shareName).buildClient();
```

### Directory
 The directory resource includes the properties for that directory. It allows the operations of creating, listing, deleting directories or subdirectories or files, getting properties, setting metadata, listing and force closing the handles.
 Once you have the SASToken, you can construct the file service client with `${accountName}`, `${shareName}`, `${directoryPath}`, `${sasToken}`

```java
String directoryURL = String.format("https://%s.file.core.windows.net/%s%s", accountName, shareName, directoryPath, sasToken);
ShareDirectoryClient directoryClient = new ShareFileClientBuilder().endpoint(directoryURL)
    .sasToken(sasToken).shareName(shareName).directoryName(directoryPath).buildDirectoryClient();
```

### File
 The file resource includes the properties for that file. It allows the operations of creating, uploading, copying, downloading, deleting files or range of the files, getting properties, setting metadata, listing and force closing the handles.
 Once you have the SASToken, you can construct the file service client with `${accountName}`, `${shareName}`, `${directoryPath}`, `${fileName}`, `${sasToken}`

```java
String fileURL = String.format("https://%s.file.core.windows.net", accountName);
ShareFileClient fileClient = new ShareFileClientBuilder().endpoint(fileURL)
    .sasToken(sasToken).shareName(shareName).filePath(directoryPath + "/" + fileName).buildFileClient();
```

## Examples

The following sections provide several code snippets covering some of the most common Configuration Service tasks, including:
- [Create a Share](#create-a-share)
- [Create a snapshot on Share](#create-a-snapshot-on-share)
- [Create a Directory](#create-a-directory)
- [Create a Subdirectory](#create-a-subdirectory)
- [Create a File](#create-a-file)
- [List all Shares](#list-all-shares)
- [List all Subdirectories and Files](#list-all-subdirectories-and-files)
- [List all ranges on file](#list-all-ranges-on-file)
- [Delete a Share](#delete-a-share)
- [Delete a Directory](#delete-a-directory)
- [Delete a Subdirectory](#delete-a-subdirectory)
- [Delete a File](#delete-a-file)
- [Copy a File](#copy-a-file)
- [Abort copy a File](#Abort-copy-a-file)
- [Upload data to Storage File](#upload-data-to-storage)
- [Upload file to Storage File](#upload-file-to-storage)
- [Download data from file range](#download-data-from-file-range)
- [Download file from Storage File](#download-file-from-storage)
- [Get a file service properties](#get-a-file-service-properties)
- [Set a file service properties](#set-a-file-service-properties)
- [Set a Share metadata](#Set-a-share-metadata)
- [Get a Share access policy](#Get-a-share-access-policy)
- [Set a Share access policy](#Set-a-share-access-policy)
- [Get handles on Directory and File](#Get-handles-on-directory-file)
- [Force close handles on handle id](#Force-close-handles-on-handle-id)
- [Set quota on Share](#Set-quota-on-share)
- [Set file httpHeaders](#Set-file-httpheaders)

### Create a share
Create a share in the Storage Account. Throws StorageException If the share fails to be created.
Taking a ShareServiceClient in KeyConcept, [`${fileServiceClient}`](#share-services).

```Java
String shareName = "testshare";
shareServiceClient.createShare(shareName);
```

### Create a snapshot on Share
Taking a ShareServiceClient in KeyConcept, [`${fileServiceClient}`](#share-services).

```Java
String shareName = "testshare";
ShareClient shareClient = shareServiceClient.getShareClient(shareName);
shareClient.createSnapshot();
```

### Create a directory
Taking the [`${shareClient}](#create-a-snapshot-on-share) initialized above, [`${shareClient}`](#share).

```Java
String dirName = "testdir";
shareClient.createDirectory(dirName);
```

### Create a subdirectory
Taking the directoryClient in KeyConcept, [`${directoryClient}`](#Directory).

```Java
String subDirName = "testsubdir";
directoryClient.createSubdirectory(subDirName);
```

### Create a File
Taking the directoryClient in KeyConcept, [`${directoryClient}`](#Directory) .

```Java
String fileName = "testfile";
long maxSize = 1024;
directoryClient.createFile(fileName, maxSize);
```

### List all Shares
Taking the fileServiceClient in KeyConcept, [`${fileServiceClient}`](#share-services)

```Java
shareServiceClient.listShares();
```

### List all subdirectories and files
Taking the directoryClient in KeyConcept, [`${directoryClient}`](#Directory)

```Java
directoryClient.listFilesAndDirectories();
```

### List all ranges on file
Taking the fileClient in KeyConcept, [`${fileClient}`](#File)

```Java
fileClient.listRanges();
```

### Delete a share
Taking the shareClient in KeyConcept, [`${shareClient}`](#Share)

```Java
shareClient.delete();
```

### Delete a directory
Taking the shareClient in KeyConcept, [`${shareClient}`](#Share) .

```Java
String dirName = "testdir";
shareClient.deleteDirectory(dirName);
```

### Delete a subdirectory
Taking the directoryClient in KeyConcept, [`${directoryClient}`](#Directory) .

```Java
String subDirName = "testsubdir";
directoryClient.deleteSubdirectory(subDirName);
```

### Delete a file
Taking the directoryClient in KeyConcept, [`${directoryClient}`](#Directory) .

```Java
String fileName = "testfile";
directoryClient.deleteFile(fileName);
```

### Copy a file
Taking the fileClient in KeyConcept, [`${fileClient}`](#File) with string of source URL.

```Java
String sourceURL = "https://myaccount.file.core.windows.net/myshare/myfile";
FileCopyInfo copyInfo = fileClient.startCopy(sourceURL, null);
```

### Abort copy a file
Taking the fileClient in KeyConcept, [`${fileClient}`](#File) with the copy info response returned above `${copyId}=[copyInfoResponse](#Copy-a-file)`.

```Java
String copyId = copyInfoResponse.copyId();
fileClient.abortCopy(copyId);
```

### Upload data to storage
Taking the fileClient in KeyConcept, [`${fileClient}`](#File) with data of "default" .

```Java
String uploadText = "default";
ByteBuffer data = ByteBuffer.wrap(uploadText.getBytes(StandardCharsets.UTF_8));
fileClient.upload(data, uploadText.length());
```

### Upload file to storage
Taking the fileClient in KeyConcept, [`${fileClient}`](#File) .
```Java
String filePath = "/mydir/myfile";
fileClient.uploadFromFile(filePath);
```

### Download data from file range
Taking the fileClient in KeyConcept, [`${fileClient}`](#File) with the range from 1024 to 2048.
```Java
FileRange fileRange = new FileRange(1024L, 2047L);
fileClient.downloadWithPropertiesWithResponse(fileRange, false, null, Context.NONE);
```

### Download file from storage
Taking the fileClient in KeyConcept, [`${fileClient}`](#File) and download to the file of filePath.
```Java
String filePath = "/mydir/myfile";
fileClient.downloadToFile(filePath);
```

### Get a file service properties
Taking a FileServiceClient in KeyConcept, [`${fileServiceClient}`](#share-services) .

```Java
shareServiceClient.getProperties();
```

### Set a file service properties
Taking a FileServiceClient in KeyConcept, [`${fileServiceClient}`](#share-services) .

```Java
FileServiceProperties properties = shareServiceClient.getProperties();

properties.getMinuteMetrics().setEnabled(true);
properties.getHourMetrics().setEnabled(true);

shareServiceClient.setProperties(properties);
```

### Set a share metadata
Taking the shareClient in KeyConcept, [`${shareClient}`](#Share) .

```Java
Map<String, String> metadata = Collections.singletonMap("directory", "metadata");
shareClient.setMetadata(metadata);
```

### Get a share access policy
Taking the shareClient in KeyConcept, [`${shareClient}`](#Share)

```Java
shareClient.getAccessPolicy();
```

### Set a share access policy
Taking the shareClient in KeyConcept, [`${shareClient}`](#Share) .

```java
AccessPolicy accessPolicy = new AccessPolicy().setPermission("r")
    .setStart(OffsetDateTime.now(ZoneOffset.UTC))
    .setExpiry(OffsetDateTime.now(ZoneOffset.UTC).plusDays(10));

SignedIdentifier permission = new SignedIdentifier().setId("mypolicy").setAccessPolicy(accessPolicy);
shareClient.setAccessPolicy(Collections.singletonList(permission));
```

### Get handles on directory file
Taking the directoryClient in KeyConcept, [`${directoryClient}`](#Directory)

```Java
PagedIterable<HandleItem> handleItems = directoryClient.listHandles(null, true, Duration.ofSeconds(30), Context.NONE);
```

### Force close handles on handle id
Taking the directoryClient in KeyConcept, [`${directoryClient}`](#Directory) and the handle id returned above `${handleId}=[handleItems](#Get-handles-on-directory-file)`

```Java
String handleId = handleItems.iterator().next().getHandleId();
directoryClient.forceCloseHandles(handleId, true, Duration.ofSeconds(30), Context.NONE);
```

### Set quota on share
Taking the shareClient in KeyConcept, [`${shareClient}`](#Share) .

```Java
int quotaOnGB = 1;
shareClient.setQuota(quotaOnGB);
```

### Set file httpheaders
Taking the fileClient in KeyConcept, [`${fileClient}`](#File) .

```Java
FileHTTPHeaders httpHeaders = new FileHTTPHeaders().setFileContentType("text/plain");
long newFileSize = 1024;
fileClient.setHttpHeaders(newFileSize, httpHeaders);
```

## Troubleshooting

## General

When you interact with file using this Java client library, errors returned by the service correspond to the same HTTP status codes returned for [REST API][storage_file_rest] requests. For example, if you try to retrieve a share that doesn't exist in your Storage Account, a `404` error is returned, indicating `Not Found`.

## Next steps

## Contributing
This project welcomes contributions and suggestions.  Most contributions require you to agree to a
Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us
the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide
a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions
provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/).
For more information see the [Code of Conduct FAQ](https://opensource.microsoft.com/codeofconduct/faq/) or
contact [opencode@microsoft.com](mailto:opencode@microsoft.com) with any additional questions or comments.

If you would like to become an active contributor to this project please follow the instructions provided in [Microsoft Azure Projects Contribution Guidelines](http://azure.github.io/guidelines.html).

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

<!-- LINKS -->
[source_code]: src/
[reference_docs]: https://azure.github.io/azure-sdk-for-java/
[rest_api_documentation]: https://docs.microsoft.com/rest/api/storageservices/file-service-rest-api
[storage_docs]: https://docs.microsoft.com/azure/storage/files/storage-files-introduction
[jdk]: https://docs.microsoft.com/java/azure/jdk/
[maven]: https://maven.apache.org/
[azure_subscription]: https://azure.microsoft.com/free/
[storage_account]: https://docs.microsoft.com/azure/storage/common/storage-quickstart-create-account?tabs=azure-portal
[azure_cli]: https://docs.microsoft.com/cli/azure
[sas_token]: https://docs.microsoft.com/azure/storage/common/storage-dotnet-shared-access-signature-part-1
[RFC_URL_1]: https://www.ietf.org/rfc/rfc2616.txt
[RFL_URL_2]: https://www.ietf.org/rfc/rfc3987.txt
[csharp_identifiers]: https://docs.microsoft.com/dotnet/csharp/language-reference/
[storage_file_rest]: https://docs.microsoft.com/rest/api/storageservices/file-service-error-codes
[samples]: src/samples
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fstorage%2Fazure-storage-file-share%2FREADME.png)
