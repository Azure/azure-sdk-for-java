// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import org.testng.ITestResult;

public class SplitTestsRetryAnalyzer extends FlakyTestRetryAnalyzer {
    public SplitTestsRetryAnalyzer() {
        super();
        this.retryLimit = 5;
    }

    @Override
    public boolean retry(ITestResult iTestResult) {
        if (!(iTestResult.getThrowable() instanceof SplitTimeoutException)) {
            return false;
        }

        return super.retry(iTestResult);
    }
}
