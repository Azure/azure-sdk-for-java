package com.azure.mixedreality.remoterendering;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of the RemoteRenderingService supported by this client library.
 */
public enum RemoteRenderingServiceVersion implements ServiceVersion {
    V2021_01_01_Preview("2021-01-01-preview");

    private final String version;

    RemoteRenderingServiceVersion(String version) {
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
     * Gets the latest service version supported by this client library.
     *
     * @return the latest {@link com.azure.mixedreality.remoterendering.RemoteRenderingServiceVersion}
     */
    public static com.azure.mixedreality.remoterendering.RemoteRenderingServiceVersion getLatest() {
        return V2021_01_01_Preview;
    }
}
