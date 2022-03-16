// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.Checker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.Collection;
import java.util.Collections;

import static com.azure.tools.checkstyle.checks.UseCaughtExceptionCauseCheck.UNUSED_CAUGHT_EXCEPTION_ERROR;

/**
 * Tests for using the caught exception variable, UseCaughtExceptionTest.
 */
public class UseCaughtExceptionTest extends AbstractModuleTestSupport {
    private Checker checker;

    @Before
    public void prepare() throws Exception {
        checker = createChecker(createModuleConfig(UseCaughtExceptionCauseCheck.class));
    }

    @After
    public void cleanup() {
        checker.destroy();
    }

    @Override
    protected String getPackageLocation() {
        return "com/azure/tools/checkstyle/checks/UseCaughtExceptionCheck";
    }


    @Test
    public void unusedCaughtExceptionTestData() throws Exception {
        String[] expected = {
            expectedErrorMessage(17, 13, String.format(UNUSED_CAUGHT_EXCEPTION_ERROR, "e"))
        };
        verify(checker, getPath("UseCaughtExceptionTestData.java"), expected);
    }

    private String expectedErrorMessage(int line, int column, String errorMessage) {
        return String.format("%d:%d: %s", line, column, errorMessage);
    }
}
