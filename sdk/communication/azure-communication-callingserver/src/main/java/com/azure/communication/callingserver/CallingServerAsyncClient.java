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
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;


import java.util.List;
import java.util.Objects;

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
    private final ClientLogger logger;

    CallingServerAsyncClient(AzureCommunicationCallingServerServiceImpl callServiceClient) {
        callConnectionInternal = callServiceClient.getCallConnections();
        serverCallingInternal = callServiceClient.getServerCallings();
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
        return createCallWithResponse(source, targets, callbackUri, createCallOptions).flatMap(FluxUtil::toMono);
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
    public Mono<Response<CallConnectionAsync>> createCallWithResponse(CallSource source,
                                                                      List<CommunicationIdentifier> targets,
                                                                      String callbackUri,
                                                                      CreateCallOptions createCallOptions) {
        return withContext(context -> createCallWithResponseInternal(source, targets, callbackUri,
            createCallOptions, context));
    }

    Mono<Response<CallConnectionAsync>> createCallWithResponseInternal(CallSource source,
                                                                       List<CommunicationIdentifier> targets,
                                                                       String callbackUri,
                                                                       CreateCallOptions createCallOptions,
                                                                       Context context) {
        try {
            Objects.requireNonNull(source, "The source parameter cannot be null.");
            Objects.requireNonNull(targets, "The targets parameter cannot be null.");
            Objects.requireNonNull(callbackUri, "The callbackUri parameter cannot be null.");
            context = context == null ? Context.NONE : context;
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

            return serverCallingInternal.createCallWithResponseAsync(request, context).map(
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
        return answerCallWithResponse(incomingCallContext, answerCallOptions).flatMap(FluxUtil::toMono);
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
        return withContext(context -> answerCallWithResponseInternal(incomingCallContext, answerCallOptions, context));
    }

    Mono<Response<CallConnectionAsync>> answerCallWithResponseInternal(String incomingCallContext,
                                                                       AnswerCallOptions answerCallOptions,
                                                                       Context context) {
        try {
            Objects.requireNonNull(incomingCallContext, "The incomingCallContext parameter cannot be null.");
            context = context == null ? Context.NONE : context;

            AnswerCallRequestInternal request = new AnswerCallRequestInternal()
                .setIncomingCallContext(incomingCallContext);
            if (answerCallOptions != null) {
                request.setCallbackUri(answerCallOptions.getCallbackUri());
            }

            return serverCallingInternal.answerCallWithResponseAsync(request, context)
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
        return redirectCallWithResponse(incomingCallContext, target).flatMap(FluxUtil::toMono);
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
        return withContext(context -> redirectCallWithResponseInternal(incomingCallContext, target, context));
    }

    Mono<Response<Void>> redirectCallWithResponseInternal(String incomingCallContext, CommunicationIdentifier target,
                                                          Context context) {
        try {
            Objects.requireNonNull(incomingCallContext, "The incomingCallContext parameter cannot be null.");
            Objects.requireNonNull(target, "The target parameter cannot be null.");
            context = context == null ? Context.NONE : context;

            RedirectCallRequestInternal request = new RedirectCallRequestInternal()
                .setIncomingCallContext(incomingCallContext)
                .setTarget(CommunicationIdentifierConverter.convert(target));

            return serverCallingInternal.redirectCallWithResponseAsync(request, context);
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
        return rejectCallWithResponse(incomingCallContext, rejectCallOptions).flatMap(FluxUtil::toMono);
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
        return withContext(context -> rejectCallWithResponseInternal(incomingCallContext, rejectCallOptions, context));
    }

    Mono<Response<Void>> rejectCallWithResponseInternal(String incomingCallContext, RejectCallOptions rejectCallOptions,
                                                        Context context) {
        try {
            Objects.requireNonNull(incomingCallContext, "The incomingCallContext parameter cannot be null.");
            context = context == null ? Context.NONE : context;

            RejectCallRequestInternal request = new RejectCallRequestInternal()
                .setIncomingCallContext(incomingCallContext);
            if (rejectCallOptions != null) {
                request.setCallRejectReason(CallRejectReason.fromString(rejectCallOptions.getCallRejectReason()));
            }

            return serverCallingInternal.rejectCallWithResponseAsync(request, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }
}
