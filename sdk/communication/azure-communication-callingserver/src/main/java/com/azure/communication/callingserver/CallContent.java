// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.models.CallingServerErrorException;
import com.azure.communication.callingserver.models.FileSource;
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
public class CallContent {
    private final CallContentAsync callContentAsync;

    CallContent(CallContentAsync callContentAsync) {
        this.callContentAsync = callContentAsync;
    }

    /**
     * Play
     *
     * @param fileSource type of the play source
     * @param playTo the targets to be played
     * @param playSourceId the identifier to be used for caching related media, Optional.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return PlayResponse
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void play(FileSource fileSource, List<CommunicationIdentifier> playTo, String playSourceId) {
        return callContentAsync.play(fileSource, playTo, playSourceId).block();
    }

    /**
     * Play to all participants
     *
     * @param fileSource type of the play source
     * @param playSourceId the identifier to be used for caching related media
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return PlayResponse
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Void playAll(FileSource fileSource, String playSourceId) {
        return callContentAsync.playAll(fileSource, playSourceId).block();
    }

    /**
     * PlayWithResponse
     *
     * @param fileSource type of the play source
     * @param playTo the targets to be played
     * @param playSourceId the identifier to be used for caching related media, Optional.
     * @param context Place_holder
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return PlayResponse
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> playWithResponse(FileSource fileSource, List<CommunicationIdentifier> playTo,
                                                   String playSourceId, Context context) {
        return callContentAsync.playWithResponseInternal(fileSource, playTo, playSourceId, context).block();
    }

    /**
     * PlayAllWithResponse
     *
     * @param fileSource type of the play source
     * @param playSourceId the identifier to be used for caching related media, Optional.
     * @param context Place_holder
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return PlayResponse
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<Void> playAllWithResponse(FileSource fileSource, String playSourceId, Context context) {
        return callContentAsync
            .playWithResponseInternal(fileSource, Collections.emptyList(), playSourceId, context)
            .block();
    }
}
