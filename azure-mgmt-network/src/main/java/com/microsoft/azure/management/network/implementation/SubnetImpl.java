/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import java.io.IOException;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.network.Subnet;
import com.microsoft.azure.management.network.implementation.api.NetworkSecurityGroupInner;
import com.microsoft.azure.management.network.implementation.api.SubnetInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

/**
 *  Implementation for {@link Subnet} and its create and update interfaces.
 */
class SubnetImpl
    extends ChildResourceImpl<SubnetInner, NetworkImpl>
    implements
        Subnet,
        Subnet.Definition<Network.DefinitionStages.WithCreateAndSubnet>,
        Subnet.Update {

    protected SubnetImpl(String name, SubnetInner inner, NetworkImpl parent) {
        super(name, inner, parent);
    }

    // Getters
    @Override
    public String addressPrefix() {
        return this.inner().addressPrefix();
    }

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public NetworkSecurityGroup networkSecurityGroup() throws CloudException, IllegalArgumentException, IOException {
        NetworkSecurityGroupInner nsgInner = this.inner().networkSecurityGroup();
        if (nsgInner == null) {
            return null;
        } else {
            return this.parent().myManager().networkSecurityGroups().getById(nsgInner.id());
        }
    }

    // Fluent setters

    @Override
    public SubnetImpl withAddressPrefix(String cidr) {
        this.inner().withAddressPrefix(cidr);
        return this;
    }

    // Verbs

    @Override
    public NetworkImpl attach() {
        return this.parent().withSubnet(this);
    }

    @Override
    public NetworkImpl set() {
        return this.parent();
    }
}
