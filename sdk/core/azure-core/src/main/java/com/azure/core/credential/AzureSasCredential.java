// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.credential;

import com.azure.core.util.logging.ClientLogger;

import java.util.Objects;
import java.util.function.Function;

/**
 * Represents a credential that uses a shared access signature to authenticate to an Azure Service.
 */
public final class AzureSasCredential {
    private final ClientLogger logger = new ClientLogger(AzureSasCredential.class);
    private final Function<String, String> signatureEncoder;

    private volatile String signature;

    /**
     * Creates a credential that authorizes request with the given shared access signature.
     * <p>
     * The {@code signature} passed is assumed to be encoded. This constructor is effectively the same as calling {@link
     * #AzureSasCredential(String, Function) new AzureSasCredential(signature, null))}.
     *
     * @param signature The shared access signature used to authorize requests.
     * @throws NullPointerException If {@code signature} is {@code null}.
     * @throws IllegalArgumentException If {@code signature} is an empty string.
     */
    public AzureSasCredential(String signature) {
        this(signature, null);
    }

    /**
     * Creates a credential that authorizes request within the given shared access signature.
     * <p>
     * If {@code signatureEncoder} is non-null the {@code signature}, and all {@link #update(String) updated
     * signatures}, will be encoded using the function. {@code signatureEncoder} should be as idempotent as possible to
     * reduce the chance of double encoding errors.
     *
     * @param signature The shared access signature used to authorize requests.
     * @param signatureEncoder An optional function which encodes the {@code signature}.
     * @throws NullPointerException If {@code signature} is {@code null}.
     * @throws IllegalArgumentException If {@code signature} is an empty string.
     */
    public AzureSasCredential(String signature, Function<String, String> signatureEncoder) {
        Objects.requireNonNull(signature, "'signature' cannot be null.");
        if (signature.isEmpty()) {
            throw logger.logExceptionAsError(new IllegalArgumentException("'signature' cannot be empty."));
        }

        this.signatureEncoder = signatureEncoder;
        this.signature = (signatureEncoder == null) ? signature : signatureEncoder.apply(signature);
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

        this.signature = (signatureEncoder == null) ? signature : signatureEncoder.apply(signature);
        return this;
    }
}
