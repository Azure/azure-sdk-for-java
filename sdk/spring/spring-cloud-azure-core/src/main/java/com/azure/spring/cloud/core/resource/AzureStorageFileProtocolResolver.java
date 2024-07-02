// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.core.resource;

import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareServiceClient;
import com.azure.storage.file.share.models.ListSharesOptions;
import com.azure.storage.file.share.models.ShareItem;
import org.springframework.core.io.ProtocolResolver;
import org.springframework.core.io.Resource;

import java.util.stream.Stream;

/**
 * A {@link ProtocolResolver} implementation for the {@code azure-file://} protocol.
 */
public final class AzureStorageFileProtocolResolver extends AbstractAzureStorageProtocolResolver {

    /**
     * Creates an instance of {@link AzureStorageFileProtocolResolver}.
     */
    public AzureStorageFileProtocolResolver() {
    }

    private ShareServiceClient shareServiceClient;

    /**
     * The storageType of current protocolResolver.
     *
     * @return StorageType.FILE;
     */
    @Override
    protected StorageType getStorageType() {
        return StorageType.FILE;
    }

    @Override
    protected Stream<StorageContainerItem> listStorageContainers(String containerPrefix) {
        ListSharesOptions options = new ListSharesOptions();

        options.setPrefix(containerPrefix);
        options.setIncludeDeleted(false);
        options.setIncludeMetadata(false);
        options.setIncludeSnapshots(false);
        return getShareServiceClient().listShares(options, null, null)
                                 .stream()
                                 .map(ShareItem::getName)
                                 .map(StorageContainerItem::new);
    }

    @Override
    protected StorageContainerClient getStorageContainerClient(String name) {
        return new StorageFileContainerClient(name);
    }

    private class StorageFileContainerClient implements StorageContainerClient {

        private final String name;

        StorageFileContainerClient(String name) {
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public Stream<StorageItem> listItems(String itemPrefix) {
            ShareClient shareClient = getShareServiceClient().getShareClient(name);
            if (shareClient.exists()) {
                return shareClient.getRootDirectoryClient().listFilesAndDirectories(itemPrefix, null, null, null)
                                  .stream()
                                  .filter(file -> !file.isDirectory())
                                  .map(file -> new StorageItem(name, file.getName(), getStorageType()));
            } else {
                return Stream.empty();
            }
        }
    }

    @Override
    protected Resource getStorageResource(String location, Boolean autoCreate) {
        return new StorageFileResource(getShareServiceClient(), location, autoCreate);
    }

    private ShareServiceClient getShareServiceClient() {
        if (shareServiceClient == null) {
            shareServiceClient = beanFactory.getBean(ShareServiceClient.class);
        }
        return shareServiceClient;
    }
}
