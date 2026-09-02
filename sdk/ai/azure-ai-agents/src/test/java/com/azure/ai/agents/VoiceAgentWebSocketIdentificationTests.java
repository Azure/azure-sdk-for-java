// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents;

import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpRequest;
import com.azure.core.http.rest.RequestOptions;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Header;
import org.junit.jupiter.api.Test;
import reactor.test.StepVerifier;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class VoiceAgentWebSocketIdentificationTests {
    private static final String ENDPOINT = "https://localhost:8080/api/projects/project";
    private static final String AGENT_NAME = "voice-agent-test";
    private static final String CLIENT_SDK_QUERY_PARAMETER = "x-ms-client-sdk";

    @Test
    public void syncVoiceAgentHandshakeIncludesSdkIdentificationAndPreservesQueryParameters() {
        DeterministicHttpClient httpClient = new DeterministicHttpClient().enqueue(101, new HttpHeaders(), new byte[0]);
        BetaVoiceAgentWebSocketClient client
            = createBuilder(httpClient, new ClientOptions().setApplicationId("test-application"))
                .buildBetaVoiceAgentWebSocketClient();
        RequestOptions requestOptions = new RequestOptions().addQueryParam("transport", "websocket");

        client.connectVoiceAgentWithResponse(AGENT_NAME, requestOptions);

        HttpRequest request = httpClient.getRequest(0);
        String userAgent = request.getHeaders().getValue(HttpHeaderName.USER_AGENT);
        assertTrue(userAgent.startsWith("test-application azsdk-java-azure-ai-agents/"));
        assertEquals(userAgent, getQueryParameters(request).get(CLIENT_SDK_QUERY_PARAMETER));
        assertEquals("websocket", getQueryParameters(request).get("transport"));
        assertEquals(AgentsServiceVersion.V1.getVersion(), getQueryParameters(request).get("api-version"));
    }

    @Test
    public void asyncVoiceAgentHandshakeIncludesSdkIdentification() {
        DeterministicHttpClient httpClient = new DeterministicHttpClient().enqueue(101, new HttpHeaders(), new byte[0]);
        BetaVoiceAgentWebSocketAsyncClient client
            = createBuilder(httpClient, null).buildBetaVoiceAgentWebSocketAsyncClient();

        StepVerifier.create(client.connectVoiceAgentWithResponse(AGENT_NAME, new RequestOptions()))
            .expectNextCount(1)
            .verifyComplete();

        HttpRequest request = httpClient.getRequest(0);
        String userAgent = request.getHeaders().getValue(HttpHeaderName.USER_AGENT);
        assertTrue(userAgent.startsWith("azsdk-java-azure-ai-agents/"));
        assertEquals(userAgent, getQueryParameters(request).get(CLIENT_SDK_QUERY_PARAMETER));
    }

    @Test
    public void callerProvidedClientSdkQueryParameterIsPreserved() {
        DeterministicHttpClient httpClient = new DeterministicHttpClient().enqueue(101, new HttpHeaders(), new byte[0]);
        BetaVoiceAgentWebSocketClient client = createBuilder(httpClient, null).buildBetaVoiceAgentWebSocketClient();
        RequestOptions requestOptions = new RequestOptions().addQueryParam(CLIENT_SDK_QUERY_PARAMETER, "custom-sdk-id");

        client.connectVoiceAgentWithResponse(AGENT_NAME, requestOptions);

        Map<String, String> queryParameters = getQueryParameters(httpClient.getRequest(0));
        assertEquals("custom-sdk-id", queryParameters.get(CLIENT_SDK_QUERY_PARAMETER));
        assertEquals(1, countQueryParameters(httpClient.getRequest(0), CLIENT_SDK_QUERY_PARAMETER));
    }

    @Test
    public void customUserAgentOverridesHeaderOnly() {
        DeterministicHttpClient httpClient = new DeterministicHttpClient().enqueue(101, new HttpHeaders(), new byte[0]);
        ClientOptions clientOptions
            = new ClientOptions().setHeaders(Collections.singletonList(new Header("User-Agent", "custom-user-agent")));
        BetaVoiceAgentWebSocketClient client
            = createBuilder(httpClient, clientOptions).buildBetaVoiceAgentWebSocketClient();

        client.connectVoiceAgentWithResponse(AGENT_NAME, new RequestOptions());

        HttpRequest request = httpClient.getRequest(0);
        assertEquals("custom-user-agent", request.getHeaders().getValue(HttpHeaderName.USER_AGENT));
        assertTrue(
            getQueryParameters(request).get(CLIENT_SDK_QUERY_PARAMETER).startsWith("azsdk-java-azure-ai-agents/"));
    }

    private static AgentsClientBuilder createBuilder(DeterministicHttpClient httpClient, ClientOptions clientOptions) {
        AgentsClientBuilder builder = new AgentsClientBuilder().endpoint(ENDPOINT)
            .credential(new MockTokenCredential())
            .httpClient(httpClient)
            .serviceVersion(AgentsServiceVersion.V1);
        return clientOptions == null ? builder : builder.clientOptions(clientOptions);
    }

    private static Map<String, String> getQueryParameters(HttpRequest request) {
        Map<String, String> queryParameters = new LinkedHashMap<>();
        String query = request.getUrl().getQuery();
        if (query == null || query.isEmpty()) {
            return queryParameters;
        }

        for (String pair : query.split("&")) {
            int separator = pair.indexOf('=');
            String name = separator < 0 ? pair : pair.substring(0, separator);
            String value = separator < 0 ? "" : pair.substring(separator + 1);
            queryParameters.put(decode(name), decode(value));
        }
        return queryParameters;
    }

    private static int countQueryParameters(HttpRequest request, String expectedName) {
        int count = 0;
        for (String pair : request.getUrl().getQuery().split("&")) {
            int separator = pair.indexOf('=');
            String name = separator < 0 ? pair : pair.substring(0, separator);
            if (expectedName.equals(decode(name))) {
                count++;
            }
        }
        return count;
    }

    private static String decode(String value) {
        try {
            return URLDecoder.decode(value, StandardCharsets.UTF_8.name());
        } catch (UnsupportedEncodingException e) {
            throw new IllegalStateException("UTF-8 encoding is not supported.", e);
        }
    }
}
