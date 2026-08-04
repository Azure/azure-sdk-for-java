// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api;

import com.microsoft.agentserver.api.implementation.IdGenerator;
import com.openai.core.JsonValue;
import com.openai.models.responses.ResponseCreateParams;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Locale;
import java.util.Optional;

/**
 * Resolves the {@code agent_session_id} for a response, per the Responses
 * the protocol spec and.
 * <p>
 * Priority chain:
 * <ol>
 *  <li>{@code request.agent_session_id} — payload-level client-supplied affinity.</li>
 *  <li>{@code FOUNDRY_AGENT_SESSION_ID} environment variable — platform-supplied.</li>
 *  <li>Deterministic derivation — SHA-256 of {@code "agentName:agentVersion:partitionHint"},
 *  first 63 lowercase hex characters. Falls back to a random 63-char lowercase
 *  hex when no conversational context (no {@code conversation_id} /
 *  {@code previous_response_id}) is present.</li>
 * </ol>
 * The resolved value is always 63 lowercase hex characters (or whatever the
 * client supplied at tier 1 — that string is returned unchanged).
 */
final class SessionIdResolver {

    /**
     * Output length of the deterministic derivation.
     */
    private static final int HEX_LENGTH = 63;

    /**
     * Default agent name when {@code agent_reference.name} is absent or empty.
     */
    private static final String DEFAULT_AGENT_NAME = "server-default-agent";

    private static final SecureRandom RANDOM = new SecureRandom();

    private SessionIdResolver() {
    }

    /**
     * Resolves the session ID for the given request.
     *
     * @param request      the create-response request (non-null)
     * @param envSessionId the value of {@code FOUNDRY_AGENT_SESSION_ID}, or {@code null}/empty
     * @return the resolved session ID (never null/empty)
     */
    static String resolve(AgentServerCreateResponse request, String envSessionId) {
        // Tier 1: client-supplied payload field.
        String fromPayload = extractPayloadSessionId(request);
        if (isNonEmpty(fromPayload)) {
            return fromPayload;
        }
        // Tier 2: platform-supplied environment variable.
        if (isNonEmpty(envSessionId)) {
            return envSessionId;
        }
        // Tier 3: deterministic derivation (or random if no context).
        return derive(request);
    }

    /**
     * Returns the {@code agent_session_id} value from the request body's top-level
     * additional properties, or {@code null} when absent / wrong type / empty.
     */
    private static String extractPayloadSessionId(AgentServerCreateResponse request) {
        try {
            ResponseCreateParams.Body body = request.responseCreateParams();
            if (body == null) {
                return null;
            }
            JsonValue val = body._additionalProperties().get("agent_session_id");
            if (val == null) {
                return null;
            }
            @SuppressWarnings("unchecked")
            Optional<String> str = val.asString();
            return str.filter(SessionIdResolver::isNonEmpty).orElse(null);
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Computes the deterministic session ID per Tier 3 of the algorithm.
     */
    private static String derive(AgentServerCreateResponse request) {
        ResponseCreateParams.Body body = request.responseCreateParams();

        // Select partition source: conversation_id wins, then previous_response_id,
        // else random.
        String partitionSource = null;
        if (body != null) {
            Optional<String> convId = body.conversation().flatMap(c -> {
                if (c.isId()) {
                    return Optional.of(c.asId());
                }
                if (c.isResponseConversationParam()) {
                    return Optional.of(c.asResponseConversationParam().id());
                }
                return Optional.<String>empty();
            }).filter(SessionIdResolver::isNonEmpty);

            if (convId.isPresent()) {
                partitionSource = convId.get();
            } else {
                Optional<String> prev = body.previousResponseId().filter(SessionIdResolver::isNonEmpty);
                if (prev.isPresent()) {
                    partitionSource = prev.get();
                }
            }
        }

        if (partitionSource == null) {
            return randomHex();
        }

        // Extract the 18-char partition key if the source is a well-formed Foundry ID;
        // otherwise use the raw value as the hint.
        String partitionHint;
        try {
            partitionHint = IdGenerator.extractPartitionKey(partitionSource);
        } catch (RuntimeException ex) {
            partitionHint = partitionSource;
        }

        // Agent identity.
        String agentName = DEFAULT_AGENT_NAME;
        String agentVersion = "";
        AgentReference ref = request.agent();
        if (ref != null) {
            if (isNonEmpty(ref.name())) {
                agentName = ref.name();
            }
            if (ref.version() != null) {
                agentVersion = ref.version();
            }
        }

        String seed = agentName + ":" + agentVersion + ":" + partitionHint;
        return sha256Hex(seed).substring(0, HEX_LENGTH);
    }

    private static String randomHex() {
        // 63 chars → 32 bytes (we'll truncate the trailing nibble).
        byte[] bytes = new byte[(HEX_LENGTH + 1) / 2];
        RANDOM.nextBytes(bytes);
        return toHex(bytes).substring(0, HEX_LENGTH);
    }

    private static String sha256Hex(String input) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                .digest(input.getBytes(StandardCharsets.UTF_8));
            return toHex(digest);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }

    private static String toHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(Character.forDigit((b >> 4) & 0xF, 16));
            sb.append(Character.forDigit(b & 0xF, 16));
        }
        // Already lowercase via Character.forDigit.
        return sb.toString().toLowerCase(Locale.ROOT);
    }

    private static boolean isNonEmpty(String s) {
        return s != null && !s.isEmpty();
    }
}

