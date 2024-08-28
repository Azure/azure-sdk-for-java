// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.testcontainers.implementation.service.connection.eventhubs;

import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties.AzureEventHubsConnectionDetails;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionDetailsFactory;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionSource;
import org.testcontainers.containers.Container;

class EventHubsContainerConnectionDetailsFactory
    extends ContainerConnectionDetailsFactory<Container<?>, AzureEventHubsConnectionDetails> {

    private static final int EVENT_HUBS_PORT = 5672;

    EventHubsContainerConnectionDetailsFactory() {
        super("azure-messaging/eventhubs-emulator");
    }

    @Override
    protected AzureEventHubsConnectionDetails getContainerConnectionDetails(ContainerConnectionSource<Container<?>> source) {
        return new EventHubsContainerConnectionDetails(source);
    }

    /**
     * {@link AzureEventHubsConnectionDetails} backed by a {@link ContainerConnectionSource}.
     */
    private static class EventHubsContainerConnectionDetails extends ContainerConnectionDetails<Container<?>>
        implements AzureEventHubsConnectionDetails {

        EventHubsContainerConnectionDetails(ContainerConnectionSource<Container<?>> source) {
            super(source);
        }

        @Override
        public String getConnectionString() {
            return "Endpoint=sb://%s:%d;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=SAS_KEY_VALUE;UseDevelopmentEmulator=true;".formatted(getContainer().getHost(), getContainer().getMappedPort(EVENT_HUBS_PORT));
        }
    }
}
