// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.implementation.CallMediasImpl;
import com.azure.communication.callautomation.implementation.accesshelpers.ErrorConstructorProxy;
import com.azure.communication.callautomation.implementation.converters.CommunicationIdentifierConverter;
import com.azure.communication.callautomation.implementation.models.DtmfOptionsInternal;
import com.azure.communication.callautomation.implementation.models.DtmfToneInternal;
import com.azure.communication.callautomation.implementation.models.FileSourceInternal;
import com.azure.communication.callautomation.implementation.models.GenderTypeInternal;
import com.azure.communication.callautomation.implementation.models.TextSourceInternal;
import com.azure.communication.callautomation.implementation.models.SsmlSourceInternal;
import com.azure.communication.callautomation.implementation.models.PlayOptionsInternal;
import com.azure.communication.callautomation.implementation.models.SpeechOptionsInternal;
import com.azure.communication.callautomation.implementation.models.PlayRequest;
import com.azure.communication.callautomation.implementation.models.PlaySourceInternal;
import com.azure.communication.callautomation.implementation.models.PlaySourceTypeInternal;
import com.azure.communication.callautomation.implementation.models.RecognizeChoiceInternal;
import com.azure.communication.callautomation.implementation.models.RecognizeInputTypeInternal;
import com.azure.communication.callautomation.implementation.models.RecognizeOptionsInternal;
import com.azure.communication.callautomation.implementation.models.RecognizeRequest;
import com.azure.communication.callautomation.models.CallMediaRecognizeChoiceOptions;
import com.azure.communication.callautomation.models.CallMediaRecognizeDtmfOptions;
import com.azure.communication.callautomation.models.CallingServerErrorException;
import com.azure.communication.callautomation.models.DtmfTone;
import com.azure.communication.callautomation.models.FileSource;
import com.azure.communication.callautomation.models.PlayOptions;
import com.azure.communication.callautomation.models.PlaySource;
import com.azure.communication.callautomation.models.RecognizeChoice;
import com.azure.communication.callautomation.models.TextSource;
import com.azure.communication.callautomation.models.SsmlSource;
import com.azure.communication.callautomation.models.CallMediaRecognizeOptions;
import com.azure.communication.callautomation.models.CallMediaRecognizeSpeechOptions;
import com.azure.communication.callautomation.models.CallMediaRecognizeSpeechOrDtmfOptions;
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
        PlaySourceInternal playSourceInternal = new PlaySourceInternal();
        if (playSource instanceof FileSource) {
            playSourceInternal = getPlaySourceInternalFromFileSource((FileSource) playSource);
        } else if (playSource instanceof TextSource) {
            playSourceInternal = getPlaySourceInternalFromTextSource((TextSource) playSource);
        } else if (playSource instanceof SsmlSource) {
            playSourceInternal = getPlaySourceInternalFromSsmlSource((SsmlSource) playSource);
        }

        if (playSourceInternal.getSourceType() != null) {
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

    private PlaySourceInternal getPlaySourceInternalFromFileSource(FileSource playSource) {
        FileSourceInternal fileSourceInternal = new FileSourceInternal().setUri(playSource.getUri());
        PlaySourceInternal playSourceInternal = new PlaySourceInternal()
            .setSourceType(PlaySourceTypeInternal.FILE)
            .setFileSource(fileSourceInternal)
            .setPlaySourceId(playSource.getPlaySourceId());
        return playSourceInternal;
    }

    private PlaySourceInternal getPlaySourceInternalFromTextSource(TextSource playSource) {
        TextSourceInternal textSourceInternal = new TextSourceInternal().setText(playSource.getText());
        if (playSource.getVoiceGender() != null) {
            textSourceInternal.setVoiceGender(GenderTypeInternal.fromString(playSource.getVoiceGender().toString()));
        }
        if (playSource.getSourceLocale() != null) {
            textSourceInternal.setSourceLocale(playSource.getSourceLocale());
        }
        if (playSource.getVoiceName() != null) {
            textSourceInternal.setVoiceName(playSource.getVoiceName());
        }

        PlaySourceInternal playSourceInternal = new PlaySourceInternal()
            .setSourceType(PlaySourceTypeInternal.TEXT)
            .setTextSource(textSourceInternal)
            .setPlaySourceId(playSource.getPlaySourceId());
        return playSourceInternal;
    }

    private PlaySourceInternal getPlaySourceInternalFromSsmlSource(SsmlSource playSource) {
        SsmlSourceInternal ssmlSourceInternal = new SsmlSourceInternal().setSsmlText(playSource.getSsmlText());
        PlaySourceInternal playSourceInternal = new PlaySourceInternal()
            .setSourceType(PlaySourceTypeInternal.SSML)
            .setSsmlSource(ssmlSourceInternal)
            .setPlaySourceId(playSource.getPlaySourceId());
        return playSourceInternal;
    }

    private PlaySourceInternal convertPlaySourceToPlaySourceInternal(PlaySource playSource) {
        PlaySourceInternal playSourceInternal = new PlaySourceInternal();
        if (playSource instanceof FileSource) {
            playSourceInternal = getPlaySourceInternalFromFileSource((FileSource) playSource);
        } else if (playSource instanceof TextSource) {
            playSourceInternal = getPlaySourceInternalFromTextSource((TextSource) playSource);
        }
        return playSourceInternal;
    }

    private List<RecognizeChoiceInternal> convertListRecognizeChoiceInternal(List<RecognizeChoice> recognizeChoices) {
        return recognizeChoices.stream()
            .map(this::convertRecognizeChoiceInternal)
            .collect(Collectors.toList());
    }

    private RecognizeChoiceInternal convertRecognizeChoiceInternal(RecognizeChoice recognizeChoice) {
        RecognizeChoiceInternal internalRecognizeChoice = new RecognizeChoiceInternal();
        if (recognizeChoice.getLabel() != null) {
            internalRecognizeChoice.setLabel(recognizeChoice.getLabel());
        }
        if (recognizeChoice.getPhrases() != null) {
            internalRecognizeChoice.setPhrases(recognizeChoice.getPhrases());
        }
        if (recognizeChoice.getTone() != null) {
            internalRecognizeChoice.setTone(convertDtmfToneInternal(recognizeChoice.getTone()));
        }
        return internalRecognizeChoice;
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

    private RecognizeRequest getRecognizeRequestFromChoiceConfiguration(CallMediaRecognizeOptions recognizeOptions) {
        CallMediaRecognizeChoiceOptions choiceRecognizeOptions = (CallMediaRecognizeChoiceOptions) recognizeOptions;

        RecognizeOptionsInternal recognizeOptionsInternal = new RecognizeOptionsInternal()
            .setChoices(convertListRecognizeChoiceInternal(choiceRecognizeOptions.getRecognizeChoices()))
            .setInterruptPrompt(choiceRecognizeOptions.isInterruptPrompt())
            .setTargetParticipant(CommunicationIdentifierConverter.convert(choiceRecognizeOptions.getTargetParticipant()));

        recognizeOptionsInternal.setInitialSilenceTimeoutInSeconds((int) choiceRecognizeOptions.getInitialSilenceTimeout().getSeconds());

        if (choiceRecognizeOptions.getSpeechLanguage() != null) {
            if (!choiceRecognizeOptions.getSpeechLanguage().isEmpty()) {
                recognizeOptionsInternal.setSpeechLanguage(choiceRecognizeOptions.getSpeechLanguage());
            }
        }

        PlaySourceInternal playSourceInternal = getPlaySourceInternalFromRecognizeOptions(recognizeOptions);

        RecognizeRequest recognizeRequest = new RecognizeRequest()
            .setRecognizeInputType(RecognizeInputTypeInternal.fromString(choiceRecognizeOptions.getRecognizeInputType().toString()))
            .setInterruptCallMediaOperation(choiceRecognizeOptions.isInterruptCallMediaOperation())
            .setPlayPrompt(playSourceInternal)
            .setRecognizeOptions(recognizeOptionsInternal)
            .setOperationContext(recognizeOptions.getOperationContext());

        return recognizeRequest;
    }

    private RecognizeRequest getRecognizeRequestFromSpeechConfiguration(CallMediaRecognizeOptions recognizeOptions) {
        CallMediaRecognizeSpeechOptions speechRecognizeOptions = (CallMediaRecognizeSpeechOptions) recognizeOptions;

        SpeechOptionsInternal speechOptionsInternal = new SpeechOptionsInternal().setEndSilenceTimeoutInMs(speechRecognizeOptions.getEndSilenceTimeoutInMs().toMillis());

        RecognizeOptionsInternal recognizeOptionsInternal = new RecognizeOptionsInternal()
            .setSpeechOptions(speechOptionsInternal)
            .setInterruptPrompt(speechRecognizeOptions.isInterruptPrompt())
            .setTargetParticipant(CommunicationIdentifierConverter.convert(speechRecognizeOptions.getTargetParticipant()));

        recognizeOptionsInternal.setInitialSilenceTimeoutInSeconds((int) speechRecognizeOptions.getInitialSilenceTimeout().getSeconds());

        PlaySourceInternal playSourceInternal = getPlaySourceInternalFromRecognizeOptions(recognizeOptions);

        RecognizeRequest recognizeRequest = new RecognizeRequest()
            .setRecognizeInputType(RecognizeInputTypeInternal.fromString(speechRecognizeOptions.getRecognizeInputType().toString()))
            .setInterruptCallMediaOperation(speechRecognizeOptions.isInterruptCallMediaOperation())
            .setPlayPrompt(playSourceInternal)
            .setRecognizeOptions(recognizeOptionsInternal)
            .setOperationContext(recognizeOptions.getOperationContext());

        return recognizeRequest;
    }

    private RecognizeRequest getRecognizeRequestFromSpeechOrDtmfConfiguration(CallMediaRecognizeOptions recognizeOptions) {
        CallMediaRecognizeSpeechOrDtmfOptions speechOrDtmfRecognizeOptions = (CallMediaRecognizeSpeechOrDtmfOptions) recognizeOptions;

        DtmfOptionsInternal dtmfOptionsInternal = new DtmfOptionsInternal();
        dtmfOptionsInternal.setInterToneTimeoutInSeconds((int) speechOrDtmfRecognizeOptions.getInterToneTimeout().getSeconds());

        if (speechOrDtmfRecognizeOptions.getMaxTonesToCollect() != null) {
            dtmfOptionsInternal.setMaxTonesToCollect(speechOrDtmfRecognizeOptions.getMaxTonesToCollect());
        }

        if (speechOrDtmfRecognizeOptions.getStopTones() != null) {
            List<DtmfToneInternal> dtmfTones = speechOrDtmfRecognizeOptions.getStopTones().stream()
                                        .map(this::convertDtmfToneInternal)
                                        .collect(Collectors.toList());
            dtmfOptionsInternal.setStopTones(dtmfTones);
        }

        SpeechOptionsInternal speechOptionsInternal = new SpeechOptionsInternal().setEndSilenceTimeoutInMs(speechOrDtmfRecognizeOptions.getEndSilenceTimeoutInMs().toMillis());

        RecognizeOptionsInternal recognizeOptionsInternal = new RecognizeOptionsInternal()
            .setSpeechOptions(speechOptionsInternal)
            .setDtmfOptions(dtmfOptionsInternal)
            .setInterruptPrompt(speechOrDtmfRecognizeOptions.isInterruptPrompt())
            .setTargetParticipant(CommunicationIdentifierConverter.convert(speechOrDtmfRecognizeOptions.getTargetParticipant()));

        recognizeOptionsInternal.setInitialSilenceTimeoutInSeconds((int) speechOrDtmfRecognizeOptions.getInitialSilenceTimeout().getSeconds());

        PlaySourceInternal playSourceInternal = getPlaySourceInternalFromRecognizeOptions(recognizeOptions);

        RecognizeRequest recognizeRequest = new RecognizeRequest()
            .setRecognizeInputType(RecognizeInputTypeInternal.fromString(speechOrDtmfRecognizeOptions.getRecognizeInputType().toString()))
            .setInterruptCallMediaOperation(speechOrDtmfRecognizeOptions.isInterruptCallMediaOperation())
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
