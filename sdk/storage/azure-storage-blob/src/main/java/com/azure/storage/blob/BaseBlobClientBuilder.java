// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.blob;

import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.storage.blob.models.StorageException;
import com.azure.storage.common.BaseClientBuilder;
import com.azure.storage.common.Constants;
import com.azure.storage.common.policy.ResponseValidationPolicyBuilder;

abstract class BaseBlobClientBuilder<T extends BaseClientBuilder<T>> extends BaseClientBuilder<T> {

    private static final String BLOB_ENDPOINT_MIDFIX = "blob";

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
        builder
            .addOptionalEcho(Constants.HeaderConstants.ENCRYPTION_KEY_SHA256)
            .addCustomAssertion((response, logger) -> {
                // if this was a CPK request
                if (response.request().headers().value(Constants.HeaderConstants.ENCRYPTION_KEY) != null) {
                    if (!Boolean.parseBoolean(response.headerValue(Constants.HeaderConstants.REQUEST_SERVER_ENCRYPTED))) {
                        throw logger.logExceptionAsError(new RuntimeException(String.format(
                            "Unexpected response header of `%s: %s`. Expected value `%s`.",
                            Constants.HeaderConstants.REQUEST_SERVER_ENCRYPTED, "false", "true"
                        )));
                    }
                }
            });
    }
}
