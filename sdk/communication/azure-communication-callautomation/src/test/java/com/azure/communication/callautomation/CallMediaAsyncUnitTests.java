// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import com.azure.communication.callautomation.models.FileSource;
import com.azure.communication.callautomation.models.PlayOptions;
import com.azure.communication.common.CommunicationUserIdentifier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class CallMediaAsyncUnitTests {

    private CallMediaAsync callMedia;
    private FileSource playSource;

    private PlayOptions playOptions;

    @BeforeEach
    public void setup() {
        CallConnectionAsync callConnection =
            CallAutomationUnitTestBase.getCallConnectionAsync(new ArrayList<>(
                Collections.singletonList(new AbstractMap.SimpleEntry<>("", 202)))
            );
        callMedia = callConnection.getCallMediaAsync();

        playSource = new FileSource();
        playSource.setPlaySourceId("playSourceId");
        playSource.setUri("filePath");

        playOptions = new PlayOptions()
            .setLoop(false)
            .setOperationContext("operationContext");
    }

    @Test
    public void playFileWithResponseTest() {
        StepVerifier.create(
            callMedia.playWithResponse(playSource,
                Collections.singletonList(new CommunicationUserIdentifier("id")), playOptions))
            .consumeNextWith(response -> assertEquals(202, response.getStatusCode()))
            .verifyComplete();
    }

    @Test
    public void playFileToAllWithResponseTest() {
        StepVerifier.create(
                callMedia.playToAllWithResponse(playSource, playOptions))
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
}
