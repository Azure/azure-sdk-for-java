// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.data.cosmos.internal.directconnectivity.rntbd;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.node.JsonNodeType;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.handler.codec.CorruptedFrameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ConcurrentHashMap;

import static com.google.common.base.Preconditions.checkNotNull;
import static com.google.common.base.Strings.lenientFormat;

public final class RntbdObjectMapper {

    private static final Logger logger = LoggerFactory.getLogger(RntbdObjectMapper.class);
    private static final SimpleFilterProvider filterProvider = new SimpleFilterProvider();
    private static final ObjectMapper objectMapper = new ObjectMapper().setFilterProvider(filterProvider);
    private static final ObjectWriter objectWriter = objectMapper.writer();
    private static final ConcurrentHashMap<Class<?>, String> simpleClassNames = new ConcurrentHashMap<>();

    private RntbdObjectMapper() {
    }

    public static String toJson(final Object value) {
        try {
            return objectWriter.writeValueAsString(value);
        } catch (final JsonProcessingException error) {
            logger.debug("could not convert {} value to JSON due to:", value.getClass(), error);
            try {
                return lenientFormat("{\"error\":%s}", objectWriter.writeValueAsString(error.toString()));
            } catch (final JsonProcessingException exception) {
                return "null";
            }
        }
    }

    public static String toString(final Object value) {
        final String name = simpleClassNames.computeIfAbsent(value.getClass(), Class::getSimpleName);
        return lenientFormat("%s(%s)", name, toJson(value));
    }

    public static ObjectWriter writer() {
        return objectWriter;
    }

    static ObjectNode readTree(final RntbdResponse response) {
        checkNotNull(response, "response");
        return readTree(response.getContent());
    }

    static ObjectNode readTree(final ByteBuf in) {

        checkNotNull(in, "in");
        final JsonNode node;

        try (final InputStream istream = new ByteBufInputStream(in)) {
            node = objectMapper.readTree(istream);
        } catch (final IOException error) {
            throw new CorruptedFrameException(error);
        }

        if (node.isObject()) {
            return (ObjectNode)node;
        }

        final String cause = lenientFormat("Expected %s, not %s", JsonNodeType.OBJECT, node.getNodeType());
        throw new CorruptedFrameException(cause);
    }

    @SuppressWarnings("SameParameterValue")
    static void registerPropertyFilter(final Class<?> type, final Class<? extends PropertyFilter> filter) {

        checkNotNull(type, "type");
        checkNotNull(filter, "filter");

        try {
            filterProvider.addFilter(type.getSimpleName(), filter.newInstance());
        } catch (final ReflectiveOperationException error) {
            throw new IllegalStateException(error);
        }
    }
}
