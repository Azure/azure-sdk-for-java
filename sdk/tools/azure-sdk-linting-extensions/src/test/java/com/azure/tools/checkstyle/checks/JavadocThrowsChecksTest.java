// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.Checker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.util.Arrays;

public class JavadocThrowsChecksTest extends AbstractModuleTestSupport {
    private Checker checker;

    @BeforeEach
    public void prepare() throws Exception {
        checker = createChecker(createModuleConfig(JavadocThrowsChecks.class));
    }

    @AfterEach
    public void cleanup() {
        checker.destroy();
    }

    @Override
    protected String getPackageLocation() {
        return "com/azure/tools/checkstyle/checks/JavadocThrowsChecks";
    }

    @Test
    public void simpleThrows() throws Exception {
        File file = TestUtils.createCheckFile("simpleThrows", Arrays.asList(
            "public class SimpleThrows {",
            "    /**",
            "     * Method that doesn't list its throw.",
            "     */",
            "    public void simpleThrow() {",
            "        throw new IllegalArgumentException(); // line 6, column 9",
            "    }",
            "",
            "    public void instantiatedThrow() {",
            "        IllegalArgumentException e = new IllegalArgumentException();",
            "        throw e; // line 11, column 9",
            "    }",
            "",
            "    /**",
            "     * @throws IllegalArgumentException I documented my throw!",
            "     */",
            "    protected void simpleThrow() {",
            "        throw new IllegalArgumentException(); // No message should be logged.",
            "    }",
            "",
            "    /**",
            "     * @throws IllegalArgumentException I documented my throw!",
            "     */",
            "    public void instantiatedThrow() {",
            "        IllegalArgumentException e = new IllegalArgumentException();",
            "        //throw e; // No message should be logged. // This is being thrown as an issue since I haven't implemented tracking instantiations.",
            "    }",
            "",
            "    /**",
            "     * I state that I throw but I don't say why.",
            "     * line 32, column 16",
            "     * @throws IllegalArgumentException",
            "     */",
            "    public void noMessageThrow() {",
            "        throw new IllegalArgumentException();",
            "    }",
            "}"
        ));
        String[] expected = {
            expectedThrowsMessage(6, 9),
            expectedThrowsMessage(11, 9),
            expectedDescriptionMessage(32)
        };

        verify(checker, new File[] { file }, file.getAbsolutePath(), expected);
    }

    @Test
    public void tryCatchThrows() throws Exception {
        File file = TestUtils.createCheckFile("tryCatchThrows", Arrays.asList(
            "public class TryCatchThrows {",
            "    /**",
            "     * I don't list my throw.",
            "     */",
            "    public void invalidCatchAndThrow() {",
            "        try {",
            "            int i = 1;",
            "        } catch (IllegalAccessError e) {",
            "            throw e; // line 9, column 19",
            "        }",
            "    }",
            "",
            "    /**",
            "     * I don't list my throws.",
            "     */",
            "    public void invalidCatchAndThrowUnion() {",
            "        try {",
            "            int i = 1;",
            "        } catch (IllegalArgumentException | IllegalAccessError e) {",
            "            throw e; //line 20, columns 19",
            "        }",
            "    }",
            "",
            "    /**",
            "     * I sort of documented my exceptions.",
            "     * line 27",
            "     * @throws IllegalAccessError",
            "     */",
            "    public void invalidCatchAndThrowUnion() {",
            "        try {",
            "            int i = 1;",
            "        } catch (IllegalArgumentException | IllegalAccessError e) {",
            "            throw e; //line 33, columns 19",
            "        }",
            "    }",
            "",
            "    /**",
            "     * I sort of documented my exceptions.",
            "     *",
            "     * @throws IllegalAccessError One of my throws",
            "     */",
            "    public void anotherInvalidCatchAndThrowUnion() {",
            "        try {",
            "            int i = 1;",
            "        } catch (IllegalArgumentException | IllegalAccessError e) {",
            "            throw e; //line 46, columns 19",
            "        }",
            "    }",
            "",
            "    /**",
            "     * I list my throw.",
            "     *",
            "     * @throws IllegalAccessError My exception",
            "     */",
            "    public void validCatchAndThrow() {",
            "        try {",
            "            int i = 1;",
            "        } catch (IllegalAccessError e) {",
            "            throw e;",
            "        }",
            "    }",
            "",
            "    /**",
            "     * I list my throws.",
            "     *",
            "     * @throws IllegalAccessError One of my exceptions",
            "     * @throws IllegalArgumentException Another of my exceptions",
            "     */",
            "    public void validCatchAndThrowUnion() {",
            "        try {",
            "            int i = 1;",
            "        } catch (IllegalArgumentException | IllegalAccessError e) {",
            "            throw e;",
            "        }",
            "    }",
            "}"
        ));
        String[] expected = {
            expectedThrowsMessage(9, 19),
            expectedThrowsMessage(20, 19),
            expectedDescriptionMessage(27),
            expectedThrowsMessage(33, 19),
            expectedThrowsMessage(46, 19)
        };

        verify(checker, new File[] { file }, file.getAbsolutePath(), expected);
    }

    @Test
    public void checkedThrows() throws Exception {
        File file = TestUtils.createCheckFile("checkedThrows", Arrays.asList(
            "public class CheckedThrows {",
            "    /**",
            "     * I don't document my throw.",
            "     */",
            "    public void invalidCheckedThrow() throws IllegalAccessError { // line 5, column 46",
            "        return;",
            "    }",
            "",
            "    /**",
            "     * I don't document either of my throws.",
            "     */",
            "    public void invalidCheckedThrows() throws IllegalAccessError, IllegalArgumentException { // line 12, columns 47 and 67",
            "        return;",
            "    }",
            "",
            "    /**",
            "     * @throws IllegalAccessError Documented throw.",
            "     */",
            "    public void validCheckedThrow() throws IllegalAccessError {",
            "        return;",
            "    }",
            "}"
        ));
        String[] expected = {
            expectedThrowsMessage(5, 46),
            expectedThrowsMessage(12, 47),
            expectedThrowsMessage(12, 67)
        };

        verify(checker, new File[] { file }, file.getAbsolutePath(), expected);
    }

    @Test
    public void scopeExemptThrows() throws Exception {
        File file = TestUtils.createCheckFile("scopeExemptThrows", Arrays.asList(
            "public class ScopeExemptThrows {",
            "    /**",
            "     * I am private I don't need to document my throw.",
            "     */",
            "    private void simpleThrowPrivate() {",
            "        throw new IllegalArgumentException();",
            "    }",
            "",
            "    /**",
            "     * I am private I don't need to document my throw.",
            "     */",
            "    private void catchAndThrowPrivate() {",
            "        try {",
            "            int i = 1;",
            "        } catch (IllegalAccessError e) {",
            "            throw e;",
            "        }",
            "    }",
            "",
            "    /**",
            "     * I am private I don't need to document my throws.",
            "     */",
            "    private void catchUnionAndThrowPrivate() {",
            "        try {",
            "            int i = 1;",
            "        } catch (IllegalArgumentException | IllegalAccessError e) {",
            "            throw e;",
            "        }",
            "    }",
            "",
            "    /**",
            "     * I am private I don't need to document my throw.",
            "     */",
            "    private void statesThrowsPrivate() throws IllegalAccessError {",
            "        int i = 1;",
            "    }",
            "}"
        ));
        String[] expected = new String[0];
        verify(checker, new File[] { file }, file.getAbsolutePath(), expected);
    }

    @Test
    public void constructorThrows() throws Exception {
        File file = TestUtils.createCheckFile("constructorThrows", Arrays.asList(
            "public class ConstructorThrows {",
            "    /**",
            "     * Invalid checked exception documentation",
            "     */",
            "    public ConstructorThrows() throws IllegalArgumentException { // line 5, column 39",
            "    }",
            "",
            "    /**",
            "     * Invalid unchecked exception documentation",
            "     * @param i My parameter",
            "     */",
            "    public ConstructorThrows(int i) {",
            "        throw new IllegalArgumentException(\"Invalid documentation\"); // line 13, column 9",
            "    }",
            "",
            "    /**",
            "     * Valid checked exception documentation",
            "     * @param s My parameter",
            "     * @throws IllegalArgumentException When I get bad input",
            "     */",
            "    public ConstructorThrows(String s) throws IllegalArgumentException {",
            "    }",
            "",
            "    /**",
            "     * Valid unchecked exception documentation",
            "     * @param b My parameter",
            "     * @throws IllegalArgumentException When I get bad input",
            "     */",
            "    public ConstructorThrows(boolean b) {",
            "        throw new IllegalArgumentException(\"Valid documentation\");",
            "    }",
            "}"
        ));
        String[] expected = {
            expectedThrowsMessage(5, 39),
            expectedThrowsMessage(13, 9)
        };

        verify(checker, new File[] { file }, file.getAbsolutePath(), expected);
    }

    @Test
    public void testThrowsClassField() throws Exception {
        File file = TestUtils.createCheckFile("testThrowsClassField", Arrays.asList(
            "import com.azure.core.util.logging.ClientLogger;",
            "",
            "import java.io.IOException;",
            "",
            "public class ThrowsClassField {",
            "    private static final IOException staticException = new IOException(\"Common exception\");",
            "    private IOException exception;",
            "",
            "    /**",
            "     * Method that throws exception in class using \"this\".",
            "     */",
            "    public void throwExceptionWithThis() {",
            "        throw this.exception;",
            "    }",
            "",
            "    /**",
            "     * Method that throws exception in class.",
            "     */",
            "    public void throwException() {",
            "        throw exception;",
            "    }",
            "",
            "    /**",
            "     * Method that throws static exception in class.",
            "     */",
            "    public void throwStaticException() {",
            "        throw staticException;",
            "    }",
            "",
            "    /**",
            "     * Method that throws static exception in class with classname qualifier.",
            "     */",
            "    public void throwStaticExceptionWithClassname() {",
            "        throw ThrowsClassField.staticException;",
            "    }",
            "",
            "}"
        ));
        String[] expected =  {
            expectedThrowsMessage(13, 19),
            expectedThrowsMessage(20, 15),
            expectedThrowsMessage(27, 15),
            expectedThrowsMessage(34, 31)
        };
        verify(checker, new File[] { file }, file.getAbsolutePath(), expected);
    }

    private String expectedDescriptionMessage(int line) {
        return TestUtils.expectedErrorMessage(line, JavadocThrowsChecks.MISSING_DESCRIPTION_MESSAGE);
    }

    private String expectedThrowsMessage(int line, int column) {
        return TestUtils.expectedErrorMessage(line, column, JavadocThrowsChecks.MISSING_THROWS_TAG_MESSAGE);
    }
}
