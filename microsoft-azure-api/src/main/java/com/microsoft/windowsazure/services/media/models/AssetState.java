package com.microsoft.windowsazure.services.media.models;

/**
 * Specifies the states of the asset.
 */
public enum AssetState {
    Initialized(0), Published(1), Deleted(2);

    private int assetStateCode;

    private AssetState(int assetStateCode) {
        this.assetStateCode = assetStateCode;
    }

    public int getCode() {
        return assetStateCode;
    }
}
