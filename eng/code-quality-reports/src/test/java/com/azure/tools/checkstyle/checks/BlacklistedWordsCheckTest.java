// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class BlacklistedWordsCheckTest extends AbstractModuleTestSupport {
    private Checker checker;

    @Before
    public void prepare() throws Exception {
        checker = prepareCheckStyleChecker();
        checker.addListener(this.getBriefUtLogger());
    }

    @After
    public void cleanup() {
        checker.destroy();
    }

    @Override
    protected String getPackageLocation() {
        return "com/azure/tools/checkstyle/checks/BlacklistedWordsChecks";
    }

    @Test
    public void blacklistedWordsTestData() throws Exception {
        String[] expected = {
            expectedErrorMessage(3, 5, String.format(BlacklistedWordsCheck.ERROR_MESSAGE, "errorHTTPMethod", "XML, HTTP, URL")),
            expectedErrorMessage(9, 5, String.format(BlacklistedWordsCheck.ERROR_MESSAGE, "invalidXMLMethod", "XML, HTTP, URL"))
        };
        verify(checker, getPath("BlacklistedWordsTestData.java"), expected);
    }

    @Test
    public void blacklistedWordsInterface() throws Exception {
        verify(checker, getPath("BlacklistedWordsInterface.java"));
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
        DefaultConfiguration blacklistedWordsCheck = new DefaultConfiguration(BlacklistedWordsCheck.class.getCanonicalName());
        blacklistedWordsCheck.addProperty("blacklistedWords", "URL, HTTP, XML");
        checks.addChild(treeWalker);
        treeWalker.addChild(blacklistedWordsCheck);
        return checks;
    }
}
