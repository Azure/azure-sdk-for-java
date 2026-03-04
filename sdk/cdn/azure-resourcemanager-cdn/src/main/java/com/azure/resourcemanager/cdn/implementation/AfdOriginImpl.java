// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn.implementation;

import com.azure.resourcemanager.cdn.fluent.models.AfdOriginInner;
import com.azure.resourcemanager.cdn.models.AfdOrigin;
import com.azure.resourcemanager.cdn.models.AfdOriginGroup;
import com.azure.resourcemanager.cdn.models.AfdOriginUpdateParameters;
import com.azure.resourcemanager.cdn.models.AfdProvisioningState;
import com.azure.resourcemanager.cdn.models.CdnProfile;
import com.azure.resourcemanager.cdn.models.DeploymentStatus;
import com.azure.resourcemanager.cdn.models.EnabledState;
import com.azure.resourcemanager.cdn.models.OriginCapacityResourceProperties;
import com.azure.resourcemanager.cdn.models.ResourceReference;
import com.azure.resourcemanager.cdn.models.SharedPrivateLinkResourceProperties;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import reactor.core.publisher.Mono;

/**
 * Implementation for {@link AfdOrigin}.
 */
class AfdOriginImpl extends ExternalChildResourceImpl<AfdOrigin, AfdOriginInner, AfdOriginGroupImpl, AfdOriginGroup>
    implements AfdOrigin,
    AfdOrigin.DefinitionStages.Blank<AfdOriginGroup.DefinitionStages.WithAttach<CdnProfile.DefinitionStages.WithStandardCreate>>,
    AfdOrigin.DefinitionStages.WithHostname<AfdOriginGroup.DefinitionStages.WithAttach<CdnProfile.DefinitionStages.WithStandardCreate>>,
    AfdOrigin.DefinitionStages.WithAttach<AfdOriginGroup.DefinitionStages.WithAttach<CdnProfile.DefinitionStages.WithStandardCreate>>,
    AfdOrigin.UpdateDefinitionStages.Blank<AfdOriginGroup.UpdateDefinitionStages.WithAttach<CdnProfile.Update>>,
    AfdOrigin.UpdateDefinitionStages.WithHostname<AfdOriginGroup.UpdateDefinitionStages.WithAttach<CdnProfile.Update>>,
    AfdOrigin.UpdateDefinitionStages.WithAttach<AfdOriginGroup.UpdateDefinitionStages.WithAttach<CdnProfile.Update>>,
    AfdOrigin.Update {

    AfdOriginImpl(String name, AfdOriginGroupImpl parent, AfdOriginInner inner) {
        super(name, parent, inner);
    }

    @Override
    public String id() {
        return this.innerModel().id();
    }

    @Override
    public String originGroupName() {
        return this.innerModel().originGroupName();
    }

    @Override
    public ResourceReference azureOrigin() {
        return this.innerModel().azureOrigin();
    }

    @Override
    public String hostname() {
        return this.innerModel().hostname();
    }

    @Override
    public Integer httpPort() {
        return this.innerModel().httpPort();
    }

    @Override
    public Integer httpsPort() {
        return this.innerModel().httpsPort();
    }

    @Override
    public String originHostHeader() {
        return this.innerModel().originHostHeader();
    }

    @Override
    public Integer priority() {
        return this.innerModel().priority();
    }

    @Override
    public Integer weight() {
        return this.innerModel().weight();
    }

    @Override
    public SharedPrivateLinkResourceProperties sharedPrivateLinkResource() {
        return this.innerModel().sharedPrivateLinkResource();
    }

    @Override
    public OriginCapacityResourceProperties originCapacityResource() {
        return this.innerModel().originCapacityResource();
    }

    @Override
    public EnabledState enabledState() {
        return this.innerModel().enabledState();
    }

    @Override
    public Boolean enforceCertificateNameCheck() {
        return this.innerModel().enforceCertificateNameCheck();
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
    public Mono<AfdOrigin> createResourceAsync() {
        final AfdOriginImpl self = this;
        return this.parent()
            .parent()
            .manager()
            .serviceClient()
            .getAfdOrigins()
            .createAsync(this.parent().parent().resourceGroupName(), this.parent().parent().name(),
                this.parent().name(), this.name(), this.innerModel())
            .map(inner -> {
                self.setInner(inner);
                return self;
            });
    }

    @Override
    public Mono<AfdOrigin> updateResourceAsync() {
        final AfdOriginImpl self = this;
        AfdOriginUpdateParameters parameters
            = new AfdOriginUpdateParameters().withHostname(this.innerModel().hostname())
                .withAzureOrigin(this.innerModel().azureOrigin())
                .withHttpPort(this.innerModel().httpPort())
                .withHttpsPort(this.innerModel().httpsPort())
                .withOriginHostHeader(this.innerModel().originHostHeader())
                .withPriority(this.innerModel().priority())
                .withWeight(this.innerModel().weight())
                .withSharedPrivateLinkResource(this.innerModel().sharedPrivateLinkResource())
                .withOriginCapacityResource(this.innerModel().originCapacityResource())
                .withEnabledState(this.innerModel().enabledState())
                .withEnforceCertificateNameCheck(this.innerModel().enforceCertificateNameCheck());
        return this.parent()
            .parent()
            .manager()
            .serviceClient()
            .getAfdOrigins()
            .updateAsync(this.parent().parent().resourceGroupName(), this.parent().parent().name(),
                this.parent().name(), this.name(), parameters)
            .map(inner -> {
                self.setInner(inner);
                return self;
            });
    }

    @Override
    public Mono<Void> deleteResourceAsync() {
        return this.parent()
            .parent()
            .manager()
            .serviceClient()
            .getAfdOrigins()
            .deleteAsync(this.parent().parent().resourceGroupName(), this.parent().parent().name(),
                this.parent().name(), this.name());
    }

    @Override
    protected Mono<AfdOriginInner> getInnerAsync() {
        return this.parent()
            .parent()
            .manager()
            .serviceClient()
            .getAfdOrigins()
            .getAsync(this.parent().parent().resourceGroupName(), this.parent().parent().name(), this.parent().name(),
                this.name());
    }

    @Override
    public AfdOriginGroupImpl attach() {
        return this.parent().withAfdOrigin(this);
    }

    // ---- Fluent setters (shared by DefinitionStages, UpdateDefinitionStages, and Update) ----

    @Override
    public AfdOriginImpl withHostname(String hostname) {
        this.innerModel().withHostname(hostname);
        return this;
    }

    @Override
    public AfdOriginImpl withAzureOrigin(ResourceReference azureOrigin) {
        this.innerModel().withAzureOrigin(azureOrigin);
        return this;
    }

    @Override
    public AfdOriginImpl withHttpPort(Integer httpPort) {
        this.innerModel().withHttpPort(httpPort);
        return this;
    }

    @Override
    public AfdOriginImpl withHttpsPort(Integer httpsPort) {
        this.innerModel().withHttpsPort(httpsPort);
        return this;
    }

    @Override
    public AfdOriginImpl withOriginHostHeader(String originHostHeader) {
        this.innerModel().withOriginHostHeader(originHostHeader);
        return this;
    }

    @Override
    public AfdOriginImpl withPriority(Integer priority) {
        this.innerModel().withPriority(priority);
        return this;
    }

    @Override
    public AfdOriginImpl withWeight(Integer weight) {
        this.innerModel().withWeight(weight);
        return this;
    }

    @Override
    public AfdOriginImpl withSharedPrivateLinkResource(SharedPrivateLinkResourceProperties sharedPrivateLinkResource) {
        this.innerModel().withSharedPrivateLinkResource(sharedPrivateLinkResource);
        return this;
    }

    @Override
    public AfdOriginImpl withOriginCapacityResource(OriginCapacityResourceProperties originCapacityResource) {
        this.innerModel().withOriginCapacityResource(originCapacityResource);
        return this;
    }

    @Override
    public AfdOriginImpl withEnabledState(EnabledState enabledState) {
        this.innerModel().withEnabledState(enabledState);
        return this;
    }

    @Override
    public AfdOriginImpl withEnforceCertificateNameCheck(Boolean enforce) {
        this.innerModel().withEnforceCertificateNameCheck(enforce);
        return this;
    }
}
