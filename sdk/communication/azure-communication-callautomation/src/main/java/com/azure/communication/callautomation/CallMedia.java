// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.CallMediaRecognizeOptions;
import com.azure.communication.callautomation.models.ContinuousDtmfRecognitionOptions;
import com.azure.communication.callautomation.models.DtmfTone;
import com.azure.communication.callautomation.models.HoldOptions;
import com.azure.communication.callautomation.models.PlayOptions;
import com.azure.communication.callautomation.models.PlayToAllOptions;
import com.azure.communication.callautomation.models.SendDtmfTonesOptions;
import com.azure.communication.callautomation.models.SendDtmfTonesResult;
import com.azure.communication.callautomation.models.StartHoldMusicOptions;
import com.azure.communication.callautomation.models.StartTranscriptionOptions;
import com.azure.communication.callautomation.models.StopTranscriptionOptions;
import com.azure.communication.callautomation.models.PlaySource;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.exception.HttpResponseException;

import java.util.List;

/**
 * CallContent.
 */
public final class CallMedia {
    private final CallMediaAsync callMediaAsync;

    CallMedia(CallMediaAsync callMediaAsync) {
        this.callMediaAsync = callMediaAsync;
    }

    /**
     * Play
     *
     * @param playSources A List of {@link PlaySource} representing the sources to play.
     * @param playTo the targets to play to.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void play(List<PlaySource> playSources, List<CommunicationIdentifier> playTo) {
        callMediaAsync.play(playSources, playTo).block();
    }

    /**
     * Play
     *
     * @param playSource A {@link PlaySource} representing the source to play.
     * @param playTo the targets to play to.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void play(PlaySource playSource, List<CommunicationIdentifier> playTo) {
        callMediaAsync.play(playSource, playTo).block();
    }

    /**
     * Play to all participants
     *
     * @param playSources A List of {@link PlaySource} representing the sources to play.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void playToAll(List<PlaySource> playSources) {
        callMediaAsync.playToAll(playSources).block();
    }

    /**
     * Play to all participants
     *
     * @param playSource A {@link PlaySource} representing the source to play.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void playToAll(PlaySource playSource) {
        callMediaAsync.playToAll(playSource).block();
    }

    /**
     * PlayWithResponse
     *
     * @param options play options.
     * @param context A {@link Context} representing the request context.
     * @return Response for successful play request.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> playWithResponse(PlayOptions options, Context context) {
        return callMediaAsync.playWithResponseInternal(options, context).block();
    }

    /**
     * PlayAllWithResponse
     *
     * @param options play options.
     * @param context A {@link Context} representing the request context.
     * @return Response for successful playAll request.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> playToAllWithResponse(PlayToAllOptions options, Context context) {
        return callMediaAsync
            .playToAllWithResponseInternal(options, context)
            .block();
    }

    /**
     * Recognize tones.
     * @param callMediaRecognizeOptions Optional elements for recognize.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void startRecognizing(CallMediaRecognizeOptions callMediaRecognizeOptions) {
        callMediaAsync.startRecognizing(callMediaRecognizeOptions).block();
    }

    /**
     * Recognize tones.
     * @param callMediaRecognizeOptions Optional elements for recognize.
     * @param context A {@link Context} representing the request context.
     * @return Response for a successful recognize request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> startRecognizingWithResponse(CallMediaRecognizeOptions callMediaRecognizeOptions, Context context) {
        return callMediaAsync
            .recognizeWithResponseInternal(callMediaRecognizeOptions, context)
            .block();
    }

    /**
     * Cancels all the queued media operations.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void cancelAllMediaOperations() {
        cancelAllMediaOperationsWithResponse(null);
    }

    /**
     * Cancels all the queued media operations
     * @param context A {@link Context} representing the request context.
     * @return Response for successful playAll request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> cancelAllMediaOperationsWithResponse(Context context) {
        return callMediaAsync.cancelAllMediaOperationsWithResponseInternal(context).block();
    }

    /**
     * Sends Dtmf tones
     *
     * @param tones tones to be sent
     * @param targetParticipant the target participant
     * @return Response for successful sendDtmfTones request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public SendDtmfTonesResult sendDtmfTones(List<DtmfTone> tones, CommunicationIdentifier targetParticipant) {
        return callMediaAsync.sendDtmfTones(tones, targetParticipant).block();
    }

     /**
     * Sends Dtmf tones
     *
     * @param options SendDtmfTones configuration options
     * @param context Context
     * @return Response for successful sendDtmfTones request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<SendDtmfTonesResult> sendDtmfTonesWithResponse(SendDtmfTonesOptions options, Context context) {
        return callMediaAsync.sendDtmfTonesWithResponseInternal(options, context).block();
    }

    /**
     * Starts continuous Dtmf recognition.
     * @param targetParticipant the target participant
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void startContinuousDtmfRecognition(CommunicationIdentifier targetParticipant) {
        callMediaAsync.startContinuousDtmfRecognition(targetParticipant).block();
    }

    /**
     * Starts continuous Dtmf recognition.
     *
     * @param options ContinuousDtmfRecognition configuration options
     * @param context Context
     * @return Response for successful start continuous dtmf recognition request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> startContinuousDtmfRecognitionWithResponse(ContinuousDtmfRecognitionOptions options, Context context) {
        return callMediaAsync.startContinuousDtmfRecognitionWithResponseInternal(options, context).block();
    }

    /**
     * Stops continuous Dtmf recognition.
     * @param targetParticipant the target participant
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void stopContinuousDtmfRecognition(CommunicationIdentifier targetParticipant) {
        callMediaAsync.stopContinuousDtmfRecognition(targetParticipant).block();
    }

    /**
     * Stops continuous Dtmf recognition.
     * @param options ContinuousDtmfRecognition configuration options
     * @param context Context
     * @return Response for successful stop continuous dtmf recognition request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> stopContinuousDtmfRecognitionWithResponse(ContinuousDtmfRecognitionOptions options, Context context) {
        return callMediaAsync.stopContinuousDtmfRecognitionWithResponseInternal(options, context).block();
    }

    /**
     * Holds participant in call.
     * @param targetParticipant the target.
     * @param playSourceInfo audio to play.
     * @return Response for successful operation.
     * @deprecated This operations is deprecated, please use Hold instead.
    */
    @Deprecated
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void startHoldMusic(CommunicationIdentifier targetParticipant,
                               PlaySource playSourceInfo) {
        return callMediaAsync.startHoldMusic(targetParticipant, playSourceInfo).block();
    }

    /**
     * Holds participant in call.
     * @param options - Different options to pass to the request.
     * @param context Context
     * @return Response for successful operation.
     * @deprecated This operations is deprecated, please use HoldWithResponse instead
    */
    @Deprecated
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> startHoldMusicWithResponse(StartHoldMusicOptions options,
                                                     Context context) {
        return callMediaAsync.startHoldMusicWithResponseInternal(options, context).block();
    }

    /**
     * Removes hold from participant in call.
     * @param targetParticipant the target.
     * @return Response for successful operation.
     * @deprecated This operations is deprecated, please use Unhold instead.
    */
    @Deprecated
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void stopHoldMusic(CommunicationIdentifier targetParticipant) {
        return callMediaAsync.stopHoldMusic(targetParticipant).block();
    }
    
    /**
     * Removes hold from participant in call.
     * @param targetParticipant the target.
     * @param operationContext operational context.
     * @param context Context.
     * @return Response for successful operation.
     * @deprecated This operations is deprecated, please use UnholdWithResponse instead.
    */
    @Deprecated
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> stopHoldMusicWithResponse(CommunicationIdentifier targetParticipant,
                                                     String operationContext,
                                                     Context context) {
        return callMediaAsync.stopHoldMusicWithResponseInternal(targetParticipant, operationContext, context).block();
    }

    /**
     * Holds participant in call.
     * @param targetParticipant the target.
     * @return Response for successful operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void hold(CommunicationIdentifier targetParticipant) {
        return callMediaAsync.hold(targetParticipant).block();
    }

    /**
     * Holds participant in call.
     * @param options - Different options to pass to the request.
     * @param context Context
     * @return Response for successful operation.
    */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> holdWithResponse(HoldOptions options,
                                                     Context context) {
        return callMediaAsync.holdWithResponseInternal(options, context).block();
    }

    /**
     * Removes hold from participant in call.
     * @param targetParticipant the target.
     * @return Response for successful operation.
    */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void unhold(CommunicationIdentifier targetParticipant) {
        return callMediaAsync.unhold(targetParticipant).block();
    }

    /**
     * Removes hold from participant in call.
     * @param targetParticipant the target.
     * @param operationContext operational context.
     * @param context Context.
     * @return Response for successful operation.
    */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> unholdWithResponse(CommunicationIdentifier targetParticipant,
                                                     String operationContext,
                                                     Context context) {
        return callMediaAsync.unholdWithResponseInternal(targetParticipant, operationContext, context).block();
    }
    
    /**
     * Starts transcription in the call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void startTranscription() {
        callMediaAsync.startTranscription().block();
    }

    /**
     * Starts transcription in the call.
     *
     * @param options Options for the Start Transcription operation.
     * @param context Context
     * @return Response for successful start transcription request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> startTranscriptionWithResponse(StartTranscriptionOptions options, Context context) {
        return callMediaAsync.startTranscriptionWithResponseInternal(options, context).block();
    }

    /**
     * Stops transcription in the call.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void stopTranscription() {
        callMediaAsync.stopTranscription().block();
    }

    /**
     * Stops transcription in the call.
     *
     * @param options Options for the Stop Transcription operation.
     * @param context Context
     * @return Response for successful stop transcription request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> stopTranscriptionWithResponse(StopTranscriptionOptions options, Context context) {
        return callMediaAsync.stopTranscriptionWithResponseInternal(options, context).block();
    }

    /**
     * Updates transcription language in the call.
     * @param locale Defines new locale for transcription.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void updateTranscription(String locale) {
        callMediaAsync.updateTranscription(locale).block();
    }

    /**
     * Updates transcription language in the call.
     *
     * @param locale Defines new locale for transcription.
     * @param context Context
     * @return Response for successful update transcription request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> updateTranscriptionWithResponse(String locale, Context context) {
        return callMediaAsync.updateTranscriptionWithResponseInternal(locale, context).block();
    }
}
