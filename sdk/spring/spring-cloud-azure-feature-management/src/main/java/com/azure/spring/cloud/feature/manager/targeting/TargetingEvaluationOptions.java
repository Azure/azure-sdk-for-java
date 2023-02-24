// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.spring.cloud.feature.manager.targeting;

/**
 * Configuration options for the `Microsoft.TargetingFilter`.
 */
public final class TargetingEvaluationOptions {

    private boolean ignoreCase;

    /**
     * Ignore case of users/groups
     * @return boolean
     */
    public boolean isIgnoreCase() {
        return ignoreCase;
    }

    /**
     * Enables ignoring case of users/groups in the Targeting Filter.
     * @param ignoreCase if true case is ignored.
     * @return TargetingEvaluationOptions
     */
    public TargetingEvaluationOptions setIgnoreCase(boolean ignoreCase) {
        this.ignoreCase = ignoreCase;
        return this;
    }

}
