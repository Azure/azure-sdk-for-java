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
        final String testSubDirectory = "SimpleEmptyLibrary";
        cleanUpGeneratedCodes(testSubDirectory);

        String classTypeName = "EmptyPublicClass";
        String enumTypeName = "MyEnum";
        String interfaceTypeName = "EmptyPublicInterface";

        JavaLibrary library = new JavaLibrary(FileHelpers.getTestResourcesDirectoryPath(TEST_SUB_DIRECTORY + "/" + testSubDirectory), "com.azure.test;");

        library.addClass(Access.PUBLIC, Novelty.NORMAL, classTypeName, Multiplicity.INSTANCE, null, null);
        library.addInterface(Access.PUBLIC, interfaceTypeName, null, null);
        library.addEnum(Access.PUBLIC, enumTypeName, true);

        library.addImportStatement("com.azure.imports;");
        library.addImportStatement("java.utils;");

        library.generate();
        compareDirectoryContents(TEST_SUB_DIRECTORY + "/" + testSubDirectory, EXPECTED_FILE_DIRECTORY);
    }

    /**
     * Find the expected output in "src/test/resources/LibraryTestResources/Expected/"
     * Find the expected output in "src/test/resources/LibraryTestResources/"
     *
     * @throws IOException IOException.
     */
    @Test
    public void libraryWithComplexComponents() throws IOException {
        final String testSubDirectory = "FullIntegrationTest";
        cleanUpGeneratedCodes(testSubDirectory);

        String classTypeName = "ComplexClass";
        String enumTypeName = "ComplexEnum";
        String interfaceTypeName = "ComplexInterface";

        JavaLibrary library = new JavaLibrary(FileHelpers.getTestResourcesDirectoryPath(TEST_SUB_DIRECTORY + "/" + testSubDirectory), "com.azure.test;");

        JavaClass javaClass = library.addClass(Access.PUBLIC, Novelty.NORMAL, classTypeName, Multiplicity.INSTANCE, "SomeType", interfaceTypeName);
        JavaInterface javaInterface = library.addInterface(Access.PUBLIC, interfaceTypeName, null, "AnotherInterface");
        JavaEnum javaEnum = library.addEnum(Access.PUBLIC, enumTypeName, true);

        library.addImportStatement("com.azure.imports;");
        library.addImportStatement("java.utils;");

        createComplexJavaClass(javaClass);
        createComplexJavaInterface(javaInterface);
        createComplexJavaEnum(javaEnum);

        library.generate();
        compareDirectoryContents(TEST_SUB_DIRECTORY + "/" + testSubDirectory, EXPECTED_FILE_DIRECTORY);
    }

    private JavaClass createComplexJavaClass(JavaClass javaClass) {
        // Add summary
        javaClass.addSummary("This is a complex java class.");
        javaClass.addSummary("This class is of type {@link " + javaClass.getName() + "}.");
        javaClass.addRemarks("And it has a remarks section.");

        javaClass.addField(Access.PRIVATE, "boolean", "field1", "false", Multiplicity.INSTANCE, Mutability.MUTABLE, "This is an instance field.");
        javaClass.addField(Access.PUBLIC, "String", "FIELD_2", "Static value", Multiplicity.STATIC, Mutability.FINAL, "This is a final static field.");
        javaClass.addField(Access.PRIVATE, "String", "field3", null, Multiplicity.INSTANCE, Mutability.MUTABLE, "This is a string field.");
        javaClass.addField(Access.PRIVATE, "int", "field4", null, Multiplicity.INSTANCE, Mutability.MUTABLE, "This is an integer field.");

        JavaConstructor javaConstructor = javaClass.addConstructor(Access.PUBLIC, Multiplicity.INSTANCE);
        javaConstructor.addSummary("We already know what this does.");
        javaConstructor
            .addParameter("String", "input1", "Some text to initialize the class with.")
            .addParameter("int", "input2", "Some number to do things to.");

        JavaScope constructorBody = new JavaScope(null);
        constructorBody.line("this.field1 = true;")
            .line("this.field3 = input1;")
            .line("this.field4 = input2;");

        javaConstructor.setBody(constructorBody);

        javaClass.addProperty(Access.PUBLIC, "String", "propertyNumberOne");
        javaClass.addProperty(Access.PROTECTED, "int", "propertyNumberTwo");


        JavaMethod method1 = javaClass.addMethod(Access.PUBLIC, Novelty.NORMAL, javaClass.getType(), "getCurrentObject", Multiplicity.INSTANCE);
        method1
            .addReturnComment("This object.")
            .addSummary("This method returns this object.")
            .addSummary("This class it totally pointless.")
            .addRemarks("And it has some remarks.");

        JavaScope method1Body = new JavaScope(null);
        method1.setBody(method1Body.line("return this;"));

        JavaMethod method2 = javaClass.addMethod(Access.PROTECTED, Novelty.NORMAL, "int", "getHashCode", Multiplicity.INSTANCE);
        method2
            .addReturnComment("The object hashcode.")
            .addSummary("This method returns this object's hashcode.")
            .addSummary("This class it totally pointless.")
            .addRemarks("And it has some remarks.");

        JavaScope method2Body = new JavaScope(null);
        method2.setBody(method2Body.line("return this.hashCode();"));

        JavaMethod method3 = javaClass.addMethod(Access.PROTECTED, Novelty.NORMAL, "int", "countChars", Multiplicity.INSTANCE);
        method3.addAttributes("Override");
        method3.addParameter("String", "input", "The String to count.");
        method3.setInheritDoc(true);

        JavaScope method3Body = new JavaScope(null);
        JavaTry javaTry = method3Body.addTry();
        javaTry.addIf("input == null").line("throw new ArgumentNullException();");
        javaTry.line("return input.size();");
        javaTry.addCatch("ArgumentException ex").line("return 0;");
        method3.setBody(method3Body);


        JavaMethod method4 = javaClass.addMethod(Access.PUBLIC, Novelty.NORMAL, "void", "printHashCode", Multiplicity.INSTANCE);
        method4.setInheritDoc(true);
        method4.addAttributes("Override");
        JavaScope method4Body = new JavaScope(null);
        method4.setBody(method4Body.line("System.out.println(this.getCurrentObject().getHashCode());"));
        return javaClass;
    }

    private JavaInterface createComplexJavaInterface(JavaInterface javaInterface) {
        JavaMethod javaMethod = javaInterface.method(Access.PROTECTED, "int", "countChars").addParameter("String", "input", "The String to count.");
        javaMethod.addSummary("Count characters in a string.");

        JavaMethod javaMethod2 = javaInterface.method(Access.PUBLIC, "void", "printHashCode");
        javaMethod2.addSummary("Print the object hashcode.");

        return javaInterface;
    }

    private JavaEnum createComplexJavaEnum(JavaEnum javaEnum) {
        javaEnum.addSummary("Some not really useful enum.");
        javaEnum.value("RANDOM_THING_1", "Something that is random and 1.");
        javaEnum.value("RANDOM_THING_2", "Something that is random and 2.");
        javaEnum.value("RANDOM_THING_3", "Something that is random and 3.");
        return javaEnum;
    }

    // Clean up every generated code for each test directory
    public static void cleanUpGeneratedCodes(String individualTestSubDirectory) {
        FileHelpers.deleteJavaFilesInSubDirectory(TEST_SUB_DIRECTORY + "/" + individualTestSubDirectory);
    }
}
