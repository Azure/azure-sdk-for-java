// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms;
import com.azure.communication.rooms.models.*;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import java.util.List;
import java.util.stream.IntStream;

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
        CommunicationRoom createCommunicationRoom = roomsClient.createRoom(VALID_FROM, VALID_UNTIL, RoomJoinPolicy.INVITE_ONLY, participants4);
        assertHappyPath(createCommunicationRoom);
        assertEquals(createCommunicationRoom.getParticipants().size(), 2);

        String roomId = createCommunicationRoom.getRoomId();

        CommunicationRoom updateCommunicationRoom = roomsClient.updateRoom(roomId, VALID_FROM, VALID_FROM.plusMonths(4), RoomJoinPolicy.INVITE_ONLY, null);
        assertEquals(updateCommunicationRoom.getParticipants().size(), 2);
        assertHappyPath(updateCommunicationRoom);

        ParticipantsCollection roomParticipants = roomsClient.addParticipants(roomId, participants6);
        assertEquals(roomParticipants.getParticipants().size(), 3);

        CommunicationRoom getCommunicationRoom = roomsClient.getRoom(roomId);
        assertEquals(getCommunicationRoom.getParticipants().size(), 3);
        assertHappyPath(getCommunicationRoom);

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void deleteRoomSync(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "deleteRoomSync");
        assertNotNull(roomsClient);
        CommunicationRoom createCommunicationRoom = roomsClient.createRoom(VALID_FROM, VALID_UNTIL, RoomJoinPolicy.INVITE_ONLY, participants4);
        assertHappyPath(createCommunicationRoom);
        assertEquals(createCommunicationRoom.getParticipants().size(), 2);

        String roomId = createCommunicationRoom.getRoomId();

        // Test delete room without response
        Void deleteResponse = roomsClient.deleteRoom(roomId);        
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncWithFullOperationWithResponse(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncWithFullOperationWithResponse");
        assertNotNull(roomsClient);
        Response<CommunicationRoom> createdRoomResponse = roomsClient.createRoomWithResponse(VALID_FROM, VALID_UNTIL, RoomJoinPolicy.INVITE_ONLY, participants4, Context.NONE);
        assertHappyPath(createdRoomResponse, 201);
        assertEquals(createdRoomResponse.getValue().getParticipants().size(), 2);

        String roomId = createdRoomResponse.getValue().getRoomId();

        Response<CommunicationRoom> updateRoomResponse = roomsClient.updateRoomWithResponse(roomId, VALID_FROM, VALID_FROM.plusMonths(4), RoomJoinPolicy.INVITE_ONLY, null, Context.NONE);
        assertHappyPath(updateRoomResponse, 200);

        ParticipantsCollection roomParticipants = roomsClient.addParticipants(roomId, participants6);
        assertEquals(roomParticipants.getParticipants().size(), 3);

        Response<CommunicationRoom> getRoomResponse = roomsClient.getRoomWithResponse(roomId, Context.NONE);
        assertHappyPath(getRoomResponse, 200);
        assertEquals(getRoomResponse.getValue().getParticipants().size(), 3);

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void addParticipantsSyncWithFullOperation(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "addParticipantsSyncWithFullOperation");
        assertNotNull(roomsClient);
        CommunicationRoom createCommunicationRoom = roomsClient.createRoom(VALID_FROM, VALID_UNTIL, RoomJoinPolicy.INVITE_ONLY, null);
        assertHappyPath(createCommunicationRoom);
        assertEquals(createCommunicationRoom.getParticipants(), Collections.EMPTY_LIST);

        String roomId = createCommunicationRoom.getRoomId();

        // Add 3 participants.
        ParticipantsCollection roomParticipants = roomsClient.addParticipants(roomId, participants5);
        assertEquals(roomParticipants.getParticipants().size(), 3);

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void addParticipantsSyncWithFullOperationWithResponse(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "addParticipantsSyncWithFullOperationWithResponse");
        assertNotNull(roomsClient);

        Response<CommunicationRoom> createdRoomResponse = roomsClient.createRoomWithResponse(VALID_FROM, VALID_UNTIL, RoomJoinPolicy.INVITE_ONLY, null, Context.NONE);
        assertHappyPath(createdRoomResponse, 201);
        assertEquals(createdRoomResponse.getValue().getParticipants(), Collections.EMPTY_LIST);

        String roomId = createdRoomResponse.getValue().getRoomId();

        // Add 3 participants.
        Response<ParticipantsCollection> roomParticipants = roomsClient.addParticipantsWithResponse(roomId, participants5, Context.NONE);
        assertEquals(roomParticipants.getStatusCode(), 200);
        assertEquals(roomParticipants.getValue().getParticipants().size(), 3);

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateParticipantsSyncWithFullOperation(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateParticipantsSyncWithFullOperation");
        assertNotNull(roomsClient);
        CommunicationRoom createCommunicationRoom = roomsClient.createRoom(VALID_FROM, VALID_UNTIL, RoomJoinPolicy.INVITE_ONLY, participants2);
        assertHappyPath(createCommunicationRoom);
        assertEquals(createCommunicationRoom.getParticipants().size(), 2);

        String roomId = createCommunicationRoom.getRoomId();

        // Update 2 participants.
        ParticipantsCollection roomParticipants = roomsClient.updateParticipants(roomId, participantsWithRoleUpdates);
        assertEquals(roomParticipants.getParticipants().size(), 2);

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateParticipantsSyncWithFullOperationWithResponse(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateParticipantsSyncWithFullOperationWithResponse");
        assertNotNull(roomsClient);

        Response<CommunicationRoom> createdRoomResponse = roomsClient.createRoomWithResponse(VALID_FROM, VALID_UNTIL, RoomJoinPolicy.INVITE_ONLY, participants2, Context.NONE);
        assertHappyPath(createdRoomResponse, 201);
        assertEquals(createdRoomResponse.getValue().getParticipants().size(), 2);

        String roomId = createdRoomResponse.getValue().getRoomId();

        // Add 3 participants.
        Response<ParticipantsCollection> participantsCollection = roomsClient.updateParticipantsWithResponse(roomId, participantsWithRoleUpdates, Context.NONE);
        assertEquals(participantsCollection.getStatusCode(), 200);
        assertEquals(participantsCollection.getValue().getParticipants().size(), 2);

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }


    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void patchMeetingValidTimeWithResponse(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "patchMeetingValidTimeWithResponse");
        assertNotNull(roomsClient);
        Response<CommunicationRoom> createdRoomResponse = roomsClient.createRoomWithResponse(VALID_FROM, null, RoomJoinPolicy.INVITE_ONLY, participants4, Context.NONE);
        assertHappyPath(createdRoomResponse, 201);
        assertEquals(createdRoomResponse.getValue().getParticipants().size(), 2);
        //assertEquals(VALID_FROM.getDayOfYear(), createdRoomResponse.getValue().getValidFrom().getDayOfYear());

        String roomId = createdRoomResponse.getValue().getRoomId();

        Response<CommunicationRoom> updateRoomResponse = roomsClient.updateRoomWithResponse(roomId, VALID_FROM, VALID_UNTIL, RoomJoinPolicy.INVITE_ONLY, null, Context.NONE);
        assertHappyPath(updateRoomResponse, 200);
        assertEquals(updateRoomResponse.getValue().getParticipants().size(), 2);
        //assertEquals(updateRoomResponse.getValue().getValidFrom().getDayOfYear(), VALID_FROM.getDayOfYear());
        //assertEquals(updateRoomResponse.getValue().getValidUntil().getDayOfYear(), VALID_UNTIL.getDayOfYear());

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
        CommunicationRoom createCommunicationRoom = roomsClient.createRoom(null, null, null, null);
        assertHappyPath(createCommunicationRoom);
        assertEquals(createCommunicationRoom.getParticipants().size(), 0);

        String roomId = createCommunicationRoom.getRoomId();

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncOnlyOpenRoom(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncOnlyOpenRoom");
        assertNotNull(roomsClient);
        CommunicationRoom createCommunicationRoom = roomsClient.createRoom(null, null, RoomJoinPolicy.COMMUNICATION_SERVICE_USERS, null);
        assertHappyPath(createCommunicationRoom);
        assertEquals(createCommunicationRoom.getRoomJoinPolicy(), RoomJoinPolicy.COMMUNICATION_SERVICE_USERS);
        assertEquals(createCommunicationRoom.getParticipants().size(), 0);

        String roomId = createCommunicationRoom.getRoomId();

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncOnlyValidFrom(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncOnlyValidFrom");
        assertNotNull(roomsClient);
        CommunicationRoom createCommunicationRoom = roomsClient.createRoom(VALID_FROM, null, null, null);
        assertHappyPath(createCommunicationRoom);
        assertEquals(createCommunicationRoom.getParticipants().size(), 0);
        //assertEquals(VALID_FROM.getDayOfYear(), createCommunicationRoom.getValidFrom().getDayOfYear());
        String roomId = createCommunicationRoom.getRoomId();

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncOnlyValidUntil(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncOnlyValidUntil");
        assertNotNull(roomsClient);
        CommunicationRoom createCommunicationRoom = roomsClient.createRoom(null, VALID_UNTIL, null, null);
        assertHappyPath(createCommunicationRoom);
        assertEquals(createCommunicationRoom.getParticipants().size(), 0);
        //assertEquals(VALID_UNTIL.getDayOfYear(), createCommunicationRoom.getValidUntil().getDayOfYear());
        String roomId = createCommunicationRoom.getRoomId();

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncOnlyParticipants(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncOnlyParticipants");
        assertNotNull(roomsClient);
        CommunicationRoom createCommunicationRoom = roomsClient.createRoom(null, null, null, participants4);
        assertHappyPath(createCommunicationRoom);
        assertEquals(createCommunicationRoom.getParticipants().size(), 2);
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
            roomsClient.createRoom(null, VALID_FROM.plusDays(181), null, null);
        });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncOnlyValidFromGreaterThan180(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncOnlyValidFromGreaterThan180");
        assertNotNull(roomsClient);
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.createRoom(VALID_FROM.plusDays(181), null, null, null);
        });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncValidFromValidUntilGreaterThan180(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncValidFromValidUntilGreaterThan180");
        assertNotNull(roomsClient);
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.createRoom(VALID_FROM, VALID_FROM.plusDays(181), null, null);
        });
    }


    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncBadParticipantMri(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncBadParticipantMri");
        assertNotNull(roomsClient);
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.createRoom(null, null, null, badParticipant);
        });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateRoomSyncOnlyValidFrom(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateRoomSyncOnlyValidFrom");
        assertNotNull(roomsClient);
        CommunicationRoom createdRoom = roomsClient.createRoom(VALID_FROM, VALID_UNTIL, RoomJoinPolicy.INVITE_ONLY, participants4);
        assertHappyPath(createdRoom);
        assertEquals(createdRoom.getParticipants().size(), 2);

        String roomId = createdRoom.getRoomId();
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.updateRoom(roomId, VALID_FROM.plusMonths(4), null, RoomJoinPolicy.INVITE_ONLY, null);
        });

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateRoomSyncOnlyValidUntil(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateRoomSyncOnlyValidUntil");
        assertNotNull(roomsClient);
        CommunicationRoom createdRoom = roomsClient.createRoom(VALID_FROM, VALID_UNTIL, RoomJoinPolicy.INVITE_ONLY, participants4);
        assertHappyPath(createdRoom);
        assertEquals(createdRoom.getParticipants().size(), 2);

        String roomId = createdRoom.getRoomId();
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.updateRoom(roomId, null, VALID_FROM.plusMonths(4), RoomJoinPolicy.INVITE_ONLY, null);
        });

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateRoomSyncValidUntilGreaterThan180(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateRoomSyncValidUntilGreaterThan180");
        assertNotNull(roomsClient);
        CommunicationRoom createdRoom = roomsClient.createRoom(VALID_FROM, VALID_UNTIL, roomJoinPolicy, participants4);
        assertHappyPath(createdRoom);

        assertEquals(createdRoom.getParticipants().size(), 2);

        String roomId = createdRoom.getRoomId();
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.updateRoom(roomId, VALID_FROM, VALID_FROM.plusDays(181), null, null);
        });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateRoomSyncValidFromGreaterThan180(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateRoomSyncValidFromGreaterThan180");
        assertNotNull(roomsClient);
        CommunicationRoom createdRoom = roomsClient.createRoom(VALID_FROM, VALID_UNTIL, roomJoinPolicy, participants4);
        assertHappyPath(createdRoom);

        assertEquals(createdRoom.getParticipants().size(), 2);

        String roomId = createdRoom.getRoomId();
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.updateRoom(roomId, VALID_FROM.plusDays(181), VALID_UNTIL, null, null);
        });
    }


    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateRoomSyncBadParticipantMri(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateRoomSyncBadParticipantMri");
        assertNotNull(roomsClient);
        CommunicationRoom createdRoom = roomsClient.createRoom(VALID_FROM, VALID_UNTIL, RoomJoinPolicy.INVITE_ONLY, participants4);
        assertHappyPath(createdRoom);
        assertEquals(createdRoom.getParticipants().size(), 2);

        String roomId = createdRoom.getRoomId();
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.addParticipants(roomId,  badParticipant);
        });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateClosedRoomToOpenRoomStarted(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateClosedRoomToOpenRoomStarted");
        assertNotNull(roomsClient);
        CommunicationRoom createdRoom = roomsClient.createRoom(VALID_FROM, VALID_UNTIL, RoomJoinPolicy.INVITE_ONLY, participants4);
        assertHappyPath(createdRoom);

        assertEquals(createdRoom.getParticipants().size(), 2);

        String roomId = createdRoom.getRoomId();

        assertThrows(HttpResponseException.class, () -> {
            roomsClient.updateRoom(roomId, VALID_FROM, VALID_UNTIL, RoomJoinPolicy.COMMUNICATION_SERVICE_USERS, null);
        });

        assertThrows(HttpResponseException.class, () -> {
            roomsClient.updateRoom(roomId, null, VALID_UNTIL, RoomJoinPolicy.COMMUNICATION_SERVICE_USERS, null);
        });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncWithResponseNoAttributes(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncWithResponseNoAttributes");
        assertNotNull(roomsClient);

        Response<CommunicationRoom> createCommunicationRoom = roomsClient.createRoomWithResponse(null, null, null, null, Context.NONE);
        assertHappyPath(createCommunicationRoom, 201);
        assertEquals(createCommunicationRoom.getValue().getParticipants().size(), 0);

        String roomId = createCommunicationRoom.getValue().getRoomId();
        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncWithResponseOnlyOpenRoom(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncWithResponseOnlyOpenRoom");
        assertNotNull(roomsClient);

        Response<CommunicationRoom> createCommunicationRoom = roomsClient.createRoomWithResponse(null, null, RoomJoinPolicy.COMMUNICATION_SERVICE_USERS, null, Context.NONE);
        assertHappyPath(createCommunicationRoom, 201);
        assertEquals(createCommunicationRoom.getValue().getRoomJoinPolicy(), RoomJoinPolicy.COMMUNICATION_SERVICE_USERS);
        assertEquals(createCommunicationRoom.getValue().getParticipants().size(), 0);

        String roomId = createCommunicationRoom.getValue().getRoomId();
        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }


    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncWithResponseOnlyValidFrom(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncWithResponseOnlyValidFrom");
        assertNotNull(roomsClient);
        Response<CommunicationRoom> createCommunicationRoom = roomsClient.createRoomWithResponse(VALID_FROM, null, null, null, Context.NONE);

        assertHappyPath(createCommunicationRoom, 201);
        assertEquals(createCommunicationRoom.getValue().getParticipants().size(), 0);
        //assertEquals(VALID_FROM.getDayOfYear(), createCommunicationRoom.getValue().getValidFrom().getDayOfYear());
        

        String roomId = createCommunicationRoom.getValue().getRoomId();

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncWithReponseOnlyValidUntil(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncWithReponseOnlyValidUntil");
        assertNotNull(roomsClient);
        Response<CommunicationRoom> createCommunicationRoom = roomsClient.createRoomWithResponse(null, VALID_UNTIL, null, null, Context.NONE);

        assertHappyPath(createCommunicationRoom, 201);
        assertEquals(createCommunicationRoom.getValue().getParticipants().size(), 0);
        //assertEquals(VALID_UNTIL.getDayOfYear(), createCommunicationRoom.getValue().getValidUntil().getDayOfYear());

        String roomId = createCommunicationRoom.getValue().getRoomId();

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncWithReponseOnlyParticipants(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncWithReponseOnlyParticipants");
        assertNotNull(roomsClient);

        Response<CommunicationRoom> createCommunicationRoom = roomsClient.createRoomWithResponse(null, null, null, participants6, Context.NONE);
        assertHappyPath(createCommunicationRoom, 201);
        List<RoomParticipant> returnedParticipants = createCommunicationRoom.getValue().getParticipants();
        assertEquals(participants6.size(), returnedParticipants.size());
        IntStream.range(0, returnedParticipants.size())
            .forEach(x -> assertTrue(areParticipantsEqual(participants6.get(x), returnedParticipants.get(x))));
        String roomId = createCommunicationRoom.getValue().getRoomId();

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncWithReponseOnlyValidUntilGreaterThan180(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncWithReponseOnlyValidUntilGreaterThan180");
        assertNotNull(roomsClient);
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.createRoomWithResponse(null, VALID_FROM.plusDays(181), null, null, Context.NONE);
        });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncWithReponseOnlyValidFromGreaterThan180(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncWithReponseOnlyValidFromGreaterThan180");
        assertNotNull(roomsClient);
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.createRoomWithResponse(VALID_FROM.plusDays(181), null, null, null, Context.NONE);
        });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncWithReponseValidFromValidUntilGreaterThan180(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncWithReponseValidFromValidUntilGreaterThan180");
        assertNotNull(roomsClient);
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.createRoomWithResponse(VALID_FROM, VALID_FROM.plusDays(181), null, null, Context.NONE);
        });
    }


    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncWithResponseBadParticipantMri(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncWithResponseBadParticipantMri");
        assertNotNull(roomsClient);
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.createRoomWithResponse(null, null, null, badParticipant, Context.NONE);
        });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateRoomSyncWithReponseOnlyValidFrom(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateRoomSyncWithReponseOnlyValidFrom");
        assertNotNull(roomsClient);
        CommunicationRoom createdRoom = roomsClient.createRoom(VALID_FROM, VALID_UNTIL, RoomJoinPolicy.INVITE_ONLY, participants4);
        assertHappyPath(createdRoom);
        assertEquals(createdRoom.getParticipants().size(), 2);

        String roomId = createdRoom.getRoomId();
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.updateRoomWithResponse(roomId, VALID_FROM.plusMonths(4), null, null, null, Context.NONE);
        });

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateRoomSyncWithReponseOnlyValidUntil(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateRoomSyncWithReponseOnlyValidUntil");
        assertNotNull(roomsClient);
        CommunicationRoom createdRoom = roomsClient.createRoom(VALID_FROM, VALID_UNTIL, RoomJoinPolicy.INVITE_ONLY, participants4);
        assertHappyPath(createdRoom);
        assertEquals(createdRoom.getParticipants().size(), 2);

        String roomId = createdRoom.getRoomId();
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.updateRoomWithResponse(roomId, null, VALID_FROM.plusMonths(4), RoomJoinPolicy.INVITE_ONLY, null, Context.NONE);
        });

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateRoomSyncWithReponseValidUntilGreaterThan180(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateRoomSyncValidUntilWithReponseGreaterThan180");
        assertNotNull(roomsClient);
        CommunicationRoom createdRoom = roomsClient.createRoom(VALID_FROM, VALID_UNTIL, RoomJoinPolicy.INVITE_ONLY, participants4);
        assertHappyPath(createdRoom);

        assertEquals(createdRoom.getParticipants().size(), 2);

        String roomId = createdRoom.getRoomId();
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.updateRoomWithResponse(roomId, VALID_FROM, VALID_FROM.plusDays(181), RoomJoinPolicy.INVITE_ONLY, null, Context.NONE);
        });
        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateRoomSyncWithReponseValidFromGreaterThan180(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateRoomSyncWithReponseValidFromGreaterThan180");
        assertNotNull(roomsClient);
        CommunicationRoom createdRoom = roomsClient.createRoom(VALID_FROM, VALID_UNTIL, RoomJoinPolicy.INVITE_ONLY, participants4);
        assertHappyPath(createdRoom);

        assertEquals(createdRoom.getParticipants().size(), 2);

        String roomId = createdRoom.getRoomId();
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.updateRoomWithResponse(roomId, VALID_FROM.plusDays(181), VALID_UNTIL, RoomJoinPolicy.INVITE_ONLY, null, Context.NONE);
        });
    }


    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateRoomSyncWithReponseBadParticipantMri(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateRoomSyncWithReponseBadParticipantMri");
        assertNotNull(roomsClient);
        CommunicationRoom createdRoom = roomsClient.createRoom(VALID_FROM, VALID_UNTIL, RoomJoinPolicy.INVITE_ONLY, participants4);
        assertHappyPath(createdRoom);

        assertEquals(createdRoom.getParticipants().size(), 2);

        String roomId = createdRoom.getRoomId();
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.addParticipantsWithResponse(roomId,  badParticipant, Context.NONE);
        });
    }

    private RoomsClient setupSyncClient(HttpClient httpClient, String testName) {
        RoomsClientBuilder builder = getRoomsClientWithConnectionString(httpClient, RoomsServiceVersion.V2022_02_01);
        createUsers(httpClient);
        return addLoggingPolicy(builder, testName).buildClient();
    }
}
