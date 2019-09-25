# Azure Storage Blobs client library for Java

Azure Blob storage is Microsoft's object storage solution for the cloud. Blob
storage is optimized for storing massive amounts of unstructured data.
Unstructured data is data that does not adhere to a particular data model or
definition, such as text or binary data.

[Source code][source] | [API reference documentation][docs] | [REST API documentation][rest_docs] | [Product documentation][product_docs] | [Samples][samples]

## Getting started

### Prerequisites

-  Java Development Kit (JDK) with version 8 or above
- [Azure Subscription][azure_subscription]
- [Create Storage Account][storage_account]

### Adding the package to your product

```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-storage-blob</artifactId>
  <version>12.0.0-preview.3</version>
</dependency>
```

### Default HTTP Client
All client libraries support a pluggable HTTP transport layer. Users can specify an HTTP client specific for their needs by including the following dependency in the Maven pom.xml file:

```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core-http-netty</artifactId>
  <version>1.0.0-preview.4</version>
</dependency>
```

This will automatically configure all client libraries on the same classpath to make use of Netty for the HTTP client. Netty is the recommended HTTP client for most applications. OkHttp is recommended only when the application being built is deployed to Android devices.

If, instead of Netty it is preferable to use OkHTTP, there is a HTTP client available for that too. Simply include the following dependency instead:

```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core-http-okhttp</artifactId>
  <version>1.0.0-preview.4</version>
</dependency>
```

### Configuring HTTP Clients
When an HTTP client is included on the classpath, as shown above, it is not necessary to specify it in the client library [builders](#create-blobserviceclient), unless you want to customize the HTTP client in some fashion. If this is desired, the `httpClient` builder method is often available to achieve just this, by allowing users to provide a custom (or customized) `com.azure.core.http.HttpClient` instances.

For starters, by having the Netty or OkHTTP dependencies on your classpath, as shown above, you can create new instances of these `HttpClient` types using their builder APIs. For example, here is how you would create a Netty HttpClient instance:

```java
HttpClient client = new NettyAsyncHttpClientBuilder()
    .port(8080)
    .wiretap(true)
    .build();
```

### Create a Storage Account
To create a Storage Account you can use the Azure Portal or [Azure CLI][storage_account_create_cli].

```Powershell
az stoage account create \
    --resource-group <resource-group-name> \
    --name <storage-account-name> \
    --location <location>
```

### Authenticate the client

In order to interact with the Storage service (Blob, Queue, Message, MessageId, File) you'll need to create an instance of the Service Client class.
To make this possible you'll need the Account SAS (shared access signature) string of Storage account. Learn more at [SAS Token][sas_token]

#### Get credentials

- **SAS Token**

a. Use the [Azure CLI][azure_cli] snippet below to get the SAS token from the Storage account.

```Powershell
az storage blob generate-sas
    --name {queue name}
    --expiry {date/time to expire SAS token}
    --permission {permission to grant}
    --connection-string {connection string of the storage account}
    --services {storage services the SAS allows}
    --resource-types {resource types the SAS allows}
```

```Powershell
CONNECTION_STRING=<connection-string>

az storage blob generate-sas
    --name javasdksas
    --expiry 2019-06-05
    --permission rpau
    --connection-string $CONNECTION_STRING
```
b. Alternatively, get the Account SAS Token from the Azure Portal.
```
Go to your storage account -> Shared access signature -> Click on Generate SAS and connection string (after setup)
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

Blob storage is designed for:

- Serving images or documents directly to a browser.
- Storing files for distributed access.
- Streaming video and audio.
- Writing to log files.
- Storing data for backup and restore, disaster recovery, and archiving.
- Storing data for analysis by an on-premises or Azure-hosted service.

## Examples

The following sections provide several code snippets covering some of the most common Azure Storage Blob tasks, including:

- [Create BlobServiceClient](#create-blobserviceclient)
- [Create ContainerClient](#create-containerclient)
- [Create BlobClient](#create-blobclient)
- [Create a container](#create-a-container)
- [Upload a blob from InputStream](#uploading-a-blob-from-a-stream)
- [Upload a blob from File](#uploading-a-blob-from-file)
- [Download a blob to OutputStream](#downloading-a-blob-to-output-stream)
- [Download a blob to File](#downloading-a-blob-to-local-path)
- [Enumerating blobs](#enumerating-blobs)
- [Authenticate with Azure.Identity](#authenticate-with-azureidentity)

### Create BlobServiceClient

Create a BlobServiceClient using the [`sasToken`](#get-credentials) generated above.
```java
BlobServiceClient blobServiceClient = new BlobServiceClientBuilder()
        .endpoint("<your-storage-blob-url>")
        .credential("<your-sasToken>")
        .buildClient();
```

### Create ContainerClient

Create a ContainerClient if a BlobServiceClient exists.
```java
ContainerClient containerClient = blobServiceClient.getContainerClient("mycontainer");
```

or

Create the ContainerClient from the builder [`sasToken`](#get-credentials) generated above.
```java
ContainerClient containerClient = new ContainerClientBuilder()
         .endpoint("<your-storage-blob-url>")
         .credential("<your-sasToken>")
         .containerName("mycontainer")
         .buildClient();
```

### Create BlobClient

Create a BlobClient if container client exists.
```java
BlobClient blobClient = containerClient.getBlobClient("myblob");
```

or

Create the BlobClient from the builder [`sasToken`](#get-credentials) generated above.
```java
BlobClient blobClient = new BlobClientBuilder()
         .endpoint("<your-storage-blob-url>")
         .credential("<your-sasToken>")
         .containerName("mycontainer")
         .blobName("myblob")
         .buildBlobClient();
```

### Create a container

Create a container from a BlobServiceClient.
```java
blobServiceClient.createContainer("mycontainer");
```

or

Create a container using ContainerClient.
```java
containerClient.create();
```

### Uploading a blob from a stream

Upload data stream to a blob using BlockBlobClient generated from a ContainerClient.

```java
BlockBlobClient blockBlobClient = containerClient.getBlockBlobClient("myblockblob");
String dataSample = "samples";
try (ByteArrayInputStream dataStream = new ByteArrayInputStream(dataSample.getBytes())) {
    blockBlobClient.upload(dataStream, dataSample.length());
}
```

### Uploading a blob from `File`

Upload a file to a blob using BlockBlobClient generated from ContainerClient.

```java
BlockBlobClient blockBlobClient = containerClient.getBlockBlobClient("myblockblob");
blockBlobClient.uploadFromFile("local-file.jpg");
```

### Downloading a blob to output stream

Download blob to output stream using BlobClient.

```java
try(ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
    blobClient.download(outputStream);
}
```

### Downloading a blob to local path

Download blob to local file using BlobClient.
```java
blobClient.downloadToFile("downloaded-file.jpg");
```

### Enumerating blobs

Enumerating all blobs using ContainerClient
```java
containerClient.listBlobsFlat()
        .forEach(
            blobItem -> System.out.println("This is the blob name: " + blobItem.getName())
        );
```

### Authenticate with Azure.Identity

The [Azure Identity library][identity] provides Azure Active Directory support for authenticating with Azure Storage.

```java
BlobServiceClient storageClient = new BlobServiceClientBuilder()
        .endpoint(endpoint)
        .credential(new DefaultAzureCredentialBuilder().build())
        .buildClient();
```

## Troubleshooting

When interacting with blobs using this Java client library, errors returned by the service correspond to the same HTTP
status codes returned for [REST API][error_codes] requests. For example, if you try to retrieve a container or blob that
doesn't exist in your Storage Account, a `404` error is returned, indicating `Not Found`.

## Next steps

Get started with our [Blob samples][samples]:

1. [Basic Examples][samples_basic]: Create storage, container, blob clients, Upload, download, and list blobs.
1. [File Transfer Examples][samples_file_transfer]: Upload and download a large file through blobs.
1. [Storage Error Examples][samples_storage_error]: Handle the exceptions from storage blob service side.
1. [List Container Examples][samples_list_containers]: Create, list and delete containers.
1. [Set metadata and HTTPHeaders Examples][samples_metadata]: Set metadata for container and blob, and set HTTPHeaders for blob.
1. [Azure Identity Examples][samples_identity]: Use DefaultAzureCredential to do the authentication.

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the Code of Conduct FAQ or contact opencode@microsoft.com with any additional questions or comments.

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fstorage%2FAzure.Storage.Blobs%2FREADME.png)

<!-- LINKS -->
[source]: src
[docs]: http://azure.github.io/azure-sdk-for-java/
[rest_docs]: https://docs.microsoft.com/rest/api/storageservices/blob-service-rest-api
[product_docs]: https://docs.microsoft.com/azure/storage/blobs/storage-blobs-overview
[sas_token]: https://docs.microsoft.com/azure/storage/common/storage-dotnet-shared-access-signature-part-1
[jdk]: https://docs.microsoft.com/java/azure/java-supported-jdk-runtime?view=azure-java-stable
[maven]: https://maven.apache.org/
[azure_subscription]: https://azure.microsoft.com/free/
[storage_account]: https://docs.microsoft.com/azure/storage/common/storage-quickstart-create-account?tabs=azure-portal
[storage_account_create_ps]: https://docs.microsoft.com/azure/storage/common/storage-quickstart-create-account?tabs=azure-powershell
[storage_account_create_cli]: https://docs.microsoft.com/azure/storage/common/storage-quickstart-create-account?tabs=azure-cli
[storage_account_create_portal]: https://docs.microsoft.com/azure/storage/common/storage-quickstart-create-account?tabs=azure-portal
[azure_cli]: https://docs.microsoft.com/cli/azure
[azure_sub]: https://azure.microsoft.com/free/
[identity]: https://github.com/Azure/azure-sdk-for-java/blob/master/sdk/identity/azure-identity/README.md
[error_codes]: https://docs.microsoft.com/rest/api/storageservices/blob-service-error-codes
[samples]: src/samples
[samples_basic]: src/samples/java/com/azure/storage/blob/BasicExample.java
[samples_file_transfer]: src/samples/java/com/azure/storage/blob/FileTransferExample.java
[samples_storage_error]: src/samples/java/com/azure/storage/blob/StorageErrorHandlingExample.java
[samples_list_containers]: src/samples/java/com/azure/storage/blob/ListContainersExample.java
[samples_metadata]: src/samples/java/com/azure/storage/blob/SetMetadataAndHTTPHeadersExample.java
[samples_identity]: src/samples/java/com/azure/storage/blob/AzureIdentityExample.java
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java/sdk/storage/azure-storage-blob/README.png)
