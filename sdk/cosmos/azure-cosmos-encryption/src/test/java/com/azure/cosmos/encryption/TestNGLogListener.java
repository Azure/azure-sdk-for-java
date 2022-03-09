// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos.encryption;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.IInvokedMethod;
import org.testng.IInvokedMethodListener;
import org.testng.ITestResult;
import org.testng.SkipException;

public class TestNGLogListener implements IInvokedMethodListener {
    private final Logger logger = LoggerFactory.getLogger(TestNGLogListener.class);

    @Override
    public void beforeInvocation(IInvokedMethod iInvokedMethod, ITestResult iTestResult) {
        logger.info("beforeInvocation: {}", methodName(iInvokedMethod));
    }

    @Override
    public void afterInvocation(IInvokedMethod iInvokedMethod, ITestResult iTestResult) {
        logger.info("afterInvocation: {}, total time {}ms, result {}",
            methodName(iInvokedMethod),
            iTestResult.getEndMillis() - iTestResult.getStartMillis(),
            resultDetails(iTestResult)
        );
    }

    private String resultDetails(ITestResult iTestResult) {
        if (iTestResult.isSuccess()) {
            return "success";
        }

        if (iTestResult.getThrowable() instanceof SkipException) {
            return "skipped. reason: " + failureDetails(iTestResult);
        }

        return "failed. reason: " + failureDetails(iTestResult);
    }

    private String failureDetails(ITestResult iTestResult) {
        if (iTestResult.isSuccess()) {
            return null;
        }

        if (iTestResult.getThrowable() == null) {
            logger.error("throwable is null");
            return null;
        }

        return iTestResult.getThrowable().getClass().getName() + ": " + iTestResult.getThrowable().getMessage();
    }

    private String methodName(IInvokedMethod iInvokedMethod) {
        return iInvokedMethod.getTestMethod().getRealClass().getSimpleName() + "#" + iInvokedMethod.getTestMethod().getMethodName();
    }
}
