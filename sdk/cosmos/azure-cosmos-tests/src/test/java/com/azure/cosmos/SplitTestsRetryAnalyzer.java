// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

import org.testng.ITestResult;

public class SplitTestsRetryAnalyzer extends FlakyTestRetryAnalyzer {
    public SplitTestsRetryAnalyzer() {
        super();
        this.retryLimit = 10;
    }

    @Override
    public boolean retry(ITestResult iTestResult) {
        Throwable throwable = iTestResult.getThrowable();
        // Forcing a partition split is inherently disruptive. Besides the explicit
        // SplitTimeoutException, a split on a shared live-test account can surface transient
        // 408 (timeout), 429 (throttling) and 503 (service unavailable) errors while the split
        // is in progress. Retry all of these (bounded by retryLimit) so split tests are not
        // flaky on transient contention; deterministic failures are still surfaced.
        if (!(throwable instanceof SplitTimeoutException) && !isTransientCosmosFailure(throwable)) {
            return false;
        }

        return super.retry(iTestResult);
    }

    private static boolean isTransientCosmosFailure(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof CosmosException) {
                int statusCode = ((CosmosException) current).getStatusCode();
                return statusCode == 408 || statusCode == 429 || statusCode == 503;
            }
            current = current.getCause();
        }
        return false;
    }
}
