// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.RecognizeChoice;
import com.azure.communication.callautomation.models.CallMediaRecognizeChoiceOptions;
import com.azure.communication.callautomation.models.CallMediaRecognizeDtmfOptions;
import com.azure.communication.callautomation.models.CallMediaRecognizeSpeechOptions;
import com.azure.communication.callautomation.models.DtmfTone;
import com.azure.communication.callautomation.models.FileSource;
import com.azure.communication.callautomation.models.GenderType;
import com.azure.communication.callautomation.models.TextSource;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CallMediaAsyncUnitTests {

    private CallMediaAsync callMedia;
    private FileSource playFileSource;
    private TextSource playTextSource;


    private PlayOptions playOptions;

    @BeforeEach
    public void setup() {
        CallConnectionAsync callConnection =
            CallAutomationUnitTestBase.getCallConnectionAsync(new ArrayList<>(
                Collections.singletonList(new AbstractMap.SimpleEntry<>("", 202)))
            );
        callMedia = callConnection.getCallMediaAsync();

        playFileSource = new FileSource();
        playFileSource.setPlaySourceId("playFileSourceId");
        playFileSource.setUri("filePath");

        playTextSource = new TextSource();
        playTextSource.setPlaySourceId("playTextSourceId");
        playTextSource.setVoiceGender(GenderType.MALE);
        playTextSource.setSourceLocale("en-US");
        playTextSource.setVoiceName("LULU");

        playOptions = new PlayOptions()
            .setLoop(false)
            .setOperationContext("operationContext");
    }

    @Test
    public void playFileWithResponseTest() {
        StepVerifier.create(
            callMedia.playWithResponse(playFileSource,
                Collections.singletonList(new CommunicationUserIdentifier("id")), playOptions))
            .consumeNextWith(response -> assertEquals(202, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void playFileToAllWithResponseTest() {
        StepVerifier.create(
                callMedia.playToAllWithResponse(playFileSource, playOptions))
            .consumeNextWith(response -> assertEquals(202, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void playTextWithResponseTest() {
        StepVerifier.create(
            callMedia.playWithResponse(playTextSource,
                Collections.singletonList(new CommunicationUserIdentifier("id")), playOptions))
            .consumeNextWith(response -> assertEquals(202, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void playTextToAllWithResponseTest() {
        StepVerifier.create(
                callMedia.playToAllWithResponse(playTextSource, playOptions))
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
        List<DtmfTone> stopDtmfTones = new ArrayList<DtmfTone>();
        stopDtmfTones.add(DtmfTone.ZERO);
        stopDtmfTones.add(DtmfTone.ONE);
        stopDtmfTones.add(DtmfTone.TWO);
        recognizeOptions.setStopTones(stopDtmfTones);
        recognizeOptions.setRecognizeInputType(RecognizeInputType.DTMF);
        recognizeOptions.setPlayPrompt(new FileSource().setUri("abc"));
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
    public void recognizeWithResponseWithTextSourceDtmfOptions() {
        CallMediaRecognizeDtmfOptions recognizeOptions = new CallMediaRecognizeDtmfOptions(new CommunicationUserIdentifier("id"), 5);

        recognizeOptions.setInterToneTimeout(Duration.ofSeconds(3));
        List<DtmfTone> stopDtmfTones = new ArrayList<DtmfTone>();
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
        List<RecognizeChoice> recognizeChoices = new ArrayList<RecognizeChoice>(
            Arrays.asList(recognizeChoice1, recognizeChoice2)
        );
        CallMediaRecognizeChoiceOptions recognizeOptions = new CallMediaRecognizeChoiceOptions(new CommunicationUserIdentifier("id"), recognizeChoices);

        recognizeOptions.setRecognizeInputType(RecognizeInputType.CHOICES);
        recognizeOptions.setPlayPrompt(new FileSource().setUri("abc"));
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
    public void recognizeWithResponseTextChoiceOptions() {

        RecognizeChoice recognizeChoice1 = new RecognizeChoice();
        RecognizeChoice recognizeChoice2 = new RecognizeChoice();
        recognizeChoice1.setTone(DtmfTone.ZERO);
        recognizeChoice2.setTone(DtmfTone.THREE);
        List<RecognizeChoice> recognizeChoices = new ArrayList<RecognizeChoice>(
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

        CallMediaRecognizeSpeechOptions recognizeOptions = new CallMediaRecognizeSpeechOptions(RecognizeInputType.SPEECH, new CommunicationUserIdentifier("id"), Long.valueOf(500));

        recognizeOptions.setRecognizeInputType(RecognizeInputType.SPEECH);
        recognizeOptions.setPlayPrompt(new TextSource().setText("Test recognize choice with text source."));
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
}
