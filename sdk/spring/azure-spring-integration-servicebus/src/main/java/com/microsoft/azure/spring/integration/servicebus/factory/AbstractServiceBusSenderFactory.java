// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.microsoft.azure.spring.integration.servicebus.factory;

import com.microsoft.azure.spring.cloud.context.core.api.ResourceManagerProvider;

/**
 * Base class of service bus client factory to provide connection string
 *
 * @author Warren Zhu
 */
abstract class AbstractServiceBusSenderFactory implements ServiceBusSenderFactory {
    protected final String connectionString;
    protected String namespace;
    protected ResourceManagerProvider resourceManagerProvider;

    AbstractServiceBusSenderFactory(String connectionString) {
        this.connectionString = connectionString;
    }

    public void setResourceManagerProvider(ResourceManagerProvider resourceManagerProvider) {
        this.resourceManagerProvider = resourceManagerProvider;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }
}
