// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.digitaltwins.parser;

import com.azure.digitaltwins.parser.implementation.codegen.Access;
import com.azure.digitaltwins.parser.implementation.codegen.CodeWriter;
import com.azure.digitaltwins.parser.implementation.codegen.JavaInterface;
import com.azure.digitaltwins.parser.implementation.codegen.JavaMethod;
import org.junit.jupiter.api.Test;

import java.io.IOException;

/**
 * Tests code generation for {@link JavaInterface}.
 * Note that The tests make the following assumptions:
 * - Each test has a field called: "typeName" which will dictate the name of each class.
 * - Each test expects an existing file with the "typeName" and ".expected" extension in the src/test/resources/InterfaceTestResources
 * - Each test will have a code comment with the path to the target files.
 * - Each test generates a file with ".temp.generated" extension. Generated files will be deleted after the test pass.
 * - In case of test failure, the generated file will remain in the directory for further inspection.
 */
public class InterfaceCodeGeneratorTests extends GeneratedCodeCompareBase {
    private static final String TEST_SUB_DIRECTORY = "InterfaceTestResources";

    /**
     * Find the expected output in "src/test/resources/InterfaceTestResources/EmptyPublicInterface.expected"
     * Find the expected output in "src/test/resources/InterfaceTestResources/EmptyPublicInterface.temp.expected"
     *
     * @throws IOException IOException.
     */
    @Test
    public void emptyPublicInterface() throws IOException {
        String typeName = "EmptyPublicInterface";

        CodeWriter codeWriter = this.getCodeWriter(TEST_SUB_DIRECTORY, typeName);
        JavaInterface javaInterface = new JavaInterface(Access.PUBLIC, typeName, null, null);

        javaInterface.generateCode(codeWriter);
        codeWriter.close();

        compareGeneratedCodeWithExpected(TEST_SUB_DIRECTORY, typeName);
    }

    /**
     * Find the expected output in "src/test/resources/InterfaceTestResources/EmptyPrivateInterface.expected"
     * Find the expected output in "src/test/resources/InterfaceTestResources/EmptyPrivateInterface.temp.expected"
     *
     * @throws IOException IOException.
     */
    @Test
    public void emptyPrivateInterface() throws IOException {
        String typeName = "EmptyPrivateInterface";

        CodeWriter codeWriter = this.getCodeWriter(TEST_SUB_DIRECTORY, typeName);
        JavaInterface javaInterface = new JavaInterface(Access.PRIVATE, typeName, null, null);

        javaInterface.generateCode(codeWriter);
        codeWriter.close();

        compareGeneratedCodeWithExpected(TEST_SUB_DIRECTORY, typeName);
    }

    /**
     * Find the expected output in "src/test/resources/InterfaceTestResources/EmptyPublicInterfaceAndCodeDocs.expected"
     * Find the expected output in "src/test/resources/InterfaceTestResources/EmptyPublicInterfaceAndCodeDocs.temp.expected"
     *
     * @throws IOException IOException.
     */
    @Test
    public void emptyPublicInterfaceAndCodeDocs() throws IOException {
        String typeName = "EmptyPublicInterfaceAndCodeDocs";

        CodeWriter codeWriter = this.getCodeWriter(TEST_SUB_DIRECTORY, typeName);
        JavaInterface javaInterface = new JavaInterface(Access.PRIVATE, typeName, null, null);

        javaInterface.addSummary("This is an interface.");
        javaInterface.addSummary("This is a second line of docs.");
        javaInterface.addRemarks("This is remarks section.");

        javaInterface.generateCode(codeWriter);
        codeWriter.close();

        compareGeneratedCodeWithExpected(TEST_SUB_DIRECTORY, typeName);
    }

    /**
     * Find the expected output in "src/test/resources/InterfaceTestResources/PublicInterfaceImplementsType.expected"
     * Find the expected output in "src/test/resources/InterfaceTestResources/PublicInterfaceImplementsType.temp.expected"
     *
     * @throws IOException IOException.
     */
    @Test
    public void publicInterfaceImplementsType() throws IOException {
        String typeName = "PublicInterfaceImplementsType";

        CodeWriter codeWriter = this.getCodeWriter(TEST_SUB_DIRECTORY, typeName);
        JavaInterface javaInterface = new JavaInterface(Access.PRIVATE, typeName, null, "RandomType");

        javaInterface.addSummary("This is an interface.");
        javaInterface.addSummary("This is a second line of docs.");
        javaInterface.addRemarks("This is remarks section.");

        javaInterface.generateCode(codeWriter);
        codeWriter.close();

        compareGeneratedCodeWithExpected(TEST_SUB_DIRECTORY, typeName);
    }

    /**
     * Find the expected output in "src/test/resources/InterfaceTestResources/PublicInterfaceSingleMethod.expected"
     * Find the expected output in "src/test/resources/InterfaceTestResources/PublicInterfaceSingleMethod.temp.expected"
     *
     * @throws IOException IOException.
     */
    @Test
    public void publicInterfaceSingleMethod() throws IOException {
        String typeName = "PublicInterfaceSingleMethod";

        CodeWriter codeWriter = this.getCodeWriter(TEST_SUB_DIRECTORY, typeName);
        JavaInterface javaInterface = new JavaInterface(Access.PRIVATE, typeName, null, "RandomType");

        javaInterface.addSummary("This is an interface.");
        javaInterface.addSummary("This is a second line of docs.");
        javaInterface.addRemarks("This is remarks section.");

        javaInterface.method(Access.PUBLIC, "String", "getNothing");

        javaInterface.generateCode(codeWriter);
        codeWriter.close();

        compareGeneratedCodeWithExpected(TEST_SUB_DIRECTORY, typeName);
    }

    /**
     * Find the expected output in "src/test/resources/InterfaceTestResources/PublicInterfaceTwoMethods.expected"
     * Find the expected output in "src/test/resources/InterfaceTestResources/PublicInterfaceTwoMethods.temp.expected"
     *
     * @throws IOException IOException.
     */
    @Test
    public void publicInterfaceTwoMethods() throws IOException {
        String typeName = "PublicInterfaceTwoMethods";

        CodeWriter codeWriter = this.getCodeWriter(TEST_SUB_DIRECTORY, typeName);
        JavaInterface javaInterface = new JavaInterface(Access.PRIVATE, typeName, null, "RandomType");

        javaInterface.addSummary("This is an interface.");
        javaInterface.addSummary("This is a second line of docs.");
        javaInterface.addRemarks("This is remarks section.");

        JavaMethod method1 = javaInterface.method(Access.PUBLIC, "String", "getNothing");
        method1.addSummary("This returns nothing.");
        method1.addParameter("String", "input", "input description.");
        method1.addParameter("int", "value", "value description.");

        JavaMethod method2 = javaInterface.method(Access.PRIVATE, "int", "getSomething");
        method1.addSummary("This returns something.");
        method2.addParameter("String", "input", "input description.");
        method2.addParameter("int", "value", "value description.");

        javaInterface.generateCode(codeWriter);
        codeWriter.close();

        compareGeneratedCodeWithExpected(TEST_SUB_DIRECTORY, typeName);
    }
}
