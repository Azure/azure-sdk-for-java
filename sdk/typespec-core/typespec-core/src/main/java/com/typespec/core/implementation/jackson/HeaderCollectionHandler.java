// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.typespec.core.implementation.jackson;

import com.typespec.core.implementation.ReflectionUtils;
import com.typespec.core.util.logging.ClientLogger;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.PrivilegedAction;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Internal helper class that helps manage converting headers into their header collection.
 */
final class HeaderCollectionHandler {
    private static final int CACHE_SIZE_LIMIT = 10000;
    private static final Map<Field, MethodHandle> FIELD_TO_SETTER_CACHE = new ConcurrentHashMap<>();

    // Dummy constant that indicates no setter was found for the Field.
    private static final MethodHandle NO_SETTER_HANDLE = MethodHandles.identity(HeaderCollectionHandler.class);

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

    @SuppressWarnings({"deprecation", "removal"})
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
                java.security.AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
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
                java.security.AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
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

        MethodHandle setterHandler = getFromCache(declaringField, clazz, clazzSimpleName, fieldName, logger);

        if (setterHandler == NO_SETTER_HANDLE) {
            return false;
        }

        try {
            setterHandler.invokeWithArguments(deserializedHeaders, values);
            logger.verbose("Set header collection {} on class {} using MethodHandle.", fieldName, clazzSimpleName);

            return true;
        } catch (Throwable ex) {
            if (ex instanceof Error) {
                throw (Error) ex;
            }

            logger.verbose("Failed to set header {} collection on class {} using MethodHandle.", fieldName,
                clazzSimpleName, ex);
            return false;
        }
    }

    private static String getPotentialSetterName(String fieldName) {
        return "set" + fieldName.substring(0, 1).toUpperCase(Locale.ROOT) + fieldName.substring(1);
    }

    private static MethodHandle getFromCache(Field key, Class<?> clazz, String clazzSimpleName,
        String fieldName, ClientLogger logger) {
        if (FIELD_TO_SETTER_CACHE.size() >= CACHE_SIZE_LIMIT) {
            FIELD_TO_SETTER_CACHE.clear();
        }

        return FIELD_TO_SETTER_CACHE.computeIfAbsent(key, field -> {
            MethodHandles.Lookup lookupToUse;
            try {
                lookupToUse = ReflectionUtils.getLookupToUse(clazz);
            } catch (Exception ex) {
                logger.verbose("Failed to retrieve MethodHandles.Lookup for field {}. Will attempt to make field accessible.", field, ex);

                // In a previous implementation compute returned null here in an attempt to indicate that there is no
                // setter for the field. Unfortunately, null isn't a valid indicator to computeIfAbsent that a
                // computation has been performed and this cache would never effectively be a cache as compute would
                // always be performed when there was no setter for the field.
                //
                // Now the implementation returns a dummy constant when there is no setter for the field. This now
                // results in this case properly inserting into the cache and only running when a new type is seen or
                // the cache is cleared due to reaching capacity.
                return NO_SETTER_HANDLE;
            }

            String setterName = getPotentialSetterName(fieldName);

            try {
                MethodHandle handle = lookupToUse.findVirtual(clazz, setterName,
                    MethodType.methodType(clazz, Map.class));

                logger.verbose("Using MethodHandle for setter {} on class {}.", setterName, clazzSimpleName);

                return handle;
            } catch (ReflectiveOperationException ex) {
                logger.verbose("Failed to retrieve MethodHandle for setter {} on class {}. "
                    + "Will attempt to make field accessible. "
                    + "Please consider adding public setter.", setterName,
                    clazzSimpleName, ex);
            }

            try {
                Method setterMethod = clazz.getDeclaredMethod(setterName, Map.class);
                MethodHandle handle = lookupToUse.unreflect(setterMethod);

                logger.verbose("Using unreflected MethodHandle for setter {} on class {}.", setterName,
                    clazzSimpleName);

                return handle;
            } catch (ReflectiveOperationException ex) {
                logger.verbose("Failed to unreflect MethodHandle for setter {} on class {}."
                        + "Will attempt to make field accessible. "
                        + "Please consider adding public setter.", setterName, clazzSimpleName, ex);
            }

            // In a previous implementation compute returned null here in an attempt to indicate that there is no setter
            // for the field. Unfortunately, null isn't a valid indicator to computeIfAbsent that a computation has been
            // performed and this cache would never effectively be a cache as compute would always be performed when
            // there was no setter for the field.
            //
            // Now the implementation returns a dummy constant when there is no setter for the field. This now results
            // in this case properly inserting into the cache and only running when a new type is seen or the cache is
            // cleared due to reaching capacity.
            return NO_SETTER_HANDLE;
        });
    }
}
