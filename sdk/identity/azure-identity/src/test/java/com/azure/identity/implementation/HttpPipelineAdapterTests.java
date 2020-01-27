// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.implementation;

import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpResponse;
import com.microsoft.aad.msal4j.HttpMethod;
import com.microsoft.aad.msal4j.HttpRequest;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@RunWith(PowerMockRunner.class)
@PowerMockIgnore({"com.sun.org.apache.xerces.*", "javax.xml.*", "org.xml.*"})
@PrepareForTest({HttpPipeline.class, HttpRequest.class, HttpResponse.class, URL.class})
public class HttpPipelineAdapterTests {

    @Test
    public void testSendRequest() throws Exception {
        Mono<String> bodyResponse = Mono.just("dummy-body");
        HttpPipeline pipeline = PowerMockito.mock(HttpPipeline.class);
        HttpPipelineAdapter pipelineAdapter = new HttpPipelineAdapter(pipeline);
        HttpRequest req = mockForSendRequest(bodyResponse, pipeline);
        pipelineAdapter.send(req);
    }

    @Test
    public void testSendRequestEmptyBody() throws Exception {
        HttpPipeline pipeline = PowerMockito.mock(HttpPipeline.class);
        HttpPipelineAdapter pipelineAdapter = new HttpPipelineAdapter(pipeline);
        HttpRequest req = mockForSendRequest(Mono.empty(), pipeline);
        pipelineAdapter.send(req);
    }

    private HttpRequest mockForSendRequest(Mono<String> bodyResponse, HttpPipeline pipeline) throws MalformedURLException {
        HttpRequest req = PowerMockito.mock(HttpRequest.class);
        HttpMethod method = HttpMethod.GET;
        when(req.httpMethod()).thenReturn(method);
        URL url = new URL("https://localhost.com/");
        when(req.url()).thenReturn(url);
        when(req.body()).thenReturn("");
        Map<String, String> a = new HashMap<>();
        when(req.headers()).thenReturn(a);
        HttpResponse coreResponse = PowerMockito.mock(HttpResponse.class);
        when(coreResponse.getBodyAsString()).thenReturn(bodyResponse);
        when(coreResponse.getHeaders()).thenReturn(new HttpHeaders(a));
        when(coreResponse.getStatusCode()).thenReturn(200);
        when(pipeline.send(any(com.azure.core.http.HttpRequest.class))).thenReturn(Mono.just(coreResponse));
        return req;
    }
}
