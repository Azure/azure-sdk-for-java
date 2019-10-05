// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


package com.azure.data.cosmos.internal;

import com.azure.data.cosmos.internal.TestConfigurations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.util.RetryAnalyzerCount;

import java.util.concurrent.TimeUnit;

public class RetryAnalyzer extends RetryAnalyzerCount {
    private final Logger logger = LoggerFactory.getLogger(RetryAnalyzer.class);
    private final int waitBetweenRetriesInSeconds = 120;

    public RetryAnalyzer() {
        this.setCount(Integer.parseInt(TestConfigurations.MAX_RETRY_LIMIT));
    }

    @Override
    public boolean retryMethod(ITestResult result) {
        try {
            TimeUnit.SECONDS.sleep(waitBetweenRetriesInSeconds);
        } catch (InterruptedException e) {
            return false;
        }

        return true;
    }
}
