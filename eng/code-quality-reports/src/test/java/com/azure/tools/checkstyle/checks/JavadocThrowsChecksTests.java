package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.Checker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JavadocThrowsChecksTests extends AbstractModuleTestSupport {
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
            "6:9: Javadoc @throws tag required for unchecked throw.",
            "11:9: Javadoc @throws tag required for unchecked throw."
        };

        verify(checker, getPath("SimpleThrows.java"), expected);
    }

    @Test
    public void tryCatchThrows() throws Exception {
        String[] expected = {

        };

        verify(checker, getPath("TryCatchThrows.java"), expected);
    }

    @Test
    public void checkedThrows() throws Exception {
        String[] expected = {

        };

        verify(checker, getPath("CheckedThrows.java"), expected);
    }

    @Test
    public void scopeExemptThrows() throws Exception {
        String[] expected = new String[0];
        verify(checker, getPath("ScopeExemptThrows.java"), expected);
    }
}
