// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.JsonNode;
import com.azure.core.util.serializer.JsonSerializer;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * Jackson based implementation of the {@link JsonSerializer} interface.
 */
public final class JacksonJsonSerializer implements JsonSerializer {
    private final ClientLogger logger = new ClientLogger(JacksonJsonSerializer.class);

    private final ObjectMapper mapper;

    /**
     * Constructs a {@link JsonSerializer} using the passed Jackson serializer.
     *
     * @param mapper Configured Jackson serializer.
     */
    JacksonJsonSerializer(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    @Override
    public <T> T deserialize(byte[] input, Class<T> clazz) {
        if (input == null) {
            return null;
        }

        try {
            return mapper.readValue(input, clazz);
        } catch (IOException ex) {
            throw logger.logExceptionAsError(new UncheckedIOException(ex));
        }
    }

    @Override
    public <T> T deserializeTree(JsonNode jsonNode, Class<T> clazz) {
        try {
            return mapper.treeToValue(JsonNodeUtils.toJacksonNode(jsonNode), clazz);
        } catch (JsonProcessingException ex) {
            throw logger.logExceptionAsError(new UncheckedIOException(ex));
        }
    }

    @Override
    public byte[] serialize(Object value) {
        try {
            return mapper.writeValueAsBytes(value);
        } catch (IOException ex) {
            throw logger.logExceptionAsError(new UncheckedIOException(ex));
        }
    }

    @Override
    public byte[] serializeTree(JsonNode jsonNode) {
        return serialize(JsonNodeUtils.toJacksonNode(jsonNode));
    }

    @Override
    public JsonNode toTree(byte[] input) {
        try {
            return JsonNodeUtils.fromJacksonNode(mapper.readTree(input));
        } catch (IOException ex) {
            throw logger.logExceptionAsError(new UncheckedIOException(ex));
        }
    }

    @Override
    public JsonNode toTree(Object value) {
        return JsonNodeUtils.fromJacksonNode(mapper.valueToTree(value));
    }
}
