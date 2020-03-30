/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.appservice.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.appservice.DeploymentSlot;
import com.azure.management.appservice.DeploymentSlots;
import com.azure.management.appservice.WebApp;
import com.azure.management.appservice.models.SiteConfigResourceInner;
import com.azure.management.appservice.models.SiteInner;
import com.azure.management.appservice.models.SiteLogsConfigInner;
import com.azure.management.appservice.models.WebAppsInner;
import com.azure.management.resources.fluentcore.arm.collection.implementation.IndependentChildResourcesImpl;
import com.azure.management.resources.fluentcore.utils.PagedConverter;
import reactor.core.publisher.Mono;

/**
 * The implementation DeploymentSlots.
 */
class DeploymentSlotsImpl
        extends IndependentChildResourcesImpl<
        DeploymentSlot,
        DeploymentSlotImpl,
        SiteInner,
        WebAppsInner,
        AppServiceManager,
        WebApp>
        implements DeploymentSlots {

    private final WebAppImpl parent;

    DeploymentSlotsImpl(final WebAppImpl parent) {
        super(parent.manager().inner().webApps(), parent.manager());

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
        return PagedConverter.flatMapPage(innerPage, siteInner -> Mono.zip(
                this.inner().getConfigurationSlotAsync(siteInner.resourceGroup(), parent.name(), siteInner.getName()),
                this.inner().getDiagnosticLogsConfigurationSlotAsync(siteInner.resourceGroup(), parent.name(), siteInner.getName()),
                (siteConfigResourceInner, logsConfigInner) -> this.wrapModel(siteInner, siteConfigResourceInner, logsConfigInner)));
    }

    @Override
    public DeploymentSlotImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public Mono<DeploymentSlot> getByParentAsync(final String resourceGroup, final String parentName, final String name) {
        return innerCollection.getSlotAsync(resourceGroup, parentName, name).flatMap(siteInner -> Mono.zip(
                innerCollection.getConfigurationSlotAsync(resourceGroup, parentName, name),
                innerCollection.getDiagnosticLogsConfigurationSlotAsync(resourceGroup, parentName, name),
                (SiteConfigResourceInner siteConfigResourceInner, SiteLogsConfigInner logsConfigInner) -> wrapModel(siteInner, siteConfigResourceInner, logsConfigInner)));
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

    private DeploymentSlotImpl wrapModel(SiteInner inner, SiteConfigResourceInner siteConfig, SiteLogsConfigInner logConfig) {
        if (inner == null) {
            return null;
        }
        return new DeploymentSlotImpl(inner.getName(), inner, siteConfig, logConfig, parent);
    }
}