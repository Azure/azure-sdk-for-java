package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DisallowedWordsCheckTests extends AbstractModuleTestSupport {
    private static final String DISALLOWED_WORD_ERROR_MESSAGE = "%s, All Public API Classes, Fields and Methods should follow" +
        " Camelcase standards for the following words: XML, HTTP, URL.";

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
        return "com/azure/tools/checkstyle/checks/DisallowedWordsChecks";
    }

    @Test
    public void disallowedWordsTestData() throws Exception {
        String[] expected = {
            expectedErrorMessage(2, 5, String.format(DISALLOWED_WORD_ERROR_MESSAGE, "errorURLCase")),
            expectedErrorMessage(4, 5, String.format(DISALLOWED_WORD_ERROR_MESSAGE, "errorHTTPMethod"))
        };
        verify(checker, getPath("DisallowedWordsTestData.java"), expected);
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
        DefaultConfiguration camelCaseCheck = new DefaultConfiguration(DisallowedWordsCheck.class.getCanonicalName());
        camelCaseCheck.addAttribute("disallowedWords", "URL, HTTP, XML");
        checks.addChild(treeWalker);
        treeWalker.addChild(camelCaseCheck);
        return checks;
    }
}
