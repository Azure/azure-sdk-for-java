// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.core.implementation.instrumentation;

/**
 * Loads optional instrumentation APIs on Java 8 and on modular runtimes.
 */
public final class OptionalDependency {
    private OptionalDependency() {
    }

    /**
     * Loads an optional type and allows ClientCore to read its unnamed module.
     *
     * @param className the optional type
     * @throws ReflectiveOperationException if the type cannot be loaded or the read edge cannot be added
     */
    public static void load(String className) throws ReflectiveOperationException {
        Class<?> dependency = Class.forName(className, false, OptionalDependency.class.getClassLoader());
        java.lang.reflect.Method getModule;
        try {
            getModule = Class.class.getMethod("getModule");
        } catch (NoSuchMethodException ignored) {
            // Java 8 has no module boundaries.
            return;
        }

        Object dependencyModule = getModule.invoke(dependency);
        Class<?> moduleClass = getModule.getReturnType();
        if (!(boolean) moduleClass.getMethod("isNamed").invoke(dependencyModule)) {
            Object coreModule = getModule.invoke(OptionalDependency.class);
            moduleClass.getMethod("addReads", moduleClass).invoke(coreModule, dependencyModule);
        }
    }
}
