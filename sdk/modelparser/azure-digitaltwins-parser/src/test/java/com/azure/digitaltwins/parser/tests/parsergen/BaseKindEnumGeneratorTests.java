// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.tests.parsergen;

import com.azure.digitaltwins.parser.FileHelpers;
import com.azure.digitaltwins.parser.GeneratedCodeCompareBase;
import com.azure.digitaltwins.parser.implementation.codegen.JavaLibrary;
import com.azure.digitaltwins.parser.implementation.parsergen.BaseKindEnumGenerator;
import com.azure.digitaltwins.parser.implementation.parsergen.MetamodelDigest;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class BaseKindEnumGeneratorTests extends GeneratedCodeCompareBase {
    private static final String TEST_SUB_DIRECTORY = "BaseKindEnum";
    private static final String EXPECTED_FILE_DIRECTORY = "Expected";

    @Test
    public void generateBaseKindEnum() throws IOException {
        final String testSubDirectory = "BaseKindEnumFull";
        cleanUpGeneratedCodes(testSubDirectory);

        String fullDigest = FileHelpers.getFileContentsByFileName("", "digest.json");
        MetamodelDigest digest = new MetamodelDigest(fullDigest);

        JavaLibrary library = new JavaLibrary(FileHelpers.getTestResourcesDirectoryPath(TEST_SUB_DIRECTORY + "/" + testSubDirectory), "com.azure.test;");

        BaseKindEnumGenerator baseKindEnumGenerator = new BaseKindEnumGenerator(digest.getMaterialClasses(), digest.getBaseClass());
        baseKindEnumGenerator.generateCode(library);

        library.generate();
        compareDirectoryContents(TEST_SUB_DIRECTORY + "/" + testSubDirectory, EXPECTED_FILE_DIRECTORY);
    }

    // Clean up every generated code for each test directory
    public static void cleanUpGeneratedCodes(String individualTestSubDirectory) {
        FileHelpers.deleteJavaFilesInSubDirectory(TEST_SUB_DIRECTORY + "/" + individualTestSubDirectory);
    }
}
