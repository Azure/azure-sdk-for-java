/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.graphrbac.ActiveDirectoryUser;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.WrapperImpl;

/**
 * Implementation for User and its parent interfaces.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.Graph.RBAC.Fluent")
class ActiveDirectoryUserImpl
        extends WrapperImpl<UserInner>
        implements
        ActiveDirectoryUser {

    private final GraphRbacManager manager;

    ActiveDirectoryUserImpl(UserInner innerObject, GraphRbacManager manager) {
        super(innerObject);
        this.manager = manager;
    }

    @Override
    public String id() {
        return inner().objectId();
    }

    @Override
    public String userPrincipalName() {
        return inner().userPrincipalName();
    }

    @Override
    public String name() {
        return inner().displayName();
    }

    @Override
    public String signInName() {
        return inner().signInName();
    }

    @Override
    public String mail() {
        return inner().mail();
    }

    @Override
    public String mailNickname() {
        return inner().mailNickname();
    }

    @Override
    public GraphRbacManager manager() {
        return manager;
    }
}
