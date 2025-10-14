// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.cosmos;

public class SuperFlakyTestRetryAnalyzer extends FlakyTestRetryAnalyzer {
    public SuperFlakyTestRetryAnalyzer() {
        super();
        this.retryLimit = 10;
    }
}
