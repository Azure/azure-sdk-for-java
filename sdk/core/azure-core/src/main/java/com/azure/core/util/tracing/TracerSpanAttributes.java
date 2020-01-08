// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.core.util.tracing;

/**
 * Class to hold the attributes for tracer spans.
 */
public class TracerSpanAttributes {
    private final String resourceProviderName;

    /**
     * Creates the tracer span attributes with the provided resourceProviderName.
     *
     * @param resourceProviderName the resource provider namespace mapped to Azure services.
     */
    public TracerSpanAttributes(final String resourceProviderName) {
        this.resourceProviderName = resourceProviderName;
    }

    /**
     * Returns the resource provider namespace of the Azure service.
     *
     * @return the resource provider namespace of the Azure service.
     */
    public String getResourceProviderName() {
        return resourceProviderName;
    }
}
