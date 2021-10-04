// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import static com.azure.core.util.FluxUtil.fluxError;
import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

import java.io.IOException;
import java.io.OutputStream;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousFileChannel;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.security.InvalidParameterException;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.azure.communication.callingserver.implementation.AzureCommunicationCallingServerServiceImpl;
import com.azure.communication.callingserver.implementation.CallConnectionsImpl;
import com.azure.communication.callingserver.implementation.ServerCallsImpl;
import com.azure.communication.callingserver.implementation.converters.CallConnectionRequestConverter;
import com.azure.communication.callingserver.implementation.converters.CallLocatorConverter;
import com.azure.communication.callingserver.implementation.converters.CallParticipantConverter;
import com.azure.communication.callingserver.implementation.converters.CallingServerErrorConverter;
import com.azure.communication.callingserver.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callingserver.implementation.converters.JoinCallRequestConverter;
import com.azure.communication.callingserver.implementation.converters.PhoneNumberIdentifierConverter;
import com.azure.communication.callingserver.implementation.converters.PlayAudioResultConverter;
import com.azure.communication.callingserver.implementation.models.AddParticipantWithCallLocatorRequest;
import com.azure.communication.callingserver.implementation.models.CallRejectReason;
import com.azure.communication.callingserver.implementation.models.CancelMediaOperationWithCallLocatorRequest;
import com.azure.communication.callingserver.implementation.models.CancelParticipantMediaOperationWithCallLocatorRequest;
import com.azure.communication.callingserver.implementation.models.CommunicationErrorResponseException;
import com.azure.communication.callingserver.implementation.models.CreateCallRequest;
import com.azure.communication.callingserver.implementation.models.GetAllParticipantsWithCallLocatorRequest;
import com.azure.communication.callingserver.implementation.models.GetParticipantWithCallLocatorRequest;
import com.azure.communication.callingserver.implementation.models.PlayAudioToParticipantWithCallLocatorRequest;
import com.azure.communication.callingserver.implementation.models.PlayAudioWithCallLocatorRequest;
import com.azure.communication.callingserver.implementation.models.RedirectCallRequest;
import com.azure.communication.callingserver.implementation.models.RejectCallRequest;
import com.azure.communication.callingserver.implementation.models.RemoveParticipantWithCallLocatorRequest;
import com.azure.communication.callingserver.implementation.models.StartCallRecordingWithCallLocatorRequest;
import com.azure.communication.callingserver.models.AddParticipantResult;
import com.azure.communication.callingserver.models.CallLocator;
import com.azure.communication.callingserver.models.CallParticipant;
import com.azure.communication.callingserver.models.CallRecordingProperties;
import com.azure.communication.callingserver.models.CallingServerErrorException;
import com.azure.communication.callingserver.models.CreateCallOptions;
import com.azure.communication.callingserver.models.JoinCallOptions;
import com.azure.communication.callingserver.models.ParallelDownloadOptions;
import com.azure.communication.callingserver.models.PlayAudioOptions;
import com.azure.communication.callingserver.models.PlayAudioResult;
import com.azure.communication.callingserver.models.StartCallRecordingResult;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpRange;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;


/**
 * Asynchronous client that supports calling server operations.
 *
 * <p><strong>Instantiating a asynchronous CallingServer client</strong></p>
 *
 * {@codesnippet com.azure.communication.callingserver.CallingServerAsyncClient.pipeline.instantiation}
 *
 * <p>View {@link CallingServerClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see CallingServerClientBuilder
 */
@ServiceClient(builder = CallingServerClientBuilder.class, isAsync = true)
public final class CallingServerAsyncClient {
    private final CallConnectionsImpl callConnectionInternal;
    private final ServerCallsImpl serverCallInternal;
    private final ClientLogger logger = new ClientLogger(CallingServerAsyncClient.class);
    private final ContentDownloader contentDownloader;

    CallingServerAsyncClient(AzureCommunicationCallingServerServiceImpl callServiceClient) {
        callConnectionInternal = callServiceClient.getCallConnections();
        serverCallInternal = callServiceClient.getServerCalls();

        contentDownloader = new ContentDownloader(
            callServiceClient.getEndpoint(),
            callServiceClient.getHttpPipeline());
    }

    /**
     * Create a call connection request from a source identity to targets identity.
     *
     * @param source The source identity.
     * @param targets The target identities.
     * @param createCallOptions The call options.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     *
     * {@codesnippet com.azure.communication.callingserver.CallingServerAsyncClient.create.call.connection.async}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CallConnectionAsync> createCallConnection(
        CommunicationIdentifier source,
        List<CommunicationIdentifier> targets,
        CreateCallOptions createCallOptions) {
        try {
            Objects.requireNonNull(source, "'source' cannot be null.");
            Objects.requireNonNull(targets, "'targets' cannot be null.");
            Objects.requireNonNull(createCallOptions, "'createCallOptions' cannot be null.");
            CreateCallRequest request = CallConnectionRequestConverter.convert(source, targets, createCallOptions);
            return callConnectionInternal.createCallAsync(request)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(response -> Mono.just(new CallConnectionAsync(response.getCallConnectionId(), callConnectionInternal)));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Create a Call Connection Request from source identity to targets identity.
     *
     * @param source The source identity.
     * @param targets The target identities.
     * @param createCallOptions The call options.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CallConnectionAsync>> createCallConnectionWithResponse(
        CommunicationIdentifier source,
        List<CommunicationIdentifier> targets,
        CreateCallOptions createCallOptions) {
        try {
            Objects.requireNonNull(source, "'source' cannot be null.");
            Objects.requireNonNull(targets, "'targets' cannot be null.");
            Objects.requireNonNull(createCallOptions, "'CreateCallOptions' cannot be null.");
            CreateCallRequest request = CallConnectionRequestConverter.convert(source, targets, createCallOptions);
            return callConnectionInternal.createCallWithResponseAsync(request)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .map(response -> new SimpleResponse<>(response, new CallConnectionAsync(response.getValue().getCallConnectionId(), callConnectionInternal)));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<CallConnection> createCallConnectionInternal(
        CommunicationIdentifier source,
        List<CommunicationIdentifier> targets,
        CreateCallOptions createCallOptions) {
        try {
            Objects.requireNonNull(source, "'source' cannot be null.");
            Objects.requireNonNull(targets, "'targets' cannot be null.");
            Objects.requireNonNull(createCallOptions, "'createCallOptions' cannot be null.");
            CreateCallRequest request = CallConnectionRequestConverter.convert(source, targets, createCallOptions);
            return callConnectionInternal.createCallAsync(request)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(response -> Mono.just(new CallConnection(new CallConnectionAsync(response.getCallConnectionId(), callConnectionInternal))));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<CallConnection>> createCallConnectionWithResponseInternal(
        CommunicationIdentifier source,
        List<CommunicationIdentifier> targets,
        CreateCallOptions createCallOptions,
        Context context) {
        try {
            Objects.requireNonNull(source, "'source' cannot be null.");
            Objects.requireNonNull(targets, "'targets' cannot be null.");
            Objects.requireNonNull(createCallOptions, "'CreateCallOptions' cannot be null.");
            CreateCallRequest request = CallConnectionRequestConverter.convert(source, targets, createCallOptions);
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return callConnectionInternal.createCallWithResponseAsync(request, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                    .map(response -> new SimpleResponse<>(response,
                        new CallConnection(new CallConnectionAsync(response.getValue().getCallConnectionId(), callConnectionInternal))));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Join a Call
     *
     * @param callLocator the call locator.
     * @param source Source identity.
     * @param joinCallOptions Join call options.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful join request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CallConnectionAsync> joinCall(
        CallLocator callLocator,
        CommunicationIdentifier source,
        JoinCallOptions joinCallOptions) {
        try {
            Objects.requireNonNull(source, "'source' cannot be null.");
            Objects.requireNonNull(joinCallOptions, "'joinCallOptions' cannot be null.");
            return serverCallInternal
                .joinCallAsync(JoinCallRequestConverter.convert(callLocator, source, joinCallOptions))
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(response -> Mono.just(new CallConnectionAsync(response.getCallConnectionId(), callConnectionInternal)));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Join a call
     *
     * @param callLocator the call locator.
     * @param source Source identity.
     * @param joinCallOptions Join call options.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful join request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CallConnectionAsync>> joinCallWithResponse(
        CallLocator callLocator,
        CommunicationIdentifier source,
        JoinCallOptions joinCallOptions) {
        try {
            Objects.requireNonNull(source, "'source' cannot be null.");
            Objects.requireNonNull(joinCallOptions, "'joinCallOptions' cannot be null.");
            return serverCallInternal.
                joinCallWithResponseAsync(JoinCallRequestConverter.convert(callLocator, source, joinCallOptions))
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .map(response -> new SimpleResponse<>(response,
                    new CallConnectionAsync(response.getValue().getCallConnectionId(), callConnectionInternal)));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<CallConnection> joinInternal(
        CallLocator callLocator,
        CommunicationIdentifier source,
        JoinCallOptions joinCallOptions) {
        try {
            Objects.requireNonNull(source, "'source' cannot be null.");
            Objects.requireNonNull(joinCallOptions, "'joinCallOptions' cannot be null.");
            return serverCallInternal
                .joinCallAsync(JoinCallRequestConverter.convert(callLocator, source, joinCallOptions))
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(response ->
                    Mono.just(new CallConnection(new CallConnectionAsync(response.getCallConnectionId(),
                        callConnectionInternal))));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<CallConnection>>joinWithResponseInternal(
        CallLocator callLocator,
        CommunicationIdentifier source,
        JoinCallOptions joinCallOptions,
        Context context) {
        try {
            Objects.requireNonNull(source, "'source' cannot be null.");
            Objects.requireNonNull(joinCallOptions, "'joinCallOptions' cannot be null.");

            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return serverCallInternal
                    .joinCallWithResponseAsync(JoinCallRequestConverter.convert(callLocator, source, joinCallOptions), contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                    .map(response ->
                        new SimpleResponse<>(
                            response,
                            new CallConnection(new CallConnectionAsync(response.getValue().getCallConnectionId(),
                                callConnectionInternal))));
            });

        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get CallConnection object
     *
     * @param callConnectionId Call connection id.
     * @return CallConnection object.
     */
    public CallConnectionAsync getCallConnection(String callConnectionId) {
        Objects.requireNonNull(callConnectionId, "'callConnectionId' cannot be null.");
        return new CallConnectionAsync(callConnectionId, callConnectionInternal);
    }

    CallConnection getCallConnectionInternal(String callConnectionId) {
        Objects.requireNonNull(callConnectionId, "'callConnectionId' cannot be null.");
        return new CallConnection(new CallConnectionAsync(callConnectionId, callConnectionInternal));
    }

    /**
     * Add a participant to the call.
     *
     * @param callLocator the call locator.
     * @param participant The identifier of the participant.
     * @param callBackUri callBackUri to get notifications.
     * @param alternateCallerId Phone number to use when adding a phone number participant.
     * @param operationContext Value to identify context of the operation. This is used to co-relate other
     *                         communications related to this operation
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AddParticipantResult> addParticipant(
        CallLocator callLocator,
        CommunicationIdentifier participant,
        URI callBackUri,
        String alternateCallerId,
        String operationContext) {
        try {
            Objects.requireNonNull(participant, "'participant' cannot be null.");
            Objects.requireNonNull(callLocator, "'callLocator' cannot be null.");

            AddParticipantWithCallLocatorRequest requestWithCallLocator = new AddParticipantWithCallLocatorRequest()
            .setCallLocator(CallLocatorConverter.convert(callLocator))
            .setParticipant(CommunicationIdentifierConverter.convert(participant))
            .setAlternateCallerId(PhoneNumberIdentifierConverter.convert(alternateCallerId))
            .setOperationContext(operationContext)
            .setCallbackUri(callBackUri.toString());

            return serverCallInternal.addParticipantAsync(requestWithCallLocator, Context.NONE)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.just(new AddParticipantResult(result.getParticipantId())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Add a participant to the call.
     *
     * @param callLocator the call locator.
     * @param participant The identifier of the participant.
     * @param callBackUri callBackUri to get notifications.
     * @param alternateCallerId Phone number to use when adding a phone number participant.
     * @param operationContext Value to identify context of the operation. This is used to co-relate other
     *                         communications related to this operation
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AddParticipantResult>> addParticipantWithResponse(
        CallLocator callLocator,
        CommunicationIdentifier participant,
        URI callBackUri,
        String alternateCallerId,
        String operationContext) {
        return addParticipantWithResponse(
            callLocator,
            participant,
            callBackUri,
            alternateCallerId,
            operationContext,
            null);
    }

    Mono<Response<AddParticipantResult>> addParticipantWithResponse(
        CallLocator callLocator,
        CommunicationIdentifier participant,
        URI callBackUri,
        String alternateCallerId,
        String operationContext,
        Context context) {
        try {
            Objects.requireNonNull(participant, "'participant' cannot be null.");

            AddParticipantWithCallLocatorRequest requestWithCallLocator = new AddParticipantWithCallLocatorRequest()
            .setCallLocator(CallLocatorConverter.convert(callLocator))
            .setParticipant(CommunicationIdentifierConverter.convert(participant))
            .setAlternateCallerId(PhoneNumberIdentifierConverter.convert(alternateCallerId))
            .setOperationContext(operationContext)
            .setCallbackUri(callBackUri.toString());

            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return serverCallInternal
                    .addParticipantWithResponseAsync(requestWithCallLocator, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                    .map(response ->
                        new SimpleResponse<>(response, new AddParticipantResult(response.getValue().getParticipantId())));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Remove a participant from the call.
     *
     * @param callLocator the call locator.
     * @param participant The identifier of the participant.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful remove participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> removeParticipant(CallLocator callLocator, CommunicationIdentifier participant) {
        try {
            RemoveParticipantWithCallLocatorRequest requestWithCallLocator = getRemoveParticipantWithCallLocatorRequest(callLocator, participant);

            return serverCallInternal.removeParticipantAsync(requestWithCallLocator)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.empty());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Remove a participant from the call.
     *
     * @param callLocator the call locator.
     * @param participant The identifier of the participant.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful remove participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> removeParticipantWithResponse(CallLocator callLocator, CommunicationIdentifier participant) {
        return removeParticipantWithResponse(callLocator, participant, null);
    }

    Mono<Response<Void>> removeParticipantWithResponse(CallLocator callLocator, CommunicationIdentifier participant, Context context) {
        try {
            RemoveParticipantWithCallLocatorRequest requestWithCallLocator = getRemoveParticipantWithCallLocatorRequest(callLocator, participant);
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return serverCallInternal
                    .removeParticipantWithResponseAsync(requestWithCallLocator, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    private RemoveParticipantWithCallLocatorRequest getRemoveParticipantWithCallLocatorRequest(CallLocator callLocator,
            CommunicationIdentifier participant) {
        RemoveParticipantWithCallLocatorRequest requestWithCallLocator = new RemoveParticipantWithCallLocatorRequest()
        .setCallLocator(CallLocatorConverter.convert(callLocator))
        .setIdentifier(CommunicationIdentifierConverter.convert(participant));
        return requestWithCallLocator;
    }

    /**
     * Get participant from the call.
     *
     * @param callLocator the call locator.
     * @param participant The identifier of the participant.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful get participant request.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Mono<List<CallParticipant>> getParticipant(CallLocator callLocator, CommunicationIdentifier participant) {
        try {
            GetParticipantWithCallLocatorRequest requestWithCallLocator = getGetParticipantWithCallLocatorRequest(callLocator, participant);

            return serverCallInternal.getParticipantAsync(requestWithCallLocator)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.just(
                    result.stream().map(CallParticipantConverter::convert).collect(Collectors.toList())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    private GetParticipantWithCallLocatorRequest getGetParticipantWithCallLocatorRequest(CallLocator callLocator,
            CommunicationIdentifier participant) {
        GetParticipantWithCallLocatorRequest requestWithCallLocator = new GetParticipantWithCallLocatorRequest()
            .setCallLocator(CallLocatorConverter.convert(callLocator))
            .setIdentifier(CommunicationIdentifierConverter.convert(participant));
        return requestWithCallLocator;
    }

    /**
     * Get participant from the call using identifier.
     *
     * @param callLocator the call locator.
     * @param participant The identifier of the participant.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful get participant request.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Mono<Response<List<CallParticipant>>> getParticipantWithResponse(CallLocator callLocator, CommunicationIdentifier participant) {
        return getParticipantWithResponse(callLocator, participant, Context.NONE);
    }

    Mono<Response<List<CallParticipant>>> getParticipantWithResponse(CallLocator callLocator, CommunicationIdentifier participant, Context context) {
        try {
            GetParticipantWithCallLocatorRequest requestWithCallLocator = getGetParticipantWithCallLocatorRequest(callLocator, participant);

            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return serverCallInternal.getParticipantWithResponseAsync(requestWithCallLocator, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                    .map(response ->
                        new SimpleResponse<>(response,
                            response.getValue()
                            .stream()
                            .map(CallParticipantConverter::convert)
                            .collect(Collectors.toList()
                        )
                    )
                );
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get all participants of the call.
     *
     * @param callLocator the call locator.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful get participants request.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Mono<List<CallParticipant>> getAllParticipants(CallLocator callLocator) {
        try {
            GetAllParticipantsWithCallLocatorRequest requestWithCallLocator = new GetAllParticipantsWithCallLocatorRequest()
                .setCallLocator(CallLocatorConverter.convert(callLocator));

            return serverCallInternal.getParticipantsAsync(requestWithCallLocator)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.just(
                    result.stream().map(CallParticipantConverter::convert).collect(Collectors.toList())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get all participants of the call.
     *
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful get participants request.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Mono<Response<List<CallParticipant>>> getAllParticipantsWithResponse(CallLocator callLocator) {
        return getParticipantsWithResponse(callLocator, Context.NONE);
    }

    Mono<Response<List<CallParticipant>>> getParticipantsWithResponse(CallLocator callLocator, Context context) {
        try {
            GetAllParticipantsWithCallLocatorRequest requestWithCallLocator = new GetAllParticipantsWithCallLocatorRequest()
                .setCallLocator(CallLocatorConverter.convert(callLocator));

            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return serverCallInternal.getParticipantsWithResponseAsync(requestWithCallLocator, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                    .map(response ->
                        new SimpleResponse<>(response,
                            response.getValue()
                            .stream()
                            .map(CallParticipantConverter::convert)
                            .collect(Collectors.toList()
                        )
                    )
                );
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Start recording of the call.     *
     *
     * @param callLocator the call locator.
     * @param recordingStateCallbackUri Uri to send state change callbacks.
     * @throws InvalidParameterException is recordingStateCallbackUri is absolute uri.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful start recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<StartCallRecordingResult> startRecording(CallLocator callLocator, URI recordingStateCallbackUri) {
        try {
            Objects.requireNonNull(recordingStateCallbackUri, "'recordingStateCallbackUri' cannot be null.");
            if (!Boolean.TRUE.equals(recordingStateCallbackUri.isAbsolute())) {
                throw logger.logExceptionAsError(new InvalidParameterException("'recordingStateCallbackUri' has to be an absolute Uri"));
            }
            StartCallRecordingWithCallLocatorRequest requestWithCallLocator = getStartCallRecordingWithCallLocatorRequest(callLocator,
                    recordingStateCallbackUri);

            return serverCallInternal.startRecordingAsync(requestWithCallLocator, null)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.just(new StartCallRecordingResult(result.getRecordingId())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    private StartCallRecordingWithCallLocatorRequest getStartCallRecordingWithCallLocatorRequest(CallLocator callLocator,
        URI recordingStateCallbackUri) {
        StartCallRecordingWithCallLocatorRequest requestWithCallLocator = new StartCallRecordingWithCallLocatorRequest()
        .setCallLocator(CallLocatorConverter.convert(callLocator))
        .setRecordingStateCallbackUri(recordingStateCallbackUri.toString());
        return requestWithCallLocator;
    }

    /**
     * Start recording of the call.
     *
     * @param callLocator the call locator.
     * @param recordingStateCallbackUri Uri to send state change callbacks.
     * @throws InvalidParameterException is recordingStateCallbackUri is absolute uri.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful start recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<StartCallRecordingResult>> startRecordingWithResponse(CallLocator callLocator, URI recordingStateCallbackUri) {
        return startRecordingWithResponse(callLocator, recordingStateCallbackUri, null);
    }

    Mono<Response<StartCallRecordingResult>> startRecordingWithResponse(
        CallLocator callLocator,
        URI recordingStateCallbackUri,
        Context context) {
        try {
            Objects.requireNonNull(recordingStateCallbackUri, "'recordingStateCallbackUri' cannot be null.");
            if (!Boolean.TRUE.equals(recordingStateCallbackUri.isAbsolute())) {
                throw logger.logExceptionAsError(new InvalidParameterException("'recordingStateCallbackUri' has to be an absolute Uri"));
            }
            StartCallRecordingWithCallLocatorRequest requestWithCallLocator = getStartCallRecordingWithCallLocatorRequest(callLocator,
                    recordingStateCallbackUri);

            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return serverCallInternal
                    .startRecordingWithResponseAsync(requestWithCallLocator, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                    .map(response ->
                        new SimpleResponse<>(response, new StartCallRecordingResult(response.getValue().getRecordingId())));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Stop recording of the call.
     *
     * @param recordingId Recording id to stop.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful stop recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> stopRecording(String recordingId) {
        try {
            return serverCallInternal.stopRecordingAsync(recordingId)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.empty());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Stop recording of the call.
     *
     * @param recordingId Recording id to stop.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful stop recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> stopRecordingWithResponse(String recordingId) {
        return stopRecordingWithResponse(recordingId, Context.NONE);
    }

    Mono<Response<Void>> stopRecordingWithResponse(String recordingId, Context context) {
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return serverCallInternal
                    .stopRecordingWithResponseAsync(recordingId, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Pause recording of the call.
     *
     * @param recordingId Recording id to stop.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful pause recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> pauseRecording(String recordingId) {
        try {
            return serverCallInternal.pauseRecordingAsync(recordingId)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.empty());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Pause recording of the call.
     *
     * @param recordingId Recording id to stop.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful pause recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> pauseRecordingWithResponse(String recordingId) {
        return pauseRecordingWithResponse(recordingId, Context.NONE);
    }

    Mono<Response<Void>> pauseRecordingWithResponse(String recordingId, Context context) {
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return serverCallInternal
                    .pauseRecordingWithResponseAsync(recordingId, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Resume recording of the call.
     *
     * @param recordingId Recording id to stop.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return response for a successful resume recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> resumeRecording(String recordingId) {
        try {
            return serverCallInternal.resumeRecordingAsync(recordingId)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.empty());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Resume recording of the call.
     *
     * @param recordingId Recording id to stop.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return response for a successful resume recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> resumeRecordingWithResponse(String recordingId) {
        return resumeRecordingWithResponse(recordingId, Context.NONE);
    }

    Mono<Response<Void>> resumeRecordingWithResponse(String recordingId, Context context) {
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return serverCallInternal
                    .resumeRecordingWithResponseAsync(recordingId, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException);
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get current recording state by recording id.
     *
     * @param recordingId Recording id to stop.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful get recording state request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CallRecordingProperties> getRecordingState(String recordingId) {
        try {
            return serverCallInternal.getRecordingPropertiesAsync(recordingId)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.just(new CallRecordingProperties(result.getRecordingState())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get current recording state by recording id.
     *
     * @param recordingId Recording id to stop.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful get recording state request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CallRecordingProperties>> getRecordingStateWithResponse(String recordingId) {
        return getRecordingStateWithResponse(recordingId, Context.NONE);
    }

    Mono<Response<CallRecordingProperties>> getRecordingStateWithResponse(String recordingId, Context context) {
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return serverCallInternal
                    .getRecordingPropertiesWithResponseAsync(recordingId, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                    .map(response ->
                        new SimpleResponse<>(response, new CallRecordingProperties(response.getValue().getRecordingState())));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Download the recording content, e.g. Recording's metadata, Recording video, from the ACS endpoint
     * passed as parameter.
     * @param sourceEndpoint - URL where the content is located.
     * @return A {@link Flux} object containing the byte stream of the content requested.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Flux<ByteBuffer> downloadStream(String sourceEndpoint) {
        try {
            Objects.requireNonNull(sourceEndpoint, "'sourceEndpoint' cannot be null");
            return downloadStream(sourceEndpoint, null);
        } catch (RuntimeException ex) {
            return fluxError(logger, ex);
        }
    }

    /**
     * Download the recording content, e.g. Recording's metadata, Recording video, from the ACS endpoint
     * passed as parameter.
     * @param sourceEndpoint - URL where the content is located.
     * @param httpRange - An optional {@link HttpRange} value containing the range of bytes to download. If missing,
     *                  the whole content will be downloaded.
     * @return A {@link Flux} object containing the byte stream of the content requested.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Flux<ByteBuffer> downloadStream(String sourceEndpoint, HttpRange httpRange) {
        try {
            Objects.requireNonNull(sourceEndpoint, "'sourceEndpoint' cannot be null");
            return contentDownloader.downloadStreamWithResponse(sourceEndpoint, httpRange, Context.NONE)
                .map(Response::getValue)
                .flux()
                .flatMap(flux -> flux);
        } catch (RuntimeException ex) {
            return fluxError(logger, ex);
        }
    }

    /**
     * Download the recording content, (e.g. Recording's metadata, Recording video, etc.) from the {@code endpoint}.
     * @param sourceEndpoint - URL where the content is located.
     * @param range - An optional {@link HttpRange} value containing the range of bytes to download. If missing,
     *                  the whole content will be downloaded.
     * @return A {@link Mono} object containing a {@link Response} with the byte stream of the content requested.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Flux<ByteBuffer>>> downloadStreamWithResponse(String sourceEndpoint, HttpRange range) {
        try {
            Objects.requireNonNull(sourceEndpoint, "'sourceEndpoint' cannot be null");
            return contentDownloader.downloadStreamWithResponse(sourceEndpoint, range, Context.NONE);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Download the content located in {@code endpoint} into a file marked by {@code path}.
     * This download will be done using parallel workers.
     * @param sourceEndpoint - ACS URL where the content is located.
     * @param destinationPath - File location.
     * @param parallelDownloadOptions - an optional {@link ParallelDownloadOptions} object to modify how the parallel
     *                               download will work.
     * @param overwrite - True to overwrite the file if it exists.
     * @return Response for a successful downloadTo request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> downloadTo(
        String sourceEndpoint,
        Path destinationPath,
        ParallelDownloadOptions parallelDownloadOptions,
        boolean overwrite) {
        try {
            return downloadToWithResponse(sourceEndpoint, destinationPath, parallelDownloadOptions, overwrite, Context.NONE)
                .then();
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Download the content located in {@code endpoint} into a file marked by {@code path}.
     * This download will be done using parallel workers.
     * @param sourceEndpoint - ACS URL where the content is located.
     * @param destinationPath - File location.
     * @param parallelDownloadOptions - an optional {@link ParallelDownloadOptions} object to modify how the parallel
     *                               download will work.
     * @param overwrite - True to overwrite the file if it exists.
     * @return Response containing the http response information from the download.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> downloadToWithResponse(
        String sourceEndpoint,
        Path destinationPath,
        ParallelDownloadOptions parallelDownloadOptions,
        boolean overwrite) {
        try {
            return downloadToWithResponse(sourceEndpoint, destinationPath, parallelDownloadOptions, overwrite, Context.NONE);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> downloadToWithResponse(
        String sourceEndpoint,
        OutputStream destinationStream,
        HttpRange httpRange,
        Context context) {

        return contentDownloader.downloadToStreamWithResponse(sourceEndpoint, destinationStream, httpRange, context);
    }

    Mono<Response<Void>> downloadToWithResponse(
        String sourceEndpoint,
        Path destinationPath,
        ParallelDownloadOptions parallelDownloadOptions,
        boolean overwrite,
        Context context) {
        Objects.requireNonNull(sourceEndpoint, "'sourceEndpoint' cannot be null");
        Objects.requireNonNull(destinationPath, "'destinationPath' cannot be null");

        Set<OpenOption> openOptions = new HashSet<>();

        if (overwrite) {
            openOptions.add(StandardOpenOption.CREATE);
        } else {
            openOptions.add(StandardOpenOption.CREATE_NEW);
        }
        openOptions.add(StandardOpenOption.WRITE);

        try {
            AsynchronousFileChannel file = AsynchronousFileChannel.open(destinationPath, openOptions, null);
            return downloadToWithResponse(sourceEndpoint, destinationPath, file, parallelDownloadOptions, context);
        } catch (IOException ex) {
            return monoError(logger, new RuntimeException(ex));
        }
    }

    Mono<Response<Void>> downloadToWithResponse(
        String sourceEndpoint,
        Path destinationPath,
        AsynchronousFileChannel fileChannel,
        ParallelDownloadOptions parallelDownloadOptions,
        Context context
    ) {
        ParallelDownloadOptions finalParallelDownloadOptions =
            parallelDownloadOptions == null
                ? new ParallelDownloadOptions()
                : parallelDownloadOptions;

        return Mono.just(fileChannel).flatMap(
            c -> contentDownloader.downloadToFileWithResponse(sourceEndpoint, c, finalParallelDownloadOptions, context))
            .doFinally(signalType -> contentDownloader.downloadToFileCleanup(fileChannel, destinationPath, signalType));
    }

    /**
     * Play audio in a call.
     *
     * @param callLocator The call locator.
     * @param audioFileUri The media resource uri of the play audio request. Currently only Wave file (.wav) format
     *                     audio prompts are supported. More specifically, the audio content in the wave file must
     *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param playAudioOptions Options for play audio.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PlayAudioResult> playAudio(CallLocator callLocator, URI audioFileUri, PlayAudioOptions playAudioOptions) {
        return playAudioInternal(callLocator, audioFileUri, playAudioOptions, Context.NONE);
    }

    Mono<PlayAudioResult> playAudioInternal(CallLocator callLocator, URI audioFileUri, PlayAudioOptions playAudioOptions, Context context) {
        try {
            Objects.requireNonNull(callLocator, "'callLocator' cannot be null.");
            Objects.requireNonNull(audioFileUri, "'audioFileUri' cannot be null.");

            PlayAudioWithCallLocatorRequest requestWithCallLocator = getPlayAudioWithCallLocatorRequest(callLocator, audioFileUri,
                    playAudioOptions);

            return serverCallInternal.playAudioAsync(requestWithCallLocator, context)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.just(PlayAudioResultConverter.convert(result)));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Play audio in a call.
     *
     * @param callLocator The server call id.
     * @param audioFileUri The media resource uri of the play audio request. Currently only Wave file (.wav) format
     *                     audio prompts are supported. More specifically, the audio content in the wave file must
     *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param playAudioOptions Options for play audio.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PlayAudioResult>> playAudioWithResponse(
        CallLocator callLocator,
        URI audioFileUri,
        PlayAudioOptions playAudioOptions) {
        return playAudioWithResponseInternal(callLocator, audioFileUri, playAudioOptions, Context.NONE);
    }

    Mono<Response<PlayAudioResult>> playAudioWithResponseInternal(
        CallLocator callLocator,
        URI audioFileUri,
        PlayAudioOptions playAudioOptions,
        Context context) {
        try {
            Objects.requireNonNull(callLocator, "'callLocator' cannot be null.");
            Objects.requireNonNull(audioFileUri, "'audioFileUri' cannot be null.");

            PlayAudioWithCallLocatorRequest requestWithCallLocator = getPlayAudioWithCallLocatorRequest(callLocator, audioFileUri,
                    playAudioOptions);

            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return serverCallInternal
                    .playAudioWithResponseAsync(requestWithCallLocator, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class,
                                CallingServerErrorConverter::translateException)
                    .map(response ->
                        new SimpleResponse<>(response, PlayAudioResultConverter.convert(response.getValue())));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    private PlayAudioWithCallLocatorRequest getPlayAudioWithCallLocatorRequest(CallLocator callLocator, URI audioFileUri,
            PlayAudioOptions playAudioOptions) {

        PlayAudioWithCallLocatorRequest requestWithCallLocator = new PlayAudioWithCallLocatorRequest()
        .setCallLocator(CallLocatorConverter.convert(callLocator))
        .setAudioFileUri(audioFileUri.toString());

        if (playAudioOptions != null) {
            requestWithCallLocator
                .setLoop(playAudioOptions.isLoop())
                .setOperationContext(playAudioOptions.getOperationContext())
                .setAudioFileId(playAudioOptions.getAudioFileId())
                .setCallbackUri(playAudioOptions.getCallbackUri().toString());
        }
        return requestWithCallLocator;
    }

    /**
     * Cancel Media Operation.
     *
     * @param callLocator The server call id.
     * @param mediaOperationId The Id of the media operation to Cancel.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> cancelMediaOperation(
        CallLocator callLocator,
        String mediaOperationId) {
        return cancelMediaOperationWithResponseInternal(callLocator, mediaOperationId, Context.NONE).block();
    }

    /**
     * Cancel Media Operation.
     *
     * @param callLocator The server call id.
     * @param mediaOperationId The Id of the media operation to Cancel.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> cancelMediaOperationWithResponse(
        CallLocator callLocator,
        String mediaOperationId) {
        return cancelMediaOperationWithResponseInternal(callLocator, mediaOperationId, Context.NONE);
    }

    /**
     * Cancel Media Operation.
     *
     * @param callLocator The server call id.
     * @param mediaOperationId The Id of the media operation to Cancel.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> cancelParticipantMediaOperation(
        CallLocator callLocator,
        String mediaOperationId) {
        return cancelMediaOperationWithResponseInternal(callLocator, mediaOperationId, Context.NONE).block();
    }

    Mono<Response<Void>> cancelMediaOperationWithResponseInternal(
        CallLocator callLocator,
        String mediaOperationId,
        Context context) {
        try {
            Objects.requireNonNull(callLocator, "'callLocator' cannot be null.");
            Objects.requireNonNull(mediaOperationId, "'mediaOperationId' cannot be null.");

            CancelMediaOperationWithCallLocatorRequest requestWithCallLocator = new CancelMediaOperationWithCallLocatorRequest()
            .setCallLocator(CallLocatorConverter.convert(callLocator))
            .setMediaOperationId(mediaOperationId);

            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return serverCallInternal
                    .cancelMediaOperationAsync(requestWithCallLocator, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class,
                                CallingServerErrorConverter::translateException)
                    .flatMap(result -> Mono.empty());
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Cancel Participant Media Operation.
     *
     * @param callLocator The server call id.
     * @param participant The identifier of the participant.
     * @param mediaOperationId The Id of the media operation to Cancel.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> cancelParticipantMediaOperationWithResponse(
        CallLocator callLocator,
        CommunicationIdentifier participant,
        String mediaOperationId) {
        return cancelParticipantMediaOperationWithResponseInternal(callLocator, participant, mediaOperationId, Context.NONE);
    }

    /**
     * Cancel Participant Media Operation.
     *
     * @param callLocator The server call id.
     * @param participant The identifier of the participant.
     * @param mediaOperationId The Id of the media operation to Cancel.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> cancelParticipantMediaOperation(
        CallLocator callLocator,
        CommunicationIdentifier participant,
        String mediaOperationId) {
        return cancelParticipantMediaOperationWithResponseInternal(callLocator, participant, mediaOperationId, Context.NONE).block();
    }

    Mono<Response<Void>> cancelParticipantMediaOperationWithResponseInternal(
        CallLocator callLocator,
        CommunicationIdentifier participant,
        String mediaOperationId,
        Context context) {
        try {

            CancelParticipantMediaOperationWithCallLocatorRequest requestWithCallLocator = new CancelParticipantMediaOperationWithCallLocatorRequest()
            .setCallLocator(CallLocatorConverter.convert(callLocator))
            .setIdentifier(CommunicationIdentifierConverter.convert(participant))
            .setMediaOperationId(mediaOperationId);

            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return serverCallInternal
                    .cancelParticipantMediaOperationAsync(requestWithCallLocator, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class,
                                CallingServerErrorConverter::translateException)
                    .flatMap(result -> Mono.empty());
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Play audio to a participant.
     *
     * @param callLocator The server call id.
     * @param participant The identifier of the participant.
     * @param audioFileUri The media resource uri of the play audio request. Currently only Wave file (.wav) format
     *                     audio prompts are supported. More specifically, the audio content in the wave file must
     *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param playAudioOptions Options for play audio.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio to participant operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PlayAudioResult> playAudioToParticipant(CallLocator callLocator, CommunicationIdentifier participant, URI audioFileUri, PlayAudioOptions playAudioOptions) {
        return playAudioToParticipantInternal(callLocator, participant, audioFileUri, playAudioOptions, Context.NONE);
    }

    Mono<PlayAudioResult> playAudioToParticipantInternal(CallLocator callLocator, CommunicationIdentifier participant, URI audioFileUri,
            PlayAudioOptions playAudioOptions, Context context) {
        try {
            Objects.requireNonNull(callLocator, "'callLocator' cannot be null.");
            Objects.requireNonNull(participant, "'participant' cannot be null.");
            Objects.requireNonNull(audioFileUri, "'audioFileUri' cannot be null.");

            PlayAudioToParticipantWithCallLocatorRequest requestWithCallLocator= new PlayAudioToParticipantWithCallLocatorRequest()
            .setCallLocator(CallLocatorConverter.convert(callLocator))
            .setIdentifier(CommunicationIdentifierConverter.convert(participant))
            .setAudioFileUri(audioFileUri.toString());

            if (playAudioOptions != null) {
                requestWithCallLocator
                    .setLoop(playAudioOptions.isLoop())
                    .setOperationContext(playAudioOptions.getOperationContext())
                    .setAudioFileId(playAudioOptions.getAudioFileId())
                    .setCallbackUri(playAudioOptions.getCallbackUri().toString());
            }

            return serverCallInternal.participantPlayAudioAsync(requestWithCallLocator, context)
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.just(PlayAudioResultConverter.convert(result)));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Play audio to a participant.
     *
     * @param callLocator The server call id.
     * @param participant The identifier of the participant.
     * @param audioFileUri The media resource uri of the play audio request. Currently only Wave file (.wav) format
     *                     audio prompts are supported. More specifically, the audio content in the wave file must
     *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param playAudioOptions Options for play audio.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PlayAudioResult>> playAudioToParticipantWithResponse(
        CallLocator callLocator,
        CommunicationIdentifier participant,
        URI audioFileUri,
        PlayAudioOptions playAudioOptions) {
        return playAudioToParticipantWithResponseInternal(callLocator, participant, audioFileUri, playAudioOptions, Context.NONE);
    }

    Mono<Response<PlayAudioResult>> playAudioToParticipantWithResponseInternal(
        CallLocator callLocator,
        CommunicationIdentifier participant,
        URI audioFileUri,
        PlayAudioOptions playAudioOptions,
        Context context) {
        try {
            Objects.requireNonNull(callLocator, "'callLocator' cannot be null.");
            Objects.requireNonNull(participant, "'participant' cannot be null.");
            Objects.requireNonNull(audioFileUri, "'audioFileUri' cannot be null.");

            PlayAudioToParticipantWithCallLocatorRequest requestWithCallLocator = new PlayAudioToParticipantWithCallLocatorRequest()
            .setCallLocator(CallLocatorConverter.convert(callLocator))
            .setIdentifier(CommunicationIdentifierConverter.convert(participant))
            .setAudioFileUri(audioFileUri.toString());

            if (playAudioOptions != null) {
                requestWithCallLocator
                    .setLoop(playAudioOptions.isLoop())
                    .setOperationContext(playAudioOptions.getOperationContext())
                    .setAudioFileId(playAudioOptions.getAudioFileId())
                    .setCallbackUri(playAudioOptions.getCallbackUri().toString());
            }

            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return serverCallInternal
                    .participantPlayAudioWithResponseAsync(requestWithCallLocator, contextValue)
                    .onErrorMap(CommunicationErrorResponseException.class,
                                CallingServerErrorConverter::translateException)
                    .map(response ->
                        new SimpleResponse<>(response, PlayAudioResultConverter.convert(response.getValue())));
            });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Redirect the call.
     *
     * @param incomingCallContext the incomingCallContext value to set.
     * @param targets the targets value to set.
     * @param callbackUrl the callbackUrl value to set.
     * @param timeout the timeout value to set.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> redirectCall(String incomingCallContext, List<CommunicationIdentifier> targets, URI callbackUri, Integer timeout) {
        try {
            RedirectCallRequest request = getRedirectCallRequest(incomingCallContext, targets, callbackUri, timeout);

            return serverCallInternal.redirectCallAsync(request)
            .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    private RedirectCallRequest getRedirectCallRequest(String incomingCallContext, List<CommunicationIdentifier> targets,
            URI callbackUri, Integer timeout) {
        Objects.requireNonNull(incomingCallContext, "'redirectCallRequest' cannot be null.");
        Objects.requireNonNull(targets, "'targets' cannot be null.");
        RedirectCallRequest request = new RedirectCallRequest()
            .setCallbackUrl(callbackUri.toString())
            .setIncomingCallContext(incomingCallContext)
            .setTimeout(timeout)
            .setTargets(targets
                .stream()
                .map(CommunicationIdentifierConverter::convert)
                .collect(Collectors.toList()));
        return request;
    }

    /**
     * Redirect the call.
     *
     * @param incomingCallContext the incomingCallContext value to set.
     * @param targets the targets value to set.
     * @param callbackUrl the callbackUrl value to set.
     * @param timeout the timeout value to set.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> redirectCallWithResponse(String incomingCallContext, List<CommunicationIdentifier> targets, URI callbackUri, Integer timeout) {
        return redirectCallWithResponseInternal(incomingCallContext, targets, callbackUri, timeout, Context.NONE);
    }

    Mono<Response<Void>> redirectCallWithResponseInternal(String incomingCallContext, List<CommunicationIdentifier> targets, URI callbackUri, Integer timeout, Context context) {
        try {
            RedirectCallRequest request = getRedirectCallRequest(incomingCallContext, targets, callbackUri, timeout);
            return serverCallInternal.redirectCallWithResponseAsync(request, context)
            .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

   /**
     * Redirect the call.
     *
     * @param incomingCallContext the incomingCallContext value to set.
     * @param callbackUrl the callbackUrl value to set.
     * @param rejectReason the call reject reason value to set.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> rejectCall(String incomingCallContext, URI callbackUri, CallRejectReason rejectReason) {
        try {
            RejectCallRequest request = getRejectCallRequest(incomingCallContext, callbackUri, rejectReason);

            return serverCallInternal.rejectCallAsync(request)
            .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    private RejectCallRequest getRejectCallRequest(String incomingCallContext, URI callbackUri, CallRejectReason rejectReason) {
        Objects.requireNonNull(incomingCallContext, "'redirectCallRequest' cannot be null.");
        RejectCallRequest request = new RejectCallRequest()
            .setCallbackUrl(callbackUri.toString())
            .setIncomingCallContext(incomingCallContext)
            .setCallRejectReason(rejectReason);
        return request;
    }

    /**
     * Redirect the call.
     *
     * @param incomingCallContext the incomingCallContext value to set.
     * @param callbackUrl the callbackUrl value to set.
     * @param rejectReason the call reject reason value to set.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> rejectCallWithResponse(String incomingCallContext, URI callbackUri, CallRejectReason rejectReason) {
        return rejectCallWithResponseInternal(incomingCallContext, callbackUri, rejectReason, Context.NONE);
    }

    Mono<Response<Void>> rejectCallWithResponseInternal(String incomingCallContext, URI callbackUri, CallRejectReason rejectReason, Context context) {
        try {
            RejectCallRequest request = getRejectCallRequest(incomingCallContext, callbackUri, rejectReason);
            return serverCallInternal.rejectCallWithResponseAsync(request, context)
            .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }
}
