// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.common.implementation;

import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.HttpPipelineBuilder;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.HttpResponse;
import com.azure.core.util.Context;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;

import java.net.MalformedURLException;
import java.net.URL;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.CoreMatchers.equalTo;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.when;

public class RedirectPolicyTests {
    static final String ORIGINAL_LOCATION = "https://localhost.com";
    static final String REDIRECT_LOCATION = "https://localhost-2.com";

    @Mock
    HttpClient httpClient;

    @Mock
    HttpResponse response200;

    @Mock
    HttpResponse response302;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        when(response200.getStatusCode()).thenReturn(200);
        when(response302.getStatusCode()).thenReturn(302);
        when(response302.getHeaderValue("Location")).thenReturn(REDIRECT_LOCATION);
    }

    @Test
    public void noRedirectionPerformedTest() throws MalformedURLException {
        doAnswer(invocation -> {
            HttpRequest request = invocation.getArgument(0);
            assertThat(request.getUrl().toString(), is(equalTo(ORIGINAL_LOCATION)));
            return Mono.just(response200);
        }).when(httpClient).send(any(HttpRequest.class), any(Context.class));

        final RedirectPolicy redirectPolicy = new RedirectPolicy();

        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(redirectPolicy)
            .build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, new URL(ORIGINAL_LOCATION));
        Mono<HttpResponse> httpResponse = pipeline.send(request);
        assertThat(httpResponse.block(), is(equalTo(response200)));
    }

    @Test
    public void redirectionPerformedTest() throws MalformedURLException {
        doAnswer(invocation -> {
            HttpRequest request = invocation.getArgument(0);
            assertThat(request.getUrl().toString(), is(equalTo(ORIGINAL_LOCATION)));
            return Mono.just(response302);
        })
        .doAnswer(invocation -> {
            HttpRequest request = invocation.getArgument(0);
            assertThat(request.getUrl().toString(), is(equalTo(REDIRECT_LOCATION)));
            return Mono.just(response200);
        }).when(httpClient).send(any(HttpRequest.class), any(Context.class));

        final RedirectPolicy redirectPolicy = new RedirectPolicy();

        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(redirectPolicy)
            .build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, new URL(ORIGINAL_LOCATION));
        Mono<HttpResponse> httpResponse = pipeline.send(request);
        assertThat(httpResponse.block(), is(equalTo(response200)));
    }

    @Test
    public void sameLocationUsedShortCircuitTest() throws MalformedURLException {
        doAnswer(invocation -> Mono.just(response302))
            .doAnswer(invocation -> Mono.just(response302))
            .when(httpClient).send(any(HttpRequest.class), any(Context.class));

        final RedirectPolicy redirectPolicy = new RedirectPolicy();

        final HttpPipeline pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(redirectPolicy)
            .build();

        HttpRequest request = new HttpRequest(HttpMethod.GET, new URL(ORIGINAL_LOCATION));
        Mono<HttpResponse> httpResponse = pipeline.send(request);
        assertThat(httpResponse.block(), is(equalTo(response302)));
    }
}
