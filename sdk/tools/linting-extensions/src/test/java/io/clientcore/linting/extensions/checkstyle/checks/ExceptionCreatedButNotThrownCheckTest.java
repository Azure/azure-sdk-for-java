// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.linting.extensions.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.Checker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ExceptionCreatedButNotThrownCheckTest extends AbstractModuleTestSupport {
    private Checker checker;

    @BeforeEach
    public void prepare() throws Exception {
        checker = createChecker(createModuleConfig(ExceptionCreatedButNotThrownCheck.class));
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
    public void throwCreatedException() throws Exception {
        String[] expected = { "8:32: " + ExceptionCreatedButNotThrownCheck.ERROR_MESSAGE };
        verify(checker, getPath("ThrowCreatedExceptionCheckTestData.java"), expected);
    }
}
