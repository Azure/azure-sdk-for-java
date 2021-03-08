// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.credential;

import com.azure.core.util.logging.ClientLogger;

import java.util.Objects;

/**
 * Represents a credential that uses a shared access signature to authenticate to an Azure Service.
 */
public final class AzureSasCredential {
    private final ClientLogger logger = new ClientLogger(AzureSasCredential.class);
    private volatile String signature;

    /**
     * Creates a credential that authorizes request with the given shared access signature.
     *
     * @param signature The shared access signature used to authorize requests.
     * @throws NullPointerException If {@code signature} is {@code null}.
     * @throws IllegalArgumentException If {@code signature} is an empty string.
     */
    public AzureSasCredential(String signature) {
        Objects.requireNonNull(signature, "'signature' cannot be null.");
        if (signature.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'signature' cannot be empty."));
        }

        this.signature = signature;
    }

    /**
     * Retrieves the shared access signature associated to this credential.
     *
     * @return The shared access signature being used to authorize requests.
     */
    public String getSignature() {
        return signature;
    }

    /**
     * Rotates the shared access signature associated to this credential.
     *
     * @param signature The new shared access signature to be associated with this credential.
     * @return The updated {@code AzureSasCredential} object.
     * @throws NullPointerException If {@code signature} is {@code null}.
     * @throws IllegalArgumentException If {@code signature} is an empty string.
     */
    public AzureSasCredential update(String signature) {
        Objects.requireNonNull(signature, "'signature' cannot be null.");
        if (signature.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'signature' cannot be empty."));
        }

        this.signature = signature;
        return this;
    }
}
