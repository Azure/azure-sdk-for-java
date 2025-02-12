// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.testcontainers.implementation.service.connection.servicebus;

import com.azure.spring.cloud.autoconfigure.implementation.servicebus.properties.AzureServiceBusConnectionDetails;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionDetailsFactory;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionSource;
import org.testcontainers.containers.Container;

class ServiceBusContainerConnectionDetailsFactory
    extends ContainerConnectionDetailsFactory<Container<?>, AzureServiceBusConnectionDetails> {

    private static final int SERVICE_BUS_PORT = 5672;

    ServiceBusContainerConnectionDetailsFactory() {
        super("azure-messaging/servicebus-emulator");
    }

    @Override
    protected AzureServiceBusConnectionDetails getContainerConnectionDetails(ContainerConnectionSource<Container<?>> source) {
        return new ServiceBusContainerConnectionDetails(source);
    }

    /**
     * {@link AzureServiceBusConnectionDetails} backed by a {@link ContainerConnectionSource}.
     */
    private static class ServiceBusContainerConnectionDetails extends ContainerConnectionDetails<Container<?>>
        implements AzureServiceBusConnectionDetails {

        ServiceBusContainerConnectionDetails(ContainerConnectionSource<Container<?>> source) {
            super(source);
        }

        @Override
        public String getConnectionString() {
            return "Endpoint=sb://%s:%d;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=SAS_KEY_VALUE;UseDevelopmentEmulator=true;"
                .formatted(getContainer().getHost(), getContainer().getMappedPort(SERVICE_BUS_PORT));
        }
    }
}
