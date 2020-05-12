package com.azure.analytics.synapse.development;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure Synapse Analytics Access Control supported by this client library.
 */
public enum DevelopmentServiceVersion implements ServiceVersion {
    V2019_06_01_preview("2019-06-01-preview");

    private final String version;

    DevelopmentServiceVersion(String version) {
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
     * @return the latest {@link DevelopmentServiceVersion}
     */
    public static DevelopmentServiceVersion getLatest() {
        return V2019_06_01_preview;
    }
}
