package com.azure.storage.datamover.filesystem;

import com.azure.storage.datamover.StorageResource;
import com.azure.storage.datamover.models.TransferCapabilities;
import com.azure.storage.datamover.models.TransferCapabilitiesBuilder;

import java.nio.file.Path;

class FileResource extends StorageResource {

    private final Path path;

    FileResource(Path path) {
        if (!path.toFile().isFile()) {
            throw new IllegalArgumentException("provided path isn't file");
        }
        this.path = path;
    }

    @Override
    protected TransferCapabilities getIncomingTransferCapabilities() {
        return new TransferCapabilitiesBuilder()
            .canStream(true)
            .build();
    }

    @Override
    protected TransferCapabilities getOutgoingTransferCapabilities() {
        return new TransferCapabilitiesBuilder()
            .canStream(true)
            .build();
    }
}
