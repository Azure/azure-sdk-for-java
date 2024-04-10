// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.azure.communication.identity.CommunicationIdentityClient;
import com.azure.communication.rooms.models.*;
import com.azure.communication.rooms.implementation.models.CommunicationErrorResponseException;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.test.http.AssertingHttpClientBuilder;
import java.util.Arrays;
import java.util.List;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;

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
                .setPstnDialOutEnabled(true)
                .setParticipants(participants);

        Mono<Response<CommunicationRoom>> response1 = roomsAsyncClient.createRoomWithResponse(roomOptions);

        StepVerifier.create(response1)
                .assertNext(roomResult -> {
                    assertHappyPath(roomResult, 201);
                    assertTrue(roomResult.getValue().isPstnDialOutEnabled());
                })
                .verifyComplete();

        String roomId = response1.block().getValue().getRoomId();

        UpdateRoomOptions updateOptions = new UpdateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_FROM.plusMonths(3))
                .setPstnDialOutEnabled(false);

        Mono<Response<CommunicationRoom>> response3 = roomsAsyncClient.updateRoomWithResponse(roomId, updateOptions);

        System.out.println(VALID_FROM.plusMonths(3).getDayOfYear());

        StepVerifier.create(response3)
                .assertNext(roomResult -> {
                    assertHappyPath(roomResult, 200);
                    assertFalse(roomResult.getValue().isPstnDialOutEnabled());
                }).verifyComplete();

        Mono<Response<CommunicationRoom>> response4 = roomsAsyncClient.getRoomWithResponse(roomId, null);

        StepVerifier.create(response4)
                .assertNext(result4 -> {
                    assertHappyPath(result4, 200);
                    assertFalse(result4.getValue().isPstnDialOutEnabled());
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
                .setPstnDialOutEnabled(true)
                .setParticipants(participants);

        Mono<CommunicationRoom> response1 = roomsAsyncClient.createRoom(roomOptions);

        StepVerifier.create(response1)
                .assertNext(roomResult -> {
                    assertTrue(roomResult.getRoomId() != null);
                    assertTrue(roomResult.getCreatedAt() != null);
                    assertTrue(roomResult.getValidFrom() != null);
                    assertTrue(roomResult.getValidUntil() != null);
                    assertTrue(roomResult.isPstnDialOutEnabled());
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
                    assertTrue(result3.isPstnDialOutEnabled());

                }).verifyComplete();

        Mono<CommunicationRoom> response4 = roomsAsyncClient.getRoom(roomId);

        StepVerifier.create(response4)
                .assertNext(result4 -> {
                    assertEquals(result4.getRoomId(), roomId);
                }).verifyComplete();

        Mono<Void> response5 = roomsAsyncClient.deleteRoom(roomId, null);
        StepVerifier.create(response5).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomWithNoAttributes(HttpClient httpClient) {
        roomsAsyncClient = setupAsyncClient(httpClient,
                "createRoomWithNoAttributes");
        assertNotNull(roomsAsyncClient);

        CreateRoomOptions roomOptions = new CreateRoomOptions();

        Mono<CommunicationRoom> response1 = roomsAsyncClient.createRoom(roomOptions);

        StepVerifier.create(response1)
                .assertNext(roomResult -> {
                    assertTrue(roomResult.getRoomId() != null);
                    assertTrue(roomResult.getCreatedAt() != null);
                    assertTrue(roomResult.getValidFrom() != null);
                    assertTrue(roomResult.getValidUntil() != null);
                    assertFalse(roomResult.isPstnDialOutEnabled());
                }).verifyComplete();


        String roomId = response1.block().getRoomId();

        Mono<CommunicationRoom> response2 = roomsAsyncClient.getRoom(roomId);

        StepVerifier.create(response2)
                .assertNext(result2 -> {
                    assertEquals(result2.getRoomId(), roomId);
                }).verifyComplete();

        Mono<Response<Void>> response3 = roomsAsyncClient.deleteRoomWithResponse(roomId);
        StepVerifier.create(response3)
                .assertNext(result3 -> {
                    assertEquals(result3.getStatusCode(), 204);
                }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomWithOnlyParticipantAttributes(HttpClient httpClient) {
        roomsAsyncClient = setupAsyncClient(httpClient,
                "createRoomWithOnlyParticipantAttributes");
        assertNotNull(roomsAsyncClient);

        RoomParticipant firstParticipant = new RoomParticipant(communicationClient.createUser());
        List<RoomParticipant> participants = Arrays.asList(firstParticipant);

        CreateRoomOptions roomOptions = new CreateRoomOptions()
                .setParticipants(participants)
                .setPstnDialOutEnabled(true);

        Mono<CommunicationRoom> response1 = roomsAsyncClient.createRoom(roomOptions);

        StepVerifier.create(response1)
                .assertNext(roomResult -> {
                    assertTrue(roomResult.getRoomId() != null);
                    assertTrue(roomResult.getCreatedAt() != null);
                    assertTrue(roomResult.getValidFrom() != null);
                    assertTrue(roomResult.getValidUntil() != null);
                    assertTrue(roomResult.isPstnDialOutEnabled());
                }).verifyComplete();


        String roomId = response1.block().getRoomId();

        Mono<Response<Void>> response2 = roomsAsyncClient.deleteRoomWithResponse(roomId, null);
        StepVerifier.create(response2)
                .assertNext(result2 -> {
                    assertEquals(result2.getStatusCode(), 204);
                }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomWithOnlyPstnEnabledAttribute(HttpClient httpClient) {
        roomsAsyncClient = setupAsyncClient(httpClient,
                "createRoomWithOnlyParticipantAttributes");
        assertNotNull(roomsAsyncClient);

        CreateRoomOptions roomOptions = new CreateRoomOptions()
                .setPstnDialOutEnabled(true);

        Mono<CommunicationRoom> response1 = roomsAsyncClient.createRoom(roomOptions);

        StepVerifier.create(response1)
                .assertNext(roomResult -> {
                    assertTrue(roomResult.getRoomId() != null);
                    assertTrue(roomResult.getCreatedAt() != null);
                    assertTrue(roomResult.getValidFrom() != null);
                    assertTrue(roomResult.getValidUntil() != null);
                    assertTrue(roomResult.isPstnDialOutEnabled());
                }).verifyComplete();


        String roomId = response1.block().getRoomId();

        Mono<Response<Void>> response2 = roomsAsyncClient.deleteRoomWithResponse(roomId, null);
        StepVerifier.create(response2)
                .assertNext(result2 -> {
                    assertEquals(result2.getStatusCode(), 204);
                }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomWithValidUntilInPast(HttpClient httpClient) {
        roomsAsyncClient = setupAsyncClient(httpClient,
                "createRoomWithValidUntilInPast");
        assertNotNull(roomsAsyncClient);

        RoomParticipant firstParticipant = new RoomParticipant(communicationClient.createUser());
        List<RoomParticipant> participants = Arrays.asList(firstParticipant);

        CreateRoomOptions roomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL.minusMonths(6))
                .setPstnDialOutEnabled(true)
                .setParticipants(participants);

        CommunicationErrorResponseException exception =
            assertThrows(CommunicationErrorResponseException.class, () -> {
                roomsAsyncClient.createRoomWithResponse(roomOptions, null).block();
            });
        assertEquals("BadRequest", exception.getValue().getError().getCode());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomWithValidUntilGreaterThan180(HttpClient httpClient) {
        roomsAsyncClient = setupAsyncClient(httpClient,
                "createRoomWithValidUntilGreaterThan180");
        assertNotNull(roomsAsyncClient);

        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(null)
                .setValidUntil(VALID_FROM.plusDays(181))
                .setParticipants(null);

        CommunicationErrorResponseException exception =
            assertThrows(CommunicationErrorResponseException.class, () -> {
                roomsAsyncClient.createRoomWithResponse(createRoomOptions, Context.NONE).block();
            });
        assertEquals("BadRequest", exception.getValue().getError().getCode());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomWithOnlyValidFromGreaterThan180(HttpClient httpClient) {
        roomsAsyncClient = setupAsyncClient(httpClient,
                "createRoomWithOnlyValidFromGreaterThan180");
        assertNotNull(roomsAsyncClient);

        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM.plusDays(181))
                .setValidUntil(null)
                .setParticipants(null);

        CommunicationErrorResponseException exception =
            assertThrows(CommunicationErrorResponseException.class, () -> {
                roomsAsyncClient.createRoomWithResponse(createRoomOptions, Context.NONE).block();
            });
        assertEquals("BadRequest", exception.getValue().getError().getCode());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomWithBadParticipantMri(HttpClient httpClient) {
        roomsAsyncClient = setupAsyncClient(httpClient,
                "createRoomWithBadParticipantMri");
        assertNotNull(roomsAsyncClient);

        // Create list of room participant with bad mri
        List<RoomParticipant> badParticipant = Arrays
                .asList(new RoomParticipant(new CommunicationUserIdentifier("badMRI")));

        CreateRoomOptions roomOptions = new CreateRoomOptions()
                .setParticipants(badParticipant);

        CommunicationErrorResponseException exception =
            assertThrows(CommunicationErrorResponseException.class, () -> {
                roomsAsyncClient.createRoomWithResponse(roomOptions, Context.NONE).block();
            });
        assertEquals("BadRequest", exception.getValue().getError().getCode());
        assertFalse(exception.getValue().getError().getMessage().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getRoomWithUnexistingRoomIdReturnBadRequest(HttpClient httpClient) {
        roomsAsyncClient = setupAsyncClient(httpClient,
                "getRoomWithUnexistingRoomIdReturnBadRequest");
        assertNotNull(roomsAsyncClient);
        CommunicationErrorResponseException exception =
            assertThrows(CommunicationErrorResponseException.class, () -> {
                roomsAsyncClient.getRoom(nonExistRoomId).block();
            });
        assertEquals("BadRequest", exception.getValue().getError().getCode());
        assertFalse(exception.getValue().getError().getMessage().isEmpty());
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateRoomValidUntilGreaterThan180(HttpClient httpClient) {
        roomsAsyncClient = setupAsyncClient(httpClient,
                "updateRoomValidUntilGreaterThan180");
        assertNotNull(roomsAsyncClient);

        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL);

        Mono<Response<CommunicationRoom>> response1 = roomsAsyncClient.createRoomWithResponse(createRoomOptions, null);

        StepVerifier.create(response1)
                .assertNext(roomResult -> {
                    assertHappyPath(roomResult, 201);
                })
                .verifyComplete();

        String roomId = response1.block().getValue().getRoomId();

        UpdateRoomOptions updateRoomOptions = new UpdateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_FROM.plusDays(181));

        CommunicationErrorResponseException exception =
            assertThrows(CommunicationErrorResponseException.class, () -> {
                roomsAsyncClient.updateRoom(roomId, updateRoomOptions, null).block();
            });
        assertEquals("BadRequest", exception.getValue().getError().getCode());

        Mono<Void> response5 = roomsAsyncClient.deleteRoom(roomId, null);
        StepVerifier.create(response5).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateRoomValidFromGreaterThan180(HttpClient httpClient) {
        roomsAsyncClient = setupAsyncClient(httpClient,
                "updateRoomValidFromGreaterThan180");
        assertNotNull(roomsAsyncClient);

        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL);

        Mono<Response<CommunicationRoom>> response1 = roomsAsyncClient.createRoomWithResponse(createRoomOptions);

        StepVerifier.create(response1)
                .assertNext(roomResult -> {
                    assertHappyPath(roomResult, 201);
                })
                .verifyComplete();

        String roomId = response1.block().getValue().getRoomId();

        UpdateRoomOptions updateRoomOptions = new UpdateRoomOptions()
                .setValidFrom(VALID_FROM.plusDays(181))
                .setValidUntil(VALID_UNTIL);

        CommunicationErrorResponseException exception =
            assertThrows(CommunicationErrorResponseException.class, () -> {
                roomsAsyncClient.updateRoom(roomId, updateRoomOptions).block();
            });
        assertEquals("BadRequest", exception.getValue().getError().getCode());

        Mono<Response<Void>> response2 = roomsAsyncClient.deleteRoomWithResponse(roomId);
        StepVerifier.create(response2)
                .assertNext(result2 -> {
                    assertEquals(result2.getStatusCode(), 204);
                }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateRoomValidUntilInPast(HttpClient httpClient) {
        roomsAsyncClient = setupAsyncClient(httpClient,
                "updateRoomValidUntilInPast");
        assertNotNull(roomsAsyncClient);

        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL);

        Mono<Response<CommunicationRoom>> response1 = roomsAsyncClient.createRoomWithResponse(createRoomOptions);

        StepVerifier.create(response1)
                .assertNext(roomResult -> {
                    assertHappyPath(roomResult, 201);
                })
                .verifyComplete();

        String roomId = response1.block().getValue().getRoomId();

        UpdateRoomOptions updateRoomOptions = new UpdateRoomOptions()
                .setValidFrom(VALID_FROM.minusMonths(6))
                .setValidUntil(VALID_FROM.minusMonths(3));

        CommunicationErrorResponseException exception =
            assertThrows(CommunicationErrorResponseException.class, () -> {
                roomsAsyncClient.updateRoom(roomId, updateRoomOptions).block();
            });
        assertEquals("BadRequest", exception.getValue().getError().getCode());

        Mono<Response<Void>> response2 = roomsAsyncClient.deleteRoomWithResponse(roomId);
        StepVerifier.create(response2)
                .assertNext(result2 -> {
                    assertEquals(result2.getStatusCode(), 204);
                }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateRoomWithInvalidRoomId(HttpClient httpClient) {
        roomsAsyncClient = setupAsyncClient(httpClient,
                "updateRoomWithInvalidRoomId");
        assertNotNull(roomsAsyncClient);

        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL);

        Mono<Response<CommunicationRoom>> response1 = roomsAsyncClient.createRoomWithResponse(createRoomOptions);

        StepVerifier.create(response1)
                .assertNext(roomResult -> {
                    assertHappyPath(roomResult, 201);
                })
                .verifyComplete();

        String roomId = response1.block().getValue().getRoomId();

        UpdateRoomOptions updateRoomOptions = new UpdateRoomOptions()
                .setValidFrom(VALID_FROM.minusMonths(6))
                .setValidUntil(VALID_FROM.minusMonths(3));

        CommunicationErrorResponseException exception =
            assertThrows(CommunicationErrorResponseException.class, () -> {
                roomsAsyncClient.updateRoom(roomId, updateRoomOptions).block();
            });
        assertEquals("BadRequest", exception.getValue().getError().getCode());

        Mono<Response<Void>> response2 = roomsAsyncClient.deleteRoomWithResponse(roomId);
        StepVerifier.create(response2)
                .assertNext(result2 -> {
                    assertEquals(result2.getStatusCode(), 204);
                }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateRoomWithNoParameters(HttpClient httpClient) {
        roomsAsyncClient = setupAsyncClient(httpClient,
                "updateRoomWithNoParameters");
        assertNotNull(roomsAsyncClient);

        CreateRoomOptions createRoomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL)
                .setPstnDialOutEnabled(true);

        Mono<Response<CommunicationRoom>> response1 = roomsAsyncClient.createRoomWithResponse(createRoomOptions);

        StepVerifier.create(response1)
                .assertNext(roomResult -> {
                    assertHappyPath(roomResult, 201);
                })
                .verifyComplete();

        String roomId = response1.block().getValue().getRoomId();

        UpdateRoomOptions updateRoomOptions = new UpdateRoomOptions();
        Mono<Response<CommunicationRoom>> response2 = roomsAsyncClient.updateRoomWithResponse(roomId, updateRoomOptions);

        StepVerifier.create(response2)
                .assertNext(roomResult2 -> {
                    assertHappyPath(roomResult2, 200);
                    assertTrue(roomResult2.getValue().isPstnDialOutEnabled());
                }).verifyComplete();

        Mono<Response<CommunicationRoom>> response3 = roomsAsyncClient.getRoomWithResponse(roomId, null);

        StepVerifier.create(response3)
                .assertNext(result3 -> {
                    assertHappyPath(result3, 200);
                    assertTrue(result3.getValue().isPstnDialOutEnabled());
                }).verifyComplete();

        Mono<Response<Void>> response4 = roomsAsyncClient.deleteRoomWithResponse(roomId);
        StepVerifier.create(response4)
                .assertNext(result4 -> {
                    assertEquals(result4.getStatusCode(), 204);
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
                .setValidUntil(VALID_UNTIL)
                .setPstnDialOutEnabled(true);

        Mono<CommunicationRoom> createCommunicationRoom = roomsAsyncClient.createRoom(createRoomOptions);

        StepVerifier.create(createCommunicationRoom)
                .assertNext(roomResult -> {
                    assertTrue(roomResult.getRoomId() != null);
                    assertTrue(roomResult.getCreatedAt() != null);
                    assertTrue(roomResult.getValidFrom() != null);
                    assertTrue(roomResult.getValidUntil() != null);
                    assertTrue(roomResult.isPstnDialOutEnabled());
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
        AddOrUpdateParticipantsResult addParticipantResponse = roomsAsyncClient.addOrUpdateParticipants(roomId, participants, null).block();

        // Check participant count, expected 3
        PagedFlux<RoomParticipant> listParticipantsResponse2 = roomsAsyncClient.listParticipants(roomId, null);

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
        Mono<Response<RemoveParticipantsResult>> removeParticipantResponse = roomsAsyncClient.removeParticipantsWithResponse(roomId,
                participantsIdentifiersForParticipants, null);

        StepVerifier.create(removeParticipantResponse)
                .assertNext(result -> {
                    assertEquals(result.getStatusCode(), 200);
                })
                .verifyComplete();

        // Check participant count, expected 1
        PagedFlux<RoomParticipant> listParticipantsResponse4 = roomsAsyncClient.listParticipants(roomId);

        StepVerifier.create(listParticipantsResponse4.count())
                .expectNext(1L)
                .verifyComplete();

        // Remove participant with incorrect MRI
        List<CommunicationIdentifier> participantsIdentifiersForNonExistentParticipant = Arrays
            .asList(new CommunicationUserIdentifier("8:acs:nonExistentParticipant"));
        CommunicationErrorResponseException exception =
            assertThrows(CommunicationErrorResponseException.class, () -> {
                roomsAsyncClient.removeParticipants(roomId, participantsIdentifiersForNonExistentParticipant).block();
            });
        assertEquals("BadRequest", exception.getValue().getError().getCode());
        assertFalse(exception.getValue().getError().getMessage().isEmpty());

        // Remove Non-existent participants
        Mono<RemoveParticipantsResult> removeParticipantResponse2 = roomsAsyncClient.removeParticipants(roomId,
                participantsIdentifiersForParticipants);

        StepVerifier.create(removeParticipantResponse2)
                .assertNext(result -> {
                    assertEquals(true, result instanceof RemoveParticipantsResult);
                })
                .verifyComplete();

        // Check participant count, expected 1
        PagedFlux<RoomParticipant> listParticipantsResponse5 = roomsAsyncClient.listParticipants(roomId);

        StepVerifier.create(listParticipantsResponse5.count())
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
                .setValidUntil(VALID_UNTIL)
                .setPstnDialOutEnabled(true);

        Mono<CommunicationRoom> createCommunicationRoom = roomsAsyncClient.createRoom(createRoomOptions);

        StepVerifier.create(createCommunicationRoom)
                .assertNext(roomResult -> {
                    assertTrue(roomResult.getRoomId() != null);
                    assertTrue(roomResult.getCreatedAt() != null);
                    assertTrue(roomResult.getValidFrom() != null);
                    assertTrue(roomResult.getValidUntil() != null);
                    assertTrue(roomResult.isPstnDialOutEnabled());
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
        roomsAsyncClient.addOrUpdateParticipants(roomId, participants).block();

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
    public void addUpdateInvalidParticipants(HttpClient httpClient) {
        roomsAsyncClient = setupAsyncClient(httpClient, "addUpdateInvalidParticipants");
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

        RoomParticipant firstParticipant = new RoomParticipant(new CommunicationUserIdentifier("badMRI"));
        RoomParticipant secondParticipant = new RoomParticipant(new CommunicationUserIdentifier("badMRI2"));

        List<RoomParticipant> participants = Arrays.asList(firstParticipant, secondParticipant);

        // Add Invalid participants.
        CommunicationErrorResponseException exception =
            assertThrows(CommunicationErrorResponseException.class, () -> {
                roomsAsyncClient.addOrUpdateParticipants(roomId, participants).block();
            });
        assertEquals(400, exception.getResponse().getStatusCode());

        Mono<Response<Void>> response2 = roomsAsyncClient.deleteRoomWithResponse(roomId);
        StepVerifier.create(response2)
                .assertNext(result2 -> {
                    assertEquals(result2.getStatusCode(), 204);
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
        PagedFlux<CommunicationRoom> listRoomResponse = roomsAsyncClient.listRooms(null);

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
                .addOrUpdateParticipantsWithResponse(roomId, participantToUpdate, null);

        StepVerifier.create(response2)
                .assertNext(result2 -> {
                    assertEquals(result2.getStatusCode(), 200);
                }).verifyComplete();

        PagedFlux<RoomParticipant> response3 = roomsAsyncClient.listParticipants(roomId);

        StepVerifier.create(response3).assertNext(response4 -> {
            assertEquals(ParticipantRole.ATTENDEE, response4.getRole());
        })
                .verifyComplete();

        Mono<Response<Void>> response4 = roomsAsyncClient.deleteRoomWithResponse(roomId);
        StepVerifier.create(response4)
                .assertNext(result4 -> {
                    assertEquals(result4.getStatusCode(), 204);
                }).verifyComplete();

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

    private HttpClient buildAsyncAssertingClient(HttpClient httpClient) {
        return new AssertingHttpClientBuilder(httpClient)
            .assertAsync()
            .build();
    }

    private RoomsAsyncClient setupAsyncClient(HttpClient httpClient, String testName) {
        RoomsClientBuilder builder = getRoomsClientWithConnectionString(
                buildAsyncAssertingClient(httpClient == null ? interceptorManager.getPlaybackClient()
                : httpClient),
                RoomsServiceVersion.V2024_04_15);
        communicationClient = getCommunicationIdentityClientBuilder(httpClient).buildClient();
        return addLoggingPolicy(builder, testName).buildAsyncClient();
    }
}
