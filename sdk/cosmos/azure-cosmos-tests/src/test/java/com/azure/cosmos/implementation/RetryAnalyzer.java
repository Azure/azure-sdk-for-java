// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.


package com.azure.cosmos.implementation;

import com.azure.cosmos.CosmosException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.ITestResult;
import org.testng.util.RetryAnalyzerCount;

import java.time.Duration;
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

            int timeToWaitBeforeRetryInSeconds = getTimeToWaitInSeconds(result);
            TimeUnit.SECONDS.sleep(timeToWaitBeforeRetryInSeconds);
        } catch (InterruptedException e) {
            return false;
        }

        return true;
    }

    private int getTimeToWaitInSeconds(ITestResult result) {
        Throwable throwable = result.getThrowable();
        CosmosException cosmosException = extractCosmosExceptionIfAny(throwable);

        if (cosmosException == null) {
            return  waitBetweenRetriesInSeconds;
        }

        Duration retryAfterDuration = cosmosException.getRetryAfterDuration();
        if (retryAfterDuration.toMillis() <= 0) {
            return waitBetweenRetriesInSeconds;
        }

        return Math.max(Math.toIntExact(retryAfterDuration.getSeconds()), waitBetweenRetriesInSeconds);
    }

    private CosmosException extractCosmosExceptionIfAny(Throwable t) {
        if (t == null) {
            return null;
        }

        while( t != null && !(t instanceof CosmosException)) {
            t = t.getCause();
        }

        return (CosmosException) t;
    }
}
