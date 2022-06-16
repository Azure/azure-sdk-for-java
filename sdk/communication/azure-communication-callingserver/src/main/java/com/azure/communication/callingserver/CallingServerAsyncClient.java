// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.AzureCommunicationCallingServerServiceImpl;
import com.azure.communication.callingserver.implementation.CallConnectionsImpl;
import com.azure.communication.callingserver.implementation.ServerCallingsImpl;
import com.azure.communication.callingserver.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callingserver.implementation.converters.CallSourceConverter;
import com.azure.communication.callingserver.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.callingserver.implementation.models.CreateCallRequestInternal;
import com.azure.communication.callingserver.implementation.models.AnswerCallRequestInternal;
import com.azure.communication.callingserver.implementation.models.RedirectCallRequestInternal;
import com.azure.communication.callingserver.implementation.models.RejectCallRequestInternal;
import com.azure.communication.callingserver.implementation.models.CallRejectReason;
import com.azure.communication.callingserver.models.CallSource;
import com.azure.communication.callingserver.models.AnswerCallOptions;
import com.azure.communication.callingserver.models.CreateCallOptions;
import com.azure.communication.callingserver.models.RejectCallOptions;
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


import java.util.List;

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
    private final HttpPipeline httpPipelineInternal;
    private final String resourceEndpoint;


    CallingServerAsyncClient(AzureCommunicationCallingServerServiceImpl callServiceClient) {
        callConnectionInternal = callServiceClient.getCallConnections();
        serverCallingInternal = callServiceClient.getServerCallings();
        httpPipelineInternal = callServiceClient.getHttpPipeline();
        resourceEndpoint = callServiceClient.getEndpoint();
        logger = new ClientLogger(CallingServerAsyncClient.class);
    }

    /**
     * Create a call connection request from a source identity to a target identity.
     *
     * @param source The source property.
     * @param targets The targets of the call.
     * @param callbackUri The call back URI.
     * @param createCallOptions The call option.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CallConnectionAsync> createCall(CallSource source, List<CommunicationIdentifier> targets,
                                 String callbackUri, CreateCallOptions createCallOptions) {
        return createCall(source, targets, callbackUri, createCallOptions, null);
    }

    /**
     * Create a call connection request from a source identity to a target identity.
     *
     * @param source The source property.
     * @param targets The targets of the call.
     * @param callbackUri The call back URI.
     * @param createCallOptions The call option.
     * @param context The context to associate with this operation.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CallConnectionAsync> createCall(CallSource source, List<CommunicationIdentifier> targets,
                                 String callbackUri, CreateCallOptions createCallOptions,
                                 Context context) {
        try {
            List<CommunicationIdentifierModel> targetsModel = null;
            for (CommunicationIdentifier target : targets) {
                targetsModel.add(CommunicationIdentifierConverter.convert(target));
            }

            CreateCallRequestInternal request = new CreateCallRequestInternal()
                .setSource(CallSourceConverter.convert(source))
                .setTargets(targetsModel)
                .setCallbackUri(callbackUri);
            if (createCallOptions != null) {
                request.setSubject(createCallOptions.getSubject());
            }

            return (context == null ? serverCallingInternal.createCallAsync(request)
                : serverCallingInternal.createCallAsync(request, context)).flatMap(
                    result -> Mono.just(new CallConnectionAsync(result.getCallConnectionId(), callConnectionInternal)));

        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Create a call connection request from a source identity to a target identity.
     *
     * @param source The source property.
     * @param targets The targets of the call.
     * @param callbackUri The call back URI.
     * @param createCallOptions The call option.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CallConnectionAsync>> createCallWithResponse(CallSource source, List<CommunicationIdentifier> targets,
                                                       String callbackUri, CreateCallOptions createCallOptions) {
        return createCallWithResponse(source, targets, callbackUri, createCallOptions, null);
    }

    /**
     * Create a call connection request from a source identity to a target identity.
     *
     * @param source The source property.
     * @param targets The targets of the call.
     * @param callbackUri The call back URI.
     * @param createCallOptions The call option.
     * @param context The context to associate with this operation.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CallConnectionAsync>> createCallWithResponse(CallSource source,
                                                                      List<CommunicationIdentifier> targets,
                                                                      String callbackUri,
                                                                      CreateCallOptions createCallOptions,
                                                                      Context context) {
        try {
            List<CommunicationIdentifierModel> targetsModel = null;
            for (CommunicationIdentifier target : targets) {
                targetsModel.add(CommunicationIdentifierConverter.convert(target));
            }

            CreateCallRequestInternal request = new CreateCallRequestInternal()
                .setSource(CallSourceConverter.convert(source))
                .setTargets(targetsModel)
                .setCallbackUri(callbackUri);
            if (createCallOptions != null) {
                request.setSubject(createCallOptions.getSubject());
            }

            return (context == null ? serverCallingInternal.createCallWithResponseAsync(request)
                : serverCallingInternal.createCallWithResponseAsync(request, context)).map(
                    response -> new SimpleResponse<>(response, new CallConnectionAsync(response.getValue().getCallConnectionId(),
                        callConnectionInternal)));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Answer an incoming call
     *
     * @param incomingCallContext The incoming call context.
     * @param answerCallOptions The option of answering a call
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CallConnectionAsync> answerCall(String incomingCallContext, AnswerCallOptions answerCallOptions) {
        return answerCall(incomingCallContext, answerCallOptions, null);
    }

    /**
     * Answer an incoming call
     *
     * @param incomingCallContext The incoming call context.
     * @param answerCallOptions The option of answering a call
     * @param context The context to associate with this operation.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CallConnectionAsync> answerCall(String incomingCallContext, AnswerCallOptions answerCallOptions, Context context) {
        try {
            AnswerCallRequestInternal request = new AnswerCallRequestInternal()
                .setIncomingCallContext(incomingCallContext);
            if (answerCallOptions != null) {
                request.setCallbackUri(answerCallOptions.getCallbackUri());
            }
            return (context == null ? serverCallingInternal.answerCallAsync(request)
                : serverCallingInternal.answerCallAsync(request, context))
                .flatMap(response -> Mono.just(new CallConnectionAsync(response.getCallConnectionId(), callConnectionInternal)));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Create a call connection request from a source identity to a target identity.
     *
     * @param incomingCallContext The incoming call context.
     * @param answerCallOptions The option of answering a call
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CallConnectionAsync>> answerCallWithResponse(String incomingCallContext,
                                                                      AnswerCallOptions answerCallOptions) {
        return answerCallWithResponse(incomingCallContext, answerCallOptions, null);
    }

    /**
     * Create a call connection request from a source identity to a target identity.
     *
     * @param incomingCallContext The incoming call context.
     * @param answerCallOptions The option of answering a call
     * @param context The context to associate with this operation.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CallConnectionAsync>> answerCallWithResponse(String incomingCallContext,
                                                                      AnswerCallOptions answerCallOptions, Context context) {
        try {
            AnswerCallRequestInternal request = new AnswerCallRequestInternal()
                .setIncomingCallContext(incomingCallContext);
            if (answerCallOptions != null) {
                request.setCallbackUri(answerCallOptions.getCallbackUri());
            }
            return (context == null ? serverCallingInternal.answerCallWithResponseAsync(request)
                : serverCallingInternal.answerCallWithResponseAsync(request, context))
                .map(response -> new SimpleResponse<>(response,
                    new CallConnectionAsync(response.getValue().getCallConnectionId(), callConnectionInternal)));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Redirect a call
     *
     * @param incomingCallContext The incoming call context.
     * @param target The target identity.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> redirectCall(String incomingCallContext, CommunicationIdentifier target) {
        return redirectCall(incomingCallContext, target, null);
    }

    /**
     * Redirect a call
     *
     * @param incomingCallContext The incoming call context.
     * @param target The target identity.
     * @param context The context to associate with this operation.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> redirectCall(String incomingCallContext, CommunicationIdentifier target, Context context) {
        try {
            RedirectCallRequestInternal request = new RedirectCallRequestInternal()
                .setIncomingCallContext(incomingCallContext)
                .setTarget(CommunicationIdentifierConverter.convert(target));
            return (context == null ? serverCallingInternal.redirectCallAsync(request)
                : serverCallingInternal.redirectCallAsync(request, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Redirect a call
     *
     * @param incomingCallContext The incoming call context.
     * @param target The target identity.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> redirectCallWithResponse(String incomingCallContext, CommunicationIdentifier target) {
        return redirectCallWithResponse(incomingCallContext, target, null);
    }

    /**
     * Redirect a call
     *
     * @param incomingCallContext The incoming call context.
     * @param target The target identity.
     * @param context The context to associate with this operation.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> redirectCallWithResponse(String incomingCallContext,
                                                         CommunicationIdentifier target, Context context) {
        try {
            RedirectCallRequestInternal request = new RedirectCallRequestInternal()
                .setIncomingCallContext(incomingCallContext)
                .setTarget(CommunicationIdentifierConverter.convert(target));
            return (context == null ? serverCallingInternal.redirectCallWithResponseAsync(request)
                : serverCallingInternal.redirectCallWithResponseAsync(request, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Reject a call
     *
     * @param incomingCallContext The incoming call context.
     * @param rejectCallOptions Options of the reject call.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> rejectCall(String incomingCallContext, RejectCallOptions rejectCallOptions) {
        return rejectCall(incomingCallContext, rejectCallOptions, null);
    }

    /**
     * Reject a call
     *
     * @param incomingCallContext The incoming call context.
     * @param rejectCallOptions Options of the reject call.
     * @param context The context to associate with this operation.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> rejectCall(String incomingCallContext, RejectCallOptions rejectCallOptions, Context context) {
        try {
            RejectCallRequestInternal request = new RejectCallRequestInternal()
                .setIncomingCallContext(incomingCallContext);
            if (rejectCallOptions != null) {
                request.setCallRejectReason(CallRejectReason.fromString(rejectCallOptions.getCallRejectReason()));
            }

            return (context == null ? serverCallingInternal.rejectCallAsync(request)
                : serverCallingInternal.rejectCallAsync(request, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Reject a call
     *
     * @param incomingCallContext The incoming call context.
     * @param rejectCallOptions Options of the reject call.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> rejectCallWithResponse(String incomingCallContext, RejectCallOptions rejectCallOptions) {
        return rejectCallWithResponse(incomingCallContext, rejectCallOptions, null);
    }

    /**
     * Reject a call
     *
     * @param incomingCallContext The incoming call context.
     * @param rejectCallOptions Options of the reject call.
     * @param context The context to associate with this operation.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> rejectCallWithResponse(String incomingCallContext, RejectCallOptions rejectCallOptions,
                                                       Context context) {
        try {
            RejectCallRequestInternal request = new RejectCallRequestInternal()
                .setIncomingCallContext(incomingCallContext);
            if (rejectCallOptions != null) {
                request.setCallRejectReason(CallRejectReason.fromString(rejectCallOptions.getCallRejectReason()));
            }

            return (context == null ? serverCallingInternal.rejectCallWithResponseAsync(request)
                : serverCallingInternal.rejectCallWithResponseAsync(request, context));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }
}
