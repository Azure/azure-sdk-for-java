// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


package com.azure.spring.cloud.autoconfigure.user.agent.util;

import java.lang.reflect.Field;

public class UserAgentTestUtil {

    public static Object getPrivateFieldValue(Class<?> clazz, String field, Object object) {

        Field privateStringField;
        try {
            privateStringField = clazz.getDeclaredField(field);
            privateStringField.setAccessible(true);
            return privateStringField.get(object);
        } catch (NoSuchFieldException | IllegalAccessException e) {
            return null;
        }
    }
}
