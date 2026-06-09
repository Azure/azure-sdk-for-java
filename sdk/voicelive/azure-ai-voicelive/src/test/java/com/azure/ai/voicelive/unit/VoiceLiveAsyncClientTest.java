// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.unit;

import com.azure.ai.voicelive.VoiceLiveAsyncClient;
import com.azure.ai.voicelive.models.AgentSessionConfig;
import com.azure.core.credential.KeyCredential;
import com.azure.core.http.HttpHeaders;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

/**
 * White-box unit tests for {@link VoiceLiveAsyncClient} covering its package-private constructor and the
 * {@code toQueryParameters} helper.
 * <p>
 * These members are intentionally package-private (internal API surface), so this test reaches them via reflection
 * rather than living in the {@code com.azure.ai.voicelive} package. Behavioral {@code startSession} tests live in
 * {@link VoiceLiveAsyncClientStartSessionTest}.
 */
class VoiceLiveAsyncClientTest {
    private final KeyCredential mockKeyCredential = new KeyCredential("fake");
    private final HttpHeaders mockHeaders = new HttpHeaders();

    private URI testEndpoint;
    private Constructor<VoiceLiveAsyncClient> keyCredentialConstructor;
    private Method toQueryParameters;

    @BeforeEach
    void setUp() throws Exception {
        testEndpoint = new URI("https://test.cognitiveservices.azure.com");
        keyCredentialConstructor = VoiceLiveAsyncClient.class.getDeclaredConstructor(URI.class, KeyCredential.class,
            String.class, HttpHeaders.class);
        keyCredentialConstructor.setAccessible(true);
        toQueryParameters = VoiceLiveAsyncClient.class.getDeclaredMethod("toQueryParameters", AgentSessionConfig.class);
        toQueryParameters.setAccessible(true);
    }

    private VoiceLiveAsyncClient newClient(URI endpoint, KeyCredential credential) throws Exception {
        return keyCredentialConstructor.newInstance(endpoint, credential, "2024-10-01-preview", mockHeaders);
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> toQueryParameters(AgentSessionConfig config) throws Exception {
        return (Map<String, String>) toQueryParameters.invoke(null, config);
    }

    @Test
    void testConstructorWithValidParameters() throws Exception {
        // Act & Assert
        assertNotNull(newClient(testEndpoint, mockKeyCredential));
    }

    @Test
    void testConstructorWithNullEndpoint() {
        // Act & Assert - the package-private constructor rejects a null endpoint with NPE
        InvocationTargetException thrown
            = assertThrows(InvocationTargetException.class, () -> newClient(null, mockKeyCredential));
        assertInstanceOf(NullPointerException.class, thrown.getCause());
    }

    @Test
    void testConstructorWithNullCredential() {
        // Act & Assert - the package-private constructor rejects a null credential with NPE
        InvocationTargetException thrown
            = assertThrows(InvocationTargetException.class, () -> newClient(testEndpoint, null));
        assertInstanceOf(NullPointerException.class, thrown.getCause());
    }

    @Test
    void testToQueryParametersWithRequiredOnly() throws Exception {
        AgentSessionConfig config = new AgentSessionConfig("my-agent", "my-project");

        Map<String, String> params = toQueryParameters(config);

        assertEquals(2, params.size());
        assertEquals("my-agent", params.get("agent-name"));
        assertEquals("my-project", params.get("agent-project-name"));
    }

    @Test
    void testToQueryParametersWithAllOptions() throws Exception {
        AgentSessionConfig config = new AgentSessionConfig("my-agent", "my-project").setAgentVersion("2.0")
            .setConversationId("conversation-xyz")
            .setAuthenticationIdentityClientId("auth-client-id")
            .setFoundryResourceOverride("override-resource");

        Map<String, String> params = toQueryParameters(config);

        assertEquals(6, params.size());
        assertEquals("my-agent", params.get("agent-name"));
        assertEquals("my-project", params.get("agent-project-name"));
        assertEquals("2.0", params.get("agent-version"));
        assertEquals("conversation-xyz", params.get("conversation-id"));
        assertEquals("auth-client-id", params.get("agent-authentication-identity-client-id"));
        assertEquals("override-resource", params.get("foundry-resource-override"));
    }

    @Test
    void testToQueryParametersExcludesEmptyOptionalValues() throws Exception {
        AgentSessionConfig config
            = new AgentSessionConfig("my-agent", "my-project").setAgentVersion("").setConversationId("");

        Map<String, String> params = toQueryParameters(config);

        assertEquals(2, params.size());
        assertFalse(params.containsKey("agent-version"));
        assertFalse(params.containsKey("conversation-id"));
    }
}
