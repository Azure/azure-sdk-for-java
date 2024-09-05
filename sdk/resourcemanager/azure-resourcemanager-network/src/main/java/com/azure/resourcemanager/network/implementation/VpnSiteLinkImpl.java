// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.network.fluent.models.VpnSiteLinkInner;
import com.azure.resourcemanager.network.models.VpnLinkBgpSettings;
import com.azure.resourcemanager.network.models.VpnLinkProviderProperties;
import com.azure.resourcemanager.network.models.VpnSite;
import com.azure.resourcemanager.network.models.VpnSiteLink;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

import java.util.ArrayList;

public class VpnSiteLinkImpl
    extends ChildResourceImpl<VpnSiteLinkInner, VpnSiteImpl, VpnSite>
    implements VpnSiteLink,
        VpnSiteLink.Definition<VpnSite.DefinitionStages.WithCreate>,
        VpnSiteLink.UpdateDefinition<VpnSite.Update>,
        VpnSiteLink.Update {
    private final ClientLogger logger = new ClientLogger(getClass());

    VpnSiteLinkImpl(VpnSiteLinkInner inner, VpnSiteImpl parent) {
        super(inner, parent);
    }

    @Override
    public String ipAddress() {
        return this.innerModel().ipAddress();
    }

    @Override
    public String fqdn() {
        return this.innerModel().fqdn();
    }

    @Override
    public VpnLinkProviderProperties linkProperties() {
        return this.innerModel().linkProperties();
    }

    @Override
    public VpnLinkBgpSettings bgpProperties() {
        return this.innerModel().bgpProperties();
    }

    @Override
    public VpnSiteLinkImpl withBgpProperties(String bgpPeeringAddress, Long asn) {
        if (this.innerModel().bgpProperties() == null) {
            this.innerModel().withBgpProperties(new VpnLinkBgpSettings());
        }
        this.innerModel().bgpProperties().withBgpPeeringAddress(bgpPeeringAddress);
        this.innerModel().bgpProperties().withAsn(asn);
        return this;
    }

    @Override
    public VpnSiteLinkImpl withIpAddress(String ipAddress) {
        this.innerModel().withIpAddress(ipAddress);
        return this;
    }

    @Override
    public VpnSiteLinkImpl withFqdn(String fqdn) {
        this.innerModel().withFqdn(fqdn);
        return this;
    }

    @Override
    public VpnSiteLinkImpl withLinkProperties(String providerName, Integer speedInMbps) {
        if (this.innerModel().linkProperties() == null) {
            this.innerModel().withLinkProperties(new VpnLinkProviderProperties());
        }
        this.innerModel().linkProperties().withLinkProviderName(providerName);
        this.innerModel().linkProperties().withLinkSpeedInMbps(speedInMbps);
        return this;
    }

    @Override
    public String name() {
        return this.innerModel().name();
    }

    @Override
    public VpnSiteImpl attach() {
        if (this.parent().innerModel().vpnSiteLinks() == null) {
            this.parent().innerModel().withVpnSiteLinks(new ArrayList<VpnSiteLinkInner>());
        }
        this.parent().innerModel().vpnSiteLinks().add(this.innerModel());
        return this.parent();
    }
}
