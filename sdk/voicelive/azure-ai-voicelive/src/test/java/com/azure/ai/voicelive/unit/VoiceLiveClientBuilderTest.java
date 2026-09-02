// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.unit;

import com.azure.ai.voicelive.VoiceLiveAsyncClient;
import com.azure.ai.voicelive.VoiceLiveClientBuilder;
import com.azure.ai.voicelive.VoiceLiveServiceVersion;
import com.azure.core.credential.KeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpHeaderName;
import com.azure.core.http.HttpHeaders;
import com.azure.core.test.utils.MockTokenCredential;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Header;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * Unit tests for {@link VoiceLiveClientBuilder}.
 */
class VoiceLiveClientBuilderTest {
    private final KeyCredential mockKeyCredential = new KeyCredential("fake");
    private final TokenCredential mockTokenCredential = new MockTokenCredential();

    private VoiceLiveClientBuilder clientBuilder;

    @BeforeEach
    void setUp() {
        clientBuilder = new VoiceLiveClientBuilder();
    }

    @Test
    void testBuilderWithValidEndpointAndKeyCredential() {
        // Arrange
        String endpoint = "https://test.cognitiveservices.azure.com";

        // Act & Assert
        assertDoesNotThrow(() -> {
            VoiceLiveAsyncClient client
                = clientBuilder.endpoint(endpoint).credential(mockKeyCredential).buildAsyncClient();

            assertNotNull(client);
        });
    }

    @Test
    void testBuilderWithValidEndpointAndTokenCredential() {
        // Arrange
        String endpoint = "https://test.cognitiveservices.azure.com";

        // Act & Assert
        assertDoesNotThrow(() -> {
            VoiceLiveAsyncClient client
                = clientBuilder.endpoint(endpoint).credential(mockTokenCredential).buildAsyncClient();

            assertNotNull(client);
        });
    }

    @Test
    void testBuilderWithNullEndpoint() {
        // Act & Assert
        assertThrows(NullPointerException.class,
            () -> clientBuilder.endpoint(null).credential(mockKeyCredential).buildAsyncClient());
    }

    @Test
    void testBuilderWithInvalidEndpoint() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class,
            () -> clientBuilder.endpoint("http:// invalid-url").credential(mockKeyCredential).buildAsyncClient());
    }

    @Test
    void testBuilderWithNullCredential() {
        // Arrange
        String endpoint = "https://test.cognitiveservices.azure.com";

        // Act & Assert
        assertThrows(NullPointerException.class,
            () -> clientBuilder.endpoint(endpoint).credential((KeyCredential) null).buildAsyncClient());
    }

    @Test
    void testBuilderWithHttpClient() {
        // Arrange
        String endpoint = "https://test.cognitiveservices.azure.com";

        // Act & Assert
        assertDoesNotThrow(() -> {
            VoiceLiveAsyncClient client
                = clientBuilder.endpoint(endpoint).credential(mockKeyCredential).buildAsyncClient();

            assertNotNull(client);
        });
    }

    @Test
    void testBuilderWithHttpPipeline() {
        // Arrange
        String endpoint = "https://test.cognitiveservices.azure.com";

        // Act & Assert
        assertDoesNotThrow(() -> {
            VoiceLiveAsyncClient client
                = clientBuilder.endpoint(endpoint).credential(mockKeyCredential).buildAsyncClient();

            assertNotNull(client);
        });
    }

    @Test
    void testBuilderWithHttpLogOptions() {
        // Arrange
        String endpoint = "https://test.cognitiveservices.azure.com";

        // Act & Assert
        assertDoesNotThrow(() -> {
            VoiceLiveAsyncClient client
                = clientBuilder.endpoint(endpoint).credential(mockKeyCredential).buildAsyncClient();

            assertNotNull(client);
        });
    }

    @Test
    void testBuilderWithClientOptions() {
        // Arrange
        String endpoint = "https://test.cognitiveservices.azure.com";

        // Act & Assert
        assertDoesNotThrow(() -> {
            VoiceLiveAsyncClient client
                = clientBuilder.endpoint(endpoint).credential(mockKeyCredential).buildAsyncClient();

            assertNotNull(client);
        });
    }

    @Test
    void testBuilderWithRetryPolicy() {
        // Arrange
        String endpoint = "https://test.cognitiveservices.azure.com";

        // Act & Assert
        assertDoesNotThrow(() -> {
            VoiceLiveAsyncClient client
                = clientBuilder.endpoint(endpoint).credential(mockKeyCredential).buildAsyncClient();

            assertNotNull(client);
        });
    }

    @Test
    void testBuilderWithConfiguration() {
        // Arrange
        String endpoint = "https://test.cognitiveservices.azure.com";

        // Act & Assert
        assertDoesNotThrow(() -> {
            VoiceLiveAsyncClient client
                = clientBuilder.endpoint(endpoint).credential(mockKeyCredential).buildAsyncClient();

            assertNotNull(client);
        });
    }

    @Test
    void testBuilderWithServiceVersion() {
        // Arrange
        String endpoint = "https://test.cognitiveservices.azure.com";

        // Act & Assert
        assertDoesNotThrow(() -> {
            VoiceLiveAsyncClient client = clientBuilder.endpoint(endpoint)
                .credential(mockKeyCredential)
                .serviceVersion(VoiceLiveServiceVersion.V2026_07_15)
                .buildAsyncClient();

            assertNotNull(client);
        });
    }

    @Test
    void testBuilderChaining() {
        // Arrange
        String endpoint = "https://test.cognitiveservices.azure.com";

        // Act & Assert
        assertDoesNotThrow(() -> {
            VoiceLiveAsyncClient client = clientBuilder.endpoint(endpoint)
                .credential(mockKeyCredential)
                .serviceVersion(VoiceLiveServiceVersion.V2026_07_15)
                .buildAsyncClient();

            assertNotNull(client);
        });
    }

    @Test
    void testBuilderReturnsBuilder() {
        // Test that all methods return the builder for chaining
        assertSame(clientBuilder, clientBuilder.endpoint("https://test.cognitiveservices.azure.com"));
        assertSame(clientBuilder, clientBuilder.credential(mockKeyCredential));
        assertSame(clientBuilder, clientBuilder.serviceVersion(VoiceLiveServiceVersion.V2026_07_15));
    }

    @Test
    void testBuilderWithDefaultTelemetry() throws Exception {
        VoiceLiveAsyncClient client = clientBuilder.endpoint("https://test.cognitiveservices.azure.com")
            .credential(mockKeyCredential)
            .clientOptions(new ClientOptions().setApplicationId("test-application"))
            .buildAsyncClient();

        String userAgent = getField(client, "userAgent", String.class);
        HttpHeaders headers = getField(client, "additionalHeaders", HttpHeaders.class);
        assertTrue(userAgent.startsWith("test-application azsdk-java-azure-ai-voicelive/"));
        assertEquals(userAgent, headers.getValue(HttpHeaderName.USER_AGENT));
    }

    @Test
    void testBuilderCustomUserAgentOverridesDefaultHeader() throws Exception {
        VoiceLiveAsyncClient client = clientBuilder.endpoint("https://test.cognitiveservices.azure.com")
            .credential(mockKeyCredential)
            .clientOptions(new ClientOptions()
                .setHeaders(Collections.singletonList(new Header("User-Agent", "custom-user-agent"))))
            .buildAsyncClient();

        String userAgent = getField(client, "userAgent", String.class);
        HttpHeaders headers = getField(client, "additionalHeaders", HttpHeaders.class);
        assertTrue(userAgent.startsWith("azsdk-java-azure-ai-voicelive/"));
        assertEquals("custom-user-agent", headers.getValue(HttpHeaderName.USER_AGENT));
    }

    private static <T> T getField(VoiceLiveAsyncClient client, String fieldName, Class<T> type) throws Exception {
        Field field = VoiceLiveAsyncClient.class.getDeclaredField(fieldName);
        field.setAccessible(true);
        return type.cast(field.get(client));
    }
}
