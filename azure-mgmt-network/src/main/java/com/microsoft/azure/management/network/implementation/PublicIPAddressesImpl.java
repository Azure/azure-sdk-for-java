/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.PublicIPAddress;
import com.microsoft.azure.management.network.PublicIPAddressDnsSettings;
import com.microsoft.azure.management.network.PublicIPAddresses;
import com.microsoft.azure.management.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

/**
 *  Implementation for {@link PublicIPAddresses}.
 */
@LangDefinition
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
        return new PublicIPAddressImpl(inner.id(), inner, this.manager());
    }
}