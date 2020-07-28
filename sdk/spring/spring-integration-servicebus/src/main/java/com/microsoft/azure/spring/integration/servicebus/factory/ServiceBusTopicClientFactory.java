/*
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See LICENSE in the project root for
 * license information.
 */

package com.microsoft.azure.spring.integration.servicebus.factory;

import com.microsoft.azure.servicebus.ISubscriptionClient;

/**
 * Factory to return functional creator of service bus topic and subscription client
 *
 * @author Warren Zhu
 */
public interface ServiceBusTopicClientFactory extends ServiceBusSenderFactory {
    /**
     * Return a function which accepts service bus topic and subscription name, then returns {@link ISubscriptionClient}
     */
    ISubscriptionClient getOrCreateSubscriptionClient(String topic, String subscription);
}
