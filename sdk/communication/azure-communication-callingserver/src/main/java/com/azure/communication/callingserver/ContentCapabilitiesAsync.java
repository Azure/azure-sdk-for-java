// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.ContentsImpl;
import com.azure.communication.callingserver.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callingserver.implementation.models.PlayRequest;
import com.azure.communication.callingserver.implementation.models.PlayResponse;
import com.azure.communication.callingserver.implementation.models.PlaySource;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.withContext;

public class ContentCapabilitiesAsync {
    private final ContentsImpl contentsInternal;
    private final String callConnectionId;

    ContentCapabilitiesAsync(String callConnectionId, ContentsImpl contentsInternal) {
        this.callConnectionId = callConnectionId;
        this.contentsInternal = contentsInternal;
    }

    public Mono<PlayResponse> PlayAsync(PlaySource playSource, List<CommunicationIdentifier> playTo) {
        return PlayAsyncWithResponse(playSource, playTo).flatMap(FluxUtil::toMono);
    }

    public Mono<PlayResponse> PlayAllAsync(PlaySource playSource) {
        return PlayAllAsyncWithResponse(playSource).flatMap(FluxUtil::toMono);
    }

    public Mono<Response<PlayResponse>> PlayAsyncWithResponse(
        PlaySource playSource,
        List<CommunicationIdentifier> playTo) {
        return withContext(context -> playAsyncWithResponseInternal(playSource, playTo, context));
    }

    public Mono<Response<PlayResponse>> PlayAllAsyncWithResponse(
        PlaySource playSource) {
        return withContext(context -> playAsyncWithResponseInternal(playSource, Collections.emptyList(), context));
    }

    Mono<Response<PlayResponse>> playAsyncWithResponseInternal(
        PlaySource playSource,
        List<CommunicationIdentifier> playTo,
        Context context) {
        PlayRequest request = new PlayRequest();

        context = context == null ? Context.NONE : context;

        request.setPlaySourceInfo(playSource);
        request.setPlayTo(
            playTo
                .stream()
                .map(CommunicationIdentifierConverter::convert)
                .collect(Collectors.toList()));

        return contentsInternal.playWithResponseAsync(
            callConnectionId,
            request,
            context
        );
    }
}
