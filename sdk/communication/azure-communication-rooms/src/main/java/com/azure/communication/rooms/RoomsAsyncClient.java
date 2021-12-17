// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms;

import com.azure.communication.rooms.implementation.AzureCommunicationRoomServiceImpl;
import com.azure.communication.rooms.implementation.RoomsImpl;
import com.azure.communication.rooms.implementation.models.RoomModel;
import com.azure.communication.rooms.implementation.models.CreateRoomRequest;
import com.azure.communication.rooms.implementation.models.CreateRoomResponse;
import com.azure.communication.rooms.implementation.models.UpdateRoomRequest;
import com.azure.communication.rooms.implementation.models.UpdateRoomResponse;
import com.azure.communication.rooms.models.CommunicationRoom;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import java.time.OffsetDateTime;
import java.util.Map;

import reactor.core.publisher.Mono;
import static com.azure.core.util.FluxUtil.monoError;

/**
 * The Async client for create, update, get, delete room of Azure Communciation Room Service.
 */
@ServiceClient(builder = RoomsClientBuilder.class, isAsync = true)
public class RoomsAsyncClient {
    private final RoomsImpl roomsClient;
    private final ClientLogger logger = new ClientLogger(RoomsAsyncClient.class);

    RoomsAsyncClient(AzureCommunicationRoomServiceImpl roomsServiceClient) {
        roomsClient = roomsServiceClient.getRooms();
    }

    /**
     * Create a new Room, input fields are nullable.
     * 
     * @param validFrom The valid starting time point of a room.
     * @param validUntil The valid ending time point of a room.
     * @param participants The participants of a room.
     * @return response for a successful create room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CommunicationRoom> createRoom(OffsetDateTime validFrom, OffsetDateTime validUntil, Map<String, Object> participants) {
        return createRoom(validFrom, validUntil, participants, null);
    }

    Mono<CommunicationRoom> createRoom(OffsetDateTime validFrom, OffsetDateTime validUntil, Map<String, Object> participants, Context context) {
        CreateRoomRequest createRoomRequest = getCreateRoomRequest(validFrom, validUntil, participants);
        context = context == null ? Context.NONE : context;
        try {
            return this.roomsClient
            .createRoomWithResponseAsync(createRoomRequest, context)
            .flatMap((Response<CreateRoomResponse> response) -> {
                return Mono.just(getCommunicationRoomFromResponse(response.getValue().getRoom(), response.getValue().getInvalidParticipants()));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

        /**
     * Create a new Room, input fields are nullable.
     * 
     * @param validFrom The valid starting time point of a room.
     * @param validUntil The valid ending time point of a room.
     * @param participants The participants of a room.
     * @return response for a successful create room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CommunicationRoom>> createRoomWithResponse(OffsetDateTime validFrom, OffsetDateTime validUntil, Map<String, Object> participants) {
        return createRoomWithResponse(validFrom, validUntil, participants, null);
    }

    Mono<Response<CommunicationRoom>> createRoomWithResponse(OffsetDateTime validFrom, OffsetDateTime validUntil, Map<String, Object> participants, Context context) {
        CreateRoomRequest createRoomRequest = getCreateRoomRequest(validFrom, validUntil, participants);
        context = context == null ? Context.NONE : context;
        try {
            return this.roomsClient
            .createRoomWithResponseAsync(createRoomRequest, context)
            .flatMap((Response<CreateRoomResponse> response) -> {
                CommunicationRoom communicationRoom = getCommunicationRoomFromResponse(response.getValue().getRoom(), response.getValue().getInvalidParticipants());
                return Mono.just(new SimpleResponse<CommunicationRoom>(response, communicationRoom));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
    public Mono<CommunicationRoom> updateRoom(String roomId, OffsetDateTime validFrom, OffsetDateTime validUntil, Map<String, Object> participants) {
        return updateRoom(roomId, validFrom, validUntil, participants, null);
    }

    Mono<CommunicationRoom> updateRoom(String roomId, OffsetDateTime validFrom, OffsetDateTime validUntil, Map<String, Object> participants, Context context) {
        UpdateRoomRequest updateRoomRequest = getUpdateRoomRequest(validFrom, validUntil, participants);
        context = context == null ? Context.NONE : context;
        try {
            return this.roomsClient
            .updateRoomWithResponseAsync(roomId, updateRoomRequest, context)
            .flatMap((Response<UpdateRoomResponse> response) -> {
                return Mono.just(getCommunicationRoomFromResponse(response.getValue().getRoom(), response.getValue().getInvalidParticipants()));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }

    }

    /**
     * Update an existing room with response.
     * 
     * @param roomId The room id.
     * @param validFrom The valid starting time point of a room.
     * @param validUntil The valid ending time point of a room.
     * @param participants The participants of a room.
     * @return response for a successful update room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CommunicationRoom>> updateRoomWithResponse(String roomId, OffsetDateTime validFrom, OffsetDateTime validUntil, Map<String, Object> participants) {
        return updateRoomWithResponse(roomId, validFrom, validUntil, participants, null);
    }

    Mono<Response<CommunicationRoom>> updateRoomWithResponse(String roomId, OffsetDateTime validFrom, OffsetDateTime validUntil, Map<String, Object> participants, Context context) {
        UpdateRoomRequest updateRoomRequest = getUpdateRoomRequest(validFrom, validUntil, participants);
        context = context == null ? Context.NONE : context;
        try {
            return this.roomsClient
            .updateRoomWithResponseAsync(roomId, updateRoomRequest, context)
            .flatMap((Response<UpdateRoomResponse> response) -> {
                CommunicationRoom communicationRoom = getCommunicationRoomFromResponse(response.getValue().getRoom(), response.getValue().getInvalidParticipants());
                return Mono.just(new SimpleResponse<CommunicationRoom>(response, communicationRoom));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }

    }

    /**
     * Get an existing room.
     * 
     * @param roomId The room Id.
     * @return The existing room.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CommunicationRoom> getRoom(String roomId) {
        return getRoom(roomId, null);
    }

    Mono<CommunicationRoom> getRoom(String roomId, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            return this.roomsClient
            .getRoomWithResponseAsync(roomId, context)
            .flatMap(
                (Response<RoomModel> response) -> {
                    return Mono.just(getCommunicationRoomFromResponse(response.getValue(), null));
                }
            );
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }

    }

    /**
     * Get an existing room with response.
     * 
     * @param roomId The room Id.
     * @return The existing room.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CommunicationRoom>> getRoomWithResponse(String roomId) {
        return getRoomWithResponse(roomId, null);
    }

    Mono<Response<CommunicationRoom>> getRoomWithResponse(String roomId, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            return this.roomsClient
            .getRoomWithResponseAsync(roomId, context)
            .flatMap(
                (Response<RoomModel> response) -> {
                    CommunicationRoom communicationRoom = getCommunicationRoomFromResponse(response.getValue(), null);
                    return Mono.just(new SimpleResponse<CommunicationRoom>(response, communicationRoom));
                }
            );
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }

    }

    /**
     * Delete a existing room.
     * 
     * @param roomId The room Id.
     * @return The response with status code.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteRoomWithResponse(String roomId) {
        return deleteRoomWithResponse(roomId, null);
    }

    Mono<Response<Void>> deleteRoomWithResponse(String roomId, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            return this.roomsClient
            .deleteRoomWithResponseAsync(roomId, context)
            .flatMap((Response<Void> response) -> {
                return Mono.just(response);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    private CreateRoomRequest getCreateRoomRequest(OffsetDateTime validFrom, OffsetDateTime validUntil, Map<String, Object> participants) {
        CreateRoomRequest createRoomRequest = new CreateRoomRequest();
        createRoomRequest.setValidFrom(validFrom)
            .setValidUntil(validUntil)
            .setParticipants(participants);
        return createRoomRequest;
    }

    private UpdateRoomRequest getUpdateRoomRequest(OffsetDateTime validFrom, OffsetDateTime validUntil, Map<String, Object> participants) {
        UpdateRoomRequest updateRoomRequest = new UpdateRoomRequest();
        updateRoomRequest.setValidFrom(validFrom)
            .setValidUntil(validUntil)
            .setParticipants(participants);
        return updateRoomRequest;
    }

    private CommunicationRoom getCommunicationRoomFromResponse(RoomModel room, Map<String, Object> invalidParticipant) {
        return new CommunicationRoom(room.getId(), 
            room.getValidFrom(),
            room.getValidUntil(),
            room.getCreatedDateTime(),
            room.getParticipants(),
            invalidParticipant);
    }
}
