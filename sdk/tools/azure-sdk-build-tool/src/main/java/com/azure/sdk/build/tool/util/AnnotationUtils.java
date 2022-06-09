package com.azure.sdk.build.tool.util;

import com.azure.sdk.build.tool.util.logging.Logger;
import org.reflections8.Reflections;
import org.reflections8.ReflectionsException;
import org.reflections8.scanners.MemberUsageScanner;
import org.reflections8.scanners.MethodAnnotationsScanner;
import org.reflections8.util.ClasspathHelper;
import org.reflections8.util.ConfigurationBuilder;

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

/**
 * Utility class to check for annotations.
 */
public final class AnnotationUtils {
    private static Logger LOGGER = Logger.getInstance();

    private AnnotationUtils() {
        // no-op
    }

    public static ClassLoader getCompleteClassLoader(final Stream<Path> paths) {
        final List<URL> urls = paths.map(AnnotationUtils::pathToUrl).collect(Collectors.toList());
        return URLClassLoader.newInstance(urls.toArray(new URL[0]));
    }

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
        config.addClassLoader(URLClassLoader.newInstance(urls.toArray(new URL[0])));

        // This is extremely ugly code, but it is necessary as the reflections library throws away the classloader
        // I have built above, and so when it goes looking for classes it cannot always find them. What I am doing here
        // is augmenting the actual context class loader with the additional urls, so that when the reflections library
        // falls back to using the context class loader (which it does by default, because it throws away the proper
        // class loader I built above), it can still find the classes I want it to find.
        final URLClassLoader contextClassLoader = (URLClassLoader) ClasspathHelper.contextClassLoader();
        try {
            final Method method = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
            method.setAccessible(true);
            for (final URL url : urls) {
                method.invoke(contextClassLoader, url);
            }
        } catch (Exception e) {
            if (LOGGER.isErrorEnabled()) {
                LOGGER.error("Unable to reflectively call addURL method on URL class. " + e.getMessage());
            }
        }

        final Reflections reflections = new Reflections(config);
        final Set<Method> annotatedMethods = reflections.getMethodsAnnotatedWith(annotation);
        final Set<AnnotatedMethodCallerResult> results = new HashSet<>();

        annotatedMethods.forEach(method -> {
            checkMethod(reflections, annotation, method, interestedPackages, recursive, results);
        });

        return results;
    }

    private static void checkMethod(final Reflections reflections,
                                    final Class<? extends Annotation> annotation,
                                    final Method method,
                                    final Set<String> interestedPackages,
                                    final boolean recursive,
                                    final Set<AnnotatedMethodCallerResult> results) {
        final Set<Member> callingMethods;
        try {
            callingMethods = reflections.getMethodUsage(method);
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
                        checkMethod(reflections, annotation, methodMember, interestedPackages, recursive, results);
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
