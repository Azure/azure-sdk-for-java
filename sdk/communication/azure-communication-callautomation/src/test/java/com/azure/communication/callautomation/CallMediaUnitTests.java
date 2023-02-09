// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.implementation.models.RecognizeChoice;
import com.azure.communication.callautomation.models.CallMediaRecognizeDtmfOptions;
import com.azure.communication.callautomation.models.CallMediaRecognizeChoiceOptions;
import com.azure.communication.callautomation.models.FileSource;
import com.azure.communication.callautomation.models.GenderType;
import com.azure.communication.callautomation.models.PlayOptions;
import com.azure.communication.callautomation.models.TextSource;
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

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CallMediaUnitTests {

    private CallMedia callMedia;
    private FileSource playFileSource;
    private PlayOptions playOptions;
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
        Response<Void> response = callMedia.playWithResponse(playFileSource,
            Collections.singletonList(new CommunicationUserIdentifier("id")), playOptions, Context.NONE);
        assertEquals(response.getStatusCode(), 202);
    }

    @Test
    public void playFileToAllWithResponseTest() {
        Response<Void> response = callMedia.playToAllWithResponse(playFileSource, playOptions, Context.NONE);
        assertEquals(response.getStatusCode(), 202);
    }

    @Test
    public void playTextWithResponseTest() {
        Response<Void> response = callMedia.playWithResponse(playTextSource,
            Collections.singletonList(new CommunicationUserIdentifier("id")), playOptions, Context.NONE);
        assertEquals(response.getStatusCode(), 202);
    }

    @Test
    public void playTextToAllWithResponseTest() {
        Response<Void> response = callMedia.playToAllWithResponse(playTextSource, playOptions, Context.NONE);
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
        List<RecognizeChoice> recognizeChoices = new ArrayList<RecognizeChoice>(
            Arrays.asList(recognizeChoice1, recognizeChoice2)
        );
        CallMediaRecognizeChoiceOptions callMediaRecognizeOptions = new CallMediaRecognizeChoiceOptions(new CommunicationUserIdentifier("id"), recognizeChoices);
        Response<Void> response = callMedia.startRecognizingWithResponse(callMediaRecognizeOptions, Context.NONE);
        assertEquals(response.getStatusCode(), 202);
    }
}
