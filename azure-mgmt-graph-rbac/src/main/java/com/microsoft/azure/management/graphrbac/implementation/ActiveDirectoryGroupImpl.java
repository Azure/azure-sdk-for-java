/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.graphrbac.ActiveDirectoryGroup;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;

/**
 * Implementation for Group and its parent interfaces.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.Graph.RBAC.Fluent")
class ActiveDirectoryGroupImpl
        extends WrapperImpl<ADGroupInner>
        implements ActiveDirectoryGroup {

    private final GraphRbacManager manager;

    ActiveDirectoryGroupImpl(ADGroupInner innerModel, GraphRbacManager manager) {
        super(innerModel);
        this.manager = manager;
    }

    @Override
    public String id() {
        return inner().objectId();
    }

    @Override
    public String name() {
        return inner().displayName();
    }

    @Override
    public boolean securityEnabled() {
        return Utils.toPrimitiveBoolean(inner().securityEnabled());
    }

    @Override
    public String mail() {
        return inner().mail();
    }

    @Override
    public GraphRbacManager manager() {
        return manager;
    }
}
