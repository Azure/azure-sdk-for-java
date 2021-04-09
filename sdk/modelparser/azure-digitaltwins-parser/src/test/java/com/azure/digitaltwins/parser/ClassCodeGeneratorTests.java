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

        JavaConstructor constructor = javaClass.addConstructor(Access.PUBLIC, Multiplicity.INSTANCE);
        constructor.addSummary("This is more information.");
        constructor.addSummary("This is even more information.");
        constructor.addRemarks("This is remarks.");

        constructor.param("String", "firstParam", "This is my first parameter.");
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

        JavaConstructor constructor = javaClass.addConstructor(Access.PUBLIC, Multiplicity.INSTANCE);
        constructor.addSummary("This is more information.");
        constructor.addSummary("This is even more information.");
        constructor.addRemarks("This is remarks.");

        constructor.param("String", "firstParam", "This is my first parameter.");
        constructor.param("int", "secondParam", "This is my second parameter.");
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

        javaClass.addField(Access.PRIVATE, "String", "FIELD_1", "\"default string value\"", Multiplicity.STATIC, Mutability.FINAL, "field 1 description.");
        javaClass.addField(Access.PRIVATE, "String", "field2", null, Multiplicity.INSTANCE, Mutability.MUTABLE, "field 2 description.");
        javaClass.addField(Access.PRIVATE, "int", "field3", "2", Multiplicity.INSTANCE, Mutability.MUTABLE, "field 3 description.");
        javaClass.addField(Access.PRIVATE, "boolean", "field4", "false", Multiplicity.INSTANCE, Mutability.MUTABLE, "field 4 description.");
        javaClass.addField(Access.PRIVATE, "String", "field5", "\"default string value\"", Multiplicity.INSTANCE, Mutability.MUTABLE, "field 5 description.");

        javaClass.addConstructor(Access.PUBLIC, Multiplicity.INSTANCE);

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

        javaClass.addField(Access.PRIVATE, "String", "field1", null, Multiplicity.INSTANCE, Mutability.MUTABLE, "field 1 description.");

        JavaConstructor constructor = javaClass.addConstructor(Access.PUBLIC, Multiplicity.INSTANCE);
        constructor.addSummary("This is more information.");
        constructor.addSummary("This is even more information.");
        constructor.addRemarks("This is remarks.");

        constructor.param("String", "firstParam", "This is my first parameter.");
        constructor.param("int", "secondParam", "This is my second parameter.");

        JavaScope body = new JavaScope(null);
        body.addStatement(new JavaLine("this.field1 = firstParam;"));
        constructor.setBody(body);

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

        javaClass.addMethod(Access.PUBLIC, Novelty.NORMAL, "String", "getNothing", Multiplicity.INSTANCE);
        javaClass.addMethod(Access.PRIVATE, Novelty.NORMAL, "int", "getNothing2", Multiplicity.STATIC);
        javaClass.addMethod(Access.PRIVATE, Novelty.ABSTRACT, "void", "getNothing3", Multiplicity.INSTANCE);

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

        JavaMethod javaMethod = javaClass.addMethod(Access.PUBLIC, Novelty.NORMAL, "String", "getNothing", Multiplicity.INSTANCE);
        javaMethod.addSummary("This is method summary.");
        javaMethod.addRemarks("This is remarks.");
        javaMethod.param("String", "name", "Name of the thing.");
        javaMethod.param("int", "howMany", null);

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

        JavaMethod javaMethod = javaClass.addMethod(Access.PUBLIC, Novelty.NORMAL, "String", "getNothing", Multiplicity.INSTANCE);
        javaMethod.addSummary("This is method summary.");
        javaMethod.addRemarks("This is remarks.");
        javaMethod.typeParam("T", "Generic Type of the thing.");
        javaMethod.param("T", "name", "Name of the thing.");
        javaMethod.param("int", "howMany", "Really, how many.");

        JavaMethod javaMethod2 = javaClass.addMethod(Access.PUBLIC, Novelty.NORMAL, "int", "getNothing2", Multiplicity.INSTANCE);
        javaMethod2.addSummary("This is method 2 summary.");
        javaMethod2.addRemarks("This is remarks.");
        javaMethod2.typeParam("T", "Generic Type of the thing.");
        javaMethod2.typeParam("T2", "2nd generic Type of the thing.");
        javaMethod2.param("T", "name", "Name of the thing.");
        javaMethod2.param("T2", "howMany", "Really, how many.");

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

        JavaMethod javaMethod = javaClass.addMethod(Access.PUBLIC, Novelty.NORMAL, "String", "toUpper", Multiplicity.STATIC);
        javaMethod.addSummary("Converts string to upper case.");
        javaMethod.param("String", "input", "String to convert case.");
        javaMethod.returns("Input in upper case.");
        JavaScope body = new JavaScope(null);

        JavaIf ifStatement = body.addIf("input != null");

        ifStatement.line("return input.toUpperCase();");
        body.line("return null;");

        javaMethod.setBody(body);

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

        JavaMethod javaMethod = javaClass.addMethod(Access.PUBLIC, Novelty.NORMAL, "String", "toUpper", Multiplicity.STATIC);
        javaMethod.addSummary("Converts string to upper case.");
        javaMethod.param("String", "input", "String to convert case.");
        javaMethod.returns("Input in upper case.");
        JavaScope body = new JavaScope(null);

        JavaIf ifStatement = body.addIf("input != null");
        JavaElse elseStatment = ifStatement.addElse();
        elseStatment.line("return input;");
        ifStatement.line("return input.toUpperCase();");
        body.line("return null;");

        javaMethod.setBody(body);

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

        JavaMethod javaMethod = javaClass.addMethod(Access.PUBLIC, Novelty.NORMAL, "String", "toUpper", Multiplicity.STATIC);
        javaMethod.addSummary("Converts string to upper case.");
        javaMethod.param("String", "input", "String to convert case.");
        javaMethod.returns("Input in upper case.");
        JavaScope body = new JavaScope(null);

        JavaIf ifStatement = body.addIf("input != null");
        JavaIf oneNestedIf = ifStatement.addIf("input.startsWith(\"a\")");
        oneNestedIf.line("return input.toUpperCase();");
        JavaIf twoNestedIf = oneNestedIf.addIf("input.startsWith(\"ab\")");
        twoNestedIf.line("return null;");

        JavaElse elseStatment = ifStatement.addElse();
        elseStatment.line("return input;");
        ifStatement.line("return input.toUpperCase();");
        body.line("return null;");

        javaMethod.setBody(body);

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

        JavaMethod javaMethod = javaClass.addMethod(Access.PUBLIC, Novelty.NORMAL, "String", "toUpper", Multiplicity.STATIC);
        javaMethod.addSummary("Converts string to upper case.");
        javaMethod.param("String", "input", "String to convert case.");
        javaMethod.returns("Input in upper case.");
        JavaScope body = new JavaScope(null);

        JavaIf ifStatement = body.addIf("input != null");
        ifStatement.line("return input.toUpperCase();");

        JavaElseIf javaElseIf = ifStatement.addElseIf("input.startsWith(\"a\")");
        javaElseIf.line("return null;");

        JavaElse javaElse = javaElseIf.addElse();
        javaElse.line("return \"\";");

        body.line("return null;");

        javaMethod.setBody(body);

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
     *
     * @throws IOException IOException.
     */
    @Test
    public void publicClassWithInvalidFieldCasing() throws IOException {
        final String typeName = "NoOpType";

        JavaClass javaClass = new JavaClass(
            Access.PUBLIC,
            Novelty.NORMAL,
            typeName,
            Multiplicity.INSTANCE,
            null,
            null);

        Assertions.assertThrows(StyleException.class, () -> javaClass.addField(Access.PRIVATE, "String", "field", null, Multiplicity.STATIC, Mutability.FINAL, "field description."));
        Assertions.assertThrows(StyleException.class, () -> javaClass.addField(Access.PUBLIC, "String", "field", null, Multiplicity.INSTANCE, Mutability.FINAL, "field description."));
        Assertions.assertThrows(StyleException.class, () -> javaClass.addField(Access.PUBLIC, "String", "field", null, Multiplicity.STATIC, Mutability.MUTABLE, "field description."));
    }
}
