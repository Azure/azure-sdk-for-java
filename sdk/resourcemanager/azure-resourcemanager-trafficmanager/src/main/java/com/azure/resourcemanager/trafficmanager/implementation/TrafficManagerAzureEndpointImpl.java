/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.resourcemanager.trafficmanager.implementation;

import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.trafficmanager.fluent.EndpointsClient;
import com.azure.resourcemanager.trafficmanager.fluent.inner.EndpointInner;
import com.azure.resourcemanager.trafficmanager.models.TrafficManagerAzureEndpoint;
import com.azure.resourcemanager.trafficmanager.models.TargetAzureResourceType;

/**
 * Implementation for {@link TrafficManagerAzureEndpoint}.
 */
class TrafficManagerAzureEndpointImpl extends TrafficManagerEndpointImpl
 implements TrafficManagerAzureEndpoint {
    TrafficManagerAzureEndpointImpl(String name,
                                    TrafficManagerProfileImpl parent,
                                    EndpointInner inner,
                                    EndpointsClient client) {
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
