// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.Checker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


import static com.azure.tools.checkstyle.checks.ThrowFromClientLoggerCheck.THROW_LOGGER_EXCEPTION_MESSAGE;

public class ThrowFromClientLoggerCheckTest extends AbstractModuleTestSupport {
    private Checker checker;

    @Before
    public void prepare() throws Exception {
        checker = createChecker(createModuleConfig(ThrowFromClientLoggerCheck.class));
    }

    @After
    public void cleanup() {
        checker.destroy();
    }

    @Override
    protected String getPackageLocation() {
        return "com/azure/tools/checkstyle/checks/ThrowFromClientLoggerCheck";
    }

    @Test
    public void directThrowExceptionTestData() throws Exception {
        String[] expected = {
            expectedErrorMessage(12, 9),
            expectedErrorMessage(60, 9)
        };
        verify(checker, getPath("DirectThrowExceptionTestData.java"), expected);
    }

    private String expectedErrorMessage(int line, int column) {
        return String.format("%d:%d: %s", line, column, THROW_LOGGER_EXCEPTION_MESSAGE);
    }
}
