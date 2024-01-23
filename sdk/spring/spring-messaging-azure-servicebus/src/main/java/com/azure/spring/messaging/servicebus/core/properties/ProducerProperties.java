// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.messaging.servicebus.core.properties;

import com.azure.spring.cloud.service.implementation.servicebus.properties.ServiceBusSenderClientProperties;

/**
 * A service bus producer related properties.
 */
public class ProducerProperties extends CommonProperties implements ServiceBusSenderClientProperties {

    /**
     * Create an instance of {@link ProducerProperties}.
     */
    public ProducerProperties() {
    }
}
