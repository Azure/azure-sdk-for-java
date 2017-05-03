/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.graphrbac.ActiveDirectoryGroup;
import com.microsoft.azure.management.resources.fluentcore.model.implementation.CreatableUpdatableImpl;
import rx.Observable;

/**
 * Implementation for Group and its parent interfaces.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.Fluent.Graph.RBAC")
class ActiveDirectoryGroupImpl
        extends CreatableUpdatableImpl<ActiveDirectoryGroup, ADGroupInner, ActiveDirectoryGroupImpl>
        implements
            ActiveDirectoryGroup,
            ActiveDirectoryGroup.Definition,
            ActiveDirectoryGroup.Update {
    private GraphRbacManager manager;
    private GroupCreateParametersInner createParameters;

    ActiveDirectoryGroupImpl(String userPrincipalName, ADGroupInner innerModel, final GraphRbacManager manager) {
        super(userPrincipalName, innerModel);
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
    protected Observable<ADGroupInner> getInnerAsync() {
        return null;
    }

    @Override
    public ActiveDirectoryGroupImpl withDisplayName(String displayName) {
        inner().withDisplayName(displayName);
        return this;
    }

    @Override
    public ActiveDirectoryGroupImpl withMailNickname(String mailNickname) {
        inner().withMail(mailNickname);
        return this;
    }

    @Override
    public boolean isInCreateMode() {
        return objectId() == null;
    }

    @Override
    public Observable<ActiveDirectoryGroup> createResourceAsync() {
        return null;
    }

    @Override
    public GraphRbacManager manager() {
        return manager;
    }
}
