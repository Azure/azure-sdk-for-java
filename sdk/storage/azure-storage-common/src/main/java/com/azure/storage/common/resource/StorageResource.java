package com.azure.storage.common.resource;

import java.io.InputStream;
import java.util.List;

public abstract class StorageResource {
    public abstract TransferCapabilities getIncomingTransferCapabilities();
    public abstract TransferCapabilities getOutgoingTransferCapabilities();

    public abstract InputStream openInputStream();
    public abstract long getLength();
    public abstract void consumeInputStream(InputStream inputStream, long length);
    public abstract String getSasUri();
    public abstract void consumeSasUri(String sasUri);
    public abstract List<String> getPath();
}
