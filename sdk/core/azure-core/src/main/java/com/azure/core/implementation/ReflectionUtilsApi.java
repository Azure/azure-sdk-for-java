// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import java.lang.invoke.MethodHandles;

/**
 * API for {@link ReflectionUtils}.
 */
public interface ReflectionUtilsApi {
    ReflectionUtilsApi INSTANCE = new ReflectionUtils();

    /**
     * Gets the {@link MethodHandles.Lookup} to use based on the target {@link Class}.
     *
     * @param targetClass The target {@link Class}.
     * @return The {@link MethodHandles.Lookup} to use.
     * @throws Throwable If an error occurs while attempting to find the lookup.
     */
    MethodHandles.Lookup getLookupToUse(Class<?> targetClass) throws Throwable;

    /**
     * Gets the Java implementation major version.
     *
     * @return The Java implementation major version.
     */
    default int getJavaImplementationMajorVersion() {
        return 8;
    }
}
