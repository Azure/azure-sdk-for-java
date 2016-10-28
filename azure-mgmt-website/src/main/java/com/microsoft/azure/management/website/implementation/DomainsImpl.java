/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.website.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import com.microsoft.azure.management.website.Domain;
import com.microsoft.azure.management.website.Domains;
import rx.Observable;
import rx.functions.Func1;

/**
 * The implementation for {@link Domains}.
 */
class DomainsImpl
        extends GroupableResourcesImpl<
        Domain,
        DomainImpl,
        DomainInner,
        DomainsInner,
        AppServiceManager>
        implements Domains {
    TopLevelDomainsInner topLevelDomainsInner;

    DomainsImpl(DomainsInner innerCollection, TopLevelDomainsInner topLevelDomainsInner, AppServiceManager manager) {
        super(innerCollection, manager);
        this.topLevelDomainsInner = topLevelDomainsInner;
    }

    @Override
    public DomainImpl getByGroup(String groupName, String name) {
        return wrapModel(innerCollection.get(groupName, name));
    }

    @Override
    public Observable<Void> deleteByGroupAsync(String groupName, String name) {
        return innerCollection.deleteAsync(groupName, name)
                .map(new Func1<Object, Void>() {
                    @Override
                    public Void call(Object o) {
                        return null;
                    }
                });
    }

    @Override
    protected DomainImpl wrapModel(String name) {
        return new DomainImpl(name, new DomainInner(), innerCollection, topLevelDomainsInner, myManager);
    }

    @Override
    protected DomainImpl wrapModel(DomainInner inner) {
        if (inner == null) {
            return null;
        }
        return new DomainImpl(inner.name(), inner, innerCollection, topLevelDomainsInner, myManager);
    }

    @Override
    public DomainImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    public PagedList<Domain> list() {
        return wrapList(innerCollection.list());
    }

    @Override
    public PagedList<Domain> listByGroup(String resourceGroupName) {
        return wrapList(innerCollection.listByResourceGroup(resourceGroupName));
    }
}