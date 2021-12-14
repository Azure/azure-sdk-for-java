// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.Checker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class NoImplInPublicApiTest extends AbstractModuleTestSupport {

    private static final String DIRECT_THROW_ERROR_MESSAGE = "Directly throwing an exception is disallowed. Must "
        + "throw through 'ClientLogger' API, either of 'logger.logExceptionAsError', 'logger.logThrowableAsError', "
        + "'logger.logExceptionAsWarning', or 'logger.logThrowableAsWarning' where 'logger' is type of ClientLogger "
        + "from Azure Core package.";

    private Checker checker;

    @Before
    public void prepare() throws Exception {
        checker = createChecker(createModuleConfig(NoImplInPublicAPI.class));
    }

    @After
    public void cleanup() {
        checker.destroy();
    }

    @Override
    protected String getPackageLocation() {
        return "com/azure/tools/checkstyle/checks/NoImplInPublicApiCheck";
    }

    @Test
    public void staticInitializerDoesNotCountAsApi() throws Exception {
        String[] expected = new String[0];
        verify(checker, getPath("StaticInitializer.java"), expected);
    }

    private String expectedErrorMessage(int line, int column) {
        return String.format("%d:%d: %s", line, column, DIRECT_THROW_ERROR_MESSAGE);
    }
}
