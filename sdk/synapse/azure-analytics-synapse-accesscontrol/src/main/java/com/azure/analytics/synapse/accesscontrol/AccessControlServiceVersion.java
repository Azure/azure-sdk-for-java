package com.azure.analytics.synapse.accesscontrol;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure Synapse Analytics Access Control supported by this client library.
 */
public enum AccessControlServiceVersion implements ServiceVersion {
    V2020_02_01_preview("2020-02-01-preview");

    private final String version;

    AccessControlServiceVersion(String version) {
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
     * @return the latest {@link AccessControlServiceVersion}
     */
    public static AccessControlServiceVersion getLatest() {
        return V2020_02_01_preview;
    }

}
