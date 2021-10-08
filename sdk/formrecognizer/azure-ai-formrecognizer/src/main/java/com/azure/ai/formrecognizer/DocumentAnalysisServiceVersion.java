// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure Form Recognizer supported by this client library.
 */
public enum DocumentAnalysisServiceVersion implements ServiceVersion {
    V2021_09_30_PREVIEW("2021-09-30-preview");

    private final String version;

    DocumentAnalysisServiceVersion(String version) {
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
     * @return the latest {@link DocumentAnalysisServiceVersion}
     */
    public static DocumentAnalysisServiceVersion getLatest() {
        return V2021_09_30_PREVIEW;
    }
}
