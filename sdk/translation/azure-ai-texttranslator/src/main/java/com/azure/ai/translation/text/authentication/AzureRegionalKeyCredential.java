// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.ai.translation.text.authentication;

import com.azure.core.credential.AzureKeyCredential;
import java.util.Objects;

/**
 * Regional Cognitive Services Key Credentials.
 */
public class AzureRegionalKeyCredential {
    private final AzureKeyCredential key;
    private final String region;

    /**
     * Creates an instance of AzureRegionalKeyCredential class.
     *
     * @param key Azure key credentials.
     * @param region Azure region.
     */
    public AzureRegionalKeyCredential(AzureKeyCredential key, String region) {
        Objects.requireNonNull(key, "'key' cannot be null.");
        Objects.requireNonNull(region, "'region' cannot be null.");

        this.key = key;
        this.region = region;
    }

    /**
     * Get the region.
     *
     * @return the region value.
     */
    public String getRegion() {
        return this.region;
    }

    /**
     * Get the key.
     *
     * @return the key value;
     */
    public AzureKeyCredential getKey() {
        return this.key;
    }
}
