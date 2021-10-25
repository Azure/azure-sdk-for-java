// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.serializer;

import com.azure.core.http.HttpHeaders;
import com.azure.core.implementation.TypeUtil;
import com.azure.core.implementation.jackson.ObjectMapperShim;
import com.azure.core.util.Configuration;
import com.azure.core.util.CoreUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.lang.invoke.LambdaMetafactory;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.lang.reflect.Parameter;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.regex.Pattern;

/**
 * Implementation of {@link SerializerAdapter} for Jackson.
 */
public class JacksonAdapter implements SerializerAdapter {
    private static final Pattern PATTERN = Pattern.compile("^\"*|\"*$");
    static volatile Function<Callable<Object>, Object> accessHelper;

    private static final ClientLogger LOGGER = new ClientLogger(JacksonAdapter.class);

    static {
        accessHelper = loadAccessHelper(Configuration.getGlobalConfiguration());
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

    /*
     * The lazily-created serializer for this ServiceClient.
     */
    private static SerializerAdapter serializerAdapter;

    /**
     * Sets the access helper function that JacksonAdapter uses when serialization must cross access boundaries not
     * allowed globally. An example of this would be when a SecurityManager is being used and Jackson isn't able to
     * access non-public (private, package-private) constructors, fields, or methods. In this case, an application can
     * supply a well-known access helper function from itself which allows Jackson to cross the access boundaries
     * configured.
     *
     * @param accessHelper The access helper function that wraps serialization and deserialization calls.
     */
    public static void setAccessHelper(Function<Callable<Object>, Object> accessHelper) {
        JacksonAdapter.accessHelper = accessHelper;
    }

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

    private static Object useAccessHelper(Callable<Object> serializationCall) throws IOException {
        try {
            return accessHelper == null ? serializationCall.call() : accessHelper.apply(serializationCall);
        } catch (Exception ex) {
            if (ex instanceof IOException) {
                throw (IOException) ex;
            } else if (ex instanceof RuntimeException) {
                throw (RuntimeException) ex;
            }

            throw new IOException(ex);
        }
    }

    @SuppressWarnings("unchecked")
    static Function<Callable<Object>, Object> loadAccessHelper(Configuration configuration) {
        String accessHelper = configuration.get("AZURE_JACKSON_ADAPTER_ACCESS_HELPER");

        // Only attempt to search for the access helper method if AZURE_JACKSON_ADAPTER_ACCESS_HELPER is set
        // and AZURE_JACKSON_ADAPTER_ACCESS_HELPER contains #.
        if (CoreUtils.isNullOrEmpty(accessHelper) || !accessHelper.contains(".")) {
            return null;
        }

        try {
            StringBuilder validationErrors = new StringBuilder();
            int lastDot = accessHelper.lastIndexOf('.');

            Class<?> clazz = JacksonAdapter.class.getClassLoader().loadClass(accessHelper.substring(0, lastDot));
            if (!Modifier.isPublic(clazz.getModifiers())) {
                validationErrors.append("'AZURE_JACKSON_ADAPTER_ACCESS_HELPER' is a Method that "
                    + "isn't public, ignoring configuration.")
                    .append(System.lineSeparator());
            }

            Method method = clazz.getDeclaredMethod(accessHelper.substring(lastDot + 1), Callable.class);
            if (!Modifier.isPublic(method.getModifiers())) {
                validationErrors.append("'AZURE_JACKSON_ADAPTER_ACCESS_HELPER' is a Method that "
                    + "isn't public, ignoring configuration.")
                    .append(System.lineSeparator());
            }

            if (!Modifier.isStatic(method.getModifiers())) {
                validationErrors.append("'AZURE_JACKSON_ADAPTER_ACCESS_HELPER' is a Method that "
                        + "isn't static, ignoring configuration.")
                    .append(System.lineSeparator());
            }

            // The referenced method must return type Object and take only Callable<Object> as a parameter.
            if (method.getReturnType() != Object.class) {
                validationErrors.append("'AZURE_JACKSON_ADAPTER_ACCESS_HELPER' is a Method that "
                    + "returns a type other than Object, ignoring configuration.")
                    .append(System.lineSeparator());
            }

            Parameter[] parameterTypes = method.getParameters();
            if (TypeUtil.getTypeArgument(parameterTypes[0].getParameterizedType()) != Object.class) {
                validationErrors.append("'AZURE_JACKSON_ADAPTER_ACCESS_HELPER' is a Method that "
                    + "doesn't take Callable<Object> as the only parameter, ignoring configuration.");
            }

            if (validationErrors.length() != 0) {
                LOGGER.log(LogLevel.INFORMATIONAL, validationErrors::toString);
                return null;
            }

            MethodHandles.Lookup lookup = MethodHandles.lookup();
            MethodHandle accessHelperHandle = lookup.unreflect(method);

            return (Function<Callable<Object>, Object>) LambdaMetafactory.metafactory(lookup,
                    "apply",
                    MethodType.methodType(Function.class), MethodType.methodType(Object.class, Object.class),
                    accessHelperHandle, accessHelperHandle.type())
                .getTarget().invoke();
        } catch (Throwable ex) {
            LOGGER.log(LogLevel.INFORMATIONAL, () -> "Failed to lookup 'AZURE_JACKSON_ADAPTER_ACCESS_HELPER' "
                + "method. Execution will continue without usage of an access helper.", ex);
            return null;
        }
    }
}
