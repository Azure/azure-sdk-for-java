package com.azure.storage.datamover;

import com.azure.storage.datamover.models.TransferCapabilities;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.List;

public abstract class StorageResource {
    protected abstract TransferCapabilities getIncomingTransferCapabilities();
    protected abstract TransferCapabilities getOutgoingTransferCapabilities();

    protected abstract InputStream openInputStream();
    protected abstract long getLength();
    protected abstract void consumeInputStream(InputStream inputStream, long length);
    protected abstract List<String> getPath();
}
