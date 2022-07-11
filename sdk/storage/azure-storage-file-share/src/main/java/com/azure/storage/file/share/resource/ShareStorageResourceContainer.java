package com.azure.storage.file.share.resource;

import com.azure.storage.common.resource.StorageResource;
import com.azure.storage.common.resource.StorageResourceContainer;
import com.azure.storage.file.share.ShareClient;
import com.azure.storage.file.share.ShareDirectoryClient;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.Stream;

class ShareStorageResourceContainer implements StorageResourceContainer {

    private final ShareClient shareClient;

    ShareStorageResourceContainer(ShareClient shareClient) {
        this.shareClient = Objects.requireNonNull(shareClient);
    }

    @Override
    public Iterable<StorageResource> listResources() {
        return listResources(shareClient.getRootDirectoryClient(), shareClient.getRootDirectoryClient())
            .collect(Collectors.toList());
    }

    private Stream<StorageResource> listResources(ShareDirectoryClient directoryClient, ShareDirectoryClient rootDir) {
        return directoryClient
            .listFilesAndDirectories()
            .stream()
            .flatMap(item -> {
                if (item.isDirectory()) {
                    return listResources(directoryClient.getSubdirectoryClient(item.getName()), rootDir);
                } else {
                    return Stream.of(new ShareFileStorageResource(directoryClient.getFileClient(item.getName()), rootDir));
                }
            });
    }

    @Override
    public List<String> getPath() {
        return Collections.emptyList();
    }

    @Override
    public StorageResource getStorageResource(List<String> path) {
        return new ShareFileStorageResource(
            shareClient.getFileClient(String.join("/", path)), shareClient.getRootDirectoryClient());
    }

    @Override
    public StorageResourceContainer getStorageResourceContainer(List<String> path) {
        return new ShareDirectoryStorageResourceContainer(
            shareClient.getRootDirectoryClient()
                .getSubdirectoryClient(String.join("/", path)));
    }
}
