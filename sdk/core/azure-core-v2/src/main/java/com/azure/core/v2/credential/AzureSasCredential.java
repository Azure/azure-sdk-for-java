// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.v2.credential;

import io.clientcore.core.util.ClientLogger;

import java.util.Objects;
import java.util.function.Function;

/**
 * <p>
 * Represents a credential that uses a shared access signature to authenticate to an Azure Service.
 * It is used for authenticating and authorizing access to Azure services using a shared access signature.
 * </p>
 *
 * <p>
 * A shared access signature is a string-based token that grants limited permissions and access to specific
 * resources within an Azure service for a specified period. It allows you to provide time-limited access to your
 * resources without sharing your account key or other sensitive credentials.
 * </p>
 *
 * <p>
 * The {@link AzureSasCredential} enables you to authenticate and access Azure services that
 * support shared access signatures. By creating an instance of the {@link AzureSasCredential} class and providing the
 * SAS token as a parameter, you can use this credential to authenticate requests to Azure services.
 * </p>
 *
 * <p>
 * To use the Credential, you typically pass it to the appropriate Azure client or service client
 * builder during instantiation. The library internally handles the authentication process and includes the
 * SAS token in the HTTP requests made to the Azure service, allowing you to access the resources specified in
 * the SAS token.
 * </p>
 *
 * <p>
 * The {@link AzureSasCredential} is particularly useful when you need to grant temporary and limited access to
 * specific resources, such as Azure Storage containers, blobs, queues, or files, without exposing
 * your account key.
 * </p>
 *
 * <p>
 * It's important to note that the availability and usage of the {@link AzureSasCredential} may depend on the
 * specific Azure service and its support for shared access signatures. Additionally, the format and content of the
 * SAS token may vary depending on the service and resource you are targeting.
 * </p>
 *
 * <p>
 * <strong>Sample: Azure SAS Authentication</strong>
 * </p>
 *
 * <p>
 * The following code sample demonstrates the creation of a {@link com.azure.core.v2.credential.AzureSasCredential},
 * using the sas token to configure it.
 * </p>
 *
 * <!-- src_embed com.azure.core.credential.azureSasCredential -->
 * <!-- end com.azure.core.credential.azureSasCredential -->
 *
 * @see com.azure.core.credential
 */
public final class AzureSasCredential {
    // AzureSasCredential is a commonly used credential type, use a static logger.
    private static final ClientLogger LOGGER = new ClientLogger(AzureSasCredential.class);
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
            throw LOGGER.logThrowableAsError(new IllegalArgumentException("'signature' cannot be empty."));
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
            throw LOGGER.logThrowableAsError(new IllegalArgumentException("'signature' cannot be empty."));
        }

        this.signature = (signatureEncoder == null) ? signature : signatureEncoder.apply(signature);
        return this;
    }
}
