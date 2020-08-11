// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty.implementation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;

/**
 * Helper methods for {@link ReadTimeoutHandlerTests}, {@link ResponseTimeoutHandlerTests}, and
 * {@link WriteTimeoutHandlerTests}.
 */
public class TimeoutTestHelpers {
    public static Method getInvokableMethod(Object obj, String methodName, Class<?>... parameters) throws Exception {
        Method method = obj.getClass().getDeclaredMethod(methodName, parameters);

        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            method.setAccessible(true);
            return null;
        });

        return method;
    }

    public static <T> T getFieldValue(Object obj, String fieldName, Class<T> type)
        throws Exception {
        Field field = obj.getClass().getDeclaredField(fieldName);

        AccessController.doPrivileged((PrivilegedAction<Object>) () -> {
            field.setAccessible(true);
            return null;
        });

        return type.cast(field.get(obj));
    }
}
