// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json;

import java.io.IOException;

// BEGIN: com.azure.json.JsonSerializable.immutable

/**
 * Implementation of JsonSerializable where all properties are set in the constructor.
 */
public class ImmutableJsonSerializableExample implements JsonSerializable<FluentJsonSerializableExample> {
    private final int anInt;
    private final boolean aBoolean;
    private final String aString;
    private final Double aNullableDecimal;

    /**
     * Creates an instance of ImmutableJsonSerializableExample.
     *
     * @param anInt The integer value.
     * @param aBoolean The boolean value.
     * @param aString The string value.
     * @param aNullableDecimal The nullable decimal value.
     */
    public ImmutableJsonSerializableExample(int anInt, boolean aBoolean, String aString, Double aNullableDecimal) {
        // This constructor could be made package-private or private as 'fromJson' has access to internal APIs.
        this.anInt = anInt;
        this.aBoolean = aBoolean;
        this.aString = aString;
        this.aNullableDecimal = aNullableDecimal;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeIntField("int", anInt)
            .writeBooleanField("boolean", aBoolean)
            .writeStringField("string", aString)
            // 'writeNullableField' will always write a field, even if the value is null.
            .writeNullableField("decimal", aNullableDecimal, JsonWriter::writeDouble)
            .writeEndObject()
            // In this case 'toJson' eagerly flushes the JsonWriter.
            // Flushing too often may result in performance penalties.
            .flush();
    }

    /**
     * Reads an instance of ImmutableJsonSerializableExample from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of ImmutableJsonSerializableExample if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the ImmutableJsonSerializableExample.
     * @throws IllegalStateException If any of the required properties to create ImmutableJsonSerializableExample
     * aren't found.
     */
    public static ImmutableJsonSerializableExample fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            // Local variables to keep track of what values have been found.
            // Some properties have a corresponding 'boolean found<Name>' to track if a JSON property with that name
            // was found. If the value wasn't found an exception will be thrown at the end of reading the object.
            int anInt = 0;
            boolean foundAnInt = false;
            boolean aBoolean = false;
            boolean foundABoolean = false;
            String aString = null;
            Double aNullableDecimal = null;

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
                    reader.skipChildren();
                }
            }

            // Check that all required fields were found.
            if (foundAnInt && foundABoolean) {
                return new ImmutableJsonSerializableExample(anInt, aBoolean, aString, aNullableDecimal);
            }

            // If required fields were missing throw an exception.
            throw new IOException("Missing one, or more, required fields. Required fields are 'int' and 'boolean'.");
        });
    }
}
// END: com.azure.json.JsonSerializable.immutable
