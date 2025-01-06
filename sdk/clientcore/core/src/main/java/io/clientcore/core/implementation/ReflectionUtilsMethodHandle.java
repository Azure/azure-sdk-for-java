// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package io.clientcore.core.implementation;

import java.lang.invoke.MethodHandles;
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;

/**
 * Implementation for {@link ReflectionUtilsApi} using {@code java.lang.invoke} to handle reflectively invoking APIs.
 */
final class ReflectionUtilsMethodHandle implements ReflectionUtilsApi {
    private static final Module CORE_MODULE = ReflectionUtilsMethodHandle.class.getModule();
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    @Override
    public ReflectiveInvoker getMethodInvoker(Class<?> targetClass, Method method, boolean scopeToGenericCore)
        throws Exception {
        MethodHandles.Lookup lookup = getLookupToUse(targetClass, scopeToGenericCore);

        return new MethodHandleReflectiveInvoker(lookup.unreflect(method));
    }

    @Override
    public ReflectiveInvoker getConstructorInvoker(Class<?> targetClass, Constructor<?> constructor,
        boolean scopeToGenericCore) throws Exception {
        MethodHandles.Lookup lookup = getLookupToUse(targetClass, scopeToGenericCore);

        return new MethodHandleReflectiveInvoker(lookup.unreflectConstructor(constructor));
    }

    /**
     * Gets the {@link MethodHandles.Lookup} to use when performing reflective operations.
     * <p>
     * If Java 8 is being used this will always return {@link MethodHandles.Lookup#publicLookup()} as Java 8 doesn't
     * have module boundaries that will prevent reflective access to the {@code targetClass}.
     * <p>
     * If Java 9 or above is being used this will return a {@link MethodHandles.Lookup} based on whether the module
     * containing the {@code targetClass} exports the package containing the class. Otherwise, the
     * {@link MethodHandles.Lookup} associated to {@code io.clientcore.core} will attempt to read the module containing
     * {@code targetClass}.
     *
     * @param targetClass The {@link Class} that will need to be reflectively accessed.
     * @param scopeToGenericCore Whether to scope the {@link MethodHandles.Lookup} to {@code io.clientcore.core} if Java 9+
     * modules is being used.
     * @return The {@link MethodHandles.Lookup} that will allow {@code io.clientcore.core} to access the
     * {@code targetClass} reflectively.
     * @throws Exception If the underlying reflective calls throw an exception.
     */
    private static MethodHandles.Lookup getLookupToUse(Class<?> targetClass, boolean scopeToGenericCore)
        throws Exception {
        try {
            if (!scopeToGenericCore) {
                return MethodHandles.publicLookup();
            }

            Module responseModule = targetClass.getModule();

            // The unnamed module is opened unconditionally, have Core read it and use a private proxy lookup to
            // enable all lookup scenarios.
            if (!responseModule.isNamed()) {
                CORE_MODULE.addReads(responseModule);
                return performSafePrivateLookupIn(targetClass);
            }

            // If the response module is the Core module return the Core private lookup.
            if (responseModule == CORE_MODULE) {
                return LOOKUP;
            }

            // Next check if the target class module is opened either unconditionally or to Core's module. If so,
            // also use a private proxy lookup to enable all lookup scenarios.
            String packageName = targetClass.getPackage().getName();
            if (responseModule.isOpen(packageName) || responseModule.isOpen(packageName, CORE_MODULE)) {
                CORE_MODULE.addReads(responseModule);
                return performSafePrivateLookupIn(targetClass);
            }

            // Otherwise, return the public lookup as there are no specialty ways to access the other module.
            return MethodHandles.publicLookup();
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

    private static MethodHandles.Lookup performSafePrivateLookupIn(Class<?> targetClass) throws Throwable {
        return MethodHandles.privateLookupIn(targetClass, LOOKUP);
    }
}
