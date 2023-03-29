// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms;

import com.azure.communication.rooms.implementation.AzureCommunicationRoomServiceImpl;


import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.rooms.implementation.RoomsImpl;
import com.azure.communication.rooms.implementation.ParticipantsImpl;
import com.azure.communication.rooms.implementation.converters.RoomParticipantConverter;
import com.azure.communication.rooms.implementation.converters.RoomsErrorConverter;
import com.azure.communication.rooms.implementation.converters.ParticipantsCollectionConverter;
import com.azure.communication.rooms.implementation.models.RoomModel;
import com.azure.communication.rooms.implementation.models.RoomsCollection;
import com.azure.communication.rooms.implementation.models.ParticipantProperties;
import com.azure.communication.rooms.models.CommunicationRoom;
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
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
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
    private final ParticipantsImpl participantsClient;
    private final ClientLogger logger = new ClientLogger(RoomsAsyncClient.class);

    RoomsAsyncClient(AzureCommunicationRoomServiceImpl roomsServiceClient) {
        roomsClient = roomsServiceClient.getRooms();
        participantsClient = roomsServiceClient.getParticipants();
    }

    /**
     * Create a new Room, input fields are nullable.
     *
     * @param validFrom the validFrom value to set.
     * @param validUntil the validUntil value to set.
     * @param participants the participants value to set.
     * @return response for a successful create room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CommunicationRoom> createRoom(OffsetDateTime validFrom, OffsetDateTime validUntil, List<RoomParticipant> participants) {
        return createRoom(validFrom, validUntil, participants, null);
    }

    Mono<CommunicationRoom> createRoom(OffsetDateTime validFrom, OffsetDateTime validUntil, List<RoomParticipant> participants, Context context) {
        context = context == null ? Context.NONE : context;

        try {
            return this.roomsClient
            .createWithResponseAsync(toCreateRoomRequest(validFrom, validUntil, participants), context)
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
     * @param participants the participants value to set.
     * @return response for a successful create room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CommunicationRoom>> createRoomWithResponse(OffsetDateTime validFrom, OffsetDateTime validUntil, List<RoomParticipant> participants) {
        return createRoomWithResponse(validFrom, validUntil, participants, null);
    }

    Mono<Response<CommunicationRoom>> createRoomWithResponse(OffsetDateTime validFrom, OffsetDateTime validUntil, List<RoomParticipant> participants, Context context) {
        context = context == null ? Context.NONE : context;

        try {
            return this.roomsClient
            .createWithResponseAsync(toCreateRoomRequest(validFrom, validUntil, participants), context)
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
     * @return response for a successful update room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CommunicationRoom> updateRoom(String roomId, OffsetDateTime validFrom, OffsetDateTime validUntil) {
        return updateRoom(roomId, validFrom, validUntil, null);
    }

    Mono<CommunicationRoom> updateRoom(String roomId, OffsetDateTime validFrom, OffsetDateTime validUntil, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            return this.roomsClient
            .updateWithResponseAsync(roomId, toUpdateRoomRequest(validFrom, validUntil), context)
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
     * @return response for a successful update room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CommunicationRoom>> updateRoomWithResponse(String roomId, OffsetDateTime validFrom, OffsetDateTime validUntil) {
        return updateRoomWithResponse(roomId, validFrom, validUntil, null);
    }

    Mono<Response<CommunicationRoom>> updateRoomWithResponse(String roomId, OffsetDateTime validFrom, OffsetDateTime validUntil, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            return this.roomsClient
            .updateWithResponseAsync(roomId, toUpdateRoomRequest(validFrom, validUntil), context)
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
            .getWithResponseAsync(roomId, context)
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
            .getWithResponseAsync(roomId, context)
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
     * Lists all rooms.
     *
     * @return The existing rooms.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedFlux<RoomModel>> listRooms() {
        return listRooms(null);
    }

    Mono<PagedFlux<RoomModel>> listRooms(Context context) {
        context = context == null ? Context.NONE : context;
        try {
            return this.roomsClient
            .listAsync(context)
            .flatMap((Response<PagedFlux<RoomModel>> response) -> {
                return Mono.just(response.getValue());
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Lists all rooms with response
     *
     * @return The existing rooms.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PagedFlux<RoomModel>> listRoomsWithResponse() {
        return listRoomsWithResponse(null);
    }

    Mono<Response<PagedFlux<RoomModel>>> listRoomsWithResponse(Context context) {
        context = context == null ? Context.NONE : context;
        try {
            return this.roomsClient
            .listAsync(context)
            .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e))
            .flatMap((Response<PagedFlux<RoomModel>> response) -> {
                return Mono.just(response.getValue());
            });
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
            .deleteWithResponseAsync(roomId, context)
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
            .deleteWithResponseAsync(roomId, context)
            .flatMap((Response<Void> response) -> {
                return Mono.just(response);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Upsert participants to an existing Room.
     *
     * @param roomId The room id.
     * @param participants The participants list.
     * @return response for a successful upsert participants room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Object> upsertParticipants(String roomId, List<RoomParticipant> participants) {
        return upsertParticipants(roomId, participants, null);
    }

    Mono<Object> upsertParticipants(String roomId, List<RoomParticipant> participants, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            Objects.requireNonNull(participants, "'participants' cannot be null.");
            Objects.requireNonNull(roomId, "'roomId' cannot be null.");

            Map<String, ParticipantProperties> participantMap = ConvertRoomParticipantsToMapForUpsert(participants);
            UpdateParticipantsRequest updateRequest = new UpdateParticipantsRequest().setParticipants(participantMap);

            return this.participantsClient
            .updateAsync(roomId, updateRequest)
            .flatMap((Response<Object> response) -> {
                return Mono.just(response);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Upsert participants to an existing Room with response
     *
     * @param roomId The room id.
     * @param participants The participants list.
     * @return response for a successful upsert participants room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Object>> upsertParticipantsWithResponse(String roomId, List<RoomParticipant> participants) {
        return upsertParticipantsWithResponse(roomId, participants, null);
    }

    Mono<Response<Object>> upsertParticipantsWithResponse(String roomId, List<RoomParticipant> participants, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            Objects.requireNonNull(participants, "'participants' cannot be null.");
            Objects.requireNonNull(roomId, "'roomId' cannot be null.");

            Map<String, ParticipantProperties> participantMap = ConvertRoomParticipantsToMapForUpsert(participants);
            UpdateParticipantsRequest updateRequest = new UpdateParticipantsRequest().setParticipants(participantMap);

            return this.participantsClient
            .updateWithResponseAsync(roomId, updateRequest)
            .flatMap((Response<Object> response) -> {
                return Mono.just(response);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Remove participants from an existing Room.
     *
     * @param roomId The room id.
     * @param participants The participants list.
     * @return response for a successful add participants room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Object> removeParticipants(String roomId, List<CommunicationIdentifier> communicationIdentifiers) {
        return removeParticipants(roomId, communicationIdentifiers, null);
    }

    Mono<Object> removeParticipants(String roomId, List<CommunicationIdentifier> communicationIdentifiers, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            Objects.requireNonNull(communicationIdentifiers, "'communicationIdentifiers' cannot be null.");
            Objects.requireNonNull(roomId, "'roomId' cannot be null.");

            Map<String, CommunicationIdentifier> participantMap = ConvertRoomIdentifiersToMapForRemove(communicationIdentifiers);
            UpdateParticipantsRequest updateRequest = new UpdateParticipantsRequest().setParticipants(participantMap);

            return this.participantsClient
            .updateAsync(roomId, updateRequest)
            .flatMap((Response<Object> response) -> {
                return Mono.just(response);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Remove participants from an existing Room with response.
     *
     * @param roomId The room id.
     * @param participants The participants list.
     * @return response for a successful add participants room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Object>>removeParticipantsWithResponse(String roomId, List<CommunicationIdentifier> communicationIdentifiers) {
        return removeParticipantsWithResponse(roomId, communicationIdentifiers, null);
    }

    Mono<Response<Object>> removeParticipantsWithResponse(String roomId, List<CommunicationIdentifier> communicationIdentifiers, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            Objects.requireNonNull(communicationIdentifiers, "'communicationIdentifiers' cannot be null.");
            Objects.requireNonNull(roomId, "'roomId' cannot be null.");

            Map<String, CommunicationIdentifier> participantMap = ConvertRoomParticipantsToMapForRemove(participants);

            return this.participantsClient
            .updateWithResponseAsync(roomId, participantMap, context)
            .flatMap((Response<Object> response) -> {
                return Mono.just(response);
            });
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
    public Mono<PagedFlux<RoomParticipant>> getParticipants(String roomId) {
        return getParticipants(roomId, null);
    }

    Mono<PagedFlux<RoomParticipant>> getParticipants(String roomId, Context context) {

        context = context == null ? Context.NONE : context;
        try {
            Objects.requireNonNull(roomId, "'roomId' cannot be null.");
            return this.participantsClient
            .listAsync(roomId, context)
            .flatMap((Response<PagedFlux<RoomParticipant>> response) -> {
                return Mono.just(response);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get a room participants with response.
     *
     * @param roomId The room Id.
     * @return The existing room.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PagedFlux<RoomParticipant>>> getParticipantsWithResponse(String roomId) {
        return getParticipantsWithResponse(roomId, null);
    }

    Mono<Response<PagedFlux<RoomParticipant>>> getParticipantsWithResponse(String roomId, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            return this.participantsClient
            .listAsync(roomId, context)
            .onErrorMap(CommunicationErrorResponseException.class, e -> translateException(e))
            .flatMap((Response<PagedFlux<RoomParticipant>> response) -> {
                return Mono.just(response);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    private CommunicationRoom getCommunicationRoomFromResponse(RoomModel room) {
        return new CommunicationRoom(
            room.getId(),
            room.getValidFrom(),
            room.getValidUntil(),
            room.getCreatedAt());
    }

    /**
     * Translate to create room request.
     *
     * @return The create room request.
     */
    private CreateRoomRequest toCreateRoomRequest(OffsetDateTime validFrom, OffsetDateTime validUntil, List<RoomParticipant> participants) {
        CreateRoomRequest createRoomRequest = new CreateRoomRequest();
        if (validFrom != null) {
            createRoomRequest.setValidFrom(validFrom);
        }

        if (validUntil != null) {
            createRoomRequest.setValidUntil(validUntil);
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
    private UpdateRoomRequest toUpdateRoomRequest(OffsetDateTime validFrom, OffsetDateTime validUntil) {
        UpdateRoomRequest updateRoomRequest = new UpdateRoomRequest();

        if (validFrom != null) {
            updateRoomRequest.setValidFrom(validFrom);
        }

        if (validUntil != null) {
            updateRoomRequest.setValidUntil(validUntil);
        }

        return updateRoomRequest;
    }


    /**
     * Translate to map for upsert participants.
     *
     * @return Map of participants.
     */
    private Map<String, ParticipantProperties> ConvertRoomParticipantsToMapForUpsert(List<RoomParticipant> participants) {
        Map<String, ParticipantProperties> participantMap = new HashMap<>();

        if (participants != null) {
            for (RoomParticipant participant : participants) {
                participantMap.put(participant.getCommunicationIdentifier().getRawId(), new ParticipantProperties().setRole(participant.getRole()));
            }
        }
        return participantMap;
    }

    /**
     * Translate to map for remove participants.
     *
     * @return Map of participants.
     */
    private Map<String, ParticipantProperties> ConvertRoomIdentifiersToMapForRemove(List<CommunicationIdentifier> identifiers) {
        Map<String, ParticipantProperties> participantMap = new HashMap<>();

        if (identifiers != null) {
            for (CommunicationIdentifier identifier : identifiers) {
                participantMap.put(identifier.getRawId(), null);
            }
        }

        return participantMap;
    }

    private RoomsErrorResponseException translateException(CommunicationErrorResponseException exception) {
        RoomsError error = null;
        if (exception.getValue() != null) {
            error = RoomsErrorConverter.convert(exception.getValue().getError());
        }
        return new RoomsErrorResponseException(exception.getMessage(), exception.getResponse(), error);
    }

}
