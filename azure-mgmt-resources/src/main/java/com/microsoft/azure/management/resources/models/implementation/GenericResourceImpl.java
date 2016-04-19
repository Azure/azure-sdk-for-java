package com.microsoft.azure.management.resources.models.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.implementation.ResourceGroupsImpl;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.resources.models.GenericResource;
import com.microsoft.azure.management.resources.models.implementation.api.GenericResourceInner;
import com.microsoft.azure.management.resources.models.implementation.api.Plan;

public class GenericResourceImpl
    extends GroupableResourceImpl<GenericResource, GenericResourceInner, GenericResourceImpl>
    implements GenericResource {
    public GenericResourceImpl(String id, GenericResourceInner innerObject, ResourceManagementClientImpl serviceClient) {
        super(id, innerObject, new ResourceGroupsImpl(serviceClient));
    }

    @Override
    public Plan plan() throws Exception {
        return inner().plan();
    }

    @Override
    public Object properties() throws Exception {
        return inner().properties();
    }

    @Override
    public GenericResource refresh() throws Exception {
        return null;
    }
}
