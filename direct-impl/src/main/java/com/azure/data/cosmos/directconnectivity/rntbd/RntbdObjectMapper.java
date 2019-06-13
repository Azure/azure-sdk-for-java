/*
 * The MIT License (MIT)
 * Copyright (c) 2018 Microsoft Corporation
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package com.azure.data.cosmos.directconnectivity.rntbd;

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
import io.netty.handler.codec.EncoderException;

import java.io.IOException;
import java.io.InputStream;

import static com.google.common.base.Preconditions.checkNotNull;

public final class RntbdObjectMapper {

    private static final SimpleFilterProvider filterProvider;
    private static final ObjectMapper objectMapper;
    private static final ObjectWriter objectWriter;

    static {
        objectMapper = new ObjectMapper().setFilterProvider(filterProvider = new SimpleFilterProvider());
        objectWriter = objectMapper.writer();
    }

    private RntbdObjectMapper() {
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

        final String cause = String.format("Expected %s, not %s", JsonNodeType.OBJECT, node.getNodeType());
        throw new CorruptedFrameException(cause);
    }

    static void registerPropertyFilter(final Class<?> type, final Class<? extends PropertyFilter> filter) {

        checkNotNull(type, "type");
        checkNotNull(filter, "filter");

        try {
            filterProvider.addFilter(type.getSimpleName(), filter.newInstance());
        } catch (final ReflectiveOperationException error) {
            throw new IllegalStateException(error);
        }
    }

    public static String toJson(Object value) {
        try {
            return objectWriter.writeValueAsString(value);
        } catch (final JsonProcessingException error) {
            throw new EncoderException(error);
        }
    }

    public static ObjectWriter writer() {
        return objectWriter;
    }
}
