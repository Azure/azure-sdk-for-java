// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.factory;

import javax.annotation.Nullable;

/**
 * Base class of service bus client factory to provide connection string
 *
 * @author Warren Zhu
 */
abstract class AbstractServiceBusSenderFactory implements ServiceBusSenderFactory {

    protected final String connectionString;

    @Nullable
    protected ServiceBusNamespaceManager serviceBusNamespaceManager;
    @Nullable
    protected ServiceBusQueueManager serviceBusQueueManager;
    @Nullable
    protected ServiceBusTopicManager serviceBusTopicManager;
    @Nullable
    protected ServiceBusTopicSubscriptionManager serviceBusTopicSubscriptionManager;
    @Nullable
    protected String namespace;

    AbstractServiceBusSenderFactory(String connectionString) {
        this.connectionString = connectionString;
    }

    public void setServiceBusNamespaceManager(ServiceBusNamespaceManager serviceBusNamespaceManager) {
        this.serviceBusNamespaceManager = serviceBusNamespaceManager;
    }

    public void setServiceBusQueueManager(ServiceBusQueueManager serviceBusQueueManager) {
        this.serviceBusQueueManager = serviceBusQueueManager;
    }

    public void setServiceBusTopicManager(ServiceBusTopicManager serviceBusTopicManager) {
        this.serviceBusTopicManager = serviceBusTopicManager;
    }

    public void setServiceBusTopicSubscriptionManager(
            ServiceBusTopicSubscriptionManager serviceBusTopicSubscriptionManager) {
        this.serviceBusTopicSubscriptionManager = serviceBusTopicSubscriptionManager;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getConnectionString() {
        return connectionString;
    }
}
