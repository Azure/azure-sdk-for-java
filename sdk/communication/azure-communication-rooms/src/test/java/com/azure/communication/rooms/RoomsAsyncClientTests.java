// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms;

import java.util.ArrayList;

import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.azure.communication.rooms.models.*;
import com.azure.core.http.HttpClient;
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
        Mono<Response<CommunicationRoom>> response1 = roomsAsyncClient.createRoomWithResponse(VALID_FROM, VALID_UNTIL, roomJoinPolicy, participants1);
        
        List<String> roomParticipantsMri = new ArrayList<String>();
        roomParticipantsMri.add(participants1.get(0).getCommunicationIdentifier().getRawId());
        roomParticipantsMri.add(participants1.get(1).getCommunicationIdentifier().getRawId());
        roomParticipantsMri.add(participants1.get(2).getCommunicationIdentifier().getRawId());
        
        StepVerifier.create(response1)
            .assertNext(roomResult -> {
                assertHappyPath(roomResult, 201);
                assertEquals(roomResult.getValue().getParticipants().size(), 3);
                assertEquals(roomParticipantsMri.contains(roomResult.getValue().getParticipants().get(0).getCommunicationIdentifier().getRawId()), true);
                assertEquals(roomParticipantsMri.contains(roomResult.getValue().getParticipants().get(1).getCommunicationIdentifier().getRawId()), true);
                assertEquals(roomParticipantsMri.contains(roomResult.getValue().getParticipants().get(2).getCommunicationIdentifier().getRawId()), true);
            })
            .verifyComplete();

        String roomId = response1.block().getValue().getRoomId();

        Mono<Response<CommunicationRoom>> response3 =  roomsAsyncClient.updateRoomWithResponse(roomId, VALID_FROM, VALID_FROM.plusMonths(3), null, null);

        System.out.println(VALID_FROM.plusMonths(3).getDayOfYear());
        
        StepVerifier.create(response3)
            .assertNext(roomResult -> {
                assertHappyPath(roomResult, 200);
                assertEquals(roomResult.getValue().getParticipants().size(), 3);
            }).verifyComplete();

        Mono<Response<CommunicationRoom>> response4 =  roomsAsyncClient.getRoomWithResponse(roomId);

        StepVerifier.create(response4)
            .assertNext(result4 -> {
                assertHappyPath(result4, 200);
                assertEquals(result4.getValue().getParticipants().size(), 3);
            }).verifyComplete();

        Mono<Response<Void>> response5 =  roomsAsyncClient.deleteRoomWithResponse(roomId);
        StepVerifier.create(response5)
        .assertNext(result5 -> {
            assertEquals(result5.getStatusCode(), 204);
        }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomFullCycleWithOutResponseStep(HttpClient httpClient) {
        roomsAsyncClient = setupAsyncClient(httpClient, "createRoomFullCycleWithOutResponse");
        Mono<CommunicationRoom> response1 = roomsAsyncClient.createRoom(VALID_FROM, VALID_UNTIL, roomJoinPolicy, participants1);

        StepVerifier.create(response1)
          .assertNext(roomResult -> {
              assertEquals(roomResult.getParticipants().size(), 3);
          }).verifyComplete();

        String roomId = response1.block().getRoomId();

        Mono<CommunicationRoom> response3 =  roomsAsyncClient.updateRoom(roomId, VALID_FROM, VALID_FROM.plusMonths(3), null, null);

        StepVerifier.create(response3)
            .assertNext(result3 -> {
                assertEquals(result3.getParticipants().size(), 3);
            }).verifyComplete();

        Mono<CommunicationRoom> response4 = roomsAsyncClient.getRoom(roomId);

        StepVerifier.create(response4)
            .assertNext(result4 -> {
                assertEquals(result4.getParticipants().size(), 3);
            }).verifyComplete();

        Mono<Response<Void>> response5 =  roomsAsyncClient.deleteRoomWithResponse(roomId);
        StepVerifier.create(response5)
        .assertNext(result5 -> {
            assertEquals(result5.getStatusCode(), 204);
        }).verifyComplete();

    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void deleteParticipantsWithOutResponseStep(HttpClient httpClient) {
        roomsAsyncClient = setupAsyncClient(httpClient, "deleteParticipantsWithOutResponseStep");
        createUsers(httpClient);

        assertNotNull(roomsAsyncClient);

        Mono<CommunicationRoom> response1 = roomsAsyncClient.createRoom(VALID_FROM, VALID_UNTIL, roomJoinPolicy, participants1);

        StepVerifier.create(response1)
        .assertNext(roomResult -> {
            assertEquals(roomResult.getParticipants().size(), 3);

        }).verifyComplete();

        String roomId = response1.block().getRoomId();

        Mono<ParticipantsCollection> response4 =  roomsAsyncClient.removeParticipants(roomId, participants5);
        StepVerifier.create(response4)
            .assertNext(result4 -> {
                assertEquals(result4.getParticipants().size(), 0);
            }).verifyComplete();

        Mono<Response<Void>> response5 =  roomsAsyncClient.deleteRoomWithResponse(roomId);
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

        Mono<Response<CommunicationRoom>> response1 = roomsAsyncClient.createRoomWithResponse(VALID_FROM, VALID_UNTIL, roomJoinPolicy, null);
        StepVerifier.create(response1)
            .assertNext(roomResult -> {
                assertHappyPath(roomResult, 201);
                assertEquals(roomResult.getValue().getParticipants(), Collections.EMPTY_LIST);
            })
            .verifyComplete();

        String roomId = response1.block().getValue().getRoomId();

        Mono<Response<ParticipantsCollection>> response2 =  roomsAsyncClient.addParticipantsWithResponse(roomId, participants5);

        StepVerifier.create(response2)
            .assertNext(result2 -> {
                assertEquals(result2.getStatusCode(), 200);
                assertEquals(result2.getValue().getParticipants().size(), 3);
            }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void addParticipantsWithOutResponseStep(HttpClient httpClient) {
        roomsAsyncClient = setupAsyncClient(httpClient, "addParticipantsWithOutResponseStep");
        assertNotNull(roomsAsyncClient);
        List<RoomParticipant> participants = new ArrayList<RoomParticipant>();
        Mono<CommunicationRoom> response1 = roomsAsyncClient.createRoom(VALID_FROM, VALID_UNTIL, roomJoinPolicy, participants);

        StepVerifier.create(response1)
        .assertNext(roomResult -> {
            assertEquals(roomResult.getParticipants(), participants);
        }).verifyComplete();

        String roomId = response1.block().getRoomId();

        Mono<ParticipantsCollection> response2 =  roomsAsyncClient.addParticipants(roomId, participants5);

        StepVerifier.create(response2)
            .assertNext(roomResult -> {
                assertEquals(roomResult.getParticipants().size(), 3);
            }).verifyComplete();

        StepVerifier.create(roomsAsyncClient.getRoom(roomId)).assertNext(response4 -> {
            assertEquals(response4.getParticipants().size(), 3);
        }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateParticipantsWithResponseStep(HttpClient httpClient) {
        roomsAsyncClient = setupAsyncClient(httpClient, "updateParticipantsWithResponseStep");
        assertNotNull(roomsAsyncClient);

        Mono<Response<CommunicationRoom>> response1 = roomsAsyncClient.createRoomWithResponse(VALID_FROM, VALID_UNTIL, roomJoinPolicy, participants2);
        StepVerifier.create(response1)
            .assertNext(roomResult -> {
                assertHappyPath(roomResult, 201);
                assertEquals(roomResult.getValue().getParticipants().size(), 2);
            })
            .verifyComplete();

        String roomId = response1.block().getValue().getRoomId();

        Mono<Response<ParticipantsCollection>> response2 =  roomsAsyncClient.updateParticipantsWithResponse(roomId, participantsWithRoleUpdates);

        StepVerifier.create(response2)
            .assertNext(result2 -> {
                assertEquals(result2.getStatusCode(), 200);
                assertEquals(result2.getValue().getParticipants().size(), 2);
            }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void updateParticipantsWithOutResponseStep(HttpClient httpClient) {
        roomsAsyncClient = setupAsyncClient(httpClient, "updateParticipantsWithOutResponseStep");
        assertNotNull(roomsAsyncClient);
        Mono<CommunicationRoom> response1 = roomsAsyncClient.createRoom(VALID_FROM, VALID_UNTIL, roomJoinPolicy, participants2);

        StepVerifier.create(response1)
        .assertNext(roomResult -> {
            assertEquals(roomResult.getParticipants().size(), 2);
        }).verifyComplete();

        String roomId = response1.block().getRoomId();
        Mono<ParticipantsCollection> response2 =  roomsAsyncClient.updateParticipants(roomId, participantsWithRoleUpdates);

        StepVerifier.create(response2)
            .assertNext(roomResult -> {
                assertEquals(roomResult.getParticipants().size(), 2);
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
        RoomsClientBuilder builder = getRoomsClientWithConnectionString(httpClient, RoomsServiceVersion.V2022_02_01_Preview);
        createUsers(httpClient);
        return addLoggingPolicy(builder, testName).buildAsyncClient();
    }
}
