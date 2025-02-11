// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.purview.sharing;

import com.azure.analytics.purview.sharing.models.BlobAccountSink;
import com.azure.analytics.purview.sharing.models.BlobAccountSinkProperties;
import com.azure.analytics.purview.sharing.models.BlobStorageArtifact;
import com.azure.analytics.purview.sharing.models.BlobStorageArtifactProperties;
import com.azure.analytics.purview.sharing.models.InPlaceReceivedShare;
import com.azure.analytics.purview.sharing.models.InPlaceReceivedShareProperties;
import com.azure.analytics.purview.sharing.models.InPlaceSentShare;
import com.azure.analytics.purview.sharing.models.InPlaceSentShareProperties;
import com.azure.analytics.purview.sharing.models.ReceivedShare;
import com.azure.analytics.purview.sharing.models.ReferenceNameType;
import com.azure.analytics.purview.sharing.models.SentShare;
import com.azure.analytics.purview.sharing.models.ServiceInvitation;
import com.azure.analytics.purview.sharing.models.ServiceInvitationProperties;
import com.azure.analytics.purview.sharing.models.ShareResource;
import com.azure.analytics.purview.sharing.models.StorageAccountPath;
import com.azure.analytics.purview.sharing.models.StoreReference;
import com.azure.analytics.purview.sharing.models.UserInvitation;
import com.azure.analytics.purview.sharing.models.UserInvitationProperties;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.SyncPoller;
import com.azure.identity.DefaultAzureCredentialBuilder;

import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@SuppressWarnings("unused")
public final class ReadmeSamples {

    public void createSentShareClientSample() {
        // BEGIN: com.azure.analytics.purview.sharing.createSentShareClient
        SentSharesClient sentSharesClient = new SentSharesClientBuilder().credential(
                new DefaultAzureCredentialBuilder().build())
            .endpoint("https://<my-account-name>.purview.azure.com/share")
            .buildClient();
        // END: com.azure.analytics.purview.sharing.createSentShareClient
    }

    public void createSentShareSample() {
        // BEGIN: com.azure.analytics.purview.sharing.createSentShare
        SentSharesClient sentSharesClient = new SentSharesClientBuilder().credential(
                new DefaultAzureCredentialBuilder().build())
            .endpoint("https://<my-account-name>.purview.azure.com/share")
            .buildClient();

        String sentShareId = UUID.randomUUID().toString();
        InPlaceSentShare sentShare = new InPlaceSentShare().setProperties(
            new InPlaceSentShareProperties().setDisplayName("sample-share").setDescription("A sample share"));

        StoreReference storeReference = new StoreReference().setReferenceName(
                "/subscriptions/de06c3a0-4610-4ca0-8cbb-bbdac204bd65/resourceGroups/provider-storage-rg/providers/Microsoft.Storage/storageAccounts/providerstorage")
            .setType(ReferenceNameType.ARM_RESOURCE_REFERENCE);

        StorageAccountPath storageAccountPath = new StorageAccountPath().setContainerName("container-name")
            .setReceiverPath("shared-file-name.txt")
            .setSenderPath("original/file-name.txt");

        List<StorageAccountPath> paths = new ArrayList<>();
        paths.add(storageAccountPath);

        BlobStorageArtifact artifact = new BlobStorageArtifact().setStoreReference(storeReference)
            .setProperties(new BlobStorageArtifactProperties().setPaths(paths));

        sentShare.getProperties().setArtifact(artifact);

        SyncPoller<BinaryData, BinaryData> response = sentSharesClient.beginCreateOrReplaceSentShare(sentShareId,
            BinaryData.fromObject(sentShare), new RequestOptions());
        // END: com.azure.analytics.purview.sharing.createSentShare
    }

    public void getSentShareSample() {
        // BEGIN: com.azure.analytics.purview.sharing.getSentShare
        SentSharesClient sentSharesClient = new SentSharesClientBuilder().credential(
                new DefaultAzureCredentialBuilder().build())
            .endpoint("https://<my-account-name>.purview.azure.com/share")
            .buildClient();

        SentShare retrievedSentShare = sentSharesClient.getSentShareWithResponse("<sent-share-id>",
            new RequestOptions()).getValue().toObject(SentShare.class);
        // END: com.azure.analytics.purview.sharing.getSentShare
    }

    public void deleteSentShareSample() {
        // BEGIN: com.azure.analytics.purview.sharing.deleteSentShare
        SentSharesClient sentSharesClient = new SentSharesClientBuilder().credential(
                new DefaultAzureCredentialBuilder().build())
            .endpoint("https://<my-account-name>.purview.azure.com/share")
            .buildClient();

        sentSharesClient.beginDeleteSentShare("<sent-share-id", new RequestOptions());
        // END: com.azure.analytics.purview.sharing.deleteSentShare
    }

    public void listSentSharesSample() {
        // BEGIN: com.azure.analytics.purview.sharing.listSentShares
        SentSharesClient sentSharesClient = new SentSharesClientBuilder().credential(
                new DefaultAzureCredentialBuilder().build())
            .endpoint("https://<my-account-name>.purview.azure.com/share")
            .buildClient();

        PagedIterable<BinaryData> sentShareResults = sentSharesClient.listSentShares(
            "/subscriptions/de06c3a0-4610-4ca0-8cbb-bbdac204bd65/resourceGroups/provider-storage-rg/providers/Microsoft.Storage/storageAccounts/providerstorage",
            new RequestOptions());

        List<SentShare> sentShares = sentShareResults.stream()
            .map(binaryData -> binaryData.toObject(SentShare.class))
            .collect(Collectors.toList());
        // END: com.azure.analytics.purview.sharing.listSentShares
    }

    public void sendUserInvitationSample() {
        // BEGIN: com.azure.analytics.purview.sharing.sendUserInvitation
        SentSharesClient sentSharesClient = new SentSharesClientBuilder().credential(
                new DefaultAzureCredentialBuilder().build())
            .endpoint("https://<my-account-name>.purview.azure.com/share")
            .buildClient();

        String sentShareId = "<sent-share-id>";
        String sentShareInvitationId = UUID.randomUUID().toString();

        UserInvitation sentShareInvitation = new UserInvitation().setProperties(
            new UserInvitationProperties().setTargetEmail("receiver@microsoft.com")
                .setNotify(true)
                .setExpirationDate(OffsetDateTime.now().plusDays(60)));

        Response<BinaryData> response = sentSharesClient.createSentShareInvitationWithResponse(sentShareId,
            sentShareInvitationId, BinaryData.fromObject(sentShareInvitation), new RequestOptions());
        // END: com.azure.analytics.purview.sharing.sendUserInvitation
    }

    public void sendServiceInvitationSample() {
        // BEGIN: com.azure.analytics.purview.sharing.sendServiceInvitation
        SentSharesClient sentSharesClient = new SentSharesClientBuilder().credential(
                new DefaultAzureCredentialBuilder().build())
            .endpoint("https://<my-account-name>.purview.azure.com/share")
            .buildClient();

        String sentShareId = "<sent-share-id>";
        String sentShareInvitationId = UUID.randomUUID().toString();

        ServiceInvitation sentShareInvitation = new ServiceInvitation().setProperties(new ServiceInvitationProperties()
            .setTargetActiveDirectoryId(UUID.fromString("<tenant-id>"))
            .setTargetObjectId(UUID.fromString("<object-id>"))
            .setExpirationDate(OffsetDateTime.now().plusDays(60)));

        Response<BinaryData> response = sentSharesClient.createSentShareInvitationWithResponse(sentShareId,
            sentShareInvitationId, BinaryData.fromObject(sentShareInvitation), new RequestOptions());
        // END: com.azure.analytics.purview.sharing.sendServiceInvitation
    }

    public void viewSentInvitationsSample() {
        // BEGIN: com.azure.analytics.purview.sharing.listSentShareInvitations
        SentSharesClient sentSharesClient = new SentSharesClientBuilder().credential(
                new DefaultAzureCredentialBuilder().build())
            .endpoint("https://<my-account-name>.purview.azure.com/share")
            .buildClient();

        String sentShareId = "<sent-share-id>";

        RequestOptions requestOptions = new RequestOptions().addQueryParam("$orderBy", "properties/sentAt desc");
        PagedIterable<BinaryData> response = sentSharesClient.listSentShareInvitations(sentShareId, requestOptions);
        // END: com.azure.analytics.purview.sharing.listSentShareInvitations
    }

    public void getSentInvitationSample() {
        // BEGIN: com.azure.analytics.purview.sharing.getSentShareInvitation
        SentSharesClient sentSharesClient = new SentSharesClientBuilder().credential(
                new DefaultAzureCredentialBuilder().build())
            .endpoint("https://<my-account-name>.purview.azure.com/share")
            .buildClient();

        String sentShareId = "<sent-share-id>";
        String sentShareInvitationId = "<sent-share-invitation-id>";

        Response<BinaryData> sentShareInvitation = sentSharesClient.getSentShareInvitationWithResponse(sentShareId,
            sentShareInvitationId, new RequestOptions());
        // END: com.azure.analytics.purview.sharing.getSentShareInvitation
    }

    public void deleteSentInvitationSample() {
        // BEGIN: com.azure.analytics.purview.sharing.deleteSentShareInvitation
        SentSharesClient sentSharesClient = new SentSharesClientBuilder().credential(
                new DefaultAzureCredentialBuilder().build())
            .endpoint("https://<my-account-name>.purview.azure.com/share")
            .buildClient();

        String sentShareId = "<sent-share-id>";
        String sentShareInvitationId = "<sent-share-invitation-id>";

        sentSharesClient.beginDeleteSentShareInvitation(sentShareId, sentShareInvitationId, new RequestOptions());
        // END: com.azure.analytics.purview.sharing.deleteSentShareInvitation
    }

    public void createReceivedShareClientSample() {
        // BEGIN: com.azure.analytics.purview.sharing.createReceivedShareClient
        ReceivedSharesClient receivedSharesClient = new ReceivedSharesClientBuilder().credential(
                new DefaultAzureCredentialBuilder().build())
            .endpoint("https://<my-account-name>.purview.azure.com/share")
            .buildClient();
        // END: com.azure.analytics.purview.sharing.createReceivedShareClient
    }

    public void listDetachedReceivedSharesSample() {
        // BEGIN: com.azure.analytics.purview.sharing.listDetachedReceivedShares
        ReceivedSharesClient receivedSharesClient = new ReceivedSharesClientBuilder().credential(
                new DefaultAzureCredentialBuilder().build())
            .endpoint("https://<my-account-name>.purview.azure.com/share")
            .buildClient();

        RequestOptions requestOptions = new RequestOptions().addQueryParam("$orderBy", "properties/createdAt desc");
        PagedIterable<BinaryData> response = receivedSharesClient.listDetachedReceivedShares(requestOptions);
        // END: com.azure.analytics.purview.sharing.listDetachedReceivedShares
    }

    public void attachReceivedShareSample() throws IOException {
        // BEGIN: com.azure.analytics.purview.sharing.attachReceivedShare
        ReceivedSharesClient receivedSharesClient = new ReceivedSharesClientBuilder().credential(
                new DefaultAzureCredentialBuilder().build())
            .endpoint("https://<my-account-name>.purview.azure.com/share")
            .buildClient();

        RequestOptions listRequestOptions = new RequestOptions().addQueryParam("$orderBy", "properties/createdAt desc");
        PagedIterable<BinaryData> listResponse = receivedSharesClient.listDetachedReceivedShares(listRequestOptions);

        Optional<BinaryData> detachedReceivedShare = listResponse.stream().findFirst();

        if (!detachedReceivedShare.isPresent()) {
            return;
        }

        String receivedShareId = detachedReceivedShare.map(binaryData -> binaryData.toObject(ReceivedShare.class).getId())
            .orElse(null);

        InPlaceReceivedShare receivedShare = new InPlaceReceivedShare()
            .setProperties(new InPlaceReceivedShareProperties().setDisplayName("my-received-share"));

        StoreReference storeReference = new StoreReference().setReferenceName(
                "/subscriptions/de06c3a0-4610-4ca0-8cbb-bbdac204bd65/resourceGroups/consumer-storage-rg/providers/Microsoft.Storage/storageAccounts/consumerstorage")
            .setType(ReferenceNameType.ARM_RESOURCE_REFERENCE);

        BlobAccountSink sink = new BlobAccountSink().setStoreReference(storeReference).setProperties(
            new BlobAccountSinkProperties()
                .setContainerName("container-name")
                .setFolder("folderName")
                .setMountPath("optionalMountPath"));

        receivedShare.getProperties().setSink(sink);

        SyncPoller<BinaryData, BinaryData> createResponse = receivedSharesClient.beginCreateOrReplaceReceivedShare(
            receivedShareId, BinaryData.fromObject(receivedShare), new RequestOptions());
        // END: com.azure.analytics.purview.sharing.attachReceivedShare
    }

    public void getReceivedShareSample() {
        // BEGIN: com.azure.analytics.purview.sharing.getReceivedShare
        ReceivedSharesClient receivedSharesClient = new ReceivedSharesClientBuilder().credential(
                new DefaultAzureCredentialBuilder().build())
            .endpoint("https://<my-account-name>.purview.azure.com/share")
            .buildClient();

        Response<BinaryData> receivedShare = receivedSharesClient.getReceivedShareWithResponse("<received-share-id>",
            new RequestOptions());
        // END: com.azure.analytics.purview.sharing.getReceivedShare
    }

    public void listAttachedReceivedShareSample() {
        // BEGIN: com.azure.analytics.purview.sharing.listAttachedReceivedShares
        ReceivedSharesClient receivedSharesClient = new ReceivedSharesClientBuilder().credential(
                new DefaultAzureCredentialBuilder().build())
            .endpoint("https://<my-account-name>.purview.azure.com/share")
            .buildClient();

        RequestOptions requestOptions = new RequestOptions().addQueryParam("$orderBy", "properties/createdAt desc");
        PagedIterable<BinaryData> response = receivedSharesClient.listAttachedReceivedShares(
            "/subscriptions/de06c3a0-4610-4ca0-8cbb-bbdac204bd65/resourceGroups/consumer-storage-rg/providers/Microsoft.Storage/storageAccounts/consumerstorage",
            requestOptions);

        Optional<BinaryData> receivedShare = response.stream().findFirst();

        if (!receivedShare.isPresent()) {
            return;
        }

        ReceivedShare receivedShareResponse = receivedShare.get().toObject(InPlaceReceivedShare.class);
        // END: com.azure.analytics.purview.sharing.listAttachedReceivedShares
    }

    public void deleteReceivedShareSample() {
        // BEGIN: com.azure.analytics.purview.sharing.deleteReceivedShare
        ReceivedSharesClient receivedSharesClient = new ReceivedSharesClientBuilder().credential(
                new DefaultAzureCredentialBuilder().build())
            .endpoint("https://<my-account-name>.purview.azure.com/share")
            .buildClient();

        receivedSharesClient.beginDeleteReceivedShare("<received-share-id>", new RequestOptions());
        // END: com.azure.analytics.purview.sharing.deleteReceivedShare
    }

    public void listShareResourcesSample() {
        // BEGIN: com.azure.analytics.purview.sharing.listShareResources
        ShareResourcesClient shareResourcesClient = new ShareResourcesClientBuilder().credential(
                new DefaultAzureCredentialBuilder().build())
            .endpoint("https://<my-account-name>.purview.azure.com/share")
            .buildClient();

        PagedIterable<BinaryData> shareResourceResults = shareResourcesClient.listShareResources(new RequestOptions());

        List<ShareResource> shareResources = shareResourceResults.stream()
            .map(binaryData -> binaryData.toObject(ShareResource.class))
            .collect(Collectors.toList());
        // END: com.azure.analytics.purview.sharing.listShareResources
    }
}
