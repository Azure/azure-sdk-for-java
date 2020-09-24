// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.trafficmanager.models;

/** Target Azure resource types supported for an Azure endpoint in a traffic manager profile. */
public class TargetAzureResourceType {
    /** Static value Microsoft.Network/publicIPAddresses for TargetAzureResourceType. */
    public static final TargetAzureResourceType PUBLICIP =
        new TargetAzureResourceType("Microsoft.Network", "publicIPAddresses");

    /** Static value Microsoft.Web/sites for TargetAzureResourceType. */
    public static final TargetAzureResourceType WEBAPP = new TargetAzureResourceType("Microsoft.Web", "sites");

    /** Static value Microsoft.ClassicCompute/domainNames for TargetAzureResourceType. */
    public static final TargetAzureResourceType CLOUDSERVICE =
        new TargetAzureResourceType("Microsoft.ClassicCompute", "domainNames");

    private final String value;

    /**
     * Creates TargetAzureResourceType.
     *
     * @param resourceProviderName the resource provider name
     * @param resourceType the resource type
     */
    public TargetAzureResourceType(String resourceProviderName, String resourceType) {
        this.value = resourceProviderName + "/" + resourceType;
    }

    @Override
    public String toString() {
        return this.value;
    }

    @Override
    public int hashCode() {
        return this.value.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        String value = this.toString();
        if (!(obj instanceof TargetAzureResourceType)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        TargetAzureResourceType rhs = (TargetAzureResourceType) obj;
        return value.equals(rhs.value);
    }
}
