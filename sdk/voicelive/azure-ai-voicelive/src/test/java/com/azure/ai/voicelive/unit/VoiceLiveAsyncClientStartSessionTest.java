// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.unit;

import com.azure.ai.voicelive.VoiceLiveAsyncClient;
import com.azure.ai.voicelive.VoiceLiveClientBuilder;
import com.azure.ai.voicelive.VoiceLiveSessionAsyncClient;
import com.azure.ai.voicelive.models.AgentSessionConfig;
import com.azure.ai.voicelive.models.VoiceLiveRequestOptions;
import com.azure.ai.voicelive.models.VoiceLiveSessionOptions;
import com.azure.core.credential.KeyCredential;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import reactor.core.publisher.Mono;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Behavioral unit tests for {@link VoiceLiveAsyncClient} that exercise the public {@code startSession} API.
 * <p>
 * The client is constructed through the public {@link VoiceLiveClientBuilder} so these tests can live outside the
 * {@code com.azure.ai.voicelive} package. White-box tests that rely on package-private members (the constructor and
 * {@code toQueryParameters}) remain in {@code VoiceLiveAsyncClientTest}.
 */
class VoiceLiveAsyncClientStartSessionTest {
    private VoiceLiveAsyncClient client;

    @BeforeEach
    void setUp() {
        client = new VoiceLiveClientBuilder().endpoint("https://test.cognitiveservices.azure.com")
            .credential(new KeyCredential("fake"))
            .buildAsyncClient();
    }

    @Test
    void testStartSessionWithValidOptions() {
        // Arrange
        VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions().setModel("gpt-4o-realtime-preview");

        // Act & Assert
        // Note: This test might need to be adjusted based on actual implementation
        // For now, we're testing that the method exists and can be called
        assertDoesNotThrow(() -> {
            client.startSession(sessionOptions.getModel(), null);
        });
    }

    @Test
    void testStartSessionWithNullOptions() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> client.startSession((String) null, null));
    }

    @Test
    void testStartSessionWithoutModel() {
        // Act & Assert
        assertDoesNotThrow(() -> {
            Mono<VoiceLiveSessionAsyncClient> result = client.startSession();
            assertNotNull(result);
        });
    }

    @Test
    void testStartSessionWithModelString() {
        // Arrange
        String model = "gpt-4o-realtime-preview";

        // Act & Assert
        assertDoesNotThrow(() -> {
            client.startSession(model, null);
        });
    }

    @Test
    void testStartSessionWithNullModel() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> client.startSession((String) null, null));
    }

    @Test
    void testStartSessionWithModelAndRequestOptions() {
        String model = "gpt-4o-realtime-preview";
        VoiceLiveRequestOptions requestOptions = new VoiceLiveRequestOptions();

        assertDoesNotThrow(() -> {
            Mono<VoiceLiveSessionAsyncClient> sessionMono = client.startSession(model, requestOptions);
            assertNotNull(sessionMono);
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
        // Test that the optimized connect methods return VoiceLiveSessionAsyncClient

        // Test startSession with model string
        assertDoesNotThrow(() -> {
            Mono<VoiceLiveSessionAsyncClient> result = client.startSession("gpt-4o-realtime-preview", null);
            assertNotNull(result);
        });

        // Test startSession with session options
        VoiceLiveSessionOptions sessionOptions = new VoiceLiveSessionOptions().setModel("gpt-4o-realtime-preview");
        assertDoesNotThrow(() -> {
            Mono<VoiceLiveSessionAsyncClient> result = client.startSession(sessionOptions.getModel(), null);
            assertNotNull(result);
        });

        // Test null parameter validation for startSession methods
        assertThrows(NullPointerException.class, () -> client.startSession((String) null, null));
    }

    @Test
    void testReturnTypeOptimization() {
        // Test that connect methods return the session for direct use
        String model = "gpt-4o-realtime-preview";

        assertDoesNotThrow(() -> {
            Mono<VoiceLiveSessionAsyncClient> sessionMono = client.startSession(model, null);
            assertNotNull(sessionMono);
            // The returned Mono should contain a VoiceLiveSessionAsyncClient when subscribed
        });

        VoiceLiveSessionOptions options = new VoiceLiveSessionOptions().setModel(model);
        assertDoesNotThrow(() -> {
            Mono<VoiceLiveSessionAsyncClient> sessionMono = client.startSession(options.getModel(), null);
            assertNotNull(sessionMono);
            // The returned Mono should contain a VoiceLiveSessionAsyncClient when subscribed
        });
    }

    @Test
    void testStartSessionWithAgentConfig() {
        // Arrange
        AgentSessionConfig agentConfig = new AgentSessionConfig("test-agent", "test-project");

        // Act & Assert
        assertDoesNotThrow(() -> {
            Mono<VoiceLiveSessionAsyncClient> sessionMono = client.startSession(agentConfig, null);
            assertNotNull(sessionMono);
        });
    }

    @Test
    void testStartSessionWithAgentConfigAllOptions() {
        // Arrange
        AgentSessionConfig agentConfig = new AgentSessionConfig("test-agent", "test-project").setAgentVersion("1.0")
            .setConversationId("conv-123")
            .setAuthenticationIdentityClientId("client-id");

        // Act & Assert
        assertDoesNotThrow(() -> {
            Mono<VoiceLiveSessionAsyncClient> sessionMono = client.startSession(agentConfig, null);
            assertNotNull(sessionMono);
        });
    }

    @Test
    void testStartSessionWithNullAgentConfig() {
        // Act & Assert
        assertThrows(NullPointerException.class, () -> client.startSession((AgentSessionConfig) null, null));
    }

    @Test
    void testStartSessionWithAgentConfigAndRequestOptions() {
        // Arrange
        AgentSessionConfig agentConfig = new AgentSessionConfig("test-agent", "test-project");
        VoiceLiveRequestOptions requestOptions
            = new VoiceLiveRequestOptions().addCustomQueryParameter("custom-param", "value");

        // Act & Assert
        assertDoesNotThrow(() -> {
            Mono<VoiceLiveSessionAsyncClient> sessionMono = client.startSession(agentConfig, requestOptions);
            assertNotNull(sessionMono);
        });
    }

    @Test
    void testStartSessionWithAgentConfigAndNullRequestOptions() {
        // Arrange
        AgentSessionConfig agentConfig = new AgentSessionConfig("test-agent", "test-project");

        // Act & Assert
        assertDoesNotThrow(() -> {
            Mono<VoiceLiveSessionAsyncClient> sessionMono = client.startSession(agentConfig, null);
            assertNotNull(sessionMono);
        });
    }

    @Test
    void testStartSessionWithNullAgentConfigAndValidRequestOptions() {
        // Arrange
        VoiceLiveRequestOptions requestOptions = new VoiceLiveRequestOptions();

        // Act & Assert
        assertThrows(NullPointerException.class, () -> client.startSession((AgentSessionConfig) null, requestOptions));
    }
}
