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
     * Returns the maximum time to wait for in-flight handlers to complete during processor
     * shutdown. The Spring property surface mirrors the new
     * {@code ServiceBusProcessorClientBuilder.drainTimeout(Duration)} setter so that the
     * spring-cloud-azure-service structural test stays in sync with the SDK builder API.
     *
     * <p><strong>Note:</strong> the corresponding builder factory wiring
     * ({@code propertyMapper.from(properties.getDrainTimeout()).to(builder::drainTimeout)})
     * is intentionally omitted in this PR because the Spring modules still pin
     * {@code com.azure:azure-messaging-servicebus} at the previous released version, which does
     * not expose {@code builder::drainTimeout}. The wiring will be added in a follow-up PR after
     * the Spring servicebus dependency is bumped to the release that ships
     * {@code drainTimeout(Duration)}.</p>
     *
     * @return the configured drain timeout, or {@code null} if not set.
     */
    Duration getDrainTimeout();

}
