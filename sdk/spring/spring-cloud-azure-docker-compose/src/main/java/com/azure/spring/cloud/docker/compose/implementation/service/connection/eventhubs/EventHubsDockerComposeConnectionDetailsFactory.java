// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.docker.compose.implementation.service.connection.eventhubs;

import com.azure.spring.cloud.autoconfigure.implementation.eventhubs.properties.AzureEventHubsConnectionDetails;
import org.springframework.boot.docker.compose.core.RunningService;
import org.springframework.boot.docker.compose.service.connection.DockerComposeConnectionDetailsFactory;
import org.springframework.boot.docker.compose.service.connection.DockerComposeConnectionSource;

class EventHubsDockerComposeConnectionDetailsFactory extends DockerComposeConnectionDetailsFactory<AzureEventHubsConnectionDetails> {

    private static final int EVENTHUBS_PORT = 5672;

    protected EventHubsDockerComposeConnectionDetailsFactory() {
        super("azure-messaging/eventhubs-emulator");
    }

    @Override
    protected AzureEventHubsConnectionDetails getDockerComposeConnectionDetails(DockerComposeConnectionSource source) {
        return new EventHubsContainerConnectionDetails(source.getRunningService());
    }

    /**
     * {@link AzureEventHubsConnectionDetails} backed by a {@code EventHubs}
     * {@link RunningService}.
     */
    private static class EventHubsContainerConnectionDetails extends DockerComposeConnectionDetails
        implements AzureEventHubsConnectionDetails {

        private final String host;

        private final int port;

        EventHubsContainerConnectionDetails(RunningService service) {
            super(service);
            this.host = service.host();
            this.port = service.ports().get(EVENTHUBS_PORT);
        }

        @Override
        public String getConnectionString() {
            return "Endpoint=sb://%s:%d;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=SAS_KEY_VALUE;UseDevelopmentEmulator=true;"
                .formatted(this.host, this.port);
        }
    }
}
