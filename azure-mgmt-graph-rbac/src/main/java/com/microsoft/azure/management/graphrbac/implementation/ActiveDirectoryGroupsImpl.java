/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.graphrbac.implementation;

import com.microsoft.azure.Page;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.graphrbac.ActiveDirectoryGroup;
import com.microsoft.azure.management.graphrbac.ActiveDirectoryGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.CreatableWrappersImpl;
import com.microsoft.rest.ServiceCallback;
import com.microsoft.rest.ServiceFuture;
import rx.Completable;
import rx.Observable;
import rx.functions.Func1;

/**
 * The implementation of Users and its parent interfaces.
 */
@LangDefinition(ContainerName = "/Microsoft.Azure.Management.Graph.RBAC.Fluent")
class ActiveDirectoryGroupsImpl
        extends CreatableWrappersImpl<
                ActiveDirectoryGroup,
                ActiveDirectoryGroupImpl,
                ADGroupInner>
        implements
        ActiveDirectoryGroups {
    private final GraphRbacManager manager;

    ActiveDirectoryGroupsImpl(final GraphRbacManager manager) {
        this.manager = manager;
    }

    @Override
    public PagedList<ActiveDirectoryGroup> list() {
        return wrapList(this.manager.inner().groups().list());
    }

    @Override
    protected ActiveDirectoryGroupImpl wrapModel(ADGroupInner groupInner) {
        if (groupInner == null) {
            return null;
        }
        return new ActiveDirectoryGroupImpl(groupInner, manager());
    }

    @Override
    public ActiveDirectoryGroupImpl getById(String objectId) {
        return (ActiveDirectoryGroupImpl) getByIdAsync(objectId).toBlocking().single();
    }

    @Override
    public Observable<ActiveDirectoryGroup> getByIdAsync(String id) {
        return manager.inner().groups().getAsync(id)
                .map(new Func1<ADGroupInner, ActiveDirectoryGroup>() {
                    @Override
                    public ActiveDirectoryGroup call(ADGroupInner groupInner) {
                        if (groupInner == null) {
                            return null;
                        } else {
                            return new ActiveDirectoryGroupImpl(groupInner, manager());
                        }
                    }
                });
    }

    @Override
    public ServiceFuture<ActiveDirectoryGroup> getByIdAsync(String id, ServiceCallback<ActiveDirectoryGroup> callback) {
        return ServiceFuture.fromBody(getByIdAsync(id), callback);
    }

    @Override
    public Observable<ActiveDirectoryGroup> listAsync() {
        return wrapPageAsync(manager().inner().groups().listAsync());
    }

    @Override
    public Observable<ActiveDirectoryGroup> getByNameAsync(String name) {
        return manager().inner().groups().listAsync(String.format("displayName eq '%s'", name))
                .map(new Func1<Page<ADGroupInner>, ActiveDirectoryGroup>() {
                    @Override
                    public ActiveDirectoryGroup call(Page<ADGroupInner> adGroupInnerPage) {
                        if (adGroupInnerPage.items() == null || adGroupInnerPage.items().isEmpty()) {
                            return null;
                        } else {
                            return new ActiveDirectoryGroupImpl(adGroupInnerPage.items().get(0), manager());
                        }
                    }
                });
    }

    @Override
    public ActiveDirectoryGroup getByName(String name) {
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

    @Override
    public ActiveDirectoryGroupImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    protected ActiveDirectoryGroupImpl wrapModel(String name) {
        return wrapModel(new ADGroupInner().withDisplayName(name));
    }

    @Override
    public Completable deleteByIdAsync(String id) {
        return manager().inner().groups().deleteAsync(id).toCompletable();
    }
}
