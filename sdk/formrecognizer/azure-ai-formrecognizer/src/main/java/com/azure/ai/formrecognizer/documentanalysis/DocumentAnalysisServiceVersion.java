// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer.documentanalysis;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure Form Recognizer supported by this client library.
 */
public enum DocumentAnalysisServiceVersion implements ServiceVersion {

    /**
     * Service version {@code 2022-08-31"}.
     */
    V2022_08_31("2022-08-31"),

    /**
     * Service version {@code 2023-02-28-preview}.
     */
    V2023_02_28_preview("2023-02-28-preview");

    private final String version;

    DocumentAnalysisServiceVersion(String version) {
        this.version = version;
    }

    /** {@inheritDoc} */
    @Override
    public String getVersion() {
        return this.version;
    }

    /**
     * Gets the latest service version supported by this client library
     *
     * @return the latest {@link DocumentAnalysisServiceVersion}
     */
    public static DocumentAnalysisServiceVersion getLatest() {
        // Switch to the latest preview once recordings are updated.
        return V2023_02_28_preview;
    }
}
