// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.linting.extensions.checkstyle.checks;

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
        return "io/clientcore/linting/extensions/checkstyle/checks/JavadocThrowsChecks";
    }

    @Test
    public void simpleThrows() throws Exception {
        String[] expected = {
            "6:9: " + JavadocThrowsChecks.MISSING_THROWS_TAG_MESSAGE,
            "11:9: " + JavadocThrowsChecks.MISSING_THROWS_TAG_MESSAGE,
            "32: " + JavadocThrowsChecks.MISSING_DESCRIPTION_MESSAGE };

        verify(checker, getPath("SimpleThrows.java"), expected);
    }

    @Test
    public void tryCatchThrows() throws Exception {
        String[] expected = {
            "9:19: " + JavadocThrowsChecks.MISSING_THROWS_TAG_MESSAGE,
            "20:19: " + JavadocThrowsChecks.MISSING_THROWS_TAG_MESSAGE,
            "27: " + JavadocThrowsChecks.MISSING_DESCRIPTION_MESSAGE,
            "33:19: " + JavadocThrowsChecks.MISSING_THROWS_TAG_MESSAGE,
            "46:19: " + JavadocThrowsChecks.MISSING_THROWS_TAG_MESSAGE };

        verify(checker, getPath("TryCatchThrows.java"), expected);
    }

    @Test
    public void checkedThrows() throws Exception {
        String[] expected = {
            "5:46: " + JavadocThrowsChecks.MISSING_THROWS_TAG_MESSAGE,
            "12:47: " + JavadocThrowsChecks.MISSING_THROWS_TAG_MESSAGE,
            "12:67: " + JavadocThrowsChecks.MISSING_THROWS_TAG_MESSAGE, };

        verify(checker, getPath("CheckedThrows.java"), expected);
    }

    @Test
    public void scopeExemptThrows() throws Exception {
        verify(checker, getPath("ScopeExemptThrows.java"));
    }

    @Test
    public void constructorThrows() throws Exception {
        String[] expected = {
            "5:39: " + JavadocThrowsChecks.MISSING_THROWS_TAG_MESSAGE,
            "13:9: " + JavadocThrowsChecks.MISSING_THROWS_TAG_MESSAGE, };

        verify(checker, getPath("ConstructorThrows.java"), expected);
    }

    @Test
    public void testThrowsClassField() throws Exception {
        String[] expected = {
            "13:19: " + JavadocThrowsChecks.MISSING_THROWS_TAG_MESSAGE,
            "20:15: " + JavadocThrowsChecks.MISSING_THROWS_TAG_MESSAGE,
            "27:15: " + JavadocThrowsChecks.MISSING_THROWS_TAG_MESSAGE,
            "34:31: " + JavadocThrowsChecks.MISSING_THROWS_TAG_MESSAGE, };
        verify(checker, getPath("ThrowsClassField.java"), expected);
    }
}
