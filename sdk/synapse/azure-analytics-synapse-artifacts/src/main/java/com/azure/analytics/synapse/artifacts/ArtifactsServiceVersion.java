package com.azure.analytics.synapse.artifacts;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure Synapse Analytics Access Control supported by this client library.
 */
public enum ArtifactsServiceVersion implements ServiceVersion {
    V2019_06_01_preview("2019-06-01-preview");

    private final String version;

    ArtifactsServiceVersion(String version) {
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
     * @return the latest {@link ArtifactsServiceVersion}
     */
    public static ArtifactsServiceVersion getLatest() {
        return V2019_06_01_preview;
    }
}
