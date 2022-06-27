package com.azure.storage.datamover.filesystem;

import com.azure.storage.datamover.StorageResource;
import com.azure.storage.datamover.StorageResourceContainer;
import com.azure.storage.datamover.models.TransferMethod;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;
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
                .map(FileResource::new)
                .collect(Collectors.toList());
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    @Override
    protected Set<TransferMethod> getIncomingTransferMethods() {
        return Collections.singleton(TransferMethod.STREAMING);
    }
}
