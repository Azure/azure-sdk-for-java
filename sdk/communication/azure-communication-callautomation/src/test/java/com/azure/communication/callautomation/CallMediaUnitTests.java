// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.CallMediaRecognizeDtmfOptions;
import com.azure.communication.callautomation.models.FileSource;
import com.azure.communication.callautomation.models.PlayOptions;
import com.azure.communication.callautomation.models.PlayToAllOptions;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CallMediaUnitTests {

    private CallMedia callMedia;
    private FileSource playFileSource;
    private PlayOptions playOptions;
    private PlayToAllOptions playToAllOptions;

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

}
