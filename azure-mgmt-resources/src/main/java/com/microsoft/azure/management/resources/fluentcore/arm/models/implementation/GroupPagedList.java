/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.resources.fluentcore.arm.models.implementation;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.implementation.PageImpl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * Defines a list of resources paginated across resource groups.
 *
 * @param <E> the item type
 */
public abstract class GroupPagedList<E> extends PagedList<E> {
    private Iterator<ResourceGroup> resourceGroupItr;

    /**
     * Creates an instance from a list of resource groups.
     *
     * @param resourceGroupList the list of resource groups
     */
    public GroupPagedList(PagedList<ResourceGroup> resourceGroupList) {
        this.resourceGroupItr = resourceGroupList.iterator();
        setCurrentPage(nextPage("dummy"));
    }

    @Override
    public Page<E> nextPage(String s) {
        if (resourceGroupItr.hasNext()) {
            ResourceGroup resourceGroup = resourceGroupItr.next();
            PageImpl<E> page = new PageImpl<>();
            page.setItems(listNextGroup(resourceGroup.name()));
            page.setNextPageLink(s);
            return page;
        } else {
            // return an empty page without next link so that iteration will terminate
            PageImpl<E> page = new PageImpl<>();
            page.setItems(new ArrayList<E>());
            return page;
        }
    }

    /**
     * Override this method to implement how to list resources in a resource group.
     *
     * @param resourceGroupName the name of the resource group
     * @return the list of resources in this group.
     */
    public abstract List<E> listNextGroup(String resourceGroupName);
}
