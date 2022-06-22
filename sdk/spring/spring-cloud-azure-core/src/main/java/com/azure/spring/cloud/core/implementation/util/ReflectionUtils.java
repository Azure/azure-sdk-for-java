// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.util;

import java.lang.reflect.Field;

public final class ReflectionUtils {

    public static final String FIELD_NOT_FOUND_FORMAT = "Field %s can't found on class %s";
    public static Object getField(Class<?> clazz, String fieldName, Object target) {
        Field field = org.springframework.util.ReflectionUtils.findField(clazz, fieldName);
        if (field == null) {
            throw new IllegalArgumentException(String.format(FIELD_NOT_FOUND_FORMAT, fieldName, clazz));
        }
        org.springframework.util.ReflectionUtils.makeAccessible(field);
        return org.springframework.util.ReflectionUtils.getField(field, target);
    }
}
