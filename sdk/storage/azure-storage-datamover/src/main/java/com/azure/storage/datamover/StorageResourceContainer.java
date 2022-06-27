package com.azure.storage.datamover;

import com.azure.storage.datamover.models.TransferMethod;

import java.util.Set;

public abstract class StorageResourceContainer {

    protected abstract Iterable<StorageResource> listResources();

    protected abstract Set<TransferMethod> getIncomingTransferMethods();
}
