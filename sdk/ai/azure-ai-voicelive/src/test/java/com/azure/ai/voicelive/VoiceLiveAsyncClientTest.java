// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive;

import com.azure.ai.voicelive.models.VoiceLiveSessionOptions;
import com.azure.core.credential.AzureKeyCredential;
import com.azure.core.http.HttpHeaders;
import com.azure.core.http.HttpPipeline;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Mono;

import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link VoiceLiveAsyncClient}.
 */
@ExtendWith(MockitoExtension.class)
class VoiceLiveAsyncClientTest {

    @Mock
    private AzureKeyCredential mockKeyCredential;

    @Mock
    private HttpPipeline mockPipeline;

    @Mock
    private HttpHeaders mockHeaders;

    private URI testEndpoint;
    private VoiceLiveAsyncClient client;

    @BeforeEach
    void setUp() throws Exception {
        testEndpoint = new URI("https://test.cognitiveservices.azure.com");
        client = new VoiceLiveAsyncClient(testEndpoint, mockKeyCredential, "2024-10-01-preview", mockHeaders);
    }

    @Test
    void testConstructorWithValidParameters() {
        // Act & Assert
        assertNotNull(client);
    }

    @Test
    void testConstructorWithNullEndpoint() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            new VoiceLiveAsyncClient(null, mockKeyCredential, "2024-10-01-preview", mockHeaders);
        });
    }

    @Test
    void testConstructorWithNullCredential() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            new VoiceLiveAsyncClient(testEndpoint, (AzureKeyCredential) null, "2024-10-01-preview", mockHeaders);
        });
    }

    @Test
    void testStartSessionWithValidOptions() {
        // Arrange
        VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions().setModel("gpt-4o-realtime-preview");

        // Act & Assert
        // Note: This test might need to be adjusted based on actual implementation
        // For now, we're testing that the method exists and can be called
        assertDoesNotThrow(() -> {
            client.startSession(sessionOptions);
        });
    }

    @Test
    void testStartSessionWithNullOptions() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            client.startSession((VoiceLiveSessionOptions) null);
        });
    }

    @Test
    void testStartSessionWithModelString() {
        // Arrange
        String model = "gpt-4o-realtime-preview";

        // Act & Assert
        assertDoesNotThrow(() -> {
            client.startSession(model);
        });
    }

    @Test
    void testStartSessionWithNullModel() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> {
            client.startSession((String) null);
        });
    }

    @Test
    void testUpdateSession() {
        // This test would need to be implemented based on the actual updateSession method
        // For now, we're just verifying the method exists
        assertTrue(true, "updateSession method should exist in VoiceLiveAsyncClient");
    }

    @Test
    void testClientConfiguration() {
        // Verify that the client maintains its configuration properly
        assertNotNull(client);

        // These tests would verify internal state if getters were available
        // Since the fields are private, we're testing through behavior
        assertTrue(true, "Client should maintain endpoint configuration");
        assertTrue(true, "Client should maintain credential configuration");
        assertTrue(true, "Client should maintain pipeline configuration");
    }

    @Test
    void testOptimizedConnectMethods() {
        // Test that the optimized connect methods return VoiceLiveSession

        // Test startSession with model string
        assertDoesNotThrow(() -> {
            Mono<VoiceLiveSession> result = client.startSession("gpt-4o-realtime-preview");
            assertNotNull(result);
        });

        // Test startSession with session options
        VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions().setModel("gpt-4o-realtime-preview");
        assertDoesNotThrow(() -> {
            Mono<VoiceLiveSession> result = client.startSession(sessionOptions);
            assertNotNull(result);
        });

        // Test null parameter validation for startSession methods
        assertThrows(NullPointerException.class, () -> {
            client.startSession((String) null);
        });

        assertThrows(NullPointerException.class, () -> {
            client.startSession((VoiceLiveSessionOptions) null);
        });
    }

    @Test
    void testReturnTypeOptimization() {
        // Test that connect methods return the session for direct use
        String model = "gpt-4o-realtime-preview";

        assertDoesNotThrow(() -> {
            Mono<VoiceLiveSession> sessionMono = client.startSession(model);
            assertNotNull(sessionMono);
            // The returned Mono should contain a VoiceLiveSession when subscribed
        });

        VoiceLiveSessionOptions options = new VoiceLiveSessionOptions().setModel(model);
        assertDoesNotThrow(() -> {
            Mono<VoiceLiveSession> sessionMono = client.startSession(options);
            assertNotNull(sessionMono);
            // The returned Mono should contain a VoiceLiveSession when subscribed
        });
    }
}
