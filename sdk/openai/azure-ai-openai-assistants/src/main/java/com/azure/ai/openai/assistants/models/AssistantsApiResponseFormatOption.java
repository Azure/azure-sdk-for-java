package com.azure.ai.openai.assistants.models;

import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
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

    /**
     * The mode in which the model will handle the return format of a tool call.
     */
    private final AssistantsApiToolChoiceOptionMode mode;

    /**
     * The format in which the model will handle the return format of a tool call.
     */
    private final AssistantsApiResponseFormat format;

    /**
     * Creates a new instance of AssistantsApiResponseFormatOption.
     *
     * @param mode The mode in which the model will handle the return format of a tool call.
     */
    public AssistantsApiResponseFormatOption(AssistantsApiToolChoiceOptionMode mode) {
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

    public AssistantsApiToolChoiceOptionMode getMode() {
        return this.mode;
    }

    public AssistantsApiResponseFormat getFormat() {
        return this.format;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        if (this.mode != null && this.format != null) {
            throw new IllegalArgumentException("Only set `mode` or `format` can be set, not both.");
        }

        if (this.mode != null) {
            jsonWriter.writeString(this.mode.toString());
        } else if (this.format != null) {
            jsonWriter.writeStartObject();
            jsonWriter.writeJson(this.format);
            jsonWriter.writeEndObject();
        }
        return jsonWriter;
    }

    /**
     * Reads an instance of AssistantsApiResponseFormatOption from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of AssistantsApiResponseFormatOption.
     */
    public AssistantsApiResponseFormatOption fromJson(JsonReader jsonReader) throws IOException {
        if(this.mode != null && this.format != null) {
            throw new IllegalArgumentException("Only set `mode` or `format` can be set, not both.");
        }

        JsonToken firstToken = jsonReader.nextToken();
        if (firstToken == JsonToken.START_OBJECT) {
            AssistantsApiResponseFormat format = jsonReader.readObject(AssistantsApiResponseFormat::fromJson);
            return new AssistantsApiResponseFormatOption(format);
        } else if (firstToken == JsonToken.STRING) {
            AssistantsApiToolChoiceOptionMode mode = AssistantsApiToolChoiceOptionMode.fromString(jsonReader.getString());
            return new AssistantsApiResponseFormatOption(mode);
        }

        return null;
    }
}
