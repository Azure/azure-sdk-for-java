// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.ContentsImpl;
import com.azure.communication.callingserver.implementation.accesshelpers.ErrorConstructorProxy;
import com.azure.communication.callingserver.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callingserver.implementation.models.FileSourceInternal;
import com.azure.communication.callingserver.implementation.models.PlayRequest;
import com.azure.communication.callingserver.implementation.models.PlaySourceInternal;
import com.azure.communication.callingserver.implementation.models.PlaySourceTypeInternal;
import com.azure.communication.callingserver.models.CallingServerErrorException;
import com.azure.communication.callingserver.models.PlaySource;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * CallContent.
 */
public class CallContentAsync {
    private final ContentsImpl contentsInternal;
    private final String callConnectionId;
    private final ClientLogger logger;

    CallContentAsync(String callConnectionId, ContentsImpl contentsInternal) {
        this.callConnectionId = callConnectionId;
        this.contentsInternal = contentsInternal;
        this.logger = new ClientLogger(CallContentAsync.class);
    }

    /**
     * Play
     *
     * @param playSource type of the play source
     * @param playTo the targets to be played
     * @param playSourceId the identifier to be used for caching related media, Optional.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Placeholder
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> play(PlaySource playSource, List<CommunicationIdentifier> playTo, String playSourceId) {
        return playWithResponse(playSource, playTo, playSourceId).flatMap(FluxUtil::toMono);
    }

    /**
     * Play to all participants
     *
     * @param playSource type of the play source
     * @param playSourceId the identifier to be used for caching related media, Optional.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Placeholder
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> playAll(PlaySource playSource, String playSourceId) {
        return playAllWithResponse(playSource, playSourceId).flatMap(FluxUtil::toMono);
    }

    /**
     * Play
     *
     * @param playSource type of the play source
     * @param playTo the targets to be played
     * @param playSourceId the identifier to be used for caching related media, Optional.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Placeholder
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> playWithResponse(PlaySource playSource, List<CommunicationIdentifier> playTo,
                                                         String playSourceId) {
        return withContext(context -> playWithResponseInternal(playSource, playTo, playSourceId, context));
    }

    /**
     * Play to all participants
     *
     * @param playSource type of the play source
     * @param playSourceId the identifier to be used for caching related media, Optional.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Placeholder
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> playAllWithResponse(PlaySource playSource, String playSourceId) {
        return withContext(context ->
            playWithResponseInternal(playSource, Collections.emptyList(), playSourceId, context)
        );
    }

    Mono<Response<Void>> playWithResponseInternal(PlaySource playSource, List<CommunicationIdentifier> playTo,
                                                          String playSourceId, Context context) {
        try {
            PlayRequest request = new PlayRequest();
            context = context == null ? Context.NONE : context;
            FileSourceInternal fileSourceInternal = new FileSourceInternal()
                .setUri(playSource.getFileSource().getUri());
            PlaySourceInternal playSourceInternal = new PlaySourceInternal()
                .setSourceType(PlaySourceTypeInternal.fromString(playSource.getSourceType().toString()))
                .setFileSource(fileSourceInternal)
                .setPlaySourceId(playSourceId);

            request.setPlaySourceInfo(playSourceInternal);
            request.setPlayTo(
                playTo
                    .stream()
                    .map(CommunicationIdentifierConverter::convert)
                    .collect(Collectors.toList()));

            return contentsInternal.playWithResponseAsync(callConnectionId, request, context)
                .onErrorMap(HttpResponseException.class, ErrorConstructorProxy::create);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }
}
