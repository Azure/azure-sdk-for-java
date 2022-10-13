// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;


import com.azure.communication.callautomation.models.AnswerCallOptions;
import com.azure.communication.callautomation.models.AnswerCallResult;
import com.azure.communication.callautomation.models.CallingServerErrorException;
import com.azure.communication.callautomation.models.CreateCallOptions;
import com.azure.communication.callautomation.models.CreateCallResult;
import com.azure.communication.callautomation.models.RedirectCallOptions;
import com.azure.communication.callautomation.models.RejectCallOptions;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import java.util.List;

/**
 * Synchronous client that supports calling server operations.
 *
 * <p><strong>Instantiating a synchronous Calling Server Client</strong></p>
 *
 * <p>View {@link CallAutomationClientBuilder this} for additional ways to construct the client.</p>
 *
 * @see CallAutomationClientBuilder
 */
@ServiceClient(builder = CallAutomationClientBuilder.class)
public final class CallAutomationClient {
    private final CallAutomationAsyncClient callAutomationAsyncClient;

    CallAutomationClient(CallAutomationAsyncClient callAutomationAsyncClient) {
        this.callAutomationAsyncClient = callAutomationAsyncClient;
    }

    //region Pre-call Actions
    /**
     * Create a call connection request from a source identity to a target identity.
     *
     * @param source The caller.
     * @param targets The list of targets.
     * @param callbackUrl The call back url for receiving events.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return A CallConnectionDelete object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CreateCallResult createCall(CommunicationIdentifier source,
                                       List<CommunicationIdentifier> targets,
                                       String callbackUrl) {
        return callAutomationAsyncClient.createCall(source, targets, callbackUrl).block();
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
    public Response<CreateCallResult> createCallWithResponse(CreateCallOptions createCallOptions, Context context) {
        return callAutomationAsyncClient.createCallWithResponseInternal(createCallOptions, context).block();
    }

    /**
     * Answer an incoming call
     *
     * @param incomingCallContext The incoming call context.
     * @param callbackUrl The call back uri.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AnswerCallResult answerCall(String incomingCallContext, String callbackUrl) {
        return callAutomationAsyncClient.answerCall(incomingCallContext, callbackUrl).block();
    }

    /**
     * Create a call connection request from a source identity to a target identity.
     *
     * @param answerCallOptions The options of answering the call.
     * @param context The context to associate with this operation.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AnswerCallResult> answerCallWithResponse(AnswerCallOptions answerCallOptions,
                                                             Context context) {
        return callAutomationAsyncClient.answerCallWithResponseInternal(answerCallOptions, context).block();
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
        return callAutomationAsyncClient.redirectCall(incomingCallContext, target).block();
    }

    /**
     * Redirect a call
     *
     * @param redirectCallOptions options of redirecting a call
     * @param context The context to associate with this operation.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> redirectCallWithResponse(RedirectCallOptions redirectCallOptions, Context context) {
        return callAutomationAsyncClient.redirectCallWithResponseInternal(redirectCallOptions, context).block();
    }

    /**
     * Reject a call
     *
     * @param incomingCallContext The incoming call context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void rejectCall(String incomingCallContext) {
        return callAutomationAsyncClient.rejectCall(incomingCallContext).block();
    }

    /**
     * Reject a call
     *
     * @param rejectCallOptions The options of rejecting the call.
     * @param context The context to associate with this operation.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> rejectCallWithResponse(RejectCallOptions rejectCallOptions, Context context) {
        return callAutomationAsyncClient.rejectCallWithResponseInternal(rejectCallOptions, context).block();
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
    public CallConnection getCallConnection(String callConnectionId) {
        return new CallConnection(callAutomationAsyncClient.getCallConnectionAsync(callConnectionId));
    }
    //endregion

    //region Recording Management actions
    /***
     * Returns an object of CallRecording
     *
     * @return a CallRecording.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CallRecording getCallRecording() {
        return new CallRecording(callAutomationAsyncClient.getCallRecordingAsync());
    }
    //endregion
}
