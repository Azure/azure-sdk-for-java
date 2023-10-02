// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.util.serializer;

import com.typespec.core.http.HttpHeaders;
import com.typespec.core.implementation.AccessibleByteArrayOutputStream;
import com.typespec.core.implementation.ImplUtils;
import com.typespec.core.implementation.ReflectionSerializable;
import com.typespec.core.implementation.TypeUtil;
import com.typespec.core.implementation.jackson.ObjectMapperShim;
import com.typespec.core.util.Configuration;
import com.typespec.core.util.CoreUtils;
import com.typespec.core.util.DateTimeRfc1123;
import com.typespec.core.util.ExpandableStringEnum;
import com.typespec.core.util.Header;
import com.typespec.core.util.logging.ClientLogger;
import com.typespec.json.JsonSerializable;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiConsumer;

import static com.typespec.core.implementation.ReflectionSerializable.deserializeAsJsonSerializable;
import static com.typespec.core.implementation.ReflectionSerializable.deserializeAsXmlSerializable;
import static com.typespec.core.implementation.ReflectionSerializable.supportsJsonSerializable;
import static com.typespec.core.implementation.ReflectionSerializable.supportsXmlSerializable;

/**
 * Implementation of {@link SerializerAdapter} for Jackson.
 */
public class JacksonAdapter implements SerializerAdapter {
    private static final ClientLogger LOGGER = new ClientLogger(JacksonAdapter.class);

    private static boolean useAccessHelper;

    static {
        useAccessHelper = Boolean.parseBoolean(Configuration.getGlobalConfiguration()
            .get("AZURE_JACKSON_ADAPTER_USE_ACCESS_HELPER"));
    }

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

    /**
     * Raw mappers are needed only to support deprecated simpleMapper() and serializer().
     */
    private ObjectMapper rawOuterMapper;
    private ObjectMapper rawInnerMapper;

    /**
     * Creates a new JacksonAdapter instance with default mapper settings.
     */
    public JacksonAdapter() {
        this((outerMapper, innerMapper) -> {
        });
    }

    /**
     * Creates a new JacksonAdapter instance with Azure Core mapper settings and applies additional configuration
     * through {@code configureSerialization} callback.
     * <p>
     * {@code configureSerialization} callback provides outer and inner instances of {@link ObjectMapper}. Both of them
     * are pre-configured for Azure serialization needs, but only outer mapper capable of flattening and populating
     * additionalProperties. Outer mapper is used by {@code JacksonAdapter} for all serialization needs.
     * <p>
     * Register modules on the outer instance to add custom (de)serializers similar to
     * {@code new JacksonAdapter((outer, inner) -> outer.registerModule(new MyModule()))}
     *
     * Use inner mapper for chaining serialization logic in your (de)serializers.
     *
     * @param configureSerialization Applies additional configuration to outer mapper using inner mapper for module
     * chaining.
     */
    public JacksonAdapter(BiConsumer<ObjectMapper, ObjectMapper> configureSerialization) {
        Objects.requireNonNull(configureSerialization, "'configureSerialization' cannot be null.");
        this.headerMapper = ObjectMapperShim.createHeaderMapper();
        this.mapper = ObjectMapperShim.createJsonMapper(ObjectMapperShim.createSimpleMapper(),
            (outerMapper, innerMapper) -> captureRawMappersAndConfigure(outerMapper, innerMapper, configureSerialization));
    }

    /**
     * Temporary way to capture raw ObjectMapper instances, allows to support deprecated simpleMapper() and
     * serializer()
     */
    private void captureRawMappersAndConfigure(ObjectMapper outerMapper, ObjectMapper innerMapper,
        BiConsumer<ObjectMapper, ObjectMapper> configure) {
        this.rawOuterMapper = outerMapper;
        this.rawInnerMapper = innerMapper;

        configure.accept(outerMapper, innerMapper);
    }

    /**
     * Gets a static instance of {@link ObjectMapper} that doesn't handle flattening.
     *
     * @return an instance of {@link ObjectMapper}.
     * @deprecated deprecated, use {@code JacksonAdapter(BiConsumer<ObjectMapper, ObjectMapper>)} constructor to
     * configure modules.
     */
    @Deprecated
    protected ObjectMapper simpleMapper() {
        return rawInnerMapper;
    }

    /**
     * maintain singleton instance of the default serializer adapter.
     *
     * @return the default serializer
     */
    public static SerializerAdapter createDefaultSerializerAdapter() {
        return GlobalSerializerAdapter.SERIALIZER_ADAPTER.getSerializerAdapter();
    }

    /**
     * @return the original serializer type.
     * @deprecated deprecated to avoid direct {@link ObjectMapper} usage in favor of using more resilient and debuggable
     * {@link JacksonAdapter} APIs.
     */
    @Deprecated
    public ObjectMapper serializer() {
        return rawOuterMapper;
    }

    @Override
    public String serialize(Object object, SerializerEncoding encoding) throws IOException {
        if (object == null) {
            return null;
        }

        return (String) useAccessHelper(() -> {
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
        });
    }

    @Override
    public byte[] serializeToBytes(Object object, SerializerEncoding encoding) throws IOException {
        if (object == null) {
            return null;
        }

        return (byte[]) useAccessHelper(() -> {
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
        });
    }

    @Override
    public void serialize(Object object, SerializerEncoding encoding, OutputStream outputStream) throws IOException {
        if (object == null) {
            return;
        }

        useAccessHelper(() -> {
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

            return null;
        });
    }

    @Override
    public String serializeRaw(Object object) {
        if (object == null) {
            return null;
        }

        try {
            return (String) useAccessHelper(() -> {
                try {
                    return removeLeadingAndTrailingQuotes(serialize(object, SerializerEncoding.JSON));
                } catch (IOException ex) {
                    LOGGER.warning("Failed to serialize {} to JSON.", object.getClass(), ex);
                    return null;
                }
            });
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
        try {
            return (String) useAccessHelper(() -> serializeIterable(list, format));
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(String value, Type type, SerializerEncoding encoding) throws IOException {
        if (CoreUtils.isNullOrEmpty(value)) {
            return null;
        }

        return (T) useAccessHelper(() -> {
            if (encoding == SerializerEncoding.XML) {
                Class<?> rawClass = TypeUtil.getRawClass(type);
                return supportsXmlSerializable(rawClass)
                    ? deserializeAsXmlSerializable(rawClass, value.getBytes(StandardCharsets.UTF_8))
                    : getXmlMapper().readValue(value, type);
            } else if (encoding == SerializerEncoding.TEXT) {
                return deserializeText(value, type);
            } else {
                Class<?> rawClass = TypeUtil.getRawClass(type);
                return supportsJsonSerializable(rawClass)
                    ? deserializeAsJsonSerializable(rawClass, value.getBytes(StandardCharsets.UTF_8))
                    : mapper.readValue(value, type);
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(byte[] bytes, Type type, SerializerEncoding encoding) throws IOException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        return (T) useAccessHelper(() -> {
            if (encoding == SerializerEncoding.XML) {
                Class<?> rawClass = TypeUtil.getRawClass(type);
                return supportsXmlSerializable(rawClass)
                    ? deserializeAsXmlSerializable(rawClass, bytes)
                    : getXmlMapper().readValue(bytes, type);
            } else if (encoding == SerializerEncoding.TEXT) {
                return deserializeText(CoreUtils.bomAwareToString(bytes, null), type);
            } else {
                Class<?> rawClass = TypeUtil.getRawClass(type);
                return supportsJsonSerializable(rawClass)
                    ? deserializeAsJsonSerializable(rawClass, bytes)
                    : mapper.readValue(bytes, type);
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(InputStream inputStream, final Type type, SerializerEncoding encoding)
        throws IOException {
        if (inputStream == null) {
            return null;
        }

        return (T) useAccessHelper(() -> {
            if (encoding == SerializerEncoding.XML) {
                Class<?> rawClass = TypeUtil.getRawClass(type);
                return supportsXmlSerializable(rawClass)
                    ? deserializeAsXmlSerializable(rawClass, inputStreamToBytes(inputStream))
                    : getXmlMapper().readValue(inputStream, type);
            } else if (encoding == SerializerEncoding.TEXT) {
                AccessibleByteArrayOutputStream outputStream = new AccessibleByteArrayOutputStream();
                byte[] buffer = new byte[8192];
                int readCount;
                while ((readCount = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, readCount);
                }

                return deserializeText(outputStream.bomAwareToString(null), type);
            } else {
                Class<?> rawClass = TypeUtil.getRawClass(type);
                return supportsJsonSerializable(rawClass)
                    ? deserializeAsJsonSerializable(rawClass, inputStreamToBytes(inputStream))
                    : mapper.readValue(inputStream, type);
            }
        });
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

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(HttpHeaders headers, Type deserializedHeadersType) throws IOException {
        return (T) useAccessHelper(() -> headerMapper.deserialize(headers, deserializedHeadersType));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserializeHeader(Header header, Type type) throws IOException {
        return (T) useAccessHelper(() -> headerMapper.readValue(header.getValue(), type));
    }

    private ObjectMapperShim getXmlMapper() {
        return GlobalXmlMapper.XML_MAPPER.getXmlMapper();
    }

    @SuppressWarnings("removal")
    private static Object useAccessHelper(IOExceptionCallable serializationCall) throws IOException {
        if (useAccessHelper) {
            try {
                return java.security.AccessController.doPrivileged((PrivilegedExceptionAction<Object>)
                    serializationCall::call);
            } catch (PrivilegedActionException ex) {
                Throwable cause = ex.getCause();
                // If the privileged call failed due to an IOException unwrap it.
                if (cause instanceof IOException) {
                    throw (IOException) cause;
                } else if (cause instanceof RuntimeException) {
                    throw (RuntimeException) cause;
                }

                throw LOGGER.logExceptionAsError(new RuntimeException(cause));
            }
        } else {
            return serializationCall.call();
        }
    }

    @FunctionalInterface
    private interface IOExceptionCallable {
        Object call() throws IOException;
    }

    static boolean isUseAccessHelper() {
        return useAccessHelper;
    }

    static void setUseAccessHelper(boolean useAccessHelper) {
        JacksonAdapter.useAccessHelper = useAccessHelper;
    }
}
