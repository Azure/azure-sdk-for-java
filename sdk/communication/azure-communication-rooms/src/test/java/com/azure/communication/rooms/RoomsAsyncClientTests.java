// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.azure.communication.rooms.models.CommunicationRoom;
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
        Mono<Response<CommunicationRoom>> response1 = roomsAsyncClient.createRoomWithResponse(VALID_FROM, VALID_UNTIL, PARTICIPANTS1);
        StepVerifier.create(response1)
            .assertNext(roomResult -> {
                assertHappyPath(roomResult, 201);
                assertEquals(roomResult.getValue().getParticipants().size(), 3);
            })
            .verifyComplete();

        String roomId = response1.block().getValue().getRoomId();
        StepVerifier.create(
            roomsAsyncClient.updateRoomWithResponse(roomId, VALID_FROM, VALID_UNTIL, PARTICIPANTS2))
            .assertNext(response2 -> {
                assertHappyPath(response2, 200);
                assertEquals(response2.getValue().getParticipants().size(), 1);
            }).verifyComplete();

        StepVerifier.create(roomsAsyncClient.getRoomWithResponse(roomId)).assertNext(response3 -> {
            assertHappyPath(response3, 200);
            assertEquals(response3.getValue().getParticipants().size(), 1);
        }).verifyComplete();
    }

    @ParameterizedTest
    @MethodSource("com.azure.core.test.TestBase#getHttpClients")
    public void createRoomWithFullCycle(HttpClient httpClient) {
        RoomsClientBuilder builder = getRoomsClientWithConnectionString(httpClient);
        roomsAsyncClient = setupAsyncClient(builder, "createRoomWithFullCycle");
        assertNotNull(roomsAsyncClient);

        Mono<CommunicationRoom> response1 = roomsAsyncClient.createRoom(VALID_FROM, VALID_UNTIL, PARTICIPANTS1);

        StepVerifier.create(response1)
            .assertNext(response -> {
                assertHappyPath(response);
                assertEquals(response.getParticipants().size(), 3);
            }).verifyComplete();
        
        String roomId = response1.block().getRoomId();

        StepVerifier.create(
            roomsAsyncClient.updateRoom(roomId, VALID_FROM, VALID_UNTIL, PARTICIPANTS2))
            .assertNext(response2 -> {
                assertHappyPath(response2);
                assertEquals(response2.getParticipants().size(), 1);
            }).verifyComplete();

        StepVerifier.create(roomsAsyncClient.getRoom(roomId))
            .assertNext(response3 -> {
                assertHappyPath(response3);
                assertEquals(response3.getParticipants().size(), 1);
            }).verifyComplete();

        StepVerifier.create(roomsAsyncClient.deleteRoomWithResponse(roomId))
            .assertNext(response4 -> {
                assertEquals(response4.getStatusCode(), 204);
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
