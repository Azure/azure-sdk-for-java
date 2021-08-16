// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.appservice.implementation;

import com.azure.core.http.rest.PagedFlux;
import com.azure.core.http.rest.PagedIterable;
import com.azure.resourcemanager.appservice.AppServiceManager;
import com.azure.resourcemanager.appservice.fluent.WebAppsClient;
import com.azure.resourcemanager.appservice.fluent.models.SiteConfigResourceInner;
import com.azure.resourcemanager.appservice.fluent.models.SiteInner;
import com.azure.resourcemanager.appservice.fluent.models.SiteLogsConfigInner;
import com.azure.resourcemanager.appservice.models.FunctionApp;
import com.azure.resourcemanager.appservice.models.FunctionDeploymentSlot;
import com.azure.resourcemanager.appservice.models.FunctionDeploymentSlotBasic;
import com.azure.resourcemanager.appservice.models.FunctionDeploymentSlots;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.IndependentChildResourcesImpl;
import reactor.core.publisher.Mono;
import com.azure.resourcemanager.resources.fluentcore.utils.PagedConverter;

/** The implementation DeploymentSlots. */
class FunctionDeploymentSlotsImpl
    extends IndependentChildResourcesImpl<
        FunctionDeploymentSlot, FunctionDeploymentSlotImpl, SiteInner, WebAppsClient, AppServiceManager, FunctionApp>
    implements FunctionDeploymentSlots {

    private final FunctionAppImpl parent;

    FunctionDeploymentSlotsImpl(final FunctionAppImpl parent) {
        super(parent.manager().serviceClient().getWebApps(), parent.manager());

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
    public FunctionDeploymentSlotImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public Mono<FunctionDeploymentSlot> getByParentAsync(
        final String resourceGroup, final String parentName, final String name) {
        return innerCollection
            .getSlotAsync(resourceGroup, parentName, name)
            .flatMap(
                siteInner ->
                    Mono
                        .zip(
                            innerCollection.getConfigurationSlotAsync(resourceGroup, parentName,
                                name.replaceAll(".*/", "")),
                            innerCollection.getDiagnosticLogsConfigurationSlotAsync(resourceGroup, parentName,
                                name.replaceAll(".*/", "")),
                            (SiteConfigResourceInner siteConfigResourceInner, SiteLogsConfigInner logsConfigInner) ->
                                wrapModel(siteInner, siteConfigResourceInner, logsConfigInner)));
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
    public PagedIterable<FunctionDeploymentSlotBasic> list() {
        return new PagedIterable<>(this.listAsync());
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
    public PagedFlux<FunctionDeploymentSlotBasic> listAsync() {
        return PagedConverter.mapPage(innerCollection.listSlotsAsync(parent.resourceGroupName(), parent.name()),
            inner -> new FunctionDeploymentSlotBasicImpl(inner, parent));
    }

    private FunctionDeploymentSlotImpl wrapModel(
        SiteInner inner, SiteConfigResourceInner siteConfig, SiteLogsConfigInner logConfig) {
        if (inner == null) {
            return null;
        }
        return new FunctionDeploymentSlotImpl(inner.name(), inner, siteConfig, logConfig, parent);
    }
}
