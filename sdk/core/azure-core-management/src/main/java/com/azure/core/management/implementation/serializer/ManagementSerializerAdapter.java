// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.management.implementation.serializer;

import com.azure.core.http.HttpHeaders;
import com.azure.core.management.exception.ManagementError;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.Header;
import com.azure.core.util.serializer.CollectionFormat;
import com.azure.core.util.serializer.JacksonAdapter;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.core.io.JsonStringEncoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class ManagementSerializerAdapter implements SerializerAdapter {
    private static final JsonFactory FACTORY = JsonFactory.builder().build();
    private static final JsonStringEncoder JSON_STRING_ENCODER = JsonStringEncoder.getInstance();

    private enum InternalAdapter {
        INTERNAL_ADAPTER(JacksonAdapter.createDefaultSerializerAdapter());

        InternalAdapter(SerializerAdapter adapter) {
            this.adapter = adapter;
        }

        private final SerializerAdapter adapter;

        SerializerAdapter getAdapter() {
            return adapter;
        }
    }

    @Override
    public String serialize(Object object, SerializerEncoding encoding) throws IOException {
        return InternalAdapter.INTERNAL_ADAPTER.getAdapter().serialize(object, encoding);
    }

    @Override
    public byte[] serializeToBytes(Object object, SerializerEncoding encoding) throws IOException {
        return InternalAdapter.INTERNAL_ADAPTER.getAdapter().serializeToBytes(object, encoding);
    }

    @Override
    public void serialize(Object object, SerializerEncoding encoding, OutputStream outputStream) throws IOException {
        InternalAdapter.INTERNAL_ADAPTER.getAdapter().serialize(object, encoding, outputStream);
    }

    @Override
    public String serializeRaw(Object object) {
        return InternalAdapter.INTERNAL_ADAPTER.getAdapter().serializeRaw(object);
    }

    @Override
    public String serializeList(List<?> list, CollectionFormat format) {
        return InternalAdapter.INTERNAL_ADAPTER.getAdapter().serializeList(list, format);
    }

    @Override
    public String serializeIterable(Iterable<?> iterable, CollectionFormat format) {
        return InternalAdapter.INTERNAL_ADAPTER.getAdapter().serializeIterable(iterable, format);
    }

    @Override
    public <T> T deserialize(String value, Type type, SerializerEncoding encoding) throws IOException {
        if (CoreUtils.isNullOrEmpty(value)) {
            return null;
        }

        if (isManagementErrorType(type)) {
            return deserializeManagementError(value.getBytes(StandardCharsets.UTF_8), type, encoding);
        } else {
            return InternalAdapter.INTERNAL_ADAPTER.getAdapter().deserialize(value, type, encoding);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Type type, SerializerEncoding encoding) throws IOException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        if (isManagementErrorType(type)) {
            return deserializeManagementError(bytes, type, encoding);
        } else {
            return InternalAdapter.INTERNAL_ADAPTER.getAdapter().deserialize(bytes, type, encoding);
        }
    }

    @Override
    public <T> T deserialize(InputStream inputStream, Type type, SerializerEncoding encoding) throws IOException {
        if (inputStream == null) {
            return null;
        }

        if (isManagementErrorType(type)) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int length;
            while ((length = inputStream.read(buffer)) != -1) {
                baos.write(buffer, 0, length);
            }

            return deserializeManagementError(baos.toByteArray(), type, encoding);
        } else {
            return InternalAdapter.INTERNAL_ADAPTER.getAdapter().deserialize(inputStream, type, encoding);
        }
    }

    @Override
    public <T> T deserialize(HttpHeaders headers, Type type) throws IOException {
        return InternalAdapter.INTERNAL_ADAPTER.getAdapter().deserialize(headers, type);
    }

    @Override
    public <T> T deserializeHeader(Header header, Type type) throws IOException {
        return InternalAdapter.INTERNAL_ADAPTER.getAdapter().deserializeHeader(header, type);
    }

    private static boolean isManagementErrorType(Type type) {
        return type instanceof Class<?> && ManagementError.class.isAssignableFrom((Class<?>) type);
    }

    private static <T> T deserializeManagementError(byte[] bytes, Type type, SerializerEncoding encoding)
        throws IOException {
        try (JsonParser parser = FACTORY.createParser(bytes)) {
            // Initialize the parser to the start object or JSON null.
            parser.nextToken();

            // Get the current field name.
            String fieldName = parser.nextFieldName();

            // Parse the JSON until an 'error' field is found.
            while (fieldName != null && !"error".equals(fieldName)) {
                // Skip over any nested JSON arrays and objects.
                if (parser.nextToken().isStructStart()) {
                    parser.skipChildren();
                }

                // Get the next field name.
                fieldName = parser.nextFieldName();
            }

            // If the 'error' JSON node is found iterate to the next JSON token, should either be JSON start object or
            // null, and extract that to begin reading the wrapped ManagementError.
            if ("error".equals(fieldName)) {
                parser.nextToken();

                return InternalAdapter.INTERNAL_ADAPTER.getAdapter().deserialize(
                    readWrappedManagementError(parser, bytes.length), type, encoding);
            } else {
                // Otherwise, read the JSON value directly as ManagementError.
                return InternalAdapter.INTERNAL_ADAPTER.getAdapter().deserialize(bytes, type, encoding);
            }
        }
    }

    private static String readWrappedManagementError(JsonParser parser, int bufferSize) throws IOException {
        JsonToken token = parser.currentToken();

        // Likely pointing to JSON null, just return it.
        if (token != JsonToken.START_OBJECT) {
            return parser.getText();
        }

        StringBuilder buffer = new StringBuilder(bufferSize);

        buffer.append(parser.getText());

        // Initial array or object depth is 1.
        int depth = 1;

        while (depth > 0) {
            JsonToken previousToken = token;
            token = parser.nextToken();

            if (isStartArrayOrObject(token)) {
                // Entering another array or object, increase depth.
                depth++;
            } else if (isEndArrayOrObject(token)) {
                // Existing the array or object, decrease depth.
                depth--;
            } else if (token == null) {
                // Should never get into this state if the JSON token stream is properly formatted JSON.
                // But if this happens, just return until a better strategy can be determined.
                return buffer.toString();
            }

            // 1. If the previous token was a struct start token it should never be followed by ','.
            // 2. If the current token is a struct end a ',' should never occur between it and the previous token.
            // 3. If the previous token was a field name a ',' should never occur after it.
            if (!(isStartArrayOrObject(previousToken)
                || isEndArrayOrObject(token)
                || previousToken == JsonToken.FIELD_NAME)) {
                buffer.append(',');
            }

            if (token == JsonToken.FIELD_NAME) {
                String fieldName = parser.getText();
                if ("code".equalsIgnoreCase(fieldName)) {
                    buffer.append("\"code\":");
                } else if ("message".equalsIgnoreCase(fieldName)) {
                    buffer.append("\"message\":");
                } else if ("target".equalsIgnoreCase(fieldName)) {
                    buffer.append("\"target\":");
                } else if ("details".equalsIgnoreCase(fieldName)) {
                    buffer.append("\"details\":");
                } else {
                    buffer.append("\"").append(parser.getText()).append("\":");
                }
            } else if (token == JsonToken.VALUE_STRING) {
                buffer.append("\"").append(JSON_STRING_ENCODER.quoteAsString(parser.getText())).append("\"");
            } else {
                buffer.append(parser.getText());
            }
        }

        return buffer.toString();
    }

    private static boolean isStartArrayOrObject(JsonToken token) {
        return token == JsonToken.START_ARRAY || token == JsonToken.START_OBJECT;
    }

    private static boolean isEndArrayOrObject(JsonToken token) {
        return token == JsonToken.END_ARRAY || token == JsonToken.END_OBJECT;
    }
}
