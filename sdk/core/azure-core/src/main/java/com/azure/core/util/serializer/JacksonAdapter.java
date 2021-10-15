// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import com.azure.core.http.HttpHeaders;
import com.azure.core.implementation.jackson.ObjectMapperShim;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.function.BiConsumer;
import java.util.regex.Pattern;

/**
 * Implementation of {@link SerializerAdapter} for Jackson.
 */
public class JacksonAdapter implements SerializerAdapter {
    private static final Pattern PATTERN = Pattern.compile("^\"*|\"*$");
    private final ClientLogger logger = new ClientLogger(JacksonAdapter.class);

    /**
     * An instance of {@link ObjectMapperShim} to serialize/deserialize objects.
     */
    private final ObjectMapperShim mapper;

    private final ObjectMapperShim xmlMapper;

    private final ObjectMapperShim headerMapper;

    /**
     * Raw mappers are needed only to support deprecated simpleMapper() and
     * serializer().
     */
    private ObjectMapper rawOuterMapper;
    private ObjectMapper rawInnerMapper;

    /*
     * The lazily-created serializer for this ServiceClient.
     */
    private static SerializerAdapter serializerAdapter;

    /**
     * Creates a new JacksonAdapter instance with default mapper settings.
     */
    public JacksonAdapter() {
        this((outerMapper, innerMapper) -> { });
    }

    /**
     * Creates a new JacksonAdapter instance with Azure Core mapper settings and applies
     * additional configuration through {@code configureSerialization} callback.
     *
     * {@code configureSerialization} callback provides outer and inner instances of {@link ObjectMapper}.
     * Both of them are pre-configured for Azure serialization needs, but only outer mapper capable of
     * flattening and populating additionalProperties. Outer mapper is used by {@code JacksonAdapter} for
     * all serialization needs.
     *
     * Register modules on the outer instance to add custom (de)serializers similar to
     * {@code new JacksonAdapter((outer, inner) -> outer.registerModule(new MyModule()))}
     *
     * Use inner mapper for chaining serialization logic in your (de)serializers.
     *
     * @param configureSerialization Applies additional configuration to outer
     *                               mapper using inner mapper for module chaining.
     */
    public JacksonAdapter(BiConsumer<ObjectMapper, ObjectMapper> configureSerialization) {
        Objects.requireNonNull(configureSerialization, "'configureSerialization' cannot be null.");
        this.headerMapper = ObjectMapperShim.createHeaderMapper();
        this.xmlMapper = ObjectMapperShim.createXmlMapper();
        this.mapper = ObjectMapperShim.createJsonMapper(ObjectMapperShim.createSimpleMapper(),
            (outerMapper, innerMapper) -> captureRawMappersAndConfigure(outerMapper, innerMapper, configureSerialization));
    }

    /**
     *  Temporary way to capture raw ObjectMapper instances, allows to support deprecated simpleMapper()
     *  and serializer()
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

    /**
     * maintain singleton instance of the default serializer adapter.
     *
     * @return the default serializer
     */
    public static synchronized SerializerAdapter createDefaultSerializerAdapter() {
        if (serializerAdapter == null) {
            serializerAdapter = new JacksonAdapter();
        }
        return serializerAdapter;
    }

    /**
     * @return the original serializer type.
     * @deprecated deprecated to avoid direct {@link ObjectMapper} usage in favor
     * of using more resilient and debuggable {@link JacksonAdapter} APIs.
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

        if (encoding == SerializerEncoding.XML) {
            return xmlMapper.writeValueAsString(object);
        } else {
            return mapper.writeValueAsString(object);
        }
    }

    @Override
    public byte[] serializeToBytes(Object object, SerializerEncoding encoding) throws IOException {
        if (object == null) {
            return null;
        }

        if (encoding == SerializerEncoding.XML) {
            return xmlMapper.writeValueAsBytes(object);
        } else {
            return mapper.writeValueAsBytes(object);
        }
    }

    @Override
    public void serialize(Object object, SerializerEncoding encoding, OutputStream outputStream) throws IOException {
        if (object == null) {
            return;
        }

        if ((encoding == SerializerEncoding.XML)) {
            xmlMapper.writeValue(outputStream, object);
        } else {
            mapper.writeValue(outputStream, object);
        }
    }

    @Override
    public String serializeRaw(Object object) {
        if (object == null) {
            return null;
        }

        try {
            return PATTERN.matcher(serialize(object, SerializerEncoding.JSON)).replaceAll("");
        } catch (IOException ex) {
            logger.warning("Failed to serialize {} to JSON.", object.getClass(), ex);
            return null;
        }
    }

    @Override
    public String serializeList(List<?> list, CollectionFormat format) {
        return serializeIterable(list, format);
    }

    @Override
    public <T> T deserialize(String value, Type type, SerializerEncoding encoding) throws IOException {
        if (CoreUtils.isNullOrEmpty(value)) {
            return null;
        }

        if (encoding == SerializerEncoding.XML) {
            return xmlMapper.readValue(value, type);
        } else {
            return mapper.readValue(value, type);
        }
    }

    @Override
    public <T> T deserialize(byte[] bytes, Type type, SerializerEncoding encoding) throws IOException {
        if (bytes == null || bytes.length == 0) {
            return null;
        }

        if (encoding == SerializerEncoding.XML) {
            return xmlMapper.readValue(bytes, type);
        } else {
            return mapper.readValue(bytes, type);
        }
    }

    @Override
    public <T> T deserialize(InputStream inputStream, final Type type, SerializerEncoding encoding)
        throws IOException {
        if (inputStream == null) {
            return null;
        }

        if (encoding == SerializerEncoding.XML) {
            return xmlMapper.readValue(inputStream, type);
        } else {
            return mapper.readValue(inputStream, type);
        }
    }

    @Override
    public <T> T deserialize(HttpHeaders headers, Type deserializedHeadersType) throws IOException {
        return headerMapper.deserialize(headers, deserializedHeadersType);
    }
}
