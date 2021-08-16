// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver;


import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.AbstractMap.SimpleEntry;

import com.azure.core.http.HttpRange;
import com.azure.core.http.rest.Response;
import com.azure.core.util.Context;

import org.junit.jupiter.api.Test;

public class DownloadContentUnitTests {
    @Test
    public void downloadTo() throws IOException {
        CallingServerClient callingServerClient = CallingServerResponseMocker.getCallingServerClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(CallingServerResponseMocker.generateDownloadResult("VideoContents"), 200)
            )));
        
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        callingServerClient.downloadTo("https://url.com", stream, new HttpRange(1));
        Reader reader = new InputStreamReader(new ByteArrayInputStream(stream.toByteArray()));
        BufferedReader b = new BufferedReader(reader);
        assertEquals("VideoContents", b.readLine());
    }
    
    @Test
    public void downloadToWithResponse() throws IOException {
        CallingServerClient callingServerClient = CallingServerResponseMocker.getCallingServerClient(new ArrayList<SimpleEntry<String, Integer>>(
            Arrays.asList(
                new SimpleEntry<String, Integer>(CallingServerResponseMocker.generateDownloadResult("VideoContents"), 200)
            )));
        
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        Response<Void> response = callingServerClient.downloadToWithResponse("https://url.com", stream, new HttpRange(1), Context.NONE);
        assertEquals(200, response.getStatusCode());
        Reader reader = new InputStreamReader(new ByteArrayInputStream(stream.toByteArray()));
        BufferedReader b = new BufferedReader(reader);
        assertEquals("VideoContents", b.readLine());
    }       
}
