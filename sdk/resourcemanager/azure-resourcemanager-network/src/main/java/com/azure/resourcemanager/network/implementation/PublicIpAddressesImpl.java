// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.PublicIpAddressesClient;
import com.azure.resourcemanager.network.fluent.models.PublicIpAddressInner;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.network.models.PublicIpAddressDnsSettings;
import com.azure.resourcemanager.network.models.PublicIpAddresses;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;
import com.azure.resourcemanager.resources.fluentcore.model.Accepted;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.AcceptedImpl;

import java.util.function.Function;

/** Implementation for {@link PublicIpAddresses}. */
public class PublicIpAddressesImpl
    extends TopLevelModifiableResourcesImpl<
        PublicIpAddress, PublicIpAddressImpl, PublicIpAddressInner, PublicIpAddressesClient, NetworkManager>
    implements PublicIpAddresses {

    private final ClientLogger logger = new ClientLogger(this.getClass());

    public PublicIpAddressesImpl(final NetworkManager networkManager) {
        super(networkManager.serviceClient().getPublicIpAddresses(), networkManager);
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

    @Override
    public Accepted<Void> beginDeleteById(String id) {
        return beginDeleteByResourceGroup(ResourceUtils.groupFromResourceId(id), ResourceUtils.nameFromResourceId(id));
    }

    @Override
    public Accepted<Void> beginDeleteByResourceGroup(String resourceGroupName, String name) {
        return AcceptedImpl
            .newAccepted(
                logger,
                this.manager().serviceClient().getHttpPipeline(),
                this.manager().serviceClient().getDefaultPollInterval(),
                () -> this.inner().deleteWithResponseAsync(resourceGroupName, name).block(),
                Function.identity(),
                Void.class,
                null,
                Context.NONE);
    }
}
