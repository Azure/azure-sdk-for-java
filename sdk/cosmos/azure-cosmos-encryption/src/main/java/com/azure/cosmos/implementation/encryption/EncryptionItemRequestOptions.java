// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

/**
 *
 * The {@link com.azure.cosmos.models.CosmosItemRequestOptions} that allows to specify options for encryption / decryption.
 */
package com.azure.cosmos.implementation.encryption;

import com.azure.cosmos.implementation.encryption.api.EncryptionOptions;
import com.azure.cosmos.models.CosmosItemRequestOptions;

import java.util.function.Consumer;

// TODO: how invoking setter methods chaining works? parent setters returns CosmosItemRequestOptions
// this class setter require EncryptionItemRequestOptions
public class EncryptionItemRequestOptions extends CosmosItemRequestOptions {

    private Consumer<DecryptionResult> decryptionResultHandler;
    private EncryptionOptions encryptionOptions;

    /**
     * Gets options to be provided for encryption of data.
     * @return EncryptionOptions
     */
    public EncryptionOptions getEncryptionOptions() {
        return encryptionOptions;
    }

    /**
     * Gets delegate method that will be invoked (if configured) in case of decryption failure.
     * @return Consumer<DecryptionResult>
     */
    public Consumer<DecryptionResult> getDecryptionResultHandler() {
        return decryptionResultHandler;
    }

    /**
     * Sets options to be provided for encryption of data.
     * @param encryptionOptions
     */
    public EncryptionItemRequestOptions setEncryptionOptions(EncryptionOptions encryptionOptions) {
        this.encryptionOptions = encryptionOptions;
        return this;
    }

    /**
     * Sets delegate method that will be invoked (if configured) in case of decryption failure.
     *
     * If DecryptionResultHandler is not configured, we throw exception.
     * If DecryptionResultHandler is configured, we invoke the delegate method and return the encrypted document as is (without decryption) in case of failure.
     *
     * @param decryptionResultHandler
     * @return the current request options
     */
    public EncryptionItemRequestOptions setDecryptionResultHandler(Consumer<DecryptionResult> decryptionResultHandler) {
        this.decryptionResultHandler = decryptionResultHandler;
        return this;
    }
}
