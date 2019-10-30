// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.tools.checkstyle.checks;

import com.puppycrawl.tools.checkstyle.Checker;
import com.puppycrawl.tools.checkstyle.DefaultConfiguration;
import com.puppycrawl.tools.checkstyle.api.CheckstyleException;

public class TestUtils {

    public static Checker prepareCheckStyleChecker(DefaultConfiguration defaultConfiguration) throws CheckstyleException {
        Checker checker = new Checker();
        checker.setModuleClassLoader(Thread.currentThread().getContextClassLoader());
        checker.configure(defaultConfiguration);
        return checker;
    }

    public static String expectedErrorMessage(int line, int column, String errorMessage) {
        return String.format("%d:%d: %s", line, column, errorMessage);
    }

    public static String expectedThrowsMessage(int line, int column, String errorMesasge) {
        return String.format("%d:%d: %s", line, column, errorMesasge);
    }

    public static String expectedDescriptionMessage(int line, String errorMessage) {
        return String.format("%d: %s", line, errorMessage);
    }
}
