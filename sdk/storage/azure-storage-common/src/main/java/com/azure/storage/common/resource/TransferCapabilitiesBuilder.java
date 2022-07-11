package com.azure.storage.common.resource;

public class TransferCapabilitiesBuilder {
    private boolean canStream;
    private boolean canUseSasUri;

    public TransferCapabilitiesBuilder canStream(boolean canStream) {
        this.canStream = canStream;
        return this;
    }

    public TransferCapabilitiesBuilder canUseSasUri(boolean canUseSasUri) {
        this.canUseSasUri = canUseSasUri;
        return this;
    }

    public TransferCapabilities build() {
        return new TransferCapabilities(canStream, canUseSasUri);
    }
}
