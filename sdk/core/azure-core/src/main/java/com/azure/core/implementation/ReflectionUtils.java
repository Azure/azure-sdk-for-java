// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;

/**
 * Utility methods that aid in performing reflective operations.
 */
@SuppressWarnings("deprecation")
final class ReflectionUtils implements ReflectionUtilsApi {
    private static final MethodHandle PRIVATE_LOOKUP_IN;

    static {
        try {
            Constructor<MethodHandles.Lookup> privateLookupInConstructor =
                MethodHandles.Lookup.class.getDeclaredConstructor(Class.class);

            if (!privateLookupInConstructor.isAccessible()) {
                privateLookupInConstructor.setAccessible(true);
            }

            PRIVATE_LOOKUP_IN = MethodHandles.lookup().unreflectConstructor(privateLookupInConstructor);
        } catch (ReflectiveOperationException ex) {
            throw new RuntimeException("Unable to use private lookup in constructor.", ex);
        }
    }

    /**
     * Gets the {@link MethodHandles.Lookup} to use when performing reflective operations.
     * <p>
     * If Java 8 is being used this will always return {@link MethodHandles.Lookup#publicLookup()} as Java 8 doesn't
     * have module boundaries that will prevent reflective access to the {@code targetClass}.
     * <p>
     * If Java 9 or above is being used this will return a {@link MethodHandles.Lookup} based on whether the module
     * containing the {@code targetClass} exports the package containing the class. Otherwise, the {@link
     * MethodHandles.Lookup} associated to {@code com.azure.core} will attempt to read the module containing {@code
     * targetClass}.
     *
     * @param targetClass The {@link Class} that will need to be reflectively accessed.
     * @return The {@link MethodHandles.Lookup} that will allow {@code com.azure.core} to access the {@code targetClass}
     * reflectively.
     * @throws Exception If the underlying reflective calls throw an exception.
     */
    public MethodHandles.Lookup getLookupToUse(Class<?> targetClass) throws Exception {
        try {
            return (MethodHandles.Lookup) PRIVATE_LOOKUP_IN.invoke(targetClass);
        } catch (Throwable throwable) {
            // invoke(Class<?) throws a Throwable as the underlying method being called through reflection can throw
            // anything, but the constructor being called is owned by the Java SDKs which won't throw Throwable. So,
            // only Error needs to be inspected and handled specially, otherwise it can be assumed the Throwable is
            // a type of Exception which can be thrown based on this method having Exception checked.
            if (throwable instanceof Error) {
                throw (Error) throwable;
            } else {
                throw (Exception) throwable;
            }
        }
    }

    public int getJavaImplementationMajorVersion() {
        return 8;
    }

    ReflectionUtils() {
    }
}
