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
import com.microsoft.azure.management.website.Site;
import com.microsoft.azure.management.website.Sites;

import java.io.IOException;

/**
 * The implementation for {@link Sites}.
 */
public class SitesImpl
    extends GroupableResourcesImpl<
            Site,
            SiteImpl,
            SiteInner,
            SitesInner,
            WebsiteManager>
    implements Sites {

    protected SitesImpl(SitesInner innerCollection, WebsiteManager manager) {
        super(innerCollection, manager);
    }

    @Override
    public void delete(String groupName, String name) throws Exception {
        innerCollection.deleteSite(groupName, name);
    }

    @Override
    public PagedList<Site> listByGroup(String resourceGroupName) throws CloudException, IOException {
        return wrapList(innerCollection.getSites(resourceGroupName).getBody().value());
    }

    @Override
    public Site getByGroup(String groupName, String name) throws CloudException, IOException {
        return wrapModel(innerCollection.getSite(groupName, name).getBody());
    }

    @Override
    protected SiteImpl wrapModel(String name) {
        return new SiteImpl(name, new SiteInner(), innerCollection, super.myManager);
    }

    @Override
    protected SiteImpl wrapModel(SiteInner inner) {
        return new SiteImpl(inner.name(), inner, innerCollection, super.myManager);
    }

    @Override
    public Site.DefinitionStages.Blank define(String name) {
        return wrapModel(name);
    }

    @Override
    public void delete(String id) throws Exception {
        delete(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
    }
}
