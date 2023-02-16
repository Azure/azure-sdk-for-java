// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms;

import com.azure.communication.rooms.implementation.AzureCommunicationRoomServiceImpl;

import com.azure.communication.rooms.implementation.RoomsImpl;
import com.azure.communication.rooms.implementation.converters.RoomParticipantConverter;
import com.azure.communication.rooms.implementation.converters.RoomsErrorConverter;
import com.azure.communication.rooms.implementation.converters.ParticipantsCollectionConverter;
import com.azure.communication.rooms.implementation.models.RoomModel;
import com.azure.communication.rooms.models.CommunicationRoom;
import com.azure.communication.rooms.models.RoomJoinPolicy;
import com.azure.communication.rooms.models.RoomParticipant;
import com.azure.communication.rooms.models.RoomsError;
import com.azure.communication.rooms.models.RoomsErrorResponseException;
import com.azure.communication.rooms.implementation.models.UpdateParticipantsRequest;
import com.azure.communication.rooms.implementation.models.UpdateRoomRequest;
import com.azure.communication.rooms.implementation.models.AddParticipantsRequest;
import com.azure.communication.rooms.implementation.models.CommunicationErrorResponseException;
import com.azure.communication.rooms.implementation.models.CreateRoomRequest;
import com.azure.communication.rooms.implementation.models.RemoveParticipantsRequest;
import com.azure.communication.rooms.models.ParticipantsCollection;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import reactor.core.publisher.Mono;

import static com.azure.core.util.FluxUtil.monoError;

/**
 * The Async client for create, update, get, delete room of Azure Communication Room Service.
 */
@ServiceClient(builder = RoomsClientBuilder.class, isAsync = true)
public final class RoomsAsyncClient {
    private final RoomsImpl roomsClient;
    private final ClientLogger logger = new ClientLogger(RoomsAsyncClient.class);

    RoomsAsyncClient(AzureCommunicationRoomServiceImpl roomsServiceClient) {
        roomsClient = roomsServiceClient.getRooms();
    }

    /**
     * Create a new Room, input fields are nullable.
     *
     * @param validFrom the validFrom value to set.
     * @param validUntil the validUntil value to set.
     * @param roomJoinPolicy the roomJoinPolicy value to set.
     * @param participants the participants value to set.
     * @return response for a successful create room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CommunicationRoom> createRoom(OffsetDateTime validFrom, OffsetDateTime validUntil, RoomJoinPolicy roomJoinPolicy, List<RoomParticipant> participants) {
        return createRoom(validFrom, validUntil, roomJoinPolicy, participants, null);
    }

    Mono<CommunicationRoom> createRoom(OffsetDateTime validFrom, OffsetDateTime validUntil, RoomJoinPolicy roomJoinPolicy, List<RoomParticipant> participants, Context context) {
        context = context == null ? Context.NONE : context;

        try {
            return this.roomsClient
            .createRoomWithResponseAsync(toCreateRoomRequest(validFrom, validUntil, roomJoinPolicy, participants), context)
            .flatMap((Response<RoomModel> response) -> {
                return Mono.just(getCommunicationRoomFromResponse(response.getValue()));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Create a new Room, input fields are nullable.
     *
     * @param validFrom the validFrom value to set.
     * @param validUntil the validUntil value to set.
     * @param roomJoinPolicy the roomJoinPolicy value to set.
     * @param participants the participants value to set.
     * @return response for a successful create room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CommunicationRoom>> createRoomWithResponse(OffsetDateTime validFrom, OffsetDateTime validUntil, RoomJoinPolicy roomJoinPolicy, List<RoomParticipant> participants) {
        return createRoomWithResponse(validFrom, validUntil, roomJoinPolicy, participants, null);
    }

    Mono<Response<CommunicationRoom>> createRoomWithResponse(OffsetDateTime validFrom, OffsetDateTime validUntil, RoomJoinPolicy roomJoinPolicy, List<RoomParticipant> participants, Context context) {
        context = context == null ? Context.NONE : context;

        try {
            return this.roomsClient
            .createRoomWithResponseAsync(toCreateRoomRequest(validFrom, validUntil, roomJoinPolicy, participants), context)
            .flatMap((Response<RoomModel> response) -> {
                CommunicationRoom communicationRoom = getCommunicationRoomFromResponse(response.getValue());
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
     * @param validFrom the validFrom value to set.
     * @param validUntil the validUntil value to set.
     * @param roomJoinPolicy the roomJoinPolicy value to set.
     * @param participants the participants value to set.
     * @return response for a successful update room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CommunicationRoom> updateRoom(String roomId, OffsetDateTime validFrom, OffsetDateTime validUntil, RoomJoinPolicy roomJoinPolicy, List<RoomParticipant> participants) {
        return updateRoom(roomId, validFrom, validUntil, roomJoinPolicy, participants, null);
    }

    Mono<CommunicationRoom> updateRoom(String roomId, OffsetDateTime validFrom, OffsetDateTime validUntil, RoomJoinPolicy roomJoinPolicy, List<RoomParticipant> participants, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            return this.roomsClient
            .updateRoomWithResponseAsync(roomId, toUpdateRoomRequest(validFrom, validUntil, roomJoinPolicy, participants), context)
            .flatMap((Response<RoomModel> response) -> {
                return Mono.just(getCommunicationRoomFromResponse(response.getValue()));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Update an existing room with response.
     *
     * @param roomId The room id.
     * @param validFrom the validFrom value to set.
     * @param validUntil the validUntil value to set.
     * @param roomJoinPolicy the roomJoinPolicy value to set.
     * @param participants the participants value to set.
     * @return response for a successful update room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CommunicationRoom>> updateRoomWithResponse(String roomId, OffsetDateTime validFrom, OffsetDateTime validUntil, RoomJoinPolicy roomJoinPolicy, List<RoomParticipant> participants) {
        return updateRoomWithResponse(roomId, validFrom, validUntil, roomJoinPolicy, participants, null);
    }

    Mono<Response<CommunicationRoom>> updateRoomWithResponse(String roomId, OffsetDateTime validFrom, OffsetDateTime validUntil, RoomJoinPolicy roomJoinPolicy, List<RoomParticipant> participants, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            return this.roomsClient
            .updateRoomWithResponseAsync(roomId, toUpdateRoomRequest(validFrom, validUntil, roomJoinPolicy, participants), context)
            .flatMap((Response<RoomModel> response) -> {
                CommunicationRoom communicationRoom = getCommunicationRoomFromResponse(response.getValue());
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
                    return Mono.just(getCommunicationRoomFromResponse(response.getValue()));
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
                    CommunicationRoom communicationRoom = getCommunicationRoomFromResponse(response.getValue());
                    return Mono.just(new SimpleResponse<CommunicationRoom>(response, communicationRoom));
                }
            );
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    
    /**
     * Delete an existing room.
     *
     * @param roomId The room Id.
     * @return The response with status code.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteRoom(String roomId) {
        return deleteRoom(roomId, null);
    }

    Mono<Void> deleteRoom(String roomId, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            return this.roomsClient
            .deleteRoomWithResponseAsync(roomId, context)
            .flatMap((Response<Void> response) -> {
                return Mono.empty();
            });
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
     * @return response for a successful add participants room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ParticipantsCollection> addParticipants(String roomId, List<RoomParticipant> participants) {
        return addParticipants(roomId, participants, null);
    }

    Mono<ParticipantsCollection> addParticipants(String roomId, List<RoomParticipant> participants, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            Objects.requireNonNull(participants, "'participants' cannot be null.");
            Objects.requireNonNull(roomId, "'roomId' cannot be null.");
            return this.roomsClient
            .addParticipantsWithResponseAsync(roomId, toAddParticipantsRequest(participants), context)
            .map(result -> new SimpleResponse<ParticipantsCollection>(
                result, ParticipantsCollectionConverter.convert(result.getValue())))
            .flatMap((Response<ParticipantsCollection> response) -> {
                return Mono.just(response.getValue());
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
     * @return response for a successful add participants to room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ParticipantsCollection>> addParticipantsWithResponse(String roomId, List<RoomParticipant> participants) {
        return addParticipantsWithResponse(roomId, participants, null);
    }

    Mono<Response<ParticipantsCollection>> addParticipantsWithResponse(String roomId, List<RoomParticipant> participants, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            return this.roomsClient
            .addParticipantsWithResponseAsync(roomId, toAddParticipantsRequest(participants), context)
            .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e))
            .map(result -> new SimpleResponse<ParticipantsCollection>(
                result, ParticipantsCollectionConverter.convert(result.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Update participants to an existing Room.
     *
     * @param roomId The room id.
     * @param participants The participants list.
     * @return response for a successful add participants room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ParticipantsCollection> updateParticipants(String roomId, List<RoomParticipant> participants) {
        return updateParticipants(roomId, participants, null);
    }

    Mono<ParticipantsCollection> updateParticipants(String roomId, List<RoomParticipant> participants, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            Objects.requireNonNull(participants, "'participants' cannot be null.");
            Objects.requireNonNull(roomId, "'roomId' cannot be null.");
            return this.roomsClient
            .updateParticipantsWithResponseAsync(roomId, toUpdateParticipantsRequest(participants), context)
            .map(result -> new SimpleResponse<ParticipantsCollection>(
                result, ParticipantsCollectionConverter.convert(result.getValue())))
            .flatMap((Response<ParticipantsCollection> response) -> {
                return Mono.just(response.getValue());
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Update participants to an existing room with response.
     *
     * @param roomId The room id.
     * @param participants The participant list.
     * @return response for a successful update participants request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ParticipantsCollection>> updateParticipantsWithResponse(String roomId, List<RoomParticipant> participants) {
        return updateParticipantsWithResponse(roomId, participants, null);
    }

    Mono<Response<ParticipantsCollection>> updateParticipantsWithResponse(String roomId, List<RoomParticipant> participants, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            return this.roomsClient
            .updateParticipantsWithResponseAsync(roomId, toUpdateParticipantsRequest(participants), context)
            .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e))
            .map(result -> new SimpleResponse<ParticipantsCollection>(
                result, ParticipantsCollectionConverter.convert(result.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Remove participants from an existing Room.
     *
     * @param roomId The room id.
     * @param participants The participants list.
     * @return response for a successful remove  participants room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ParticipantsCollection> removeParticipants(String roomId, List<RoomParticipant> participants) {
        return removeParticipants(roomId, participants, null);
    }

    Mono<ParticipantsCollection> removeParticipants(String roomId, List<RoomParticipant> participants, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            Objects.requireNonNull(participants, "'participants' cannot be null.");
            Objects.requireNonNull(roomId, "'roomId' cannot be null.");
            return this.roomsClient
            .removeParticipantsWithResponseAsync(roomId, toRemoveParticipantsRequest(participants), context)
            .map(result -> new SimpleResponse<ParticipantsCollection>(
                result, ParticipantsCollectionConverter.convert(result.getValue())))
            .flatMap((Response<ParticipantsCollection> response) -> {
                return Mono.just(response.getValue());
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
     * @return response for a successful remove participants request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ParticipantsCollection>> removeParticipantsWithResponse(String roomId, List<RoomParticipant> participants) {
        return removeParticipantsWithResponse(roomId, participants, null);
    }

    Mono<Response<ParticipantsCollection>>  removeParticipantsWithResponse(String roomId, List<RoomParticipant> participants, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            return this.roomsClient
            .removeParticipantsWithResponseAsync(roomId, toRemoveParticipantsRequest(participants), context)
            .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e))
            .map(result -> new SimpleResponse<ParticipantsCollection>(
                result, ParticipantsCollectionConverter.convert(result.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get a room participants.
     *
     * @param roomId The room Id.
     * @return The existing room.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ParticipantsCollection> getParticipants(String roomId) {
        return getParticipants(roomId, null);
    }

    Mono<ParticipantsCollection> getParticipants(String roomId, Context context) {

        context = context == null ? Context.NONE : context;
        try {
            Objects.requireNonNull(roomId, "'roomId' cannot be null.");
            return this.roomsClient
            .getParticipantsWithResponseAsync(roomId, context)
            .map(result -> new SimpleResponse<ParticipantsCollection>(
                result, ParticipantsCollectionConverter.convert(result.getValue())))
            .flatMap((Response<ParticipantsCollection> response) -> {
                return Mono.just(response.getValue());
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get an existing room participants
     *
     * @param roomId The room Id.
     * @return The existing room.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ParticipantsCollection>> getParticipantsWithResponse(String roomId) {
        return getParticipantsWithResponse(roomId, null);
    }

    Mono<Response<ParticipantsCollection>> getParticipantsWithResponse(String roomId, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            return this.roomsClient
            .getParticipantsWithResponseAsync(roomId, context)
            .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e))
            .map(result -> new SimpleResponse<ParticipantsCollection>(
                result, ParticipantsCollectionConverter.convert(result.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    private CommunicationRoom getCommunicationRoomFromResponse(RoomModel room) {
        List<com.azure.communication.rooms.models.RoomParticipant> roomParticipants = new ArrayList<>();

        if (room.getParticipants() != null) {
            roomParticipants = room.getParticipants()
                .stream()
                .map((participant) -> RoomParticipantConverter.convert(participant))
                .collect(Collectors.toList());
        }

        return new CommunicationRoom(room.getId(),
            room.getValidFrom(),
            room.getValidUntil(),
            room.getRoomJoinPolicy(),
            room.getCreatedDateTime(),
            roomParticipants);
    }

    /**
     * Translate to create room request.
     *
     * @return The create room request.
     */
    private CreateRoomRequest toCreateRoomRequest(OffsetDateTime validFrom, OffsetDateTime validUntil, RoomJoinPolicy roomJoinPolicy, List<RoomParticipant> participants) {
        CreateRoomRequest createRoomRequest = new CreateRoomRequest();
        if (validFrom != null) {
            createRoomRequest.setValidFrom(validFrom);
        }

        if (validUntil != null) {
            createRoomRequest.setValidUntil(validUntil);
        }

        if (roomJoinPolicy != null) {
            createRoomRequest.setRoomJoinPolicy(roomJoinPolicy);
        }

        List<com.azure.communication.rooms.implementation.models.RoomParticipant> roomParticipants = new ArrayList<>();

        if (participants != null) {
            roomParticipants = participants
                .stream()
                .map((participant) -> RoomParticipantConverter.convert(participant))
                .collect(Collectors.toList());
        }

        if (participants != null) {
            createRoomRequest.setParticipants(roomParticipants);
        }

        return createRoomRequest;
    }

        /**
     * Translate to update room request.
     *
     * @return The update room request.
     */
    private UpdateRoomRequest toUpdateRoomRequest(OffsetDateTime validFrom, OffsetDateTime validUntil, RoomJoinPolicy roomJoinPolicy, List<RoomParticipant> participants) {
        UpdateRoomRequest updateRoomRequest = new UpdateRoomRequest();

        if (validFrom != null) {
            updateRoomRequest.setValidFrom(validFrom);
        }

        if (validUntil != null) {
            updateRoomRequest.setValidUntil(validUntil);
        }

        if (roomJoinPolicy != null) {
            updateRoomRequest.setRoomJoinPolicy(roomJoinPolicy);
        }

        List<com.azure.communication.rooms.implementation.models.RoomParticipant> roomParticipants = new ArrayList<>();

        if (participants != null) {
            roomParticipants = participants
                .stream()
                .map((participant) -> RoomParticipantConverter.convert(participant))
                .collect(Collectors.toList());
        }

        if (participants != null) {
            updateRoomRequest.setParticipants(roomParticipants);
        }

        return updateRoomRequest;
    }

    /**
     * Translate to Add participants request.
     *
     * @return The add participants room request.
     */
    private AddParticipantsRequest toAddParticipantsRequest(List<RoomParticipant> participants) {
        AddParticipantsRequest addParticipantsRequest = new AddParticipantsRequest();

        List<com.azure.communication.rooms.implementation.models.RoomParticipant> roomParticipants = new ArrayList<>();

        if (participants != null) {
            roomParticipants = participants
                .stream()
                .map((participant) -> RoomParticipantConverter.convert(participant))
                .collect(Collectors.toList());
        }

        if (participants != null) {
            addParticipantsRequest.setParticipants(roomParticipants);
        }

        return addParticipantsRequest;
    }

    /**
     * Translate to Remove participants request.
     *
     * @return The Remove participants room request.
     */
    private RemoveParticipantsRequest toRemoveParticipantsRequest(List<RoomParticipant> participants) {
        RemoveParticipantsRequest removeParticipantsRequest = new RemoveParticipantsRequest();

        List<com.azure.communication.rooms.implementation.models.RoomParticipant> roomParticipants = new ArrayList<>();

        if (participants != null) {
            roomParticipants = participants
                .stream()
                .map((participant) -> RoomParticipantConverter.convert(participant))
                .collect(Collectors.toList());
        }

        if (participants != null) {
            removeParticipantsRequest.setParticipants(roomParticipants);
        }

        return removeParticipantsRequest;
    }

    /**
     * Translate to Update participants request.
     *
     * @return The Update participants room request.
     */
    private UpdateParticipantsRequest toUpdateParticipantsRequest(List<RoomParticipant> participants) {
        UpdateParticipantsRequest updateParticipantsRequest = new UpdateParticipantsRequest();

        List<com.azure.communication.rooms.implementation.models.RoomParticipant> roomParticipants = new ArrayList<>();

        if (participants != null) {
            roomParticipants = participants
                .stream()
                .map((participant) -> RoomParticipantConverter.convert(participant))
                .collect(Collectors.toList());
        }

        if (participants != null) {
            updateParticipantsRequest.setParticipants(roomParticipants);
        }

        return updateParticipantsRequest;
    }

    private RoomsErrorResponseException translateException(CommunicationErrorResponseException exception) {
        RoomsError error = null;
        if (exception.getValue() != null) {
            error = RoomsErrorConverter.convert(exception.getValue().getError());
        }
        return new RoomsErrorResponseException(exception.getMessage(), exception.getResponse(), error);
    }

}
