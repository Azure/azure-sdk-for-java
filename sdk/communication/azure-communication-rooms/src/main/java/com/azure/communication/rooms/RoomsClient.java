// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms;

import java.util.Set;

import com.azure.communication.rooms.models.CommunicationRoom;
import com.azure.communication.rooms.models.RoomRequest;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

/**
 * Client for rooms operations with Azure Communication Rooms Service
 */
@ServiceClient(builder = RoomsClientBuilder.class)
public class RoomsClient {
    private final RoomsAsyncClient roomsAsyncClient;

    RoomsClient(RoomsAsyncClient roomsAsyncClient) {
        this.roomsAsyncClient = roomsAsyncClient;
    }

    /**
     * Create a new room.
     * 
     * @param request The room request.
     * @return response for a successful create room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CommunicationRoom createRoom(RoomRequest request) {
        return roomsAsyncClient.createRoom(request).block();
    }

    /**
     * Create a new Room with response.
     * 
     * @param request The room request.
     * @param context The context of key value pairs for http request.
     * @return response for a successful create room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CommunicationRoom> createRoomWithResponse(RoomRequest request, Context context) {
        return roomsAsyncClient.createRoomWithResponse(request, context).block();
    }

    /**
     * Update an existing Room.
     * 
     * @param roomId The room id.
     * @param request The room request.
     * @return response for a successful update room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CommunicationRoom updateRoom(String roomId, RoomRequest request) {
        return roomsAsyncClient.updateRoom(roomId, request).block();
    }

    /**
     * Update an existing Room with response.
     * 
     * @param roomId The room id.
     * @param request The room request.
     * @param context The context of key value pairs for http request.
     * @return response for a successful update room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CommunicationRoom> updateRoomWithResponse(String roomId, RoomRequest request, Context context) {
        return roomsAsyncClient.updateRoomWithResponse(roomId, request, context).block();
    }

    /**
     * Get an existing room.
     * 
     * @param roomId The room id.
     * @return The existing room.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CommunicationRoom getRoom(String roomId) {
        return roomsAsyncClient.getRoom(roomId).block();
    }

    /**
     * Get an existing room with response.
     * 
     * @param roomId The room id.
     * @param context The context of key value pairs for http request.
     * @return The existing room.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CommunicationRoom> getRoomWithResponse(String roomId, Context context) {
        return roomsAsyncClient.getRoomWithResponse(roomId, context).block();
    }


    /**
     * Delete an existing room.
     * 
     * @param roomId The room Id.
     * @param context The context of key value pairs for http request.
     * @return Response with status code only.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> deleteRoomWithResponse(String roomId, Context context) {
        return roomsAsyncClient.deleteRoomWithResponse(roomId, context).block();
    }

    /**
     * Add participants to an existing room.
     * 
     * @param roomId The room id.
     * @param participants The participants to add.
     * @return The existing room.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CommunicationRoom addParticipants(String roomId, Set<String> participants) {
        return roomsAsyncClient.addParticipants(roomId, participants).block();
    }

    /**
     * Add participants to an existing room with response.
     * 
     * @param roomId The room id.
     * @param participants The participants to add.
     * @param context The context of key value pairs for http request.
     * @return The existing room.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CommunicationRoom> addParticipantsWithResponse(String roomId, Set<String> participants, Context context) {
        return roomsAsyncClient.addParticipantsWithResponse(roomId, participants, context).block();
    }

    /**
     * Remove participants to an existing room.
     * 
     * @param roomId The room id.
     * @param participants The participants to remove.
     * @return The existing room.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CommunicationRoom removeParticipants(String roomId, Set<String> participants) {
        return roomsAsyncClient.removeParticipants(roomId, participants).block();
    }

    /**
     * Remove participants to an existing room with response.
     * 
     * @param roomId The room id.
     * @param participants The participants to remove.
     * @param context The context of key value pairs for http request.
     * @return The existing room.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CommunicationRoom> removeParticipantsWithResponse(String roomId, Set<String> participants, Context context) {
        return roomsAsyncClient.removeParticipantsWithResponse(roomId, participants, context).block();
    }
}
