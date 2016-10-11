/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.trafficmanager.implementation;

import com.microsoft.azure.management.trafficmanager.ExternalEndpoint;

/**
 * Implementation for  {@link ExternalEndpoint}
 */
class ExternalEndpointImpl extends EndpointImpl
    implements ExternalEndpoint {
    ExternalEndpointImpl(String name,
                      ProfileImpl parent,
                      EndpointInner inner,
                      EndpointsInner client) {
        super(name, parent, inner, client);
    }

    @Override
    public String fqdn() {
        return inner().target();
    }
}
