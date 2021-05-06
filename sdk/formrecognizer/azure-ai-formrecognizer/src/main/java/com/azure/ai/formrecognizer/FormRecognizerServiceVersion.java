// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.formrecognizer;

import com.azure.core.util.ServiceVersion;

/**
 * The versions of Azure Form Recognizer supported by this client library.
 */
public enum FormRecognizerServiceVersion implements ServiceVersion {
    V2_0("v2.0"),
    V2_1_PREVIEW_3("v2.1-preview.3");

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
     * @return the latest {@link FormRecognizerServiceVersion}
     */
    public static FormRecognizerServiceVersion getLatest() {
        return V2_1_PREVIEW_3;
    }

}
