package com.azure.messaging.signalr;

import com.azure.core.util.ServiceVersion;

public enum SignalRServiceVersion implements ServiceVersion {
    V1_0("1.0");

    private final String version;

    SignalRServiceVersion(String version) {
        this.version = version;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVersion() {
        return this.version;
    }

    /**
     * Gets the latest service version supported by this client library
     *
     * @return the latest {@link SignalRServiceVersion}
     */
    public static SignalRServiceVersion getLatest() {
        return V1_0;
    }
}
