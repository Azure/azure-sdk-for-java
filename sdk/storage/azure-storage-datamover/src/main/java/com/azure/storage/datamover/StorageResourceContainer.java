package com.azure.storage.datamover;

import com.azure.storage.datamover.models.TransferCapabilities;

import java.util.List;

public abstract class StorageResourceContainer {

    protected abstract Iterable<StorageResource> listResources();

    protected abstract TransferCapabilities getIncomingTransferCapabilities();

    protected abstract List<String> getPath();

    protected abstract StorageResource getStorageResource(List<String> path);
}
