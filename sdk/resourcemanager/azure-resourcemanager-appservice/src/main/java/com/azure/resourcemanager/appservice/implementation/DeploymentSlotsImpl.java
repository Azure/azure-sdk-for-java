// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.appservice.fluent.WebAppsClient;
import com.azure.resourcemanager.appservice.fluent.inner.SiteConfigResourceInner;
import com.azure.resourcemanager.appservice.fluent.inner.SiteInner;
import com.azure.resourcemanager.appservice.fluent.inner.SiteLogsConfigInner;
import com.azure.resourcemanager.appservice.models.DeploymentSlot;
import com.azure.resourcemanager.appservice.models.DeploymentSlots;
import com.azure.resourcemanager.appservice.models.WebApp;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.IndependentChildResourcesImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;
import reactor.core.publisher.Mono;

/** The implementation DeploymentSlots. */
class DeploymentSlotsImpl
    extends IndependentChildResourcesImpl<
        DeploymentSlot, DeploymentSlotImpl, SiteInner, WebAppsClient, AppServiceManager, WebApp>
    implements DeploymentSlots {

    private final WebAppImpl parent;

    DeploymentSlotsImpl(final WebAppImpl parent) {
        super(parent.manager().inner().getWebApps(), parent.manager());

        this.parent = parent;
    }

    @Override
    protected DeploymentSlotImpl wrapModel(String name) {
        return new DeploymentSlotImpl(name, new SiteInner(), null, null, parent)
            .withRegion(parent.regionName())
            .withExistingResourceGroup(parent.resourceGroupName());
    }

    @Override
    protected DeploymentSlotImpl wrapModel(SiteInner inner) {
        return wrapModel(inner, null, null);
    }

    @Override
    protected PagedFlux<DeploymentSlot> wrapPageAsync(PagedFlux<SiteInner> innerPage) {
        return PagedConverter
            .flatMapPage(
                innerPage,
                siteInner ->
                    Mono
                        .zip(
                            this
                                .inner()
                                .getConfigurationSlotAsync(
                                    siteInner.resourceGroup(), parent.name(), siteInner.name()),
                            this
                                .inner()
                                .getDiagnosticLogsConfigurationSlotAsync(
                                    siteInner.resourceGroup(), parent.name(), siteInner.name()),
                            (siteConfigResourceInner, logsConfigInner) ->
                                this.wrapModel(siteInner, siteConfigResourceInner, logsConfigInner)));
    }

    @Override
    public DeploymentSlotImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public Mono<DeploymentSlot> getByParentAsync(
        final String resourceGroup, final String parentName, final String name) {
        return innerCollection
            .getSlotAsync(resourceGroup, parentName, name)
            .flatMap(
                siteInner ->
                    Mono
                        .zip(
                            innerCollection.getConfigurationSlotAsync(resourceGroup, parentName, name),
                            innerCollection.getDiagnosticLogsConfigurationSlotAsync(resourceGroup, parentName, name),
                            (SiteConfigResourceInner siteConfigResourceInner, SiteLogsConfigInner logsConfigInner) ->
                                wrapModel(siteInner, siteConfigResourceInner, logsConfigInner)));
    }

    @Override
    public PagedIterable<DeploymentSlot> listByParent(String resourceGroupName, String parentName) {
        return new PagedIterable<>(wrapPageAsync(innerCollection.listSlotsAsync(resourceGroupName, parentName)));
    }

    @Override
    public Mono<Void> deleteByParentAsync(String groupName, String parentName, String name) {
        return innerCollection.deleteSlotAsync(groupName, parentName, name);
    }

    @Override
    public void deleteByName(String name) {
        deleteByParent(parent.resourceGroupName(), parent.name(), name);
    }

    @Override
    public Mono<Void> deleteByNameAsync(String name) {
        return deleteByParentAsync(parent.resourceGroupName(), parent.name(), name);
    }

    @Override
    public PagedIterable<DeploymentSlot> list() {
        return listByParent(parent.resourceGroupName(), parent.name());
    }

    @Override
    public DeploymentSlot getByName(String name) {
        return getByParent(parent.resourceGroupName(), parent.name(), name);
    }

    @Override
    public Mono<DeploymentSlot> getByNameAsync(String name) {
        return getByParentAsync(parent.resourceGroupName(), parent.name(), name);
    }

    @Override
    public WebApp parent() {
        return this.parent;
    }

    @Override
    public PagedFlux<DeploymentSlot> listAsync() {
        return wrapPageAsync(innerCollection.listSlotsAsync(parent.resourceGroupName(), parent.name()));
    }

    private DeploymentSlotImpl wrapModel(
        SiteInner inner, SiteConfigResourceInner siteConfig, SiteLogsConfigInner logConfig) {
        if (inner == null) {
            return null;
        }
        return new DeploymentSlotImpl(inner.name(), inner, siteConfig, logConfig, parent);
    }
}
