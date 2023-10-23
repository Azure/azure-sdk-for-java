// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.implementation.AzureCommunicationCallAutomationServiceImpl;
import com.azure.communication.callautomation.implementation.CallConnectionsImpl;
import com.azure.communication.callautomation.implementation.CallMediasImpl;
import com.azure.communication.callautomation.implementation.CallRecordingsImpl;
import com.azure.communication.callautomation.implementation.CallDialogsImpl;
import com.azure.communication.callautomation.implementation.accesshelpers.CallConnectionPropertiesConstructorProxy;
import com.azure.communication.callautomation.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callautomation.implementation.converters.CommunicationUserIdentifierConverter;
import com.azure.communication.callautomation.implementation.converters.PhoneNumberIdentifierConverter;
import com.azure.communication.callautomation.implementation.models.MediaStreamingAudioChannelTypeInternal;
import com.azure.communication.callautomation.implementation.models.MediaStreamingConfigurationInternal;
import com.azure.communication.callautomation.implementation.models.MediaStreamingContentTypeInternal;
import com.azure.communication.callautomation.implementation.models.MediaStreamingTransportTypeInternal;
import com.azure.communication.callautomation.models.AnswerCallOptions;
import com.azure.communication.callautomation.models.AnswerCallResult;
import com.azure.communication.callautomation.models.CallInvite;
import com.azure.communication.callautomation.models.CreateCallOptions;
import com.azure.communication.callautomation.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.callautomation.implementation.models.CommunicationUserIdentifierModel;
import com.azure.communication.callautomation.implementation.models.CreateCallRequestInternal;
import com.azure.communication.callautomation.implementation.models.CustomContext;
import com.azure.communication.callautomation.implementation.models.AnswerCallRequestInternal;
import com.azure.communication.callautomation.implementation.models.RedirectCallRequestInternal;
import com.azure.communication.callautomation.implementation.models.RejectCallRequestInternal;
import com.azure.communication.callautomation.implementation.models.CallRejectReasonInternal;
import com.azure.communication.callautomation.models.CreateGroupCallOptions;
import com.azure.communication.callautomation.models.CreateCallResult;
import com.azure.communication.callautomation.models.MediaStreamingOptions;
import com.azure.communication.callautomation.models.RedirectCallOptions;
import com.azure.communication.callautomation.models.RejectCallOptions;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.exception.HttpResponseException;
import reactor.core.publisher.Mono;

import java.net.URISyntaxException;
import java.time.OffsetDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
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
    private final CallDialogsImpl callDialogsInternal;
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
        this.callDialogsInternal = callServiceClient.getCallDialogs();
        this.logger = new ClientLogger(CallAutomationAsyncClient.class);
        this.contentDownloader = new ContentDownloader(callServiceClient.getEndpoint(), callServiceClient.getHttpPipeline());
        this.httpPipelineInternal = callServiceClient.getHttpPipeline();
        this.resourceUrl = callServiceClient.getEndpoint();
        this.sourceIdentity = sourceIdentity == null ? null : CommunicationUserIdentifierConverter.convert(sourceIdentity);
    }

    //region Pre-call Actions
    /**
     * Get Source Identity that is used for create and answer call
     * @return {@link CommunicationUserIdentifier} represent source
     */
    public CommunicationUserIdentifier getSourceIdentity() {
        return sourceIdentity == null ? null : CommunicationUserIdentifierConverter.convert(sourceIdentity);
    }

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
        try {
            context = context == null ? Context.NONE : context;
            CreateCallRequestInternal request = getCreateCallRequestInternal(createCallOptions);
            return azureCommunicationCallAutomationServiceInternal.createCallWithResponseAsync(
                    request,
                    UUID.randomUUID(),
                    OffsetDateTime.now(),
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

    Mono<Response<CreateCallResult>> createGroupCallWithResponseInternal(CreateGroupCallOptions createGroupCallOptions, Context context) {
        try {
            context = context == null ? Context.NONE : context;
            CreateCallRequestInternal request = getCreateCallRequestInternal(createGroupCallOptions);
            return azureCommunicationCallAutomationServiceInternal.createCallWithResponseAsync(
                    request,
                    UUID.randomUUID(),
                    OffsetDateTime.now(),
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

        CreateCallRequestInternal request = new CreateCallRequestInternal()
            .setSourceCallerIdNumber(PhoneNumberIdentifierConverter.convert(createCallOptions.getCallInvite().getSourceCallerIdNumber()))
            .setSourceDisplayName(createCallOptions.getCallInvite().getSourceDisplayName())
            .setSourceIdentity(sourceIdentity)
            .setTargets(targetsModel)
            .setCallbackUri(createCallOptions.getCallbackUrl())
            .setOperationContext(createCallOptions.getOperationContext());

        // Need to do a null check since SipHeaders and VoipHeaders are optional; If they both are null then we do not need to set custom context
        if (createCallOptions.getCallInvite().getCustomContext().getSipHeaders() != null || createCallOptions.getCallInvite().getCustomContext().getVoipHeaders() != null) {
            CustomContext customContext = new CustomContext();
            customContext.setSipHeaders(createCallOptions.getCallInvite().getCustomContext().getSipHeaders());
            customContext.setVoipHeaders(createCallOptions.getCallInvite().getCustomContext().getVoipHeaders());
            request.setCustomContext(customContext);
        }

        if (createCallOptions.getMediaStreamingConfiguration() != null) {
            MediaStreamingConfigurationInternal streamingConfigurationInternal =
                getMediaStreamingConfigurationInternal(createCallOptions.getMediaStreamingConfiguration());
            request.setMediaStreamingConfiguration(streamingConfigurationInternal);
        }

        if (createCallOptions.getAzureCognitiveServicesUrl() != null && !createCallOptions.getAzureCognitiveServicesUrl().isEmpty()) {
            request.setAzureCognitiveServicesEndpointUrl(createCallOptions.getAzureCognitiveServicesUrl());
        }

        return request;
    }

    private CreateCallRequestInternal getCreateCallRequestInternal(CreateGroupCallOptions createCallGroupOptions) {
        List<CommunicationIdentifierModel> targetsModel = createCallGroupOptions.getTargetParticipants()
            .stream().map(CommunicationIdentifierConverter::convert).collect(Collectors.toList());

        CreateCallRequestInternal request = new CreateCallRequestInternal()
            .setSourceCallerIdNumber(PhoneNumberIdentifierConverter.convert(createCallGroupOptions.getSourceCallIdNumber()))
            .setSourceDisplayName(createCallGroupOptions.getSourceDisplayName())
            .setSourceIdentity(sourceIdentity)
            .setTargets(targetsModel)
            .setCallbackUri(createCallGroupOptions.getCallbackUrl())
            .setOperationContext(createCallGroupOptions.getOperationContext());

        if (createCallGroupOptions.getCustomContext().getSipHeaders() != null || createCallGroupOptions.getCustomContext().getVoipHeaders() != null) {
            CustomContext customContext = new CustomContext();
            customContext.setSipHeaders(createCallGroupOptions.getCustomContext().getSipHeaders());
            customContext.setVoipHeaders(createCallGroupOptions.getCustomContext().getVoipHeaders());
            request.setCustomContext(customContext);
        }

        if (createCallGroupOptions.getMediaStreamingConfiguration() != null) {
            MediaStreamingConfigurationInternal streamingConfigurationInternal =
                getMediaStreamingConfigurationInternal(createCallGroupOptions.getMediaStreamingConfiguration());
            request.setMediaStreamingConfiguration(streamingConfigurationInternal);
        }

        if (createCallGroupOptions.getAzureCognitiveServicesUrl() != null && !createCallGroupOptions.getAzureCognitiveServicesUrl().isEmpty()) {
            request.setAzureCognitiveServicesEndpointUrl(createCallGroupOptions.getAzureCognitiveServicesUrl());
        }

        return request;
    }

    private MediaStreamingConfigurationInternal getMediaStreamingConfigurationInternal(
        MediaStreamingOptions mediaStreamingOptions) {
        return new MediaStreamingConfigurationInternal()
            .setTransportUrl(mediaStreamingOptions.getTransportUrl())
            .setAudioChannelType(
                MediaStreamingAudioChannelTypeInternal.fromString(
                    mediaStreamingOptions.getAudioChannelType().toString()))
            .setContentType(
                MediaStreamingContentTypeInternal.fromString(
                    mediaStreamingOptions.getContentType().toString()))
            .setTransportType(
                MediaStreamingTransportTypeInternal.fromString(
                    mediaStreamingOptions.getTransportType().toString()));
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
                .setAnsweredByIdentifier(sourceIdentity)
                .setOperationContext(answerCallOptions.getOperationContext());

            if (answerCallOptions.getMediaStreamingConfiguration() != null) {
                MediaStreamingConfigurationInternal mediaStreamingConfigurationInternal =
                    getMediaStreamingConfigurationInternal(answerCallOptions.getMediaStreamingConfiguration());

                request.setMediaStreamingConfiguration(mediaStreamingConfigurationInternal);
            }

            if (answerCallOptions.getAzureCognitiveServicesUrl() != null && !answerCallOptions.getAzureCognitiveServicesUrl().isEmpty()) {
                request.setAzureCognitiveServicesEndpointUrl(answerCallOptions.getAzureCognitiveServicesUrl());
            }

            return azureCommunicationCallAutomationServiceInternal.answerCallWithResponseAsync(
                    request,
                    UUID.randomUUID(),
                    OffsetDateTime.now(),
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

            // Need to do a null check since SipHeaders and VoipHeaders are optional; If they both are null then we do not need to set custom context
            if (redirectCallOptions.getTargetParticipant().getCustomContext().getSipHeaders() != null || redirectCallOptions.getTargetParticipant().getCustomContext().getVoipHeaders() != null) {
                CustomContext customContext = new CustomContext();
                customContext.setSipHeaders(redirectCallOptions.getTargetParticipant().getCustomContext().getSipHeaders());
                customContext.setVoipHeaders(redirectCallOptions.getTargetParticipant().getCustomContext().getVoipHeaders());
                request.setCustomContext(customContext);
            }

            return azureCommunicationCallAutomationServiceInternal.redirectCallWithResponseAsync(
                    request,
                    UUID.randomUUID(),
                    OffsetDateTime.now(),
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
                    UUID.randomUUID(),
                    OffsetDateTime.now(),
                    context);
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
        return new CallConnectionAsync(callConnectionId, callConnectionsInternal, callMediasInternal, callDialogsInternal);
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
