// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.provisioning;

import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusConsumerProperties;
import com.azure.spring.cloud.stream.binder.servicebus.core.properties.ServiceBusProducerProperties;

/**
 * An interface to provision Service Bus entity resources.
 */
public interface ServiceBusProvisioner {

    /**
     * Provision the namespace and queue of the Service Bus.
     * @param namespace the namespace of the Service Bus.
     * @param queue the queue of the Service Bus.
     * @deprecated use {@link #provisionQueue(String, String, ServiceBusProducerProperties)} instead.
     */
    @Deprecated
    void provisionQueue(String namespace, String queue);

    /**
     * Provision the namespace and topic of the Service Bus.
     * @param namespace the namespace of the Service Bus.
     * @param topic the topic of the Service Bus.
     * @deprecated use {@link #provisionTopic(String, String, ServiceBusProducerProperties)} instead.
     */
    @Deprecated
    void provisionTopic(String namespace, String topic);

    /**
     * Provision the namespace and queue of the Service Bus.
     * @param namespace the namespace of the Service Bus.
     * @param queue the queue of the Service Bus.
     * @param producerProperties the Service Bus producer properties.
     */
    void provisionQueue(String namespace, String queue, ServiceBusProducerProperties producerProperties);

    /**
     * Provision the namespace and topic of the Service Bus.
     * @param namespace the namespace of the Service Bus.
     * @param topic the topic of the Service Bus.
     * @param producerProperties the Service Bus producer properties.
     */
    void provisionTopic(String namespace, String topic, ServiceBusProducerProperties producerProperties);

    /**
     * Provision the namespace and topic of the Service Bus.
     * @param namespace the namespace of the Service Bus.
     * @param topic the topic of the Service Bus.
     * @param subscription the subscription of the topic.
     * @deprecated use {@link #provisionSubscription(String, String, String, ServiceBusConsumerProperties)} instead.
     */
    @Deprecated
    void provisionSubscription(String namespace, String topic, String subscription);

    /**
     * Provision the namespace and topic of the Service Bus.
     * @param namespace the namespace of the Service Bus.
     * @param topic the topic of the Service Bus.
     * @param subscription the subscription of the topic.
     * @param consumerProperties the Service Bus consumer properties.
     */
    void provisionSubscription(String namespace,
                               String topic,
                               String subscription,
                               ServiceBusConsumerProperties consumerProperties);
}
