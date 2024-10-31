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

import static com.azure.tools.checkstyle.checks.TestUtils.expectedErrorMessage;
import static com.azure.tools.checkstyle.checks.ThrowFromClientLoggerCheck.THROW_LOGGER_EXCEPTION_MESSAGE;

public class ThrowFromClientLoggerCheckTest extends AbstractModuleTestSupport {
    private Checker checker;

    @BeforeEach
    public void prepare() throws Exception {
        checker = createChecker(createModuleConfig(ThrowFromClientLoggerCheck.class));
    }

    @AfterEach
    public void cleanup() {
        checker.destroy();
    }

    @Override
    protected String getPackageLocation() {
        return "com/azure/tools/checkstyle/checks/ThrowFromClientLoggerCheck";
    }

    @Test
    public void directThrowExceptionTestData() throws Exception {
        File file = TestUtils.createCheckFile("directThrowExceptionTestData", Arrays.asList(
            "import com.azure.core.util.logging.ClientLogger;",
            "",
            "public class DirectThrowExceptionTestData {",
            "    private final ClientLogger logger = new ClientLogger(DirectThrowExceptionTestData.class);",
            "",
            "    // Skip check on constructor",
            "    DirectThrowExceptionTestData() {",
            "        throw new RuntimeException(\"Error Messages.\");",
            "    }",
            "",
            "    public void directThrowException () {",
            "        throw new RuntimeException(\"Error Messages.\");",
            "    }",
            "",
            "    public void correctThrowException() {",
            "        throw logger.logExceptionAsWarning(Exceptions.propagate(new IllegalStateException(\"Error Messages\")));",
            "    }",
            "",
            "    public static skipCheckOnStaticMethod() {",
            "        throw new RuntimeException(\"Error Messages.\");",
            "    }",
            "",
            "    // Skip check on static class",
            "    static class SkipCheckOnStaticClass {",
            "        public void directThrowException () {",
            "            throw new RuntimeException(\"Error Messages.\");",
            "        }",
            "    }",
            "",
            "    public void validLogExceptionAsError() {",
            "        throw logger.logExceptionAsError(new RuntimeException(\"Error message.\"));",
            "    }",
            "",
            "    public void validLogThrowableAsError() {",
            "        throw logger.logThrowableAsError(new RuntimeException(\"Error message.\"));",
            "    }",
            "",
            "    public void validLogExceptionAsWarning() {",
            "        throw logger.logExceptionAsWarning(new RuntimeException(\"Error message.\"));",
            "    }",
            "",
            "    public void validLogThrowableAsWarning() {",
            "        throw logger.logThrowableAsWarning(new RuntimeException(\"Error message.\"));",
            "    }",
            "",
            "    public void validThrowExceptionWithBuilder() {",
            "        throw logger.atError().log(Exceptions.propagate(new IllegalStateException(\"Error Messages\")));",
            "    }",
            "",
            "    public void validThrowExceptionWithBuilderAndContext() {",
            "        throw logger.atError().addKeyValuePair(\"foo\", \"bar\").log(new RuntimeException(\"Error message.\"));",
            "    }",
            "",
            "    public void validThrowExceptionWithBuilderAndContextAdvanced() {",
            "        LoggingEventBuilder builder = logger.atError();",
            "        throw builder.addKeyValuePair(\"foo\", \"bar\").log(new RuntimeException(\"Error message.\"));",
            "    }",
            "",
            "    public void invalidLoggingBuilderNoLogCall() {",
            "        throw logger.atError();",
            "    }",
            "}"));
        String[] expected = {
            expectedErrorMessage(12, 9, THROW_LOGGER_EXCEPTION_MESSAGE),
            expectedErrorMessage(60, 9, THROW_LOGGER_EXCEPTION_MESSAGE)
        };
        verify(checker, new File[]{file}, file.getAbsolutePath(), expected);
    }
}
