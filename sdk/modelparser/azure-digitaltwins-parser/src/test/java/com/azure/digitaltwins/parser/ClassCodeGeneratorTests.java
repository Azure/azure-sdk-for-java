// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser;

import com.azure.digitaltwins.parser.implementation.codegen.*;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Tests code generation for {@link JavaClass}.
 * Note that The tests make the following assumptions:
 * - Each test has a field called: "typeName" which will dictate the name of each class.
 * - Each test expects an existing file with the "typeName" and ".expected" extension in the src/test/resources/ClassTestResources
 * - Each test will have a code comment with the path to the target files.
 * - Each test generates a file with ".temp.generated" extension. generated files will be deleted after the test pass.
 * - In case of test failure, the generated file will remain in the directory for further inspection.
 */
public class ClassCodeGeneratorTests extends GeneratedCodeCompareBase {

    private static final String TEST_SUB_DIRECTORY = "ClassTestResources";

    /**
     * Tests an empty public java class with no class comments.
     * Find the expected output in "src/test/resources/ClassTestResources/EmptyPublicClass.expected"
     * Find the expected output in "src/test/resources/ClassTestResources/EmptyPublicClass.temp.expected"
     *
     * @throws IOException IOException.
     */
    @Test
    public void emptyPublicClass() throws IOException {
        final String typeName = "EmptyPublicClass";

        CodeWriter codeWriter = this.getCodeWriter(TEST_SUB_DIRECTORY, typeName);

        JavaClass javaClass = new JavaClass(
            Access.PUBLIC,
            Novelty.NORMAL,
            typeName,
            Multiplicity.INSTANCE,
            null,
            null);

        javaClass.generateCode(codeWriter);

        codeWriter.close();

        this.compareGeneratedCodeWithExpected(TEST_SUB_DIRECTORY, typeName);
    }

    /**
     * Tests an empty package private java class with no class comments.
     * Find the expected output in "src/test/resources/ClassTestResources/EmptyPackagePrivateClass.expected"
     * Find the generated file in "src/test/resources/ClassTestResources/EmptyPackagePrivateClass.temp.generated"
     *
     * @throws IOException IOException.
     */
    @Test
    public void emptyPackagePrivateClass() throws IOException {
        final String typeName = "EmptyPackagePrivateClass";

        CodeWriter codeWriter = this.getCodeWriter(TEST_SUB_DIRECTORY, typeName);

        JavaClass javaClass = new JavaClass(
            Access.PACKAGE_PRIVATE,
            Novelty.NORMAL,
            typeName,
            Multiplicity.INSTANCE,
            null,
            null);

        javaClass.generateCode(codeWriter);

        codeWriter.close();

        this.compareGeneratedCodeWithExpected(TEST_SUB_DIRECTORY, typeName);
    }

    /**
     * Tests an empty package private java class with no class comments.
     * Find the expected output in "src/test/resources/ClassTestResources/EmptyPublicAbstractClass.expected"
     * Find the generated file in "src/test/resources/ClassTestResources/EmptyPublicAbstractClass.temp.generated"
     *
     * @throws IOException IOException.
     */
    @Test
    public void emptyPublicAbstractClass() throws IOException {
        final String typeName = "EmptyPublicAbstractClass";

        CodeWriter codeWriter = this.getCodeWriter(TEST_SUB_DIRECTORY, typeName);

        JavaClass javaClass = new JavaClass(
            Access.PUBLIC,
            Novelty.ABSTRACT,
            typeName,
            Multiplicity.INSTANCE,
            null,
            null);

        javaClass.generateCode(codeWriter);

        codeWriter.close();

        this.compareGeneratedCodeWithExpected(TEST_SUB_DIRECTORY, typeName);
    }

    /**
     * Tests an empty public static java class with no class comments.
     * Find the expected output in "src/test/resources/ClassTestResources/EmptyPublicStaticClass.expected"
     * Find the generated file in "src/test/resources/ClassTestResources/EmptyPublicStaticClass.temp.generated"
     *
     * @throws IOException IOException.
     */
    @Test
    public void emptyPublicStaticClass() throws IOException {
        final String typeName = "EmptyPublicStaticClass";

        CodeWriter codeWriter = this.getCodeWriter(TEST_SUB_DIRECTORY, typeName);

        JavaClass javaClass = new JavaClass(
            Access.PUBLIC,
            Novelty.NORMAL,
            typeName,
            Multiplicity.STATIC,
            null,
            null);

        javaClass.generateCode(codeWriter);

        codeWriter.close();

        this.compareGeneratedCodeWithExpected(TEST_SUB_DIRECTORY, typeName);
    }

    /**
     * Tests an empty public java class with no class comments which extends a type.
     * Find the expected output in "src/test/resources/ClassTestResources/EmptyPublicExtendsClass.expected"
     * Find the generated file in "src/test/resources/ClassTestResources/EmptyPublicExtendsClass.temp.generated"
     *
     * @throws IOException IOException.
     */
    @Test
    public void emptyPublicExtendsClass() throws IOException {
        final String typeName = "EmptyPublicExtendsClass";

        CodeWriter codeWriter = this.getCodeWriter(TEST_SUB_DIRECTORY, typeName);

        JavaClass javaClass = new JavaClass(
            Access.PUBLIC,
            Novelty.NORMAL,
            typeName,
            Multiplicity.INSTANCE,
            "TestType",
            null);

        javaClass.generateCode(codeWriter);

        codeWriter.close();

        this.compareGeneratedCodeWithExpected(TEST_SUB_DIRECTORY, typeName);
    }
}
