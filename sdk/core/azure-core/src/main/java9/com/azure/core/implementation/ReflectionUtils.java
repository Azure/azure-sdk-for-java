// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import java.lang.invoke.MethodHandles;
import java.security.PrivilegedExceptionAction;

/**
 * Utility methods that aid in performing reflective operations.
 */
final class ReflectionUtils implements ReflectionUtilsApi {
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
     * @throws Exception If the underlying reflective calls throw an exception.
     */
    public MethodHandles.Lookup getLookupToUse(Class<?> targetClass) throws Exception {
        Module responseModule = targetClass.getModule();

        // The unnamed module is opened unconditionally, have Core read it and use a private proxy lookup to enable all
        // lookup scenarios.
        if (!responseModule.isNamed()) {
            CORE_MODULE.addReads(responseModule);
            return performSafePrivateLookupIn(targetClass);
        }


        // If the response module is the Core module return the Core private lookup.
        if (responseModule == CORE_MODULE) {
            return LOOKUP;
        }

        // Next check if the target class module is opened either unconditionally or to Core's module. If so, also use
        // a private proxy lookup to enable all lookup scenarios.
        if (responseModule.isOpen(targetClass.getPackageName())
            || responseModule.isOpen(targetClass.getPackageName(), CORE_MODULE)) {
            CORE_MODULE.addReads(responseModule);
            return performSafePrivateLookupIn(targetClass);
        }

        // Otherwise, return the public lookup as there are no specialty ways to access the other module.
        return MethodHandles.publicLookup();
    }

    public int getJavaImplementationMajorVersion() {
        return 9;
    }

    @SuppressWarnings("removal")
    private static MethodHandles.Lookup performSafePrivateLookupIn(Class<?> targetClass) throws Exception {
        // MethodHandles::privateLookupIn() throws SecurityException if denied by the security manager
        if (System.getSecurityManager() == null) {
            return MethodHandles.privateLookupIn(targetClass, LOOKUP);
        } else {
            return java.security.AccessController.doPrivileged((PrivilegedExceptionAction<MethodHandles.Lookup>) () ->
                MethodHandles.privateLookupIn(targetClass, LOOKUP));
        }
    }

    ReflectionUtils() {
    }
}