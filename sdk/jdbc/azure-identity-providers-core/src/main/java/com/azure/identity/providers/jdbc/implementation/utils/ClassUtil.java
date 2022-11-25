// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.identity.providers.jdbc.implementation.utils;

import com.azure.core.exception.AzureException;
import com.azure.identity.providers.jdbc.implementation.exception.AzureInstantiateException;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * {@code java.lang.Class} utility methods.
 * Mainly for internal use.
 */
public final class ClassUtil {

    private static final Map<Class<?>, Object> DEFAULT_TYPE_VALUES;
    private static final Map<Class<?>, Class<?>> PRIMITIVE_WRAPPER_TYPE_MAP = new HashMap<>();
    private static final Map<Class<?>, Class<?>> PRIMITIVE_TYPE_TO_WRAPPER_MAP = new HashMap<>();

    static {
        Map<Class<?>, Object> values = new HashMap<>();
        values.put(boolean.class, false);
        values.put(byte.class, (byte) 0);
        values.put(short.class, (short) 0);
        values.put(int.class, 0);
        values.put(long.class, 0L);
        values.put(float.class, 0F);
        values.put(double.class, 0D);
        values.put(char.class, '\0');
        DEFAULT_TYPE_VALUES = Collections.unmodifiableMap(values);

        PRIMITIVE_WRAPPER_TYPE_MAP.put(Boolean.class, Boolean.TYPE);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Byte.class, Byte.TYPE);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Character.class, Character.TYPE);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Double.class, Double.TYPE);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Float.class, Float.TYPE);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Integer.class, Integer.TYPE);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Long.class, Long.TYPE);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Short.class, Short.TYPE);
        PRIMITIVE_WRAPPER_TYPE_MAP.put(Void.class, Void.TYPE);

        for (Map.Entry<Class<?>, Class<?>> entry : PRIMITIVE_WRAPPER_TYPE_MAP.entrySet()) {
            PRIMITIVE_TYPE_TO_WRAPPER_MAP.put(entry.getValue(), entry.getKey());
        }
    }

    private ClassUtil() {
    }

    /**
     * Return an instance with the given class and constructor arguments.
     *
     * @param baseClass The Class used to create instances.
     * @param constructorArguments Parameters to used in the constructor.
     * @param <R> The return type of instance.
     * @return An instance with the given class and constructor arguments.
     *
     */
    @SuppressWarnings("unchecked")
    public static <R> R instantiateClass(Class<?> baseClass, Object... constructorArguments) {
        Optional<Constructor<?>> constructor = findConstructor(baseClass, constructorArguments);

        return constructor.map(it -> (R) instantiateClass(it, constructorArguments))
            .orElseThrow(() -> new IllegalStateException(String.format(
                "No suitable constructor found on %s to match the given arguments: %s. Make sure you implement a constructor taking these",
                baseClass, Arrays.stream(constructorArguments).map(Object::getClass).map(u -> getQualifiedName(u))
                    .collect(Collectors.joining(", ")))));
    }

    /**
     * Get instance with provided className.
     *
     * @param className The canonical name of the underlying class.
     * @param assignableClass Used to check whether it's assignable.
     * @param <T> The return value type.
     * @param <P> The type of assignableClass.
     * @return An instance of the provided className.
     * @throws AzureException Will throw AzureException if the provided className is not a assignableClass.<br>
     *                        Will throw AzureException if the provided className can't be found.
     */
    @SuppressWarnings("unchecked")
    public static <T, P> Class<T> getClass(String className, Class<P> assignableClass) {
        if (className != null && !className.isEmpty()) {
            try {
                Class<?> clazz = Class.forName(className);
                if (!assignableClass.isAssignableFrom(clazz)) {
                    throw new AzureException("Provided class [" + className + "] is not a [ " + assignableClass.getSimpleName() + "]");
                }
                return (Class<T>) clazz;
            } catch (ClassNotFoundException e) {
                throw new AzureException("The provided class [" + className + "] can't be found", e);
            }
        }
        return null;
    }

    private static Optional<Constructor<?>> findConstructor(Class<?> type, Object... constructorArguments) {

        notNull(type, "Target type must not be null");
        notNull(constructorArguments, "Constructor arguments must not be null");

        return Arrays.stream(type.getDeclaredConstructors())
            .filter(constructor -> argumentsMatch(constructor.getParameterTypes(), constructorArguments))
            .findFirst();
    }

    private static boolean argumentsMatch(Class<?>[] parameterTypes, Object[] arguments) {

        if (parameterTypes.length != arguments.length) {
            return false;
        }

        int index = 0;

        for (Class<?> argumentType : parameterTypes) {

            Object argument = arguments[index];

            // Reject nulls for primitives
            if (argumentType.isPrimitive() && argument == null) {
                return false;
            }

            // Type check if argument is not null
            if (argument != null && !isAssignableValue(argumentType, argument)) {
                return false;
            }

            index++;
        }

        return true;
    }

    private static <T> T instantiateClass(Constructor<T> ctor, Object... args) {
        notNull(ctor, "Constructor must not be null");
        try {
            makeAccessible(ctor);
            Class<?>[] parameterTypes = ctor.getParameterTypes();
            isTrue(args.length <= parameterTypes.length, "Can't specify more arguments than constructor parameters");
            Object[] argsWithDefaultValues = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                if (args[i] == null) {
                    Class<?> parameterType = parameterTypes[i];
                    argsWithDefaultValues[i] = (parameterType.isPrimitive() ? DEFAULT_TYPE_VALUES.get(parameterType) : null);
                } else {
                    argsWithDefaultValues[i] = args[i];
                }
            }
            return ctor.newInstance(argsWithDefaultValues);
        } catch (InstantiationException ex) {
            throw new AzureInstantiateException("Failed to instantiate [" + ctor.getDeclaringClass().getName() + "]: "
                + "Is it an abstract class?", ex);
        } catch (IllegalAccessException ex) {
            throw new AzureInstantiateException("Failed to instantiate [" + ctor.getDeclaringClass().getName() + "]: "
                + "Is the constructor accessible?", ex);
        } catch (IllegalArgumentException ex) {
            throw new AzureInstantiateException("Failed to instantiate [" + ctor.getDeclaringClass().getName() + "]: "
                + "Illegal arguments for constructor", ex);
        } catch (InvocationTargetException ex) {
            throw new AzureInstantiateException("Failed to instantiate [" + ctor.getDeclaringClass().getName() + "]: "
                + "Constructor threw exception", ex);
        }
    }

    private static void makeAccessible(Constructor<?> ctor) {
        if ((!Modifier.isPublic(ctor.getModifiers())
            || !Modifier.isPublic(ctor.getDeclaringClass().getModifiers())) && !ctor.isAccessible()) {
            ctor.setAccessible(true);
        }
    }

    private static void notNull(Object object, String message) {
        if (object == null) {
            throw new IllegalArgumentException(message);
        }
    }

    private static void isTrue(boolean expression, String message) {
        if (!expression) {
            throw new IllegalArgumentException(message);
        }
    }

    private static String getQualifiedName(Class<?> clazz) {
        notNull(clazz, "Class must not be null");
        return clazz.getTypeName();
    }

    private static boolean isAssignableValue(Class<?> type, Object value) {
        notNull(type, "Type must not be null");
        return value != null ? isAssignable(type, value.getClass()) : !type.isPrimitive();
    }

    private static boolean isAssignable(Class<?> lhsType, Class<?> rhsType) {
        Objects.requireNonNull(lhsType, "Left-hand side type must not be null");
        Objects.requireNonNull(rhsType, "Right-hand side type must not be null");
        if (lhsType.isAssignableFrom(rhsType)) {
            return true;
        } else {
            if (lhsType.isPrimitive()) {
                Class<?> resolvedWrapper = PRIMITIVE_WRAPPER_TYPE_MAP.get(rhsType);
                return lhsType == resolvedWrapper;
            } else {
                Class<?> resolvedWrapper = PRIMITIVE_TYPE_TO_WRAPPER_MAP.get(rhsType);
                return resolvedWrapper != null && lhsType.isAssignableFrom(resolvedWrapper);
            }
        }
    }
}
