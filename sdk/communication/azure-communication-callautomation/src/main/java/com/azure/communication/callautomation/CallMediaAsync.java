// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.implementation.CallMediasImpl;
import com.azure.communication.callautomation.implementation.accesshelpers.SendDtmfTonesResponseConstructorProxy;
import com.azure.communication.callautomation.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callautomation.implementation.models.ContinuousDtmfRecognitionRequestInternal;
import com.azure.communication.callautomation.implementation.models.DtmfOptionsInternal;
import com.azure.communication.callautomation.implementation.models.DtmfToneInternal;
import com.azure.communication.callautomation.implementation.models.FileSourceInternal;
import com.azure.communication.callautomation.implementation.models.HoldRequest;
import com.azure.communication.callautomation.implementation.models.PlayOptionsInternal;
import com.azure.communication.callautomation.implementation.models.PlayRequest;
import com.azure.communication.callautomation.implementation.models.PlaySourceInternal;
import com.azure.communication.callautomation.implementation.models.PlaySourceTypeInternal;
import com.azure.communication.callautomation.implementation.models.RecognitionChoiceInternal;
import com.azure.communication.callautomation.implementation.models.RecognizeInputTypeInternal;
import com.azure.communication.callautomation.implementation.models.RecognizeOptionsInternal;
import com.azure.communication.callautomation.implementation.models.RecognizeRequest;
import com.azure.communication.callautomation.implementation.models.SendDtmfTonesRequestInternal;
import com.azure.communication.callautomation.implementation.models.SpeechOptionsInternal;
import com.azure.communication.callautomation.implementation.models.SsmlSourceInternal;
import com.azure.communication.callautomation.implementation.models.StartHoldMusicRequestInternal;
import com.azure.communication.callautomation.implementation.models.StartTranscriptionRequestInternal;
import com.azure.communication.callautomation.implementation.models.StopHoldMusicRequestInternal;
import com.azure.communication.callautomation.implementation.models.StopTranscriptionRequestInternal;
import com.azure.communication.callautomation.implementation.models.TextSourceInternal;
import com.azure.communication.callautomation.implementation.models.UnholdRequest;
import com.azure.communication.callautomation.implementation.models.UpdateTranscriptionRequestInternal;
import com.azure.communication.callautomation.implementation.models.VoiceKindInternal;
import com.azure.communication.callautomation.models.CallMediaRecognizeChoiceOptions;
import com.azure.communication.callautomation.models.CallMediaRecognizeDtmfOptions;
import com.azure.communication.callautomation.models.CallMediaRecognizeOptions;
import com.azure.communication.callautomation.models.CallMediaRecognizeSpeechOptions;
import com.azure.communication.callautomation.models.CallMediaRecognizeSpeechOrDtmfOptions;
import com.azure.communication.callautomation.models.ContinuousDtmfRecognitionOptions;
import com.azure.communication.callautomation.models.DtmfTone;
import com.azure.communication.callautomation.models.FileSource;
import com.azure.communication.callautomation.models.HoldOptions;
import com.azure.communication.callautomation.models.PlayOptions;
import com.azure.communication.callautomation.models.PlaySource;
import com.azure.communication.callautomation.models.PlayToAllOptions;
import com.azure.communication.callautomation.models.RecognitionChoice;
import com.azure.communication.callautomation.models.SendDtmfTonesOptions;
import com.azure.communication.callautomation.models.SendDtmfTonesResult;
import com.azure.communication.callautomation.models.SsmlSource;
import com.azure.communication.callautomation.models.StartHoldMusicOptions;
import com.azure.communication.callautomation.models.StartTranscriptionOptions;
import com.azure.communication.callautomation.models.StopTranscriptionOptions;
import com.azure.communication.callautomation.models.TextSource;
import com.azure.communication.common.CommunicationIdentifier;
import com.azure.core.annotation.ReturnType;
import com.azure.core.annotation.ServiceMethod;
import com.azure.core.exception.HttpResponseException;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.core.util.Context;
import com.azure.core.util.FluxUtil;
import com.azure.core.util.logging.ClientLogger;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.ArrayList;
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
            } else if (recognizeOptions instanceof CallMediaRecognizeChoiceOptions) {
                RecognizeRequest recognizeRequest = getRecognizeRequestFromChoiceConfiguration(recognizeOptions);
                return contentsInternal.recognizeWithResponseAsync(callConnectionId, recognizeRequest, context);
            } else if (recognizeOptions instanceof CallMediaRecognizeSpeechOptions) {
                RecognizeRequest recognizeRequest = getRecognizeRequestFromSpeechConfiguration(recognizeOptions);
                return contentsInternal.recognizeWithResponseAsync(callConnectionId, recognizeRequest, context);
            } else if (recognizeOptions instanceof CallMediaRecognizeSpeechOrDtmfOptions) {
                RecognizeRequest recognizeRequest = getRecognizeRequestFromSpeechOrDtmfConfiguration(recognizeOptions);
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
            PlayToAllOptions playOptions = new PlayToAllOptions(options.getPlaySources());
            playOptions.setLoop(options.isLoop());
            playOptions.setInterruptCallMediaOperation(options.isInterruptCallMediaOperation());
            playOptions.setOperationContext(options.getOperationContext());
            playOptions.setOperationCallbackUrl(options.getOperationCallbackUrl());

            return withContext(contextValue -> {
                contextValue = context == null ? contextValue : context;
                PlayRequest request = getPlayToAllRequest(options);
                return contentsInternal.playWithResponseAsync(callConnectionId, request, contextValue);
            });
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
            } else if (source instanceof TextSource) {
                playSourceInternal = getPlaySourceInternalFromTextSource((TextSource) source);
            } else if (source instanceof SsmlSource) {
                playSourceInternal = getPlaySourceInternalFromSsmlSource((SsmlSource) source);
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
            request.setOperationCallbackUri(options.getOperationCallbackUrl());

            return request;
        }

        throw logger.logExceptionAsError(new IllegalArgumentException(options.getPlaySources().getClass().getCanonicalName()));
    }

    PlayRequest getPlayToAllRequest(PlayToAllOptions options) {
        List<PlaySourceInternal> playSourcesInternal = new ArrayList<>();
        for (PlaySource source: options.getPlaySources()) {
            PlaySourceInternal playSourceInternal = null;
            if (source instanceof FileSource) {
                playSourceInternal = getPlaySourceInternalFromFileSource((FileSource) source);
            } else if (source instanceof TextSource) {
                playSourceInternal = getPlaySourceInternalFromTextSource((TextSource) source);
            } else if (source instanceof SsmlSource) {
                playSourceInternal = getPlaySourceInternalFromSsmlSource((SsmlSource) source);
            }
            if (playSourceInternal != null && playSourceInternal.getKind() != null) {
                playSourcesInternal.add(playSourceInternal);
            } else {
                throw logger.logExceptionAsError(new IllegalArgumentException(source.getClass().getCanonicalName()));
            }
        }

        if (!playSourcesInternal.isEmpty()) {
            PlayRequest request = new PlayRequest()
                .setPlaySources(playSourcesInternal);

            request.setPlayOptions(new PlayOptionsInternal().setLoop(options.isLoop())
                                                            .setInterruptCallMediaOperation(options.isInterruptCallMediaOperation()));
            request.setOperationContext(options.getOperationContext());
            request.setOperationCallbackUri(options.getOperationCallbackUrl());

            return request;
        }

        throw logger.logExceptionAsError(new IllegalArgumentException(options.getPlaySources().getClass().getCanonicalName()));
    }

    private PlaySourceInternal getPlaySourceInternalFromFileSource(FileSource playSource) {
        FileSourceInternal fileSourceInternal = new FileSourceInternal().setUri(playSource.getUrl());
        return new PlaySourceInternal()
            .setKind(PlaySourceTypeInternal.FILE)
            .setFile(fileSourceInternal)
            .setPlaySourceCacheId(playSource.getPlaySourceCacheId());
    }

    private PlaySourceInternal getPlaySourceInternalFromTextSource(TextSource playSource) {
        TextSourceInternal textSourceInternal = new TextSourceInternal().setText(playSource.getText());
        if (playSource.getVoiceKind() != null) {
            textSourceInternal.setVoiceKind(VoiceKindInternal.fromString(playSource.getVoiceKind().toString()));
        }
        if (playSource.getSourceLocale() != null) {
            textSourceInternal.setSourceLocale(playSource.getSourceLocale());
        }
        if (playSource.getVoiceName() != null) {
            textSourceInternal.setVoiceName(playSource.getVoiceName());
        }
        if (playSource.getCustomVoiceEndpointId() != null) {
            textSourceInternal.setCustomVoiceEndpointId(playSource.getCustomVoiceEndpointId());
        }

        return new PlaySourceInternal()
            .setKind(PlaySourceTypeInternal.TEXT)
            .setText(textSourceInternal)
            .setPlaySourceCacheId(playSource.getPlaySourceCacheId());
    }

    private PlaySourceInternal getPlaySourceInternalFromSsmlSource(SsmlSource playSource) {
        SsmlSourceInternal ssmlSourceInternal = new SsmlSourceInternal().setSsmlText(playSource.getSsmlText());

        if (playSource.getCustomVoiceEndpointId() != null) {
            ssmlSourceInternal.setCustomVoiceEndpointId(playSource.getCustomVoiceEndpointId());
        }

        return new PlaySourceInternal()
            .setKind(PlaySourceTypeInternal.SSML)
            .setSsml(ssmlSourceInternal)
            .setPlaySourceCacheId(playSource.getPlaySourceCacheId());
    }

    private PlaySourceInternal convertPlaySourceToPlaySourceInternal(PlaySource playSource) {
        PlaySourceInternal playSourceInternal = new PlaySourceInternal();
        if (playSource instanceof FileSource) {
            playSourceInternal = getPlaySourceInternalFromFileSource((FileSource) playSource);
        } else if (playSource instanceof TextSource) {
            playSourceInternal = getPlaySourceInternalFromTextSource((TextSource) playSource);
        } else if (playSource instanceof SsmlSource) {
            playSourceInternal = getPlaySourceInternalFromSsmlSource((SsmlSource) playSource);
        }
        return playSourceInternal;
    }

    private List<RecognitionChoiceInternal> convertListRecognitionChoiceInternal(List<RecognitionChoice> recognitionChoices) {
        return recognitionChoices.stream()
            .map(this::convertRecognitionChoiceInternal)
            .collect(Collectors.toList());
    }

    private RecognitionChoiceInternal convertRecognitionChoiceInternal(RecognitionChoice recognitionChoice) {
        RecognitionChoiceInternal internalRecognitionChoice = new RecognitionChoiceInternal();
        if (recognitionChoice.getLabel() != null) {
            internalRecognitionChoice.setLabel(recognitionChoice.getLabel());
        }
        if (recognitionChoice.getPhrases() != null) {
            internalRecognitionChoice.setPhrases(recognitionChoice.getPhrases());
        }
        if (recognitionChoice.getTone() != null) {
            internalRecognitionChoice.setTone(convertDtmfToneInternal(recognitionChoice.getTone()));
        }
        return internalRecognitionChoice;
    }

    private DtmfToneInternal convertDtmfToneInternal(DtmfTone dtmfTone) {
        return DtmfToneInternal.fromString(dtmfTone.toString());
    }

    private RecognizeRequest getRecognizeRequestFromDtmfConfiguration(CallMediaRecognizeOptions recognizeOptions) {
        CallMediaRecognizeDtmfOptions dtmfRecognizeOptions = (CallMediaRecognizeDtmfOptions) recognizeOptions;

        DtmfOptionsInternal dtmfOptionsInternal = getDtmfOptionsInternal(
            dtmfRecognizeOptions.getInterToneTimeout(),
            dtmfRecognizeOptions.getMaxTonesToCollect(),
            dtmfRecognizeOptions.getStopTones()
        );

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
            .setOperationContext(recognizeOptions.getOperationContext())
            .setOperationCallbackUri(recognizeOptions.getOperationCallbackUrl());

        return recognizeRequest;
    }

    private RecognizeRequest getRecognizeRequestFromChoiceConfiguration(CallMediaRecognizeOptions recognizeOptions) {
        CallMediaRecognizeChoiceOptions choiceRecognizeOptions = (CallMediaRecognizeChoiceOptions) recognizeOptions;

        RecognizeOptionsInternal recognizeOptionsInternal = new RecognizeOptionsInternal()
            .setChoices(convertListRecognitionChoiceInternal(choiceRecognizeOptions.getChoices()))
            .setInterruptPrompt(choiceRecognizeOptions.isInterruptPrompt())
            .setTargetParticipant(CommunicationIdentifierConverter.convert(choiceRecognizeOptions.getTargetParticipant()));

        recognizeOptionsInternal.setInitialSilenceTimeoutInSeconds((int) choiceRecognizeOptions.getInitialSilenceTimeout().getSeconds());

        if (choiceRecognizeOptions.getSpeechLanguage() != null) {
            if (!choiceRecognizeOptions.getSpeechLanguage().isEmpty()) {
                recognizeOptionsInternal.setSpeechLanguage(choiceRecognizeOptions.getSpeechLanguage());
            }
        }

        if (choiceRecognizeOptions.getSpeechRecognitionModelEndpointId() != null) {
            if (!choiceRecognizeOptions.getSpeechRecognitionModelEndpointId().isEmpty()) {
                recognizeOptionsInternal.setSpeechRecognitionModelEndpointId(choiceRecognizeOptions.getSpeechRecognitionModelEndpointId());
            }
        }

        PlaySourceInternal playSourceInternal = getPlaySourceInternalFromRecognizeOptions(recognizeOptions);

        RecognizeRequest recognizeRequest = new RecognizeRequest()
            .setRecognizeInputType(RecognizeInputTypeInternal.fromString(choiceRecognizeOptions.getRecognizeInputType().toString()))
            .setInterruptCallMediaOperation(choiceRecognizeOptions.isInterruptCallMediaOperation())
            .setPlayPrompt(playSourceInternal)
            .setRecognizeOptions(recognizeOptionsInternal)
            .setOperationContext(recognizeOptions.getOperationContext())
            .setOperationCallbackUri(recognizeOptions.getOperationCallbackUrl());

        return recognizeRequest;
    }

    private RecognizeRequest getRecognizeRequestFromSpeechConfiguration(CallMediaRecognizeOptions recognizeOptions) {
        CallMediaRecognizeSpeechOptions speechRecognizeOptions = (CallMediaRecognizeSpeechOptions) recognizeOptions;

        SpeechOptionsInternal speechOptionsInternal = new SpeechOptionsInternal().setEndSilenceTimeoutInMs(speechRecognizeOptions.getEndSilenceTimeout().toMillis());

        RecognizeOptionsInternal recognizeOptionsInternal = new RecognizeOptionsInternal()
            .setSpeechOptions(speechOptionsInternal)
            .setInterruptPrompt(speechRecognizeOptions.isInterruptPrompt())
            .setTargetParticipant(CommunicationIdentifierConverter.convert(speechRecognizeOptions.getTargetParticipant()));

        recognizeOptionsInternal.setInitialSilenceTimeoutInSeconds((int) speechRecognizeOptions.getInitialSilenceTimeout().getSeconds());


        if (speechRecognizeOptions.getSpeechLanguage() != null) {
            if (!speechRecognizeOptions.getSpeechLanguage().isEmpty()) {
                recognizeOptionsInternal.setSpeechLanguage(speechRecognizeOptions.getSpeechLanguage());
            }
        }

        if (speechRecognizeOptions.getSpeechRecognitionModelEndpointId() != null) {
            if (!speechRecognizeOptions.getSpeechRecognitionModelEndpointId().isEmpty()) {
                recognizeOptionsInternal.setSpeechRecognitionModelEndpointId(speechRecognizeOptions.getSpeechRecognitionModelEndpointId());
            }
        }

        PlaySourceInternal playSourceInternal = getPlaySourceInternalFromRecognizeOptions(recognizeOptions);

        RecognizeRequest recognizeRequest = new RecognizeRequest()
            .setRecognizeInputType(RecognizeInputTypeInternal.fromString(speechRecognizeOptions.getRecognizeInputType().toString()))
            .setInterruptCallMediaOperation(speechRecognizeOptions.isInterruptCallMediaOperation())
            .setPlayPrompt(playSourceInternal)
            .setRecognizeOptions(recognizeOptionsInternal)
            .setOperationContext(recognizeOptions.getOperationContext())
            .setOperationCallbackUri(recognizeOptions.getOperationCallbackUrl());

        return recognizeRequest;
    }

    private RecognizeRequest getRecognizeRequestFromSpeechOrDtmfConfiguration(CallMediaRecognizeOptions recognizeOptions) {
        CallMediaRecognizeSpeechOrDtmfOptions speechOrDtmfRecognizeOptions = (CallMediaRecognizeSpeechOrDtmfOptions) recognizeOptions;

        DtmfOptionsInternal dtmfOptionsInternal = getDtmfOptionsInternal(
            speechOrDtmfRecognizeOptions.getInterToneTimeout(),
            speechOrDtmfRecognizeOptions.getMaxTonesToCollect(),
            speechOrDtmfRecognizeOptions.getStopTones()
        );

        SpeechOptionsInternal speechOptionsInternal = new SpeechOptionsInternal().setEndSilenceTimeoutInMs(speechOrDtmfRecognizeOptions.getEndSilenceTimeout().toMillis());

        RecognizeOptionsInternal recognizeOptionsInternal = new RecognizeOptionsInternal()
            .setSpeechOptions(speechOptionsInternal)
            .setDtmfOptions(dtmfOptionsInternal)
            .setInterruptPrompt(speechOrDtmfRecognizeOptions.isInterruptPrompt())
            .setTargetParticipant(CommunicationIdentifierConverter.convert(speechOrDtmfRecognizeOptions.getTargetParticipant()));

        recognizeOptionsInternal.setInitialSilenceTimeoutInSeconds((int) speechOrDtmfRecognizeOptions.getInitialSilenceTimeout().getSeconds());
        if (speechOrDtmfRecognizeOptions.getSpeechLanguage() != null) {
            if (!speechOrDtmfRecognizeOptions.getSpeechLanguage().isEmpty()) {
                recognizeOptionsInternal.setSpeechLanguage(speechOrDtmfRecognizeOptions.getSpeechLanguage());
            }
        }
        if (speechOrDtmfRecognizeOptions.getSpeechRecognitionModelEndpointId() != null) {
            if (!speechOrDtmfRecognizeOptions.getSpeechRecognitionModelEndpointId().isEmpty()) {
                recognizeOptionsInternal.setSpeechRecognitionModelEndpointId(speechOrDtmfRecognizeOptions.getSpeechRecognitionModelEndpointId());
            }
        }

        PlaySourceInternal playSourceInternal = getPlaySourceInternalFromRecognizeOptions(recognizeOptions);

        RecognizeRequest recognizeRequest = new RecognizeRequest()
            .setRecognizeInputType(RecognizeInputTypeInternal.fromString(speechOrDtmfRecognizeOptions.getRecognizeInputType().toString()))
            .setInterruptCallMediaOperation(speechOrDtmfRecognizeOptions.isInterruptCallMediaOperation())
            .setPlayPrompt(playSourceInternal)
            .setRecognizeOptions(recognizeOptionsInternal)
            .setOperationContext(recognizeOptions.getOperationContext())
            .setOperationCallbackUri(recognizeOptions.getOperationCallbackUrl());

        return recognizeRequest;
    }

    private DtmfOptionsInternal getDtmfOptionsInternal(Duration interToneTimeout, Integer maxTonesToCollect, List<DtmfTone> stopTones) {
        DtmfOptionsInternal dtmfOptionsInternal = new DtmfOptionsInternal();
        dtmfOptionsInternal.setInterToneTimeoutInSeconds((int) interToneTimeout.getSeconds());
        if (maxTonesToCollect != null) {
            dtmfOptionsInternal.setMaxTonesToCollect(maxTonesToCollect);
        }
        if (stopTones != null) {
            List<DtmfToneInternal> dtmfTones = stopTones.stream()
                                        .map(this::convertDtmfToneInternal)
                                        .collect(Collectors.toList());
            dtmfOptionsInternal.setStopTones(dtmfTones);
        }
        return dtmfOptionsInternal;
    }

    private PlaySourceInternal getPlaySourceInternalFromRecognizeOptions(CallMediaRecognizeOptions recognizeOptions) {
        PlaySourceInternal playSourceInternal = null;
        if (recognizeOptions.getPlayPrompt() != null) {
            PlaySource playSource = recognizeOptions.getPlayPrompt();
            playSourceInternal = convertPlaySourceToPlaySourceInternal(playSource);
        }
        return playSourceInternal;
    }

    /**
     * Send DTMF tones
     *
     * @param tones tones to be sent
     * @param targetParticipant the target participant
     * @return Response for successful sendDtmfTones request.
     */
    public Mono<SendDtmfTonesResult> sendDtmfTones(List<DtmfTone> tones, CommunicationIdentifier targetParticipant) {
        return sendDtmfTonesWithResponse(new SendDtmfTonesOptions(tones, targetParticipant)).flatMap(FluxUtil::toMono);
    }

    /**
     * Send DTMF tones
     *
     * @param options SendDtmfTones configuration options
     * @return Response for successful sendDtmfTones request.
     */
    public Mono<Response<SendDtmfTonesResult>> sendDtmfTonesWithResponse(SendDtmfTonesOptions options) {
        return withContext(context -> sendDtmfTonesWithResponseInternal(options, context));
    }

    Mono<Response<SendDtmfTonesResult>> sendDtmfTonesWithResponseInternal(SendDtmfTonesOptions options, Context context) {
        try {
            context = context == null ? Context.NONE : context;
            SendDtmfTonesRequestInternal requestInternal = new SendDtmfTonesRequestInternal()
                .setTargetParticipant(CommunicationIdentifierConverter.convert(options.getTargetParticipant()))
                .setTones(options.getTones().stream()
                .map(this::convertDtmfToneInternal)
                .collect(Collectors.toList()))
                .setOperationContext(options.getOperationContext())
                .setOperationCallbackUri(options.getOperationCallbackUrl());

            return contentsInternal.sendDtmfTonesWithResponseAsync(
                callConnectionId,
                requestInternal,
                context
            ).map(response -> new SimpleResponse<>(response, SendDtmfTonesResponseConstructorProxy.create(response.getValue())));
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Starts continuous Dtmf recognition.
     * @param targetParticipant the target participant
     * @return void
     */
    public Mono<Void> startContinuousDtmfRecognition(CommunicationIdentifier targetParticipant) {
        return startContinuousDtmfRecognitionWithResponse(new ContinuousDtmfRecognitionOptions(targetParticipant)).then();
    }

    /**
     * Starts continuous Dtmf recognition.
     * @param options ContinuousDtmfRecognition configuration options
     * @return Response for successful start continuous dtmf recognition request.
     */
    public Mono<Response<Void>> startContinuousDtmfRecognitionWithResponse(ContinuousDtmfRecognitionOptions options) {
        return withContext(context -> startContinuousDtmfRecognitionWithResponseInternal(options, context));
    }

    Mono<Response<Void>> startContinuousDtmfRecognitionWithResponseInternal(ContinuousDtmfRecognitionOptions options, Context context) {
        try {
            context = context == null ? Context.NONE : context;
            ContinuousDtmfRecognitionRequestInternal requestInternal = new ContinuousDtmfRecognitionRequestInternal()
                .setTargetParticipant(CommunicationIdentifierConverter.convert(options.getTargetParticipant()))
                .setOperationContext(options.getOperationContext());

            return contentsInternal.startContinuousDtmfRecognitionWithResponseAsync(callConnectionId, requestInternal, context);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Stops continuous Dtmf recognition.
     * @param targetParticipant the target participant
     * @return void
     */
    public Mono<Void> stopContinuousDtmfRecognition(CommunicationIdentifier targetParticipant) {
        return stopContinuousDtmfRecognitionWithResponse(new ContinuousDtmfRecognitionOptions(targetParticipant)).then();
    }

    /**
     * Stops continuous Dtmf recognition.
     * @param options ContinuousDtmfRecognition configuration options
     * @return Response for successful stop continuous dtmf recognition request.
     */
    public Mono<Response<Void>> stopContinuousDtmfRecognitionWithResponse(ContinuousDtmfRecognitionOptions options) {
        return withContext(context -> stopContinuousDtmfRecognitionWithResponseInternal(options, context));
    }

    Mono<Response<Void>> stopContinuousDtmfRecognitionWithResponseInternal(ContinuousDtmfRecognitionOptions options, Context context) {
        try {
            context = context == null ? Context.NONE : context;
            ContinuousDtmfRecognitionRequestInternal requestInternal = new ContinuousDtmfRecognitionRequestInternal()
                .setTargetParticipant(CommunicationIdentifierConverter.convert(options.getTargetParticipant()))
                .setOperationContext(options.getOperationContext())
                .setOperationCallbackUri(options.getOperationCallbackUrl());

            return contentsInternal.stopContinuousDtmfRecognitionWithResponseAsync(callConnectionId, requestInternal, context);
        } catch (RuntimeException e) {
            return monoError(logger, e);
        }
    }

    /**
     * Holds participant in call.
     * @param targetParticipant the target.
     * @param playSourceInfo audio to play.
     * @return Response for successful operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> startHoldMusic(CommunicationIdentifier targetParticipant,
                                     PlaySource playSourceInfo) {
        return startHoldMusicWithResponseInternal(
            new StartHoldMusicOptions(targetParticipant, playSourceInfo),
            Context.NONE).then();
    }

    /**
     * Holds participant in call.
     * @param options - Different options to pass to the request.
     * @return Response for successful operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> startHoldMusicWithResponse(StartHoldMusicOptions options) {
        return withContext(context -> startHoldMusicWithResponseInternal(
            options, context));
    }

    Mono<Response<Void>> startHoldMusicWithResponseInternal(StartHoldMusicOptions options, Context context) {
        try {
            context = context == null ? Context.NONE : context;

            StartHoldMusicRequestInternal request = new StartHoldMusicRequestInternal()
                .setTargetParticipant(CommunicationIdentifierConverter.convert(options.getTargetParticipant()))
                .setPlaySourceInfo(convertPlaySourceToPlaySourceInternal(options.getPlaySourceInfo()))
                .setOperationContext(options.getOperationContext());

            return contentsInternal
                .startHoldMusicWithResponseAsync(callConnectionId, request, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Removes hold from participant in call.
     * @param targetParticipant the target.
     * @return Response for successful operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> stopHoldMusic(CommunicationIdentifier targetParticipant) {
        return stopHoldMusicWithResponse(targetParticipant, null).then();
    }

    /**
     * Holds participant in call.
     * @param targetParticipant the target.
     * @param operationContext Operational context.
     * @return Response for successful operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> stopHoldMusicWithResponse(CommunicationIdentifier targetParticipant,
                                                               String operationContext) {
        return withContext(context -> stopHoldMusicWithResponseInternal(targetParticipant, operationContext, context));
    }

    Mono<Response<Void>> stopHoldMusicWithResponseInternal(CommunicationIdentifier targetParticipant,
                                                            String operationContext,
                                                            Context context) {
        try {
            context = context == null ? Context.NONE : context;
            StopHoldMusicRequestInternal request = new StopHoldMusicRequestInternal()
                .setTargetParticipant(CommunicationIdentifierConverter.convert(targetParticipant))
                .setOperationContext(operationContext);

            return contentsInternal
                .stopHoldMusicWithResponseAsync(callConnectionId, request, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

   /**
     * Holds participant in call.
     * @param targetParticipant the target.
     * @return Response for successful operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> hold(CommunicationIdentifier targetParticipant) {
        return holdInternal(targetParticipant, Context.NONE).then();
    }

    Mono<Response<Void>> holdInternal(CommunicationIdentifier targetParticipant, Context context) {
        try {
            context = context == null ? Context.NONE : context;
            HoldRequest request = new HoldRequest()
                .setTargetParticipant(CommunicationIdentifierConverter.convert(targetParticipant));

            return contentsInternal
                .holdWithResponseAsync(callConnectionId, request, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Holds participant in call.
     * @param options - Different options to pass to the request.
     * @return Response for successful operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> holdWithResponse(HoldOptions options) {
        return withContext(context -> holdWithResponseInternal(
            options, context));
    }

    Mono<Response<Void>> holdWithResponseInternal(HoldOptions options, Context context) {
        try {
            context = context == null ? Context.NONE : context;
            HoldRequest request = new HoldRequest()
                .setTargetParticipant(CommunicationIdentifierConverter.convert(options.getTargetParticipant()))
                .setPlaySourceInfo(convertPlaySourceToPlaySourceInternal(options.getPlaySourceInfo()))
                .setOperationContext(options.getOperationContext());

            return contentsInternal
                .holdWithResponseAsync(callConnectionId, request, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Removes hold from participant in call.
     * @param targetParticipant the target.
     * @return Response for successful operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> unhold(CommunicationIdentifier targetParticipant) {
        return unholdWithResponse(targetParticipant, null).then();
    }

    /**
     * Holds participant in call.
     * @param targetParticipant the target.
     * @param operationContext Operational context.
     * @return Response for successful operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> unholdWithResponse(CommunicationIdentifier targetParticipant,
                                                               String operationContext) {
        return withContext(context -> unholdWithResponseInternal(targetParticipant, operationContext, context));
    }

    Mono<Response<Void>> unholdWithResponseInternal(CommunicationIdentifier targetParticipant,
                                                            String operationContext,
                                                            Context context) {
        try {
            context = context == null ? Context.NONE : context;
            UnholdRequest request = new UnholdRequest()
                .setTargetParticipant(CommunicationIdentifierConverter.convert(targetParticipant))
                .setOperationContext(operationContext);

            return contentsInternal
                .unholdWithResponseAsync(callConnectionId, request, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Starts transcription in the call.
     *
     * @return Response for successful operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> startTranscription() {
        return startTranscriptionWithResponseAsync(null).then();
    }

    /**
     * Starts transcription in the call with options.
     *
     * @param options Options for the Start Transcription operation.
     * @return Response for successful operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> startTranscriptionWithResponseAsync(StartTranscriptionOptions options) {
        return withContext(context -> startTranscriptionWithResponseInternal(options, context));
    }

    Mono<Response<Void>> startTranscriptionWithResponseInternal(StartTranscriptionOptions options, Context context) {
        try {
            context = context == null ? Context.NONE : context;
            StartTranscriptionRequestInternal request = new StartTranscriptionRequestInternal();
            if (options != null) {
                request.setLocale(options.getLocale());
                request.setOperationContext(options.getOperationContext());
            }
            return contentsInternal
                .startTranscriptionWithResponseAsync(callConnectionId, request, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Stops transcription in the call.
     *
     * @return Response for successful operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> stopTranscription() {
        return stopTranscriptionWithResponseAsync(null).then();
    }

    /**
     * Stops transcription in the call with options.
     *
     * @param options Options for the Stop Transcription operation.
     * @return Response for successful operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Response<Void>> stopTranscriptionWithResponseAsync(StopTranscriptionOptions options) {
        return withContext(context -> stopTranscriptionWithResponseInternal(options, context));
    }

    Mono<Response<Void>> stopTranscriptionWithResponseInternal(StopTranscriptionOptions options, Context context) {
        try {
            context = context == null ? Context.NONE : context;
            StopTranscriptionRequestInternal request = new StopTranscriptionRequestInternal();
            if (options != null) {
                request.setOperationContext(options.getOperationContext());
            }
            return contentsInternal
                .stopTranscriptionWithResponseAsync(callConnectionId, request, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }

    /**
     * Updates transcription language
     *
     * @param locale Defines new locale for transcription.
     * @return Response for successful operation.
     */
    @ServiceMethod(returns = ReturnType.SINGLE)
    public Mono<Void> updateTranscription(String locale) {
        return withContext(context -> updateTranscriptionWithResponseInternal(locale, context)).then();
    }

    Mono<Response<Void>> updateTranscriptionWithResponseInternal(String locale, Context context) {
        try {
            context = context == null ? Context.NONE : context;
            UpdateTranscriptionRequestInternal request = new UpdateTranscriptionRequestInternal();
            request.setLocale(locale);
            return contentsInternal
                .updateTranscriptionWithResponseAsync(callConnectionId, request, context);
        } catch (RuntimeException ex) {
            return monoError(logger, ex);
        }
    }


}
