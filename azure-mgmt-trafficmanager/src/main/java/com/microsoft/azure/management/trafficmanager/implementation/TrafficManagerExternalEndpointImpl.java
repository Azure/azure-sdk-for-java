/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.trafficmanager.implementation;

import com.microsoft.azure.management.trafficmanager.TrafficManagerExternalEndpoint;

/**
 * Implementation for  {@link TrafficManagerExternalEndpoint}
 */
class TrafficManagerExternalEndpointImpl extends TrafficManagerEndpointImpl
    implements TrafficManagerExternalEndpoint {
    TrafficManagerExternalEndpointImpl(String name,
                                       TrafficManagerProfileImpl parent,
                                       EndpointInner inner,
                                       EndpointsInner client) {
        super(name, parent, inner, client);
    }

    @Override
    public String fqdn() {
        return inner().target();
    }
}
