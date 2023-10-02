// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.json;

import java.io.IOException;

/**
 * Simple example of implementing {@link JsonSerializable} where all properties of the class are primitive JSON values.
 */
public class BasicJsonSerializable implements JsonSerializable<BasicJsonSerializable> {
    private int anInt;
    private boolean aBoolean;
    private String aString;
    private Double aNullableDecimal;

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        return jsonWriter.writeStartObject()
            .writeIntField("int", anInt)
            .writeBooleanField("boolean", aBoolean)
            // Nullable type field writing won't serialize the field if the value is null. To serialize a JSON null
            // field when the nullable value is null use
            // 'writeNullableField(String, Object, WriteValueCallback<JsonWriter, Object>)', where the callback uses
            // the JsonWriter API for that type, such as 'writeString' for String.
            .writeStringField("string", aString)
            .writeNumberField("decimal", aNullableDecimal)
            .writeEndObject();
    }

    /**
     * Reads an instance of BasicJsonSerializable from the JsonReader.
     * <p>
     * If this static method doesn't exist calling this method will use the implementation on {@link JsonSerializable}
     * that will throw an {@link UnsupportedOperationException} as the base implementation doesn't know which type to
     * deserialize.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of BasicJsonSerializable if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the BasicJsonSerializable.
     */
    public static BasicJsonSerializable fromJson(JsonReader jsonReader) throws IOException {
        // 'JsonReader.readObject' prepares the JsonReader for reading a JSON object by checking the current JSON token
        // type. If the current token is null that is generally an indication that JSON reading hasn't begun so the
        // next token is retrieved. At this point if the token is still null or JSON null, null will be return,
        // otherwise the token is checked to be JSON start object and if it's not an exception will be thrown. If the
        // token is start object the 'ReadValueCallback<JsonReader, T>' will be invoked.
        return jsonReader.readObject(reader -> {
            // BasicJsonSerializable doesn't have any constructor parameters, so eagerly initialize an instance and
            // deserialize values directly into the object.
            BasicJsonSerializable value = new BasicJsonSerializable();

            // Read the JSON object until the end object token for it is seen.
            while (reader.nextToken() != JsonToken.END_OBJECT) {
                // Get the field name now as the reader will need to be progress to the value portion of the field.
                String fieldName = reader.getFieldName();
                reader.nextToken();

                // Check for the field name matching any of the JSON field names used by the object.
                // This can be done any way you wish and is generally case-sensitive but case-insensitive or aliasing
                // can be used as well.
                if ("int".equals(fieldName)) {
                    value.anInt = reader.getInt();
                } else if ("boolean".equals(fieldName)) {
                    value.aBoolean = reader.getBoolean();
                } else if ("string".equals(fieldName)) {
                    value.aString = reader.getString();
                } else if ("decimal".equals(fieldName)) {
                    // For nullable types, except String, 'JsonReader.getNullable' needs to be used to prevent reading
                    // null as a value which would cause an exception.
                    value.aNullableDecimal = reader.getNullable(JsonReader::getDouble);
                } else {
                    // This is a fallthrough case when the JSON field is unknown. The most common handling is to ignore
                    // unknown fields by using 'JsonReader.skipChildren' which is a no-op for primitive JSON values but
                    // for JSON arrays and objects the entire array or object are skipped. Other options here are to
                    // generically read the field with 'JsonReader.readUntyped' into a Map or to throw an exception
                    // about unknown fields.
                    reader.skipChildren();
                }
            }

            // Return the deserialized value.
            return value;
        });
    }
}
