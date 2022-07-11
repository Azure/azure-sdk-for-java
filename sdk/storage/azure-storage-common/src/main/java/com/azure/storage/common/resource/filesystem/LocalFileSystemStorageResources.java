package com.azure.storage.common.resource.filesystem;

import com.azure.storage.common.resource.StorageResource;
import com.azure.storage.common.resource.StorageResourceContainer;

import java.nio.file.Path;
import java.util.Collections;

public final class LocalFileSystemStorageResources {
    private LocalFileSystemStorageResources() {
    }

    public static StorageResource file(Path path) {
        return new LocalFileStorageResource(path, Collections.singletonList(path.toFile().getName()));
    }

    public static StorageResourceContainer directory(Path path) {
        return new LocalDirectoryStorageResourceContainer(path);
    }
}
