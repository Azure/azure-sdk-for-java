// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.service.implementation.servicebus.properties;

import java.time.Duration;

/**
 *
 */
public interface ServiceBusProcessorClientProperties extends ServiceBusReceiverClientProperties {

    Integer getMaxConcurrentCalls();

    Integer getMaxConcurrentSessions();

    Duration getSessionIdleTimeout();

    /**
     * Returns the maximum time the processor will wait for in-flight message handlers to
     * complete before disposing the underlying receiver during shutdown. Mirrors the
     * {@code drainTimeout(Duration)} setter on the underlying Service Bus processor builder.
     *
     * @return the configured drain timeout, or {@code null} if not set (the underlying SDK
     *     applies its own default in that case).
     */
    Duration getDrainTimeout();

}
