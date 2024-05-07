// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.serializer.json.jackson.implementation;

import com.azure.core.implementation.ReflectiveInvoker;
import com.azure.core.implementation.ReflectionUtils;
import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;

import java.lang.reflect.Field;
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
    private static final Map<Field, ReflectiveInvoker> FIELD_TO_SETTER_INVOKER_CACHE = new ConcurrentHashMap<>();

    // Dummy constant that indicates no setter was found for the Field.
    private static final ReflectiveInvoker NO_SETTER_REFLECTIVE_INVOKER = ReflectionUtils.createNoOpInvoker();

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

    @SuppressWarnings({ "deprecation", "removal" })
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

        ReflectiveInvoker setterReflectiveInvoker
            = getFromCache(declaringField, clazz, clazzSimpleName, fieldName, logger);

        if (setterReflectiveInvoker == NO_SETTER_REFLECTIVE_INVOKER) {
            return false;
        }

        try {
            setterReflectiveInvoker.invokeWithArguments(deserializedHeaders, values);
            logger.log(LogLevel.VERBOSE,
                () -> "Set header collection " + fieldName + " on class " + clazzSimpleName + " using reflection.");

            return true;
        } catch (Exception ex) {
            logger.log(LogLevel.VERBOSE, () -> "Failed to set header " + fieldName + " collection on class "
                + clazzSimpleName + " using reflection.", ex);
            return false;
        }
    }

    private static String getPotentialSetterName(String fieldName) {
        return "set" + fieldName.substring(0, 1).toUpperCase(Locale.ROOT) + fieldName.substring(1);
    }

    private static ReflectiveInvoker getFromCache(Field key, Class<?> clazz, String clazzSimpleName, String fieldName,
        ClientLogger logger) {
        if (FIELD_TO_SETTER_INVOKER_CACHE.size() >= CACHE_SIZE_LIMIT) {
            FIELD_TO_SETTER_INVOKER_CACHE.clear();
        }

        return FIELD_TO_SETTER_INVOKER_CACHE.computeIfAbsent(key, field -> {
            String setterName = getPotentialSetterName(fieldName);

            try {
                ReflectiveInvoker reflectiveInvoker
                    = ReflectionUtils.getMethodInvoker(clazz, clazz.getDeclaredMethod(setterName, Map.class));

                logger.log(LogLevel.VERBOSE,
                    () -> "Using invoker for setter " + setterName + " on class " + clazzSimpleName + ".");

                return reflectiveInvoker;
            } catch (Exception ex) {
                logger.log(LogLevel.VERBOSE,
                    () -> "Failed to retrieve invoker for setter " + setterName + " on class " + clazzSimpleName
                        + ". Will attempt to make field accessible. Please consider adding public setter.",
                    ex);
            }

            // In a previous implementation compute returned null here in an attempt to indicate that there is no setter
            // for the field. Unfortunately, null isn't a valid indicator to computeIfAbsent that a computation has been
            // performed and this cache would never effectively be a cache as compute would always be performed when
            // there was no setter for the field.
            //
            // Now the implementation returns a dummy constant when there is no setter for the field. This now results
            // in this case properly inserting into the cache and only running when a new type is seen or the cache is
            // cleared due to reaching capacity.
            return NO_SETTER_REFLECTIVE_INVOKER;
        });
    }
}
