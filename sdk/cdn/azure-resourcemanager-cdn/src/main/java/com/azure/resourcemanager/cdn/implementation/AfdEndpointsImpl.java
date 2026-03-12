// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn.implementation;

import com.azure.resourcemanager.cdn.fluent.models.AfdEndpointInner;
import com.azure.resourcemanager.cdn.models.AfdEndpoint;
import com.azure.resourcemanager.cdn.models.CdnProfile;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesNonCachedImpl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents a collection of AFD endpoints associated with a CDN profile.
 */
class AfdEndpointsImpl extends
    ExternalChildResourcesNonCachedImpl<AfdEndpointImpl, AfdEndpoint, AfdEndpointInner, CdnProfileImpl, CdnProfile> {

    AfdEndpointsImpl(CdnProfileImpl parent) {
        super(parent, parent.taskGroup(), "AfdEndpoint");
    }

    Map<String, AfdEndpoint> endpointsAsMap() {
        Map<String, AfdEndpoint> result = new HashMap<>();
        for (AfdEndpointInner endpointInner : this.getParent()
            .manager()
            .serviceClient()
            .getAfdEndpoints()
            .listByProfile(this.getParent().resourceGroupName(), this.getParent().name())) {
            AfdEndpointImpl endpoint = new AfdEndpointImpl(endpointInner.name(), this.getParent(), endpointInner);
            result.put(endpoint.name(), endpoint);
        }
        return Collections.unmodifiableMap(result);
    }

    void remove(String name) {
        this.prepareInlineRemove(new AfdEndpointImpl(name, getParent(), new AfdEndpointInner()));
    }

    void addEndpoint(AfdEndpointImpl endpoint) {
        this.childCollection.put(endpoint.name(), endpoint);
    }

    AfdEndpointImpl defineNewEndpoint(String name) {
        AfdEndpointImpl endpoint
            = this.prepareInlineDefine(new AfdEndpointImpl(name, this.getParent(), new AfdEndpointInner()));
        if (endpoint.parent().region() != null) {
            endpoint.innerModel().withLocation(endpoint.parent().region().toString());
        }
        return endpoint;
    }

    AfdEndpointImpl updateEndpoint(String name) {
        AfdEndpointInner endpointInner = this.getParent()
            .manager()
            .serviceClient()
            .getAfdEndpoints()
            .get(this.getParent().resourceGroupName(), this.getParent().name(), name);
        return this.prepareInlineUpdate(new AfdEndpointImpl(name, this.getParent(), endpointInner));
    }
}
