// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.test.implementation;

public class TestRunMetrics {
    private final String logPrefix;
    private final long startMillis;

    public TestRunMetrics(String logPrefix, long startMillis) {
        this.logPrefix = logPrefix;
        this.startMillis = startMillis;
    }

    public String getLogPrefix() {
        return logPrefix;
    }

    public long getStartMillis() {
        return startMillis;
    }
}
