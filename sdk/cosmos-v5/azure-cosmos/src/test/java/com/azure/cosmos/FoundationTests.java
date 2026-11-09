// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos;

import io.clientcore.core.models.binarydata.BinaryData;
import org.junit.jupiter.api.Test;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.module.ModuleDescriptor;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FoundationTests {
    @Test
    void coreDependencyIsUsableWithoutNativeLibraries() {
        assertEquals("foundation", BinaryData.fromString("foundation").toString());
    }

    @Test
    void libraryMetadataIsFiltered() throws IOException {
        Properties properties = new Properties();
        try (InputStream stream = getClass().getResourceAsStream("/azure-cosmos.properties")) {
            assertNotNull(stream);
            properties.load(stream);
        }
        assertEquals("azure-cosmos", properties.getProperty("name"));
        String expectedVersion = System.getProperty("foundation.version");
        assertNotNull(expectedVersion);
        assertEquals(expectedVersion, properties.getProperty("version"));
    }

    @Test
    void moduleHasNoPublicApiOrNativeImplementation() throws IOException {
        try (InputStream stream = Files.newInputStream(Path.of("target", "classes", "module-info.class"))) {
            ModuleDescriptor descriptor = ModuleDescriptor.read(stream);
            assertEquals("com.azure.cosmos", descriptor.name());
            assertTrue(descriptor.exports().isEmpty());
            assertTrue(descriptor.opens().isEmpty());
            Set<String> dependencies
                = descriptor.requires().stream().map(ModuleDescriptor.Requires::name).collect(Collectors.toSet());
            assertTrue(dependencies.containsAll(Set.of("com.azure.v2.core", "io.clientcore.core")));
        }
    }

    @Test
    void mainAndTestBytecodeUseJava17() throws Exception {
        try (DataInputStream stream
            = new DataInputStream(Files.newInputStream(Path.of("target", "classes", "module-info.class")))) {
            assertEquals(0xCAFEBABE, stream.readInt());
            stream.readUnsignedShort();
            assertEquals(61, stream.readUnsignedShort());
        }
        try (InputStream resource = getClass().getResourceAsStream("FoundationTests.class")) {
            assertNotNull(resource);
            try (DataInputStream stream = new DataInputStream(resource)) {
                assertEquals(0xCAFEBABE, stream.readInt());
                stream.readUnsignedShort();
                assertEquals(61, stream.readUnsignedShort());
            }
        }
    }
}
