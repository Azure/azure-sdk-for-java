// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.DtmfTone;
import com.azure.communication.callautomation.models.PlayOptions;
import com.azure.communication.callautomation.models.PlayToAllOptions;
import com.azure.communication.callautomation.models.PlaySource;
import com.azure.communication.callautomation.models.CallMediaRecognizeOptions;
import com.azure.communication.callautomation.models.StartHoldMusicOptions;
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
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void sendDtmf(List<DtmfTone> tones, CommunicationIdentifier targetParticipant) {
        callMediaAsync.sendDtmf(tones, targetParticipant).block();
    }

    /**
     * Sends Dtmf tones
     *
     * @param tones tones to be sent
     * @param targetParticipant the target participant
     * @param operationContext operationContext (pass null if not applicable)
     * @param callbackUrl the call back URI override to set (pass null if not applicable)
     * @param context Context
     * @return Response for successful sendDtmf request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> sendDtmfWithResponse(List<DtmfTone> tones, CommunicationIdentifier targetParticipant,
                                               String operationContext, String callbackUrl, Context context) {
        return callMediaAsync.sendDtmfWithResponseInternal(targetParticipant, tones, operationContext, callbackUrl, context).block();
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
     * @param targetParticipant the target participant
     * @param operationContext operationContext (pass null if not applicable)
     * @param context Context
     * @return Response for successful start continuous dtmf recognition request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> startContinuousDtmfRecognitionWithResponse(CommunicationIdentifier targetParticipant, String operationContext, Context context) {
        return callMediaAsync.startContinuousDtmfRecognitionWithResponseInternal(targetParticipant, operationContext, context).block();
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
     * @param targetParticipant the target participant
     * @param operationContext operationContext (pass null if not applicable)
     * @param context Context
     * @param callbackUrl the call back URI override to set (pass null if not applicable)
     * @return Response for successful stop continuous dtmf recognition request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> stopContinuousDtmfRecognitionWithResponse(CommunicationIdentifier targetParticipant, String operationContext, String callbackUrl, Context context) {
        return callMediaAsync.stopContinuousDtmfRecognitionWithResponseInternal(targetParticipant, operationContext, callbackUrl, context).block();
    }

    /**
     * Holds participant in call.
     * @param targetParticipant the target.
     * @param playSourceInfo audio to play.
     * @return Response for successful operation.
     */
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
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> startHoldMusicWithResponse(StartHoldMusicOptions options,
                                                     Context context) {
        return callMediaAsync.startHoldMusicWithResponseInternal(options, context).block();
    }

    /**
     * Removes hold from participant in call.
     * @param targetParticipant the target.
     * @return Response for successful operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void stopHoldMusic(CommunicationIdentifier targetParticipant) {
        return callMediaAsync.stopHoldMusicAsync(targetParticipant).block();
    }

    /**
     * Removes hold from participant in call.
     * @param targetParticipant the target.
     * @param operationContext operational context.
     * @param context Context.
     * @return Response for successful operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> stopHoldMusicWithResponse(CommunicationIdentifier targetParticipant,
                                                     String operationContext,
                                                     Context context) {
        return callMediaAsync.stopHoldMusicWithResponseInternal(targetParticipant, operationContext, context).block();
    }
}
