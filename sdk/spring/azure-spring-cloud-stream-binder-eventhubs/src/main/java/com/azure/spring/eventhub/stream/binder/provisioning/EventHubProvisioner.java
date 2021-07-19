// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.eventhub.stream.binder.provisioning;

/**
 *
 */
public interface EventHubProvisioner {

    void provisionEventHubNamespace(String namespace);

    void provisionEventHub(String namespace, String eventHub);

    void provisionConsumerGroup(String namespace, String eventHub, String consumerGroup);

}
