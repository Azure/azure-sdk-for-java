package com.azure.storage.common.resource.filesystem;

import com.azure.storage.common.resource.StorageResource;
import com.azure.storage.common.resource.StorageResourceContainer;
import com.azure.storage.common.resource.TransferCapabilities;
import com.azure.storage.common.resource.TransferCapabilitiesBuilder;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

class LocalDirectoryStorageResourceContainer extends StorageResourceContainer {
    private final Path path;

    LocalDirectoryStorageResourceContainer(Path path) {
        if (!path.toFile().isDirectory()) {
            throw new IllegalArgumentException("provided path isn't directory");
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
            throw new UncheckedIOException(e);
        }
    }

    @Override
    protected TransferCapabilities getIncomingTransferCapabilities() {
        return new TransferCapabilitiesBuilder()
            .canStream(true)
            .build();
    }

    @Override
    protected List<String> getPath() {
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

}
