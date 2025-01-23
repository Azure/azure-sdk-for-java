// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.util;

import io.clientcore.core.instrumentation.logging.ClientLogger;

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
 *
 * <!-- src_embed io.clientcore.core.util.union.UnionJavaDocCodeSnippetsBasic -->
 * <pre>
 * Union union = Union.ofTypes&#40;String.class, Integer.class&#41;;
 * </pre>
 * <!-- end io.clientcore.core.util.union.UnionJavaDocCodeSnippetsBasic -->
 *
 * <p><strong>Create an instance from primitives</strong></p>
 *
 * <!-- src_embed io.clientcore.core.util.union.UnionJavaDocCodeSnippetsPrimitiveType -->
 * <pre>
 * Union unionPrimitives = Union.ofTypes&#40;int.class, double.class&#41;;
 * </pre>
 * <!-- end io.clientcore.core.util.union.UnionJavaDocCodeSnippetsPrimitiveType -->
 *
 * <p><strong>Create an instance from collections</strong></p>
 *
 * <!-- src_embed io.clientcore.core.util.union.UnionJavaDocCodeSnippetsCollectionType -->
 * <pre>
 * Union unionCollections = Union.ofTypes&#40;
 *     new GenericParameterizedType&#40;List.class, String.class&#41;,
 *     new GenericParameterizedType&#40;List.class, Integer.class&#41;&#41;;
 * </pre>
 * <!-- end io.clientcore.core.util.union.UnionJavaDocCodeSnippetsCollectionType -->
 *
 * <p><strong>Consume the value of the Union if it is of the expected type</strong></p>
 *
 * <!-- src_embed io.clientcore.core.util.union.UnionJavaDocCodeSnippetsSwitch -->
 * <pre>
 * Union union = Union.ofTypes&#40;String.class, Integer.class&#41;;
 * union.setValue&#40;&quot;Hello&quot;&#41;;
 * switch &#40;union.getValue&#40;&#41;&#41; &#123;
 *     case String s -&gt; System.out.println&#40;&quot;String value: &quot; + s&#41;;
 *     case Integer i -&gt; System.out.println&#40;&quot;Integer value: &quot; + i&#41;;
 *     default -&gt; throw new IllegalArgumentException&#40;
 *         &quot;Unknown type: &quot; + union.getCurrentType&#40;&#41;.getTypeName&#40;&#41;&#41;;
 * &#125;
 * </pre>
 * <!-- end io.clientcore.core.util.union.UnionJavaDocCodeSnippetsSwitch -->
 *
 * or
 *
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
 *
 */
public final class Union {
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
        this.types = typeCopy;
    }

    /**
     * Creates a new instance of {@link Union} with the provided types.
     * <p>
     * Currently, the types can be of type {@link Class} or {@link ParameterizedType}. If the type is a {@link Class},
     * it represents a simple type. If the type is a {@link ParameterizedType}, it represents a generic type.
     * For example, {@code List<String>} would be represented as {@code new GenericParameterizedType(List.class, String.class)}.
     * </p>
     *
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
     * Sets the value of the union.
     *
     * @param value The value of the union.
     * @throws IllegalArgumentException If the value is not of one of the types in the union.
     */
    @SuppressWarnings("unchecked")
    public void setValue(Object value) {
        for (Type type : types) {
            if (isInstanceOfType(value, type) || isPrimitiveTypeMatch(value, type)) {
                this.value = value;
                this.currentType = type;
                return;
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
     * Gets the types of the union.
     *
     * @return The types of the union.
     */
    public List<Type> getSupportedTypes() {
        return Collections.unmodifiableList(types);
    }

    /**
     * Gets the value of the union.
     *
     * @return The value of the union.
     * @param <T> The type of the value.
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue() {
        return (T) value;
    }

    /**
     * Gets the value of the union if it is of the expected type.
     *
     * @param clazz The expected type of the value.
     * @return The value of the union.
     * @param <T> The expected type of the value.
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
     * @param clazz The expected type of the value.
     * @param genericTypes The generic types of the expected type.
     *
     * @return The value of the union.
     * @param <T> The expected type of the value.
     */
    public <T> T getValue(Class<T> clazz, Class<?>... genericTypes) {
        return getValue(new GenericParameterizedType(clazz, genericTypes));
    }

    /**
     * Gets the value of the union if it is of the expected type.
     *
     * @param type The expected type of the value.
     *
     * @return The value of the union.
     * @param <T> The expected type of the value.
     */
    @SuppressWarnings("unchecked")
    public <T> T getValue(ParameterizedType type) {
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
     * @param clazz The expected type of the value.
     * @return Returns true if the value was consumable by the consumer, and false if it was not.
     * @param <T> The value type expected by the consumer.
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
     * @param consumer A consumer that will consume the value of the Union if it is of the expected type.
     * @param clazz The expected type of the value.
     * @param genericTypes A var-args representation of generic types that are expected by the consumer, for example,
     *                     <code>List&lt;String&gt;</code> would be represented as <pre>List.class, String.class</pre>.
     * @return Returns true if the value was consumable by the consumer, and false if it was not.
     * @param <T> The value type expected by the consumer.
     */
    public <T> boolean tryConsume(Consumer<T> consumer, Class<T> clazz, Class<?>... genericTypes) {
        return tryConsume(consumer, new GenericParameterizedType(clazz, genericTypes));
    }

    /**
     * This method is used to consume the value of the Union if it is of the expected type.
     *
     * @param consumer A consumer that will consume the value of the Union if it is of the expected type.
     * @param type The expected type of the value.
     * @return Returns true if the value was consumable by the consumer, and false if it was not.
     * @param <T> The value type expected by the consumer.
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
            if (pType.getRawType()instanceof Class<?> rawType && rawType.isInstance(value)) {
                Type[] actualTypeArguments = pType.getActualTypeArguments();
                if (value instanceof Collection<?> collection) {
                    return collection.stream()
                        .allMatch(element -> element != null
                            && Arrays.stream(actualTypeArguments).anyMatch(arg -> isInstanceOfType(element, arg)));
                }
            }
        } else if (type instanceof Class<?> clazz) {
            return clazz.isInstance(value);
        }
        return false;
    }

    private boolean isPrimitiveTypeMatch(Object value, Type type) {
        if (type instanceof Class<?> clazz) {
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
}
