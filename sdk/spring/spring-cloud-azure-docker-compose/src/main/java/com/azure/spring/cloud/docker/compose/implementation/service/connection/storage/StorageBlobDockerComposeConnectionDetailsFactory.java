// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.docker.compose.implementation.service.connection.storage;

import com.azure.spring.cloud.autoconfigure.implementation.storage.blob.properties.AzureStorageBlobConnectionDetails;
import org.springframework.boot.docker.compose.core.RunningService;
import org.springframework.boot.docker.compose.service.connection.DockerComposeConnectionDetailsFactory;
import org.springframework.boot.docker.compose.service.connection.DockerComposeConnectionSource;

class StorageBlobDockerComposeConnectionDetailsFactory
    extends DockerComposeConnectionDetailsFactory<AzureStorageBlobConnectionDetails> {

    private static final int STORAGE_BLOB_PORT = 10_000;

    protected StorageBlobDockerComposeConnectionDetailsFactory() {
        super("azure-storage/azurite");
    }

    @Override
    protected AzureStorageBlobConnectionDetails getDockerComposeConnectionDetails(DockerComposeConnectionSource source) {
        return new StorageBlobContainerConnectionDetails(source.getRunningService());
    }

    /**
     * {@link AzureStorageBlobConnectionDetails} backed by a {@code Storage Blob}
     * {@link RunningService}.
     */
    private static class StorageBlobContainerConnectionDetails extends DockerComposeConnectionDetails
        implements AzureStorageBlobConnectionDetails {

        private final String host;

        private final int port;

        StorageBlobContainerConnectionDetails(RunningService service) {
            super(service);
            this.host = service.host();
            this.port = service.ports().get(STORAGE_BLOB_PORT);
        }

        @Override
        public String getConnectionString() {
            return "DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;BlobEndpoint=http://%s:%d/devstoreaccount1;"
                .formatted(this.host, this.port);
        }
    }
}
