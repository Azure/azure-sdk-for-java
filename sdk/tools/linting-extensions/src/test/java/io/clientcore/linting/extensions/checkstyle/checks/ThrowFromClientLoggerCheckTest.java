// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.linting.extensions.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.Checker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static io.clientcore.linting.extensions.checkstyle.checks.ThrowFromClientLoggerCheck.THROW_LOGGER_EXCEPTION_MESSAGE;

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
        return "io/clientcore/linting/extensions/checkstyle/checks/ThrowFromClientLoggerCheck";
    }

    @Test
    public void directThrowExceptionTestData() throws Exception {
        String[] expected = { "12:9: " + THROW_LOGGER_EXCEPTION_MESSAGE, "60:9: " + THROW_LOGGER_EXCEPTION_MESSAGE };
        verify(checker, getPath("DirectThrowExceptionTestData.java"), expected);
    }
}
