// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser;

import com.azure.digitaltwins.parser.implementation.codegen.CodeWriter;
import org.junit.jupiter.api.Assertions;

import java.io.IOException;

public class GeneratedCodeComparerBase {
    private final static String DASH_SEPARATOR = "------------------------------------------------------------------------------------------------------------------------";
    private final static String GENERATED_CODE_FILE_EXTENSION = ".temp.generated";
    private final static String EXPECTED_CODE_FILE_EXTENSION = ".expected";

    public CodeWriter getCodeWriter(String subDirectoryName, String typeName) throws IOException {
        CodeWriter codeWriter = new CodeWriter(FileHelpers.getTestResourceFilePath(subDirectoryName, typeName + GENERATED_CODE_FILE_EXTENSION));
        return codeWriter;
    }

    public void compareGeneratedCodeWithExpected(String subDirectoryName, String typeName) throws IOException {
        String expectedFileContents = FileHelpers.getFileContentsByFileName(subDirectoryName, typeName + EXPECTED_CODE_FILE_EXTENSION);
        String generatedFileContents = FileHelpers.getFileContentsByFileName(subDirectoryName, typeName + GENERATED_CODE_FILE_EXTENSION);
        Assertions.assertEquals(
            expectedFileContents,
            generatedFileContents,
            String.format(
                "Expected \n%s\n%s%s\nwith %s characters, but found \n%s\n%s%s\nwith %s characters.",
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
}
