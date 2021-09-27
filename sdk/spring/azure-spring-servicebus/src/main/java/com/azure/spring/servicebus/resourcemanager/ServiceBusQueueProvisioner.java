// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.resourcemanager;

/**
 * An interface to provision Service Bus queue resources.
 */
public interface ServiceBusQueueProvisioner {

    void provisionQueue(String namespace, String queue);

}
