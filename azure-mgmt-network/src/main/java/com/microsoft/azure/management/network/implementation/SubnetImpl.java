/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.Subnet;
import com.microsoft.azure.management.network.implementation.api.SubnetInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

/**
 * Implementation of the Subnet interface.
 * (Internal use only)
 */
class SubnetImpl
    extends ChildResourceImpl<SubnetInner, NetworkImpl>
    implements
        Subnet,
        Subnet.Definition<Network.DefinitionCreatableWithSubnet> {

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

    // Fluent setters

    @Override
    public SubnetImpl withAddressPrefix(String cidr) {
        this.inner().withAddressPrefix(cidr);
        return this;
    }

    // Verbs

    @Override
    public NetworkImpl attach() {
        this.parent().inner().subnets().add(this.inner());
        return this.parent();
    }
}
