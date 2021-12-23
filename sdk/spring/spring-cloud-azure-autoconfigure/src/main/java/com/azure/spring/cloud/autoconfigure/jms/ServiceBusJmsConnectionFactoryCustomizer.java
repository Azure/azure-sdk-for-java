// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.autoconfigure.jms;

/**
 * An customizer for ServiceBusJmsConnectionFactory
 */
@FunctionalInterface
public interface ServiceBusJmsConnectionFactoryCustomizer {
    void customize(ServiceBusJmsConnectionFactory factory);
}
