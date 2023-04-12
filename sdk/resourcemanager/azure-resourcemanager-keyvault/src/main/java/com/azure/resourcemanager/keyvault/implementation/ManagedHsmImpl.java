// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.keyvault.implementation;

import com.azure.core.http.HttpPipeline;
import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.http.rest.Response;
import com.azure.core.http.rest.SimpleResponse;
import com.azure.resourcemanager.keyvault.KeyVaultManager;
import com.azure.resourcemanager.keyvault.fluent.models.ManagedHsmInner;
import com.azure.resourcemanager.keyvault.fluent.models.PrivateEndpointConnectionInner;
import com.azure.resourcemanager.keyvault.models.Keys;
import com.azure.resourcemanager.keyvault.models.ManagedHsm;
import com.azure.resourcemanager.keyvault.models.ManagedHsmSku;
import com.azure.resourcemanager.keyvault.models.MhsmNetworkRuleSet;
import com.azure.resourcemanager.keyvault.models.PrivateEndpointServiceConnectionStatus;
import com.azure.resourcemanager.keyvault.models.PrivateLinkServiceConnectionState;
import com.azure.resourcemanager.keyvault.models.PublicNetworkAccess;
import com.azure.resourcemanager.resources.fluentcore.arm.models.PrivateLinkResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.security.keyvault.keys.KeyAsyncClient;
import com.azure.security.keyvault.keys.KeyClientBuilder;
import com.azure.security.keyvault.keys.KeyServiceVersion;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation for Managed Hardware Security Module and its parent interfaces.
 */
class ManagedHsmImpl
    extends GroupableResourceImpl<ManagedHsm, ManagedHsmInner, ManagedHsmImpl, KeyVaultManager>
    implements ManagedHsm {
    private KeyAsyncClient keyClient;
    private final HttpPipeline mhsmHttpPipeline;

    ManagedHsmImpl(String name, ManagedHsmInner innerObject, KeyVaultManager manager) {
        super(name, innerObject, manager);
        mhsmHttpPipeline = manager().httpPipeline();
        if (innerModel().properties() != null && innerModel().properties().hsmUri() != null) {
            keyClient = new KeyClientBuilder()
                .pipeline(mhsmHttpPipeline)
                .vaultUrl(innerModel().properties().hsmUri())
                .serviceVersion(KeyServiceVersion.V7_2)
                .buildAsyncClient();
        }
    }

    /**
     * We haven't supported creating {@link ManagedHsm} in convenience layer yet. This is for implementing necessary abstract class contract.
     * @return {@link Mono} of the created {@link ManagedHsm} instance
     */
    @Override
    public Mono<ManagedHsm> createResourceAsync() {
        throw new UnsupportedOperationException("method [createResourceAsync] not implemented in class [com.azure.resourcemanager.keyvault.implementation.ManagedHsmImpl]");
    }

    @Override
    protected Mono<ManagedHsmInner> getInnerAsync() {
        return manager().serviceClient().getManagedHsms().getByResourceGroupAsync(resourceGroupName(), name());
    }

    @Override
    public String tenantId() {
        if (innerModel().properties() == null || innerModel().properties().tenantId() == null) {
            return null;
        }
        return innerModel().properties().tenantId().toString();
    }

    @Override
    public ManagedHsmSku sku() {
        return innerModel().sku();
    }

    @Override
    public List<String> initialAdminObjectIds() {
        if (innerModel().properties() == null || innerModel().properties().initialAdminObjectIds() == null) {
            return Collections.emptyList();
        }
        return Collections.unmodifiableList(innerModel().properties().initialAdminObjectIds());
    }

    @Override
    public String hsmUri() {
        if (innerModel().properties() == null) {
            return null;
        }
        return innerModel().properties().hsmUri();
    }

    @Override
    public boolean isSoftDeleteEnabled() {
        if (innerModel().properties() == null) {
            return false;
        }
        return ResourceManagerUtils.toPrimitiveBoolean(innerModel().properties().enableSoftDelete());
    }

    @Override
    public Integer softDeleteRetentionInDays() {
        if (innerModel().properties() == null) {
            return null;
        }
        return innerModel().properties().softDeleteRetentionInDays();
    }

    @Override
    public boolean isPurgeProtectionEnabled() {
        if (innerModel().properties() == null) {
            return false;
        }
        return ResourceManagerUtils.toPrimitiveBoolean(innerModel().properties().enablePurgeProtection());
    }

    @Override
    public MhsmNetworkRuleSet networkRuleSet() {
        if (innerModel().properties() == null) {
            return null;
        }
        return innerModel().properties().networkAcls();
    }

    @Override
    public Keys keys() {
        return new KeysImpl(keyClient, mhsmHttpPipeline);
    }

    @Override
    public OffsetDateTime scheduledPurgeDate() {
        if (innerModel().properties() == null) {
            return null;
        }
        return innerModel().properties().scheduledPurgeDate();
    }

    @Override
    public PublicNetworkAccess publicNetworkAccess() {
        if (innerModel().properties() == null) {
            return null;
        }
        return innerModel().properties().publicNetworkAccess();
    }

    @Override
    public PagedIterable<PrivateLinkResource> listPrivateLinkResources() {
        return new PagedIterable<>(listPrivateLinkResourcesAsync());
    }

    @Override
    public PagedFlux<PrivateLinkResource> listPrivateLinkResourcesAsync() {
        Mono<Response<List<PrivateLinkResource>>> retList = this.manager().serviceClient().getPrivateLinkResources()
            .listByVaultWithResponseAsync(this.resourceGroupName(), this.name())
            .map(response -> new SimpleResponse<>(response, response.getValue().value().stream()
                .map(ManagedHsmImpl.PrivateLinkResourceImpl::new)
                .collect(Collectors.toList())));
        return PagedConverter.convertListToPagedFlux(retList);
    }

    @Override
    public void approvePrivateEndpointConnection(String privateEndpointConnectionName) {
        approvePrivateEndpointConnectionAsync(privateEndpointConnectionName).block();
    }

    @Override
    public Mono<Void> approvePrivateEndpointConnectionAsync(String privateEndpointConnectionName) {
        return manager().serviceClient().getPrivateEndpointConnections().putAsync(
            this.resourceGroupName(), this.name(), privateEndpointConnectionName,
            new PrivateEndpointConnectionInner().withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionState().withStatus(PrivateEndpointServiceConnectionStatus.APPROVED)))
            .then();
    }

    @Override
    public void rejectPrivateEndpointConnection(String privateEndpointConnectionName) {
        rejectPrivateEndpointConnectionAsync(privateEndpointConnectionName).block();
    }

    @Override
    public Mono<Void> rejectPrivateEndpointConnectionAsync(String privateEndpointConnectionName) {
        return manager().serviceClient().getPrivateEndpointConnections().putAsync(
            this.resourceGroupName(), this.name(), privateEndpointConnectionName,
            new PrivateEndpointConnectionInner().withPrivateLinkServiceConnectionState(
                new PrivateLinkServiceConnectionState().withStatus(PrivateEndpointServiceConnectionStatus.REJECTED)))
            .then();
    }

    private static final class PrivateLinkResourceImpl implements PrivateLinkResource {
        private final com.azure.resourcemanager.keyvault.models.PrivateLinkResource innerModel;

        private PrivateLinkResourceImpl(com.azure.resourcemanager.keyvault.models.PrivateLinkResource innerModel) {
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
}
