/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.trafficmanager.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.trafficmanager.TrafficManagerNestedProfileEndpoint;

/**
 * Implementation for {@link TrafficManagerNestedProfileEndpoint}.
 */
class TrafficManagerNestedProfileEndpointImpl extends TrafficManagerEndpointImpl
    implements TrafficManagerNestedProfileEndpoint {
    TrafficManagerNestedProfileEndpointImpl(String name,
                                            TrafficManagerProfileImpl parent,
                                            EndpointInner inner,
                                            EndpointsInner client) {
        super(name, parent, inner, client);
    }

    @Override
    public String nestedProfileId() {
        return inner().targetResourceId();
    }

    @Override
    public int minimumChildEndpointCount() {
        if (inner().minChildEndpoints() == null) {
            return 0;
        }
        return inner().minChildEndpoints().intValue();
    }

    @Override
    public Region sourceTrafficLocation() {
        return Region.fromLabelOrName((inner().endpointLocation()));
    }
}
