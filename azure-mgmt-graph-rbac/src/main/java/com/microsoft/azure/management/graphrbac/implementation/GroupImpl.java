/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.graphrbac.Group;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

/**
 * Implementation for Group and its parent interfaces.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.Fluent.Graph.RBAC")
class GroupImpl
        extends WrapperImpl<ADGroupInner>
        implements Group {

    private final GraphRbacManager manager;

    GroupImpl(ADGroupInner innerModel, GraphRbacManager manager) {
        super(innerModel);
        this.manager = manager;
    }

    @Override
    public String objectId() {
        return inner().objectId();
    }

    @Override
    public String objectType() {
        return inner().objectType();
    }

    @Override
    public String displayName() {
        return inner().displayName();
    }

    @Override
    public Boolean securityEnabled() {
        return inner().securityEnabled();
    }

    @Override
    public String mail() {
        return inner().mail();
    }

    @Override
    public GraphRbacManager manager() {
        return manager();
    }
}
