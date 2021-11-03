// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.service.storage.common;

import com.azure.spring.core.properties.retry.RetryProperties;
import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RetryPolicyType;
import org.springframework.core.convert.converter.Converter;

import java.time.Duration;

/**
 * Converts a {@link StorageRetryProperties} to a {@link RequestRetryOptions}.
 */
public final class AzureRequestRetryOptionsConverter implements Converter<StorageRetryProperties, RequestRetryOptions> {

    @Override
    public RequestRetryOptions convert(StorageRetryProperties retryProperties) {
        if (retryProperties == null) {
            return new RequestRetryOptions();
        }

        RetryPolicyType retryPolicyType = null;
        Duration delay = null;
        Duration maxDelay = null;
        final RetryProperties.BackoffProperties backoffProperties = retryProperties.getBackoff();
        if (backoffProperties != null) {
            if (backoffProperties.getMultiplier() != null && backoffProperties.getMultiplier() > 0) {
                retryPolicyType = RetryPolicyType.EXPONENTIAL;
            } else {
                retryPolicyType = RetryPolicyType.FIXED;
            }
            delay = backoffProperties.getDelay();
            maxDelay = backoffProperties.getMaxDelay();
        }
        return new RequestRetryOptions(retryPolicyType, retryProperties.getMaxAttempts(),
            retryProperties.getTimeout(), delay, maxDelay, retryProperties.getSecondaryHost());
    }
}
