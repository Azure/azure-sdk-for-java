// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class DenyListedWordsCheckTest extends AbstractModuleTestSupport {
    private Checker checker;

    @BeforeEach
    public void prepare() throws Exception {
        checker = prepareCheckStyleChecker();
        checker.addListener(this.getBriefUtLogger());
    }

    @AfterEach
    public void cleanup() {
        checker.destroy();
    }

    @Override
    protected String getPackageLocation() {
        return "com/azure/tools/checkstyle/checks/DenyListedWordsChecks";
    }

    @Test
    public void denyListedWordsTestData() throws Exception {
        String[] expected = {
            expectedErrorMessage(3, 5, String.format(DenyListedWordsCheck.ERROR_MESSAGE, "errorHTTPMethod", "XML, HTTP, URL")),
            expectedErrorMessage(9, 5, String.format(DenyListedWordsCheck.ERROR_MESSAGE, "invalidXMLMethod", "XML, HTTP, URL"))
        };
        verify(checker, getPath("DenyListedWordsTestData.java"), expected);
    }

    @Test
    public void denyListedWordsInterface() throws Exception {
        verify(checker, getPath("DenyListedWordsInterface.java"));
    }

    private String expectedErrorMessage(int line, int column, String errorMessage) {
        return String.format("%d:%d: %s", line, column, errorMessage);
    }

    private Checker prepareCheckStyleChecker() throws CheckstyleException {
        Checker checker = new Checker();
        checker.setModuleClassLoader(Thread.currentThread().getContextClassLoader());
        checker.configure(prepareConfiguration());
        return checker;
    }

    private DefaultConfiguration prepareConfiguration() {
        DefaultConfiguration checks = new DefaultConfiguration("Checks");
        DefaultConfiguration treeWalker = new DefaultConfiguration("TreeWalker");
        DefaultConfiguration denyListedWordsCheck = new DefaultConfiguration(DenyListedWordsCheck.class.getCanonicalName());
        denyListedWordsCheck.addProperty("denyListedWords", "URL, HTTP, XML");
        checks.addChild(treeWalker);
        treeWalker.addChild(denyListedWordsCheck);
        return checks;
    }
}
