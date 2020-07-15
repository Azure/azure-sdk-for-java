/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */
package com.microsoft.azure.spring.cloud.feature.manager.targeting;

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
