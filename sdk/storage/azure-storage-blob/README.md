# Azure Storage Blob client library for Java

Azure Blob Storage is Microsoft's object storage solution for the cloud. Blob
Storage is optimized for storing massive amounts of unstructured data.
Unstructured data is data that does not adhere to a particular data model or
definition, such as text or binary data.

[Source code][source] | [API reference documentation][docs] | [REST API documentation][rest_docs] | [Product documentation][product_docs] | [Samples][samples]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]
- [Create Storage Account][storage_account]

### Include the package

[//]: # ({x-version-update-start;com.azure:azure-storage-blob;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-storage-blob</artifactId>
    <version>12.8.0</version>
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

Your storage account URL, subsequently identified as <your-storage-account-url>, would be formatted as follows
http(s)://<storage-account-name>.blob.core.windows.net

### Authenticate the client

In order to interact with the Storage Service (Blob, Queue, Message, MessageId, File) you'll need to create an instance of the Service Client class.
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

Blob Storage is designed for:

- Serving images or documents directly to a browser
- Storing files for distributed access
- Streaming video and audio
- Writing to log files
- Storing data for backup and restore, disaster recovery, and archiving
- Storing data for analysis by an on-premises or Azure-hosted service

### URL format
Blobs are addressable using the following URL format:
The following URL addresses a blob:
```
https://myaccount.blob.core.windows.net/mycontainer/myblob
```

#### Resource URI Syntax
For the storage account, the base URI for blob operations includes the name of the account only:

```
https://myaccount.blob.core.windows.net
```

For a container, the base URI includes the name of the account and the name of the container:

```
https://myaccount.blob.core.windows.net/mycontainer
```

For a blob, the base URI includes the name of the account, the name of the container and the name of the blob:

```
https://myaccount.blob.core.windows.net/mycontainer/myblob
```

Note that the above URIs may not hold for more advanced scenarios such as custom domain names.

## Examples

The following sections provide several code snippets covering some of the most common Azure Storage Blob tasks, including:

- [Create a `BlobServiceClient`](#create-a-blobserviceclient)
- [Create a `BlobContainerClient`](#create-a-blobcontainerclient)
- [Create a `BlobClient`](#create-a-blobclient)
- [Create a container](#create-a-container)
- [Upload a blob from a stream](#upload-a-blob-from-a-stream)
- [Upload a blob from local path](#upload-a-blob-from-local-path)
- [Download a blob to a stream](#download-a-blob-to-a-stream)
- [Download a blob to local path](#download-a-blob-to-local-path)
- [Enumerate blobs](#enumerate-blobs)
- [Authenticate with Azure Identity](#authenticate-with-azure-identity)

### Create a `BlobServiceClient`

Create a `BlobServiceClient` using the [`sasToken`](#get-credentials) generated above.

<!-- embedme ./src/samples/java/com/azure/storage/blob/ReadmeSamples.java#L27-L30 -->
```java
BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
    .endpoint("<your-storage-account-url>")
    .sasToken("<your-sasToken>")
    .buildClient();
```

or

<!-- embedme ./src/samples/java/com/azure/storage/blob/ReadmeSamples.java#L34-L37 -->
```java
// Only one "?" is needed here. If the sastoken starts with "?", please removing one "?".
BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
    .endpoint("<your-storage-account-url>" + "?" + "<your-sasToken>")
    .buildClient();
```

### Create a `BlobContainerClient`

Create a `BlobContainerClient` using a `BlobServiceClient`.

<!-- embedme ./src/samples/java/com/azure/storage/blob/ReadmeSamples.java#L41-L41 -->
```java
BlobContainerClient blobContainerClient = blobServiceClient.getBlobContainerClient("mycontainer");
```

Create a `BlobContainerClient` from the builder [`sasToken`](#get-credentials) generated above.

<!-- embedme ./src/samples/java/com/azure/storage/blob/ReadmeSamples.java#L45-L49 -->
```java
BlobContainerClient blobContainerClient = new BlobContainerClientBuilder()
    .endpoint("<your-storage-account-url>")
    .sasToken("<your-sasToken>")
    .containerName("mycontainer")
    .buildClient();
```

or

<!-- embedme ./src/samples/java/com/azure/storage/blob/ReadmeSamples.java#L53-L56 -->
```java
// Only one "?" is needed here. If the sastoken starts with "?", please removing one "?".
BlobContainerClient blobContainerClient = new BlobContainerClientBuilder()
    .endpoint("<your-storage-account-url>" + "/" + "mycontainer" + "?" + "<your-sasToken>")
    .buildClient();
```

### Create a `BlobClient`

Create a `BlobClient` using a `BlobContainerClient`.

<!-- embedme ./src/samples/java/com/azure/storage/blob/ReadmeSamples.java#L60-L60 -->
```java
BlobClient blobClient = blobContainerClient.getBlobClient("myblob");
```

or

Create a `BlobClient` from the builder [`sasToken`](#get-credentials) generated above.

<!-- embedme ./src/samples/java/com/azure/storage/blob/ReadmeSamples.java#L64-L69 -->
```java
BlobClient blobClient = new BlobClientBuilder()
    .endpoint("<your-storage-account-url>")
    .sasToken("<your-sasToken>")
    .containerName("mycontainer")
    .blobName("myblob")
    .buildClient();
```

or

<!-- embedme ./src/samples/java/com/azure/storage/blob/ReadmeSamples.java#L73-L76 -->
```java
// Only one "?" is needed here. If the sastoken starts with "?", please removing one "?".
BlobClient blobClient = new BlobClientBuilder()
    .endpoint("<your-storage-account-url>" + "/" + "mycontainer" + "/" + "myblob" + "?" + "<your-sasToken>")
    .buildClient();
```

### Create a container

Create a container using a `BlobServiceClient`.

<!-- embedme ./src/samples/java/com/azure/storage/blob/ReadmeSamples.java#L80-L80 -->
```java
blobServiceClient.createBlobContainer("mycontainer");
```

or

Create a container using a `BlobContainerClient`.

<!-- embedme ./src/samples/java/com/azure/storage/blob/ReadmeSamples.java#L84-L84 -->
```java
blobContainerClient.create();
```

### Upload a blob from a stream

Upload from an `InputStream` to a blob using a `BlockBlobClient` generated from a `BlobContainerClient`.

<!-- embedme ./src/samples/java/com/azure/storage/blob/ReadmeSamples.java#L88-L94 -->
```java
BlockBlobClient blockBlobClient = blobContainerClient.getBlobClient("myblockblob").getBlockBlobClient();
String dataSample = "samples";
try (ByteArrayInputStream dataStream = new ByteArrayInputStream(dataSample.getBytes())) {
    blockBlobClient.upload(dataStream, dataSample.length());
} catch (IOException e) {
    e.printStackTrace();
}
```

### Upload a blob from local path

Upload a file to a blob using a `BlobClient` generated from a `BlobContainerClient`.

<!-- embedme ./src/samples/java/com/azure/storage/blob/ReadmeSamples.java#L98-L99 -->
```java
BlobClient blobClient = blobContainerClient.getBlobClient("myblockblob");
blobClient.uploadFromFile("local-file.jpg");
```

### Download a blob to a stream

Download a blob to an `OutputStream` using a `BlobClient`.

<!-- embedme ./src/samples/java/com/azure/storage/blob/ReadmeSamples.java#L103-L107 -->
```java
try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
    blobClient.download(outputStream);
} catch (IOException e) {
    e.printStackTrace();
}
```

### Download a blob to local path

Download blob to a local file using a `BlobClient`.

<!-- embedme ./src/samples/java/com/azure/storage/blob/ReadmeSamples.java#L111-L111 -->
```java
blobClient.downloadToFile("downloaded-file.jpg");
```

### Enumerate blobs

Enumerating all blobs using a `BlobContainerClient`.

<!-- embedme ./src/samples/java/com/azure/storage/blob/ReadmeSamples.java#L115-L117 -->
```java
for (BlobItem blobItem : blobContainerClient.listBlobs()) {
    System.out.println("This is the blob name: " + blobItem.getName());
}
```

### Authenticate with Azure Identity

The [Azure Identity library][identity] provides Azure Active Directory support for authenticating with Azure Storage.

<!-- embedme ./src/samples/java/com/azure/storage/blob/ReadmeSamples.java#L121-L124 -->
```java
BlobServiceClient blobStorageClient = new BlobServiceClientBuilder()
    .endpoint("<your-storage-account-url>")
    .credential(new DefaultAzureCredentialBuilder().build())
    .buildClient();
```

## Troubleshooting

When interacting with blobs using this Java client library, errors returned by the service correspond to the same HTTP
status codes returned for [REST API][error_codes] requests. For example, if you try to retrieve a container or blob that
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

Several Storage blob Java SDK samples are available to you in the SDK's GitHub repository. These samples provide example code for additional scenarios commonly encountered while working with Key Vault:

## Next steps Samples
Samples are explained in detail [here][samples_readme].

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a [Contributor License Agreement (CLA)][cla] declaring that you have the right to, and actually do, grant us the rights to use your contribution.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct][coc]. For more information see the [Code of Conduct FAQ][coc_faq] or contact [opencode@microsoft.com][coc_contact] with any additional questions or comments.

<!-- LINKS -->
[source]: src
[samples_readme]: src/samples/README.md
[docs]: http://azure.github.io/azure-sdk-for-java/
[rest_docs]: https://docs.microsoft.com/rest/api/storageservices/blob-service-rest-api
[product_docs]: https://docs.microsoft.com/azure/storage/blobs/storage-blobs-overview
[sas_token]: https://docs.microsoft.com/azure/storage/common/storage-dotnet-shared-access-signature-part-1
[jdk]: https://docs.microsoft.com/java/azure/jdk/
[azure_subscription]: https://azure.microsoft.com/free/
[storage_account]: https://docs.microsoft.com/azure/storage/common/storage-quickstart-create-account?tabs=azure-portal
[storage_account_create_cli]: https://docs.microsoft.com/azure/storage/common/storage-quickstart-create-account?tabs=azure-cli
[storage_account_create_portal]: https://docs.microsoft.com/azure/storage/common/storage-quickstart-create-account?tabs=azure-portal
[identity]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/identity/azure-identity/README.md
[error_codes]: https://docs.microsoft.com/rest/api/storageservices/blob-service-error-codes
[samples]: src/samples
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fstorage%2Fazure-storage-blob%2FREADME.png)
