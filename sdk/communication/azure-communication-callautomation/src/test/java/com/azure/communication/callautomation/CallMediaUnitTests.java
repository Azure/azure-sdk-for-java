// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.implementation.models.SendDtmfTonesResultInternal;
import com.azure.communication.callautomation.models.CallMediaRecognizeChoiceOptions;
import com.azure.communication.callautomation.models.CallMediaRecognizeDtmfOptions;
import com.azure.communication.callautomation.models.ContinuousDtmfRecognitionOptions;
import com.azure.communication.callautomation.models.DtmfTone;
import com.azure.communication.callautomation.models.FileSource;
import com.azure.communication.callautomation.models.SendDtmfTonesOptions;
import com.azure.communication.callautomation.models.VoiceKind;
import com.azure.communication.callautomation.models.PlayOptions;
import com.azure.communication.callautomation.models.PlayToAllOptions;
import com.azure.communication.callautomation.models.RecognitionChoice;
import com.azure.communication.callautomation.models.TextSource;
import com.azure.communication.callautomation.models.SendDtmfTonesResult;
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

import static com.azure.communication.callautomation.CallAutomationUnitTestBase.CALL_OPERATION_CONTEXT;
import static com.azure.communication.callautomation.CallAutomationUnitTestBase.OPERATION_CALLBACK_URL;
import static com.azure.communication.callautomation.CallAutomationUnitTestBase.serializeObject;
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
        playFileSource.setPlaySourceCacheId("playTextSourceCacheId");
        playFileSource.setUrl("filePath");

        playTextSource = new TextSource();
        playTextSource.setPlaySourceCacheId("playTextSourceCacheId");
        playTextSource.setVoiceKind(VoiceKind.MALE);
        playTextSource.setSourceLocale("en-US");
        playTextSource.setVoiceName("LULU");
    }

    @Test
    public void playFileWithResponseTest() {
        playOptions = new PlayOptions(playFileSource, Collections.singletonList(new CommunicationUserIdentifier("id")))
            .setLoop(false)
            .setOperationContext(CALL_OPERATION_CONTEXT);
        Response<Void> response = callMedia.playWithResponse(playOptions, Context.NONE);
        assertEquals(response.getStatusCode(), 202);
    }

    @Test
    public void playFileToAllWithResponseTest() {
        playToAllOptions = new PlayToAllOptions(playFileSource)
            .setLoop(false)
            .setOperationContext(CALL_OPERATION_CONTEXT);
        Response<Void> response = callMedia.playToAllWithResponse(playToAllOptions, Context.NONE);
        assertEquals(response.getStatusCode(), 202);
    }

    @Test
    public void playTextWithResponseTest() {
        playOptions = new PlayOptions(playTextSource, Collections.singletonList(new CommunicationUserIdentifier("id")))
            .setLoop(false)
            .setOperationContext(CALL_OPERATION_CONTEXT);
        Response<Void> response = callMedia.playWithResponse(playOptions, Context.NONE);
        assertEquals(response.getStatusCode(), 202);
    }

    @Test
    public void playTextToAllWithResponseTest() {
        playToAllOptions = new PlayToAllOptions(playTextSource)
            .setLoop(false)
            .setOperationContext(CALL_OPERATION_CONTEXT);
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
        RecognitionChoice recognitionChoice1 = new RecognitionChoice();
        RecognitionChoice recognitionChoice2 = new RecognitionChoice();
        List<RecognitionChoice> recognitionChoices = new ArrayList<>(
            Arrays.asList(recognitionChoice1, recognitionChoice2)
        );
        CallMediaRecognizeChoiceOptions callMediaRecognizeOptions = new CallMediaRecognizeChoiceOptions(new CommunicationUserIdentifier("id"), recognitionChoices);
        Response<Void> response = callMedia.startRecognizingWithResponse(callMediaRecognizeOptions, Context.NONE);
        assertEquals(response.getStatusCode(), 202);
    }

    @Test
    public void startContinuousDtmfRecognitionTest() {
        // override callMedia to mock 200 response code
        CallConnection callConnection =
            CallAutomationUnitTestBase.getCallConnection(new ArrayList<>(
                Collections.singletonList(new AbstractMap.SimpleEntry<>("", 200)))
            );
        //expect no exception
        callConnection.getCallMedia().startContinuousDtmfRecognition(new CommunicationUserIdentifier("id"));
    }

    @Test
    public void startContinuousDtmfRecognitionWithResponseTest() {
        // override callMedia to mock 200 response code
        CallConnection callConnection =
            CallAutomationUnitTestBase.getCallConnection(new ArrayList<>(
                Collections.singletonList(new AbstractMap.SimpleEntry<>("", 200)))
            );
        callMedia = callConnection.getCallMedia();
        ContinuousDtmfRecognitionOptions options = new ContinuousDtmfRecognitionOptions(new CommunicationUserIdentifier("id"));
        options.setOperationContext(CALL_OPERATION_CONTEXT);
        Response<Void> response = callMedia.startContinuousDtmfRecognitionWithResponse(options, Context.NONE);
        assertEquals(response.getStatusCode(), 200);
    }


    @Test
    public void stopContinuousDtmfRecognitionTest() {
        // override callMedia to mock 200 response code
        CallConnection callConnection =
            CallAutomationUnitTestBase.getCallConnection(new ArrayList<>(
                Collections.singletonList(new AbstractMap.SimpleEntry<>("", 200)))
            );
        //expect no exception
        callConnection.getCallMedia().stopContinuousDtmfRecognition(new CommunicationUserIdentifier("id"));
    }

    @Test
    public void stopContinuousDtmfRecognitionWithResponseTest() {
        // override callMedia to mock 200 response code
        CallConnection callConnection =
            CallAutomationUnitTestBase.getCallConnection(new ArrayList<>(
                Collections.singletonList(new AbstractMap.SimpleEntry<>("", 200)))
            );
        callMedia = callConnection.getCallMedia();
        ContinuousDtmfRecognitionOptions options = new ContinuousDtmfRecognitionOptions(new CommunicationUserIdentifier("id"));
        options.setOperationContext(CALL_OPERATION_CONTEXT);
        options.setOperationCallbackUrl(OPERATION_CALLBACK_URL);
        Response<Void> response = callMedia.stopContinuousDtmfRecognitionWithResponse(options, Context.NONE);
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void sendDtmfTonesTest() {
        CallConnection callConnection =
            CallAutomationUnitTestBase.getCallConnection(new ArrayList<>(
                Collections.singletonList(new AbstractMap.SimpleEntry<>(
                    serializeObject(new SendDtmfTonesResultInternal().setOperationContext(CALL_OPERATION_CONTEXT)), 202)))
            );
        //expect no exception
        callConnection.getCallMedia().sendDtmfTones(
                Stream.of(DtmfTone.ONE, DtmfTone.TWO, DtmfTone.THREE).collect(Collectors.toList()),
                new CommunicationUserIdentifier("id")
        );
    }

    @Test
    public void sendDtmfTonesWithResponseTest() {
        CallConnection callConnection =
            CallAutomationUnitTestBase.getCallConnection(new ArrayList<>(
                Collections.singletonList(new AbstractMap.SimpleEntry<>(
                    serializeObject(new SendDtmfTonesResultInternal().setOperationContext(CALL_OPERATION_CONTEXT)), 202)))
            );
        callMedia = callConnection.getCallMedia();
        List<DtmfTone> tones = Stream.of(DtmfTone.ONE, DtmfTone.TWO, DtmfTone.THREE).collect(Collectors.toList());
        SendDtmfTonesOptions options = new SendDtmfTonesOptions(tones, new CommunicationUserIdentifier("id"));
        options.setOperationContext("ctx");
        options.setOperationCallbackUrl(OPERATION_CALLBACK_URL);
        Response<SendDtmfTonesResult> response = callMedia.sendDtmfTonesWithResponse(options, Context.NONE);
        assertEquals(response.getStatusCode(), 202);
    }

}
