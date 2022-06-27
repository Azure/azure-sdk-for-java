package com.azure.storage.datamover.filesystem;

import com.azure.storage.datamover.StorageResource;
import com.azure.storage.datamover.models.TransferMethod;

import java.nio.file.Path;
import java.util.Collections;
import java.util.Set;

class FileResource extends StorageResource {

    private final Path path;

    FileResource(Path path) {
        if (!path.toFile().isFile()) {
            throw new IllegalArgumentException("provided path isn't file");
        }
        this.path = path;
    }

    @Override
    protected Set<TransferMethod> getIncomingTransferMethods() {
        return Collections.singleton(TransferMethod.STREAMING);
    }

    @Override
    protected Set<TransferMethod> getOutgoingTransferMethods() {
        return Collections.singleton(TransferMethod.STREAMING);
    }
}
