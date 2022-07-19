// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;


import com.azure.communication.callingserver.models.CallConnectionProperties;
import com.azure.communication.callingserver.models.CallingServerErrorException;
import com.azure.communication.callingserver.models.CreateCallOptions;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
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

    //region Pre-call Actions
    /**
     * Create a call connection request from a source identity to a target identity.
     *
     * @param createCallOptions Options bag for creating a new call.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return A CallConnectionDelete object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CallConnectionProperties createCall(CreateCallOptions createCallOptions) {
        return callingServerAsyncClient.createCall(createCallOptions).block();
    }

    /**
     * Create a call connection request from a source identity to a target identity.
     *
     * @param createCallOptions Options bag for creating a new call.
     * @param context The context to associate with this operation.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CallConnectionProperties> createCallWithResponse(CreateCallOptions createCallOptions, Context context) {
        return callingServerAsyncClient.createCallWithResponseInternal(createCallOptions, context).block();
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
    public CallConnectionProperties answerCall(String incomingCallContext, String callbackUri) {
        return callingServerAsyncClient.answerCall(incomingCallContext, callbackUri).block();
    }

    /**
     * Create a call connection request from a source identity to a target identity.
     *
     * @param incomingCallContext The incoming call context.
     * @param callbackUri The call back uri. Optional
     * @param context The context to associate with this operation.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CallConnectionProperties> answerCallWithResponse(String incomingCallContext, String callbackUri,
                                                                     Context context) {
        return callingServerAsyncClient.answerCallWithResponseInternal(incomingCallContext, callbackUri, context).block();
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
    public Void redirectCall(String incomingCallContext, CommunicationIdentifier target) {
        return callingServerAsyncClient.redirectCall(incomingCallContext, target).block();
    }

    /**
     * Redirect a call
     *
     * @param incomingCallContext The incoming call context.
     * @param target The target identity.
     * @param context The context to associate with this operation.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
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
     * @throws CallingServerErrorException thrown if the request is rejected by server.
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
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> rejectCallWithResponse(String incomingCallContext, String callRejectReason,
                                                 Context context) {
        return callingServerAsyncClient.rejectCallWithResponseInternal(incomingCallContext, callRejectReason, context).block();
    }
    //endregion

    //region Mid-call Actions
    /***
     * Returns an object of CallConnection
     *
     * @param callConnectionId the id of the call connection
     * @return a CallConnection.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CallConnection getCallConnectionClient(String callConnectionId) {
        return new CallConnection(callingServerAsyncClient.getCallConnectionAsyncClient(callConnectionId));
    }
    //endregion

    //region Recording Management actions
    /***
     * Returns an object of CallContent
     *
     * @param callConnectionId the id of the call connection
     * @return a CallContentAsync.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CallContent getCallContentClient(String callConnectionId) {
        return new CallContent(callingServerAsyncClient.getCallContentAsyncClient(callConnectionId));
    }

    /***
     * Returns an object of CallRecording
     *
     * @return a CallRecording.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CallRecording getCallRecordingClient() {
        return new CallRecording(callingServerAsyncClient.getCallRecordingAsyncClient());
    }
    //endregion
}
