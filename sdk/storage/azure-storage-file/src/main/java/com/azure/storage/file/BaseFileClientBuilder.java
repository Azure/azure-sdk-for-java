// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.file;

import com.azure.core.credential.TokenCredential;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.BaseClientBuilder;
import com.azure.storage.common.policy.ResponseValidationPolicyBuilder;

abstract class BaseFileClientBuilder<T extends BaseClientBuilder<T>> extends BaseClientBuilder<T> {

    private static final String FILE_ENDPOINT_MIDFIX = "file";

    private final ClientLogger logger = new ClientLogger(BaseFileClientBuilder.class);

    @Override
    protected final UserAgentPolicy getUserAgentPolicy() {
        return new UserAgentPolicy(FileConfiguration.NAME, FileConfiguration.VERSION, super.getConfiguration());
    }

    @Override
    protected final String getServiceUrlMidfix() {
        return FILE_ENDPOINT_MIDFIX;
    }

    /**
     * UNSUPPORTED OPERATION: Azure Storage file service does not support token authorization.
     * Sets the credential used to authorize requests sent to the service
     *
     * @param credential authorization credential
     * @return the updated builder
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    @Override
    public final T credential(TokenCredential credential) {
        throw logger.logExceptionAsError(new UnsupportedOperationException(
            "Azure Storage file service does not support token authorization."));
    }

    /**
     * UNSUPPORTED OPERATION: Azure Storage file service does not support anonymous access.
     * Clears the credential used to authorize requests sent to the service
     *
     * @return the updated builder
     */
    @Override
    public final T setAnonymousCredential() {
        throw logger.logExceptionAsError(new UnsupportedOperationException(
            "Azure Storage file service does not support anonymous access."));
    }

    @Override
    protected final void applyServiceSpecificValidations(ResponseValidationPolicyBuilder builder) {
        // for file service validations
    }
}
