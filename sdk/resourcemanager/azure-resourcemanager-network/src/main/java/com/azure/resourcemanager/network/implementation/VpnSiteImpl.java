// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.implementation;

import com.azure.core.management.SubResource;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.fluent.models.VpnSiteInner;
import com.azure.resourcemanager.network.fluent.models.VpnSiteLinkInner;
import com.azure.resourcemanager.network.models.AddressSpace;
import com.azure.resourcemanager.network.models.DeviceProperties;
import com.azure.resourcemanager.network.models.O365PolicyProperties;
import com.azure.resourcemanager.network.models.VirtualWan;
import com.azure.resourcemanager.network.models.VpnSite;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Implementation for VPN site. */
public class VpnSiteImpl extends GroupableResourceImpl<VpnSite, VpnSiteInner, VpnSiteImpl, NetworkManager>
    implements VpnSite, VpnSite.Definition, VpnSite.Update {

    VpnSiteImpl(String name, VpnSiteInner innerModel, final NetworkManager manager) {
        super(name, innerModel, manager);
    }

    @Override
    public VpnSiteImpl withVirtualWan(String subResourceId) {
        if (this.innerModel().virtualWan() == null) {
            this.innerModel().withVirtualWan(new SubResource());
        }
        this.innerModel().virtualWan().withId(subResourceId);
        return this;
    }

    @Override
    public VpnSiteImpl withAddressSpace(String cidr) {
        if (this.innerModel().addressSpace() == null) {
            this.innerModel().withAddressSpace(new AddressSpace());
        }
        if (this.innerModel().addressSpace().addressPrefixes() == null) {
            this.innerModel().addressSpace().withAddressPrefixes(new ArrayList<String>());
        }
        this.innerModel().addressSpace().withAddressPrefixes(Arrays.asList(cidr));
        return this;
    }

    @Override
    public VpnSiteImpl withIsSecuritySite(Boolean isSecuritySite) {
        this.innerModel().withIsSecuritySite(isSecuritySite);
        return  this;
    }

    @Override
    public VpnSiteImpl withVpnSiteLinks(List<VpnSiteLinkInner> vpnSiteLinks) {
        if (this.innerModel().vpnSiteLinks() == null) {
            this.innerModel().withVpnSiteLinks(new ArrayList<VpnSiteLinkInner>());
        }
        this.innerModel().vpnSiteLinks().addAll(vpnSiteLinks);
        return this;
    }

    @Override
    public VpnSiteImpl withO365Policy(O365PolicyProperties o365Policy) {
        this.innerModel().withO365Policy(o365Policy);
        return this;
    }

    @Override
    public VpnSiteImpl withDevice(DeviceProperties deviceProperties) {
        this.innerModel().withDeviceProperties(deviceProperties);
        return this;
    }

    @Override
    public Mono<VpnSite> createResourceAsync() {
        this.innerModel().withLocation(this.regionName());
        this.innerModel().withTags(this.innerModel().tags());
        return this
            .manager()
            .serviceClient()
            .getVpnSites()
            .createOrUpdateAsync(this.resourceGroupName(), this.name(), this.innerModel())
            .map(innerToFluentMap(this));
    }

    @Override
    protected Mono<VpnSiteInner> getInnerAsync() {
        return this
            .manager()
            .serviceClient()
            .getVpnSites()
            .getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public boolean isSecuritySite() {
        return ResourceManagerUtils.toPrimitiveBoolean(this.innerModel().isSecuritySite());
    }

    @Override
    public List<String> addressPrefixes() {
        return this.innerModel().addressSpace().addressPrefixes();
    }

    @Override
    public VirtualWan virtualWan() {
        return this.manager().virtualWans().getById(this.innerModel().virtualWan().id());
    }

    @Override
    public List<VpnSiteLinkInner> vpnSiteLinks() {
        return this.innerModel().vpnSiteLinks();
    }

    @Override
    public O365PolicyProperties o365Policy() {
        return this.innerModel().o365Policy();
    }

    @Override
    public DeviceProperties device() {
        return this.innerModel().deviceProperties();
    }
}
