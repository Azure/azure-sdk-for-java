/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.implementation;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.graphrbac.Group;
import com.microsoft.azure.management.graphrbac.Groups;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;
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
            Group,
            GroupImpl,
            ADGroupInner>
        implements
            Groups {
    private final GraphRbacManager manager;

    GroupsImpl(final GraphRbacManager manager) {
        this.manager = manager;
    }

    @Override
    public PagedList<Group> list() {
        return wrapList(this.manager.inner().groups().list());
    }

    @Override
    protected GroupImpl wrapModel(ADGroupInner groupInner) {
        if (groupInner == null) {
            return null;
        }
        return new GroupImpl(groupInner, manager());
    }

    @Override
    public GroupImpl getById(String objectId) {
        return (GroupImpl) getByIdAsync(objectId).toBlocking().single();
    }

    @Override
    public Observable<Group> getByIdAsync(String id) {
        return manager.inner().groups().getAsync(id)
                .map(new Func1<ADGroupInner, Group>() {
                    @Override
                    public Group call(ADGroupInner groupInner) {
                        if (groupInner == null) {
                            return null;
                        } else {
                            return new GroupImpl(groupInner, manager());
                        }
                    }
                });
    }

    @Override
    public ServiceFuture<Group> getByIdAsync(String id, ServiceCallback<Group> callback) {
        return ServiceFuture.fromBody(getByIdAsync(id), callback);
    }

    @Override
    public Observable<Group> listAsync() {
        return wrapPageAsync(manager().inner().groups().listAsync());
    }

    @Override
    public Observable<Group> getByNameAsync(String name) {
        return manager().inner().groups().listAsync(String.format("displayName eq '%s'", name))
                .map(new Func1<Page<ADGroupInner>, Group>() {
                    @Override
                    public Group call(Page<ADGroupInner> adGroupInnerPage) {
                        if (adGroupInnerPage.items() == null || adGroupInnerPage.items().isEmpty()) {
                            return null;
                        } else {
                            return new GroupImpl(adGroupInnerPage.items().get(0), manager());
                        }
                    }
                });
    }

    @Override
    public Group getByName(String name) {
        return getByNameAsync(name).toBlocking().single();
    }

    @Override
    public GraphRbacManager manager() {
        return manager;
    }

    @Override
    public GroupsInner inner() {
        return manager().inner().groups();
    }
}
