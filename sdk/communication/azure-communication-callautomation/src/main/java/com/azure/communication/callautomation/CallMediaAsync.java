// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.implementation.CallMediasImpl;
import com.azure.communication.callautomation.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callautomation.implementation.models.DtmfOptionsInternal;
import com.azure.communication.callautomation.implementation.models.DtmfToneInternal;
import com.azure.communication.callautomation.implementation.models.FileSourceInternal;
import com.azure.communication.callautomation.implementation.models.PlayOptionsInternal;
import com.azure.communication.callautomation.implementation.models.PlayRequest;
import com.azure.communication.callautomation.implementation.models.PlaySourceInternal;
import com.azure.communication.callautomation.implementation.models.PlaySourceTypeInternal;
import com.azure.communication.callautomation.implementation.models.RecognizeInputTypeInternal;
import com.azure.communication.callautomation.implementation.models.RecognizeOptionsInternal;
import com.azure.communication.callautomation.implementation.models.RecognizeRequest;
import com.azure.communication.callautomation.models.CallMediaRecognizeDtmfOptions;
import com.azure.communication.callautomation.models.CallMediaRecognizeOptions;
import com.azure.communication.callautomation.models.DtmfTone;
import com.azure.communication.callautomation.models.FileSource;
import com.azure.communication.callautomation.models.PlayOptions;
import com.azure.communication.callautomation.models.PlaySource;
import com.azure.communication.callautomation.models.PlayToAllOptions;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import static com.azure.core.util.FluxUtil.monoError;
import static com.azure.core.util.FluxUtil.withContext;

/**
 * CallContent.
 */
public final class CallMediaAsync {
    private final CallMediasImpl contentsInternal;
    private final String callConnectionId;
    private final ClientLogger logger;

    CallMediaAsync(String callConnectionId, CallMediasImpl contentsInternal) {
        this.callConnectionId = callConnectionId;
        this.contentsInternal = contentsInternal;
        this.logger = new ClientLogger(CallMediaAsync.class);
    }

    /**
     * Play
     *
     * @param playSources A List of {@link PlaySource} representing the sources to play.
     * @param playTo the targets to play to
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Void for successful play request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> play(List<PlaySource> playSources, List<CommunicationIdentifier> playTo) {
        PlayOptions options = new PlayOptions(playSources, playTo);
        return playWithResponse(options).flatMap(FluxUtil::toMono);
    }

    /**
     * Play
     *
     * @param playSource A {@link PlaySource} representing the source to play.
     * @param playTo the targets to play to
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Void for successful play request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> play(PlaySource playSource, List<CommunicationIdentifier> playTo) {
        PlayOptions options = new PlayOptions(playSource, playTo);
        return playWithResponse(options).flatMap(FluxUtil::toMono);
    }

    /**
     * Play to all participants
     *
     * @param playSources A List of {@link PlaySource} representing the sources to play.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Void for successful playAll request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> playToAll(List<PlaySource> playSources) {
        PlayToAllOptions options = new PlayToAllOptions(playSources);
        return playToAllWithResponse(options).flatMap(FluxUtil::toMono);
    }

    /**
     * Play to all participants
     *
     * @param playSource A {@link PlaySource} representing the source to play.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     * @return Void for successful playAll request.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> playToAll(PlaySource playSource) {
        PlayToAllOptions options = new PlayToAllOptions(playSource);
        return playToAllWithResponse(options).flatMap(FluxUtil::toMono);
    }

    /**
     * Play
     *
     * @param options play options.
     * @return Response for successful play request.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> playWithResponse(PlayOptions options) {
        return playWithResponseInternal(options, null);
    }

    /**
     * Play to all participants
     *
     * @param options play to all options.
     * @return Response for successful playAll request.
     * @throws HttpResponseException thrown if the request is rejected by server.
     * @throws RuntimeException all other wrapped checked exceptions if the request fails to be sent.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> playToAllWithResponse(PlayToAllOptions options) {
        return playToAllWithResponseInternal(options, null);
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
                RecognizeRequest recognizeRequest = getRecognizeRequestFromDtmfConfiguration(recognizeOptions);
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
                return contentsInternal.cancelAllMediaOperationsWithResponseAsync(callConnectionId, contextValue);
            });

        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> playWithResponseInternal(PlayOptions options, Context context) {
        try {
            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                PlayRequest request = getPlayRequest(options);
                return contentsInternal.playWithResponseAsync(callConnectionId, request, contextValue);
            });

        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    Mono<Response<Void>> playToAllWithResponseInternal(PlayToAllOptions options, Context context) {
        try {
            PlayOptions playOptions = new PlayOptions(options.getPlaySources(), Collections.emptyList());
            playOptions.setLoop(options.isLoop());
            playOptions.setOperationContext(options.getOperationContext());

            return playWithResponseInternal(playOptions, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    PlayRequest getPlayRequest(PlayOptions options) {
        List<PlaySourceInternal> playSourcesInternal = new ArrayList<>();
        for (PlaySource source: options.getPlaySources()) {
            PlaySourceInternal playSourceInternal = null;
            if (source instanceof FileSource) {
                playSourceInternal = getPlaySourceInternalFromFileSource((FileSource) source);
            }
            if (playSourceInternal != null && playSourceInternal.getKind() != null) {
                playSourcesInternal.add(playSourceInternal);
            } else {
                throw logger.logExceptionAsError(new IllegalArgumentException(source.getClass().getCanonicalName()));
            }
        }

        if (!playSourcesInternal.isEmpty()) {
            PlayRequest request = new PlayRequest()
                .setPlaySources(playSourcesInternal)
                .setPlayTo(
                    options.getPlayTo()
                        .stream()
                        .map(CommunicationIdentifierConverter::convert)
                        .collect(Collectors.toList()));

            request.setPlayOptions(new PlayOptionsInternal().setLoop(options.isLoop()));
            request.setOperationContext(options.getOperationContext());

            return request;
        }

        throw logger.logExceptionAsError(new IllegalArgumentException(options.getPlaySources().getClass().getCanonicalName()));
    }

    private PlaySourceInternal getPlaySourceInternalFromFileSource(FileSource playSource) {
        FileSourceInternal fileSourceInternal = new FileSourceInternal().setUri(playSource.getUrl());
        PlaySourceInternal playSourceInternal = new PlaySourceInternal()
            .setKind(PlaySourceTypeInternal.FILE)
            .setFile(fileSourceInternal)
            .setPlaySourceCacheId(playSource.getPlaySourceCacheId());
        return playSourceInternal;
    }

    private PlaySourceInternal convertPlaySourceToPlaySourceInternal(PlaySource playSource) {
        PlaySourceInternal playSourceInternal = new PlaySourceInternal();
        if (playSource instanceof FileSource) {
            playSourceInternal = getPlaySourceInternalFromFileSource((FileSource) playSource);
        }
        return playSourceInternal;
    }

    private DtmfToneInternal convertDtmfToneInternal(DtmfTone dtmfTone) {
        return DtmfToneInternal.fromString(dtmfTone.toString());
    }

    private RecognizeRequest getRecognizeRequestFromDtmfConfiguration(CallMediaRecognizeOptions recognizeOptions) {
        CallMediaRecognizeDtmfOptions dtmfRecognizeOptions = (CallMediaRecognizeDtmfOptions) recognizeOptions;
        DtmfOptionsInternal dtmfOptionsInternal = new DtmfOptionsInternal();
        dtmfOptionsInternal.setInterToneTimeoutInSeconds((int) dtmfRecognizeOptions.getInterToneTimeout().getSeconds());

        if (dtmfRecognizeOptions.getMaxTonesToCollect() != null) {
            dtmfOptionsInternal.setMaxTonesToCollect(dtmfRecognizeOptions.getMaxTonesToCollect());
        }

        if (dtmfRecognizeOptions.getStopTones() != null) {
            List<DtmfToneInternal> dtmfTones = dtmfRecognizeOptions.getStopTones().stream()
                                        .map(this::convertDtmfToneInternal)
                                        .collect(Collectors.toList());
            dtmfOptionsInternal.setStopTones(dtmfTones);
        }

        RecognizeOptionsInternal recognizeOptionsInternal = new RecognizeOptionsInternal()
            .setDtmfOptions(dtmfOptionsInternal)
            .setInterruptPrompt(recognizeOptions.isInterruptPrompt())
            .setTargetParticipant(CommunicationIdentifierConverter.convert(recognizeOptions.getTargetParticipant()));

        recognizeOptionsInternal.setInitialSilenceTimeoutInSeconds((int) recognizeOptions.getInitialSilenceTimeout().getSeconds());

        PlaySourceInternal playSourceInternal = getPlaySourceInternalFromRecognizeOptions(recognizeOptions);

        RecognizeRequest recognizeRequest = new RecognizeRequest()
            .setRecognizeInputType(RecognizeInputTypeInternal.fromString(recognizeOptions.getRecognizeInputType().toString()))
            .setInterruptCallMediaOperation(recognizeOptions.isInterruptCallMediaOperation())
            .setPlayPrompt(playSourceInternal)
            .setRecognizeOptions(recognizeOptionsInternal)
            .setOperationContext(recognizeOptions.getOperationContext());

        return recognizeRequest;
    }

    private PlaySourceInternal getPlaySourceInternalFromRecognizeOptions(CallMediaRecognizeOptions recognizeOptions) {
        PlaySourceInternal playSourceInternal = null;
        if (recognizeOptions.getPlayPrompt() != null) {
            PlaySource playSource = recognizeOptions.getPlayPrompt();
            playSourceInternal = convertPlaySourceToPlaySourceInternal(playSource);
        }
        return playSourceInternal;
    }
}
