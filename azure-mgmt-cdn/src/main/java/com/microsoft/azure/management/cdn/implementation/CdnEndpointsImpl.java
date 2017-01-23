/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.cdn.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.cdn.CdnEndpoint;
import com.microsoft.azure.management.cdn.CdnProfile;
import com.microsoft.azure.management.cdn.CheckNameAvailabilityResult;
import com.microsoft.azure.management.cdn.DeepCreatedOrigin;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ExternalChildResourcesCachedImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents an endpoint collection associated with a CDN manager profile.
 */
@LangDefinition
class CdnEndpointsImpl extends
        ExternalChildResourcesCachedImpl<CdnEndpointImpl,
                        CdnEndpoint,
                        EndpointInner,
                        CdnProfileImpl,
                        CdnProfile> {
    private final EndpointsInner client;
    private final OriginsInner originsClient;
    private final CustomDomainsInner customDomainsClient;

    CdnEndpointsImpl(EndpointsInner client,
                     OriginsInner originsClient,
                     CustomDomainsInner customDomainsClient,
                     CdnProfileImpl parent) {
        super(parent, "Endpoint");
        this.client = client;
        this.originsClient = originsClient;
        this.customDomainsClient = customDomainsClient;
        if (parent.id() != null) {
            this.cacheCollection();
        }
    }

    /**
     * @return the azure endpoints as a map indexed by name.
     */
    Map<String, CdnEndpoint> endpointsAsMap() {
        Map<String, CdnEndpoint> result = new HashMap<>();
        for (Map.Entry<String, CdnEndpointImpl> entry : this.collection().entrySet()) {
            CdnEndpointImpl endpoint = entry.getValue();
            result.put(entry.getKey(), endpoint);
        }
        return Collections.unmodifiableMap(result);
    }

    /**
     * Mark the endpoint with given name as to be removed.
     *
     * @param name the name of the endpoint to be removed
     */
    public void remove(String name) {
        this.prepareRemove(name);
    }

    /**
     * Adds the endpoint to the collection.
     *
     * @param endpoint the endpoint
     */
    public void addEndpoint(CdnEndpointImpl endpoint) {
        this.addChildResource(endpoint);
    }

    @Override
    protected List<CdnEndpointImpl> listChildResources() {
        List<CdnEndpointImpl> childResources = new ArrayList<>();

        for (EndpointInner innerEndpoint : this.client.listByProfile(
                                        this.parent().resourceGroupName(),
                                        this.parent().name())) {
            CdnEndpointImpl endpointResource = new CdnEndpointImpl(innerEndpoint.name(),
                    this.parent(),
                    innerEndpoint,
                    this.client,
                    this.originsClient,
                    this.customDomainsClient);
            for (CustomDomainInner customDomain : this.customDomainsClient.listByEndpoint(
                    this.parent().resourceGroupName(),
                    this.parent().name(),
                    innerEndpoint.name())) {
                endpointResource.withCustomDomain(customDomain.hostName());
            }
            childResources.add(endpointResource);
        }
        return Collections.unmodifiableList(childResources);
    }

    @Override
    protected CdnEndpointImpl newChildResource(String name) {
        CdnEndpointImpl endpoint = new CdnEndpointImpl(name,
                this.parent(),
                new EndpointInner(),
                this.client,
                this.originsClient,
                this.customDomainsClient);

        return endpoint;
    }

    public CdnEndpointImpl defineNewEndpoint(String endpointName, String originName, String endpointOriginHostname) {
        CdnEndpointImpl endpoint = this.defineNewEndpoint(endpointName);
        endpoint.inner().origins().add(
                new DeepCreatedOrigin()
                        .withName(originName)
                        .withHostName(endpointOriginHostname));
        return endpoint;
    }

    public CdnEndpointImpl defineNewEndpoint(String endpointName, String endpointOriginHostname) {
        return this.defineNewEndpoint(endpointName, "origin", endpointOriginHostname);
    }

    public CdnEndpointImpl defineNewEndpoint(String name) {
        CdnEndpointImpl endpoint = this.prepareDefine(name);
        endpoint.inner().withLocation(endpoint.parent().region().toString());
        endpoint.inner().withOrigins(new ArrayList<DeepCreatedOrigin>());
        return endpoint;
    }


    public CdnEndpointImpl defineNewEndpoint() {
        String endpointName = this.generateUniqueEndpointName("Endpoint");
        return this.defineNewEndpoint(endpointName);
    }

    public CdnEndpointImpl defineNewEndpointWithOriginHostname(String endpointOriginHostname) {
        String endpointName = this.generateUniqueEndpointName("Endpoint");
        CdnEndpointImpl endpoint = this.defineNewEndpoint(endpointName, "origin", endpointOriginHostname);
        return endpoint;
    }

    public CdnEndpointImpl updateEndpoint(String name) {
        CdnEndpointImpl endpoint = this.prepareUpdate(name);
        return endpoint;
    }

    private String generateUniqueEndpointName(String endpointNamePrefix) {
        String endpointName;
        CheckNameAvailabilityResult result;

        do {
            endpointName = SdkContext.randomResourceName(endpointNamePrefix, 50);

            result = this.parent().checkEndpointNameAvailability(endpointName);

        } while (!result.nameAvailable());

        return endpointName;
    }
}
