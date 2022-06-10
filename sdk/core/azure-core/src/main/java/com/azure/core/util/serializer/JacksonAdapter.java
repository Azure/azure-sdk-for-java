// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import com.azure.core.http.HttpHeaders;
import com.azure.core.implementation.jackson.ObjectMapperShim;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.Header;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.reflect.Type;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

/**
 * Implementation of {@link SerializerAdapter} for Jackson.
 */
public class JacksonAdapter implements SerializerAdapter {
    private static final Pattern PATTERN = Pattern.compile("^\"*|\"*$");
    private static final ClientLogger LOGGER = new ClientLogger(JacksonAdapter.class);

    private static boolean useAccessHelper;

    static {
        useAccessHelper = Boolean.parseBoolean(Configuration.getGlobalConfiguration()
            .get("AZURE_JACKSON_ADAPTER_USE_ACCESS_HELPER"));
    }

    /**
     * An instance of {@link ObjectMapperShim} to serialize/deserialize objects.
     */
    private final ObjectMapperShim mapper;

    private final ObjectMapperShim xmlMapper;

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
     *
     * {@code configureSerialization} callback provides outer and inner instances of {@link ObjectMapper}. Both of them
     * are pre-configured for Azure serialization needs, but only outer mapper capable of flattening and populating
     * additionalProperties. Outer mapper is used by {@code JacksonAdapter} for all serialization needs.
     *
     * Register modules on the outer instance to add custom (de)serializers similar to {@code new JacksonAdapter((outer,
     * inner) -> outer.registerModule(new MyModule()))}
     *
     * Use inner mapper for chaining serialization logic in your (de)serializers.
     *
     * @param configureSerialization Applies additional configuration to outer mapper using inner mapper for module
     * chaining.
     */
    public JacksonAdapter(BiConsumer<ObjectMapper, ObjectMapper> configureSerialization) {
        Objects.requireNonNull(configureSerialization, "'configureSerialization' cannot be null.");
        this.headerMapper = ObjectMapperShim.createHeaderMapper();
        this.xmlMapper = ObjectMapperShim.createXmlMapper();
        this.mapper = ObjectMapperShim.createJsonMapper(ObjectMapperShim.createSimpleMapper(),
            (outerMapper, innerMapper) -> captureRawMappersAndConfigure(outerMapper, innerMapper, configureSerialization));
    }

    /**
     * Temporary way to capture raw ObjectMapper instances, allows to support deprecated simpleMapper() and
     * serializer()
     */
    private void captureRawMappersAndConfigure(ObjectMapper outerMapper, ObjectMapper innerMapper, BiConsumer<ObjectMapper, ObjectMapper> configure) {
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

    private static final class SerializerAdapterHolder {
        /*
         * The lazily-created serializer for this ServiceClient.
         */
        private static final SerializerAdapter SERIALIZER_ADAPTER = new JacksonAdapter();
    }

    /**
     * maintain singleton instance of the default serializer adapter.
     *
     * @return the default serializer
     */
    public static SerializerAdapter createDefaultSerializerAdapter() {
        return SerializerAdapterHolder.SERIALIZER_ADAPTER;
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

        return (String) useAccessHelper(() -> (encoding == SerializerEncoding.XML)
            ? xmlMapper.writeValueAsString(object)
            : mapper.writeValueAsString(object));
    }

    @Override
    public byte[] serializeToBytes(Object object, SerializerEncoding encoding) throws IOException {
        if (object == null) {
            return null;
        }

        return (byte[]) useAccessHelper(() -> (encoding == SerializerEncoding.XML)
            ? xmlMapper.writeValueAsBytes(object)
            : mapper.writeValueAsBytes(object));
    }

    @Override
    public void serialize(Object object, SerializerEncoding encoding, OutputStream outputStream) throws IOException {
        if (object == null) {
            return;
        }

        useAccessHelper(() -> {
            if (encoding == SerializerEncoding.XML) {
                xmlMapper.writeValue(outputStream, object);
            } else {
                mapper.writeValue(outputStream, object);
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
                    return PATTERN.matcher(serialize(object, SerializerEncoding.JSON)).replaceAll("");
                } catch (IOException ex) {
                    LOGGER.warning("Failed to serialize {} to JSON.", object.getClass(), ex);
                    return null;
                }
            });
        } catch (IOException ex) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(ex));
        }
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

        return (T) useAccessHelper(() -> (encoding == SerializerEncoding.XML)
            ? xmlMapper.readValue(value, type)
            : mapper.readValue(value, type));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(byte[] bytes, Type type, SerializerEncoding encoding) throws IOException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        return (T) useAccessHelper(() -> (encoding == SerializerEncoding.XML)
            ? xmlMapper.readValue(bytes, type)
            : mapper.readValue(bytes, type));
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserialize(InputStream inputStream, final Type type, SerializerEncoding encoding)
        throws IOException {
        if (inputStream == null) {
            return null;
        }

        return (T) useAccessHelper(() -> (encoding == SerializerEncoding.XML)
            ? xmlMapper.readValue(inputStream, type)
            : mapper.readValue(inputStream, type));
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
