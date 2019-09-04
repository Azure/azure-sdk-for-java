// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.queue;

import com.azure.core.http.policy.UserAgentPolicy;
import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.BaseClientBuilder;
import com.azure.storage.common.policy.ResponseValidationPolicyBuilder;

abstract class BaseQueueClientBuilder<T extends BaseClientBuilder<T>> extends BaseClientBuilder<T> {

    private static final String QUEUE_ENDPOINT_MIDFIX = "queue";

    private final ClientLogger logger = new ClientLogger(BaseQueueClientBuilder.class);

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
     * @return the updated builder
     */
    public final T setAnonymousCredential() {
        throw logger.logExceptionAsError(new UnsupportedOperationException(
            "Azure Storage file service does not support anonymous access."));
    }

    @Override
    protected final void applyServiceSpecificValidations(ResponseValidationPolicyBuilder builder) {
        // for queue service validations
    }
}
