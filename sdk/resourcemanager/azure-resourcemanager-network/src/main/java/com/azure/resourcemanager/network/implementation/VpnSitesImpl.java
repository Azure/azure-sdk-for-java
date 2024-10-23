// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.implementation;

import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.VpnSitesClient;
import com.azure.resourcemanager.network.fluent.models.VpnSiteInner;
import com.azure.resourcemanager.network.models.VpnSite;
import com.azure.resourcemanager.network.models.VpnSites;
import com.azure.resourcemanager.resources.fluentcore.arm.collection.implementation.TopLevelModifiableResourcesImpl;

/** The implementation of VPN site parent interfaces. */
public class VpnSitesImpl
    extends TopLevelModifiableResourcesImpl<VpnSite, VpnSiteImpl, VpnSiteInner, VpnSitesClient, NetworkManager>
    implements VpnSites {

    public VpnSitesImpl(final NetworkManager networkManager) {
        super(networkManager.serviceClient().getVpnSites(), networkManager);
    }

    @Override
    public VpnSiteImpl define(String name) {
        return wrapModel(name);
    }

    // Fluent model create helpers

    @Override
    protected VpnSiteImpl wrapModel(String name) {
        VpnSiteInner inner = new VpnSiteInner();

        return new VpnSiteImpl(name, inner, super.manager());
    }

    @Override
    protected VpnSiteImpl wrapModel(VpnSiteInner inner) {
        if (inner == null) {
            return null;
        }
        return new VpnSiteImpl(inner.name(), inner, this.manager());
    }
}
