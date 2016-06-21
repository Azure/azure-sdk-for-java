/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.CloudException;
import com.microsoft.azure.PagedList;
import com.microsoft.azure.management.network.Subnet;
import com.microsoft.azure.management.network.Subnets;
import com.microsoft.azure.management.network.implementation.api.SubnetInner;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.ReadableWrappersImpl;

import java.io.IOException;

/**
 * The implementation for {@link Subnets}.
 */
class SubnetsImpl
        extends ReadableWrappersImpl<Subnet, SubnetImpl, SubnetInner>
        implements Subnets {

    private final NetworkImpl parentNetwork;

    SubnetsImpl(NetworkImpl parentNetwork) {
        this.parentNetwork = parentNetwork;
    }

    @Override
    public PagedList<Subnet> list() throws CloudException, IOException {
        return wrapList(parentNetwork.inner().subnets());
    }

    @Override
    protected SubnetImpl wrapModel(SubnetInner inner) {
        return new SubnetImpl(inner.name(), inner, this.parentNetwork);
    }
}
