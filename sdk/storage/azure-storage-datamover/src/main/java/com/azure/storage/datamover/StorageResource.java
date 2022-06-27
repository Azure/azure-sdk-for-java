package com.azure.storage.datamover;

import com.azure.storage.datamover.models.TransferCapabilities;

public abstract class StorageResource {
    protected abstract TransferCapabilities getIncomingTransferCapabilities();
    protected abstract TransferCapabilities getOutgoingTransferCapabilities();
}
