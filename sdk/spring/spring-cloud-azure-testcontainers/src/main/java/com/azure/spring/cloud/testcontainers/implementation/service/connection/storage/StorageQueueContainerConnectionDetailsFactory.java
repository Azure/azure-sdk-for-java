// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.testcontainers.implementation.service.connection.storage;

import com.azure.spring.cloud.autoconfigure.implementation.storage.blob.properties.AzureStorageBlobConnectionDetails;
import com.azure.spring.cloud.autoconfigure.implementation.storage.queue.properties.AzureStorageQueueConnectionDetails;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionDetailsFactory;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionSource;
import org.testcontainers.containers.Container;

class StorageQueueContainerConnectionDetailsFactory
    extends ContainerConnectionDetailsFactory<Container<?>, AzureStorageQueueConnectionDetails> {

    private static final int STORAGE_QUEUE_PORT = 10001;

    StorageQueueContainerConnectionDetailsFactory() {
        super("azure-storage/azurite");
    }

    @Override
    protected AzureStorageQueueConnectionDetails getContainerConnectionDetails(ContainerConnectionSource<Container<?>> source) {
        return new StorageQueueContainerConnectionDetails(source);
    }

    /**
     * {@link AzureStorageBlobConnectionDetails} backed by a {@link ContainerConnectionSource}.
     */
    private static class StorageQueueContainerConnectionDetails extends ContainerConnectionDetails<Container<?>>
        implements AzureStorageQueueConnectionDetails {

        StorageQueueContainerConnectionDetails(ContainerConnectionSource<Container<?>> source) {
            super(source);
        }

        @Override
        public String getConnectionString() {
            return "DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;QueueEndpoint=http://%s:%d/devstoreaccount1;"
                .formatted(getContainer().getHost(), getContainer().getMappedPort(STORAGE_QUEUE_PORT));
        }

        @Override
        public String getEndpoint() {
            return "http://%s:%d".formatted(getContainer().getHost(), getContainer().getMappedPort(STORAGE_QUEUE_PORT));
        }
    }
}
