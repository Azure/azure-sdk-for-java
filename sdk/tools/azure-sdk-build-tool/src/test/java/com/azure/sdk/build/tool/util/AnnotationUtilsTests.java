package com.azure.sdk.build.tool.util;

import com.test.models.AnnotationA;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Stream;

public class AnnotationUtilsTests {

    @Disabled("Due to classloader issue noted below")
    @Test
    public void findAnnotationTest() {
        final Set<String> interestedPackages = new TreeSet<>(Comparator.comparingInt(String::length));
        Path path = Paths.get("target", "test-classes");
        Stream<Path> pathStream = Stream.of(path);

        buildPackageList(path.toFile().getAbsolutePath(), path.toFile().getAbsolutePath(), interestedPackages);

        // This throws ClassCastException because AnnotationUtils has some custom logic to use the URLClassLoader
        // java.lang.ClassCastException: class jdk.internal.loader.ClassLoaders$AppClassLoader
        // cannot be cast to class java.net.URLClassLoader (jdk.internal.loader.ClassLoaders$AppClassLoader
        // and java.net.URLClassLoader are in module java.base of loader 'bootstrap')
        Set<AnnotatedMethodCallerResult> callsToAnnotatedMethod = AnnotationUtils.findCallsToAnnotatedMethod(AnnotationA.class, pathStream, interestedPackages, true);
    }

    static void buildPackageList(String rootDir, String currentDir, Set<String> packages) {
        final File directory = new File(currentDir);

        final File[] files = directory.listFiles();
        if (files == null) {
            return;
        }

        for (final File file : files) {
            if (file.isFile()) {
                final String path = file.getPath();
                final String packageName = path.substring(rootDir.length() + 1, path.lastIndexOf(File.separator));
                packages.add(packageName.replace(File.separatorChar, '.'));
            } else if (file.isDirectory()) {
                buildPackageList(rootDir, file.getAbsolutePath(), packages);
            }
        }
    }
}
