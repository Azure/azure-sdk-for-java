// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.CallingServerErrorException;
import com.azure.communication.callautomation.models.PlayOptions;
import com.azure.communication.callautomation.models.PlaySource;
import com.azure.communication.callautomation.models.CallMediaRecognizeOptions;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import java.util.Collections;
import java.util.List;

/**
 * CallContent.
 */
public class CallMedia {
    private final CallMediaAsync callMediaAsync;

    CallMedia(CallMediaAsync callMediaAsync) {
        this.callMediaAsync = callMediaAsync;
    }

    /**
     * Play
     *
     * @param playSource A {@link PlaySource} representing the source to play.
     * @param playTo the targets to play to.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void play(PlaySource playSource, List<CommunicationIdentifier> playTo) {
        callMediaAsync.play(playSource, playTo).block();
    }

    /**
     * Play to all participants
     *
     * @param playSource A {@link PlaySource} representing the source to play.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public void playToAll(PlaySource playSource) {
        callMediaAsync.playToAll(playSource).block();
    }

    /**
     * PlayWithResponse
     *
     * @param playSource A {@link PlaySource} representing the source to play.
     * @param playTo the targets to play to.
     * @param options play options.
     * @param context A {@link Context} representing the request context.
     * @return Response for successful play request.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> playWithResponse(PlaySource playSource, List<CommunicationIdentifier> playTo,
                                           PlayOptions options, Context context) {
        return callMediaAsync.playWithResponseInternal(playSource, playTo, options, context).block();
    }

    /**
     * PlayAllWithResponse
     *
     * @param playSource A {@link PlaySource} representing the source to play.
     * @param options play options.
     * @param context A {@link Context} representing the request context.
     * @return Response for successful playAll request.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> playToAllWithResponse(PlaySource playSource, PlayOptions options, Context context) {
        return callMediaAsync
            .playWithResponseInternal(playSource, Collections.emptyList(), options, context)
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

}
