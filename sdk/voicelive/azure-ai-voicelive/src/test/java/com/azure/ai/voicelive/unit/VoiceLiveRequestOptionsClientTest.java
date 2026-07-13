// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.unit;

import com.azure.ai.voicelive.VoiceLiveAsyncClient;
import com.azure.ai.voicelive.models.VoiceLiveRequestOptions;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpHeaders;
import com.azure.core.test.utils.MockTokenCredential;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * White-box tests for how {@link VoiceLiveAsyncClient} applies {@link VoiceLiveRequestOptions} when building the
 * WebSocket endpoint.
 * <p>
 * The client's constructor and {@code convertToWebSocketEndpoint} method are intentionally non-public (internal API
 * surface), so this test reaches them via reflection rather than living in the {@code com.azure.ai.voicelive} package.
 * Model-only tests live in {@link VoiceLiveRequestOptionsTest}.
 */
class VoiceLiveRequestOptionsClientTest {
    private URI testEndpoint;
    private String apiVersion;
    private Constructor<VoiceLiveAsyncClient> tokenCredentialConstructor;
    private Method convertToWebSocketEndpoint;

    @BeforeEach
    void setUp() throws Exception {
        testEndpoint = new URI("https://test.cognitiveservices.azure.com");
        apiVersion = "2024-10-01-preview";
        tokenCredentialConstructor = VoiceLiveAsyncClient.class.getDeclaredConstructor(URI.class, TokenCredential.class,
            String.class, HttpHeaders.class);
        tokenCredentialConstructor.setAccessible(true);
        convertToWebSocketEndpoint = VoiceLiveAsyncClient.class.getDeclaredMethod("convertToWebSocketEndpoint",
            URI.class, String.class, Map.class);
        convertToWebSocketEndpoint.setAccessible(true);
    }

    private VoiceLiveAsyncClient newClient(URI endpoint) throws Exception {
        return tokenCredentialConstructor.newInstance(endpoint, new MockTokenCredential(), apiVersion,
            new HttpHeaders());
    }

    private URI convert(VoiceLiveAsyncClient client, URI endpoint, String model, Map<String, String> queryParams)
        throws Exception {
        return (URI) convertToWebSocketEndpoint.invoke(client, endpoint, model, queryParams);
    }

    @Test
    void testConvertToWebSocketEndpointWithRequestOptions() throws Exception {
        // Arrange
        VoiceLiveRequestOptions requestOptions
            = new VoiceLiveRequestOptions().addCustomQueryParameter("deployment-id", "test-deployment")
                .addCustomQueryParameter("custom-param", "custom-value");

        VoiceLiveAsyncClient client = newClient(testEndpoint);

        // Act
        URI result
            = convert(client, testEndpoint, "gpt-4o-realtime-preview", requestOptions.getCustomQueryParameters());

        // Assert
        assertNotNull(result);
        assertEquals("wss", result.getScheme());
        assertNotNull(result.getQuery());
        assertTrue(result.getQuery().contains("api-version=" + apiVersion));
        assertTrue(result.getQuery().contains("model=gpt-4o-realtime-preview"));
        assertTrue(result.getQuery().contains("deployment-id=test-deployment"));
        assertTrue(result.getQuery().contains("custom-param=custom-value"));
    }

    @Test
    void testConvertToWebSocketEndpointWithRequestOptionsWithoutModel() throws Exception {
        // Arrange
        VoiceLiveRequestOptions requestOptions
            = new VoiceLiveRequestOptions().addCustomQueryParameter("deployment-id", "test-deployment")
                .addCustomQueryParameter("model", "custom-model-from-params");

        VoiceLiveAsyncClient client = newClient(testEndpoint);

        // Act - passing null as model parameter
        URI result = convert(client, testEndpoint, null, requestOptions.getCustomQueryParameters());

        // Assert
        assertNotNull(result);
        assertEquals("wss", result.getScheme());
        assertNotNull(result.getQuery());
        assertTrue(result.getQuery().contains("api-version=" + apiVersion));
        assertTrue(result.getQuery().contains("model=custom-model-from-params"));
        assertTrue(result.getQuery().contains("deployment-id=test-deployment"));
    }

    @Test
    void testRequestOptionsQueryParameterPrecedence() throws Exception {
        // Arrange - endpoint has api-version, requestOptions overrides it (but SDK should win)
        URI endpointWithQuery = new URI("https://test.cognitiveservices.azure.com?api-version=old-version");

        VoiceLiveRequestOptions requestOptions
            = new VoiceLiveRequestOptions().addCustomQueryParameter("deployment-id", "test-deployment");

        VoiceLiveAsyncClient client = newClient(endpointWithQuery);

        // Act
        URI result
            = convert(client, endpointWithQuery, "gpt-4o-realtime-preview", requestOptions.getCustomQueryParameters());

        // Assert - SDK's api-version should take precedence
        assertNotNull(result);
        assertTrue(result.getQuery().contains("api-version=" + apiVersion));
        assertTrue(result.getQuery().contains("deployment-id=test-deployment"));
    }
}
