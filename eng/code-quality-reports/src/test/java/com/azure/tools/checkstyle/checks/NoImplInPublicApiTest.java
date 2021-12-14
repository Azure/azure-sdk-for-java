// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.Checker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static com.azure.tools.checkstyle.checks.NoImplInPublicAPI.RETURN_TYPE_ERROR;

public class NoImplInPublicApiTest extends AbstractModuleTestSupport {
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

    @Test
    public void gettersNeedToCheckForImplementation() throws Exception {
        String[] expected = {
            expectedErrorMessage(40, 12, String.format(RETURN_TYPE_ERROR, "AnImplementationClass")),
            expectedErrorMessage(44, 15, String.format(RETURN_TYPE_ERROR, "AnImplementationClass")),
            expectedErrorMessage(57, 36, String.format(RETURN_TYPE_ERROR,
                "com.azure.implementation.AnImplementationClass")),
            expectedErrorMessage(61, 39, String.format(RETURN_TYPE_ERROR,
                "com.azure.implementation.AnImplementationClass"))
        };

        verify(checker, getPath("Getters.java"), expected);
    }

    private String expectedErrorMessage(int line, int column, String error) {
        return String.format("%d:%d: %s", line, column, error);
    }
}
