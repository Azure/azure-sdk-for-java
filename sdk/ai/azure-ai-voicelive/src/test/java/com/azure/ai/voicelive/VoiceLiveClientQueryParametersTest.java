// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive;

import com.azure.core.credential.KeyCredential;
import com.azure.core.http.HttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.lang.reflect.Method;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for custom query parameters feature in VoiceLive client.
 */
@ExtendWith(MockitoExtension.class)
class VoiceLiveClientQueryParametersTest {

    @Mock
    private KeyCredential mockKeyCredential;

    @Mock
    private HttpHeaders mockHeaders;

    private URI testEndpoint;
    private String apiVersion;

    @BeforeEach
    void setUp() throws Exception {
        testEndpoint = new URI("https://test.cognitiveservices.azure.com");
        apiVersion = "2024-10-01-preview";
    }

    @Test
    void testClientBuilderWithCustomQueryParameters() {
        // Arrange
        Map<String, String> customParams = new HashMap<>();
        customParams.put("deployment-id", "test-deployment");
        customParams.put("region", "eastus");

        // Act
        VoiceLiveClientBuilder builder = new VoiceLiveClientBuilder();
        VoiceLiveAsyncClient client = builder.endpoint(testEndpoint.toString())
            .credential(mockKeyCredential)
            .customQueryParameters(customParams)
            .buildAsyncClient();

        // Assert
        assertNotNull(client);
    }

    @Test
    void testConvertToWebSocketEndpointWithCustomQueryParameters() throws Exception {
        // Arrange
        Map<String, String> customParams = new HashMap<>();
        customParams.put("deployment-id", "test-deployment");
        customParams.put("custom-param", "custom-value");

        VoiceLiveAsyncClient client
            = new VoiceLiveAsyncClient(testEndpoint, mockKeyCredential, apiVersion, mockHeaders, customParams);

        // Use reflection to access the private method
        Method method
            = VoiceLiveAsyncClient.class.getDeclaredMethod("convertToWebSocketEndpoint", URI.class, String.class);
        method.setAccessible(true);

        // Act
        URI result = (URI) method.invoke(client, testEndpoint, "gpt-4o-realtime-preview");

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
    void testConvertToWebSocketEndpointWithoutModel() throws Exception {
        // Arrange
        Map<String, String> customParams = new HashMap<>();
        customParams.put("deployment-id", "test-deployment");

        VoiceLiveAsyncClient client
            = new VoiceLiveAsyncClient(testEndpoint, mockKeyCredential, apiVersion, mockHeaders, customParams);

        // Use reflection to access the private method
        Method method
            = VoiceLiveAsyncClient.class.getDeclaredMethod("convertToWebSocketEndpoint", URI.class, String.class);
        method.setAccessible(true);

        // Act
        URI result = (URI) method.invoke(client, testEndpoint, null);

        // Assert
        assertNotNull(result);
        assertNotNull(result.getQuery());
        assertTrue(result.getQuery().contains("api-version=" + apiVersion));
        assertTrue(result.getQuery().contains("deployment-id=test-deployment"));
        // Model should not be in query string when null
        assertTrue(!result.getQuery().contains("model="));
    }

    @Test
    void testConvertToWebSocketEndpointWithExistingQueryParameters() throws Exception {
        // Arrange
        URI endpointWithQuery = new URI("https://test.cognitiveservices.azure.com?existing-param=existing-value");
        Map<String, String> customParams = new HashMap<>();
        customParams.put("custom-param", "custom-value");

        VoiceLiveAsyncClient client
            = new VoiceLiveAsyncClient(endpointWithQuery, mockKeyCredential, apiVersion, mockHeaders, customParams);

        // Use reflection to access the private method
        Method method
            = VoiceLiveAsyncClient.class.getDeclaredMethod("convertToWebSocketEndpoint", URI.class, String.class);
        method.setAccessible(true);

        // Act
        URI result = (URI) method.invoke(client, endpointWithQuery, "gpt-4o-realtime-preview");

        // Assert
        assertNotNull(result);
        assertNotNull(result.getQuery());
        assertTrue(result.getQuery().contains("existing-param=existing-value"));
        assertTrue(result.getQuery().contains("custom-param=custom-value"));
        assertTrue(result.getQuery().contains("api-version=" + apiVersion));
        assertTrue(result.getQuery().contains("model=gpt-4o-realtime-preview"));
    }

    @Test
    void testCustomQueryParametersOverrideExistingParameters() throws Exception {
        // Arrange - endpoint has deployment-id, custom params also have deployment-id
        URI endpointWithQuery = new URI("https://test.cognitiveservices.azure.com?deployment-id=old-value");
        Map<String, String> customParams = new HashMap<>();
        customParams.put("deployment-id", "new-value");

        VoiceLiveAsyncClient client
            = new VoiceLiveAsyncClient(endpointWithQuery, mockKeyCredential, apiVersion, mockHeaders, customParams);

        // Use reflection to access the private method
        Method method
            = VoiceLiveAsyncClient.class.getDeclaredMethod("convertToWebSocketEndpoint", URI.class, String.class);
        method.setAccessible(true);

        // Act
        URI result = (URI) method.invoke(client, endpointWithQuery, "gpt-4o-realtime-preview");

        // Assert
        assertNotNull(result);
        assertNotNull(result.getQuery());
        // Should contain the new value from customParams
        assertTrue(result.getQuery().contains("deployment-id=new-value"));
        // Should not contain the old value
        assertTrue(!result.getQuery().contains("deployment-id=old-value"));
    }

    @Test
    void testApiVersionAndModelTakePrecedence() throws Exception {
        // Arrange - custom params try to set api-version and model
        Map<String, String> customParams = new HashMap<>();
        customParams.put("api-version", "wrong-version");
        customParams.put("model", "wrong-model");

        VoiceLiveAsyncClient client
            = new VoiceLiveAsyncClient(testEndpoint, mockKeyCredential, apiVersion, mockHeaders, customParams);

        // Use reflection to access the private method
        Method method
            = VoiceLiveAsyncClient.class.getDeclaredMethod("convertToWebSocketEndpoint", URI.class, String.class);
        method.setAccessible(true);

        // Act
        URI result = (URI) method.invoke(client, testEndpoint, "correct-model");

        // Assert
        assertNotNull(result);
        assertNotNull(result.getQuery());
        // SDK's apiVersion should take precedence
        assertTrue(result.getQuery().contains("api-version=" + apiVersion));
        // Method parameter model should take precedence
        assertTrue(result.getQuery().contains("model=correct-model"));
        // Should not contain the wrong values
        assertTrue(!result.getQuery().contains("api-version=wrong-version"));
        assertTrue(!result.getQuery().contains("model=wrong-model"));
    }

    @Test
    void testConvertToWebSocketEndpointWithNullCustomQueryParameters() throws Exception {
        // Arrange
        VoiceLiveAsyncClient client
            = new VoiceLiveAsyncClient(testEndpoint, mockKeyCredential, apiVersion, mockHeaders, null);

        // Use reflection to access the private method
        Method method
            = VoiceLiveAsyncClient.class.getDeclaredMethod("convertToWebSocketEndpoint", URI.class, String.class);
        method.setAccessible(true);

        // Act
        URI result = (URI) method.invoke(client, testEndpoint, "gpt-4o-realtime-preview");

        // Assert
        assertNotNull(result);
        assertNotNull(result.getQuery());
        assertTrue(result.getQuery().contains("api-version=" + apiVersion));
        assertTrue(result.getQuery().contains("model=gpt-4o-realtime-preview"));
    }

    @Test
    void testConvertToWebSocketEndpointWithEmptyCustomQueryParameters() throws Exception {
        // Arrange
        Map<String, String> emptyParams = new HashMap<>();
        VoiceLiveAsyncClient client
            = new VoiceLiveAsyncClient(testEndpoint, mockKeyCredential, apiVersion, mockHeaders, emptyParams);

        // Use reflection to access the private method
        Method method
            = VoiceLiveAsyncClient.class.getDeclaredMethod("convertToWebSocketEndpoint", URI.class, String.class);
        method.setAccessible(true);

        // Act
        URI result = (URI) method.invoke(client, testEndpoint, "gpt-4o-realtime-preview");

        // Assert
        assertNotNull(result);
        assertNotNull(result.getQuery());
        assertTrue(result.getQuery().contains("api-version=" + apiVersion));
        assertTrue(result.getQuery().contains("model=gpt-4o-realtime-preview"));
    }

    @Test
    void testSchemeConversionWithCustomQueryParameters() throws Exception {
        // Test https -> wss conversion
        Map<String, String> customParams = new HashMap<>();
        customParams.put("test-param", "test-value");

        VoiceLiveAsyncClient client
            = new VoiceLiveAsyncClient(testEndpoint, mockKeyCredential, apiVersion, mockHeaders, customParams);

        Method method
            = VoiceLiveAsyncClient.class.getDeclaredMethod("convertToWebSocketEndpoint", URI.class, String.class);
        method.setAccessible(true);

        URI result = (URI) method.invoke(client, testEndpoint, "model");
        assertEquals("wss", result.getScheme());

        // Test http -> ws conversion
        URI httpEndpoint = new URI("http://test.cognitiveservices.azure.com");
        result = (URI) method.invoke(client, httpEndpoint, "model");
        assertEquals("ws", result.getScheme());

        // Test wss remains wss
        URI wssEndpoint = new URI("wss://test.cognitiveservices.azure.com");
        result = (URI) method.invoke(client, wssEndpoint, "model");
        assertEquals("wss", result.getScheme());
    }

    @Test
    void testPathNormalizationWithCustomQueryParameters() throws Exception {
        // Arrange
        Map<String, String> customParams = new HashMap<>();
        customParams.put("param", "value");

        VoiceLiveAsyncClient client
            = new VoiceLiveAsyncClient(testEndpoint, mockKeyCredential, apiVersion, mockHeaders, customParams);

        Method method
            = VoiceLiveAsyncClient.class.getDeclaredMethod("convertToWebSocketEndpoint", URI.class, String.class);
        method.setAccessible(true);

        // Test path without /realtime
        URI result = (URI) method.invoke(client, testEndpoint, "model");
        assertTrue(result.getPath().endsWith("/voice-live/realtime"));

        // Test path with trailing slash
        URI endpointWithSlash = new URI("https://test.cognitiveservices.azure.com/");
        result = (URI) method.invoke(client, endpointWithSlash, "model");
        assertTrue(result.getPath().endsWith("/voice-live/realtime"));
    }

    @Test
    void testStartSessionWithAndWithoutModelParameter() {
        // Arrange
        Map<String, String> customParams = new HashMap<>();
        customParams.put("deployment-id", "test");

        VoiceLiveAsyncClient client = new VoiceLiveClientBuilder().endpoint(testEndpoint.toString())
            .credential(mockKeyCredential)
            .customQueryParameters(customParams)
            .buildAsyncClient();

        // Act & Assert - startSession with model
        assertDoesNotThrow(() -> {
            assertNotNull(client.startSession("gpt-4o-realtime-preview"));
        });

        // Act & Assert - startSession without model
        assertDoesNotThrow(() -> {
            assertNotNull(client.startSession());
        });
    }
}
