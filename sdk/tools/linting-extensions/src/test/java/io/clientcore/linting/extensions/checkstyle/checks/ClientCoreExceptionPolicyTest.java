// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.linting.extensions.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ClientCoreExceptionPolicyTest extends AbstractModuleTestSupport {
    private Checker checker;

    @BeforeEach
    public void prepare() throws Exception {
        DefaultConfiguration check = createModuleConfig(ExceptionHandlingCheck.class);
        check.addProperty("checkUnthrownExceptions", "false");
        check.addProperty("checkCaughtExceptionCause", "false");
        checker = createChecker(check);
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
        String[] expected = {
            "8:15: " + ExceptionHandlingCheck.RAW_EXCEPTION_ERROR,
            "13:33: " + ExceptionHandlingCheck.RAW_EXCEPTION_ERROR, };
        verify(checker, getPath("LogNewExceptionCheckTestData.java"), expected);
    }
}
