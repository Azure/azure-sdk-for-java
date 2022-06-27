package com.azure.storage.datamover.models;

public class TransferCapabilities {
    private final boolean canStream;
    private final boolean canUseSasUri;

    TransferCapabilities(boolean canStream, boolean canUseSasUri) {
        this.canStream = canStream;
        this.canUseSasUri = canUseSasUri;
    }

    public boolean isCanStream() {
        return canStream;
    }

    public boolean isCanUseSasUri() {
        return canUseSasUri;
    }
}
