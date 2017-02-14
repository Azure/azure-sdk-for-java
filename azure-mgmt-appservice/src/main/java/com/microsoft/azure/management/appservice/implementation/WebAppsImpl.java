/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.appservice.implementation;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ListableGroupableResourcesPageImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.PagedListConverter;
import com.microsoft.azure.management.appservice.WebApp;
import com.microsoft.azure.management.appservice.WebApps;
import rx.Completable;
import rx.Observable;

/**
 * The implementation for WebApps.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.AppService.Fluent")
class WebAppsImpl
        extends ListableGroupableResourcesPageImpl<
            WebApp,
            WebAppImpl,
            SiteInner,
            WebAppsInner,
            AppServiceManager>
        implements WebApps {

    private final PagedListConverter<SiteInner, WebApp> converter;

    WebAppsImpl(final AppServiceManager manager) {
        super(manager.inner().webApps(), manager);
        converter = new PagedListConverter<SiteInner, WebApp>() {
            @Override
            public WebApp typeConvert(SiteInner siteInner) {
                siteInner.withSiteConfig(manager.inner().webApps().getConfiguration(siteInner.resourceGroup(), siteInner.name()));
                WebAppImpl impl = wrapModel(siteInner);
                return impl.cacheAppSettingsAndConnectionStrings().toBlocking().single();
            }
        };
    }

    @Override
    public PagedList<WebApp> listByGroup(String resourceGroupName) {
        return wrapList(this.inner().listByResourceGroup(resourceGroupName));
    }

    @Override
    public WebApp getByGroup(String groupName, String name) {
        SiteInner siteInner = this.inner().get(groupName, name);
        if (siteInner == null) {
            return null;
        }
        siteInner.withSiteConfig(this.inner().getConfiguration(groupName, name));
        return wrapModel(siteInner).cacheAppSettingsAndConnectionStrings().toBlocking().single();
    }

    @Override
    protected WebAppImpl wrapModel(String name) {
        return new WebAppImpl(name, new SiteInner(), null, this.manager());
    }

    @Override
    protected WebAppImpl wrapModel(SiteInner inner) {
        if (inner == null) {
            return null;
        }
        SiteConfigInner configInner = inner.siteConfig();
        return new WebAppImpl(inner.name(), inner, configInner, this.manager());
    }

    protected PagedList<WebApp> wrapList(PagedList<SiteInner> pagedList) {
        return converter.convert(pagedList);
    }


    @Override
    public WebAppImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public Completable deleteByGroupAsync(String groupName, String name) {
        return this.inner().deleteAsync(groupName, name).toCompletable();
    }

    @Override
    public PagedList<WebApp> list() {
        return wrapList(inner().list());
    }

    @Override
    protected Observable<Page<SiteInner>> listAsyncPage() {
        return inner().listAsync();
    }

    @Override
    protected Observable<Page<SiteInner>> listByGroupAsyncPage(String resourceGroupName) {
        return inner().listByResourceGroupAsync(resourceGroupName);
    }
}