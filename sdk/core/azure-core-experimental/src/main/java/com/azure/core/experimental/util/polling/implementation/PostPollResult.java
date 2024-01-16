// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.experimental.util.polling.implementation;

import com.azure.core.util.BinaryData;

public final class PostPollResult {
    private BinaryData result;

    public BinaryData getResult() {
        return result;
    }

    public void setResult(BinaryData result) {
        this.result = result;
    }
}
