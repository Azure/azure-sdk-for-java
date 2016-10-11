/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.trafficmanager.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.trafficmanager.AzureEndpoint;
import com.microsoft.azure.management.trafficmanager.TargetAzureResourceType;

/**
 * Implementation for {@link AzureEndpoint}
 */
class AzureEndpointImpl extends EndpointImpl
 implements AzureEndpoint {
    AzureEndpointImpl(String name,
                 ProfileImpl parent,
                 EndpointInner inner,
                 EndpointsInner client) {
        super(name, parent, inner, client);
    }

    @Override
    public String targetAzureResourceId() {
        return inner().targetResourceId();
    }

    @Override
    public TargetAzureResourceType targetResourceType() {
        return new TargetAzureResourceType(ResourceUtils.resourceProviderFromResourceId(targetAzureResourceId()),
                ResourceUtils.resourceTypeFromResourceId(targetAzureResourceId()));
    }
}
