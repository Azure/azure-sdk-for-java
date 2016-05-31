/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.network.Subnet;
import com.microsoft.azure.management.network.implementation.api.SubnetInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

class SubnetImpl 
    extends ChildResourceImpl<SubnetInner, NetworkImpl>
    implements Subnet {

    protected SubnetImpl(String name, SubnetInner inner, NetworkImpl network) {
        super(name, inner, network);
    }

    @Override
    public String addressPrefix() {
        return this.inner().addressPrefix();
    }

    @Override
    public String name() {
        return this.inner().name();
    }
}
