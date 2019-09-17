package com.azure.core.implementation;

import com.azure.core.implementation.annotation.SkipParentValidation;
import com.azure.core.implementation.util.TypeUtil;
import com.fasterxml.jackson.annotation.JsonProperty;

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
                    } else if (Map.class.isAssignableFrom(propertyType)) {
                        Map<?, ?> entries = (Map<?, ?>) property;
                        for (Map.Entry<?, ?> entry : entries.entrySet()) {
                            Validator.validate(entry.getKey());
                            Validator.validate(entry.getValue());
                        }
                    } else if (parameter.getClass() != propertyType) {
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
