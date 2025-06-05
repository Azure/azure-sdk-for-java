// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.communication.callautomation.models;

import com.azure.core.annotation.Fluent;

/**
 * AI options for the call.
 */
@Fluent
public final class CallIntelligenceOptions {

    /**
     * The endpoint URL of the Azure Cognitive Services resource attached
     */
    private String cognitiveServicesEndpoint;

    /**
     * Creates an instance of {@link CallIntelligenceOptions}.
     */
    public CallIntelligenceOptions() {
    }

    /**
     * Get the cognitiveServicesEndpoint property: The endpoint URL of the Azure Cognitive Services resource
     * attached.
     *
     * @return the cognitiveServicesEndpoint value.
     */
    public String getCognitiveServicesEndpoint() {
        return this.cognitiveServicesEndpoint;
    }

    /**
     * Set the cognitiveServicesEndpoint property: The endpoint URL of the Azure Cognitive Services resource
     * attached.
     *
     * @param cognitiveServicesEndpoint the cognitiveServicesEndpoint value to set.
     * @return the CallIntelligenceOptions object itself.
     */
    public CallIntelligenceOptions setCognitiveServicesEndpoint(String cognitiveServicesEndpoint) {
        this.cognitiveServicesEndpoint = cognitiveServicesEndpoint;
        return this;
    }
}
