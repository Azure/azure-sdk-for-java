// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cosmos.encryption;

import com.azure.cosmos.models.CosmosQueryRequestOptions;

import java.util.function.Consumer;

public class EncryptionQueryRequestOptions extends CosmosQueryRequestOptions {
    private Consumer<DecryptionResult> decryptionResultHandler;

    /**
     * Sets delegate method that will be invoked (if configured) in case of decryption failure.
     *
     * If DecryptionResultHandler is not configured, we throw exception.
     * If DecryptionResultHandler is configured, we invoke the delegate method and return the encrypted document as is (without decryption) in case of failure.
     *
     * @param decryptionResultHandler decryption result handler.
     * @return the current request options
     */
    public EncryptionQueryRequestOptions setDecryptionResultHandler(Consumer<DecryptionResult> decryptionResultHandler) {
        this.decryptionResultHandler = decryptionResultHandler;
        return this;
    }

    /**
     * Gets delegate method that will be invoked (if configured) in case of decryption failure.
     * @return Consumer of DecryptionResult
     */
    public Consumer<DecryptionResult> getDecryptionResultHandler() {
        return decryptionResultHandler;
    }
}

// TODO: moderakh should we implement all the parent class apis here?

