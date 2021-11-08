// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerregistry.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.resourcemanager.containerregistry.ContainerRegistryManager;
import com.azure.resourcemanager.containerregistry.fluent.models.RegistryInner;
import com.azure.resourcemanager.containerregistry.fluent.models.RunInner;
import com.azure.resourcemanager.containerregistry.models.AccessKeyType;
import com.azure.resourcemanager.containerregistry.models.PublicNetworkAccess;
import com.azure.resourcemanager.containerregistry.models.Registry;
import com.azure.resourcemanager.containerregistry.models.RegistryCredentials;
import com.azure.resourcemanager.containerregistry.models.RegistryTaskRun;
import com.azure.resourcemanager.containerregistry.models.RegistryUpdateParameters;
import com.azure.resourcemanager.containerregistry.models.RegistryUsage;
import com.azure.resourcemanager.containerregistry.models.Sku;
import com.azure.resourcemanager.containerregistry.models.SkuName;
import com.azure.resourcemanager.containerregistry.models.SourceUploadDefinition;
import com.azure.resourcemanager.containerregistry.models.WebhookOperations;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Collection;

/** Implementation for Registry and its create and update interfaces. */
public class RegistryImpl extends GroupableResourceImpl<Registry, RegistryInner, RegistryImpl, ContainerRegistryManager>
    implements Registry, Registry.Definition, Registry.Update {

    private RegistryUpdateParameters updateParameters;
    private WebhooksImpl webhooks;

    protected RegistryImpl(
        String name, RegistryInner innerObject, ContainerRegistryManager manager) {
        super(name, innerObject, manager);

        this.webhooks = new WebhooksImpl(this, "Webhook");
    }

    @Override
    protected Mono<RegistryInner> getInnerAsync() {
        return this.manager().serviceClient().getRegistries()
            .getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public RegistryImpl update() {
        updateParameters = new RegistryUpdateParameters();
        return super.update();
    }

    @Override
    public Mono<Registry> createResourceAsync() {
        final RegistryImpl self = this;
        if (isInCreateMode()) {
            return manager()
                .serviceClient()
                .getRegistries()
                .createAsync(self.resourceGroupName(), self.name(), self.innerModel())
                .map(innerToFluentMap(this));
        } else {
            updateParameters.withTags(innerModel().tags());
            return manager()
                .serviceClient()
                .getRegistries()
                .updateAsync(self.resourceGroupName(), self.name(), self.updateParameters)
                .map(innerToFluentMap(this));
        }
    }

    @Override
    public Mono<Void> afterPostRunAsync(boolean isGroupFaulted) {
        this.webhooks.clear();
        return Mono.empty();
    }

    @Override
    public Sku sku() {
        return this.innerModel().sku();
    }

    @Override
    public String loginServerUrl() {
        return this.innerModel().loginServer();
    }

    @Override
    public OffsetDateTime creationDate() {
        return this.innerModel().creationDate();
    }

    @Override
    public boolean adminUserEnabled() {
        return this.innerModel().adminUserEnabled();
    }

    @Override
    public RegistryImpl withBasicSku() {
        return setManagedSku(new Sku().withName(SkuName.BASIC));
    }

    @Override
    public RegistryImpl withStandardSku() {
        return setManagedSku(new Sku().withName(SkuName.STANDARD));
    }

    @Override
    public RegistryImpl withPremiumSku() {
        return setManagedSku(new Sku().withName(SkuName.PREMIUM));
    }

    private RegistryImpl setManagedSku(Sku sku) {
        if (this.isInCreateMode()) {
            this.innerModel().withSku(sku);
        } else {
            this.updateParameters.withSku(sku);
        }

        return this;
    }

    @Override
    public RegistryImpl withRegistryNameAsAdminUser() {
        if (this.isInCreateMode()) {
            this.innerModel().withAdminUserEnabled(true);
        } else {
            this.updateParameters.withAdminUserEnabled(true);
        }

        return this;
    }

    @Override
    public RegistryImpl withoutRegistryNameAsAdminUser() {
        if (this.isInCreateMode()) {
            this.innerModel().withAdminUserEnabled(false);
        } else {
            this.updateParameters.withAdminUserEnabled(false);
        }

        return this;
    }

    @Override
    public RegistryCredentials getCredentials() {
        return this.manager().containerRegistries().getCredentials(this.resourceGroupName(), this.name());
    }

    @Override
    public Mono<RegistryCredentials> getCredentialsAsync() {
        return this.manager().containerRegistries().getCredentialsAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public RegistryCredentials regenerateCredential(AccessKeyType accessKeyType) {
        return this
            .manager()
            .containerRegistries()
            .regenerateCredential(this.resourceGroupName(), this.name(), accessKeyType);
    }

    @Override
    public Mono<RegistryCredentials> regenerateCredentialAsync(AccessKeyType accessKeyType) {
        return this
            .manager()
            .containerRegistries()
            .regenerateCredentialAsync(this.resourceGroupName(), this.name(), accessKeyType);
    }

    @Override
    public Collection<RegistryUsage> listQuotaUsages() {
        return this.manager().containerRegistries().listQuotaUsages(this.resourceGroupName(), this.name());
    }

    @Override
    public PagedFlux<RegistryUsage> listQuotaUsagesAsync() {
        return this.manager().containerRegistries().listQuotaUsagesAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public WebhookOperations webhooks() {
        return new WebhookOperationsImpl(this);
    }

    @Override
    public PublicNetworkAccess publicNetworkAccess() {
        return innerModel().publicNetworkAccess();
    }

    @Override
    public RegistryTaskRun.DefinitionStages.BlankFromRegistry scheduleRun() {
        return new RegistryTaskRunImpl(this.manager(), new RunInner())
            .withExistingRegistry(this.resourceGroupName(), this.name());
    }

    @Override
    public SourceUploadDefinition getBuildSourceUploadUrl() {
        return this.getBuildSourceUploadUrlAsync().block();
    }

    @Override
    public Mono<SourceUploadDefinition> getBuildSourceUploadUrlAsync() {
        return this
            .manager()
            .serviceClient()
            .getRegistries()
            .getBuildSourceUploadUrlAsync(this.resourceGroupName(), this.name())
            .map(sourceUploadDefinitionInner -> new SourceUploadDefinitionImpl(sourceUploadDefinitionInner));
    }

    @Override
    public RegistryImpl withoutWebhook(String name) {
        webhooks.withoutWebhook(name);
        return this;
    }

    @Override
    public WebhookImpl updateWebhook(String name) {
        return webhooks.updateWebhook(name);
    }

    @Override
    public WebhookImpl defineWebhook(String name) {
        return webhooks.defineWebhook(name);
    }

    @Override
    public RegistryImpl enablePublicNetworkAccess() {
        if (this.isInCreateMode()) {
            this.innerModel().withPublicNetworkAccess(PublicNetworkAccess.ENABLED);
        } else {
            updateParameters.withPublicNetworkAccess(PublicNetworkAccess.ENABLED);
        }
        return this;
    }

    @Override
    public RegistryImpl disablePublicNetworkAccess() {
        if (this.isInCreateMode()) {
            this.innerModel().withPublicNetworkAccess(PublicNetworkAccess.DISABLED);
        } else {
            updateParameters.withPublicNetworkAccess(PublicNetworkAccess.DISABLED);
        }
        return this;
    }
}
