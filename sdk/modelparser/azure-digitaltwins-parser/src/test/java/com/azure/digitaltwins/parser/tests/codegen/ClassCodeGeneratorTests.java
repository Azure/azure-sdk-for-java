// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser.tests.codegen;

import com.azure.digitaltwins.parser.GeneratedCodeCompareBase;
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
 * - Each test generates a file with ".temp.generated" extension. Generated files will be deleted after the test pass.
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

        JavaConstructor constructor = javaClass.constructor(Access.PUBLIC, Multiplicity.INSTANCE);

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

        JavaConstructor constructor = javaClass.constructor(Access.PUBLIC, Multiplicity.INSTANCE);
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
    public void publicClassWithConstructorAndOneParam() throws IOException {
        final String typeName = "PublicClassWithConstructorAndOneParam";

        CodeWriter codeWriter = this.getCodeWriter(TEST_SUB_DIRECTORY, typeName);

        JavaClass javaClass = new JavaClass(
            Access.PUBLIC,
            Novelty.NORMAL,
            typeName,
            Multiplicity.INSTANCE,
            null,
            null);

        JavaConstructor constructor = javaClass.constructor(Access.PUBLIC, Multiplicity.INSTANCE);
        constructor.parameter("String", "firstParam", "This is my first parameter.");

        constructor.addSummary("This is more information.");
        constructor.addSummary("This is even more information.");
        constructor.addRemarks("This is remarks.");

        javaClass.generateCode(codeWriter);
        codeWriter.close();

        this.compareGeneratedCodeWithExpected(TEST_SUB_DIRECTORY, typeName);
    }

    /**
     * Find the expected output in "src/test/resources/ClassTestResources/PublicClassWithConstructorAndTwoParams.expected"
     * Find the generated file in "src/test/resources/ClassTestResources/PublicClassWithConstructorAndTwoParams.temp.generated"
     *
     * @throws IOException IOException.
     */
    @Test
    public void publicClassWithConstructorAndTwoParams() throws IOException {
        final String typeName = "PublicClassWithConstructorAndTwoParams";

        CodeWriter codeWriter = this.getCodeWriter(TEST_SUB_DIRECTORY, typeName);

        JavaClass javaClass = new JavaClass(
            Access.PUBLIC,
            Novelty.NORMAL,
            typeName,
            Multiplicity.INSTANCE,
            null,
            null);

        JavaConstructor constructor = javaClass.constructor(Access.PUBLIC, Multiplicity.INSTANCE);
        constructor.parameter("String", "firstParam", "This is my first parameter.");
        constructor.parameter("int", "secondParam", "This is my second parameter.");

        constructor.addSummary("This is more information.");
        constructor.addSummary("This is even more information.");
        constructor.addRemarks("This is remarks.");

        javaClass.generateCode(codeWriter);

        codeWriter.close();

        this.compareGeneratedCodeWithExpected(TEST_SUB_DIRECTORY, typeName);
    }

    /**
     * Find the expected output in "src/test/resources/ClassTestResources/PublicClassWithFields.expected"
     * Find the generated file in "src/test/resources/ClassTestResources/PublicClassWithFields.temp.generated"
     *
     * @throws IOException IOException.
     */
    @Test
    public void publicClassWithFields() throws IOException {
        final String typeName = "PublicClassWithFields";

        CodeWriter codeWriter = this.getCodeWriter(TEST_SUB_DIRECTORY, typeName);

        JavaClass javaClass = new JavaClass(
            Access.PUBLIC,
            Novelty.NORMAL,
            typeName,
            Multiplicity.INSTANCE,
            null,
            null);

        javaClass.field(Access.PRIVATE, "String", "FIELD_1", "\"default string value\"", Multiplicity.STATIC, Mutability.FINAL, "field 1 description.");
        javaClass.field(Access.PRIVATE, "String", "field2", null, Multiplicity.INSTANCE, Mutability.MUTABLE, "field 2 description.");
        javaClass.field(Access.PRIVATE, "int", "field3", "2", Multiplicity.INSTANCE, Mutability.MUTABLE, "field 3 description.");
        javaClass.field(Access.PRIVATE, "boolean", "field4", "false", Multiplicity.INSTANCE, Mutability.MUTABLE, "field 4 description.");
        javaClass.field(Access.PRIVATE, "String", "field5", "\"default string value\"", Multiplicity.INSTANCE, Mutability.MUTABLE, "field 5 description.");

        javaClass.constructor(Access.PUBLIC, Multiplicity.INSTANCE);

        javaClass.generateCode(codeWriter);

        codeWriter.close();

        this.compareGeneratedCodeWithExpected(TEST_SUB_DIRECTORY, typeName);
    }

    /**
     * Find the expected output in "src/test/resources/ClassTestResources/PublicClassWithConstructorAndSimpleBody.expected"
     * Find the generated file in "src/test/resources/ClassTestResources/PublicClassWithConstructorAndSimpleBody.temp.generated"
     *
     * @throws IOException IOException.
     */
    @Test
    public void publicClassWithConstructorAndSimpleBody() throws IOException {
        final String typeName = "PublicClassWithConstructorAndSimpleBody";

        CodeWriter codeWriter = this.getCodeWriter(TEST_SUB_DIRECTORY, typeName);

        JavaClass javaClass = new JavaClass(
            Access.PUBLIC,
            Novelty.NORMAL,
            typeName,
            Multiplicity.INSTANCE,
            null,
            null);

        javaClass.field(Access.PRIVATE, "String", "field1", null, Multiplicity.INSTANCE, Mutability.MUTABLE, "field 1 description.");

        JavaConstructor constructor = javaClass.constructor(Access.PUBLIC, Multiplicity.INSTANCE);

        constructor.parameter("String", "firstParam", "This is my first parameter.");
        constructor.parameter("int", "secondParam", "This is my second parameter.");

        constructor.addSummary("This is more information.");
        constructor.addSummary("This is even more information.");
        constructor.addRemarks("This is remarks.");

        JavaScope body = new JavaScope(null);
        body.addStatement(new JavaLine("this.field1 = firstParam;"));
        constructor.body(body);

        javaClass.generateCode(codeWriter);
        codeWriter.close();

        this.compareGeneratedCodeWithExpected(TEST_SUB_DIRECTORY, typeName);
    }

    /**
     * Find the expected output in "src/test/resources/ClassTestResources/PublicClassWithSimpleEmptyMethods.expected"
     * Find the generated file in "src/test/resources/ClassTestResources/PublicClassWithSimpleEmptyMethods.temp.generated"
     *
     * @throws IOException IOException.
     */
    @Test
    public void publicClassWithEmptyMethods() throws IOException {
        final String typeName = "PublicClassWithSimpleEmptyMethods";

        CodeWriter codeWriter = this.getCodeWriter(TEST_SUB_DIRECTORY, typeName);

        JavaClass javaClass = new JavaClass(
            Access.PUBLIC,
            Novelty.NORMAL,
            typeName,
            Multiplicity.INSTANCE,
            null,
            null);

        javaClass.method(Access.PUBLIC, Novelty.NORMAL, "String", "getNothing", Multiplicity.INSTANCE);
        javaClass.method(Access.PRIVATE, Novelty.NORMAL, "int", "getNothing2", Multiplicity.STATIC);
        javaClass.method(Access.PRIVATE, Novelty.ABSTRACT, "void", "getNothing3", Multiplicity.INSTANCE);

        javaClass.generateCode(codeWriter);

        codeWriter.close();

        this.compareGeneratedCodeWithExpected(TEST_SUB_DIRECTORY, typeName);
    }

    /**
     * Find the expected output in "src/test/resources/ClassTestResources/ClassWithMethodsAndParameters.expected"
     * Find the generated file in "src/test/resources/ClassTestResources/ClassWithMethodsAndParameters.temp.generated"
     *
     * @throws IOException IOException.
     */
    @Test
    public void publicClassWithMethodsAndParameters() throws IOException {
        final String typeName = "ClassWithMethodsAndParameters";

        CodeWriter codeWriter = this.getCodeWriter(TEST_SUB_DIRECTORY, typeName);

        JavaClass javaClass = new JavaClass(
            Access.PUBLIC,
            Novelty.NORMAL,
            typeName,
            Multiplicity.INSTANCE,
            null,
            null);

        JavaMethod javaMethod = javaClass.method(Access.PUBLIC, Novelty.NORMAL, "String", "getNothing", Multiplicity.INSTANCE)
            .parameter("String", "name", "Name of the thing.")
            .parameter("int", "howMany", null);
        javaMethod.addSummary("This is method summary.");
        javaMethod.addRemarks("This is remarks.");

        javaClass.generateCode(codeWriter);
        codeWriter.close();

        this.compareGeneratedCodeWithExpected(TEST_SUB_DIRECTORY, typeName);
    }

    /**
     * Find the expected output in "src/test/resources/ClassTestResources/ClassWithParametersAndTypeParametersMethods.expected"
     * Find the generated file in "src/test/resources/ClassTestResources/ClassWithParametersAndTypeParametersMethods.temp.generated"
     *
     * @throws IOException IOException.
     */
    @Test
    public void publicClassWithParametersAndTypeParametersMethods() throws IOException {
        final String typeName = "ClassWithParametersAndTypeParametersMethods";

        CodeWriter codeWriter = this.getCodeWriter(TEST_SUB_DIRECTORY, typeName);

        JavaClass javaClass = new JavaClass(
            Access.PUBLIC,
            Novelty.NORMAL,
            typeName,
            Multiplicity.INSTANCE,
            null,
            null);

        JavaMethod javaMethod = javaClass.method(Access.PUBLIC, Novelty.NORMAL, "String", "getNothing", Multiplicity.INSTANCE)
            .typeParam("T", "Generic Type of the thing.")
            .parameter("T", "name", "Name of the thing.")
            .parameter("int", "howMany", "Really, how many.");

        javaMethod.addSummary("This is method summary.");
        javaMethod.addRemarks("This is remarks.");

        JavaMethod javaMethod2 = javaClass.method(Access.PUBLIC, Novelty.NORMAL, "int", "getNothing2", Multiplicity.INSTANCE)
            .typeParam("T", "Generic Type of the thing.")
            .typeParam("T2", "2nd generic Type of the thing.")
            .parameter("T", "name", "Name of the thing.")
            .parameter("T2", "howMany", "Really, how many.");

        javaMethod2.addSummary("This is method 2 summary.");
        javaMethod2.addRemarks("This is remarks.");

        javaClass.generateCode(codeWriter);
        codeWriter.close();

        this.compareGeneratedCodeWithExpected(TEST_SUB_DIRECTORY, typeName);
    }

    /**
     * Find the expected output in "src/test/resources/ClassTestResources/ClassWithMethodsIfStatement.expected"
     * Find the generated file in "src/test/resources/ClassTestResources/ClassWithMethodsIfStatement.temp.generated"
     *
     * @throws IOException IOException.
     */
    @Test
    public void publicClassWithMethodsIfStatement() throws IOException {
        final String typeName = "ClassWithMethodsIfStatement";

        CodeWriter codeWriter = this.getCodeWriter(TEST_SUB_DIRECTORY, typeName);

        JavaClass javaClass = new JavaClass(
            Access.PUBLIC,
            Novelty.NORMAL,
            typeName,
            Multiplicity.INSTANCE,
            null,
            null);

        JavaMethod javaMethod = javaClass.method(Access.PUBLIC, Novelty.NORMAL, "String", "toUpper", Multiplicity.STATIC)
            .parameter("String", "input", "String to convert case.")
            .addReturnComment("Input in upper case.");

        javaMethod.addSummary("Converts string to upper case.");
        JavaScope body = new JavaScope(null);

        body.jIf("input != null")
            .line("return input.toUpperCase();");

        body.line("return null;");
        javaMethod.body(body);
        javaClass.generateCode(codeWriter);

        codeWriter.close();

        this.compareGeneratedCodeWithExpected(TEST_SUB_DIRECTORY, typeName);
    }

    /**
     * Find the expected output in "src/test/resources/ClassTestResources/ClassWithMethodsIfAndElseStatements.expected"
     * Find the generated file in "src/test/resources/ClassTestResources/ClassWithMethodsIfAndElseStatements.temp.generated"
     *
     * @throws IOException IOException.
     */
    @Test
    public void publicClassWithMethodsIfAndElseStatements() throws IOException {
        final String typeName = "ClassWithMethodsIfAndElseStatements";

        CodeWriter codeWriter = this.getCodeWriter(TEST_SUB_DIRECTORY, typeName);

        JavaClass javaClass = new JavaClass(
            Access.PUBLIC,
            Novelty.NORMAL,
            typeName,
            Multiplicity.INSTANCE,
            null,
            null);

        JavaMethod javaMethod = javaClass.method(Access.PUBLIC, Novelty.NORMAL, "String", "toUpper", Multiplicity.STATIC)
            .parameter("String", "input", "String to convert case.")
            .addReturnComment("Input in upper case.");

        javaMethod.addSummary("Converts string to upper case.");

        JavaScope body = new JavaScope(null);

        JavaIf ifStatement = body.jIf("input != null").line("return input.toUpperCase();");

        ifStatement.jElse()
            .line("return input;");

        body.line("return null;");

        javaMethod.body(body);
        javaClass.generateCode(codeWriter);
        codeWriter.close();

        this.compareGeneratedCodeWithExpected(TEST_SUB_DIRECTORY, typeName);
    }

    /**
     * Find the expected output in "src/test/resources/ClassTestResources/ClassWithMethodsWithNestedIf.expected"
     * Find the generated file in "src/test/resources/ClassTestResources/ClassWithMethodsWithNestedIf.temp.generated"
     *
     * @throws IOException IOException.
     */
    @Test
    public void publicClassWithMethodsWithNestedIf() throws IOException {
        final String typeName = "ClassWithMethodsWithNestedIf";

        CodeWriter codeWriter = this.getCodeWriter(TEST_SUB_DIRECTORY, typeName);

        JavaClass javaClass = new JavaClass(
            Access.PUBLIC,
            Novelty.NORMAL,
            typeName,
            Multiplicity.INSTANCE,
            null,
            null);

        JavaMethod javaMethod = javaClass.method(Access.PUBLIC, Novelty.NORMAL, "String", "toUpper", Multiplicity.STATIC)
            .parameter("String", "input", "String to convert case.")
            .addReturnComment("Input in upper case.");

        javaMethod.addSummary("Converts string to upper case.");

        JavaScope body = new JavaScope(null);

        JavaIf ifStatement = body.jIf("input != null");
        JavaIf oneNestedIf = ifStatement
            .jIf("input.startsWith(\"a\")")
            .line("return input.toUpperCase();");

        oneNestedIf
            .jIf("input.startsWith(\"ab\")")
            .line("return null;");

        ifStatement
            .jElse()
            .line("return input;");

        ifStatement
            .line("return input.toUpperCase();");

        body.line("return null;");

        javaMethod.body(body);

        javaClass.generateCode(codeWriter);

        codeWriter.close();

        this.compareGeneratedCodeWithExpected(TEST_SUB_DIRECTORY, typeName);
    }

    /**
     * Find the expected output in "src/test/resources/ClassTestResources/ClassWithMethodsIfElseIfStatement.expected"
     * Find the generated file in "src/test/resources/ClassTestResources/ClassWithMethodsIfElseIfStatement.temp.generated"
     *
     * @throws IOException IOException.
     */
    @Test
    public void publicClassWithMethodsIfElseIfStatement() throws IOException {
        final String typeName = "ClassWithMethodsIfElseIfStatement";

        CodeWriter codeWriter = this.getCodeWriter(TEST_SUB_DIRECTORY, typeName);

        JavaClass javaClass = new JavaClass(
            Access.PUBLIC,
            Novelty.NORMAL,
            typeName,
            Multiplicity.INSTANCE,
            null,
            null);

        JavaMethod javaMethod = javaClass.method(Access.PUBLIC, Novelty.NORMAL, "String", "toUpper", Multiplicity.STATIC)
            .parameter("String", "input", "String to convert case.")
            .addReturnComment("Input in upper case.");

        javaMethod.addSummary("Converts string to upper case.");
        JavaScope body = new JavaScope(null);

        JavaIf ifStatement = body
            .jIf("input != null")
            .line("return input.toUpperCase();");

        JavaElseIf javaElseIf = ifStatement.jElseIf("input.startsWith(\"a\")")
            .line("return null;");

        JavaElse javaElse = javaElseIf
            .jElse()
            .line("return \"\";");

        body.line("return null;");

        javaMethod.body(body);

        javaClass.generateCode(codeWriter);

        codeWriter.close();

        this.compareGeneratedCodeWithExpected(TEST_SUB_DIRECTORY, typeName);
    }

    /**
     * Find the expected output in "src/test/resources/ClassTestResources/ClassWithMethodForLoop.expected"
     * Find the generated file in "src/test/resources/ClassTestResources/ClassWithMethodForLoop.temp.generated"
     *
     * @throws IOException IOException.
     */
    @Test
    public void publicClassWithMethodForLoop() throws IOException {
        final String typeName = "ClassWithMethodForLoop";

        CodeWriter codeWriter = this.getCodeWriter(TEST_SUB_DIRECTORY, typeName);

        JavaClass javaClass = new JavaClass(
            Access.PUBLIC,
            Novelty.NORMAL,
            typeName,
            Multiplicity.INSTANCE,
            null,
            null);

        JavaScope methodBody = new JavaScope(null);
        methodBody
            .jFor("int i = 0; i<10; i++")
            .line("// do nothing.");

        javaClass.method(Access.PUBLIC, Novelty.NORMAL, "void", "doNothing", Multiplicity.INSTANCE)
            .body(methodBody);

        javaClass.generateCode(codeWriter);

        codeWriter.close();

        this.compareGeneratedCodeWithExpected(TEST_SUB_DIRECTORY, typeName);
    }

    /**
     * Find the expected output in "src/test/resources/ClassTestResources/ClassWithMethodForLoopAndIf.expected"
     * Find the generated file in "src/test/resources/ClassTestResources/ClassWithMethodForLoopAndIf.temp.generated"
     *
     * @throws IOException IOException.
     */
    @Test
    public void publicClassWithMethodForLoopAndIf() throws IOException {
        final String typeName = "ClassWithMethodForLoopAndIf";

        CodeWriter codeWriter = this.getCodeWriter(TEST_SUB_DIRECTORY, typeName);

        JavaClass javaClass = new JavaClass(
            Access.PUBLIC,
            Novelty.NORMAL,
            typeName,
            Multiplicity.INSTANCE,
            null,
            null);

        JavaMethod javaMethod = javaClass.method(Access.PUBLIC, Novelty.NORMAL, "void", "doNothing", Multiplicity.INSTANCE);

        JavaScope methodBody = new JavaScope(null);
        methodBody.jFor("int i = 0; i<10; i++")
            .jIf("i == 1")
            .line("System.out.println(\"yay\");")
            .jElse()
            .line("continue;");

        javaMethod.body(methodBody);
        javaClass.generateCode(codeWriter);

        codeWriter.close();

        this.compareGeneratedCodeWithExpected(TEST_SUB_DIRECTORY, typeName);
    }

    /**
     * Find the expected output in "src/test/resources/ClassTestResources/ClassWithProperty.expected"
     * Find the generated file in "src/test/resources/ClassTestResources/ClassWithProperty.temp.generated"
     *
     * @throws IOException IOException.
     */
    @Test
    public void publicClassWithProperty() throws IOException {
        final String typeName = "ClassWithProperty";

        CodeWriter codeWriter = this.getCodeWriter(TEST_SUB_DIRECTORY, typeName);

        JavaClass javaClass = new JavaClass(
            Access.PUBLIC,
            Novelty.NORMAL,
            typeName,
            Multiplicity.INSTANCE,
            null,
            null);

        javaClass.property(Access.PUBLIC, "String", "stringProp");
        javaClass.property(Access.PROTECTED, "int", "intProp");
        javaClass.property(Access.PRIVATE, "String", "privateStringProp");

        javaClass.generateCode(codeWriter);

        codeWriter.close();

        this.compareGeneratedCodeWithExpected(TEST_SUB_DIRECTORY, typeName);
    }

    /**
     * Find the expected output in "src/test/resources/ClassTestResources/ClassWithTryCatch.expected"
     * Find the generated file in "src/test/resources/ClassTestResources/ClassWithTryCatch.temp.generated"
     *
     * @throws IOException IOException.
     */
    @Test
    public void publicClassWithTryCatch() throws IOException {
        final String typeName = "ClassWithTryCatch";

        CodeWriter codeWriter = this.getCodeWriter(TEST_SUB_DIRECTORY, typeName);

        JavaClass javaClass = new JavaClass(
            Access.PUBLIC,
            Novelty.NORMAL,
            typeName,
            Multiplicity.INSTANCE,
            null,
            null);

        JavaMethod javaMethod = javaClass
            .method(Access.PUBLIC, Novelty.NORMAL, "String", "toLower", Multiplicity.INSTANCE)
            .parameter("String", "input", "String input.");

        javaMethod.addSummary("Converts input to lowercase.");
        javaMethod.addReturnComment("Converted input in lowercase.");

        JavaScope methodBody = new JavaScope(null);
        JavaTry javaTry = methodBody.jTry();
        javaTry.jCatch("Exception ex").line("return null;");
        javaTry.jIf("input != null")
            .line("return input.toLowerCase();")
            .jElse()
            .line("return input;");

        javaMethod.body(methodBody);
        javaClass.generateCode(codeWriter);

        codeWriter.close();

        this.compareGeneratedCodeWithExpected(TEST_SUB_DIRECTORY, typeName);
    }

    /**
     * Find the expected output in "src/test/resources/ClassTestResources/ClassWithWhileLoop.expected"
     * Find the generated file in "src/test/resources/ClassTestResources/ClassWithWhileLoop.temp.generated"
     *
     * @throws IOException IOException.
     */
    @Test
    public void publicClassWithWhileLoop() throws IOException {
        final String typeName = "ClassWithWhileLoop";

        CodeWriter codeWriter = this.getCodeWriter(TEST_SUB_DIRECTORY, typeName);

        JavaClass javaClass = new JavaClass(
            Access.PUBLIC,
            Novelty.NORMAL,
            typeName,
            Multiplicity.INSTANCE,
            null,
            null);

        JavaMethod javaMethod = javaClass
            .method(Access.PUBLIC, Novelty.NORMAL, "String", "toLower", Multiplicity.INSTANCE)
            .parameter("String", "input", "String input.");

        javaMethod.addSummary("Converts input to lowercase.");
        javaMethod.addReturnComment("Converted input in lowercase.");

        JavaScope methodBody = new JavaScope(null);
        JavaWhile javaWhile = methodBody.jWhile("input.size() > 0");
        javaWhile.jIf("input != null").line("continue;");
        javaWhile.line("input = input.substring(1);");

        javaMethod.body(methodBody);
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

    /**
     * No file will be generated.
     */
    @Test
    public void typeParameterDoesNotStartWithT() {
        final String typeName = "NoOpType";
        JavaMethod javaMethod = new JavaMethod(true, Access.PUBLIC, Novelty.NORMAL, "String", "getNothing", Multiplicity.INSTANCE, Mutability.MUTABLE);

        Assertions.assertThrows(StyleException.class, () -> javaMethod.typeParam("T", "Description missing period"));
        // All generic Types should start with T
        Assertions.assertThrows(StyleException.class, () -> javaMethod.typeParam("K", null));
    }

    /**
     * No files are generated.
     */
    @Test
    public void publicClassWithInvalidFieldCasing() {
        final String typeName = "NoOpType";

        JavaClass javaClass = new JavaClass(
            Access.PUBLIC,
            Novelty.NORMAL,
            typeName,
            Multiplicity.INSTANCE,
            null,
            null);

        Assertions.assertThrows(StyleException.class, () -> javaClass.field(Access.PRIVATE, "String", "field", null, Multiplicity.STATIC, Mutability.FINAL, "field description."));
        Assertions.assertThrows(StyleException.class, () -> javaClass.field(Access.PUBLIC, "String", "field", null, Multiplicity.INSTANCE, Mutability.FINAL, "field description."));
        Assertions.assertThrows(StyleException.class, () -> javaClass.field(Access.PUBLIC, "String", "field", null, Multiplicity.STATIC, Mutability.MUTABLE, "field description."));
    }
}
