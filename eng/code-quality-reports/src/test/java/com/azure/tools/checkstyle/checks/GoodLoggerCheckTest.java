// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.Checker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for good logging practices, GoodLoggerCheck.
 */
public class GoodLoggerCheckTest extends AbstractModuleTestSupport {
    private static final String INCONSISTENCY_NAMING_MESSAGE = "ClientLogger instance naming: use 'logger'"
        + " instead of 'wrongLoggerName' for consistency.";
    private static final String EXTERNAL_LOGGER_USED_MESSAGE = "Do not use external logger class. "
        + "Use 'com.azure.core.util.logging.ClientLogger' as a logging mechanism instead of '%s'.";
    private static final String NOT_NEWING_MATCH_CLASS_NAME = "Not newing a ClientLogger with matching class name. "
        + "Use 'WrongClassInLoggerConstructorTestData.class' instead of 'XXXXXX.class'.";
    private static final String STATIC_LOGGER_MESSAGE = "Use a static ClientLogger instance in a static method.";

    private Checker checker;

    @Before
    public void prepare() throws Exception {
        checker = createChecker(createModuleConfig(GoodLoggingCheck.class));
    }

    @After
    public void cleanup() {
        checker.destroy();
    }

    @Override
    protected String getPackageLocation() {
        return "com/azure/tools/checkstyle/checks/GoodLoggerCheck";
    }

    @Test
    public void externalLoggerLibraryTestData() throws Exception {
        String[] expected = {
            expectedErrorMessage(3, 1, String.format(EXTERNAL_LOGGER_USED_MESSAGE,
                "org.apache.logging.log4j")),
            expectedErrorMessage(4, 1, String.format(EXTERNAL_LOGGER_USED_MESSAGE, "org.slf4j")),
            expectedErrorMessage(5,  1, String.format(EXTERNAL_LOGGER_USED_MESSAGE, "java.util.logging"))
        };
        verify(checker, getPath("ExternalLoggerLibraryTestData.java"), expected);
    }

    @Test
    public void invalidLoggerNameTestData() throws Exception {
        String[] expected = {
            expectedErrorMessage(5, 5, INCONSISTENCY_NAMING_MESSAGE),
        };
        verify(checker, getPath("InvalidLoggerNameTestData.java"), expected);
    }

    @Test
    public void nonStaticLoggerTestData() throws Exception {
        String[] expected = {
            expectedErrorMessage(9, 5, STATIC_LOGGER_MESSAGE)
        };
        verify(checker, getPath("NonStaticLoggerTestData.java"), expected);
    }

    @Test
    public void wrongClassInLoggerConstructorTestData() throws Exception {
        String[] expected = {
            expectedErrorMessage(5, 64, NOT_NEWING_MATCH_CLASS_NAME)
        };
        verify(checker, getPath("WrongClassInLoggerConstructorTestData.java"), expected);
    }

    private String expectedErrorMessage(int line, int column, String errorMessage) {
        return String.format("%d:%d: %s", line, column, errorMessage);
    }
}
