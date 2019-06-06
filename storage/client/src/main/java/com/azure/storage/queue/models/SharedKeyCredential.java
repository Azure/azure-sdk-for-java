// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue.models;

public class SharedKeyCredential {
    private final String sharedKey;

    public SharedKeyCredential(String sharedKey) {
        this.sharedKey = sharedKey;
    }

    public String sharedKey() {
        return sharedKey;
    }
}
