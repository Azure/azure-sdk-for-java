// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.pipline.policies;

import static com.azure.spring.cloud.config.AppConfigurationConstants.CORRELATION_CONTEXT;
import static com.azure.spring.cloud.config.AppConfigurationConstants.DEV_ENV_TRACING;
import static com.azure.spring.cloud.config.AppConfigurationConstants.KEY_VAULT_CONFIGURED_TRACING;
import static com.azure.spring.cloud.config.AppConfigurationConstants.USER_AGENT_TYPE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;


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
        BaseAppConfigurationPolicy.setWatchRequests(false);
    }

    @AfterEach
    public void cleanup() throws Exception {
        MockitoAnnotations.openMocks(this).close();
    }

    @Test
    public void startupThenWatchUpdateTest() throws MalformedURLException {
        URL url = new URL("https://www.test.url/kv");
        HttpRequest request = new HttpRequest(HttpMethod.GET, url);
        request.setHeader(USER_AGENT_TYPE, "PreExistingUserAgent");
        BaseAppConfigurationPolicy policy = new BaseAppConfigurationPolicy(false, false);

        when(contextMock.getHttpRequest()).thenReturn(request);

        policy.process(contextMock, nextMock);

        String userAgent = contextMock.getHttpRequest().getHeaders().get(USER_AGENT_TYPE).getValue();
        assertEquals("null/null " + PRE_USER_AGENT, userAgent);

        assertEquals("RequestType=Startup",
            contextMock.getHttpRequest().getHeaders().get(CORRELATION_CONTEXT).getValue());

        request = new HttpRequest(HttpMethod.GET, url);
        request.setHeader(USER_AGENT_TYPE, "PreExistingUserAgent");

        when(contextMock.getHttpRequest()).thenReturn(request);
        BaseAppConfigurationPolicy.setWatchRequests(true);

        policy.process(contextMock, nextMock);

        assertEquals("null/null " + PRE_USER_AGENT, userAgent);

        assertEquals("RequestType=Watch",
            contextMock.getHttpRequest().getHeaders().get(CORRELATION_CONTEXT).getValue());

        request = new HttpRequest(HttpMethod.GET, url);
        request.setHeader(USER_AGENT_TYPE, "PreExistingUserAgent");

        when(contextMock.getHttpRequest()).thenReturn(request);

        policy.process(contextMock, nextMock);
        assertEquals("null/null " + PRE_USER_AGENT, userAgent);

        assertEquals("RequestType=Watch",
            contextMock.getHttpRequest().getHeaders().get(CORRELATION_CONTEXT).getValue());
    }

    @Test
    public void devIsConfigured() throws MalformedURLException {
        BaseAppConfigurationPolicy policy = new BaseAppConfigurationPolicy(true, false);

        URL url = new URL("https://www.test.url/kv");
        HttpRequest request = new HttpRequest(HttpMethod.GET, url);
        request.setHeader(USER_AGENT_TYPE, "PreExistingUserAgent");
        when(contextMock.getHttpRequest()).thenReturn(request);

        policy.process(contextMock, nextMock);
        assertEquals("RequestType=Startup,Env=" + DEV_ENV_TRACING,
            contextMock.getHttpRequest().getHeaders().get(CORRELATION_CONTEXT).getValue());
    }

    @Test
    public void keyVaultIsConfigured() throws MalformedURLException {
        BaseAppConfigurationPolicy policy = new BaseAppConfigurationPolicy(false, true);

        URL url = new URL("https://www.test.url/kv");
        HttpRequest request = new HttpRequest(HttpMethod.GET, url);
        request.setHeader(USER_AGENT_TYPE, "PreExistingUserAgent");
        when(contextMock.getHttpRequest()).thenReturn(request);

        policy.process(contextMock, nextMock);
        assertEquals("RequestType=Startup,Env=" + KEY_VAULT_CONFIGURED_TRACING,
            contextMock.getHttpRequest().getHeaders().get(CORRELATION_CONTEXT).getValue());
    }

    @Test
    public void devAndKeyVaultAreConfigured() throws MalformedURLException {
        BaseAppConfigurationPolicy policy = new BaseAppConfigurationPolicy(true, true);

        URL url = new URL("https://www.test.url/kv");
        HttpRequest request = new HttpRequest(HttpMethod.GET, url);
        request.setHeader(USER_AGENT_TYPE, "PreExistingUserAgent");
        when(contextMock.getHttpRequest()).thenReturn(request);

        policy.process(contextMock, nextMock);
        assertEquals("RequestType=Startup,Env=" + DEV_ENV_TRACING + "," + KEY_VAULT_CONFIGURED_TRACING,
            contextMock.getHttpRequest().getHeaders().get(CORRELATION_CONTEXT).getValue());
    }

}
