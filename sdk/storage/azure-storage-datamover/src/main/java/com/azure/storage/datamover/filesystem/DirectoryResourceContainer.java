package com.azure.storage.datamover.filesystem;

import com.azure.storage.datamover.StorageResource;
import com.azure.storage.datamover.StorageResourceContainer;
import com.azure.storage.datamover.models.TransferCapabilities;
import com.azure.storage.datamover.models.TransferCapabilitiesBuilder;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

class DirectoryResourceContainer extends StorageResourceContainer {
    private final Path path;

    DirectoryResourceContainer(Path path) {
        if (!path.toFile().isDirectory()) {
            throw new IllegalArgumentException("provided path isn't directory");
        }
        this.path = path;
    }

    @Override
    protected Iterable<StorageResource> listResources() {
        try {
            return Files.walk(path)
                .filter(Files::isRegularFile)
                .map(file -> {
                    Path relativePath = path.relativize(file);
                    String[] split = relativePath.toString().split("(\\\\)|(/)");
                    return new FileResource(file, Arrays.asList(split));
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
    protected StorageResource getStorageResource(List<String> path) {
        Path resourcePath = this.path;
        for (String subPath : path) {
            resourcePath = resourcePath.resolve(subPath);
        }
        return new FileResource(resourcePath, path);
    }

}
