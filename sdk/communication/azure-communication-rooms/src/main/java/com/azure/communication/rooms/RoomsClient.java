// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.rooms;

import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.rooms.implementation.AzureCommunicationRoomServiceImpl;
import com.azure.communication.rooms.implementation.ParticipantsImpl;
import com.azure.communication.rooms.implementation.RoomsImpl;
import com.azure.communication.rooms.implementation.converters.ParticipantRoleConverter;
import com.azure.communication.rooms.implementation.converters.RoomModelConverter;
import com.azure.communication.rooms.implementation.converters.RoomParticipantConverter;
import com.azure.communication.rooms.implementation.models.CreateRoomRequest;
import com.azure.communication.rooms.implementation.models.ParticipantProperties;
import com.azure.communication.rooms.implementation.models.RoomModel;
import com.azure.communication.rooms.implementation.models.UpdateParticipantsRequest;
import com.azure.communication.rooms.implementation.models.UpdateRoomRequest;
import com.azure.communication.rooms.models.AddOrUpdateParticipantsResult;
import com.azure.communication.rooms.models.CommunicationRoom;
import com.azure.communication.rooms.models.CreateRoomOptions;
import com.azure.communication.rooms.models.RemoveParticipantsResult;
import com.azure.communication.rooms.models.RoomParticipant;
import com.azure.communication.rooms.models.UpdateRoomOptions;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonProviders;
import com.azure.json.JsonWriter;

import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

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
    private final RoomsImpl roomsClient;
    private final ParticipantsImpl participantsClient;
    private final ClientLogger logger = new ClientLogger(RoomsClient.class);

    RoomsClient(AzureCommunicationRoomServiceImpl roomsServiceClient) {
        roomsClient = roomsServiceClient.getRooms();
        participantsClient = roomsServiceClient.getParticipants();
    }

    /**
     * Create a new room. Input field is nullable.
     *
     * @param createRoomOptions the create room options.
     * @return response for a successful create room request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CommunicationRoom createRoom(CreateRoomOptions createRoomOptions) {
        RoomModel roomModel = this.roomsClient
                .create(toCreateRoomRequest(createRoomOptions.getValidFrom(),
                        createRoomOptions.getValidUntil(), createRoomOptions.isPstnDialOutEnabled(),
                        createRoomOptions.getParticipants()));
        return getCommunicationRoomFromResponse(roomModel);
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
        context = context == null ? Context.NONE : context;
        Response<RoomModel> response = this.roomsClient
                .createWithResponse(toCreateRoomRequest(createRoomOptions.getValidFrom(),
                        createRoomOptions.getValidUntil(), createRoomOptions.isPstnDialOutEnabled(),
                        createRoomOptions.getParticipants()), context);
        return new SimpleResponse<CommunicationRoom>(response, getCommunicationRoomFromResponse(response.getValue()));
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
        RoomModel roomModel = this.roomsClient
            .update(roomId,
                toUpdateRoomRequest(updateRoomOptions.getValidFrom(), updateRoomOptions.getValidUntil(),
                updateRoomOptions.isPstnDialOutEnabled()));
        return getCommunicationRoomFromResponse(roomModel);
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
        context = context == null ? Context.NONE : context;
        Response<RoomModel> response = this.roomsClient
            .updateWithResponse(roomId,
                toUpdateRoomRequest(updateRoomOptions.getValidFrom(), updateRoomOptions.getValidUntil(),
                updateRoomOptions.isPstnDialOutEnabled()), context);
        return new SimpleResponse<CommunicationRoom>(response, getCommunicationRoomFromResponse(response.getValue()));
    }

    /**
     * Get an existing room.
     *
     * @param roomId The room id.
     * @return The existing room.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CommunicationRoom getRoom(String roomId) {
        RoomModel roomModel = this.roomsClient
                    .get(roomId);
        return getCommunicationRoomFromResponse(roomModel);
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
        context = context == null ? Context.NONE : context;
        Response<RoomModel> response = this.roomsClient
                    .getWithResponse(roomId, context);
        return new SimpleResponse<CommunicationRoom>(response, getCommunicationRoomFromResponse(response.getValue()));

    }

    /**
     * Delete an existing room.
     *
     * @param roomId The room Id.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteRoom(String roomId) {
        this.roomsClient.delete(roomId);
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
        context = context == null ? Context.NONE : context;
        return this.roomsClient.deleteWithResponse(roomId, context);
    }

    /**
     * Lists all rooms.
     *
     * @return The existing rooms.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PagedIterable<CommunicationRoom> listRooms() {
        return new PagedIterable<>(
                () -> this.roomsClient.listSinglePage(),
                nextLink -> this.roomsClient.listNextSinglePage(nextLink))
            .mapPage(f -> RoomModelConverter.convert(f));
    }

    /**
     * Lists all rooms.
     *
     * @param context The context of key value pairs for http request.
     * @return The existing rooms.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PagedIterable<CommunicationRoom> listRooms(Context context) {
        final Context serviceContext = context == null ? Context.NONE : context;
        return new PagedIterable<>(
                () -> this.roomsClient.listSinglePage(serviceContext),
                nextLink -> this.roomsClient.listNextSinglePage(nextLink, serviceContext))
            .mapPage(f -> RoomModelConverter.convert(f));
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
        try {
            Objects.requireNonNull(participants, "'participants' cannot be null.");
            Objects.requireNonNull(roomId, "'roomId' cannot be null.");
            Map<String, ParticipantProperties> participantMap = convertRoomParticipantsToMapForAddOrUpdate(participants);
            String updateRequest = getUpdateRequest(participantMap);

            this.participantsClient.update(roomId, updateRequest);
            return new AddOrUpdateParticipantsResult();
        } catch (IOException ex) {
            ex.printStackTrace();
            throw logger.logExceptionAsError(new IllegalArgumentException("Failed to process JSON input", ex));
        }
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
        try {
            context = context == null ? Context.NONE : context;
            Objects.requireNonNull(participants, "'participants' cannot be null.");
            Objects.requireNonNull(roomId, "'roomId' cannot be null.");
            Map<String, ParticipantProperties> participantMap = convertRoomParticipantsToMapForAddOrUpdate(participants);
            String updateRequest = getUpdateRequest(participantMap);

            Response<Object> response = this.participantsClient
                .updateWithResponse(roomId, updateRequest, context);
            return new SimpleResponse<AddOrUpdateParticipantsResult>(
                response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw logger.logExceptionAsError(new IllegalArgumentException("Failed to process JSON input", ex));
        }
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
        try {
            Objects.requireNonNull(identifiers, "'identifiers' cannot be null.");
            Objects.requireNonNull(roomId, "'roomId' cannot be null.");
            Map<String, ParticipantProperties> participantMap = convertRoomIdentifiersToMapForRemove(
                identifiers);
            String updateRequest = getUpdateRequest(participantMap);

            this.participantsClient.update(roomId, updateRequest);
            return new RemoveParticipantsResult();
        } catch (IOException ex) {
            ex.printStackTrace();
            throw logger.logExceptionAsError(new IllegalArgumentException("Failed to process JSON input", ex));
        }
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
        try {
            context = context == null ? Context.NONE : context;
            Objects.requireNonNull(identifiers, "'identifiers' cannot be null.");
            Objects.requireNonNull(roomId, "'roomId' cannot be null.");
            Map<String, ParticipantProperties> participantMap = convertRoomIdentifiersToMapForRemove(
                identifiers);
            String updateRequest = getUpdateRequest(participantMap);

            Response<Object> response = this.participantsClient
                .updateWithResponse(roomId, updateRequest, context);

            return new SimpleResponse<RemoveParticipantsResult>(
                response.getRequest(), response.getStatusCode(), response.getHeaders(), null);
        } catch (IOException ex) {
            ex.printStackTrace();
            throw logger.logExceptionAsError(new IllegalArgumentException("Failed to process JSON input", ex));
        }
    }


    /**
     * List Room participants.
     *
     * @param roomId The room id.
     * @return Room Participants List
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PagedIterable<RoomParticipant> listParticipants(String roomId) {
        Objects.requireNonNull(roomId, "'roomId' cannot be null.");
        return new PagedIterable<>(
                () -> this.participantsClient.listSinglePage(roomId),
                nextLink -> this.participantsClient.listNextSinglePage(nextLink))
            .mapPage(f -> RoomParticipantConverter.convert(f));
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
        final Context serviceContext = context == null ? Context.NONE : context;
        Objects.requireNonNull(roomId, "'roomId' cannot be null.");

        return new PagedIterable<>(
                () -> this.participantsClient.listSinglePage(roomId, serviceContext),
                nextLink -> this.participantsClient.listNextSinglePage(nextLink, serviceContext))
            .mapPage(f -> RoomParticipantConverter.convert(f));
    }

    private CommunicationRoom getCommunicationRoomFromResponse(RoomModel room) {
        return new CommunicationRoom(
                room.getId(),
                room.getValidFrom(),
                room.getValidUntil(),
                room.getCreatedAt(),
                room.isPstnDialOutEnabled());
    }

    /**
     * Translate to create room request.
     *
     * @return The create room request.
     */
    private CreateRoomRequest toCreateRoomRequest(OffsetDateTime validFrom, OffsetDateTime validUntil,
            Boolean pstnDialOutEnabled, Iterable<RoomParticipant> participants) {
        CreateRoomRequest createRoomRequest = new CreateRoomRequest();
        if (validFrom != null) {
            createRoomRequest.setValidFrom(validFrom);
        }

        if (validUntil != null) {
            createRoomRequest.setValidUntil(validUntil);
        }

        if (pstnDialOutEnabled != null) {
            createRoomRequest.setPstnDialOutEnabled(pstnDialOutEnabled);
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
    private UpdateRoomRequest toUpdateRoomRequest(OffsetDateTime validFrom, OffsetDateTime validUntil, Boolean isPstnDialOutEnabled) {
        UpdateRoomRequest updateRoomRequest = new UpdateRoomRequest();

        if (validFrom != null) {
            updateRoomRequest.setValidFrom(validFrom);
        }

        if (validUntil != null) {
            updateRoomRequest.setValidUntil(validUntil);
        }

        if (isPstnDialOutEnabled != null) {
            updateRoomRequest.setPstnDialOutEnabled(isPstnDialOutEnabled);
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

    private static String getUpdateRequest(Map<String, ParticipantProperties> participantMap) throws IOException {
        Writer json = new StringWriter();
        try (JsonWriter jsonWriter = JsonProviders.createWriter(json)) {
            new UpdateParticipantsRequest().setParticipants(participantMap).toJson(jsonWriter);
        }
        return json.toString();
    }
}
