// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.models.PlayResponse;
import com.azure.communication.callingserver.models.PlaySourceType;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import java.util.Collections;
import java.util.List;

/**
 * ContentCapabilities.
 */
public class ContentCapabilities {
    private final ContentCapabilitiesAsync contentCapabilitiesAsync;

    ContentCapabilities(ContentCapabilitiesAsync contentCapabilitiesAsync) {
        this.contentCapabilitiesAsync = contentCapabilitiesAsync;
    }

    /**
     * Play
     *
     * @param playSourceType type of the play source
     * @param playTo the targets to be played
     * @param playSourceId the identifier to be used for caching related media, Optional.
     * @return PlayResponse
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PlayResponse play(PlaySourceType playSourceType, List<CommunicationIdentifier> playTo, String playSourceId) {
        return contentCapabilitiesAsync.play(playSourceType, playTo, playSourceId).block();
    }

    /**
     * Play to all participants
     *
     * @param playSourceType type of the play source
     * @param playSourceId the identifier to be used for caching related media
     * @return PlayResponse
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public PlayResponse playAll(PlaySourceType playSourceType, String playSourceId) {
        return contentCapabilitiesAsync.playAll(playSourceType, playSourceId).block();
    }

    /**
     * PlayWithResponse
     *
     * @param playSourceType type of the play source
     * @param playTo the targets to be played
     * @param playSourceId the identifier to be used for caching related media, Optional.
     * @param context Place_holder
     *
     * @return PlayResponse
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PlayResponse> playWithResponse(PlaySourceType playSourceType, List<CommunicationIdentifier> playTo,
                                                   String playSourceId, Context context) {
        return contentCapabilitiesAsync.playWithResponseInternal(playSourceType, playTo, playSourceId, context).block();
    }

    /**
     * PlayAllWithResponse
     *
     * @param playSourceType type of the play source
     * @param playSourceId the identifier to be used for caching related media, Optional.
     * @param context Place_holder
     * @return PlayResponse
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Response<PlayResponse> playAllWithResponse(PlaySourceType playSourceType, String playSourceId, Context context) {
        return contentCapabilitiesAsync
            .playWithResponseInternal(playSourceType, Collections.emptyList(), playSourceId, context)
            .block();
    }
}
