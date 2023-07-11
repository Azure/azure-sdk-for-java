// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.*;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CallMediaUnitTests {

    private CallMedia callMedia;
    private FileSource playFileSource;
    private PlayOptions playOptions;
    private PlayToAllOptions playToAllOptions;
    private TextSource playTextSource;

    @BeforeEach
    public void setup() {
        CallConnection callConnection =
            CallAutomationUnitTestBase.getCallConnection(new ArrayList<>(
                Collections.singletonList(new AbstractMap.SimpleEntry<>("", 202)))
            );
        callMedia = callConnection.getCallMedia();

        playFileSource = new FileSource();
        playFileSource.setPlaySourceId("playTextSourceId");
        playFileSource.setUrl("filePath");

        playTextSource = new TextSource();
        playTextSource.setPlaySourceId("playTextSourceId");
        playTextSource.setVoiceGender(GenderType.MALE);
        playTextSource.setSourceLocale("en-US");
        playTextSource.setVoiceName("LULU");
        playTextSource.setCustomVoiceEndpointId("customVoiceEndpointId");
    }

    @Test
    public void playFileWithResponseTest() {
        playOptions = new PlayOptions(playFileSource, Collections.singletonList(new CommunicationUserIdentifier("id")))
            .setLoop(false)
            .setOperationContext("operationContext");
        Response<Void> response = callMedia.playWithResponse(playOptions, Context.NONE);
        assertEquals(response.getStatusCode(), 202);
    }

    @Test
    public void playFileToAllWithResponseTest() {
        playToAllOptions = new PlayToAllOptions(playFileSource)
            .setLoop(false)
            .setOperationContext("operationContext");
        Response<Void> response = callMedia.playToAllWithResponse(playToAllOptions, Context.NONE);
        assertEquals(response.getStatusCode(), 202);
    }

    @Test
    public void playTextWithResponseTest() {
        playOptions = new PlayOptions(playTextSource, Collections.singletonList(new CommunicationUserIdentifier("id")))
            .setLoop(false)
            .setOperationContext("operationContext");
        Response<Void> response = callMedia.playWithResponse(playOptions, Context.NONE);
        assertEquals(response.getStatusCode(), 202);
    }

    @Test
    public void playTextToAllWithResponseTest() {
        playToAllOptions = new PlayToAllOptions(playTextSource)
            .setLoop(false)
            .setOperationContext("operationContext");
        Response<Void> response = callMedia.playToAllWithResponse(playToAllOptions, Context.NONE);
        assertEquals(response.getStatusCode(), 202);
    }

    @Test
    public void cancelAllOperationsWithResponse() {
        Response<Void> response = callMedia.cancelAllMediaOperationsWithResponse(Context.NONE);
        assertEquals(response.getStatusCode(), 202);
    }

    @Test
    public void recognizeWithDtmfResponseTest() {
        CallMediaRecognizeDtmfOptions callMediaRecognizeOptions = new CallMediaRecognizeDtmfOptions(new CommunicationUserIdentifier("id"), 5);
        Response<Void> response = callMedia.startRecognizingWithResponse(callMediaRecognizeOptions, Context.NONE);
        assertEquals(response.getStatusCode(), 202);
    }

    @Test
    public void recognizeWithChoiceResponseTest() {
        RecognizeChoice recognizeChoice1 = new RecognizeChoice();
        RecognizeChoice recognizeChoice2 = new RecognizeChoice();
        List<RecognizeChoice> recognizeChoices = new ArrayList<>(
            Arrays.asList(recognizeChoice1, recognizeChoice2)
        );
        CallMediaRecognizeChoiceOptions callMediaRecognizeOptions = new CallMediaRecognizeChoiceOptions(new CommunicationUserIdentifier("id"), recognizeChoices);
        Response<Void> response = callMedia.startRecognizingWithResponse(callMediaRecognizeOptions, Context.NONE);
        assertEquals(response.getStatusCode(), 202);
    }

    @Test
    public void startContinuousDtmfRecognitionWithResponseTest() {
        // override callMedia to mock 200 response code
        CallConnection callConnection =
            CallAutomationUnitTestBase.getCallConnection(new ArrayList<>(
                Collections.singletonList(new AbstractMap.SimpleEntry<>("", 200)))
            );
        callMedia = callConnection.getCallMedia();
        Response<Void> response = callMedia.startContinuousDtmfRecognitionWithResponse(
            new CommunicationUserIdentifier("id"),
            "operationContext", Context.NONE
        );
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void stopContinuousDtmfRecognitionWithResponseTest() {
        // override callMedia to mock 200 response code
        CallConnection callConnection =
            CallAutomationUnitTestBase.getCallConnection(new ArrayList<>(
                Collections.singletonList(new AbstractMap.SimpleEntry<>("", 200)))
            );
        callMedia = callConnection.getCallMedia();
        Response<Void> response = callMedia.stopContinuousDtmfRecognitionWithResponse(
            new CommunicationUserIdentifier("id"),
            "operationContext", Context.NONE
        );
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void sendDtmfWithResponseTest() {
        Response<Void> response = callMedia.sendDtmfWithResponse(
            Stream.of(DtmfTone.ONE, DtmfTone.TWO, DtmfTone.THREE).collect(Collectors.toList()), new CommunicationUserIdentifier("id"),
            "ctx", Context.NONE
        );
        assertEquals(response.getStatusCode(), 202);
    }

}
