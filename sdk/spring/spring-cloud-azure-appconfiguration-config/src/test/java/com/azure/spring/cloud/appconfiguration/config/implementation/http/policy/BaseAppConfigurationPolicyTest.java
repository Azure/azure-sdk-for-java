// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.appconfiguration.config.implementation.http.policy;

import static com.azure.spring.cloud.appconfiguration.config.implementation.AppConfigurationConstants.KEY_VAULT_CONFIGURED_TRACING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.parallel.Execution;
import org.junit.jupiter.api.parallel.ExecutionMode;
import org.junit.jupiter.api.parallel.Isolated;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;
import com.azure.core.util.Configuration;
import com.azure.spring.cloud.appconfiguration.config.implementation.RequestTracingConstants;

// This test class needs to be isolated and ran sequential as it uses BaseAppConfigurationPolicy.setWatchRequests
// which mutates a global static and can result in race condition failures.
@Isolated
@Execution(ExecutionMode.SAME_THREAD)
@ExtendWith(MockitoExtension.class)
public class BaseAppConfigurationPolicyTest {

    private static final String PRE_USER_AGENT = "PreExistingUserAgent";

    @Mock
    HttpPipelineCallContext contextMock;

    @Mock
    HttpPipelineNextPolicy nextMock;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
    }

    @AfterEach
    public void cleanup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    @Test
    public void startupThenWatchUpdateTest() throws MalformedURLException, URISyntaxException {
        URL url = new URI("https://www.test.url/kv").toURL();
        HttpRequest request = new HttpRequest(HttpMethod.GET, url);
        request.setHeader(HttpHeaderName.USER_AGENT, "PreExistingUserAgent");
        BaseAppConfigurationPolicy policy = new BaseAppConfigurationPolicy(
            new TracingInfo(false, 0, Configuration.getGlobalConfiguration()));

        when(contextMock.getHttpRequest()).thenReturn(request);

        policy.process(contextMock, nextMock);

        String userAgent = contextMock.getHttpRequest().getHeaders().get(HttpHeaderName.USER_AGENT).getValue();
        assertEquals("null/null " + PRE_USER_AGENT, userAgent);

        assertEquals("RequestType=Startup",
            contextMock.getHttpRequest().getHeaders()
                .get(HttpHeaderName.fromString(RequestTracingConstants.CORRELATION_CONTEXT_HEADER.toString()))
                .getValue());

        request = new HttpRequest(HttpMethod.GET, url);
        request.setHeader(HttpHeaderName.USER_AGENT, "PreExistingUserAgent");

        when(contextMock.getHttpRequest()).thenReturn(request);
        when(contextMock.getData("refresh")).thenReturn(Optional.of(true));

        policy.process(contextMock, nextMock);

        assertEquals("null/null " + PRE_USER_AGENT, userAgent);

        assertEquals("RequestType=Watch",
            contextMock.getHttpRequest().getHeaders()
                .get(HttpHeaderName.fromString(RequestTracingConstants.CORRELATION_CONTEXT_HEADER.toString()))
                .getValue());

        request = new HttpRequest(HttpMethod.GET, url);
        request.setHeader(HttpHeaderName.USER_AGENT, "PreExistingUserAgent");

        when(contextMock.getHttpRequest()).thenReturn(request);

        policy.process(contextMock, nextMock);
        assertEquals("null/null " + PRE_USER_AGENT, userAgent);

        assertEquals("RequestType=Watch",
            contextMock.getHttpRequest().getHeaders()
                .get(HttpHeaderName.fromString(RequestTracingConstants.CORRELATION_CONTEXT_HEADER.toString()))
                .getValue());
    }

    @Test
    public void keyVaultIsConfigured() throws MalformedURLException, URISyntaxException {
        BaseAppConfigurationPolicy policy = new BaseAppConfigurationPolicy(
            new TracingInfo(true, 0, Configuration.getGlobalConfiguration()));

        URL url = new URI("https://www.test.url/kv").toURL();
        HttpRequest request = new HttpRequest(HttpMethod.GET, url);
        request.setHeader(HttpHeaderName.USER_AGENT, "PreExistingUserAgent");
        when(contextMock.getHttpRequest()).thenReturn(request);

        policy.process(contextMock, nextMock);
        assertEquals("RequestType=Startup," + KEY_VAULT_CONFIGURED_TRACING,
            contextMock.getHttpRequest().getHeaders()
                .get(HttpHeaderName.fromString(RequestTracingConstants.CORRELATION_CONTEXT_HEADER.toString()))
                .getValue());
    }

}
