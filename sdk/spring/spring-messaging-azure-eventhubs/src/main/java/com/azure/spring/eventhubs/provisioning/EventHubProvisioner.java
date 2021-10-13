// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhubs.provisioning;

/**
 * An interface to provision Event Hubs resources.
 */
public interface EventHubProvisioner {

    void provisionNamespace(String namespace);

    void provisionEventHub(String namespace, String eventHub);

    void provisionConsumerGroup(String namespace, String eventHub, String consumerGroup);

}
