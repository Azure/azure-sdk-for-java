// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser;

import com.azure.digitaltwins.parser.implementation.codegen.CodeWriter;
import org.junit.jupiter.api.Assertions;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class GeneratedCodeCompareBase {
    private static final String DASH_SEPARATOR = "------------------------------------------------------------------------------------------------------------------------";
    private static final String EXPECTED_CODE_FILE_EXTENSION = ".expected";
    private static final String GENERATED_CODE_FILE_EXTENSION = ".temp.generated";

    public CodeWriter getCodeWriter(String subDirectoryName, String typeName) throws IOException {
        return new CodeWriter(FileHelpers.getTestResourceFilePath(subDirectoryName, typeName + GENERATED_CODE_FILE_EXTENSION), true);
    }

    public void compareGeneratedCodeWithExpected(String subDirectoryName, String typeName) throws IOException {
        String expectedFileContents = FileHelpers.getFileContentsByFileName(subDirectoryName, typeName + EXPECTED_CODE_FILE_EXTENSION);
        String generatedFileContents = FileHelpers.getFileContentsByFileName(subDirectoryName, typeName + GENERATED_CODE_FILE_EXTENSION);

        expectedFileContents = expectedFileContents.trim().replaceAll("\r\n", "\n");
        generatedFileContents = generatedFileContents.trim().replaceAll("\r\n", "\n");

        Assertions.assertEquals(
            expectedFileContents,
            generatedFileContents,
            String.format(
                "\nExpected \n%s\n%s\n%s\nwith %s characters, but found \n%s\n%s\n%s\nwith %s characters.",
                DASH_SEPARATOR,
                expectedFileContents,
                DASH_SEPARATOR,
                expectedFileContents.chars().count(),
                DASH_SEPARATOR,
                generatedFileContents,
                DASH_SEPARATOR,
                generatedFileContents.chars().count()));

        // Clean up if test is successful.
        FileHelpers.deleteFile(subDirectoryName, typeName + GENERATED_CODE_FILE_EXTENSION);
    }

    public static void compareDirectoryContents(String generatedCodeSubDirectory, String expectedCodeSubDirectory) throws IOException {
        // Enumerate through expected sub-directory files.
        File expectedDirectory = new File(FileHelpers.getTestResourcesDirectoryPath(generatedCodeSubDirectory) + "/" + expectedCodeSubDirectory);
        File[] expectedDirectoryFiles = expectedDirectory.listFiles();

        File generatedCodeDirectory = new File(FileHelpers.getTestResourcesDirectoryPath(generatedCodeSubDirectory));
        File[] generatedDirectoryFiles = generatedCodeDirectory.listFiles();

        List<String> unverifiedFiles = new ArrayList<>(
            Arrays.stream(expectedDirectoryFiles)
                .map(s -> s.getName())
                .collect(Collectors.toList()));

        List<String> verifiedFiles = new ArrayList<>();

        for (File expectedFile : expectedDirectoryFiles) {
            // look for corresponding generated file.
            // This step verifies all files that are found look identical in terms of contents;
            for (File generatedFile : generatedDirectoryFiles) {
                if (expectedFile.getName().equals(generatedFile.getName())) {
                    // We compare the contents.
                    String expectedFileContents = FileHelpers
                        .getFileContentsByPath(expectedFile.getAbsolutePath())
                        .trim()
                        .replaceAll("\r\n", "\n");

                    String generatedFileContents = FileHelpers
                        .getFileContentsByPath(generatedFile.getAbsolutePath())
                        .trim()
                        .replaceAll("\r\n", "\n");

                    Assertions.assertEquals(
                        expectedFileContents,
                        generatedFileContents,
                        String.format(
                            "\nExpected the generated file:\n%s\nto have the contents of the expected file:\n%s\n%s\n%s\n%s\nwith %s characters, but found \n%s\n%s\n%s\nwith %s characters.",
                            generatedFile.getAbsolutePath(),
                            expectedFile.getAbsolutePath(),
                            DASH_SEPARATOR,
                            expectedFileContents,
                            DASH_SEPARATOR,
                            expectedFileContents.chars().count(),
                            DASH_SEPARATOR,
                            generatedFileContents,
                            DASH_SEPARATOR,
                            generatedFileContents.chars().count()));

                    // If we find a corresponding file, we remove it from the unverified file list and add it to verified file list.
                    unverifiedFiles.remove(expectedFile.getName());
                    verifiedFiles.add(expectedFile.getName());
                    break;
                }
            }
        }

        // This step verifies all expected files were found and compared correctly.
        // If there are any unverified files, we will fail the assertion.
        if (!unverifiedFiles.isEmpty()) {
            Assertions.fail(
                String.format("Missing expected files: \n%s", String.join(", ", unverifiedFiles))
            );
        }

        // Delete all generated files.
        for (File generatedFile :
            generatedDirectoryFiles) {
            if (generatedFile.getName().endsWith(".java")) {
                generatedFile.delete();
            }
        }
    }
}
