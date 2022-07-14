// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.json.contract;

import com.azure.json.JsonReader;
import com.azure.json.JsonToken;
import com.azure.json.JsonWriter;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Utilities for {@link JsonReader} and {@link JsonWriter} contract tests.
 */
final class ContractUtils {
    static Object readUntypedField(JsonReader jsonReader) {
        return readUntypedField(jsonReader, 0);
    }

    private static Object readUntypedField(JsonReader jsonReader, int depth) {
        // Keep track of array and object nested depth to prevent a StackOverflowError from occurring.
        if (depth >= 1000) {
            throw new IllegalStateException("Untyped object exceeded allowed object nested depth of 1000.");
        }

        JsonToken token = jsonReader.currentToken();
        if (token == null) {
            token = jsonReader.nextToken();
        }

        // Untyped fields cannot begin with END_OBJECT, END_ARRAY, or FIELD_NAME as these would constitute invalid JSON.
        if (token == JsonToken.END_ARRAY || token == JsonToken.END_OBJECT || token == JsonToken.FIELD_NAME) {
            throw new IllegalStateException("Unexpected token to begin an untyped field: " + token);
        }

        if (token == JsonToken.NULL || token == null) {
            return null;
        } else if (token == JsonToken.BOOLEAN) {
            return jsonReader.getBooleanValue();
        } else if (token == JsonToken.NUMBER) {
            String numberText = jsonReader.getTextValue();
            if (numberText.contains(".")) {
                try {
                    return Float.parseFloat(numberText);
                } catch (NumberFormatException ex) {
                    return Double.parseDouble(numberText);
                }
            } else {
                try {
                    return Integer.parseInt(numberText);
                } catch (NumberFormatException ex) {
                    return Long.parseLong(numberText);
                }
            }
        } else if (token == JsonToken.STRING) {
            return jsonReader.getStringValue();
        } else if (token == JsonToken.START_ARRAY) {
            List<Object> array = new ArrayList<>();

            while (jsonReader.nextToken() != JsonToken.END_ARRAY) {
                array.add(readUntypedField(jsonReader, depth + 1));
            }

            return array;
        } else if (token == JsonToken.START_OBJECT) {
            Map<String, Object> object = new LinkedHashMap<>();

            while (jsonReader.nextToken() != JsonToken.END_OBJECT) {
                String fieldName = jsonReader.getFieldName();
                jsonReader.nextToken();
                Object value = readUntypedField(jsonReader, depth + 1);

                object.put(fieldName, value);
            }

            return object;
        }

        // This should never happen as all JsonToken cases are checked above.
        throw new IllegalStateException("Unknown token type while reading an untyped field: " + token);
    }
}
