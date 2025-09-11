// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.implementation.provisioning;

import com.azure.spring.cloud.resourcemanager.implementation.provisioning.properties.ServiceBusQueueProperties;
import com.azure.spring.cloud.resourcemanager.implementation.provisioning.properties.ServiceBusTopicProperties;

/**
 * An interface to provision Service Bus entity resources.
 */
public interface ServiceBusProvisioner {

    /**
     * Provision the namespace and queue of the Service Bus.
     * @param namespace the namespace of the Service Bus.
     * @param queue the queue of the Service Bus.
     * @deprecated use {@link #provisionQueue(String, String, ServiceBusQueueProperties)} instead.
     */
    @Deprecated
    void provisionQueue(String namespace, String queue);

    /**
     * Provision the namespace and topic of the Service Bus.
     * @param namespace the namespace of the Service Bus.
     * @param topic the topic of the Service Bus.
     * @deprecated use {@link #provisionTopic(String, String, ServiceBusTopicProperties)} instead.
     */
    @Deprecated
    void provisionTopic(String namespace, String topic);

    /**
     * Provision the namespace and queue of the Service Bus.
     * @param namespace the namespace of the Service Bus.
     * @param queue the queue of the Service Bus.
     * @param queueProperties the queue properties of the Service Bus.
     */
    void provisionQueue(String namespace, String queue, ServiceBusQueueProperties queueProperties);

    /**
     * Provision the namespace and topic of the Service Bus.
     * @param namespace the namespace of the Service Bus.
     * @param topic the topic of the Service Bus.
     * @param topicProperties the topic properties of the Service Bus.
     */
    void provisionTopic(String namespace, String topic, ServiceBusTopicProperties topicProperties);

    /**
     * Provision the namespace and topic of the Service Bus.
     * @param namespace the namespace of the Service Bus.
     * @param topic the topic of the Service Bus.
     * @param subscription the subscription of the topic.
     * @deprecated use {@link #provisionSubscription(String, String, String, ServiceBusTopicProperties)} instead.
     */
    @Deprecated
    void provisionSubscription(String namespace, String topic, String subscription);

    /**
     * Provision the namespace and topic of the Service Bus.
     * @param namespace the namespace of the Service Bus.
     * @param topic the topic of the Service Bus.
     * @param subscription the subscription of the topic.
     * @param topicProperties the topic properties of the Service Bus.
     */
    void provisionSubscription(String namespace,
                               String topic,
                               String subscription,
                               ServiceBusTopicProperties topicProperties);
}
