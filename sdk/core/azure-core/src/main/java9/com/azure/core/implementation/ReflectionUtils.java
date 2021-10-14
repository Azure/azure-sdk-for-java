// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import java.lang.invoke.MethodHandles;

/**
 * Utility methods that aid in performing reflective operations.
 */
final class ReflectionUtils implements ReflectionUtilsApi {
    // This lookup is specific to the com.azure.core module, specifically this class.
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    // Convenience pointer to the com.azure.core module.
    private static final Module CORE_MODULE = ReflectionUtils.class.getModule();

    @Override
    public MethodHandles.Lookup getLookupToUse(Class<?> targetClass) throws Throwable {
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
         * Otherwise, we use the MethodHandles.Lookup which is associated to this (com.azure.core) module, and more
         * specifically, is tied to this class (ReflectionUtils). But, in order to use this lookup we need to ensure
         * that the com.azure.core module reads the response class's module as the lookup won't have permissions
         * necessary to create the MethodHandle instance without it.
         *
         * This logic is safe due to the fact that any SDK module calling into this code path will already need to open
         * to com.azure.core as it needs to perform other reflective operations on classes in the module. Adding the
         * com.azure.core reads is handling specifically required by MethodHandle.
         */
        if (!CORE_MODULE.canRead(responseModule)) {
            CORE_MODULE.addReads(responseModule);
        }

        return LOOKUP;
    }

    @Override
    public MethodHandles.Lookup privateLookupIn(Class<?> targetClass, MethodHandles.Lookup lookup) throws Throwable {
        return MethodHandles.privateLookupIn(targetClass, lookup);
    }

    public int getJavaImplementationMajorVersion() {
        return 9;
    }

    ReflectionUtils() {
    }
}
