// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.docker.compose.implementation.service.connection.servicebus;

import com.azure.spring.cloud.autoconfigure.implementation.servicebus.properties.AzureServiceBusConnectionDetails;
import org.springframework.boot.docker.compose.core.RunningService;
import org.springframework.boot.docker.compose.service.connection.DockerComposeConnectionDetailsFactory;
import org.springframework.boot.docker.compose.service.connection.DockerComposeConnectionSource;

public class ServiceBusDockerComposeConnectionDetailsFactory
    extends DockerComposeConnectionDetailsFactory<AzureServiceBusConnectionDetails> {

    private static final int SERVICE_BUS_PORT = 5672;

    protected ServiceBusDockerComposeConnectionDetailsFactory() {
        super("azure-messaging/servicebus-emulator");
    }

    @Override
    protected AzureServiceBusConnectionDetails getDockerComposeConnectionDetails(DockerComposeConnectionSource source) {
        return new ServiceBusContainerConnectionDetails(source.getRunningService());
    }

    /**
     * {@link AzureServiceBusConnectionDetails} backed by a {@code Service Bus}
     * {@link RunningService}.
     */
    private static class ServiceBusContainerConnectionDetails extends DockerComposeConnectionDetails
        implements AzureServiceBusConnectionDetails {

        private final String host;

        private final int port;

        ServiceBusContainerConnectionDetails(RunningService service) {
            super(service);
            this.host = service.host();
            this.port = service.ports().get(SERVICE_BUS_PORT);
        }

        @Override
        public String getConnectionString() {
            return "Endpoint=sb://%s:%d;SharedAccessKeyName=RootManageSharedAccessKey;SharedAccessKey=SAS_KEY_VALUE;UseDevelopmentEmulator=true;"
                .formatted(this.host, this.port);
        }
    }
}
