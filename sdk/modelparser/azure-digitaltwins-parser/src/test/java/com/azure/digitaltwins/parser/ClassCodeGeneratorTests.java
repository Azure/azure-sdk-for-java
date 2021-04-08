// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser;

import com.azure.digitaltwins.parser.implementation.codegen.*;
import org.junit.jupiter.api.Assertions;
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

    // POSITIVE TEST CASES.

    /**
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

    /**
     * Find the expected output in "src/test/resources/ClassTestResources/EmptyPublicImplementsClass.expected"
     * Find the generated file in "src/test/resources/ClassTestResources/EmptyPublicImplementsClass.temp.generated"
     *
     * @throws IOException IOException.
     */
    @Test
    public void emptyPublicImplementsClass() throws IOException {
        final String typeName = "EmptyPublicImplementsClass";

        CodeWriter codeWriter = this.getCodeWriter(TEST_SUB_DIRECTORY, typeName);

        JavaClass javaClass = new JavaClass(
            Access.PUBLIC,
            Novelty.NORMAL,
            typeName,
            Multiplicity.INSTANCE,
            null,
            "TestType");

        javaClass.generateCode(codeWriter);

        codeWriter.close();

        this.compareGeneratedCodeWithExpected(TEST_SUB_DIRECTORY, typeName);
    }

    /**
     * Find the expected output in "src/test/resources/ClassTestResources/EmptyPublicExtendsImplementsClass.expected"
     * Find the generated file in "src/test/resources/ClassTestResources/EmptyPublicExtendsImplementsClass.temp.generated"
     *
     * @throws IOException IOException.
     */
    @Test
    public void emptyPublicImplementsAndExtendsClass() throws IOException {
        final String typeName = "EmptyPublicExtendsImplementsClass";

        CodeWriter codeWriter = this.getCodeWriter(TEST_SUB_DIRECTORY, typeName);

        JavaClass javaClass = new JavaClass(
            Access.PUBLIC,
            Novelty.NORMAL,
            typeName,
            Multiplicity.INSTANCE,
            "TestType",
            "TestType");

        javaClass.generateCode(codeWriter);

        codeWriter.close();

        this.compareGeneratedCodeWithExpected(TEST_SUB_DIRECTORY, typeName);
    }

    /**
     * Find the expected output in "src/test/resources/ClassTestResources/EmptyPublicClassWithCodeComments.expected"
     * Find the generated file in "src/test/resources/ClassTestResources/EmptyPublicClassWithCodeComments.temp.generated"
     *
     * @throws IOException IOException.
     */
    @Test
    public void emptyPublicClassWithCodeComments() throws IOException {
        final String typeName = "EmptyPublicClassWithCodeComments";

        CodeWriter codeWriter = this.getCodeWriter(TEST_SUB_DIRECTORY, typeName);

        JavaClass javaClass = new JavaClass(
            Access.PUBLIC,
            Novelty.NORMAL,
            typeName,
            Multiplicity.INSTANCE,
            null,
            null);

        javaClass.addSummary("This is code comments.");
        javaClass.addSummary("This is another line of comments.");

        javaClass.generateCode(codeWriter);

        codeWriter.close();

        this.compareGeneratedCodeWithExpected(TEST_SUB_DIRECTORY, typeName);
    }

    /**
     * Find the expected output in "src/test/resources/ClassTestResources/EmptyPublicClassWithCodeCommentsAndRemarks.expected"
     * Find the generated file in "src/test/resources/ClassTestResources/EmptyPublicClassWithCodeCommentsAndRemarks.temp.generated"
     *
     * @throws IOException IOException.
     */
    @Test
    public void emptyPublicClassWithCodeCommentsAndRemarks() throws IOException {
        final String typeName = "EmptyPublicClassWithCodeCommentsAndRemarks";

        CodeWriter codeWriter = this.getCodeWriter(TEST_SUB_DIRECTORY, typeName);

        JavaClass javaClass = new JavaClass(
            Access.PUBLIC,
            Novelty.NORMAL,
            typeName,
            Multiplicity.INSTANCE,
            null,
            null);

        javaClass.addSummary("This is code comments.");
        javaClass.addSummary("This is another line of comments.");
        javaClass.addRemarks("This is remarks.");
        javaClass.addRemarks("This is more remarks.");

        javaClass.generateCode(codeWriter);

        codeWriter.close();

        this.compareGeneratedCodeWithExpected(TEST_SUB_DIRECTORY, typeName);
    }

    /**
     * Find the expected output in "src/test/resources/ClassTestResources/PublicClassWithSimpleConstructor.expected"
     * Find the generated file in "src/test/resources/ClassTestResources/PublicClassWithSimpleConstructor.temp.generated"
     *
     * @throws IOException IOException.
     */
    @Test
    public void publicClassWithSimpleConstructor() throws IOException {
        final String typeName = "PublicClassWithSimpleConstructor";

        CodeWriter codeWriter = this.getCodeWriter(TEST_SUB_DIRECTORY, typeName);

        JavaClass javaClass = new JavaClass(
            Access.PUBLIC,
            Novelty.NORMAL,
            typeName,
            Multiplicity.INSTANCE,
            null,
            null);

        JavaConstructor constructor = javaClass.addConstructor(Access.PUBLIC, Multiplicity.INSTANCE);

        javaClass.generateCode(codeWriter);

        codeWriter.close();

        this.compareGeneratedCodeWithExpected(TEST_SUB_DIRECTORY, typeName);
    }

    /**
     * Find the expected output in "src/test/resources/ClassTestResources/PublicClassWithSimpleConstructorWithRemarks.expected"
     * Find the generated file in "src/test/resources/ClassTestResources/PublicClassWithSimpleConstructorWithRemarks.temp.generated"
     *
     * @throws IOException IOException.
     */
    @Test
    public void publicClassWithSimpleConstructorWithRemarks() throws IOException {
        final String typeName = "PublicClassWithSimpleConstructorWithRemarks";

        CodeWriter codeWriter = this.getCodeWriter(TEST_SUB_DIRECTORY, typeName);

        JavaClass javaClass = new JavaClass(
            Access.PUBLIC,
            Novelty.NORMAL,
            typeName,
            Multiplicity.INSTANCE,
            null,
            null);

        JavaConstructor constructor = javaClass.addConstructor(Access.PUBLIC, Multiplicity.INSTANCE);
        constructor.addSummary("This is more information.");
        constructor.addSummary("This is even more information.");
        constructor.addRemarks("This is remarks.");
        javaClass.generateCode(codeWriter);

        codeWriter.close();

        this.compareGeneratedCodeWithExpected(TEST_SUB_DIRECTORY, typeName);
    }

    /**
     * Find the expected output in "src/test/resources/ClassTestResources/PublicClassWithConstructorAndOneParam.expected"
     * Find the generated file in "src/test/resources/ClassTestResources/PublicClassWithConstructorAndOneParam.temp.generated"
     *
     * @throws IOException IOException.
     */
    @Test
    public void publicClassWithConstructorAndParam() throws IOException {
        final String typeName = "PublicClassWithConstructorAndOneParam";

        CodeWriter codeWriter = this.getCodeWriter(TEST_SUB_DIRECTORY, typeName);

        JavaClass javaClass = new JavaClass(
            Access.PUBLIC,
            Novelty.NORMAL,
            typeName,
            Multiplicity.INSTANCE,
            null,
            null);

        JavaConstructor constructor = javaClass.addConstructor(Access.PUBLIC, Multiplicity.INSTANCE);
        constructor.addSummary("This is more information.");
        constructor.addSummary("This is even more information.");
        constructor.addRemarks("This is remarks.");

        constructor.param("String", "firstParam", "This is my first parameter.");
        javaClass.generateCode(codeWriter);

        codeWriter.close();

        this.compareGeneratedCodeWithExpected(TEST_SUB_DIRECTORY, typeName);
    }

    // NEGATIVE TEST CASES.

    /**
     * No file will be generated.
     */
    @Test
    public void codeCommentsMissingPeriod() {
        final String typeName = "NoOpType";

        JavaClass javaClass = new JavaClass(
            Access.PUBLIC,
            Novelty.NORMAL,
            typeName,
            Multiplicity.INSTANCE,
            null,
            null);

        Assertions.assertThrows(StyleException.class, () -> javaClass.addSummary("This is code comments with no period"));
        Assertions.assertThrows(StyleException.class, () -> javaClass.addRemarks("This is code remarks with no period"));
    }
}
