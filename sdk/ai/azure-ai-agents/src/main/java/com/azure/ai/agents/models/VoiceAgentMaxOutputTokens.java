// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.models;

import com.azure.ai.agents.implementation.utils.Beta;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * An output-token limit for a voice agent, expressed as a token count or the model's maximum available output tokens.
 *
 * <p>Use {@code VoiceAgentMaxOutputTokens.of(1024)} for a numeric limit or {@link #MAXIMUM} for the {@code "inf"} variant.
 * The same type can be used with {@code setMaxOutputTokens} and {@code setMaxResponseOutputTokens}.
 * Pass {@code null} to a model's setter to clear the property.</p>
 *
 * <p>Unrecognized string values read from JSON are preserved when serialized again. For those values,
 * {@link #asInteger()} returns {@code null} and {@link #isMaximum()} returns {@code false}.</p>
 */
@Immutable
@Beta(warningText = "Preview API. VoiceAgents=V1Preview")
public final class VoiceAgentMaxOutputTokens implements JsonSerializable<VoiceAgentMaxOutputTokens> {
    private static final ClientLogger LOGGER = new ClientLogger(VoiceAgentMaxOutputTokens.class);

    /**
     * Uses the model's maximum available output tokens, represented as {@code "inf"} in JSON.
     */
    public static final VoiceAgentMaxOutputTokens MAXIMUM = new VoiceAgentMaxOutputTokens(null, "inf");

    private final Integer count;

    private final String stringValue;

    private VoiceAgentMaxOutputTokens(Integer count, String stringValue) {
        this.count = count;
        this.stringValue = stringValue;
    }

    /**
     * Creates a numeric output-token limit. The service validates the count for the selected model and operation.
     *
     * @param count the maximum number of output tokens.
     * @return the token limit.
     */
    public static VoiceAgentMaxOutputTokens of(int count) {
        return new VoiceAgentMaxOutputTokens(count, null);
    }

    /**
     * Gets the numeric output-token limit.
     *
     * @return the count, or {@code null} if this limit holds a string variant.
     */
    public Integer asInteger() {
        return count;
    }

    /**
     * Gets whether this limit uses the model's maximum available output tokens.
     *
     * @return {@code true} for the {@code "inf"} variant; otherwise, {@code false}.
     */
    public boolean isMaximum() {
        return "inf".equals(stringValue);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return count == null ? jsonWriter.writeString(stringValue) : jsonWriter.writeInt(count);
    }

    /**
     * Reads a token limit from a JSON integer or string.
     *
     * @param jsonReader the JSON reader.
     * @return the token limit, or {@code null} for JSON null.
     * @throws IOException if the JSON cannot be read.
     * @throws IllegalStateException if the value is not an integer representable as an int or a string.
     */
    public static VoiceAgentMaxOutputTokens fromJson(JsonReader jsonReader) throws IOException {
        Object value = jsonReader.readUntyped();
        if (value == null) {
            return null;
        }
        if (value instanceof Integer) {
            return of((Integer) value);
        }
        if (value instanceof String) {
            return "inf".equals(value) ? MAXIMUM : new VoiceAgentMaxOutputTokens(null, (String) value);
        }
        throw LOGGER.logExceptionAsError(new IllegalStateException("Token limit must be a JSON integer or string."));
    }
}
