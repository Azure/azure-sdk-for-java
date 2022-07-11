// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.resource.filesystem;

import com.azure.storage.common.resource.StorageResource;
import com.azure.storage.common.resource.StorageResourceContainer;

import java.nio.file.Path;
import java.util.Collections;

/**
 * A class with factories for local file system storage resources.
 */
public final class LocalFileSystemStorageResources {
    private LocalFileSystemStorageResources() {
    }

    /**
     * Creates {@link StorageResource} representing local file.
     * @param path The path of the file.
     * @return A {@link StorageResource} representing local file.
     */
    public static StorageResource file(Path path) {
        return new LocalFileStorageResource(path, Collections.singletonList(path.toFile().getName()));
    }

    /**
     * Creates {@link StorageResourceContainer} representing local directory.
     * @param path The path of the directory.
     * @return A {@link StorageResourceContainer} representing local directory.
     */
    public static StorageResourceContainer directory(Path path) {
        return new LocalDirectoryStorageResourceContainer(path);
    }
}
