// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.rx.changefeed;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class SplitTestsRetryAnalyzer implements IRetryAnalyzer {
    int counter = 0;
    int retryLimit = 2;

    @Override
    public boolean retry(ITestResult iTestResult) {
        if (iTestResult.isSuccess()) {
            return false;
        }

        if (!(iTestResult.getThrowable() instanceof SplitTimeoutException)) {
            return false;
        }

        if (counter < retryLimit) {
            counter++;
            return true;
        }

        return false;
    }
}
