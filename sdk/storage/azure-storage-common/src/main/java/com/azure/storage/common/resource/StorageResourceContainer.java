package com.azure.storage.common.resource;

import java.util.List;

public interface StorageResourceContainer {

    Iterable<StorageResource> listResources();

    List<String> getPath();

    StorageResource getStorageResource(List<String> path);

    StorageResourceContainer getStorageResourceContainer(List<String> path);
}
