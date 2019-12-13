// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.implementation;

public class RetryContext {
    public int retryCount;
    public long totalTimeInRetry;

    public RetryContext() {
    }

    public RetryContext(RetryContext retryContext) {
        if (retryContext != null) {
            this.retryCount = retryContext.retryCount;
            this.totalTimeInRetry = retryContext.totalTimeInRetry;
        }
    }
}
