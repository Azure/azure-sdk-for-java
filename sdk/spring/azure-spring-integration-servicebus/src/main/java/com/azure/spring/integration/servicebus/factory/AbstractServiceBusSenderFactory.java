// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.factory;

import com.azure.spring.cloud.context.core.impl.ServiceBusNamespaceManager;
import com.azure.spring.cloud.context.core.impl.ServiceBusQueueManager;
import com.azure.spring.cloud.context.core.impl.ServiceBusTopicManager;
import com.azure.spring.cloud.context.core.impl.ServiceBusTopicSubscriptionManager;

import javax.annotation.Nullable;

/**
 * Base class of service bus client factory to provide connection string
 *
 * @author Warren Zhu
 */
abstract class AbstractServiceBusSenderFactory implements ServiceBusSenderFactory {

    /**
     * The connection string.
     */
    protected final String connectionString;

    /**
     * The service bus name space manager.
     */
    @Nullable
    protected ServiceBusNamespaceManager serviceBusNamespaceManager;

    /**
     * The service bus queue manager.
     */
    @Nullable
    protected ServiceBusQueueManager serviceBusQueueManager;

    /**
     * The service bus topic manager.
     */
    @Nullable
    protected ServiceBusTopicManager serviceBusTopicManager;

    /**
     * The service bus topic subscription manager.
     */
    @Nullable
    protected ServiceBusTopicSubscriptionManager serviceBusTopicSubscriptionManager;

    /**
     * The namespace.
     */
    @Nullable
    protected String namespace;

    /**
     *
     * @param connectionString The connection string.
     */
    AbstractServiceBusSenderFactory(String connectionString) {
        this.connectionString = connectionString;
    }

    /**
     *
     * @param serviceBusNamespaceManager The serviceBusNamespaceManager.
     */
    public void setServiceBusNamespaceManager(ServiceBusNamespaceManager serviceBusNamespaceManager) {
        this.serviceBusNamespaceManager = serviceBusNamespaceManager;
    }

    /**
     *
     * @param serviceBusQueueManager The serviceBusQueueManager.
     */
    public void setServiceBusQueueManager(ServiceBusQueueManager serviceBusQueueManager) {
        this.serviceBusQueueManager = serviceBusQueueManager;
    }

    /**
     *
     * @param serviceBusTopicManager The serviceBusTopicManager.
     */
    public void setServiceBusTopicManager(ServiceBusTopicManager serviceBusTopicManager) {
        this.serviceBusTopicManager = serviceBusTopicManager;
    }

    /**
     *
     * @param serviceBusTopicSubscriptionManager The serviceBusTopicSubscriptionManager.
     */
    public void setServiceBusTopicSubscriptionManager(
            ServiceBusTopicSubscriptionManager serviceBusTopicSubscriptionManager) {
        this.serviceBusTopicSubscriptionManager = serviceBusTopicSubscriptionManager;
    }

    /**
     *
     * @param namespace The namespace.
     */
    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    /**
     *
     * @return The connection string.
     */
    public String getConnectionString() {
        return connectionString;
    }
}
