# Azure Purview Sharing client library for Java

Microsoft Purview Data Sharing allows data to be shared in-place from Azure Data Lake Storage Gen2 and Azure Storage accounts, both within and across organizations.

Data providers may use Microsoft Purview Data Sharing to share their data directly with other users and partners (known as data consumers) without data duplication, while centrally managing their sharing activities from within Microsoft Purview.

For data consumers, Microsoft Purview Data Sharing provides near real-time access to data shared with them by a provider.

Key capabilities delivered by Microsoft Purview Data Sharing include:
- Share data within the organization or with partners and customers outside of the organization (within the same Azure tenant or across different Azure tenants).
- Share data from ADLS Gen2 or Blob storage in-place without data duplication.
- Share data with multiple recipients.
- Access shared data in near real time.
- Manage sharing relationships and keep track of who the data is shared with/from, for each ADLSGen2 or Blob Storage account.
- Terminate share access at any time.
- Flexible experience through Microsoft Purview governance portal or via REST APIs.

**Please visit the following resources to learn more about this product.**

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
    <version>1.0.0-beta.2</version>
</dependency>
```
[//]: # ({x-version-update-end})

### Authentication

[Azure Identity][azure_identity] package provides the default implementation for authenticating the client.

## Key concepts

__Data Provider:__ A data provider is the individual who creates a share by selecting a data source, choosing which files and folders to share, and who to share them with. Microsoft Purview then sends an invitation to each data consumer.

__Data Consumer:__ A data consumer is the individual who accepts the invitation by specifying a target storage account in their own Azure subscription that they'll use to access the shared data.

## Examples

### Data Provider Examples

The following code examples demonstrate how data providers can use the Microsoft Azure Java SDK for Purview Sharing to manage their sharing activity.

#### Create a Sent Share Client
```java com.azure.analytics.purview.sharing.createSentShareClient
SentSharesClient sentSharesClient =
        new SentSharesClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("https://<my-account-name>.purview.azure.com/share")
                .buildClient();
```

#### Create a Sent Share

To begin sharing data, the data provider must first create a sent share that identifies the data they would like to share.

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
        .setReferenceName("/subscriptions/de06c3a0-4610-4ca0-8cbb-bbdac204bd65/resourceGroups/provider-storage-rg/providers/Microsoft.Storage/storageAccounts/providerstorage")
        .setType(ReferenceNameType.ARM_RESOURCE_REFERENCE);

StorageAccountPath storageAccountPath = new StorageAccountPath()
        .setContainerName("container-name")
        .setReceiverPath("shared-file-name.txt")
        .setSenderPath("original/file-name.txt");

List<StorageAccountPath> paths = new ArrayList<>();
paths.add(storageAccountPath);

BlobStorageArtifact artifact = new BlobStorageArtifact()
        .setStoreReference(storeReference)
        .setPaths(paths);

sentShare.setArtifact(artifact);

SyncPoller<BinaryData, BinaryData> response =
        sentSharesClient.beginCreateOrReplaceSentShare(
                sentShareId,
                BinaryData.fromObject(sentShare),
                new RequestOptions());
```

#### Send a Share Invitation to a User

After creating a sent share, the data provider can extend invitations to consumers who may then view the shared data.  In this example, an invitation is extended to an individual by specifying their email address.

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

#### Send a Share Invitation to a Service

Data providers can also extend invitations to services or applications by specifying the tenant id and object id of the service.  *The object id used for sending an invitation to a service must be the object id associated with the Enterprise Application (not the application registration)*.

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

#### Get a Sent Share

After creating a sent share, data providers can retrieve it.

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

#### List Sent Shares

Data providers can also retrieve a list of the sent shares they have created.

```java com.azure.analytics.purview.sharing.listSentShares
SentSharesClient sentSharesClient =
        new SentSharesClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("https://<my-account-name>.purview.azure.com/share")
                .buildClient();

PagedIterable<BinaryData> sentShareResults = sentSharesClient.listSentShares(
                "/subscriptions/de06c3a0-4610-4ca0-8cbb-bbdac204bd65/resourceGroups/provider-storage-rg/providers/Microsoft.Storage/storageAccounts/providerstorage",
                new RequestOptions());

List<SentShare> sentShares = sentShareResults.stream()
    .map(binaryData -> binaryData.toObject(SentShare.class))
    .collect(Collectors.toList());
```

#### Delete a Sent Share

A sent share can be deleted by the data provider to stop sharing their data with all data consumers.

```java com.azure.analytics.purview.sharing.deleteSentShare
SentSharesClient sentSharesClient =
        new SentSharesClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("https://<my-account-name>.purview.azure.com/share")
                .buildClient();

sentSharesClient.beginDeleteSentShare("<sent-share-id", new RequestOptions());
```

#### Get Sent Share Invitation

After creating a sent share invitation, data providers can retrieve it.

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

#### List Sent Share Invitations

Data providers can also retrieve a list of the sent share invitations they have created.

```java com.azure.analytics.purview.sharing.listSentShareInvitations
SentSharesClient sentSharesClient =
        new SentSharesClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("https://<my-account-name>.purview.azure.com/share")
                .buildClient();

String sentShareId = "<sent-share-id>";

RequestOptions requestOptions = new RequestOptions().addQueryParam("$orderBy", "properties/sentAt desc");
PagedIterable<BinaryData> response =
        sentSharesClient.listSentShareInvitations(sentShareId, requestOptions);
```

#### Delete a Sent Share Invitation

An individual sent share invitation can be deleted by the data provider to stop sharing their data with the specific data consumer to whom the invitation was addressed.

```java com.azure.analytics.purview.sharing.deleteSentShareInvitation
SentSharesClient sentSharesClient =
        new SentSharesClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("https://<my-account-name>.purview.azure.com/share")
                .buildClient();

String sentShareId = "<sent-share-id>";
String sentShareInvitationId = "<sent-share-invitation-id>";

sentSharesClient.beginDeleteSentShareInvitation(sentShareId, sentShareInvitationId, new RequestOptions());
```

### Data Consumer Examples

The following code examples demonstrate how data consumers can use the Microsoft Azure Java SDK for Purview Sharing to manage their sharing activity.

#### Create a Received Share Client
```java com.azure.analytics.purview.sharing.createReceivedShareClient
ReceivedSharesClient receivedSharesClient =
        new ReceivedSharesClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("https://<my-account-name>.purview.azure.com/share")
                .buildClient();
```

#### List Detached Received Shares

To begin viewing data shared with them, a data consumer must first retrieve a list of detached received shares.  Within this list, they can identify a detached received share to attach.

```java com.azure.analytics.purview.sharing.listDetachedReceivedShares
ReceivedSharesClient receivedSharesClient =
        new ReceivedSharesClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("https://<my-account-name>.purview.azure.com/share")
                .buildClient();

RequestOptions requestOptions = new RequestOptions().addQueryParam("$orderBy", "properties/createdAt desc");
PagedIterable<BinaryData> response = receivedSharesClient.listDetachedReceivedShares(requestOptions);
```

#### Attach a Receive Share

Once the data consumer has identified a received share, they can attach the received share to a location where they can access the shared data.  If the received share is already attached, the shared data will be made accessible at the new location specified.

```java com.azure.analytics.purview.sharing.attachReceivedShare
ReceivedSharesClient receivedSharesClient =
        new ReceivedSharesClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("https://<my-account-name>.purview.azure.com/share")
                .buildClient();

RequestOptions listRequestOptions = new RequestOptions().addQueryParam("$orderBy", "properties/createdAt desc");
PagedIterable<BinaryData> listResponse = receivedSharesClient.listDetachedReceivedShares(listRequestOptions);

Optional<BinaryData> detachedReceivedShare = listResponse.stream().findFirst();

if (!detachedReceivedShare.isPresent()) {
    return;
}

String receivedShareId = new ObjectMapper()
        .readValue(detachedReceivedShare.get().toString(), ObjectNode.class)
        .get("id")
        .textValue();
 
InPlaceReceivedShare receivedShare = new InPlaceReceivedShare()
        .setDisplayName("my-received-share");

StoreReference storeReference = new StoreReference()
        .setReferenceName("/subscriptions/de06c3a0-4610-4ca0-8cbb-bbdac204bd65/resourceGroups/consumer-storage-rg/providers/Microsoft.Storage/storageAccounts/consumerstorage")
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

#### Get Received Share

A data consumer can retrieve an individual received share.

```java com.azure.analytics.purview.sharing.getReceivedShare
ReceivedSharesClient receivedSharesClient =
        new ReceivedSharesClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("https://<my-account-name>.purview.azure.com/share")
                .buildClient();

Response<BinaryData> receivedShare =
        receivedSharesClient.getReceivedShareWithResponse("<received-share-id>", new RequestOptions());
```

#### List Attached Received Shares

Data consumers can also retrieve a list of their attached received shares.

```java com.azure.analytics.purview.sharing.listAttachedReceivedShares
ReceivedSharesClient receivedSharesClient =
        new ReceivedSharesClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("https://<my-account-name>.purview.azure.com/share")
                .buildClient();

RequestOptions requestOptions = new RequestOptions().addQueryParam("$orderBy", "properties/createdAt desc");
PagedIterable<BinaryData> response =
        receivedSharesClient.listAttachedReceivedShares(
                "/subscriptions/de06c3a0-4610-4ca0-8cbb-bbdac204bd65/resourceGroups/consumer-storage-rg/providers/Microsoft.Storage/storageAccounts/consumerstorage",
                requestOptions);

Optional<BinaryData> receivedShare = response.stream().findFirst();

if (!receivedShare.isPresent()) {
    return;
}

ReceivedShare receivedShareResponse = receivedShare.get().toObject(InPlaceReceivedShare.class);
```

#### Delete a Received Share

A received share can be deleted by the data consumer to terminate their access to shared data.

```java com.azure.analytics.purview.sharing.deleteReceivedShare
ReceivedSharesClient receivedSharesClient =
        new ReceivedSharesClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("https://<my-account-name>.purview.azure.com/share")
                .buildClient();

receivedSharesClient.beginDeleteReceivedShare("<received-share-id>", new RequestOptions()); 
```

### Share Resource Examples

The following code examples demonstrate how to use the Microsoft Azure Java SDK for Purview Sharing to view share resources.  A share resource is the underlying resource from which a provider shares data or the destination where a consumer attaches data shared with them.

#### List Share Resources

A list of share resources can be retrieved to view all resources within an account where sharing activities have taken place.

```java com.azure.analytics.purview.sharing.listShareResources
ShareResourcesClient shareResourcesClient =
        new ShareResourcesClientBuilder()
                .credential(new DefaultAzureCredentialBuilder().build())
                .endpoint("https://<my-account-name>.purview.azure.com/share")
                .buildClient();

PagedIterable<BinaryData> shareResourceResults = shareResourcesClient.listShareResources(new RequestOptions());
 
List<ShareResource> shareResources = shareResourceResults.stream()
    .map(binaryData -> binaryData.toObject(ShareResource.class))
    .collect(Collectors.toList());
```

## Troubleshooting

### Enabling Logging

Azure SDKs for Java offer a consistent logging story to help aid in troubleshooting application errors and expedite
their resolution. The logs produced will capture the flow of an application before reaching the terminal state to help
locate the root issue. View the [logging][logging] wiki for guidance about enabling logging.

## Next steps

## Contributing

For details on contributing to this repository, see the [contributing guide](https://github.com/Azure/azure-sdk-for-java/blob/main/CONTRIBUTING.md).

1. Fork it
2. Create your feature branch (`git checkout -b my-new-feature`)
3. Commit your changes (`git commit -am 'Add some feature'`)
4. Push to the branch (`git push origin my-new-feature`)
5. Create new Pull Request

<!-- LINKS -->
[source_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/purview/azure-analytics-purview-sharing/src
[package]: https://central.sonatype.com/artifact/com.azure/azure-analytics-purview-sharing
[share_product_documentation]: https://docs.microsoft.com/azure/purview/concept-data-share
[samples_code]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/purview/azure-analytics-purview-sharing/src/samples/
[docs]: https://azure.github.io/azure-sdk-for-java/
[jdk]: https://docs.microsoft.com/java/azure/jdk/
[azure_subscription]: https://azure.microsoft.com/free/
[azure_identity]: https://github.com/Azure/azure-sdk-for-java/blob/main/sdk/identity/azure-identity
[create_azure_purview_account]: https://docs.microsoft.com/azure/purview/create-catalog-portal
[logging]: https://github.com/Azure/azure-sdk-for-java/wiki/Logging-in-Azure-SDK
