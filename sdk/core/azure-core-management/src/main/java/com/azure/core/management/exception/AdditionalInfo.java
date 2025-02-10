// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.exception;

import com.azure.core.annotation.Immutable;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.IOException;

/**
 * An instance of this class provides additional information about a {@link ManagementError}.
 */
@Immutable
public final class AdditionalInfo implements JsonSerializable<AdditionalInfo> {
    /**
     * The type of additional info.
     */
    private final String type;

    /**
     * The additional info.
     */
    private final Object info;

    /**
     * Constructs a new {@link AdditionalInfo} object.
     *
     * @param type the type of addition info.
     * @param info the additional info.
     */
    @JsonCreator
    public AdditionalInfo(@JsonProperty("type") String type, @JsonProperty("info") Object info) {
        this.type = type;
        this.info = info;
    }

    /**
     * Gets the type of addition info.
     *
     * @return the type of addition info.
     */
    public String getType() {
        return this.type;
    }

    /**
     * Gets the additional info.
     *
     * @return the additional info.
     */
    public Object getInfo() {
        return this.info;
    }

    @Override
    public String toString() {
        return type == null ? super.toString() : type;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeStringField("type", type)
            .writeUntypedField("info", info)
            .writeEndObject();
    }

    /**
     * Reads a JSON stream into an {@link AdditionalInfo}.
     *
     * @param jsonReader The {@link JsonReader} being read.
     * @return The {@link AdditionalInfo} that the JSON stream represented, or null if it pointed to JSON null.
     * @throws IOException If an {@link AdditionalInfo} fails to be read from the {@code jsonReader}.
     */
    public static AdditionalInfo fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String type = null;
            Object info = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("type".equals(fieldName)) {
                    type = reader.getString();
                } else if ("info".equals(fieldName)) {
                    info = reader.readUntyped();
                } else {
                    reader.skipChildren();
                }
            }

            return new AdditionalInfo(type, info);
        });
    }
}
