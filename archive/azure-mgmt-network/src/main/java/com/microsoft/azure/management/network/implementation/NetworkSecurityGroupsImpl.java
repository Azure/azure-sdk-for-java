/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.NetworkInterface;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.network.NetworkSecurityGroups;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import rx.Completable;

import java.util.Set;

/**
 *  Implementation for NetworkSecurityGroups.
 */
@LangDefinition
class NetworkSecurityGroupsImpl
    extends TopLevelModifiableResourcesImpl<
        NetworkSecurityGroup,
        NetworkSecurityGroupImpl,
        NetworkSecurityGroupInner,
        NetworkSecurityGroupsInner,
        NetworkManager>
    implements NetworkSecurityGroups {

    NetworkSecurityGroupsImpl(final NetworkManager networkManager) {
        super(networkManager.inner().networkSecurityGroups(), networkManager);
    }

    @Override
    public Completable deleteByResourceGroupAsync(String groupName, String name) {
        // Clear NIC references if any
        NetworkSecurityGroupImpl nsg = (NetworkSecurityGroupImpl) getByResourceGroup(groupName, name);
        if (nsg != null) {
            Set<String> nicIds = nsg.networkInterfaceIds();
            if (nicIds != null) {
                for (String nicRef : nsg.networkInterfaceIds()) {
                    NetworkInterface nic = this.manager().networkInterfaces().getById(nicRef);
                    if (nic == null) {
                        continue;
                    } else if (!nsg.id().equalsIgnoreCase(nic.networkSecurityGroupId())) {
                        continue;
                    } else {
                        nic.update().withoutNetworkSecurityGroup().apply();
                    }
                }
            }
        }

        return this.deleteInnerAsync(groupName, name);
    }

    @Override
    public NetworkSecurityGroupImpl define(String name) {
        return wrapModel(name);
    }

    // Fluent model create helpers

    @Override
    protected NetworkSecurityGroupImpl wrapModel(String name) {
        NetworkSecurityGroupInner inner = new NetworkSecurityGroupInner();
        return new NetworkSecurityGroupImpl(name, inner, this.manager());
    }

    @Override
    protected NetworkSecurityGroupImpl wrapModel(NetworkSecurityGroupInner inner) {
        if (inner == null) {
            return null;
        }
        return new NetworkSecurityGroupImpl(inner.name(), inner, this.manager());
    }
}
