// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.json;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * {@link JsonSerializable} implementation with additional properties.
 */
public class AdditionalPropertiesJsonSerializable implements JsonSerializable<AdditionalPropertiesJsonSerializable> {
    private final int anInt;
    private final boolean aBoolean;
    private String aString;
    private Double aNullableDecimal;
    private Map<String, Object> additionalProperties;

    /**
     * Creates an instance of {@link AdditionalPropertiesJsonSerializable}.
     *
     * @param anInt The integer value.
     * @param aBoolean The boolean value.
     */
    public AdditionalPropertiesJsonSerializable(int anInt, boolean aBoolean) {
        this.anInt = anInt;
        this.aBoolean = aBoolean;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject()
            .writeIntField("int", anInt)
            .writeBooleanField("boolean", aBoolean)
            .writeStringField("string", aString)
            .writeNumberField("decimal", aNullableDecimal);

        // If there are additional properties they will be written as additional JSON properties.
        if (additionalProperties != null) {
            for (Map.Entry<String, Object> additionalProperty : additionalProperties.entrySet()) {
                // Additional properties are written as untyped values as the value type is unknown.
                jsonWriter.writeUntypedField(additionalProperty.getKey(), additionalProperty.getValue());
            }
        }

        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of AdditionalPropertiesJsonSerializable from the JsonReader.
     * <p>
     * If this static method doesn't exist calling this method will use the implementation on {@link JsonSerializable}
     * that will throw an {@link UnsupportedOperationException} as the base implementation doesn't know which type to
     * deserialize.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of AdditionalPropertiesJsonSerializable if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the AdditionalPropertiesJsonSerializable.
     * @throws IllegalStateException If any required properties aren't found.
     */
    public static AdditionalPropertiesJsonSerializable fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            int anInt = 0;
            boolean foundAnInt = false;
            boolean aBoolean = false;
            boolean foundABoolean = false;
            String aString = null;
            Double aNullableDecimal = null;
            Map<String, Object> additionalProperties = null;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("int".equals(fieldName)) {
                    anInt = reader.getInt();
                    foundAnInt = true;
                } else if ("boolean".equals(fieldName)) {
                    aBoolean = reader.getBoolean();
                    foundABoolean = true;
                } else if ("string".equals(fieldName)) {
                    aString = reader.getString();
                } else if ("decimal".equals(fieldName)) {
                    aNullableDecimal = reader.getNullable(JsonReader::getDouble);
                } else {
                    if (additionalProperties == null) {
                        additionalProperties = new LinkedHashMap<>();
                    }

                    // Additional properties are of unknown type, use 'JsonReader.readUntyped' to parse them. If the
                    // current token is a JSON primitive it's read as a primitive value, JSON array is read as a List,
                    // JSON object as a Map, and JSON null as null.
                    additionalProperties.put(fieldName, reader.readUntyped());
                }
            }

            // Check that all required properties have been found.
            if (foundAnInt && foundABoolean) {
                AdditionalPropertiesJsonSerializable value = new AdditionalPropertiesJsonSerializable(anInt, aBoolean);
                value.aString = aString;
                value.aNullableDecimal = aNullableDecimal;
                value.additionalProperties = additionalProperties;

                return value;
            }

            // If not all required properties are found throw an exception.
            throw new IllegalStateException(
                "Missing required JSON properties. Required properties are 'int' and 'boolean'.");
        });
    }
}
