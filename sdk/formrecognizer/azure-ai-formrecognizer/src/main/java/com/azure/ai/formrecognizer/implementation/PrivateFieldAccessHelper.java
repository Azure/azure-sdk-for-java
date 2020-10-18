// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.implementation;

import com.azure.core.util.logging.ClientLogger;

import java.lang.reflect.Field;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * The helper class for getter only models converters.
 */
public class PrivateFieldAccessHelper {
    private static final ClientLogger LOGGER = new ClientLogger(PrivateFieldAccessHelper.class);

    /**
     * Set value to the model private properties.
     *
     * @param obj The instance which sets the value to.
     * @param fieldName The fieldName to set.
     * @param value The value sets to the instance
     * @param <T> Generic type of models.
     */
    @SuppressWarnings("unchecked")
    public static <T> void set(T obj, String fieldName, Object value) {
        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            if (!field.isAccessible()) {
                AccessController.doPrivileged(new PrivilegedAction<T>() {
                    @Override
                    public T run() {
                        field.setAccessible(true);
                        return null;
                    }
                });
            }
            field.set(obj, value);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }

    /**
     * Get values out from private private.
     *
     * @param obj The instance which gets the value from.
     * @param fieldName The field name to get
     * @param outputClass The field property class.
     * @param <T> Generic type of models.
     * @param <I> The type of the field properties.
     * @return The values of instance property.
     */
    @SuppressWarnings("unchecked")
    public static <T, I> I get(T obj, String fieldName, Class<I> outputClass) {

        try {
            Field field = obj.getClass().getDeclaredField(fieldName);
            if (!field.isAccessible()) {
                AccessController.doPrivileged(new PrivilegedAction<T>() {
                    @Override
                    public T run() {
                        field.setAccessible(true);
                        return null;
                    }
                });
            }
            return (I) field.get(obj);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw LOGGER.logExceptionAsError(new RuntimeException(e));
        }
    }
}
