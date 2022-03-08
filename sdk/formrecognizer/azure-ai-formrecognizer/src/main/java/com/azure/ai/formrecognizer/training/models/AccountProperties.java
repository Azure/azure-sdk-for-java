// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.training.models;

import com.azure.core.annotation.Immutable;

/**
 * The AccountProperties model.
 */
@Immutable
public final class AccountProperties {

    /*
     * The current count of trained custom models.
     */
    private final int customModelCount;

    /*
     * Max number of models that can be trained for this account.
     */
    private final int customModelLimit;

    /**
     * Constructs an AccountProperties object.
     *
     * @param customModelCount The current count of trained custom models.
     * @param customModelLimit Max number of models that can be trained for this account.
     */
    public AccountProperties(final int customModelCount, final int customModelLimit) {
        this.customModelCount = customModelCount;
        this.customModelLimit = customModelLimit;
    }

    /**
     * Get the current count of trained custom models.
     *
     * @return the count value.
     */
    public int getCustomModelCount() {
        return this.customModelCount;
    }

    /**
     * Get the max number of models that can be trained for
     * this account.
     *
     * @return the limit value.
     */
    public int getCustomModelLimit() {
        return this.customModelLimit;
    }
}
