/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.appservice.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.management.appservice.FunctionApp;
import com.azure.management.appservice.FunctionDeploymentSlot;
import com.azure.management.appservice.FunctionDeploymentSlots;
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
class FunctionDeploymentSlotsImpl
        extends IndependentChildResourcesImpl<
        FunctionDeploymentSlot,
        FunctionDeploymentSlotImpl,
        SiteInner,
        WebAppsInner,
        AppServiceManager,
        FunctionApp>
        implements FunctionDeploymentSlots {

    private final FunctionAppImpl parent;

    FunctionDeploymentSlotsImpl(final FunctionAppImpl parent) {
        super(parent.manager().inner().webApps(), parent.manager());

        this.parent = parent;
    }

    @Override
    protected FunctionDeploymentSlotImpl wrapModel(String name) {
        return new FunctionDeploymentSlotImpl(name, new SiteInner(), null, null, parent)
                .withRegion(parent.regionName())
                .withExistingResourceGroup(parent.resourceGroupName());
    }

    @Override
    protected FunctionDeploymentSlotImpl wrapModel(SiteInner inner) {
        if (inner == null) {
            return null;
        }
        return wrapModel(inner, null, null);
    }

    @Override
    protected PagedFlux<FunctionDeploymentSlot> wrapPageAsync(PagedFlux<SiteInner> innerPage) {
        return PagedConverter.flatMapPage(innerPage, siteInner -> Mono.zip(
                this.inner().getConfigurationSlotAsync(siteInner.resourceGroup(), parent.name(), siteInner.getName()),
                this.inner().getDiagnosticLogsConfigurationSlotAsync(siteInner.resourceGroup(), parent.name(), siteInner.getName()),
                (siteConfigResourceInner, logsConfigInner) -> this.wrapModel(siteInner, siteConfigResourceInner, logsConfigInner)));
    }

    @Override
    public FunctionDeploymentSlotImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public Mono<FunctionDeploymentSlot> getByParentAsync(final String resourceGroup, final String parentName, final String name) {
        return innerCollection.getSlotAsync(resourceGroup, parentName, name).flatMap(siteInner -> Mono.zip(
                innerCollection.getConfigurationSlotAsync(resourceGroup, parentName, name),
                innerCollection.getDiagnosticLogsConfigurationSlotAsync(resourceGroup, parentName, name),
                (SiteConfigResourceInner siteConfigResourceInner, SiteLogsConfigInner logsConfigInner) -> wrapModel(siteInner, siteConfigResourceInner, logsConfigInner)));
    }

    @Override
    public PagedIterable<FunctionDeploymentSlot> listByParent(String resourceGroupName, String parentName) {
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
    public PagedIterable<FunctionDeploymentSlot> list() {
        return listByParent(parent.resourceGroupName(), parent.name());
    }

    @Override
    public FunctionDeploymentSlot getByName(String name) {
        return getByParent(parent.resourceGroupName(), parent.name(), name);
    }

    @Override
    public Mono<FunctionDeploymentSlot> getByNameAsync(String name) {
        return getByParentAsync(parent.resourceGroupName(), parent.name(), name);
    }

    @Override
    public FunctionApp parent() {
        return this.parent;
    }

    @Override
    public PagedFlux<FunctionDeploymentSlot> listAsync() {
        return wrapPageAsync(innerCollection.listSlotsAsync(parent.resourceGroupName(), parent.name()));
    }

    private FunctionDeploymentSlotImpl wrapModel(SiteInner inner, SiteConfigResourceInner siteConfig, SiteLogsConfigInner logConfig) {
        if (inner == null) {
            return null;
        }
        return new FunctionDeploymentSlotImpl(inner.getName(), inner, siteConfig, logConfig, parent);
    }
}