// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api;

import com.openai.core.JsonValue;
import com.openai.models.responses.ResponseCreateParams;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests for {@link SessionIdResolver}.
 */
class SessionIdResolverTest {

    private static AgentServerCreateResponse req(java.util.function.Consumer<ResponseCreateParams.Builder> tweak) {
        ResponseCreateParams.Builder b = ResponseCreateParams.builder()
            .input("hi").model("test-model");
        tweak.accept(b);
        return new AgentServerCreateResponse(null, b.build()._body());
    }

    private static AgentServerCreateResponse req() {
        return req(b -> {
        });
    }

    @Test
    @DisplayName("Tier 1: payload field agent_session_id wins over everything")
    void tier1Payload() {
        ResponseCreateParams.Body body = ResponseCreateParams.builder()
            .input("hi").model("test-model")
            .build()
            ._body()
            .toBuilder()
            .putAdditionalProperty("agent_session_id", JsonValue.from("client-supplied"))
            .build();
        AgentServerCreateResponse request = new AgentServerCreateResponse(null, body);

        // Even with env var present, tier-1 wins.
        assertEquals("client-supplied",
            SessionIdResolver.resolve(request, "env-session-id"));
    }

    @Test
    @DisplayName("Tier 2: FOUNDRY_AGENT_SESSION_ID is used when no payload field")
    void tier2EnvVar() {
        assertEquals("env-session-id",
            SessionIdResolver.resolve(req(), "env-session-id"));
    }

    @Test
    @DisplayName("Tier 3: no payload and no env → derived 63-char lowercase hex")
    void tier3DerivedRandomWhenNoContext() {
        String a = SessionIdResolver.resolve(req(), null);
        String b = SessionIdResolver.resolve(req(), "");
        // 63 lowercase hex chars
        assertEquals(63, a.length());
        assertTrue(a.matches("[0-9a-f]{63}"), "expected 63-char lowercase hex, got: " + a);
        // Without conversational context, the result is random — two calls differ.
        assertNotEquals(a, b);
    }

    @Test
    @DisplayName("Tier 3: deterministic when conversation_id is present")
    void tier3DeterministicWithConversation() {
        AgentServerCreateResponse request = req(b -> b.conversation("convXYZ"));
        String first = SessionIdResolver.resolve(request, null);
        String second = SessionIdResolver.resolve(request, null);
        assertEquals(first, second, "derivation must be deterministic for the same input");
        assertEquals(63, first.length());
        assertTrue(first.matches("[0-9a-f]{63}"));
    }

    @Test
    @DisplayName("Tier 3: conversation_id takes priority over previous_response_id")
    void tier3ConversationBeatsPrev() {
        AgentServerCreateResponse convReq = req(b -> b.conversation("convABC"));
        // Same conversation, different previous_response_id → same session ID.
        AgentServerCreateResponse convReqWithPrev = req(b -> b
            .conversation("convABC")
            .previousResponseId("caresp_" + "A".repeat(50)));
        assertEquals(
            SessionIdResolver.resolve(convReq, null),
            SessionIdResolver.resolve(convReqWithPrev, null));
    }

    @Test
    @DisplayName("Tier 3: previous_response_id used when conversation_id absent")
    void tier3FallsBackToPrevious() {
        String prevA = "caresp_AAAAAAAAAAAAAAAAAA" + "B".repeat(32);
        String prevB = "caresp_CCCCCCCCCCCCCCCCCC" + "D".repeat(32);
        // Different previous IDs → different session IDs (different partition keys).
        assertNotEquals(
            SessionIdResolver.resolve(req(b -> b.previousResponseId(prevA)), null),
            SessionIdResolver.resolve(req(b -> b.previousResponseId(prevB)), null));
    }

    @Test
    @DisplayName("Tier 3: different agent identity yields different derived session IDs")
    void tier3AgentIdentityMatters() {
        ResponseCreateParams.Body body = ResponseCreateParams.builder()
            .input("hi").model("test-model").conversation("conv1").build()._body();
        AgentServerCreateResponse a = new AgentServerCreateResponse(
            new AgentReference(AgentReferenceType.AGENT_REFERENCE, "agent-a", "1.0", null), body);
        AgentServerCreateResponse b = new AgentServerCreateResponse(
            new AgentReference(AgentReferenceType.AGENT_REFERENCE, "agent-b", "1.0", null), body);
        assertNotEquals(SessionIdResolver.resolve(a, null), SessionIdResolver.resolve(b, null));
    }
}


