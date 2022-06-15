// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.models.AnswerCallOptions;
import com.azure.communication.callingserver.models.CallSource;
import com.azure.communication.callingserver.models.CreateCallOptions;
import com.azure.communication.callingserver.models.RejectCallOptions;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;

import java.util.List;

/**
 * Synchronous client that supports calling server operations.
 *
 * <p><strong>Instantiating a synchronous Calling Server Client</strong></p>
 *
 * <p>View {@link CallingServerClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see CallingServerClientBuilder
 */
@ServiceClient(builder = CallingServerClientBuilder.class)
public final class CallingServerClient {
    private final CallingServerAsyncClient callingServerAsyncClient;

    CallingServerClient(CallingServerAsyncClient callingServerAsyncClient) {
        this.callingServerAsyncClient = callingServerAsyncClient;
    }

    /**
     * Create a call connection request from a source identity to a target identity.
     *
     * @param source The source property.
     * @param targets The targets of the call.
     * @param callbackUri The call back URI.
     * @param createCallOptions The call option.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return A CallConnection object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CallConnection createCall(CallSource source, List<CommunicationIdentifier> targets,
                           String callbackUri, CreateCallOptions createCallOptions) {
        return new CallConnection(callingServerAsyncClient.createCall(source, targets, callbackUri, createCallOptions).block());
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
    public Response<CallConnection> createCallWithResponse(CallSource source, List<CommunicationIdentifier> targets,
                                                 String callbackUri, CreateCallOptions createCallOptions,
                                                 Context context) {
        return callingServerAsyncClient.createCallWithResponse(source, targets, callbackUri, createCallOptions, context)
            .map(response -> new SimpleResponse<>(response, new CallConnection(response.getValue()))).block();
    }

    /**
     * Answer an incoming call
     *
     * @param incomingCallContext The incoming call context.
     * @param answerCallOptions The option of answering a call.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CallConnection answerCall(String incomingCallContext, AnswerCallOptions answerCallOptions) {
        return new CallConnection(callingServerAsyncClient.answerCall(incomingCallContext, answerCallOptions).block());
    }

    /**
     * Create a call connection request from a source identity to a target identity.
     *
     * @param incomingCallContext The incoming call context.
     * @param answerCallOptions The option of answering a call.
     * @param context The context to associate with this operation.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CallConnection> answerCallWithResponse(String incomingCallContext,
                                                           AnswerCallOptions answerCallOptions,
                                                           Context context) {
        return callingServerAsyncClient.answerCallWithResponse(incomingCallContext, answerCallOptions, context)
            .map(response -> new SimpleResponse<>(response, new CallConnection(response.getValue()))).block();
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
    public Void redirectCall(String incomingCallContext, CommunicationIdentifier target) {
        return callingServerAsyncClient.redirectCall(incomingCallContext, target).block();
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
    public Response<Void> redirectCallWithResponse(String incomingCallContext, CommunicationIdentifier target, Context context) {
        return callingServerAsyncClient.redirectCallWithResponse(incomingCallContext, target, context).block();
    }

    /**
     * Reject a call
     *
     * @param incomingCallContext The incoming call context.
     * @param rejectCallOptions The option for rejecting call.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void rejectCall(String incomingCallContext, RejectCallOptions rejectCallOptions) {
        return callingServerAsyncClient.rejectCall(incomingCallContext, rejectCallOptions).block();
    }

    /**
     * Reject a call
     *
     * @param incomingCallContext The incoming call context.
     * @param rejectCallOptions The option for rejecting call.
     * @param context The context to associate with this operation.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> rejectCallWithResponse(String incomingCallContext, RejectCallOptions rejectCallOptions,
                                                 Context context) {
        return callingServerAsyncClient.rejectCallWithResponse(incomingCallContext, rejectCallOptions, context).block();
    }
}
