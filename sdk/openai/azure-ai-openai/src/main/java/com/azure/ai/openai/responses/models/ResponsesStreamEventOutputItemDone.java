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
 * Returned when an output item is done streaming.
 */
@Immutable
public final class ResponsesStreamEventOutputItemDone extends ResponsesStreamEvent {

    /*
     * The type property.
     */
    @Generated
    private ResponsesStreamEventType type = ResponsesStreamEventType.RESPONSE_OUTPUT_ITEM_DONE;

    /*
     * The index of the output item within the response that this stream event applies to.
     */
    @Generated
    private final int outputIndex;

    /*
     * The completed output item.
     */
    @Generated
    private final ResponsesItem item;

    /**
     * Creates an instance of ResponsesStreamEventOutputItemDone class.
     *
     * @param outputIndex the outputIndex value to set.
     * @param item the item value to set.
     */
    @Generated
    private ResponsesStreamEventOutputItemDone(int outputIndex, ResponsesItem item) {
        this.outputIndex = outputIndex;
        this.item = item;
    }

    /**
     * Get the type property: The type property.
     *
     * @return the type value.
     */
    @Generated
    @Override
    public ResponsesStreamEventType getType() {
        return this.type;
    }

    /**
     * Get the outputIndex property: The index of the output item within the response that this stream event applies to.
     *
     * @return the outputIndex value.
     */
    @Generated
    public int getOutputIndex() {
        return this.outputIndex;
    }

    /**
     * Get the item property: The completed output item.
     *
     * @return the item value.
     */
    @Generated
    public ResponsesItem getItem() {
        return this.item;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeIntField("output_index", this.outputIndex);
        jsonWriter.writeJsonField("item", this.item);
        jsonWriter.writeStringField("type", this.type == null ? null : this.type.toString());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ResponsesStreamEventOutputItemDone from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ResponsesStreamEventOutputItemDone if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the ResponsesStreamEventOutputItemDone.
     */
    @Generated
    public static ResponsesStreamEventOutputItemDone fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            int outputIndex = 0;
            ResponsesItem item = null;
            ResponsesStreamEventType type = ResponsesStreamEventType.RESPONSE_OUTPUT_ITEM_DONE;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("output_index".equals(fieldName)) {
                    outputIndex = reader.getInt();
                } else if ("item".equals(fieldName)) {
                    item = ResponsesItem.fromJson(reader);
                } else if ("type".equals(fieldName)) {
                    type = ResponsesStreamEventType.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }
            ResponsesStreamEventOutputItemDone deserializedResponsesStreamEventOutputItemDone
                = new ResponsesStreamEventOutputItemDone(outputIndex, item);
            deserializedResponsesStreamEventOutputItemDone.type = type;
            return deserializedResponsesStreamEventOutputItemDone;
        });
    }
}
