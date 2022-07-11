// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.storage.common.resource.filesystem;

import com.azure.core.util.logging.ClientLogger;
import com.azure.storage.common.resource.StorageResource;
import com.azure.storage.common.resource.StorageResourceContainer;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

class LocalDirectoryStorageResourceContainer implements StorageResourceContainer {

    private static final ClientLogger LOGGER = new ClientLogger(LocalDirectoryStorageResourceContainer.class);

    private final Path path;

    LocalDirectoryStorageResourceContainer(Path path) {
        if (!path.toFile().isDirectory()) {
            throw LOGGER.logExceptionAsError(new IllegalArgumentException("provided path isn't directory"));
        }
        this.path = path;
    }

    @Override
    public Iterable<StorageResource> listResources() {
        try {
            return Files.walk(path)
                .filter(Files::isRegularFile)
                .map(file -> {
                    Path relativePath = path.relativize(file);
                    String[] split = relativePath.toString().split("(\\\\)|(/)");
                    return new LocalFileStorageResource(file, Arrays.asList(split));
                })
                .collect(Collectors.toList());
        } catch (IOException e) {
            throw LOGGER.logExceptionAsError(new UncheckedIOException(e));
        }
    }

    @Override
    public List<String> getPath() {
        return Collections.emptyList();
    }

    @Override
    public StorageResource getStorageResource(List<String> path) {
        Path resourcePath = this.path;
        for (String subPath : path) {
            resourcePath = resourcePath.resolve(subPath);
        }
        return new LocalFileStorageResource(resourcePath, path);
    }

    @Override
    public StorageResourceContainer getStorageResourceContainer(List<String> path) {
        Path resourcePath = this.path;
        for (String subPath : path) {
            resourcePath = resourcePath.resolve(subPath);
        }
        return new LocalDirectoryStorageResourceContainer(resourcePath);
    }

}
