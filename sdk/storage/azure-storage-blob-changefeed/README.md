# Azure Blob Storage change feed client library for Java

The purpose of the change feed is to provide transaction logs of all the changes that occur to
the blobs and the blob metadata in your storage account. The change feed provides ordered,
guaranteed, durable, immutable, read-only log of these changes. Client applications can read these
logs at any time. The change feed enables you to build efficient and scalable solutions that
process change events that occur in your Blob Storage account at a low cost.

## Getting started
### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]
- [Create Storage Account][storage_account]

### Include the package

[//]: # ({x-version-update-start;com.azure:azure-storage-blob-changefeed;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-storage-blob-changefeed</artifactId>
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

The change feed is stored as blobs in a special container in your storage account at standard blob
pricing cost. You can control the retention period of these files based on your requirements
(See the conditions of the current release). Change events are appended to the change feed as records
in the Apache Avro format specification: a compact, fast, binary format that provides rich data structures
with inline schema. This format is widely used in the Hadoop ecosystem, Stream Analytics, and Azure Data
Factory.

You can process these logs incrementally or in-full. Any number of client applications can independently
read the change feed, in parallel, and at their own pace. Analytics applications such as Apache Drill or
Apache Spark can consume logs directly as Avro files, which let you process them at a low-cost, with
high-bandwidth, and without having to write a custom application.

## Examples

The following sections provide several code snippets covering some of the most common Azure Storage Blob Changefeed 
tasks, including:

- [Create a `BlobChangefeedClient`](#create-a-blobchangefeedclient)
- [Get events](#get-events)
- [Get events between a start and end time](#get-events-start-end)
- [Resume with a cursor](#get-events-cursor)
- [Poll for events with a cursor](#poll-events-cursor)

### Create a `BlobChangefeedClient`

<!-- embedme ./src/samples/java/com/azure/storage/blob/changefeed/ReadmeSamples.java#L26-L26 -->
```java
client = new BlobChangefeedClientBuilder(blobServiceClient).buildClient();
```

### Get events

<!-- embedme ./src/samples/java/com/azure/storage/blob/changefeed/ReadmeSamples.java#L30-L31 -->
```java
client.getEvents().forEach(event ->
    System.out.printf("Topic: %s, Subject: %s%n", event.getTopic(), event.getSubject()));
```

### Get events between a start and end time

<!-- embedme ./src/samples/java/com/azure/storage/blob/changefeed/ReadmeSamples.java#L35-L39 -->
```java
OffsetDateTime startTime = OffsetDateTime.MIN;
OffsetDateTime endTime = OffsetDateTime.now();

client.getEvents(startTime, endTime).forEach(event ->
    System.out.printf("Topic: %s, Subject: %s%n", event.getTopic(), event.getSubject()));
```

### Resume with a cursor

<!-- embedme ./src/samples/java/com/azure/storage/blob/changefeed/ReadmeSamples.java#L43-L59 -->
```java
BlobChangefeedPagedIterable iterable = client.getEvents();
Iterable<BlobChangefeedPagedResponse> pages = iterable.iterableByPage();

String cursor = null;
for (BlobChangefeedPagedResponse page : pages) {
    page.getValue().forEach(event ->
        System.out.printf("Topic: %s, Subject: %s%n", event.getTopic(), event.getSubject()));
    /*
     * Get the change feed cursor. The cursor is not required to get each page of events,
     * it is intended to be saved and used to resume iterating at a later date.
     */
    cursor = page.getContinuationToken();
}

/* Resume iterating from the pervious position with the cursor. */
client.getEvents(cursor).forEach(event ->
    System.out.printf("Topic: %s, Subject: %s%n", event.getTopic(), event.getSubject()));
```

### Poll for events with a cursor

<!-- embedme ./src/samples/java/com/azure/storage/blob/changefeed/ReadmeSamples.java#L63-L96 -->
```java
List<BlobChangefeedEvent> changefeedEvents = new ArrayList<BlobChangefeedEvent>();

/* Get the start time.  The change feed client will round start time down to the nearest hour if you provide
   an OffsetDateTime with minutes and seconds. */
OffsetDateTime startTime = OffsetDateTime.now();

/* Get your polling interval. */
long pollingInterval = 1000 * 60 * 5; /* 5 minutes. */

/* Get initial set of events. */
Iterable<BlobChangefeedPagedResponse> pages = client.getEvents(startTime, null).iterableByPage();

String continuationToken = null;

while (true) {
    for (BlobChangefeedPagedResponse page : pages) {
        changefeedEvents.addAll(page.getValue());
        /*
         * Get the change feed cursor. The cursor is not required to get each page of events,
         * it is intended to be saved and used to resume iterating at a later date.
         */
        continuationToken = page.getContinuationToken();
    }

    /* Wait before processing next batch of events. */
    try {
        Thread.sleep(pollingInterval);
    } catch (InterruptedException e) {
        e.printStackTrace();
    }

    /* Resume from last continuation token and fetch latest set of events. */
    pages = client.getEvents(continuationToken).iterableByPage();
}
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
Several Storage blob changefeed Java SDK samples are available to you in the SDK's GitHub repository.

## Next steps Samples
Samples are explained in detail [here][samples_readme].

## Contributing
This project welcomes contributions and suggestions. Most contributions require you to agree to a Contributor License Agreement (CLA) declaring that you have the right to, and actually do, grant us the rights to use your contribution. For details, visit https://cla.microsoft.com.

When you submit a pull request, a CLA-bot will automatically determine whether you need to provide a CLA and decorate the PR appropriately (e.g., label, comment). Simply follow the instructions provided by the bot. You will only need to do this once across all repos using our CLA.

This project has adopted the [Microsoft Open Source Code of Conduct](https://opensource.microsoft.com/codeofconduct/). For more information see the Code of Conduct FAQ or contact opencode@microsoft.com with any additional questions or comments.

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

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fstorage%2Fazure-storage-blob-changefeed%2FREADME.png)
