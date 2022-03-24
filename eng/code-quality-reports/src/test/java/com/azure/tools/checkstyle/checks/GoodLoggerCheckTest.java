// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.Checker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.azure.tools.checkstyle.checks.GoodLoggingCheck.LOGGER_NAME_ERROR;
import static com.azure.tools.checkstyle.checks.GoodLoggingCheck.LOGGER_NAME_MISMATCH_ERROR;
import static com.azure.tools.checkstyle.checks.GoodLoggingCheck.NOT_CLIENT_LOGGER_ERROR;
import static com.azure.tools.checkstyle.checks.GoodLoggingCheck.STATIC_LOGGER_ERROR;

/**
 * Tests for good logging practices, GoodLoggerCheck.
 */
public class GoodLoggerCheckTest extends AbstractModuleTestSupport {
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
            expectedErrorMessage(3, 1, String.format(NOT_CLIENT_LOGGER_ERROR, "external logger",
                "com.azure.core.util.logging.ClientLogger", "org.apache.logging.log4j")),
            expectedErrorMessage(4, 1, String.format(NOT_CLIENT_LOGGER_ERROR, "external logger",
                "com.azure.core.util.logging.ClientLogger", "org.slf4j")),
            expectedErrorMessage(5,  1, String.format(NOT_CLIENT_LOGGER_ERROR, "external logger",
                "com.azure.core.util.logging.ClientLogger", "java.util.logging"))
        };
        verify(checker, getPath("ExternalLoggerLibraryTestData.java"), expected);
    }

    @Test
    public void invalidLoggerNameTestData() throws Exception {
        String[] expected = {
            expectedErrorMessage(5, 5, String.format(LOGGER_NAME_ERROR, "logger", "wrongLoggerName"))
        };
        verify(checker, getPath("InvalidLoggerNameTestData.java"), expected);
    }

    @Test
    public void wrongClassInLoggerConstructorTestData() throws Exception {
        String[] expected = {
            expectedErrorMessage(5, 64, String.format(LOGGER_NAME_MISMATCH_ERROR,
                "WrongClassInLoggerConstructorTestData", "XXXXXX.class"))
        };
        verify(checker, getPath("WrongClassInLoggerConstructorTestData.java"), expected);
    }

    private String expectedErrorMessage(int line, int column, String errorMessage) {
        return String.format("%d:%d: %s", line, column, errorMessage);
    }
}
