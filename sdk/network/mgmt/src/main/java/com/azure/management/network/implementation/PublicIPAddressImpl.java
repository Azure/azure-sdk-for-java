/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network.implementation;

import com.azure.management.network.IPAllocationMethod;
import com.azure.management.network.IPVersion;
import com.azure.management.network.IpTag;
import com.azure.management.network.LoadBalancer;
import com.azure.management.network.LoadBalancerPublicFrontend;
import com.azure.management.network.NetworkInterface;
import com.azure.management.network.NicIPConfiguration;
import com.azure.management.network.PublicIPAddress;
import com.azure.management.network.PublicIPAddressDnsSettings;
import com.azure.management.network.PublicIPSkuType;
import com.azure.management.network.models.AppliableWithTags;
import com.azure.management.network.models.IPConfigurationInner;
import com.azure.management.network.models.PublicIPAddressInner;
import com.azure.management.resources.fluentcore.arm.AvailabilityZoneId;
import com.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.management.resources.fluentcore.utils.Utils;
import reactor.core.publisher.Mono;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Implementation for PublicIPAddress and its create and update interfaces.
 */
class PublicIPAddressImpl
        extends GroupableResourceImpl<
        PublicIPAddress,
        PublicIPAddressInner,
        PublicIPAddressImpl,
        NetworkManager>
        implements
        PublicIPAddress,
        PublicIPAddress.Definition,
        PublicIPAddress.Update,
        AppliableWithTags<PublicIPAddress> {

    PublicIPAddressImpl(String name,
                        PublicIPAddressInner innerModel,
                        final NetworkManager networkManager) {
        super(name, innerModel, networkManager);
    }

    // Verbs

    @Override
    protected Mono<PublicIPAddressInner> getInnerAsync() {
        return this.manager().inner().publicIPAddresses().getByResourceGroupAsync(this.resourceGroupName(), this.name(), null);
    }

    // Setters (fluent)

    @Override
    public PublicIPAddressImpl withIdleTimeoutInMinutes(int minutes) {
        this.inner().withIdleTimeoutInMinutes(minutes);
        return this;
    }

    @Override
    public PublicIPAddressImpl withStaticIP() {
        this.inner().withPublicIPAllocationMethod(IPAllocationMethod.STATIC);
        return this;
    }

    @Override
    public PublicIPAddressImpl withDynamicIP() {
        this.inner().withPublicIPAllocationMethod(IPAllocationMethod.DYNAMIC);
        return this;
    }

    @Override
    public PublicIPAddressImpl withLeafDomainLabel(String dnsName) {
        this.inner().dnsSettings().withDomainNameLabel((dnsName == null) ? null : dnsName.toLowerCase());
        return this;
    }

    @Override
    public PublicIPAddressImpl withAvailabilityZone(AvailabilityZoneId zoneId) {
        // Note: Zone is not updatable as of now, so this is available only during definition time.
        // Service return `ResourceAvailabilityZonesCannotBeModified` upon attempt to append a new
        // zone or remove one. Trying to remove the last one means attempt to change resource from
        // zonal to regional, which is not supported.
        //
        if (this.inner().zones() == null) {
            this.inner().withZones(new ArrayList<String>());
        }
        this.inner().zones().add(zoneId.toString());
        return this;
    }

    @Override
    public PublicIPAddressImpl withSku(PublicIPSkuType skuType) {
        // Note: SKU is not updatable as of now, so this is available only during definition time.
        // Service return `SkuCannotBeChangedOnUpdate` upon attempt to change it.
        // Service default is PublicIPSkuType.BASIC
        //
        this.inner().withSku(skuType.sku());
        return this;
    }

    @Override
    public PublicIPAddressImpl withoutLeafDomainLabel() {
        return this.withLeafDomainLabel(null);
    }

    @Override
    public PublicIPAddressImpl withReverseFqdn(String reverseFqdn) {
        this.inner().dnsSettings().withReverseFqdn(reverseFqdn != null ? reverseFqdn.toLowerCase() : null);
        return this;
    }

    @Override
    public PublicIPAddressImpl withoutReverseFqdn() {
        return this.withReverseFqdn(null);
    }

    // Getters

    @Override
    public int idleTimeoutInMinutes() {
        return Utils.toPrimitiveInt(this.inner().idleTimeoutInMinutes());
    }

    @Override
    public IPAllocationMethod ipAllocationMethod() {
        return this.inner().publicIPAllocationMethod();
    }

    @Override
    public IPVersion version() {
        return this.inner().publicIPAddressVersion();
    }

    @Override
    public String fqdn() {
        if (this.inner().dnsSettings() != null) {
            return this.inner().dnsSettings().fqdn();
        } else {
            return null;
        }
    }

    @Override
    public String reverseFqdn() {
        if (this.inner().dnsSettings() != null) {
            return this.inner().dnsSettings().reverseFqdn();
        } else {
            return null;
        }
    }

    @Override
    public String ipAddress() {
        return this.inner().ipAddress();
    }

    @Override
    public String leafDomainLabel() {
        if (this.inner().dnsSettings() == null) {
            return null;
        } else {
            return this.inner().dnsSettings().domainNameLabel();
        }
    }

    // CreateUpdateTaskGroup.ResourceCreator implementation
    @Override
    public Mono<PublicIPAddress> createResourceAsync() {
        // Clean up empty DNS settings
        final PublicIPAddressDnsSettings dnsSettings = this.inner().dnsSettings();
        if (dnsSettings != null) {
            if ((dnsSettings.domainNameLabel() == null || dnsSettings.domainNameLabel().isEmpty())
                    && (dnsSettings.fqdn() == null || dnsSettings.fqdn().isEmpty())
                    && (dnsSettings.reverseFqdn() == null || dnsSettings.reverseFqdn().isEmpty())) {
                this.inner().withDnsSettings(null);
            }
        }

        return this.manager().inner().publicIPAddresses().createOrUpdateAsync(
                this.resourceGroupName(), this.name(), this.inner())
                .map(innerToFluentMap(this));
    }

    private boolean equalsResourceType(String resourceType) {
        IPConfigurationInner ipConfig = this.inner().ipConfiguration();
        if (ipConfig == null || resourceType == null) {
            return false;
        } else {
            final String refId = this.inner().ipConfiguration().getId();
            final String resourceType2 = ResourceUtils.resourceTypeFromResourceId(refId);
            return resourceType.equalsIgnoreCase(resourceType2);
        }
    }

    @Override
    public boolean hasAssignedLoadBalancer() {
        return equalsResourceType("frontendIPConfigurations");
    }

    @Override
    public LoadBalancerPublicFrontend getAssignedLoadBalancerFrontend() {
        if (this.hasAssignedLoadBalancer()) {
            final String refId = this.inner().ipConfiguration().getId();
            final String loadBalancerId = ResourceUtils.parentResourceIdFromResourceId(refId);
            final LoadBalancer lb = this.myManager.loadBalancers().getById(loadBalancerId);
            final String frontendName = ResourceUtils.nameFromResourceId(refId);
            return (LoadBalancerPublicFrontend) lb.frontends().get(frontendName);
        } else {
            return null;
        }
    }

    @Override
    public boolean hasAssignedNetworkInterface() {
        return equalsResourceType("ipConfigurations");
    }

    @Override
    public Set<AvailabilityZoneId> availabilityZones() {
        Set<AvailabilityZoneId> zones = new HashSet<>();
        if (this.inner().zones() != null) {
            for (String zone : this.inner().zones()) {
                zones.add(AvailabilityZoneId.fromString(zone));
            }
        }
        return Collections.unmodifiableSet(zones);
    }

    @Override
    public PublicIPSkuType sku() {
        return PublicIPSkuType.fromSku(this.inner().sku());
    }

    @Override
    public List<IpTag> ipTags() {
        return Collections.unmodifiableList(inner().ipTags() == null ? new ArrayList<IpTag>() : inner().ipTags());
    }

    @Override
    public NicIPConfiguration getAssignedNetworkInterfaceIPConfiguration() {
        if (this.hasAssignedNetworkInterface()) {
            final String refId = this.inner().ipConfiguration().getId();
            final String parentId = ResourceUtils.parentResourceIdFromResourceId(refId);
            final NetworkInterface nic = this.myManager.networkInterfaces().getById(parentId);
            final String childName = ResourceUtils.nameFromResourceId(refId);
            return nic.ipConfigurations().get(childName);
        } else {
            return null;
        }
    }

    @Override
    public PublicIPAddressImpl updateTags() {
        return this;
    }

    @Override
    public PublicIPAddress applyTags() {
        return applyTagsAsync().block();
    }

    @Override
    public Mono<PublicIPAddress> applyTagsAsync() {
        return this.manager().inner().publicIPAddresses().updateTagsAsync(resourceGroupName(), name(), inner().getTags())
                .flatMap(inner -> {
                    setInner(inner);
                    return Mono.just((PublicIPAddress) PublicIPAddressImpl.this);
                });
    }

    @Override
    public PublicIPAddressImpl withIpTag(String tag) {
        if (inner().ipTags() == null) {
            inner().withIpTags(new ArrayList<IpTag>());
        }
        ipTags().add(new IpTag().withTag(tag));
        return this;
    }

    @Override
    public PublicIPAddressImpl withIpTag(String tag, String ipTagType) {
        if (inner().ipTags() == null) {
            inner().withIpTags(new ArrayList<IpTag>());
        }
        inner().ipTags().add(new IpTag().withTag(tag).withIpTagType(ipTagType));
        return this;
    }

    @Override
    public PublicIPAddressImpl withoutIpTag(String tag) {
        if (tag != null && inner().ipTags() != null) {
            for (IpTag ipTag : inner().ipTags()) {
                if (tag.equals(ipTag.tag())) {
                    inner().ipTags().remove(ipTag);
                    return this;
                }
            }
        }
        return this;
    }
}
