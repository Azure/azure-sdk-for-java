// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callingserver.implementation;

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
import reactor.test.StepVerifier;

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
    static final RedirectPolicy REDIRECT_POLICY = new RedirectPolicy();
    private HttpRequest request;
    private HttpPipeline pipeline;

    @Mock
    HttpClient httpClient;

    @Mock
    HttpResponse response200;

    @Mock
    HttpResponse response302;

    @BeforeEach
    public void setup() throws MalformedURLException {
        MockitoAnnotations.openMocks(this);
        when(response200.getStatusCode()).thenReturn(200);
        when(response302.getStatusCode()).thenReturn(302);
        when(response302.getHeaderValue("Location")).thenReturn(REDIRECT_LOCATION);

        pipeline = new HttpPipelineBuilder()
            .httpClient(httpClient)
            .policies(REDIRECT_POLICY)
            .build();

        request = new HttpRequest(HttpMethod.GET, new URL(ORIGINAL_LOCATION));
    }

    @Test
    public void noRedirectionPerformedTest() {
        setSuccessMockResponse();

        verifyCorrectness(response200);
    }

    @Test
    public void redirectionPerformedTest() {
        setRedirectSuccessMockResponses();

        verifyCorrectness(response200);
    }

    @Test
    public void sameLocationUsedShortCircuitTest() {
        setRedirectRedirectMockResponse();

        verifyCorrectness(response302);
    }

    @Test
    public void sameLocationUsedInDifferentRequestsSuccessTest() {
        for (int i = 0; i < 3; i++) {
            setRedirectSuccessMockResponses();
            verifyCorrectness(response200);
        }
    }

    private void setSuccessMockResponse() {
        doAnswer(invocation -> {
            HttpRequest request = invocation.getArgument(0);
            assertThat(request.getUrl().toString(), is(equalTo(ORIGINAL_LOCATION)));
            return Mono.just(response200);
        }).when(httpClient).send(any(HttpRequest.class), any(Context.class));
    }

    private void setRedirectSuccessMockResponses() {
        doAnswer(invocation -> {
            HttpRequest request = invocation.getArgument(0);
            assertThat(request.getUrl().toString(), is(equalTo(ORIGINAL_LOCATION)));
            return Mono.just(response302);
        })
            .doAnswer(invocation -> {
                HttpRequest request = invocation.getArgument(0);
                assertThat(request.getUrl().toString(), is(equalTo(REDIRECT_LOCATION)));
                return Mono.just(response200);
            })
            .when(httpClient).send(any(HttpRequest.class), any(Context.class));
    }

    private void setRedirectRedirectMockResponse() {
        doAnswer(invocation -> Mono.just(response302))
            .doAnswer(invocation -> Mono.just(response302))
            .when(httpClient).send(any(HttpRequest.class), any(Context.class));
    }

    private void verifyCorrectness(HttpResponse expectedResponse) {
        StepVerifier.create(pipeline.send(request)).expectNext(expectedResponse).verifyComplete();
    }
}
