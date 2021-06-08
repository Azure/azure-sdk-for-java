// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.azure.communication.callingserver.implementation.AzureCommunicationCallingServerServiceImpl;
import com.azure.communication.callingserver.implementation.CallsImpl;
import com.azure.communication.callingserver.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callingserver.implementation.converters.AddParticipantConverter;
import com.azure.communication.callingserver.implementation.converters.PlayAudioConverter;
import com.azure.communication.callingserver.implementation.converters.ServerCallingErrorConverter;
import com.azure.communication.callingserver.implementation.models.CommunicationErrorException;
import com.azure.communication.callingserver.implementation.models.PhoneNumberIdentifierModel;
import com.azure.communication.callingserver.implementation.models.PlayAudioRequest;
import com.azure.communication.callingserver.models.CallModality;
import com.azure.communication.callingserver.implementation.models.CancelAllMediaOperationsRequest;
import com.azure.communication.callingserver.implementation.models.CreateCallRequestInternal;
import com.azure.communication.callingserver.models.EventSubscriptionType;
import com.azure.communication.callingserver.implementation.models.InviteParticipantsRequest;
import com.azure.communication.callingserver.models.CancelAllMediaOperationsResponse;
import com.azure.communication.callingserver.models.CreateCallOptions;
import com.azure.communication.callingserver.models.CreateCallResponse;
import com.azure.communication.callingserver.models.PlayAudioOptions;
import com.azure.communication.callingserver.models.PlayAudioResponse;
import com.azure.communication.callingserver.models.ServerCallingError;
import com.azure.communication.callingserver.models.ServerCallingErrorException;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
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
    public Mono<CreateCallResponse> createCall(CommunicationIdentifier source,
                                               CommunicationIdentifier[] targets,
                                               CreateCallOptions createCallOptions) {
        try {
            Objects.requireNonNull(source, "'source' cannot be null.");
            Objects.requireNonNull(targets, "'targets' cannot be null.");
            Objects.requireNonNull(createCallOptions, "'createCallOptions' cannot be null.");
            CreateCallRequestInternal request = createCreateCallRequest(source, targets, createCallOptions);
            return this.callClient.createCallAsync(request)
                .onErrorMap(CommunicationErrorException.class, e -> translateException(e));
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
    public Mono<Response<CreateCallResponse>> createCallWithResponse(CommunicationIdentifier source,
                                                                     CommunicationIdentifier[] targets,
                                                                     CreateCallOptions createCallOptions) {
        return createCallWithResponse(source, targets, createCallOptions, Context.NONE);
    }

    Mono<Response<CreateCallResponse>> createCallWithResponse(CommunicationIdentifier source,
                                                              CommunicationIdentifier[] targets,
                                                              CreateCallOptions createCallOptions,
                                                              Context context) {
        try {
            Objects.requireNonNull(source, "'source' cannot be null.");
            Objects.requireNonNull(targets, "'targets' cannot be null.");
            Objects.requireNonNull(createCallOptions, "'CreateCallOptions' cannot be null.");
            CreateCallRequestInternal request = createCreateCallRequest(source, targets, createCallOptions);
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return this.callClient.createCallWithResponseAsync(request, contextValue)
                    .onErrorMap(CommunicationErrorException.class, e -> translateException(e));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Play audio in a call.
     *
     * @param callId The call id.
     * @param audioFileUri The media resource uri of the play audio request. Currently only Wave file (.wav) format
     *                     audio prompts are supported. More specifically, the audio content in the wave file must
     *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param loop The flag indicating whether audio file needs to be played in loop or not.
     * @param audioFileId An id for the media in the AudioFileUri, using which we cache the media.
     * @param callbackUri call back uri to receive notifications.
     * @param operationContext The value to identify context of the operation.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PlayAudioResponse> playAudio(String callId,
                                             String audioFileUri,
                                             boolean loop,
                                             String audioFileId,
                                             String callbackUri,
                                             String operationContext) {
        PlayAudioRequest playAudioRequest =
            PlayAudioConverter.convert(audioFileUri, loop, audioFileId, callbackUri, operationContext);
        return playAudio(callId, playAudioRequest);
    }

    /**
     * Play audio in a call.
     *
     * @param callId The call id.
     * @param audioFileUri The media resource uri of the play audio request. Currently only Wave file (.wav) format
     *                     audio prompts are supported. More specifically, the audio content in the wave file must
     *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param playAudioOptions Options for play audio.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PlayAudioResponse> playAudio(String callId,
                                             String audioFileUri,
                                             PlayAudioOptions playAudioOptions) {
        PlayAudioRequest playAudioRequest = PlayAudioConverter.convert(audioFileUri, playAudioOptions);
        return playAudio(callId, playAudioRequest);

    }

    Mono<PlayAudioResponse> playAudio(String callId, PlayAudioRequest playAudioRequest) {
        try {
            Objects.requireNonNull(callId, "'callId' cannot be null.");
            Objects.requireNonNull(playAudioRequest.getAudioFileUri(), "'audioFileUri' cannot be null.");
            return this.callClient.playAudioAsync(callId, playAudioRequest)
                .onErrorMap(CommunicationErrorException.class, e -> translateException(e));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

     /**
     * Play audio in a call.
     *
     * @param callId The call id.
      * @param audioFileUri The media resource uri of the play audio request. Currently only Wave file (.wav) format
      *                     audio prompts are supported. More specifically, the audio content in the wave file must
      *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param loop The flag indicating whether audio file needs to be played in loop or not.
     * @param audioFileId An id for the media in the AudioFileUri, using which we cache the media.
     * @param callbackUri call back uri to receive notifications.
     * @param operationContext The value to identify context of the operation.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PlayAudioResponse>> playAudioWithResponse(String callId,
                                                                   String audioFileUri,
                                                                   boolean loop,
                                                                   String audioFileId,
                                                                   String callbackUri,
                                                                   String operationContext) {
        PlayAudioRequest playAudioRequest =
            PlayAudioConverter.convert(audioFileUri, loop, audioFileId, callbackUri, operationContext);
        return playAudioWithResponse(callId, playAudioRequest, Context.NONE);
    }

    /**
     * Play audio in a call.
     *
     * @param callId The call id.
     * @param audioFileUri The media resource uri of the play audio request. Currently only Wave file (.wav) format
     *                     audio prompts are supported. More specifically, the audio content in the wave file must
     *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param playAudioOptions Options for play audio.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PlayAudioResponse>> playAudioWithResponse(String callId,
                                                                   String audioFileUri,
                                                                   PlayAudioOptions playAudioOptions) {
        PlayAudioRequest playAudioRequest = PlayAudioConverter.convert(audioFileUri, playAudioOptions);
        return playAudioWithResponse(callId, playAudioRequest, Context.NONE);
    }

    Mono<Response<PlayAudioResponse>> playAudioWithResponse(String callId,
                                                            PlayAudioRequest playAudioRequest,
                                                            Context context) {
        try {
            Objects.requireNonNull(callId, "'callId' cannot be null.");
            Objects.requireNonNull(playAudioRequest.getAudioFileUri(), "'audioFileUri' cannot be null.");
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return this.callClient.playAudioWithResponseAsync(callId, playAudioRequest, contextValue)
                    .onErrorMap(CommunicationErrorException.class, e -> translateException(e));
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
            return this.callClient.hangupCallAsync(callId)
                .onErrorMap(CommunicationErrorException.class, e -> translateException(e));
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
        return hangupCallWithResponse(callId, Context.NONE);
    }

    Mono<Response<Void>> hangupCallWithResponse(String callId, Context context) {
        try {
            Objects.requireNonNull(callId, "'callId' cannot be null.");
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return this.callClient.hangupCallWithResponseAsync(callId, contextValue)
                    .onErrorMap(CommunicationErrorException.class, e -> translateException(e));
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
            return this.callClient.deleteCallAsync(callId)
                .onErrorMap(CommunicationErrorException.class, e -> translateException(e));
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
        return deleteCallWithResponse(callId, Context.NONE);
    }

    Mono<Response<Void>> deleteCallWithResponse(String callId, Context context) {
        try {
            Objects.requireNonNull(callId, "'callId' cannot be null.");
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return this.callClient.deleteCallWithResponseAsync(callId, contextValue)
                    .onErrorMap(CommunicationErrorException.class, e -> translateException(e));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Cancel all media operations in the call.
     *
     * @param callId The call leg id.
     * @param operationContext operationContext
     * @return the response payload of the cancel media operations.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CancelAllMediaOperationsResponse> cancelAllMediaOperations(String callId, String operationContext) {
        try {
            Objects.requireNonNull(callId, "'callId' cannot be null.");
            CancelAllMediaOperationsRequest request = new CancelAllMediaOperationsRequest();
            request.setOperationContext(operationContext);
            return this.callClient.cancelAllMediaOperationsAsync(callId, request)
                .onErrorMap(CommunicationErrorException.class, e -> translateException(e));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Cancel all media operations in the call.
     *
     * @param callId The call leg id.
     * @param operationContext operationContext
     * @return the response payload of the cancel media operations.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CancelAllMediaOperationsResponse>> cancelAllMediaOperationsWithResponse(String callId,
                                                                                                 String operationContext) {
        return cancelAllMediaOperationsWithResponse(callId, operationContext, Context.NONE);
    }

    Mono<Response<CancelAllMediaOperationsResponse>> cancelAllMediaOperationsWithResponse(String callId,
                                                                                          String operationContext,
                                                                                          Context context) {
        try {
            Objects.requireNonNull(callId, "'callId' cannot be null.");
            CancelAllMediaOperationsRequest request = new CancelAllMediaOperationsRequest();
            request.setOperationContext(operationContext);
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return this.callClient.cancelAllMediaOperationsWithResponseAsync(callId, request, contextValue)
                    .onErrorMap(CommunicationErrorException.class, e -> translateException(e));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Add a participant to the call.
     *
     * @param callId Call id.
     * @param participant Invited participant.
     * @param alternateCallerId The phone number to use when adding a phone number participant.
     * @param operationContext operationContext.
     * @return response for a successful addParticipant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> addParticipant(String callId,
                                     CommunicationIdentifier participant,
                                     String alternateCallerId,
                                     String operationContext) {
        try {
            Objects.requireNonNull(callId, "'callId' cannot be null.");
            Objects.requireNonNull(participant, "'participant' cannot be null.");
            InviteParticipantsRequest request = AddParticipantConverter.convert(participant,
                alternateCallerId,
                operationContext,
                null);
            return this.callClient.inviteParticipantsAsync(callId, request)
                .onErrorMap(CommunicationErrorException.class, e -> translateException(e));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Add a participant to the call.
     *
     * @param callId Call id.
     * @param participant Invited participant.
     * @param alternateCallerId The phone number to use when adding a phone number participant.
     * @param operationContext operationContext.
     * @return response for a successful addParticipant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> addParticipantWithResponse(String callId,
                                                           CommunicationIdentifier participant,
                                                           String alternateCallerId,
                                                           String operationContext) {
        return addParticipantWithResponse(callId,
            participant,
            alternateCallerId,
            operationContext,
            Context.NONE);
    }

    Mono<Response<Void>> addParticipantWithResponse(String callId,
                                                    CommunicationIdentifier participant,
                                                    String alternateCallerId,
                                                    String operationContext,
                                                    Context context) {
        try {
            Objects.requireNonNull(callId, "'callId' cannot be null.");
            Objects.requireNonNull(participant, "'participant' cannot be null.");
            InviteParticipantsRequest request = AddParticipantConverter.convert(participant, alternateCallerId, operationContext, null);
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return this.callClient.inviteParticipantsWithResponseAsync(callId, request, contextValue)
                    .onErrorMap(CommunicationErrorException.class, e -> translateException(e));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Remove a participant from the call.
     *
     * @param callId Call id.
     * @param participantId Participant id.
     * @return response for a successful removeParticipant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> removeParticipant(String callId, String participantId) {
        try {
            Objects.requireNonNull(callId, "'callId' cannot be null.");
            Objects.requireNonNull(participantId, "'participantId' cannot be null.");
            return this.callClient.removeParticipantAsync(callId, participantId)
                .onErrorMap(CommunicationErrorException.class, e -> translateException(e));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Remove a participant from the call.
     *
     * @param callId Call id.
     * @param participantId Participant id.
     * @return response for a successful removeParticipant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> removeParticipantWithResponse(String callId, String participantId) {
        return removeParticipantWithResponse(callId, participantId, Context.NONE);
    }

    /**
     * Remove participant from the call.
     */
    Mono<Response<Void>> removeParticipantWithResponse(String callId, String participantId, Context context) {
        try {
            Objects.requireNonNull(callId, "'callId' cannot be null.");
            Objects.requireNonNull(participantId, "'participantId' cannot be null.");
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return this.callClient.removeParticipantWithResponseAsync(callId, participantId, contextValue)
                    .onErrorMap(CommunicationErrorException.class, e -> translateException(e));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    private CreateCallRequestInternal createCreateCallRequest(CommunicationIdentifier source,
                                                              CommunicationIdentifier[] targets,
                                                              CreateCallOptions createCallOptions) {

        List<CommunicationIdentifier> targetsList = new ArrayList<>();
        for (CommunicationIdentifier communicationIdentifier : targets) {
            targetsList.add(communicationIdentifier);
        }

        List<CallModality> requestedModalities = new LinkedList<>();
        for (CallModality modality : createCallOptions.getRequestedModalities()) {
            requestedModalities.add(CallModality.fromString(modality.toString()));
        }

        List<EventSubscriptionType> requestedCallEvents = new LinkedList<>();
        for (EventSubscriptionType requestedCallEvent : createCallOptions.getRequestedCallEvents()) {
            requestedCallEvents.add(EventSubscriptionType.fromString(requestedCallEvent.toString()));
        }

        PhoneNumberIdentifierModel sourceAlternateIdentity = null;
        if (createCallOptions.getAlternateCallerId() != null) {
            sourceAlternateIdentity = new PhoneNumberIdentifierModel();
            sourceAlternateIdentity.setValue(createCallOptions.getAlternateCallerId().getPhoneNumber());
        }

        CreateCallRequestInternal request = new CreateCallRequestInternal();
        request.setSource(CommunicationIdentifierConverter.convert(source));
        request.setTargets(targetsList.stream()
            .map(target -> CommunicationIdentifierConverter.convert(target))
            .collect(Collectors.toList()));
        request.setCallbackUri(createCallOptions.getCallbackUri());
        request.setRequestedModalities(requestedModalities);
        request.setRequestedCallEvents(requestedCallEvents).setSourceAlternateIdentity(sourceAlternateIdentity);

        return request;
    }

    private ServerCallingErrorException translateException(CommunicationErrorException exception) {
        ServerCallingError error = null;
        if (exception.getValue() != null) {
            error = ServerCallingErrorConverter.convert(exception.getValue());
        }
        return new ServerCallingErrorException(exception.getMessage(), exception.getResponse(), error);
    }
}
