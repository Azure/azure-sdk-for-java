package com.azure.storage.common.resource.filesystem;

import com.azure.storage.common.resource.StorageResource;
import com.azure.storage.common.resource.StorageResourceContainer;

import java.nio.file.Path;
import java.util.Collections;

public final class FileSystemResources {
    private FileSystemResources() {
    }

    public static StorageResource file(Path path) {
        return new FileResource(path, Collections.singletonList(path.toFile().getName()));
    }

    public static StorageResourceContainer directory(Path path) {
        return new DirectoryResourceContainer(path);
    }
}
