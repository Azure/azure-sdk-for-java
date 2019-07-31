# Azure File client library for Java
The Server Message Block (SMB) protocol is the preferred file share protocol used on-premises today. 
The Microsoft Azure File service enables customers to leverage the availability and scalability of Azure’s Cloud Infrastructure as a Service (IaaS) SMB without having to rewrite SMB client applications.

Files stored in Azure File service shares are accessible via the SMB protocol, and also via REST APIs. 
The File service offers the following four resources: the storage account, shares, directories, and files. 
Shares provide a way to organize sets of files and also can be mounted as an SMB file share that is hosted in the cloud.

[Source code][source_code] | [Package (Maven)][package] | [API reference documentation][api_documentation] | [Product documentation][azconfig_docs]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]
- [Create Strorage Account][storage_account]

### Adding the package to your product

```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-storage-file</artifactId>
  <version>12.0.0-preview.2</version>
</dependency>
```

### Create a Storage Account
To create a Storage Account you can use the Azure Portal or [Azure CLI][azure_cli].

```Powershell
az group create \
    --name storage-resource-group \
    --location westus
```

### Authenticate the client

In order to interact with the Storage service (File Service, Share, Directory, MessageId, File) you'll need to create an instance of the Service Client class. 
To make this possible you'll need the Account SAS (shared access signature) string of Storage account. Learn more at [SAS Token][sas_token]

#### Get Credentials

- **SAS Token**
 
a. Use the [Azure CLI][azure_cli] snippet below to get the SAS token from the Storage account.

```Powershell
az storage file generate-sas
    --name {account name}
    --expiry {date/time to expire SAS token}
    --permission {permission to grant}
    --connection-string {connection string of the storage account}
```

```Powershell
CONNECTION_STRING=<connection-string>

az storage file generate-sas
    --name javasdksas
    --expiry 2019-06-05
    --permission rpau
    --connection-string $CONNECTION_STRING
```
b. Alternatively, get the Account SAS Token from the Azure Portal.
```
Go to your storage account -> Shared access signature -> Click on Generate SAS and connection string 
```

- **Shared Key Credential**

a. Use account name and account key. Account name is your storage account name.
```
// Here is where we get the key
Go to your storage account -> Access keys -> Key 1/ Key 2 -> Key
```
b. Use the connection string
```
// Here is where we get the key
Go to your storage account -> Access Keys -> Keys 1/ Key 2 -> Connection string
```
## Key concepts
### URL format
Files are addressable using the following URL format:
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
```java
TODO
```

### Resource Names
The URI to reference a share, directory or file must be unique. Within a given storage account, every share must have a unique name. Every file within a given share or directory must also have a unique name within that share or directory.

If you attempt to create a share, directory, or file with a name that violates naming rules, the request will fail with status code 400 (Bad Request).

### Share Names
The rules for File service share names are more restrictive than what is prescribed by the SMB protocol for SMB share names, so that the Blob and File services can share similar naming conventions for containers and shares. The naming restrictions for shares are as follows:

1. A share name must be a valid DNS name.
1. Share names must start with a letter or number, and can contain only letters, numbers, and the dash (-) character.
1. Every dash (-) character must be immediately preceded and followed by a letter or number; consecutive dashes are not permitted in share names.
1. All letters in a share name must be lowercase.
1. Share names must be from 3 through 63 characters long.

### Directory and File Names
The Azure File service naming rules for directory and file names are as follows:

1. Directory and file names are case-preserving and case-insensitive.
1. Directory and file component names must be no more than 255 characters in length.
1. Directory names cannot end with the forward slash character (/). If provided, it will be automatically removed.
1. File names must not end with the forward slash character (/).
1. Reserved URL characters must be properly escaped.
1. The following characters are not allowed: " \ / : | < > * ?
1. Illegal URL path characters not allowed. Code points like \uE000, while valid in NTFS filenames, are not valid Unicode characters. In addition, some ASCII or Unicode characters, like control characters (0x00 to 0x1F, \u0081, etc.), are also not allowed. For rules governing Unicode strings in HTTP/1.1 see [RFC 2616, Section 2.2: Basic Rules][RFC_URL_1] and [RFC 3987][RFL_URL_2].
1. The following file names are not allowed: LPT1, LPT2, LPT3, LPT4, LPT5, LPT6, LPT7, LPT8, LPT9, COM1, COM2, COM3, COM4, COM5, COM6, COM7, COM8, COM9, PRN, AUX, NUL, CON, CLOCK$, dot character (.), and two dot characters (..).

### Metadata Names
Metadata for a share or file resource is stored as name-value pairs associated with the resource. Directories do not have metadata. Metadata names must adhere to the naming rules for [C# identifiers][C_identifiers].

Note that metadata names preserve the case with which they were created, but are case-insensitive when set or read. If two or more metadata headers with the same name are submitted for a resource, the Azure File service returns status code 400 (Bad Request).

### File Services
The File Service REST API provides operations on accounts and manage file service properties. It allows the operations of listing and deleting shares, getting and setting file service properties.
Once you have the SASToken, you can construct the file service client with `${accountName}`, `${sasToken}`

```
String fileServiceURL = String.format("https://%s.file.core.windows.net", accountName);
FileServiceClient fileServiceClient = new FileServiceClientBuilder().endpoint(fileServiceURL)
    .credential(sasToken).buildClient();
```

### Share 
The share resource includes metadata and properties for that share. It allows the opertions of creating, creating snapshot, deleting shares, getting share properties, setting metadata, getting and setting ACL (Access policy).
Once you have the SASToken, you can construct the file service client with `${accountName}`, `${shareName}`, `${sasToken}`

```
String shareURL = String.format("https://%s.file.core.windows.net", accountName);
ShareClient shareClient = new ShareClientBuilder().endpoint(shareURL)
    .credential(sasToken).shareName(shareName).buildClient();
```

### Directory
 The directory resource includes the properties for that directory. It allows the operations of creating, listing, deleting directories or subdirectories or files, getting properties, setting metadata, listing and force closing the handles.
 Once you have the SASToken, you can construct the file service client with `${accountName}`, `${shareName}`, `${directoryPath}`, `${sasToken}`
 
 ```
 String directoryURL = String.format("https://%s.file.core.windows.net/%s%s", accountName, shareName, directoryPath, sasToken);
 DirectoryClient directoryClient = new DirectoryClientBuilder().endpoint(directoryURL)
    .credential(sasToken).shareName(shareName).directoryName(directoryPath).buildClient();
 ```
### File
 The file resource includes the properties for that file. It allows the operations of creating, uploading, copying, downloading, deleting files or range of the files, getting properties, setting metadata, listing and force closing the handles.
 Once you have the SASToken, you can construct the file service client with `${accountName}`, `${shareName}`, `${directoryPath}`, `${fileName}`, `${sasToken}`
 
 ```
 String fileURL = String.format("https://%s.file.core.windows.net", accountName);
 FileClient fileClient = new FileClientBuilder().endpoint(fileURL)
    .credential(sasToken).shareName(shareName).filePath(directoryPath + "/" + fileName).buildClient();
 ```

## Examples

The following sections provide several code snippets covering some of the most common Configuration Service tasks, including:
- [Create a Share](#Create-a-share)
- [Create a snapshot on Share](#Create-a-snapshot-on-share)
- [Create a Directory](#Create-a-directory)
- [Create a Subdirectory](#Create-a-subdirectory)
- [Create a File](#Create-a-file)
- [List all Shares](#List-all-shares)
- [List all Subdirectories and Files](#List-all-subdirectories-and-files)
- [List all ranges on file](#List-all-ranges-on-file)
- [Delete a Share](#Delete-a-share)
- [Delete a Directory](#Delete-a-directory)
- [Delete a Subdirectory](#Delete-a-subdirectory)
- [Delete a File](#Delete-a-file)
- [Copy a File](#Copy-a-file)
- [Abort copy a File](#Abort-copy-a-file)
- [Upload data to Storage File](#Upload-data-to-storage)
- [Upload file to Storage File](#Upload-file-to-storage)
- [Download data from Storage File](#Download-data-from-storage)
- [Download file from Storage File](#Download-file-from-storage)
- [Get a File Service property](#Get-a-file-service-property)
- [Set a File Service property](#set-a-file-service-property)
- [Set a Share metadata](#Set-a-share-metadata)
- [Get a Share access policy](#Get-a-share-access-policy)
- [Set a Share access policy](#Set-a-share-access-policy)
- [Get handles on Directory and File](#Get-handles-on-directory-file)
- [Force close handles on handle id](#Force-close-handles-on-handle-id)
- [Set quota on Share](#Set-quota-on-share)
- [Set file httpHeaders](#Set-file-httpheaders)

### Create a share
Create a share in the Storage Account. Throws StorageErrorException If the share fails to be created.
Taking a FileServiceClient in KeyConcept, [`${fileServiceClient}`](#File-services) .

```Java
String shareName = "testshare";
fileServiceClient.createShare(shareName);
```

### Create a snapshot on Share
Taking a FileServiceClient in KeyConcept, [`${fileServiceClient}`](#File-services) .

```Java
String shareName = "testshare";
ShareClient shareClient = fileServiceClient.getShareClient(shareName);
shareClient.createSnapshot();
```

### Create a directory
Taking the [`${shareClient}](#Create-snapshot-on-share) initialized above, [`${shareClient}`](#Share) .

```Java
String dirName = "testdir";
shareClient.createDirectory(dirName);
```

### Create a subdirectory
Taking the directoryClient in KeyConcept, [`${directoryClient}`](#Directory) .

```Java
String subDirName = "testsubdir";
directoryClient.createSubDirectory(subDirName);
```

### Create a File
Taking the directoryClient in KeyConcept, [`${directoryClient}`](#Directory) .

```Java
String fileName = "testfile";
directoryClient.createFile(fileName);
```

### List all Shares
Taking the fileServiceClient in KeyConcept, [`${fileServiceClient}`](#File-services)

```Java
fileServiceClient.listShares();
```

### Create all subdirectories and files
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
shareClient.deleteDirectory(dirName)
```

### Delete a subdirectory
Taking the directoryClient in KeyConcept, [`${directoryClient}`](#Directory) .

```Java
String subDirName = "testsubdir";
directoryClient.deleteSubDirectory(subDirName)
```

### Delete a file
Taking the directoryClient in KeyConcept, [`${directoryClient}`](#Directory) .

```Java
String fileName = "testfile";
directoryClient.deleteFile(fileName)
```

### Copy a file
Taking the fileClient in KeyConcept, [`${fileClient}`](#File) with string of source URL.

```Java
String sourceURL = "https://myaccount.file.core.windows.net/myshare/myfile";
Response<FileCopyInfo> copyInfoResponse = fileClient.startCopy(sourceURL, null);
```

### Abort copy a file
Taking the fileClient in KeyConcept, [`${fileClient}`](#File) with the copy info response returned above `${copyId}=[copyInfoResponse](#Copy-a-file)`.

```Java
String copyId = copyInfoResponse.value().copyId();
fileClient.abortCopy(copyId);
```

### Upload data to storage
Taking the fileClient in KeyConcept, [`${fileClient}`](#File) with data of "default" .

```Java
ByteBuf data = Unpooled.wrappedBuffer("default".getBytes(StandardCharsets.UTF_8));
fileClient.upload(data, data.readableBytes());
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
FileRange fileRange = new FileRange(1024, 2047);
fileClient.downloadWithProperties(fileRange, false);
```

### Download file from storage
Taking the fileClient in KeyConcept, [`${fileClient}`](#File) and download to the file of filePath.
```Java
String filePath = "/mydir/myfile";
fileClient.downloadToFile(filePath);
```

### Get a file service properties
Taking a FileServiceClient in KeyConcept, [`${fileServiceClient}`](#File-services) .

```Java
fileServiceClient.getProperties();
```

### Set a file service properties
Taking a FileServiceClient in KeyConcept, [`${fileServiceClient}`](#File-services) .

```Java
FileServiceProperties properties = fileServiceClient.getProperties().value();

properties.minuteMetrics().enabled(true);
properties.hourMetrics().enabled(true);

VoidResponse response = fileServiceClient.setProperties(properties);
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

```Java
AccessPolicy accessPolicy = new AccessPolicy().permission("r")
            .start(OffsetDateTime.now(ZoneOffset.UTC))
            .expiry(OffsetDateTime.now(ZoneOffset.UTC).plusDays(10));

SignedIdentifier permission = new SignedIdentifier().id("mypolicy").accessPolicy(accessPolicy);
shareClient.setAccessPolicy(Collections.singletonList(permission));
```

### Get handles on directory file
Taking the directoryClient in KeyConcept, [`${directoryClient}`](#Directory)

```Java
Iterable<HandleItem> handleItems = directoryClient.getHandles(null, true);
```

### Force close handles on handle id
Taking the directoryClient in KeyConcept, [`${directoryClient}`](#Directory) and the handle id returned above `${handleId}=[handleItems](#Get-handles-on-directory-file)`

```Java
String handleId = result.iterator().next().handleId();
directoryClient.forceCloseHandles(handleId);
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
FileHTTPHeaders httpHeaders = new FileHTTPHeaders().fileContentType("text/plain");
fileClient.setHttpHeaders(httpHeaders);
```

## Troubleshooting

## General

When you interact with file using this Java client library, errors returned by the service correspond to the same HTTP status codes returned for [REST API][storage_file_rest] requests. For example, if you try to retrieve a share that doesn't exist in your Storage Account, a `404` error is returned, indicating `Not Found`.

## Next steps

### More Samples
- [FileServiceSample](src/samples/java/file/FileServiceSample.java)
- [ShareSample](src/samples/java/file/ShareSample.java)
- [DirectorySample](src/samples/java/file/DirectorySample.java)
- [FileSample](src/samples/java/file/FileSample.java)
- [AsyncSample](src/samples/java/file/AsyncSample.java)

[Quickstart: Create a Java Spring app with App Configuration](https://docs.microsoft.com/en-us/azure/azure-app-configuration/quickstart-java-spring-app)

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
[source_code]: to-be-continue
[package]: to-be-continue
[api_documentation]: https://docs.microsoft.com/en-us/rest/api/storageservices/file-service-rest-api
[storage_docs]: https://docs.microsoft.com/en-us/azure/storage/files/storage-files-introduction
[jdk]: https://docs.microsoft.com/en-us/java/azure/java-supported-jdk-runtime?view=azure-java-stable
[maven]: https://maven.apache.org/
[azure_subscription]: https://azure.microsoft.com/en-us/free/
[storage_account]: https://docs.microsoft.com/en-us/azure/storage/common/storage-quickstart-create-account?tabs=azure-portal
[azure_cli]: https://docs.microsoft.com/cli/azure
[sas_token]: https://docs.microsoft.com/en-us/azure/storage/common/storage-dotnet-shared-access-signature-part-1
[RFC_URL_1]: https://www.ietf.org/rfc/rfc2616.txt
[RFL_URL_2]: https://www.ietf.org/rfc/rfc3987.txt
[C_identifiers]: https://docs.microsoft.com/en-us/dotnet/csharp/language-reference/
[storage_file_rest]: https://docs.microsoft.com/en-us/rest/api/storageservices/file-service-error-codes
