/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.appservice.FunctionApp;
import com.microsoft.azure.management.appservice.FunctionApps;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import rx.Completable;
import rx.Observable;

/**
 * The implementation for WebApps.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
class FunctionAppsImpl
        extends GroupableResourcesImpl<
            FunctionApp,
            FunctionAppImpl,
            SiteInner,
            WebAppsInner,
            AppServiceManager>
        implements FunctionApps {

    private final PagedListConverter<SiteInner, FunctionApp> converter;

    FunctionAppsImpl(final AppServiceManager manager) {
        super(manager.inner().webApps(), manager);
        converter = new PagedListConverter<SiteInner, FunctionApp>() {
            @Override
            public FunctionApp typeConvert(SiteInner siteInner) {
                FunctionAppImpl impl = wrapModel(siteInner, manager.inner().webApps().getConfiguration(siteInner.resourceGroup(), siteInner.name()));
                return impl.cacheSiteProperties().toBlocking().single();
            }

            @Override
            protected boolean filter(SiteInner inner) {
                return "functionapp".equals(inner.kind());
            }
        };
    }

    @Override
    public PagedList<FunctionApp> listByGroup(String resourceGroupName) {
        return wrapList(this.inner().listByResourceGroup(resourceGroupName));
    }

    @Override
    public FunctionApp getByGroup(String groupName, String name) {
        SiteInner siteInner = this.inner().getByResourceGroup(groupName, name);
        if (siteInner == null) {
            return null;
        }
        return wrapModel(siteInner, this.inner().getConfiguration(groupName, name)).cacheSiteProperties().toBlocking().single();
    }

    @Override
    protected Observable<SiteInner> getInnerAsync(String resourceGroupName, String name) {
        return this.inner().getByResourceGroupAsync(resourceGroupName, name);
    }

    @Override
    protected Completable deleteInnerAsync(String resourceGroupName, String name) {
        return this.inner().deleteAsync(resourceGroupName, name).toCompletable();
    }

    @Override
    protected FunctionAppImpl wrapModel(String name) {
        return new FunctionAppImpl(name, new SiteInner(), null, this.manager());
    }

    @Override
    protected FunctionAppImpl wrapModel(SiteInner inner) {
        return wrapModel(inner, null);
    }

    private FunctionAppImpl wrapModel(SiteInner inner, SiteConfigResourceInner configResourceInner) {
        if (inner == null) {
            return null;
        }
        return new FunctionAppImpl(inner.name(), inner, configResourceInner, this.manager());
    }

    protected PagedList<FunctionApp> wrapList(PagedList<SiteInner> pagedList) {
        return converter.convert(pagedList);
    }


    @Override
    public FunctionAppImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public Completable deleteByGroupAsync(String groupName, String name) {
        return this.inner().deleteAsync(groupName, name).toCompletable();
    }
}