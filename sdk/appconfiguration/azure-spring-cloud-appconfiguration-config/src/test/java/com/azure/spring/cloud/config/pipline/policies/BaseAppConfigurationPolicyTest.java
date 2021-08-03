// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.config.pipline.policies;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.when;

import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpHeaders;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.azure.core.http.HttpMethod;
import com.azure.core.http.HttpPipelineCallContext;
import com.azure.core.http.HttpPipelineNextPolicy;
import com.azure.core.http.HttpRequest;

public class BaseAppConfigurationPolicyTest {

    private static final String PRE_USER_AGENT = "PreExistingUserAgent";
    @Mock
    HttpPipelineCallContext contextMock;
    @Mock
    HttpPipelineNextPolicy nextMock;

    @Before
    public void setup() {
        MockitoAnnotations.initMocks(this);
    }

    @Test
    public void processTest() throws MalformedURLException {
        URL url = new URL("https://www.test.url/kv");
        HttpRequest request = new HttpRequest(HttpMethod.GET, url);
        request.setHeader(HttpHeaders.USER_AGENT, "PreExistingUserAgent");
        BaseAppConfigurationPolicy policy = new BaseAppConfigurationPolicy();

        when(contextMock.getHttpRequest()).thenReturn(request);

        policy.process(contextMock, nextMock);

        String userAgent = contextMock.getHttpRequest().getHeaders().get(HttpHeaders.USER_AGENT).getValue();
        assertEquals("null/null " + PRE_USER_AGENT, userAgent);

        assertEquals("RequestType=Startup",
            contextMock.getHttpRequest().getHeaders().get("Correlation-Context").getValue());
    }

    @Test
    public void watchUpdateTest() throws MalformedURLException {
        URL url = new URL("https://www.test.url/kv");
        HttpRequest request = new HttpRequest(HttpMethod.GET, url);
        request.setHeader(HttpHeaders.USER_AGENT, "PreExistingUserAgent");
        BaseAppConfigurationPolicy policy = new BaseAppConfigurationPolicy();

        when(contextMock.getHttpRequest()).thenReturn(request);

        policy.process(contextMock, nextMock);

        String userAgent = contextMock.getHttpRequest().getHeaders().get(HttpHeaders.USER_AGENT).getValue();
        assertEquals("null/null " + PRE_USER_AGENT, userAgent);

        assertEquals("RequestType=Startup",
            contextMock.getHttpRequest().getHeaders().get("Correlation-Context").getValue());

        url = new URL("https://www.test.url/revisions");
        request = new HttpRequest(HttpMethod.GET, url);
        request.setHeader(HttpHeaders.USER_AGENT, "PreExistingUserAgent");

        when(contextMock.getHttpRequest()).thenReturn(request);

        policy.process(contextMock, nextMock);

        assertEquals("null/null " + PRE_USER_AGENT, userAgent);

        assertEquals("RequestType=Watch",
            contextMock.getHttpRequest().getHeaders().get("Correlation-Context").getValue());

        url = new URL("https://www.test.url/kv");
        request = new HttpRequest(HttpMethod.GET, url);
        request.setHeader(HttpHeaders.USER_AGENT, "PreExistingUserAgent");

        when(contextMock.getHttpRequest()).thenReturn(request);

        policy.process(contextMock, nextMock);
        assertEquals("null/null " + PRE_USER_AGENT, userAgent);

        assertEquals("RequestType=Watch",
            contextMock.getHttpRequest().getHeaders().get("Correlation-Context").getValue());
    }

}
