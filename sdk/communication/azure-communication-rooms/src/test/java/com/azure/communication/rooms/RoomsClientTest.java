// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms;

import com.azure.communication.rooms.models.CommunicationRoom;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Collections;

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
        CommunicationRoom createCommunicationRoom = roomsClient.createRoom(VALID_FROM, VALID_UNTIL, participants4);
        assertHappyPath(createCommunicationRoom);
        assertEquals(createCommunicationRoom.getParticipants().size(), 2);

        String roomId = createCommunicationRoom.getRoomId();

        CommunicationRoom updateCommunicationRoom = roomsClient.updateRoom(roomId, VALID_FROM, VALID_FROM.plusMonths(4));
        assertEquals(updateCommunicationRoom.getParticipants().size(), 2);
        assertHappyPath(updateCommunicationRoom);

        CommunicationRoom addCommunicationRoom = roomsClient.addParticipants(roomId, participants6);
        assertEquals(addCommunicationRoom.getParticipants().size(), 3);
        assertHappyPath(addCommunicationRoom);

        CommunicationRoom getCommunicationRoom = roomsClient.getRoom(roomId);
        assertEquals(getCommunicationRoom.getParticipants().size(), 3);
        assertHappyPath(getCommunicationRoom);

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncWithFullOperationWithResponse(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncWithFullOperationWithResponse");
        assertNotNull(roomsClient);
        Response<CommunicationRoom> createdRoomResponse = roomsClient.createRoomWithResponse(VALID_FROM, VALID_UNTIL, participants4, Context.NONE);
        assertHappyPath(createdRoomResponse, 201);
        assertEquals(createdRoomResponse.getValue().getParticipants().size(), 2);

        String roomId = createdRoomResponse.getValue().getRoomId();

        Response<CommunicationRoom> updateRoomResponse = roomsClient.updateRoomWithResponse(roomId, VALID_FROM, VALID_FROM.plusMonths(4), Context.NONE);
        assertHappyPath(updateRoomResponse, 200);

        CommunicationRoom addCommunicationRoom = roomsClient.addParticipants(roomId, participants6);
        assertEquals(addCommunicationRoom.getParticipants().size(), 3);
        assertHappyPath(addCommunicationRoom);

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
        CommunicationRoom createCommunicationRoom = roomsClient.createRoom(VALID_FROM, VALID_UNTIL, null);
        assertHappyPath(createCommunicationRoom);
        assertEquals(createCommunicationRoom.getParticipants(), Collections.EMPTY_LIST);

        String roomId = createCommunicationRoom.getRoomId();

        // Add 3 participants.
        CommunicationRoom addedParticipantsRoom = roomsClient.addParticipants(roomId, participants5);
        assertHappyPath(addedParticipantsRoom);
        assertEquals(addedParticipantsRoom.getParticipants().size(), 3);
        assertEquals(addedParticipantsRoom.getParticipants().contains(firstParticipant), true);
        assertEquals(addedParticipantsRoom.getParticipants().contains(secondParticipant), true);
        assertEquals(addedParticipantsRoom.getParticipants().contains(thirdParticipant), true);

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void addParticipantsSyncWithFullOperationWithResponse(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "addParticipantsSyncWithFullOperationWithResponse");
        assertNotNull(roomsClient);

        Response<CommunicationRoom> createdRoomResponse = roomsClient.createRoomWithResponse(VALID_FROM, VALID_UNTIL, null, Context.NONE);
        assertHappyPath(createdRoomResponse, 201);
        assertEquals(createdRoomResponse.getValue().getParticipants(), Collections.EMPTY_LIST);

        String roomId = createdRoomResponse.getValue().getRoomId();

        // Add 3 participants.
        Response<CommunicationRoom> addedParticipantsRoom = roomsClient.addParticipantsWithResponse(roomId, participants5, Context.NONE);
        assertHappyPath(addedParticipantsRoom.getValue());
        assertEquals(addedParticipantsRoom.getValue().getParticipants().size(), 3);
        assertEquals(addedParticipantsRoom.getValue().getParticipants().contains(firstParticipant), true);
        assertEquals(addedParticipantsRoom.getValue().getParticipants().contains(secondParticipant), true);
        assertEquals(addedParticipantsRoom.getValue().getParticipants().contains(thirdParticipant), true);

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateParticipantsSyncWithFullOperation(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateParticipantsSyncWithFullOperation");
        assertNotNull(roomsClient);
        CommunicationRoom createCommunicationRoom = roomsClient.createRoom(VALID_FROM, VALID_UNTIL, participants2);
        assertHappyPath(createCommunicationRoom);
        assertEquals(createCommunicationRoom.getParticipants().size(), 2);
        assertEquals(createCommunicationRoom.getParticipants().contains(firstParticipant), true);
        assertEquals(createCommunicationRoom.getParticipants().contains(secondParticipant), true);

        String roomId = createCommunicationRoom.getRoomId();

        // Update 2 participants.
        CommunicationRoom addedParticipantsRoom = roomsClient.updateParticipants(roomId, participantsWithRoleUpdates);
        assertHappyPath(addedParticipantsRoom);
        assertEquals(addedParticipantsRoom.getParticipants().size(), 2);
        assertEquals(addedParticipantsRoom.getParticipants().contains(firstChangeParticipant), true);
        assertEquals(addedParticipantsRoom.getParticipants().contains(secondChangeParticipant), true);

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateParticipantsSyncWithFullOperationWithResponse(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateParticipantsSyncWithFullOperationWithResponse");
        assertNotNull(roomsClient);

        Response<CommunicationRoom> createdRoomResponse = roomsClient.createRoomWithResponse(VALID_FROM, VALID_UNTIL, participants2, Context.NONE);
        assertHappyPath(createdRoomResponse, 201);
        assertEquals(createdRoomResponse.getValue().getParticipants().size(), 2);
        assertEquals(createdRoomResponse.getValue().getParticipants().contains(firstParticipant), true);
        assertEquals(createdRoomResponse.getValue().getParticipants().contains(secondParticipant), true);

        String roomId = createdRoomResponse.getValue().getRoomId();

        // Add 3 participants.
        Response<CommunicationRoom> addedParticipantsRoom = roomsClient.updateParticipantsWithResponse(roomId, participantsWithRoleUpdates, Context.NONE);
        assertHappyPath(addedParticipantsRoom.getValue());
        assertEquals(addedParticipantsRoom.getValue().getParticipants().size(), 2);
        assertEquals(addedParticipantsRoom.getValue().getParticipants().contains(firstChangeParticipant), true);
        assertEquals(addedParticipantsRoom.getValue().getParticipants().contains(secondChangeParticipant), true);

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }


    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void patchMeetingValidTimeWithResponse(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "patchMeetingValidTimeWithResponse");
        assertNotNull(roomsClient);
        Response<CommunicationRoom> createdRoomResponse = roomsClient.createRoomWithResponse(VALID_FROM, null, participants4, Context.NONE);
        assertHappyPath(createdRoomResponse, 201);
        assertEquals(createdRoomResponse.getValue().getParticipants().size(), 2);
        assertEquals(createdRoomResponse.getValue().getValidFrom().getDayOfYear(), VALID_FROM.getDayOfYear());

        String roomId = createdRoomResponse.getValue().getRoomId();

        Response<CommunicationRoom> updateRoomResponse = roomsClient.updateRoomWithResponse(roomId, VALID_FROM, VALID_UNTIL, Context.NONE);
        assertHappyPath(updateRoomResponse, 200);
        assertEquals(updateRoomResponse.getValue().getParticipants().size(), 2);
        assertEquals(updateRoomResponse.getValue().getValidFrom().getDayOfYear(), VALID_FROM.getDayOfYear());
        assertEquals(updateRoomResponse.getValue().getValidUntil().getDayOfYear(), VALID_UNTIL.getDayOfYear());

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
        CommunicationRoom createCommunicationRoom = roomsClient.createRoom(null, null, null);
        assertHappyPath(createCommunicationRoom);
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
        CommunicationRoom createCommunicationRoom = roomsClient.createRoom(VALID_FROM, null, null);
        assertHappyPath(createCommunicationRoom);
        assertEquals(createCommunicationRoom.getParticipants().size(), 0);
        assertEquals(createCommunicationRoom.getValidFrom().getDayOfYear(), VALID_FROM.getDayOfYear());
        String roomId = createCommunicationRoom.getRoomId();

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncOnlyValidUntil(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncOnlyValidUntil");
        assertNotNull(roomsClient);
        CommunicationRoom createCommunicationRoom = roomsClient.createRoom(null, VALID_UNTIL, null);
        assertHappyPath(createCommunicationRoom);
        assertEquals(createCommunicationRoom.getParticipants().size(), 0);
        assertEquals(createCommunicationRoom.getValidUntil().getDayOfYear(), VALID_UNTIL.getDayOfYear());
        String roomId = createCommunicationRoom.getRoomId();

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncOnlyParticipants(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncOnlyParticipants");
        assertNotNull(roomsClient);
        CommunicationRoom createCommunicationRoom = roomsClient.createRoom(null, null, participants4);
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
            roomsClient.createRoom(null, VALID_FROM.plusDays(181), null);
        });

    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncOnlyValidFromGreaterThan180(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncOnlyValidFromGreaterThan180");
        assertNotNull(roomsClient);
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.createRoom(VALID_FROM.plusDays(181), null, null);
        });

    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncValidFromValidUntilGreaterThan180(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncValidFromValidUntilGreaterThan180");
        assertNotNull(roomsClient);
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.createRoom(VALID_FROM, VALID_FROM.plusDays(181), null);
        });

    }


    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncBadParticipantMri(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncBadParticipantMri");
        assertNotNull(roomsClient);
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.createRoom(null, null, badParticipant);
        });

    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateRoomSyncOnlyValidFrom(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateRoomSyncOnlyValidFrom");
        assertNotNull(roomsClient);
        CommunicationRoom createdRoom = roomsClient.createRoom(VALID_FROM, VALID_UNTIL, participants4);
        assertHappyPath(createdRoom);
        assertEquals(createdRoom.getParticipants().size(), 2);

        String roomId = createdRoom.getRoomId();
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.updateRoom(roomId, VALID_FROM.plusMonths(4), null);
        });

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateRoomSyncOnlyValidUntil(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateRoomSyncOnlyValidUntil");
        assertNotNull(roomsClient);
        CommunicationRoom createdRoom = roomsClient.createRoom(VALID_FROM, VALID_UNTIL, participants4);
        assertHappyPath(createdRoom);
        assertEquals(createdRoom.getParticipants().size(), 2);

        String roomId = createdRoom.getRoomId();
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.updateRoom(roomId, null, VALID_FROM.plusMonths(4));
        });

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateRoomSyncValidUntilGreaterThan180(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateRoomSyncValidUntilGreaterThan180");
        assertNotNull(roomsClient);
        CommunicationRoom createdRoom = roomsClient.createRoom(VALID_FROM, VALID_UNTIL, participants4);
        assertHappyPath(createdRoom);

        assertEquals(createdRoom.getParticipants().size(), 2);

        String roomId = createdRoom.getRoomId();
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.updateRoom(roomId, VALID_FROM, VALID_FROM.plusDays(181));
        });

    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateRoomSyncValidFromGreaterThan180(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateRoomSyncValidFromGreaterThan180");
        assertNotNull(roomsClient);
        CommunicationRoom createdRoom = roomsClient.createRoom(VALID_FROM, VALID_UNTIL, participants4);
        assertHappyPath(createdRoom);

        assertEquals(createdRoom.getParticipants().size(), 2);

        String roomId = createdRoom.getRoomId();
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.updateRoom(roomId, VALID_FROM.plusDays(181), VALID_UNTIL);
        });

    }


    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateRoomSyncBadParticipantMri(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateRoomSyncBadParticipantMri");
        assertNotNull(roomsClient);
        CommunicationRoom createdRoom = roomsClient.createRoom(VALID_FROM, VALID_UNTIL, participants4);
        assertHappyPath(createdRoom);

        assertEquals(createdRoom.getParticipants().size(), 2);

        String roomId = createdRoom.getRoomId();
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.addParticipants(roomId,  badParticipant);
        });

    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncWithResponseNoAttributes(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncWithResponseNoAttributes");
        assertNotNull(roomsClient);

        Response<CommunicationRoom> createCommunicationRoom = roomsClient.createRoomWithResponse(null, null, null, Context.NONE);
        assertHappyPath(createCommunicationRoom, 201);
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
        Response<CommunicationRoom> createCommunicationRoom = roomsClient.createRoomWithResponse(VALID_FROM, null, null, Context.NONE);

        assertHappyPath(createCommunicationRoom, 201);
        assertEquals(createCommunicationRoom.getValue().getParticipants().size(), 0);
        assertEquals(createCommunicationRoom.getValue().getValidFrom().getDayOfYear(), VALID_FROM.getDayOfYear());

        String roomId = createCommunicationRoom.getValue().getRoomId();

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncWithReponseOnlyValidUntil(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncWithReponseOnlyValidUntil");
        assertNotNull(roomsClient);
        Response<CommunicationRoom> createCommunicationRoom = roomsClient.createRoomWithResponse(null, VALID_UNTIL, null, Context.NONE);

        assertHappyPath(createCommunicationRoom, 201);
        assertEquals(createCommunicationRoom.getValue().getParticipants().size(), 0);
        assertEquals(createCommunicationRoom.getValue().getValidUntil().getDayOfYear(), VALID_UNTIL.getDayOfYear());

        String roomId = createCommunicationRoom.getValue().getRoomId();

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncWithReponseOnlyParticipants(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncWithReponseOnlyParticipants");
        assertNotNull(roomsClient);

        Response<CommunicationRoom> createCommunicationRoom = roomsClient.createRoomWithResponse(null, null, participants4, Context.NONE);
        assertHappyPath(createCommunicationRoom, 201);
        assertEquals(createCommunicationRoom.getValue().getParticipants().size(), 2);
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
            roomsClient.createRoomWithResponse(null, VALID_FROM.plusDays(181), null, Context.NONE);
        });

    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncWithReponseOnlyValidFromGreaterThan180(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncWithReponseOnlyValidFromGreaterThan180");
        assertNotNull(roomsClient);
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.createRoomWithResponse(VALID_FROM.plusDays(181), null, null, Context.NONE);
        });

    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncWithReponseValidFromValidUntilGreaterThan180(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncWithReponseValidFromValidUntilGreaterThan180");
        assertNotNull(roomsClient);
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.createRoomWithResponse(VALID_FROM, VALID_FROM.plusDays(181), null, Context.NONE);
        });

    }


    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncWithResponseBadParticipantMri(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncWithResponseBadParticipantMri");
        assertNotNull(roomsClient);
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.createRoomWithResponse(null, null, badParticipant, Context.NONE);
        });

    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateRoomSyncWithReponseOnlyValidFrom(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateRoomSyncWithReponseOnlyValidFrom");
        assertNotNull(roomsClient);
        CommunicationRoom createdRoom = roomsClient.createRoom(VALID_FROM, VALID_UNTIL, participants4);
        assertHappyPath(createdRoom);
        assertEquals(createdRoom.getParticipants().size(), 2);

        String roomId = createdRoom.getRoomId();
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.updateRoomWithResponse(roomId, VALID_FROM.plusMonths(4), null, Context.NONE);
        });

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateRoomSyncWithReponseOnlyValidUntil(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateRoomSyncWithReponseOnlyValidUntil");
        assertNotNull(roomsClient);
        CommunicationRoom createdRoom = roomsClient.createRoom(VALID_FROM, VALID_UNTIL, participants4);
        assertHappyPath(createdRoom);
        assertEquals(createdRoom.getParticipants().size(), 2);

        String roomId = createdRoom.getRoomId();
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.updateRoomWithResponse(roomId, null, VALID_FROM.plusMonths(4), Context.NONE);
        });

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateRoomSyncWithReponseValidUntilGreaterThan180(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateRoomSyncValidUntilWithReponseGreaterThan180");
        assertNotNull(roomsClient);
        CommunicationRoom createdRoom = roomsClient.createRoom(VALID_FROM, VALID_UNTIL, participants4);
        assertHappyPath(createdRoom);

        assertEquals(createdRoom.getParticipants().size(), 2);

        String roomId = createdRoom.getRoomId();
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.updateRoomWithResponse(roomId, VALID_FROM, VALID_FROM.plusDays(181), Context.NONE);
        });
        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateRoomSyncWithReponseValidFromGreaterThan180(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateRoomSyncWithReponseValidFromGreaterThan180");
        assertNotNull(roomsClient);
        CommunicationRoom createdRoom = roomsClient.createRoom(VALID_FROM, VALID_UNTIL, participants4);
        assertHappyPath(createdRoom);

        assertEquals(createdRoom.getParticipants().size(), 2);

        String roomId = createdRoom.getRoomId();
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.updateRoomWithResponse(roomId, VALID_FROM.plusDays(181), VALID_UNTIL, Context.NONE);
        });

    }


    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateRoomSyncWithReponseBadParticipantMri(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateRoomSyncWithReponseBadParticipantMri");
        assertNotNull(roomsClient);
        CommunicationRoom createdRoom = roomsClient.createRoom(VALID_FROM, VALID_UNTIL, participants4);
        assertHappyPath(createdRoom);

        assertEquals(createdRoom.getParticipants().size(), 2);

        String roomId = createdRoom.getRoomId();
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.addParticipantsWithResponse(roomId,  badParticipant, Context.NONE);
        });

    }

    private RoomsClient setupSyncClient(HttpClient httpClient, String testName) {
        RoomsClientBuilder builder = getRoomsClientWithConnectionString(httpClient, RoomsServiceVersion.V2022_02_01_Preview);

        createUsers(httpClient);
        return addLoggingPolicy(builder, testName).buildClient();
    }
}
