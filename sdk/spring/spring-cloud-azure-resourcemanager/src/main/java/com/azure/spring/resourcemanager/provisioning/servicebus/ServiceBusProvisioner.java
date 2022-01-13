// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.resourcemanager.provisioning.servicebus;

/**
 * An interface to provision Service Bus queue resources.
 */
public interface ServiceBusProvisioner {

    void provisionQueue(String namespace, String queue);

    void provisionTopic(String namespace, String topic);

    void provisionSubscription(String namespace, String topic, String subscription);

}
