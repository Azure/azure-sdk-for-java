// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.implementation.telemetry;

import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.TypeReference;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Collections;
import java.util.Locale;
import java.util.Map;

/**
 * Utility methods for VoiceLive telemetry payload parsing and conversion.
 */
public final class VoiceLiveTelemetryUtils {

    private static final ClientLogger LOGGER = new ClientLogger(VoiceLiveTelemetryUtils.class);
    private static final TypeReference<Map<String, Object>> MAP_TYPE = new TypeReference<Map<String, Object>>() {
    };

    /**
     * Parse a JSON string into a Map.
     *
     * @param json The JSON string to parse.
     * @return A Map representation of the JSON, or empty map if json is null/empty/invalid.
     */
    public static Map<String, Object> parseJsonObject(String json) {
        if (json == null || json.isEmpty()) {
            return Collections.emptyMap();
        }
        try {
            Map<String, Object> parsed = BinaryData.fromString(json).toObject(MAP_TYPE);
            return parsed == null ? Collections.emptyMap() : parsed;
        } catch (RuntimeException ex) {
            LOGGER.atVerbose().log("Failed to parse telemetry payload as JSON", ex);
            return Collections.emptyMap();
        }
    }

    /**
     * Get a nested map value.
     *
     * @param value The object to cast.
     * @return A Map, or null if value is not a map.
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> getMap(Object value) {
        return value instanceof Map<?, ?> ? (Map<String, Object>) value : null;
    }

    /**
     * Convert value to String.
     *
     * @param value The object to convert.
     * @return String representation or null if value is null.
     */
    public static String getString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    /**
     * Convert value to Long.
     *
     * @param value The object to convert.
     * @return Long value or null if value is not a number or parseable long.
     */
    public static Long getLong(Object value) {
        if (value instanceof Number) {
            return ((Number) value).longValue();
        }
        if (value instanceof String) {
            try {
                return Long.parseLong(value.toString());
            } catch (NumberFormatException ignored) {
                return null;
            }
        }
        return null;
    }

    /**
     * Convert value to Boolean.
     *
     * @param value The object to convert.
     * @return Boolean value or null if value is not boolean or parseable.
     */
    public static Boolean getBoolean(Object value) {
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        if (value instanceof String) {
            return Boolean.parseBoolean(value.toString());
        }
        return null;
    }

    /**
     * Parse a JSON value string (handles nested objects).
     *
     * @param value The JSON value string.
     * @return The parsed object, or the original string if parse fails.
     */
    public static Object parseJsonValue(String value) {
        if (value == null) {
            return null;
        }
        try {
            return BinaryData.fromString(value).toObject(Object.class);
        } catch (RuntimeException ex) {
            return value;
        }
    }

    /**
     * Serialize an object to JSON.
     *
     * @param value The object to serialize.
     * @return JSON string representation.
     */
    public static String serializeJson(Object value) {
        return BinaryData.fromObject(value).toString();
    }

    /**
     * Return the first non-blank string.
     *
     * @param first First candidate.
     * @param second Second candidate.
     * @return The first non-blank string, or null.
     */
    public static String firstNonBlank(String first, String second) {
        if (first != null && !first.trim().isEmpty()) {
            return first;
        }
        if (second != null && !second.trim().isEmpty()) {
            return second;
        }
        return null;
    }

    /**
     * Get the byte length of a string payload in UTF-8.
     *
     * @param payload The payload string.
     * @return The UTF-8 byte length, or 0 if null.
     */
    public static long messageSize(String payload) {
        return payload == null ? 0 : payload.getBytes(StandardCharsets.UTF_8).length;
    }

    /**
     * Decode a base64 string and return the byte length.
     *
     * @param value The base64 string.
     * @return The decoded byte length, or UTF-8 byte length if decode fails.
     */
    public static long base64Length(String value) {
        if (value == null) {
            return 0;
        }
        try {
            return Base64.getDecoder().decode(value).length;
        } catch (IllegalArgumentException ex) {
            LOGGER.atVerbose().log("Failed to decode base64 payload for telemetry byte counting", ex);
            return value.getBytes(StandardCharsets.UTF_8).length;
        }
    }

    /**
     * Get the default port for a scheme.
     *
     * @param scheme The URI scheme (http, https, ws, wss, etc).
     * @return The default port (80 for http/ws, 443 for others).
     */
    public static long defaultPort(String scheme) {
        if (scheme == null) {
            return 443;
        }
        String normalized = scheme.toLowerCase(Locale.ROOT);
        if ("http".equals(normalized) || "ws".equals(normalized)) {
            return 80;
        }
        return 443;
    }

    // Prevent instantiation
    private VoiceLiveTelemetryUtils() {
    }
}
