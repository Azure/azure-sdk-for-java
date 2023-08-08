// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.resourcemanager.provisioning;

/**
 * An interface to provision Event Hubs resources.
 */
public interface EventHubsProvisioner {

    /**
     * Provision the namespace of the Event Hubs.
     * @param namespace the namespace of the Event Hubs.
     */
    void provisionNamespace(String namespace);

    /**
     * Provision the namespace and event hub of the Event Hubs.
     * @param namespace the namespace of the Event Hubs.
     * @param eventHub the event hub of the Event Hubs.
     */
    void provisionEventHub(String namespace, String eventHub);

    /**
     * Provision the namespace, event hub and consumer group of the Event Hubs.
     * @param namespace the namespace of the Event Hubs.
     * @param eventHub the event hub of the Event Hubs.
     * @param consumerGroup the consumer of the event hub.
     */
    void provisionConsumerGroup(String namespace, String eventHub, String consumerGroup);

}
