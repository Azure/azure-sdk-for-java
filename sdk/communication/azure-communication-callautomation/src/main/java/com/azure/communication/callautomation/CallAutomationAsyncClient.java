// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.implementation.AzureCommunicationCallAutomationServiceImpl;
import com.azure.communication.callautomation.implementation.CallConnectionsImpl;
import com.azure.communication.callautomation.implementation.CallMediasImpl;
import com.azure.communication.callautomation.implementation.CallRecordingsImpl;
import com.azure.communication.callautomation.implementation.accesshelpers.CallConnectionPropertiesConstructorProxy;
import com.azure.communication.callautomation.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callautomation.implementation.converters.CommunicationUserIdentifierConverter;
import com.azure.communication.callautomation.implementation.converters.PhoneNumberIdentifierConverter;
import com.azure.communication.callautomation.implementation.models.AnswerCallRequestInternal;
import com.azure.communication.callautomation.implementation.models.CallIntelligenceOptionsInternal;
import com.azure.communication.callautomation.implementation.models.CallRejectReasonInternal;
import com.azure.communication.callautomation.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.callautomation.implementation.models.CommunicationUserIdentifierModel;
import com.azure.communication.callautomation.implementation.models.CreateCallRequestInternal;
import com.azure.communication.callautomation.implementation.models.MediaStreamingAudioChannelTypeInternal;
import com.azure.communication.callautomation.implementation.models.MediaStreamingContentTypeInternal;
import com.azure.communication.callautomation.implementation.models.MediaStreamingOptionsInternal;
import com.azure.communication.callautomation.implementation.models.MediaStreamingTransportTypeInternal;
import com.azure.communication.callautomation.implementation.models.RedirectCallRequestInternal;
import com.azure.communication.callautomation.implementation.models.RejectCallRequestInternal;
import com.azure.communication.callautomation.implementation.models.ConnectRequestInternal;
import com.azure.communication.callautomation.implementation.models.CallLocatorInternal;
import com.azure.communication.callautomation.implementation.models.CallLocatorKindInternal;
import com.azure.communication.callautomation.implementation.models.TranscriptionOptionsInternal;
import com.azure.communication.callautomation.implementation.models.TranscriptionTransportTypeInternal;
import com.azure.communication.callautomation.models.AnswerCallOptions;
import com.azure.communication.callautomation.models.AnswerCallResult;
import com.azure.communication.callautomation.models.CallInvite;
import com.azure.communication.callautomation.models.CreateCallOptions;
import com.azure.communication.callautomation.models.CreateCallResult;
import com.azure.communication.callautomation.models.CreateGroupCallOptions;
import com.azure.communication.callautomation.models.MediaStreamingOptions;
import com.azure.communication.callautomation.models.RedirectCallOptions;
import com.azure.communication.callautomation.models.RejectCallOptions;
import com.azure.communication.callautomation.models.ConnectCallOptions;
import com.azure.communication.callautomation.models.CallLocator;
import com.azure.communication.callautomation.models.CallLocatorKind;
import com.azure.communication.callautomation.models.GroupCallLocator;
import com.azure.communication.callautomation.models.ServerCallLocator;
import com.azure.communication.callautomation.models.RoomCallLocator;
import com.azure.communication.callautomation.models.ConnectCallResult;
import com.azure.communication.callautomation.models.TranscriptionOptions;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.net.URISyntaxException;
import java.security.InvalidParameterException;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * Asynchronous client that supports calling server operations.
 *
 * <p><strong>Instantiating a asynchronous CallingServer client</strong></p>
 *
 * <p>View {@link CallAutomationClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see CallAutomationClientBuilder
 */
@ServiceClient(builder = CallAutomationClientBuilder.class, isAsync = true)
public final class CallAutomationAsyncClient {
    private final CallConnectionsImpl callConnectionsInternal;
    private final AzureCommunicationCallAutomationServiceImpl azureCommunicationCallAutomationServiceInternal;
    private final CallRecordingsImpl callRecordingsInternal;
    private final CallMediasImpl callMediasInternal;
    private final ClientLogger logger;
    private final ContentDownloader contentDownloader;
    private final HttpPipeline httpPipelineInternal;
    private final String resourceUrl;
    private final CommunicationUserIdentifierModel sourceIdentity;

    CallAutomationAsyncClient(AzureCommunicationCallAutomationServiceImpl callServiceClient, CommunicationUserIdentifier sourceIdentity) {
        this.callConnectionsInternal = callServiceClient.getCallConnections();
        this.azureCommunicationCallAutomationServiceInternal = callServiceClient;
        this.callRecordingsInternal = callServiceClient.getCallRecordings();
        this.callMediasInternal = callServiceClient.getCallMedias();
        this.logger = new ClientLogger(CallAutomationAsyncClient.class);
        this.contentDownloader = new ContentDownloader(callServiceClient.getEndpoint(), callServiceClient.getHttpPipeline());
        this.httpPipelineInternal = callServiceClient.getHttpPipeline();
        this.resourceUrl = callServiceClient.getEndpoint();
        this.sourceIdentity = sourceIdentity == null ? null : CommunicationUserIdentifierConverter.convert(sourceIdentity);
    }

    /**
     * Get Source Identity that is used for create and answer call
     * @return {@link CommunicationUserIdentifier} represent source
     */
    public CommunicationUserIdentifier getSourceIdentity() {
        return sourceIdentity == null ? null : CommunicationUserIdentifierConverter.convert(sourceIdentity);
    }

    //region Pre-call Actions
    /**
     * Create a call connection request from a source identity to a target identity.
     *
     * @param targetParticipant Call invitee's information
     * @param callbackUrl The call back url for receiving events.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Result of creating the call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CreateCallResult> createCall(CallInvite targetParticipant,
                                             String callbackUrl) {
        CreateCallOptions createCallOptions = new CreateCallOptions(targetParticipant, callbackUrl);
        return createCallWithResponse(createCallOptions).flatMap(FluxUtil::toMono);
    }

    /**
     * Create a call connection request from a source identity to a list of target identity.
     *
     * @param targetParticipants The list of targetParticipants.
     * @param callbackUrl The call back url for receiving events.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Result of creating the call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CreateCallResult> createGroupCall(List<CommunicationIdentifier> targetParticipants,
                                                  String callbackUrl) {
        CreateGroupCallOptions createGroupCallOptions = new CreateGroupCallOptions(targetParticipants, callbackUrl);
        return createGroupCallWithResponse(createGroupCallOptions).flatMap(FluxUtil::toMono);
    }

    /**
     * Create a call connection request from a source identity to a target identity.
     *
     * @param createCallOptions Options for creating a new call.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response with result of creating the call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CreateCallResult>> createCallWithResponse(CreateCallOptions createCallOptions) {
        return withContext(context -> createCallWithResponseInternal(createCallOptions, context));
    }

    /**
     * Create a group call connection request from a source identity to multiple identities.
     *
     * @param createGroupCallOptions Options for creating a new group call.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response with result of creating the call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CreateCallResult>> createGroupCallWithResponse(CreateGroupCallOptions createGroupCallOptions) {
        return withContext(context -> createGroupCallWithResponseInternal(createGroupCallOptions, context));
    }

    Mono<Response<CreateCallResult>> createCallWithResponseInternal(CreateCallOptions createCallOptions, Context context) {
        return getCreateCallResponseMono(context, getCreateCallRequestInternal(createCallOptions));
    }

    Mono<Response<CreateCallResult>> createGroupCallWithResponseInternal(CreateGroupCallOptions createGroupCallOptions, Context context) {
        return getCreateCallResponseMono(context, getCreateCallRequestInternal(createGroupCallOptions));
    }

    private Mono<Response<CreateCallResult>> getCreateCallResponseMono(Context context, CreateCallRequestInternal createCallRequestInternal) {
        try {
            context = context == null ? Context.NONE : context;
            return azureCommunicationCallAutomationServiceInternal.createCallWithResponseAsync(
                    createCallRequestInternal,
                    context)
                .map(response -> {
                    try {
                        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(response.getValue().getCallConnectionId());

                        return new SimpleResponse<>(response,
                            new CreateCallResult(CallConnectionPropertiesConstructorProxy.create(response.getValue()),
                                new CallConnection(callConnectionAsync), callConnectionAsync));
                    } catch (URISyntaxException e) {
                        throw logger.logExceptionAsError(new RuntimeException(e));
                    }
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    private CreateCallRequestInternal getCreateCallRequestInternal(CreateCallOptions createCallOptions) {
        List<CommunicationIdentifierModel> targetsModel = new LinkedList<CommunicationIdentifierModel>();
        targetsModel.add(CommunicationIdentifierConverter.convert(createCallOptions.getCallInvite().getTargetParticipant()));

        CallIntelligenceOptionsInternal callIntelligenceOptionsInternal = null;
        if (createCallOptions.getCallIntelligenceOptions() != null && createCallOptions.getCallIntelligenceOptions().getCognitiveServicesEndpoint() != null) {
            callIntelligenceOptionsInternal = new CallIntelligenceOptionsInternal();
            callIntelligenceOptionsInternal.setCognitiveServicesEndpoint(createCallOptions.getCallIntelligenceOptions().getCognitiveServicesEndpoint());
        }

        CreateCallRequestInternal request = new CreateCallRequestInternal()
            .setSourceCallerIdNumber(PhoneNumberIdentifierConverter.convert(createCallOptions.getCallInvite().getSourceCallerIdNumber()))
            .setSourceDisplayName(createCallOptions.getCallInvite().getSourceDisplayName())
            .setSource(sourceIdentity)
            .setTargets(targetsModel)
            .setCallbackUri(createCallOptions.getCallbackUrl())
            .setCallIntelligenceOptions(callIntelligenceOptionsInternal)
            .setOperationContext(createCallOptions.getOperationContext());        

        if (createCallOptions.getMediaStreamingOptions() != null) {
            MediaStreamingOptionsInternal streamingOptionsInternal =
                getMediaStreamingOptionsInternal(createCallOptions.getMediaStreamingOptions());
            request.setMediaStreamingOptions(streamingOptionsInternal);
        }

        if (createCallOptions.getTranscriptionOptions() != null) {
            TranscriptionOptionsInternal  transcriptionOptionsInternal =
                getTranscriptionOptionsInternal(createCallOptions.getTranscriptionOptions());
            request.setTranscriptionOptions(transcriptionOptionsInternal);
        }

        return request;
    }

    private CreateCallRequestInternal getCreateCallRequestInternal(CreateGroupCallOptions createCallGroupOptions) {
        List<CommunicationIdentifierModel> targetsModel = createCallGroupOptions.getTargetParticipants()
            .stream().map(CommunicationIdentifierConverter::convert).collect(Collectors.toList());

        CallIntelligenceOptionsInternal callIntelligenceOptionsInternal = null;
        if (createCallGroupOptions.getCallIntelligenceOptions() != null && createCallGroupOptions.getCallIntelligenceOptions().getCognitiveServicesEndpoint() != null) {
            callIntelligenceOptionsInternal = new CallIntelligenceOptionsInternal();
            callIntelligenceOptionsInternal.setCognitiveServicesEndpoint(createCallGroupOptions.getCallIntelligenceOptions().getCognitiveServicesEndpoint());
        }

        CreateCallRequestInternal request = new CreateCallRequestInternal()
            .setSourceCallerIdNumber(PhoneNumberIdentifierConverter.convert(createCallGroupOptions.getSourceCallIdNumber()))
            .setSourceDisplayName(createCallGroupOptions.getSourceDisplayName())
            .setSource(sourceIdentity)
            .setTargets(targetsModel)
            .setCallbackUri(createCallGroupOptions.getCallbackUrl())
            .setCallIntelligenceOptions(callIntelligenceOptionsInternal)
            .setOperationContext(createCallGroupOptions.getOperationContext());

        if (createCallGroupOptions.getMediaStreamingOptions() != null) {
            MediaStreamingOptionsInternal streamingOptionsInternal =
                getMediaStreamingOptionsInternal(createCallGroupOptions.getMediaStreamingOptions());
            request.setMediaStreamingOptions(streamingOptionsInternal);
        }

        if (createCallGroupOptions.getTranscriptionOptions() != null) {
            TranscriptionOptionsInternal transcriptionOptionsInternal =
                getTranscriptionOptionsInternal(createCallGroupOptions.getTranscriptionOptions());
            request.setTranscriptionOptions(transcriptionOptionsInternal);
        }

        return request;
    }

    private MediaStreamingOptionsInternal getMediaStreamingOptionsInternal(
        MediaStreamingOptions mediaStreamingOptions) {
        return new MediaStreamingOptionsInternal()
            .setTransportUrl(mediaStreamingOptions.getTransportUrl())
            .setAudioChannelType(
                MediaStreamingAudioChannelTypeInternal.fromString(
                    mediaStreamingOptions.getAudioChannelType().toString()))
            .setContentType(
                MediaStreamingContentTypeInternal.fromString(
                    mediaStreamingOptions.getContentType().toString()))
            .setTransportType(
                MediaStreamingTransportTypeInternal.fromString(
                    mediaStreamingOptions.getTransportType().toString()))
            .setStartMediaStreaming(mediaStreamingOptions.isStartMediaStreamingEnabled());
    }

    private TranscriptionOptionsInternal getTranscriptionOptionsInternal(
        TranscriptionOptions transcriptionOptions) {
        return new TranscriptionOptionsInternal()
            .setTransportUrl(transcriptionOptions.getTransportUrl())
            .setTransportType(
                TranscriptionTransportTypeInternal.fromString(
                    transcriptionOptions.getTransportType().toString()))
            .setLocale(transcriptionOptions.getLocale())
            .setStartTranscription(transcriptionOptions.getStartTranscription())
            .setEnableIntermediateResults(transcriptionOptions.isIntermediateResultsEnabled())
            .setSpeechRecognitionModelEndpointId(transcriptionOptions.getSpeechRecognitionModelEndpointId());
    }

    /**
     * Answer an incoming call
     *
     * @param incomingCallContext The incoming call context.
     * @param callbackUrl The call back url.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Result of answering the call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AnswerCallResult> answerCall(String incomingCallContext, String callbackUrl) {
        return answerCallWithResponse(new AnswerCallOptions(incomingCallContext, callbackUrl))
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Create a call connection request from a source identity to a target identity.
     *
     * @param answerCallOptions The options of answering the call.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response with result of answering the call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AnswerCallResult>> answerCallWithResponse(AnswerCallOptions answerCallOptions) {
        return withContext(context -> answerCallWithResponseInternal(answerCallOptions, context));
    }

    Mono<Response<AnswerCallResult>> answerCallWithResponseInternal(AnswerCallOptions answerCallOptions,
                                                                    Context context) {
        try {
            context = context == null ? Context.NONE : context;

            AnswerCallRequestInternal request = new AnswerCallRequestInternal()
                .setIncomingCallContext(answerCallOptions.getIncomingCallContext())
                .setCallbackUri(answerCallOptions.getCallbackUrl())
                .setAnsweredBy(sourceIdentity)
                .setOperationContext(answerCallOptions.getOperationContext());

            if (answerCallOptions.getCallIntelligenceOptions() != null && answerCallOptions.getCallIntelligenceOptions().getCognitiveServicesEndpoint() != null) {
                CallIntelligenceOptionsInternal callIntelligenceOptionsInternal = new CallIntelligenceOptionsInternal();
                callIntelligenceOptionsInternal.setCognitiveServicesEndpoint(answerCallOptions.getCallIntelligenceOptions().getCognitiveServicesEndpoint());
                request.setCallIntelligenceOptions(callIntelligenceOptionsInternal);
            }

            if (answerCallOptions.getMediaStreamingOptions() != null) {
                MediaStreamingOptionsInternal streamingOptionsInternal =
                    getMediaStreamingOptionsInternal(answerCallOptions.getMediaStreamingOptions());
                request.setMediaStreamingOptions(streamingOptionsInternal);
            }

            if (answerCallOptions.getTranscriptionOptions() != null) {
                TranscriptionOptionsInternal transcriptionOptionsInternal =
                    getTranscriptionOptionsInternal(answerCallOptions.getTranscriptionOptions());
                request.setTranscriptionOptions(transcriptionOptionsInternal);
            }

            return azureCommunicationCallAutomationServiceInternal.answerCallWithResponseAsync(
                    request,
                    context)
                .map(response -> {
                    try {
                        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(response.getValue().getCallConnectionId());
                        return new SimpleResponse<>(response,
                            new AnswerCallResult(CallConnectionPropertiesConstructorProxy.create(response.getValue()),
                                new CallConnection(callConnectionAsync), callConnectionAsync));
                    } catch (URISyntaxException e) {
                        throw logger.logExceptionAsError(new RuntimeException(e));
                    }
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Redirect a call
     *
     * @param incomingCallContext The incoming call context.
     * @param targetParticipant {@link CallInvite} represent redirect targetParticipant
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Void
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> redirectCall(String incomingCallContext, CallInvite targetParticipant) {
        RedirectCallOptions redirectCallOptions = new RedirectCallOptions(incomingCallContext, targetParticipant);
        return redirectCallWithResponse(redirectCallOptions).flatMap(FluxUtil::toMono);
    }

    /**
     * Redirect a call
     *
     * @param redirectCallOptions Options for redirecting a call.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response with Void.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> redirectCallWithResponse(RedirectCallOptions redirectCallOptions) {
        return withContext(context -> redirectCallWithResponseInternal(redirectCallOptions, context));
    }

    Mono<Response<Void>> redirectCallWithResponseInternal(RedirectCallOptions redirectCallOptions, Context context) {
        try {
            context = context == null ? Context.NONE : context;

            RedirectCallRequestInternal request = new RedirectCallRequestInternal()
                .setIncomingCallContext(redirectCallOptions.getIncomingCallContext())
                .setTarget(CommunicationIdentifierConverter.convert(redirectCallOptions.getTargetParticipant().getTargetParticipant()));

            return azureCommunicationCallAutomationServiceInternal.redirectCallWithResponseAsync(
                    request,
                    context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Reject a call
     *
     * @param incomingCallContext The incoming call context.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Void
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> rejectCall(String incomingCallContext) {
        RejectCallOptions rejectCallOptions = new RejectCallOptions(incomingCallContext);
        return rejectCallWithResponse(rejectCallOptions).flatMap(FluxUtil::toMono);
    }

    /**
     * Reject a call
     *
     * @param rejectCallOptions the options of rejecting the call
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response with Void.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> rejectCallWithResponse(RejectCallOptions rejectCallOptions) {
        return withContext(context -> rejectCallWithResponseInternal(rejectCallOptions, context));
    }

    Mono<Response<Void>> rejectCallWithResponseInternal(RejectCallOptions rejectCallOptions, Context context) {
        try {
            context = context == null ? Context.NONE : context;

            RejectCallRequestInternal request = new RejectCallRequestInternal()
                .setIncomingCallContext(rejectCallOptions.getIncomingCallContext());
            if (rejectCallOptions.getCallRejectReason() != null) {
                request.setCallRejectReason(CallRejectReasonInternal.fromString(rejectCallOptions.getCallRejectReason().toString()));
            }

            return azureCommunicationCallAutomationServiceInternal.rejectCallWithResponseAsync(
                    request,
                    context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Create a connect request.
     *
     * @param callLocator Call locator.
     * @param callbackUrl The call back url.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Result of connect request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<ConnectCallResult> connectCall(CallLocator callLocator, String callbackUrl) {
        return connectCallWithResponse(new ConnectCallOptions(callLocator, callbackUrl))
            .flatMap(FluxUtil::toMono);
    }

    /**
     * Create a connect request.
     *
     * @param connectCallOptions Options for connect request.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response with result of connect.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<ConnectCallResult>> connectCallWithResponse(ConnectCallOptions connectCallOptions) {
        return withContext(context -> connectCallWithResponseInternal(connectCallOptions, context));
    }

    Mono<Response<ConnectCallResult>> connectCallWithResponseInternal(ConnectCallOptions connectCallOptions,
                                                                    Context context) {
        try {
            context = context == null ? Context.NONE : context;

            CallLocator callLocator = connectCallOptions.getCallLocator();
            CallLocatorInternal callLocatorInternal = new CallLocatorInternal()
                .setKind(CallLocatorKindInternal.fromString(callLocator.getKind().toString()));

            if (callLocator.getKind() == CallLocatorKind.GROUP_CALL_LOCATOR) {
                callLocatorInternal.setGroupCallId(((GroupCallLocator) callLocator).getGroupCallId());
            } else if (callLocator.getKind() == CallLocatorKind.SERVER_CALL_LOCATOR) {
                callLocatorInternal.setServerCallId(((ServerCallLocator) callLocator).getServerCallId());
            } else if (callLocator.getKind() == CallLocatorKind.ROOM_CALL_LOCATOR) {
                callLocatorInternal.setRoomId(((RoomCallLocator) callLocator).getRoomId());
            } else {
                throw logger.logExceptionAsError(new InvalidParameterException("callLocator has invalid kind."));
            }

            ConnectRequestInternal request = new ConnectRequestInternal()
                .setCallbackUri(connectCallOptions.getCallbackUrl())
                .setCallLocator(callLocatorInternal);

            if (connectCallOptions.getOperationContext() != null) {
                request.setOperationContext(connectCallOptions.getOperationContext());
            }

            if (connectCallOptions.getCallIntelligenceOptions() != null && connectCallOptions.getCallIntelligenceOptions().getCognitiveServicesEndpoint() != null) {
                CallIntelligenceOptionsInternal callIntelligenceOptionsInternal = new CallIntelligenceOptionsInternal();
                callIntelligenceOptionsInternal.setCognitiveServicesEndpoint(connectCallOptions.getCallIntelligenceOptions().getCognitiveServicesEndpoint());
                request.setCallIntelligenceOptions(callIntelligenceOptionsInternal);
            }

            return azureCommunicationCallAutomationServiceInternal.connectWithResponseAsync(
                    request,
                    context)
                .map(response -> {
                    try {
                        CallConnectionAsync callConnectionAsync = getCallConnectionAsync(response.getValue().getCallConnectionId());
                        return new SimpleResponse<>(response,
                            new ConnectCallResult(CallConnectionPropertiesConstructorProxy.create(response.getValue()),
                                new CallConnection(callConnectionAsync), callConnectionAsync));
                    } catch (URISyntaxException e) {
                        throw logger.logExceptionAsError(new RuntimeException(e));
                    }
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }
    //endregion

    //region Mid-call Actions
    /***
     * Returns an object of CallConnectionAsync
     *
     * @param callConnectionId the id of the call connection
     * @return a CallContentAsync.
     */
    public CallConnectionAsync getCallConnectionAsync(String callConnectionId) {
        return new CallConnectionAsync(callConnectionId, callConnectionsInternal, callMediasInternal);
    }
    //endregion

    //region Content management Actions
    /***
     * Returns an object of CallRecordingAsync
     *
     * @return a CallRecordingAsync.
     */
    public CallRecordingAsync getCallRecordingAsync() {
        return new CallRecordingAsync(callRecordingsInternal, contentDownloader, httpPipelineInternal, resourceUrl);
    }
    //endregion
}
