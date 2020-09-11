// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.PublicIpPrefixesClient;
import com.azure.resourcemanager.network.fluent.inner.PublicIpPrefixInner;
import com.azure.resourcemanager.network.models.PublicIpPrefix;
import com.azure.resourcemanager.network.models.PublicIpPrefixes;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

public class PublicIpPrefixesImpl
    extends TopLevelModifiableResourcesImpl<
    PublicIpPrefix, PublicIpPrefixImpl, PublicIpPrefixInner, PublicIpPrefixesClient, NetworkManager>
    implements PublicIpPrefixes {

    public PublicIpPrefixesImpl(final NetworkManager networkManager) {
        super(networkManager.inner().getPublicIpPrefixes(), networkManager);
    }

    @Override
    public PublicIpPrefixImpl define(String name) {
        return wrapModel(name);
    }

    @Override
    protected PublicIpPrefixImpl wrapModel(PublicIpPrefixInner inner) {
        return new PublicIpPrefixImpl(inner.name(), inner, manager());
    }

    @Override
    protected PublicIpPrefixImpl wrapModel(String name) {
        return new PublicIpPrefixImpl(name, new PublicIpPrefixInner(), this.manager());
    }

}
