// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.PublicIpAddress;
import com.azure.resourcemanager.network.PublicIpAddressDnsSettings;
import com.azure.resourcemanager.network.PublicIpAddresses;
import com.azure.resourcemanager.network.models.PublicIpAddressInner;
import com.azure.resourcemanager.network.models.PublicIpAddressesInner;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

/** Implementation for {@link PublicIpAddresses}. */
class PublicIpAddressesImpl
    extends TopLevelModifiableResourcesImpl<
    PublicIpAddress, PublicIpAddressImpl, PublicIpAddressInner, PublicIpAddressesInner, NetworkManager>
    implements PublicIpAddresses {

    PublicIpAddressesImpl(final NetworkManager networkManager) {
        super(networkManager.inner().publicIpAddresses(), networkManager);
    }

    @Override
    public PublicIpAddressImpl define(String name) {
        return wrapModel(name);
    }

    // Fluent model create helpers

    @Override
    protected PublicIpAddressImpl wrapModel(String name) {
        PublicIpAddressInner inner = new PublicIpAddressInner();

        if (null == inner.dnsSettings()) {
            inner.withDnsSettings(new PublicIpAddressDnsSettings());
        }

        return new PublicIpAddressImpl(name, inner, this.manager());
    }

    @Override
    protected PublicIpAddressImpl wrapModel(PublicIpAddressInner inner) {
        if (inner == null) {
            return null;
        }
        return new PublicIpAddressImpl(inner.id(), inner, this.manager());
    }
}
