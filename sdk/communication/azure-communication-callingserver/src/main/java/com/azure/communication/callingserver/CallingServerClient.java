// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

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
     * Get call connection properties.
     *
     * @param callConnectionId the call connection Id
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CallConnection getCall(String callConnectionId) {
        return new CallConnection(callingServerAsyncClient.getCall(callConnectionId).block());
    }

    /**
     * Get call connection properties.
     *
     * @param callConnectionId the call connection Id
     * @param context A {@link Context} representing the request context.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CallConnection> getCallWithResponse(String callConnectionId, Context context) {
        return callingServerAsyncClient.getCallWithResponseInternal(callConnectionId, context)
            .map(response -> new SimpleResponse<>(response, new CallConnection(response.getValue()))).block();
    }

    /**
     * Create a call connection request from a source identity to a target identity.
     *
     * @param source The source property.
     * @param targets The targets of the call.
     * @param callbackUri The call back URI.
     * @param sourceCallerId The source caller Id that's shown to the PSTN participant being invited.
     *                       Required only when inviting a PSTN participant. Optional
     * @param subject The subject. Optional
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return A CallConnection object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CallConnection createCall(CommunicationIdentifier source, List<CommunicationIdentifier> targets,
                           String callbackUri, String sourceCallerId, String subject) {
        return new CallConnection(callingServerAsyncClient.createCall(source, targets, callbackUri, sourceCallerId,
            subject).block());
    }

    /**
     * Create a call connection request from a source identity to a target identity.
     *
     * @param source The source property.
     * @param targets The targets of the call.
     * @param callbackUri The call back URI.
     * @param sourceCallerId The source caller Id that's shown to the PSTN participant being invited.
     *                       Required only when inviting a PSTN participant. Optional
     * @param subject The subject. Optional
     * @param context The context to associate with this operation.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CallConnection> createCallWithResponse(CommunicationIdentifier source, List<CommunicationIdentifier> targets,
                                                 String callbackUri, String sourceCallerId, String subject,
                                                 Context context) {
        return callingServerAsyncClient.createCallWithResponseInternal(source, targets, callbackUri, sourceCallerId,
                subject, context)
            .map(response -> new SimpleResponse<>(response, new CallConnection(response.getValue()))).block();
    }

    /**
     * Answer an incoming call
     *
     * @param incomingCallContext The incoming call context.
     * @param callbackUri The call back uri. Optional
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
     * @param callbackUri The call back uri. Optional
     * @param context The context to associate with this operation.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CallConnection> answerCallWithResponse(String incomingCallContext, String callbackUri,
                                                           Context context) {
        return callingServerAsyncClient.answerCallWithResponseInternal(incomingCallContext, callbackUri, context)
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
        return callingServerAsyncClient.redirectCallWithResponseInternal(incomingCallContext, target, context).block();
    }

    /**
     * Reject a call
     *
     * @param incomingCallContext The incoming call context.
     * @param callRejectReason The reason why call is rejected. Optional
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void rejectCall(String incomingCallContext, String callRejectReason) {
        return callingServerAsyncClient.rejectCall(incomingCallContext, callRejectReason).block();
    }

    /**
     * Reject a call
     *
     * @param incomingCallContext The incoming call context.
     * @param callRejectReason The reason why call is rejected. Optional
     * @param context The context to associate with this operation.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> rejectCallWithResponse(String incomingCallContext, String callRejectReason,
                                                 Context context) {
        return callingServerAsyncClient.rejectCallWithResponseInternal(incomingCallContext, callRejectReason, context).block();
    }
}
