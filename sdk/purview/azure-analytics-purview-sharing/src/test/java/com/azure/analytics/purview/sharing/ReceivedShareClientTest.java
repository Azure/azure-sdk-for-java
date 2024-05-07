// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.purview.sharing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.azure.analytics.purview.sharing.models.BlobAccountSink;
import com.azure.analytics.purview.sharing.models.InPlaceReceivedShare;
import com.azure.analytics.purview.sharing.models.ReferenceNameType;
import com.azure.analytics.purview.sharing.models.ShareStatus;
import com.azure.analytics.purview.sharing.models.Sink;
import com.azure.analytics.purview.sharing.models.StoreReference;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;

@SuppressWarnings("unused")
class ReceivedShareClientTest extends PurviewShareTestBase {

    @Override
    protected void beforeTest() {
        super.beforeTest();
    }

    @Test
    void listDetachedShareTest() {
        UUID sentShareId = UUID.fromString(testResourceNamer.randomUuid());
        UUID sentShareInvitationId = UUID.fromString(testResourceNamer.randomUuid());

        super.createSentShareAndServiceInvitation(sentShareId, sentShareInvitationId);

        RequestOptions requestOptions = new RequestOptions().addQueryParam("$orderBy", "properties/createdAt desc");
        PagedIterable<BinaryData> receivedShares = receivedSharesClient.listDetachedReceivedShares(requestOptions);

        assertTrue(receivedShares.stream().findAny().isPresent());
        assertTrue(receivedShares
                    .stream()
                    .map(binaryData -> binaryData.toObject(InPlaceReceivedShare.class))
                    .allMatch(share -> share.getShareStatus().equals(ShareStatus.DETACHED)));
    }

    @Test
    void getReceivedShareTest() {
        UUID sentShareId = UUID.fromString(testResourceNamer.randomUuid());
        UUID sentShareInvitationId = UUID.fromString(testResourceNamer.randomUuid());

        super.createSentShareAndServiceInvitation(sentShareId, sentShareInvitationId);

        RequestOptions requestOptions = new RequestOptions().addQueryParam("$orderBy", "properties/createdAt desc");
        PagedIterable<BinaryData> receivedShares = receivedSharesClient.listDetachedReceivedShares(requestOptions);

        InPlaceReceivedShare receivedShare = receivedShares.stream().findFirst().get().toObject(InPlaceReceivedShare.class);

        InPlaceReceivedShare retrievedShare = this.receivedSharesClient
                .getReceivedShareWithResponse(receivedShare.getId(), new RequestOptions())
                .getValue()
                .toObject(InPlaceReceivedShare.class);

        assertNotNull(retrievedShare);
        assertEquals(receivedShare.getId(), retrievedShare.getId());
        assertEquals(receivedShare.getDisplayName(), retrievedShare.getDisplayName());
    }

    @Test
    void deleteReceivedShareTest() {
        UUID sentShareId = UUID.fromString(testResourceNamer.randomUuid());
        UUID sentShareInvitationId = UUID.fromString(testResourceNamer.randomUuid());

        super.createSentShareAndServiceInvitation(sentShareId, sentShareInvitationId);

        RequestOptions requestOptions = new RequestOptions().addQueryParam("$orderBy", "properties/createdAt desc");
        PagedIterable<BinaryData> receivedShares = receivedSharesClient.listDetachedReceivedShares(requestOptions);

        InPlaceReceivedShare receivedShare = receivedShares.stream().findFirst().get().toObject(InPlaceReceivedShare.class);

        SyncPoller<BinaryData, Void> syncPoller = setPlaybackSyncPollerPollInterval(
            this.receivedSharesClient.beginDeleteReceivedShare(receivedShare.getId(), new RequestOptions()));

        PollResponse<BinaryData> result = syncPoller.waitForCompletion();
        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, result.getStatus());
    }

    @Test
    void attachReceivedShareTest() {

        UUID sentShareId = UUID.fromString(testResourceNamer.randomUuid());
        UUID sentShareInvitationId = UUID.fromString(testResourceNamer.randomUuid());

        super.createSentShareAndServiceInvitation(sentShareId, sentShareInvitationId);

        RequestOptions listRequestOptions = new RequestOptions().addQueryParam("$orderBy", "properties/createdAt desc");
        PagedIterable<BinaryData> listResponse = receivedSharesClient.listDetachedReceivedShares(listRequestOptions);

        Optional<BinaryData> detachedReceivedShare = listResponse.stream().findFirst();

        if (!detachedReceivedShare.isPresent()) {
            fail("ReceivedShare not found.");
        }

        InPlaceReceivedShare receivedShare = detachedReceivedShare.get().toObject(InPlaceReceivedShare.class);

        StoreReference storeReference = new StoreReference()
                .setReferenceName(this.consumerStorageAccountResourceId)
                .setType(ReferenceNameType.ARM_RESOURCE_REFERENCE);

        Sink sink = new BlobAccountSink()
                .setStoreReference(storeReference)
                .setContainerName(testResourceNamer.randomName("container", 26))
                .setFolder(testResourceNamer.randomName("folder", 20))
                .setMountPath(testResourceNamer.randomName("mountpath", 20));

        receivedShare.setSink(sink);

        SyncPoller<BinaryData, BinaryData> createResponse = setPlaybackSyncPollerPollInterval(
            receivedSharesClient.beginCreateOrReplaceReceivedShare(receivedShare.getId(),
                BinaryData.fromObject(receivedShare), new RequestOptions()));

        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, createResponse.waitForCompletion().getStatus());
    }
}
