/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.graphrbac.ActiveDirectoryGroup;
import com.microsoft.azure.management.graphrbac.Groups;
import com.microsoft.azure.management.graphrbac.User;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Observable;
import rx.functions.Func1;

/**
 * The implementation of Users and its parent interfaces.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.Fluent.Graph.RBAC")
class GroupsImpl
        extends ReadableWrappersImpl<
            ActiveDirectoryGroup,
            ActiveDirectoryGroupImpl,
            ADGroupInner>
        implements
            Groups,
            HasManager<GraphRbacManager>,
            HasInner<GroupsInner> {
    private GroupsInner innerCollection;
    private GraphRbacManager manager;

    GroupsImpl(
            final GroupsInner client,
            final GraphRbacManager graphRbacManager) {
        this.innerCollection = client;
        this.manager = graphRbacManager;
    }

    @Override
    public PagedList<ActiveDirectoryGroup> list() {
        return wrapList(this.innerCollection.list());
    }

    @Override
    protected ActiveDirectoryGroupImpl wrapModel(ADGroupInner groupInner) {
        if (groupInner == null) {
            return null;
        }
        return new ActiveDirectoryGroupImpl(groupInner.displayName(), groupInner, manager);
    }

    @Override
    public ActiveDirectoryGroupImpl getByObjectId(String objectId) {
        return new ActiveDirectoryGroupImpl(innerCollection.get(objectId), innerCollection);
    }


    @Override
    public GraphRbacManager manager() {
        return this.manager;
    }

    @Override
    public GroupsInner inner() {
        return this.innerCollection;
    }

    @Override
    public Observable<ActiveDirectoryGroup> listAsync() {
        return wrapPageAsync(this.inner().listAsync());
    }
}
