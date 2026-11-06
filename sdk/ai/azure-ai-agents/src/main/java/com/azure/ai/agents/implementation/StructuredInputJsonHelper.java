// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.agents.implementation;

import com.azure.core.util.BinaryData;
import com.azure.core.util.logging.ClientLogger;
import com.azure.json.JsonReader;
import com.azure.json.JsonSerializable;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;
import java.io.IOException;

/**
 * Reads structured input JSON without coercing its numbers to Java numeric types.
 */
public final class StructuredInputJsonHelper {
    private static final ClientLogger LOGGER = new ClientLogger(StructuredInputJsonHelper.class);

    private StructuredInputJsonHelper() {
    }

    /**
     * Reads a non-null JSON value while retaining its numeric precision and JSON types.
     *
     * @param reader the reader positioned at the value.
     * @return the JSON value as serializable BinaryData.
     * @throws IOException if the JSON cannot be read.
     * @throws IllegalStateException if the reader is not positioned at a non-null JSON value.
     */
    public static BinaryData readJsonValue(JsonReader reader) throws IOException {
        JsonToken token = reader.currentToken();
        if (token == JsonToken.STRING) {
            return BinaryData.fromObject(reader.getString());
        }
        String json;
        if (token == JsonToken.START_OBJECT || token == JsonToken.START_ARRAY) {
            json = reader.readChildren();
        } else if (token == JsonToken.NUMBER || token == JsonToken.BOOLEAN) {
            json = reader.getText();
        } else {
            throw LOGGER
                .logExceptionAsError(new IllegalStateException("Expected a non-null JSON value, found " + token));
        }
        return BinaryData.fromObject(new JsonValue(json));
    }

    private static final class JsonValue implements JsonSerializable<JsonValue> {
        private final String json;

        private JsonValue(String json) {
            this.json = json;
        }

        @Override
        public JsonWriter toJson(JsonWriter writer) throws IOException {
            return writer.writeRawValue(json);
        }

        public static JsonValue fromJson(JsonReader reader) throws IOException {
            if (reader.currentToken() == null) {
                reader.nextToken();
            }
            return reader.getNullable(nonNullReader -> new JsonValue(readJsonValue(nonNullReader).toString()));
        }
    }
}
