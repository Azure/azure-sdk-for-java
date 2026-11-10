package com.azure.tools.apiview.processor;

import org.junit.jupiter.api.Test;

import java.io.File;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

public class CommandLineOptionsTests {
    @Test
    public void defaultsToJsonRenderer() {
        CommandLineOptions options = CommandLineOptions.parse(new String[] { "library-sources.jar", "output" });

        assertEquals(OutputRenderer.JSON, options.getRenderer());
        assertArrayEquals(new String[] { "library-sources.jar" }, options.getJarFiles());
        assertEquals(new File("output"), options.getOutputDirectory());
    }

    @Test
    public void parsesMarkdownRendererAndMultipleJars() {
        CommandLineOptions options = CommandLineOptions.parse(new String[] {
            "--renderer=markdown",
            "first-sources.jar,second-sources.jar",
            "output"
        });

        assertEquals(OutputRenderer.MARKDOWN, options.getRenderer());
        assertArrayEquals(new String[] { "first-sources.jar", "second-sources.jar" }, options.getJarFiles());
        assertEquals(new File("output"), options.getOutputDirectory());
    }

    @Test
    public void parsesExplicitJsonRenderer() {
        CommandLineOptions options = CommandLineOptions.parse(
            new String[] { "--renderer=json", "library-sources.jar", "output" });

        assertEquals(OutputRenderer.JSON, options.getRenderer());
    }

    @Test
    public void rejectsInvalidArgumentCount() {
        assertThrows(IllegalArgumentException.class, () -> CommandLineOptions.parse(new String[] { "input.jar" }));
        assertThrows(IllegalArgumentException.class,
            () -> CommandLineOptions.parse(new String[] { "one.jar", "output", "unexpected", "extra" }));
    }

    @Test
    public void rejectsUnsupportedOption() {
        assertThrows(IllegalArgumentException.class,
            () -> CommandLineOptions.parse(new String[] { "--format=markdown", "input.jar", "output" }));
        assertThrows(IllegalArgumentException.class,
            () -> CommandLineOptions.parse(new String[] { "--format=markdown", "output" }));
    }

    @Test
    public void rejectsRendererWithoutPositionalArguments() {
        assertThrows(IllegalArgumentException.class,
            () -> CommandLineOptions.parse(new String[] { "--renderer=markdown", "output" }));
    }

    @Test
    public void rejectsUnsupportedRenderer() {
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> CommandLineOptions.parse(new String[] { "--renderer=xml", "input.jar", "output" }));

        assertEquals("Unsupported renderer 'xml'. Expected json or markdown.", exception.getMessage());
    }
}
