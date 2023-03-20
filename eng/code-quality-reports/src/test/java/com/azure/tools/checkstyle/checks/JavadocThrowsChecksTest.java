// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.Checker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JavadocThrowsChecksTest extends AbstractModuleTestSupport {
    private Checker checker;

    @BeforeEach
    public void prepare() throws Exception {
        checker = createChecker(createModuleConfig(JavadocThrowsChecks.class));
    }

    @AfterEach
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
            expectedThrowsMessage(9, 19),
            expectedThrowsMessage(20, 19),
            expectedDescriptionMessage(27),
            expectedThrowsMessage(33, 19),
            expectedThrowsMessage(46, 19)
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

    @Test
    public void constructorThrows() throws Exception {
        String[] expected = {
            expectedThrowsMessage(5, 39),
            expectedThrowsMessage(13, 9)
        };

        verify(checker, getPath("ConstructorThrows.java"), expected);
    }

    @Test
    public void testThrowsClassField() throws Exception {
        String[] expected =  {
            expectedThrowsMessage(13, 19),
            expectedThrowsMessage(20, 15),
            expectedThrowsMessage(27, 15),
            expectedThrowsMessage(34, 31)
        };
        verify(checker, getPath("ThrowsClassField.java"), expected);
    }

    private String expectedDescriptionMessage(int line) {
        return String.format("%d: %s", line, JavadocThrowsChecks.MISSING_DESCRIPTION_MESSAGE);
    }

    private String expectedThrowsMessage(int line, int column) {
        return String.format("%d:%d: %s", line, column, JavadocThrowsChecks.MISSING_THROWS_TAG_MESSAGE);
    }
}
