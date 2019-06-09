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

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.fasterxml.jackson.databind.ser.PropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufInputStream;
import io.netty.handler.codec.CorruptedFrameException;

import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

class RntbdObjectMapper {

    private static final SimpleFilterProvider propertyFilterProvider = new SimpleFilterProvider();
    private static final ObjectMapper objectMapper = new ObjectMapper().setFilterProvider(propertyFilterProvider);
    private static volatile ObjectWriter objectWriter = null;

    private RntbdObjectMapper() {
    }

    static JsonNode readTree(ByteBuf in) {

        Objects.requireNonNull(in, "in");
        InputStream istream = new ByteBufInputStream(in);

        try {
            return objectMapper.readTree(istream);
        } catch (IOException error) {
            throw new CorruptedFrameException(error);
        }
    }

    static void registerPropertyFilter(Class<?> type, Class<? extends PropertyFilter> filter) {

        Objects.requireNonNull(type, "type");
        Objects.requireNonNull(filter, "filter");

        try {
            propertyFilterProvider.addFilter(type.getSimpleName(), filter.newInstance());
        } catch (ReflectiveOperationException error) {
            throw new IllegalStateException(error);
        }
    }

    static ObjectWriter writer() {
        if (objectWriter == null) {
            synchronized (objectMapper) {
                if (objectWriter == null) {
                    objectWriter = objectMapper.writer();
                }
            }
        }
        return objectWriter;
    }
}
