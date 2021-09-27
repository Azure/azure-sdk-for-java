// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.provisioning;

/**
 * An interface to provision Service Bus topic resources.
 */
public interface ServiceBusTopicProvisioner {

    void provisionTopic(String namespace, String topic);

    void provisionSubscription(String namespace, String topic, String subscription);

}
