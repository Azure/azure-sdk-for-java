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

### Include the package

#### Include the BOM file

Please include the azure-sdk-bom to your project to take dependency on GA version of the library. In the following snippet, replace the {bom_version_to_target} placeholder with the version number.
To learn more about the BOM, see the [AZURE SDK BOM README](https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/boms/azure-sdk-bom/README.md).

```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>com.azure</groupId>
            <artifactId>azure-sdk-bom</artifactId>
            <version>{bom_version_to_target}</version>
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>
```
and then include the direct dependency in the dependencies section without the version tag.

```xml
<dependencies>
  <dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-storage-file-share</artifactId>
  </dependency>
</dependencies>
```

#### Include direct dependency
If you want to take dependency on a particular version of the library that is not present in the BOM,
add the direct dependency to your project as follows.


[//]: # ({x-version-update-start;com.azure:azure-storage-file-share;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-storage-file-share</artifactId>
  <version>12.19.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Create a Storage Account
To create a Storage Account you can use the Azure Portal or [Azure CLI][azure_cli].

```bash
az storage account create \
    --resource-group <resource-group-name> \
    --name <storage-account-name> \
    --location <location>
```

### Authenticate the client

In order to interact with the Storage service (File Share Service, Share, Directory, MessageId, File) you'll need to create an instance of the Service Client class.
To make this possible you'll need the Account SAS (shared access signature) string of Storage account. Learn more at [SAS Token][sas_token]

#### Get Credentials

- **SAS Token**
    * Use the [Azure CLI][azure_cli] snippet below to get the SAS token from the Storage account.

        ```bash
        az storage file generate-sas
            --name {account name}
            --expiry {date/time to expire SAS token}
            --permission {permission to grant}
            --connection-string {connection string of the storage account}
        ```

        ```bash
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
Uses the `shareServiceClient` generated from [shareServiceClient](#share-services) section below.

```java readme-sample-handleException
try {
    shareServiceClient.createShare("myShare");
} catch (ShareStorageException e) {
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
Once you have the SASToken, you can construct the `shareServiceClient` with `${accountName}`, `${sasToken}`

```java readme-sample-createShareServiceClient
String shareServiceURL = String.format("https://%s.file.core.windows.net", ACCOUNT_NAME);
ShareServiceClient shareServiceClient = new ShareServiceClientBuilder().endpoint(shareServiceURL)
    .sasToken(SAS_TOKEN).buildClient();
```

### Share
The share resource includes metadata and properties for that share. It allows the operations of creating, creating snapshot, deleting shares, getting share properties, setting metadata, getting and setting ACL (Access policy). Getting and setting ACL (Access policy) can only be used by ShareClient with ConnectionString. 

#### Share With SASToken
Once you have the SASToken, you can construct the share client with `${accountName}`, `${shareName}`, `${sasToken}`

```java readme-sample-createShareClient
String shareURL = String.format("https://%s.file.core.windows.net", ACCOUNT_NAME);
ShareClient shareClient = new ShareClientBuilder().endpoint(shareURL)
    .sasToken(SAS_TOKEN).shareName(shareName).buildClient();
```

#### Share With ConnectionString
Once you have the ConnectionString, you can construct the share client with `${accountName}`, `${shareName}`, `${connectionString}`

```java readme-sample-createShareClientWithConnectionString
String shareURL = String.format("https://%s.file.core.windows.net", ACCOUNT_NAME);
ShareClient shareClient = new ShareClientBuilder().endpoint(shareURL)
    .connectionString(CONNECTION_STRING).shareName(shareName).buildClient();
```

#### Share with `TokenCredential`
Once you have the TokenCredential, you can construct the share client with `${accountName}`, `${shareName}` and `ShareTokenIntent`. 
`ShareTokenIntent.BACKUP` specifies requests that are intended for backup/admin type operations, meaning that all
file/directory ACLs are bypassed and full permissions are granted. User must have required RBAC permission in order to 
use `ShareTokenIntent.BACKUP`.

```java readme-sample-createShareClientWithTokenCredential
String shareURL = String.format("https://%s.file.core.windows.net", ACCOUNT_NAME);

ShareClient serviceClient = new ShareClientBuilder()
    .endpoint(shareURL)
    .credential(tokenCredential)
    .shareTokenIntent(ShareTokenIntent.BACKUP)
    .shareName(shareName)
    .buildClient();
```

### Directory
 The directory resource includes the properties for that directory. It allows the operations of creating, listing, deleting directories or subdirectories or files, getting properties, setting metadata, listing and force closing the handles.
 Once you have the SASToken, you can construct the file service client with `${accountName}`, `${shareName}`, `${directoryPath}`, `${sasToken}`

```java readme-sample-createDirectoryClient
String directoryURL = String.format("https://%s.file.core.windows.net", ACCOUNT_NAME);
ShareDirectoryClient directoryClient = new ShareFileClientBuilder().endpoint(directoryURL)
    .sasToken(SAS_TOKEN).shareName(shareName).resourcePath(directoryPath).buildDirectoryClient();
```

### File
 The file resource includes the properties for that file. It allows the operations of creating, uploading, copying, downloading, deleting files or range of the files, getting properties, setting metadata, listing and force closing the handles.
 Once you have the SASToken, you can construct the file service client with `${accountName}`, `${shareName}`, `${directoryPath}`, `${fileName}`, `${sasToken}`

```java readme-sample-createFileClient
String fileURL = String.format("https://%s.file.core.windows.net", ACCOUNT_NAME);
ShareFileClient fileClient = new ShareFileClientBuilder().connectionString(CONNECTION_STRING)
    .endpoint(fileURL).shareName(shareName).resourcePath(directoryPath + "/" + fileName).buildFileClient();
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
- [Abort copy a File](#abort-copy-a-file)
- [Upload data to Storage File](#upload-data-to-storage)
- [Upload data bigger than 4 MB to Storage File](#upload-data-bigger-than-4-mb-to-storage)
- [Upload file to Storage File](#upload-file-to-storage)
- [Download data from file range](#download-data-from-file-range)
- [Download file from Storage File](#download-file-from-storage)
- [Get a share service properties](#get-a-share-service-properties)
- [Set a share service properties](#set-a-share-service-properties)
- [Set a Share metadata](#set-a-share-metadata)
- [Get a Share access policy](#get-a-share-access-policy)
- [Set a Share access policy](#set-a-share-access-policy)
- [Get handles on Directory and File](#get-handles-on-directory-file)
- [Force close handles on handle id](#force-close-handles-on-handle-id)
- [Set quota on Share](#set-quota-on-share)
- [Set file httpHeaders](#set-file-httpheaders)

### Create a share
Create a share in the Storage Account. Throws StorageException If the share fails to be created.
Taking a ShareServiceClient in KeyConcept, [`${shareServiceClient}`](#share-services).

```java readme-sample-createShare
String shareName = "testshare";
shareServiceClient.createShare(shareName);
```

### Create a snapshot on Share
Taking a ShareServiceClient in KeyConcept, [`${shareServiceClient}`](#share-services).

```java readme-sample-createSnapshotOnShare
String shareName = "testshare";
ShareClient shareClient = shareServiceClient.getShareClient(shareName);
shareClient.createSnapshot();
```

### Create a directory
Taking the shareClient initialized above, [`${shareClient}`](#share).

```java readme-sample-createDirectory
String dirName = "testdir";
shareClient.createDirectory(dirName);
```

### Create a subdirectory
Taking the directoryClient in KeyConcept, [`${directoryClient}`](#directory).

```java readme-sample-createSubDirectory
String subDirName = "testsubdir";
directoryClient.createSubdirectory(subDirName);
```

### Create a File
Taking the directoryClient in KeyConcept, [`${directoryClient}`](#directory) .

```java readme-sample-createFile
String fileName = "testfile";
long maxSize = 1024;
directoryClient.createFile(fileName, maxSize);
```

### List all Shares
Taking the shareServiceClient in KeyConcept, [`${shareServiceClient}`](#share-services)

```java readme-sample-getShareList
shareServiceClient.listShares();
```

### List all subdirectories and files
Taking the directoryClient in KeyConcept, [`${directoryClient}`](#directory)

```java readme-sample-getSubDirectoryAndFileList
directoryClient.listFilesAndDirectories();
```

### List all ranges on file
Taking the fileClient in KeyConcept, [`${fileClient}`](#file)

```java readme-sample-getRangeList
fileClient.listRanges();
```

### Delete a share
Taking the shareClient in KeyConcept, [`${shareClient}`](#share-with-sastoken)

```java readme-sample-deleteShare
shareClient.delete();
```

### Delete a directory
Taking the shareClient in KeyConcept, [`${shareClient}`](#share-with-sastoken) .

```java readme-sample-deleteDirectory
String dirName = "testdir";
shareClient.deleteDirectory(dirName);
```

### Delete a subdirectory
Taking the directoryClient in KeyConcept, [`${directoryClient}`](#directory) .

```java readme-sample-deleteSubDirectory
String subDirName = "testsubdir";
directoryClient.deleteSubdirectory(subDirName);
```

### Delete a file
Taking the directoryClient in KeyConcept, [`${directoryClient}`](#directory) .

```java readme-sample-deleteFile
String fileName = "testfile";
directoryClient.deleteFile(fileName);
```

### Copy a file
Taking the fileClient in KeyConcept, [`${fileClient}`](#file) with string of source URL.

```java readme-sample-copyFile
String sourceURL = "https://myaccount.file.core.windows.net/myshare/myfile";
Duration pollInterval = Duration.ofSeconds(2);
SyncPoller<ShareFileCopyInfo, Void> poller = fileClient.beginCopy(sourceURL, (Map<String, String>) null, pollInterval);
```

### Abort copy a file
Taking the fileClient in KeyConcept, [`${fileClient}`](#file) with the copy info response returned above `${copyId}=[copyInfoResponse](#copy-a-file)`.

```java readme-sample-abortCopyFile
fileClient.abortCopy("copyId");
```

### Upload data to storage
Taking the fileClient in KeyConcept, [`${fileClient}`](#file) with data of "default" .

```java readme-sample-uploadDataToStorage
String uploadText = "default";
InputStream data = new ByteArrayInputStream(uploadText.getBytes(StandardCharsets.UTF_8));
fileClient.upload(data, uploadText.length());
```

### Upload data bigger than 4 MB to storage
Taking the fileClient in KeyConcept, [`${fileClient}`](#file) with data of "default" .

```java readme-sample-uploadDataToStorageBiggerThan4MB
byte[] data = "Hello, data sample!".getBytes(StandardCharsets.UTF_8);

long chunkSize = 4 * 1024 * 1024L;
if (data.length > chunkSize) {
    for (int offset = 0; offset < data.length; offset += chunkSize) {
        try {
            // the last chunk size is smaller than the others
            chunkSize = Math.min(data.length - offset, chunkSize);

            // select the chunk in the byte array
            byte[] subArray = Arrays.copyOfRange(data, offset, (int) (offset + chunkSize));

            // upload the chunk
            fileClient.uploadWithResponse(new ByteArrayInputStream(subArray), chunkSize, (long) offset, null, Context.NONE);
        } catch (RuntimeException e) {
            logger.error("Failed to upload the file", e);
            if (Boolean.TRUE.equals(fileClient.exists())) {
                fileClient.delete();
            }
            throw e;
        }
    }
} else {
    fileClient.upload(new ByteArrayInputStream(data), data.length);
}
```

### Upload file to storage
Taking the fileClient in KeyConcept, [`${fileClient}`](#file) .

```java readme-sample-uploadFileToStorage
String filePath = "${myLocalFilePath}";
fileClient.uploadFromFile(filePath);
```

### Download data from file range
Taking the fileClient in KeyConcept, [`${fileClient}`](#file) with the range from 1024 to 2048.

```java readme-sample-downloadDataFromFileRange
ShareFileRange fileRange = new ShareFileRange(0L, 2048L);
OutputStream stream = new ByteArrayOutputStream();
fileClient.downloadWithResponse(stream, fileRange, false, null, Context.NONE);
```

### Download file from storage
Taking the fileClient in KeyConcept, [`${fileClient}`](#file) and download to the file of filePath.

```java readme-sample-downloadFileFromFileRange
String filePath = "${myLocalFilePath}";
fileClient.downloadToFile(filePath);
```

### Get a share service properties
Taking a ShareServiceClient in KeyConcept, [`${shareServiceClient}`](#share-services) .

```java readme-sample-getShareServiceProperties
shareServiceClient.getProperties();
```

### Set a share service properties
Taking a ShareServiceClient in KeyConcept, [`${shareServiceClient}`](#share-services) .

```java readme-sample-setShareServiceProperties
ShareServiceProperties properties = shareServiceClient.getProperties();

properties.getMinuteMetrics().setEnabled(true).setIncludeApis(true);
properties.getHourMetrics().setEnabled(true).setIncludeApis(true);

shareServiceClient.setProperties(properties);
```

### Set a share metadata
Taking the shareClient in KeyConcept, [`${shareClient}`](#share-with-sastoken) .

```java readme-sample-setShareMetadata
Map<String, String> metadata = Collections.singletonMap("directory", "metadata");
shareClient.setMetadata(metadata);
```

### Get a share access policy
Taking the shareClient in KeyConcept, [`${shareClient}`](#share-with-connectionstring) .

```java readme-sample-getAccessPolicy
shareClient.getAccessPolicy();
```

### Set a share access policy
Taking the shareClient in KeyConcept, [`${shareClient}`](#share-with-connectionstring) .

```java readme-sample-setAccessPolicy
ShareAccessPolicy accessPolicy = new ShareAccessPolicy().setPermissions("r")
    .setStartsOn(OffsetDateTime.now(ZoneOffset.UTC))
    .setExpiresOn(OffsetDateTime.now(ZoneOffset.UTC).plusDays(10));
ShareSignedIdentifier permission = new ShareSignedIdentifier().setId("mypolicy").setAccessPolicy(accessPolicy);
shareClient.setAccessPolicy(Collections.singletonList(permission));
```

### Get handles on directory file
Taking the directoryClient in KeyConcept, [`${directoryClient}`](#directory)

```java readme-sample-getHandleList
PagedIterable<HandleItem> handleItems = directoryClient.listHandles(null, true, Duration.ofSeconds(30), Context.NONE);
```

### Force close handles on handle id
Taking the directoryClient in KeyConcept, [`${directoryClient}`](#directory) and the handle id returned above `${handleId}=[handleItems](#get-handles-on-directory-file)`

```java readme-sample-forceCloseHandleWithResponse
PagedIterable<HandleItem> handleItems = directoryClient.listHandles(null, true, Duration.ofSeconds(30), Context.NONE);
String handleId = handleItems.iterator().next().getHandleId();
directoryClient.forceCloseHandleWithResponse(handleId, Duration.ofSeconds(30), Context.NONE);
```

### Set quota on share
Taking the shareClient in KeyConcept, [`${shareClient}`](#share-with-sastoken) .

```java readme-sample-setQuotaOnShare
int quotaOnGB = 1;
shareClient.setPropertiesWithResponse(new ShareSetPropertiesOptions().setQuotaInGb(quotaOnGB), null, Context.NONE);
```

### Set file httpheaders
Taking the fileClient in KeyConcept, [`${fileClient}`](#file) .

```java readme-sample-setFileHttpHeaders
ShareFileHttpHeaders httpHeaders = new ShareFileHttpHeaders().setContentType("text/plain");
fileClient.setProperties(1024, httpHeaders, null, null);
```

## Troubleshooting

## General

When you interact with file using this Java client library, errors returned by the service correspond to the same HTTP status codes returned for [REST API][storage_file_rest] requests. For example, if you try to retrieve a share that doesn't exist in your Storage Account, a `404` error is returned, indicating `Not Found`.

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

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

<!-- LINKS -->
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/storage/azure-storage-file-share/src/
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
[samples]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/storage/azure-storage-file-share/src/samples
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fstorage%2Fazure-storage-file-share%2FREADME.png)
