// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.storage.blob.models.CpkInfo;
import com.azure.storage.blob.models.CustomerProvidedKey;
import com.azure.storage.common.BaseClientBuilder;
import com.azure.storage.common.Constants;
import com.azure.storage.common.policy.ResponseValidationPolicyBuilder;

/**
 * RESERVED FOR INTERNAL USE ONLY
 * Base builder for Azure Storage Blobs.
 * @param <T> Generic type that extends {@link BaseClientBuilder}.
 */
public abstract class BaseBlobClientBuilder<T extends BaseClientBuilder<T>> extends BaseClientBuilder<T> {

    private static final String BLOB_ENDPOINT_MIDFIX = "blob";

    protected CpkInfo customerProvidedKey;

    /**
     * Sets the {@link CustomerProvidedKey customer provided key} that is used to encrypt blob contents on the server.
     *
     * @param key Customer provided key containing the encryption key
     * @return the updated builder object
     */
    public T customerProvidedKey(CustomerProvidedKey key) {
        if (key == null) {
            customerProvidedKey = null;
        } else {
            customerProvidedKey = new CpkInfo()
                .setEncryptionKey(key.getKey())
                .setEncryptionKeySha256(key.getKeySHA256())
                .setEncryptionAlgorithm(key.getEncryptionAlgorithm());
        }

        return getClazz().cast(this);
    }

    /**
     * Gets the {@link UserAgentPolicy user agent policy} that is used to set the User-Agent header for each request.
     *
     * @return the {@code UserAgentPolicy} that will be used in the {@link HttpPipeline}.
     */
    @Override
    protected final UserAgentPolicy getUserAgentPolicy() {
        return new UserAgentPolicy(BlobConfiguration.NAME, BlobConfiguration.VERSION, super.getConfiguration());
    }

    /**
     * Gets the midfix used to create the resource URL.
     *
     * @return the Azure Storage Blob midfix.
     */
    @Override
    protected final String getServiceUrlMidfix() {
        return BLOB_ENDPOINT_MIDFIX;
    }

    /**
     * Configures the response validation rules that are applied to each request/response.
     *
     * @param builder Builder to assemble assertions together.
     */
    @Override
    protected final void applyServiceSpecificValidations(ResponseValidationPolicyBuilder builder) {
        // CPK
        builder.addOptionalEcho(Constants.HeaderConstants.ENCRYPTION_KEY_SHA256);
    }
}
