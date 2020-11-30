// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.trafficmanager.implementation;

import com.azure.core.management.Region;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.trafficmanager.fluent.EndpointsClient;
import com.azure.resourcemanager.trafficmanager.fluent.models.EndpointInner;
import com.azure.resourcemanager.trafficmanager.models.TrafficManagerNestedProfileEndpoint;

/** Implementation for {@link TrafficManagerNestedProfileEndpoint}. */
class TrafficManagerNestedProfileEndpointImpl extends TrafficManagerEndpointImpl
    implements TrafficManagerNestedProfileEndpoint {
    TrafficManagerNestedProfileEndpointImpl(
        String name, TrafficManagerProfileImpl parent, EndpointInner inner, EndpointsClient client) {
        super(name, parent, inner, client);
    }

    @Override
    public String nestedProfileId() {
        return innerModel().targetResourceId();
    }

    @Override
    public long minimumChildEndpointCount() {
        return ResourceManagerUtils.toPrimitiveLong(innerModel().minChildEndpoints());
    }

    @Override
    public Region sourceTrafficLocation() {
        return Region.fromName((innerModel().endpointLocation()));
    }
}
