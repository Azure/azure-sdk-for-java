/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.IndependentChildResourcesImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.website.DeploymentSlot;
import com.microsoft.azure.management.website.DeploymentSlots;
import com.microsoft.rest.ServiceCall;
import com.microsoft.rest.ServiceCallback;
import rx.Observable;
import rx.functions.Func1;

/**
 * The implementation for {@link DeploymentSlots}.
 */
class DeploymentSlotsImpl
        extends IndependentChildResourcesImpl<
                        DeploymentSlot,
                        DeploymentSlotImpl,
                        SiteInner,
                        WebAppsInner,
                        AppServiceManager>
        implements DeploymentSlots {

    private final PagedListConverter<SiteInner, DeploymentSlot> converter;
    private final WebAppImpl parent;

    DeploymentSlotsImpl(final WebAppImpl parent, final WebAppsInner innerCollection, AppServiceManager manager) {
        super(innerCollection, manager);

        this.parent = parent;
        converter = new PagedListConverter<SiteInner, DeploymentSlot>() {
            @Override
            public DeploymentSlot typeConvert(SiteInner siteInner) {
                siteInner.withSiteConfig(innerCollection.getConfiguration(siteInner.resourceGroup(), siteInner.name()));
                return wrapModel(siteInner);
            }
        };
    }

    @Override
    protected DeploymentSlotImpl wrapModel(String name) {
        return new DeploymentSlotImpl(name, new SiteInner(), new SiteConfigInner(), parent, innerCollection, super.manager)
                .withRegion(parent.regionName())
                .withExistingResourceGroup(parent.resourceGroupName());
    }

    @Override
    protected DeploymentSlotImpl wrapModel(SiteInner inner) {
        if (inner == null) {
            return null;
        }
        SiteConfigInner configInner = inner.siteConfig();
        if (configInner == null) {
            configInner = new SiteConfigInner();
            configInner.withLocation(inner.location());
        }
        return new DeploymentSlotImpl(inner.name(), inner, configInner, parent, innerCollection, super.manager);
    }

    protected PagedList<DeploymentSlot> wrapList(PagedList<SiteInner> pagedList) {
        return converter.convert(pagedList);
    }

    @Override
    public DeploymentSlotImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public DeploymentSlot getByParent(String resourceGroup, String parentName, String name) {
        SiteInner siteInner = innerCollection.getSlot(resourceGroup, parentName, name);
        if (siteInner == null) {
            return null;
        }
        siteInner.withSiteConfig(innerCollection.getConfigurationSlot(resourceGroup, parentName, name));
        return wrapModel(siteInner);
    }

    @Override
    public PagedList<DeploymentSlot> listByParent(String resourceGroupName, String parentName) {
        return wrapList(innerCollection.listSlots(resourceGroupName, parentName));
    }

    @Override
    public Observable<Void> deleteByParentAsync(String groupName, String parentName, String name) {
        return innerCollection.deleteSlotAsync(groupName, parentName, name)
                .flatMap(new Func1<Object, Observable<Void>>() {
                    @Override
                    public Observable<Void> call(Object o) {
                        return null;
                    }
                });
    }

    @Override
    public void deleteByName(String name) {
        deleteByParent(parent.resourceGroupName(), parent.name(), name);
    }

    @Override
    public ServiceCall<Void> deleteByNameAsync(String name, ServiceCallback<Void> callback) {
        return deleteByParentAsync(parent.resourceGroupName(), parent.name(), name, callback);
    }

    @Override
    public Observable<Void> deleteByNameAsync(String name) {
        return deleteByParentAsync(parent.resourceGroupName(), parent.name(), name);
    }

    @Override
    public PagedList<DeploymentSlot> list() {
        return listByParent(parent.resourceGroupName(), parent.name());
    }

    @Override
    public DeploymentSlot getByName(String name) {
        return getByParent(parent.resourceGroupName(), parent.name(), name);
    }
}