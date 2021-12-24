// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.resourcemanager.provisioner.eventhubs;

/**
 * An interface to provision Event Hubs resources.
 */
public interface EventHubsProvisioner {

    void provisionNamespace(String namespace);

    void provisionEventHub(String namespace, String eventHub);

    void provisionConsumerGroup(String namespace, String eventHub, String consumerGroup);

}
