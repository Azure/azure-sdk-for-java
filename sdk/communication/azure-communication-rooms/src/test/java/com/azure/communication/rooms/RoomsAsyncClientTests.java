// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.azure.communication.rooms.models.*;
import com.azure.core.http.HttpClient;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.Response;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;

import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

public class RoomsAsyncClientTests extends RoomsTestBase {
    private RoomsAsyncClient roomsAsyncClient;

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
    public void createRoomFullCycleWithResponseStep(HttpClient httpClient) {
        roomsAsyncClient = setupAsyncClient(httpClient, "createRoomFullCycleWithResponse");
        assertNotNull(roomsAsyncClient);

        CreateRoomOptions roomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL)
                .setParticipants(participants1);

        Mono<Response<CommunicationRoom>> response1 = roomsAsyncClient.createRoomWithResponse(roomOptions);

        List<String> roomParticipantsMri = new ArrayList<String>();
        roomParticipantsMri.add(participants1.get(0).getCommunicationIdentifier().getRawId());
        roomParticipantsMri.add(participants1.get(1).getCommunicationIdentifier().getRawId());
        roomParticipantsMri.add(participants1.get(2).getCommunicationIdentifier().getRawId());

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
        roomsAsyncClient = setupAsyncClient(httpClient, "createRoomFullCycleWithOutResponse");
        assertNotNull(roomsAsyncClient);

        CreateRoomOptions roomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL)
                .setParticipants(participants1);

        Mono<CommunicationRoom> response1 = roomsAsyncClient.createRoom(roomOptions);

        // StepVerifier.create(response1)
        // .assertNext(roomResult -> {
        // assertEquals(VALID_FROM, roomResult.getValidFrom());
        // assertEquals(VALID_UNTIL, roomResult.getValidUntil());
        // }).verifyComplete();

        String roomId = response1.block().getRoomId();

        UpdateRoomOptions updateOptions = new UpdateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_FROM.plusMonths(3));

        Mono<CommunicationRoom> response3 = roomsAsyncClient.updateRoom(roomId, updateOptions);

        // StepVerifier.create(response3)
        // .assertNext(result3 -> {
        // assertEquals(result3.getValidUntil(), VALID_FROM.plusMonths(3));
        // }).verifyComplete();

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
    public void deleteParticipantsWithOutResponseStep(HttpClient httpClient) {
        roomsAsyncClient = setupAsyncClient(httpClient, "deleteParticipantsWithOutResponseStep");
        assertNotNull(roomsAsyncClient);

        CreateRoomOptions roomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL)
                .setParticipants(participants1);

        Mono<CommunicationRoom> response1 = roomsAsyncClient.createRoom(roomOptions);

        // StepVerifier.create(response1)
        // .assertNext(roomResult -> {
        // assertEquals(VALID_FROM, roomResult.getValidFrom());
        // assertEquals(VALID_UNTIL, roomResult.getValidUntil());

        // }).verifyComplete();

        String roomId = response1.block().getRoomId();

        Mono<RemoveParticipantsResult> response4 = roomsAsyncClient.removeParticipants(roomId,
                communicationIdentifiersForParticipants5);
        StepVerifier.create(response4)
                .assertNext(result4 -> {
                    assertTrue(result4 instanceof RemoveParticipantsResult);
                }).verifyComplete();

        //TODO: Add get after delete

        Mono<Response<Void>> response5 = roomsAsyncClient.deleteRoomWithResponse(roomId);
        StepVerifier.create(response5)
                .assertNext(result5 -> {
                    assertEquals(result5.getStatusCode(), 204);
                }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void addParticipantsWithResponseStep(HttpClient httpClient) {
        roomsAsyncClient = setupAsyncClient(httpClient, "addParticipantsWithResponseStep");
        assertNotNull(roomsAsyncClient);

        CreateRoomOptions roomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL);

        Mono<Response<CommunicationRoom>> response1 = roomsAsyncClient.createRoomWithResponse(roomOptions);

        StepVerifier.create(response1)
                .assertNext(roomResult -> {
                    assertHappyPath(roomResult, 201);
                })
                .verifyComplete();

        String roomId = response1.block().getValue().getRoomId();

        Mono<Response<UpsertParticipantsResult>> response2 = roomsAsyncClient.upsertParticipantsWithResponse(roomId,
                participants5);

        StepVerifier.create(response2)
                .assertNext(result2 -> {
                    assertEquals(result2.getStatusCode(), 200);
                }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void addParticipantsWithOutResponseStep(HttpClient httpClient) {
        roomsAsyncClient = setupAsyncClient(httpClient, "addParticipantsWithOutResponseStep");
        assertNotNull(roomsAsyncClient);
        List<RoomParticipant> participants = new ArrayList<RoomParticipant>();

        CreateRoomOptions roomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL)
                .setParticipants(participants);

        Mono<CommunicationRoom> response1 = roomsAsyncClient.createRoom(roomOptions);

        StepVerifier.create(response1)
                .assertNext(roomResult -> {
                    assertTrue(roomResult.getValidFrom() != null);
                }).verifyComplete();

        String roomId = response1.block().getRoomId();

        Mono<UpsertParticipantsResult> response2 = roomsAsyncClient.upsertParticipants(roomId, participants3);

        StepVerifier.create(response2)
                .assertNext(result2 -> {
                    assertTrue(result2 instanceof UpsertParticipantsResult);
                }).verifyComplete();

        StepVerifier.create(roomsAsyncClient.listParticipants(roomId)).assertNext(response4 -> {
            assertEquals(response4.getCommunicationIdentifier().getRawId(),
                    secondParticipant.getCommunicationIdentifier().getRawId());
        })
                .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateParticipantsWithResponseStep(HttpClient httpClient) {
        roomsAsyncClient = setupAsyncClient(httpClient, "updateParticipantsWithResponseStep");
        assertNotNull(roomsAsyncClient);

        CreateRoomOptions roomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL)
                .setParticipants(participants2);

        Mono<Response<CommunicationRoom>> response1 = roomsAsyncClient.createRoomWithResponse(roomOptions);

        StepVerifier.create(response1)
                .assertNext(roomResult -> {
                    assertHappyPath(roomResult, 201);
                })
                .verifyComplete();

        String roomId = response1.block().getValue().getRoomId();

        Mono<Response<UpsertParticipantsResult>> response2 = roomsAsyncClient.upsertParticipantsWithResponse(roomId,
                participantsWithRoleUpdates);

        StepVerifier.create(response2)
                .assertNext(result2 -> {
                    assertEquals(result2.getStatusCode(), 200);
                }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateParticipantsToDefaultRoleWithOutResponseStep(HttpClient httpClient) {
        roomsAsyncClient = setupAsyncClient(httpClient, "updateParticipantsToDefaultRoleWithOutResponseStep");
        assertNotNull(roomsAsyncClient);

        CreateRoomOptions roomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL)
                .setParticipants(participants8);

        Mono<CommunicationRoom> response1 = roomsAsyncClient.createRoom(roomOptions);

        StepVerifier.create(response1)
                .assertNext(roomResult -> {
                    assertTrue(roomResult.getValidFrom() != null);
                }).verifyComplete();

        String roomId = response1.block().getRoomId();

        Mono<UpsertParticipantsResult> response2 = roomsAsyncClient.upsertParticipants(roomId, participants9);

        StepVerifier.create(response2)
                .assertNext(result2 -> {
                    assertTrue(result2 instanceof UpsertParticipantsResult);
                }).verifyComplete();

        // StepVerifier.create(roomsAsyncClient.listParticipants(roomId)).assertNext(response4 -> {
        //     assertEquals(ParticipantRole.ATTENDEE, response4.getRole());
        // })
        //         .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateParticipantsToDefaultRoleWithResponseStep(HttpClient httpClient) {
        roomsAsyncClient = setupAsyncClient(httpClient, "updateParticipantsToDefaultRoleWithResponseStep");
        assertNotNull(roomsAsyncClient);

        CreateRoomOptions roomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL)
                .setParticipants(participants8);

        Mono<Response<CommunicationRoom>> response1 = roomsAsyncClient.createRoomWithResponse(roomOptions);

        StepVerifier.create(response1)
                .assertNext(roomResult -> {
                    assertHappyPath(roomResult, 201);
                })
                .verifyComplete();

        String roomId = response1.block().getValue().getRoomId();

        Mono<Response<UpsertParticipantsResult>> response2 = roomsAsyncClient.upsertParticipantsWithResponse(roomId,
                participants9);

        StepVerifier.create(response2)
                .assertNext(result2 -> {
                    assertEquals(result2.getStatusCode(), 200);
                }).verifyComplete();

        PagedFlux<RoomParticipant> response3 = roomsAsyncClient.listParticipants(roomId);

        // StepVerifier.create(response3).assertNext(response4 -> {
        //     assertTrue(response4.getRole() == ParticipantRole.ATTENDEE);
        // })
        //         .verifyComplete();

    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void listParticipantsWithOutResponseStep(HttpClient httpClient) {
        roomsAsyncClient = setupAsyncClient(httpClient, "listParticipantsWithOutResponseStep");
        assertNotNull(roomsAsyncClient);

        CreateRoomOptions roomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL)
                .setParticipants(participants5);

        Mono<CommunicationRoom> response1 = roomsAsyncClient.createRoom(roomOptions);

        StepVerifier.create(response1)
                .assertNext(roomResult -> {
                    assertTrue(roomResult.getValidFrom() != null);
                }).verifyComplete();

        String roomId = response1.block().getRoomId();

        PagedFlux<RoomParticipant> response2 = roomsAsyncClient.listParticipants(roomId);

        // StepVerifier.create(response2).assertNext(response4 -> {
        //     assertEquals(participants5.size(), response2.getPageSize());
        // })
        //         .verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateParticipantsWithOutResponseStep(HttpClient httpClient) {
        roomsAsyncClient = setupAsyncClient(httpClient, "updateParticipantsWithOutResponseStep");
        assertNotNull(roomsAsyncClient);

        CreateRoomOptions roomOptions = new CreateRoomOptions()
                .setValidFrom(VALID_FROM)
                .setValidUntil(VALID_UNTIL)
                .setParticipants(participants2);

        Mono<CommunicationRoom> response1 = roomsAsyncClient.createRoom(roomOptions);

        // StepVerifier.create(response1)
        // .assertNext(roomResult -> {
        // assertEquals(VALID_FROM, roomResult.getValidFrom());
        // assertEquals(VALID_UNTIL, roomResult.getValidUntil());
        // }).verifyComplete();

        String roomId = response1.block().getRoomId();

        Mono<UpsertParticipantsResult> response2 = roomsAsyncClient.upsertParticipants(roomId,
                participantsWithRoleUpdates);

        StepVerifier.create(response2)
                .assertNext(roomResult -> {
                    assertTrue(roomResult instanceof UpsertParticipantsResult);
                }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getRoomWithUnexistingRoomId(HttpClient httpClient) {
        roomsAsyncClient = setupAsyncClient(httpClient, "getRoomWithUnexistingRoomId");
        assertNotNull(roomsAsyncClient);

        StepVerifier.create(roomsAsyncClient.getRoom(NONEXIST_ROOM_ID)).verifyError();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void deleteRoomWithConnectionString(HttpClient httpClient) {
        roomsAsyncClient = setupAsyncClient(httpClient, "deleteRoomWithConnectionString");
        assertNotNull(roomsAsyncClient);

        StepVerifier.create(roomsAsyncClient.deleteRoomWithResponse(NONEXIST_ROOM_ID)).verifyError();
    }

    private RoomsAsyncClient setupAsyncClient(HttpClient httpClient, String testName) {
        RoomsClientBuilder builder = getRoomsClientWithConnectionString(httpClient,
                RoomsServiceVersion.V2023_03_31_PREVIEW);
        createUsers(httpClient);
        return addLoggingPolicy(builder, testName).buildAsyncClient();
    }
}
