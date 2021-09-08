// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation.jackson;

import com.azure.core.implementation.ReflectionUtils;
import com.azure.core.util.logging.ClientLogger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

/*
 * Internal helper class that helps manage converting headers into their header collection.
 */
final class HeaderCollectionHandler {
    private static final int CACHE_SIZE_LIMIT = 10000;
    private static final Map<Field, MethodHandle> FIELD_TO_SETTER_CACHE = new ConcurrentHashMap<>();
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

        /*
         * Otherwise, fallback to setting the field directly.
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
        final Class<?> clazz = deserializedHeaders.getClass();
        final String clazzSimpleName = clazz.getSimpleName();
        final String fieldName = declaringField.getName();

        MethodHandle setterHandler = getFromCache(declaringField, field -> {
            MethodHandles.Lookup lookupToUse;
            try {
                lookupToUse = ReflectionUtils.getLookupToUse(clazz);
            } catch (Throwable t) {
                logger.verbose("Failed to retrieve MethodHandles.Lookup for field {}.", field, t);
                return null;
            }

            String setterName = getPotentialSetterName(fieldName);

            try {
                MethodHandle handle = lookupToUse.findVirtual(clazz, setterName,
                    MethodType.methodType(clazz, Map.class));

                logger.verbose("Using MethodHandle for setter {} on class {}.", setterName, clazzSimpleName);

                return handle;
            } catch (ReflectiveOperationException ex) {
                logger.verbose("Failed to retrieve MethodHandle for setter {} on class {}.", setterName,
                    clazzSimpleName, ex);
            }

            try {
                Method setterMethod = deserializedHeaders.getClass()
                    .getDeclaredMethod(setterName, Map.class);
                MethodHandle handle = lookupToUse.unreflect(setterMethod);

                logger.verbose("Using unreflected MethodHandle for setter {} on class {}.", setterName,
                    clazzSimpleName);

                return handle;
            } catch (ReflectiveOperationException ex) {
                logger.verbose("Failed to unreflect MethodHandle for setter {} on class {}.", setterName,
                    clazzSimpleName, ex);
            }

            return null;
        });

        if (setterHandler == null) {
            return false;
        }

        try {
            setterHandler.invokeWithArguments(deserializedHeaders, values);
            logger.verbose("Set header collection {} on class {} using MethodHandle.", fieldName, clazzSimpleName);

            return true;
        } catch (Throwable ex) {
            logger.verbose("Failed to set header {} collection on class {} using MethodHandle.", fieldName,
                clazzSimpleName, ex);
            return false;
        }
    }

    private static String getPotentialSetterName(String fieldName) {
        return "set" + fieldName.substring(0, 1).toUpperCase(Locale.ROOT) + fieldName.substring(1);
    }

    private static MethodHandle getFromCache(Field key, Function<Field, MethodHandle> compute) {
        if (FIELD_TO_SETTER_CACHE.size() >= CACHE_SIZE_LIMIT) {
            FIELD_TO_SETTER_CACHE.clear();
        }

        return FIELD_TO_SETTER_CACHE.computeIfAbsent(key, compute);
    }
}
