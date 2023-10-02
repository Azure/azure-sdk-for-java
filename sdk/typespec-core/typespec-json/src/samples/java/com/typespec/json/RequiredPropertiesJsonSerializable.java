// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.json;

import java.io.IOException;

/**
 * {@link JsonSerializable} with required properties.
 */
public class RequiredPropertiesJsonSerializable implements JsonSerializable<RequiredPropertiesJsonSerializable> {
    private final int anInt;
    private final boolean aBoolean;
    private String aString;
    private Double aNullableDecimal;

    /**
     * Creates an instance of {@link RequiredPropertiesJsonSerializable}.
     *
     * @param anInt The integer value.
     * @param aBoolean The boolean value.
     */
    public RequiredPropertiesJsonSerializable(int anInt, boolean aBoolean) {
        this.anInt = anInt;
        this.aBoolean = aBoolean;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeIntField("int", anInt)
            .writeBooleanField("boolean", aBoolean)
            .writeStringField("string", aString)
            .writeNumberField("decimal", aNullableDecimal)
            .writeEndObject();
    }

    /**
     * Reads an instance of RequiredPropertiesJsonSerializable from the JsonReader.
     * <p>
     * If this static method doesn't exist calling this method will use the implementation on {@link JsonSerializable}
     * that will throw an {@link UnsupportedOperationException} as the base implementation doesn't know which type to
     * deserialize.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of RequiredPropertiesJsonSerializable if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the RequiredPropertiesJsonSerializable.
     * @throws IllegalStateException If any required properties aren't found.
     */
    public static RequiredPropertiesJsonSerializable fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            // RequiredPropertiesJsonSerializable has required properties so the deserialized value has to be created
            // at the end. Use local variables to keep track of values found and flag to determine if required
            // properties have been found. The value of required properties cannot be used to determine if it was found
            // as the JSON value could be the same as the default value for the type.
            int anInt = 0;
            boolean foundAnInt = false;
            boolean aBoolean = false;
            boolean foundABoolean = false;
            String aString = null;
            Double aNullableDecimal = null;

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
                    reader.skipChildren();
                }
            }

            // Check that all required properties have been found.
            if (foundAnInt && foundABoolean) {
                RequiredPropertiesJsonSerializable value = new RequiredPropertiesJsonSerializable(anInt, aBoolean);
                value.aString = aString;
                value.aNullableDecimal = aNullableDecimal;

                return value;
            }

            // If not all required properties are found throw an exception.
            throw new IllegalStateException(
                "Missing required JSON properties. Required properties are 'int' and 'boolean'.");
        });
    }
}
