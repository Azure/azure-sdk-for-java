// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.pipline.policies;

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
    
    public static final String USER_AGENT_TYPE = "User-Agent";
    
    @Mock
    HttpPipelineCallContext contextMock;
    @Mock
    HttpPipelineNextPolicy nextMock;

    @BeforeEach
    public void setup() {
        MockitoAnnotations.openMocks(this);
        BaseAppConfigurationPolicy.setIsDev(false);
        BaseAppConfigurationPolicy.setIsKeyVaultConfigured(false);
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
        BaseAppConfigurationPolicy policy = new BaseAppConfigurationPolicy();

        when(contextMock.getHttpRequest()).thenReturn(request);

        policy.process(contextMock, nextMock);

        String userAgent = contextMock.getHttpRequest().getHeaders().get(USER_AGENT_TYPE).getValue();
        assertEquals("null/null " + PRE_USER_AGENT, userAgent);

        assertEquals("RequestType=Startup",
            contextMock.getHttpRequest().getHeaders().get("Correlation-Context").getValue());

        request = new HttpRequest(HttpMethod.GET, url);
        request.setHeader(USER_AGENT_TYPE, "PreExistingUserAgent");

        when(contextMock.getHttpRequest()).thenReturn(request);
        BaseAppConfigurationPolicy.setWatchRequests(true);

        policy.process(contextMock, nextMock);

        assertEquals("null/null " + PRE_USER_AGENT, userAgent);

        assertEquals("RequestType=Watch",
            contextMock.getHttpRequest().getHeaders().get("Correlation-Context").getValue());
        
        request = new HttpRequest(HttpMethod.GET, url);
        request.setHeader(USER_AGENT_TYPE, "PreExistingUserAgent");

        when(contextMock.getHttpRequest()).thenReturn(request);

        policy.process(contextMock, nextMock);
        assertEquals("null/null " + PRE_USER_AGENT, userAgent);

        assertEquals("RequestType=Watch",
            contextMock.getHttpRequest().getHeaders().get("Correlation-Context").getValue());
    }

    @Test
    public void devIsConfigured() throws MalformedURLException {
        BaseAppConfigurationPolicy.setIsDev(true);
        BaseAppConfigurationPolicy policy = new BaseAppConfigurationPolicy();

        URL url = new URL("https://www.test.url/kv");
        HttpRequest request = new HttpRequest(HttpMethod.GET, url);
        request.setHeader(HttpHeaders.USER_AGENT, "PreExistingUserAgent");
        when(contextMock.getHttpRequest()).thenReturn(request);

        policy.process(contextMock, nextMock);
        assertEquals("Dev", contextMock.getHttpRequest().getHeaders().get("Env").getValue());
    }

    @Test
    public void keyVaultIsConfigured() throws MalformedURLException {
        BaseAppConfigurationPolicy.setIsKeyVaultConfigured(true);
        BaseAppConfigurationPolicy policy = new BaseAppConfigurationPolicy();

        URL url = new URL("https://www.test.url/kv");
        HttpRequest request = new HttpRequest(HttpMethod.GET, url);
        request.setHeader(HttpHeaders.USER_AGENT, "PreExistingUserAgent");
        when(contextMock.getHttpRequest()).thenReturn(request);

        policy.process(contextMock, nextMock);
        assertEquals("ConfigureKeyVault", contextMock.getHttpRequest().getHeaders().get("Env").getValue());
    }

    @Test
    public void devAndKeyVaultAreConfigured() throws MalformedURLException {
        BaseAppConfigurationPolicy.setIsDev(true);
        BaseAppConfigurationPolicy.setIsKeyVaultConfigured(true);
        BaseAppConfigurationPolicy policy = new BaseAppConfigurationPolicy();

        URL url = new URL("https://www.test.url/kv");
        HttpRequest request = new HttpRequest(HttpMethod.GET, url);
        request.setHeader(HttpHeaders.USER_AGENT, "PreExistingUserAgent");
        when(contextMock.getHttpRequest()).thenReturn(request);

        policy.process(contextMock, nextMock);
        assertEquals("Dev,ConfigureKeyVault", contextMock.getHttpRequest().getHeaders().get("Env").getValue());
    }

}
