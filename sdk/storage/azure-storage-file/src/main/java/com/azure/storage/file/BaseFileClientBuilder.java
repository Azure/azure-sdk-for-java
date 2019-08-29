package com.azure.storage.file;

import com.azure.core.credentials.TokenCredential;
import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.storage.common.BaseClientBuilder;

abstract class BaseFileClientBuilder<T extends BaseClientBuilder> extends BaseClientBuilder<T> {

    private static final String FILE_ENDPOINT_MIDFIX = "file";

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
     * @return the updated {@link T} object
     * @throws NullPointerException If {@code credential} is {@code null}.
     */
    public final T credential(TokenCredential credential) {
        throw new UnsupportedOperationException("Azure Storage file service does not support token authorization.");
    }

    /**
     * UNSUPPORTED OPERATION: Azure Storage file service does not support anonymous access.
     * Clears the credential used to authorize requests sent to the service
     *
     * @return the updated {@link T} object
     */
    public final T setAnonymousCredential() {
        throw new UnsupportedOperationException("Azure Storage file service does not support anonymous access.");
    }
}
