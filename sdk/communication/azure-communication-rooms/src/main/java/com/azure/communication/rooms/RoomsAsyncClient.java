// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms;

import com.azure.communication.rooms.implementation.AzureCommunicationRoomServiceImpl;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.rooms.implementation.RoomsImpl;
import com.azure.communication.rooms.implementation.ParticipantsImpl;
import com.azure.communication.rooms.implementation.converters.ParticipantRoleConverter;
import com.azure.communication.rooms.implementation.converters.RoomModelConverter;
import com.azure.communication.rooms.implementation.converters.RoomParticipantConverter;
import com.azure.communication.rooms.implementation.models.RoomModel;
import com.azure.communication.rooms.implementation.models.ParticipantProperties;
import com.azure.communication.rooms.models.CommunicationRoom;
import com.azure.communication.rooms.models.CreateRoomOptions;
import com.azure.communication.rooms.models.RemoveParticipantsResult;
import com.azure.communication.rooms.models.RoomParticipant;
import com.azure.communication.rooms.models.UpdateRoomOptions;
import com.azure.communication.rooms.models.AddOrUpdateParticipantsResult;
import com.azure.communication.rooms.implementation.models.UpdateParticipantsRequest;
import com.azure.communication.rooms.implementation.models.UpdateRoomRequest;
import com.azure.communication.rooms.implementation.models.CreateRoomRequest;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedResponse;
import com.azure.core.http.rest.PagedResponseBase;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.paging.PageRetriever;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.HashMap;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.pagedFluxError;

/**
 * The Async client for Rooms of Azure Communication Room Service
 *
 * <p>
 * <strong>Instantiating an asynchronous Room Client</strong>
 * </p>
 *
 * <!-- src_embed readme-sample-createRoomsAsyncClientUsingAzureKeyCredential
 * -->
 *
 * <pre>
 * RoomsAsyncClient roomsClient = new RoomsClientBuilder()
 *      .endpoint&#40;endpoint&#41;
 *      .credential&#40;azureKeyCredential&#41;
 *      .buildAsyncClient&#40;&#41;;
 * </pre>
 *
 * <!-- end readme-sample-createRoomsAsyncClientUsingAzureKeyCredential -->
 *
 * @see RoomsClientBuilder
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
     * Create a new Room, input field is nullable.
     *
     * @param createRoomOptions the create room options.
     * @return response for a successful create room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CommunicationRoom> createRoom(CreateRoomOptions createRoomOptions) {
        return createRoom(createRoomOptions, null);
    }

    Mono<CommunicationRoom> createRoom(CreateRoomOptions createRoomOptions, Context context) {
        context = context == null ? Context.NONE : context;

        try {
            return this.roomsClient
                    .createWithResponseAsync(toCreateRoomRequest(createRoomOptions.getValidFrom(),
                            createRoomOptions.getValidUntil(), createRoomOptions.getParticipants()), context)
                    .flatMap((Response<RoomModel> response) -> {
                        return Mono.just(getCommunicationRoomFromResponse(response.getValue()));
                    });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Create a new Room, input field is nullable.
     *
     * @param createRoomOptions the create room options.
     * @return response for a successful create room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CommunicationRoom>> createRoomWithResponse(CreateRoomOptions createRoomOptions) {
        return createRoomWithResponse(createRoomOptions, null);
    }

    Mono<Response<CommunicationRoom>> createRoomWithResponse(CreateRoomOptions createRoomOptions, Context context) {
        context = context == null ? Context.NONE : context;

        try {
            return this.roomsClient
                    .createWithResponseAsync(toCreateRoomRequest(createRoomOptions.getValidFrom(),
                            createRoomOptions.getValidUntil(), createRoomOptions.getParticipants()), context)
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
     * @param roomId The room Id.
     * @param updateRoomOptions The update room options.
     * @return response for a successful update room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CommunicationRoom> updateRoom(String roomId, UpdateRoomOptions updateRoomOptions) {
        return updateRoom(roomId, updateRoomOptions, null);
    }

    Mono<CommunicationRoom> updateRoom(String roomId, UpdateRoomOptions updateRoomOptions, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            return this.roomsClient
                    .updateWithResponseAsync(roomId,
                            toUpdateRoomRequest(updateRoomOptions.getValidFrom(), updateRoomOptions.getValidUntil()),
                            context)
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
     * @param roomId The room Id.
     * @param updateRoomOptions The update room options.
     * @return response for a successful update room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CommunicationRoom>> updateRoomWithResponse(String roomId,
            UpdateRoomOptions updateRoomOptions) {
        return updateRoomWithResponse(roomId, updateRoomOptions, null);
    }

    Mono<Response<CommunicationRoom>> updateRoomWithResponse(String roomId, UpdateRoomOptions updateRoomOptions,
            Context context) {
        context = context == null ? Context.NONE : context;
        try {
            return this.roomsClient
                    .updateWithResponseAsync(roomId,
                            toUpdateRoomRequest(updateRoomOptions.getValidFrom(), updateRoomOptions.getValidUntil()),
                            context)
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
                            });
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
                                CommunicationRoom communicationRoom = getCommunicationRoomFromResponse(
                                        response.getValue());
                                return Mono.just(new SimpleResponse<CommunicationRoom>(response, communicationRoom));
                            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Lists all rooms.
     *
     * @return The existing rooms.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<CommunicationRoom> listRooms() {
        return listRooms(null);
    }

    PagedFlux<CommunicationRoom> listRooms(Context context) {
        final Context serviceContext = context == null ? Context.NONE : context;

        try {
            return pagedFluxConvert(new PagedFlux<>(
                    () -> this.roomsClient.listSinglePageAsync(serviceContext),
                    nextLink -> this.roomsClient.listNextSinglePageAsync(nextLink, serviceContext)),
                    f -> RoomModelConverter.convert(f));

        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
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
     * Add or update participants to an existing Room.
     *
     * @param roomId The room id.
     * @param participants The participants list.
     * @return response for a successful add or update participants room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AddOrUpdateParticipantsResult> addOrUpdateParticipants(String roomId, Iterable<RoomParticipant> participants) {
        return addOrUpdateParticipants(roomId, participants, null);
    }

    Mono<AddOrUpdateParticipantsResult> addOrUpdateParticipants(String roomId, Iterable<RoomParticipant> participants,
            Context context) {
        context = context == null ? Context.NONE : context;
        try {
            Objects.requireNonNull(participants, "'participants' cannot be null.");
            Objects.requireNonNull(roomId, "'roomId' cannot be null.");

            Map<String, ParticipantProperties> participantMap = convertRoomParticipantsToMapForAddOrUpdate(participants);

            ObjectMapper mapper = new ObjectMapper();

            String updateRequest = mapper.writeValueAsString(new UpdateParticipantsRequest().setParticipants(participantMap));


            return this.participantsClient
                    .updateAsync(roomId, updateRequest, context)
                    .flatMap((response) -> {
                        return Mono.just(new AddOrUpdateParticipantsResult());
                    });

        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            return Mono.error(new IllegalArgumentException("Failed to process JSON input", ex));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Add or update participants to an existing Room with response
     *
     * @param roomId The room id.
     * @param participants The participants list.
     * @return response for a successful add or update participants room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AddOrUpdateParticipantsResult>> addOrUpdateParticipantsWithResponse(String roomId,
            Iterable<RoomParticipant> participants) {
        return addOrUpdateParticipantsWithResponse(roomId, participants, null);
    }

    Mono<Response<AddOrUpdateParticipantsResult>> addOrUpdateParticipantsWithResponse(String roomId,
            Iterable<RoomParticipant> participants, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            Objects.requireNonNull(participants, "'participants' cannot be null.");
            Objects.requireNonNull(roomId, "'roomId' cannot be null.");

            Map<String, ParticipantProperties> participantMap = convertRoomParticipantsToMapForAddOrUpdate(participants);

            ObjectMapper mapper = new ObjectMapper();

            String updateRequest = mapper.writeValueAsString(new UpdateParticipantsRequest().setParticipants(participantMap));



            return this.participantsClient
                    .updateWithResponseAsync(roomId, updateRequest, context)
                    .map(result -> new SimpleResponse<AddOrUpdateParticipantsResult>(
                            result.getRequest(), result.getStatusCode(), result.getHeaders(), null));
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            return Mono.error(new IllegalArgumentException("Failed to process JSON input", ex));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Remove participants from an existing Room.
     *
     * @param roomId The room id.
     * @param participantsIdentifiers The communication identifier list.
     * @return response for a successful add participants room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RemoveParticipantsResult> removeParticipants(String roomId,
            Iterable<CommunicationIdentifier> participantsIdentifiers) {
        return removeParticipants(roomId, participantsIdentifiers, null);
    }

    Mono<RemoveParticipantsResult> removeParticipants(String roomId,
            Iterable<CommunicationIdentifier> participantsIdentifiers, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            Objects.requireNonNull(participantsIdentifiers, "'participantsIdentifiers' cannot be null.");
            Objects.requireNonNull(roomId, "'roomId' cannot be null.");

            Map<String, ParticipantProperties> participantMap = convertRoomIdentifiersToMapForRemove(
                    participantsIdentifiers);

            ObjectMapper mapper = new ObjectMapper();

            String updateRequest =  mapper.writeValueAsString(new UpdateParticipantsRequest().setParticipants(participantMap));

            return this.participantsClient
                    .updateAsync(roomId, updateRequest, context)
                    .flatMap((response) -> {
                        return Mono.just(new RemoveParticipantsResult());
                    });
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            return Mono.error(new IllegalArgumentException("Failed to process JSON input", ex));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Remove participants from an existing Room with response.
     *
     * @param roomId The room id.
     * @param participantsIdentifiers The communication identifier list.
     * @return response for a successful add participants room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RemoveParticipantsResult>> removeParticipantsWithResponse(String roomId,
            Iterable<CommunicationIdentifier> participantsIdentifiers) {
        return removeParticipantsWithResponse(roomId, participantsIdentifiers, null);
    }

    Mono<Response<RemoveParticipantsResult>> removeParticipantsWithResponse(String roomId,
            Iterable<CommunicationIdentifier> participantsIdentifiers, Context context) {
        context = context == null ? Context.NONE : context;
        try {
            Objects.requireNonNull(participantsIdentifiers, "'participantsIdentifiers' cannot be null.");
            Objects.requireNonNull(roomId, "'roomId' cannot be null.");

            Map<String, ParticipantProperties> participantMap = convertRoomIdentifiersToMapForRemove(
                    participantsIdentifiers);

            ObjectMapper mapper = new ObjectMapper();

            String updateRequest = mapper.writeValueAsString(new UpdateParticipantsRequest().setParticipants(participantMap));

            return this.participantsClient
                    .updateWithResponseAsync(roomId, updateRequest, context)
                    .map(result -> new SimpleResponse<RemoveParticipantsResult>(
                            result.getRequest(), result.getStatusCode(), result.getHeaders(), null));
        } catch (JsonProcessingException ex) {
            ex.printStackTrace();
            return Mono.error(new IllegalArgumentException("Failed to process JSON input", ex));
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
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public PagedFlux<RoomParticipant> listParticipants(String roomId) {
        return listParticipants(roomId, null);
    }

    PagedFlux<RoomParticipant> listParticipants(String roomId, Context context) {
        final Context serviceContext = context == null ? Context.NONE : context;

        try {
            Objects.requireNonNull(roomId, "'roomId' cannot be null.");

            return pagedFluxConvert(new PagedFlux<>(
                    () -> this.participantsClient.listSinglePageAsync(roomId, serviceContext),
                    nextLink -> this.participantsClient.listNextSinglePageAsync(nextLink, serviceContext)),
                    f -> RoomParticipantConverter.convert(f));

        } catch (RuntimeException ex) {
            return pagedFluxError(logger, ex);
        }
    }

    private <T1, T2> PagedFlux<T1> pagedFluxConvert(PagedFlux<T2> originalPagedFlux, Function<T2, T1> func) {

        final Function<PagedResponse<T2>, PagedResponse<T1>> responseMapper = response -> new PagedResponseBase<Void, T1>(
                response.getRequest(),
                response.getStatusCode(),
                response.getHeaders(),
                response.getValue()
                        .stream()
                        .map(value -> func.apply(value)).collect(Collectors.toList()),
                response.getContinuationToken(),
                null);

        final Supplier<PageRetriever<String, PagedResponse<T1>>> provider = () -> (continuationToken, pageSize) -> {
            Flux<PagedResponse<T2>> flux = (continuationToken == null)
                    ? originalPagedFlux.byPage()
                    : originalPagedFlux.byPage(continuationToken);
            return flux.map(responseMapper);
        };

        return PagedFlux.create(provider);
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
    private CreateRoomRequest toCreateRoomRequest(OffsetDateTime validFrom, OffsetDateTime validUntil,
            Iterable<RoomParticipant> participants) {
        CreateRoomRequest createRoomRequest = new CreateRoomRequest();
        if (validFrom != null) {
            createRoomRequest.setValidFrom(validFrom);
        }

        if (validUntil != null) {
            createRoomRequest.setValidUntil(validUntil);
        }

        Map<String, ParticipantProperties> roomParticipants = new HashMap<>();

        if (participants != null) {
            roomParticipants = convertRoomParticipantsToMapForAddOrUpdate(participants);
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
     * Translate to map for add or update participants.
     *
     * @return Map of participants.
     */
    private Map<String, ParticipantProperties> convertRoomParticipantsToMapForAddOrUpdate(
            Iterable<RoomParticipant> participants) {
        Map<String, ParticipantProperties> participantMap = new HashMap<>();

        if (participants != null) {
            for (RoomParticipant participant : participants) {
                participantMap.put(participant.getCommunicationIdentifier().getRawId(),
                        new ParticipantProperties().setRole(ParticipantRoleConverter.convert(participant.getRole())));
            }
        }

        return participantMap;
    }

    /**
     * Translate to map for remove participants.
     *
     * @return Map of participants.
     */
    private Map<String, ParticipantProperties> convertRoomIdentifiersToMapForRemove(
            Iterable<CommunicationIdentifier> identifiers) {
        Map<String, ParticipantProperties> participantMap = new HashMap<>();

        if (identifiers != null) {
            for (CommunicationIdentifier identifier : identifiers) {
                participantMap.put(identifier.getRawId(), null);
            }
        }

        return participantMap;
    }

}
