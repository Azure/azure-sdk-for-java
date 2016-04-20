package com.microsoft.azure.management.resources.fluentcore.arm.models.implementation;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.ResourceGroup;
import com.microsoft.azure.management.resources.implementation.api.PageImpl;
import com.microsoft.rest.RestException;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public abstract class GroupPagedList<E> extends PagedList<E> {
    private Queue<ResourceGroup> resourceGroupList;

    public GroupPagedList(List<ResourceGroup> resourceGroupList) {
        this.resourceGroupList = new LinkedList<>(resourceGroupList);
    }

    @Override
    public boolean hasNextPage() {
        return !resourceGroupList.isEmpty();
    }

    @Override
    public Page<E> nextPage(String s) throws RestException, IOException {
        ResourceGroup resourceGroup = resourceGroupList.poll();
        PageImpl<E> page = new PageImpl<>();
        page.setItems(listNextGroup(resourceGroup.name()));
        return page;
    }

    public abstract List<E> listNextGroup(String resourceGroupName) throws RestException, IOException;
}
