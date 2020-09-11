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
import com.azure.core.util.ExpandableStringEnum;
import com.azure.core.util.logging.ClientLogger;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import com.fasterxml.jackson.dataformat.xml.ser.ToXmlGenerator;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.nio.charset.StandardCharsets;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.regex.Pattern;

import static java.util.stream.Collectors.toMap;

/**
 * Implementation of {@link SerializerAdapter} for Jackson.
 */
public class JacksonAdapter implements SerializerAdapter {
    private static final Pattern PATTERN = Pattern.compile("^\"*|\"*$");
    private static final Map<Type, JavaType> JAVA_TYPE_CACHE = new ConcurrentHashMap<>();

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

    /**
     * Creates a new JacksonAdapter instance with default mapper settings.
     */
    public JacksonAdapter() {
        simpleMapper = initializeObjectMapper(new ObjectMapper());

        xmlMapper = XmlMapper.builder()
            .defaultUseWrapper(false)
            .configure(ToXmlGenerator.Feature.WRITE_XML_DECLARATION, true)
            .build();
        initializeObjectMapper(xmlMapper);

        ObjectMapper flatteningMapper = initializeObjectMapper(new ObjectMapper())
            .registerModule(FlatteningSerializer.getModule(simpleMapper()))
            .registerModule(FlatteningDeserializer.getModule(simpleMapper()));

        mapper = initializeObjectMapper(new ObjectMapper())
            // Order matters: must register in reverse order of hierarchy
            .registerModule(AdditionalPropertiesSerializer.getModule(flatteningMapper))
            .registerModule(AdditionalPropertiesDeserializer.getModule(flatteningMapper))
            .registerModule(FlatteningSerializer.getModule(simpleMapper()))
            .registerModule(FlatteningDeserializer.getModule(simpleMapper()));

        headerMapper = simpleMapper
            .copy()
            .configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
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

    @SuppressWarnings({"unchecked", "rawtypes"})
    static <T> T convertText(String value, Class<T> type) {
        // Check the default value's type to determine how it needs to be converted.
        Object convertedValue;
        if (byte.class.isAssignableFrom(type)) {
            convertedValue = convertToPrimitive(value, Byte::parseByte);
        } else if (Byte.class.isAssignableFrom(type)) {
            convertedValue = convertToBoxedPrimitive(value, Byte::valueOf);
        } else if (short.class.isAssignableFrom(type)) {
            convertedValue = convertToPrimitive(value, Short::parseShort);
        } else if (Short.class.isAssignableFrom(type)) {
            convertedValue = convertToBoxedPrimitive(value, Short::valueOf);
        } else if (int.class.isAssignableFrom(type)) {
            convertedValue = convertToPrimitive(value, Integer::parseInt);
        } else if (Integer.class.isAssignableFrom(type)) {
            convertedValue = convertToBoxedPrimitive(value, Integer::valueOf);
        } else if (long.class.isAssignableFrom(type)) {
            convertedValue = convertToPrimitive(value, Long::parseLong);
        } else if (Long.class.isAssignableFrom(type)) {
            convertedValue = convertToBoxedPrimitive(value, Long::valueOf);
        } else if (float.class.isAssignableFrom(type)) {
            convertedValue = convertToPrimitive(value, Float::parseFloat);
        } else if (Float.class.isAssignableFrom(type)) {
            convertedValue = convertToBoxedPrimitive(value, Float::valueOf);
        } else if (double.class.isAssignableFrom(type)) {
            convertedValue = convertToPrimitive(value, Double::parseDouble);
        } else if (Double.class.isAssignableFrom(type)) {
            convertedValue = convertToBoxedPrimitive(value, Double::valueOf);
        } else if (boolean.class.isAssignableFrom(type)) {
            convertedValue = Boolean.parseBoolean(value);
        } else if (Boolean.class.isAssignableFrom(type)) {
            convertedValue = convertToBoxedPrimitive(value, Boolean::valueOf);
        } else if (Enum.class.isAssignableFrom(type)) {
            if (CoreUtils.isNullOrEmpty(value)) {
                return null;
            }

            Class<? extends Enum> enumType = (Class<? extends Enum>) type;
            convertedValue = Enum.valueOf(enumType, value);
        } else if (ExpandableStringEnum.class.isAssignableFrom(type)) {
            if (CoreUtils.isNullOrEmpty(value)) {
                return null;
            }

            Class<? extends ExpandableStringEnum> enumType = (Class<? extends ExpandableStringEnum>) type;
            convertedValue = ExpandableStringEnum.fromString(value, enumType);
        } else if (CharSequence.class.isAssignableFrom(type)) {
            // Should this check if there are any String only constructors or static factories?
            convertedValue = value;
        } else {
            throw new IllegalStateException(String.format("Unable to convert 'text' to type %s.", type.getName()));
        }

        return (T) convertedValue;
    }

    private static <T> T convertToBoxedPrimitive(String value, Function<String, T> converter) {
        return CoreUtils.isNullOrEmpty(value) ? null : converter.apply(value);
    }

    private static <T> T convertToPrimitive(String value, Function<String, T> converter) {
        return CoreUtils.isNullOrEmpty(value) ? converter.apply("0") : converter.apply(value);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(String value, Type type, SerializerEncoding encoding) throws IOException {
        if (CoreUtils.isNullOrEmpty(value)) {
            return null;
        }

        final JavaType javaType = createJavaType(type);
        try {
            if (encoding == SerializerEncoding.XML) {
                return (T) xmlMapper.readValue(value, javaType);
            } else if (encoding == SerializerEncoding.TEXT) {
                return (T) convertText(value, javaType.getRawClass());
            } else {
                return (T) serializer().readValue(value, javaType);
            }
        } catch (JsonParseException jpe) {
            throw logger.logExceptionAsError(new MalformedValueException(jpe.getMessage(), jpe));
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public <T> T deserialize(InputStream inputStream, final Type type, SerializerEncoding encoding)
        throws IOException {
        if (inputStream == null) {
            return null;
        }

        final JavaType javaType = createJavaType(type);
        try {
            if (encoding == SerializerEncoding.XML) {
                return (T) xmlMapper.readValue(inputStream, javaType);
            } else if (encoding == SerializerEncoding.TEXT) {
                ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    outputStream.write(buffer, 0, length);
                }
                return (T) convertText(outputStream.toString("UTF-8"), javaType.getRawClass());
            } else {
                return (T) serializer().readValue(inputStream, javaType);
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
        for (final Field declaredField : deserializedHeadersClass.getDeclaredFields()) {
            if (!declaredField.isAnnotationPresent(HeaderCollection.class)) {
                continue;
            }

            final Type declaredFieldType = declaredField.getGenericType();
            if (!TypeUtil.isTypeOrSubTypeOf(declaredField.getType(), Map.class)) {
                continue;
            }

            final Type[] mapTypes = TypeUtil.getTypeArguments(declaredFieldType);
            if (mapTypes.length != 2 || mapTypes[0] != String.class || mapTypes[1] != String.class) {
                continue;
            }

            final HeaderCollection headerCollectionAnnotation = declaredField.getAnnotation(HeaderCollection.class);
            final String collectionPrefix = headerCollectionAnnotation.value().toLowerCase(Locale.ROOT);
            if (collectionPrefix.length() <= 0) {
                continue;
            }

            final Map<String, String> headerCollection = headers.stream()
                .filter(header -> header.getName().toLowerCase(Locale.ROOT).startsWith(collectionPrefix))
                .collect(toMap(header -> header.getName().substring(collectionPrefix.length()), HttpHeader::getValue));

            final boolean declaredFieldAccessibleBackup = declaredField.isAccessible();
            try {
                if (!declaredFieldAccessibleBackup) {
                    AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                        declaredField.setAccessible(true);
                        return null;
                    });
                }
                declaredField.set(deserializedHeaders, headerCollection);
            } catch (IllegalAccessException ignored) {
            } finally {
                if (!declaredFieldAccessibleBackup) {
                    AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
                        declaredField.setAccessible(false);
                        return null;
                    });
                }
            }
        }

        return deserializedHeaders;
    }

    /**
     * Initializes an instance of JacksonMapperAdapter with default configurations applied to the object mapper.
     *
     * @param mapper the object mapper to use.
     */
    private static <T extends ObjectMapper> T initializeObjectMapper(T mapper) {
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false)
            .configure(SerializationFeature.WRITE_EMPTY_JSON_ARRAYS, true)
            .configure(SerializationFeature.FAIL_ON_EMPTY_BEANS, false)
            .configure(DeserializationFeature.ACCEPT_EMPTY_STRING_AS_NULL_OBJECT, true)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .configure(DeserializationFeature.ACCEPT_SINGLE_VALUE_AS_ARRAY, true)
            .setSerializationInclusion(JsonInclude.Include.NON_NULL)
            .registerModule(new JavaTimeModule())
            .registerModule(ByteArraySerializer.getModule())
            .registerModule(Base64UrlSerializer.getModule())
            .registerModule(DateTimeSerializer.getModule())
            .registerModule(DateTimeDeserializer.getModule())
            .registerModule(DateTimeRfc1123Serializer.getModule())
            .registerModule(DurationSerializer.getModule())
            .registerModule(HttpHeadersSerializer.getModule())
            .registerModule(UnixTimeSerializer.getModule());
        mapper.setVisibility(mapper.getSerializationConfig().getDefaultVisibilityChecker()
            .withFieldVisibility(JsonAutoDetect.Visibility.ANY)
            .withSetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withGetterVisibility(JsonAutoDetect.Visibility.NONE)
            .withIsGetterVisibility(JsonAutoDetect.Visibility.NONE));
        return mapper;
    }

    private JavaType createJavaType(Type type) {
        /*
         * Early outs when type is null and type is already a JavaType.
         */
        if (type == null) {
            return null;
        } else if (type instanceof JavaType) {
            return (JavaType) type;
        }

        /*
         * Check the cache for the JavaType for type already existing. If the type doesn't exist in the cache construct
         * the corresponding JavaType and add it to the cache.
         *
         * Caching is used to reduce the amount of reflective calls required. We only expect a limited set of types to
         * be used during the lifetime of the application as they are generally tied to code generation, which is mostly
         * static.
         */
        return JAVA_TYPE_CACHE.computeIfAbsent(type, t -> {
            if (type instanceof ParameterizedType) {
                final ParameterizedType parameterizedType = (ParameterizedType) type;
                final Type[] actualTypeArguments = parameterizedType.getActualTypeArguments();
                JavaType[] javaTypeArguments = new JavaType[actualTypeArguments.length];
                for (int i = 0; i != actualTypeArguments.length; i++) {
                    javaTypeArguments[i] = createJavaType(actualTypeArguments[i]);
                }

                return mapper.getTypeFactory()
                    .constructParametricType((Class<?>) parameterizedType.getRawType(), javaTypeArguments);
            }

            return mapper.getTypeFactory().constructType(t);
        });
    }

}
