// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import java.lang.invoke.MethodHandles;

/**
 * Utility methods that aid in performing reflective operations.
 */
public final class ReflectionUtils {
    // This lookup is specific to the com.azure.core module, specifically this class.
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    // Convenience pointer to the com.azure.core module.
    private static final Module CORE_MODULE = ReflectionUtils.class.getModule();

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
    public static MethodHandles.Lookup getLookupToUse(Class<?> targetClass) throws Throwable {
        Module responseModule = targetClass.getModule();

        /*
         * First check if the response class's module is exported to all unnamed modules. If it is we will use
         * MethodHandles.publicLookup() which is meant for creating MethodHandle instances for publicly accessible
         * classes.
         */
        if (responseModule.isExported("")) {
            return MethodHandles.publicLookup();
        }

        /*
         * Otherwise, we use the MethodHandles.Lookup which is associated to this (com.azure.core) module, and
         * more specifically, is tied to this class (ResponseConstructorsCache). But, in order to use this
         * lookup we need to ensure that the com.azure.core module reads the response class's module as the
         * lookup won't have permissions necessary to create the MethodHandle instance without it.
         */
        if (!CORE_MODULE.canRead(responseModule)) {
            CORE_MODULE.addReads(responseModule);
        }

        return LOOKUP;
    }

    private ReflectionUtils() {
    }
}
