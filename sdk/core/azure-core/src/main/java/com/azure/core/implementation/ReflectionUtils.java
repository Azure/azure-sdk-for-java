// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;

/**
 * Utility methods that aid in performing reflective operations.
 */
@SuppressWarnings("deprecation")
final class ReflectionUtils implements ReflectionUtilsApi {
    private static final Constructor<MethodHandles.Lookup> PRIVATE_LOOKUP_IN_CTOR;

    static {
        try {
            PRIVATE_LOOKUP_IN_CTOR = MethodHandles.Lookup.class.getDeclaredConstructor(Class.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("Failed to access the private constructor in MethodHandles.Lookup.", e);
        }

        if (!PRIVATE_LOOKUP_IN_CTOR.isAccessible()) {
            PRIVATE_LOOKUP_IN_CTOR.setAccessible(true);
        }
    }

    @Override
    public MethodHandles.Lookup getLookupToUse(Class<?> targetClass) throws Throwable {
        // Always return the public lookup in Java 8.
        return MethodHandles.publicLookup();
    }

    @Override
    public MethodHandles.Lookup privateLookupIn(Class<?> targetClass, MethodHandles.Lookup lookup) throws Throwable {
        return PRIVATE_LOOKUP_IN_CTOR.newInstance(targetClass);
    }

    public int getJavaImplementationMajorVersion() {
        return 8;
    }

    ReflectionUtils() {
    }
}
