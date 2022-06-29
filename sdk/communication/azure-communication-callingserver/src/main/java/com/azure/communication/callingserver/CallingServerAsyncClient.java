// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.AzureCommunicationCallingServerServiceImpl;
import com.azure.communication.callingserver.implementation.CallConnectionsImpl;
import com.azure.communication.callingserver.implementation.ServerCallingsImpl;
import com.azure.communication.callingserver.implementation.converters.AcsCallParticipantConverter;
import com.azure.communication.callingserver.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callingserver.implementation.converters.PhoneNumberIdentifierConverter;
import com.azure.communication.callingserver.implementation.models.AddParticipantsRequestInternal;
import com.azure.communication.callingserver.implementation.models.CommunicationIdentifierModel;
import com.azure.communication.callingserver.implementation.models.CreateCallRequestInternal;
import com.azure.communication.callingserver.implementation.models.AnswerCallRequestInternal;
import com.azure.communication.callingserver.implementation.models.GetParticipantRequestInternal;
import com.azure.communication.callingserver.implementation.models.RedirectCallRequestInternal;
import com.azure.communication.callingserver.implementation.models.RejectCallRequestInternal;
import com.azure.communication.callingserver.implementation.models.CallRejectReason;
import com.azure.communication.callingserver.implementation.models.CallSourceDto;
import com.azure.communication.callingserver.implementation.models.PhoneNumberIdentifierModel;
import com.azure.communication.callingserver.implementation.models.RemoveParticipantsRequestInternal;
import com.azure.communication.callingserver.implementation.models.TransferToParticipantRequestInternal;
import com.azure.communication.callingserver.models.AcsCallParticipant;
import com.azure.communication.callingserver.models.AddParticipantsResponse;
import com.azure.communication.callingserver.models.RemoveParticipantsResponse;
import com.azure.communication.callingserver.models.TransferCallResponse;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;
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
    private final ClientLogger logger;

    CallingServerAsyncClient(AzureCommunicationCallingServerServiceImpl callServiceClient) {
        callConnectionInternal = callServiceClient.getCallConnections();
        serverCallingInternal = callServiceClient.getServerCallings();
        logger = new ClientLogger(CallingServerAsyncClient.class);
    }

    //region Pre-call Actions
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
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CallConnection> createCall(CommunicationIdentifier source, List<CommunicationIdentifier> targets,
                                           String callbackUri, String sourceCallerId, String subject) {
        return createCallWithResponse(source, targets, callbackUri, sourceCallerId, subject).flatMap(FluxUtil::toMono);
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
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CallConnection>> createCallWithResponse(CommunicationIdentifier source,
                                                                 List<CommunicationIdentifier> targets,
                                                                 String callbackUri, String sourceCallerId,
                                                                 String subject) {
        return withContext(context -> createCallWithResponseInternal(source, targets, callbackUri, sourceCallerId,
            subject, context));
    }

    Mono<Response<CallConnection>> createCallWithResponseInternal(CommunicationIdentifier source,
                                                                  List<CommunicationIdentifier> targets,
                                                                  String callbackUri, String sourceCallerId,
                                                                  String subject,
                                                                  Context context) {
        try {
            context = context == null ? Context.NONE : context;
            List<CommunicationIdentifierModel> targetsModel = targets
                .stream().map(CommunicationIdentifierConverter::convert).collect(Collectors.toList());

            CallSourceDto callSourceDto = new CallSourceDto().setIdentifier(CommunicationIdentifierConverter.convert(source));
            if (sourceCallerId != null) {
                callSourceDto.setCallerId(new PhoneNumberIdentifierModel().setValue(sourceCallerId));
            }

            CreateCallRequestInternal request = new CreateCallRequestInternal()
                .setSource(callSourceDto)
                .setTargets(targetsModel)
                .setCallbackUri(callbackUri)
                .setSubject(subject);

            return serverCallingInternal.createCallWithResponseAsync(request, context).map(
                response -> new SimpleResponse<>(response, new CallConnection(response.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
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
    public Mono<CallConnection> answerCall(String incomingCallContext, String callbackUri) {
        return answerCallWithResponse(incomingCallContext, callbackUri).flatMap(FluxUtil::toMono);
    }

    /**
     * Create a call connection request from a source identity to a target identity.
     *
     * @param incomingCallContext The incoming call context.
     * @param callbackUri The call back uri. Optional
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CallConnection>> answerCallWithResponse(String incomingCallContext,
                                                                 String callbackUri) {
        return withContext(context -> answerCallWithResponseInternal(incomingCallContext, callbackUri, context));
    }

    Mono<Response<CallConnection>> answerCallWithResponseInternal(String incomingCallContext, String callbackUri,
                                                                  Context context) {
        try {
            context = context == null ? Context.NONE : context;

            AnswerCallRequestInternal request = new AnswerCallRequestInternal()
                .setIncomingCallContext(incomingCallContext)
                .setCallbackUri(callbackUri);

            return serverCallingInternal.answerCallWithResponseAsync(request, context)
                .map(response -> new SimpleResponse<>(response, new CallConnection(response.getValue())));
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
     * @param callRejectReason The reason why call is rejected. Optional
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

            return serverCallingInternal.rejectCallWithResponseAsync(request, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }
    //endregion

    //region Mid-call Actions
    /**
     * Get call connection properties.
     *
     * @param callConnectionId The connection id of the call
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<CallConnection> getCall(String callConnectionId) {
        return getCallWithResponse(callConnectionId).flatMap(FluxUtil::toMono);
    }

    /**
     * Get call connection properties.
     *
     * @param callConnectionId The connection id of the call
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<CallConnection>> getCallWithResponse(String callConnectionId) {
        return withContext(context -> getCallWithResponseInternal(callConnectionId, context));
    }

    Mono<Response<CallConnection>> getCallWithResponseInternal(String callConnectionId, Context context) {
        try {
            context = context == null ? Context.NONE : context;

            return callConnectionInternal.getCallWithResponseAsync(callConnectionId, context).map(response ->
                new SimpleResponse<>(response, new CallConnection(response.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Hangup a call.
     *
     * @param callConnectionId The connection id of the call
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful hangup request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> hangup(String callConnectionId) {
        return hangupWithResponse(callConnectionId).flatMap(FluxUtil::toMono);
    }

    /**
     * Hangup a call.
     *
     * @param callConnectionId The connection id of the call
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful hangup request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> hangupWithResponse(String callConnectionId) {
        return withContext(context -> hangupWithResponseInternal(callConnectionId, context));
    }

    Mono<Response<Void>> hangupWithResponseInternal(String callConnectionId, Context context) {
        try {
            context = context == null ? Context.NONE : context;

            return callConnectionInternal.hangupCallWithResponseAsync(callConnectionId, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Terminates the conversation for all participants in the call.
     *
     * @param callConnectionId The connection id of the call
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful call termination request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> terminateCall(String callConnectionId) {
        return terminateCallWithResponse(callConnectionId).flatMap(FluxUtil::toMono);
    }

    /**
     * Terminates the conversation for all participants in the call.
     *
     * @param callConnectionId The connection id of the call
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful call termination request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> terminateCallWithResponse(String callConnectionId) {
        return withContext(context -> terminateCallWithResponseInternal(callConnectionId, context));
    }

    Mono<Response<Void>> terminateCallWithResponseInternal(String callConnectionId, Context context) {
        try {
            context = context == null ? Context.NONE : context;

            return callConnectionInternal.terminateCallWithResponseAsync(callConnectionId, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Get a specific participant.
     *
     * @param callConnectionId The connection id of the call
     * @param participant The participant.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AcsCallParticipant> getParticipant(String callConnectionId, CommunicationIdentifier participant) {
        return getParticipantWithResponse(callConnectionId, participant).flatMap(FluxUtil::toMono);
    }

    /**
     * Get a specific participant.
     *
     * @param callConnectionId The connection id of the call
     * @param participant The participant.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AcsCallParticipant>> getParticipantWithResponse(String callConnectionId,
                                                                         CommunicationIdentifier participant) {
        return withContext(context -> getParticipantWithResponseInternal(callConnectionId, participant, context));
    }

    Mono<Response<AcsCallParticipant>> getParticipantWithResponseInternal(String callConnectionId,
                                                                          CommunicationIdentifier participant,
                                                                          Context context) {
        try {
            context = context == null ? Context.NONE : context;

            GetParticipantRequestInternal getParticipantRequestInternal = new GetParticipantRequestInternal()
                .setParticipant(CommunicationIdentifierConverter.convert(participant));

            return callConnectionInternal.getParticipantWithResponseAsync(callConnectionId,
                getParticipantRequestInternal, context).map(response ->
                new SimpleResponse<>(response, AcsCallParticipantConverter.convert(response.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Transfer the call to a participant.
     *
     * @param callConnectionId The connection id of the call
     * @param targetParticipant A {@link CommunicationIdentifier} representing the target participant of this transfer.
     * @param transfereeCallerId A {@link PhoneNumberIdentifier} representing the caller ID of the transferee
     *                           if transferring to a pstn number. Optional
     * @param userToUserInformation The user to user information. Optional
     * @param operationContext The operation context. Optional
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful call termination request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<TransferCallResponse> transferToParticipantCall(String callConnectionId,
                                                                CommunicationIdentifier targetParticipant,
                                                                PhoneNumberIdentifier transfereeCallerId,
                                                                String userToUserInformation, String operationContext) {
        return transferToParticipantCallWithResponse(
            callConnectionId, targetParticipant, transfereeCallerId, userToUserInformation, operationContext).flatMap(FluxUtil::toMono);
    }

    /**
     * Transfer the call to a participant.
     *
     * @param callConnectionId The connection id of the call
     * @param targetParticipant A {@link CommunicationIdentifier} representing the target participant of this transfer.
     * @param transfereeCallerId A {@link PhoneNumberIdentifier} representing the caller ID of the transferee
     *                           if transferring to a pstn number. Optional
     * @param userToUserInformation The user to user information. Optional
     * @param operationContext The operation context. Optional
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful call termination request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<TransferCallResponse>> transferToParticipantCallWithResponse(String callConnectionId,
                                                                                      CommunicationIdentifier targetParticipant, PhoneNumberIdentifier transfereeCallerId, String userToUserInformation,
                                                                                      String operationContext) {
        return withContext(context -> transferToParticipantCallWithResponseInternal(
            callConnectionId, targetParticipant, transfereeCallerId, userToUserInformation, operationContext, context));
    }

    Mono<Response<TransferCallResponse>> transferToParticipantCallWithResponseInternal(String callConnectionId,
                                                                                       CommunicationIdentifier targetParticipant, PhoneNumberIdentifier transfereeCallerId, String userToUserInformation,
                                                                                       String operationContext, Context context) {
        try {
            context = context == null ? Context.NONE : context;

            TransferToParticipantRequestInternal request = new TransferToParticipantRequestInternal()
                .setTargetParticipant(CommunicationIdentifierConverter.convert(targetParticipant))
                .setTransfereeCallerId(PhoneNumberIdentifierConverter.convert(transfereeCallerId))
                .setUserToUserInformation(userToUserInformation)
                .setOperationContext(operationContext);

            return callConnectionInternal.transferToParticipantWithResponseAsync(callConnectionId, request, context)
                .map(response ->
                    new SimpleResponse<>(response, new TransferCallResponse(response.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Add a participant to the call.
     *
     * @param callConnectionId The connection id of the call
     * @param participants The participants to invite.
     * @param sourceCallerId The source caller Id that's shown to the PSTN participant being invited.
     *                       Required only when inviting a PSTN participant. Optional
     * @param invitationTimeoutInSeconds The timeout to wait for the invited participant to pickup.
     *                                   The maximum value of this is 180 seconds. Optional
     * @param operationContext The operation context. Optional
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<AddParticipantsResponse> addParticipants(String callConnectionId,
                                                         List<CommunicationIdentifier> participants,
                                                         PhoneNumberIdentifier sourceCallerId,
                                                         Integer invitationTimeoutInSeconds,
                                                         String operationContext) {
        return addParticipantsWithResponse(callConnectionId, participants, sourceCallerId, invitationTimeoutInSeconds,
            operationContext).flatMap(FluxUtil::toMono);
    }

    /**
     * Add a participant to the call.
     *
     * @param callConnectionId The connection id of the call
     * @param participants The participants to invite.
     * @param sourceCallerId The source caller Id that's shown to the PSTN participant being invited.
     *                       Required only when inviting a PSTN participant. Optional
     * @param invitationTimeoutInSeconds The timeout to wait for the invited participant to pickup.
     *                                   The maximum value of this is 180 seconds. Optional
     * @param operationContext The operation context. Optional
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<AddParticipantsResponse>> addParticipantsWithResponse(String callConnectionId,
                                                                               List<CommunicationIdentifier> participants, PhoneNumberIdentifier sourceCallerId,
                                                                               Integer invitationTimeoutInSeconds, String operationContext) {
        return withContext(context -> addParticipantsWithResponseInternal(callConnectionId, participants, sourceCallerId,
            invitationTimeoutInSeconds, operationContext, context));
    }

    Mono<Response<AddParticipantsResponse>> addParticipantsWithResponseInternal(String callConnectionId,
                                                                                List<CommunicationIdentifier> participants,
                                                                                PhoneNumberIdentifier sourceCallerId,
                                                                                Integer invitationTimeoutInSeconds,
                                                                                String operationContext,
                                                                                Context context) {
        try {
            context = context == null ? Context.NONE : context;
            List<CommunicationIdentifierModel> participantModels = participants
                .stream().map(CommunicationIdentifierConverter::convert).collect(Collectors.toList());

            AddParticipantsRequestInternal request = new AddParticipantsRequestInternal()
                .setParticipantsToAdd(participantModels)
                .setSourceCallerId(PhoneNumberIdentifierConverter.convert(sourceCallerId))
                .setInvitationTimeoutInSeconds(invitationTimeoutInSeconds)
                .setOperationContext(operationContext);

            return callConnectionInternal.addParticipantWithResponseAsync(callConnectionId, request, context).map(
                response -> new SimpleResponse<>(response, new AddParticipantsResponse(response.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Remove a list of participants from the call.
     *
     * @param callConnectionId The connection id of the call
     * @param participantsToRemove The identifier list of the participant to be removed.
     * @param operationContext The operation context. Optional
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<RemoveParticipantsResponse> removeParticipants(String callConnectionId, List<CommunicationIdentifier> participantsToRemove,
                                                               String operationContext) {
        return removeParticipantsWithResponse(callConnectionId, participantsToRemove, operationContext).flatMap(FluxUtil::toMono);
    }

    /**
     * Remove a list of participants from the call.
     *
     * @param callConnectionId The connection id of the call
     * @param participantsToRemove The identifier list of the participant to be removed.
     * @param operationContext The operation context. Optional
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<RemoveParticipantsResponse>> removeParticipantsWithResponse(String callConnectionId,
                                                                                     List<CommunicationIdentifier> participantsToRemove,
                                                                                     String operationContext) {
        return withContext(context -> removeParticipantsWithResponseInternal(callConnectionId, participantsToRemove,
            operationContext, context));
    }

    Mono<Response<RemoveParticipantsResponse>> removeParticipantsWithResponseInternal(String callConnectionId,
                                                                                      List<CommunicationIdentifier> participantsToRemove, String operationContext, Context context) {
        try {
            context = context == null ? Context.NONE : context;
            List<CommunicationIdentifierModel> participantModels = participantsToRemove
                .stream().map(CommunicationIdentifierConverter::convert).collect(Collectors.toList());

            RemoveParticipantsRequestInternal request = new RemoveParticipantsRequestInternal()
                .setParticipantsToRemove(participantModels)
                .setOperationContext(operationContext);

            return callConnectionInternal.removeParticipantsWithResponseAsync(callConnectionId, request, context).map(
                response -> new SimpleResponse<>(response, new RemoveParticipantsResponse(response.getValue())));
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }
    //endregion
}
