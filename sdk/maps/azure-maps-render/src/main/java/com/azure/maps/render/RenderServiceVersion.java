package com.azure.maps.render;

import com.azure.core.util.ServiceVersion;

/**
 * Contains the versions of the Render Service available for the clients.
 */
public enum RenderServiceVersion implements ServiceVersion{
    /**
    * Service version {@code 2.1}.
    */
    V2_1("2.1");

    private final String version;

    /**
     * Creates a new {@link RenderServiceVersion} with a version string.
     *
     * @param version
     */
    RenderServiceVersion(String version) {
        this.version = version;
    }

    /**
     * Gets the latest service version supported by this client library
     *
     * @return the latest {@link RenderServiceVersion}
     */
    public static RenderServiceVersion getLatest() {
        return V2_1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getVersion() {
        return this.version;
    }  
}