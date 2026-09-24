// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.http.netty;

import io.netty.util.Version;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.condition.EnabledForJreRange;
import org.junit.jupiter.api.condition.JRE;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Verifies native module resolution without loading platform-specific JNI libraries.
 */
@EnabledForJreRange(min = JRE.JAVA_11)
public class NettyNativeTransportModulesTests {
    private static final String EPOLL_CLASSES = "io.netty.transport.classes.epoll";
    private static final String EPOLL_NATIVE = "io.netty.transport.epoll.linux.x86_64";
    private static final String KQUEUE_CLASSES = "io.netty.transport.classes.kqueue";
    private static final String KQUEUE_NATIVE = "io.netty.transport.kqueue.osx.x86_64";

    @ParameterizedTest
    @MethodSource("nativeTransports")
    @Timeout(value = 30, unit = TimeUnit.SECONDS)
    public void nativeTransportRequiresBothModuleRoots(String classesModule, String nativeModule) throws Exception {
        String withoutRoots = resolveModules("io.netty.transport");
        assertFalse(isResolved(withoutRoots, classesModule), withoutRoots);
        assertFalse(isResolved(withoutRoots, nativeModule), withoutRoots);

        String withClasses = resolveModules("io.netty.transport", classesModule);
        assertTrue(isResolved(withClasses, classesModule), withClasses);
        assertFalse(isResolved(withClasses, nativeModule), withClasses);

        String withBoth = resolveModules("io.netty.transport", classesModule, nativeModule);
        assertTrue(isResolved(withBoth, classesModule), withBoth);
        assertTrue(isResolved(withBoth, nativeModule), withBoth);
    }

    private static Stream<Arguments> nativeTransports() {
        return Stream.of(Arguments.of(EPOLL_CLASSES, EPOLL_NATIVE), Arguments.of(KQUEUE_CLASSES, KQUEUE_NATIVE));
    }

    private static boolean isResolved(String output, String name) {
        return output.contains("root " + name + " ")
            || output.contains(" requires " + name + " ")
            || output.contains(" binds " + name + " ");
    }

    private static String resolveModules(String... roots) throws Exception {
        String modulePath = modulePath();
        String executable
            = System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("win") ? "java.exe" : "java";
        String java = Paths.get(System.getProperty("java.home"), "bin", executable).toString();
        List<String> command = Arrays.asList(java, "--module-path", modulePath, "--add-modules",
            String.join(",", roots), "--show-module-resolution", "-version");

        Process process = new ProcessBuilder(command).redirectErrorStream(true).start();
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        try (InputStream input = process.getInputStream()) {
            byte[] buffer = new byte[4096];
            int count;
            while ((count = input.read(buffer)) != -1) {
                output.write(buffer, 0, count);
            }
        }

        String text = new String(output.toByteArray(), StandardCharsets.UTF_8);
        assertEquals(0, process.waitFor(), text);
        return text;
    }

    private static String modulePath() {
        Version nettyCommon
            = Version.identify(NettyNativeTransportModulesTests.class.getClassLoader()).get("netty-common");
        assertNotNull(nettyCommon, "Missing Netty common version");
        String version = nettyCommon.artifactVersion();
        // Surefire separates resolved modules from the test classpath containing the optional native JARs.
        List<Path> entries = Stream.of("surefire.test.class.path", "jdk.module.path", "java.class.path")
            .map(System::getProperty)
            .filter(value -> value != null && !value.isEmpty())
            .flatMap(value -> Arrays.stream(value.split(Pattern.quote(File.pathSeparator))))
            .map(Paths::get)
            .distinct()
            .collect(Collectors.toList());
        assertFalse(entries.isEmpty(), "Missing test classpath and module path");
        List<Path> jars = Arrays.asList(jarFromClasspath(entries, "netty-common-" + version + ".jar"),
            jarFromClasspath(entries, "netty-buffer-" + version + ".jar"),
            jarFromClasspath(entries, "netty-transport-" + version + ".jar"),
            jarFromClasspath(entries, "netty-resolver-" + version + ".jar"),
            jarFromClasspath(entries, "netty-transport-native-unix-common-" + version + ".jar"),
            jarFromClasspath(entries, "netty-transport-classes-epoll-" + version + ".jar"),
            jarFromClasspath(entries, "netty-transport-classes-kqueue-" + version + ".jar"),
            jarFromClasspath(entries, "netty-transport-native-epoll-" + version + "-linux-x86_64.jar"),
            jarFromClasspath(entries, "netty-transport-native-kqueue-" + version + "-osx-x86_64.jar"));
        return jars.stream().distinct().map(Path::toString).collect(Collectors.joining(File.pathSeparator));
    }

    private static Path jarFromClasspath(List<Path> entries, String fileName) {
        List<Path> matches = entries.stream()
            .filter(path -> path.getFileName().toString().equals(fileName))
            .collect(Collectors.toList());
        assertEquals(1, matches.size(), "Expected one Netty test dependency for " + fileName);
        return matches.get(0);
    }
}
