// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.PlayToAllOptions;
import com.azure.communication.callautomation.models.RecognizeChoice;
import com.azure.communication.callautomation.models.CallMediaRecognizeChoiceOptions;
import com.azure.communication.callautomation.models.CallMediaRecognizeDtmfOptions;
import com.azure.communication.callautomation.models.CallMediaRecognizeSpeechOptions;
import com.azure.communication.callautomation.models.CallMediaRecognizeSpeechOrDtmfOptions;
import com.azure.communication.callautomation.models.DtmfTone;
import com.azure.communication.callautomation.models.FileSource;
import com.azure.communication.callautomation.models.GenderType;
import com.azure.communication.callautomation.models.TextSource;
import com.azure.communication.callautomation.models.SsmlSource;
import com.azure.communication.callautomation.models.PlayOptions;
import com.azure.communication.callautomation.models.RecognizeInputType;
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
        CallConnectionAsync callConnection =
            CallAutomationUnitTestBase.getCallConnectionAsync(new ArrayList<>(
                Collections.singletonList(new AbstractMap.SimpleEntry<>("", 202)))
            );
        callMedia = callConnection.getCallMediaAsync();

        playFileSource = new FileSource();
        playFileSource.setPlaySourceId("playFileSourceId");
        playFileSource.setUrl("filePath");

        playTextSource = new TextSource();
        playTextSource.setPlaySourceId("playTextSourceId");
        playTextSource.setVoiceGender(GenderType.MALE);
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
        CallConnectionAsync callConnection =
            CallAutomationUnitTestBase.getCallConnectionAsync(new ArrayList<>(
                Collections.singletonList(new AbstractMap.SimpleEntry<>("", 200)))
            );
        callMedia = callConnection.getCallMediaAsync();
        StepVerifier.create(
                callMedia.startContinuousDtmfRecognitionWithResponse(new CommunicationUserIdentifier("id"),
                    "operationContext")
            )
            .consumeNextWith(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void stopContinuousDtmfRecognitionWithResponse() {
        // override callMedia to mock 200 response code
        CallConnectionAsync callConnection =
            CallAutomationUnitTestBase.getCallConnectionAsync(new ArrayList<>(
                Collections.singletonList(new AbstractMap.SimpleEntry<>("", 200)))
            );
        callMedia = callConnection.getCallMediaAsync();
        StepVerifier.create(
                callMedia.stopContinuousDtmfRecognitionWithResponse(new CommunicationUserIdentifier("id"),
                    "operationContext")
            )
            .consumeNextWith(response -> assertEquals(200, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void sendDtmfWithResponse() {
        StepVerifier.create(
                callMedia.sendDtmfWithResponse(
                        Stream.of(DtmfTone.ONE, DtmfTone.TWO, DtmfTone.THREE).collect(Collectors.toList()), new CommunicationUserIdentifier("id"),
                        "operationContext"
                )
            ).consumeNextWith(response -> assertEquals(202, response.getStatusCode()))
            .verifyComplete();
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

        RecognizeChoice recognizeChoice1 = new RecognizeChoice();
        RecognizeChoice recognizeChoice2 = new RecognizeChoice();
        recognizeChoice1.setTone(DtmfTone.ZERO);
        recognizeChoice2.setTone(DtmfTone.SIX);
        List<RecognizeChoice> recognizeChoices = new ArrayList<>(
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

        RecognizeChoice recognizeChoice1 = new RecognizeChoice();
        RecognizeChoice recognizeChoice2 = new RecognizeChoice();
        recognizeChoice1.setTone(DtmfTone.ZERO);
        recognizeChoice2.setTone(DtmfTone.THREE);
        List<RecognizeChoice> recognizeChoices = new ArrayList<>(
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
}
