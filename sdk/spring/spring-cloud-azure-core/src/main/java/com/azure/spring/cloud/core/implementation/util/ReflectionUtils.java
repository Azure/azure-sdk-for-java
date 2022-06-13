// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.implementation.util;

import java.lang.reflect.Field;

public final class ReflectionUtils {

    public static Object getField(Class<?> clazz, String fieldName, Object target) {
        Field field = org.springframework.util.ReflectionUtils.findField(clazz, fieldName);
        field.setAccessible(true);
        return org.springframework.util.ReflectionUtils.getField(field, target);
    }
}
