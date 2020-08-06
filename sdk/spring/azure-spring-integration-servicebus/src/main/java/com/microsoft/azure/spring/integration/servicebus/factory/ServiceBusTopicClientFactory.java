// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

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
     * @param topic topic
     * @param subscription subscription
     * @return subscription client
     */
    ISubscriptionClient getOrCreateSubscriptionClient(String topic, String subscription);
}
