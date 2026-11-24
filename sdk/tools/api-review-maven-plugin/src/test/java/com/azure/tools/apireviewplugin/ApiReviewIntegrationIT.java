// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apireviewplugin;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.MessageDigest;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ApiReviewIntegrationIT {
    @Test
    public void mavenAndIsolatedStandaloneGenerateIdenticalBundles() throws Exception {
        Path target = Paths.get(System.getProperty("apiReview.buildDirectory"));
        Path project = target.resolve("it/basic");
        Path bundle = project.resolve("target/api-review");
        byte[] bytes = Files.readAllBytes(bundle.resolve("api.md"));
        String markdown = new String(bytes, StandardCharsets.UTF_8);
        assertTrue(markdown.contains("Widget"));
        assertFalse(markdown.contains("\r"));
        assertTrue(markdown.endsWith("\n") && !markdown.endsWith("\n\n"));
        StringBuilder hash = new StringBuilder();
        for (byte value : MessageDigest.getInstance("SHA-256").digest(bytes)) {
            hash.append(String.format("%02x", value & 0xff));
        }
        String metadata = read(bundle.resolve("api.metadata.yml"));
        assertEquals(
            "apiHash: " + hash + "\npackageVersion: '1.0.0'\n" + "packageRelativePath: 'sdk/test/azure-widget'\n",
            metadata);

        Path standalone = Files.createDirectories(project.resolve("target/standalone"));
        Path tool = standalone.resolve("tool.jar");
        Files.copy(target.resolve(System.getProperty("apiReview.finalName") + ".jar"), tool,
            StandardCopyOption.REPLACE_EXISTING);
        Path sources = standalone.resolve("azure-widget-1.0.0-sources.jar");
        Files.copy(project.resolve("target/custom-output-sources.jar"), sources, StandardCopyOption.REPLACE_EXISTING);
        Path home = Files.createDirectories(standalone.resolve("home"));
        Path destination = standalone.resolve("bundle");
        Path log = standalone.resolve("run.log");
        Path java = Paths.get(System.getProperty("java.home"), "bin",
            System.getProperty("os.name").startsWith("Windows") ? "java.exe" : "java");
        ProcessBuilder builder = new ProcessBuilder(java.toString(), "-Duser.home=" + home,
            "-Dhttp.proxyHost=127.0.0.1", "-Dhttp.proxyPort=1", "-Dhttps.proxyHost=127.0.0.1", "-Dhttps.proxyPort=1",
            "-jar", tool.toString(), "--sources-jar", sources.toString(), "--package-relative-path",
            "sdk/test/azure-widget", "--output-directory", destination.toString());
        builder.directory(standalone.toFile()).redirectErrorStream(true).redirectOutput(log.toFile());
        builder.environment().remove("CLASSPATH");
        Process process = builder.start();
        try {
            assertTrue(process.waitFor(30, TimeUnit.SECONDS), "Standalone generation timed out.");
            assertEquals(0, process.exitValue(), () -> read(log));
        } finally {
            if (process.isAlive()) {
                process.destroyForcibly();
            }
        }
        assertFalse(Files.exists(standalone.resolve("pom.xml")));
        assertFalse(Files.exists(home.resolve(".m2")));
        assertArrayEquals(bytes, Files.readAllBytes(destination.resolve("api.md")));
        assertEquals(metadata, read(destination.resolve("api.metadata.yml")));
    }

    private static String read(Path path) {
        try {
            return new String(Files.readAllBytes(path), StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Cannot read " + path, exception);
        }
    }
}
