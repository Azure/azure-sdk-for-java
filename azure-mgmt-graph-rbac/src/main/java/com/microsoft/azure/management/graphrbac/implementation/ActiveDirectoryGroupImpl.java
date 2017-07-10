/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.graphrbac.ActiveDirectoryGroup;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import rx.Observable;

/**
 * Implementation for Group and its parent interfaces.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.Graph.RBAC.Fluent")
class ActiveDirectoryGroupImpl
        extends CreatableUpdatableImpl<ActiveDirectoryGroup, ADGroupInner, ActiveDirectoryGroupImpl>
        implements ActiveDirectoryGroup,
            ActiveDirectoryGroup.Definition {

    private final GraphRbacManager manager;
    private GroupCreateParametersInner createParameters;

    ActiveDirectoryGroupImpl(ADGroupInner innerModel, GraphRbacManager manager) {
        super(innerModel.displayName(), innerModel);
        this.manager = manager;
        this.createParameters = new GroupCreateParametersInner()
                .withDisplayName(innerModel.displayName())
                .withMailEnabled(false)
                .withSecurityEnabled(true);
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

    @Override
    protected Observable<ADGroupInner> getInnerAsync() {
        return manager().inner().groups().getAsync(id());
    }

    @Override
    public boolean isInCreateMode() {
        return id() == null;
    }

    @Override
    public Observable<ActiveDirectoryGroup> createResourceAsync() {
        return null;
    }

    @Override
    public ActiveDirectoryGroupImpl withEmailAlias(String mailNickname) {
        createParameters.withMailNickname(mailNickname);
        return this;
    }
}
