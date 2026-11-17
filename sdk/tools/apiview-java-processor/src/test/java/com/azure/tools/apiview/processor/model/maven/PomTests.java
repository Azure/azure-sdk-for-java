// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apiview.processor.model.maven;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.xml.sax.SAXException;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class PomTests {
    @ParameterizedTest
    @ValueSource(strings = { "custom-sources.jar", "custom-1.0.0-sources.jar" })
    public void selectsExpectedPomFromCustomNamedSourcesJar(String filename, @TempDir Path directory)
        throws IOException {
        Path archive = directory.resolve(filename);
        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(archive))) {
            for (String group : new String[] { "com.azure", "org.other" }) {
                jar.putNextEntry(new JarEntry("META-INF/maven/" + group + "/azure-widget/pom.xml"));
                jar.write(("<project><modelVersion>4.0.0</modelVersion><groupId>" + group + "</groupId>"
                    + "<artifactId>azure-widget</artifactId><version>1.0.0</version></project>")
                        .getBytes(StandardCharsets.UTF_8));
                jar.closeEntry();
            }
        }

        Pom pom = Pom.fromSourcesJarFile(archive.toFile(), "com.azure", "azure-widget");

        assertEquals("com.azure", pom.getGroupId());
        assertEquals("azure-widget", pom.getArtifactId());
        assertEquals("1.0.0", pom.getVersion());
    }

    @Test
    public void malformedPomPreservesParserFailure() {
        IOException exception = assertThrows(IOException.class,
            () -> new Pom(new ByteArrayInputStream("<project>".getBytes(StandardCharsets.UTF_8))));
        assertInstanceOf(SAXException.class, exception.getCause());
    }
}
