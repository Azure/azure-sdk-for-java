// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.implementation.graalvm;

import org.graalvm.nativeimage.hosted.Feature;

import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.TreeSet;

import static com.azure.core.experimental.implementation.graalvm.GraalVMFeatureUtils.addProxyClass;
import static com.azure.core.experimental.implementation.graalvm.GraalVMFeatureUtils.findClass;
import static com.azure.core.experimental.implementation.graalvm.GraalVMFeatureUtils.getClassesForPackage;
import static com.azure.core.experimental.implementation.graalvm.GraalVMFeatureUtils.registerClass;

/**
 * Implementations of this interface should configure the features specific to the Azure SDK client libraries.
 */
public interface GraalVMFeature extends Feature {

    default Set<String[]> getDynamicProxies() {
        return Collections.emptySet();
    }

    default Set<ClassReflectionAttributes> getReflectionClasses() {
        return Collections.emptySet();
    }

    /**
     * This should the root package of the library - all classes within that package (and all sub-packages, including
     * implementation classes) will be made available for reflection.
     */
    String getRootPackage();

    @Override
    default void beforeAnalysis(final BeforeAnalysisAccess access) {
        Feature.super.beforeAnalysis(access);

        // register the reflection classes and dynamic proxies
        final Set<ClassReflectionAttributes> reflectionClasses = getReflectionClasses();
        final Set<String[]> dynamicProxies = getDynamicProxies();

        // Before we do that, we validate each of the classes specified can be found on the classpath.
        // If we can't find **all** of them, we don't proceed and we log an error to the console.
        final Set<String> missingClasses = new TreeSet<>(String::compareTo);
        if (!reflectionClasses.isEmpty()) {
            reflectionClasses.forEach(cls -> {
                if (!findClass(access, cls.getName()).isPresent()) {
                    missingClasses.add(cls.getName());
                }
            });

            dynamicProxies.forEach(interfaces -> {
                Arrays.stream(interfaces).forEach(cls -> {
                    if (!findClass(access, cls).isPresent()) {
                        missingClasses.add(cls);
                    }
                });
            });
        }

        if (!missingClasses.isEmpty()) {
            System.out.println("AZURE SDK: Not registering Azure GraalVM support for " + getClass()
                    + " as not all specified classes were found on classpath. Missing classes are:");
            missingClasses.forEach(cls -> System.out.println("  - " + cls));
        } else {
            System.out.println("AZURE SDK: Registering Azure GraalVM support for " + getClass());
            reflectionClasses.forEach(reflectiveClass -> registerClass(access, reflectiveClass));
            dynamicProxies.forEach(interfaces -> addProxyClass(access, interfaces));

            // we also register all other classes as discovered in the exported packages set
            getClassesForPackage(access, getRootPackage(), true)
                .sorted()
                .map(ClassReflectionAttributes::createWithAllDeclared)   // create ReflectiveClass instances for all, with full API access enabled
                .filter(reflectiveClass -> !reflectionClasses.contains(reflectiveClass)) // don't overwrite custom rules
                .forEach(reflectiveClass -> registerClass(access, reflectiveClass));
        }
    }
}
