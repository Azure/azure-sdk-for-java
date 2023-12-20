// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.sdk.build.tool.util;

import com.azure.sdk.build.tool.util.logging.Logger;
import org.reflections8.Reflections;
import org.reflections8.ReflectionsException;
import org.reflections8.scanners.MemberUsageScanner;
import org.reflections8.scanners.MethodAnnotationsScanner;
import org.reflections8.util.ConfigurationBuilder;
import org.reflections8.util.Utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.reflections8.util.Utils.index;
import static org.reflections8.util.Utils.name;

/**
 * Utility class to check for annotations.
 */
public final class AnnotationUtils {
    private static final Logger LOGGER = Logger.getInstance();

    private AnnotationUtils() {
        // no-op
    }

    /**
     * Returns a classloader that contains all the given paths.
     *
     * @param paths The paths to include in the classloader
     * @return The classloader that contains the given paths.
     */
    public static ClassLoader getCompleteClassLoader(final Stream<Path> paths) {
        final List<URL> urls = paths.map(AnnotationUtils::pathToUrl).collect(Collectors.toList());
        return URLClassLoader.newInstance(urls.toArray(new URL[0]));
    }

    /**
     * Returns the annotation class with the given name if found.
     * @param name The name of the annotation to look for.
     * @param classLoader The class loader to use to load the annotation.
     * @return The annotation class if found, otherwise an empty optional.
     */
    public static Optional<Class<? extends Annotation>> getAnnotation(String name, ClassLoader classLoader) {
        try {
            return Optional.of(Class.forName(name, false, classLoader).asSubclass(Annotation.class));
        } catch (ClassNotFoundException e) {
            LOGGER.info("Unable to find annotation " + name + " in classpath");
        }
        return Optional.empty();
    }

    /**
     * Returns a list of methods that call methods that are annotated with the given annotation.
     * @param annotation The annotation on the method to look for.
     * @param paths The paths to scan.
     * @param interestedPackages The packages that this scan should be limited to.
     * @param recursive If true, look for packages in the sub-directories of the given paths.
     * @return A set of methods that call methods with the annotation.
     */
    public static Set<AnnotatedMethodCallerResult> findCallsToAnnotatedMethod(final Class<? extends Annotation> annotation,
                                                                              final Stream<Path> paths,
                                                                              final Set<String> interestedPackages,
                                                                              final boolean recursive) {
        final ConfigurationBuilder config = new ConfigurationBuilder()
                .setScanners(new MethodAnnotationsScanner(), new MemberUsageScanner());

        final List<URL> urls = paths.map(AnnotationUtils::pathToUrl).collect(Collectors.toList());
        config.addUrls(urls);
        URLClassLoader classLoader = URLClassLoader.newInstance(urls.toArray(new URL[0]));
        config.addClassLoader(classLoader);
        final Reflections reflections = new Reflections(config);
        final Set<Method> annotatedMethods = reflections.getMethodsAnnotatedWith(annotation);
        final Set<AnnotatedMethodCallerResult> results = new HashSet<>();

        annotatedMethods.forEach(method -> {
            checkMethod(reflections, annotation, method, interestedPackages, recursive, results, classLoader);
        });

        return results;
    }

    private static void checkMethod(final Reflections reflections,
                                    final Class<? extends Annotation> annotation,
                                    final Method method,
                                    final Set<String> interestedPackages,
                                    final boolean recursive,
                                    final Set<AnnotatedMethodCallerResult> results,
                                    ClassLoader classLoader) {
        final Set<Member> callingMethods;
        try {
            String methodName = name(method);
            Iterable<String> values = reflections.getStore().get(index(MemberUsageScanner.class), methodName);
            callingMethods = Utils.getMembersFromDescriptors(values, classLoader);
        } catch (ReflectionsException e) {
            LOGGER.info("Unable to get method usage for method " + method.getName() + ". " + e.getMessage());
            return;
        }

        callingMethods.forEach(member -> {
            // we only add a result if the calling method is in the list of packages we are interested in
            if (member instanceof Method) {
                final Method methodMember = (Method) member;
                final String packageName = methodMember.getDeclaringClass().getPackage().getName();

                if (interestedPackages.contains(packageName)) {
                    // we have reached a point where we have found a method call from code in a package
                    // we are interested in, so we will record it as a valid result. We do not recurse
                    // further from this method.
                    results.add(new AnnotatedMethodCallerResult(annotation, method, member));
                } else {
                    if (recursive && !methodMember.equals(method)) {
                        // we are looking at code that we know calls an annotated service method, but it is not
                        // within one of the packages we are interested in. We recurse here, finding all methods
                        // that call this method, until such time that we run out of methods to check.
                        checkMethod(reflections, annotation, methodMember, interestedPackages, recursive, results, classLoader);
                    }
                }
            }
        });
    }

    private static URL pathToUrl(Path path) {
        try {
            URL url = path.toUri().toURL();
            return url;
        } catch (MalformedURLException e) {
            LOGGER.info("Path " + path + " cannot be converted to URL. " + e.getMessage());
            return null;
        }
    }
}
