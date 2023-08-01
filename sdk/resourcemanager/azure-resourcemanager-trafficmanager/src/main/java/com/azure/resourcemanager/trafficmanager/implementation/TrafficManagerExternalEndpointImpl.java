// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.trafficmanager.implementation;

import com.azure.core.management.Region;
import com.azure.resourcemanager.trafficmanager.fluent.EndpointsClient;
import com.azure.resourcemanager.trafficmanager.fluent.models.EndpointInner;
import com.azure.resourcemanager.trafficmanager.models.TrafficManagerExternalEndpoint;

/** Implementation for {@link TrafficManagerExternalEndpoint}. */
class TrafficManagerExternalEndpointImpl extends TrafficManagerEndpointImpl implements TrafficManagerExternalEndpoint {
    TrafficManagerExternalEndpointImpl(
        String name, TrafficManagerProfileImpl parent, EndpointInner inner, EndpointsClient client) {
        super(name, parent, inner, client);
    }

    @Override
    public String fqdn() {
        return innerModel().target();
    }

    @Override
    public Region sourceTrafficLocation() {
        return Region.fromName((innerModel().endpointLocation()));
    }
}
