// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure Form Recognizer supported by this client library.
 */
public enum FormRecognizerServiceVersion implements ServiceVersion {
    V3_0_preview_1("v3.0-preview.1");

    private final String version;

    FormRecognizerServiceVersion(String version) {
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
     * @return the latest {@link com.azure.ai.formrecognizer.FormRecognizerServiceVersion}
     */
    public static com.azure.ai.formrecognizer.FormRecognizerServiceVersion getLatest() {
        return V3_0_preview_1;
    }

}
