// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.serialization.json;

import io.clientcore.core.implementation.ReflectionUtils;
import io.clientcore.core.implementation.ReflectiveInvoker;
import io.clientcore.core.implementation.TypeUtil;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.models.binarydata.BinaryData;
import io.clientcore.core.serialization.ObjectSerializer;
import io.clientcore.core.serialization.SerializationFormat;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.lang.reflect.WildcardType;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Class providing basic JSON serialization and deserialization methods.
 * <p>
 * The implementation of this class is based on the usage of {@link JsonReader} and {@link JsonWriter}.
 * <p>
 * The deserialization methods only work with primitive types, simple list and map collections, and models implementing
 * {@link JsonSerializable}. Or, in code terms, types that are producible calling {@link JsonReader#readUntyped()} or
 * provide a static factory method {@code fromJson(JsonReader)}.
 * <p>
 * The serialization methods will work with any value but for complex types that don't implement
 * {@link JsonSerializable} they will serialize the object using the type's {@code toString()} method.
 */
public class JsonSerializer implements ObjectSerializer {
    private static final ClientLogger LOGGER = new ClientLogger(JsonSerializer.class);

    private static final JsonSerializer INSTANCE = new JsonSerializer();

    /**
     * Get an instance of the {@link JsonSerializer}
     * @return An instance of {@link JsonSerializer}
     */
    public static JsonSerializer getInstance() {
        return INSTANCE;
    }

    /**
     * Creates an instance of the {@link JsonSerializer}.
     */
    public JsonSerializer() {
    }

    /**
     * Reads a JSON byte array into its object representation.
     *
     * @param bytes The JSON byte array.
     * @param type {@link Type} representing the object.
     * @param <T> Type of the object.
     * @return The object represented by the deserialized JSON byte array.
     * @throws IOException If the deserialization fails.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserializeFromBytes(byte[] bytes, Type type) throws IOException {
        try (JsonReader jsonReader = JsonReader.fromBytes(bytes)) {
            if (type instanceof ParameterizedType && List.class.isAssignableFrom(TypeUtil.getRawClass(type))) {
                ParameterizedType parameterizedType = (ParameterizedType) type;
                Type listElementType = parameterizedType.getActualTypeArguments()[0];
                if (JsonSerializable.class.isAssignableFrom(TypeUtil.getRawClass(listElementType))) {
                    return deserializeListOfJsonSerializables(jsonReader, parameterizedType);
                } else if (BinaryData.class.isAssignableFrom(TypeUtil.getRawClass(listElementType))) {
                    return deserializeListOfBinaryData(jsonReader);
                } else {
                    return (T) convertValue(jsonReader.readUntyped(), parameterizedType);
                }
            } else if (type instanceof Class<?>
                && JsonSerializable.class.isAssignableFrom(TypeUtil.getRawClass(type))) {
                Class<T> clazz = (Class<T>) type;

                return (T) clazz.getMethod("fromJson", JsonReader.class).invoke(null, jsonReader);
            }
            return (T) convertValue(jsonReader.readUntyped(), type);
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw LOGGER.throwableAtError().log(e, RuntimeException::new);
        }
    }

    @SuppressWarnings("unchecked")
    private <T> T deserializeListOfBinaryData(JsonReader jsonReader) throws IOException {
        return (T) jsonReader.readArray(arrayReader -> BinaryData.fromObject(arrayReader.readUntyped()));
    }

    @SuppressWarnings("unchecked")
    private <T> T deserializeListOfJsonSerializables(JsonReader jsonReader, ParameterizedType parameterizedType)
        throws IOException {
        Type actualTypeArgument = parameterizedType.getActualTypeArguments()[0];
        Class<?> listElementClass = (Class<?>) actualTypeArgument;
        ReflectiveInvoker methodInvoker;
        try {

            Method fromJson = listElementClass.getDeclaredMethod("fromJson", JsonReader.class);
            fromJson.setAccessible(true);
            methodInvoker = ReflectionUtils.getMethodInvoker(listElementClass, fromJson);
        } catch (Exception e) {
            throw LOGGER.throwableAtError().log(e, RuntimeException::new);
        }
        return (T) jsonReader.readArray(arrayReader -> {
            try {
                return methodInvoker.invoke(arrayReader);
            } catch (Throwable e) {
                if (e instanceof Error) {
                    throw (Error) LOGGER.throwableAtError().log(message -> e);
                } else if (e instanceof IOException) {
                    throw (IOException) LOGGER.throwableAtError().log(message -> e);
                } else {
                    throw LOGGER.throwableAtError().log(e, RuntimeException::new);
                }
            }
        });
    }

    /**
     * Reads a JSON stream into its object representation.
     *
     * @param stream JSON stream.
     * @param type {@link Type} representing the object.
     * @param <T> Type of the object.
     * @return The object represented by the deserialized JSON stream.
     * @throws IOException If the deserialization fails.
     */
    @SuppressWarnings("unchecked")
    @Override
    public <T> T deserializeFromStream(InputStream stream, Type type) throws IOException {
        try (JsonReader jsonReader = JsonReader.fromStream(stream)) {
            if (type instanceof Class<?> && JsonSerializable.class.isAssignableFrom(TypeUtil.getRawClass(type))) {
                Class<T> clazz = (Class<T>) type;

                return (T) clazz.getMethod("fromJson", JsonReader.class).invoke(null, jsonReader);
            }
            return (T) convertValue(jsonReader.readUntyped(), type);
        } catch (InvocationTargetException | IllegalAccessException | NoSuchMethodException e) {
            throw LOGGER.throwableAtError().log(e, RuntimeException::new);
        }
    }

    private Object convertValue(Object value, Type targetType) throws IOException {
        if (value == null || targetType == null || targetType == Object.class) {
            return value;
        }

        if (targetType instanceof WildcardType) {
            Type[] upperBounds = ((WildcardType) targetType).getUpperBounds();
            return upperBounds.length == 0 ? value : convertValue(value, upperBounds[0]);
        }

        if (targetType instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) targetType;
            Class<?> rawType = TypeUtil.getRawClass(parameterizedType);
            Type[] typeArguments = parameterizedType.getActualTypeArguments();
            if (List.class.isAssignableFrom(rawType) && value instanceof List<?> && typeArguments.length == 1) {
                List<?> values = (List<?>) value;
                List<Object> convertedValues = new ArrayList<>(values.size());
                for (Object item : values) {
                    convertedValues.add(convertValue(item, typeArguments[0]));
                }
                return convertedValues;
            }

            if (Map.class.isAssignableFrom(rawType) && value instanceof Map<?, ?> && typeArguments.length == 2) {
                Map<?, ?> values = (Map<?, ?>) value;
                Map<Object, Object> convertedValues = new LinkedHashMap<>(values.size());
                for (Map.Entry<?, ?> entry : values.entrySet()) {
                    convertedValues.put(convertValue(entry.getKey(), typeArguments[0]),
                        convertValue(entry.getValue(), typeArguments[1]));
                }
                return convertedValues;
            }

            return convertValue(value, rawType);
        }

        if (!(targetType instanceof Class<?>)) {
            return value;
        }

        Class<?> targetClass = (Class<?>) targetType;
        if (targetClass.isInstance(value)) {
            return value;
        }
        if (targetClass == BinaryData.class) {
            return BinaryData.fromObject(value, this);
        }
        if (JsonSerializable.class.isAssignableFrom(targetClass)) {
            return deserializeFromBytes(serializeToBytes(value), targetClass);
        }
        if (targetClass == OffsetDateTime.class && value instanceof String) {
            return OffsetDateTime.parse((String) value);
        }
        if (targetClass == Duration.class && value instanceof String) {
            return Duration.parse((String) value);
        }
        if (targetClass == byte[].class && value instanceof String) {
            return Base64.getDecoder().decode((String) value);
        }
        if (targetClass.isEnum() && value instanceof String) {
            return convertEnumValue(targetClass, (String) value);
        }
        if (value instanceof Number) {
            return convertNumber((Number) value, targetClass);
        }
        if (targetClass == String.class) {
            return String.valueOf(value);
        }
        if ((targetClass == Character.class || targetClass == Character.TYPE)
            && value instanceof String
            && !((String) value).isEmpty()) {
            return ((String) value).charAt(0);
        }

        return value;
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Object convertEnumValue(Class<?> targetClass, String value) throws IOException {
        try {
            Method fromString = targetClass.getMethod("fromString", String.class);
            return fromString.invoke(null, value);
        } catch (NoSuchMethodException ignored) {
            return convertJavaEnumValue(targetClass, value);
        } catch (IllegalAccessException | InvocationTargetException exception) {
            throw LOGGER.throwableAtError()
                .log("Unable to deserialize enum value '" + value + "' as " + targetClass.getName(), exception,
                    IOException::new);
        }
    }

    @SuppressWarnings({ "unchecked", "rawtypes" })
    private static Object convertJavaEnumValue(Class<?> targetClass, String value) throws IOException {
        try {
            return Enum.valueOf((Class<? extends Enum>) targetClass, value);
        } catch (IllegalArgumentException exception) {
            throw LOGGER.throwableAtError()
                .log("Unable to deserialize enum value '" + value + "' as " + targetClass.getName(), exception,
                    IOException::new);
        }
    }

    private static Object convertNumber(Number value, Class<?> targetClass) {
        if (targetClass == Byte.class || targetClass == Byte.TYPE) {
            return value.byteValue();
        } else if (targetClass == Short.class || targetClass == Short.TYPE) {
            return value.shortValue();
        } else if (targetClass == Integer.class || targetClass == Integer.TYPE) {
            return value.intValue();
        } else if (targetClass == Long.class || targetClass == Long.TYPE) {
            return value.longValue();
        } else if (targetClass == Float.class || targetClass == Float.TYPE) {
            return value.floatValue();
        } else if (targetClass == Double.class || targetClass == Double.TYPE) {
            return value.doubleValue();
        }
        return value;
    }

    /**
     * Converts the object into a JSON byte array.
     *
     * @param value The object.
     * @return The JSON binary representation of the serialized object.
     * @throws IOException If the serialization fails.
     */
    @Override
    public byte[] serializeToBytes(Object value) throws IOException {
        if (value == null) {
            return null;
        }

        if (value instanceof JsonSerializable<?>) {
            return ((JsonSerializable<?>) value).toJsonBytes();
        }

        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            JsonWriter jsonWriter = JsonWriter.toStream(byteArrayOutputStream)) {

            jsonWriter.writeUntyped(value);
            jsonWriter.flush();

            return byteArrayOutputStream.toByteArray();
        }
    }

    /**
     * Writes an object's JSON representation into a stream.
     *
     * @param stream {@link OutputStream} where the object's JSON representation will be written.
     * @param value The object to serialize.
     * @throws IOException If the serialization fails.
     */
    @Override
    public void serializeToStream(OutputStream stream, Object value) throws IOException {
        if (value == null) {
            return;
        }

        try (JsonWriter jsonWriter = JsonWriter.toStream(stream)) {
            jsonWriter.writeUntyped(value);
        }
    }

    @Override
    public final boolean supportsFormat(SerializationFormat format) {
        return format == SerializationFormat.JSON;
    }
}
