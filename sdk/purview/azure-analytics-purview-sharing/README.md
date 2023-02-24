# Azure Purview Sharing client library for Java

**Please rely heavily on the [service's documentation][product_documentation] and our [data-plane client docs][protocol_method] to use this library**

[Source code][source_code] | [Package (Maven)][package] | [API reference documentation][docs] | [Product Documentation][share_product_documentation] | [Samples][samples_code]

## Getting started

### Prerequisites

- [Java Development Kit (JDK)][jdk] with version 8 or above
- [Azure Subscription][azure_subscription]
- An existing Microsoft Purview account.

For more information about creating a Microsoft Purview account, see [here][create_azure_purview_account].

## Documentation

Various documentation is available to help you get started

- [API reference documentation][docs]
- [Product documentation][share_product_documentation]

### Adding the package to your product

[//]: # ({x-version-update-start;com.azure:azure-analytics-purview-sharing;current})
```xml
<dependency>
    <groupId>com.azure</groupId>
    <artifactId>azure-analytics-purview-sharing</artifactId>
    <version>1.0.0-beta.1</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authentication

[Azure Identity][azure_identity] package provides the default implementation for authenticating the client.

## Key concepts

## Examples

### Create a Sent Share Client
```java com.azure.analytics.purview.sharing.createSentShareClient
SentSharesClient sentSharesClient =
        new SentSharesClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("https://<my-account-name>.purview.azure.com/share")
                .buildClient();
```

### Create a Sent Share
```java com.azure.analytics.purview.sharing.createSentShare
SentSharesClient sentSharesClient =
        new SentSharesClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("https://<my-account-name>.purview.azure.com/share")
                .buildClient();

String sentShareId = UUID.randomUUID().toString();
InPlaceSentShare sentShare = new InPlaceSentShare()
        .setDisplayName("sample-share")
        .setDescription("A sample share");

StoreReference storeReference = new StoreReference()
        .setReferenceName("/subscriptions/de06c3a0-4610-4ca0-8cbb-bbdac204bd65/resourceGroups/sender-storage-rg/providers/Microsoft.Storage/storageAccounts/providerstorage")
        .setType(ReferenceNameType.ARM_RESOURCE_REFERENCE);

StorageAccountPath storageAccountPath = new StorageAccountPath()
        .setContainerName("container-name")
        .setReceiverPath("shared-file-name.txt")
        .setSenderPath("original/file-name.txt");

BlobStorageArtifact artifact = new BlobStorageArtifact()
        .setStoreReference(storeReference)
        .setPaths(List.of(storageAccountPath));

sentShare.setArtifact(artifact);

SyncPoller<BinaryData, BinaryData> response =
        sentSharesClient.beginCreateOrReplaceSentShare(
                sentShareId,
                BinaryData.fromObject(sentShare),
                new RequestOptions());
```

### Get a Sent Share
```java com.azure.analytics.purview.sharing.getSentShare
SentSharesClient sentSharesClient =
        new SentSharesClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("https://<my-account-name>.purview.azure.com/share")
                .buildClient();

SentShare retrievedSentShare = sentSharesClient
        .getSentShareWithResponse("<sent-share-id>", new RequestOptions())
        .getValue()
        .toObject(SentShare.class);
```

### Get All Sent Shares
```java com.azure.analytics.purview.sharing.getAllSentShares
SentSharesClient sentSharesClient =
        new SentSharesClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("https://<my-account-name>.purview.azure.com/share")
                .buildClient();

PagedIterable<BinaryData> sentShareResults = sentSharesClient.getAllSentShares(
                "/subscriptions/de06c3a0-4610-4ca0-8cbb-bbdac204bd65/resourceGroups/sender-storage-rg/providers/Microsoft.Storage/storageAccounts/providerstorage",
                new RequestOptions());

List<SentShare> sentShares = sentShareResults.stream()
    .map(binaryData -> binaryData.toObject(SentShare.class))
    .collect(Collectors.toList());
```

### Delete a Sent Share
```java com.azure.analytics.purview.sharing.deleteSentShare
SentSharesClient sentSharesClient =
        new SentSharesClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("https://<my-account-name>.purview.azure.com/share")
                .buildClient();

sentSharesClient.beginDeleteSentShare("<sent-share-id", new RequestOptions());
```

### Send a Share Invitation to a User
```java com.azure.analytics.purview.sharing.sendUserInvitation
SentSharesClient sentSharesClient =
        new SentSharesClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("https://<my-account-name>.purview.azure.com/share")
                .buildClient();

String sentShareId = "<sent-share-id>";
String sentShareInvitationId = UUID.randomUUID().toString();

UserInvitation sentShareInvitation = new UserInvitation()
        .setTargetEmail("receiver@microsoft.com")
        .setNotify(true)
        .setExpirationDate(OffsetDateTime.now().plusDays(60));

Response<BinaryData> response =
        sentSharesClient.createSentShareInvitationWithResponse(
                sentShareId,
                sentShareInvitationId,
                BinaryData.fromObject(sentShareInvitation),
                new RequestOptions());
```

### Send a Share Invitation to a Service
```java com.azure.analytics.purview.sharing.sendServiceInvitation
SentSharesClient sentSharesClient =
        new SentSharesClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("https://<my-account-name>.purview.azure.com/share")
                .buildClient();

String sentShareId = "<sent-share-id>";
String sentShareInvitationId = UUID.randomUUID().toString();

ServiceInvitation sentShareInvitation = new ServiceInvitation()
        .setTargetActiveDirectoryId(UUID.fromString("<tenant-id>"))
        .setTargetObjectId(UUID.fromString("<object-id>"))
        .setExpirationDate(OffsetDateTime.now().plusDays(60));

Response<BinaryData> response =
        sentSharesClient.createSentShareInvitationWithResponse(
                sentShareId,
                sentShareInvitationId,
                BinaryData.fromObject(sentShareInvitation),
                new RequestOptions());
```

### Get All Sent Share Invitations
```java com.azure.analytics.purview.sharing.getAllSentShareInvitations
SentSharesClient sentSharesClient =
        new SentSharesClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("https://<my-account-name>.purview.azure.com/share")
                .buildClient();

String sentShareId = "<sent-share-id>";

RequestOptions requestOptions = new RequestOptions().addQueryParam("$orderBy", "properties/sentAt desc");
PagedIterable<BinaryData> response =
        sentSharesClient.getAllSentShareInvitations(sentShareId, requestOptions);
```

### Get Sent Share Invitation
```java com.azure.analytics.purview.sharing.getSentShareInvitation
SentSharesClient sentSharesClient =
        new SentSharesClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("https://<my-account-name>.purview.azure.com/share")
                .buildClient();

String sentShareId = "<sent-share-id>";
String sentShareInvitationId = "<sent-share-invitation-id>";

Response<BinaryData> sentShareInvitation =
        sentSharesClient.getSentShareInvitationWithResponse(sentShareId, sentShareInvitationId, new RequestOptions());
```

### Create a Received Share Client
```java com.azure.analytics.purview.sharing.createReceivedShareClient
ReceivedSharesClient receivedSharesClient =
        new ReceivedSharesClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("https://<my-account-name>.purview.azure.com/share")
                .buildClient();
```

### Get All Detached Received Shares
```java com.azure.analytics.purview.sharing.getAllDetachedReceivedShares
ReceivedSharesClient receivedSharesClient =
        new ReceivedSharesClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("https://<my-account-name>.purview.azure.com/share")
                .buildClient();

RequestOptions requestOptions = new RequestOptions().addQueryParam("$orderBy", "properties/createdAt desc");
PagedIterable<BinaryData> response = receivedSharesClient.getAllDetachedReceivedShares(requestOptions);
```
### Get Received Share
```java com.azure.analytics.purview.sharing.getReceivedShare
ReceivedSharesClient receivedSharesClient =
        new ReceivedSharesClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("https://<my-account-name>.purview.azure.com/share")
                .buildClient();

Response<BinaryData> receivedShare =
        receivedSharesClient.getReceivedShareWithResponse("<received-share-id>", new RequestOptions());
```

### Attach Received Shares
```java com.azure.analytics.purview.sharing.attachReceivedShare
ReceivedSharesClient receivedSharesClient =
        new ReceivedSharesClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("https://<my-account-name>.purview.azure.com/share")
                .buildClient();

RequestOptions listRequestOptions = new RequestOptions().addQueryParam("$orderBy", "properties/createdAt desc");
PagedIterable<BinaryData> listResponse = receivedSharesClient.getAllDetachedReceivedShares(listRequestOptions);

Optional<BinaryData> detachedReceivedShare = listResponse.stream().findFirst();

if (detachedReceivedShare.isEmpty()) {
    return;
}

String receivedShareId = new ObjectMapper()
        .readValue(detachedReceivedShare.get().toString(), ObjectNode.class)
        .get("id")
        .textValue();
 
InPlaceReceivedShare receivedShare = new InPlaceReceivedShare()
        .setDisplayName("my-received-share");

StoreReference storeReference = new StoreReference()
        .setReferenceName("/subscriptions/de06c3a0-4610-4ca0-8cbb-bbdac204bd65/resourceGroups/receiver-storage-rg/providers/Microsoft.Storage/storageAccounts/receiverstorage")
        .setType(ReferenceNameType.ARM_RESOURCE_REFERENCE); 

BlobAccountSink sink = new BlobAccountSink()
        .setStoreReference(storeReference)
        .setContainerName("container-name")
        .setFolder("folderName")
        .setMountPath("optionalMountPath");

receivedShare.setSink(sink);

SyncPoller<BinaryData, BinaryData> createResponse =
        receivedSharesClient.beginCreateOrReplaceReceivedShare(receivedShareId, BinaryData.fromObject(receivedShare), new RequestOptions());
```

### Get All Attached Received Shares
```java com.azure.analytics.purview.sharing.getAllAttachedReceivedShares
ReceivedSharesClient receivedSharesClient =
        new ReceivedSharesClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("https://<my-account-name>.purview.azure.com/share")
                .buildClient();

RequestOptions requestOptions = new RequestOptions().addQueryParam("$orderBy", "properties/createdAt desc");
PagedIterable<BinaryData> response =
        receivedSharesClient.getAllAttachedReceivedShares(
                "/subscriptions/4D8FD81D-431D-4B1D-B46C-C770CFC034FC/resourceGroups/contoso-rg/providers/Microsoft.Storage/storageAccounts/blobAccount",
                requestOptions);

Optional<BinaryData> receivedShare = response.stream().findFirst();

if (receivedShare.isEmpty()) {
    return;
}

ReceivedShare receivedShareResponse = receivedShare.get().toObject(InPlaceReceivedShare.class);
```

### Delete a Received Share
```java com.azure.analytics.purview.sharing.deleteReceivedShare
ReceivedSharesClient receivedSharesClient =
        new ReceivedSharesClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("https://<my-account-name>.purview.azure.com/share")
                .buildClient();

receivedSharesClient.beginDeleteReceivedShare("<received-share-id", new RequestOptions()); 
```

## Troubleshooting

## Next steps

### Enabling Logging

Azure SDKs for Java offer a consistent logging story to help aid in troubleshooting application errors and expedite
their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help
locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
1. Create your feature branch (`git checkout -b my-new-feature`)
1. Commit your changes (`git commit -am 'Add some feature'`)
1. Push to the branch (`git push origin my-new-feature`)
1. Create new Pull Request

<!-- LINKS -->
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/purview/azure-analytics-purview-sharing/src
[package]: https://mvnrepository.com/artifact/com.azure/azure-analytics-purview-sharing
[share_product_documentation]: https://docs.microsoft.com/azure/purview/concept-data-share
[samples_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/purview/azure-analytics-purview-sharing/src/samples/
[product_documentation]: https://azure.microsoft.com/services/
[docs]: https://azure.github.io/azure-sdk-for-java/
[jdk]: https://docs.microsoft.com/java/azure/jdk/
[azure_subscription]: https://azure.microsoft.com/free/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
[create_azure_purview_account]: https://docs.microsoft.com/azure/purview/create-catalog-portal
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-with-Azure-SDK
