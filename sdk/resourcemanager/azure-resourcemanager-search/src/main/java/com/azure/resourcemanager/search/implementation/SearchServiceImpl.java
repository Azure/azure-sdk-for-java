// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.search.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableParentResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.search.SearchServiceManager;
import com.azure.resourcemanager.search.fluent.models.SearchServiceInner;
import com.azure.resourcemanager.search.models.AdminKeyKind;
import com.azure.resourcemanager.search.models.AdminKeys;
import com.azure.resourcemanager.search.models.HostingMode;
import com.azure.resourcemanager.search.models.ProvisioningState;
import com.azure.resourcemanager.search.models.QueryKey;
import com.azure.resourcemanager.search.models.SearchService;
import com.azure.resourcemanager.search.models.SearchServiceStatus;
import com.azure.resourcemanager.search.models.Sku;
import com.azure.resourcemanager.search.models.SkuName;
import reactor.core.publisher.Mono;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

/**
 * Implementation for Search service and its create and update interfaces.
 */
class SearchServiceImpl
    extends GroupableParentResourceImpl<
        SearchService,
        SearchServiceInner,
        SearchServiceImpl,
        SearchServiceManager>
    implements
        SearchService,
        SearchService.Definition,
        SearchService.Update {

    SearchServiceImpl(String name, final SearchServiceInner innerModel, final SearchServiceManager searchManager) {
        super(name, innerModel, searchManager);
    }

    @Override
    protected Mono<SearchServiceInner> createInner() {
        return this.manager().serviceClient().getServices()
            .createOrUpdateAsync(this.resourceGroupName(), this.name(), this.innerModel())
            // TODO: remove this after azure-core-management upgrade to 1.0.1
            .switchIfEmpty(this.manager().serviceClient().getServices()
                .getByResourceGroupAsync(this.resourceGroupName(), this.name()));
    }

    @Override
    protected void initializeChildrenFromInner() {
    }

    @Override
    protected Mono<SearchServiceInner> getInnerAsync() {
        return this.manager().serviceClient().getServices()
            .getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public HostingMode hostingMode() {
        return this.innerModel().hostingMode();
    }

    @Override
    public int partitionCount() {
        return ResourceManagerUtils.toPrimitiveInt(this.innerModel().partitionCount());
    }

    @Override
    public ProvisioningState provisioningState() {
        return this.innerModel().provisioningState();
    }

    @Override
    public int replicaCount() {
        return ResourceManagerUtils.toPrimitiveInt(this.innerModel().replicaCount());
    }

    @Override
    public Sku sku() {
        return this.innerModel().sku();
    }

    @Override
    public SearchServiceStatus status() {
        return this.innerModel().status();
    }

    @Override
    public String statusDetails() {
        return this.innerModel().statusDetails();
    }

    @Override
    public AdminKeys getAdminKeys() {
        return getAdminKeysAsync().block();
    }

    @Override
    public Mono<AdminKeys> getAdminKeysAsync() {
        return this.manager().serviceClient().getAdminKeys().getAsync(this.resourceGroupName(), this.name())
            .map(AdminKeysImpl::new);
    }

    @Override
    public PagedIterable<QueryKey> listQueryKeys() {
        return new PagedIterable<>(listQueryKeysAsync());
    }

    @Override
    public PagedFlux<QueryKey> listQueryKeysAsync() {
        return PagedConverter.mapPage(this.manager().serviceClient().getQueryKeys()
            .listBySearchServiceAsync(this.resourceGroupName(), this.name()),
            QueryKeyImpl::new);
    }

    @Override
    public AdminKeys regenerateAdminKeys(AdminKeyKind keyKind) {
        return regenerateAdminKeysAsync(keyKind).block();
    }

    @Override
    public Mono<AdminKeys> regenerateAdminKeysAsync(AdminKeyKind keyKind) {
        return this.manager().serviceClient().getAdminKeys()
            .regenerateAsync(this.resourceGroupName(), this.name(), keyKind)
            .map(AdminKeysImpl::new);
    }

    @Override
    public QueryKey createQueryKey(String name) {
        return createQueryKeyAsync(name).block();
    }

    @Override
    public Mono<QueryKey> createQueryKeyAsync(String name) {
        return this.manager().serviceClient().getQueryKeys()
            .createAsync(this.resourceGroupName(), this.name(), name)
            .map(QueryKeyImpl::new);
    }

    @Override
    public void deleteQueryKey(String key) {
        deleteQueryKeyAsync(key).block();
    }

    @Override
    public Mono<Void> deleteQueryKeyAsync(String key) {
        return this.manager().serviceClient().getQueryKeys()
            .deleteAsync(this.resourceGroupName(), this.name(), key);
    }

    @Override
    public SearchServiceImpl withSku(SkuName skuName) {
        this.innerModel().withSku(new Sku().withName(skuName));
        return this;
    }

    @Override
    public SearchServiceImpl withFreeSku() {
        this.innerModel().withSku(new Sku().withName(SkuName.FREE));
        return this;
    }

    @Override
    public SearchServiceImpl withBasicSku() {
        this.innerModel().withSku(new Sku().withName(SkuName.BASIC));
        return this;
    }

    @Override
    public SearchServiceImpl withStandardSku() {
        this.innerModel().withSku(new Sku().withName(SkuName.STANDARD));
        return this;
    }

    @Override
    public SearchServiceImpl withReplicaCount(int count) {
        this.innerModel().withReplicaCount(count);
        return this;
    }

    @Override
    public SearchServiceImpl withPartitionCount(int count) {
        this.innerModel().withPartitionCount(count);
        return this;
    }
}
