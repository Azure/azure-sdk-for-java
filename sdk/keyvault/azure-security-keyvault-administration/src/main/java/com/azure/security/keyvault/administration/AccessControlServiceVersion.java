package com.azure.security.keyvault.administration;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure Form Recognizer supported by this client library.
 */
public enum AccessControlServiceVersion implements ServiceVersion {
    V7_2_preview("7.2-preview");

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
        return V7_2_preview;
    }
}

