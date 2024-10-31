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

import static com.azure.tools.checkstyle.checks.GoodLoggingCheck.LOGGER_NAME_ERROR;
import static com.azure.tools.checkstyle.checks.GoodLoggingCheck.LOGGER_NAME_MISMATCH_ERROR;
import static com.azure.tools.checkstyle.checks.GoodLoggingCheck.NOT_CLIENT_LOGGER_ERROR;
import static com.azure.tools.checkstyle.checks.TestUtils.expectedErrorMessage;

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
        return "com/azure/tools/checkstyle/checks/GoodLoggerCheck";
    }

    @Test
    public void externalLoggerLibraryTestData() throws Exception {
        File file = TestUtils.createCheckFile("externalLoggerLibraryTestData", Arrays.asList(
            "import com.azure.core.util.logging.ClientLogger;",
            "// invalid external logger library",
            "import org.apache.logging.log4j;",
            "import org.slf4j;",
            "import java.util.logging;",
            "",
            "public class ExternalLoggerLibraryTestData {",
            "}"
        ));

        String[] expected = {
            expectedErrorMessage(3, 1, String.format(NOT_CLIENT_LOGGER_ERROR, "external logger",
                "com.azure.core.util.logging.ClientLogger", "org.apache.logging.log4j")),
            expectedErrorMessage(4, 1, String.format(NOT_CLIENT_LOGGER_ERROR, "external logger",
                "com.azure.core.util.logging.ClientLogger", "org.slf4j")),
            expectedErrorMessage(5,  1, String.format(NOT_CLIENT_LOGGER_ERROR, "external logger",
                "com.azure.core.util.logging.ClientLogger", "java.util.logging"))
        };
        verify(checker, new File[] { file }, file.getAbsolutePath(), expected);
    }

    @Test
    public void invalidLoggerNameTestData() throws Exception {
        File file = TestUtils.createCheckFile("invalidLoggerNameTestData", Arrays.asList(
            "import com.azure.core.util.logging.ClientLogger;",
            "",
            "public class InvalidLoggerNameTestData {",
            "    // invalid logger name",
            "    private final ClientLogger wrongLoggerName = new ClientLogger(InvalidLoggerNameTestData.class);",
            "",
            "    private final ClientLogger logger = new ClientLogger(InvalidLoggerNameTestData.class);",
            "}"
        ));
        String[] expected = {
            expectedErrorMessage(5, 5, String.format(LOGGER_NAME_ERROR, "logger", "wrongLoggerName"))
        };
        verify(checker, new File[] { file }, file.getAbsolutePath(), expected);
    }

    @Test
    public void wrongClassInLoggerConstructorTestData() throws Exception {
        File file = TestUtils.createCheckFile("wrongClassInLoggerConstructorTestData", Arrays.asList(
            "import com.azure.core.util.logging.ClientLogger;",
            "",
            "public class WrongClassInLoggerConstructorTestData {",
            "    // wrong class in ClientLogger constructor",
            "    private final ClientLogger logger = new ClientLogger(XXXXXX.class);",
            "}"
            ));
        String[] expected = {
            expectedErrorMessage(5, 64, String.format(LOGGER_NAME_MISMATCH_ERROR,
                "WrongClassInLoggerConstructorTestData", "XXXXXX.class"))
        };
        verify(checker, new File[] { file }, file.getAbsolutePath(), expected);
    }
}
