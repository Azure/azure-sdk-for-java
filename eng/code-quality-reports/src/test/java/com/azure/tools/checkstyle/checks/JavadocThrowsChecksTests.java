package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.Checker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JavadocThrowsChecksTests extends AbstractModuleTestSupport {
    private static final String MISSING_DESCRIPTION_MESSAGE = "@throws tag requires a description explaining when the error is thrown.";
    private static final String MISSING_THROWS_TAG_MESSAGE = "Javadoc @throws tag required for unchecked throw.";

    private Checker checker;

    @Before
    public void prepare() throws Exception {
        checker = createChecker(createModuleConfig(JavadocThrowsChecks.class));
    }

    @After
    public void cleanup() {
        checker.destroy();
    }

    @Override
    protected String getPackageLocation() {
        return "com/azure/tools/checkstyle/checks/JavadocThrowsChecks";
    }

    @Test
    public void simpleThrows() throws Exception {
        String[] expected = {
            expectedThrowsMessage(6, 9),
            expectedThrowsMessage(11, 9),
            expectedDescriptionMessage(32)
        };

        verify(checker, getPath("SimpleThrows.java"), expected);
    }

    @Test
    public void tryCatchThrows() throws Exception {
        String[] expected = {
            expectedThrowsMessage(8, 18),
            expectedThrowsMessage(19, 18),
            expectedThrowsMessage(19, 45),
            expectedDescriptionMessage(27),
            expectedThrowsMessage(32, 18)
        };

        verify(checker, getPath("TryCatchThrows.java"), expected);
    }

    @Test
    public void checkedThrows() throws Exception {
        String[] expected = {
            expectedThrowsMessage(5, 46),
            expectedThrowsMessage(12, 47),
            expectedThrowsMessage(12, 67)
        };

        verify(checker, getPath("CheckedThrows.java"), expected);
    }

    @Test
    public void scopeExemptThrows() throws Exception {
        String[] expected = new String[0];
        verify(checker, getPath("ScopeExemptThrows.java"), expected);
    }

    private String expectedDescriptionMessage(int line) {
        return String.format("%d: %s", line, MISSING_DESCRIPTION_MESSAGE);
    }

    private String expectedThrowsMessage(int line, int column) {
        return String.format("%d:%d: %s", line, column, MISSING_THROWS_TAG_MESSAGE);
    }
}
