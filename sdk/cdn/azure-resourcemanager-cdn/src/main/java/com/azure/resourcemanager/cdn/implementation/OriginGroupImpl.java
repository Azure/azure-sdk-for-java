// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn.implementation;

import com.azure.resourcemanager.cdn.fluent.models.AfdOriginGroupInner;
import com.azure.resourcemanager.cdn.models.AfdOriginGroupUpdateParameters;
import com.azure.resourcemanager.cdn.models.AfdProvisioningState;
import com.azure.resourcemanager.cdn.models.CdnProfile;
import com.azure.resourcemanager.cdn.models.DeploymentStatus;
import com.azure.resourcemanager.cdn.models.EnabledState;
import com.azure.resourcemanager.cdn.models.HealthProbeParameters;
import com.azure.resourcemanager.cdn.models.LoadBalancingSettingsParameters;
import com.azure.resourcemanager.cdn.models.Origin;
import com.azure.resourcemanager.cdn.models.OriginAuthenticationProperties;
import com.azure.resourcemanager.cdn.models.OriginGroup;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Implementation for {@link OriginGroup}.
 */
class OriginGroupImpl extends ExternalChildResourceImpl<OriginGroup, AfdOriginGroupInner, CdnProfileImpl, CdnProfile>
    implements OriginGroup, OriginGroup.DefinitionStages.Blank<CdnProfile.DefinitionStages.WithStandardCreate>,
    OriginGroup.DefinitionStages.WithAttach<CdnProfile.DefinitionStages.WithStandardCreate>,
    OriginGroup.UpdateDefinitionStages.Blank<CdnProfile.Update>,
    OriginGroup.UpdateDefinitionStages.WithAttach<CdnProfile.Update>, OriginGroup.Update {

    private final OriginsImpl origins;

    OriginGroupImpl(String name, CdnProfileImpl parent, AfdOriginGroupInner inner) {
        super(name, parent, inner);
        this.origins = new OriginsImpl(this);
    }

    @Override
    public String id() {
        return this.innerModel().id();
    }

    @Override
    public String profileName() {
        return this.innerModel().profileName();
    }

    @Override
    public LoadBalancingSettingsParameters loadBalancingSettings() {
        return this.innerModel().loadBalancingSettings();
    }

    @Override
    public HealthProbeParameters healthProbeSettings() {
        return this.innerModel().healthProbeSettings();
    }

    @Override
    public Integer trafficRestorationTimeToHealedOrNewEndpointsInMinutes() {
        return this.innerModel().trafficRestorationTimeToHealedOrNewEndpointsInMinutes();
    }

    @Override
    public EnabledState sessionAffinityState() {
        return this.innerModel().sessionAffinityState();
    }

    @Override
    public OriginAuthenticationProperties authentication() {
        return this.innerModel().authentication();
    }

    @Override
    public AfdProvisioningState provisioningState() {
        return this.innerModel().provisioningState();
    }

    @Override
    public DeploymentStatus deploymentStatus() {
        return this.innerModel().deploymentStatus();
    }

    @Override
    public Map<String, Origin> origins() {
        return this.origins.originsAsMap();
    }

    @Override
    public Mono<OriginGroup> createResourceAsync() {
        final OriginGroupImpl self = this;
        return this.parent()
            .manager()
            .serviceClient()
            .getAfdOriginGroups()
            .createAsync(this.parent().resourceGroupName(), this.parent().name(), this.name(), this.innerModel())
            .map(inner -> {
                self.setInner(inner);
                return self;
            });
    }

    @Override
    public Mono<OriginGroup> updateResourceAsync() {
        final OriginGroupImpl self = this;
        AfdOriginGroupUpdateParameters parameters
            = new AfdOriginGroupUpdateParameters().withLoadBalancingSettings(this.innerModel().loadBalancingSettings())
                .withHealthProbeSettings(this.innerModel().healthProbeSettings())
                .withTrafficRestorationTimeToHealedOrNewEndpointsInMinutes(
                    this.innerModel().trafficRestorationTimeToHealedOrNewEndpointsInMinutes())
                .withSessionAffinityState(this.innerModel().sessionAffinityState());
        return this.parent()
            .manager()
            .serviceClient()
            .getAfdOriginGroups()
            .updateAsync(this.parent().resourceGroupName(), this.parent().name(), this.name(), parameters)
            .map(inner -> {
                self.setInner(inner);
                return self;
            });
    }

    @Override
    public Mono<Void> deleteResourceAsync() {
        return this.parent()
            .manager()
            .serviceClient()
            .getAfdOriginGroups()
            .deleteAsync(this.parent().resourceGroupName(), this.parent().name(), this.name());
    }

    @Override
    protected Mono<AfdOriginGroupInner> getInnerAsync() {
        return this.parent()
            .manager()
            .serviceClient()
            .getAfdOriginGroups()
            .getAsync(this.parent().resourceGroupName(), this.parent().name(), this.name());
    }

    @Override
    protected Mono<Void> afterPostRunAsync(boolean isGroupFaulted) {
        this.origins.clear();
        return Mono.empty();
    }

    @Override
    public CdnProfileImpl attach() {
        return this.parent().withOriginGroup(this);
    }

    // ---- Fluent setters (shared by DefinitionStages, UpdateDefinitionStages, and Update) ----

    @Override
    public OriginGroupImpl withLoadBalancingSettings(LoadBalancingSettingsParameters loadBalancingSettings) {
        this.innerModel().withLoadBalancingSettings(loadBalancingSettings);
        return this;
    }

    @Override
    public OriginGroupImpl withHealthProbeSettings(HealthProbeParameters healthProbeSettings) {
        this.innerModel().withHealthProbeSettings(healthProbeSettings);
        return this;
    }

    @Override
    public OriginGroupImpl withTrafficRestorationTimeToHealedOrNewEndpointsInMinutes(Integer minutes) {
        this.innerModel().withTrafficRestorationTimeToHealedOrNewEndpointsInMinutes(minutes);
        return this;
    }

    @Override
    public OriginGroupImpl withSessionAffinityState(EnabledState sessionAffinityState) {
        this.innerModel().withSessionAffinityState(sessionAffinityState);
        return this;
    }

    // ---- Nested origin CRUD ----

    @Override
    public OriginImpl defineOrigin(String name) {
        return this.origins.defineNewOrigin(name);
    }

    @Override
    public OriginImpl updateOrigin(String name) {
        return this.origins.updateOrigin(name);
    }

    @Override
    public OriginGroupImpl withoutOrigin(String name) {
        this.origins.remove(name);
        return this;
    }

    OriginGroupImpl withOrigin(OriginImpl origin) {
        this.origins.addOrigin(origin);
        return this;
    }
}
