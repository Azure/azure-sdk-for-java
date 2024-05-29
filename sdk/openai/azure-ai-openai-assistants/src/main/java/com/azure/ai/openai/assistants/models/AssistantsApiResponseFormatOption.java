package com.azure.ai.openai.assistants.models;

import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonWriter;

import java.io.IOException;

/**
 * Specifies the format that the model must output. Compatible with GPT-4 Turbo and all GPT-3.5 Turbo models since `gpt-3.5-turbo-1106`.
 * Setting to `{ "type": "json_object" }` enables JSON mode, which guarantees the message the model generates is valid JSON.
 * **Important:** when using JSON mode, you **must** also instruct the model to produce JSON yourself via a system or user message.
 * Without this, the model may generate an unending stream of whitespace until the generation reaches the token limit,
 * resulting in a long-running and seemingly "stuck" request. Also note that the message content may be partially cut off
 * if `finish_reason="length"`, which indicates the generation exceeded `max_tokens` or the conversation exceeded the max context length.
 */
@Immutable
public final class AssistantsApiResponseFormatOption implements JsonSerializable<AssistantsApiResponseFormatOption> {

    private final AssistantsApiToolChoiceOptionMode mode;

    private final AssistantsApiResponseFormat format;

    public AssistantsApiResponseFormatOption(AssistantsApiToolChoiceOptionMode mode) {
        this.mode = mode;
        this.format = null;
    }

    public AssistantsApiResponseFormatOption(AssistantsApiResponseFormat format) {
        this.mode = null;
        this.format = format;
    }


    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return null;
    }

    public AssistantsApiResponseFormatOption fromJson(JsonReader jsonReader) {
        return null;
    }
}
