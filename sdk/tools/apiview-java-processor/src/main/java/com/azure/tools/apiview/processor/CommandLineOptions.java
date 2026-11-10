package com.azure.tools.apiview.processor;

import java.io.File;

final class CommandLineOptions {
    private static final String RENDERER_OPTION_PREFIX = "--renderer=";

    private final OutputRenderer renderer;
    private final String[] jarFiles;
    private final File outputDirectory;

    private CommandLineOptions(OutputRenderer renderer, String[] jarFiles, File outputDirectory) {
        this.renderer = renderer;
        this.jarFiles = jarFiles;
        this.outputDirectory = outputDirectory;
    }

    static CommandLineOptions parse(String[] args) {
        if (args.length != 2 && args.length != 3) {
            throw new IllegalArgumentException("Expected two positional arguments and an optional renderer.");
        }

        boolean hasRendererOption = args[0].startsWith(RENDERER_OPTION_PREFIX);
        if (!hasRendererOption && (args.length == 3 || args[0].startsWith("--"))) {
            throw new IllegalArgumentException("Unsupported option '" + args[0] + "'.");
        }
        if (args.length == 2 && hasRendererOption) {
            throw new IllegalArgumentException("The renderer option requires input JARs and an output directory.");
        }

        int positionalArgumentOffset = hasRendererOption ? 1 : 0;
        OutputRenderer renderer = hasRendererOption
            ? OutputRenderer.fromValue(args[0].substring(RENDERER_OPTION_PREFIX.length()))
            : OutputRenderer.JSON;

        return new CommandLineOptions(
            renderer,
            args[positionalArgumentOffset].split(","),
            new File(args[positionalArgumentOffset + 1]));
    }

    OutputRenderer getRenderer() {
        return renderer;
    }

    String[] getJarFiles() {
        return jarFiles;
    }

    File getOutputDirectory() {
        return outputDirectory;
    }
}
