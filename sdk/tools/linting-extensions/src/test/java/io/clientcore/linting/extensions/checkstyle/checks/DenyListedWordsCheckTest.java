// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package io.clientcore.linting.extensions.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.AbstractModuleTestSupport;
import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;

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
        return "io/clientcore/linting/extensions/checkstyle/checks/DenyListedWordsChecks";
    }

    @Test
    public void denyListedWordsTestData() throws Exception {
        String[] expected = {
            "3:5: errorHTTPMethod" + DenyListedWordsCheck.ERROR_MESSAGE_TEMPLATE + "URL, HTTP, XML",
            "9:5: invalidXMLMethod" + DenyListedWordsCheck.ERROR_MESSAGE_TEMPLATE + "URL, HTTP, XML" };
        File file = TestUtils.createCheckFile("denyListedWordsTestData",
            "@JacksonXmlRootElement(localName = \"File-SetHTTPHeaders-Headers\")", "public class CamelCaseTestData {",
            "    public void errorHTTPMethod() { throw new RuntimeException(\"Error Messages.\"); }", "",
            "    public void validHttpMethod() { throw new RuntimeException(\"Error Messages.\"); }", "",
            "    public static void itIsAURLError() { throw new RuntimeException(\"Error Messages.\"); }", "",
            "    protected void invalidXMLMethod() { throw new RuntimeException(\"Error Messages.\"); }", "",
            "    private void shouldNotSearch() { throw new RuntimeException(\"Error Messages.\"); }", "}");

        verify(checker, new File[] { file }, file.getAbsolutePath(), expected);
    }

    @Test
    public void denyListedWordsInterface() throws Exception {
        File file = TestUtils.createCheckFile("denyListedWordsInterface",
            "package io.clientcore.linting.extensions.checkstyle.checks;", "import java.time.Duration;",
            "public interface DenyListedWordsInterface {", "    int HTTP_STATUS_TOO_MANY_REQUESTS = 429;",
            "    Duration calculateRetryDelay(int retryAttempts);", "}");

        verify(checker, new File[] { file }, file.getAbsolutePath());
    }

    @Test
    public void implementationPackageIgnored() throws Exception {
        File file = TestUtils.createCheckFile("implementationPackageIgnored", "package com.test.implementation;",
            "@JacksonXmlRootElement(localName = \"File-SetHTTPHeaders-Headers\")", "public class CamelCaseTestData {",
            "    public void errorHTTPMethod() { throw new RuntimeException(\"Error Messages.\"); }",
            "    public void validHttpMethod() { throw new RuntimeException(\"Error Messages.\"); }",
            "    public static void itIsAURLError() { throw new RuntimeException(\"Error Messages.\"); }",
            "    protected void invalidXMLMethod() { throw new RuntimeException(\"Error Messages.\"); }",
            "    private void shouldNotSearch() { throw new RuntimeException(\"Error Messages.\"); }", "}");

        verify(checker, new File[] { file }, file.getAbsolutePath());
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
        DefaultConfiguration denyListedWordsCheck
            = new DefaultConfiguration(DenyListedWordsCheck.class.getCanonicalName());
        denyListedWordsCheck.addProperty("denyListedWords", "URL, HTTP, XML");
        checks.addChild(treeWalker);
        treeWalker.addChild(denyListedWordsCheck);
        return checks;
    }
}
