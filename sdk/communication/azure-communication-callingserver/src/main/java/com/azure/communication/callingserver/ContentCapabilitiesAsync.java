// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.ContentsImpl;
import com.azure.communication.callingserver.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callingserver.implementation.models.PlayRequest;
import com.azure.communication.callingserver.implementation.models.PlaySourceInternal;
import com.azure.communication.callingserver.implementation.models.PlaySourceTypeInternal;
import com.azure.communication.callingserver.models.PlayResponse;
import com.azure.communication.callingserver.models.PlaySourceType;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.withContext;

/**
 * ContentCapabilities.
 */
public class ContentCapabilitiesAsync {
    private final ContentsImpl contentsInternal;
    private final String callConnectionId;

    ContentCapabilitiesAsync(String callConnectionId, ContentsImpl contentsInternal) {
        this.callConnectionId = callConnectionId;
        this.contentsInternal = contentsInternal;
    }

    /**
     * Play
     *
     * @param playSourceType type of the play source
     * @param playTo the targets to be played
     * @param playSourceId the identifier to be used for caching related media, Optional.
     * @return Placeholder
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PlayResponse> play(PlaySourceType playSourceType, List<CommunicationIdentifier> playTo, String playSourceId) {
        return playWithResponse(playSourceType, playTo, playSourceId).flatMap(FluxUtil::toMono);
    }

    /**
     * Play to all participants
     *
     * @param playSourceType type of the play source
     * @param playSourceId the identifier to be used for caching related media, Optional.
     *
     * @return Placeholder
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<PlayResponse> playAll(PlaySourceType playSourceType, String playSourceId) {
        return playAllWithResponse(playSourceType, playSourceId).flatMap(FluxUtil::toMono);
    }

    /**
     * Play
     *
     * @param playSourceType type of the play source
     * @param playTo the targets to be played
     * @param playSourceId the identifier to be used for caching related media, Optional.
     * @return Placeholder
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PlayResponse>> playWithResponse(PlaySourceType playSourceType, List<CommunicationIdentifier> playTo,
                                                         String playSourceId) {
        return withContext(context -> playWithResponseInternal(playSourceType, playTo, playSourceId, context));
    }

    /**
     * Play to all participants
     *
     * @param playSourceType type of the play source
     * @param playSourceId the identifier to be used for caching related media, Optional.
     * @return Placeholder
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<PlayResponse>> playAllWithResponse(PlaySourceType playSourceType, String playSourceId) {
        return withContext(context -> playWithResponseInternal(playSourceType, Collections.emptyList(), playSourceId, context));
    }

    Mono<Response<PlayResponse>> playWithResponseInternal(PlaySourceType playSourceType, List<CommunicationIdentifier> playTo,
                                                          String playSourceId, Context context) {
        PlayRequest request = new PlayRequest();
        context = context == null ? Context.NONE : context;
        PlaySourceInternal playSource = new PlaySourceInternal()
            .setSourceType(PlaySourceTypeInternal.fromString(playSourceType.toString()))
            .setPlaySourceId(playSourceId);

        request.setPlaySourceInfo(playSource);
        request.setPlayTo(
            playTo
                .stream()
                .map(CommunicationIdentifierConverter::convert)
                .collect(Collectors.toList()));

        return contentsInternal.playWithResponseAsync(callConnectionId, request, context)
            .map(response -> new SimpleResponse<>(response, new PlayResponse(response.getValue())));
    }
}
