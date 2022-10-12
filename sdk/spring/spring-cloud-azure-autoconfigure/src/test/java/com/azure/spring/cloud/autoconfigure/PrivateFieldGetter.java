package com.azure.spring.cloud.autoconfigure;

import java.lang.reflect.Field;

public class PrivateFieldGetter {

    public static Object getField(Class<?> clazz, String fieldName, Object object) {
        try {
            Field field = clazz.getDeclaredField(fieldName);
            field.setAccessible(true);
            return field.get(object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            throw new IllegalArgumentException(e);
        }
    }
}
