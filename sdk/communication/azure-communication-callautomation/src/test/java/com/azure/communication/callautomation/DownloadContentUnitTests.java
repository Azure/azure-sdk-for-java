// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.AbstractMap.SimpleEntry;
import java.util.Collections;

import com.azure.core.util.Context;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class DownloadContentUnitTests {

    private static final String CONTENT = "VideoContents";
    private static final String AMS_ENDPOINT = "https://url.com";
    private CallRecording callRecording;

    @BeforeEach
    public void setUp() {
        CallAutomationClient callAutomationClient = CallAutomationUnitTestBase.getCallAutomationClient(
            new ArrayList<>(
                Collections.singletonList(
                    new SimpleEntry<>(CallAutomationUnitTestBase.generateDownloadResult(CONTENT), 200)
                )));
        callRecording = callAutomationClient.getCallRecording();
    }

    @Test
    public void downloadTo() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        callRecording.downloadTo(AMS_ENDPOINT, stream);
        Reader reader = new InputStreamReader(new ByteArrayInputStream(stream.toByteArray()));
        BufferedReader b = new BufferedReader(reader);
        assertEquals(CONTENT, b.readLine());
    }

    @Test
    public void downloadToWithResponse() throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        callRecording.downloadTo(AMS_ENDPOINT, stream, null, Context.NONE);
        Reader reader = new InputStreamReader(new ByteArrayInputStream(stream.toByteArray()));
        BufferedReader b = new BufferedReader(reader);
        assertEquals(CONTENT, b.readLine());
    }
}
