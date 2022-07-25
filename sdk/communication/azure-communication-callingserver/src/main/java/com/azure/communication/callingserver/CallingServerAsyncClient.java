// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.AzureCommunicationCallingServerServiceImpl;
import com.azure.communication.callingserver.implementation.CallConnectionsImpl;
import com.azure.communication.callingserver.implementation.ContentsImpl;
import com.azure.communication.callingserver.implementation.ServerCallingsImpl;
import com.azure.communication.callingserver.implementation.ServerCallsImpl;
import com.azure.communication.callingserver.implementation.accesshelpers.CallConnectionPropertiesConstructorProxy;
import com.azure.communication.callingserver.implementation.accesshelpers.ErrorConstructorProxy;
import com.azure.communication.callingserver.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callingserver.implementation.models.CallSourceInternal;
import com.azure.communication.callingserver.models.CallConnectionProperties;
import com.azure.communication.callingserver.models.CallingServerErrorException;
import com.azure.communication.callingserver.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.callingserver.implementation.models.CreateCallRequestInternal;
import com.azure.communication.callingserver.implementation.models.AnswerCallRequestInternal;
import com.azure.communication.callingserver.implementation.models.RedirectCallRequestInternal;
import com.azure.communication.callingserver.implementation.models.RejectCallRequestInternal;
import com.azure.communication.callingserver.implementation.models.CallRejectReason;
import com.azure.communication.callingserver.implementation.models.PhoneNumberIdentifierModel;
import com.azure.communication.callingserver.models.CreateCallOptions;
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
 * <p>View {@link CallingServerClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see CallingServerClientBuilder
 */
@ServiceClient(builder = CallingServerClientBuilder.class, isAsync = true)
public final class CallingServerAsyncClient {
    private final CallConnectionsImpl callConnectionInternal;
    private final ServerCallingsImpl serverCallingInternal;
    private final ServerCallsImpl serverCallsInternal;
    private final ContentsImpl contentsInternal;
    private final ClientLogger logger;
    private final ContentDownloader contentDownloader;
    private final HttpPipeline httpPipelineInternal;
    private final String resourceEndpoint;

    CallingServerAsyncClient(AzureCommunicationCallingServerServiceImpl callServiceClient) {
        this.callConnectionInternal = callServiceClient.getCallConnections();
        this.serverCallingInternal = callServiceClient.getServerCallings();
        this.serverCallsInternal = callServiceClient.getServerCalls();
        this.contentsInternal = callServiceClient.getContents();
        this.logger = new ClientLogger(CallingServerAsyncClient.class);
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
    public Mono<CallConnectionProperties> createCall(CreateCallOptions createCallOptions) {
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
    public Mono<Response<CallConnectionProperties>> createCallWithResponse(CreateCallOptions createCallOptions) {
        return withContext(context -> createCallWithResponseInternal(createCallOptions, context));
    }

    Mono<Response<CallConnectionProperties>> createCallWithResponseInternal(CreateCallOptions createCallOptions,
                                                                            Context context) {
        try {
            context = context == null ? Context.NONE : context;
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
                .setCallbackUri(createCallOptions.getCallbackUri().toString())
                .setSubject(createCallOptions.getSubject());

            return serverCallingInternal.createCallWithResponseAsync(request, context)
                .onErrorMap(HttpResponseException.class, ErrorConstructorProxy::create)
                .map(response -> {
                    try {
                        return new SimpleResponse<>(response, CallConnectionPropertiesConstructorProxy.create(response.getValue()));
                    } catch (URISyntaxException e) {
                        logger.logExceptionAsError(new RuntimeException(e));
                        return null;
                    }
                });
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Answer an incoming call
     *
     * @param incomingCallContext The incoming call context.
     * @param callbackUri The call back uri. Optional
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CallConnectionProperties> answerCall(String incomingCallContext, String callbackUri) {
        return answerCallWithResponse(incomingCallContext, callbackUri).flatMap(FluxUtil::toMono);
    }

    /**
     * Create a call connection request from a source identity to a target identity.
     *
     * @param incomingCallContext The incoming call context.
     * @param callbackUri The call back uri. Optional
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CallConnectionProperties>> answerCallWithResponse(String incomingCallContext,
                                                                           String callbackUri) {
        return withContext(context -> answerCallWithResponseInternal(incomingCallContext, callbackUri, context));
    }

    Mono<Response<CallConnectionProperties>> answerCallWithResponseInternal(String incomingCallContext, String callbackUri,
                                                                            Context context) {
        try {
            context = context == null ? Context.NONE : context;

            AnswerCallRequestInternal request = new AnswerCallRequestInternal()
                .setIncomingCallContext(incomingCallContext)
                .setCallbackUri(callbackUri);

            return serverCallingInternal.answerCallWithResponseAsync(request, context)
                .onErrorMap(HttpResponseException.class, ErrorConstructorProxy::create)
                .map(response -> {
                    try {
                        return new SimpleResponse<>(response, CallConnectionPropertiesConstructorProxy.create(response.getValue()));
                    } catch (URISyntaxException e) {
                        logger.logExceptionAsError(new RuntimeException(e));
                        return null;
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

            return serverCallingInternal.redirectCallWithResponseAsync(request, context)
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
    public Mono<Void> rejectCall(String incomingCallContext, String callRejectReason) {
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
    public Mono<Response<Void>> rejectCallWithResponse(String incomingCallContext, String callRejectReason) {
        return withContext(context -> rejectCallWithResponseInternal(incomingCallContext, callRejectReason, context));
    }

    Mono<Response<Void>> rejectCallWithResponseInternal(String incomingCallContext, String callRejectReason,
                                                        Context context) {
        try {
            context = context == null ? Context.NONE : context;

            RejectCallRequestInternal request = new RejectCallRequestInternal()
                .setIncomingCallContext(incomingCallContext)
                .setCallRejectReason(CallRejectReason.fromString(callRejectReason));

            return serverCallingInternal.rejectCallWithResponseAsync(request, context)
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
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CallConnectionAsync getCallConnectionAsync(String callConnectionId) {
        return new CallConnectionAsync(callConnectionId, callConnectionInternal, contentsInternal);
    }

    /***
     * Returns the singleton object of EventHandler.
     *
     * @return the instance of EventHandler.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public EventHandler getEventHandler() {
        return EventHandler.getEventHandler();
    }
    //endregion

    //region Content management Actions
    /***
     * Returns an object of CallRecordingAsync
     *
     * @return a CallRecordingAsync.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CallRecordingAsync getCallRecordingAsync() {
        return new CallRecordingAsync(serverCallsInternal, contentsInternal,
            contentDownloader, httpPipelineInternal, resourceEndpoint);
    }
    //endregion
}
