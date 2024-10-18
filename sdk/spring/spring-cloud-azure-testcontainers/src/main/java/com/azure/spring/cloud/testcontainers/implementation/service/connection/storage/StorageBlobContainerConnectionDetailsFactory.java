// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.testcontainers.implementation.service.connection.storage;

import com.azure.spring.cloud.autoconfigure.implementation.storage.blob.properties.AzureStorageBlobConnectionDetails;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionDetailsFactory;
import org.springframework.boot.testcontainers.service.connection.ContainerConnectionSource;
import org.testcontainers.containers.Container;

class StorageBlobContainerConnectionDetailsFactory
    extends ContainerConnectionDetailsFactory<Container<?>, AzureStorageBlobConnectionDetails> {

    private static final int STORAGE_BLOB_PORT = 10_000;

    StorageBlobContainerConnectionDetailsFactory() {
        super("azure-storage/azurite");
    }

    @Override
    protected AzureStorageBlobConnectionDetails getContainerConnectionDetails(ContainerConnectionSource<Container<?>> source) {
        return new StorageBlobContainerConnectionDetails(source);
    }

    /**
     * {@link AzureStorageBlobConnectionDetails} backed by a {@link ContainerConnectionSource}.
     */
    private static class StorageBlobContainerConnectionDetails extends ContainerConnectionDetails<Container<?>>
        implements AzureStorageBlobConnectionDetails {

        StorageBlobContainerConnectionDetails(ContainerConnectionSource<Container<?>> source) {
            super(source);
        }

        @Override
        public String getConnectionString() {
            return "DefaultEndpointsProtocol=http;AccountName=devstoreaccount1;AccountKey=Eby8vdM02xNOcqFlqUwJPLlmEtlCDXJ1OUzFT50uSRZ6IFsuFq2UVErCz4I6tq/K1SZFPTOtr/KBHBeksoGMGw==;BlobEndpoint=http://%s:%d/devstoreaccount1;"
                .formatted(getContainer().getHost(), getContainer().getMappedPort(STORAGE_BLOB_PORT));
        }
    }
}
