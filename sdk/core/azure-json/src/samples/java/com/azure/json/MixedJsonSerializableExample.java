// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json;

// BEGIN: com.azure.json.JsonSerializable.mixed

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Implementation of JsonSerializable where some properties are set in the constructor and some properties are set
 * using fluent methods.
 */
public class MixedJsonSerializableExample implements JsonSerializable<MixedJsonSerializableExample> {
    private final int anInt;
    private final boolean aBoolean;
    private String aString;
    private Double aNullableDecimal;
    private Map<String, Object> additionalProperties;

    /**
     * Creates an instance of MixedJsonSerializableExample.
     *
     * @param anInt The integer value.
     * @param aBoolean The boolean value.
     */
    public MixedJsonSerializableExample(int anInt, boolean aBoolean) {
        this.anInt = anInt;
        this.aBoolean = aBoolean;
    }

    /**
     * Sets a string value.
     *
     * @param aString The string value.
     * @return The update MixedJsonSerializableExample
     */
    public MixedJsonSerializableExample setAString(String aString) {
        this.aString = aString;
        return this;
    }

    /**
     * Sets a nullable decimal value.
     *
     * @param aNullableDecimal The nullable decimal value.
     * @return The update MixedJsonSerializableExample
     */
    public MixedJsonSerializableExample setANullableDecimal(Double aNullableDecimal) {
        this.aNullableDecimal = aNullableDecimal;
        return this;
    }

    /**
     * Sets additional properties found while deserializing the JSON object.
     *
     * @param additionalProperties Additional JSON properties.
     * @return The update MixedJsonSerializableExample
     */
    public MixedJsonSerializableExample setAdditionalProperties(Map<String, Object> additionalProperties) {
        this.additionalProperties = additionalProperties;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject()
            .writeIntField("int", anInt)
            .writeBooleanField("boolean", aBoolean)
            .writeStringField("string", aString)
            .writeNullableField("decimal", aNullableDecimal, JsonWriter::writeDouble);

        // Include additional properties in JSON serialization.
        if (additionalProperties != null) {
            for (Map.Entry<String, Object> additionalProperty : additionalProperties.entrySet()) {
                jsonWriter.writeUntypedField(additionalProperty.getKey(), additionalProperty.getValue());
            }
        }

        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of MixedJsonSerializableExample from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of MixedJsonSerializableExample if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the MixedJsonSerializableExample.
     * @throws IllegalStateException If any of the required properties to create MixedJsonSerializableExample
     * aren't found.
     */
    public static MixedJsonSerializableExample fromJson(JsonReader jsonReader) throws IOException {
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

                // Example of case-insensitive names.
                if ("int".equalsIgnoreCase(fieldName)) {
                    anInt = reader.getInt();
                    foundAnInt = true;
                } else if ("boolean".equalsIgnoreCase(fieldName)) {
                    aBoolean = reader.getBoolean();
                    foundABoolean = true;
                } else if ("string".equalsIgnoreCase(fieldName)) {
                    aString = reader.getString();
                } else if ("decimal".equalsIgnoreCase(fieldName)) {
                    aNullableDecimal = reader.getNullable(JsonReader::getDouble);
                } else {
                    // Fallthrough case but the JSON property is maintained.
                    if (additionalProperties == null) {
                        // Maintain ordering of additional properties using a LinkedHashMap.
                        additionalProperties = new LinkedHashMap<>();
                    }

                    // Additional properties are unknown types, use 'readUntyped'.
                    additionalProperties.put(fieldName, reader.readUntyped());
                }
            }

            // Check that all required fields were found.
            if (foundAnInt && foundABoolean) {
                return new MixedJsonSerializableExample(anInt, aBoolean)
                    .setAString(aString)
                    .setANullableDecimal(aNullableDecimal)
                    .setAdditionalProperties(additionalProperties);
            }

            // If required fields were missing throw an exception.
            throw new IOException("Missing one, or more, required fields. Required fields are 'int' and 'boolean'.");
        });
    }
}
// END: com.azure.json.JsonSerializable.mixed
