// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.AzureCommunicationCallingServerServiceImpl;
import com.azure.communication.callingserver.implementation.CallConnectionsImpl;
import com.azure.communication.callingserver.implementation.ServerCallingsImpl;
import com.azure.communication.callingserver.implementation.converters.CallingServerErrorConverter;
import com.azure.communication.callingserver.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callingserver.implementation.converters.PhoneNumberIdentifierConverter;
import com.azure.communication.callingserver.implementation.models.CreateCallRequestInternal;
import com.azure.communication.callingserver.implementation.models.AnswerCallRequestInternal;
import com.azure.communication.callingserver.implementation.models.RedirectCallRequestInternal;
import com.azure.communication.callingserver.implementation.models.CommunicationErrorResponseException;
import com.azure.communication.callingserver.models.CallingServerErrorException;
import com.azure.communication.callingserver.models.CreateCallOptions;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;


import static com.azure.core.util.FluxUtil.monoError;

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
    private final ClientLogger logger;
    private final ContentDownloader contentDownloader;
    private final HttpPipeline httpPipelineInternal;
    private final String resourceEndpoint;


    CallingServerAsyncClient(AzureCommunicationCallingServerServiceImpl callServiceClient) {
        callConnectionInternal = callServiceClient.getCallConnections();
        serverCallingInternal = callServiceClient.getServerCallings();
        httpPipelineInternal = callServiceClient.getHttpPipeline();
        resourceEndpoint = callServiceClient.getEndpoint();
        contentDownloader = new ContentDownloader(resourceEndpoint, httpPipelineInternal);
        logger = new ClientLogger(CallingServerAsyncClient.class);
    }

    /**
     * Create a call connection request from a source identity to a target identity.
     *
     * @param source The source identity.
     * @param target The target identity.
     * @param callbackUri The call back URI.
     * @param createCallOptions The call options.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CallConnectionAsync> createCall(CommunicationIdentifier source, CommunicationIdentifier target,
                                                String callbackUri, CreateCallOptions createCallOptions) {
        return createCall(source, target, callbackUri, createCallOptions, null);
    }

    /**
     * Create a call connection request from a source identity to a target identity.
     *
     * @param source The source identity.
     * @param target The target identity.
     * @param callbackUri The call back URI.
     * @param createCallOptions The call options.
     * @param context The context to associate with this operation.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CallConnectionAsync> createCall(CommunicationIdentifier source, CommunicationIdentifier target,
                                                String callbackUri, CreateCallOptions createCallOptions, Context context) {
        try {
            CreateCallRequestInternal request = new CreateCallRequestInternal()
                .setSource(CommunicationIdentifierConverter.convert(source))
                .setTarget(CommunicationIdentifierConverter.convert(target))
                .setCallbackUri(callbackUri);
            if (createCallOptions != null) {
                request.setAlternateCallerId(PhoneNumberIdentifierConverter.convert(createCallOptions.getAlternateCallerId()))
                    .setSubject(createCallOptions.getSubject());
            }
            return (context == null ? serverCallingInternal.createCallAsync(request)
                : serverCallingInternal.createCallAsync(request, context))
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(response -> Mono.just(new CallConnectionAsync(response.getCallLegId(), callConnectionInternal)));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Create a call connection request from a source identity to a target identity.
     *
     * @param source The source identity.
     * @param target The target identity.
     * @param callbackUri The call back URI.
     * @param createCallOptions The call options.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CallConnectionAsync>> createCallWithResponse(CommunicationIdentifier source, CommunicationIdentifier target,
                                                String callbackUri, CreateCallOptions createCallOptions) {
        return createCallWithResponse(source, target, callbackUri, createCallOptions, null);
    }

    /**
     * Create a call connection request from a source identity to a target identity.
     *
     * @param source The source identity.
     * @param target The target identity.
     * @param callbackUri The call back URI.
     * @param createCallOptions The call options.
     * @param context The context to associate with this operation.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CallConnectionAsync>> createCallWithResponse(CommunicationIdentifier source, CommunicationIdentifier target,
                                                 String callbackUri, CreateCallOptions createCallOptions, Context context) {
        try {
            CreateCallRequestInternal request = new CreateCallRequestInternal()
                .setSource(CommunicationIdentifierConverter.convert(source))
                .setTarget(CommunicationIdentifierConverter.convert(target))
                .setCallbackUri(callbackUri);
            if (createCallOptions != null) {
                request.setAlternateCallerId(PhoneNumberIdentifierConverter.convert(createCallOptions.getAlternateCallerId()))
                    .setSubject(createCallOptions.getSubject());
            }
            return (context == null ? serverCallingInternal.createCallWithResponseAsync(request)
                : serverCallingInternal.createCallWithResponseAsync(request, context))
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .map(response -> new SimpleResponse<>(response,
                    new CallConnectionAsync(response.getValue().getCallLegId(), callConnectionInternal)));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Answer an incoming call
     *
     * @param incomingCallContext The incoming call context.
     * @param callbackUri The call back URI.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CallConnectionAsync> answerCall(String incomingCallContext, String callbackUri) {
        return answerCall(incomingCallContext, callbackUri, null);
    }

    /**
     * Answer an incoming call
     *
     * @param incomingCallContext The incoming call context.
     * @param callbackUri The call back URI.
     * @param context The context to associate with this operation.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CallConnectionAsync> answerCall(String incomingCallContext, String callbackUri, Context context) {
        try {
            AnswerCallRequestInternal request = new AnswerCallRequestInternal()
                .setIncomingCallContext(incomingCallContext)
                .setCallbackUri(callbackUri);
            return (context == null ? serverCallingInternal.answerCallAsync(request)
                : serverCallingInternal.answerCallAsync(request, context))
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(response -> Mono.just(new CallConnectionAsync(response.getCallLegId(), callConnectionInternal)));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Create a call connection request from a source identity to a target identity.
     *
     * @param incomingCallContext The incoming call context.
     * @param callbackUri The call back URI.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CallConnectionAsync>> answerCallWithResponse(String incomingCallContext, String callbackUri) {
        return answerCallWithResponse(incomingCallContext, callbackUri, null);
    }

    /**
     * Create a call connection request from a source identity to a target identity.
     *
     * @param incomingCallContext The incoming call context.
     * @param callbackUri The call back URI.
     * @param context The context to associate with this operation.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CallConnectionAsync>> answerCallWithResponse(String incomingCallContext, String callbackUri, Context context) {
        try {
            AnswerCallRequestInternal request = new AnswerCallRequestInternal()
                .setIncomingCallContext(incomingCallContext)
                .setCallbackUri(callbackUri);
            return (context == null ? serverCallingInternal.answerCallWithResponseAsync(request)
                : serverCallingInternal.answerCallWithResponseAsync(request, context))
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .map(response -> new SimpleResponse<>(response,
                    new CallConnectionAsync(response.getValue().getCallLegId(), callConnectionInternal)));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Redirect a call
     *
     * @param incomingCallContext The incoming call context.
     * @param target The target identity.
     * @param callbackUri The call back URI.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> redirectCall(String incomingCallContext, CommunicationIdentifier target, String callbackUri) {
        return redirectCall(incomingCallContext, target, callbackUri, null);
    }

    /**
     * Redirect a call
     *
     * @param incomingCallContext The incoming call context.
     * @param target The target identity.
     * @param callbackUri The call back URI.
     * @param context The context to associate with this operation.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> redirectCall(String incomingCallContext, CommunicationIdentifier target, String callbackUri, Context context) {
        try {
            RedirectCallRequestInternal request = new RedirectCallRequestInternal()
                .setIncomingCallContext(incomingCallContext)
                .setTarget(CommunicationIdentifierConverter.convert(target))
                .setCallbackUri(callbackUri);
            return (context == null ? serverCallingInternal.redirectCallAsync(request)
                : serverCallingInternal.redirectCallAsync(request, context))
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException)
                .flatMap(result -> Mono.empty());
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Redirect a call
     *
     * @param incomingCallContext The incoming call context.
     * @param target The target identity.
     * @param callbackUri The call back URI.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> redirectCallWithResponse(String incomingCallContext, CommunicationIdentifier target, String callbackUri) {
        return redirectCallWithResponse(incomingCallContext, target, callbackUri, null);
    }

    /**
     * Redirect a call
     *
     * @param incomingCallContext The incoming call context.
     * @param target The target identity.
     * @param callbackUri The call back URI.
     * @param context The context to associate with this operation.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> redirectCallWithResponse(String incomingCallContext, CommunicationIdentifier target, String callbackUri, Context context) {
        try {
            RedirectCallRequestInternal request = new RedirectCallRequestInternal()
                .setIncomingCallContext(incomingCallContext)
                .setTarget(CommunicationIdentifierConverter.convert(target))
                .setCallbackUri(callbackUri);
            return (context == null ? serverCallingInternal.redirectCallWithResponseAsync(request)
                : serverCallingInternal.redirectCallWithResponseAsync(request, context))
                .onErrorMap(CommunicationErrorResponseException.class, CallingServerErrorConverter::translateException);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }
}
