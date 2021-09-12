// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.factory;

/**
 * An interface to provision Service Bus resources.
 */
public interface ServiceBusProvisioner {

    void provisionQueue(String namespace, String queue);

    void provisionTopic(String namespace, String topic);

    void provisionSubscription(String namespace, String topic, String subscription);

}
