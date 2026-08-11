// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.cdn.implementation;

import com.azure.resourcemanager.cdn.fluent.models.AfdOriginInner;
import com.azure.resourcemanager.cdn.models.AfdOriginUpdateParameters;
import com.azure.resourcemanager.cdn.models.AfdProvisioningState;
import com.azure.resourcemanager.cdn.models.CdnProfile;
import com.azure.resourcemanager.cdn.models.DeploymentStatus;
import com.azure.resourcemanager.cdn.models.EnabledState;
import com.azure.resourcemanager.cdn.models.Origin;
import com.azure.resourcemanager.cdn.models.OriginGroup;
import com.azure.resourcemanager.cdn.models.ResourceReference;
import com.azure.resourcemanager.cdn.models.SharedPrivateLinkResourceProperties;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ExternalChildResourceImpl;
import reactor.core.publisher.Mono;

/**
 * Implementation for {@link Origin}.
 */
class OriginImpl extends ExternalChildResourceImpl<Origin, AfdOriginInner, OriginGroupImpl, OriginGroup>
    implements Origin,
    Origin.DefinitionStages.Blank<OriginGroup.DefinitionStages.WithAttach<CdnProfile.DefinitionStages.WithStandardCreate>>,
    Origin.DefinitionStages.WithHostname<OriginGroup.DefinitionStages.WithAttach<CdnProfile.DefinitionStages.WithStandardCreate>>,
    Origin.DefinitionStages.WithAttach<OriginGroup.DefinitionStages.WithAttach<CdnProfile.DefinitionStages.WithStandardCreate>>,
    Origin.UpdateDefinitionStages.Blank<OriginGroup.Update>,
    Origin.UpdateDefinitionStages.WithHostname<OriginGroup.Update>,
    Origin.UpdateDefinitionStages.WithAttach<OriginGroup.Update>, Origin.Update {

    OriginImpl(String name, OriginGroupImpl parent, AfdOriginInner inner) {
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
    public String azureOriginResourceId() {
        ResourceReference ref = this.innerModel().azureOrigin();
        return ref == null ? null : ref.id();
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
    public Mono<Origin> createResourceAsync() {
        final OriginImpl self = this;
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
    public Mono<Origin> updateResourceAsync() {
        final OriginImpl self = this;
        AfdOriginUpdateParameters parameters
            = new AfdOriginUpdateParameters().withHostname(this.innerModel().hostname())
                .withAzureOrigin(this.innerModel().azureOrigin())
                .withHttpPort(this.innerModel().httpPort())
                .withHttpsPort(this.innerModel().httpsPort())
                .withOriginHostHeader(this.innerModel().originHostHeader())
                .withPriority(this.innerModel().priority())
                .withWeight(this.innerModel().weight())
                .withSharedPrivateLinkResource(this.innerModel().sharedPrivateLinkResource())
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
    public OriginGroupImpl attach() {
        return this.parent().withOrigin(this);
    }

    // ---- Fluent setters (shared by DefinitionStages, UpdateDefinitionStages, and Update) ----

    @Override
    public OriginImpl withHostname(String hostname) {
        this.innerModel().withHostname(hostname);
        return this;
    }

    @Override
    public OriginImpl withAzureOriginResourceId(String azureOriginResourceId) {
        this.innerModel()
            .withAzureOrigin(
                azureOriginResourceId == null ? null : new ResourceReference().withId(azureOriginResourceId));
        return this;
    }

    @Override
    public OriginImpl withHttpPort(Integer httpPort) {
        this.innerModel().withHttpPort(httpPort);
        return this;
    }

    @Override
    public OriginImpl withHttpsPort(Integer httpsPort) {
        this.innerModel().withHttpsPort(httpsPort);
        return this;
    }

    @Override
    public OriginImpl withOriginHostHeader(String originHostHeader) {
        this.innerModel().withOriginHostHeader(originHostHeader);
        return this;
    }

    @Override
    public OriginImpl withPriority(Integer priority) {
        this.innerModel().withPriority(priority);
        return this;
    }

    @Override
    public OriginImpl withWeight(Integer weight) {
        this.innerModel().withWeight(weight);
        return this;
    }

    @Override
    public OriginImpl withSharedPrivateLinkResource(SharedPrivateLinkResourceProperties sharedPrivateLinkResource) {
        this.innerModel().withSharedPrivateLinkResource(sharedPrivateLinkResource);
        return this;
    }

    @Override
    public OriginImpl withEnabledState(EnabledState enabledState) {
        this.innerModel().withEnabledState(enabledState);
        return this;
    }

    @Override
    public OriginImpl withEnforceCertificateNameCheck(Boolean enforce) {
        this.innerModel().withEnforceCertificateNameCheck(enforce);
        return this;
    }
}
