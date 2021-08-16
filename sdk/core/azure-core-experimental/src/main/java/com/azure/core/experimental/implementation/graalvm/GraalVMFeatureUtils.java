// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.implementation.graalvm;

import com.oracle.svm.core.jdk.proxy.DynamicProxyRegistry;
import org.graalvm.nativeimage.ImageSingletons;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.Feature.BeforeAnalysisAccess;
import org.graalvm.nativeimage.hosted.RuntimeReflection;

import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Enumeration;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import static org.graalvm.nativeimage.hosted.RuntimeReflection.register;

/**
 * Utility class to configure GraalVM features.
 */
public final class GraalVMFeatureUtils {

    /**
     * Registers the given interfaces for dynamic proxy generation.
     *
     * @param access     The {@link BeforeAnalysisAccess} instance
     * @param interfaces the list of interfaces that the generated proxy can implement
     */
    public static void addProxyClass(final Feature.FeatureAccess access, final String... interfaces) {
        final List<Class<?>> classList = new ArrayList<>();
        for (final String anInterface : interfaces) {
            final Class<?> clazz = access.findClassByName(anInterface);
            if (clazz != null) {
                classList.add(clazz);
            }
        }
        if (classList.size() == interfaces.length) {
            ImageSingletons.lookup(DynamicProxyRegistry.class).addProxyClass(classList.toArray(new Class<?>[interfaces.length]));
        }
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> Set<T> setOf(final T... s) {
        final Set<T> set = new LinkedHashSet<>(s.length);
        set.addAll(Arrays.asList(s));
        return set;
    }

    @SafeVarargs
    @SuppressWarnings("varargs")
    public static <T> Set<T[]> setsOf(final T[]... s) {
        final Set<T[]> set = new LinkedHashSet<>(s.length);
        Collections.addAll(set, s);
        return set;
    }

    public static Optional<Class<?>> findClass(final Feature.FeatureAccess access, final String className) {
        return Optional.ofNullable(access.findClassByName(className));
    }

    public static String[] interfaces(String... strings) {
        return strings;
    }

    public static void registerClass(final Feature.FeatureAccess access, final ClassReflectionAttributes reflectiveClass) {
        GraalVMFeatureUtils.findClass(access, reflectiveClass.getName()).ifPresent(cls -> {
            RuntimeReflection.register(cls);

            // fields
            if (reflectiveClass.includeDeclaredFields()) {
                register(cls.getDeclaredFields());
            }
            if (reflectiveClass.includePublicFields()) {
                register(cls.getFields());
            }

            // constructors
            if (reflectiveClass.includeDeclaredConstructors()) {
                register(cls.getDeclaredConstructors());
            }
            if (reflectiveClass.includePublicConstructors()) {
                register(cls.getConstructors());
            }

            // methods
            if (reflectiveClass.includeDeclaredMethods()) {
                register(cls.getDeclaredMethods());
            }
            if (reflectiveClass.includePublicMethods()) {
                register(cls.getMethods());
            }

            // classes
            if (reflectiveClass.includeDeclaredClasses()) {
                register(cls.getDeclaredClasses());
            }
            if (reflectiveClass.includePublicClasses()) {
                register(cls.getClasses());
            }
        });
    }

    public static Stream<String> getClassesForPackage(final Feature.FeatureAccess access,
                                                      final String packageName,
                                                      final boolean recursive) {
        final ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        final String packagePath = packageName.replace('.', '/');

        try {
            final Enumeration<URL> resources = classLoader.getResources(packagePath);
            while (resources.hasMoreElements()) {
                final URL url = resources.nextElement();
                final URLConnection connection = url.openConnection();
                if (connection instanceof JarURLConnection) {
                    return findClassesInJar(access, ((JarURLConnection) connection).getJarFile(), packageName, recursive);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Could not enable reflective access to classes from package " + packageName, e);
        }

        return Stream.empty();
    }

    private static Stream<String> findClassesInJar(final Feature.FeatureAccess access,
                                                   final JarFile jarFile,
                                                   final String packageName,
                                                   final boolean recursive) {
        final List<String> classNames = new ArrayList<>();
        final Enumeration<JarEntry> entries = jarFile.entries();

        // This enumerates all files in a single jar, so we need to be sure the entry is specifically in the
        // specified package, and not a sub-package
        final int packageDepth = countInString(packageName, '.');
        while (entries.hasMoreElements()) {
            final String entryName = entries.nextElement().getName();

            // we compare the package depth of the given entry with the package depth of the package we are looking for.
            // At this point the entry is using forward slashes for packages, so we adjust accordingly here, and we
            // subtract one to account for the slash before the class name
            if (!recursive && countInString(entryName, '/') - 1 != packageDepth) {
                continue;
            }

            if (entryName.endsWith(".class")) {
                String fqcn = entryName
                    .replace('/', '.')
                    .replace(".class", "");

                if (fqcn.startsWith(packageName)) {
                    if (!fqcn.startsWith(packageName)) {
                        continue;
                    }

                    classNames.add(fqcn);
                }
            }
        }

        return classNames.stream();
    }

    private static int countInString(String haystack, char needle) {
        int count = 0;
        for (int i = 0; i < haystack.length(); i++) {
            if (haystack.charAt(i) == needle) {
                count++;
            }
        }
        return count;
    }

    private GraalVMFeatureUtils() {
        // private ctor
    }
}
