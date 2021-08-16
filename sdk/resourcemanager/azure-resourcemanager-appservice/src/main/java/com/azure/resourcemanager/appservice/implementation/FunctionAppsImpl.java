// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.core.util.CoreUtils;
import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.appservice.fluent.WebAppsClient;
import com.azure.resourcemanager.appservice.fluent.models.SiteConfigResourceInner;
import com.azure.resourcemanager.appservice.fluent.models.SiteInner;
import com.azure.resourcemanager.appservice.fluent.models.SiteLogsConfigInner;
import com.azure.resourcemanager.appservice.models.FunctionApp;
import com.azure.resourcemanager.appservice.models.FunctionAppBasic;
import com.azure.resourcemanager.appservice.models.FunctionApps;
import com.azure.resourcemanager.appservice.models.FunctionEnvelope;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.SupportsBatchDeletion;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.BatchDeletionImpl;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;

/** The implementation for WebApps. */
public class FunctionAppsImpl
    extends GroupableResourcesImpl<FunctionApp, FunctionAppImpl, SiteInner, WebAppsClient, AppServiceManager>
    implements FunctionApps, SupportsBatchDeletion {

    public FunctionAppsImpl(final AppServiceManager manager) {
        super(manager.serviceClient().getWebApps(), manager);
    }

    @Override
    public FunctionApp getByResourceGroup(String groupName, String name) {
        SiteInner siteInner = this.inner().getByResourceGroup(groupName, name);
        if (siteInner == null) {
            return null;
        }
        return wrapModel(
            siteInner,
            this.inner().getConfiguration(groupName, name),
            this.inner().getDiagnosticLogsConfiguration(groupName, name));
    }

    @Override
    public Mono<FunctionApp> getByResourceGroupAsync(final String resourceGroupName, final String name) {
        if (CoreUtils.isNullOrEmpty(resourceGroupName)) {
            return Mono.error(
                new IllegalArgumentException("Parameter 'resourceGroupName' is required and cannot be null."));
        }
        if (CoreUtils.isNullOrEmpty(name)) {
            return Mono.error(
                new IllegalArgumentException("Parameter 'name' is required and cannot be null."));
        }
        return this
            .getInnerAsync(resourceGroupName, name)
            .flatMap(
                siteInner ->
                    Mono
                        .zip(
                            this.inner().getConfigurationAsync(resourceGroupName, name),
                            this.inner().getDiagnosticLogsConfigurationAsync(resourceGroupName, name),
                            (SiteConfigResourceInner siteConfigResourceInner, SiteLogsConfigInner logsConfigInner) ->
                                wrapModel(siteInner, siteConfigResourceInner, logsConfigInner)));
    }

    @Override
    protected Mono<SiteInner> getInnerAsync(String resourceGroupName, String name) {
        return this.inner().getByResourceGroupAsync(resourceGroupName, name);
    }

    @Override
    public PagedIterable<FunctionEnvelope> listFunctions(String resourceGroupName, String name) {
        return PagedConverter.mapPage(this
            .manager()
            .serviceClient()
            .getWebApps()
            .listFunctions(resourceGroupName, name),
            FunctionEnvelopeImpl::new);
    }

    @Override
    protected Mono<Void> deleteInnerAsync(String resourceGroupName, String name) {
        return this.inner().deleteAsync(resourceGroupName, name);
    }

    @Override
    protected FunctionAppImpl wrapModel(String name) {
        return new FunctionAppImpl(name, new SiteInner().withKind("functionapp"), null, null, this.manager());
    }

    @Override
    protected FunctionAppImpl wrapModel(SiteInner inner) {
        if (inner == null) {
            return null;
        }
        return wrapModel(inner, null, null);
    }

    private FunctionAppImpl wrapModel(
        SiteInner inner, SiteConfigResourceInner siteConfig, SiteLogsConfigInner logConfig) {
        if (inner == null) {
            return null;
        }
        return new FunctionAppImpl(inner.name(), inner, siteConfig, logConfig, this.manager());
    }

    @Override
    public FunctionAppImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public Mono<Void> deleteByResourceGroupAsync(String resourceGroupName, String name) {
        if (CoreUtils.isNullOrEmpty(resourceGroupName)) {
            return Mono.error(
                new IllegalArgumentException("Parameter 'resourceGroupName' is required and cannot be null."));
        }
        if (CoreUtils.isNullOrEmpty(name)) {
            return Mono.error(
                new IllegalArgumentException("Parameter 'name' is required and cannot be null."));
        }
        return this.inner().deleteAsync(resourceGroupName, name);
    }

    @Override
    public Flux<String> deleteByIdsAsync(Collection<String> ids) {
        return BatchDeletionImpl.deleteByIdsAsync(ids, this::deleteInnerAsync);
    }

    @Override
    public Flux<String> deleteByIdsAsync(String... ids) {
        return this.deleteByIdsAsync(new ArrayList<>(Arrays.asList(ids)));
    }

    @Override
    public void deleteByIds(Collection<String> ids) {
        if (ids != null && !ids.isEmpty()) {
            this.deleteByIdsAsync(ids).blockLast();
        }
    }

    @Override
    public void deleteByIds(String... ids) {
        this.deleteByIds(new ArrayList<>(Arrays.asList(ids)));
    }

    @Override
    public PagedIterable<FunctionAppBasic> listByResourceGroup(String resourceGroupName) {
        return new PagedIterable<>(this.listByResourceGroupAsync(resourceGroupName));
    }

    @Override
    public PagedFlux<FunctionAppBasic> listByResourceGroupAsync(String resourceGroupName) {
        if (CoreUtils.isNullOrEmpty(resourceGroupName)) {
            return new PagedFlux<>(() -> Mono.error(
                new IllegalArgumentException("Parameter 'resourceGroupName' is required and cannot be null.")));
        }
        return PagedConverter.flatMapPage(inner().listByResourceGroupAsync(resourceGroupName),
            inner -> isFunctionApp(inner) ? Mono.just(new FunctionAppBasicImpl(inner, this.manager())) : Mono.empty());
    }

    @Override
    public PagedIterable<FunctionAppBasic> list() {
        return new PagedIterable<>(this.listAsync());
    }

    @Override
    public PagedFlux<FunctionAppBasic> listAsync() {
        return PagedConverter.flatMapPage(inner().listAsync(),
            inner -> isFunctionApp(inner) ? Mono.just(new FunctionAppBasicImpl(inner, this.manager())) : Mono.empty());
    }

    private static boolean isFunctionApp(SiteInner inner) {
        return inner.kind() != null && Arrays.asList(inner.kind().split(",")).contains("functionapp");
    }
}
