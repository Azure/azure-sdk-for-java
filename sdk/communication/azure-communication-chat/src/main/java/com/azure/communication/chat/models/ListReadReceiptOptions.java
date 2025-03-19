// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.chat.models;

import com.azure.core.annotation.Fluent;

/**
 * Additional parameters for list read receipts operation.
 */
@Fluent
public final class ListReadReceiptOptions {
    private Integer maxPageSize;
    private Integer skip;

    /**
     * Gets the maximum page size. It represents the number of read receipts being requested.
     *
     * @return The maximum page size.
     */
    public Integer getMaxPageSize() {
        return maxPageSize;
    }

    /**
     * Set the maximum page size. It represents the number of read receipts being requested.
     *
     * @param maxPageSize The maximum page size.
     * @return The {@link ListReadReceiptOptions} object itself.
     */
    public ListReadReceiptOptions setMaxPageSize(Integer maxPageSize) {
        this.maxPageSize = maxPageSize;
        return this;
    }

    /**
     * Gets the skip for the range to query.
     *
     * @return The skip.
     */
    public Integer getSkip() {
        return skip;
    }

    /**
     * Sets the skip for the range to query.
     *
     * @param skip The number of items to skip.
     * @return The {@link ListReadReceiptOptions} object itself.
     */
    public ListReadReceiptOptions setSkip(Integer skip) {
        this.skip = skip;
        return this;
    }
}
