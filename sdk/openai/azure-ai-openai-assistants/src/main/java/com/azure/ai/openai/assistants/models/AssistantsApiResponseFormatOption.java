package com.azure.ai.openai.assistants.models;

import com.azure.core.annotation.Immutable;

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

    public AssistantsApiResponseFormatMode getMode() {
        return this.mode;
    }

    public AssistantsApiResponseFormat getFormat() {
        return this.format;
    }
}
