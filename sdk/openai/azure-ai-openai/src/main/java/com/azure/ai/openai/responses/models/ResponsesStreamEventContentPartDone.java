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
 * Emitted when a content part is done.
 */
@Immutable
public final class ResponsesStreamEventContentPartDone extends ResponsesStreamEvent {

    /*
     * The type property.
     */
    @Generated
    private ResponsesStreamEventType type = ResponsesStreamEventType.RESPONSE_CONTENT_PART_DONE;

    /*
     * The ID of the item that this stream event applies to.
     */
    @Generated
    private final String itemId;

    /*
     * The index of the output item within the response that this stream event applies to.
     */
    @Generated
    private final int outputIndex;

    /*
     * The index of the content part that was added to an item's content collection.
     */
    @Generated
    private final int contentIndex;

    /*
     * The initialized content part that was added.
     */
    @Generated
    private final ResponsesContent part;

    /**
     * Creates an instance of ResponsesStreamEventContentPartDone class.
     *
     * @param itemId the itemId value to set.
     * @param outputIndex the outputIndex value to set.
     * @param contentIndex the contentIndex value to set.
     * @param part the part value to set.
     */
    @Generated
    private ResponsesStreamEventContentPartDone(String itemId, int outputIndex, int contentIndex,
        ResponsesContent part) {
        this.itemId = itemId;
        this.outputIndex = outputIndex;
        this.contentIndex = contentIndex;
        this.part = part;
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
     * Get the itemId property: The ID of the item that this stream event applies to.
     *
     * @return the itemId value.
     */
    @Generated
    public String getItemId() {
        return this.itemId;
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
     * Get the contentIndex property: The index of the content part that was added to an item's content collection.
     *
     * @return the contentIndex value.
     */
    @Generated
    public int getContentIndex() {
        return this.contentIndex;
    }

    /**
     * Get the part property: The initialized content part that was added.
     *
     * @return the part value.
     */
    @Generated
    public ResponsesContent getPart() {
        return this.part;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("item_id", this.itemId);
        jsonWriter.writeIntField("output_index", this.outputIndex);
        jsonWriter.writeIntField("content_index", this.contentIndex);
        jsonWriter.writeJsonField("part", this.part);
        jsonWriter.writeStringField("type", this.type == null ? null : this.type.toString());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ResponsesStreamEventContentPartDone from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ResponsesStreamEventContentPartDone if the JsonReader was pointing to an instance of it,
     * or null if it was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the ResponsesStreamEventContentPartDone.
     */
    @Generated
    public static ResponsesStreamEventContentPartDone fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String itemId = null;
            int outputIndex = 0;
            int contentIndex = 0;
            ResponsesContent part = null;
            ResponsesStreamEventType type = ResponsesStreamEventType.RESPONSE_CONTENT_PART_DONE;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("item_id".equals(fieldName)) {
                    itemId = reader.getString();
                } else if ("output_index".equals(fieldName)) {
                    outputIndex = reader.getInt();
                } else if ("content_index".equals(fieldName)) {
                    contentIndex = reader.getInt();
                } else if ("part".equals(fieldName)) {
                    part = ResponsesContent.fromJson(reader);
                } else if ("type".equals(fieldName)) {
                    type = ResponsesStreamEventType.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }
            ResponsesStreamEventContentPartDone deserializedResponsesStreamEventContentPartDone
                = new ResponsesStreamEventContentPartDone(itemId, outputIndex, contentIndex, part);
            deserializedResponsesStreamEventContentPartDone.type = type;
            return deserializedResponsesStreamEventContentPartDone;
        });
    }
}
