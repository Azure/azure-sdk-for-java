package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import org.junit.After;
import org.junit.Test;

public class DisallowedWordsCheckTests extends AbstractModuleTestSupport {
    private static final String DISALLOWED_WORD_ERROR_MESSAGE = "All Public API Classes, Fields and Methods should follow" +
        " Camelcase standards for the following words: XML, HTTP, URL.";
    private static final String NO_DISALLOWED_WORDS_ERROR_MESSAGE = "The disallowedWords property is required for the DisallowedWordsCheck module. Please specify which words should be disallowed from being used.";

    private Checker checker;

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
        checker = prepareCheckStyleChecker(true);
        checker.addListener(this.getBriefUtLogger());
        String[] expected = {
            expectedErrorMessage(2, 5, DISALLOWED_WORD_ERROR_MESSAGE),
            expectedErrorMessage(4, 5, DISALLOWED_WORD_ERROR_MESSAGE)
        };

        verify(checker, getPath("DisallowedWordsTestData.java"), expected);
    }

    @Test
    public void camelCaseNoPropertySetTestData() throws Exception {
        checker = prepareCheckStyleChecker(false);
        checker.addListener(this.getBriefUtLogger());
        String[] expected = {
            expectedErrorMessage(1, 1, NO_DISALLOWED_WORDS_ERROR_MESSAGE),
        };

        verify(checker, getPath("DisallowedWordsTestData.java"), expected);
    }

    private String expectedErrorMessage(int line, int column, String errorMessage) {
        return String.format("%d:%d: %s", line, column, errorMessage);
    }

    private Checker prepareCheckStyleChecker(boolean addDisallowedWords) throws CheckstyleException {
        Checker checker = new Checker();
        checker.setModuleClassLoader(Thread.currentThread().getContextClassLoader());
        checker.configure(prepareConfiguration(addDisallowedWords));
        return checker;
    }

    private DefaultConfiguration prepareConfiguration(boolean addDisallowedWords) {
        DefaultConfiguration checks = new DefaultConfiguration("Checks");
        DefaultConfiguration treeWalker = new DefaultConfiguration("TreeWalker");
        DefaultConfiguration camelCaseCheck = new DefaultConfiguration(DisallowedWordsCheck.class.getCanonicalName());
        if (addDisallowedWords) {
            camelCaseCheck.addAttribute("disallowedWords", "URL, HTTP, XML");
        }
        checks.addChild(treeWalker);
        treeWalker.addChild(camelCaseCheck);
        return checks;
    }
}
