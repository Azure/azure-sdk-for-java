// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses.models;

import com.azure.core.annotation.Generated;
import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * The ResponsesItem model.
 */
@Immutable
public class ResponsesItem implements JsonSerializable<ResponsesItem> {

    /*
     * The type property.
     */
    @Generated
    private ResponsesItemType type;

    /*
     * The id property.
     */
    @Generated
    private String id;

    /**
     * Creates an instance of ResponsesItem class.
     */
    @Generated
    public ResponsesItem() {
    }

    /**
     * Get the type property: The type property.
     *
     * @return the type value.
     */
    @Generated
    public ResponsesItemType getType() {
        return this.type;
    }

    /**
     * Get the id property: The id property.
     *
     * @return the id value.
     */
    @Generated
    public String getId() {
        return this.id;
    }

    /**
     * Set the id property: The id property.
     *
     * @param id the id value to set.
     * @return the ResponsesItem object itself.
     */
    @Generated
    ResponsesItem setId(String id) {
        this.id = id;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("type", this.type == null ? null : this.type.toString());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ResponsesItem from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ResponsesItem if the JsonReader was pointing to an instance of it, or null if it was
     * pointing to JSON null.
     * @throws IOException If an error occurs while reading the ResponsesItem.
     */
    @Generated
    public static ResponsesItem fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String discriminatorValue = null;
            try (JsonReader readerToUse = reader.bufferObject()) {
                // Prepare for reading
                readerToUse.nextToken();
                while (readerToUse.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = readerToUse.getFieldName();
                    readerToUse.nextToken();
                    if ("type".equals(fieldName)) {
                        discriminatorValue = readerToUse.getString();
                        break;
                    } else {
                        readerToUse.skipChildren();
                    }
                }
                // Use the discriminator value to determine which subtype should be deserialized.
                if ("message".equals(discriminatorValue)) {
                    return ResponsesMessage.fromJson(readerToUse.reset());
                } else if ("function_call".equals(discriminatorValue)) {
                    return ResponsesFunctionCallItem.fromJson(readerToUse.reset());
                } else if ("function_call_output".equals(discriminatorValue)) {
                    return ResponsesFunctionCallOutput.fromJson(readerToUse.reset());
                } else if ("computer_call".equals(discriminatorValue)) {
                    return ResponsesComputerCallItem.fromJson(readerToUse.reset());
                } else if ("computer_call_output".equals(discriminatorValue)) {
                    return ResponsesComputerCallOutputItem.fromJson(readerToUse.reset());
                } else if ("file_search_call".equals(discriminatorValue)) {
                    return ResponsesFileSearchCallItem.fromJson(readerToUse.reset());
                } else if ("item_reference".equals(discriminatorValue)) {
                    return ResponsesItemReferenceItem.fromJson(readerToUse.reset());
                } else if ("web_search_call".equals(discriminatorValue)) {
                    return ResponsesWebSearchCallItem.fromJson(readerToUse.reset());
                } else if ("reasoning".equals(discriminatorValue)) {
                    return ResponsesReasoningItem.fromJson(readerToUse.reset());
                } else {
                    return fromJsonKnownDiscriminator(readerToUse.reset());
                }
            }
        });
    }

    @Generated
    static ResponsesItem fromJsonKnownDiscriminator(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            ResponsesItem deserializedResponsesItem = new ResponsesItem();
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("type".equals(fieldName)) {
                    deserializedResponsesItem.type = ResponsesItemType.fromString(reader.getString());
                } else if ("id".equals(fieldName)) {
                    deserializedResponsesItem.id = reader.getString();
                } else {
                    reader.skipChildren();
                }
            }
            return deserializedResponsesItem;
        });
    }
}
