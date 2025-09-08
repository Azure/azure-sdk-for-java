// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses.models;

import com.azure.core.annotation.Generated;
import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * The ResponsesReasoningItemSummaryElementSummaryText model.
 */
@Immutable
public final class ResponsesReasoningItemSummaryElementSummaryText extends ResponsesReasoningItemSummaryElement {

    /*
     * The type property.
     */
    @Generated
    private ResponsesReasoningItemSummaryType type = ResponsesReasoningItemSummaryType.SUMMARY_TEXT;

    /*
     * The text property.
     */
    @Generated
    private final String text;

    /**
     * Creates an instance of ResponsesReasoningItemSummaryElementSummaryText class.
     *
     * @param text the text value to set.
     */
    @Generated
    public ResponsesReasoningItemSummaryElementSummaryText(String text) {
        this.text = text;
    }

    /**
     * Get the type property: The type property.
     *
     * @return the type value.
     */
    @Generated
    @Override
    public ResponsesReasoningItemSummaryType getType() {
        return this.type;
    }

    /**
     * Get the text property: The text property.
     *
     * @return the text value.
     */
    @Generated
    public String getText() {
        return this.text;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("text", this.text);
        jsonWriter.writeStringField("type", this.type == null ? null : this.type.toString());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ResponsesReasoningItemSummaryElementSummaryText from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ResponsesReasoningItemSummaryElementSummaryText if the JsonReader was pointing to an
     * instance of it, or null if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the ResponsesReasoningItemSummaryElementSummaryText.
     */
    @Generated
    public static ResponsesReasoningItemSummaryElementSummaryText fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String text = null;
            ResponsesReasoningItemSummaryType type = ResponsesReasoningItemSummaryType.SUMMARY_TEXT;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("text".equals(fieldName)) {
                    text = reader.getString();
                } else if ("type".equals(fieldName)) {
                    type = ResponsesReasoningItemSummaryType.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }
            ResponsesReasoningItemSummaryElementSummaryText deserializedResponsesReasoningItemSummaryElementSummaryText
                = new ResponsesReasoningItemSummaryElementSummaryText(text);
            deserializedResponsesReasoningItemSummaryElementSummaryText.type = type;
            return deserializedResponsesReasoningItemSummaryElementSummaryText;
        });
    }
}
