/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network.implementation;

import com.azure.management.network.PublicIPAddress;
import com.azure.management.network.PublicIPAddressDnsSettings;
import com.azure.management.network.PublicIPAddresses;
import com.azure.management.network.models.PublicIPAddressInner;
import com.azure.management.network.models.PublicIPAddressesInner;
import com.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

/**
 *  Implementation for {@link PublicIPAddresses}.
 */
class PublicIPAddressesImpl
    extends TopLevelModifiableResourcesImpl<
            PublicIPAddress,
            PublicIPAddressImpl,
            PublicIPAddressInner,
            PublicIPAddressesInner,
            NetworkManager>
    implements PublicIPAddresses {

    PublicIPAddressesImpl(final NetworkManager networkManager) {
        super(networkManager.inner().publicIPAddresses(), networkManager);
    }

    @Override
    public PublicIPAddressImpl define(String name) {
        return wrapModel(name);
    }

    // Fluent model create helpers

    @Override
    protected PublicIPAddressImpl wrapModel(String name) {
        PublicIPAddressInner inner = new PublicIPAddressInner();

        if (null == inner.dnsSettings()) {
            inner.withDnsSettings(new PublicIPAddressDnsSettings());
        }

        return new PublicIPAddressImpl(name, inner, this.manager());
    }

    @Override
    protected PublicIPAddressImpl wrapModel(PublicIPAddressInner inner) {
        if (inner == null) {
            return null;
        }
        return new PublicIPAddressImpl(inner.getId(), inner, this.manager());
    }
}