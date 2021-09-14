// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.servicebus.core;

import com.azure.spring.servicebus.support.ServiceBusProvisioner;

import javax.annotation.Nullable;

/**
 * Base class of service bus client factory to provide connection string
 *
 * @author Warren Zhu
 */
abstract class AbstractServiceBusSenderFactory implements ServiceBusSenderFactory {

    protected final String connectionString;

    @Nullable
    protected ServiceBusProvisioner serviceBusProvisioner;

    @Nullable
    protected String namespace;

    AbstractServiceBusSenderFactory(String connectionString) {
        this.connectionString = connectionString;
    }

    public void setServiceBusProvisioner(@Nullable ServiceBusProvisioner serviceBusProvisioner) {
        this.serviceBusProvisioner = serviceBusProvisioner;
    }

    public void setNamespace(String namespace) {
        this.namespace = namespace;
    }

    public String getConnectionString() {
        return connectionString;
    }
}
