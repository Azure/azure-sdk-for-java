// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.storage.blob.models.CpkInfo;
import com.azure.storage.blob.models.CustomerProvidedKey;
import com.azure.storage.common.BaseClientBuilder;
import com.azure.storage.common.Constants;
import com.azure.storage.common.policy.ResponseValidationPolicyBuilder;

abstract class BaseBlobClientBuilder<T extends BaseClientBuilder<T>> extends BaseClientBuilder<T> {

    private static final String BLOB_ENDPOINT_MIDFIX = "blob";

    protected CpkInfo cpk;

    @SuppressWarnings("unchecked")
    public T customerProvidedKey(CustomerProvidedKey key) {
        cpk = new CpkInfo()
            .setEncryptionKey(key.getKey())
            .setEncryptionKeySha256(key.getKeySHA256())
            .setEncryptionAlgorithm(key.getEncryptionAlgorithm());

        return (T) this;
    }

    @Override
    protected final UserAgentPolicy getUserAgentPolicy() {
        return new UserAgentPolicy(BlobConfiguration.NAME, BlobConfiguration.VERSION, super.getConfiguration());
    }

    @Override
    protected final String getServiceUrlMidfix() {
        return BLOB_ENDPOINT_MIDFIX;
    }

    @Override
    protected final void applyServiceSpecificValidations(ResponseValidationPolicyBuilder builder) {
        // CPK
        builder.addOptionalEcho(Constants.HeaderConstants.ENCRYPTION_KEY_SHA256);
    }
}
