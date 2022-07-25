// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.implementation.ContentsImpl;
import com.azure.communication.callingserver.implementation.accesshelpers.ErrorConstructorProxy;
import com.azure.communication.callingserver.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callingserver.implementation.models.FileSourceInternal;
import com.azure.communication.callingserver.implementation.models.PlayOptionsInternal;
import com.azure.communication.callingserver.implementation.models.PlayRequest;
import com.azure.communication.callingserver.implementation.models.PlaySourceInternal;
import com.azure.communication.callingserver.implementation.models.PlaySourceTypeInternal;
import com.azure.communication.callingserver.models.CallingServerErrorException;
import com.azure.communication.callingserver.models.FileSource;
import com.azure.communication.callingserver.models.PlayOptions;
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
public class CallMediaAsync {
    private final ContentsImpl contentsInternal;
    private final String callConnectionId;
    private final ClientLogger logger;

    CallMediaAsync(String callConnectionId, ContentsImpl contentsInternal) {
        this.callConnectionId = callConnectionId;
        this.contentsInternal = contentsInternal;
        this.logger = new ClientLogger(CallMediaAsync.class);
    }

    /**
     * Play
     *
     * @param playSource A {@link PlaySource} representing the source to play.
     * @param playTo the targets to play to
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Void for successful play request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> play(PlaySource playSource, List<CommunicationIdentifier> playTo) {
        return playWithResponse(playSource, playTo, null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Play to all participants
     *
     * @param playSource A {@link PlaySource} representing the source to play.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Void for successful playAll request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> playAll(PlaySource playSource) {
        return playAllWithResponse(playSource, null, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Play
     *
     * @param playSource A {@link PlaySource} representing the source to play.
     * @param playTo the targets to play to
     * @param options play options.
     * @param context A {@link Context} representing the request context.
     * @return Response for successful play request.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> playWithResponse(PlaySource playSource, List<CommunicationIdentifier> playTo,
                                                 PlayOptions options, Context context) {
        return withContext(contextValue -> {
            contextValue = context == null ? contextValue : context;
            return playWithResponseInternal(playSource, playTo, options, contextValue);
        });
    }

    /**
     * Play to all participants
     *
     * @param playSource A {@link PlaySource} representing the source to play.
     * @param options play options.
     * @param context A {@link Context} representing the request context.
     * @return Response for successful playAll request.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> playAllWithResponse(PlaySource playSource, PlayOptions options, Context context) {
        return withContext(contextValue -> {
            contextValue = context == null ? contextValue : context;
            return playWithResponseInternal(playSource, Collections.emptyList(), options, contextValue);
        });
    }

    /**
     * Cancels all the queued media operations.
     * @return Void
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> cancelAllMediaOperations() {
        return cancelAllMediaOperationsWithResponse(null).then();
    }

    /**
     * Cancels all the queued media operations
     * @param context A {@link Context} representing the request context.
     * @return Response for successful playAll request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> cancelAllMediaOperationsWithResponse(Context context) {
        try {
            context = context == null ? Context.NONE : context;

            return contentsInternal.cancelAllMediaOperationsWithResponseAsync(callConnectionId, context)
                .onErrorMap(HttpResponseException.class, ErrorConstructorProxy::create);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> playWithResponseInternal(PlaySource playSource, List<CommunicationIdentifier> playTo,
                                                  PlayOptions options, Context context) {
        try {
            context = context == null ? Context.NONE : context;
            PlayRequest request = getPlayRequest(playSource, playTo, options);

            return contentsInternal.playWithResponseAsync(callConnectionId, request, context)
                .onErrorMap(HttpResponseException.class, ErrorConstructorProxy::create);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    PlayRequest getPlayRequest(PlaySource playSource, List<CommunicationIdentifier> playTo, PlayOptions options) {
        if (playSource instanceof FileSource) {
            FileSource fileSource = (FileSource) playSource;
            PlayRequest request = new PlayRequest();
            FileSourceInternal fileSourceInternal = new FileSourceInternal().setUri(fileSource.getUri());
            PlaySourceInternal playSourceInternal = new PlaySourceInternal()
                .setSourceType(PlaySourceTypeInternal.FILE)
                .setFileSource(fileSourceInternal)
                .setPlaySourceId(fileSource.getPlaySourceId());

            request.setPlaySourceInfo(playSourceInternal);
            request.setPlayTo(
                playTo
                    .stream()
                    .map(CommunicationIdentifierConverter::convert)
                    .collect(Collectors.toList()));

            if (options != null) {
                request.setPlayOptions(new PlayOptionsInternal().setLoop(options.isLoop()));
                request.setOperationContext(options.getOperationContext());
            }

            return request;
        }

        throw logger.logExceptionAsError(new IllegalArgumentException(playSource.getClass().getCanonicalName()));
    }
}
