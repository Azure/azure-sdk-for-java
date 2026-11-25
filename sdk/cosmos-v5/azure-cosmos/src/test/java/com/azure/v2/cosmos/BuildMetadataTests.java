// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.v2.cosmos;

import io.clientcore.core.models.binarydata.BinaryData;
import org.junit.jupiter.api.Test;

import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.module.ModuleDescriptor;
import java.lang.module.Configuration;
import java.lang.module.ModuleFinder;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Arrays;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuildMetadataTests {
    @Test
    void coreDependencyIsUsableWithoutNativeLibraries() {
        assertEquals("build-metadata", BinaryData.fromString("build-metadata").toString());
    }

    @Test
    void libraryMetadataIsFiltered() throws IOException {
        Properties properties = new Properties();
        try (InputStream stream = getClass().getResourceAsStream("/com.azure.v2.cosmos.properties")) {
            assertNotNull(stream);
            properties.load(stream);
        }
        assertEquals("azure-cosmos", properties.getProperty("name"));
        assertEquals("com.azure.v2", properties.getProperty("groupId"));
        String expectedVersion = System.getProperty("cosmos.expectedVersion");
        assertNotNull(expectedVersion);
        assertEquals(expectedVersion, properties.getProperty("version"));
    }

    @Test
    void moduleHasNoPublicApiExports() throws IOException {
        try (InputStream stream = Files.newInputStream(Path.of("target", "classes", "module-info.class"))) {
            ModuleDescriptor descriptor = ModuleDescriptor.read(stream);
            assertEquals("com.azure.v2.cosmos", descriptor.name());
            assertTrue(descriptor.exports().isEmpty());
            assertTrue(descriptor.opens().isEmpty());
            Set<String> dependencies
                = descriptor.requires().stream().map(ModuleDescriptor.Requires::name).collect(Collectors.toSet());
            assertTrue(dependencies.containsAll(Set.of("com.azure.v2.core", "io.clientcore.core")));
        }
    }

    @Test
    void v4AndV5CanShareTheClasspath() throws Exception {
        Path[] paths = testRuntimePaths();
        URL[] urls = new URL[paths.length];
        for (int i = 0; i < paths.length; i++) {
            urls[i] = paths[i].toUri().toURL();
        }
        try (URLClassLoader loader = URLClassLoader.newInstance(urls, ClassLoader.getPlatformClassLoader())) {
            Class<?> legacyClient = loader.loadClass("com.azure.cosmos.CosmosClientBuilder");
            assertNotNull(legacyClient.getConstructor().newInstance());
            assertFalse(legacyClient.getModule().isNamed());
            Properties legacy = new Properties();
            try (InputStream stream = loader.getResourceAsStream("azure-cosmos.properties")) {
                assertNotNull(stream);
                legacy.load(stream);
            }
            assertTrue(legacy.getProperty("version").startsWith("4."));
            Properties current = new Properties();
            try (InputStream stream = loader.getResourceAsStream("com.azure.v2.cosmos.properties")) {
                assertNotNull(stream);
                current.load(stream);
            }
            assertEquals("com.azure.v2", current.getProperty("groupId"));
            assertEquals(System.getProperty("cosmos.expectedVersion"), current.getProperty("version"));
        }
    }

    @Test
    void v4AndV5CanShareAModuleLayer() throws Exception {
        ModuleFinder finder = ModuleFinder.of(testRuntimePaths());
        Configuration configuration = ModuleLayer.boot()
            .configuration()
            .resolve(finder, ModuleFinder.of(), Set.of("com.azure.cosmos", "com.azure.v2.cosmos"));
        ModuleLayer layer
            = ModuleLayer.boot().defineModulesWithOneLoader(configuration, ClassLoader.getPlatformClassLoader());
        Class<?> legacyClient = layer.findLoader("com.azure.cosmos").loadClass("com.azure.cosmos.CosmosClientBuilder");
        assertNotNull(legacyClient.getConstructor().newInstance());
        assertEquals("com.azure.cosmos", legacyClient.getModule().getName());
        Module current = layer.findModule("com.azure.v2.cosmos").orElseThrow();
        assertSame(layer, current.getLayer());
        Properties metadata = new Properties();
        try (InputStream stream = current.getResourceAsStream("com.azure.v2.cosmos.properties")) {
            assertNotNull(stream);
            metadata.load(stream);
        }
        assertEquals("com.azure.v2", metadata.getProperty("groupId"));
        assertEquals(System.getProperty("cosmos.expectedVersion"), metadata.getProperty("version"));
    }

    private static Path[] testRuntimePaths() {
        assertFalse(BuildMetadataTests.class.getModule().isNamed());
        Path classes = Path.of("target", "classes").toAbsolutePath();
        String classpath = System.getProperty("surefire.test.class.path");
        assertNotNull(classpath);
        String searchPath = classpath + File.pathSeparator + System.getProperty("jdk.module.path", "");
        return Arrays.stream(searchPath.split(File.pathSeparator))
            .map(Path::of)
            .map(Path::toAbsolutePath)
            .filter(path -> path.equals(classes) || path.toString().endsWith(".jar"))
            .distinct()
            .toArray(Path[]::new);
    }

    @Test
    void mainAndTestBytecodeUseJava17() throws Exception {
        try (DataInputStream stream
            = new DataInputStream(Files.newInputStream(Path.of("target", "classes", "module-info.class")))) {
            assertEquals(0xCAFEBABE, stream.readInt());
            stream.readUnsignedShort();
            assertEquals(61, stream.readUnsignedShort());
        }
        try (InputStream resource = getClass().getResourceAsStream("BuildMetadataTests.class")) {
            assertNotNull(resource);
            try (DataInputStream stream = new DataInputStream(resource)) {
                assertEquals(0xCAFEBABE, stream.readInt());
                stream.readUnsignedShort();
                assertEquals(61, stream.readUnsignedShort());
            }
        }
    }
}
