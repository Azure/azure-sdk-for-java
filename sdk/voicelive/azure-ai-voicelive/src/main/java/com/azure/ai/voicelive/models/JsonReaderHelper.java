// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.voicelive.models;

import com.azure.json.JsonReader;
import com.azure.json.JsonToken;

import java.io.IOException;

/**
 * Helper class to work around TypeSpec bufferObject() bug.
 * This utility reads JsonReader content to String so it can be replayed multiple times.
 */
final class JsonReaderHelper {

    private JsonReaderHelper() {
        // Utility class
    }

    /**
     * Reads the entire JSON object from JsonReader into a String.
     * The reader can be positioned at START_OBJECT or already inside the object (after readObject callback).
     *
     * @param reader The JsonReader to read from
     * @param alreadyInObject True if START_OBJECT has already been consumed (e.g., in readObject callback)
     * @return The JSON object as a String
     * @throws IOException If reading fails
     */
    static String readObjectAsString(JsonReader reader, boolean alreadyInObject) throws IOException {
        StringBuilder json = new StringBuilder();
        json.append("{");

        int depth = alreadyInObject ? 1 : 0;
        boolean needsComma = false;
        boolean inArray = false;
        int arrayDepth = 0;

        if (!alreadyInObject) {
            if (reader.currentToken() == null) {
                reader.nextToken();
            }
            if (reader.currentToken() != JsonToken.START_OBJECT) {
                throw new IOException("Expected START_OBJECT, got " + reader.currentToken());
            }
            depth = 1;
        }

        while (depth > 0 && reader.nextToken() != null) {
            JsonToken token = reader.currentToken();

            switch (token) {
                case FIELD_NAME:
                    if (needsComma) {
                        json.append(",");
                    }
                    json.append("\"").append(escapeJson(reader.getFieldName())).append("\":");
                    needsComma = false;
                    break;

                case START_OBJECT:
                    if (needsComma && inArray) {
                        json.append(",");
                    }
                    json.append("{");
                    depth++;
                    needsComma = false;
                    break;

                case END_OBJECT:
                    json.append("}");
                    depth--;
                    needsComma = true;
                    break;

                case START_ARRAY:
                    json.append("[");
                    inArray = true;
                    arrayDepth++;
                    needsComma = false;
                    break;

                case END_ARRAY:
                    json.append("]");
                    arrayDepth--;
                    if (arrayDepth == 0) {
                        inArray = false;
                    }
                    needsComma = true;
                    break;

                case STRING:
                    if (needsComma && (inArray || arrayDepth > 0)) {
                        json.append(",");
                    }
                    json.append("\"").append(escapeJson(reader.getString())).append("\"");
                    needsComma = true;
                    break;

                case NUMBER:
                    if (needsComma && (inArray || arrayDepth > 0)) {
                        json.append(",");
                    }
                    json.append(reader.getString());
                    needsComma = true;
                    break;

                case BOOLEAN:
                    if (needsComma && (inArray || arrayDepth > 0)) {
                        json.append(",");
                    }
                    json.append(reader.getBoolean());
                    needsComma = true;
                    break;

                case NULL:
                    if (needsComma && (inArray || arrayDepth > 0)) {
                        json.append(",");
                    }
                    json.append("null");
                    needsComma = true;
                    break;

                default:
                    break;
            }
        }

        return json.toString();
    }

    /**
    * Escapes special characters in JSON strings.
    */
    private static String escapeJson(String value) {
        if (value == null) {
            return "";
        }

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < value.length(); i++) {
            char ch = value.charAt(i);
            switch (ch) {
                case '"':
                    sb.append("\\\"");
                    break;

                case '\\':
                    sb.append("\\\\");
                    break;

                case '\b':
                    sb.append("\\b");
                    break;

                case '\f':
                    sb.append("\\f");
                    break;

                case '\n':
                    sb.append("\\n");
                    break;

                case '\r':
                    sb.append("\\r");
                    break;

                case '\t':
                    sb.append("\\t");
                    break;

                default:
                    if (ch < ' ') {
                        sb.append(String.format("\\u%04x", (int) ch));
                    } else {
                        sb.append(ch);
                    }
                    break;
            }
        }
        return sb.toString();
    }

    /**
     * Extracts a discriminator field value from JSON string.
     *
     * @param jsonString The JSON string
     * @param fieldName The field name to extract
     * @return The field value or null if not found
     */
    static String extractDiscriminator(String jsonString, String fieldName) {
        String searchPattern = "\"" + fieldName + "\"";
        int index = jsonString.indexOf(searchPattern);
        if (index < 0) {
            return null;
        }

        int colonIndex = jsonString.indexOf(':', index);
        if (colonIndex < 0) {
            return null;
        }

        int quoteStart = jsonString.indexOf('"', colonIndex);
        if (quoteStart < 0) {
            return null;
        }

        int quoteEnd = jsonString.indexOf('"', quoteStart + 1);
        if (quoteEnd < 0) {
            return null;
        }

        return jsonString.substring(quoteStart + 1, quoteEnd);
    }
}
