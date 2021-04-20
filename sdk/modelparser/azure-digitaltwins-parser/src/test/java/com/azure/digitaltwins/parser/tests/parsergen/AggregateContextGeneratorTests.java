// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.tests.parsergen;

import com.azure.digitaltwins.parser.FileHelpers;
import com.azure.digitaltwins.parser.GeneratedCodeCompareBase;
import com.azure.digitaltwins.parser.implementation.codegen.JavaLibrary;
import com.azure.digitaltwins.parser.implementation.parsergen.AggregateContextGenerator;
import com.azure.digitaltwins.parser.implementation.parsergen.MetamodelDigest;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class AggregateContextGeneratorTests extends GeneratedCodeCompareBase {
    private static final String TEST_SUB_DIRECTORY = "AggregateContext";
    private static final String EXPECTED_FILE_DIRECTORY = "Expected";

    @Test
    public void generateAggregateContextClass() throws IOException {
        final String testSubDirectory = "AggregateContextFullClass";
        cleanUpGeneratedCodes(testSubDirectory);

        String fullDigest = FileHelpers.getFileContentsByFileName("", "digest.json");
        MetamodelDigest digest = new MetamodelDigest(fullDigest);

        JavaLibrary library = new JavaLibrary(FileHelpers.getTestResourcesDirectoryPath(TEST_SUB_DIRECTORY + "/" + testSubDirectory), "com.azure.test;");

        AggregateContextGenerator contextGenerator = new AggregateContextGenerator(digest.getContexts(), digest.getDtdlVersionsAllowingLocalTerms());
        contextGenerator.generateCode(library);

        library.generate();
        compareDirectoryContents(TEST_SUB_DIRECTORY + "/" + testSubDirectory, EXPECTED_FILE_DIRECTORY);
    }

    // Clean up every generated code for each test directory
    public static void cleanUpGeneratedCodes(String individualTestSubDirectory) {
        FileHelpers.deleteJavaFilesInSubDirectory(TEST_SUB_DIRECTORY + "/" + individualTestSubDirectory);
    }
}
