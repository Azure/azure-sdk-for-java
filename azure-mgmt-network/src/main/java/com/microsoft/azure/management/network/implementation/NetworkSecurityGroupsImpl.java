/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.network.NetworkSecurityGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.GroupableResourcesImpl;
import rx.Observable;

/**
 *  Implementation for {@link NetworkSecurityGroups}.
 */
@LangDefinition
class NetworkSecurityGroupsImpl
        extends GroupableResourcesImpl<
            NetworkSecurityGroup,
            NetworkSecurityGroupImpl,
            NetworkSecurityGroupInner,
            NetworkSecurityGroupsInner,
            NetworkManager>
        implements NetworkSecurityGroups {

    NetworkSecurityGroupsImpl(
            final NetworkSecurityGroupsInner innerCollection,
            final NetworkManager networkManager) {
        super(innerCollection, networkManager);
    }

    @Override
    public PagedList<NetworkSecurityGroup> list() {
        return wrapList(this.innerCollection.listAll());
    }

    @Override
    public PagedList<NetworkSecurityGroup> listByGroup(String groupName) {
        return wrapList(this.innerCollection.list(groupName));
    }

    @Override
    public NetworkSecurityGroupImpl getByGroup(String groupName, String name) {
        return wrapModel(this.innerCollection.get(groupName, name));
    }

    @Override
    public Observable<Void> deleteByGroupAsync(String groupName, String name) {
        return this.innerCollection.deleteAsync(groupName, name);
    }

    @Override
    public NetworkSecurityGroupImpl define(String name) {
        return wrapModel(name);
    }

    // Fluent model create helpers

    @Override
    protected NetworkSecurityGroupImpl wrapModel(String name) {
        NetworkSecurityGroupInner inner = new NetworkSecurityGroupInner();
        return new NetworkSecurityGroupImpl(
                name,
                inner,
                this.innerCollection,
                super.myManager);
    }

    @Override
    protected NetworkSecurityGroupImpl wrapModel(NetworkSecurityGroupInner inner) {
        if (inner == null) {
            return null;
        }
        return new NetworkSecurityGroupImpl(
                inner.name(),
                inner,
                this.innerCollection,
                this.myManager);
    }
}
