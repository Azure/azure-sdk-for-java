// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.core.implementation.serializer;

import com.azure.core.http.HttpHeaders;
import com.azure.core.util.Header;
import com.azure.core.util.serializer.CollectionFormat;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

public final class DigitalTwinsSerializerAdapter implements SerializerAdapter {
    private static final SerializerAdapter ADAPTER = JacksonAdapter.createDefaultSerializerAdapter();
    private static final JsonFactory FACTORY = new JsonFactory();

    @Override
    public byte[] serializeToBytes(Object object, SerializerEncoding encoding) throws IOException {
        if (object instanceof String && shouldWriteRawValue((String) object)) {
            return ((String) object).getBytes(StandardCharsets.UTF_8);
        } else {
            return ADAPTER.serializeToBytes(object, encoding);
        }
    }

    @Override
    public void serialize(Object object, SerializerEncoding encoding, OutputStream outputStream) throws IOException {
        if (object instanceof String && shouldWriteRawValue((String) object)) {
            outputStream.write(((String) object).getBytes(StandardCharsets.UTF_8));
        } else {
            ADAPTER.serialize(object, encoding, outputStream);
        }
    }

    @Override
    public String serialize(Object object, SerializerEncoding encoding) throws IOException {
        if (object instanceof String && shouldWriteRawValue((String) object)) {
            return (String) object;
        } else {
            return ADAPTER.serialize(object, encoding);
        }
    }

    @Override
    public String serializeRaw(Object object) {
        if (object instanceof String && shouldWriteRawValue((String) object)) {
            return (String) object;
        } else {
            return ADAPTER.serializeRaw(object);
        }
    }

    @Override
    public String serializeList(List<?> list, CollectionFormat format) {
        return serializeIterable(list, format);
    }

    @Override
    public String serializeIterable(Iterable<?> iterable, CollectionFormat format) {
        if (iterable == null) {
            return null;
        }

        return StreamSupport.stream(iterable.spliterator(), false)
            .map(this::serializeRaw)
            .map(serializedString -> serializedString == null ? "" : serializedString)
            .collect(Collectors.joining(format.getDelimiter()));
    }

    @Override
    public <T> T deserialize(byte[] bytes, Type type, SerializerEncoding encoding) throws IOException {
        return ADAPTER.deserialize(bytes, type, encoding);
    }

    @Override
    public <T> T deserialize(InputStream inputStream, Type type, SerializerEncoding encoding) throws IOException {
        return ADAPTER.deserialize(inputStream, type, encoding);
    }

    @Override
    public <T> T deserializeHeader(Header header, Type type) throws IOException {
        return ADAPTER.deserializeHeader(header, type);
    }

    @Override
    public <T> T deserialize(String value, Type type, SerializerEncoding encoding) throws IOException {
        return ADAPTER.deserialize(value, type, encoding);
    }

    @Override
    public <T> T deserialize(HttpHeaders headers, Type type) throws IOException {
        return ADAPTER.deserialize(headers, type);
    }

    /**
     * Decides whether a string token should be written as a raw value. For example: a string representation of a json
     * payload should be written as raw value as it's the json part we are interested in. It's important to note that
     * only string tokens will end up in the string serializer. If the token is of a non-string primitive type, it
     * should be written as a string and not as that data type. take "1234" or "false" as examples, they are both valid
     * json nodes of types Number and Boolean but the token is not intended to be intercepted as primitive types (since
     * it's a string token). The only types we like to treat as json payloads are actual json objects (for when String
     * is chosen as the generic type for APIs) or the token itself is an escaped json string node.
     *
     * @param value The string token to evaluate.
     * @return True if the string token should be treated as a json node and not a string representation.
     */
    private static boolean shouldWriteRawValue(String value) {
        try (JsonParser parser = FACTORY.createParser(value)) {
            JsonToken token = parser.nextToken();
            return token == JsonToken.START_OBJECT || token == JsonToken.START_ARRAY || token == JsonToken.VALUE_STRING;
        } catch (IOException ignored) {
            return false;
        }
    }
}
