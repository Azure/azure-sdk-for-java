// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms;

import com.azure.communication.rooms.models.*;


import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class RoomsClientTest extends RoomsTestBase {
    private RoomsClient roomsClient;

    @Override
    protected void beforeTest() {
        super.beforeTest();
    }

    @Override
    protected void afterTest() {
        super.afterTest();
        cleanUpUsers();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncWithFullOperation(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncWithFullOperation");
        assertNotNull(roomsClient);

        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL)
                .setParticipants(participants2);

        CommunicationRoom createCommunicationRoom = roomsClient.createRoom(createRoomOptions);
        assertHappyPath(createCommunicationRoom);

        String roomId = createCommunicationRoom.getRoomId();

        UpdateRoomOptions updateRoomOptions = new UpdateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_FROM.plusMonths(4));

        CommunicationRoom updateCommunicationRoom = roomsClient.updateRoom(roomId, updateRoomOptions);
        assertEquals(true, updateCommunicationRoom.getValidUntil().toEpochSecond() > VALID_FROM.toEpochSecond());
        assertHappyPath(updateCommunicationRoom);

        AddOrUpdateParticipantsResult roomParticipants = roomsClient.addOrUpdateParticipants(roomId, participants2);

        CommunicationRoom getCommunicationRoom = roomsClient.getRoom(roomId);
        assertHappyPath(getCommunicationRoom);

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void deleteRoomSync(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "deleteRoomSync");
        assertNotNull(roomsClient);

        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL)
                .setParticipants(participants2);

        CommunicationRoom createCommunicationRoom = roomsClient.createRoom(createRoomOptions);
        assertHappyPath(createCommunicationRoom);

        String roomId = createCommunicationRoom.getRoomId();

        // Test delete room without response
        Void deleteResponse = roomsClient.deleteRoom(roomId);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncWithFullOperationWithResponse(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncWithFullOperationWithResponse");
        assertNotNull(roomsClient);

        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL)
                .setParticipants(participants2);

        Response<CommunicationRoom> createdRoomResponse = roomsClient.createRoomWithResponse(createRoomOptions,
                Context.NONE);
        assertHappyPath(createdRoomResponse, 201);

        String roomId = createdRoomResponse.getValue().getRoomId();

        UpdateRoomOptions updateRoomOptions = new UpdateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_FROM.plusMonths(4));

        Response<CommunicationRoom> updateRoomResponse = roomsClient.updateRoomWithResponse(roomId, updateRoomOptions,
                Context.NONE);
        assertHappyPath(updateRoomResponse, 200);

        Response<AddOrUpdateParticipantsResult> addPartcipantResponse = roomsClient.addOrUpdateParticipantsWithResponse(roomId,
                participants2, Context.NONE);
        assertEquals(addPartcipantResponse.getStatusCode(), 200);

        Response<CommunicationRoom> getRoomResponse = roomsClient.getRoomWithResponse(roomId, Context.NONE);
        assertHappyPath(getRoomResponse, 200);

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void addUpdateAndRemoveParticipantsOperationsSyncWithFullFlow(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "addUpdateAndRemoveParticipantsOperationsSyncWithFullFlow");
        assertNotNull(roomsClient);

        // Create empty room
        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL);

        CommunicationRoom createCommunicationRoom = roomsClient.createRoom(createRoomOptions);
        assertHappyPath(createCommunicationRoom);

        String roomId = createCommunicationRoom.getRoomId();

        // Check participant count, expected 0
        PagedIterable<RoomParticipant> listParticipantsResponse1 = roomsClient.listParticipants(roomId);
        assertEquals(0, listParticipantsResponse1.stream().count());

        // Add 3 participants.
        AddOrUpdateParticipantsResult addPartcipantResponse = roomsClient.addOrUpdateParticipants(roomId,
                participants3);
        assertEquals(true, addPartcipantResponse instanceof AddOrUpdateParticipantsResult);

        // Check participant count, expected 3
        PagedIterable<RoomParticipant> listParticipantsResponse2 = roomsClient.listParticipants(roomId);
        assertEquals(3, listParticipantsResponse2.stream().count());

        // Update 2 participants roles, ATTENDEE -> CONSUMER
        AddOrUpdateParticipantsResult updateParticipantResponse = roomsClient.addOrUpdateParticipants(roomId,
                participantsWithRoleUpdates);
        assertEquals(true, updateParticipantResponse instanceof AddOrUpdateParticipantsResult);

        // Check paticipants new roles, everyone should be CONSUMER
        PagedIterable<RoomParticipant> listParticipantsResponse3 = roomsClient.listParticipants(roomId);

        for (RoomParticipant participant : listParticipantsResponse3) {
            assertEquals(ParticipantRole.CONSUMER, participant.getRole());
        }

        // Remove 2 participants
        RemoveParticipantsResult removeParticipantResponse = roomsClient.removeParticipants(roomId, participantsIdentifiersForParticipants2);

        // Check participant count, expected 1
        PagedIterable<RoomParticipant> listParticipantsResponse4 = roomsClient.listParticipants(roomId);
        assertEquals(1, listParticipantsResponse4.stream().count());

        // Delete Room
        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void addParticipantsSyncWithFullOperation(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "addParticipantsSyncWithFullOperation");
        assertNotNull(roomsClient);

        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL);

        CommunicationRoom createCommunicationRoom = roomsClient.createRoom(createRoomOptions);
        assertHappyPath(createCommunicationRoom);

        String roomId = createCommunicationRoom.getRoomId();

        // Add 3 participants
        AddOrUpdateParticipantsResult addPartcipantResponse = roomsClient.addOrUpdateParticipants(roomId, participants3);

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void addParticipantsSyncWithFullOperationWithResponse(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "addParticipantsSyncWithFullOperationWithResponse");
        assertNotNull(roomsClient);

        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL);

        Response<CommunicationRoom> createdRoomResponse = roomsClient.createRoomWithResponse(createRoomOptions,
                Context.NONE);
        assertEquals(createdRoomResponse.getStatusCode(), 201);

        String roomId = createdRoomResponse.getValue().getRoomId();

        // Add 3 participants.
        Response<AddOrUpdateParticipantsResult> addPartcipantResponse = roomsClient.addOrUpdateParticipantsWithResponse(roomId,
                participants3, Context.NONE);
        assertEquals(addPartcipantResponse.getStatusCode(), 200);

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateParticipantsToDefaultRoleSyncWithFullOperationWithResponse(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateParticipantsToDefaultRoleSyncWithFullOperationWithResponse");
        assertNotNull(roomsClient);

        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL)
                .setParticipants(participant1);

        Response<CommunicationRoom> createCommunicationRoom = roomsClient.createRoomWithResponse(createRoomOptions,
                Context.NONE);
        assertHappyPath(createCommunicationRoom, 201);

        String roomId = createCommunicationRoom.getValue().getRoomId();

        // Update participant to default role with null role.
        Response<AddOrUpdateParticipantsResult> addPartcipantResponse = roomsClient.addOrUpdateParticipantsWithResponse(roomId,
                participant1, Context.NONE);
        assertEquals(200, addPartcipantResponse.getStatusCode());

        PagedIterable<RoomParticipant> listReponse = roomsClient.listParticipants(roomId);

        for (RoomParticipant participant : listReponse) {
            assertEquals(ParticipantRole.ATTENDEE, participant.getRole());
        }

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateParticipantsSyncWithFullOperation(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateParticipantsSyncWithFullOperation");
        assertNotNull(roomsClient);

        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL)
                .setParticipants(participants2);

        CommunicationRoom createCommunicationRoom = roomsClient.createRoom(createRoomOptions);
        assertHappyPath(createCommunicationRoom);

        String roomId = createCommunicationRoom.getRoomId();

        // Update 2 participants.
        AddOrUpdateParticipantsResult addPartcipantResponse = roomsClient.addOrUpdateParticipants(roomId,
                participantsWithRoleUpdates);

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateParticipantsSyncWithFullOperationWithResponse(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateParticipantsSyncWithFullOperationWithResponse");
        assertNotNull(roomsClient);

        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL)
                .setParticipants(participants2);

        Response<CommunicationRoom> createdRoomResponse = roomsClient.createRoomWithResponse(createRoomOptions,
                Context.NONE);
        assertHappyPath(createdRoomResponse, 201);

        String roomId = createdRoomResponse.getValue().getRoomId();

        // Add 3 participants.
        Response<AddOrUpdateParticipantsResult> addPartcipantResponse = roomsClient.addOrUpdateParticipantsWithResponse(roomId,
                participantsWithRoleUpdates, Context.NONE);
        assertEquals(addPartcipantResponse.getStatusCode(), 200);

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void patchMeetingValidTimeWithResponse(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "patchMeetingValidTimeWithResponse");
        assertNotNull(roomsClient);

        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setParticipants(participants2);

        Response<CommunicationRoom> createdRoomResponse = roomsClient.createRoomWithResponse(createRoomOptions,
                Context.NONE);
        assertHappyPath(createdRoomResponse, 201);

        String roomId = createdRoomResponse.getValue().getRoomId();

        UpdateRoomOptions updateRoomOptions = new UpdateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL);

        Response<CommunicationRoom> updateRoomResponse = roomsClient.updateRoomWithResponse(roomId, updateRoomOptions,
                Context.NONE);
        assertHappyPath(updateRoomResponse, 200);

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getRoomWithUnexistingRoomIdReturnBadRequest(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "getRoomWithUnexistingRoomIdReturnBadRequest");
        assertNotNull(roomsClient);
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.getRoom(NONEXIST_ROOM_ID);
        });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void deleteRoomWithUnexistingRoomIdReturnBadRequest(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "deleteRoomWithUnexistingRoomIdReturnBadRequest");
        assertNotNull(roomsClient);
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.deleteRoomWithResponse(NONEXIST_ROOM_ID, Context.NONE);
        });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncNoAttributes(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncNoAttributes");
        assertNotNull(roomsClient);
        CommunicationRoom createCommunicationRoom = roomsClient.createRoom(new CreateRoomOptions());
        assertHappyPath(createCommunicationRoom);

        String roomId = createCommunicationRoom.getRoomId();

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncOnlyValidFrom(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncOnlyValidFrom");
        assertNotNull(roomsClient);

        CommunicationRoom createCommunicationRoom = roomsClient
                .createRoom(new CreateRoomOptions().setValidFrom(VALID_FROM));
        assertHappyPath(createCommunicationRoom);

        String roomId = createCommunicationRoom.getRoomId();

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncOnlyValidUntil(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncOnlyValidUntil");
        assertNotNull(roomsClient);
        CommunicationRoom createCommunicationRoom = roomsClient
                .createRoom(new CreateRoomOptions().setValidUntil(VALID_UNTIL));
        assertHappyPath(createCommunicationRoom);

        String roomId = createCommunicationRoom.getRoomId();

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncOnlyParticipants(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncOnlyParticipants");
        assertNotNull(roomsClient);
        CommunicationRoom createCommunicationRoom = roomsClient
                .createRoom(new CreateRoomOptions().setParticipants(participants2));
        assertHappyPath(createCommunicationRoom);

        String roomId = createCommunicationRoom.getRoomId();

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncOnlyValidUntilGreaterThan180(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncOnlyValidUntilGreaterThan180");
        assertNotNull(roomsClient);
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.createRoom(new CreateRoomOptions().setValidUntil(VALID_FROM.plusDays(181)));
        });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncOnlyValidFromGreaterThan180(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncOnlyValidFromGreaterThan180");
        assertNotNull(roomsClient);
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.createRoom(new CreateRoomOptions().setValidFrom(VALID_FROM.plusDays(181)));
        });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncValidFromValidUntilGreaterThan180(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncValidFromValidUntilGreaterThan180");
        assertNotNull(roomsClient);
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.createRoom(
                    new CreateRoomOptions().setValidFrom(VALID_FROM).setValidUntil(VALID_FROM.plusDays(181)));
        });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncBadParticipantMri(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncBadParticipantMri");
        assertNotNull(roomsClient);
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.createRoom(new CreateRoomOptions().setParticipants(badParticipant));
        });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateRoomSyncOnlyValidFrom(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateRoomSyncOnlyValidFrom");
        assertNotNull(roomsClient);

        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL)
                .setParticipants(participants2);

        CommunicationRoom createdRoom = roomsClient.createRoom(createRoomOptions);
        assertHappyPath(createdRoom);

        String roomId = createdRoom.getRoomId();

        UpdateRoomOptions updateRoomOptions = new UpdateRoomOptions()
                .setValidFrom(VALID_FROM.plusMonths(4));

        assertThrows(HttpResponseException.class, () -> {
            roomsClient.updateRoom(roomId, updateRoomOptions);
        });

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateRoomSyncOnlyValidUntil(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateRoomSyncOnlyValidUntil");
        assertNotNull(roomsClient);

        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL)
                .setParticipants(participants2);

        CommunicationRoom createdRoom = roomsClient.createRoom(createRoomOptions);

        assertHappyPath(createdRoom);

        String roomId = createdRoom.getRoomId();

        UpdateRoomOptions updateRoomOptions = new UpdateRoomOptions()
                .setValidUntil(VALID_FROM.plusMonths(4));

        assertThrows(HttpResponseException.class, () -> {
            roomsClient.updateRoom(roomId, updateRoomOptions);
        });

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateRoomSyncValidUntilGreaterThan180(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateRoomSyncValidUntilGreaterThan180");
        assertNotNull(roomsClient);

        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL)
                .setParticipants(participants2);

        CommunicationRoom createdRoom = roomsClient.createRoom(createRoomOptions);
        assertHappyPath(createdRoom);

        String roomId = createdRoom.getRoomId();

        UpdateRoomOptions updateRoomOptions = new UpdateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_FROM.plusDays(181));

        assertThrows(HttpResponseException.class, () -> {
            roomsClient.updateRoom(roomId, updateRoomOptions);
        });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateRoomSyncValidFromGreaterThan180(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateRoomSyncValidFromGreaterThan180");
        assertNotNull(roomsClient);

        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL)
                .setParticipants(participants2);

        CommunicationRoom createdRoom = roomsClient.createRoom(createRoomOptions);
        assertHappyPath(createdRoom);

        String roomId = createdRoom.getRoomId();

        UpdateRoomOptions updateRoomOptions = new UpdateRoomOptions()
                .setValidFrom(VALID_FROM.plusDays(181))
                .setValidUntil(VALID_UNTIL);

        assertThrows(HttpResponseException.class, () -> {
            roomsClient.updateRoom(roomId, updateRoomOptions);
        });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateRoomSyncBadParticipantMri(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateRoomSyncBadParticipantMri");
        assertNotNull(roomsClient);

        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL)
                .setParticipants(participants2);

        CommunicationRoom createdRoom = roomsClient.createRoom(createRoomOptions);
        assertHappyPath(createdRoom);

        String roomId = createdRoom.getRoomId();
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.addOrUpdateParticipants(roomId, badParticipant);
        });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncWithResponseNoAttributes(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncWithResponseNoAttributes");
        assertNotNull(roomsClient);

        Response<CommunicationRoom> createCommunicationRoom = roomsClient
                .createRoomWithResponse(new CreateRoomOptions(), Context.NONE);
        assertHappyPath(createCommunicationRoom, 201);

        String roomId = createCommunicationRoom.getValue().getRoomId();
        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncWithResponseOnlyValidFrom(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncWithResponseOnlyValidFrom");
        assertNotNull(roomsClient);

        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM);

        Response<CommunicationRoom> createCommunicationRoom = roomsClient.createRoomWithResponse(createRoomOptions,
                Context.NONE);

        assertHappyPath(createCommunicationRoom, 201);

        String roomId = createCommunicationRoom.getValue().getRoomId();

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncWithReponseOnlyValidUntil(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncWithReponseOnlyValidUntil");
        assertNotNull(roomsClient);

        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidUntil(VALID_UNTIL);

        Response<CommunicationRoom> createCommunicationRoom = roomsClient.createRoomWithResponse(createRoomOptions,
                Context.NONE);

        assertHappyPath(createCommunicationRoom, 201);

        String roomId = createCommunicationRoom.getValue().getRoomId();

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncWithReponseOnlyValidUntilGreaterThan180(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncWithReponseOnlyValidUntilGreaterThan180");
        assertNotNull(roomsClient);

        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(null)
                .setValidUntil(VALID_FROM.plusDays(181))
                .setParticipants(null);

        assertThrows(HttpResponseException.class, () -> {
            roomsClient.createRoomWithResponse(createRoomOptions, Context.NONE);
        });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncWithReponseOnlyValidFromGreaterThan180(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncWithReponseOnlyValidFromGreaterThan180");
        assertNotNull(roomsClient);

        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM.plusDays(181))
                .setValidUntil(null)
                .setParticipants(null);

        assertThrows(HttpResponseException.class, () -> {
            roomsClient.createRoomWithResponse(createRoomOptions, Context.NONE);
        });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncWithReponseValidFromValidUntilGreaterThan180(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncWithReponseValidFromValidUntilGreaterThan180");
        assertNotNull(roomsClient);

        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_FROM.plusDays(181));

        assertThrows(HttpResponseException.class, () -> {
            roomsClient.createRoomWithResponse(createRoomOptions, Context.NONE);
        });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncWithResponseBadParticipantMri(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncWithResponseBadParticipantMri");
        assertNotNull(roomsClient);

        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(null)
                .setValidUntil(null)
                .setParticipants(badParticipant);

        assertThrows(HttpResponseException.class, () -> {
            roomsClient.createRoomWithResponse(createRoomOptions, Context.NONE);
        });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateRoomSyncWithReponseOnlyValidFrom(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateRoomSyncWithReponseOnlyValidFrom");
        assertNotNull(roomsClient);

        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL)
                .setParticipants(participants2);

        CommunicationRoom createdRoom = roomsClient.createRoom(createRoomOptions);
        assertHappyPath(createdRoom);

        String roomId = createdRoom.getRoomId();

        UpdateRoomOptions updateRoomOptions = new UpdateRoomOptions()
                .setValidFrom(VALID_FROM.plusMonths(4))
                .setValidUntil(null);

        assertThrows(HttpResponseException.class, () -> {
            roomsClient.updateRoomWithResponse(roomId, updateRoomOptions, Context.NONE);
        });
        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateRoomSyncWithReponseOnlyValidUntil(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateRoomSyncWithReponseOnlyValidUntil");
        assertNotNull(roomsClient);

        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL)
                .setParticipants(participants2);

        CommunicationRoom createdRoom = roomsClient.createRoom(createRoomOptions);
        assertHappyPath(createdRoom);

        String roomId = createdRoom.getRoomId();

        UpdateRoomOptions updateRoomOptions = new UpdateRoomOptions()
                .setValidFrom(null)
                .setValidUntil(VALID_FROM.plusMonths(4));

        assertThrows(HttpResponseException.class, () -> {
            roomsClient.updateRoomWithResponse(roomId, updateRoomOptions, Context.NONE);
        });

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateRoomSyncWithReponseValidUntilGreaterThan180(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateRoomSyncValidUntilWithReponseGreaterThan180");
        assertNotNull(roomsClient);

        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL)
                .setParticipants(participants2);

        CommunicationRoom createdRoom = roomsClient.createRoom(createRoomOptions);
        assertHappyPath(createdRoom);

        String roomId = createdRoom.getRoomId();

        UpdateRoomOptions updateRoomOptions = new UpdateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_FROM.plusDays(181));

        assertThrows(HttpResponseException.class, () -> {
            roomsClient.updateRoomWithResponse(roomId, updateRoomOptions, Context.NONE);
        });
        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateRoomSyncWithReponseValidFromGreaterThan180(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateRoomSyncWithReponseValidFromGreaterThan180");
        assertNotNull(roomsClient);

        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL)
                .setParticipants(participants2);

        CommunicationRoom createdRoom = roomsClient.createRoom(createRoomOptions);
        assertHappyPath(createdRoom);

        String roomId = createdRoom.getRoomId();

        UpdateRoomOptions updateRoomOptions = new UpdateRoomOptions()
                .setValidFrom(VALID_FROM.plusDays(181))
                .setValidUntil(VALID_UNTIL);

        assertThrows(HttpResponseException.class, () -> {
            roomsClient.updateRoomWithResponse(roomId, updateRoomOptions, Context.NONE);
        });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateRoomSyncWithReponseBadParticipantMri(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateRoomSyncWithReponseBadParticipantMri");
        assertNotNull(roomsClient);

        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL)
                .setParticipants(participants2);

        CommunicationRoom createdRoom = roomsClient.createRoom(createRoomOptions);
        assertHappyPath(createdRoom);

        String roomId = createdRoom.getRoomId();
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.addOrUpdateParticipants(roomId, badParticipant);
        });
    }

    private RoomsClient setupSyncClient(HttpClient httpClient, String testName) {
        RoomsClientBuilder builder = getRoomsClientWithConnectionString(httpClient,
                RoomsServiceVersion.V2023_03_31_PREVIEW);
        createUsers(httpClient);
        return addLoggingPolicy(builder, testName).buildClient();
    }
}
