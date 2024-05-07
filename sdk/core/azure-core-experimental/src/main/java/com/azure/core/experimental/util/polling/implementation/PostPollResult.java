// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.util.polling.implementation;

import com.azure.core.util.BinaryData;

/**
 * The result of a POST request to a long-running operation.
 */
public final class PostPollResult {
    private BinaryData result;

    /**
     * Gets the POST poll result.
     *
     * @return The POST poll result.
     */
    public BinaryData getResult() {
        return result;
    }

    /**
     * Sets the POST poll result.
     *
     * @param result The POST poll result.
     */
    public void setResult(BinaryData result) {
        this.result = result;
    }
}
