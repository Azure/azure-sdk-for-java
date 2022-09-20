// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.implementation.ContentsImpl;
import com.azure.communication.callautomation.implementation.accesshelpers.ErrorConstructorProxy;
import com.azure.communication.callautomation.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callautomation.implementation.models.DtmfOptionsInternal;
import com.azure.communication.callautomation.implementation.models.FileSourceInternal;
import com.azure.communication.callautomation.implementation.models.PlayOptionsInternal;
import com.azure.communication.callautomation.implementation.models.PlayRequest;
import com.azure.communication.callautomation.implementation.models.PlaySourceInternal;
import com.azure.communication.callautomation.implementation.models.PlaySourceTypeInternal;
import com.azure.communication.callautomation.implementation.models.RecognizeInputTypeInternal;
import com.azure.communication.callautomation.implementation.models.RecognizeOptionsInternal;
import com.azure.communication.callautomation.implementation.models.RecognizeRequest;
import com.azure.communication.callautomation.models.CallMediaRecognizeDtmfOptions;
import com.azure.communication.callautomation.models.CallingServerErrorException;
import com.azure.communication.callautomation.models.FileSource;
import com.azure.communication.callautomation.models.PlayOptions;
import com.azure.communication.callautomation.models.PlaySource;
import com.azure.communication.callautomation.models.CallMediaRecognizeOptions;
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
        return playWithResponse(playSource, playTo, null).flatMap(FluxUtil::toMono);
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
    public Mono<Void> playToAll(PlaySource playSource) {
        return playToAllWithResponse(playSource, null).flatMap(FluxUtil::toMono);
    }

    /**
     * Play
     *
     * @param playSource A {@link PlaySource} representing the source to play.
     * @param playTo the targets to play to
     * @param options play options.
     * @return Response for successful play request.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> playWithResponse(PlaySource playSource, List<CommunicationIdentifier> playTo,
                                                 PlayOptions options) {
        return playWithResponseInternal(playSource, playTo, options, null);
    }

    /**
     * Play to all participants
     *
     * @param playSource A {@link PlaySource} representing the source to play.
     * @param options play options.
     * @return Response for successful playAll request.
     * @throws CallingServerErrorException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> playToAllWithResponse(PlaySource playSource, PlayOptions options) {
        return playWithResponseInternal(playSource, Collections.emptyList(), options, null);
    }

    /**
     * Recognize operation.
     * @param recognizeOptions Different attributes for recognize.
     * @return Response for successful recognize request.
     */
    public Mono<Void> startRecognizing(CallMediaRecognizeOptions recognizeOptions) {
        return startRecognizingWithResponse(recognizeOptions).then();
    }

    /**
     * Recognize operation
     * @param recognizeOptions Different attributes for recognize.
     * @return Response for successful recognize request.
     */
    public Mono<Response<Void>> startRecognizingWithResponse(CallMediaRecognizeOptions recognizeOptions) {
        return withContext(context -> recognizeWithResponseInternal(recognizeOptions, context));
    }

    Mono<Response<Void>> recognizeWithResponseInternal(CallMediaRecognizeOptions recognizeOptions, Context context) {
        try {
            context = context == null ? Context.NONE : context;

            if (recognizeOptions instanceof CallMediaRecognizeDtmfOptions) {

                CallMediaRecognizeDtmfOptions dtmfRecognizeOptions = (CallMediaRecognizeDtmfOptions) recognizeOptions;
                DtmfOptionsInternal dtmfOptionsInternal = new DtmfOptionsInternal();


                dtmfOptionsInternal.setInterToneTimeoutInSeconds((int) dtmfRecognizeOptions.getInterToneTimeout().getSeconds());


                if (dtmfRecognizeOptions.getMaxTonesToCollect() != null) {
                    dtmfOptionsInternal.setMaxTonesToCollect(dtmfRecognizeOptions.getMaxTonesToCollect());
                }

                RecognizeOptionsInternal recognizeOptionsInternal = new RecognizeOptionsInternal()
                    .setDtmfOptions(dtmfOptionsInternal)
                    .setInterruptPrompt(recognizeOptions.isInterruptPrompt())
                    .setTargetParticipant(CommunicationIdentifierConverter.convert(recognizeOptions.getTargetParticipant()));

                recognizeOptionsInternal.setInitialSilenceTimeoutInSeconds((int) recognizeOptions.getInitialSilenceTimeout().getSeconds());

                PlaySourceInternal playSourceInternal = null;
                if (recognizeOptions.getPlayPrompt() != null) {
                    PlaySource playSource = recognizeOptions.getPlayPrompt();
                    if (playSource instanceof FileSource) {
                        playSourceInternal = getPlaySourceInternal((FileSource) playSource);
                    }
                }

                RecognizeRequest recognizeRequest = new RecognizeRequest()
                    .setRecognizeInputType(RecognizeInputTypeInternal.fromString(recognizeOptions.getRecognizeInputType().toString()))
                    .setInterruptCallMediaOperation(recognizeOptions.isInterruptCallMediaOperation())
                    .setPlayPrompt(playSourceInternal)
                    .setRecognizeOptions(recognizeOptionsInternal)
                    .setOperationContext(recognizeOptions.getOperationContext());

                return contentsInternal.recognizeWithResponseAsync(callConnectionId, recognizeRequest, context);

            } else {
                return monoError(logger, new UnsupportedOperationException(recognizeOptions.getClass().getName()));
            }

        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Cancels all the queued media operations.
     * @return Void
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> cancelAllMediaOperations() {
        return cancelAllMediaOperationsWithResponse().then();
    }

    /**
     * Cancels all the queued media operations
     * @return Response for successful playAll request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> cancelAllMediaOperationsWithResponse() {
        return cancelAllMediaOperationsWithResponseInternal(null);
    }

    Mono<Response<Void>> cancelAllMediaOperationsWithResponseInternal(Context context) {
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                return contentsInternal.cancelAllMediaOperationsWithResponseAsync(callConnectionId, contextValue)
                    .onErrorMap(HttpResponseException.class, ErrorConstructorProxy::create);
            });

        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> playWithResponseInternal(PlaySource playSource, List<CommunicationIdentifier> playTo,
                                                  PlayOptions options, Context context) {
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                PlayRequest request = getPlayRequest(playSource, playTo, options);
                return contentsInternal.playWithResponseAsync(callConnectionId, request, contextValue)
                    .onErrorMap(HttpResponseException.class, ErrorConstructorProxy::create);
            });

        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    PlayRequest getPlayRequest(PlaySource playSource, List<CommunicationIdentifier> playTo, PlayOptions options) {
        if (playSource instanceof FileSource) {
            PlaySourceInternal playSourceInternal = getPlaySourceInternal((FileSource) playSource);

            PlayRequest request = new PlayRequest()
                .setPlaySourceInfo(playSourceInternal)
                .setPlayTo(
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

    private PlaySourceInternal getPlaySourceInternal(FileSource fileSource) {
        FileSourceInternal fileSourceInternal = new FileSourceInternal().setUri(fileSource.getUri());
        PlaySourceInternal playSourceInternal = new PlaySourceInternal()
            .setSourceType(PlaySourceTypeInternal.FILE)
            .setFileSource(fileSourceInternal)
            .setPlaySourceId(fileSource.getPlaySourceId());
        return playSourceInternal;
    }
}
