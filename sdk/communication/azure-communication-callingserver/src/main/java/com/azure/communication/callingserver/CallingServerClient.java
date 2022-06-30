// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.models.CallLocator;
import com.azure.communication.callingserver.implementation.models.GetCallRecordingStateResponse;
import com.azure.communication.callingserver.implementation.models.RecordingChannel;
import com.azure.communication.callingserver.implementation.models.RecordingContent;
import com.azure.communication.callingserver.implementation.models.RecordingFormat;
import com.azure.communication.callingserver.implementation.models.StartCallRecordingResponse;
import com.azure.communication.callingserver.models.AcsCallParticipant;
import com.azure.communication.callingserver.models.AddParticipantsResponse;
import com.azure.communication.callingserver.models.ParallelDownloadOptions;
import com.azure.communication.callingserver.models.RemoveParticipantsResponse;
import com.azure.communication.callingserver.models.TransferCallResponse;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.communication.common.PhoneNumberIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpRange;
import com.azure.core.http.HttpResponse;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import java.io.OutputStream;
import java.net.URI;
import java.nio.file.Path;
import java.security.InvalidParameterException;
import java.util.List;
import java.util.Objects;

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
     * @param source The source property.
     * @param targets The targets of the call.
     * @param callbackUri The call back URI.
     * @param sourceCallerId The source caller Id that's shown to the PSTN participant being invited.
     *                       Required only when inviting a PSTN participant. Optional
     * @param subject The subject. Optional
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return A CallConnectionDelete object.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CallConnection createCall(CommunicationIdentifier source, List<CommunicationIdentifier> targets,
                                           String callbackUri, String sourceCallerId, String subject) {
        return callingServerAsyncClient.createCall(source, targets, callbackUri, sourceCallerId, subject).block();
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
                subject, context).block();
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
        return callingServerAsyncClient.answerCall(incomingCallContext, callbackUri).block();
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
        return callingServerAsyncClient.answerCallWithResponseInternal(incomingCallContext, callbackUri, context).block();
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
    //endregion

    //region Mid-call Actions
    /**
     * Get call connection properties.
     *
     * @param callConnectionId the call connection Id
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CallConnection getCall(String callConnectionId) {
        return callingServerAsyncClient.getCall(callConnectionId).block();
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
        return callingServerAsyncClient.getCallWithResponseInternal(callConnectionId, context).block();
    }

    /**
     * Hangup a call.
     *
     * @param callConnectionId The connection id of the call
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful hangup request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void hangup(String callConnectionId) {
        return callingServerAsyncClient.hangup(callConnectionId).block();
    }

    /**
     * Hangup a call.
     *
     * @param callConnectionId The connection id of the call
     * @param context A {@link Context} representing the request context.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful hangup request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> hangupWithResponse(String callConnectionId, Context context) {
        return callingServerAsyncClient.hangupWithResponseInternal(callConnectionId, context).block();
    }


    /**
     * Terminates the conversation for all participants in the call.
     *
     * @param callConnectionId The connection id of the call
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful call termination request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void terminateCall(String callConnectionId) {
        return callingServerAsyncClient.terminateCall(callConnectionId).block();
    }

    /**
     * Terminates the conversation for all participants in the call.
     *
     * @param callConnectionId The connection id of the call
     * @param context A {@link Context} representing the request context.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful call termination request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> terminateCallWithResponse(String callConnectionId, Context context) {
        return callingServerAsyncClient.terminateCallWithResponseInternal(callConnectionId, context).block();
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
    public AcsCallParticipant getParticipant(String callConnectionId, CommunicationIdentifier participant) {
        return callingServerAsyncClient.getParticipant(callConnectionId, participant).block();
    }

    /**
     * Get all participants.
     *
     * @param callConnectionId The connection id of the call
     * @param participant The participant.
     * @param context A {@link Context} representing the request context.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AcsCallParticipant> getParticipantWithResponse(String callConnectionId,
                                                                   CommunicationIdentifier participant,
                                                                   Context context) {
        return callingServerAsyncClient.getParticipantWithResponseInternal(callConnectionId, participant, context).block();
    }

    /**
     * Get all participants.
     *
     * @param callConnectionId The connection id of the call
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public List<AcsCallParticipant> listParticipants(String callConnectionId) {
        return callingServerAsyncClient.listParticipants(callConnectionId).block();
    }

    /**
     * Get all participants.
     *
     * @param callConnectionId The connection id of the call
     * @param context A {@link Context} representing the request context.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for a successful get call connection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<List<AcsCallParticipant>> listParticipantsWithResponse(String callConnectionId, Context context) {
        return callingServerAsyncClient.listParticipantsWithResponseInternal(callConnectionId, context).block();
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
    public TransferCallResponse transferToParticipantCall(String callConnectionId,
                                                          CommunicationIdentifier targetParticipant,
                                                          PhoneNumberIdentifier transfereeCallerId,
                                                          String userToUserInformation, String operationContext) {
        return callingServerAsyncClient.transferToParticipantCall(
            callConnectionId, targetParticipant, transfereeCallerId, userToUserInformation, operationContext).block();
    }

    /**
     * Transfer the call to a participant.
     *
     * @param callConnectionId The connection id of the call
     * @param targetParticipant A {@link CommunicationIdentifier} representing the target participant of this transfer.
     * @param transfereeCallerId A {@link PhoneNumberIdentifier} representing the caller ID of the transferee
     *                           if transferring to a pstn number.
     * @param userToUserInformation The user to user information. Optional
     * @param operationContext The operation context. Optional
     * @param context A {@link Context} representing the request context.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful call termination request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<TransferCallResponse> transferToParticipantCallWithResponse(String callConnectionId,
        CommunicationIdentifier targetParticipant, PhoneNumberIdentifier transfereeCallerId,
        String userToUserInformation, String operationContext, Context context) {
        return callingServerAsyncClient.transferToParticipantCallWithResponseInternal(
            callConnectionId, targetParticipant, transfereeCallerId, userToUserInformation, operationContext, context).block();
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
    public AddParticipantsResponse addParticipants(String callConnectionId, List<CommunicationIdentifier> participants,
                                                   PhoneNumberIdentifier sourceCallerId, Integer invitationTimeoutInSeconds,
                                                   String operationContext) {
        return callingServerAsyncClient.addParticipants(callConnectionId, participants, sourceCallerId,
            invitationTimeoutInSeconds, operationContext).block();
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
     * @param context A {@link Context} representing the request context.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AddParticipantsResponse> addParticipantsWithResponse(String callConnectionId,
                                                                         List<CommunicationIdentifier> participants,
                                                                         PhoneNumberIdentifier sourceCallerId,
                                                                         Integer invitationTimeoutInSeconds,
                                                                         String operationContext,
                                                                         Context context) {
        return callingServerAsyncClient.addParticipantsWithResponseInternal(callConnectionId, participants, sourceCallerId,
            invitationTimeoutInSeconds, operationContext, context).block();
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
    public RemoveParticipantsResponse removeParticipants(String callConnectionId,
                                                         List<CommunicationIdentifier> participantsToRemove,
                                                         String operationContext) {
        return callingServerAsyncClient.removeParticipants(callConnectionId, participantsToRemove, operationContext).block();
    }

    /**
     * Remove a list of participant from the call.
     *
     * @param callConnectionId The connection id of the call
     * @param participantsToRemove The identifier list of the participant to be removed.
     * @param operationContext The operation context. Optional
     * @param context A {@link Context} representing the request context.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful add participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<RemoveParticipantsResponse> removeParticipantsWithResponse(String callConnectionId,
                                                                               List<CommunicationIdentifier> participantsToRemove,
                                                                               String operationContext, Context context) {
        return callingServerAsyncClient.removeParticipantsWithResponseInternal(callConnectionId, participantsToRemove,
            operationContext, context).block();
    }
    //endregion

    //region Recording Management actions
    /**
     * Start recording of the call.     *
     *
     * @param callLocator the call locator.
     * @param recordingStateCallbackUri Uri to send state change callbacks.
     * @throws InvalidParameterException is recordingStateCallbackUri is absolute uri.
//     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Result for a successful start recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public StartCallRecordingResponse startRecording(CallLocator callLocator, URI recordingStateCallbackUri) {
        return callingServerAsyncClient.startRecording(callLocator, recordingStateCallbackUri).block();
    }

    /**
     * Start recording of the call.     *
     *
     * @param callLocator the call locator.
     * @param recordingStateCallbackUri Uri to send state change callbacks.
     * @param content Content Type.
     * @param format Format Type.
     * @param channel Channel Type
     * @param context A {@link Context} representing the request context.
     * @throws InvalidParameterException is recordingStateCallbackUri is absolute uri.
//     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Result for a successful start recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<StartCallRecordingResponse> startRecordingWithResponse(
        CallLocator callLocator,
        URI recordingStateCallbackUri,
        RecordingContent content,
        RecordingFormat format,
        RecordingChannel channel,
        Context context) {
        return callingServerAsyncClient.startRecordingWithResponse(
            callLocator,
            recordingStateCallbackUri,
            content,
            format,
            channel,
            context).block();
    }

    /**
     * Stop recording of the call.
     *
     * @param recordingId Recording id to stop.
//     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void stopRecording(String recordingId) {
        callingServerAsyncClient.stopRecording(recordingId).block();
    }

    /**
     * Stop recording of the call.
     *
     * @param recordingId Recording id to stop.
     * @param context A {@link Context} representing the request context.
//     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful stop recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> stopRecordingWithResponse(String recordingId, Context context) {
        return callingServerAsyncClient.stopRecordingWithResponse(recordingId, context).block();
    }

    /**
     * Pause recording of the call.
     *
     * @param recordingId Recording id to stop.
//     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void pauseRecording(String recordingId) {
        callingServerAsyncClient.pauseRecording(recordingId).block();
    }

    /**
     * Pause recording of the call.
     *
     * @param recordingId Recording id to stop.
     * @param context A {@link Context} representing the request context.
//     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful pause recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> pauseRecordingWithResponse(String recordingId, Context context) {
        return callingServerAsyncClient.pauseRecordingWithResponse(recordingId, context).block();
    }

    /**
     * Resume recording of the call.
     *
     * @param recordingId The recording id to stop.
//     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void resumeRecording(String recordingId) {
        callingServerAsyncClient.resumeRecording(recordingId).block();
    }

    /**
     * Resume recording of the call.
     *
     * @param recordingId The recording id to stop.
     * @param context A {@link Context} representing the request context.
//     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful resume recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> resumeRecordingWithResponse(String recordingId, final Context context) {
        return callingServerAsyncClient.resumeRecordingWithResponse(recordingId, context).block();
    }

    /**
     * Get the current recording state by recording id.
     *
     * @param recordingId The recording id to stop.
//     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful get recording state request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public GetCallRecordingStateResponse getRecordingState(String recordingId) {
        return callingServerAsyncClient.getRecordingState(recordingId).block();
    }

    /**
     * Get the current recording state by recording id.
     *
     * @param recordingId The recording id to stop.
     * @param context A {@link Context} representing the request context.
//     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful get recording state request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<GetCallRecordingStateResponse> getRecordingStateWithResponse(String recordingId, Context context) {
        return callingServerAsyncClient.getRecordingStateWithResponse(recordingId, context).block();
    }

    /**
     * Download the recording content, e.g. Recording's metadata, Recording video, etc., from
     * {@code endpoint} and write it in the {@link OutputStream} passed as parameter.
     * @param sourceEndpoint - ACS URL where the content is located.
     * @param destinationStream - A stream where to write the downloaded content.
     * @param httpRange - An optional {@link HttpRange} value containing the range of bytes to download. If missing,
     *                  the whole content will be downloaded.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void downloadTo(String sourceEndpoint, OutputStream destinationStream, HttpRange httpRange) {
        downloadToWithResponse(sourceEndpoint, destinationStream, httpRange, null);
    }

    /**
     * Download the recording content, e.g. Recording's metadata, Recording video, etc., from
     * {@code endpoint} and write it in the {@link OutputStream} passed as parameter.
     * @param sourceEndpoint - ACS URL where the content is located.
     * @param destinationStream - A stream where to write the downloaded content.
     * @param httpRange - An optional {@link HttpRange} value containing the range of bytes to download. If missing,
     *                  the whole content will be downloaded.
     * @param context A {@link Context} representing the request context.
     * @return Response containing the http response information from the download.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> downloadToWithResponse(String sourceEndpoint,
                                                 OutputStream destinationStream,
                                                 HttpRange httpRange,
                                                 final Context context) {
        Objects.requireNonNull(sourceEndpoint, "'sourceEndpoint' cannot be null");
        Objects.requireNonNull(destinationStream, "'destinationStream' cannot be null");
        return callingServerAsyncClient
            .downloadToWithResponse(sourceEndpoint, destinationStream, httpRange, context)
            .block();
    }

    /**
     * Download the content located in {@code endpoint} into a file marked by {@code path}.
     * This download will be done using parallel workers.
     * @param sourceEndpoint - ACS URL where the content is located.
     * @param destinationPath - File location.
     * @param parallelDownloadOptions - an optional {@link ParallelDownloadOptions} object to modify how the parallel
     *                               download will work.
     * @param overwrite - True to overwrite the file if it exists.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void downloadTo(String sourceEndpoint,
                           Path destinationPath,
                           ParallelDownloadOptions parallelDownloadOptions,
                           boolean overwrite) {
        downloadToWithResponse(sourceEndpoint, destinationPath, parallelDownloadOptions, overwrite, null);
    }

    /**
     * Download the content located in {@code endpoint} into a file marked by {@code path}.
     * This download will be done using parallel workers.
     * @param sourceEndpoint - ACS URL where the content is located.
     * @param destinationPath - File location.
     * @param parallelDownloadOptions - an optional {@link ParallelDownloadOptions} object to modify how the parallel
     *                               download will work.
     * @param overwrite - True to overwrite the file if it exists.
     * @param context A {@link Context} representing the request context.
     * @return Response containing the http response information from the download.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> downloadToWithResponse(String sourceEndpoint,
                                                 Path destinationPath,
                                                 ParallelDownloadOptions parallelDownloadOptions,
                                                 boolean overwrite,
                                                 final Context context) {
        Objects.requireNonNull(sourceEndpoint, "'sourceEndpoint' cannot be null");
        Objects.requireNonNull(destinationPath, "'destinationPath' cannot be null");
        return callingServerAsyncClient.downloadToWithResponse(sourceEndpoint, destinationPath,
            parallelDownloadOptions, overwrite, context).block();
    }

    /**
     * Delete the content located in the deleteEndpoint
     *
     * @param deleteEndpoint - ACS URL where the content is located.
     * @param context A {@link Context} representing the request context.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void deleteRecording(String deleteEndpoint, final Context context) {
        callingServerAsyncClient.deleteRecordingWithResponse(deleteEndpoint, context).block();
    }

    /**
     * Delete the content located in the deleteEndpoint
     *
     * @param deleteEndpoint - ACS URL where the content is located.
     * @param context A {@link Context} representing the request context.
     * @return Response containing the http response information from the download.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<HttpResponse> deleteRecordingWithResponse(String deleteEndpoint, final Context context) {
        return callingServerAsyncClient.deleteRecordingWithResponse(deleteEndpoint, context).block();
    }

    /***
     * Returns an object of ContentCapabilities
     * @param callConnectionId
     * @return
     */ @ServiceMethod(returns = ReturnType.SINGLE)
    public ContentCapabilities getContentCapabilities(String callConnectionId) {
        return new ContentCapabilities(callingServerAsyncClient.getContentCapabilities(callConnectionId));
    }
    //endregion
}
