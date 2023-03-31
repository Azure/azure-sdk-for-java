// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms;

import com.azure.communication.common.CommunicationIdentifier;

import com.azure.communication.rooms.implementation.models.RoomsCollection;
import com.azure.communication.rooms.implementation.models.ParticipantProperties;
import com.azure.communication.rooms.implementation.models.RoomModel;
import com.azure.communication.rooms.models.CommunicationRoom;
import com.azure.communication.rooms.models.InvitedRoomParticipant;
import com.azure.communication.rooms.models.RemoveParticipantsResult;
import com.azure.communication.rooms.models.RoomJoinPolicy;
import com.azure.communication.rooms.models.RoomParticipant;
import com.azure.communication.rooms.models.UpsertParticipantsResult;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Client for rooms operations with Azure Communication Rooms Service
 */
@ServiceClient(builder = RoomsClientBuilder.class)
public final class RoomsClient {
    private final RoomsAsyncClient roomsAsyncClient;

    RoomsClient(RoomsAsyncClient roomsAsyncClient) {
        this.roomsAsyncClient = roomsAsyncClient;
    }

    /**
     * Create a new room.
     *
     * @param validFrom the validFrom value to set.
     * @param validUntil the validUntil value to set.
     * @param participants the participants value to set.
     * @return response for a successful create room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CommunicationRoom createRoom(OffsetDateTime validFrom, OffsetDateTime validUntil, List<InvitedRoomParticipant> participants) {
        return roomsAsyncClient.createRoom(validFrom, validUntil, participants).block();
    }

    /**
     * Create a new Room with response.
     *
     * @param validFrom the validFrom value to set.
     * @param validUntil the validUntil value to set.
     * @param participants the participants value to set.
     * @param context The context of key value pairs for http request.
     * @return response for a successful create room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CommunicationRoom> createRoomWithResponse(OffsetDateTime validFrom, OffsetDateTime validUntil, List<InvitedRoomParticipant> participants, Context context) {
        return roomsAsyncClient.createRoomWithResponse(validFrom, validUntil, participants, context).block();
    }

    /**
     * Update an existing Room.
     *
     * @param roomId The room id.
     * @param validFrom the validFrom value to set.
     * @param validUntil the validUntil value to set.
     * @return response for a successful update room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CommunicationRoom updateRoom(String roomId, OffsetDateTime validFrom, OffsetDateTime validUntil) {
        return roomsAsyncClient.updateRoom(roomId, validFrom, validUntil).block();
    }

    /**
     * Update an existing Room with response.
     *
     * @param roomId The room id.
     * @param validFrom the validFrom value to set.
     * @param validUntil the validUntil value to set.
     * @param context The context of key value pairs for http request.
     * @return response for a successful update room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CommunicationRoom> updateRoomWithResponse(String roomId, OffsetDateTime validFrom, OffsetDateTime validUntil, Context context) {
        return roomsAsyncClient.updateRoomWithResponse(roomId, validFrom, validUntil, context).block();
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
     * @return Response with status code only.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void deleteRoom(String roomId) {
        return roomsAsyncClient.deleteRoom(roomId).block();
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
     * Upsert participants to an existing Room.
     *
     * @param roomId The room id.
     * @param participants The participants list.
     * @return response for a successful upsert participants room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public UpsertParticipantsResult upsertParticipants(String roomId, List<InvitedRoomParticipant> participants) {
        return roomsAsyncClient.upsertParticipants(roomId, participants).block();
    }

    /**
     * Upsert participants to an existing Room with response
     *
     * @param roomId The room id.
     * @param participants The participants list.
     * @return response for a successful upsert participants room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<UpsertParticipantsResult> upsertParticipantsWithResponse(String roomId, List<InvitedRoomParticipant> participants) {
        return roomsAsyncClient.upsertParticipantsWithResponse(roomId, participants).block();
    }

    /**
     * Remove participants to an existing Room.
     *
     * @param roomId The room id.
     * @param identifiers The communication identifiers list.
     * @return response for a successful remove participants room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public RemoveParticipantsResult removeParticipants(String roomId, List<CommunicationIdentifier> identifiers) {
        return roomsAsyncClient.removeParticipants(roomId, identifiers).block();
    }

    /**
     * Remove participants to an existing Room with response
     *
     * @param roomId The room id.
     * @param identifiers The communication identifiers list.
     * @return response for a successful remove participants room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RemoveParticipantsResult> removeParticipantsWithResponse(String roomId, List<CommunicationIdentifier> identifiers) {
        return roomsAsyncClient.removeParticipantsWithResponse(roomId, identifiers).block();
    }

    /**
     * List Room participants.
     *
     * @param roomId The room id.
     * @return Room Participants List
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PagedIterable<RoomParticipant> getParticipants(String roomId) {
        return new PagedIterable<>(roomsAsyncClient.listParticipants(roomId));
    }

}
