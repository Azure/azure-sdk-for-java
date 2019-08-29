package com.azure.storage.queue;

import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.storage.common.BaseClientBuilder;

abstract class BaseQueueClientBuilder<T extends BaseClientBuilder> extends BaseClientBuilder<T> {

    private static final String QUEUE_ENDPOINT_MIDFIX = "queue";

    @Override
    protected final UserAgentPolicy getUserAgentPolicy() {
        return new UserAgentPolicy(QueueConfiguration.NAME, QueueConfiguration.VERSION, super.getConfiguration());
    }

    @Override
    protected final String getServiceUrlMidfix() {
        return QUEUE_ENDPOINT_MIDFIX;
    }

    /**
     * UNSUPPORTED OPERATION: Azure Storage queue service does not support anonymous access.
     * Clears the credential used to authorize requests sent to the service
     *
     * @return the updated {@link T} object
     */
    public final T setAnonymousCredential() {
        throw new UnsupportedOperationException("Azure Storage file service does not support anonymous access.");
    }
}
