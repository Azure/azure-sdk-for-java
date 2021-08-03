// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager.targeting;

public class TargetingEvaluationOptions {

    private boolean ignoreCase;

    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    public TargetingEvaluationOptions setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
        return this;
    }

}
