// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms;

import com.azure.communication.rooms.implementation.AzureCommunicationRoomServiceImpl;
import com.azure.communication.rooms.implementation.RoomsImpl;
import com.azure.communication.rooms.implementation.models.RoomModel;
import com.azure.communication.rooms.implementation.models.UpdateRoomRequest;
import com.azure.communication.rooms.implementation.models.CreateRoomRequest;
import com.azure.communication.rooms.implementation.models.CreateRoomResponse;
import com.azure.communication.rooms.implementation.models.UpdateRoomResponse;
import com.azure.communication.rooms.models.CommunicationRoom;
import com.azure.communication.rooms.models.RoomParticipant;
import com.azure.communication.rooms.models.RoomRequest;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

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
     * @param request the room request.
     * @return response for a successful create room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CommunicationRoom> createRoom(RoomRequest request) {
        return createRoom(request, null);
    }

    Mono<CommunicationRoom> createRoom(RoomRequest request, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            return this.roomsClient
            .createRoomWithResponseAsync(toCreateRoomRequest(request), context)
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
     * @param request the room request.
     * @return response for a successful create room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CommunicationRoom>> createRoomWithResponse(RoomRequest request) {
        return createRoomWithResponse(request, null);
    }

    Mono<Response<CommunicationRoom>> createRoomWithResponse(RoomRequest request, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            return this.roomsClient
            .createRoomWithResponseAsync(toCreateRoomRequest(request), context)
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
     * @param request The room request.
     * @return response for a successful update room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CommunicationRoom> updateRoom(String roomId, RoomRequest request) {
        return updateRoom(roomId, request, null);
    }

    Mono<CommunicationRoom> updateRoom(String roomId, RoomRequest request, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            return this.roomsClient
            .updateRoomWithResponseAsync(roomId, toUpdateRoomRequest(request), context)
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
     * @param request The room request.
     * @return response for a successful update room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CommunicationRoom>> updateRoomWithResponse(String roomId, RoomRequest request) {
        return updateRoomWithResponse(roomId, request, null);
    }

    Mono<Response<CommunicationRoom>> updateRoomWithResponse(String roomId, RoomRequest request, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            return this.roomsClient
            .updateRoomWithResponseAsync(roomId, toUpdateRoomRequest(request), context)
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

    /**
     * Add participants to an existing Room.
     * 
     * @param roomId The room id.
     * @param participants The participants list.
     * @return response for a successful update room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CommunicationRoom> addParticipants(String roomId, Set<String> participants) {
        return addParticipants(roomId, participants, null);
    }

    Mono<CommunicationRoom> addParticipants(String roomId, Set<String> participants, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            RoomRequest request = getRoomRequest(participants, new RoomParticipant());
            return this.roomsClient
            .updateRoomWithResponseAsync(roomId, toUpdateRoomRequest(request), context)
            .flatMap((Response<UpdateRoomResponse> response) -> {
                return Mono.just(getCommunicationRoomFromResponse(response.getValue().getRoom(), response.getValue().getInvalidParticipants()));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }

    }

    /**
     * Add participants to an existing room with response.
     * 
     * @param roomId The room id.
     * @param participants The participant list.
     * @return response for a successful update room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CommunicationRoom>> addParticipantsWithResponse(String roomId, Set<String> participants) {
        return addParticipantsWithResponse(roomId, participants, null);
    }

    Mono<Response<CommunicationRoom>> addParticipantsWithResponse(String roomId, Set<String> participants, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            RoomRequest request = getRoomRequest(participants, new RoomParticipant());
            return this.roomsClient
            .updateRoomWithResponseAsync(roomId, toUpdateRoomRequest(request), context)
            .flatMap((Response<UpdateRoomResponse> response) -> {
                CommunicationRoom communicationRoom = getCommunicationRoomFromResponse(response.getValue().getRoom(), response.getValue().getInvalidParticipants());
                return Mono.just(new SimpleResponse<CommunicationRoom>(response, communicationRoom));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Remove participants to an existing Room.
     * 
     * @param roomId The room id.
     * @param participants The participants list.
     * @return response for a successful update room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CommunicationRoom> removeParticipants(String roomId, Set<String> participants) {
        return removeParticipants(roomId, participants, null);
    }

    Mono<CommunicationRoom> removeParticipants(String roomId, Set<String> participants, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            RoomRequest request = getRoomRequest(participants, null);
            return this.roomsClient
            .updateRoomWithResponseAsync(roomId, toUpdateRoomRequest(request), context)
            .flatMap((Response<UpdateRoomResponse> response) -> {
                return Mono.just(getCommunicationRoomFromResponse(response.getValue().getRoom(), response.getValue().getInvalidParticipants()));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }

    }

    /**
     * Remove participants to an existing room with response.
     * 
     * @param roomId The room id.
     * @param participants The participant list.
     * @return response for a successful update room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CommunicationRoom>> removeParticipantsWithResponse(String roomId, Set<String> participants) {
        return removeParticipantsWithResponse(roomId, participants, null);
    }

    Mono<Response<CommunicationRoom>> removeParticipantsWithResponse(String roomId, Set<String> participants, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            RoomRequest request = getRoomRequest(participants, null);
            return this.roomsClient
            .updateRoomWithResponseAsync(roomId, toUpdateRoomRequest(request), context)
            .flatMap((Response<UpdateRoomResponse> response) -> {
                CommunicationRoom communicationRoom = getCommunicationRoomFromResponse(response.getValue().getRoom(), response.getValue().getInvalidParticipants());
                return Mono.just(new SimpleResponse<CommunicationRoom>(response, communicationRoom));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    private RoomRequest getRoomRequest(Set<String> participants, RoomParticipant roomParticipant) {
        Map<String, Object> participantMap = new HashMap<>();
        for (String participant : participants) {
            participantMap.put(participant, roomParticipant);
        }
        RoomRequest request = new RoomRequest();
        request.setParticipants(participantMap);
        return request;
    }

    private CommunicationRoom getCommunicationRoomFromResponse(RoomModel room, Map<String, Object> invalidParticipant) {
        return new CommunicationRoom(room.getId(), 
            room.getValidFrom(),
            room.getValidUntil(),
            room.getCreatedDateTime(),
            room.getParticipants(),
            invalidParticipant);
    }

    /**
     * Translate to create room request.
     *
     * @return The create room request.
     */
    private CreateRoomRequest toCreateRoomRequest(RoomRequest request) {
        CreateRoomRequest createRoomRequest = new CreateRoomRequest();
        if (request.getValidFrom() != null) {
            createRoomRequest.setValidFrom(request.getValidFrom());
        }

        if (request.getValidUntil() != null) {
            createRoomRequest.setValidUntil(request.getValidUntil());
        }

        if (request.getParticipants() != null) {
            createRoomRequest.setParticipants(request.getParticipants());
        }

        return createRoomRequest;
    }

    /**
     * Translate to update room request.
     *
     * @return The update room request.
     */
    private UpdateRoomRequest toUpdateRoomRequest(RoomRequest request) {
        UpdateRoomRequest updateRoomRequest = new UpdateRoomRequest();

        if (request.getValidFrom() != null) {
            updateRoomRequest.setValidFrom(request.getValidFrom());
        }

        if (request.getValidUntil() != null) {
            updateRoomRequest.setValidUntil(request.getValidUntil());
        }

        if (request.getParticipants() != null) {
            updateRoomRequest.setParticipants(request.getParticipants());
        }

        return updateRoomRequest;
    }
}
