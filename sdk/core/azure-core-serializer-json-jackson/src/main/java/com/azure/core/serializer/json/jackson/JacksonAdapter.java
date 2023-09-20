// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson;

import com.azure.core.http.HttpHeaders;
import com.azure.core.implementation.AccessibleByteArrayOutputStream;
import com.azure.core.implementation.ImplUtils;
import com.azure.core.implementation.ReflectionSerializable;
import com.azure.core.implementation.TypeUtil;
import com.azure.core.serializer.json.jackson.implementation.ObjectMapperShim;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.DateTimeRfc1123;
import com.azure.core.util.ExpandableStringEnum;
import com.azure.core.util.Header;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.serializer.CollectionFormat;
import com.azure.core.util.serializer.SerializerAdapter;
import com.azure.core.util.serializer.SerializerEncoding;
import com.azure.json.JsonSerializable;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.UUID;

import static com.azure.core.implementation.ReflectionSerializable.deserializeAsJsonSerializable;
import static com.azure.core.implementation.ReflectionSerializable.deserializeAsXmlSerializable;
import static com.azure.core.implementation.ReflectionSerializable.supportsJsonSerializable;
import static com.azure.core.implementation.ReflectionSerializable.supportsXmlSerializable;

/**
 * Implementation of {@link SerializerAdapter} that uses Jackson.
 * <p>
 * This is similar to {@code JacksonAdapter} found in the {@code com.azure:azure-core} package and will serve as the
 * long term replacement plan for the {@code JacksonAdapter} found in {@code com.azure:azure-core}.
 */
public final class JacksonAdapter implements SerializerAdapter {
    private static final ClientLogger LOGGER = new ClientLogger(JacksonAdapter.class);

    // Enum Singleton Pattern
    private enum GlobalXmlMapper {
        XML_MAPPER(ObjectMapperShim.createXmlMapper());

        private final ObjectMapperShim xmlMapper;

        GlobalXmlMapper(ObjectMapperShim xmlMapper) {
            this.xmlMapper = xmlMapper;
        }

        private ObjectMapperShim getXmlMapper() {
            return xmlMapper;
        }
    }

    private enum GlobalSerializerAdapter {
        SERIALIZER_ADAPTER(new JacksonAdapter());

        private final SerializerAdapter serializerAdapter;

        GlobalSerializerAdapter(SerializerAdapter serializerAdapter) {
            this.serializerAdapter = serializerAdapter;
        }

        private SerializerAdapter getSerializerAdapter() {
            return serializerAdapter;
        }
    }

    /**
     * An instance of {@link ObjectMapperShim} to serialize/deserialize objects.
     */
    private final ObjectMapperShim mapper;
    private final ObjectMapperShim headerMapper;

    private JacksonAdapter() {
        this.headerMapper = ObjectMapperShim.createHeaderMapper();
        this.mapper = ObjectMapperShim.createJsonMapper(ObjectMapperShim.createSimpleMapper());
    }

    /**
     * maintain singleton instance of the default serializer adapter.
     *
     * @return the default serializer
     */
    public static SerializerAdapter defaultSerializerAdapter() {
        return GlobalSerializerAdapter.SERIALIZER_ADAPTER.getSerializerAdapter();
    }

    @Override
    public String serialize(Object object, SerializerEncoding encoding) throws IOException {
        if (object == null) {
            return null;
        }

        if (encoding == SerializerEncoding.XML) {
            return supportsXmlSerializable(object.getClass())
                ? ReflectionSerializable.serializeXmlSerializableToString(object)
                : getXmlMapper().writeValueAsString(object);
        } else if (encoding == SerializerEncoding.TEXT) {
            return object.toString();
        } else {
            return ReflectionSerializable.supportsJsonSerializable(object.getClass())
                ? ReflectionSerializable.serializeJsonSerializableToString((JsonSerializable<?>) object)
                : mapper.writeValueAsString(object);
        }
    }

    @Override
    public byte[] serializeToBytes(Object object, SerializerEncoding encoding) throws IOException {
        if (object == null) {
            return null;
        }

        if (encoding == SerializerEncoding.XML) {
            return supportsXmlSerializable(object.getClass())
                ? ReflectionSerializable.serializeXmlSerializableToBytes(object)
                : getXmlMapper().writeValueAsBytes(object);
        } else if (encoding == SerializerEncoding.TEXT) {
            return object.toString().getBytes(StandardCharsets.UTF_8);
        } else {
            return ReflectionSerializable.supportsJsonSerializable(object.getClass())
                ? ReflectionSerializable.serializeJsonSerializableToBytes((JsonSerializable<?>) object)
                : mapper.writeValueAsBytes(object);
        }
    }

    @Override
    public void serialize(Object object, SerializerEncoding encoding, OutputStream outputStream) throws IOException {
        if (object == null) {
            return;
        }

        if (encoding == SerializerEncoding.XML) {
            if (supportsXmlSerializable(object.getClass())) {
                ReflectionSerializable.serializeXmlSerializableIntoOutputStream(object, outputStream);
            } else {
                getXmlMapper().writeValue(outputStream, object);
            }
        } else if (encoding == SerializerEncoding.TEXT) {
            outputStream.write(object.toString().getBytes(StandardCharsets.UTF_8));
        } else {
            if (ReflectionSerializable.supportsJsonSerializable(object.getClass())) {
                ReflectionSerializable.serializeJsonSerializableIntoOutputStream((JsonSerializable<?>) object,
                    outputStream);
            } else {
                mapper.writeValue(outputStream, object);
            }
        }
    }

    @Override
    public String serializeRaw(Object object) {
        if (object == null) {
            return null;
        }

        try {
            return removeLeadingAndTrailingQuotes(serialize(object, SerializerEncoding.JSON));
        } catch (IOException ex) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(ex));
        }
    }

    /*
     * Used by 'serializeRaw' to removal all leading and trailing quotes (").
     */
    static String removeLeadingAndTrailingQuotes(String str) {
        int strLength = str.length();

        // Continue incrementing the start offset until a non-quote character is found.
        int startOffset = 0;
        while (startOffset < strLength) {
            if (str.charAt(startOffset) != '"') {
                break;
            }

            startOffset++;
        }

        // All characters were quotes, early out return an empty string.
        if (startOffset == strLength) {
            return "";
        }

        // Continue decrementing the end offset until a non-quote character is found.
        int endOffset = strLength - 1;
        while (endOffset >= 0) {
            if (str.charAt(endOffset) != '"') {
                break;
            }

            endOffset--;
        }

        // Return the substring range.
        // Remember to add one to the end offset as it's exclusive.
        return str.substring(startOffset, endOffset + 1);
    }

    @Override
    public String serializeList(List<?> list, CollectionFormat format) {
        return serializeIterable(list, format);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(String value, Type type, SerializerEncoding encoding) throws IOException {
        if (CoreUtils.isNullOrEmpty(value)) {
            return null;
        }

        if (encoding == SerializerEncoding.XML) {
            Class<?> rawClass = TypeUtil.getRawClass(type);
            return supportsXmlSerializable(rawClass)
                ? (T) deserializeAsXmlSerializable(rawClass, value.getBytes(StandardCharsets.UTF_8))
                : getXmlMapper().readValue(value, type);
        } else if (encoding == SerializerEncoding.TEXT) {
            return (T) deserializeText(value, type);
        } else {
            Class<?> rawClass = TypeUtil.getRawClass(type);
            return supportsJsonSerializable(rawClass)
                ? (T) deserializeAsJsonSerializable(rawClass, value.getBytes(StandardCharsets.UTF_8))
                : mapper.readValue(value, type);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(byte[] bytes, Type type, SerializerEncoding encoding) throws IOException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        if (encoding == SerializerEncoding.XML) {
            Class<?> rawClass = TypeUtil.getRawClass(type);
            return supportsXmlSerializable(rawClass)
                ? (T) deserializeAsXmlSerializable(rawClass, bytes)
                : getXmlMapper().readValue(bytes, type);
        } else if (encoding == SerializerEncoding.TEXT) {
            return (T) deserializeText(CoreUtils.bomAwareToString(bytes, null), type);
        } else {
            Class<?> rawClass = TypeUtil.getRawClass(type);
            return supportsJsonSerializable(rawClass)
                ? (T) deserializeAsJsonSerializable(rawClass, bytes)
                : mapper.readValue(bytes, type);
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(InputStream inputStream, final Type type, SerializerEncoding encoding)
        throws IOException {
        if (inputStream == null) {
            return null;
        }

        if (encoding == SerializerEncoding.XML) {
            Class<?> rawClass = TypeUtil.getRawClass(type);
            return supportsXmlSerializable(rawClass)
                ? (T) deserializeAsXmlSerializable(rawClass, inputStreamToBytes(inputStream))
                : getXmlMapper().readValue(inputStream, type);
        } else if (encoding == SerializerEncoding.TEXT) {
            AccessibleByteArrayOutputStream outputStream = new AccessibleByteArrayOutputStream();
            byte[] buffer = new byte[8192];
            int readCount;
            while ((readCount = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, readCount);
            }

            return (T) deserializeText(outputStream.bomAwareToString(null), type);
        } else {
            Class<?> rawClass = TypeUtil.getRawClass(type);
            return supportsJsonSerializable(rawClass)
                ? (T) deserializeAsJsonSerializable(rawClass, inputStreamToBytes(inputStream))
                : mapper.readValue(inputStream, type);
        }
    }

    private static byte[] inputStreamToBytes(InputStream inputStream) throws IOException {
        AccessibleByteArrayOutputStream outputStream = new AccessibleByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int readCount;
        while ((readCount = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, readCount);
        }

        return outputStream.toByteArray();
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static Object deserializeText(String value, Type type) throws IOException {
        if (type == String.class || type == CharSequence.class) {
            return value;
        } else if (type == int.class || type == Integer.class) {
            return Integer.parseInt(value);
        } else if (type == char.class || type == Character.class) {
            return CoreUtils.isNullOrEmpty(value) ? null : value.charAt(0);
        } else if (type == byte.class || type == Byte.class) {
            return CoreUtils.isNullOrEmpty(value) ? null : (byte) value.charAt(0);
        } else if (type == byte[].class) {
            return CoreUtils.isNullOrEmpty(value) ? null : value.getBytes(StandardCharsets.UTF_8);
        } else if (type == long.class || type == Long.class) {
            return Long.parseLong(value);
        } else if (type == short.class || type == Short.class) {
            return Short.parseShort(value);
        } else if (type == float.class || type == Float.class) {
            return Float.parseFloat(value);
        } else if (type == double.class || type == Double.class) {
            return Double.parseDouble(value);
        } else if (type == boolean.class || type == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (type == OffsetDateTime.class) {
            return OffsetDateTime.parse(value);
        } else if (type == DateTimeRfc1123.class) {
            return new DateTimeRfc1123(value);
        } else if (type == URL.class) {
            try {
                return ImplUtils.createUrl(value);
            } catch (MalformedURLException ex) {
                throw new IOException(ex);
            }
        } else if (type == URI.class) {
            return URI.create(value);
        } else if (type == UUID.class) {
            return UUID.fromString(value);
        } else if (type == LocalDate.class) {
            return LocalDate.parse(value);
        } else if (Enum.class.isAssignableFrom((Class<?>) type)) {
            return Enum.valueOf((Class) type, value);
        } else if (ExpandableStringEnum.class.isAssignableFrom((Class<?>) type)) {
            try {
                return ((Class<?>) type).getDeclaredMethod("fromString", String.class).invoke(null, value);
            } catch (ReflectiveOperationException ex) {
                throw new IOException(ex);
            }
        } else {
            throw new IllegalStateException("Unsupported text Content-Type Type: " + type);
        }
    }

    @Override
    public <T> T deserialize(HttpHeaders headers, Type deserializedHeadersType) throws IOException {
        return headerMapper.deserialize(headers, deserializedHeadersType);
    }

    @Override
    public <T> T deserializeHeader(Header header, Type type) throws IOException {
        return headerMapper.readValue(header.getValue(), type);
    }

    private ObjectMapperShim getXmlMapper() {
        return GlobalXmlMapper.XML_MAPPER.getXmlMapper();
    }
}
