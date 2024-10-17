// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.implementation.models.SendDtmfTonesResultInternal;
import com.azure.communication.callautomation.models.CallMediaRecognizeChoiceOptions;
import com.azure.communication.callautomation.models.CallMediaRecognizeDtmfOptions;
import com.azure.communication.callautomation.models.CallMediaRecognizeSpeechOptions;
import com.azure.communication.callautomation.models.CallMediaRecognizeSpeechOrDtmfOptions;
import com.azure.communication.callautomation.models.ContinuousDtmfRecognitionOptions;
import com.azure.communication.callautomation.models.DtmfTone;
import com.azure.communication.callautomation.models.FileSource;
import com.azure.communication.callautomation.models.HoldOptions;
import com.azure.communication.callautomation.models.PlayOptions;
import com.azure.communication.callautomation.models.PlayToAllOptions;
import com.azure.communication.callautomation.models.RecognitionChoice;
import com.azure.communication.callautomation.models.RecognizeInputType;
import com.azure.communication.callautomation.models.SendDtmfTonesOptions;
import com.azure.communication.callautomation.models.SsmlSource;
import com.azure.communication.callautomation.models.StartHoldMusicOptions;
import com.azure.communication.callautomation.models.StartTranscriptionOptions;
import com.azure.communication.callautomation.models.StopTranscriptionOptions;
import com.azure.communication.callautomation.models.StartMediaStreamingOptions;
import com.azure.communication.callautomation.models.StopMediaStreamingOptions;
import com.azure.communication.callautomation.models.TextSource;
import com.azure.communication.callautomation.models.VoiceKind;
import com.azure.communication.common.CommunicationUserIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CallMediaAsyncUnitTests {

    private CallMediaAsync callMedia;
    private FileSource playFileSource;
    private TextSource playTextSource;
    private SsmlSource playSsmlSource;
    private PlayOptions playOptions;
    private PlayToAllOptions playToAllOptions;

    @BeforeEach
    public void setup() {
        callMedia = getMockCallMedia(202);

        playFileSource = new FileSource();
        playFileSource.setPlaySourceCacheId("playFileSourceId");
        playFileSource.setUrl("filePath");

        playTextSource = new TextSource();
        playTextSource.setPlaySourceCacheId("playTextSourceId");
        playTextSource.setVoiceKind(VoiceKind.MALE);
        playTextSource.setSourceLocale("en-US");
        playTextSource.setVoiceName("LULU");
        playTextSource.setCustomVoiceEndpointId("customVoiceEndpointId");

        playSsmlSource = new SsmlSource();
        playSsmlSource.setSsmlText("<speak><voice name=\"LULU\"></voice></speak>");
        playSsmlSource.setCustomVoiceEndpointId("customVoiceEndpointId");
    }

    @Test
    public void playFileWithResponseTest() {
        playOptions = new PlayOptions(playFileSource, Collections.singletonList(new CommunicationUserIdentifier("id")))
            .setLoop(false)
            .setOperationContext("operationContext");

        StepVerifier.create(
            callMedia.playWithResponse(playOptions))
            .consumeNextWith(response -> assertEquals(202, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void playFileToAllWithResponseTest() {
        playToAllOptions = new PlayToAllOptions(playFileSource)
            .setLoop(false)
            .setOperationContext("operationContext");
        StepVerifier.create(
                callMedia.playToAllWithResponse(playToAllOptions))
            .consumeNextWith(response -> assertEquals(202, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void playFileToAllWithBargeInWithResponseTest() {
        playToAllOptions = new PlayToAllOptions(playFileSource)
            .setLoop(false)
            .setInterruptCallMediaOperation(true)
            .setOperationContext("operationContext");
        StepVerifier.create(
                callMedia.playToAllWithResponse(playToAllOptions))
            .consumeNextWith(response -> assertEquals(202, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void playTextWithResponseTest() {
        playOptions = new PlayOptions(playTextSource, Collections.singletonList(new CommunicationUserIdentifier("id")))
            .setLoop(false)
            .setOperationContext("operationContext");
        StepVerifier.create(
            callMedia.playWithResponse(playOptions))
            .consumeNextWith(response -> assertEquals(202, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void playTextToAllWithResponseTest() {
        playToAllOptions = new PlayToAllOptions(playTextSource)
            .setLoop(false)
            .setOperationContext("operationContext");
        StepVerifier.create(
            callMedia.playToAllWithResponse(playToAllOptions))
            .consumeNextWith(response -> assertEquals(202, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void playTextToAllWithBargeInWithResponseTest() {
        playToAllOptions = new PlayToAllOptions(playTextSource)
            .setLoop(false)
            .setInterruptCallMediaOperation(true)
            .setOperationContext("operationContext");
        StepVerifier.create(
            callMedia.playToAllWithResponse(playToAllOptions))
            .consumeNextWith(response -> assertEquals(202, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void playSsmlWithResponseTest() {
        playOptions = new PlayOptions(playSsmlSource, Collections.singletonList(new CommunicationUserIdentifier("id")))
            .setLoop(false)
            .setOperationContext("operationContext");
        StepVerifier.create(
            callMedia.playWithResponse(playOptions))
            .consumeNextWith(response -> assertEquals(202, response.getStatusCode()))
            .verifyComplete();
    }
    
    @Test
    public void playSsmlToAllWithResponseTest() {
        playToAllOptions = new PlayToAllOptions(playSsmlSource)
            .setLoop(false)
            .setOperationContext("operationContext");
        StepVerifier.create(
            callMedia.playToAllWithResponse(playToAllOptions))
            .consumeNextWith(response -> assertEquals(202, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void playSsmlToAllWithBargeInWithResponseTest() {
        playToAllOptions = new PlayToAllOptions(playSsmlSource)
            .setLoop(false)
            .setInterruptCallMediaOperation(true)
            .setOperationContext("operationContext");
        StepVerifier.create(
            callMedia.playToAllWithResponse(playToAllOptions))
            .consumeNextWith(response -> assertEquals(202, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void cancelAllOperationsWithResponse() {
        StepVerifier.create(
                callMedia.cancelAllMediaOperationsWithResponse())
            .consumeNextWith(response -> assertEquals(202, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void recognizeWithResponse() {
        CallMediaRecognizeDtmfOptions recognizeOptions = new CallMediaRecognizeDtmfOptions(new CommunicationUserIdentifier("id"), 1);
        StepVerifier.create(
                callMedia.startRecognizingWithResponse(recognizeOptions))
            .consumeNextWith(response -> assertEquals(202, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void recognizeWithResponseWithFileSourceDtmfOptions() {
        CallMediaRecognizeDtmfOptions recognizeOptions = new CallMediaRecognizeDtmfOptions(new CommunicationUserIdentifier("id"), 5);

        recognizeOptions.setInterToneTimeout(Duration.ofSeconds(3));
        List<DtmfTone> stopDtmfTones = new ArrayList<>();
        stopDtmfTones.add(DtmfTone.ZERO);
        stopDtmfTones.add(DtmfTone.ONE);
        stopDtmfTones.add(DtmfTone.TWO);
        recognizeOptions.setStopTones(stopDtmfTones);
        recognizeOptions.setRecognizeInputType(RecognizeInputType.DTMF);
        recognizeOptions.setPlayPrompt(new FileSource().setUrl("abc"));
        recognizeOptions.setInterruptCallMediaOperation(true);
        recognizeOptions.setStopCurrentOperations(true);
        recognizeOptions.setOperationContext("operationContext");
        recognizeOptions.setInterruptPrompt(true);
        recognizeOptions.setInitialSilenceTimeout(Duration.ofSeconds(4));

        StepVerifier.create(
                callMedia.startRecognizingWithResponse(recognizeOptions))
            .consumeNextWith(response -> assertEquals(202, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void startContinuousDtmfRecognitionWithResponse() {
        // override callMedia to mock 200 response code
        callMedia = getMockCallMedia(200);
        ContinuousDtmfRecognitionOptions options = new ContinuousDtmfRecognitionOptions(new CommunicationUserIdentifier("id"));
        StepVerifier.create(
                callMedia.startContinuousDtmfRecognitionWithResponse(
                    options))
            .consumeNextWith(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void stopContinuousDtmfRecognitionWithResponse() {
        // override callMedia to mock 200 response code
        callMedia = getMockCallMedia(200);
        ContinuousDtmfRecognitionOptions options = new ContinuousDtmfRecognitionOptions(new CommunicationUserIdentifier("id"));
        StepVerifier.create(
                callMedia.stopContinuousDtmfRecognitionWithResponse(
                    options))
            .consumeNextWith(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void sendDtmfWithResponse() {
        CallConnectionAsync callConnection =
            CallAutomationUnitTestBase.getCallConnectionAsync(new ArrayList<>(
                Collections.singletonList(new AbstractMap.SimpleEntry<>(
                    CallAutomationUnitTestBase.serializeObject(new SendDtmfTonesResultInternal().setOperationContext("operationContext")), 202)))
            );
        callMedia = callConnection.getCallMediaAsync();
        List<DtmfTone> tones = Stream.of(DtmfTone.ONE, DtmfTone.TWO, DtmfTone.THREE).collect(Collectors.toList());
        SendDtmfTonesOptions options = new SendDtmfTonesOptions(tones, new CommunicationUserIdentifier("id"));
        options.setOperationContext("operationContext");
        options.setOperationCallbackUrl(CallAutomationUnitTestBase.CALL_CALLBACK_URL);
        StepVerifier.create(callMedia.sendDtmfTonesWithResponse(options))
            .consumeNextWith(response -> assertEquals(202, response.getStatusCode())).verifyComplete();
    }

    @Test
    public void recognizeWithResponseWithTextSourceDtmfOptions() {
        CallMediaRecognizeDtmfOptions recognizeOptions = new CallMediaRecognizeDtmfOptions(new CommunicationUserIdentifier("id"), 5);

        recognizeOptions.setInterToneTimeout(Duration.ofSeconds(3));
        List<DtmfTone> stopDtmfTones = new ArrayList<>();
        stopDtmfTones.add(DtmfTone.ZERO);
        stopDtmfTones.add(DtmfTone.ONE);
        stopDtmfTones.add(DtmfTone.TWO);
        recognizeOptions.setRecognizeInputType(RecognizeInputType.DTMF);
        recognizeOptions.setStopTones(stopDtmfTones);
        recognizeOptions.setPlayPrompt(new TextSource().setText("Test dmtf option with text source."));
        recognizeOptions.setInterruptCallMediaOperation(true);
        recognizeOptions.setStopCurrentOperations(true);
        recognizeOptions.setOperationContext("operationContext");
        recognizeOptions.setInterruptPrompt(true);
        recognizeOptions.setInitialSilenceTimeout(Duration.ofSeconds(4));

        StepVerifier.create(
                callMedia.startRecognizingWithResponse(recognizeOptions))
            .consumeNextWith(response -> assertEquals(202, response.getStatusCode()))
            .verifyComplete();
    }



    @Test
    public void recognizeWithResponseWithFileSourceChoiceOptions() {

        RecognitionChoice recognizeChoice1 = new RecognitionChoice();
        RecognitionChoice recognizeChoice2 = new RecognitionChoice();
        recognizeChoice1.setTone(DtmfTone.ZERO);
        recognizeChoice2.setTone(DtmfTone.SIX);
        List<RecognitionChoice> recognizeChoices = new ArrayList<>(
            Arrays.asList(recognizeChoice1, recognizeChoice2)
        );
        CallMediaRecognizeChoiceOptions recognizeOptions = new CallMediaRecognizeChoiceOptions(new CommunicationUserIdentifier("id"), recognizeChoices);

        recognizeOptions.setRecognizeInputType(RecognizeInputType.CHOICES);
        recognizeOptions.setPlayPrompt(new FileSource().setUrl("abc"));
        recognizeOptions.setInterruptCallMediaOperation(true);
        recognizeOptions.setStopCurrentOperations(true);
        recognizeOptions.setOperationContext("operationContext");
        recognizeOptions.setInterruptPrompt(true);
        recognizeOptions.setInitialSilenceTimeout(Duration.ofSeconds(4));
        recognizeOptions.setSpeechLanguage("en-US");
        recognizeOptions.setSpeechModelEndpointId("customModelEndpointId");

        StepVerifier.create(
                callMedia.startRecognizingWithResponse(recognizeOptions))
            .consumeNextWith(response -> assertEquals(202, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void recognizeWithResponseTextChoiceOptions() {

        RecognitionChoice recognizeChoice1 = new RecognitionChoice();
        RecognitionChoice recognizeChoice2 = new RecognitionChoice();
        recognizeChoice1.setTone(DtmfTone.ZERO);
        recognizeChoice2.setTone(DtmfTone.THREE);
        List<RecognitionChoice> recognizeChoices = new ArrayList<>(
            Arrays.asList(recognizeChoice1, recognizeChoice2)
        );
        CallMediaRecognizeChoiceOptions recognizeOptions = new CallMediaRecognizeChoiceOptions(new CommunicationUserIdentifier("id"), recognizeChoices);

        recognizeOptions.setRecognizeInputType(RecognizeInputType.CHOICES);
        recognizeOptions.setPlayPrompt(new TextSource().setText("Test recognize choice with text source."));
        recognizeOptions.setInterruptCallMediaOperation(true);
        recognizeOptions.setStopCurrentOperations(true);
        recognizeOptions.setOperationContext("operationContext");
        recognizeOptions.setInterruptPrompt(true);
        recognizeOptions.setInitialSilenceTimeout(Duration.ofSeconds(4));
        recognizeOptions.setSpeechLanguage("en-US");

        StepVerifier.create(
                callMedia.startRecognizingWithResponse(recognizeOptions))
            .consumeNextWith(response -> assertEquals(202, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void recognizeWithResponseTextSpeechOptions() {

        CallMediaRecognizeSpeechOptions recognizeOptions = new CallMediaRecognizeSpeechOptions(new CommunicationUserIdentifier("id"), Duration.ofMillis(1000));

        recognizeOptions.setRecognizeInputType(RecognizeInputType.SPEECH);
        recognizeOptions.setPlayPrompt(new TextSource().setText("Test recognize speech or dtmf with text source."));
        recognizeOptions.setInterruptCallMediaOperation(true);
        recognizeOptions.setStopCurrentOperations(true);
        recognizeOptions.setOperationContext("operationContext");
        recognizeOptions.setInterruptPrompt(true);
        recognizeOptions.setInitialSilenceTimeout(Duration.ofSeconds(4));
        recognizeOptions.setSpeechModelEndpointId("customModelEndpointId");

        StepVerifier.create(
                callMedia.startRecognizingWithResponse(recognizeOptions))
            .consumeNextWith(response -> assertEquals(202, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void recognizeWithResponseTextSpeechOrDtmfOptions() {

        CallMediaRecognizeSpeechOrDtmfOptions recognizeOptions = new CallMediaRecognizeSpeechOrDtmfOptions(new CommunicationUserIdentifier("id"), 6, Duration.ofMillis(1000));

        recognizeOptions.setRecognizeInputType(RecognizeInputType.SPEECH_OR_DTMF);
        recognizeOptions.setPlayPrompt(new SsmlSource().setSsmlText("<speak version=\"1.0\" xmlns=\"http://www.w3.org/2001/10/synthesis\" xml:lang=\"en-US\"><voice name=\"en-US-JennyNeural\">No input recieved and recognition timed out, Disconnecting the call. Played through SSML. Thank you!</voice></speak>"));
        recognizeOptions.setInterruptCallMediaOperation(true);
        recognizeOptions.setStopCurrentOperations(true);
        recognizeOptions.setOperationContext("operationContext");
        recognizeOptions.setInterruptPrompt(true);
        recognizeOptions.setInitialSilenceTimeout(Duration.ofSeconds(4));
        recognizeOptions.setSpeechModelEndpointId("customModelEndpointId");

        StepVerifier.create(
                callMedia.startRecognizingWithResponse(recognizeOptions))
            .consumeNextWith(response -> assertEquals(202, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void startHoldMusicWithResponseTest() {

        callMedia = getMockCallMedia(200);
        StartHoldMusicOptions options = new StartHoldMusicOptions(
            new CommunicationUserIdentifier("id"),
            new TextSource().setText("audio to play"));
        StepVerifier.create(
                callMedia.startHoldMusicWithResponse(options))
            .consumeNextWith(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void stopHoldMusicWithResponseTest() {

        callMedia = getMockCallMedia(200);
        StepVerifier.create(
                callMedia.stopHoldMusicWithResponse(
                    new CommunicationUserIdentifier("id"),
                    "operationalContext"
                ))
            .consumeNextWith(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }
    
    @Test
    public void holdWithResponseTest() {

        callMedia = getMockCallMedia(200);
        HoldOptions options = new HoldOptions(
            new CommunicationUserIdentifier("id"))
            .setPlaySourceInfo(new TextSource().setText("audio to play"));
        StepVerifier.create(
                callMedia.holdWithResponse(options))
            .consumeNextWith(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void holdWithResponseNoPromptTest() {

        callMedia = getMockCallMedia(200);
        HoldOptions options = new HoldOptions(
            new CommunicationUserIdentifier("id"))
            .setPlaySourceInfo(new TextSource().setText("audio to play"));
        StepVerifier.create(
                callMedia.holdWithResponse(options))
            .consumeNextWith(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void unholdWithResponseTest() {

        callMedia = getMockCallMedia(200);
        StepVerifier.create(
                callMedia.unholdWithResponse(
                    new CommunicationUserIdentifier("id"),
                    "operationalContext"
                ))
            .consumeNextWith(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    private CallMediaAsync getMockCallMedia(int expectedStatusCode) {
        CallConnectionAsync callConnection =
            CallAutomationUnitTestBase.getCallConnectionAsync(new ArrayList<>(
                Collections.singletonList(new AbstractMap.SimpleEntry<>("", expectedStatusCode)))
            );
        return callConnection.getCallMediaAsync();
    }

    @Test
    public void startTranscriptionWithResponse() {
        callMedia = getMockCallMedia(202);
        StartTranscriptionOptions options = new StartTranscriptionOptions();
        options.setOperationContext("operationContext");
        options.setLocale("en-US");
        StepVerifier.create(
                callMedia.startTranscriptionWithResponseAsync(options))
            .consumeNextWith(response -> assertEquals(202, response.getStatusCode())
            )
            .verifyComplete();
    }
    @Test
    public void stopTranscriptionWithResponse() {
        callMedia = getMockCallMedia(202);
        StopTranscriptionOptions options = new StopTranscriptionOptions();
        options.setOperationContext("operationContext");
        StepVerifier.create(
                callMedia.stopTranscriptionWithResponseAsync(options)
            )
            .consumeNextWith(response -> assertEquals(202, response.getStatusCode()))
            .verifyComplete();
    }
    @Test
    public void updateTranscriptionWithResponse() {
        callMedia = getMockCallMedia(202);
        StepVerifier.create(
                callMedia.updateTranscription("en-US")
            ).verifyComplete();
    }

    @Test
    public void startMediaStreamingWithResponse() {
        callMedia = getMockCallMedia(202);
        StartMediaStreamingOptions options = new StartMediaStreamingOptions();
        options.setOperationCallbackUrl("https://localhost");
        options.setOperationContext("operationContext");
        StepVerifier.create(
                callMedia.startMediaStreamingWithResponse(options))
            .consumeNextWith(response -> assertEquals(202, response.getStatusCode())
            )
            .verifyComplete();
    }

    @Test
    public void stopMediaStreamingWithResponse() {
        callMedia = getMockCallMedia(202);
        StopMediaStreamingOptions options = new StopMediaStreamingOptions();
        options.setOperationCallbackUrl("https://localhost");
        StepVerifier.create(
                callMedia.stopMediaStreamingWithResponse(options))
            .consumeNextWith(response -> assertEquals(202, response.getStatusCode())
            )
            .verifyComplete();
    }
}
