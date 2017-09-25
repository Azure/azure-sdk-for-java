/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.appservice.FunctionApp;
import com.microsoft.azure.management.appservice.FunctionDeploymentSlots;
import com.microsoft.azure.management.appservice.FunctionDeploymentSlot;
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
class FunctionDeploymentSlotsImpl
        extends IndependentChildResourcesImpl<
        FunctionDeploymentSlot,
        FunctionDeploymentSlotImpl,
        SiteInner,
        WebAppsInner,
        AppServiceManager,
        FunctionApp>
        implements FunctionDeploymentSlots {

    private final PagedListConverter<SiteInner, FunctionDeploymentSlot> converter;
    private final FunctionAppImpl parent;

    FunctionDeploymentSlotsImpl(final FunctionAppImpl parent) {
        super(parent.manager().inner().webApps(), parent.manager());

        this.parent = parent;
        final WebAppsInner innerCollection = this.inner();
        converter = new PagedListConverter<SiteInner, FunctionDeploymentSlot>() {
            @Override
            public FunctionDeploymentSlot typeConvert(SiteInner siteInner) {
                return wrapModelWithConfigChange(siteInner, innerCollection, parent);
            }
        };
    }

    private FunctionDeploymentSlot wrapModelWithConfigChange(SiteInner siteInner, WebAppsInner innerCollection, FunctionAppImpl parent) {
        return wrapModel(siteInner, innerCollection.getConfigurationSlot(siteInner.resourceGroup(), parent.name(), siteInner.name().replaceAll(".*/", ""))).cacheSiteProperties().toBlocking().single();
    }

    @Override
    protected FunctionDeploymentSlotImpl wrapModel(String name) {
        return new FunctionDeploymentSlotImpl(name, new SiteInner(), null, parent)
                .withRegion(parent.regionName())
                .withExistingResourceGroup(parent.resourceGroupName());
    }

    @Override
    protected FunctionDeploymentSlotImpl wrapModel(SiteInner inner) {
        return wrapModel(inner, null);
    }

    protected PagedList<FunctionDeploymentSlot> wrapList(PagedList<SiteInner> pagedList) {
        return converter.convert(pagedList);
    }

    @Override
    public FunctionDeploymentSlotImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public Observable<FunctionDeploymentSlot> getByParentAsync(final String resourceGroup, final String parentName, final String name) {
        return innerCollection.getSlotAsync(resourceGroup, parentName, name).flatMap(new Func1<SiteInner, Observable<FunctionDeploymentSlot>>() {
            @Override
            public Observable<FunctionDeploymentSlot> call(final SiteInner siteInner) {
                if (siteInner == null) {
                    return null;
                }
                return innerCollection.getConfigurationSlotAsync(resourceGroup, parentName, name)
                        .flatMap(new Func1<SiteConfigResourceInner, Observable<FunctionDeploymentSlot>>() {
                            @Override
                            public Observable<FunctionDeploymentSlot> call(SiteConfigResourceInner siteConfigInner) {
                                return wrapModel(siteInner, siteConfigInner).cacheSiteProperties();
                            }
                        });
            }
        });
    }

    @Override
    public PagedList<FunctionDeploymentSlot> listByParent(String resourceGroupName, String parentName) {
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
    public PagedList<FunctionDeploymentSlot> list() {
        return listByParent(parent.resourceGroupName(), parent.name());
    }

    @Override
    public FunctionDeploymentSlot getByName(String name) {
        return getByParent(parent.resourceGroupName(), parent.name(), name);
    }

    @Override
    public FunctionApp parent() {
        return this.parent;
    }

    @Override
    public Observable<FunctionDeploymentSlot> listAsync() {
        return convertPageToInnerAsync(innerCollection.listSlotsAsync(parent.resourceGroupName(), parent.name())).map(new Func1<SiteInner, FunctionDeploymentSlot>() {
            @Override
            public FunctionDeploymentSlot call(SiteInner siteInner) {
                return wrapModelWithConfigChange(siteInner, innerCollection, parent);
            }
        });
    }

    private FunctionDeploymentSlotImpl wrapModel(SiteInner inner, SiteConfigResourceInner configResourceInner) {
        if (inner == null) {
            return null;
        }
        return new FunctionDeploymentSlotImpl(inner.name(), inner, configResourceInner, parent);
    }
}