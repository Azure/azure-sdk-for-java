// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.assistants.models;

import com.azure.core.annotation.Immutable;
import com.azure.core.util.BinaryData;

import java.io.UncheckedIOException;

// Convenience class added for the purpose of handling a wire type that can be either a JSON object or a string.
// The class still uses the documentation from the original union in the service spec.
/**
 * Specifies the format that the model must output. Compatible with GPT-4 Turbo and all GPT-3.5 Turbo models since `gpt-3.5-turbo-1106`.
 * Setting to `{ "type": "json_object" }` enables JSON mode, which guarantees the message the model generates is valid JSON.
 * **Important:** when using JSON mode, you **must** also instruct the model to produce JSON yourself via a system or user message.
 * Without this, the model may generate an unending stream of whitespace until the generation reaches the token limit,
 * resulting in a long-running and seemingly "stuck" request. Also note that the message content may be partially cut off
 * if `finish_reason="length"`, which indicates the generation exceeded `max_tokens` or the conversation exceeded the max context length.
 */
@Immutable
public final class AssistantsApiResponseFormatOption {

    /**
     * The mode in which the model will handle the return format of a tool call.
     */
    private final AssistantsApiResponseFormatMode mode;

    /**
     * The format in which the model will handle the return format of a tool call.
     */
    private final AssistantsApiResponseFormat format;

    /**
     * Creates a new instance of AssistantsApiResponseFormatOption.
     *
     * @param mode The mode in which the model will handle the return format of a tool call.
     */
    public AssistantsApiResponseFormatOption(AssistantsApiResponseFormatMode mode) {
        this.mode = mode;
        this.format = null;
    }

    /**
     * Creates a new instance of AssistantsApiResponseFormatOption.
     *
     * @param format The format in which the model will handle the return format of a tool call.
     */
    public AssistantsApiResponseFormatOption(AssistantsApiResponseFormat format) {
        this.mode = null;
        this.format = format;
    }

    /**
     * Gets the mode in which the model will handle the return format of a tool call.
     *
     * @return The mode in which the model will handle the return format of a tool call.
     */
    public AssistantsApiResponseFormatMode getMode() {
        return this.mode;
    }

    /**
     * Gets the format in which the model will handle the return format of a tool call.
     *
     * @return The format in which the model will handle the return format of a tool call.
     */
    public AssistantsApiResponseFormat getFormat() {
        return this.format;
    }

    /**
     * Creates a new instance of AssistantsApiResponseFormatOption based on a JSON string.
     *
     * @param responseFormatBinaryData input JSON string
     * @return a new instance of AssistantsApiResponseFormatOption
     * @throws IllegalArgumentException If the provided JSON string does not match the expected format.
     */
    public static AssistantsApiResponseFormatOption fromBinaryData(BinaryData responseFormatBinaryData) {
        if (responseFormatBinaryData == null) {
            return null;
        }
        try {
            AssistantsApiResponseFormat format = responseFormatBinaryData.toObject(AssistantsApiResponseFormat.class);
            if (format != null) {
                return new AssistantsApiResponseFormatOption(format);
            }
        } catch (UncheckedIOException e) {
            AssistantsApiResponseFormatMode mode = responseFormatBinaryData.toObject(AssistantsApiResponseFormatMode.class);
            // AssistantsApiResponseFormatMode is an expandable union, so if we get `null` as result, that will be returned as a new string value variant.
            if (AssistantsApiResponseFormatMode.values().contains(mode)) {
                return new AssistantsApiResponseFormatOption(mode);
            }
        }
        throw new IllegalArgumentException("The provided JSON string does not match the expected format.");
    }
}
