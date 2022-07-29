// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.core.util.logging.LogLevel;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.reflect.Constructor;
import java.security.PrivilegedExceptionAction;

/**
 * Utility methods that aid in performing reflective operations.
 */
@SuppressWarnings("deprecation")
public final class ReflectionUtils {
    private static final boolean MODULE_BASED;

    private static final MethodHandle CLASS_GET_MODULE;
    private static final MethodHandle MODULE_IS_NAMED;
    private static final MethodHandle MODULE_ADD_READS;
    private static final MethodHandle METHOD_HANDLES_PRIVATE_LOOKUP_IN;
    private static final MethodHandle MODULE_IS_OPEN_UNCONDITIONALLY;
    private static final MethodHandle MODULE_IS_OPEN_TO_OTHER_MODULE;

    private static final MethodHandles.Lookup LOOKUP;
    private static final Object CORE_MODULE;

    private static final MethodHandle JDK_INTERNAL_PRIVATE_LOOKUP_IN_CONSTRUCTOR;

    static {
        ClientLogger logger = new ClientLogger(ReflectionUtils.class);

        boolean moduleBased = false;
        MethodHandle classGetModule = null;
        MethodHandle moduleIsNamed = null;
        MethodHandle moduleAddReads = null;
        MethodHandle methodHandlesPrivateLookupIn = null;
        MethodHandle moduleIsOpenUnconditionally = null;
        MethodHandle moduleIsOpenToOtherModule = null;

        MethodHandles.Lookup lookup = MethodHandles.lookup();
        Object coreModule = null;

        MethodHandle jdkInternalPrivateLookupInConstructor = null;

        try {
            Class<?> moduleClass = Class.forName("java.lang.Module");
            classGetModule = lookup.unreflect(Class.class.getDeclaredMethod("getModule"));
            moduleIsNamed = lookup.unreflect(moduleClass.getDeclaredMethod("isNamed"));
            moduleAddReads = lookup.unreflect(moduleClass.getDeclaredMethod("addReads", moduleClass));
            methodHandlesPrivateLookupIn = lookup.findStatic(MethodHandles.class, "privateLookupIn",
                MethodType.methodType(MethodHandles.Lookup.class, Class.class, MethodHandles.Lookup.class));
            moduleIsOpenUnconditionally = lookup.unreflect(moduleClass.getDeclaredMethod("isOpen", String.class));
            moduleIsOpenToOtherModule = lookup.unreflect(
                moduleClass.getDeclaredMethod("isOpen", String.class, moduleClass));

            coreModule = classGetModule.invokeWithArguments(ReflectionUtils.class);
            moduleBased = true;
        } catch (Throwable throwable) {
            if (throwable instanceof Error) {
                throw (Error) throwable;
            } else {
                logger.log(LogLevel.INFORMATIONAL,
                    () -> "Unable to use Module.privateLookupIn for finding the Method.Lookup to use.", throwable);
            }
        }

        if (!moduleBased) {
            try {
                Constructor<MethodHandles.Lookup> privateLookupInConstructor =
                    MethodHandles.Lookup.class.getDeclaredConstructor(Class.class);

                if (!privateLookupInConstructor.isAccessible()) {
                    privateLookupInConstructor.setAccessible(true);
                }

                jdkInternalPrivateLookupInConstructor = lookup.unreflectConstructor(privateLookupInConstructor);
            } catch (ReflectiveOperationException ex) {
                throw new RuntimeException("Unable to use private lookup in constructor.", ex);
            }
        }

        MODULE_BASED = moduleBased;
        CLASS_GET_MODULE = classGetModule;
        MODULE_IS_NAMED = moduleIsNamed;
        MODULE_ADD_READS = moduleAddReads;
        METHOD_HANDLES_PRIVATE_LOOKUP_IN = methodHandlesPrivateLookupIn;
        MODULE_IS_OPEN_UNCONDITIONALLY = moduleIsOpenUnconditionally;
        MODULE_IS_OPEN_TO_OTHER_MODULE = moduleIsOpenToOtherModule;
        LOOKUP = lookup;
        CORE_MODULE = coreModule;
        JDK_INTERNAL_PRIVATE_LOOKUP_IN_CONSTRUCTOR = jdkInternalPrivateLookupInConstructor;
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
    public static MethodHandles.Lookup getLookupToUse(Class<?> targetClass) throws Exception {
        if (MODULE_BASED) {
            try {
                Object responseModule = CLASS_GET_MODULE.invoke(targetClass);

                // The unnamed module is opened unconditionally, have Core read it and use a private proxy lookup to enable all
                // lookup scenarios.
                if (!(boolean) MODULE_IS_NAMED.invoke(responseModule)) {
                    MODULE_ADD_READS.invokeWithArguments(CORE_MODULE, responseModule);
                    return performSafePrivateLookupIn(targetClass);
                }


                // If the response module is the Core module return the Core private lookup.
                if (responseModule == CORE_MODULE) {
                    return LOOKUP;
                }

                // Next check if the target class module is opened either unconditionally or to Core's module. If so, also use
                // a private proxy lookup to enable all lookup scenarios.
                String packageName = targetClass.getPackage().getName();
                if ((boolean) MODULE_IS_OPEN_UNCONDITIONALLY.invokeWithArguments(responseModule, packageName)
                    || (boolean) MODULE_IS_OPEN_TO_OTHER_MODULE.invokeWithArguments(responseModule, packageName, CORE_MODULE)) {
                    MODULE_ADD_READS.invokeWithArguments(CORE_MODULE, responseModule);
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
        } else {
            try {
                return (MethodHandles.Lookup) JDK_INTERNAL_PRIVATE_LOOKUP_IN_CONSTRUCTOR.invoke(targetClass);
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
    }

    @SuppressWarnings("removal")
    private static MethodHandles.Lookup performSafePrivateLookupIn(Class<?> targetClass) throws Throwable {
        // MethodHandles::privateLookupIn() throws SecurityException if denied by the security manager
        if (System.getSecurityManager() == null) {
            return (MethodHandles.Lookup) METHOD_HANDLES_PRIVATE_LOOKUP_IN.invokeExact(targetClass, LOOKUP);
        } else {
            return java.security.AccessController.doPrivileged((PrivilegedExceptionAction<MethodHandles.Lookup>) () -> {
                try {
                    return (MethodHandles.Lookup) METHOD_HANDLES_PRIVATE_LOOKUP_IN.invokeExact(targetClass, LOOKUP);
                } catch (Throwable throwable) {
                    if (throwable instanceof Error) {
                        throw (Error) throwable;
                    } else {
                        throw (Exception) throwable;
                    }
                }
            });
        }
    }

    public static boolean isModuleBased() {
        return MODULE_BASED;
    }

    ReflectionUtils() {
    }
}
