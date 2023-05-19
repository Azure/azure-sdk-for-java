// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms;

import com.azure.communication.common.CommunicationIdentifier;

import com.azure.communication.rooms.models.CommunicationRoom;
import com.azure.communication.rooms.models.CreateRoomOptions;
import com.azure.communication.rooms.models.RemoveParticipantsResult;
import com.azure.communication.rooms.models.RoomParticipant;
import com.azure.communication.rooms.models.UpdateRoomOptions;
import com.azure.communication.rooms.models.AddOrUpdateParticipantsResult;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.http.rest.PagedIterable;

/**
 * Client for Rooms operations of Azure Communication Room Service
 *
 * <p>
 * <strong>Instantiating a Room Client</strong>
 * </p>
 *
 * <!-- src_embed readme-sample-createRoomsClientUsingAzureKeyCredential
 * -->
 *
 * <pre>
 * RoomsClient roomsClient = new RoomsClientBuilder()
 *      .endpoint&#40;endpoint&#41;
 *      .credential&#40;azureKeyCredential&#41;
 *      .buildClient&#40;&#41;;
 * </pre>
 *
 * <!-- end readme-sample-createRoomsClientUsingAzureKeyCredential -->
 *
 * @see RoomsClientBuilder
 *
 */
@ServiceClient(builder = RoomsClientBuilder.class)
public final class RoomsClient {
    private final RoomsAsyncClient roomsAsyncClient;

    RoomsClient(RoomsAsyncClient roomsAsyncClient) {
        this.roomsAsyncClient = roomsAsyncClient;
    }

    /**
     * Create a new room. Input field is nullable.
     *
     * @param createRoomOptions the create room options.
     * @return response for a successful create room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CommunicationRoom createRoom(CreateRoomOptions createRoomOptions) {
        return roomsAsyncClient.createRoom(createRoomOptions).block();
    }

    /**
     * Create a new Room with response.
     *
     * @param createRoomOptions the create room options.
     * @param context The context of key value pairs for http request.
     * @return response for a successful create room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CommunicationRoom> createRoomWithResponse(CreateRoomOptions createRoomOptions, Context context) {
        return roomsAsyncClient.createRoomWithResponse(createRoomOptions, context).block();
    }

    /**
     * Update an existing Room.
     *
     * @param roomId The room Id.
     * @param updateRoomOptions the update room options.
     * @return response for a successful update room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CommunicationRoom updateRoom(String roomId, UpdateRoomOptions updateRoomOptions) {
        return roomsAsyncClient.updateRoom(roomId, updateRoomOptions).block();
    }

    /**
     * Update an existing Room with response.
     *
     * @param roomId The room Id.
     * @param updateRoomOptions the update room options.
     * @param context The context of key value pairs for http request.
     * @return response for a successful update room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CommunicationRoom> updateRoomWithResponse(String roomId, UpdateRoomOptions updateRoomOptions, Context context) {
        return roomsAsyncClient.updateRoomWithResponse(roomId, updateRoomOptions, context).block();
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
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteRoom(String roomId) {
        roomsAsyncClient.deleteRoom(roomId).block();
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
     * Lists all rooms.
     *
     * @return The existing rooms.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PagedIterable<CommunicationRoom> listRooms() {
        return new PagedIterable<>(roomsAsyncClient.listRooms());
    }

    /**
     * Lists all rooms.
     *
     * @param context The context of key value pairs for http request.
     * @return The existing rooms.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PagedIterable<CommunicationRoom> listRooms(Context context) {
        return new PagedIterable<>(roomsAsyncClient.listRooms(context));
    }

    /**
     * addOrUpdate participants to an existing Room.
     *
     * @param roomId The room id.
     * @param participants The participants list.
     * @return response for a successful addOrUpdate participants room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AddOrUpdateParticipantsResult addOrUpdateParticipants(String roomId, Iterable<RoomParticipant> participants) {
        return roomsAsyncClient.addOrUpdateParticipants(roomId, participants).block();
    }

    /**
     * addOrUpdate participants to an existing Room with response
     *
     * @param roomId The room id.
     * @param participants The participants list.
     * @param context The context of key value pairs for http request.
     * @return response for a successful addOrUpdate participants room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AddOrUpdateParticipantsResult> addOrUpdateParticipantsWithResponse(String roomId, Iterable<RoomParticipant> participants, Context context) {
        return roomsAsyncClient.addOrUpdateParticipantsWithResponse(roomId, participants, context).block();
    }

    /**
     * Remove participants to an existing Room.
     *
     * @param roomId The room id.
     * @param identifiers The communication identifiers list.
     * @return response for a successful remove participants room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RemoveParticipantsResult removeParticipants(String roomId, Iterable<CommunicationIdentifier> identifiers) {
        return roomsAsyncClient.removeParticipants(roomId, identifiers).block();
    }

    /**
     * Remove participants to an existing Room with response
     *
     * @param roomId The room id.
     * @param identifiers The communication identifiers list.
     * @param context The context of key value pairs for http request.
     * @return response for a successful remove participants room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RemoveParticipantsResult> removeParticipantsWithResponse(String roomId, Iterable<CommunicationIdentifier> identifiers, Context context) {
        return roomsAsyncClient.removeParticipantsWithResponse(roomId, identifiers, context).block();
    }

    /**
     * List Room participants.
     *
     * @param roomId The room id.
     * @return Room Participants List
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PagedIterable<RoomParticipant> listParticipants(String roomId) {
        return new PagedIterable<>(roomsAsyncClient.listParticipants(roomId));
    }

    /**
     * List Room participants.
     *
     * @param roomId The room id.
     * @param context The context of key value pairs for http request.
     * @return Room Participants List
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PagedIterable<RoomParticipant> listParticipants(String roomId, Context context) {
        return new PagedIterable<>(roomsAsyncClient.listParticipants(roomId, context));
    }
}
