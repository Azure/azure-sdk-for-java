package com.microsoft.azure.management.resources.fluentcore.arm.models.implementation;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.IndexableWrapperImpl;

public abstract class ChildResourceImpl<InnerT extends SubResource, ParentImplT> 
    extends IndexableWrapperImpl<InnerT>
    implements ChildResource {

    private final ParentImplT parent;

    protected ChildResourceImpl(String name, InnerT innerObject, ParentImplT parent) {
        super(name, innerObject);
        this.parent = parent;
    }

    protected ParentImplT parent() {
        return this.parent;
    }

    @Override
    public abstract String name();
}
