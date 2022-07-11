package com.azure.storage.common.resource;

import java.util.List;

public abstract class StorageResourceContainer {

    public abstract Iterable<StorageResource> listResources();

    protected abstract TransferCapabilities getIncomingTransferCapabilities();

    protected abstract List<String> getPath();

    public abstract StorageResource getStorageResource(List<String> path);
}
