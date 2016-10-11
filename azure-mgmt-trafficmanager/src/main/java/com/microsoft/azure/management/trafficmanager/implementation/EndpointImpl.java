/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.trafficmanager.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.microsoft.azure.management.trafficmanager.Endpoint;
import com.microsoft.azure.management.trafficmanager.EndpointMonitorStatus;
import com.microsoft.azure.management.trafficmanager.Profile;
import com.microsoft.azure.management.trafficmanager.EndpointType;
import rx.Observable;
import rx.functions.Func1;

/**
 * Implementation for {@link Endpoint}
 */
class EndpointImpl extends ExternalChildResourceImpl<Endpoint,
        EndpointInner,
        ProfileImpl,
        Profile>
        implements Endpoint,
        Endpoint.Definition<Profile.DefinitionStages.WithCreate>,
        Endpoint.UpdateDefinition<Profile.Update>,
        Endpoint.UpdateAzureEndpoint,
        Endpoint.UpdateExternalEndpoint,
        Endpoint.UpdateNestedProfileEndpoint {
    private final EndpointsInner client;
    private final String endpointStatusDisabled = "Disabled";
    private final String endpointStatusEnabled = "Enabled";

    EndpointImpl(String name,
                 ProfileImpl parent,
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
    public EndpointType endpointType() {
        return EndpointType.fromValue(this.inner().type());
    }

    @Override
    public EndpointMonitorStatus monitorStatus() {
        return EndpointMonitorStatus.fromValue(this.inner().endpointMonitorStatus());
    }

    @Override
    public boolean isDisabled() {
        return this.inner().endpointStatus().equalsIgnoreCase(this.endpointStatusDisabled);
    }

    @Override
    public int routingWeight() {
        return this.inner().weight().intValue();
    }

    @Override
    public int routingPriority() {
        return this.inner().priority().intValue();
    }

    @Override
    public EndpointImpl withMinimumChildEndpoints(int count) {
        this.inner().withMinChildEndpoints(new Long(count));
        return this;
    }

    @Override
    public EndpointImpl withTargetAzureResourceId(String resourceId) {
        this.inner().withTargetResourceId(resourceId);
        return this;
    }

    @Override
    public EndpointImpl withExternalFqdn(String externalFqdn) {
        this.inner().withTarget(externalFqdn);
        return this;
    }

    public EndpointImpl withSourceTrafficLocation(Region location) {
        this.inner().withEndpointLocation(location.toString());
        return this;
    }

    @Override
    public EndpointImpl withNestedProfile(Profile nestedProfile) {
        this.inner().withTargetResourceId(nestedProfile.id());
        return this;
    }

    @Override
    public EndpointImpl withRoutingPriority(int priority) {
        this.inner().withPriority(new Long(priority));
        return this;
    }

    @Override
    public EndpointImpl withTrafficDisabled() {
        this.inner().withEndpointStatus(this.endpointStatusDisabled);
        return this;
    }

    @Override
    public EndpointImpl withTrafficEnabled() {
        this.inner().withEndpointStatus(this.endpointStatusEnabled);
        return this;
    }

    @Override
    public EndpointImpl withRoutingWeight(int weight) {
        this.inner().withWeight(new Long(weight));
        return this;
    }

    @Override
    public Observable<Endpoint> createAsync() {
        final EndpointImpl self = this;
        return this.client.createOrUpdateAsync(this.parent().resourceGroupName(),
                this.parent().name(),
                this.endpointType().toString(),
                this.name(),
                this.inner())
                .map(new Func1<EndpointInner, Endpoint>() {
                    @Override
                    public Endpoint call(EndpointInner inner) {
                        self.setInner(inner);
                        return self;
                    }
                });
    }

    @Override
    public Observable<Endpoint> updateAsync() {
        return createAsync();
    }

    @Override
    public Observable<Void> deleteAsync() {
        return this.client.deleteAsync(this.parent().resourceGroupName(),
                this.parent().name(),
                this.endpointType().toString(),
                this.name()).map(new Func1<Void, Void>() {
            @Override
            public Void call(Void result) {
                return result;
            }
        });
    }

    @Override
    public ProfileImpl attach() {
        return this.parent().withEndpoint(this);
    }

    @Override
    public EndpointImpl refresh() {
        EndpointInner inner = this.client.get(this.parent().resourceGroupName(),
                this.parent().name(),
                this.endpointType().toString(),
                this.name());
        this.setInner(inner);
        return this;
    }
}
