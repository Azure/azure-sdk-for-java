// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.core.resource;

import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareServiceClient;
import com.azure.storage.file.share.models.ListSharesOptions;
import com.azure.storage.file.share.models.ShareItem;
import java.util.stream.Stream;
import org.springframework.core.io.ProtocolResolver;
import org.springframework.core.io.Resource;

/**
 * A {@link ProtocolResolver} implementation for the {@code azure-file://} protocol.
 */
public class AzureStorageFileProtocolResolver extends AbstractAzureStorageProtocolResolver {

    private ShareServiceClient shareServiceClient;

    /**
     * The default constructor of AzureStorageFileProtocolResolver
     */
    public AzureStorageFileProtocolResolver() {
    }

    /**
     * The storageType of current protocolResolver
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
            return shareClient.getRootDirectoryClient().listFilesAndDirectories(itemPrefix, null, null, null)
                              .stream()
                              .filter(file -> !file.isDirectory())
                              .map(file -> new StorageItem(name, file.getName(), getStorageType()));
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
