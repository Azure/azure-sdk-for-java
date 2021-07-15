package com.azure.spring.core;


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
