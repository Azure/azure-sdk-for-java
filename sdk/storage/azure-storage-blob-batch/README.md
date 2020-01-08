# Azure Storage Blobs Batch client library for Java

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

[//]: # ({x-version-update-start;com.azure:azure-storage-blob-batch;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-storage-blob-batch</artifactId>
  <version>12.0.2</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Default HTTP Client
All client libraries, by default, use Netty HTTP client. Adding the above dependency will automatically configure 
Storage Blob Batch to use Netty HTTP client. 

### Alternate HTTP client
If, instead of Netty it is preferable to use OkHTTP, there is a HTTP client available for that too. Exclude the default
Netty and include OkHTTP client in your pom.xml.

[//]: # ({x-version-update-start;com.azure:azure-storage-blob-batch;current})
```xml
<!-- Add Storage Blob Batch dependency without Netty HTTP client -->
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-storage-blob-batch</artifactId>
    <version>12.0.1</version>
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
<!-- Add OkHTTP client to use with Storage Blob Batch -->
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-core-http-okhttp</artifactId>
  <version>1.0.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

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
az storage account create \
    --resource-group <resource-group-name> \
    --name <storage-account-name> \
    --location <location>
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

The following sections provide several code snippets covering some of the most common Azure Storage Blob Batch tasks, including:

- [Creating BlobBatchClient](#create-blobbatchclient)
- [Bulk Deleting Blobs](#bulk-deleting-blobs)
- [Bulk Setting AccessTier](#bulk-setting-accesstier)
- [Advanced Batching](#advanced-batching)

### Creating BlobBatchClient

Create a BlobBatchClient from a [`BlobServiceClient`]().

```java
BlobBatchClient blobBatchClient = new BlobBatchClientBuilder(blobServiceClient).buildClient();
```

### Bulk Deleting Blobs

```java
blobBatchClient.deleteBlobs(blobUrls, DeleteSnapshotsOptionType.INCLUDE).forEach(response ->
    System.out.printf("Deleting blob with URL %s completed with status code %d%n",
        response.getRequest().getUrl(), response.getStatusCode()));
```

### Bulk Setting AccessTier

```java
blobBatchClient.setBlobsAccessTier(blobUrls, AccessTier.HOT).forEach(response ->
    System.out.printf("Setting blob access tier with URL %s completed with status code %d%n",
        response.getRequest().getUrl(), response.getStatusCode()));
```

### Advanced Batching

Deleting blobs in a batch that have different pre-requisites.

```java
BlobBatch blobBatch = blobBatchClient.getBlobBatch();

// Delete a blob.
Response<Void> deleteResponse = blobBatch.deleteBlob(blobUrl);
        
// Delete a specific blob snapshot.
Response<Void> deleteSnapshotResponse =
    blobBatch.deleteBlob(blobUrlWithSnapshot, DeleteSnapshotsOptionType.ONLY, null);
        
// Delete a blob that has a lease.
Response<Void> deleteWithLeaseResponse =
    blobBatch.deleteBlob(blobUrlWithLease, DeleteSnapshotsOptionType.INCLUDE, new BlobAccessConditions()
        .setLeaseId("leaseId"));

blobBatchClient.submitBatch(blobBatch);
System.out.printf("Deleting blob completed with status code %d%n", deleteResponse.getStatusCode());
System.out.printf("Deleting blob snapshot completed with status code %d%n",
    deleteSnapshotResponse.getStatusCode());
System.out.printf("Deleting blob with lease completed with status code %d%n",
    deleteWithLeaseResponse.getStatusCode());
```

Setting `AccessTier` on blobs in batch that have different pre-requisites. 

```java
BlobBatch blobBatch = blobBatchClient.getBlobBatch();

// Set AccessTier on a blob.
Response<Void> setTierResponse = blobBatch.setBlobAccessTier(blobUrl, AccessTier.COOL);

// Set AccessTier on another blob.
Response<Void> setTierResponse2 = blobBatch.setBlobAccessTier(blobUrl2, AccessTier.ARCHIVE);

// Set AccessTier on a blob that has a lease.
Response<Void> setTierWithLeaseResponse = blobBatch.setBlobAccessTier(blobUrlWithLease, AccessTier.HOT, 
    .setLeaseId("leaseId");

blobBatchClient.submitBatch(blobBatch);
System.out.printf("Set AccessTier on blob completed with status code %d%n", setTierResponse.getStatusCode());
System.out.printf("Set AccessTier on blob completed with status code %d%n", setTierResponse2.getStatusCode());
System.out.printf("Set AccessTier on  blob with lease completed with status code %d%n",
    setTierWithLeaseResponse.getStatusCode());
```

## Troubleshooting

When interacts with blobs using this Java client library, errors returned by the service correspond to the same HTTP
status codes returned for [REST API][error_codes] requests. For example, if you try to retrieve a container or blob that
doesn't exist in your Storage Account, a `404` error is returned, indicating `Not Found`.

## Next steps

Get started with our [Blob Batch samples][blob_samples]:

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the Code of Conduct FAQ or contact opencode@microsoft.com with any additional questions or comments.

<!-- LINKS -->
[docs]: http://azure.github.io/azure-sdk-for-java/
[rest_docs]: https://docs.microsoft.com/rest/api/storageservices/blob-service-rest-api
[product_docs]: https://docs.microsoft.com/azure/storage/blobs/storage-blobs-overview
[azure_subscription]: https://azure.microsoft.com/free/
[storage_account]: https://docs.microsoft.com/azure/storage/common/storage-quickstart-create-account?tabs=azure-portal
[storage_account_create_cli]: https://docs.microsoft.com/azure/storage/common/storage-quickstart-create-account?tabs=azure-cli
[error_codes]: https://docs.microsoft.com/rest/api/storageservices/blob-service-error-codes
[blob_samples]: src/samples/README.md
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java/sdk/storage/azure-storage-blob-batch/README.png)
