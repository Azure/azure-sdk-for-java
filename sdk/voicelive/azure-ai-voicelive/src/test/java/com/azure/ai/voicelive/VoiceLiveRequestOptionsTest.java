// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive;

import com.azure.ai.voicelive.models.VoiceLiveRequestOptions;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.test.utils.MockTokenCredential;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for VoiceLiveRequestOptions feature in VoiceLive client.
 */
class VoiceLiveRequestOptionsTest {
    private static final HttpHeaderName X_CUSTOM_HEADER = HttpHeaderName.fromString("X-Custom-Header");
    private static final HttpHeaderName X_ANOTHER_HEADER = HttpHeaderName.fromString("X-Another-Header");

    private URI testEndpoint;
    private String apiVersion;

    @BeforeEach
    void setUp() throws Exception {
        testEndpoint = new URI("https://test.cognitiveservices.azure.com");
        apiVersion = "2024-10-01-preview";
    }

    @Test
    void testRequestOptionsWithCustomQueryParameters() {
        // Arrange & Act
        VoiceLiveRequestOptions options
            = new VoiceLiveRequestOptions().addCustomQueryParameter("deployment-id", "test-deployment")
                .addCustomQueryParameter("region", "eastus");

        // Assert
        assertNotNull(options.getCustomQueryParameters());
        assertEquals(2, options.getCustomQueryParameters().size());
        assertEquals("test-deployment", options.getCustomQueryParameters().get("deployment-id"));
        assertEquals("eastus", options.getCustomQueryParameters().get("region"));
    }

    @Test
    void testRequestOptionsWithCustomHeaders() {
        // Arrange & Act
        VoiceLiveRequestOptions options
            = new VoiceLiveRequestOptions().addCustomHeader("X-Custom-Header", "custom-value")
                .addCustomHeader("X-Another-Header", "another-value");

        // Assert
        assertNotNull(options.getCustomHeaders());
        assertEquals("custom-value", options.getCustomHeaders().getValue(X_CUSTOM_HEADER));
        assertEquals("another-value", options.getCustomHeaders().getValue(X_ANOTHER_HEADER));
    }

    @Test
    void testRequestOptionsFluentApi() {
        // Arrange & Act
        VoiceLiveRequestOptions options = new VoiceLiveRequestOptions();

        VoiceLiveRequestOptions result1 = options.addCustomQueryParameter("key1", "value1");
        VoiceLiveRequestOptions result2 = options.addCustomHeader("Header1", "value1");

        // Assert
        assertSame(options, result1);
        assertSame(options, result2);
    }

    @Test
    void testRequestOptionsSetCustomHeaders() {
        // Arrange
        HttpHeaders headers = new HttpHeaders();
        headers.set(X_CUSTOM_HEADER, "value1");
        headers.set(X_ANOTHER_HEADER, "value2");

        // Act
        VoiceLiveRequestOptions options = new VoiceLiveRequestOptions().setCustomHeaders(headers);

        // Assert
        assertNotNull(options.getCustomHeaders());
        assertEquals("value1", options.getCustomHeaders().getValue(X_CUSTOM_HEADER));
        assertEquals("value2", options.getCustomHeaders().getValue(X_ANOTHER_HEADER));
    }

    @Test
    void testConvertToWebSocketEndpointWithRequestOptions() throws Exception {
        // Arrange
        VoiceLiveRequestOptions requestOptions
            = new VoiceLiveRequestOptions().addCustomQueryParameter("deployment-id", "test-deployment")
                .addCustomQueryParameter("custom-param", "custom-value");

        VoiceLiveAsyncClient client
            = new VoiceLiveAsyncClient(testEndpoint, new MockTokenCredential(), apiVersion, new HttpHeaders());

        // Use reflection to access the private method
        Method method = VoiceLiveAsyncClient.class.getDeclaredMethod("convertToWebSocketEndpoint", URI.class,
            String.class, Map.class);
        method.setAccessible(true);

        // Act
        URI result = (URI) method.invoke(client, testEndpoint, "gpt-4o-realtime-preview",
            requestOptions.getCustomQueryParameters());

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

        VoiceLiveAsyncClient client
            = new VoiceLiveAsyncClient(testEndpoint, new MockTokenCredential(), apiVersion, new HttpHeaders());

        // Use reflection to access the private method
        Method method = VoiceLiveAsyncClient.class.getDeclaredMethod("convertToWebSocketEndpoint", URI.class,
            String.class, Map.class);
        method.setAccessible(true);

        // Act - passing null as model parameter
        URI result = (URI) method.invoke(client, testEndpoint, null, requestOptions.getCustomQueryParameters());

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

        VoiceLiveAsyncClient client
            = new VoiceLiveAsyncClient(endpointWithQuery, new MockTokenCredential(), apiVersion, new HttpHeaders());

        // Use reflection to access the private method
        Method method = VoiceLiveAsyncClient.class.getDeclaredMethod("convertToWebSocketEndpoint", URI.class,
            String.class, Map.class);
        method.setAccessible(true);

        // Act
        URI result = (URI) method.invoke(client, endpointWithQuery, "gpt-4o-realtime-preview",
            requestOptions.getCustomQueryParameters());

        // Assert - SDK's api-version should take precedence
        assertNotNull(result);
        assertTrue(result.getQuery().contains("api-version=" + apiVersion));
        assertTrue(result.getQuery().contains("deployment-id=test-deployment"));
    }
}
