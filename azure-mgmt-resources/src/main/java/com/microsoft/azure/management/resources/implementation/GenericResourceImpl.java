package com.microsoft.azure.management.resources.implementation;

import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.microsoft.azure.management.resources.implementation.api.ResourceManagementClientImpl;
import com.microsoft.azure.management.resources.GenericResource;
import com.microsoft.azure.management.resources.implementation.api.GenericResourceInner;
import com.microsoft.azure.management.resources.implementation.api.Plan;

public class GenericResourceImpl
    extends GroupableResourceImpl<GenericResource, GenericResourceInner, GenericResourceImpl>
    implements GenericResource {
    public GenericResourceImpl(String id, GenericResourceInner innerModel, final ResourceManagementClientImpl serviceClient) {
        super(id, innerModel, new ResourceGroupsImpl(serviceClient));
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

    @Override
    protected void createResource() throws Exception {
        throw new Exception("GenericResource is not creatable");
    }
}
