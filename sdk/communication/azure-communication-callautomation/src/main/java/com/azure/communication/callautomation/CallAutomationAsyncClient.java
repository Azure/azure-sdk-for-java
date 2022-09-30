// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.implementation.AzureCommunicationCallAutomationServiceImpl;
import com.azure.communication.callautomation.implementation.CallConnectionsImpl;
import com.azure.communication.callautomation.implementation.ContentsImpl;
import com.azure.communication.callautomation.implementation.ServerCallingsImpl;
import com.azure.communication.callautomation.implementation.ServerCallsImpl;
import com.azure.communication.callautomation.implementation.accesshelpers.CallConnectionPropertiesConstructorProxy;
import com.azure.communication.callautomation.implementation.accesshelpers.ErrorConstructorProxy;
import com.azure.communication.callautomation.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callautomation.implementation.models.CallSourceInternal;
import com.azure.communication.callautomation.implementation.models.MediaStreamingAudioChannelTypeInternal;
import com.azure.communication.callautomation.implementation.models.MediaStreamingConfigurationInternal;
import com.azure.communication.callautomation.implementation.models.MediaStreamingContentTypeInternal;
import com.azure.communication.callautomation.implementation.models.MediaStreamingTransportTypeInternal;
import com.azure.communication.callautomation.models.AnswerCallResult;
import com.azure.communication.callautomation.models.CallRejectReason;
import com.azure.communication.callautomation.models.CallingServerErrorException;
import com.azure.communication.callautomation.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.callautomation.implementation.models.CreateCallRequestInternal;
import com.azure.communication.callautomation.implementation.models.AnswerCallRequestInternal;
import com.azure.communication.callautomation.implementation.models.RedirectCallRequestInternal;
import com.azure.communication.callautomation.implementation.models.RejectCallRequestInternal;
import com.azure.communication.callautomation.implementation.models.CallRejectReasonInternal;
import com.azure.communication.callautomation.implementation.models.PhoneNumberIdentifierModel;
import com.azure.communication.callautomation.models.CreateCallOptions;
import com.azure.communication.callautomation.models.CreateCallResult;
import com.azure.communication.callautomation.models.MediaStreamingOptions;
import com.azure.communication.common.CommunicationIdentifier;
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
    private final CallConnectionsImpl callConnectionInternal;
    private final ServerCallingsImpl serverCallingInternal;
    private final ServerCallsImpl serverCallsInternal;
    private final ContentsImpl contentsInternal;
    private final ClientLogger logger;
    private final ContentDownloader contentDownloader;
    private final HttpPipeline httpPipelineInternal;
    private final String resourceEndpoint;

    CallAutomationAsyncClient(AzureCommunicationCallAutomationServiceImpl callServiceClient) {
        this.callConnectionInternal = callServiceClient.getCallConnections();
        this.serverCallingInternal = callServiceClient.getServerCallings();
        this.serverCallsInternal = callServiceClient.getServerCalls();
        this.contentsInternal = callServiceClient.getContents();
        this.logger = new ClientLogger(CallAutomationAsyncClient.class);
        this.contentDownloader = new ContentDownloader(
            callServiceClient.getEndpoint(),
            callServiceClient.getHttpPipeline());
        this.httpPipelineInternal = callServiceClient.getHttpPipeline();
        this.resourceEndpoint = callServiceClient.getEndpoint();
    }

    //region Pre-call Actions
    /**
     * Create a call connection request from a source identity to a target identity.
     *
     * @param createCallOptions Options bag for creating a new call.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CreateCallResult> createCall(CreateCallOptions createCallOptions) {
        return createCallWithResponse(createCallOptions).flatMap(FluxUtil::toMono);
    }

    /**
     * Create a call connection request from a source identity to a target identity.
     *
     * @param createCallOptions Options bag for creating a new call.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CreateCallResult>> createCallWithResponse(CreateCallOptions createCallOptions) {
        return withContext(context -> createCallWithResponseInternal(createCallOptions, context));
    }

    Mono<Response<CreateCallResult>> createCallWithResponseInternal(CreateCallOptions createCallOptions,
                                                                    Context context) {
        try {
            context = context == null ? Context.NONE : context;
            CreateCallRequestInternal request = getCreateCallRequestInternal(createCallOptions);

            return serverCallingInternal.createCallWithResponseAsync(request, null, null, context)
                .onErrorMap(HttpResponseException.class, ErrorConstructorProxy::create)
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
        List<CommunicationIdentifierModel> targetsModel = createCallOptions.getTargets()
            .stream().map(CommunicationIdentifierConverter::convert).collect(Collectors.toList());

        CallSourceInternal callSourceDto = new CallSourceInternal().setIdentifier(
            CommunicationIdentifierConverter.convert(createCallOptions.getSource()));
        if (createCallOptions.getSourceCallerId() != null) {
            callSourceDto.setCallerId(new PhoneNumberIdentifierModel().setValue(createCallOptions.getSourceCallerId()));
        }

        CreateCallRequestInternal request = new CreateCallRequestInternal()
            .setSource(callSourceDto)
            .setTargets(targetsModel)
            .setCallbackUri(createCallOptions.getCallbackUrl())
            .setSubject(createCallOptions.getSubject());

        if (createCallOptions.getMediaStreamingConfiguration() != null) {
            MediaStreamingConfigurationInternal streamingConfigurationInternal =
                getMediaStreamingConfigurationInternal(createCallOptions.getMediaStreamingConfiguration());
            request.setMediaStreamingConfiguration(streamingConfigurationInternal);
        }
        return request;
    }

    private MediaStreamingConfigurationInternal getMediaStreamingConfigurationInternal(
        MediaStreamingOptions mediaStreamingOptions
    ) {
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
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AnswerCallResult> answerCall(String incomingCallContext, String callbackUrl) {
        return answerCallWithResponse(incomingCallContext, callbackUrl, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Create a call connection request from a source identity to a target identity.
     *
     * @param incomingCallContext The incoming call context.
     * @param callbackUrl The call back url.
     * @param mediaStreamingOptions The MediaStreamingConfiguration. Optional
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AnswerCallResult>> answerCallWithResponse(String incomingCallContext,
                                                                   String callbackUrl, MediaStreamingOptions mediaStreamingOptions) {
        return withContext(context -> answerCallWithResponseInternal(incomingCallContext, callbackUrl, mediaStreamingOptions, context));
    }

    Mono<Response<AnswerCallResult>> answerCallWithResponseInternal(String incomingCallContext, String callbackUrl,
                                                                    MediaStreamingOptions mediaStreamingOptions,
                                                                    Context context) {
        try {
            context = context == null ? Context.NONE : context;

            AnswerCallRequestInternal request = new AnswerCallRequestInternal()
                .setIncomingCallContext(incomingCallContext)
                .setCallbackUri(callbackUrl);

            if (mediaStreamingOptions != null) {
                MediaStreamingConfigurationInternal mediaStreamingConfigurationInternal =
                    getMediaStreamingConfigurationInternal(mediaStreamingOptions);

                request.setMediaStreamingConfiguration(mediaStreamingConfigurationInternal);
            }


            return serverCallingInternal.answerCallWithResponseAsync(request, null, null, context)
                .onErrorMap(HttpResponseException.class, ErrorConstructorProxy::create)
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
     * @param target The target identity.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> redirectCall(String incomingCallContext, CommunicationIdentifier target) {
        return redirectCallWithResponse(incomingCallContext, target).flatMap(FluxUtil::toMono);
    }

    /**
     * Redirect a call
     *
     * @param incomingCallContext The incoming call context.
     * @param target The target identity.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> redirectCallWithResponse(String incomingCallContext, CommunicationIdentifier target) {
        return withContext(context -> redirectCallWithResponseInternal(incomingCallContext, target, context));
    }

    Mono<Response<Void>> redirectCallWithResponseInternal(String incomingCallContext, CommunicationIdentifier target,
                                                          Context context) {
        try {
            context = context == null ? Context.NONE : context;

            RedirectCallRequestInternal request = new RedirectCallRequestInternal()
                .setIncomingCallContext(incomingCallContext)
                .setTarget(CommunicationIdentifierConverter.convert(target));

            return serverCallingInternal.redirectCallWithResponseAsync(request, null, null, context)
                .onErrorMap(HttpResponseException.class, ErrorConstructorProxy::create);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Reject a call
     *
     * @param incomingCallContext The incoming call context.
     * @param callRejectReason The reason why call is rejected. Optional
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> rejectCall(String incomingCallContext, CallRejectReason callRejectReason) {
        return rejectCallWithResponse(incomingCallContext, callRejectReason).flatMap(FluxUtil::toMono);
    }

    /**
     * Reject a call
     *
     * @param incomingCallContext The incoming call context.
     * @param callRejectReason The reason why call is rejected. Optional
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> rejectCallWithResponse(String incomingCallContext, CallRejectReason callRejectReason) {
        return withContext(context -> rejectCallWithResponseInternal(incomingCallContext, callRejectReason, context));
    }

    Mono<Response<Void>> rejectCallWithResponseInternal(String incomingCallContext, CallRejectReason callRejectReason,
                                                        Context context) {
        try {
            context = context == null ? Context.NONE : context;

            RejectCallRequestInternal request = new RejectCallRequestInternal()
                .setIncomingCallContext(incomingCallContext)
                .setCallRejectReason(CallRejectReasonInternal.fromString(callRejectReason.toString()));

            return serverCallingInternal.rejectCallWithResponseAsync(request, null, null, context)
                .onErrorMap(HttpResponseException.class, ErrorConstructorProxy::create);
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
        return new CallConnectionAsync(callConnectionId, callConnectionInternal, contentsInternal);
    }
    //endregion

    //region Content management Actions
    /***
     * Returns an object of CallRecordingAsync
     *
     * @return a CallRecordingAsync.
     */
    public CallRecordingAsync getCallRecordingAsync() {
        return new CallRecordingAsync(serverCallsInternal, contentsInternal,
            contentDownloader, httpPipelineInternal, resourceEndpoint);
    }
    //endregion
}
