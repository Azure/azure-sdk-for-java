/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.common.implementation;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.azure.common.annotations.SkipParentValidation;
import com.azure.common.implementation.util.TypeUtil;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.time.Duration;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;

/**
 * Validates user provided parameters are not null if they are required.
 */
public final class Validator {
    /**
     * Private Ctr.
     */
    private Validator() { }

    /**
     * Validates a user provided required parameter to be not null.
     *
     * An {@link IllegalArgumentException} is thrown if a property fails the validation.
     *
     * @param parameter the parameter to validate
     * @throws IllegalArgumentException thrown when the Validator determines the argument is invalid
     */
    public static void validate(Object parameter) {
        // Validation of top level payload is done outside
        if (parameter == null) {
            return;
        }

        Class<?> type = parameter.getClass();
        if (type == Double.class
                || type == Float.class
                || type == Long.class
                || type == Integer.class
                || type == Short.class
                || type == Character.class
                || type == Byte.class
                || type == Boolean.class) {
            type = wrapperToPrimitive(type);
        }
        if (type.isPrimitive()
                || type.isEnum()
                || type.isAssignableFrom(Class.class)
                || type.isAssignableFrom(LocalDate.class)
                || type.isAssignableFrom(OffsetDateTime.class)
                || type.isAssignableFrom(String.class)
                || type.isAssignableFrom(DateTimeRfc1123.class)
                || type.isAssignableFrom(Duration.class)) {
            return;
        }

        Annotation skipParentAnnotation = type.getAnnotation(SkipParentValidation.class);
        //
        if (skipParentAnnotation == null) {
            for (Class<?> c : TypeUtil.getAllClasses(type)) {
                validateClass(c, parameter);
            }
        } else {
            validateClass(type, parameter);
        }
    }

    private static Class<?> wrapperToPrimitive(Class<?> clazz) {
        if (!clazz.isPrimitive()) {
            return clazz;
        }

        if (clazz == Integer.class) {
            return Integer.TYPE;
        } else if (clazz == Long.class) {
            return Long.TYPE;
        } else if (clazz == Boolean.class) {
            return Boolean.TYPE;
        } else if (clazz == Byte.class) {
            return Byte.TYPE;
        } else if (clazz == Character.class) {
            return Character.TYPE;
        } else if (clazz == Float.class) {
            return Float.TYPE;
        } else if (clazz == Double.class) {
            return Double.TYPE;
        } else if (clazz == Short.class) {
            return Short.TYPE;
        } else if (clazz == Void.class) {
            return Void.TYPE;
        }

        return clazz;
    }

    private static void validateClass(Class<?> c, Object parameter) {
        // Ignore checks for Object type.
        if (c.isAssignableFrom(Object.class)) {
            return;
        }
        //
        for (Field field : c.getDeclaredFields()) {
            field.setAccessible(true);
            int mod = field.getModifiers();
            // Skip static fields since we don't have any, skip final fields since users can't modify them
            if (Modifier.isFinal(mod) || Modifier.isStatic(mod)) {
                continue;
            }
            JsonProperty annotation = field.getAnnotation(JsonProperty.class);
            // Skip read-only properties (WRITE_ONLY)
            if (annotation != null && annotation.access().equals(JsonProperty.Access.WRITE_ONLY)) {
                continue;
            }
            Object property;
            try {
                property = field.get(parameter);
            } catch (IllegalAccessException e) {
                throw new IllegalArgumentException(e.getMessage(), e);
            }
            if (property == null) {
                if (annotation != null && annotation.required()) {
                    throw new IllegalArgumentException(field.getName() + " is required and cannot be null.");
                }
            } else {
                try {
                    Class<?> propertyType = property.getClass();
                    if (List.class.isAssignableFrom(propertyType)) {
                        List<?> items = (List<?>) property;
                        for (Object item : items) {
                            Validator.validate(item);
                        }
                    }
                    else if (Map.class.isAssignableFrom(propertyType)) {
                        Map<?, ?> entries = (Map<?, ?>) property;
                        for (Map.Entry<?, ?> entry : entries.entrySet()) {
                            Validator.validate(entry.getKey());
                            Validator.validate(entry.getValue());
                        }
                    }
                    else if (parameter.getClass() != propertyType) {
                        Validator.validate(property);
                    }
                } catch (IllegalArgumentException ex) {
                    if (ex.getCause() == null) {
                        // Build property chain
                        throw new IllegalArgumentException(field.getName() + "." + ex.getMessage());
                    } else {
                        throw ex;
                    }
                }
            }
        }
    }
}
