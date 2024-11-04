// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.testcontainers.implementation.service.connection.cosmos;

import com.azure.cosmos.ConnectionMode;
import com.azure.spring.cloud.autoconfigure.implementation.cosmos.properties.AzureCosmosConnectionDetails;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionDetailsFactory;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionSource;
import org.testcontainers.containers.CosmosDBEmulatorContainer;

class CosmosContainerConnectionDetailsFactory
    extends ContainerConnectionDetailsFactory<CosmosDBEmulatorContainer, AzureCosmosConnectionDetails> {

    @Override
    protected AzureCosmosConnectionDetails getContainerConnectionDetails(ContainerConnectionSource<CosmosDBEmulatorContainer> source) {
        return new CosmosContainerConnectionDetails(source);
    }

    /**
     * {@link AzureCosmosConnectionDetails} backed by a {@link ContainerConnectionSource}.
     */
    private static class CosmosContainerConnectionDetails extends ContainerConnectionDetails<CosmosDBEmulatorContainer>
        implements AzureCosmosConnectionDetails {

        CosmosContainerConnectionDetails(ContainerConnectionSource<CosmosDBEmulatorContainer> source) {
            super(source);
        }

        @Override
        public String getEndpoint() {
            return getContainer().getEmulatorEndpoint();
        }

        @Override
        public String getKey() {
            return getContainer().getEmulatorKey();
        }

        @Override
        public String getDatabase() {
            return "test-emulator";
        }

        @Override
        public Boolean getEndpointDiscoveryEnabled() {
            return false;
        }

        @Override
        public ConnectionMode getConnectionMode() {
            return ConnectionMode.GATEWAY;
        }
    }
}
