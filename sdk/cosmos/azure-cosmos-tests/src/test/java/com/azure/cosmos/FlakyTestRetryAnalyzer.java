// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class FlakyTestRetryAnalyzer implements IRetryAnalyzer {
    private int counter = 0;
    protected int retryLimit = 2;

    @Override
    public boolean retry(ITestResult iTestResult) {
        if (iTestResult.isSuccess()) {
            return false;
        }

        if (counter < retryLimit) {
            counter++;
            return true;
        }

        return false;
    }
}
