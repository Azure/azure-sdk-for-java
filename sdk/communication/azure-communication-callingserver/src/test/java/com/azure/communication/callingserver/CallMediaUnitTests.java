// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;

import com.azure.communication.callingserver.models.FileSource;
import com.azure.communication.callingserver.models.PlayOptions;
import com.azure.communication.callingserver.models.RecognizeConfigurations;
import com.azure.communication.callingserver.models.RecognizeInputType;
import com.azure.communication.callingserver.models.RecognizeOptions;
import com.azure.communication.common.CommunicationUserIdentifier;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CallMediaUnitTests {

    private CallMedia callMedia;
    private FileSource playSource;
    private PlayOptions playOptions;

    @BeforeEach
    public void setup() {
        CallConnection callConnection =
            CallAutomationUnitTestBase.getCallConnection(new ArrayList<>(
                Collections.singletonList(new AbstractMap.SimpleEntry<>("", 202)))
            );
        callMedia = callConnection.getCallMedia();

        playSource = new FileSource();
        playSource.setPlaySourceId("playSourceId");
        playSource.setUri("filePath");

        playOptions = new PlayOptions()
            .setLoop(false)
            .setOperationContext("operationContext");
    }

    @Test
    @Disabled("Disabling test as calling sever is in the process of decommissioning")
    public void playFileWithResponseTest() {
        Response<Void> response = callMedia.playWithResponse(playSource,
            Collections.singletonList(new CommunicationUserIdentifier("id")), playOptions, Context.NONE);
        assertEquals(response.getStatusCode(), 202);
    }

    @Test
    @Disabled("Disabling test as calling sever is in the process of decommissioning")
    public void playFileToAllWithResponseTest() {
        Response<Void> response = callMedia.playToAllWithResponse(playSource, playOptions, Context.NONE);
        assertEquals(response.getStatusCode(), 202);
    }

    @Test
    @Disabled("Disabling test as calling sever is in the process of decommissioning")
    public void cancelAllOperationsWithResponse() {
        Response<Void> response = callMedia.cancelAllMediaOperationsWithResponse(Context.NONE);
        assertEquals(response.getStatusCode(), 202);
    }

    @Test
    @Disabled("Disabling test as calling sever is in the process of decommissioning")
    public void recognizeWithResponseTest() {
        RecognizeOptions recognizeOptions = new RecognizeOptions(RecognizeInputType.DTMF, new RecognizeConfigurations());
        Response<Void> response = callMedia.recognizeWithResponse(recognizeOptions, Context.NONE);
        assertEquals(response.getStatusCode(), 202);
    }
}
