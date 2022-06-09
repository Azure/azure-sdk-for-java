// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.policy

import com.azure.core.http.policy.ExponentialBackoffOptions
import com.azure.core.http.policy.FixedDelayOptions
import com.azure.core.http.policy.RetryOptions
import spock.lang.Specification

import java.time.Duration

class RequestRetryOptionsTest extends Specification {

    def "RetryOptions to RequestRetryOptions mapping"() {
        when:
        RequestRetryOptions.fromRetryOptions(null, null, null)

        then:
        thrown(NullPointerException.class)

        when:
        def coreOptions = new RetryOptions(new FixedDelayOptions(3, Duration.ofSeconds(4)))
        def storageOptions = RequestRetryOptions.fromRetryOptions(coreOptions, Duration.ofSeconds(12), "secondaryHost")

        then:
        storageOptions.maxTries == coreOptions.fixedDelayOptions.maxRetries + 1
        storageOptions.retryDelay == coreOptions.fixedDelayOptions.delay
        storageOptions.tryTimeoutDuration == Duration.ofSeconds(12)
        storageOptions.secondaryHost == "secondaryHost"
        storageOptions.calculateDelayInMs(2) == coreOptions.fixedDelayOptions.delay.toMillis()
        storageOptions.calculateDelayInMs(3) == coreOptions.fixedDelayOptions.delay.toMillis()

        when:
        coreOptions = new RetryOptions(
            new ExponentialBackoffOptions()
                .setMaxRetries(3)
                .setBaseDelay(Duration.ofSeconds(4))
                .setMaxDelay(Duration.ofSeconds(10)))
        storageOptions = RequestRetryOptions.fromRetryOptions(coreOptions, Duration.ofSeconds(12), "secondaryHost")

        then:
        storageOptions.maxTries == coreOptions.exponentialBackoffOptions.maxRetries + 1
        storageOptions.retryDelay == coreOptions.exponentialBackoffOptions.baseDelay
        storageOptions.maxRetryDelay == coreOptions.exponentialBackoffOptions.maxDelay
        storageOptions.tryTimeoutDuration == Duration.ofSeconds(12)
        storageOptions.secondaryHost == "secondaryHost"
        storageOptions.calculateDelayInMs(2) == coreOptions.exponentialBackoffOptions.baseDelay.toMillis()
        storageOptions.calculateDelayInMs(20) == coreOptions.exponentialBackoffOptions.maxDelay.toMillis()
    }
}
