// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.azure.communication.rooms.models.CommunicationRoom;
import com.azure.communication.rooms.models.RoomRequest;
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

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomFullCycleWithResponseStep(HttpClient httpClient) {
        RoomsClientBuilder builder = getRoomsClientWithConnectionString(httpClient);
        roomsAsyncClient = setupAsyncClient(builder, "createRoomFullCycleWithResponse");
        assertNotNull(roomsAsyncClient);
        
        RoomRequest request = new RoomRequest();
        request.setValidFrom(VALID_FROM);
        request.setValidUntil(VALID_UNTIL);
        request.setParticipants(PARTICIPANTS1);

        Mono<Response<CommunicationRoom>> response1 = roomsAsyncClient.createRoomWithResponse(request);
        StepVerifier.create(response1)
            .assertNext(roomResult -> {
                assertHappyPath(roomResult, 201);
                assertEquals(roomResult.getValue().getParticipants().size(), 3);
                assertEquals(roomResult.getValue().getParticipants().containsKey(USER1), true);
                assertEquals(roomResult.getValue().getParticipants().containsKey(USER2), true);
                assertEquals(roomResult.getValue().getParticipants().containsKey(USER3), true);
            })
            .verifyComplete();

        String roomId = response1.block().getValue().getRoomId();


        request.setParticipants(null);
        Mono<Response<CommunicationRoom>> response2 =  roomsAsyncClient.updateRoomWithResponse(roomId, request);

        StepVerifier.create(response2)
            .assertNext(roomResult -> {
                assertHappyPath(roomResult, 200);
                assertEquals(roomResult.getValue().getParticipants().size(), 3);
                assertEquals(roomResult.getValue().getParticipants().containsKey(USER1), true);
                assertEquals(roomResult.getValue().getParticipants().containsKey(USER2), true);
                assertEquals(roomResult.getValue().getParticipants().containsKey(USER3), true);
            }).verifyComplete();

        request.setParticipants(PARTICIPANTS2);
        Mono<Response<CommunicationRoom>> response3 =  roomsAsyncClient.updateRoomWithResponse(roomId, request);

        StepVerifier.create(response3)
            .assertNext(roomResult -> {
                assertHappyPath(roomResult, 200);
                assertEquals(roomResult.getValue().getParticipants().size(), 1);
                assertEquals(roomResult.getValue().getParticipants().containsKey(USER3), true);
            }).verifyComplete();

        Mono<Response<CommunicationRoom>> response4 =  roomsAsyncClient.getRoomWithResponse(roomId);

        StepVerifier.create(response4)
            .assertNext(result4 -> {
                assertHappyPath(result4, 200);
                assertEquals(result4.getValue().getParticipants().size(), 1);
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
        RoomsClientBuilder builder = getRoomsClientWithConnectionString(httpClient);
        roomsAsyncClient = setupAsyncClient(builder, "createRoomFullCycleWithOutResponse");
        assertNotNull(roomsAsyncClient);
        
        RoomRequest request = new RoomRequest();
        request.setValidFrom(VALID_FROM);
        request.setValidUntil(VALID_UNTIL);
        request.setParticipants(PARTICIPANTS1);

        Mono<CommunicationRoom> response1 = roomsAsyncClient.createRoom(request);

        StepVerifier.create(response1)
        .assertNext(roomResult -> {
            assertEquals(roomResult.getParticipants().size(), 3);
            assertEquals(roomResult.getParticipants().containsKey(USER1), true);
            assertEquals(roomResult.getParticipants().containsKey(USER2), true);
            assertEquals(roomResult.getParticipants().containsKey(USER3), true);
        }).verifyComplete();

        String roomId = response1.block().getRoomId();

        request.setParticipants(null);
        Mono<CommunicationRoom> response2 =  roomsAsyncClient.updateRoom(roomId, request);

        StepVerifier.create(response2)
            .assertNext(result2 -> {
                assertEquals(result2.getParticipants().size(), 3);
                assertEquals(result2.getParticipants().containsKey(USER1), true);
                assertEquals(result2.getParticipants().containsKey(USER2), true);
                assertEquals(result2.getParticipants().containsKey(USER3), true);
            }).verifyComplete();

        request.setParticipants(PARTICIPANTS2);
        Mono<CommunicationRoom> response3 =  roomsAsyncClient.updateRoom(roomId, request);

        StepVerifier.create(response3)
            .assertNext(result3 -> {
                assertEquals(result3.getParticipants().size(), 1);
                assertEquals(result3.getParticipants().containsKey(USER3), true);
            }).verifyComplete();

        Mono<CommunicationRoom> response4 = roomsAsyncClient.getRoom(roomId);

        StepVerifier.create(response4)
            .assertNext(result4 -> {
                assertEquals(result4.getParticipants().size(), 1);
                assertEquals(result4.getParticipants().containsKey(USER3), true);
            }).verifyComplete();

        Mono<Response<Void>> response5 =  roomsAsyncClient.deleteRoomWithResponse(roomId);
        StepVerifier.create(response5)
        .assertNext(result5 -> {
            assertEquals(result5.getStatusCode(), 204);
        }).verifyComplete();

    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void addRemoveParticipantsWithResponseStep(HttpClient httpClient) {
        RoomsClientBuilder builder = getRoomsClientWithConnectionString(httpClient);
        roomsAsyncClient = setupAsyncClient(builder, "addRemoveParticipantsWithResponseStep");
        assertNotNull(roomsAsyncClient);
        
        RoomRequest request = new RoomRequest();
        request.setValidFrom(VALID_FROM);
        request.setValidUntil(VALID_UNTIL);

        Mono<Response<CommunicationRoom>> response1 = roomsAsyncClient.createRoomWithResponse(request);
        StepVerifier.create(response1)
            .assertNext(roomResult -> {
                assertHappyPath(roomResult, 201);
                assertEquals(roomResult.getValue().getParticipants(), null);
            })
            .verifyComplete();

        String roomId = response1.block().getValue().getRoomId();

        Mono<Response<CommunicationRoom>> response2 =  roomsAsyncClient.addParticipantsWithResponse(roomId, PARTICIPANTS5);

        StepVerifier.create(response2)
            .assertNext(result2 -> {
                assertHappyPath(result2, 200);
                assertEquals(result2.getValue().getParticipants().size(), 3);
                assertEquals(result2.getValue().getParticipants().containsKey(USER1), true);
                assertEquals(result2.getValue().getParticipants().containsKey(USER2), true);
                assertEquals(result2.getValue().getParticipants().containsKey(USER3), true);
            }).verifyComplete();

        Mono<Response<CommunicationRoom>> response3 =  roomsAsyncClient.removeParticipantsWithResponse(roomId, PARTICIPANTS6);

        StepVerifier.create(response3)
            .assertNext(result3 -> {
                assertHappyPath(result3, 200);
                assertEquals(result3.getValue().getParticipants().size(), 1);
                assertEquals(result3.getValue().getParticipants().containsKey(USER1), true);
            }).verifyComplete();


    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void addRemoveParticipantsWithOutResponseStep(HttpClient httpClient) {
        RoomsClientBuilder builder = getRoomsClientWithConnectionString(httpClient);
        roomsAsyncClient = setupAsyncClient(builder, "addRemoveParticipantsWithOutResponseStep");
        assertNotNull(roomsAsyncClient);
        
        RoomRequest request = new RoomRequest();
        request.setValidFrom(VALID_FROM);
        request.setValidUntil(VALID_UNTIL);

        Mono<CommunicationRoom> response1 = roomsAsyncClient.createRoom(request);

        StepVerifier.create(response1)
        .assertNext(roomResult -> {
            assertEquals(roomResult.getParticipants(), null);
        }).verifyComplete();

        String roomId = response1.block().getRoomId();

        Mono<CommunicationRoom> response2 =  roomsAsyncClient.addParticipants(roomId, PARTICIPANTS5);
        
        StepVerifier.create(response2)
            .assertNext(roomResult -> {
                assertEquals(roomResult.getParticipants().size(), 3);
                assertEquals(roomResult.getParticipants().containsKey(USER1), true);
                assertEquals(roomResult.getParticipants().containsKey(USER2), true);
                assertEquals(roomResult.getParticipants().containsKey(USER3), true);
            }).verifyComplete();

        Mono<CommunicationRoom> response3 =  roomsAsyncClient.removeParticipants(roomId, PARTICIPANTS6);

        StepVerifier.create(response3)
            .assertNext(roomResult -> {
                assertEquals(roomResult.getParticipants().size(), 1);
                assertEquals(roomResult.getParticipants().containsKey(USER1), true);
            }).verifyComplete();

        StepVerifier.create(roomsAsyncClient.getRoom(roomId)).assertNext(response4 -> {
            assertEquals(response4.getParticipants().size(), 1);
            assertEquals(response4.getParticipants().containsKey(USER1), true);
        }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void getRoomWithUnexistingRoomId(HttpClient httpClient) {
        RoomsClientBuilder builder = getRoomsClientWithConnectionString(httpClient);
        roomsAsyncClient = setupAsyncClient(builder, "getRoomWithUnexistingRoomId");
        assertNotNull(roomsAsyncClient);

        StepVerifier.create(roomsAsyncClient.getRoom(NONEXIST_ROOM_ID)).verifyError();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void deleteRoomWithConnectionString(HttpClient httpClient) {
        RoomsClientBuilder builder = getRoomsClientWithConnectionString(httpClient);
        roomsAsyncClient = setupAsyncClient(builder, "deleteRoomWithConnectionString");
        assertNotNull(roomsAsyncClient);

        StepVerifier.create(roomsAsyncClient.deleteRoomWithResponse(NONEXIST_ROOM_ID)).verifyError();
    }

    private RoomsAsyncClient setupAsyncClient(RoomsClientBuilder builder, String testName) {
        return addLoggingPolicy(builder, testName).buildAsyncClient();
    }
}
