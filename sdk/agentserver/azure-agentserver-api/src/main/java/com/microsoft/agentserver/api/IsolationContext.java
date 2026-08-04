// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.agentserver.api;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Carries the platform-injected isolation keys for a single request.
 * The Foundry platform sets {@code x-agent-user-isolation-key} and
 * {@code x-agent-chat-isolation-key} headers on every protocol request.
 * These opaque partition keys let handlers scope user-private and
 * conversation-shared state without inspecting user identity.
 * <p>
 * <b>User isolation key</b> — the partition for data that belongs to the
 * individual who initiated the request (e.g., OAuth tokens, personal memory,
 * per-user preferences, cache entries). Stable for a given user across sessions.
 * <p>
 * <b>Chat isolation key</b> — the partition for conversation-scoped state
 * (e.g., conversation history, turn state, shared files). In a 1:1 user↔agent
 * chat this equals the user isolation key. It differs only in shared-surface
 * scenarios (e.g., a Teams group chat) where it represents the common partition
 * all participants write to.
 * <p>
 * Both keys are opaque, platform-generated, and scoped to the agent — data
 * cannot leak between agents. Neither key is guaranteed to be present when
 * running locally (outside the platform); handlers should handle {@code null}
 * values gracefully (e.g., fall back to a default partition).
 */
public record IsolationContext(String userIsolationKey, String chatIsolationKey) {

    /**
     * An empty {@link IsolationContext} with both keys {@code null}.
     * Used when the platform headers are absent (e.g., local development).
     */
    public static final IsolationContext EMPTY = new IsolationContext(null, null);

    /**
     * Creates a new {@link IsolationContext} with the given keys.
     *
     * @param userIsolationKey the value of the {@code x-agent-user-isolation-key} header,
     *                         or {@code null} if the header was absent.
     * @param chatIsolationKey the value of the {@code x-agent-chat-isolation-key} header,
     *                         or {@code null} if the header was absent.
     */
    public IsolationContext {
    }

    /**
     * Extracts isolation keys from a map of HTTP headers (case-insensitive lookup).
     * Returns {@link #EMPTY} when neither platform header is present.
     *
     * @param headers a map of header name → value (case-insensitive keys recommended).
     * @return an {@link IsolationContext} with the extracted keys.
     */
    public static IsolationContext fromHeaders(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return EMPTY;
        }
        String userKey = normalizeHeaderValue(headers.get(PlatformHeaders.USER_ISOLATION_KEY));
        String chatKey = normalizeHeaderValue(headers.get(PlatformHeaders.CHAT_ISOLATION_KEY));

        if (userKey == null && chatKey == null) {
            return EMPTY;
        }
        return new IsolationContext(userKey, chatKey);
    }

    /**
     * Extracts client headers (those prefixed with {@code x-client-}) from a map of
     * HTTP request headers. The prefix is preserved in the returned keys.
     *
     * @param headers a map of all request headers.
     * @return an unmodifiable map of client headers (may be empty, never null).
     */
    public static Map<String, String> extractClientHeaders(Map<String, String> headers) {
        if (headers == null || headers.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> clientHeaders = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            String key = entry.getKey();
            if (key != null && key.toLowerCase().startsWith(PlatformHeaders.CLIENT_HEADER_PREFIX)) {
                clientHeaders.put(key.toLowerCase(), entry.getValue());
            }
        }
        return Collections.unmodifiableMap(clientHeaders);
    }

    /**
     * Gets the user isolation key — the partition under which
     * <b>user-private</b> state should be stored.
     *
     * @return an opaque string from {@code x-agent-user-isolation-key},
     * or {@code null} when running outside the platform.
     */
    @Override
    public String userIsolationKey() {
        return userIsolationKey;
    }

    /**
     * Gets the chat isolation key — the partition under which
     * <b>conversation / shared</b> state should be stored.
     *
     * @return an opaque string from {@code x-agent-chat-isolation-key},
     * or {@code null} when running outside the platform.
     * In a 1:1 user↔agent chat this equals {@link #userIsolationKey ()}.
     */
    @Override
    public String chatIsolationKey() {
        return chatIsolationKey;
    }

    /**
     * Returns {@code true} if both isolation keys are absent.
     */
    public boolean isEmpty() {
        return userIsolationKey == null && chatIsolationKey == null;
    }

    private static String normalizeHeaderValue(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }
        return value.trim();
    }
}

