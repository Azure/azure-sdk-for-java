// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.tools.lintingextensions.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.Checker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.clientcore.tools.lintingextensions.checkstyle.checks.GoodLoggingCheck.LOGGER_NAME_ERROR;
import static io.clientcore.tools.lintingextensions.checkstyle.checks.GoodLoggingCheck.LOGGER_NAME_MISMATCH_ERROR;
import static io.clientcore.tools.lintingextensions.checkstyle.checks.GoodLoggingCheck.NOT_CLIENT_LOGGER_ERROR;

/**
 * Tests for good logging practices, GoodLoggerCheck.
 */
public class GoodLoggerCheckTest extends AbstractModuleTestSupport {
    private Checker checker;

    @BeforeEach
    public void prepare() throws Exception {
        checker = createChecker(createModuleConfig(GoodLoggingCheck.class));
    }

    @AfterEach
    public void cleanup() {
        checker.destroy();
    }

    @Override
    protected String getPackageLocation() {
        return "io/clientcore/tools/lintingextensions/checkstyle/checks/GoodLoggerCheck";
    }

    @Test
    public void externalLoggerLibraryTestData() throws Exception {
        String[] expected = {
            "3:1: " + String.format(NOT_CLIENT_LOGGER_ERROR, "external logger",
                "com.azure.core.util.logging.ClientLogger", "org.apache.logging.log4j"),
            "4:1: " + String.format(NOT_CLIENT_LOGGER_ERROR, "external logger",
                "com.azure.core.util.logging.ClientLogger", "org.slf4j"),
            "5:1: " + String.format(NOT_CLIENT_LOGGER_ERROR, "external logger",
                "com.azure.core.util.logging.ClientLogger", "java.util.logging"),
        };
        verify(checker, getPath("ExternalLoggerLibraryTestData.java"), expected);
    }

    @Test
    public void invalidLoggerNameTestData() throws Exception {
        String[] expected = {
            "5:5: " + String.format(LOGGER_NAME_ERROR, "logger", "wrongLoggerName"),
        };
        verify(checker, getPath("InvalidLoggerNameTestData.java"), expected);
    }

    @Test
    public void wrongClassInLoggerConstructorTestData() throws Exception {
        String[] expected = {
            "5:64: " + String.format(LOGGER_NAME_MISMATCH_ERROR,
                "WrongClassInLoggerConstructorTestData", "XXXXXX.class")
        };
        verify(checker, getPath("WrongClassInLoggerConstructorTestData.java"), expected);
    }
}
