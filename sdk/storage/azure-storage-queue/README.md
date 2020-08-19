# Azure Storage Queue client library for Java
Azure Queue storage is a service for storing large numbers of messages that can be accessed from anywhere in the world via authenticated calls using HTTP or HTTPS.
A single queue message can be up to 64 KB in size, and a queue can contain millions of messages, up to the total capacity limit of a storage account.

[Source code][source_code] | [API reference documentation][api_documentation] | [Product documentation][storage_docs] |
[Samples][samples]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]
- [Create Storage Account][storage_account]

### Include the package

[//]: # ({x-version-update-start;com.azure:azure-storage-queue;current})
```xml
<dependency>
  <groupId>com.azure</groupId>
  <artifactId>azure-storage-queue</artifactId>
  <version>12.6.0</version>
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

In order to interact with the Storage service (Blob, Queue, Message, MessageId, File) you'll need to create an instance of the Service Client class.
To make this possible you'll need the Account SAS (shared access signature) string of Storage account. Learn more at [SAS Token][sas_token]

#### Get Credentials

- **SAS Token**

a. Use the [Azure CLI][azure_cli] snippet below to get the SAS token from the Storage account.

```Powershell
az storage queue generate-sas
    --name {queue name}
    --expiry {date/time to expire SAS token}
    --permission {permission to grant}
    --connection-string {connection string of the storage account}
```

```Powershell
CONNECTION_STRING=<connection-string>
az storage queue generate-sas
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
### URL format
Queues are addressable using the following URL format:
The following URL addresses a queue in the diagram:
https://myaccount.queue.core.windows.net/images-to-download

#### Resource URI Syntax
For the storage account, the base URI for queue operations includes the name of the account only:

```$xslt
https://myaccount.queue.core.windows.net
```

For a queue, the base URI includes the name of the account and the name of the queue:

```$xslt
https://myaccount.queue.core.windows.net/myqueue
```

### Handling Exceptions
Uses the `queueServiceClient` generated from [Queue Service Client](#queue-service-client) section below.

<!-- embedme ./src/samples/java/com/azure/storage/queue/ReadmeSamples.java#L68-L72 -->
```java
try {
    queueServiceClient.createQueue("myQueue");
} catch (QueueStorageException e) {
    logger.error("Failed to create a queue with error code: " + e.getErrorCode());
}
```

### Queue Names
Every queue within an account must have a unique name. The queue name must be a valid DNS name, and cannot be changed once created. Queue names must confirm to the following rules:
1. A queue name must start with a letter or number, and can only contain letters, numbers, and the dash (-) character.
1. The first and last letters in the queue name must be alphanumeric. The dash (-) character cannot be the first or last character. Consecutive dash characters are not permitted in the queue name.
1. All letters in a queue name must be lowercase.
1. A queue name must be from 3 through 63 characters long.

### Queue Services
The queue service do operations on the queues in the storage account and manage the queue properties.

### Queue Service Client

The client performs the interactions with the Queue service, create or delete a queue, getting and setting Queue properties, list queues in account, and get queue statistics. An asynchronous, `QueueServiceAsyncClient`, and synchronous, `QueueClient`, client exists in the SDK allowing for selection of a client based on an application's use case.
Once you have the value of the SASToken you can create the queue service client with `${accountName}`, `${SASToken}`.

<!-- embedme ./src/samples/java/com/azure/storage/queue/ReadmeSamples.java#L76-L80 -->
```Java
String queueServiceURL = String.format("https://%s.queue.core.windows.net", ACCOUNT_NAME);
QueueServiceClient queueServiceClient = new QueueServiceClientBuilder().endpoint(queueServiceURL)
        .sasToken(SAS_TOKEN).buildClient();

QueueClient newQueueClient = queueServiceClient.createQueue("myQueue");
```

or

<!-- embedme ./src/samples/java/com/azure/storage/queue/ReadmeSamples.java#L84-L93 -->
```Java
String queueServiceAsyncURL = String.format("https://%s.queue.core.windows.net/", ACCOUNT_NAME);
QueueServiceAsyncClient queueServiceAsyncClient = new QueueServiceClientBuilder().endpoint(queueServiceAsyncURL)
        .sasToken(SAS_TOKEN).buildAsyncClient();
queueServiceAsyncClient.createQueue("newAsyncQueue").subscribe(result -> {
    // do something when new queue created
}, error -> {
    // do something if something wrong happened
    }, () -> {
    // completed, do something
    });
```

### Queue
Azure Queue storage is a service for storing large numbers of messages that can be accessed from anywhere in the world via authenticated calls using HTTP or HTTPS.
A single queue message can be up to 64 KB in size, and a queue can contain millions of messages, up to the total capacity limit of a storage account.

### QueueClient
Once you have the value of the SASToken you can create the queue service client with `${accountName}`, `${queueName}`, `${SASToken}`.

<!-- embedme ./src/samples/java/com/azure/storage/queue/ReadmeSamples.java#L97-L101 -->
```Java
String queueURL = String.format("https://%s.queue.core.windows.net/%s", ACCOUNT_NAME, queueName);
QueueClient queueClient = new QueueClientBuilder().endpoint(queueURL).sasToken(SAS_TOKEN).buildClient();

// metadata is map of key-value pair
queueClient.createWithResponse(metadata, Duration.ofSeconds(30), Context.NONE);
```

or

<!-- embedme ./src/samples/java/com/azure/storage/queue/ReadmeSamples.java#L105-L115 -->
```Java
// Only one "?" is needed here. If the sastoken starts with "?", please removing one "?".
String queueAsyncURL = String.format("https://%s.queue.core.windows.net/%s?%s", ACCOUNT_NAME, queueAsyncName,
        SAS_TOKEN);
QueueAsyncClient queueAsyncClient = new QueueClientBuilder().endpoint(queueAsyncURL).buildAsyncClient();
queueAsyncClient.createWithResponse(metadata).subscribe(result -> {
    // do something when new queue created
}, error -> {
    // do something if something wrong happened
    }, () -> {
    // completed, do something
    });
```

## Examples

The following sections provide several code snippets covering some of the most common Configuration Service tasks, including:
- [Build a client](#build-a-client)
- [Create a Queue](#create-a-queue)
- [Delete a queue](#delete-a-queue)
- [List the queues in account](#list-queues-in-account)
- [Get properties in Queue account](#get-properties-in-queue-account)
- [Set properties in Queue account](#set-properties-in-queue-account)
- [Get statistics of queue](#get-queue-service-statistics)
- [Enqueue message into a queue](#enqueue-message-into-a-queue)
- [Update a message in a queue](#update-a-message-in-a-queue)
- [Peek at messages in a queue](#peek-at-messages-in-a-queue)
- [Dequeue messages from a queue](#dequeue-messages-from-a-queue)
- [Delete message from a queue](#delete-message-from-a-queue)
- [Get a Queue properties](#get-a-queue-properties)
- [Set/Update a Queue metadata](#set-a-queue-metadata)

### Build a client
We have two ways of building QueueService or Queue Client. Here will take queueServiceClient as an example. Same things apply to queueClient.

First, build client from full URL/endpoint (e.g. with queueName, with SASToken and etc.)

<!-- embedme ./src/samples/java/com/azure/storage/queue/ReadmeSamples.java#L53-L55 -->
```Java
// Only one "?" is needed here. If the sastoken starts with "?", please removing one "?".
String queueServiceURL = String.format("https://%s.queue.core.windows.net/?%s", ACCOUNT_NAME, SAS_TOKEN);
QueueServiceClient queueServiceClient = new QueueServiceClientBuilder().endpoint(queueServiceURL).buildClient();
```

Or

We can build the queueServiceClient from the builder using `${SASToken}` as credential.

<!-- embedme ./src/samples/java/com/azure/storage/queue/ReadmeSamples.java#L59-L61 -->
```Java
String queueServiceURL = String.format("https://%s.queue.core.windows.net", ACCOUNT_NAME);
QueueServiceClient queueServiceClient = new QueueServiceClientBuilder().endpoint(queueServiceURL)
        .sasToken(SAS_TOKEN).buildClient();
```

### Create a queue

Create a queue in the Storage Account using `${SASToken}` as credential.
Throws StorageException If the queue fails to be created.

<!-- embedme ./src/samples/java/com/azure/storage/queue/ReadmeSamples.java#L76-L80 -->
```Java
String queueServiceURL = String.format("https://%s.queue.core.windows.net", ACCOUNT_NAME);
QueueServiceClient queueServiceClient = new QueueServiceClientBuilder().endpoint(queueServiceURL)
        .sasToken(SAS_TOKEN).buildClient();

QueueClient newQueueClient = queueServiceClient.createQueue("myQueue");
```

### Delete a queue

Delete a queue in the Storage Account using `${SASToken}` as credential.
Throws StorageException If the queue fails to be deleted.

<!-- embedme ./src/samples/java/com/azure/storage/queue/ReadmeSamples.java#L119-L123 -->
```Java
String queueServiceURL = String.format("https://%s.queue.core.windows.net", ACCOUNT_NAME);
QueueServiceClient queueServiceClient = new QueueServiceClientBuilder().endpoint(queueServiceURL)
        .sasToken(SAS_TOKEN).buildClient();

queueServiceClient.deleteQueue("myqueue");
```

### List queues in account

List all the queues in account using `${SASToken}` as credential.

<!-- embedme ./src/samples/java/com/azure/storage/queue/ReadmeSamples.java#L127-L136 -->
```Java
String queueServiceURL = String.format("https://%s.queue.core.windows.net", ACCOUNT_NAME);
QueueServiceClient queueServiceClient = new QueueServiceClientBuilder().endpoint(queueServiceURL)
        .sasToken(SAS_TOKEN).buildClient();
// @param marker: Starting point to list the queues
// @param options: Filter for queue selection
// @param timeout: An optional timeout applied to the operation.
// @param context: Additional context that is passed through the Http pipeline during the service call.
queueServiceClient.listQueues(markers, options, timeout, context).stream().forEach(queueItem -> {
    System.out.printf("Queue %s exists in the account.", queueItem.getName());
});
```

### Get properties in queue account

Get queue properties in account, including properties for Storage Analytics and CORS (Cross-Origin Resource Sharing) rules.

Use `${SASToken}` as credential.

<!-- embedme ./src/samples/java/com/azure/storage/queue/ReadmeSamples.java#L140-L144 -->
```Java
String queueServiceURL = String.format("https://%s.queue.core.windows.net", ACCOUNT_NAME);
QueueServiceClient queueServiceClient = new QueueServiceClientBuilder().endpoint(queueServiceURL)
        .sasToken(SAS_TOKEN).buildClient();

QueueServiceProperties properties = queueServiceClient.getProperties();
```

### Set properties in queue account

Set queue properties in account, including properties for Storage Analytics and CORS (Cross-Origin Resource Sharing) rules.

Use `${SASToken}` as credential.

<!-- embedme ./src/samples/java/com/azure/storage/queue/ReadmeSamples.java#L148-L154 -->
```Java
String queueServiceURL = String.format("https://%s.queue.core.windows.net", ACCOUNT_NAME);
QueueServiceClient queueServiceClient = new QueueServiceClientBuilder().endpoint(queueServiceURL)
        .sasToken(SAS_TOKEN).buildClient();

QueueServiceProperties properties = queueServiceClient.getProperties();
properties.setCors(Collections.emptyList());
queueServiceClient.setProperties(properties);
```

### Get queue service statistics
The `Get Queue Service Stats` operation retrieves statistics related to replication for the Queue service.

Use `${SASToken}` as credential.
It is only available on the secondary location endpoint when read-access geo-redundant replication is enabled for the storage account.

<!-- embedme ./src/samples/java/com/azure/storage/queue/ReadmeSamples.java#L158-L162 -->
```Java
String queueServiceURL = String.format("https://%s.queue.core.windows.net", ACCOUNT_NAME);
QueueServiceClient queueServiceClient = new QueueServiceClientBuilder().endpoint(queueServiceURL)
        .sasToken(SAS_TOKEN).buildClient();

QueueServiceStatistics queueStats = queueServiceClient.getStatistics();
```

### Enqueue message into a queue
The operation adds a new message to the back of the message queue. A visibility timeout can also be specified to make the message invisible until the visibility timeout expires.

Use `${SASToken}` as credential.
A message must be in a format that can be included in an XML request with UTF-8 encoding. The encoded message can be up to 64 KB in size for versions 2011-08-18 and newer, or 8 KB in size for previous versions.

<!-- embedme ./src/samples/java/com/azure/storage/queue/ReadmeSamples.java#L166-L170 -->
```Java
String queueURL = String.format("https://%s.queue.core.windows.net", ACCOUNT_NAME);
QueueClient queueClient = new QueueClientBuilder().endpoint(queueURL).sasToken(SAS_TOKEN).queueName("myqueue")
        .buildClient();

queueClient.sendMessage("myMessage");
```

### Update a message in a queue
The operation updates a message in the message queue. Use `${SASToken}` as credential.

<!-- embedme ./src/samples/java/com/azure/storage/queue/ReadmeSamples.java#L174-L180 -->
```Java
String queueURL = String.format("https://%s.queue.core.windows.net", ACCOUNT_NAME);
QueueClient queueClient = new QueueClientBuilder().endpoint(queueURL).sasToken(SAS_TOKEN).queueName("myqueue")
        .buildClient();
// @param messageId: Id of the message
// @param popReceipt: Unique identifier that must match the message for it to be updated
// @param visibilityTimeout: How long the message will be invisible in the queue in seconds
queueClient.updateMessage(messageId, popReceipt, "new message", visibilityTimeout);
```

### Peek at messages in a queue
The operation retrieves one or more messages from the front of the queue. Use `${SASToken}` as credential.

<!-- embedme ./src/samples/java/com/azure/storage/queue/ReadmeSamples.java#L184-L191 -->
```Java
String queueURL = String.format("https://%s.queue.core.windows.net", ACCOUNT_NAME);
QueueClient queueClient = new QueueClientBuilder().endpoint(queueURL).sasToken(SAS_TOKEN).queueName("myqueue")
        .buildClient();
// @param key: The key with which the specified value should be associated.
// @param value: The value to be associated with the specified key.
queueClient.peekMessages(5, Duration.ofSeconds(1), new Context(key, value)).forEach(message -> {
    System.out.println(message.getMessageText());
});
```


### Receive messages from a queue
The operation retrieves one or more messages from the front of the queue. Use `${SASToken}` as credential.

<!-- embedme ./src/samples/java/com/azure/storage/queue/ReadmeSamples.java#L195-L201 -->
```Java
String queueURL = String.format("https://%s.queue.core.windows.net", ACCOUNT_NAME);
QueueClient queueClient = new QueueClientBuilder().endpoint(queueURL).sasToken(SAS_TOKEN).queueName("myqueue")
        .buildClient();
// Try to receive 10 mesages: Maximum number of messages to get
queueClient.receiveMessages(10).forEach(message -> {
    System.out.println(message.getMessageText());
});
```


### Delete message from a queue
The operation retrieves one or more messages from the front of the queue. Use `${SASToken}` as credential.

<!-- embedme ./src/samples/java/com/azure/storage/queue/ReadmeSamples.java#L205-L209 -->
```Java
String queueURL = String.format("https://%s.queue.core.windows.net", ACCOUNT_NAME);
QueueClient queueClient = new QueueClientBuilder().endpoint(queueURL).sasToken(SAS_TOKEN).queueName("myqueue")
        .buildClient();

queueClient.deleteMessage(messageId, popReceipt);
```

### Get a queue properties
The operation retrieves user-defined metadata and queue properties on the specified queue. Metadata is associated with the queue as name-values pairs.

Use `${SASToken}` as credential.

<!-- embedme ./src/samples/java/com/azure/storage/queue/ReadmeSamples.java#L213-L217 -->
```Java
String queueURL = String.format("https://%s.queue.core.windows.net", ACCOUNT_NAME);
QueueClient queueClient = new QueueClientBuilder().endpoint(queueURL).sasToken(SAS_TOKEN).queueName("myqueue")
        .buildClient();

QueueProperties properties = queueClient.getProperties();
```

### Set a queue metadata
The operation sets user-defined metadata on the specified queue. Metadata is associated with the queue as name-value pairs.

Use `${SASToken}` as credential.
<!-- embedme ./src/samples/java/com/azure/storage/queue/ReadmeSamples.java#L221-L231 -->
```Java
String queueURL = String.format("https://%s.queue.core.windows.net", ACCOUNT_NAME);
QueueClient queueClient = new QueueClientBuilder().endpoint(queueURL).sasToken(SAS_TOKEN).queueName("myqueue")
        .buildClient();

Map<String, String> metadata = new HashMap<String, String>() {
    {
        put("key1", "val1");
        put("key2", "val2");
    }
};
queueClient.setMetadata(metadata);
```

## Troubleshooting

## General

When you interact with queue using this Java client library, errors returned by the service correspond to the same HTTP status codes returned for [REST API][storage_rest] requests. For example, if you try to retrieve a queue that doesn't exist in your Storage Account, a `404` error is returned, indicating `Not Found`.

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
Several Storage Queue Java SDK samples are available to you in the SDK's GitHub repository. These samples provide example code for additional scenarios commonly encountered while working with Key Vault:

## Next steps Samples
Samples are explained in detail [here][samples_readme].

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
[source_code]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/storage/azure-storage-queue/src
[samples_readme]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/storage/azure-storage-queue/src/samples/README.md
[api_documentation]: https://docs.microsoft.com/rest/api/storageservices/queue-service-rest-api
[storage_docs]: https://docs.microsoft.com/azure/storage/queues/storage-queues-introduction
[jdk]: https://docs.microsoft.com/en-us/java/azure/jdk/?view=azure-java-stable
[maven]: https://maven.apache.org/
[azure_subscription]: https://azure.microsoft.com/free/
[storage_account]: https://docs.microsoft.com/azure/storage/common/storage-quickstart-create-account?tabs=azure-portal
[azure_cli]: https://docs.microsoft.com/cli/azure
[sas_token]: https://docs.microsoft.com/azure/storage/common/storage-dotnet-shared-access-signature-part-1
[storage_rest]: https://docs.microsoft.com/rest/api/storageservices/queue-service-error-codes
[samples]: https://github.com/Azure/azure-sdk-for-java/tree/master/sdk/storage/azure-storage-queue/src/samples
[performance_tuning]: https://github.com/Azure/azure-sdk-for-java/wiki/Performance-Tuning

![Impressions](https://azure-sdk-impressions.azurewebsites.net/api/impressions/azure-sdk-for-java%2Fsdk%2Fstorage%2Fazure-storage-queue%2FREADME.png)
