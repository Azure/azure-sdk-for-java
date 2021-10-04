// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.models.CallRejectReason;
import com.azure.communication.callingserver.models.AddParticipantResult;
import com.azure.communication.callingserver.models.CallLocator;
import com.azure.communication.callingserver.models.CallParticipant;
import com.azure.communication.callingserver.models.CallRecordingProperties;
import com.azure.communication.callingserver.models.CallingServerErrorException;
import com.azure.communication.callingserver.models.CreateCallOptions;
import com.azure.communication.callingserver.models.JoinCallOptions;
import com.azure.communication.callingserver.models.ParallelDownloadOptions;
import com.azure.communication.callingserver.models.PlayAudioOptions;
import com.azure.communication.callingserver.models.PlayAudioResult;
import com.azure.communication.callingserver.models.StartCallRecordingResult;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceClient;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.HttpRange;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import java.io.OutputStream;
import java.nio.file.Path;
import java.net.URI;
import java.util.List;
import java.util.Objects;

/**
 * Synchronous client that supports calling server operations.
 *
 * <p><strong>Instantiating a synchronous Calling Server Client</strong></p>
 *
 * {@codesnippet com.azure.communication.callingserver.CallingServerClient.pipeline.instantiation}
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
     * Create a call connection request from source identity to targets identity.
     *
     * @param source The source of the call.
     * @param targets The targets of the call.
     * @param createCallOptions The call Options.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     *
     * {@codesnippet com.azure.communication.callingserver.CallingServerClient.create.call.connection}
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CallConnection createCallConnection(
        CommunicationIdentifier source,
        List<CommunicationIdentifier> targets,
        CreateCallOptions createCallOptions) {
        return callingServerAsyncClient.createCallConnectionInternal(source, targets, createCallOptions).block();
    }

    /**
     * Create a Call Connection Request from source identity to targets identity.
     *
     * @param source The source of the call.
     * @param targets The targets of the call.
     * @param createCallOptions The call Options.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful CreateCallConnection request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CallConnection> createCallConnectionWithResponse(
        CommunicationIdentifier source,
        List<CommunicationIdentifier> targets,
        CreateCallOptions createCallOptions,
        Context context) {
        return callingServerAsyncClient
            .createCallConnectionWithResponseInternal(source, targets, createCallOptions, context).block();
    }

    /**
     * Join a call
     *
     * @param callLocator The call locator.
     * @param source of Join Call request.
     * @param joinCallOptions to Join Call.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return CallConnection for a successful join request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CallConnection joinCall(
        CallLocator callLocator,
        CommunicationIdentifier source,
        JoinCallOptions joinCallOptions) {
        return callingServerAsyncClient.joinInternal(callLocator, source, joinCallOptions).block();
    }

    /**
     * Join a call
     *
     * @param callLocator The call locator.
     * @param source of Join Call request.
     * @param joinCallOptions to Join Call.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @param context A {@link Context} representing the request context.
     * @return Response for a successful join request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CallConnection> joinCallWithResponse(
        CallLocator callLocator,
        CommunicationIdentifier source,
        JoinCallOptions joinCallOptions,
        Context context) {
        return callingServerAsyncClient.joinWithResponseInternal(callLocator, source, joinCallOptions, context).block();
    }

    /**
     * Get CallConnection object
     *
     * @param callConnectionId The call connection id.
     * @return CallConnection.
     */
    public CallConnection getCallConnection(String callConnectionId) {
        return callingServerAsyncClient.getCallConnectionInternal(callConnectionId);
    }

     /**
     * Add a participant to the call.
     *
     * @param callLocator the call locator.
     * @param participant The identifier of the participant.
     * @param callBackUri callBackUri to get notifications.
     * @param alternateCallerId Phone number to use when adding a phone number participant.
     * @param operationContext Value to identify context of the operation. This is used to co-relate other
     *                         communications related to this operation
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful start recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public AddParticipantResult addParticipant(
        CallLocator callLocator,
        CommunicationIdentifier participant,
        URI callBackUri,
        String alternateCallerId,
        String operationContext) {
        return callingServerAsyncClient.addParticipant(callLocator, participant, callBackUri, alternateCallerId, operationContext).block();
    }

     /**
     * Add a participant to the call.
     *
     * @param callLocator the call locator.
     * @param participant The identifier of the participant.
     * @param callBackUri callBackUri to get notifications.
     * @param alternateCallerId Phone number to use when adding a phone number participant.
     * @param operationContext Value to identify context of the operation. This is used to co-relate other
     *                         communications related to this operation
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @param context A {@link Context} representing the request context.
     * @return Response for a successful join request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<AddParticipantResult> addParticipantWithResponse(
        CallLocator callLocator,
        CommunicationIdentifier participant,
        URI callBackUri,
        String alternateCallerId,
        String operationContext,
        Context context) {
        return callingServerAsyncClient.addParticipantWithResponse(callLocator, participant, callBackUri, alternateCallerId, operationContext, context).block();
    }

    /**
     * Remove a participant from the call.
     *
     * @param callLocator the call locator.
     * @param participant The identifier of the participant.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful remove participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void removeParticipant(CallLocator callLocator, CommunicationIdentifier participant) {
            return callingServerAsyncClient.removeParticipant(callLocator, participant).block();
    }

    /**
     * Remove a participant from the call.
     *
     * @param callLocator the call locator.
     * @param participant The identifier of the participant.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful remove participant request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> removeParticipantWithResponse(CallLocator callLocator, CommunicationIdentifier participant, Context context) {
        return callingServerAsyncClient.removeParticipantWithResponse(callLocator, participant, context).block();
    }

    /**
     * Get participant from the call.
     *
     * @param callLocator the call locator.
     * @param participant The identifier of the participant.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful get participant request.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public List<CallParticipant> getParticipant(CallLocator callLocator, CommunicationIdentifier participant) {
        return callingServerAsyncClient.getParticipant(callLocator, participant).block();
    }

    /**
     * Get participant from the call using identifier.
     *
     * @param callLocator the call locator.
     * @param participant The identifier of the participant.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful get participant request.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Response<List<CallParticipant>> getParticipantWithResponse(CallLocator callLocator, CommunicationIdentifier participant, Context context) {
        return callingServerAsyncClient.getParticipantWithResponse(callLocator, participant, context).block();
    }

    /**
     * Get all participants of the call.
     *
     * @param callLocator the call locator.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful get participants request.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public List<CallParticipant> getAllParticipants(CallLocator callLocator) {
        return callingServerAsyncClient.getAllParticipants(callLocator).block();
    }

    /**
     * Get all participants of the call.
     *
     * @param callLocator the call locator.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful get participants request.
     */
    @ServiceMethod(returns = ReturnType.COLLECTION)
    public Response<List<CallParticipant>> getAllParticipantsWithResponse(CallLocator callLocator, Context context) {
        return callingServerAsyncClient.getParticipantsWithResponse(callLocator, context).block();
    }

    /**
     * Start recording of the call.     *
     *
     * @param callLocator the call locator.
     * @param recordingStateCallbackUri Uri to send state change callbacks.
     * @throws InvalidParameterException is recordingStateCallbackUri is absolute uri.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful start recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public StartCallRecordingResult startRecording(CallLocator callLocator, URI recordingStateCallbackUri) {
        return callingServerAsyncClient.startRecording(callLocator, recordingStateCallbackUri).block();
    }

    /**
     * Start recording of the call.     *
     *
     * @param callLocator the call locator.
     * @param recordingStateCallbackUri Uri to send state change callbacks.
     * @throws InvalidParameterException is recordingStateCallbackUri is absolute uri.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @param context A {@link Context} representing the request context.
     * @return Response for a successful join request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<StartCallRecordingResult> startRecordingWithResponse(CallLocator callLocator, URI recordingStateCallbackUri, Context context) {
        return callingServerAsyncClient.startRecordingWithResponse(callLocator, recordingStateCallbackUri, context).block();
    }

    /**
     * Stop recording of the call.
     *
     * @param recordingId Recording id to stop.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
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
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful stop recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> stopRecordingWithResponse(String recordingId, final Context context) {
        return callingServerAsyncClient.stopRecordingWithResponse(recordingId, context).block();
    }

    /**
     * Pause recording of the call.
     *
     * @param recordingId Recording id to stop.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
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
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful pause recording request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> pauseRecordingWithResponse(String recordingId, final Context context) {
        return callingServerAsyncClient.pauseRecordingWithResponse(recordingId, context).block();
    }

    /**
     * Resume recording of the call.
     *
     * @param recordingId The recording id to stop.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
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
     * @throws CallingServerErrorException thrown if the request is rejected by server.
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
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful get recording state request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public CallRecordingProperties getRecordingState(String recordingId) {
        return callingServerAsyncClient.getRecordingState(recordingId).block();
    }

    /**
     * Get the current recording state by recording id.
     *
     * @param recordingId The recording id to stop.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response for a successful get recording state request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<CallRecordingProperties> getRecordingStateWithResponse(String recordingId, final Context context) {
        return callingServerAsyncClient.getRecordingStateWithResponse(recordingId, context).block();
    }

    /**
     * Download the recording content, e.g. Recording's metadata, Recording video, etc., from
     * {@code endpoint} and write it into the {@link OutputStream} passed as parameter.
     * @param sourceEndpoint - ACS URL where the content is located.
     * @param destinationStream - A stream where to write the downloaded content.
     * @param httpRange - An optional {@link HttpRange} value containing the range of bytes to download. If missing,
     *                  the whole content will be downloaded.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void downloadTo(String sourceEndpoint, OutputStream destinationStream, HttpRange httpRange) {
        downloadToWithResponse(sourceEndpoint, destinationStream, httpRange, Context.NONE);
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
                                                 Context context) {
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
        downloadToWithResponse(sourceEndpoint, destinationPath, parallelDownloadOptions, overwrite, Context.NONE);
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
                                                 Context context) {
        Objects.requireNonNull(sourceEndpoint, "'sourceEndpoint' cannot be null");
        Objects.requireNonNull(destinationPath, "'destinationPath' cannot be null");
        return callingServerAsyncClient.downloadToWithResponse(sourceEndpoint, destinationPath,
            parallelDownloadOptions, overwrite, context).block();
    }

    /**
     * Play audio in the call.
     *
     * @param callLocator The call locator.
     * @param audioFileUri The media resource uri of the play audio request. Currently only Wave file (.wav) format
     *                     audio prompts are supported. More specifically, the audio content in the wave file must
     *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param playAudioOptions Options for play audio.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PlayAudioResult playAudio(CallLocator callLocator, URI audioFileUri, PlayAudioOptions playAudioOptions) {
        return playAudioWithResponse(callLocator, audioFileUri, playAudioOptions, Context.NONE).getValue();
    }

    /**
     * Play audio in the call.
     *
     * @param callLocator The call locator.
     * @param audioFileUri The media resource uri of the play audio request. Currently only Wave file (.wav) format
     *                     audio prompts are supported. More specifically, the audio content in the wave file must
     *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param playAudioOptions Options for play audio.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PlayAudioResult> playAudioWithResponse(
        CallLocator callLocator,
        URI audioFileUri,
        PlayAudioOptions playAudioOptions,
        Context context) {
        return callingServerAsyncClient
            .playAudioWithResponseInternal(callLocator, audioFileUri, playAudioOptions, context)
            .block();
    }

    /**
     * Cancel Media Operation.
     *
     * @param callLocator The server call id.
     * @param mediaOperationId The Id of the media operation to Cancel.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void cancelMediaOperation(
        CallLocator callLocator,
        String mediaOperationId) {
        cancelMediaOperationWithResponse(callLocator, mediaOperationId, Context.NONE);
    }

    /**
     * Cancel Media Operation.
     *
     * @param callLocator The server call id.
     * @param mediaOperationId The Id of the media operation to Cancel.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> cancelMediaOperationWithResponse(
        CallLocator callLocator,
        String mediaOperationId,
        Context context) {
        return callingServerAsyncClient.cancelMediaOperationWithResponseInternal(callLocator, mediaOperationId, context).block();
    }

    /**
     * Cancel Participant Media Operation.
     *
     * @param callLocator The call locator.
     * @param participantId The participant id.
     * @param mediaOperationId The Id of the media operation to Cancel.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response containing the http response information
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void cancelParticipantMediaOperation(CallLocator callLocator, CommunicationIdentifier participant, String mediaOperationId) {
        cancelParticipantMediaOperationWithResponse(callLocator, participant, mediaOperationId, Context.NONE);
    }

    /**
     * Cancel Participant Media Operation.
     *
     * @param callLocator The call locator.
     * @param participantId The participant id.
     * @param mediaOperationId The Id of the media operation to Cancel.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response containing the http response information
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> cancelParticipantMediaOperationWithResponse(
        CallLocator callLocator,
        CommunicationIdentifier participant,
        String mediaOperationId,
        Context context) {
        return callingServerAsyncClient
            .cancelParticipantMediaOperationWithResponseInternal(callLocator, participant, mediaOperationId, context).block();
    }

    /**
     * Play audio to a participant.
     *
     * @param callLocator The call locator.
     * @param participantId The participant id.
     * @param audioFileUri The media resource uri of the play audio request. Currently only Wave file (.wav) format
     *                     audio prompts are supported. More specifically, the audio content in the wave file must
     *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param playAudioOptions Options for play audio.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio to participant operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PlayAudioResult playAudioToParticipant(CallLocator callLocator, CommunicationIdentifier participant, URI audioFileUri, PlayAudioOptions playAudioOptions) {
        return playAudioToParticipantWithResponse(callLocator, participant, audioFileUri, playAudioOptions, Context.NONE).getValue();
    }

    /**
     * Play audio to a participant.
     *
     * @param callLocator The call locator.
     * @param participantId The participant id.
     * @param audioFileUri The media resource uri of the play audio request. Currently only Wave file (.wav) format
     *                     audio prompts are supported. More specifically, the audio content in the wave file must
     *                     be mono (single-channel), 16-bit samples with a 16,000 (16KHz) sampling rate.
     * @param playAudioOptions Options for play audio.
     * @param context A {@link Context} representing the request context.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Response payload for play audio to participant operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PlayAudioResult> playAudioToParticipantWithResponse(
        CallLocator callLocator,
        CommunicationIdentifier participant,
        URI audioFileUri,
        PlayAudioOptions playAudioOptions,
        Context context) {
        return callingServerAsyncClient
            .playAudioToParticipantWithResponseInternal(callLocator, participant, audioFileUri, playAudioOptions, context)
            .block();
    }

    /**
     * Redirect the call.
     *
     * @param incomingCallContext the incomingCallContext value to set.
     * @param targets the targets value to set.
     * @param callbackUrl the callbackUrl value to set.
     * @param timeout the timeout value to set.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void redirectCall(String incomingCallContext, List<CommunicationIdentifier> targets, URI callbackUri, Integer timeout) {
        redirectCallWithResponse(incomingCallContext, targets, callbackUri, timeout, Context.NONE);
    }

    /**
     * Redirect the call.
     *
     * @param incomingCallContext the incomingCallContext value to set.
     * @param targets the targets value to set.
     * @param callbackUrl the callbackUrl value to set.
     * @param timeout the timeout value to set.
     * @param context A {@link Context} representing the request context.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> redirectCallWithResponse(String incomingCallContext, List<CommunicationIdentifier> targets, URI callbackUri, Integer timeout, Context context) {
        return callingServerAsyncClient.redirectCallWithResponseInternal(incomingCallContext, targets, callbackUri, timeout, context).block();
    }

    /**
     * Redirect the call.
     *
     * @param incomingCallContext the incomingCallContext value to set.
     * @param callbackUrl the callbackUrl value to set.
     * @param rejectReason the call reject reason value to set.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void rejectCall(String incomingCallContext, URI callbackUri, CallRejectReason callRejectReason) {
        rejectCallWithResponse(incomingCallContext, callbackUri, callRejectReason, Context.NONE);
    }

    /**
     * Redirect the call.
     *
     * @param incomingCallContext the incomingCallContext value to set.
     * @param callbackUrl the callbackUrl value to set.
     * @param rejectReason the call reject reason value to set.
     * @param context A {@link Context} representing the request context.
     * @throws IllegalArgumentException thrown if parameters fail the validation.
     * @throws CommunicationErrorResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> rejectCallWithResponse(String incomingCallContext, URI callbackUri, CallRejectReason callRejectReason, Context context) {
        return callingServerAsyncClient.rejectCallWithResponseInternal(incomingCallContext, callbackUri, callRejectReason, context).block();
    }
}
