# Azure Storage Blobs Batch client library for Java

Azure Blob storage is Microsoft's object storage solution for the cloud. Blob
storage is optimized for storing massive amounts of unstructured data.
Unstructured data is data that does not adhere to a particular data model or
definition, such as text or binary data.

[Source code][source] | [API reference documentation][docs] | [REST API documentation][rest_docs] | [Product documentation][product_docs] | [Samples][samples]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]
- [Create Storage Account][storage_account]

### Include the package

[//]: # ({x-version-update-start;com.azure:azure-storage-blob-batch;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-storage-blob-batch</artifactId>
  <version>12.6.0</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Create a Storage Account
To create a Storage Account you can use the [Azure Portal][azure_portal] or [Azure CLI][storage_account_create_cli].

```bash
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

- [Creating BlobBatchClient](#creating-blobbatchclient)
- [Bulk Deleting Blobs](#bulk-deleting-blobs)
- [Bulk Setting AccessTier](#bulk-setting-accesstier)
- [Advanced Batching](#advanced-batching)

### Creating BlobBatchClient

Create a BlobBatchClient from a [BlobServiceClient][blob_service_client].

<!-- embedme ./src/samples/java/com/azure/storage/blob/batch/ReadmeSamples.java#L42-L42 -->
```java
BlobBatchClient blobBatchClient = new BlobBatchClientBuilder(blobServiceClient).buildClient();
```

### Bulk Deleting Blobs

<!-- embedme ./src/samples/java/com/azure/storage/blob/batch/ReadmeSamples.java#L46-L48 -->
```java
blobBatchClient.deleteBlobs(blobUrls, DeleteSnapshotsOptionType.INCLUDE).forEach(response ->
    System.out.printf("Deleting blob with URL %s completed with status code %d%n",
        response.getRequest().getUrl(), response.getStatusCode()));
```

### Bulk Setting AccessTier

<!-- embedme ./src/samples/java/com/azure/storage/blob/batch/ReadmeSamples.java#L52-L54 -->
```java
blobBatchClient.setBlobsAccessTier(blobUrls, AccessTier.HOT).forEach(response ->
    System.out.printf("Setting blob access tier with URL %s completed with status code %d%n",
        response.getRequest().getUrl(), response.getStatusCode()));
```

### Advanced Batching

Deleting blobs in a batch that have different pre-requisites.

<!-- embedme ./src/samples/java/com/azure/storage/blob/batch/ReadmeSamples.java#L58-L77 -->
```java
BlobBatch blobBatch = blobBatchClient.getBlobBatch();

// Delete a blob.
Response<Void> deleteResponse = blobBatch.deleteBlob(blobUrl);

// Delete a specific blob snapshot.
Response<Void> deleteSnapshotResponse =
    blobBatch.deleteBlob(blobUrlWithSnapshot, DeleteSnapshotsOptionType.ONLY, null);

// Delete a blob that has a lease.
Response<Void> deleteWithLeaseResponse =
    blobBatch.deleteBlob(blobUrlWithLease, DeleteSnapshotsOptionType.INCLUDE, new BlobRequestConditions()
        .setLeaseId("leaseId"));

blobBatchClient.submitBatch(blobBatch);
System.out.printf("Deleting blob completed with status code %d%n", deleteResponse.getStatusCode());
System.out.printf("Deleting blob snapshot completed with status code %d%n",
    deleteSnapshotResponse.getStatusCode());
System.out.printf("Deleting blob with lease completed with status code %d%n",
    deleteWithLeaseResponse.getStatusCode());
```

Setting `AccessTier` on blobs in batch that have different pre-requisites.

<!-- embedme ./src/samples/java/com/azure/storage/blob/batch/ReadmeSamples.java#L81-L97 -->
```java
BlobBatch blobBatch = blobBatchClient.getBlobBatch();

// Set AccessTier on a blob.
Response<Void> setTierResponse = blobBatch.setBlobAccessTier(blobUrl, AccessTier.COOL);

// Set AccessTier on another blob.
Response<Void> setTierResponse2 = blobBatch.setBlobAccessTier(blobUrl2, AccessTier.ARCHIVE);

// Set AccessTier on a blob that has a lease.
Response<Void> setTierWithLeaseResponse = blobBatch.setBlobAccessTier(blobUrlWithLease, AccessTier.HOT,
    "leaseId");

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

Get started with our [Blob Batch samples][blob_samples]:

## Contributing

This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the Code of Conduct FAQ or contact opencode@microsoft.com with any additional questions or comments.

<!-- LINKS -->
[source]: src/main/java
[docs]: http://azure.github.io/azure-sdk-for-java/
[rest_docs]: https://docs.microsoft.com/rest/api/storageservices/blob-service-rest-api
[product_docs]: https://docs.microsoft.com/azure/storage/blobs/storage-blobs-overview
[samples]: src/samples
[jdk]: https://docs.microsoft.com/en-us/java/azure/jdk/?view=azure-java-stable
[azure_subscription]: https://azure.microsoft.com/free/
[storage_account]: https://docs.microsoft.com/azure/storage/common/storage-quickstart-create-account?tabs=azure-portal
[azure_portal]: https://docs.microsoft.com/en-us/azure/storage/common/storage-account-create?tabs=azure-portal
[storage_account_create_cli]: https://docs.microsoft.com/azure/storage/common/storage-quickstart-create-account?tabs=azure-cli
[blob_service_client]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/storage/azure-storage-blob#create-a-storage-account
[error_codes]: https://docs.microsoft.com/rest/api/storageservices/blob-service-error-codes
[blob_samples]: src/samples/README.md
[cla]: https://cla.microsoft.com
[coc]: https://opensource.microsoft.com/codeofconduct/
[coc_faq]: https://opensource.microsoft.com/codeofconduct/faq/
[coc_contact]: mailto:opencode@microsoft.com
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fstorage%2Fazure-storage-blob-batch%2FREADME.png)
