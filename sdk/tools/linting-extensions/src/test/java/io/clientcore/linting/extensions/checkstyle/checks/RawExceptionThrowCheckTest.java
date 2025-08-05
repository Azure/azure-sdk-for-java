// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.linting.extensions.checkstyle.checks;

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
        return "io/clientcore/linting/extensions/checkstyle/checks/ExceptionLoggingChecks";
    }

    @Test
    public void logNewExceptionTestData() throws Exception {
        String[] expected
            = { "8:15: " + RawExceptionThrowCheck.ERROR_MESSAGE, "13:33: " + RawExceptionThrowCheck.ERROR_MESSAGE, };
        verify(checker, getPath("LogNewExceptionCheckTestData.java"), expected);
    }
}
