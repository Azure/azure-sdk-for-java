// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.internal.http.HttpClient;
import com.azure.data.cosmos.internal.http.HttpRequest;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.mockito.Mockito.doAnswer;

/**
 * This is a helper class for capturing requests sent over a httpClient.
 */
public class HttpClientUnderTestWrapper {

    final private HttpClient origHttpClient;
    final private HttpClient spyHttpClient;

    public final List<HttpRequest> capturedRequests = Collections.synchronizedList(new ArrayList<>());

    public HttpClientUnderTestWrapper(HttpClient origHttpClient) {
        this.origHttpClient = origHttpClient;
        this.spyHttpClient = Mockito.spy(origHttpClient);
        initRequestCapture(spyHttpClient);
    }

    public HttpClient getSpyHttpClient() {
        return spyHttpClient;
    }

    private void initRequestCapture(HttpClient spyClient) {
        doAnswer(invocationOnMock -> {
            HttpRequest httpRequest = invocationOnMock.getArgumentAt(0, HttpRequest.class);
            capturedRequests.add(httpRequest);
            return origHttpClient.send(httpRequest);
        }).when(spyClient).send(Mockito.any(HttpRequest.class));
    }
}
