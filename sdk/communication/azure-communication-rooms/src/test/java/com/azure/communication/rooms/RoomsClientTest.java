// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms;

import com.azure.communication.rooms.models.CommunicationRoom;
import com.azure.communication.rooms.models.RoomRequest;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
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

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncWithFullOperation(HttpClient httpClient) {
        RoomsClientBuilder builder = getRoomsClientWithConnectionString(httpClient);
        roomsClient = setupSyncClient(builder, "createRoomSyncWithFullOperation");
        assertNotNull(roomsClient);
        RoomRequest createRoomRequest = new RoomRequest();
        createRoomRequest.setValidFrom(VALID_FROM);
        createRoomRequest.setValidUntil(VALID_UNTIL);
        createRoomRequest.setParticipants(PARTICIPANTS4);
        CommunicationRoom createCommunicationRoom = roomsClient.createRoom(createRoomRequest);
        assertHappyPath(createCommunicationRoom);
        assertEquals(createCommunicationRoom.getParticipants().size(), 2);

        String roomId = createCommunicationRoom.getRoomId();

        RoomRequest updateRoomRequest = new RoomRequest();
        updateRoomRequest.setValidFrom(VALID_FROM);
        updateRoomRequest.setValidUntil(VALID_UNTIL);
        updateRoomRequest.setParticipants(PARTICIPANTS3);
        CommunicationRoom updateCommunicationRoom = roomsClient.updateRoom(roomId, updateRoomRequest);
        assertHappyPath(updateCommunicationRoom);
        assertEquals(updateCommunicationRoom.getParticipants().size(), 1);

        CommunicationRoom getCommunicationRoom = roomsClient.getRoom(roomId);
        assertEquals(getCommunicationRoom.getParticipants().size(), 1);
        assertHappyPath(getCommunicationRoom);

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncWithFullOperationWithResponse(HttpClient httpClient) {
        RoomsClientBuilder builder = getRoomsClientWithConnectionString(httpClient);
        roomsClient = setupSyncClient(builder, "createRoomSyncWithFullOperationWithResponse");
        assertNotNull(roomsClient);
        RoomRequest createRoomRequest = new RoomRequest();
        createRoomRequest.setValidFrom(VALID_FROM);
        createRoomRequest.setValidUntil(VALID_UNTIL);
        createRoomRequest.setParticipants(PARTICIPANTS4);
        Response<CommunicationRoom> createdRoomResponse = roomsClient.createRoomWithResponse(createRoomRequest, Context.NONE);
        assertHappyPath(createdRoomResponse, 201);
        assertEquals(createdRoomResponse.getValue().getParticipants().size(), 2);

        String roomId = createdRoomResponse.getValue().getRoomId();

        RoomRequest updateRoomRequest = new RoomRequest();
        updateRoomRequest.setValidFrom(VALID_FROM);
        updateRoomRequest.setValidUntil(VALID_UNTIL);
        updateRoomRequest.setParticipants(PARTICIPANTS3);
        Response<CommunicationRoom> updateRoomResponse = roomsClient.updateRoomWithResponse(roomId, updateRoomRequest, Context.NONE);
        assertHappyPath(updateRoomResponse, 200);
        assertEquals(updateRoomResponse.getValue().getParticipants().size(), 1);
        
        Response<CommunicationRoom> getRoomResponse = roomsClient.getRoomWithResponse(roomId, Context.NONE);
        assertHappyPath(getRoomResponse, 200);
        assertEquals(getRoomResponse.getValue().getParticipants().size(), 1);

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void addRemoveParticipantsSyncWithFullOperation(HttpClient httpClient) {
        RoomsClientBuilder builder = getRoomsClientWithConnectionString(httpClient);
        roomsClient = setupSyncClient(builder, "AddRemoveParticipantsSyncWithFullOperation");
        assertNotNull(roomsClient);
        RoomRequest createRoomRequest = new RoomRequest();
        createRoomRequest.setValidFrom(VALID_FROM);
        createRoomRequest.setValidUntil(VALID_UNTIL);
        CommunicationRoom createCommunicationRoom = roomsClient.createRoom(createRoomRequest);
        assertHappyPath(createCommunicationRoom);
        assertEquals(createCommunicationRoom.getParticipants(), null);

        String roomId = createCommunicationRoom.getRoomId();

        // Add 3 participants.
        CommunicationRoom addedParticipantsRoom = roomsClient.addParticipants(roomId, PARTICIPANTS5);
        assertHappyPath(addedParticipantsRoom);
        assertEquals(addedParticipantsRoom.getParticipants().size(), 3);
        assertEquals(addedParticipantsRoom.getParticipants().containsKey(USER1), true);
        assertEquals(addedParticipantsRoom.getParticipants().containsKey(USER2), true);
        assertEquals(addedParticipantsRoom.getParticipants().containsKey(USER3), true);

        // Remove 2 participants.
        CommunicationRoom removedParticipantsRoom = roomsClient.removeParticipants(roomId, PARTICIPANTS6);
        assertHappyPath(removedParticipantsRoom);
        assertEquals(removedParticipantsRoom.getParticipants().size(), 1);
        assertEquals(removedParticipantsRoom.getParticipants().containsKey(USER1), true);
        assertEquals(removedParticipantsRoom.getParticipants().containsKey(USER2), false);
        assertEquals(removedParticipantsRoom.getParticipants().containsKey(USER3), false);

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void addRemoveParticipantsSyncWithFullOperationWithResponse(HttpClient httpClient) {
        RoomsClientBuilder builder = getRoomsClientWithConnectionString(httpClient);
        roomsClient = setupSyncClient(builder, "AddRemoveParticipantsSyncWithFullOperationWithResponse");
        assertNotNull(roomsClient);
        RoomRequest createRoomRequest = new RoomRequest();
        createRoomRequest.setValidFrom(VALID_FROM);
        createRoomRequest.setValidUntil(VALID_UNTIL);
        Response<CommunicationRoom> createdRoomResponse = roomsClient.createRoomWithResponse(createRoomRequest, Context.NONE);
        assertHappyPath(createdRoomResponse, 201);
        assertEquals(createdRoomResponse.getValue().getParticipants(), null);

        String roomId = createdRoomResponse.getValue().getRoomId();

        // Add 3 participants.
        Response<CommunicationRoom> addedParticipantsRoom = roomsClient.addParticipantsWithResponse(roomId, PARTICIPANTS5, Context.NONE);
        assertHappyPath(addedParticipantsRoom.getValue());
        assertEquals(addedParticipantsRoom.getValue().getParticipants().size(), 3);
        assertEquals(addedParticipantsRoom.getValue().getParticipants().containsKey(USER1), true);
        assertEquals(addedParticipantsRoom.getValue().getParticipants().containsKey(USER2), true);
        assertEquals(addedParticipantsRoom.getValue().getParticipants().containsKey(USER3), true);

        // Remove 2 participants.
        Response<CommunicationRoom> removedParticipantsRoom = roomsClient.removeParticipantsWithResponse(roomId, PARTICIPANTS6, Context.NONE);
        assertHappyPath(removedParticipantsRoom.getValue());
        assertEquals(removedParticipantsRoom.getValue().getParticipants().size(), 1);
        assertEquals(removedParticipantsRoom.getValue().getParticipants().containsKey(USER1), true);
        assertEquals(removedParticipantsRoom.getValue().getParticipants().containsKey(USER2), false);
        assertEquals(removedParticipantsRoom.getValue().getParticipants().containsKey(USER3), false);

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getRoomWithUnexistingRoomIdReturnBadRequest(HttpClient httpClient) {
        RoomsClientBuilder builder = getRoomsClientWithConnectionString(httpClient);
        roomsClient = setupSyncClient(builder, "getRoomWithUnexistingRoomIdReturnBadRequest");
        assertNotNull(roomsClient);
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.getRoom(NONEXIST_ROOM_ID);
        });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void deleteRoomWithUnexistingRoomIdReturnBadRequest(HttpClient httpClient) {
        RoomsClientBuilder builder = getRoomsClientWithConnectionString(httpClient);
        roomsClient = setupSyncClient(builder, "deleteRoomWithUnexistingRoomIdReturnBadRequest");
        assertNotNull(roomsClient);
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.deleteRoomWithResponse(NONEXIST_ROOM_ID, Context.NONE);
        });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void patchMeetingValidTimeWithResponse(HttpClient httpClient) {
        RoomsClientBuilder builder = getRoomsClientWithConnectionString(httpClient);
        roomsClient = setupSyncClient(builder, "patchMeetingValidTimeWithResponse");
        assertNotNull(roomsClient);
        RoomRequest createRoomRequest = new RoomRequest();
        createRoomRequest.setValidFrom(VALID_FROM);
        createRoomRequest.setParticipants(PARTICIPANTS4);
        Response<CommunicationRoom> createdRoomResponse = roomsClient.createRoomWithResponse(createRoomRequest, Context.NONE);
        assertHappyPath(createdRoomResponse, 201);
        assertEquals(createdRoomResponse.getValue().getParticipants().size(), 2);
        assertEquals(createdRoomResponse.getValue().getValidFrom().getDayOfYear(), createRoomRequest.getValidFrom().getDayOfYear());

        String roomId = createdRoomResponse.getValue().getRoomId();

        RoomRequest updateRoomRequest = new RoomRequest();
        updateRoomRequest.setValidFrom(VALID_FROM);
        updateRoomRequest.setValidUntil(VALID_UNTIL);
        updateRoomRequest.setParticipants(PARTICIPANTS3);
        Response<CommunicationRoom> updateRoomResponse = roomsClient.updateRoomWithResponse(roomId, updateRoomRequest, Context.NONE);
        assertHappyPath(updateRoomResponse, 200);
        assertEquals(updateRoomResponse.getValue().getParticipants().size(), 1);
        assertEquals(updateRoomResponse.getValue().getValidFrom().getDayOfYear(), updateRoomRequest.getValidFrom().getDayOfYear());
        assertEquals(updateRoomResponse.getValue().getValidUntil().getDayOfYear(), updateRoomRequest.getValidUntil().getDayOfYear());

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    private RoomsClient setupSyncClient(RoomsClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildClient();
    }
}
