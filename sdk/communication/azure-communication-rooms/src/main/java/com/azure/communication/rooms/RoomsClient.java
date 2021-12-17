// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms;

import java.time.OffsetDateTime;
import java.util.Map;
import com.azure.communication.rooms.models.CommunicationRoom;
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
     * @param validFrom The valid starting time point of a room.
     * @param validUntil The valid ending time point of a room.
     * @param participants The participants of a room.
     * @return response for a successful create room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CommunicationRoom createRoom(OffsetDateTime validFrom, OffsetDateTime validUntil, Map<String, Object> participants) {
        return roomsAsyncClient.createRoom(validFrom, validUntil, participants).block();
    }

    /**
     * Create a new Room with response.
     * 
     * @param validFrom The valid starting time point of a room.
     * @param validUntil The valid ending time point of a room.
     * @param participants The participants of a room.
     * @param context The context of key value pairs for http request.
     * @return response for a successful create room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CommunicationRoom> createRoomWithResponse(OffsetDateTime validFrom, OffsetDateTime validUntil, Map<String, Object> participants, Context context) {
        return roomsAsyncClient.createRoomWithResponse(validFrom, validUntil, participants, context).block();
    }

    /**
     * Update an existing Room.
     * 
     * @param roomId The room id.
     * @param validFrom The valid starting time point of a room.
     * @param validUntil The valid ending time point of a room.
     * @param participants The participants of a room.
     * @return response for a successful update room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CommunicationRoom updateRoom(String roomId, OffsetDateTime validFrom, OffsetDateTime validUntil, Map<String, Object> participants) {
        return roomsAsyncClient.updateRoom(roomId, validFrom, validUntil, participants).block();
    }

    /**
     * Update an existing Room with response.
     * 
     * @param roomId The room id.
     * @param validFrom The valid starting time point of a room.
     * @param validUntil The valid ending time point of a room.
     * @param participants The participants of a room.
     * @param context The context of key value pairs for http request.
     * @return response for a successful update room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CommunicationRoom> updateRoomWithResponse(String roomId, OffsetDateTime validFrom, OffsetDateTime validUntil, Map<String, Object> participants, Context context) {
        return roomsAsyncClient.updateRoomWithResponse(roomId, validFrom, validUntil, participants, context).block();
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
}
