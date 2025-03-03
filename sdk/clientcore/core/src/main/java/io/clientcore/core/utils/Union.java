// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.utils;

import io.clientcore.core.implementation.GenericParameterizedType;
import io.clientcore.core.instrumentation.logging.ClientLogger;
import io.clientcore.core.serialization.json.JsonReader;
import io.clientcore.core.serialization.json.JsonSerializable;
import io.clientcore.core.serialization.json.JsonToken;
import io.clientcore.core.serialization.json.JsonWriter;

import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;

/**
 * A class that represents a union of types. A type that is one of a finite set of sum types.
 * This class is used to represent a union of types in a type-safe manner.
 *
 * <p><strong>Create an instance</strong></p>
 * <p>
 * <!-- src_embed io.clientcore.core.util.union.UnionJavaDocCodeSnippetsBasic -->
 * <pre>
 * Union union = Union.ofTypes&#40;String.class, Integer.class&#41;;
 * </pre>
 * <!-- end io.clientcore.core.util.union.UnionJavaDocCodeSnippetsBasic -->
 *
 * <p><strong>Create an instance from primitives</strong></p>
 * <p>
 * <!-- src_embed io.clientcore.core.util.union.UnionJavaDocCodeSnippetsPrimitiveType -->
 * <pre>
 * Union unionPrimitives = Union.ofTypes&#40;int.class, double.class&#41;;
 * </pre>
 * <!-- end io.clientcore.core.util.union.UnionJavaDocCodeSnippetsPrimitiveType -->
 *
 * <p><strong>Create an instance from collections</strong></p>
 * <p>
 * <!-- src_embed io.clientcore.core.util.union.UnionJavaDocCodeSnippetsCollectionType -->
 * <pre>
 * &#47;&#47; GenericParameterizedType is a non-public helper class that allows us to specify a generic type with
 * &#47;&#47; a class and a type. User can define any similar class to achieve the same functionality.
 * Union unionCollections = Union.ofTypes&#40;
 *     new GenericParameterizedType&#40;List.class, String.class&#41;,
 *     new GenericParameterizedType&#40;List.class, Integer.class&#41;&#41;;
 * </pre>
 * <!-- end io.clientcore.core.util.union.UnionJavaDocCodeSnippetsCollectionType -->
 *
 * <p><strong>Consume the value of the Union if it is of the expected type</strong></p>
 * <p>
 * <!-- src_embed io.clientcore.core.util.union.UnionJavaDocCodeSnippetsIfElseStatement -->
 * <pre>
 * Union union = Union.ofTypes&#40;String.class, Integer.class&#41;;
 * union.setValue&#40;&quot;Hello&quot;&#41;;
 * Object value = union.getValue&#40;&#41;;
 * &#47;&#47; we can write an if-else block to consume the value in Java 8+, or switch pattern match in Java 17+
 * if &#40;value instanceof String&#41; &#123;
 *     String s = &#40;String&#41; value;
 *     System.out.println&#40;&quot;String value: &quot; + s&#41;;
 * &#125; else if &#40;value instanceof Integer&#41; &#123;
 *     Integer i = &#40;Integer&#41; value;
 *     System.out.println&#40;&quot;Integer value: &quot; + i&#41;;
 * &#125; else &#123;
 *     throw new IllegalArgumentException&#40;&quot;Unknown type: &quot; + union.getCurrentType&#40;&#41;.getTypeName&#40;&#41;&#41;;
 * &#125;
 * </pre>
 * <!-- end io.clientcore.core.util.union.UnionJavaDocCodeSnippetsIfElseStatement -->
 * <p>
 * or
 * <p>
 * <!-- src_embed io.clientcore.core.util.union.UnionJavaDocCodeSnippetsLambda -->
 * <pre>
 * Union union = Union.ofTypes&#40;String.class, Integer.class&#41;;
 * union.setValue&#40;&quot;Hello&quot;&#41;;
 * union.tryConsume&#40;
 *     v -&gt; System.out.println&#40;&quot;String value: &quot; + v&#41;, String.class&#41;;
 * union.tryConsume&#40;
 *     v -&gt; System.out.println&#40;&quot;Integer value: &quot; + v&#41;, Integer.class&#41;;
 * </pre>
 * <!-- end io.clientcore.core.util.union.UnionJavaDocCodeSnippetsLambda -->
 */
public final class Union implements JsonSerializable<Union> {
    private static final ClientLogger LOGGER = new ClientLogger(Union.class);

    private final List<Type> types;
    private Object value;
    private Type currentType;

    private Union(Type... types) {
        if (types == null || types.length == 0) {
            throw LOGGER.logThrowableAsError(new IllegalArgumentException("types cannot be null or empty"));
        }

        ArrayList<Type> typeCopy = new ArrayList<>(types.length);
        for (int i = 0; i < types.length; i++) {
            final Type currentType = types[i];
            if (currentType == null) {
                throw LOGGER.logThrowableAsError(
                    new IllegalArgumentException("types cannot contain null values: null value in index " + i));
            } else if (!(currentType instanceof Class<?> || currentType instanceof ParameterizedType)) {
                throw LOGGER.logThrowableAsError(new IllegalArgumentException(
                    String.format("types must be of type Class or ParameterizedType: type name is %s in index %d.",
                        currentType.getTypeName(), i)));
            }

            typeCopy.add(types[i]);
        }
        this.types = Collections.unmodifiableList(typeCopy);
    }

    /**
     * Creates a new instance of {@link Union} with the provided types.
     * <p>
     * Currently, the types can be of type {@link Class} or {@link ParameterizedType}. If the type is a {@link Class},
     * it represents a simple type. If the type is a {@link ParameterizedType}, it represents a generic type.
     * For example, {@code List<String>} would be represented as {@code new GenericParameterizedType(List.class, String.class)}.
     * </p>
     * <p>
     * It throws {@link IllegalArgumentException} if:
     * <ul>
     *   <li>value is not of one of the types in the union,</li>
     *   <li>value is null,</li>
     *   <li>types array is null or empty,</li>
     *   <li>types array contains a null value,</li>
     *   <li>types array contains a type that is not of type {@link Class} or {@link ParameterizedType}.</li>
     * </ul>
     *
     * @param types The types of the union.
     * @return A new instance of {@link Union}.
     */
    public static Union ofTypes(Type... types) {
        return new Union(types);
    }

    /**
     * Sets the value of the union. A new updated immutable union is returned.
     *
     * @param value The value of the union.
     * @return A new updated immutable union.
     * @throws IllegalArgumentException If the value is not of one of the types in the union.
     */
    @SuppressWarnings("unchecked")
    public Union setValue(Object value) {
        if (value == null) {
            this.value = null;
            return this;
        }

        for (Type type : types) {
            if (isInstanceOfType(value, type) || isPrimitiveTypeMatch(value, type)) {
                this.value = value;
                this.currentType = type;
                return this;
            }
        }

        throw LOGGER.logThrowableAsError(new IllegalArgumentException("Invalid type: " + value.getClass().getName()));
    }

    /**
     * Gets the type of the value.
     *
     * @return The type of the value.
     */
    public Type getCurrentType() {
        return currentType;
    }

    /**
     * Gets the types of the union. The types are unmodifiable.
     *
     * @return The types of the union.
     */
    public List<Type> getSupportedTypes() {
        return types;
    }

    /**
     * Gets the value of the union.
     *
     * @param <T> The type of the value.
     * @return The value of the union.
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue() {
        return (T) value;
    }

    /**
     * Gets the value of the union if it is of the expected type.
     *
     * @param clazz The expected type of the value.
     * @param <T>   The expected type of the value.
     * @return The value of the union.
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue(Class<T> clazz) {
        if (clazz == currentType) {
            return (T) value;
        }

        if (clazz.isInstance(value)) {
            return clazz.cast(value);
        }
        if (isPrimitiveTypeMatch(value, clazz)) {
            return (T) value;
        }
        throw LOGGER.logThrowableAsError(new IllegalArgumentException("Value is not of type: " + clazz.getName()));
    }

    /**
     * Gets the value of the union if it is of the expected type.
     *
     * @param clazz        The expected type of the value.
     * @param genericTypes The generic types of the expected type.
     * @param <T>          The expected type of the value.
     * @return The value of the union.
     */
    public <T> T getValue(Class<T> clazz, Class<?>... genericTypes) {
        return getValue(new GenericParameterizedType(clazz, genericTypes));
    }

    /**
     * Gets the value of the union if it is of the expected type.
     *
     * @param type The expected type of the value.
     * @param <T>  The expected type of the value.
     * @return The value of the union.
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue(Type type) {
        if (type == currentType) {
            return (T) value;
        }

        if (isInstanceOfType(value, type)) {
            return (T) value;
        }
        throw LOGGER.logThrowableAsError(new IllegalArgumentException("Value is not of type: " + type.getTypeName()));
    }

    /**
     * This method is used to consume the value of the Union if it is of the expected type.
     *
     * @param consumer A consumer that will consume the value of the Union if it is of the expected type.
     * @param clazz    The expected type of the value.
     * @param <T>      The value type expected by the consumer.
     * @return Returns true if the value was consumable by the consumer, and false if it was not.
     */
    @SuppressWarnings("unchecked")
    public <T> boolean tryConsume(Consumer<T> consumer, Class<T> clazz) {
        if (clazz == currentType) {
            consumer.accept((T) value);
            return true;
        }

        if (isInstanceOfType(value, clazz)) {
            consumer.accept(clazz.cast(value));
            return true;
        }

        if (isPrimitiveTypeMatch(value, clazz)) {
            consumer.accept((T) value);
            return true;
        }
        return false;
    }

    /**
     * This method is used to consume the value of the Union if it is of the expected type.
     *
     * @param consumer     A consumer that will consume the value of the Union if it is of the expected type.
     * @param clazz        The expected type of the value.
     * @param genericTypes A var-args representation of generic types that are expected by the consumer, for example,
     *                     <code>List&lt;String&gt;</code> would be represented as <pre>List.class, String.class</pre>.
     * @param <T>          The value type expected by the consumer.
     * @return Returns true if the value was consumable by the consumer, and false if it was not.
     */
    public <T> boolean tryConsume(Consumer<T> consumer, Class<T> clazz, Class<?>... genericTypes) {
        return tryConsume(consumer, new GenericParameterizedType(clazz, genericTypes));
    }

    /**
     * This method is used to consume the value of the Union if it is of the expected type.
     *
     * @param consumer A consumer that will consume the value of the Union if it is of the expected type.
     * @param type     The expected type of the value.
     * @param <T>      The value type expected by the consumer.
     * @return Returns true if the value was consumable by the consumer, and false if it was not.
     */
    @SuppressWarnings("unchecked")
    public <T> boolean tryConsume(Consumer<T> consumer, ParameterizedType type) {
        if (type == currentType) {
            consumer.accept((T) value);
            return true;
        }

        if (isInstanceOfType(value, type)) {
            consumer.accept((T) value);
            return true;
        }
        return false;
    }

    @Override
    public String toString() {
        return value == null
            ? "Union{types=" + types + ", value=null" + "}"
            : "Union{types=" + types + ", type=" + (currentType == null ? null : currentType.getTypeName()) + ", value="
            + value + "}";
    }

    private boolean isInstanceOfType(Object value, Type type) {
        if (value == null) {
            return false;
        }

        if (type instanceof ParameterizedType) {
            ParameterizedType pType = (ParameterizedType) type;
            if (pType.getRawType() instanceof Class<?> && ((Class<?>) pType.getRawType()).isInstance(value)) {
                Type[] actualTypeArguments = pType.getActualTypeArguments();
                if (value instanceof Collection<?>) {
                    Collection<?> collection = (Collection<?>) value;
                    return collection.stream()
                        .allMatch(element -> element != null
                            && Arrays.stream(actualTypeArguments).anyMatch(arg -> isInstanceOfType(element, arg)));
                }
            }
        } else if (type instanceof Class<?>) {
            return ((Class<?>) type).isInstance(value);
        }
        return false;
    }

    private boolean isPrimitiveTypeMatch(Object value, Type type) {
        if (type instanceof Class<?>) {
            Class<?> clazz = (Class<?>) type;
            if (clazz.isPrimitive()) {
                if ((clazz == int.class && value instanceof Integer)
                    || (clazz == long.class && value instanceof Long)
                    || (clazz == float.class && value instanceof Float)
                    || (clazz == double.class && value instanceof Double)
                    || (clazz == boolean.class && value instanceof Boolean)
                    || (clazz == byte.class && value instanceof Byte)
                    || (clazz == char.class && value instanceof Character)
                    || (clazz == short.class && value instanceof Short)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public JsonWriter toJson(JsonWriter jsonWriter) throws IOException {
        if (value == null) {
            return jsonWriter;
        }
        writeValue(jsonWriter, value);
        return jsonWriter;
    }

    private void writeValue(JsonWriter jsonWriter, Object value) throws IOException {
        if (value instanceof JsonSerializable) {
            ((JsonSerializable<?>) value).toJson(jsonWriter);
        } else if (value instanceof String) {
            jsonWriter.writeString((String) value);
        } else if (value instanceof Integer) {
            jsonWriter.writeInt((Integer) value);
        } else if (value instanceof Long) {
            jsonWriter.writeLong((Long) value);
        } else if (value instanceof Float) {
            jsonWriter.writeFloat((Float) value);
        } else if (value instanceof Double) {
            jsonWriter.writeDouble((Double) value);
        } else if (value instanceof Boolean) {
            jsonWriter.writeBoolean((Boolean) value);
        } else if (value instanceof byte[]) {
            jsonWriter.writeBinary((byte[]) value);
        } else if (value instanceof List && ((List<?>) value).isEmpty()) {
            jsonWriter.writeStartArray();
            jsonWriter.writeEndArray();
        } else if (value instanceof List<?>) {
            jsonWriter.writeStartArray();
            for (Object item : (List<?>) value) {
                writeValue(jsonWriter, item);
            }
            jsonWriter.writeEndArray();
        } else {
            throw LOGGER.logThrowableAsError(new IllegalArgumentException("Invalid type: " + value.getClass().getName()));
        }
    }

    public static Union fromJson(JsonReader originalReader, Type... types) throws IOException {
        if (originalReader.currentToken() == null || originalReader.currentToken() == JsonToken.FIELD_NAME) {
            originalReader.nextToken();
        }

        JsonReader reader = originalReader;
        String jsonChildren = null;
        if (originalReader.currentToken() == JsonToken.START_ARRAY || originalReader.currentToken() == JsonToken.START_OBJECT) {
            jsonChildren = originalReader.readChildren();
        }

        Union retVal = Union.ofTypes(types);
        Object value = null;
        for (Type type : types) {
            try {
                if(jsonChildren != null) {
                    reader = JsonReader.fromString(jsonChildren);
                    reader.nextToken();
                }
                value = readValue(reader, type);
                if (value != null) {
                    break;
                }
            } catch (IOException e) {
                // ignore exception and try next type
//                System.out.println("Failed to read value of type " + type.getTypeName() + ": " + e.getMessage());
            }
        }
        return retVal.setValue(value);
    }

    private static Object readValue(JsonReader reader, Type type) throws IOException {
        // Handle parameterized types - only List<T> is currently supported.
        // TODO (srnagar): support Map<K, V> and other parameterized types.
        if (type instanceof ParameterizedType) {
            ParameterizedType parameterizedType = (ParameterizedType) type;
            if (parameterizedType.getRawType() instanceof Class<?>
                && ((Class<?>) parameterizedType.getRawType()).isAssignableFrom(List.class)
                && reader.currentToken() == JsonToken.START_ARRAY) {
                return reader.readArray(r -> readValue(r, parameterizedType.getActualTypeArguments()[0]));
            }
        } else if (type instanceof Class<?>) {
            Class<?> clazz = (Class<?>) type;
            if (reader.currentToken() == JsonToken.START_OBJECT && JsonSerializable.class.isAssignableFrom(((Class<?>) type))) {
                // for Union types, only JsonSerializable objects are supported and they are expected to have
                // a static fromJson method that takes a JsonReader and returns an instance of the class
                try {
                    Method method = clazz.getDeclaredMethod("fromJson", JsonReader.class);
                    return method.invoke(null, reader);
                } catch (ReflectiveOperationException e) {
                    throw new IOException("Failed to create instance of " + clazz.getName(), e);
                }
            } else {
                if (clazz == String.class && reader.currentToken() == JsonToken.STRING) {
                    return reader.getString();
                } else if ((clazz == Float.class || clazz == float.class) && reader.currentToken() == JsonToken.NUMBER) {
                    return reader.getFloat();
                } else if ((clazz == Double.class || clazz == double.class) && reader.currentToken() == JsonToken.NUMBER) {
                    return reader.getDouble();
                } else if ((clazz == Boolean.class || clazz == boolean.class) && reader.currentToken() == JsonToken.BOOLEAN) {
                    return reader.getBoolean();
                } else if ((clazz == Integer.class || clazz == int.class) && reader.currentToken() == JsonToken.NUMBER) {
                    return reader.getInt();
                } else if ((clazz == Long.class || clazz == long.class) && reader.currentToken() == JsonToken.NUMBER) {
                    return reader.getLong();
                } else if (clazz == byte[].class && reader.currentToken() == JsonToken.STRING) {
                    return reader.getBinary();
                }
            }
        }
        throw new IOException("Failed to read value of type " + type.getTypeName());
//        return null;
    }
}
