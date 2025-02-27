// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.testcontainers.implementation.service.connection.eventhubs;

import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties.AzureEventHubsConnectionDetails;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionDetailsFactory;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionSource;
import org.testcontainers.azure.EventHubsEmulatorContainer;

class EventHubsContainerConnectionDetailsFactory
    extends ContainerConnectionDetailsFactory<EventHubsEmulatorContainer, AzureEventHubsConnectionDetails> {

    @Override
    protected AzureEventHubsConnectionDetails getContainerConnectionDetails(ContainerConnectionSource<EventHubsEmulatorContainer> source) {
        return new EventHubsContainerConnectionDetails(source);
    }

    /**
     * {@link AzureEventHubsConnectionDetails} backed by a {@link ContainerConnectionSource}.
     */
    private static class EventHubsContainerConnectionDetails extends ContainerConnectionDetails<EventHubsEmulatorContainer>
        implements AzureEventHubsConnectionDetails {

        EventHubsContainerConnectionDetails(ContainerConnectionSource<EventHubsEmulatorContainer> source) {
            super(source);
        }

        @Override
        public String getConnectionString() {
            return getContainer().getConnectionString();
        }
    }
}
