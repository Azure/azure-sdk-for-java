// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.implementation.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

/**
 * Configures the {@link StdSerializer} with custom behavior needed to work with Digital Twins REST API.
 */
public final class DigitalTwinsStringSerializer extends StdSerializer<String> {
    private static final long serialVersionUID = 1L;
    private final ObjectMapper mapper;

    public DigitalTwinsStringSerializer(Class<String> t, ObjectMapper mapper) {
        super(t);
        this.mapper = mapper;
    }

    @Override
    public void serialize(String stringToken, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
        if (shouldWriteRawValue(stringToken)) {
            jsonGenerator.writeRawValue(stringToken);
        } else {
            jsonGenerator.writeString(stringToken);
        }
    }

    /**
     * Decides whether or not a string token should be written as a raw value.
     * For example: a string representation of a json payload should be written as raw value as it's the json part we are interested in.
     * It's important to note that only string tokens will end up in the string serializer.
     * If the token is of a non-string primitive type, it should be written as a string and not as that data type.
     * take "1234" or "false" as examples, they are both valid json nodes of types Number and Boolean but the token
     * is not intended to be intercepted as primitive types (since it's a string token). The only types we like to treat as
     * json payloads are actual json objects (for when String is chosen as the generic type for APIs) or the token itself is an escaped
     * json string node.
     *
     * @param stringToken The string token to evaluate.
     * @return True if the string token should be treated as a json node and not a string representation.
     */
    private boolean shouldWriteRawValue(String stringToken) {
        try {
            JsonNode node = mapper.readTree(stringToken);
            return node.isContainerNode() || node.isTextual();
        } catch (IOException e) {
            return false;
        }
    }
}
