// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.Checker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ThrowFromClientLoggerCheckTest extends AbstractModuleTestSupport {

    private static final String DIRRECT_THROW_ERROR_MESSAGE = "Directly throwing an exception is disallowed. Must "
        + "throw through 'ClientLogger' API, either of 'logger.logExceptionAsError' or 'logger.logExceptionAsWarning' "
        + "where 'logger' is type of ClientLogger from Azure Core package.";

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
            expectedErrorMessage(7, 9, String.format(DIRRECT_THROW_ERROR_MESSAGE))
        };
        verify(checker, getPath("DirectThrowExceptionTestData.java"), expected);
    }

    private String expectedErrorMessage(int line, int column, String errorMessage) {
        return String.format("%d:%d: %s", line, column, errorMessage);
    }
}
