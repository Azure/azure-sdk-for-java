// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn.implementation;

import com.azure.resourcemanager.cdn.fluent.models.AfdOriginGroupInner;
import com.azure.resourcemanager.cdn.models.AfdOrigin;
import com.azure.resourcemanager.cdn.models.AfdOriginGroup;
import com.azure.resourcemanager.cdn.models.AfdOriginGroupUpdateParameters;
import com.azure.resourcemanager.cdn.models.AfdProvisioningState;
import com.azure.resourcemanager.cdn.models.CdnProfile;
import com.azure.resourcemanager.cdn.models.DeploymentStatus;
import com.azure.resourcemanager.cdn.models.EnabledState;
import com.azure.resourcemanager.cdn.models.HealthProbeParameters;
import com.azure.resourcemanager.cdn.models.LoadBalancingSettingsParameters;
import com.azure.resourcemanager.cdn.models.OriginAuthenticationProperties;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import reactor.core.publisher.Mono;

import java.util.Map;

/**
 * Implementation for {@link AfdOriginGroup}.
 */
class AfdOriginGroupImpl
    extends ExternalChildResourceImpl<AfdOriginGroup, AfdOriginGroupInner, CdnProfileImpl, CdnProfile>
    implements AfdOriginGroup, AfdOriginGroup.DefinitionStages.Blank<CdnProfile.DefinitionStages.WithStandardCreate>,
    AfdOriginGroup.DefinitionStages.WithAttach<CdnProfile.DefinitionStages.WithStandardCreate>,
    AfdOriginGroup.UpdateDefinitionStages.Blank<CdnProfile.Update>,
    AfdOriginGroup.UpdateDefinitionStages.WithAttach<CdnProfile.Update>, AfdOriginGroup.Update {

    private final AfdOriginsImpl afdOrigins;

    AfdOriginGroupImpl(String name, CdnProfileImpl parent, AfdOriginGroupInner inner) {
        super(name, parent, inner);
        this.afdOrigins = new AfdOriginsImpl(this);
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
    public Map<String, AfdOrigin> origins() {
        return this.afdOrigins.originsAsMap();
    }

    @Override
    public Mono<AfdOriginGroup> createResourceAsync() {
        final AfdOriginGroupImpl self = this;
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
    public Mono<AfdOriginGroup> updateResourceAsync() {
        final AfdOriginGroupImpl self = this;
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
        this.afdOrigins.clear();
        return Mono.empty();
    }

    @Override
    public CdnProfileImpl attach() {
        return this.parent().withAfdOriginGroup(this);
    }

    // ---- Fluent setters (shared by DefinitionStages, UpdateDefinitionStages, and Update) ----

    @Override
    public AfdOriginGroupImpl withLoadBalancingSettings(LoadBalancingSettingsParameters loadBalancingSettings) {
        this.innerModel().withLoadBalancingSettings(loadBalancingSettings);
        return this;
    }

    @Override
    public AfdOriginGroupImpl withHealthProbeSettings(HealthProbeParameters healthProbeSettings) {
        this.innerModel().withHealthProbeSettings(healthProbeSettings);
        return this;
    }

    @Override
    public AfdOriginGroupImpl withTrafficRestorationTimeToHealedOrNewEndpointsInMinutes(Integer minutes) {
        this.innerModel().withTrafficRestorationTimeToHealedOrNewEndpointsInMinutes(minutes);
        return this;
    }

    @Override
    public AfdOriginGroupImpl withSessionAffinityState(EnabledState sessionAffinityState) {
        this.innerModel().withSessionAffinityState(sessionAffinityState);
        return this;
    }

    // ---- Nested origin CRUD ----

    @Override
    public AfdOriginImpl defineAfdOrigin(String name) {
        return this.afdOrigins.defineNewOrigin(name);
    }

    @Override
    public AfdOriginImpl updateAfdOrigin(String name) {
        return this.afdOrigins.updateOrigin(name);
    }

    @Override
    public AfdOriginGroupImpl withoutAfdOrigin(String name) {
        this.afdOrigins.remove(name);
        return this;
    }

    AfdOriginGroupImpl withAfdOrigin(AfdOriginImpl origin) {
        this.afdOrigins.addOrigin(origin);
        return this;
    }
}
