// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.cognitiveservices.translator.authentication;

import com.azure.core.credential.AzureKeyCredential;

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
