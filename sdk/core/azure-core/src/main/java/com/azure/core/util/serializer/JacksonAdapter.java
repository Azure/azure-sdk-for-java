// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import com.azure.core.annotation.HeaderCollection;
import com.azure.core.http.HttpHeader;
import com.azure.core.http.HttpHeaders;
import com.azure.core.implementation.AccessibleByteArrayOutputStream;
import com.azure.core.implementation.TypeUtil;
import com.azure.core.implementation.serializer.MalformedValueException;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.cfg.MapperBuilder;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.deser.FromXmlParser;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;

/**
 * Implementation of {@link SerializerAdapter} for Jackson.
 */
public class JacksonAdapter implements SerializerAdapter {
    private static final Pattern PATTERN = Pattern.compile("^\"*|\"*$");

    private final ClientLogger logger = new ClientLogger(JacksonAdapter.class);

    /**
     * An instance of {@link ObjectMapper} to serialize/deserialize objects.
     */
    private final ObjectMapper mapper;

    /**
     * An instance of {@link ObjectMapper} that does not do flattening.
     */
    private final ObjectMapper simpleMapper;

    private final ObjectMapper xmlMapper;

    private final ObjectMapper headerMapper;

    /*
     * The lazily-created serializer for this ServiceClient.
     */
    private static SerializerAdapter serializerAdapter;

    private final Map<Type, JavaType> typeToJavaTypeCache = new ConcurrentHashMap<>();

    /**
     * Creates a new JacksonAdapter instance with default mapper settings.
     */
    public JacksonAdapter() {
        this.simpleMapper = initializeMapperBuilder(JsonMapper.builder())
            .build();

        this.headerMapper = initializeMapperBuilder(JsonMapper.builder())
            .enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES)
            .build();

        this.xmlMapper = initializeMapperBuilder(XmlMapper.builder())
            .defaultUseWrapper(false)
            .enable(ToXmlGenerator.Feature.WRITE_XML_DECLARATION)
            /*
             * In Jackson 2.12 the default value of this feature changed from true to false.
             * https://github.com/FasterXML/jackson/wiki/Jackson-Release-2.12#xml-module
             */
            .enable(FromXmlParser.Feature.EMPTY_ELEMENT_AS_NULL)
            .build();

        ObjectMapper flatteningMapper = initializeMapperBuilder(JsonMapper.builder())
            .addModule(FlatteningSerializer.getModule(simpleMapper()))
            .addModule(FlatteningDeserializer.getModule(simpleMapper()))
            .build();

        this.mapper = initializeMapperBuilder(JsonMapper.builder())
            // Order matters: must register in reverse order of hierarchy
            .addModule(AdditionalPropertiesSerializer.getModule(flatteningMapper))
            .addModule(AdditionalPropertiesDeserializer.getModule(flatteningMapper))
            .addModule(FlatteningSerializer.getModule(simpleMapper()))
            .addModule(FlatteningDeserializer.getModule(simpleMapper()))
            .build();
    }

    /**
     * Gets a static instance of {@link ObjectMapper} that doesn't handle flattening.
     *
     * @return an instance of {@link ObjectMapper}.
     */
    protected ObjectMapper simpleMapper() {
        return simpleMapper;
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
     * @return the original serializer type
     */
    public ObjectMapper serializer() {
        return mapper;
    }

    @Override
    public String serialize(Object object, SerializerEncoding encoding) throws IOException {
        if (object == null) {
            return null;
        }

        ByteArrayOutputStream stream = new AccessibleByteArrayOutputStream();
        serialize(object, encoding, stream);

        return new String(stream.toByteArray(), 0, stream.size(), StandardCharsets.UTF_8);
    }

    @Override
    public void serialize(Object object, SerializerEncoding encoding, OutputStream outputStream) throws IOException {
        if (object == null) {
            return;
        }

        if ((encoding == SerializerEncoding.XML)) {
            xmlMapper.writeValue(outputStream, object);
        } else {
            serializer().writeValue(outputStream, object);
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
        if (list == null) {
            return null;
        }
        List<String> serialized = new ArrayList<>();
        for (Object element : list) {
            String raw = serializeRaw(element);
            serialized.add(raw != null ? raw : "");
        }
        return String.join(format.getDelimiter(), serialized);
    }

    @Override
    public <T> T deserialize(String value, Type type, SerializerEncoding encoding) throws IOException {
        if (CoreUtils.isNullOrEmpty(value)) {
            return null;
        }

        return deserialize(new ByteArrayInputStream(value.getBytes(StandardCharsets.UTF_8)), type, encoding);
    }

    @Override
    public <T> T deserialize(InputStream inputStream, final Type type, SerializerEncoding encoding)
        throws IOException {
        if (inputStream == null) {
            return null;
        }

        final JavaType javaType = createJavaType(type);
        try {
            if (encoding == SerializerEncoding.XML) {
                return xmlMapper.readValue(inputStream, javaType);
            } else {
                return serializer().readValue(inputStream, javaType);
            }
        } catch (JsonParseException jpe) {
            throw logger.logExceptionAsError(new MalformedValueException(jpe.getMessage(), jpe));
        }
    }

    @Override
    public <T> T deserialize(HttpHeaders headers, Type deserializedHeadersType) throws IOException {
        if (deserializedHeadersType == null) {
            return null;
        }

        T deserializedHeaders = headerMapper.convertValue(headers, createJavaType(deserializedHeadersType));

        final Class<?> deserializedHeadersClass = TypeUtil.getRawClass(deserializedHeadersType);
        final Field[] declaredFields = deserializedHeadersClass.getDeclaredFields();

        /*
         * A list containing all handlers for header collections of the header type.
         */
        final List<HeaderCollectionHandler> headerCollectionHandlers = new ArrayList<>();

        /*
         * This set is an optimization where we track the first character of all HeaderCollections defined on the
         * deserialized headers type. This allows us to optimize away startWiths checks which are much more costly than
         * getting the first character.
         */
        final Set<Character> headerCollectionsFirstCharacters = new HashSet<>();

        /*
         * Begin by looping over all declared fields and initializing all header collection information.
         */
        for (final Field declaredField : declaredFields) {
            if (!declaredField.isAnnotationPresent(HeaderCollection.class)) {
                continue;
            }

            final Type declaredFieldType = declaredField.getGenericType();
            if (!TypeUtil.isTypeOrSubTypeOf(declaredField.getType(), Map.class)) {
                continue;
            }

            final Type[] mapTypeArguments = TypeUtil.getTypeArguments(declaredFieldType);
            if (mapTypeArguments.length != 2
                || mapTypeArguments[0] != String.class
                || mapTypeArguments[1] != String.class) {
                continue;
            }

            final HeaderCollection headerCollectionAnnotation = declaredField.getAnnotation(HeaderCollection.class);
            final String headerCollectionPrefix = headerCollectionAnnotation.value().toLowerCase(Locale.ROOT);
            final int headerCollectionPrefixLength = headerCollectionPrefix.length();
            if (headerCollectionPrefixLength == 0) {
                continue;
            }

            headerCollectionHandlers.add(new HeaderCollectionHandler(headerCollectionPrefix, declaredField));
            headerCollectionsFirstCharacters.add(headerCollectionPrefix.charAt(0));
        }

        /*
         * Then loop over all headers and check if they begin with any of the prefixes found.
         */
        for (final HttpHeader header : headers) {
            String headerNameLower = header.getName().toLowerCase(Locale.ROOT);

            /*
             * Optimization to skip this header as it doesn't begin with any character starting header collections in
             * the deserialized headers type.
             */
            if (!headerCollectionsFirstCharacters.contains(headerNameLower.charAt(0))) {
                continue;
            }

            for (HeaderCollectionHandler headerCollectionHandler : headerCollectionHandlers) {
                if (headerCollectionHandler.headerStartsWithPrefix(headerNameLower)) {
                    headerCollectionHandler.addHeader(header.getName(), header.getValue());
                }
            }
        }

        /*
         * Finally inject all found header collection values into the deserialized headers.
         */
        headerCollectionHandlers.forEach(h -> h.injectValuesIntoDeclaringField(deserializedHeaders, logger));

        return deserializedHeaders;
    }


    @SuppressWarnings("deprecation")
    private static <S extends MapperBuilder<?, ?>> S initializeMapperBuilder(S mapper) {
        mapper.enable(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS)
            .enable(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT)
            .enable(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS)
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .serializationInclusion(JsonInclude.Include.NON_NULL)
            .addModule(new JavaTimeModule())
            .addModule(ByteArraySerializer.getModule())
            .addModule(Base64UrlSerializer.getModule())
            .addModule(DateTimeSerializer.getModule())
            .addModule(DateTimeDeserializer.getModule())
            .addModule(DateTimeRfc1123Serializer.getModule())
            .addModule(DurationSerializer.getModule())
            .addModule(HttpHeadersSerializer.getModule())
            .addModule(UnixTimeSerializer.getModule())
            .visibility(PropertyAccessor.FIELD, JsonAutoDetect.Visibility.ANY)
            .visibility(PropertyAccessor.SETTER, JsonAutoDetect.Visibility.NONE)
            .visibility(PropertyAccessor.GETTER, JsonAutoDetect.Visibility.NONE)
            .visibility(PropertyAccessor.IS_GETTER, JsonAutoDetect.Visibility.NONE);

        return mapper;
    }

    private JavaType createJavaType(Type type) {
        if (type == null) {
            return null;
        } else if (type instanceof JavaType) {
            return (JavaType) type;
        } else if (type instanceof ParameterizedType) {
            final ParameterizedType parameterizedType = (ParameterizedType) type;
            final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
            JavaType[] javaTypeArguments = new JavaType[actualTypeArguments.length];
            for (int i = 0; i != actualTypeArguments.length; i++) {
                javaTypeArguments[i] = createJavaType(actualTypeArguments[i]);
            }

            return typeToJavaTypeCache.computeIfAbsent(type, t -> mapper.getTypeFactory()
                .constructParametricType((Class<?>) parameterizedType.getRawType(), javaTypeArguments));
        } else {
            return typeToJavaTypeCache.computeIfAbsent(type, t -> mapper.getTypeFactory().constructType(t));
        }
    }

    /*
     * Internal helper class that helps manage converting headers into their header collection.
     */
    private static final class HeaderCollectionHandler {
        private final String prefix;
        private final int prefixLength;
        private final Map<String, String> values;
        private final Field declaringField;

        HeaderCollectionHandler(String prefix, Field declaringField) {
            this.prefix = prefix;
            this.prefixLength = prefix.length();
            this.values = new HashMap<>();
            this.declaringField = declaringField;
        }

        boolean headerStartsWithPrefix(String headerName) {
            return headerName.startsWith(prefix);
        }

        void addHeader(String headerName, String headerValue) {
            values.put(headerName.substring(prefixLength), headerValue);
        }

        @SuppressWarnings("deprecation")
        void injectValuesIntoDeclaringField(Object deserializedHeaders, ClientLogger logger) {
            /*
             * First check if the deserialized headers type has a public setter.
             */
            if (usePublicSetter(deserializedHeaders, logger)) {
                return;
            }

            logger.verbose("Failed to find or use public setter to set header collection.");

            /*
             * Otherwise fallback to setting the field directly.
             */
            final boolean declaredFieldAccessibleBackup = declaringField.isAccessible();
            try {
                if (!declaredFieldAccessibleBackup) {
                    AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                        declaringField.setAccessible(true);
                        return null;
                    });
                }
                declaringField.set(deserializedHeaders, values);
                logger.verbose("Set header collection by accessing the field directly.");
            } catch (IllegalAccessException ex) {
                logger.warning("Failed to inject header collection values into deserialized headers.", ex);
            } finally {
                if (!declaredFieldAccessibleBackup) {
                    AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                        declaringField.setAccessible(false);
                        return null;
                    });
                }
            }
        }

        private boolean usePublicSetter(Object deserializedHeaders, ClientLogger logger) {
            try {
                String potentialSetterName = getPotentialSetterName();

                // This could be cached.
                Method setterMethod = deserializedHeaders.getClass().getDeclaredMethod(potentialSetterName, Map.class);
                if (Modifier.isPublic(setterMethod.getModifiers())) {
                    setterMethod.invoke(deserializedHeaders, values);
                    logger.verbose("User setter %s on class %s to set header collection.", potentialSetterName,
                        deserializedHeaders.getClass().getSimpleName());
                    return true;
                }

                return false;
            } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException ignored) {
                return false;
            }
        }

        private String getPotentialSetterName() {
            String fieldName = declaringField.getName();

            return "set" + fieldName.substring(0, 1).toUpperCase(Locale.ROOT) + fieldName.substring(1);
        }
    }
}
