// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.properties;

import com.azure.spring.core.properties.retry.HttpRetryProperties;
import com.azure.spring.core.properties.retry.RetryProperties;
import org.springframework.boot.context.properties.NestedConfigurationProperty;

/**
 *
 */
public abstract class AbstractAzureAmqpConfigurationProperties extends AbstractAzureServiceConfigurationProperties {

    @NestedConfigurationProperty
    protected final AmqpClientConfigurationProperties client = new AmqpClientConfigurationProperties();

    @NestedConfigurationProperty
    protected final RetryProperties retry = new RetryProperties();

    @Override
    public AmqpClientConfigurationProperties getClient() {
        return client;
    }

    @Override
    public RetryProperties getRetry() {
        return retry;
    }
}
