// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai.responses.models;

import com.azure.core.annotation.Generated;
import com.azure.core.annotation.Immutable;
import com.azure.core.util.BinaryData;
import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * The ResponsesFunctionTool model.
 */
@Immutable
public final class ResponsesFunctionTool extends ResponsesTool {

    /*
     * The type property.
     */
    @Generated
    private ResponsesToolType type = ResponsesToolType.FUNCTION;

    /*
     * The name property.
     */
    @Generated
    private final String name;

    /*
     * The description property.
     */
    @Generated
    private final String description;

    /*
     * The parameters property.
     */
    @Generated
    private final BinaryData parameters;

    /*
     * The strict property.
     */
    @Generated
    private final boolean strict;

    /**
     * Creates an instance of ResponsesFunctionTool class.
     *
     * @param name the name value to set.
     * @param description the description value to set.
     * @param parameters the parameters value to set.
     * @param strict the strict value to set.
     */
    @Generated
    public ResponsesFunctionTool(String name, String description, BinaryData parameters, boolean strict) {
        this.name = name;
        this.description = description;
        this.parameters = parameters;
        this.strict = strict;
    }

    /**
     * Get the type property: The type property.
     *
     * @return the type value.
     */
    @Generated
    @Override
    public ResponsesToolType getType() {
        return this.type;
    }

    /**
     * Get the name property: The name property.
     *
     * @return the name value.
     */
    @Generated
    public String getName() {
        return this.name;
    }

    /**
     * Get the description property: The description property.
     *
     * @return the description value.
     */
    @Generated
    public String getDescription() {
        return this.description;
    }

    /**
     * Get the parameters property: The parameters property.
     *
     * @return the parameters value.
     */
    @Generated
    public BinaryData getParameters() {
        return this.parameters;
    }

    /**
     * Get the strict property: The strict property.
     *
     * @return the strict value.
     */
    @Generated
    public boolean isStrict() {
        return this.strict;
    }

    /**
     * {@inheritDoc}
     */
    @Generated
    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();
        jsonWriter.writeStringField("name", this.name);
        jsonWriter.writeStringField("description", this.description);
        jsonWriter.writeFieldName("parameters");
        this.parameters.writeTo(jsonWriter);
        jsonWriter.writeBooleanField("strict", this.strict);
        jsonWriter.writeStringField("type", this.type == null ? null : this.type.toString());
        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of ResponsesFunctionTool from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ResponsesFunctionTool if the JsonReader was pointing to an instance of it, or null if it
     * was pointing to JSON null.
     * @throws IllegalStateException If the deserialized JSON object was missing any required properties.
     * @throws IOException If an error occurs while reading the ResponsesFunctionTool.
     */
    @Generated
    public static ResponsesFunctionTool fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String name = null;
            String description = null;
            BinaryData parameters = null;
            boolean strict = false;
            ResponsesToolType type = ResponsesToolType.FUNCTION;
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();
                if ("name".equals(fieldName)) {
                    name = reader.getString();
                } else if ("description".equals(fieldName)) {
                    description = reader.getString();
                } else if ("parameters".equals(fieldName)) {
                    parameters
                        = reader.getNullable(nonNullReader -> BinaryData.fromObject(nonNullReader.readUntyped()));
                } else if ("strict".equals(fieldName)) {
                    strict = reader.getBoolean();
                } else if ("type".equals(fieldName)) {
                    type = ResponsesToolType.fromString(reader.getString());
                } else {
                    reader.skipChildren();
                }
            }
            ResponsesFunctionTool deserializedResponsesFunctionTool
                = new ResponsesFunctionTool(name, description, parameters, strict);
            deserializedResponsesFunctionTool.type = type;
            return deserializedResponsesFunctionTool;
        });
    }
}
