// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms;

import com.azure.communication.rooms.models.*;

import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.azure.communication.identity.CommunicationIdentityClient;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class RoomsClientTest extends RoomsTestBase {
    private RoomsClient roomsClient;
    private CommunicationIdentityClient communicationClient;
    private final String nonExistRoomId = "NotExistingRoomID";

    @Override
    protected void beforeTest() {
        super.beforeTest();
    }

    @Override
    protected void afterTest() {
        super.afterTest();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncWithFullOperation(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncWithFullOperation");
        assertNotNull(roomsClient);

        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL);

        CommunicationRoom createCommunicationRoom = roomsClient.createRoom(createRoomOptions);
        assertHappyPath(createCommunicationRoom);

        String roomId = createCommunicationRoom.getRoomId();

        UpdateRoomOptions updateRoomOptions = new UpdateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_FROM.plusMonths(4));

        CommunicationRoom updateCommunicationRoom = roomsClient.updateRoom(roomId, updateRoomOptions);
        assertEquals(true, updateCommunicationRoom.getValidUntil().toEpochSecond()
            > updateCommunicationRoom.getValidFrom().toEpochSecond());
        assertHappyPath(updateCommunicationRoom);

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
                .setValidUntil(VALID_UNTIL);

        CommunicationRoom createCommunicationRoom = roomsClient.createRoom(createRoomOptions);
        assertHappyPath(createCommunicationRoom);

        String roomId = createCommunicationRoom.getRoomId();

        // Test delete room without response
        roomsClient.deleteRoom(roomId);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomSyncWithFullOperationWithResponse(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncWithFullOperationWithResponse");
        assertNotNull(roomsClient);

        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL);

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

        Response<CommunicationRoom> getRoomResponse = roomsClient.getRoomWithResponse(roomId, Context.NONE);
        assertHappyPath(getRoomResponse, 200);

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }


    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listRoomTestFirstRoomIsNotNullThenDeleteRoomWithOutResponse(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "listRoomTestFirstRoomIsNotNullThenDeleteRoomWithOutResponse");
        assertNotNull(roomsClient);

        // Create empty room
        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL);

        CommunicationRoom createCommunicationRoom = roomsClient.createRoom(createRoomOptions);
        assertHappyPath(createCommunicationRoom);

        String roomId = createCommunicationRoom.getRoomId();

        // Check created room coun
        PagedIterable<CommunicationRoom> listRoomResponse = roomsClient.listRooms();

        List<CommunicationRoom> rooms = listRoomResponse.stream().collect(Collectors.toList());

        assertHappyPath(rooms.get(0));

        // Delete Room
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

        // Create 3 participants
        RoomParticipant firstParticipant = new RoomParticipant(communicationClient.createUser());
        RoomParticipant secondParticipant = new RoomParticipant(communicationClient.createUser());
        RoomParticipant thirdParticipant = new RoomParticipant(communicationClient.createUser())
                .setRole(ParticipantRole.CONSUMER);

        List<RoomParticipant> participants = Arrays.asList(firstParticipant, secondParticipant, thirdParticipant);

        // Add 3 participants.
        AddOrUpdateParticipantsResult addPartcipantResponse = roomsClient.addOrUpdateParticipants(roomId, participants);
        assertEquals(true, addPartcipantResponse instanceof AddOrUpdateParticipantsResult);

        // Check participant count, expected 3
        PagedIterable<RoomParticipant> listParticipantsResponse2 = roomsClient.listParticipants(roomId);
        assertEquals(3, listParticipantsResponse2.stream().count());

        // Participants to update
        RoomParticipant firstParticipantUpdated = new RoomParticipant(firstParticipant.getCommunicationIdentifier())
                .setRole(ParticipantRole.CONSUMER);
        RoomParticipant secondParticipantUpdated = new RoomParticipant(secondParticipant.getCommunicationIdentifier())
                .setRole(ParticipantRole.CONSUMER);

        List<RoomParticipant> participantsToUpdate = Arrays.asList(firstParticipantUpdated, secondParticipantUpdated);

        // Update 2 participants roles, ATTENDEE -> CONSUMER
        AddOrUpdateParticipantsResult updateParticipantResponse = roomsClient.addOrUpdateParticipants(roomId,
                participantsToUpdate);
        assertEquals(true, updateParticipantResponse instanceof AddOrUpdateParticipantsResult);

        // Check paticipants new roles, everyone should be CONSUMER
        PagedIterable<RoomParticipant> listParticipantsResponse3 = roomsClient.listParticipants(roomId);

        for (RoomParticipant participant : listParticipantsResponse3) {
            assertEquals(ParticipantRole.CONSUMER, participant.getRole());
        }

        // Participants to remove
        List<CommunicationIdentifier> participantsIdentifiersForParticipants = Arrays.asList(
                firstParticipant.getCommunicationIdentifier(),
                secondParticipant.getCommunicationIdentifier());

        // Remove 2 participants
        RemoveParticipantsResult removeParticipantResponse = roomsClient.removeParticipants(roomId,
                participantsIdentifiersForParticipants);

        // Check participant count, expected 1
        PagedIterable<RoomParticipant> listParticipantsResponse4 = roomsClient.listParticipants(roomId);
        assertEquals(1, listParticipantsResponse4.stream().count());

        // Delete Room
        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateParticipantsToDefaultRoleSyncWithFullOperationWithResponse(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateParticipantsToDefaultRoleSyncWithFullOperationWithResponse");
        assertNotNull(roomsClient);

        RoomParticipant firstParticipant = new RoomParticipant(communicationClient.createUser())
                .setRole(ParticipantRole.PRESENTER);

        List<RoomParticipant> participants = Arrays.asList(firstParticipant);

        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL)
                .setParticipants(participants);

        Response<CommunicationRoom> createCommunicationRoom = roomsClient.createRoomWithResponse(createRoomOptions,
                Context.NONE);
        assertHappyPath(createCommunicationRoom, 201);

        String roomId = createCommunicationRoom.getValue().getRoomId();

        RoomParticipant firstParticipantToUpdate = new RoomParticipant(firstParticipant.getCommunicationIdentifier())
                .setRole(null);

        List<RoomParticipant> participantsToUpdate = Arrays.asList(firstParticipantToUpdate);

        // Update participant to default role..
        Response<AddOrUpdateParticipantsResult> addPartcipantResponse = roomsClient
                .addOrUpdateParticipantsWithResponse(roomId, participantsToUpdate, Context.NONE);
        assertEquals(200, addPartcipantResponse.getStatusCode());

        PagedIterable<RoomParticipant> listResponse = roomsClient.listParticipants(roomId);

        for (RoomParticipant participant : listResponse) {
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

        RoomParticipant firstParticipant = new RoomParticipant(communicationClient.createUser())
                .setRole(ParticipantRole.CONSUMER);
        RoomParticipant secondParticipant = new RoomParticipant(communicationClient.createUser())
                .setRole(ParticipantRole.ATTENDEE);

        List<RoomParticipant> participants = Arrays.asList(firstParticipant, secondParticipant);

        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL)
                .setParticipants(participants);

        CommunicationRoom createCommunicationRoom = roomsClient.createRoom(createRoomOptions);
        assertHappyPath(createCommunicationRoom);

        String roomId = createCommunicationRoom.getRoomId();

        RoomParticipant firstParticipantToUpdate = new RoomParticipant(firstParticipant.getCommunicationIdentifier())
                .setRole(ParticipantRole.PRESENTER);
        RoomParticipant secondParticipantToUpdate = new RoomParticipant(secondParticipant.getCommunicationIdentifier())
                .setRole(ParticipantRole.PRESENTER);

        List<RoomParticipant> participantsToUpdate = Arrays.asList(firstParticipantToUpdate, secondParticipantToUpdate);

        // Update 2 participants.
        AddOrUpdateParticipantsResult addPartcipantResponse = roomsClient.addOrUpdateParticipants(roomId,
                participantsToUpdate);

        PagedIterable<RoomParticipant> listResponse = roomsClient.listParticipants(roomId);

        for (RoomParticipant participant : listResponse) {
            assertEquals(ParticipantRole.PRESENTER, participant.getRole());
        }

        Response<Void> deleteResponse = roomsClient.deleteRoomWithResponse(roomId, Context.NONE);
        assertEquals(deleteResponse.getStatusCode(), 204);
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void patchMeetingValidTimeWithResponse(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "patchMeetingValidTimeWithResponse");
        assertNotNull(roomsClient);

        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM);

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
            roomsClient.getRoom(nonExistRoomId);
        });
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void deleteRoomWithUnexistingRoomIdReturnBadRequest(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "deleteRoomWithUnexistingRoomIdReturnBadRequest");
        assertNotNull(roomsClient);
        assertThrows(HttpResponseException.class, () -> {
            roomsClient.deleteRoomWithResponse(nonExistRoomId, Context.NONE);
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

        // Create list of room participant with bad mri
        List<RoomParticipant> badParticipant = Arrays
                .asList(new RoomParticipant(new CommunicationUserIdentifier("badMRI")));

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
                .setValidUntil(VALID_UNTIL);

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
                .setValidUntil(VALID_UNTIL);

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
                .setValidUntil(VALID_UNTIL);

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
                .setValidUntil(VALID_UNTIL);

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
    public void createRoomSyncWithResponseOnlyValidUntil(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncWithResponseOnlyValidUntil");
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
    public void createRoomSyncWithResponseOnlyValidUntilGreaterThan180(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncWithResponseOnlyValidUntilGreaterThan180");
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
    public void createRoomSyncWithResponseOnlyValidFromGreaterThan180(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncWithResponseOnlyValidFromGreaterThan180");
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
    public void createRoomSyncWithResponseValidFromValidUntilGreaterThan180(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "createRoomSyncWithResponseValidFromValidUntilGreaterThan180");
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
    public void updateRoomSyncWithResponseOnlyValidFrom(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateRoomSyncWithResponseOnlyValidFrom");
        assertNotNull(roomsClient);

        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL);

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
    public void updateRoomSyncWithResponseOnlyValidUntil(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateRoomSyncWithResponseOnlyValidUntil");
        assertNotNull(roomsClient);

        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL);

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
    public void updateRoomSyncWithResponseValidUntilGreaterThan180(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateRoomSyncValidUntilWithResponseGreaterThan180");
        assertNotNull(roomsClient);

        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL);

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
    public void updateRoomSyncWithResponseValidFromGreaterThan180(HttpClient httpClient) {
        roomsClient = setupSyncClient(httpClient, "updateRoomSyncWithResponseValidFromGreaterThan180");
        assertNotNull(roomsClient);

        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL);

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

    private RoomsClient setupSyncClient(HttpClient httpClient, String testName) {
        RoomsClientBuilder builder = getRoomsClientWithConnectionString(httpClient,
                RoomsServiceVersion.V2023_06_14);

        communicationClient = getCommunicationIdentityClientBuilder(httpClient).buildClient();

        return addLoggingPolicy(builder, testName).buildClient();
    }
}
