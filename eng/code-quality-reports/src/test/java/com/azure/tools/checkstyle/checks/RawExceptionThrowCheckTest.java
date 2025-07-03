// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.Checker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class RawExceptionThrowCheckTest extends AbstractModuleTestSupport {
    private Checker checker;

    @BeforeEach
    public void prepare() throws Exception {
        checker = createChecker(createModuleConfig(RawExceptionThrowCheck.class));
    }

    @AfterEach
    public void cleanup() {
        checker.destroy();
    }

    @Override
    protected String getPackageLocation() {
        return "com/azure/tools/checkstyle/checks/ExceptionLoggingChecks";
    }

    @Test
    public void logNewExceptionTestData() throws Exception {
        String[] expected = {
            expectedErrorMessage(8, 15, RawExceptionThrowCheck.ERROR_MESSAGE),
            expectedErrorMessage(13, 33, RawExceptionThrowCheck.ERROR_MESSAGE),
        };
        verify(checker, getPath("LogNewExceptionCheckTestData.java"), expected);
    }

    private String expectedErrorMessage(int line, int column, String message) {
        return String.format("%d:%d: %s", line, column, message);
    }
}
