// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.provisioning;

/**
 * An interface to provision Service Bus queue resources.
 */
public interface ServiceBusProvisioner {

    /**
     * Provision the namespace and queue of the Service Bus.
     * @param namespace the namespace of the Service Bus.
     * @param queue the queue of the Service Bus.
     */
    void provisionQueue(String namespace, String queue);

    /**
     * Provision the namespace and topic of the Service Bus.
     * @param namespace the namespace of the Service Bus.
     * @param topic the topic of the Service Bus.
     */
    void provisionTopic(String namespace, String topic);

    /**
     * Provision the namespace and topic of the Service Bus.
     * @param namespace the namespace of the Service Bus.
     * @param topic the topic of the Service Bus.
     * @param subscription the subscription of the topic.
     */
    void provisionSubscription(String namespace, String topic, String subscription);

}
