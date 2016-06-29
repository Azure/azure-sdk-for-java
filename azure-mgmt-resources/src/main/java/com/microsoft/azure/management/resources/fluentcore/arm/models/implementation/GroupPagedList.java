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
import com.microsoft.rest.RestException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

/**
 * Defines a list of resources paginated across resource groups.
 *
 * @param <E> the item type
 */
public abstract class GroupPagedList<E> extends PagedList<E> {
    private PagedList<ResourceGroup> resourceGroupList;
    private Page<ResourceGroup> currentPage;
    private Queue<ResourceGroup> queue;

    /**
     * Creates an instance from a list of resource groups.
     *
     * @param resourceGroupList the list of resource groups
     */
    public GroupPagedList(PagedList<ResourceGroup> resourceGroupList) {
        this.resourceGroupList = resourceGroupList;
        this.currentPage = resourceGroupList.currentPage();
        this.queue = new LinkedList<>(currentPage.getItems());
    }

    @Override
    public boolean hasNextPage() {
        return !queue.isEmpty() || this.currentPage.getNextPageLink() != null;
    }

    @Override
    public Page<E> nextPage(String s) throws RestException, IOException {
        if (queue.isEmpty()) {
            this.currentPage = resourceGroupList.nextPage(this.currentPage.getNextPageLink());
            queue.addAll(this.currentPage.getItems());
        }

        ResourceGroup resourceGroup = queue.poll();
        PageImpl<E> page = new PageImpl<>();
        page.setItems(listNextGroup(resourceGroup.name()));
        return page;
    }

    /**
     * Override this method to implement how to list resources in a resource group.
     *
     * @param resourceGroupName the name of the resource group
     * @return the list of resources in this group.
     * @throws RestException exception thrown from REST call
     * @throws IOException exception thrown from serialization/deserialization
     */
    public abstract List<E> listNextGroup(String resourceGroupName) throws RestException, IOException;
}
