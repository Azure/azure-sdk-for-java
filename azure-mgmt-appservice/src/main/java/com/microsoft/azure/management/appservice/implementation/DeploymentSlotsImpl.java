/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.appservice.DeploymentSlot;
import com.microsoft.azure.management.appservice.DeploymentSlots;
import com.microsoft.azure.management.appservice.WebApp;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.IndependentChildResourcesImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

/**
 * The implementation DeploymentSlots.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
class DeploymentSlotsImpl
        extends IndependentChildResourcesImpl<
                        DeploymentSlot,
                        DeploymentSlotImpl,
                        SiteInner,
                        WebAppsInner,
                        AppServiceManager,
                        WebApp>
        implements DeploymentSlots {

    private final PagedListConverter<SiteInner, DeploymentSlot> converter;
    private final WebAppImpl parent;

    DeploymentSlotsImpl(final WebAppImpl parent) {
        super(parent.manager().inner().webApps(), parent.manager());

        this.parent = parent;
        final WebAppsInner innerCollection = this.inner();
        converter = new PagedListConverter<SiteInner, DeploymentSlot>() {
            @Override
            public DeploymentSlot typeConvert(SiteInner siteInner) {
                return wrapModelWithConfigChange(siteInner, innerCollection, parent);
            }
        };
    }

    private DeploymentSlot wrapModelWithConfigChange(SiteInner siteInner, WebAppsInner innerCollection, WebAppImpl parent) {
        return wrapModel(siteInner, innerCollection.getConfigurationSlot(siteInner.resourceGroup(), parent.name(), siteInner.name().replaceAll(".*/", ""))).cacheSiteProperties().toBlocking().single();
    }

    @Override
    protected DeploymentSlotImpl wrapModel(String name) {
        return new DeploymentSlotImpl(name, new SiteInner(), null, parent)
                .withRegion(parent.regionName())
                .withExistingResourceGroup(parent.resourceGroupName());
    }

    @Override
    protected DeploymentSlotImpl wrapModel(SiteInner inner) {
        return wrapModel(inner, null);
    }

    protected PagedList<DeploymentSlot> wrapList(PagedList<SiteInner> pagedList) {
        return converter.convert(pagedList);
    }

    @Override
    public DeploymentSlotImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public Observable<DeploymentSlot> getByParentAsync(final String resourceGroup, final String parentName, final String name) {
        return innerCollection.getSlotAsync(resourceGroup, parentName, name).flatMap(new Func1<SiteInner, Observable<DeploymentSlot>>() {
            @Override
            public Observable<DeploymentSlot> call(final SiteInner siteInner) {
                if (siteInner == null) {
                    return null;
                }
                return innerCollection.getConfigurationSlotAsync(resourceGroup, parentName, name)
                        .flatMap(new Func1<SiteConfigResourceInner, Observable<DeploymentSlot>>() {
                    @Override
                    public Observable<DeploymentSlot> call(SiteConfigResourceInner siteConfigInner) {
                        return wrapModel(siteInner, siteConfigInner).cacheSiteProperties();
                    }
                });
            }
        });
    }

    @Override
    public PagedList<DeploymentSlot> listByParent(String resourceGroupName, String parentName) {
        return wrapList(innerCollection.listSlots(resourceGroupName, parentName));
    }

    @Override
    public Completable deleteByParentAsync(String groupName, String parentName, String name) {
        return innerCollection.deleteSlotAsync(groupName, parentName, name).toCompletable();
    }

    @Override
    public void deleteByName(String name) {
        deleteByParent(parent.resourceGroupName(), parent.name(), name);
    }

    @Override
    public ServiceFuture<Void> deleteByNameAsync(String name, ServiceCallback<Void> callback) {
        return deleteByParentAsync(parent.resourceGroupName(), parent.name(), name, callback);
    }

    @Override
    public Completable deleteByNameAsync(String name) {
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

    @Override
    public WebApp parent() {
        return this.parent;
    }

    @Override
    public Observable<DeploymentSlot> listAsync() {
        return convertPageToInnerAsync(innerCollection.listSlotsAsync(parent.resourceGroupName(), parent.name())).map(new Func1<SiteInner, DeploymentSlot>() {
            @Override
            public DeploymentSlot call(SiteInner siteInner) {
                return wrapModelWithConfigChange(siteInner, innerCollection, parent);
            }
        });
    }

    private DeploymentSlotImpl wrapModel(SiteInner inner, SiteConfigResourceInner configResourceInner) {
        if (inner == null) {
            return null;
        }
        return new DeploymentSlotImpl(inner.name(), inner, configResourceInner, parent);
    }
}