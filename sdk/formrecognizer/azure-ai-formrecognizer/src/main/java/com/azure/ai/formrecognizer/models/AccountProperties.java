// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.models;

import com.azure.core.annotation.Immutable;

/**
 * The AccountProperties model.
 */
@Immutable
public final class AccountProperties {

    /*
     * The current count of trained custom models.
     */
    private final int count;

    /*
     * Max number of models that can be trained for this account.
     */
    private final int limit;

    /**
     * Constructs an AccountProperties object.
     *
     * @param count The current count of trained custom models.
     * @param limit Max number of models that can be trained for this account.
     */
    public AccountProperties(final int count, final int limit) {
        this.count = count;
        this.limit = limit;
    }

    /**
     * Get the current count of trained custom models.
     *
     * @return the count value.
     */
    public int getCount() {
        return this.count;
    }

    /**
     * Get the max number of models that can be trained for
     * this account.
     *
     * @return the limit value.
     */
    public int getLimit() {
        return this.limit;
    }
}
