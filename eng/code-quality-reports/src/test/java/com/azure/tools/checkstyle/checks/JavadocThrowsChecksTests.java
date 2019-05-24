package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.Checker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JavadocThrowsChecksTests extends AbstractModuleTestSupport {
    private static final String[] NO_ERRORS = new String[0];
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
    public void testSimpleInvalidThrow() throws Exception {
        String[] expected = {
            "6:9: Javadoc @throws tag required for unchecked throw.",
            "11:9: Javadoc @throws tag required for unchecked throw."
        };
        verify(checker, getPath("SimpleInvalidThrow.java"), expected);
    }

    @Test
    public void testSimpleValidThrow() throws Exception {
        verify(checker, getPath("SimpleValidThrow.java"), NO_ERRORS);
    }
}
