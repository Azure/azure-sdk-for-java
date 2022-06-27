package com.azure.storage.datamover;

import com.azure.storage.datamover.models.TransferCapabilities;

public abstract class StorageResourceContainer {

    protected abstract Iterable<StorageResource> listResources();

    protected abstract TransferCapabilities getIncomingTransferCapabilities();
}
