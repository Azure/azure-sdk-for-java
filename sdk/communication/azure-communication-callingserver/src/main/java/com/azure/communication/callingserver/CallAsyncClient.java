// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.AzureCommunicationCallingServerServiceImpl;
import com.azure.communication.callingserver.implementation.CallsImpl;
import com.azure.communication.callingserver.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callingserver.implementation.converters.InviteParticipantsRequestConverter;
import com.azure.communication.callingserver.implementation.converters.ResultInfoConverter;
import com.azure.communication.callingserver.implementation.models.CallModalityModel;
import com.azure.communication.callingserver.implementation.models.CancelMediaProcessingRequestInternal;
import com.azure.communication.callingserver.implementation.models.CreateCallRequestInternal;
import com.azure.communication.callingserver.implementation.models.CreateCallResponse;
import com.azure.communication.callingserver.implementation.models.EventSubscriptionTypeModel;
import com.azure.communication.callingserver.implementation.models.PlayAudioRequestInternal;
import com.azure.communication.callingserver.models.CallModality;
import com.azure.communication.callingserver.models.CancelMediaProcessingRequest;
import com.azure.communication.callingserver.models.CancelMediaProcessingResult;
import com.azure.communication.callingserver.models.CreateCallOptions;
import com.azure.communication.callingserver.models.CreateCallResult;
import com.azure.communication.callingserver.models.EventSubscriptionType;
import com.azure.communication.callingserver.models.InviteParticipantsRequest;
import com.azure.communication.callingserver.models.OperationStatus;
import com.azure.communication.callingserver.models.PlayAudioRequest;
import com.azure.communication.callingserver.models.PlayAudioResult;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

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
     * @param request Play audio request.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PlayAudioResult> playAudio(String callId, PlayAudioRequest request) {
        try {
            Objects.requireNonNull(callId, "'callId' cannot be null.");
            Objects.requireNonNull(request, "'request' cannot be null.");

            return this.callClient.playAudioAsync(callId, convertPlayAudioRequest(request)).flatMap(
                    (com.azure.communication.callingserver.implementation.models.PlayAudioResponse response) -> {
                        PlayAudioResult playAudioResult = convertPlayAudioResponse(response);
                        return Mono.just(playAudioResult);
                    });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Play audio in a call.
     *
     * @param callId The call id.
     * @param request Play audio request.
     * @return the response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PlayAudioResult>> playAudioWithResponse(String callId, PlayAudioRequest request) {
        return playAudioWithResponse(callId, request, null);
    }
  
    Mono<Response<PlayAudioResult>> playAudioWithResponse(String callId, PlayAudioRequest request, Context context) {
        try {
            Objects.requireNonNull(callId, "'callId' cannot be null.");
            Objects.requireNonNull(request, "'request' cannot be null.");
            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }
                return this.callClient.playAudioWithResponseAsync(callId, convertPlayAudioRequest(request)).flatMap((
                        Response<com.azure.communication.callingserver.implementation.models.PlayAudioResponse> response) -> {
                    PlayAudioResult playAudioResult = convertPlayAudioResponse(response.getValue());
                    return Mono.just(new SimpleResponse<>(response, playAudioResult));
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
     * Cancel Media Processing.
     *
     * @param callId The call leg id.
     * @param request Cancel Media Processing request.
     * @return the response payload of the cancel media processing operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CancelMediaProcessingResult> cancelMediaProcessing(String callId,
            CancelMediaProcessingRequest request) {
        try {
            Objects.requireNonNull(callId, "'callId' cannot be null.");
            Objects.requireNonNull(request, "'request' cannot be null.");

            return this.callClient.cancelMediaProcessingAsync(callId, convertCancelMediaProcessingRequest(request))
                    .flatMap((
                            com.azure.communication.callingserver.implementation.models.CancelMediaProcessingResponse response) -> {
                        CancelMediaProcessingResult cancelMediaProcessingResult = convertCancelMediaProcessingResponse(
                                response);
                        return Mono.just(cancelMediaProcessingResult);
                    });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Cancel Media Processing.
     *
     * @param callId The call leg id.
     * @param request Cancel Media Processing request.
     * @return the response payload of the cancel media processing operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CancelMediaProcessingResult>> cancelMediaProcessingWithResponse(String callId,
            CancelMediaProcessingRequest request) {
        return cancelMediaProcessingWithResponse(callId, request, null);
    }

    Mono<Response<CancelMediaProcessingResult>> cancelMediaProcessingWithResponse(String callId,
            CancelMediaProcessingRequest request, Context context) {
        try {
            Objects.requireNonNull(callId, "'callId' cannot be null.");
            Objects.requireNonNull(request, "'request' cannot be null.");
            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }
                return this.callClient
                        .cancelMediaProcessingWithResponseAsync(callId, convertCancelMediaProcessingRequest(request))
                        .flatMap((
                                Response<com.azure.communication.callingserver.implementation.models.CancelMediaProcessingResponse> response) -> {
                            CancelMediaProcessingResult cancelMediaProcessingResult = convertCancelMediaProcessingResponse(
                                    response.getValue());
                            return Mono.just(new SimpleResponse<>(response, cancelMediaProcessingResult));
                        });
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Cancel Media Processing.
     *
     * @param callId Call id.
     * @param request Invite participant request.
     * @return response for a successful inviteParticipants request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> inviteParticipants(String callId, InviteParticipantsRequest request) {
        try {
            Objects.requireNonNull(callId, "'callId' cannot be null.");
            Objects.requireNonNull(request, "'request' cannot be null.");

            return this.callClient.inviteParticipantsAsync(callId, InviteParticipantsRequestConverter.convert(request));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Cancel Media Processing.
     *
     * @param callId Call id.
     * @param request Invite participant request.
     * @return response for a successful inviteParticipants request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> inviteParticipantsWithResponse(String callId, InviteParticipantsRequest request) {
        return inviteParticipantsWithResponse(callId, request, null);
    }

    Mono<Response<Void>> inviteParticipantsWithResponse(String callId, InviteParticipantsRequest request, Context context) {
        try {
            Objects.requireNonNull(callId, "'callId' cannot be null.");
            Objects.requireNonNull(request, "'request' cannot be null.");
            return withContext(contextValue -> {
                if (context != null) {
                    contextValue = context;
                }
                return this.callClient.inviteParticipantsWithResponseAsync(callId,
                        InviteParticipantsRequestConverter.convert(request));
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
     * cancelMediaProcessingWithResponse method for use by sync client
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

        List<CallModalityModel> requestedModalities = new ArrayList<>();
        for (CallModality modality : createCallOptions.getRequestedModalities()) {
            requestedModalities.add(CallModalityModel.fromString(modality.toString()));
        }
        List<EventSubscriptionTypeModel> requestedCallEvents = new ArrayList<>();
        for (EventSubscriptionType requestedCallEvent : createCallOptions.getRequestedCallEvents()) {
            requestedCallEvents.add(EventSubscriptionTypeModel.fromString(requestedCallEvent.toString()));
        }

        request.setSource(CommunicationIdentifierConverter.convert(source))
                .setTargets(targetsList.stream().map(target -> CommunicationIdentifierConverter.convert(target))
                        .collect(Collectors.toList()))
                .setCallbackUri(createCallOptions.getCallbackUri()).setRequestedModalities(requestedModalities)
                .setRequestedCallEvents(requestedCallEvents);

        return request;
    }

    private PlayAudioResult convertPlayAudioResponse(
            com.azure.communication.callingserver.implementation.models.PlayAudioResponse response) {
        return new PlayAudioResult().setId(response.getId())
                .setStatus(OperationStatus.fromString(response.getStatus().toString()))
                .setOperationContext(response.getOperationContext())
                .setResultInfo(ResultInfoConverter.convert(response.getResultInfo()));
    }

    private CancelMediaProcessingResult convertCancelMediaProcessingResponse(
            com.azure.communication.callingserver.implementation.models.CancelMediaProcessingResponse response) {
        return new CancelMediaProcessingResult().setId(response.getId())
                .setStatus(OperationStatus.fromString(response.getStatus().toString()))
                .setOperationContext(response.getOperationContext())
                .setResultInfo(ResultInfoConverter.convert(response.getResultInfo()));
    }

    private CancelMediaProcessingRequestInternal convertCancelMediaProcessingRequest(
            CancelMediaProcessingRequest request) {
        return new CancelMediaProcessingRequestInternal().setOperationContext(request.getOperationContext());
    }

    private PlayAudioRequestInternal convertPlayAudioRequest(PlayAudioRequest request) {
        return new PlayAudioRequestInternal().setOperationContext(request.getOperationContext());
    }

    private CreateCallResult convertCreateCallWithResponse(CreateCallResponse response) {
        return new CreateCallResult().setCallLegId(response.getCallLegId());
    }
}
