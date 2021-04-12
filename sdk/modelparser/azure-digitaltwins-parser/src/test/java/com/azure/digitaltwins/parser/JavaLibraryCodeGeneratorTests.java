// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser;

import com.azure.digitaltwins.parser.implementation.codegen.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Tests code generation for {@link JavaInterface}.
 * Note that The tests make the following assumptions:
 * - Each test has a field called: "typeName" which will dictate the name of each class.
 * - Each test expects an existing file with the "typeName" and ".expected" extension in the src/test/resources/LibraryTestResources .
 * - Each test will have a code comment with the path to the target files.
 * - Each test generates a file with ".temp.generated" extension. Generated files will be deleted after the test pass.
 * - In case of test failure, the generated file will remain in the directory for further inspection.
 */
public class JavaLibraryCodeGeneratorTests extends GeneratedCodeCompareBase {
    private static final String TEST_SUB_DIRECTORY = "LibraryTestResources";
    private static final String EXPECTED_FILE_DIRECTORY = "Expected";

    /**
     * Find the expected output in "src/test/resources/LibraryTestResources/Expected/"
     * Find the expected output in "src/test/resources/LibraryTestResources/"
     *
     * @throws IOException IOException.
     */
    @Test
    public void libraryWithEmptyClassAndEnumAndInterface() throws IOException {
        String classTypeName = "EmptyPublicClass";
        String enumTypeName = "MyEnum";
        String interfaceTypeName = "EmptyPublicInterface";

        JavaLibrary library = new JavaLibrary(FileHelpers.getTestResourcesDirectoryPath(TEST_SUB_DIRECTORY), "com.azure.test;");

        library.addClass(Access.PUBLIC, Novelty.NORMAL, classTypeName, Multiplicity.INSTANCE, null, null);
        library.addInterface(Access.PUBLIC, interfaceTypeName, null, null);
        library.addEnum(Access.PUBLIC, enumTypeName, true);

        library.addImportStatement("com.azure.imports;");
        library.addImportStatement("java.utils;");

        library.generate();
        super.compareDirectoryContents(TEST_SUB_DIRECTORY, EXPECTED_FILE_DIRECTORY);
    }
}
