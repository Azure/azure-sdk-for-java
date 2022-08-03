// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.identity.impl.utils;

import com.azure.core.exception.AzureException;
import com.azure.spring.cloud.service.implementation.identity.api.credential.TokenCredentialProvider;
import org.springframework.util.Assert;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.IdentityHashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * {@code java.lang.Class} utility methods.
 * Mainly for internal use.
 */
public class ClassUtil {

    private static final Map<Class<?>, Object> DEFAULT_TYPE_VALUES;
    @SuppressWarnings("unchecked")
    private static final Map<Class<?>, Class<?>> primitiveWrapperTypeMap = new IdentityHashMap(9);
    @SuppressWarnings("unchecked")
    private static final Map<Class<?>, Class<?>> primitiveTypeToWrapperMap = new IdentityHashMap(9);

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

        primitiveWrapperTypeMap.put(Boolean.class, Boolean.TYPE);
        primitiveWrapperTypeMap.put(Byte.class, Byte.TYPE);
        primitiveWrapperTypeMap.put(Character.class, Character.TYPE);
        primitiveWrapperTypeMap.put(Double.class, Double.TYPE);
        primitiveWrapperTypeMap.put(Float.class, Float.TYPE);
        primitiveWrapperTypeMap.put(Integer.class, Integer.TYPE);
        primitiveWrapperTypeMap.put(Long.class, Long.TYPE);
        primitiveWrapperTypeMap.put(Short.class, Short.TYPE);
        primitiveWrapperTypeMap.put(Void.class, Void.TYPE);
        @SuppressWarnings("unchecked")
        Iterator iterator = primitiveWrapperTypeMap.entrySet().iterator();


        while(iterator.hasNext()) {
            Map.Entry<Class<?>, Class<?>> entry = (Map.Entry)iterator.next();
            primitiveTypeToWrapperMap.put(entry.getValue(), entry.getKey());
        }
    }

    private ClassUtil() {
    }

    @SuppressWarnings("unchecked")
    public static <R> R instantiateClass(Class<?> baseClass, Object... constructorArguments) {
        Optional<Constructor<?>> constructor = findConstructor(baseClass, constructorArguments);

        return constructor.map(it -> (R) instantiateClass(it, constructorArguments))
            .orElseThrow(() -> new IllegalStateException(String.format(
                "No suitable constructor found on %s to match the given arguments: %s. Make sure you implement a constructor taking these",
                baseClass, Arrays.stream(constructorArguments).map(Object::getClass).map(u -> getQualifiedName(u))
                    .collect(Collectors.joining(", ")))));
    }

    @SuppressWarnings("unchecked")
    public static <T> Class<T> getClass(String className, Class<TokenCredentialProvider> assignableClass) {
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

        return Arrays.stream(type.getDeclaredConstructors())//
            .filter(constructor -> argumentsMatch(constructor.getParameterTypes(), constructorArguments))//
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

    private static  <T> T instantiateClass(Constructor<T> ctor, Object... args)  {
        notNull(ctor, "Constructor must not be null");
        try {
            makeAccessible(ctor);
            Class<?>[] parameterTypes = ctor.getParameterTypes();
            isTrue(args.length <= parameterTypes.length, "Can't specify more arguments than constructor parameters");
            Object[] argsWithDefaultValues = new Object[args.length];
            for (int i = 0 ; i < args.length; i++) {
                if (args[i] == null) {
                    Class<?> parameterType = parameterTypes[i];
                    argsWithDefaultValues[i] = (parameterType.isPrimitive() ? DEFAULT_TYPE_VALUES.get(parameterType) : null);
                }
                else {
                    argsWithDefaultValues[i] = args[i];
                }
            }
            return ctor.newInstance(argsWithDefaultValues);
        }
        catch (InstantiationException ex) {
            throw new AzureException("Failed to instantiate [" + ctor.getDeclaringClass().getName() + "]: " + "Is it an abstract class?", ex);
        }
        catch (IllegalAccessException ex) {
            throw new AzureException("Failed to instantiate [" + ctor.getDeclaringClass().getName() + "]: " + "Is the constructor accessible?", ex);
        }
        catch (IllegalArgumentException ex) {
            throw new AzureException("Failed to instantiate [" + ctor.getDeclaringClass().getName() + "]: " + "Illegal arguments for constructor", ex);
        }
        catch (InvocationTargetException ex) {
            throw new AzureException("Failed to instantiate [" + ctor.getDeclaringClass().getName() + "]: " + "Constructor threw exception", ex);
        }
    }

    private static void makeAccessible(Constructor<?> ctor) {
        if ((!Modifier.isPublic(ctor.getModifiers()) || !Modifier.isPublic(ctor.getDeclaringClass().getModifiers())) && !ctor.isAccessible()) {
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
        Assert.notNull(lhsType, "Left-hand side type must not be null");
        Assert.notNull(rhsType, "Right-hand side type must not be null");
        if (lhsType.isAssignableFrom(rhsType)) {
            return true;
        } else {
            Class resolvedWrapper;
            if (lhsType.isPrimitive()) {
                resolvedWrapper = primitiveWrapperTypeMap.get(rhsType);
                return lhsType == resolvedWrapper;
            } else {
                resolvedWrapper = primitiveTypeToWrapperMap.get(rhsType);
                return resolvedWrapper != null && lhsType.isAssignableFrom(resolvedWrapper);
            }
        }
    }
}
