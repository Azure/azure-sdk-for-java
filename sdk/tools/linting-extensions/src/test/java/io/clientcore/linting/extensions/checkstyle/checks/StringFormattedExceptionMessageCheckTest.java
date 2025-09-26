// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.linting.extensions.checkstyle.checks;

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
        return "io/clientcore/linting/extensions/checkstyle/checks/ExceptionLoggingChecks";
    }

    @Test
    public void stringFormatExceptionMessageTestData() throws Exception {
        String[] expected = {
            "9:31: " + StringFormattedExceptionMessageCheck.ERROR_MESSAGE,
            "16:53: " + StringFormattedExceptionMessageCheck.ERROR_MESSAGE };
        verify(checker, getPath("StringFormattedExceptionCheckTestData.java"), expected);
    }
}
