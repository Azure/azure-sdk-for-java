// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.docker.compose.implementation.service.connection.cosmos;

import com.azure.cosmos.ConnectionMode;
import com.azure.spring.cloud.autoconfigure.implementation.cosmos.properties.AzureCosmosConnectionDetails;
import org.springframework.boot.docker.compose.core.RunningService;
import org.springframework.boot.docker.compose.service.connection.DockerComposeConnectionDetailsFactory;
import org.springframework.boot.docker.compose.service.connection.DockerComposeConnectionSource;

class CosmosDockerComposeConnectionDetailsFactory
    extends DockerComposeConnectionDetailsFactory<AzureCosmosConnectionDetails> {

    private static final int COSMOS_EMULATOR_PORT = 8081;
    private static final String COSMOS_EMULATOR_KEY =
        "C2y6yDjf5/R+ob0N8A7Cgv30VRDJIWEHLM+4QDU5DE2nQ9nDuVTqobD4b8mGGyPMbIZnqyMsEcaGQy67XIw/Jw==";
    private static final String COSMOS_DATABASE_NAME = "test-emulator";

    protected CosmosDockerComposeConnectionDetailsFactory() {
        super("mcr.microsoft.com/cosmosdb/linux/azure-cosmos-emulator", "azure-cosmos-emulator");
    }

    @Override
    protected AzureCosmosConnectionDetails getDockerComposeConnectionDetails(DockerComposeConnectionSource source) {
        return new CosmosContainerConnectionDetails(source.getRunningService());
    }

    private static class CosmosContainerConnectionDetails extends DockerComposeConnectionDetails
        implements AzureCosmosConnectionDetails {

        private final String endpoint;

        CosmosContainerConnectionDetails(RunningService service) {
            super(service);
            int mappedPort = service.ports().get(COSMOS_EMULATOR_PORT);
            this.endpoint = "https://%s:%d/".formatted(service.host(), mappedPort);
        }

        @Override
        public String getEndpoint() {
            return this.endpoint;
        }

        @Override
        public String getKey() {
            return COSMOS_EMULATOR_KEY;
        }

        @Override
        public String getDatabase() {
            return COSMOS_DATABASE_NAME;
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
