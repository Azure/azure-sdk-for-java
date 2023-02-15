// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json;

import java.io.IOException;

/**
 * Samples for {@link JsonSerializable}.
 */
public class JsonSerializableSamples {
    // BEGIN: jsonserializablesample-basic
    public class JsonSerializableExample implements JsonSerializable<JsonSerializableExample> {
        private int anInt;
        private boolean aBoolean;
        private String aString;
        private Double aNullableDecimal;

        @Override
        public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
            jsonWriter.writeStartObject();

            jsonWriter.writeIntField("anInt", anInt);
            jsonWriter.writeBooleanField("aBoolean", aBoolean);
            jsonWriter.writeStringField("aString", aString);
            // writeNumberField doesn't write the field if the value is null, if a null field is explicitly needed
            // null checking and using writeNullField should be used.
            jsonWriter.writeNumberField("aNullableDecimal", aNullableDecimal);

            // Example of null checking:
            // if (aNullableDecimal == null) {
            //     jsonWriter.writeNullField("aNullableDecimal");
            // } else {
            //     jsonWriter.writeNumberField("aNullableDecimal", aNullableDecimal);
            // }

            return jsonWriter.writeEndObject();
        }

        public JsonSerializableExample fromJson(JsonReader jsonReader) throws IOException {
            // readObject is a convenience method on JsonReader which prepares the JSON for being read as an object.
            // If the current token isn't initialized it will begin reading the JSON stream, then if the current token
            // is still null or JsonToken.NULL null will be returned without calling the reader function. If the
            // current token isn't a valid object state an exception will be thrown, and if it is a valid object state
            // the reader function will be called.
            return jsonReader.readObject(reader -> {
                // Since this class has no constructor reading to fields can be done inline.
                // If the class had a constructor with arguments the recommendation is using local variables to track
                // all field values.
                JsonSerializableExample result = new JsonSerializableExample();
                while (reader.nextToken() != JsonToken.END_OBJECT) {
                    String fieldName = reader.getFieldName();
                    if ("anInt".equals(fieldName)) {
                        result.anInt = reader.getInt();
                    } else if ("aBoolean".equals(fieldName)) {
                        result.aBoolean = reader.getBoolean();
                    } else if ("aString".equals(fieldName)) {
                        result.aString = reader.getString();
                    } else if ("aNullableDecimal".equals(fieldName)) {
                        // getNullable returns null if the current token is JsonToken.NULL, if the current token isn't
                        // JsonToken.NULL it passes the reader to the ReadValueCallback.
                        result.aNullableDecimal = reader.getNullable(JsonReader::getDouble);
                    } else {
                        // Skip children when the field is unknown.
                        // If the current token isn't an array or object this is a no-op, otherwise is skips the entire
                        // sub-array/sub-object.
                        reader.skipChildren();
                    }
                }

                return result;
            });
        }
    }
    // END: jsonserializablesample-basic
}
