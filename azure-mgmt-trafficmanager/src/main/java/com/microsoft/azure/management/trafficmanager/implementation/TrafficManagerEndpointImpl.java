/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.trafficmanager.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.Region;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.azure.management.trafficmanager.TrafficManagerEndpoint;
import com.microsoft.azure.management.trafficmanager.EndpointMonitorStatus;
import com.microsoft.azure.management.trafficmanager.TrafficManagerProfile;
import com.microsoft.azure.management.trafficmanager.EndpointType;
import rx.Observable;
import rx.functions.Func1;

/**
 * Implementation for {@link TrafficManagerEndpoint}.
 */
@LangDefinition
class TrafficManagerEndpointImpl extends ExternalChildResourceImpl<TrafficManagerEndpoint,
        EndpointInner,
        TrafficManagerProfileImpl,
        TrafficManagerProfile>
        implements TrafficManagerEndpoint,
        TrafficManagerEndpoint.Definition<TrafficManagerProfile.DefinitionStages.WithCreate>,
        TrafficManagerEndpoint.UpdateDefinition<TrafficManagerProfile.Update>,
        TrafficManagerEndpoint.UpdateAzureEndpoint,
        TrafficManagerEndpoint.UpdateExternalEndpoint,
        TrafficManagerEndpoint.UpdateNestedProfileEndpoint {
    private final EndpointsInner client;
    private final String endpointStatusDisabled = "Disabled";
    private final String endpointStatusEnabled = "Enabled";

    TrafficManagerEndpointImpl(String name,
                               TrafficManagerProfileImpl parent,
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
        return new EndpointMonitorStatus(this.inner().endpointMonitorStatus());
    }

    @Override
    public boolean isEnabled() {
        return this.inner().endpointStatus().equalsIgnoreCase(this.endpointStatusEnabled);
    }

    @Override
    public long routingWeight() {
        return Utils.toPrimitiveLong(this.inner().weight());
    }

    @Override
    public long routingPriority() {
        return Utils.toPrimitiveLong(this.inner().priority());
    }

    @Override
    public TrafficManagerEndpointImpl toResourceId(String resourceId) {
        this.inner().withTargetResourceId(resourceId);
        return this;
    }

    @Override
    public TrafficManagerEndpointImpl toFqdn(String externalFqdn) {
        this.inner().withTarget(externalFqdn);
        return this;
    }

    @Override
    public TrafficManagerEndpointImpl toProfile(TrafficManagerProfile nestedProfile) {
        this.inner().withTargetResourceId(nestedProfile.id());
        this.inner().withMinChildEndpoints(new Long(1));
        return this;
    }

    public TrafficManagerEndpointImpl fromRegion(Region location) {
        this.inner().withEndpointLocation(location.toString());
        return this;
    }

    @Override
    public TrafficManagerEndpointImpl withMinimumEndpointsToEnableTraffic(int count) {
        this.inner().withMinChildEndpoints(new Long(count));
        return this;
    }

    @Override
    public TrafficManagerEndpointImpl withRoutingPriority(int priority) {
        this.inner().withPriority(new Long(priority));
        return this;
    }

    @Override
    public TrafficManagerEndpointImpl withTrafficDisabled() {
        this.inner().withEndpointStatus(this.endpointStatusDisabled);
        return this;
    }

    @Override
    public TrafficManagerEndpointImpl withTrafficEnabled() {
        this.inner().withEndpointStatus(this.endpointStatusEnabled);
        return this;
    }

    @Override
    public TrafficManagerEndpointImpl withRoutingWeight(int weight) {
        this.inner().withWeight(new Long(weight));
        return this;
    }

    @Override
    public Observable<TrafficManagerEndpoint> createAsync() {
        final TrafficManagerEndpointImpl self = this;
        return this.client.createOrUpdateAsync(this.parent().resourceGroupName(),
                this.parent().name(),
                this.endpointType().localName(),
                this.name(),
                this.inner())
                .map(new Func1<EndpointInner, TrafficManagerEndpoint>() {
                    @Override
                    public TrafficManagerEndpoint call(EndpointInner inner) {
                        self.setInner(inner);
                        return self;
                    }
                });
    }

    @Override
    public Observable<TrafficManagerEndpoint> updateAsync() {
        return createAsync();
    }

    @Override
    public Observable<Void> deleteAsync() {
        return this.client.deleteAsync(this.parent().resourceGroupName(),
                this.parent().name(),
                this.endpointType().localName(),
                this.name());
    }

    @Override
    public TrafficManagerProfileImpl attach() {
        return this.parent().withEndpoint(this);
    }

    @Override
    protected Observable<EndpointInner> getInnerAsync() {
        return this.client.getAsync(this.parent().resourceGroupName(),
                this.parent().name(),
                this.endpointType().toString(),
                this.name());
    }
}
