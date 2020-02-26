// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;

public class TestNGLogListener implements IInvokedMethodListener {
    private final Logger logger = LoggerFactory.getLogger(TestNGLogListener.class);
    @Override
    public void beforeInvocation(IInvokedMethod iInvokedMethod, ITestResult iTestResult) {
        logger.info("... beforeInvocation: {}#{}", iInvokedMethod.getTestMethod().getRealClass().getSimpleName(), iInvokedMethod.getTestMethod().getMethodName());
    }

    @Override
    public void afterInvocation(IInvokedMethod iInvokedMethod, ITestResult iTestResult) {
        logger.info("... afterInvocation: {}#{}, total time {}ms, result {}",
            iInvokedMethod.getTestMethod().getRealClass().getSimpleName(), iInvokedMethod.getTestMethod().getMethodName(),
            iTestResult.getEndMillis() - iTestResult.getStartMillis(),
            iTestResult.isSuccess() ? "test success": "test failure. reason: " + failureDetails(iTestResult)
            );

    }

    private String failureDetails(ITestResult iTestResult) {
        if (iTestResult.isSuccess()) {
            return null;
        }


        return iTestResult.getThrowable().getClass().getName() + ": " + iTestResult.getThrowable().getMessage();
    }
}
