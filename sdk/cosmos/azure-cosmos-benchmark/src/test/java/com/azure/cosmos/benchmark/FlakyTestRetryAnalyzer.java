// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.benchmark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IRetryAnalyzer;
import org.testng.ITestResult;

public class FlakyTestRetryAnalyzer implements IRetryAnalyzer {
    protected static Logger logger = LoggerFactory.getLogger(FlakyTestRetryAnalyzer.class.getSimpleName());
    private int counter = 0;
    protected int retryLimit = 2;

    @Override
    public boolean retry(ITestResult iTestResult) {
        if (iTestResult.isSuccess()) {
            return false;
        }

        if (counter < retryLimit) {
            counter++;
            logger.info(
                "Retrying test {} - retry number {} out of at most {}. Exception seen in previous attempt: {}",
                iTestResult.getTestName(),
                counter,
                retryLimit,
                iTestResult.getThrowable());

            return true;
        }

        return false;
    }
}
