// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import java.lang.invoke.MethodHandles;

/**
 * Utility methods that aid in performing reflective operations.
 */
final class ReflectionUtils implements ReflectionUtilsApi {

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
     * @throws Throwable If the underlying reflective calls throw an exception.
     */
    public MethodHandles.Lookup getLookupToUse(Class<?> targetClass) throws Throwable {
        return MethodHandles.publicLookup();
    }

    public int getJavaImplementationMajorVersion() {
        return 8;
    }

    ReflectionUtils() {
    }
}
