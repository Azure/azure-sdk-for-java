// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.codegen.tests.parsergen;

import com.azure.digitaltwins.codegen.FileHelpers;
import com.azure.digitaltwins.codegen.GeneratedCodeCompareBase;
import com.azure.digitaltwins.codegen.implementation.parsergen.CodeGeneratorTask;
import com.azure.digitaltwins.codegen.implementation.parsergen.MetamodelDigest;
import org.junit.jupiter.api.Test;

public class CodeGeneratorTaskTests extends GeneratedCodeCompareBase {
    private static final String TEST_SUB_DIRECTORY = "CodeGeneratorTask";
    private static final String EXPECTED_FILE_DIRECTORY = "Expected";

    @Test
    public void generateAggregateContextClass() throws Exception {
        final String testSubDirectory = "CodeGeneratorTaskLibrary";
        cleanUpGeneratedCodes(testSubDirectory);

        String fullDigest = FileHelpers.getFileContentsByFileName("", "digest.json");
        MetamodelDigest digest = new MetamodelDigest(fullDigest);

        CodeGeneratorTask codeGeneratorTask = new CodeGeneratorTask(
            FileHelpers.getTestResourceFilePath("", "digest.json"),
            FileHelpers.getTestResourcesDirectoryPath(TEST_SUB_DIRECTORY + "/" + testSubDirectory));

        // Uncomment out the following line to inspect the generated code.
        //codeGeneratorTask.run();
        //cleanUpGeneratedCodes(testSubDirectory);
    }

    // Clean up every generated code for each test directory
    public static void cleanUpGeneratedCodes(String individualTestSubDirectory) {
        FileHelpers.deleteJavaFilesInSubDirectory(TEST_SUB_DIRECTORY + "/" + individualTestSubDirectory);
    }
}
