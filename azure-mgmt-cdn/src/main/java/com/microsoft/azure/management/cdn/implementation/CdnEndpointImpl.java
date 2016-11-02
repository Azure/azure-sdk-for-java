/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.cdn.implementation;

import com.microsoft.azure.management.cdn.CdnEndpoint;
import com.microsoft.azure.management.cdn.CdnProfile;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import rx.Observable;
import rx.functions.Func1;

/**
 * Implementation for {@link CdnEndpoint}.
 */
class CdnEndpointImpl extends ExternalChildResourceImpl<CdnEndpoint,
        EndpointInner,
        CdnProfileImpl,
        CdnProfile>
        implements CdnEndpoint,
        CdnEndpoint.Definition<CdnProfile.DefinitionStages.WithCreate>
        /*TrafficManagerEndpoint.UpdateDefinition<TrafficManagerProfile.Update>,
        TrafficManagerEndpoint.UpdateAzureEndpoint,
        TrafficManagerEndpoint.UpdateExternalEndpoint,
        TrafficManagerEndpoint.UpdateNestedProfileEndpoint*/ {
    private final EndpointsInner client;

    CdnEndpointImpl(String name,
                    CdnProfileImpl parent,
                    EndpointInner inner,
                    EndpointsInner client) {
        super(name, parent, inner);
        this.client = client;
    }

    @Override
    public String id() {
        return this.inner().id();
    }

    @Override
    public Observable<CdnEndpoint> createAsync() {
        final CdnEndpointImpl self = this;
        return this.client.createAsync(this.parent().resourceGroupName(),
                this.parent().name(),
                this.name(),
                this.inner())
                .map(new Func1<EndpointInner, CdnEndpoint>() {
                    @Override
                    public CdnEndpoint call(EndpointInner inner) {
                        self.setInner(inner);
                        return self;
                    }
                });
    }

    @Override
    public Observable<CdnEndpoint> updateAsync() {
        return createAsync();
    }

    @Override
    public Observable<Void> deleteAsync() {
        return this.client.deleteAsync(this.parent().resourceGroupName(),
                this.parent().name(),
                this.name()).map(new Func1<Void, Void>() {
            @Override
            public Void call(Void result) {
                return result;
            }
        });
    }

    /*@Override
    public CdnProfileImpl attach() {
        return this.parent().withEndpoint(this);
    }*/

    @Override
    public CdnEndpointImpl refresh() {
        EndpointInner inner = this.client.get(this.parent().resourceGroupName(),
                this.parent().name(),
                this.name());
        this.setInner(inner);
        return this;
    }

    @Override
    public CdnEndpointImpl withOrigin(String originName, String hostname) {
        return this;
    }

    @Override
    public CdnEndpointImpl withOrigin(String originName, String hostname, int httpPort) {
        return this;
    }

    @Override
    public CdnEndpointImpl withOrigin(String originName, String hostname, int httpPort, int httpsPort) {
        return this;
    }

    @Override
    public CdnEndpointImpl withOriginHttpPort(int httpPort) {
        return this;
    }

    @Override
    public CdnEndpointImpl withOriginHttpsPort(int httpsPort) {
        return this;
    }

    @Override
    public CdnEndpointImpl withCustomDomain(String hostName) {
        return this;
    }

    @Override
    public CdnProfileImpl attach() {
        return null;
    }
}
