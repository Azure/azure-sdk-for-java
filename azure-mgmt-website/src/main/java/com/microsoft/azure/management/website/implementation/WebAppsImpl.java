/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.microsoft.azure.management.website.WebApp;
import com.microsoft.azure.management.website.WebApps;

import java.io.IOException;

/**
 * The implementation for {@link WebApps}.
 */
class WebAppsImpl
    extends GroupableResourcesImpl<
        WebApp,
        WebAppImpl,
            SiteInner,
            SitesInner,
            WebsiteManager>
    implements WebApps {

    WebAppsImpl(SitesInner innerCollection, WebsiteManager manager) {
        super(innerCollection, manager);
    }

    @Override
    public void delete(String groupName, String name) throws Exception {
        innerCollection.deleteSite(groupName, name);
    }

    @Override
    public PagedList<WebApp> listByGroup(String resourceGroupName) throws CloudException, IOException {
        return wrapList(innerCollection.getSites(resourceGroupName).getBody().value());
    }

    @Override
    public WebApp getByGroup(String groupName, String name) throws CloudException, IOException {
        return wrapModel(innerCollection.getSite(groupName, name).getBody());
    }

    @Override
    protected WebAppImpl wrapModel(String name) {
        return new WebAppImpl(name, new SiteInner(), innerCollection, super.myManager);
    }

    @Override
    protected WebAppImpl wrapModel(SiteInner inner) {
        return new WebAppImpl(inner.name(), inner, innerCollection, super.myManager);
    }

    @Override
    public WebApp.DefinitionStages.Blank define(String name) {
        return wrapModel(name);
    }

    @Override
    public void delete(String id) throws Exception {
        delete(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
    }
}
