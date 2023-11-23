// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.BinaryData;
import com.microsoft.aad.msal4j.HttpMethod;
import com.microsoft.aad.msal4j.HttpRequest;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

public class HttpPipelineAdapterTests {

    @Test
    public void testSendRequest() throws Exception {
        Mono<String> bodyResponse = Mono.just("dummy-body");
        HttpPipeline pipeline = Mockito.mock(HttpPipeline.class);
        HttpPipelineAdapter pipelineAdapter = new HttpPipelineAdapter(pipeline, new IdentityClientOptions());
        HttpRequest req = mockForSendRequest(bodyResponse, "dummy-body", pipeline);
        pipelineAdapter.send(req);
    }

    @Test
    public void testSendRequestEmptyBody() throws Exception {
        HttpPipeline pipeline = Mockito.mock(HttpPipeline.class);
        HttpPipelineAdapter pipelineAdapter = new HttpPipelineAdapter(pipeline, new IdentityClientOptions());
        HttpRequest req = mockForSendRequest(Mono.empty(), "", pipeline);
        pipelineAdapter.send(req);
    }

    private HttpRequest mockForSendRequest(Mono<String> bodyResponseMono, String bodyResponse, HttpPipeline pipeline) throws MalformedURLException {

        HttpRequest req = Mockito.mock(HttpRequest.class);
        HttpMethod method = HttpMethod.GET;
        when(req.httpMethod()).thenReturn(method);
        URL url = new URL("https://localhost.com/");
        when(req.url()).thenReturn(url);
        when(req.body()).thenReturn("");
        Map<String, String> a = new HashMap<>();
        when(req.headers()).thenReturn(a);
        HttpResponse coreResponse = Mockito.mock(HttpResponse.class);
        when(coreResponse.getBodyAsString()).thenReturn(bodyResponseMono);
        when(coreResponse.getBodyAsBinaryData()).thenReturn(BinaryData.fromString(bodyResponse));
        when(coreResponse.getHeaders()).thenReturn(new HttpHeaders(a));
        when(coreResponse.getStatusCode()).thenReturn(200);
        when(pipeline.sendSync(any(com.azure.core.http.HttpRequest.class), any())).thenReturn(coreResponse);
        return req;
    }
}
