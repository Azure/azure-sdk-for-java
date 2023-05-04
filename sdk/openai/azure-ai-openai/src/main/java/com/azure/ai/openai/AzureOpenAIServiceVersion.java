// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.openai;

import com.azure.core.util.ServiceVersion;

/** Service version of OpenAIClient. */
public enum AzureOpenAIServiceVersion implements ServiceVersion {
    /** Enum value 2022-12-01. */
    V2022_12_01("2022-12-01"),

    /** Enum value 2023-03-15-preview. */
    V2023_03_15_PREVIEW("2023-03-15-preview");

    private final String version;

    AzureOpenAIServiceVersion(String version) {
        this.version = version;
    }

    /** {@inheritDoc} */
    @Override
    public String getVersion() {
        return this.version;
    }

    /**
     * Gets the latest service version supported by this client library.
     *
     * @return The latest {@link AzureOpenAIServiceVersion}.
     */
    public static AzureOpenAIServiceVersion getLatest() {
        return V2023_03_15_PREVIEW;
    }
}
