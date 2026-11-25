// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apireviewplugin;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.security.MessageDigest;
import java.util.jar.JarEntry;
import java.util.jar.JarOutputStream;
import org.apache.maven.artifact.DefaultArtifact;
import org.apache.maven.artifact.handler.DefaultArtifactHandler;
import org.apache.maven.model.Build;
import org.apache.maven.model.Model;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.project.MavenProject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GenerateMojoTests {
    @TempDir
    Path directory;

    @Test
    public void generatesBundleWithoutGitWithModulePathAndNormalizedHash() throws Exception {
        GenerateMojo mojo = mojo("public class Widget { public String name() { return null; } }");
        mojo.execute();

        Path output = directory.resolve("sdk/test/azure-widget/target/api-review");
        byte[] bytes = Files.readAllBytes(output.resolve("api.md"));
        String markdown = new String(bytes, StandardCharsets.UTF_8);
        assertTrue(markdown.contains("Widget"));
        assertFalse(markdown.contains("\r"));
        assertFalse(markdown.startsWith("\ufeff"));
        assertTrue(markdown.endsWith("\n"));
        assertFalse(markdown.endsWith("\n\n"));
        StringBuilder hash = new StringBuilder();
        for (byte value : MessageDigest.getInstance("SHA-256").digest(bytes)) {
            hash.append(String.format("%02x", value & 0xff));
        }
        String metadata = new String(Files.readAllBytes(output.resolve("api.metadata.yml")), StandardCharsets.UTF_8);
        assertEquals(
            "apiHash: " + hash + "\npackageVersion: '1.0.0'\n" + "packageRelativePath: 'sdk/test/azure-widget'\n",
            metadata);
    }

    @Test
    public void supportsExplicitPathWithoutGitAndExternalOutput() throws Exception {
        GenerateMojo mojo = mojo("public class Widget {}", "exported-widget");
        set(mojo, "packageRelativePath", "sdk\\test\\azure-widget");
        Path output = directory.resolve("artifacts");
        set(mojo, "outputDirectory", output.toFile());
        mojo.execute();
        assertTrue(new String(Files.readAllBytes(output.resolve("api.metadata.yml")), StandardCharsets.UTF_8)
            .contains("packageRelativePath: 'sdk/test/azure-widget'"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "exported-widget", "sdk/test/azure-widget/nested" })
    public void requiresExplicitPathForNonstandardLayout(String modulePath) throws Exception {
        Files.createDirectory(directory.resolve(".git"));
        GenerateMojo mojo = mojo("public class Widget {}", modulePath);
        MojoFailureException failure = assertThrows(MojoFailureException.class, mojo::execute);
        assertTrue(failure.getMessage().contains("api-review.packageRelativePath"));
    }

    @ParameterizedTest
    @CsvSource({ "custom,false", "custom-1.0.0,false", "attached-custom,true" })
    public void supportsCustomSourcesArtifactNames(String finalName, boolean attached) throws Exception {
        GenerateMojo mojo = mojo("public class Widget {}");
        set(mojo, "packageRelativePath", "sdk/test/azure-widget");
        MavenProject project = (MavenProject) get(mojo, "project");
        Path target = project.getBasedir().toPath().resolve("target");
        Path archive
            = Files.move(target.resolve("azure-widget-1.0.0-sources.jar"), target.resolve(finalName + "-sources.jar"));
        if (attached) {
            DefaultArtifact artifact = new DefaultArtifact("com.azure", "azure-widget", "1.0.0", "compile",
                "java-source", "sources", new DefaultArtifactHandler("java-source"));
            artifact.setFile(archive.toFile());
            project.addAttachedArtifact(artifact);
        } else {
            project.getBuild().setFinalName(finalName);
        }

        mojo.execute();

        assertTrue(Files.isRegularFile(target.resolve("api-review/api.md")));
        assertTrue(new String(Files.readAllBytes(target.resolve("api-review/api.metadata.yml")), StandardCharsets.UTF_8)
            .contains("packageVersion: '1.0.0'"));
    }

    @ParameterizedTest
    @ValueSource(strings = { "azure-widget-1.0.0", "custom" })
    public void rejectsSourceIdentityMismatch(String finalName) throws Exception {
        GenerateMojo mojo = mojo("public class Widget {}");
        set(mojo, "packageRelativePath", "sdk/test/azure-widget");
        MavenProject project = (MavenProject) get(mojo, "project");
        Path target = project.getBasedir().toPath().resolve("target");
        if (!"azure-widget-1.0.0".equals(finalName)) {
            Files.move(target.resolve("azure-widget-1.0.0-sources.jar"), target.resolve(finalName + "-sources.jar"));
            project.getBuild().setFinalName(finalName);
        }
        project.getOriginalModel().setVersion("2.0.0");
        assertThrows(MojoFailureException.class, mojo::execute);
    }

    @Test
    public void preservesPreviousBundleOnInvalidSource() throws Exception {
        GenerateMojo mojo = mojo("public class Widget {");
        set(mojo, "packageRelativePath", "sdk/test/azure-widget");
        Path output = directory.resolve("artifacts");
        Files.createDirectory(output);
        Files.write(output.resolve("api.md"), "old API".getBytes(StandardCharsets.UTF_8));
        Files.write(output.resolve("api.metadata.yml"), "old metadata".getBytes(StandardCharsets.UTF_8));
        set(mojo, "outputDirectory", output.toFile());
        assertThrows(MojoExecutionException.class, mojo::execute);
        assertEquals("old API", new String(Files.readAllBytes(output.resolve("api.md")), StandardCharsets.UTF_8));
        assertEquals("old metadata",
            new String(Files.readAllBytes(output.resolve("api.metadata.yml")), StandardCharsets.UTF_8));
    }

    @Test
    public void rejectsPackageRootAndEscapingRelativePath() throws Exception {
        GenerateMojo mojo = mojo("public class Widget {}");
        MavenProject project = (MavenProject) get(mojo, "project");
        set(mojo, "outputDirectory", project.getBasedir());
        assertThrows(MojoFailureException.class, mojo::execute);
        set(mojo, "outputDirectory", directory.resolve("artifacts").toFile());
        set(mojo, "packageRelativePath", "../other");
        assertThrows(MojoFailureException.class, mojo::execute);
    }

    @Test
    public void skipsAggregatorProjects() throws Exception {
        GenerateMojo mojo = new GenerateMojo();
        MavenProject project = new MavenProject();
        project.setPackaging("pom");
        set(mojo, "project", project);
        mojo.execute();
    }

    private GenerateMojo mojo(String source) throws Exception {
        return mojo(source, "sdk/test/azure-widget");
    }

    private GenerateMojo mojo(String source, String modulePath) throws Exception {
        Path module = directory.resolve(modulePath);
        Path target = module.resolve("target");
        Files.createDirectories(target);
        String pom = "<project><modelVersion>4.0.0</modelVersion><groupId>com.azure</groupId>"
            + "<artifactId>azure-widget</artifactId><version>1.0.0</version></project>";
        Path pomPath = module.resolve("pom.xml");
        Files.write(pomPath, pom.getBytes(StandardCharsets.UTF_8));
        Path jarPath = target.resolve("azure-widget-1.0.0-sources.jar");
        try (JarOutputStream jar = new JarOutputStream(Files.newOutputStream(jarPath))) {
            entry(jar, "META-INF/maven/com.azure/azure-widget/pom.xml", pom);
            entry(jar, "com/azure/test/Widget.java", "package com.azure.test; " + source);
        }
        Model model = new Model();
        model.setGroupId("com.azure");
        model.setArtifactId("azure-widget");
        model.setVersion("1.0.0");
        Build build = new Build();
        build.setDirectory(target.toString());
        build.setFinalName("azure-widget-1.0.0");
        model.setBuild(build);
        MavenProject project = new MavenProject(model);
        project.setOriginalModel(model);
        project.setFile(pomPath.toFile());
        GenerateMojo mojo = new GenerateMojo();
        set(mojo, "project", project);
        set(mojo, "outputDirectory", target.resolve("api-review").toFile());
        return mojo;
    }

    private static void entry(JarOutputStream jar, String name, String value) throws IOException {
        jar.putNextEntry(new JarEntry(name));
        jar.write(value.getBytes(StandardCharsets.UTF_8));
        jar.closeEntry();
    }

    private static void set(Object target, String name, Object value) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        field.set(target, value);
    }

    private static Object get(Object target, String name) throws Exception {
        Field field = target.getClass().getDeclaredField(name);
        field.setAccessible(true);
        return field.get(target);
    }
}
