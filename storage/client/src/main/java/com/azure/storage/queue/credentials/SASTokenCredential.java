// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.storage.queue.credentials;

/**
 * Holds a SAS token used for authenticating requests.
 */
public final class SASTokenCredential {
    private final String sasToken;

    public SASTokenCredential(String sharedKey) {
        this.sasToken = sharedKey;
    }

    public String sasToken() {
        return sasToken;
    }
}
