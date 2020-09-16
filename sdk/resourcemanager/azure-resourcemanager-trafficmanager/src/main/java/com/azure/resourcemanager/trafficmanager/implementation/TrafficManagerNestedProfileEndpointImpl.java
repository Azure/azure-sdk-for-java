// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.trafficmanager.implementation;

import com.azure.resourcemanager.resources.fluentcore.arm.Region;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import com.azure.resourcemanager.trafficmanager.fluent.EndpointsClient;
import com.azure.resourcemanager.trafficmanager.fluent.inner.EndpointInner;
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
        return inner().targetResourceId();
    }

    @Override
    public long minimumChildEndpointCount() {
        return Utils.toPrimitiveLong(inner().minChildEndpoints());
    }

    @Override
    public Region sourceTrafficLocation() {
        return Region.fromName((inner().endpointLocation()));
    }
}
