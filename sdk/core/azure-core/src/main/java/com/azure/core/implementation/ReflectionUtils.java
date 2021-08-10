// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.implementation;

import com.azure.core.util.logging.ClientLogger;
import reactor.core.Exceptions;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;

import static java.lang.invoke.MethodType.methodType;

/**
 * Utility methods that aid in performing reflective operations.
 */
public final class ReflectionUtils {
    private static final ClientLogger LOGGER = new ClientLogger(ReflectionUtils.class);

    // This lookup is specific to the com.azure.core module, specifically this class.
    private static final MethodHandles.Lookup LOOKUP = MethodHandles.lookup();

    // Convenience pointer to the com.azure.core module.
    // Since this is only Java 9+ functionality it needs to use reflection.
    private static final String MODULE_CLASS = "java.lang.Module";
    private static final Object CORE_MODULE;
    private static final MethodHandle GET_MODULE;
    private static final MethodHandle IS_MODULE_EXPORTED;
    private static final MethodHandle CAN_READ_MODULE;
    private static final MethodHandle ADD_MODULE_READ;

    static {
        MethodHandles.Lookup publicLookup = MethodHandles.publicLookup();

        Object coreModule = null;
        MethodHandle getModule = null;
        MethodHandle isModuleExported = null;
        MethodHandle canReadModule = null;
        MethodHandle addReadModule = null;

        try {
            Class<?> moduleClass = Class.forName(MODULE_CLASS);
            Class<?> classClass = Class.class;
            getModule = publicLookup.findVirtual(classClass, "getModule", methodType(moduleClass));
            coreModule = getModule.invoke(ReflectionUtils.class);
            isModuleExported = publicLookup.findVirtual(moduleClass, "isExported",
                methodType(boolean.class, String.class));
            canReadModule = publicLookup.findVirtual(moduleClass, "canRead", methodType(boolean.class, moduleClass));
            addReadModule = MethodHandles.lookup()
                .findVirtual(moduleClass, "addReads", methodType(moduleClass, moduleClass));
        } catch (Throwable ex) {
            new ClientLogger(ReflectionUtils.class)
                .verbose("Failed to retrieve MethodHandles used to check module information. "
                    + "If the application is not using Java 9+ runtime behavior will work as expected.", ex);
        }

        CORE_MODULE = coreModule;
        GET_MODULE = getModule;
        IS_MODULE_EXPORTED = isModuleExported;
        CAN_READ_MODULE = canReadModule;
        ADD_MODULE_READ = addReadModule;
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
     */
    public static MethodHandles.Lookup getLookupToUse(Class<?> targetClass) {
        /*
         * If we were able to write this using Java 9+ code.
         *
         * First check if the response class's module is exported to all unnamed modules. If it is we will use
         * MethodHandles.publicLookup() which is meant for creating MethodHandle instances for publicly accessible
         * classes.
         */
        if (GET_MODULE == null) {
            // Java 8 is being used, public lookup should be able to access the Response class.
            return MethodHandles.publicLookup();
        } else {
            Object responseModule = getModule(targetClass);
            if (isModuleExported(responseModule)) {
                return MethodHandles.publicLookup();
            } else {
                /*
                 * Otherwise, we use the MethodHandles.Lookup which is associated to this (com.azure.core) module, and
                 * more specifically, is tied to this class (ResponseConstructorsCache). But, in order to use this
                 * lookup we need to ensure that the com.azure.core module reads the response class's module as the
                 * lookup won't have permissions necessary to create the MethodHandle instance without it.
                 */
                if (!canReadModule(responseModule)) {
                    addModuleRead(responseModule);
                }

                return LOOKUP;
            }
        }
    }

    private static Object getModule(Class<?> clazz) {
        try {
            return GET_MODULE.invoke(clazz);
        } catch (Throwable throwable) {
            throw LOGGER.logExceptionAsError(Exceptions.propagate(throwable));
        }
    }

    private static boolean isModuleExported(Object module) {
        try {
            return (boolean) IS_MODULE_EXPORTED.invoke(module, "");
        } catch (Throwable throwable) {
            throw LOGGER.logExceptionAsError(Exceptions.propagate(throwable));
        }
    }

    private static boolean canReadModule(Object module) {
        try {
            return (boolean) CAN_READ_MODULE.invoke(CORE_MODULE, module);
        } catch (Throwable throwable) {
            throw LOGGER.logExceptionAsError(Exceptions.propagate(throwable));
        }
    }

    private static void addModuleRead(Object module) {
        try {
            ADD_MODULE_READ.invoke(CORE_MODULE, module);
        } catch (Throwable throwable) {
            throw LOGGER.logExceptionAsError(Exceptions.propagate(throwable));
        }
    }

    private ReflectionUtils() {
    }
}
