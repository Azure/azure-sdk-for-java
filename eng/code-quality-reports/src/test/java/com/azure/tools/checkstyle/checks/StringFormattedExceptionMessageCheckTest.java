// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.Checker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class StringFormattedExceptionMessageCheckTest extends AbstractModuleTestSupport {
    private Checker checker;

    @BeforeEach
    public void prepare() throws Exception {
        checker = createChecker(createModuleConfig(StringFormattedExceptionMessageCheck.class));
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
    public void stringFormatExceptionMessageTestData() throws Exception {
        String[] expected = {
            expectedErrorMessage(9, 31, StringFormattedExceptionMessageCheck.ERROR_MESSAGE),
            expectedErrorMessage(16, 53, StringFormattedExceptionMessageCheck.ERROR_MESSAGE)
        };
        verify(checker, getPath("StringFormattedExceptionCheckTestData.java"), expected);
    }

    private String expectedErrorMessage(int line, int column, String message) {
        return String.format("%d:%d: %s", line, column, message);
    }
}
