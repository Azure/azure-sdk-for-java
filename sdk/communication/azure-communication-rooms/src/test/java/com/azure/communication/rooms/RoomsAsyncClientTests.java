// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.azure.communication.identity.CommunicationIdentityClient;
import com.azure.communication.rooms.models.*;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.PagedFlux;
import java.util.Arrays;
import java.util.List;

import com.azure.communication.common.CommunicationIdentifier;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class RoomsAsyncClientTests extends RoomsTestBase {
    private RoomsAsyncClient roomsAsyncClient;
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
    public void createRoomFullCycleWithResponseStep(HttpClient httpClient) {
        roomsAsyncClient = setupAsyncClient(httpClient,
                "createRoomFullCycleWithResponse");
        assertNotNull(roomsAsyncClient);

        RoomParticipant firstParticipant = new RoomParticipant(communicationClient.createUser());
        List<RoomParticipant> participants = Arrays.asList(firstParticipant);

        CreateRoomOptions roomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL)
                .setParticipants(participants);

        Mono<Response<CommunicationRoom>> response1 = roomsAsyncClient.createRoomWithResponse(roomOptions);

        StepVerifier.create(response1)
                .assertNext(roomResult -> {
                    assertHappyPath(roomResult, 201);
                })
                .verifyComplete();

        String roomId = response1.block().getValue().getRoomId();

        UpdateRoomOptions updateOptions = new UpdateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_FROM.plusMonths(3));

        Mono<Response<CommunicationRoom>> response3 = roomsAsyncClient.updateRoomWithResponse(roomId, updateOptions);

        System.out.println(VALID_FROM.plusMonths(3).getDayOfYear());

        StepVerifier.create(response3)
                .assertNext(roomResult -> {
                    assertHappyPath(roomResult, 200);
                }).verifyComplete();

        Mono<Response<CommunicationRoom>> response4 = roomsAsyncClient.getRoomWithResponse(roomId);

        StepVerifier.create(response4)
                .assertNext(result4 -> {
                    assertHappyPath(result4, 200);
                }).verifyComplete();

        Mono<Response<Void>> response5 = roomsAsyncClient.deleteRoomWithResponse(roomId);
        StepVerifier.create(response5)
                .assertNext(result5 -> {
                    assertEquals(result5.getStatusCode(), 204);
                }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomFullCycleWithOutResponseStep(HttpClient httpClient) {
        roomsAsyncClient = setupAsyncClient(httpClient,
                "createRoomFullCycleWithOutResponse");
        assertNotNull(roomsAsyncClient);

        RoomParticipant firstParticipant = new RoomParticipant(communicationClient.createUser());
        List<RoomParticipant> participants = Arrays.asList(firstParticipant);

        CreateRoomOptions roomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL)
                .setParticipants(participants);

        Mono<CommunicationRoom> response1 = roomsAsyncClient.createRoom(roomOptions);

        StepVerifier.create(response1)
                .assertNext(roomResult -> {
                    assertEquals(true, roomResult.getRoomId() != null);
                    assertEquals(true, roomResult.getCreatedAt() != null);
                    assertEquals(true, roomResult.getValidFrom() != null);
                    assertEquals(true, roomResult.getValidUntil() != null);
                }).verifyComplete();

        String roomId = response1.block().getRoomId();

        UpdateRoomOptions updateOptions = new UpdateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_FROM.plusMonths(3));

        Mono<CommunicationRoom> response3 = roomsAsyncClient.updateRoom(roomId,
                updateOptions);

        StepVerifier.create(response3)
                .assertNext(result3 -> {
                    assertEquals(true, result3.getValidUntil().toEpochSecond() > result3.getValidFrom().toEpochSecond());
                }).verifyComplete();

        Mono<CommunicationRoom> response4 = roomsAsyncClient.getRoom(roomId);

        StepVerifier.create(response4)
                .assertNext(result4 -> {
                    assertEquals(result4.getRoomId(), roomId);
                }).verifyComplete();

        Mono<Response<Void>> response5 = roomsAsyncClient.deleteRoomWithResponse(roomId);
        StepVerifier.create(response5)
                .assertNext(result5 -> {
                    assertEquals(result5.getStatusCode(), 204);
                }).verifyComplete();

    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void addUpdateAndRemoveParticipantsOperationsWithFullFlow(HttpClient httpClient) {
        roomsAsyncClient = setupAsyncClient(httpClient, "addUpdateAndRemoveParticipantsOperationsWithFullFlow");
        assertNotNull(roomsAsyncClient);

        // Create empty room
        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL);

        Mono<CommunicationRoom> createCommunicationRoom = roomsAsyncClient.createRoom(createRoomOptions);

        StepVerifier.create(createCommunicationRoom)
                .assertNext(roomResult -> {
                    assertEquals(true, roomResult.getRoomId() != null);
                    assertEquals(true, roomResult.getCreatedAt() != null);
                    assertEquals(true, roomResult.getValidFrom() != null);
                    assertEquals(true, roomResult.getValidUntil() != null);
                }).verifyComplete();

        String roomId = createCommunicationRoom.block().getRoomId();

        // Check participant count, expected 0
        PagedFlux<RoomParticipant> listParticipantsResponse1 = roomsAsyncClient.listParticipants(roomId);

        StepVerifier.create(listParticipantsResponse1.count())
                .expectNext(0L)
                .verifyComplete();

        // Add 3 participants

        RoomParticipant firstParticipant = new RoomParticipant(communicationClient.createUser());
        RoomParticipant secondParticipant = new RoomParticipant(communicationClient.createUser());
        RoomParticipant thirdParticipant = new RoomParticipant(communicationClient.createUser())
                .setRole(ParticipantRole.CONSUMER);

        List<RoomParticipant> participants = Arrays.asList(firstParticipant, secondParticipant, thirdParticipant);

        // Add 3 participants.
        AddOrUpdateParticipantsResult addParticipantResponse = roomsAsyncClient.addOrUpdateParticipants(roomId, participants).block();

        // Check participant count, expected 3
        PagedFlux<RoomParticipant> listParticipantsResponse2 = roomsAsyncClient.listParticipants(roomId);

        StepVerifier.create(listParticipantsResponse2.count())
                .expectNext(3L)
                .verifyComplete();

        // Check for default role
        StepVerifier.create(listParticipantsResponse2)
                .expectSubscription()
                .thenConsumeWhile(participant -> true, participant -> {
                    if (participant.getCommunicationIdentifier().getRawId() == secondParticipant
                            .getCommunicationIdentifier().getRawId()) {
                        assertEquals(ParticipantRole.ATTENDEE, participant.getRole());
                    }
                })
                .expectComplete()
                .verify();

        // Participants to update
        RoomParticipant firstParticipantUpdated = new RoomParticipant(firstParticipant.getCommunicationIdentifier())
                .setRole(ParticipantRole.CONSUMER);
        RoomParticipant secondParticipantUpdated = new RoomParticipant(secondParticipant.getCommunicationIdentifier())
                .setRole(ParticipantRole.CONSUMER);

        List<RoomParticipant> participantsToUpdate = Arrays.asList(firstParticipantUpdated, secondParticipantUpdated);

        // Update 2 participants roles, ATTENDEE -> CONSUMER
        Mono<AddOrUpdateParticipantsResult> updateParticipantResponse = roomsAsyncClient.addOrUpdateParticipants(roomId,
                participantsToUpdate);

        StepVerifier.create(updateParticipantResponse)
                .assertNext(result -> {
                    assertEquals(true, result instanceof AddOrUpdateParticipantsResult);
                })
                .verifyComplete();

        // Check paticipants new roles, everyone should be CONSUMER
        PagedFlux<RoomParticipant> listParticipantsResponse3 = roomsAsyncClient.listParticipants(roomId);

        StepVerifier.create(listParticipantsResponse3)
                .expectSubscription()
                .thenConsumeWhile(participant -> true, participant -> {
                    assertEquals(ParticipantRole.CONSUMER, participant.getRole());
                })
                .expectComplete()
                .verify();

        // Participants to remove
        List<CommunicationIdentifier> participantsIdentifiersForParticipants = Arrays.asList(
                firstParticipant.getCommunicationIdentifier(),
                secondParticipant.getCommunicationIdentifier());

        // Remove 2 participants
        Mono<RemoveParticipantsResult> removeParticipantResponse = roomsAsyncClient.removeParticipants(roomId,
                participantsIdentifiersForParticipants);

        StepVerifier.create(removeParticipantResponse)
                .assertNext(result -> {
                    assertEquals(true, result instanceof RemoveParticipantsResult);
                })
                .verifyComplete();

        // Check participant count, expected 1
        PagedFlux<RoomParticipant> listParticipantsResponse4 = roomsAsyncClient.listParticipants(roomId);

        StepVerifier.create(listParticipantsResponse4.count())
                .expectNext(1L)
                .verifyComplete();

        // // Delete Room
        // Mono<Response<Void>> response5 = roomsAsyncClient.deleteRoomWithResponse(roomId);
        // StepVerifier.create(response5)
        //         .assertNext(result5 -> {
        //             assertEquals(result5.getStatusCode(), 204);
        //         }).verifyComplete();
    }


    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void addParticipantsOperationWithOutResponse(HttpClient httpClient) {
        roomsAsyncClient = setupAsyncClient(httpClient, "addParticipantsOperationWithOutResponse");
        assertNotNull(roomsAsyncClient);

        // Create empty room
        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL);

        Mono<CommunicationRoom> createCommunicationRoom = roomsAsyncClient.createRoom(createRoomOptions);

        StepVerifier.create(createCommunicationRoom)
                .assertNext(roomResult -> {
                    assertEquals(true, roomResult.getRoomId() != null);
                    assertEquals(true, roomResult.getCreatedAt() != null);
                    assertEquals(true, roomResult.getValidFrom() != null);
                    assertEquals(true, roomResult.getValidUntil() != null);
                }).verifyComplete();

        String roomId = createCommunicationRoom.block().getRoomId();

        // Check participant count, expected 0
        PagedFlux<RoomParticipant> listParticipantsResponse1 = roomsAsyncClient.listParticipants(roomId);

        StepVerifier.create(listParticipantsResponse1.count())
                .expectNext(0L)
                .verifyComplete();

        // Add 3 participants
        RoomParticipant firstParticipant = new RoomParticipant(communicationClient.createUser());
        RoomParticipant secondParticipant = new RoomParticipant(communicationClient.createUser()).setRole(null);
        RoomParticipant thirdParticipant = new RoomParticipant(communicationClient.createUser())
                .setRole(ParticipantRole.CONSUMER);

        List<RoomParticipant> participants = Arrays.asList(firstParticipant, secondParticipant, thirdParticipant);

        // Add 3 participants.
        AddOrUpdateParticipantsResult addParticipantResponse = roomsAsyncClient.addOrUpdateParticipants(roomId, participants).block();

        // Check participant count, expected 3
        PagedFlux<RoomParticipant> listParticipantsResponse2 = roomsAsyncClient.listParticipants(roomId);

        StepVerifier.create(listParticipantsResponse2.count())
                .expectNext(3L)
                .verifyComplete();

        // Delete Room
        Mono<Response<Void>> response5 = roomsAsyncClient.deleteRoomWithResponse(roomId);
        StepVerifier.create(response5)
                .assertNext(result5 -> {
                    assertEquals(result5.getStatusCode(), 204);
                }).verifyComplete();
    }


    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listRoomTestFirstRoomIsNotNullThenDeleteRoomWithOutResponse(HttpClient httpClient) {
        roomsAsyncClient = setupAsyncClient(httpClient,
                "listRoomTestFirstRoomIsValidSuccess");
        assertNotNull(roomsAsyncClient);

        // Create empty room
        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL);

        Mono<CommunicationRoom> createCommunicationRoom = roomsAsyncClient.createRoom(createRoomOptions);

        StepVerifier.create(createCommunicationRoom)
                .assertNext(roomResult -> {
                    assertEquals(true, roomResult.getRoomId() != null);
                    assertEquals(true, roomResult.getCreatedAt() != null);
                    assertEquals(true, roomResult.getValidFrom() != null);
                    assertEquals(true, roomResult.getValidUntil() != null);
                }).verifyComplete();

        String roomId = createCommunicationRoom.block().getRoomId();

        //Get created rooms
        PagedFlux<CommunicationRoom> listRoomResponse = roomsAsyncClient.listRooms();

        StepVerifier.create(listRoomResponse.take(1))
                .assertNext(room -> {
                    assertEquals(true, room.getRoomId() != null);
                    assertEquals(true, room.getCreatedAt() != null);
                    assertEquals(true, room.getValidFrom() != null);
                    assertEquals(true, room.getValidUntil() != null);
                })
                .expectComplete()
                .verify();

        // Delete Room
        Mono<Response<Void>> deleteResponse = roomsAsyncClient.deleteRoomWithResponse(roomId);
        StepVerifier.create(deleteResponse)
                .assertNext(result -> {
                    assertEquals(result.getStatusCode(), 204);
                }).verifyComplete();

    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void deleteParticipantsWithOutResponseStep(HttpClient httpClient) {
        roomsAsyncClient = setupAsyncClient(httpClient,
                "deleteParticipantsWithOutResponseStep");
        assertNotNull(roomsAsyncClient);

        RoomParticipant firstParticipant = new RoomParticipant(communicationClient.createUser());

        List<RoomParticipant> participants = Arrays.asList(firstParticipant);

        CreateRoomOptions roomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL)
                .setParticipants(participants);

        Mono<CommunicationRoom> response1 = roomsAsyncClient.createRoom(roomOptions);

        String roomId = response1.block().getRoomId();

        Mono<RemoveParticipantsResult> response4 = roomsAsyncClient.removeParticipants(roomId,
                Arrays.asList(firstParticipant.getCommunicationIdentifier()));

        Mono<Response<Void>> response5 = roomsAsyncClient.deleteRoomWithResponse(roomId);
        StepVerifier.create(response5)
                .assertNext(result5 -> {
                    assertEquals(result5.getStatusCode(), 204);
                }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateParticipantsToDefaultRoleWithResponseStep(HttpClient httpClient) {
        roomsAsyncClient = setupAsyncClient(httpClient,
                "updateParticipantsToDefaultRoleWithResponseStep");
        assertNotNull(roomsAsyncClient);

        RoomParticipant firstParticipant = new RoomParticipant(communicationClient.createUser())
                .setRole(ParticipantRole.PRESENTER);

        List<RoomParticipant> participants = Arrays.asList(firstParticipant);

        CreateRoomOptions roomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL)
                .setParticipants(participants);

        Mono<Response<CommunicationRoom>> response1 = roomsAsyncClient.createRoomWithResponse(roomOptions);

        StepVerifier.create(response1)
                .assertNext(roomResult -> {
                    assertHappyPath(roomResult, 201);
                })
                .verifyComplete();

        String roomId = response1.block().getValue().getRoomId();

        List<RoomParticipant> participantToUpdate = Arrays
                .asList(new RoomParticipant(firstParticipant.getCommunicationIdentifier()));

        Mono<Response<AddOrUpdateParticipantsResult>> response2 = roomsAsyncClient
                .addOrUpdateParticipantsWithResponse(roomId, participantToUpdate);

        StepVerifier.create(response2)
                .assertNext(result2 -> {
                    assertEquals(result2.getStatusCode(), 200);
                }).verifyComplete();

        PagedFlux<RoomParticipant> response3 = roomsAsyncClient.listParticipants(roomId);

        StepVerifier.create(response3).assertNext(response4 -> {
            assertEquals(ParticipantRole.ATTENDEE, response4.getRole());
        })
                .verifyComplete();

    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listParticipantsWithOutResponseStep(HttpClient httpClient) {
        roomsAsyncClient = setupAsyncClient(httpClient,
                "listParticipantsWithOutResponseStep");
        assertNotNull(roomsAsyncClient);

        RoomParticipant firstParticipant = new RoomParticipant(communicationClient.createUser())
                .setRole(ParticipantRole.PRESENTER);
        RoomParticipant secondParticipant = new RoomParticipant(communicationClient.createUser())
                .setRole(ParticipantRole.PRESENTER);
        RoomParticipant thirdParticipant = new RoomParticipant(communicationClient.createUser())
                .setRole(ParticipantRole.CONSUMER);

        List<RoomParticipant> participants = Arrays.asList(firstParticipant, secondParticipant, thirdParticipant);

        CreateRoomOptions roomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL)
                .setParticipants(participants);

        Mono<CommunicationRoom> response1 = roomsAsyncClient.createRoom(roomOptions);

        String roomId = response1.block().getRoomId();

        PagedFlux<RoomParticipant> response2 = roomsAsyncClient.listParticipants(roomId);

        StepVerifier.create(response2.count())
                .expectNext(3L)
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getRoomWithUnexistingRoomId(HttpClient httpClient) {
        roomsAsyncClient = setupAsyncClient(httpClient,
                "getRoomWithUnexistingRoomId");
        assertNotNull(roomsAsyncClient);

        StepVerifier.create(roomsAsyncClient.getRoom(nonExistRoomId)).verifyError();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void deleteRoomWithConnectionString(HttpClient httpClient) {
        roomsAsyncClient = setupAsyncClient(httpClient,
                "deleteRoomWithConnectionString");
        assertNotNull(roomsAsyncClient);

        StepVerifier.create(roomsAsyncClient.deleteRoomWithResponse(nonExistRoomId)).verifyError();
    }

    private RoomsAsyncClient setupAsyncClient(HttpClient httpClient, String testName) {
        RoomsClientBuilder builder = getRoomsClientWithConnectionString(httpClient,
                RoomsServiceVersion.V2023_06_14);
        communicationClient = getCommunicationIdentityClientBuilder(httpClient).buildClient();
        return addLoggingPolicy(builder, testName).buildAsyncClient();
    }
}
