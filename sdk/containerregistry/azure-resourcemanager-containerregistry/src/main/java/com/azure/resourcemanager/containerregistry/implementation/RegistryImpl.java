// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerregistry.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.containerregistry.ContainerRegistryManager;
import com.azure.resourcemanager.containerregistry.fluent.models.PrivateEndpointConnectionInner;
import com.azure.resourcemanager.containerregistry.fluent.models.PrivateLinkResourceInner;
import com.azure.resourcemanager.containerregistry.fluent.models.RegistryInner;
import com.azure.resourcemanager.containerregistry.fluent.models.RunInner;
import com.azure.resourcemanager.containerregistry.models.AccessKeyType;
import com.azure.resourcemanager.containerregistry.models.Action;
import com.azure.resourcemanager.containerregistry.models.ActionsRequired;
import com.azure.resourcemanager.containerregistry.models.ConnectionStatus;
import com.azure.resourcemanager.containerregistry.models.DefaultAction;
import com.azure.resourcemanager.containerregistry.models.IpRule;
import com.azure.resourcemanager.containerregistry.models.NetworkRuleBypassOptions;
import com.azure.resourcemanager.containerregistry.models.NetworkRuleSet;
import com.azure.resourcemanager.containerregistry.models.PrivateLinkServiceConnectionState;
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
import com.azure.resourcemanager.containerregistry.models.ZoneRedundancy;
import com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateEndpoint;
import com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateEndpointConnection;
import com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateEndpointConnectionProvisioningState;
import com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateLinkResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

/** Implementation for Registry and its create and update interfaces. */
public class RegistryImpl extends GroupableResourceImpl<Registry, RegistryInner, RegistryImpl, ContainerRegistryManager>
    implements Registry, Registry.Definition, Registry.Update {

    private RegistryUpdateParameters updateParameters;
    private WebhooksImpl webhooks;

    protected RegistryImpl(String name, RegistryInner innerObject, ContainerRegistryManager manager) {
        super(name, innerObject, manager);

        this.webhooks = new WebhooksImpl(this, "Webhook");
    }

    @Override
    protected Mono<RegistryInner> getInnerAsync() {
        return this.manager()
            .serviceClient()
            .getRegistries()
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
            return manager().serviceClient()
                .getRegistries()
                .createAsync(self.resourceGroupName(), self.name(), self.innerModel())
                .map(innerToFluentMap(this));
        } else {
            updateParameters.withTags(innerModel().tags());
            return manager().serviceClient()
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
        return this.manager()
            .containerRegistries()
            .regenerateCredential(this.resourceGroupName(), this.name(), accessKeyType);
    }

    @Override
    public Mono<RegistryCredentials> regenerateCredentialAsync(AccessKeyType accessKeyType) {
        return this.manager()
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
    public boolean canAccessFromTrustedServices() {
        return this.innerModel().networkRuleBypassOptions() == NetworkRuleBypassOptions.AZURE_SERVICES;
    }

    @Override
    public NetworkRuleSet networkRuleSet() {
        return this.innerModel().networkRuleSet();
    }

    @Override
    public boolean isDedicatedDataEndpointsEnabled() {
        return ResourceManagerUtils.toPrimitiveBoolean(this.innerModel().dataEndpointEnabled());
    }

    @Override
    public boolean isZoneRedundancyEnabled() {
        return !Objects.isNull(this.innerModel().zoneRedundancy())
            && ZoneRedundancy.ENABLED.equals(this.innerModel().zoneRedundancy());
    }

    @Override
    public List<String> dedicatedDataEndpointsHostNames() {
        return this.innerModel().dataEndpointHostNames() == null
            ? Collections.emptyList()
            : Collections.unmodifiableList(this.innerModel().dataEndpointHostNames());
    }

    @Override
    public RegistryTaskRun.DefinitionStages.BlankFromRegistry scheduleRun() {
        return new RegistryTaskRunImpl(this.manager(), new RunInner()).withExistingRegistry(this.resourceGroupName(),
            this.name());
    }

    @Override
    public SourceUploadDefinition getBuildSourceUploadUrl() {
        return this.getBuildSourceUploadUrlAsync().block();
    }

    @Override
    public Mono<SourceUploadDefinition> getBuildSourceUploadUrlAsync() {
        return this.manager()
            .taskClient()
            .getRegistryTasks()
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

    @Override
    public RegistryImpl withAccessFromSelectedNetworks() {
        ensureNetworkRuleSet();
        if (isInCreateMode()) {
            this.innerModel().networkRuleSet().withDefaultAction(DefaultAction.DENY);
        } else {
            updateParameters.networkRuleSet().withDefaultAction(DefaultAction.DENY);
        }
        return this;
    }

    @Override
    public RegistryImpl withAccessFromAllNetworks() {
        ensureNetworkRuleSet();
        if (isInCreateMode()) {
            this.innerModel().networkRuleSet().withDefaultAction(DefaultAction.ALLOW);
        } else {
            updateParameters.networkRuleSet().withDefaultAction(DefaultAction.ALLOW);
        }
        return this;
    }

    @Override
    public RegistryImpl withAccessFromIpAddressRange(String ipAddressCidr) {
        ensureNetworkRuleSet();
        if (this.innerModel()
            .networkRuleSet()
            .ipRules()
            .stream()
            .noneMatch(ipRule -> Objects.equals(ipRule.ipAddressOrRange(), ipAddressCidr))) {
            this.innerModel()
                .networkRuleSet()
                .ipRules()
                .add(new IpRule().withAction(Action.ALLOW).withIpAddressOrRange(ipAddressCidr));
        }
        if (!isInCreateMode()) {
            updateParameters.networkRuleSet().withIpRules(this.innerModel().networkRuleSet().ipRules());
        }
        return this;
    }

    @Override
    public RegistryImpl withoutAccessFromIpAddressRange(String ipAddressCidr) {
        if (this.innerModel().networkRuleSet() == null) {
            return this;
        }
        ensureNetworkRuleSet();
        this.innerModel()
            .networkRuleSet()
            .ipRules()
            .removeIf(ipRule -> Objects.equals(ipRule.ipAddressOrRange(), ipAddressCidr));
        if (!isInCreateMode()) {
            updateParameters.networkRuleSet().withIpRules(this.innerModel().networkRuleSet().ipRules());
        }
        return this;
    }

    @Override
    public RegistryImpl withAccessFromIpAddress(String ipAddress) {
        return withAccessFromIpAddressRange(ipAddress);
    }

    @Override
    public RegistryImpl withoutAccessFromIpAddress(String ipAddress) {
        return withoutAccessFromIpAddressRange(ipAddress);
    }

    @Override
    public RegistryImpl withAccessFromTrustedServices() {
        if (isInCreateMode()) {
            this.innerModel().withNetworkRuleBypassOptions(NetworkRuleBypassOptions.AZURE_SERVICES);
        } else {
            updateParameters.withNetworkRuleBypassOptions(NetworkRuleBypassOptions.AZURE_SERVICES);
        }
        return this;
    }

    @Override
    public RegistryImpl withoutAccessFromTrustedServices() {
        if (isInCreateMode()) {
            this.innerModel().withNetworkRuleBypassOptions(NetworkRuleBypassOptions.NONE);
        } else {
            updateParameters.withNetworkRuleBypassOptions(NetworkRuleBypassOptions.NONE);
        }
        return this;
    }

    @Override
    public RegistryImpl enableDedicatedDataEndpoints() {
        if (isInCreateMode()) {
            this.innerModel().withDataEndpointEnabled(true);
        } else {
            updateParameters.withDataEndpointEnabled(true);
        }
        return this;
    }

    @Override
    public RegistryImpl disableDedicatedDataEndpoints() {
        if (isInCreateMode()) {
            this.innerModel().withDataEndpointEnabled(false);
        } else {
            updateParameters.withDataEndpointEnabled(false);
        }
        return this;
    }

    @Override
    public PagedIterable<PrivateEndpointConnection> listPrivateEndpointConnections() {
        return new PagedIterable<>(this.listPrivateEndpointConnectionsAsync());
    }

    @Override
    public PagedFlux<PrivateEndpointConnection> listPrivateEndpointConnectionsAsync() {
        return PagedConverter.mapPage(this.manager()
            .serviceClient()
            .getPrivateEndpointConnections()
            .listAsync(this.resourceGroupName(), this.name()), PrivateEndpointConnectionImpl::new);
    }

    @Override
    public void approvePrivateEndpointConnection(String privateEndpointConnectionName) {
        approvePrivateEndpointConnectionAsync(privateEndpointConnectionName).block();
    }

    @Override
    public Mono<Void> approvePrivateEndpointConnectionAsync(String privateEndpointConnectionName) {
        return this.manager()
            .serviceClient()
            .getPrivateEndpointConnections()
            .createOrUpdateAsync(this.resourceGroupName(), this.name(), privateEndpointConnectionName,
                new PrivateEndpointConnectionInner().withPrivateLinkServiceConnectionState(
                    new PrivateLinkServiceConnectionState().withStatus(ConnectionStatus.APPROVED)))
            .then();
    }

    @Override
    public void rejectPrivateEndpointConnection(String privateEndpointConnectionName) {
        rejectPrivateEndpointConnectionAsync(privateEndpointConnectionName).block();
    }

    @Override
    public Mono<Void> rejectPrivateEndpointConnectionAsync(String privateEndpointConnectionName) {
        return this.manager()
            .serviceClient()
            .getPrivateEndpointConnections()
            .createOrUpdateAsync(this.resourceGroupName(), this.name(), privateEndpointConnectionName,
                new PrivateEndpointConnectionInner().withPrivateLinkServiceConnectionState(
                    new PrivateLinkServiceConnectionState().withStatus(ConnectionStatus.REJECTED)))
            .then();
    }

    private void ensureNetworkRuleSet() {
        if (this.isInCreateMode()) {
            if (this.innerModel().networkRuleSet() == null) {
                this.innerModel().withNetworkRuleSet(new NetworkRuleSet());
                this.innerModel().networkRuleSet().withIpRules(new ArrayList<>());
            }
        } else {
            if (updateParameters.networkRuleSet() == null) {
                updateParameters.withNetworkRuleSet(this.innerModel().networkRuleSet());
            }
        }
    }

    @Override
    public PagedIterable<PrivateLinkResource> listPrivateLinkResources() {
        return new PagedIterable<>(this.listPrivateLinkResourcesAsync());
    }

    @Override
    public PagedFlux<PrivateLinkResource> listPrivateLinkResourcesAsync() {
        return this.manager()
            .serviceClient()
            .getRegistries()
            .listPrivateLinkResourcesAsync(this.resourceGroupName(), this.name())
            .mapPage(PrivateLinkResourceImpl::new);
    }

    @Override
    public RegistryImpl withZoneRedundancy() {
        if (isInCreateMode()) {
            this.innerModel().withZoneRedundancy(ZoneRedundancy.ENABLED);
        }
        return this;
    }

    private static final class PrivateLinkResourceImpl implements PrivateLinkResource {
        private final PrivateLinkResourceInner innerModel;

        private PrivateLinkResourceImpl(PrivateLinkResourceInner innerModel) {
            this.innerModel = innerModel;
        }

        @Override
        public String groupId() {
            return innerModel.groupId();
        }

        @Override
        public List<String> requiredMemberNames() {
            return Collections.unmodifiableList(innerModel.requiredMembers());
        }

        @Override
        public List<String> requiredDnsZoneNames() {
            return Collections.unmodifiableList(innerModel.requiredZoneNames());
        }
    }

    private static final class PrivateEndpointConnectionImpl implements PrivateEndpointConnection {
        private final PrivateEndpointConnectionInner innerModel;

        private final PrivateEndpoint privateEndpoint;
        private final com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateLinkServiceConnectionState privateLinkServiceConnectionState;
        private final PrivateEndpointConnectionProvisioningState provisioningState;

        private PrivateEndpointConnectionImpl(PrivateEndpointConnectionInner innerModel) {
            this.innerModel = innerModel;

            this.privateEndpoint
                = innerModel.privateEndpoint() == null ? null : new PrivateEndpoint(innerModel.privateEndpoint().id());
            this.privateLinkServiceConnectionState = innerModel.privateLinkServiceConnectionState() == null
                ? null
                : new com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateLinkServiceConnectionState(
                    innerModel.privateLinkServiceConnectionState().status() == null
                        ? null
                        : com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateEndpointServiceConnectionStatus
                            .fromString(innerModel.privateLinkServiceConnectionState().status().toString()),
                    innerModel.privateLinkServiceConnectionState().description(),
                    innerModel.privateLinkServiceConnectionState().actionsRequired() == null
                        ? ActionsRequired.NONE.toString()
                        : innerModel.privateLinkServiceConnectionState().actionsRequired().toString());
            this.provisioningState = innerModel.provisioningState() == null
                ? null
                : PrivateEndpointConnectionProvisioningState.fromString(innerModel.provisioningState().toString());
        }

        @Override
        public String id() {
            return innerModel.id();
        }

        @Override
        public String name() {
            return innerModel.name();
        }

        @Override
        public String type() {
            return innerModel.type();
        }

        @Override
        public PrivateEndpoint privateEndpoint() {
            return privateEndpoint;
        }

        @Override
        public com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateLinkServiceConnectionState
            privateLinkServiceConnectionState() {
            return privateLinkServiceConnectionState;
        }

        @Override
        public PrivateEndpointConnectionProvisioningState provisioningState() {
            return provisioningState;
        }
    }
}
