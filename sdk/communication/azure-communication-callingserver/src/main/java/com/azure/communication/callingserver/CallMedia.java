// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.models.CallingServerErrorException;
import com.azure.communication.callingserver.models.PlaySource;
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
    public void playAll(PlaySource playSource) {
        callMediaAsync.playAll(playSource).block();
    }

    /**
     * PlayWithResponse
     *
     * @param playSource A {@link PlaySource} representing the source to play.
     * @param playTo the targets to play to.
     * @param context A {@link Context} representing the request context.
     * @return Response for successful play request.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> playWithResponse(PlaySource playSource, List<CommunicationIdentifier> playTo,
                                           Context context) {
        return callMediaAsync.playWithResponseInternal(playSource, playTo, context).block();
    }

    /**
     * PlayAllWithResponse
     *
     * @param playSource A {@link PlaySource} representing the source to play.
     * @param context A {@link Context} representing the request context.
     * @return Response for successful playAll request.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> playAllWithResponse(PlaySource playSource, Context context) {
        return callMediaAsync
            .playWithResponseInternal(playSource, Collections.emptyList(), context)
            .block();
    }
}
