// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.linting.extensions.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.Checker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.clientcore.linting.extensions.checkstyle.checks.UseCaughtExceptionCauseCheck.UNUSED_CAUGHT_EXCEPTION_ERROR;

/**
 * Tests for using the caught exception variable, UseCaughtExceptionTest.
 */
public class UseCaughtExceptionTest extends AbstractModuleTestSupport {
    private Checker checker;

    @BeforeEach
    public void prepare() throws Exception {
        checker = createChecker(createModuleConfig(UseCaughtExceptionCauseCheck.class));
    }

    @AfterEach
    public void cleanup() {
        checker.destroy();
    }

    @Override
    protected String getPackageLocation() {
        return "io/clientcore/linting/extensions/checkstyle/checks/UseCaughtExceptionCheck";
    }

    @Test
    public void unusedCaughtExceptionTestData() throws Exception {
        String[] expected = { "17:13: " + String.format(UNUSED_CAUGHT_EXCEPTION_ERROR, "e") };
        verify(checker, getPath("UseCaughtExceptionTestData.java"), expected);
    }
}
