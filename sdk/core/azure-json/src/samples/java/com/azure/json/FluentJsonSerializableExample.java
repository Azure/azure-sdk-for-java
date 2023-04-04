// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json;

import java.io.IOException;

// BEGIN: com.azure.json.JsonSerializable.fluent

/**
 * Implementation of JsonSerializable where all properties are fluently set.
 */
public class FluentJsonSerializableExample implements JsonSerializable<FluentJsonSerializableExample> {
    private int anInt;
    private boolean aBoolean;
    private String aString;
    private Double aNullableDecimal;

    /**
     * Sets an integer value.
     *
     * @param anInt The integer value.
     * @return The update FluentJsonSerializableExample
     */
    public FluentJsonSerializableExample setAnInt(int anInt) {
        this.anInt = anInt;
        return this;
    }

    /**
     * Sets a boolean value.
     *
     * @param aBoolean The boolean value.
     * @return The update FluentJsonSerializableExample
     */
    public FluentJsonSerializableExample setABoolean(boolean aBoolean) {
        this.aBoolean = aBoolean;
        return  this;
    }

    /**
     * Sets a string value.
     *
     * @param aString The string value.
     * @return The update FluentJsonSerializableExample
     */
    public FluentJsonSerializableExample setAString(String aString) {
        this.aString = aString;
        return this;
    }

    /**
     * Sets a nullable decimal value.
     *
     * @param aNullableDecimal The nullable decimal value.
     * @return The update FluentJsonSerializableExample
     */
    public FluentJsonSerializableExample setANullableDecimal(Double aNullableDecimal) {
        this.aNullableDecimal = aNullableDecimal;
        return this;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        // Optionally 'flush' can be included in the method call chain. It isn't necessary as the caller into this
        // method should handle flushing the JsonWriter.
        return jsonWriter.writeStartObject()
            .writeIntField("int", anInt)
            .writeBooleanField("boolean", aBoolean)
            // Writing fields with nullable types won't write the field if the value is null. If a nullable field needs
            // to always be written use 'writeNullableField(String, Object, WriteValueCallback<JsonWriter, Object>)'.
            // This will write 'fieldName: null' if the value is null.
            .writeStringField("string", aString)
            .writeNumberField("decimal", aNullableDecimal)
            .writeEndObject();
    }

    /**
     * Reads an instance of FluentJsonSerializableExample from the JsonReader.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of FluentJsonSerializableExample if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the FluentJsonSerializableExample.
     */
    public static FluentJsonSerializableExample fromJson(JsonReader jsonReader) throws IOException {
        // 'readObject' will initialize reading if the JsonReader hasn't begun JSON reading and validate that the
        // current state of reading is a JSON start object. If the state isn't JSON start object an exception will be
        // thrown.
        return jsonReader.readObject(reader -> {
            FluentJsonSerializableExample deserializedValue = new FluentJsonSerializableExample();

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                // In this case field names are case-sensitive but this could be replaced with 'equalsIgnoreCase' to
                // make them case-insensitive.
                if ("int".equals(fieldName)) {
                    deserializedValue.setAnInt(reader.getInt());
                } else if ("boolean".equals(fieldName)) {
                    deserializedValue.setABoolean(reader.getBoolean());
                } else if ("string".equals(fieldName)) {
                    deserializedValue.setAString(reader.getString());
                } else if ("decimal".equals(fieldName)) {
                    // For nullable primitives 'getNullable' must be used as it will return null if the current token
                    // is JSON null or pass the reader to the non-null callback method for reading, in this case for
                    // Double it is 'getDouble'.
                    deserializedValue.setANullableDecimal(reader.getNullable(JsonReader::getDouble));
                } else {
                    // Fallthrough case of an unknown property. In this instance the value is skipped, if it's a JSON
                    // array or object the reader will progress until it terminated. This could also throw an exception
                    // if unknown properties should cause that or be read into an additional properties Map for further
                    // usage.
                    reader.skipChildren();
                }
            }

            return deserializedValue;
        });
    }
}
// END: com.azure.json.JsonSerializable.fluent
