package com.azure.storage.datamover.filesystem;

import com.azure.storage.datamover.StorageResource;
import com.azure.storage.datamover.StorageResourceContainer;

import java.nio.file.Path;

public final class FileSystemResources {
    private FileSystemResources() {
    }

    public static StorageResource file(Path path) {
        return new FileResource(path);
    }

    public static StorageResourceContainer directory(Path path) {
        return new DirectoryResourceContainer(path);
    }
}
