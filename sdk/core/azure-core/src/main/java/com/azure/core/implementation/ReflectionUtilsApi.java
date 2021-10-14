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
     * Gets a {@link MethodHandles.Lookup} capable of doing a private lookup in the {@code targetClass}.
     *
     * @param targetClass The class being targeted to retrieve a {@link MethodHandles.Lookup} that has access to private
     * classes and members.
     * @param lookup The {@link MethodHandles.Lookup} for the caller.
     * @return A {@link MethodHandles.Lookup} with access to private classes and members on the {@code targetClass}.
     * @throws Throwable If an error occurs while attempting to create the lookup.
     */
    MethodHandles.Lookup privateLookupIn(Class<?> targetClass, MethodHandles.Lookup lookup) throws Throwable;

    /**
     * Gets the Java implementation major version.
     *
     * @return The Java implementation major version.
     */
    default int getJavaImplementationMajorVersion() {
        return 8;
    }
}
