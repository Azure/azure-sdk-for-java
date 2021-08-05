// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.search.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.CoreUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.azure.resourcemanager.search.SearchServiceManager;
import com.azure.resourcemanager.search.fluent.SearchManagementClient;
import com.azure.resourcemanager.search.fluent.models.SearchServiceInner;
import com.azure.resourcemanager.search.models.AdminKeyKind;
import com.azure.resourcemanager.search.models.AdminKeys;
import com.azure.resourcemanager.search.models.CheckNameAvailabilityOutput;
import com.azure.resourcemanager.search.models.QueryKey;
import com.azure.resourcemanager.search.models.SearchService;
import com.azure.resourcemanager.search.models.SearchServices;
import reactor.core.publisher.Mono;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

/**
 * Implementation for SearchServices.
 */
public class SearchServicesImpl
    extends GroupableResourcesImpl<
        SearchService,
        SearchServiceImpl,
        SearchServiceInner,
        SearchManagementClient,
        SearchServiceManager>
    implements SearchServices {

    public SearchServicesImpl(final SearchServiceManager searchManager) {
        super(searchManager.serviceClient(), searchManager);
    }

    @Override
    protected Mono<SearchServiceInner> getInnerAsync(String resourceGroupName, String name) {
        return this.inner().getServices().getByResourceGroupAsync(resourceGroupName, name);
    }

    @Override
    protected Mono<Void> deleteInnerAsync(String resourceGroupName, String name) {
        return this.inner().getServices().deleteAsync(resourceGroupName, name);
    }

    @Override
    protected SearchServiceImpl wrapModel(String name) {
        SearchServiceInner inner = new SearchServiceInner();
        return new SearchServiceImpl(name, inner, this.manager());
    }

    @Override
    protected SearchServiceImpl wrapModel(SearchServiceInner inner) {
        if (inner == null) {
            return null;
        }
        return new SearchServiceImpl(inner.name(), inner, this.manager());
    }

    @Override
    public CheckNameAvailabilityOutput checkNameAvailability(String name) {
        return checkNameAvailabilityAsync(name).block();
    }

    @Override
    public Mono<CheckNameAvailabilityOutput> checkNameAvailabilityAsync(String name) {
        return this.inner().getServices().checkNameAvailabilityAsync(name);
    }

    @Override
    public AdminKeys getAdminKeys(String resourceGroupName, String searchServiceName) {
        return getAdminKeysAsync(resourceGroupName, searchServiceName).block();
    }

    @Override
    public Mono<AdminKeys> getAdminKeysAsync(String resourceGroupName, String searchServiceName) {
        return this.inner().getAdminKeys().getAsync(resourceGroupName, searchServiceName)
            .map(AdminKeysImpl::new);
    }

    @Override
    public PagedIterable<QueryKey> listQueryKeys(String resourceGroupName, String searchServiceName) {
        return new PagedIterable<>(listQueryKeysAsync(resourceGroupName, searchServiceName));
    }

    @Override
    public PagedFlux<QueryKey> listQueryKeysAsync(String resourceGroupName, String searchServiceName) {
        return PagedConverter.mapPage(this.inner().getQueryKeys().listBySearchServiceAsync(resourceGroupName, searchServiceName),
            QueryKeyImpl::new);
    }

    @Override
    public AdminKeys regenerateAdminKeys(String resourceGroupName, String searchServiceName, AdminKeyKind keyKind) {
        return regenerateAdminKeysAsync(resourceGroupName, searchServiceName, keyKind).block();
    }

    @Override
    public Mono<AdminKeys> regenerateAdminKeysAsync(String resourceGroupName,
                                                    String searchServiceName,
                                                    AdminKeyKind keyKind) {
        return this.inner().getAdminKeys().regenerateAsync(resourceGroupName, searchServiceName, keyKind)
            .map(AdminKeysImpl::new);
    }

    @Override
    public QueryKey createQueryKey(String resourceGroupName, String searchServiceName, String name) {
        return createQueryKeyAsync(resourceGroupName, searchServiceName, name).block();
    }

    @Override
    public Mono<QueryKey> createQueryKeyAsync(String resourceGroupName, String searchServiceName, String name) {
        return this.inner().getQueryKeys().createAsync(resourceGroupName, searchServiceName, name)
            .map(QueryKeyImpl::new);
    }

    @Override
    public void deleteQueryKey(String resourceGroupName, String searchServiceName, String key) {
        deleteQueryKeyAsync(resourceGroupName, searchServiceName, key).block();
    }

    @Override
    public Mono<Void> deleteQueryKeyAsync(String resourceGroupName, String searchServiceName, String key) {
        return this.inner().getQueryKeys().deleteAsync(resourceGroupName, searchServiceName, key);
    }

    @Override
    public PagedIterable<SearchService> listByResourceGroup(String resourceGroupName) {
        return new PagedIterable<>(listByResourceGroupAsync(resourceGroupName));
    }

    @Override
    public PagedFlux<SearchService> listByResourceGroupAsync(String resourceGroupName) {
        if (CoreUtils.isNullOrEmpty(resourceGroupName)) {
            return new PagedFlux<>(() -> Mono.error(
                new IllegalArgumentException("Parameter 'resourceGroupName' is required and cannot be null.")));
        }
        return PagedConverter.mapPage(this.inner().getServices().listByResourceGroupAsync(resourceGroupName),
            this::wrapModel);
    }

    @Override
    public SearchServiceImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public PagedIterable<SearchService> list() {
        return new PagedIterable<>(listAsync());
    }

    @Override
    public PagedFlux<SearchService> listAsync() {
        return PagedConverter.mapPage(this.inner().getServices().listAsync(),
            this::wrapModel);
    }
}
