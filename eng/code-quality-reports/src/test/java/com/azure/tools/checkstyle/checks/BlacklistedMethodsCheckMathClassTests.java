// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BlacklistedMethodsCheckMathClassTests extends AbstractModuleTestSupport {

    private Checker checker;

    @Before
    public void prepare() throws Exception {
        checker = TestUtils.prepareCheckStyleChecker(prepareConfiguration());
        checker.addListener(this.getBriefUtLogger());
    }

    @After
    public void cleanup() {
        checker.destroy();
    }

    @Override
    protected String getPackageLocation() {
        return "com/azure/tools/checkstyle/checks/BlacklistedMethodsCheck";
    }

    @Test
    public void mathRandomTestData() throws Exception  {
        String[] expected = {
            TestUtils.expectedErrorMessage(6, 20, "Math.random is not secure random. Use java.security.SecureRandom instead."),
            TestUtils.expectedErrorMessage(14, 30, "java.lang.Math.random is not secure random. Use java.security.SecureRandom instead.")
        };
        verify(checker, getPath("InvalidRandomTestData.java"), expected);
    }

    private DefaultConfiguration prepareConfiguration() {
        DefaultConfiguration checks = new DefaultConfiguration("Checks");
        DefaultConfiguration treeWalker = new DefaultConfiguration("TreeWalker");
        DefaultConfiguration invalidMethodsCheck = new DefaultConfiguration(BlacklistedMethodsCheck.class.getCanonicalName());
        invalidMethodsCheck.addAttribute("methods", "java.lang.Math.random, Math.random");
        invalidMethodsCheck.addAttribute("message", "%s is not secure random. Use java.security.SecureRandom instead.");
        checks.addChild(treeWalker);
        treeWalker.addChild(invalidMethodsCheck);
        return checks;
    }
}
