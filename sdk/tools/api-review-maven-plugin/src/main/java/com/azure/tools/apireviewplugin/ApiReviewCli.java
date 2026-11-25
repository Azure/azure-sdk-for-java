// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.apireviewplugin;

import com.azure.tools.apiview.processor.Main;
import com.azure.tools.apiview.processor.model.ApiListing;
import java.io.IOException;
import java.io.PrintStream;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

/** Generates a review bundle without Maven or an external POM. */
public final class ApiReviewCli {
    private static final String SOURCES_JAR = "--sources-jar";
    private static final String PACKAGE_PATH = "--package-relative-path";
    private static final String OUTPUT_DIRECTORY = "--output-directory";
    private static final String USAGE = "Usage: java -jar api-review-maven-plugin.jar " + SOURCES_JAR
        + " <library-version-sources.jar> " + PACKAGE_PATH + " <sdk/path/package> " + OUTPUT_DIRECTORY + " <directory>";

    private ApiReviewCli() {
    }

    /**
     * Generates api.md and api.metadata.yml using the version in the sources JAR.
     *
     * @param args the sources JAR, repository-relative package path, and output directory
     */
    public static void main(String[] args) {
        System.exit(run(args, System.out, System.err));
    }

    static int run(String[] args, PrintStream output, PrintStream error) {
        if (args.length == 1 && ("--help".equals(args[0]) || "-h".equals(args[0]))) {
            output.println(USAGE);
            return 0;
        }
        Path sources;
        Path destination;
        String packagePath;
        try {
            Map<String, String> options = parse(args);
            sources = Paths.get(options.get(SOURCES_JAR));
            destination = Paths.get(options.get(OUTPUT_DIRECTORY)).toAbsolutePath().normalize();
            packagePath = ReviewBundleWriter.normalizePackagePath(options.get(PACKAGE_PATH));
        } catch (IllegalArgumentException exception) {
            error.println(exception.getMessage());
            error.println(USAGE);
            return 2;
        }
        try {
            ApiListing listing = Main.processSourcesJar(sources.toFile());
            new ReviewBundleWriter().write(listing, listing.getPackageVersion(), packagePath, destination);
            output.println("Generated API review bundle in " + destination);
            return 0;
        } catch (IOException | RuntimeException exception) {
            error.println("Failed to generate the API review bundle: " + exception.getMessage());
            exception.printStackTrace(error);
            return 1;
        }
    }

    private static Map<String, String> parse(String[] args) {
        Map<String, String> options = new LinkedHashMap<>();
        for (int index = 0; index < args.length; index++) {
            String option = args[index];
            if (!SOURCES_JAR.equals(option) && !PACKAGE_PATH.equals(option) && !OUTPUT_DIRECTORY.equals(option)) {
                throw new IllegalArgumentException("Unknown argument: " + option);
            }
            if (options.containsKey(option)) {
                throw new IllegalArgumentException("Duplicate argument: " + option);
            }
            if (++index == args.length || args[index].startsWith("--") || args[index].trim().isEmpty()) {
                throw new IllegalArgumentException("Missing value for " + option);
            }
            options.put(option, args[index]);
        }
        for (String option : new String[] { SOURCES_JAR, PACKAGE_PATH, OUTPUT_DIRECTORY }) {
            if (!options.containsKey(option)) {
                throw new IllegalArgumentException("Required argument: " + option);
            }
        }
        return options;
    }
}
