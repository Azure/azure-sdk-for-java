// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.spring.cloud.context.core.api;

/**
 * Metadata defining an Azure resource.
 */
public class AzureResourceMetadata {

    private String region;
    private String resourceGroup;
    private boolean autoCreateResources;

    public String getRegion() {
        return region;
    }

    public void setRegion(String region) {
        this.region = region;
    }

    public String getResourceGroup() {
        return resourceGroup;
    }

    public void setResourceGroup(String resourceGroup) {
        this.resourceGroup = resourceGroup;
    }

    public boolean isAutoCreateResources() {
        return autoCreateResources;
    }

    public void setAutoCreateResources(boolean autoCreateResources) {
        this.autoCreateResources = autoCreateResources;
    }
}
