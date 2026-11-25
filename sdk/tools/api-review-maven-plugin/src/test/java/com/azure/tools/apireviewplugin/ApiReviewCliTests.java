// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apireviewplugin;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ApiReviewCliTests {
    @TempDir
    Path directory;

    @Test
    public void generatesWithoutAnExternalPomAndNormalizesThePackagePath() throws Exception {
        Path sources = archive("1.0.0", "public class Widget {}");
        assertEquals(0, run(arguments(sources, "sdk\\test\\azure-widget")));
        assertFalse(Files.exists(directory.resolve("pom.xml")));
        assertTrue(read("api.md").contains("Widget"));
        assertTrue(read("api.metadata.yml")
            .contains("packageVersion: '1.0.0'\n" + "packageRelativePath: 'sdk/test/azure-widget'\n"));
    }

    @Test
    public void rejectsInvalidArgumentsWithoutChangingAnExistingBundle() throws Exception {
        oldBundle();
        Path sources = archive("1.0.0", "public class Widget {}");
        String[][] invalid = {
            { "--unknown" },
            { "--sources-jar", sources.toString() },
            { "--sources-jar", sources.toString(), "--sources-jar", sources.toString() },
            arguments(sources, "sdk/../other") };
        for (String[] args : invalid) {
            ByteArrayOutputStream errors = new ByteArrayOutputStream();
            assertEquals(2, ApiReviewCli.run(args, System.out, new PrintStream(errors, true, "UTF-8")));
            assertTrue(new String(errors.toByteArray(), StandardCharsets.UTF_8).contains("Usage:"));
            assertOldBundle();
        }
    }

    @Test
    public void reportsProcessingAndVersionFailuresWithoutChangingAnExistingBundle() throws Exception {
        oldBundle();
        assertEquals(1, run(arguments(archive("${revision}", "public class Widget {}"), "sdk/test/azure-widget")));
        assertOldBundle();
        assertEquals(1, run(arguments(archive("1.0.0", "public class Widget {"), "sdk/test/azure-widget")));
        assertOldBundle();
    }

    private Path archive(String version, String source) throws Exception {
        Path jar = directory.resolve("azure-widget-1.0.0-sources.jar");
        try (JarOutputStream output = new JarOutputStream(Files.newOutputStream(jar))) {
            output.putNextEntry(new JarEntry("META-INF/maven/com.azure/azure-widget/pom.xml"));
            output.write(("<project><groupId>com.azure</groupId><artifactId>azure-widget</artifactId>" + "<version>"
                + version + "</version></project>").getBytes(StandardCharsets.UTF_8));
            output.closeEntry();
            output.putNextEntry(new JarEntry("com/azure/test/Widget.java"));
            output.write(("package com.azure.test; " + source).getBytes(StandardCharsets.UTF_8));
            output.closeEntry();
        }
        return jar;
    }

    private String[] arguments(Path sources, String packagePath) {
        return new String[] {
            "--sources-jar",
            sources.toString(),
            "--package-relative-path",
            packagePath,
            "--output-directory",
            directory.resolve("bundle").toString() };
    }

    private static int run(String[] args) throws Exception {
        PrintStream sink = new PrintStream(new ByteArrayOutputStream(), true, "UTF-8");
        return ApiReviewCli.run(args, sink, sink);
    }

    private void oldBundle() throws Exception {
        Path bundle = directory.resolve("bundle");
        Files.createDirectory(bundle);
        Files.write(bundle.resolve("api.md"), "old API".getBytes(StandardCharsets.UTF_8));
        Files.write(bundle.resolve("api.metadata.yml"), "old metadata".getBytes(StandardCharsets.UTF_8));
    }

    private void assertOldBundle() throws Exception {
        assertEquals("old API", read("api.md"));
        assertEquals("old metadata", read("api.metadata.yml"));
    }

    private String read(String file) throws Exception {
        return new String(Files.readAllBytes(directory.resolve("bundle").resolve(file)), StandardCharsets.UTF_8);
    }
}
