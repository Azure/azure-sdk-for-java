// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.jackson;

import com.typespec.core.implementation.StringBuilderWriter;
import com.typespec.core.util.BinaryData;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;

import java.io.IOException;
import java.io.Writer;

/**
 * Custom deserializer for deserializing {@link BinaryData}.
 */
final class BinaryDataDeserializer extends JsonDeserializer<BinaryData> {
    /**
     * Gets a module wrapping this serializer as an adapter for the Jackson ObjectMapper.
     *
     * @return a simple module to be plugged onto Jackson ObjectMapper.
     */
    public static SimpleModule getModule() {
        SimpleModule module = new SimpleModule();
        module.addDeserializer(BinaryData.class, new BinaryDataDeserializer());
        return module;
    }

    @Override
    public BinaryData deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
        JsonToken token = p.currentToken();

        if (token == JsonToken.VALUE_NULL) {
            return null;
        } else if (token.isStructStart()) {
            return BinaryData.fromString(bufferStruct(p));
        } else {
            return BinaryData.fromString(p.getText());
        }
    }

    private static String bufferStruct(JsonParser parser) throws IOException {
        StringBuilder buffer = new StringBuilder(128);
        Writer bufferWriter = new StringBuilderWriter(buffer);

        // If this state is reached it is known that the current JsonToken is either '{' or '['
        buffer.append(parser.currentToken() == JsonToken.START_OBJECT ? '{' : '[');

        JsonToken previous = parser.currentToken();
        int depth = 1;
        while (depth > 0) {
            JsonToken next = parser.nextToken();

            if (!(previous.isStructStart() || next.isStructEnd() || previous == JsonToken.FIELD_NAME)) {
                buffer.append(',');
            }

            previous = next;
            switch (next) {
                case START_ARRAY:
                case START_OBJECT:
                    depth++;
                    buffer.append(next == JsonToken.START_OBJECT ? '{' : '[');
                    break;

                case END_ARRAY:
                case END_OBJECT:
                    depth--;
                    buffer.append(next == JsonToken.END_OBJECT ? '}' : ']');
                    break;

                case FIELD_NAME:
                case VALUE_STRING:
                    buffer.append("\"");
                    parser.getText(bufferWriter);
                    buffer.append(next == JsonToken.FIELD_NAME ? "\":" : "\"");
                    break;

                default:
                    parser.getText(bufferWriter);
                    break;
            }
        }

        return buffer.toString();
    }
}
