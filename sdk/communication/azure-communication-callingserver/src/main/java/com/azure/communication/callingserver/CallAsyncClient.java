// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.azure.communication.callingserver.implementation.AzureCommunicationCallingServerServiceImpl;
import com.azure.communication.callingserver.implementation.CallsImpl;
import com.azure.communication.callingserver.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callingserver.implementation.converters.InviteParticipantsRequestConverter;
import com.azure.communication.callingserver.implementation.models.CallModality;
import com.azure.communication.callingserver.implementation.models.CancelAllMediaOperationsRequest;
import com.azure.communication.callingserver.implementation.models.CreateCallRequestInternal;
import com.azure.communication.callingserver.implementation.models.CreateCallResponse;
import com.azure.communication.callingserver.implementation.models.EventSubscriptionType;
import com.azure.communication.callingserver.implementation.models.InviteParticipantsRequest;
import com.azure.communication.callingserver.implementation.models.PhoneNumberIdentifierModel;
import com.azure.communication.callingserver.implementation.models.PlayAudioRequest;
import com.azure.communication.callingserver.models.CancelAllMediaOperationsResponse;
import com.azure.communication.callingserver.models.CreateCallOptions;
import com.azure.communication.callingserver.models.CreateCallResult;
import com.azure.communication.callingserver.models.PlayAudioResponse;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;

import reactor.core.publisher.Mono;

/**
 * Async client that supports server call operations.
 */
@ServiceClient(builder = CallClientBuilder.class, isAsync = true)
public final class CallAsyncClient {
    private final CallsImpl callClient;
    private final ClientLogger logger = new ClientLogger(CallAsyncClient.class);

    CallAsyncClient(AzureCommunicationCallingServerServiceImpl callServiceClient) {
        callClient = callServiceClient.getCalls();
    }

    /**
     * Create a Call Request from source identity to targets identity.
     *
     * @param source The source of the call.
     * @param targets The targets of the call.
     * @param createCallOptions The call Options.
     * @return response for a successful CreateCall request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CreateCallResult> createCall(CommunicationIdentifier source, Iterable<CommunicationIdentifier> targets,
            CreateCallOptions createCallOptions) {
        try {
            Objects.requireNonNull(source, "'source' cannot be null.");
            Objects.requireNonNull(targets, "'targets' cannot be null.");
            Objects.requireNonNull(createCallOptions, "'CreateCallOptions' cannot be null.");

            CreateCallRequestInternal request = createCreateCallRequest(source, targets, createCallOptions);
            return this.callClient.createCallAsync(request).flatMap((CreateCallResponse response) -> {
                CreateCallResult createCallResult = convertCreateCallWithResponse(response);
                return Mono.just(createCallResult);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Create a Call Request from source identity to targets identity.
     *
     * @param source The source of the call.
     * @param targets The targets of the call.
     * @param createCallOptions The call Options.
     * @return response for a successful CreateCall request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CreateCallResult>> createCallWithResponse(CommunicationIdentifier source,
            Iterable<CommunicationIdentifier> targets, CreateCallOptions createCallOptions) {
        return createCallWithResponse(source, targets, createCallOptions, null);
    }

    Mono<Response<CreateCallResult>> createCallWithResponse(CommunicationIdentifier source,
            Iterable<CommunicationIdentifier> targets, CreateCallOptions createCallOptions, Context context) {
        try {
            Objects.requireNonNull(source, "'source' cannot be null.");
            Objects.requireNonNull(targets, "'targets' cannot be null.");
            Objects.requireNonNull(createCallOptions, "'CreateCallOptions' cannot be null.");

            CreateCallRequestInternal request = createCreateCallRequest(source, targets, createCallOptions);

            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }
                return this.callClient.createCallWithResponseAsync(request)
                        .flatMap((Response<CreateCallResponse> response) -> {
                            CreateCallResult createCallResult = convertCreateCallWithResponse(response.getValue());
                            return Mono.just(new SimpleResponse<>(response, createCallResult));
                        });
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Play audio in a call.
     *
     * @param callId The call id.
     * @param audioFileUri The media resource uri of the play audio request.
     * @param loop The flag indicating whether audio file needs to be played in loop or not.
     * @param audioFileId An id for the media in the AudioFileUri, using which we cache the media.
     * @param operationContext The value to identify context of the operation.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PlayAudioResponse> playAudio(String callId, String audioFileUri, boolean loop, String audioFileId, String operationContext) {
        try {
            Objects.requireNonNull(callId, "'callId' cannot be null.");
            Objects.requireNonNull(audioFileUri, "'audioFileUri' cannot be null.");

            PlayAudioRequest playAudioRequest = new PlayAudioRequest().
                setAudioFileUri(audioFileUri).setLoop(loop).setAudioFileId(audioFileId).setOperationContext(operationContext);
            return playAudio(callId, playAudioRequest);           
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    public Mono<PlayAudioResponse> playAudio(String callId, PlayAudioRequest request) {
        try {
            Objects.requireNonNull(callId, "'callId' cannot be null.");
            Objects.requireNonNull(request, "'request' cannot be null.");

            return this.callClient.playAudioAsync(callId, request).flatMap(
                (PlayAudioResponse response) -> {                    
                    return Mono.just(response);
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

     /**
     * Play audio in a call.
     *
     * @param callId The call id.
     * @param audioFileUri The media resource uri of the play audio request.
     * @param loop The flag indicating whether audio file needs to be played in loop or not.
     * @param audioFileId An id for the media in the AudioFileUri, using which we cache the media.
     * @param operationContext The value to identify context of the operation.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PlayAudioResponse>> playAudioWithResponse(String callId, String audioFileUri, boolean loop, String audioFileId, String operationContext) {
        PlayAudioRequest playAudioRequest = new PlayAudioRequest().
            setAudioFileUri(audioFileUri).setLoop(loop).setAudioFileId(audioFileId).setOperationContext(operationContext);
        return playAudioWithResponse(callId, playAudioRequest, null);
    }

    Mono<Response<PlayAudioResponse>> playAudioWithResponse(String callId, PlayAudioRequest request, Context context) {
        try {
            Objects.requireNonNull(callId, "'callId' cannot be null.");
            Objects.requireNonNull(request, "'request' cannot be null.");
            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }
                return this.callClient.playAudioWithResponseAsync(callId, request).flatMap((
                        Response<PlayAudioResponse> response) -> {
                    return Mono.just(new SimpleResponse<>(response, response.getValue()));
                });
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Hangup a call.
     *
     * @param callId Call id.
     * @return response for a successful HangupCall request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> hangupCall(String callId) {
        try {
            Objects.requireNonNull(callId, "'callId' cannot be null.");

            return this.callClient.hangupCallAsync(callId);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Hangup a call.
     *
     * @param callId Call id.
     * @return response for a successful HangupCall request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> hangupCallWithResponse(String callId) {
        return hangupCallWithResponse(callId, null);
    }

    Mono<Response<Void>> hangupCallWithResponse(String callId, Context context) {
        try {
            Objects.requireNonNull(callId, "'callId' cannot be null.");
            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }
                return this.callClient.hangupCallWithResponseAsync(callId);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Delete a call.
     *
     * @param callId Call id.
     * @return response for a successful DeleteCall request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> deleteCall(String callId) {
        try {
            Objects.requireNonNull(callId, "'callId' cannot be null.");

            return this.callClient.deleteCallAsync(callId);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Delete a call.
     *
     * @param callId Call id.
     * @return response for a successful DeleteCall request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> deleteCallWithResponse(String callId) {
        return deleteCallWithResponse(callId, null);
    }

    Mono<Response<Void>> deleteCallWithResponse(String callId, Context context) {
        try {
            Objects.requireNonNull(callId, "'callId' cannot be null.");
            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }
                return this.callClient.deleteCallWithResponseAsync(callId);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Cancel Media Operations.
     *
     * @param callId The call leg id.
     * @param operationContext operationContext
     * @return the response payload of the cancel media operations.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CancelAllMediaOperationsResponse> cancelAllMediaOperations(String callId, String operationContext) {
        try {
            Objects.requireNonNull(callId, "'callId' cannot be null.");

            CancelAllMediaOperationsRequest request = new CancelAllMediaOperationsRequest().setOperationContext(operationContext);

            return this.callClient.cancelAllMediaOperationsAsync(callId, request)
                    .flatMap((CancelAllMediaOperationsResponse response) -> {
                        return Mono.just(response);
                    });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Cancel Media Operations.
     *
     * @param callId The call leg id.
     * @param operationContext operationContext
     * @return the response payload of the cancel media operations.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CancelAllMediaOperationsResponse>> cancelAllMediaOperationsWithResponse(String callId, String operationContext) {
        return cancelAllMediaOperationsWithResponse(callId, operationContext, null);
    }

    Mono<Response<CancelAllMediaOperationsResponse>> cancelAllMediaOperationsWithResponse(String callId, String operationContext, Context context) {
        try {
            Objects.requireNonNull(callId, "'callId' cannot be null.");
            CancelAllMediaOperationsRequest request = new CancelAllMediaOperationsRequest().setOperationContext(operationContext);

            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }
                return this.callClient
                        .cancelAllMediaOperationsWithResponseAsync(callId, request, context)
                        .flatMap((
                                Response<CancelAllMediaOperationsResponse> response) -> {
                            return Mono.just(new SimpleResponse<>(response, response.getValue()));
                        });
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Invite Participats to a Call.
     *
     * @param callId Call id.
     * @param participant Invited participant.
     * @param alternateCallerId of the Invited participant.
     * @param operationContext operationContext.
     * @return response for a successful inviteParticipants request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> addParticipant(String callId, CommunicationIdentifier participant, String alternateCallerId, String operationContext) {
        try {
            Objects.requireNonNull(callId, "'callId' cannot be null.");
            Objects.requireNonNull(participant, "'request' cannot be null.");
            InviteParticipantsRequest request = InviteParticipantsRequestConverter.convert(participant, alternateCallerId, operationContext, null);
            return this.callClient.inviteParticipantsAsync(callId, request);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Invite Participats to a Call.
     *
     * @param callId Call id.
     * @param participant Invited participant.
     * @param alternateCallerId of the Invited participant.
     * @param operationContext operationContext.
     * @return response for a successful inviteParticipants request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> addParticipantWithResponse(String callId, CommunicationIdentifier participant, String alternateCallerId, String operationContext) {
        return addParticipantWithResponse(callId, participant, alternateCallerId, operationContext, null);
    }

    Mono<Response<Void>> addParticipantWithResponse(
        String callId,
        CommunicationIdentifier participant,
        String alternateCallerId,
        String operationContext,
        Context context) {
        try {
            Objects.requireNonNull(callId, "'callId' cannot be null.");
            Objects.requireNonNull(participant, "'participant' cannot be null.");
            InviteParticipantsRequest request = InviteParticipantsRequestConverter.convert(participant, alternateCallerId, operationContext, null);
            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }
                return this.callClient.inviteParticipantsWithResponseAsync(callId,request);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Remove participant from the call.
     *
     * @param callId Call id.
     * @param participantId Participant id.
     * @return response for a successful removeParticipant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> removeParticipant(String callId, String participantId) {
        try {
            Objects.requireNonNull(callId, "'callId' cannot be null.");
            Objects.requireNonNull(participantId, "'request' cannot be null.");
            return this.callClient.removeParticipantAsync(callId, participantId);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Remove participant from the call.
     *
     * @param callId Call id.
     * @param participantId Participant id.
     * @return response for a successful removeParticipant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> removeParticipantWithResponse(String callId, String participantId) {
        return removeParticipantWithResponse(callId, participantId, null);
    }

    /**
     * Remove participant from the call.
     */
    Mono<Response<Void>> removeParticipantWithResponse(String callId, String participantId, Context context) {
        try {
            Objects.requireNonNull(callId, "'callId' cannot be null.");
            Objects.requireNonNull(participantId, "'request' cannot be null.");
            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }
                return this.callClient.removeParticipantWithResponseAsync(callId, participantId);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    private CreateCallRequestInternal createCreateCallRequest(CommunicationIdentifier source,
            Iterable<CommunicationIdentifier> targets, CreateCallOptions createCallOptions) {
        CreateCallRequestInternal request = new CreateCallRequestInternal();
        List<CommunicationIdentifier> targetsList = new ArrayList<>();
        targets.forEach(targetsList::add);

        List<CallModality> requestedModalities = new ArrayList<CallModality>();
        for (CallModality modality : createCallOptions.getRequestedModalities()) {
            requestedModalities.add(CallModality.fromString(modality.toString()));
        }
        List<EventSubscriptionType> requestedCallEvents = new ArrayList<>();
        for (EventSubscriptionType requestedCallEvent : createCallOptions.getRequestedCallEvents()) {
            requestedCallEvents.add(EventSubscriptionType.fromString(requestedCallEvent.toString()));
        }

        PhoneNumberIdentifierModel sourceAlternateIdentity = createCallOptions.getAlternateCallerId() == null
            ? null : new PhoneNumberIdentifierModel()
            .setValue(createCallOptions.getAlternateCallerId().getPhoneNumber());

        request.setSource(CommunicationIdentifierConverter.convert(source))
                .setTargets(targetsList.stream().map(target -> CommunicationIdentifierConverter.convert(target))
                        .collect(Collectors.toList()))
                .setCallbackUri(createCallOptions.getCallbackUri());
        request.setRequestedModalities(requestedModalities);
        request.setRequestedCallEvents(requestedCallEvents).setSourceAlternateIdentity(sourceAlternateIdentity);

        return request;
    }

    private CreateCallResult convertCreateCallWithResponse(CreateCallResponse response) {
        return new CreateCallResult().setCallLegId(response.getCallLegId());
    }
}
