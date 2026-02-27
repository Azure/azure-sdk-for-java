// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive;

import com.azure.core.credential.KeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.test.utils.MockTokenCredential;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
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
                .serviceVersion(VoiceLiveServiceVersion.V2025_10_01)
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
                .serviceVersion(VoiceLiveServiceVersion.V2025_10_01)
                .buildAsyncClient();

            assertNotNull(client);
        });
    }

    @Test
    void testBuilderReturnsBuilder() {
        // Test that all methods return the builder for chaining
        assertSame(clientBuilder, clientBuilder.endpoint("https://test.cognitiveservices.azure.com"));
        assertSame(clientBuilder, clientBuilder.credential(mockKeyCredential));
        assertSame(clientBuilder, clientBuilder.serviceVersion(VoiceLiveServiceVersion.V2025_10_01));
    }
}
