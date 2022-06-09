// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.models.CallingServerErrorException;
import com.azure.communication.callingserver.models.CreateCallOptions;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;

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
     * @param source The source identity.
     * @param target The target identity.
     * @param callbackUri The call back URI.
     * @param createCallOptions The call options.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CallConnection createCall(CommunicationIdentifier source, CommunicationIdentifier target,
                                                String callbackUri, CreateCallOptions createCallOptions) {
        return new CallConnection(callingServerAsyncClient.createCall(source, target, callbackUri, createCallOptions).block());
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
    public Response<CallConnection> createCallWithResponse(CommunicationIdentifier source, CommunicationIdentifier target,
                                                           String callbackUri, CreateCallOptions createCallOptions,
                                                           Context context) {
        return callingServerAsyncClient.createCallWithResponse(source, target, callbackUri, createCallOptions, context)
            .map(response -> new SimpleResponse<>(response, new CallConnection(response.getValue()))).block();
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
    public CallConnection answerCall(String incomingCallContext, String callbackUri) {
        return new CallConnection(callingServerAsyncClient.answerCall(incomingCallContext, callbackUri).block());
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
    public Response<CallConnection> answerCallWithResponse(String incomingCallContext, String callbackUri,
                                                           Context context) {
        return callingServerAsyncClient.answerCallWithResponse(incomingCallContext, callbackUri, context)
            .map(response -> new SimpleResponse<>(response, new CallConnection(response.getValue()))).block();
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
    public Void redirectCall(String incomingCallContext, CommunicationIdentifier target, String callbackUri) {
        return callingServerAsyncClient.redirectCall(incomingCallContext, target, callbackUri).block();
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
    public Response<Void> redirectCallWithResponse(String incomingCallContext, CommunicationIdentifier target, String callbackUri, Context context) {
        return callingServerAsyncClient.redirectCallWithResponse(incomingCallContext, target, callbackUri, context).block();
    }

    /**
     * Reject a call
     *
     * @param incomingCallContext The incoming call context.
     * @param callRejectReason The reason for rejecting call.
     * @param callbackUri The call back URI.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void rejectCall(String incomingCallContext, String callRejectReason, String callbackUri) {
        return callingServerAsyncClient.rejectCall(incomingCallContext, callRejectReason, callbackUri).block();
    }

    /**
     * Reject a call
     *
     * @param incomingCallContext The incoming call context.
     * @param callRejectReason The reason for rejecting call.
     * @param callbackUri The call back URI.
     * @param context The context to associate with this operation.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> rejectCallWithResponse(String incomingCallContext, String callRejectReason,
                                                       String callbackUri, Context context) {
        return callingServerAsyncClient.rejectCallWithResponse(incomingCallContext, callRejectReason, callbackUri, context).block();
    }
}
