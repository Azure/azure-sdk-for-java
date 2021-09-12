// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.integration.servicebus.factory;

import com.azure.messaging.servicebus.ServiceBusClientBuilder;

import javax.annotation.Nullable;

/**
 * Base class of service bus client factory to provide connection string
 *
 * @author Warren Zhu
 */
abstract class AbstractServiceBusSenderFactory implements ServiceBusSenderFactory {

    @Nullable
    protected ServiceBusProvisioner serviceBusProvisioner;

    protected final ServiceBusClientBuilder serviceBusClientBuilder;

    public AbstractServiceBusSenderFactory(ServiceBusClientBuilder serviceBusClientBuilder) {
        this.serviceBusClientBuilder = serviceBusClientBuilder;
    }

    public void setServiceBusProvisioner(@Nullable ServiceBusProvisioner serviceBusProvisioner) {
        this.serviceBusProvisioner = serviceBusProvisioner;
    }

}
