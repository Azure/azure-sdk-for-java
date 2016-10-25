/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.microsoft.azure.management.website.WebApp;
import com.microsoft.azure.management.website.WebApps;
import rx.Observable;
import rx.functions.Func1;

/**
 * The implementation for {@link WebApps}.
 */
class WebAppsImpl
        extends GroupableResourcesImpl<
        WebApp,
        WebAppImpl,
        SiteInner,
        WebAppsInner,
        AppServiceManager>
        implements WebApps {

    WebAppsImpl(WebAppsInner innerCollection, AppServiceManager manager) {
        super(innerCollection, manager);
    }

    @Override
    public PagedList<WebApp> listByGroup(String resourceGroupName) {
        return wrapList(innerCollection.listByResourceGroup(resourceGroupName));
    }

    @Override
    public WebApp getByGroup(String groupName, String name) {
        return wrapModel(innerCollection.get(groupName, name));
    }

    @Override
    protected WebAppImpl wrapModel(String name) {
        return new WebAppImpl(name, new SiteInner(), innerCollection, super.myManager);
    }

    @Override
    protected WebAppImpl wrapModel(SiteInner inner) {
        if (inner == null) {
            return null;
        }
        return new WebAppImpl(inner.name(), inner, innerCollection, super.myManager);
    }

    @Override
    public WebAppImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public Observable<Void> deleteByGroupAsync(String groupName, String name) {
        return innerCollection.deleteAsync(groupName, name)
                .map(new Func1<Object, Void>() {
                    @Override
                    public Void call(Object o) {
                        return null;
                    }
                });
    }
}