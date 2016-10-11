/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.trafficmanager.implementation;

import com.microsoft.azure.management.trafficmanager.NestedProfileEndpoint;

/**
 * Implementation for {@link NestedProfileEndpoint}
 */
public class NestedProfileEndpointImpl extends EndpointImpl
    implements NestedProfileEndpoint {
    NestedProfileEndpointImpl(String name,
                      ProfileImpl parent,
                      EndpointInner inner,
                      EndpointsInner client) {
        super(name, parent, inner, client);
    }

    @Override
    public String nestedProfileId() {
        return inner().targetResourceId();
    }

    @Override
    public int minChildEndpoints() {
        if (inner().minChildEndpoints() == null) {
            return 0;
        }
        return inner().minChildEndpoints().intValue();
    }
}
