// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.implementation.models.SendDtmfTonesResultInternal;
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
        playFileSource.setPlaySourceCacheId("playTextSourceId");
        playFileSource.setUrl("filePath");

        playTextSource = new TextSource();
        playTextSource.setPlaySourceCacheId("playTextSourceId");
        playTextSource.setVoiceKind(VoiceKind.MALE);
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
    public void playFileToAllWithBargeInWithResponseTest() {
        playToAllOptions = new PlayToAllOptions(playFileSource)
            .setLoop(false)
            .setInterruptCallMediaOperation(true)
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
    public void playTextToAllWithBargeInWithResponseTest() {
        playToAllOptions = new PlayToAllOptions(playTextSource)
            .setLoop(false)
            .setInterruptCallMediaOperation(true)
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
        RecognitionChoice recognizeChoice1 = new RecognitionChoice();
        RecognitionChoice recognizeChoice2 = new RecognitionChoice();
        List<RecognitionChoice> recognizeChoices = new ArrayList<>(
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
        ContinuousDtmfRecognitionOptions options = new ContinuousDtmfRecognitionOptions(new CommunicationUserIdentifier("id"));
        Response<Void> response = callMedia.startContinuousDtmfRecognitionWithResponse(options, Context.NONE);
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
        ContinuousDtmfRecognitionOptions options = new ContinuousDtmfRecognitionOptions(new CommunicationUserIdentifier("id"));
        Response<Void> response = callMedia.stopContinuousDtmfRecognitionWithResponse(options, Context.NONE
        );
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void sendDtmfTonesTest() {
        CallConnection callConnection =
            CallAutomationUnitTestBase.getCallConnection(new ArrayList<>(
                Collections.singletonList(new AbstractMap.SimpleEntry<>(
                    CallAutomationUnitTestBase.serializeObject(new SendDtmfTonesResultInternal().setOperationContext(CallAutomationUnitTestBase.CALL_OPERATION_CONTEXT)), 202)))
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
                    CallAutomationUnitTestBase.serializeObject(new SendDtmfTonesResultInternal().setOperationContext(CallAutomationUnitTestBase.CALL_OPERATION_CONTEXT)), 202)))
            );
        callMedia = callConnection.getCallMedia();
        List<DtmfTone> tones = Stream.of(DtmfTone.ONE, DtmfTone.TWO, DtmfTone.THREE).collect(Collectors.toList());
        SendDtmfTonesOptions options = new SendDtmfTonesOptions(tones, new CommunicationUserIdentifier("id"));
        options.setOperationContext("ctx");
        options.setOperationCallbackUrl(CallAutomationUnitTestBase.CALL_OPERATION_CONTEXT);
        Response<SendDtmfTonesResult> response = callMedia.sendDtmfTonesWithResponse(options, Context.NONE);
        assertEquals(response.getStatusCode(), 202);
    }

    @Test
    public void holdWithResponseTest() {
        CallConnection callConnection =
            CallAutomationUnitTestBase.getCallConnection(new ArrayList<>(
                Collections.singletonList(new AbstractMap.SimpleEntry<>("", 200)))
            );
        callMedia = callConnection.getCallMedia();
        HoldOptions options = new HoldOptions(new CommunicationUserIdentifier("id"))
            .setPlaySource(new TextSource().setText("audio to play"));
        Response<Void> response = callMedia.holdWithResponse(options, null);
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void holdWithResponseNoPromptTest() {
        CallConnection callConnection =
            CallAutomationUnitTestBase.getCallConnection(new ArrayList<>(
                Collections.singletonList(new AbstractMap.SimpleEntry<>("", 200)))
            );
        callMedia = callConnection.getCallMedia();
        HoldOptions options = new HoldOptions(
            new CommunicationUserIdentifier("id"));
        Response<Void> response = callMedia.holdWithResponse(options, null);
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void unholdWithResponseTest() {
        CallConnection callConnection =
            CallAutomationUnitTestBase.getCallConnection(new ArrayList<>(
                Collections.singletonList(new AbstractMap.SimpleEntry<>("", 200)))
            );
        callMedia = callConnection.getCallMedia();

        Response<Void> response = callMedia.unholdWithResponse(new CommunicationUserIdentifier("id"),
            "operationalContext", Context.NONE);
        assertEquals(response.getStatusCode(), 200);
    }

    @Test
    public void startTranscriptionWithResponseTest() {
        StartTranscriptionOptions options = new StartTranscriptionOptions();
        options.setOperationContext("operationContext");
        options.setLocale("en-US");
        Response<Void> response = callMedia.startTranscriptionWithResponse(options, Context.NONE);
        assertEquals(response.getStatusCode(), 202);
    }

    @Test
    public void stopTranscriptionWithResponseTest() {
        StopTranscriptionOptions options = new StopTranscriptionOptions();
        options.setOperationContext("operationContext");
        Response<Void> response = callMedia.stopTranscriptionWithResponse(options, Context.NONE);
        assertEquals(response.getStatusCode(), 202);
    }

    @Test
    public void updateTranscriptionSpeechModelWithResponseTest() {
        Response<Void> response = callMedia.updateTranscriptionWithResponse("en-US", "customEndpoint", null, Context.NONE);
        assertEquals(response.getStatusCode(), 202);
    }

    @Test
    public void updateTranscriptionOperationContextWithResponseTest() {
        Response<Void> response = callMedia.updateTranscriptionWithResponse("en-US", "customEndpoint", "unittestoperationcontext", Context.NONE);
        assertEquals(response.getStatusCode(), 202);
    }

    @Test
    public void updateTranscriptionWithResponse() {
        Response<Void> response = callMedia.updateTranscriptionWithResponse("en-US", null, null, Context.NONE);
        assertEquals(response.getStatusCode(), 202);
    }

    @Test
    public void startMediaStremaingWithResponse() {
        StartMediaStreamingOptions options = new StartMediaStreamingOptions();
        options.setOperationCallbackUrl("https://localhost");
        options.setOperationContext("operationContext");
        Response<Void> response = callMedia.startMediaStreamingWithResponse(options, Context.NONE);
        assertEquals(response.getStatusCode(), 202);
    }

    @Test
    public void stopMediaStremaingWithResponse() {
        StopMediaStreamingOptions options = new StopMediaStreamingOptions();
        options.setOperationCallbackUrl("https://localhost");
        Response<Void> response = callMedia.stopMediaStreamingWithResponse(options, Context.NONE);
        assertEquals(response.getStatusCode(), 202);
    }
}
