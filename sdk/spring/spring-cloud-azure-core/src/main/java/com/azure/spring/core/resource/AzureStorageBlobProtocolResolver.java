// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.resource;

import com.azure.storage.blob.BlobServiceClient;
import com.azure.storage.blob.models.BlobContainerItem;
import com.azure.storage.blob.models.BlobContainerListDetails;
import com.azure.storage.blob.models.BlobListDetails;
import com.azure.storage.blob.models.ListBlobContainersOptions;
import com.azure.storage.blob.models.ListBlobsOptions;
import java.util.stream.Stream;
import org.springframework.core.io.ProtocolResolver;
import org.springframework.core.io.Resource;

/**
 * A {@link ProtocolResolver} implementation for the {@code azure-blob://} protocol.
 */
public class AzureStorageBlobProtocolResolver extends AbstractAzureStorageProtocolResolver {

    private final BlobServiceClient blobServiceClient;

    private final static BlobListDetails RETRIEVE_NOTHING_DETAILS = new BlobListDetails();
    private final static BlobContainerListDetails RETRIEVE_NOTHING_CONTAINER_DETAILS = new BlobContainerListDetails();

    static {
        RETRIEVE_NOTHING_DETAILS.setRetrieveCopy(false);
        RETRIEVE_NOTHING_DETAILS.setRetrieveDeletedBlobs(false);
        RETRIEVE_NOTHING_DETAILS.setRetrieveDeletedBlobsWithVersions(false);
        RETRIEVE_NOTHING_DETAILS.setRetrieveImmutabilityPolicy(false);
        RETRIEVE_NOTHING_DETAILS.setRetrieveMetadata(false);
        RETRIEVE_NOTHING_DETAILS.setRetrieveLegalHold(false);
        RETRIEVE_NOTHING_DETAILS.setRetrieveSnapshots(false);
        RETRIEVE_NOTHING_DETAILS.setRetrieveTags(false);
        RETRIEVE_NOTHING_DETAILS.setRetrieveUncommittedBlobs(false);
        RETRIEVE_NOTHING_CONTAINER_DETAILS.setRetrieveMetadata(false);
        RETRIEVE_NOTHING_CONTAINER_DETAILS.setRetrieveDeleted(false);
    }

    public AzureStorageBlobProtocolResolver(BlobServiceClient blobServiceClient) {
        this.blobServiceClient = blobServiceClient;
    }

    @Override
    protected StorageType getStorageType() {
        return StorageType.BLOB;
    }

    @Override
    protected Stream<StorageContainerItem> listStorageContainers(String containerPrefix) {

        ListBlobContainersOptions options = new ListBlobContainersOptions();
        options.setPrefix(containerPrefix);
        options.setDetails(RETRIEVE_NOTHING_CONTAINER_DETAILS);
        return blobServiceClient.listBlobContainers(options, null)
                                .stream()
                                .map(BlobContainerItem::getName)
                                .map(StorageContainerItem::new);
    }

    @Override
    protected StorageContainerClient getStorageContainerClient(String name) {
        return new StorageBlobContainerClient(name);
    }

    private class StorageBlobContainerClient implements StorageContainerClient {

        private final String name;

        public StorageBlobContainerClient(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Stream<StorageItem> listItems(String itemPrefix) {
            ListBlobsOptions options = new ListBlobsOptions();
            options.setPrefix(itemPrefix);
            options.setDetails(RETRIEVE_NOTHING_DETAILS);
            return blobServiceClient.getBlobContainerClient(name)
                                    .listBlobs(options, null)
                                    .stream()
                                    .map(blob -> new StorageItem(name, blob.getName(), getStorageType()));
        }
    }

    @Override
    protected Resource getStorageResource(String location, Boolean autoCreate) {
        return new StorageBlobResource(blobServiceClient, location, autoCreate);
    }
}
