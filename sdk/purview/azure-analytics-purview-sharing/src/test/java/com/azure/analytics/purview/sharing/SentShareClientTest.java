// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.analytics.purview.sharing;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

import org.junit.jupiter.api.Test;

import com.azure.analytics.purview.sharing.models.SentShare;
import com.azure.analytics.purview.sharing.models.ServiceInvitation;
import com.azure.analytics.purview.sharing.models.UserInvitation;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.http.rest.Response;
import com.azure.core.util.BinaryData;
import com.azure.core.util.polling.LongRunningOperationStatus;
import com.azure.core.util.polling.PollResponse;
import com.azure.core.util.polling.SyncPoller;

@SuppressWarnings("unused")
class SentShareClientTest extends PurviewShareTestBase {

    @Override
    protected void beforeTest() {
        super.beforeTest();
    }

    @Test
    void createSentShareTest() {
        UUID sentShareId = UUID.fromString(testResourceNamer.randomUuid());
        SentShare sentShare = super.createSentShare(sentShareId);

        assertNotNull(sentShare);
        assertEquals(sentShareId.toString(), sentShare.getId());
    }

    @Test
    void createSentShareUserInvitation() {

        UUID sentShareId = UUID.fromString(testResourceNamer.randomUuid());
        String sentShareInvitationId = testResourceNamer.randomUuid();

        this.createSentShare(sentShareId);

        UserInvitation sentShareInvitation = new UserInvitation()
                .setTargetEmail(super.consumerEmail)
                .setNotify(true)
                .setExpirationDate(OffsetDateTime.of(2500, 1, 1, 1, 1, 1, 1, ZoneOffset.UTC));

        Response<BinaryData> invitationResponse = sentSharesClient.createSentShareInvitationWithResponse(
                sentShareId.toString(), sentShareInvitationId, BinaryData.fromObject(sentShareInvitation),
                new RequestOptions());

        UserInvitation invitation = invitationResponse.getValue().toObject(UserInvitation.class);

        assertEquals(201, invitationResponse.getStatusCode());
        assertNotNull(invitation);
        assertEquals(sentShareInvitationId.toString(), invitation.getId());
        assertEquals(this.consumerEmail, invitation.getTargetEmail());
    }

    @Test
    void getSentShareTest() {
        UUID sentShareId = UUID.fromString(testResourceNamer.randomUuid());
        SentShare sentShare = super.createSentShare(sentShareId);

        SentShare retrievedSentShare = super.sentSharesClient
                .getSentShareWithResponse(sentShareId.toString(), new RequestOptions()).getValue()
                .toObject(SentShare.class);

        assertNotNull(retrievedSentShare);
        assertEquals(sentShareId.toString(), retrievedSentShare.getId());
        assertEquals(sentShare.getType(), retrievedSentShare.getType());
    }

    @Test
    void listSentSharesTest() {

        UUID sentShareId = UUID.fromString(testResourceNamer.randomUuid());
        SentShare sentShare = super.createSentShare(sentShareId);

        PagedIterable<BinaryData> sentShares = super.sentSharesClient
                .listSentShares(super.providerStorageAccountResourceId, new RequestOptions());

        assertTrue(sentShares.stream().findAny().isPresent());
        assertTrue(sentShares.stream().map(binaryData -> binaryData.toObject(SentShare.class))
                .anyMatch(share -> share.getId().equals(sentShare.getId())));
    }

    @Test
    void deleteSentShareTest() {
        UUID sentShareId = UUID.fromString(testResourceNamer.randomUuid());
        SentShare sentShare = super.createSentShare(sentShareId);

        SyncPoller<BinaryData, Void> syncPoller = setPlaybackSyncPollerPollInterval(
            super.sentSharesClient.beginDeleteSentShare(sentShareId.toString(), new RequestOptions()));

        PollResponse<BinaryData> result = syncPoller.waitForCompletion();
        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, result.getStatus());
    }

    @Test
    void createSentShareServiceInvitation() {
        UUID sentShareId = UUID.fromString(testResourceNamer.randomUuid());
        UUID sentShareInvitationId = UUID.fromString(testResourceNamer.randomUuid());

        Response<BinaryData> invitationResponse = super.createSentShareAndServiceInvitation(
                sentShareId,
                sentShareInvitationId);

        ServiceInvitation invitation = invitationResponse.getValue().toObject(ServiceInvitation.class);

        assertEquals(201, invitationResponse.getStatusCode());
        assertEquals(sentShareInvitationId.toString(), invitation.getId());
        assertEquals(this.targetActiveDirectoryId, invitation.getTargetActiveDirectoryId().toString());
        assertEquals(this.targetObjectId, invitation.getTargetObjectId().toString());
    }

    @Test
    void getSentShareServiceInvitation() {
        UUID sentShareId = UUID.fromString(testResourceNamer.randomUuid());
        UUID sentShareInvitationId = UUID.fromString(testResourceNamer.randomUuid());
        super.createSentShareAndServiceInvitation(sentShareId, sentShareInvitationId);

        Response<BinaryData> invitationResponse = super.sentSharesClient.getSentShareInvitationWithResponse(
                sentShareId.toString(), sentShareInvitationId.toString(), new RequestOptions());

        ServiceInvitation invitation = invitationResponse.getValue().toObject(ServiceInvitation.class);

        assertEquals(200, invitationResponse.getStatusCode());
        assertEquals(sentShareInvitationId.toString(), invitation.getId());
        assertEquals(this.targetActiveDirectoryId, invitation.getTargetActiveDirectoryId().toString());
        assertEquals(this.targetObjectId, invitation.getTargetObjectId().toString());
    }

    @Test
    void listSentShareServiceInvitations() {
        UUID sentShareId = UUID.fromString(testResourceNamer.randomUuid());
        UUID sentShareInvitationId = UUID.fromString(testResourceNamer.randomUuid());
        super.createSentShareAndServiceInvitation(sentShareId, sentShareInvitationId);

        PagedIterable<BinaryData> invitations = super.sentSharesClient
                .listSentShareInvitations(sentShareId.toString(), new RequestOptions());

        assertTrue(invitations.stream().findAny().isPresent());
        assertTrue(invitations.stream().map(binaryData -> binaryData.toObject(ServiceInvitation.class))
                .anyMatch(invitation -> invitation.getId().equals(sentShareInvitationId.toString())));
    }

    @Test
    void deleteSentShareServiceInvitation() {
        UUID sentShareId = UUID.fromString(testResourceNamer.randomUuid());
        UUID sentShareInvitationId = UUID.fromString(testResourceNamer.randomUuid());

        Response<BinaryData> invitationResponse = super.createSentShareAndServiceInvitation(sentShareId,
                sentShareInvitationId);

        ServiceInvitation invitation = invitationResponse.getValue().toObject(ServiceInvitation.class);

        SyncPoller<BinaryData, Void> syncPoller = setPlaybackSyncPollerPollInterval(
            super.sentSharesClient.beginDeleteSentShareInvitation(sentShareId.toString(),
                sentShareInvitationId.toString(), new RequestOptions()));

        PollResponse<BinaryData> result = syncPoller.waitForCompletion();
        assertEquals(LongRunningOperationStatus.SUCCESSFULLY_COMPLETED, result.getStatus());
    }
}
