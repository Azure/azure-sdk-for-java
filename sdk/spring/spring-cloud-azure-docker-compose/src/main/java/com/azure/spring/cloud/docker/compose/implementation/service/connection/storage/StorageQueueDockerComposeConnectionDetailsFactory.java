// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.docker.compose.implementation.service.connection.storage;

import com.azure.spring.cloud.autoconfigure.implementation.storage.blob.properties.AzureStorageBlobConnectionDetails;
import com.azure.spring.cloud.autoconfigure.implementation.storage.queue.properties.AzureStorageQueueConnectionDetails;
import org.springframework.boot.docker.compose.core.RunningService;
import org.springframework.boot.docker.compose.service.connection.DockerComposeConnectionDetailsFactory;
import org.springframework.boot.docker.compose.service.connection.DockerComposeConnectionSource;

class StorageQueueDockerComposeConnectionDetailsFactory
    extends DockerComposeConnectionDetailsFactory<AzureStorageQueueConnectionDetails> {

    private static final int STORAGE_QUEUE_PORT = 10_001;

    protected StorageQueueDockerComposeConnectionDetailsFactory() {
        super("azure-storage/azurite");
    }

    @Override
    protected AzureStorageQueueConnectionDetails getDockerComposeConnectionDetails(DockerComposeConnectionSource source) {
        return new StorageQueueContainerConnectionDetails(source.getRunningService());
    }

    /**
     * {@link AzureStorageBlobConnectionDetails} backed by a {@code Storage Queue}
     * {@link RunningService}.
     */
    private static class StorageQueueContainerConnectionDetails extends DockerComposeConnectionDetails
        implements AzureStorageQueueConnectionDetails {

        private final String host;

        private final int port;

        StorageQueueContainerConnectionDetails(RunningService service) {
            super(service);
            this.host = service.host();
            this.port = service.ports().get(STORAGE_QUEUE_PORT);
        }

        @Override
        public String getConnectionString() {
            return "DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;QueueEndpoint=http://%s:%d/devstoreaccount1;"
                .formatted(this.host, this.port);
        }

        @Override
        public String getEndpoint() {
            return "http://%s:%d".formatted(this.host, this.port);
        }
    }
}
