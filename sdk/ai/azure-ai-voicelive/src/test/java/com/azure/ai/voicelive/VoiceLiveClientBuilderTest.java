// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive;

import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.credential.TokenCredential;
import com.azure.core.http.HttpClient;
import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.HttpLogOptions;
import com.azure.core.http.policy.RetryPolicy;
import com.azure.core.util.ClientOptions;
import com.azure.core.util.Configuration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link VoiceLiveClientBuilder}.
 */
@ExtendWith(MockitoExtension.class)
class VoiceLiveClientBuilderTest {

    @Mock
    private AzureKeyCredential mockKeyCredential;

    @Mock
    private TokenCredential mockTokenCredential;

    @Mock
    private HttpClient mockHttpClient;

    @Mock
    private HttpPipeline mockHttpPipeline;

    @Mock
    private Configuration mockConfiguration;

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
            VoiceLiveAsyncClient client = clientBuilder.endpoint(endpoint).credential(mockKeyCredential).buildClient();

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
                = clientBuilder.endpoint(endpoint).credential(mockTokenCredential).buildClient();

            assertNotNull(client);
        });
    }

    @Test
    void testBuilderWithNullEndpoint() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            clientBuilder.endpoint(null).credential(mockKeyCredential).buildClient();
        });
    }

    @Test
    void testBuilderWithInvalidEndpoint() {
        // Act & Assert
        assertThrows(IllegalArgumentException.class, () -> {
            clientBuilder.endpoint("http:// invalid-url").credential(mockKeyCredential).buildClient();
        });
    }

    @Test
    void testBuilderWithNullCredential() {
        // Arrange
        String endpoint = "https://test.cognitiveservices.azure.com";

        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            clientBuilder.endpoint(endpoint).credential((AzureKeyCredential) null).buildClient();
        });
    }

    @Test
    void testBuilderWithHttpClient() {
        // Arrange
        String endpoint = "https://test.cognitiveservices.azure.com";

        // Act & Assert
        assertDoesNotThrow(() -> {
            VoiceLiveAsyncClient client = clientBuilder.endpoint(endpoint)
                .credential(mockKeyCredential)
                .httpClient(mockHttpClient)
                .buildClient();

            assertNotNull(client);
        });
    }

    @Test
    void testBuilderWithHttpPipeline() {
        // Arrange
        String endpoint = "https://test.cognitiveservices.azure.com";

        // Act & Assert
        assertDoesNotThrow(() -> {
            VoiceLiveAsyncClient client = clientBuilder.endpoint(endpoint)
                .credential(mockKeyCredential)
                .pipeline(mockHttpPipeline)
                .buildClient();

            assertNotNull(client);
        });
    }

    @Test
    void testBuilderWithHttpLogOptions() {
        // Arrange
        String endpoint = "https://test.cognitiveservices.azure.com";
        HttpLogOptions logOptions = new HttpLogOptions();

        // Act & Assert
        assertDoesNotThrow(() -> {
            VoiceLiveAsyncClient client = clientBuilder.endpoint(endpoint)
                .credential(mockKeyCredential)
                .httpLogOptions(logOptions)
                .buildClient();

            assertNotNull(client);
        });
    }

    @Test
    void testBuilderWithClientOptions() {
        // Arrange
        String endpoint = "https://test.cognitiveservices.azure.com";
        ClientOptions clientOptions = new ClientOptions();

        // Act & Assert
        assertDoesNotThrow(() -> {
            VoiceLiveAsyncClient client = clientBuilder.endpoint(endpoint)
                .credential(mockKeyCredential)
                .clientOptions(clientOptions)
                .buildClient();

            assertNotNull(client);
        });
    }

    @Test
    void testBuilderWithRetryPolicy() {
        // Arrange
        String endpoint = "https://test.cognitiveservices.azure.com";
        RetryPolicy retryPolicy = new RetryPolicy();

        // Act & Assert
        assertDoesNotThrow(() -> {
            VoiceLiveAsyncClient client
                = clientBuilder.endpoint(endpoint).credential(mockKeyCredential).retryPolicy(retryPolicy).buildClient();

            assertNotNull(client);
        });
    }

    @Test
    void testBuilderWithConfiguration() {
        // Arrange
        String endpoint = "https://test.cognitiveservices.azure.com";
        when(mockConfiguration.get(eq(Configuration.PROPERTY_AZURE_TELEMETRY_DISABLED), anyBoolean()))
            .thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() -> {
            VoiceLiveAsyncClient client = clientBuilder.endpoint(endpoint)
                .credential(mockKeyCredential)
                .configuration(mockConfiguration)
                .buildClient();

            assertNotNull(client);
        });
    }

    @Test
    void testBuilderWithApiVersion() {
        // Arrange
        String endpoint = "https://test.cognitiveservices.azure.com";
        String apiVersion = "2024-10-01-preview";

        // Act & Assert
        assertDoesNotThrow(() -> {
            VoiceLiveAsyncClient client
                = clientBuilder.endpoint(endpoint).credential(mockKeyCredential).apiVersion(apiVersion).buildClient();

            assertNotNull(client);
        });
    }

    @Test
    void testBuilderChaining() {
        // Arrange
        String endpoint = "https://test.cognitiveservices.azure.com";
        HttpLogOptions logOptions = new HttpLogOptions();
        ClientOptions clientOptions = new ClientOptions();
        RetryPolicy retryPolicy = new RetryPolicy();
        String apiVersion = "2024-10-01-preview";
        when(mockConfiguration.get(eq(Configuration.PROPERTY_AZURE_TELEMETRY_DISABLED), anyBoolean()))
            .thenReturn(false);

        // Act & Assert
        assertDoesNotThrow(() -> {
            VoiceLiveAsyncClient client = clientBuilder.endpoint(endpoint)
                .credential(mockKeyCredential)
                .httpClient(mockHttpClient)
                .httpLogOptions(logOptions)
                .clientOptions(clientOptions)
                .retryPolicy(retryPolicy)
                .configuration(mockConfiguration)
                .apiVersion(apiVersion)
                .buildClient();

            assertNotNull(client);
        });
    }

    @Test
    void testBuilderReturnsBuilder() {
        // Test that all methods return the builder for chaining
        assertSame(clientBuilder, clientBuilder.endpoint("https://test.cognitiveservices.azure.com"));
        assertSame(clientBuilder, clientBuilder.credential(mockKeyCredential));
        assertSame(clientBuilder, clientBuilder.httpClient(mockHttpClient));
        assertSame(clientBuilder, clientBuilder.pipeline(mockHttpPipeline));
        assertSame(clientBuilder, clientBuilder.httpLogOptions(new HttpLogOptions()));
        assertSame(clientBuilder, clientBuilder.clientOptions(new ClientOptions()));
        assertSame(clientBuilder, clientBuilder.retryPolicy(new RetryPolicy()));
        assertSame(clientBuilder, clientBuilder.configuration(mockConfiguration));
        assertSame(clientBuilder, clientBuilder.apiVersion("2024-10-01-preview"));
    }
}
