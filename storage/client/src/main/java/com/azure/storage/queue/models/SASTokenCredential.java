// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue.models;

public class SASTokenCredential {
    private final String sharedKey;

    public SASTokenCredential(String sharedKey) {
        this.sharedKey = sharedKey;
    }

    public String sharedKey() {
        return sharedKey;
    }
}
