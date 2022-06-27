package com.azure.storage.datamover;

import com.azure.storage.datamover.models.TransferMethod;

import java.util.Set;

public abstract class StorageResource {
    protected abstract Set<TransferMethod> getIncomingTransferMethods();
    protected abstract Set<TransferMethod> getOutgoingTransferMethods();
}
