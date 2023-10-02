// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.json;

import java.io.IOException;

/**
 * {@link JsonSerializable} implementation with flattened properties.
 * <p>
 * Flattened properties are a where JSON may look like the following:
 *
 * <pre><code>
 * {
 *   "flattened": {
 *     "string": "value"
 *   }
 * }
 * </code></pre>
 *
 * But the object doesn't have another object as a field but the {@code stringProperty} directly as a field.
 */
public class FlattenedPropertiesJsonSerializable implements JsonSerializable<FlattenedPropertiesJsonSerializable> {
    private final String aString;

    public FlattenedPropertiesJsonSerializable(String aString) {
        this.aString = aString;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        jsonWriter.writeStartObject();

        // Since the flattened JSON property is nullable check if for being non-null before writing the nested value.
        if (aString != null) {
            // Write the inner JSON object.
            // It's possible for flattened properties to nest multiple levels.
            jsonWriter.writeStartObject("flattened")
                .writeStringField("string", aString)
                .writeEndObject();
        }

        return jsonWriter.writeEndObject();
    }

    /**
     * Reads an instance of FlattenedPropertiesJsonSerializable from the JsonReader.
     * <p>
     * If this static method doesn't exist calling this method will use the implementation on {@link JsonSerializable}
     * that will throw an {@link UnsupportedOperationException} as the base implementation doesn't know which type to
     * deserialize.
     *
     * @param jsonReader The JsonReader being read.
     * @return An instance of FlattenedPropertiesJsonSerializable if the JsonReader was pointing to an instance of it, or
     * null if it was pointing to JSON null.
     * @throws IOException If an error occurs while reading the FlattenedPropertiesJsonSerializable.
     * @throws IllegalStateException If any required properties aren't found.
     */
    public static FlattenedPropertiesJsonSerializable fromJson(JsonReader jsonReader) throws IOException {
        return jsonReader.readObject(reader -> {
            String aString = null;
            boolean foundAString = false;

            while (reader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = reader.getFieldName();
                reader.nextToken();

                if ("flattened".equals(fieldName)) {
                    // Since the 'flattened' property is a nested object an inner reader loop will need to be used.
                    // If there are multiple levels of flattening, multiple inner reader loops will be used.
                    while (reader.nextToken() != JsonToken.END_OBJECT) {
                        fieldName = reader.getFieldName();
                        reader.nextToken();

                        if ("string".equals(fieldName)) {
                            aString = reader.getString();
                            foundAString = true;
                        } else {
                            reader.skipChildren();
                        }
                    }
                } else {
                    reader.skipChildren();
                }
            }

            // Check that all required properties have been found.
            if (foundAString) {
                return new FlattenedPropertiesJsonSerializable(aString);
            }

            // If not all required properties are found throw an exception.
            throw new IllegalStateException(
                "Missing required JSON properties. Required properties are 'flattened.string'.");
        });
    }
}
