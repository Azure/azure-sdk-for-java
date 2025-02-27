// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.testcontainers.implementation.service.connection.servicebus;

import com.azure.spring.cloud.autoconfigure.implementation.servicebus.properties.AzureServiceBusConnectionDetails;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionDetailsFactory;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionSource;
import org.testcontainers.azure.ServiceBusEmulatorContainer;

class ServiceBusContainerConnectionDetailsFactory
    extends ContainerConnectionDetailsFactory<ServiceBusEmulatorContainer, AzureServiceBusConnectionDetails> {

    @Override
    protected AzureServiceBusConnectionDetails getContainerConnectionDetails(ContainerConnectionSource<ServiceBusEmulatorContainer> source) {
        return new ServiceBusContainerConnectionDetails(source);
    }

    /**
     * {@link AzureServiceBusConnectionDetails} backed by a {@link ContainerConnectionSource}.
     */
    private static class ServiceBusContainerConnectionDetails extends ContainerConnectionDetails<ServiceBusEmulatorContainer>
        implements AzureServiceBusConnectionDetails {

        ServiceBusContainerConnectionDetails(ContainerConnectionSource<ServiceBusEmulatorContainer> source) {
            super(source);
        }

        @Override
        public String getConnectionString() {
            return getContainer().getConnectionString();
        }
    }
}
