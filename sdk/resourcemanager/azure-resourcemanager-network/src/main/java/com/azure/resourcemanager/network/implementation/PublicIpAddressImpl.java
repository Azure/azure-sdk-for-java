// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.util.Context;
import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.models.IpAllocationMethod;
import com.azure.resourcemanager.network.models.IpTag;
import com.azure.resourcemanager.network.models.IpVersion;
import com.azure.resourcemanager.network.models.LoadBalancer;
import com.azure.resourcemanager.network.models.LoadBalancerPublicFrontend;
import com.azure.resourcemanager.network.models.NetworkInterface;
import com.azure.resourcemanager.network.models.NicIpConfiguration;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.network.models.PublicIpAddressDnsSettings;
import com.azure.resourcemanager.network.models.PublicIPSkuType;
import com.azure.resourcemanager.network.models.AppliableWithTags;
import com.azure.resourcemanager.network.fluent.models.IpConfigurationInner;
import com.azure.resourcemanager.network.fluent.models.PublicIpAddressInner;
import com.azure.resourcemanager.resources.fluentcore.arm.AvailabilityZoneId;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.model.Accepted;
import com.azure.resourcemanager.resources.fluentcore.model.Indexable;
import com.azure.resourcemanager.resources.fluentcore.model.implementation.AcceptedImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/** Implementation for PublicIPAddress and its create and update interfaces. */
class PublicIpAddressImpl
    extends GroupableResourceImpl<PublicIpAddress, PublicIpAddressInner, PublicIpAddressImpl, NetworkManager>
    implements PublicIpAddress, PublicIpAddress.Definition, PublicIpAddress.Update, AppliableWithTags<PublicIpAddress> {

    private final ClientLogger logger = new ClientLogger(this.getClass());

    PublicIpAddressImpl(String name, PublicIpAddressInner innerModel, final NetworkManager networkManager) {
        super(name, innerModel, networkManager);
    }

    // Verbs

    @Override
    protected Mono<PublicIpAddressInner> getInnerAsync() {
        return this
            .manager()
            .serviceClient()
            .getPublicIpAddresses()
            .getByResourceGroupAsync(this.resourceGroupName(), this.name(), null);
    }

    // Setters (fluent)

    @Override
    public PublicIpAddressImpl withIdleTimeoutInMinutes(int minutes) {
        this.innerModel().withIdleTimeoutInMinutes(minutes);
        return this;
    }

    @Override
    public PublicIpAddressImpl withStaticIP() {
        this.innerModel().withPublicIpAllocationMethod(IpAllocationMethod.STATIC);
        return this;
    }

    @Override
    public PublicIpAddressImpl withDynamicIP() {
        this.innerModel().withPublicIpAllocationMethod(IpAllocationMethod.DYNAMIC);
        return this;
    }

    @Override
    public PublicIpAddressImpl withLeafDomainLabel(String dnsName) {
        if (this.innerModel().dnsSettings() == null) {
            this.innerModel().withDnsSettings(new PublicIpAddressDnsSettings());
        }
        this
            .innerModel()
            .dnsSettings()
            .withDomainNameLabel((dnsName == null) ? null : dnsName.toLowerCase(Locale.ROOT));
        return this;
    }

    @Override
    public PublicIpAddressImpl withAvailabilityZone(AvailabilityZoneId zoneId) {
        // Note: Zone is not updatable as of now, so this is available only during definition time.
        // Service return `ResourceAvailabilityZonesCannotBeModified` upon attempt to append a new
        // zone or remove one. Trying to remove the last one means attempt to change resource from
        // zonal to regional, which is not supported.
        //
        if (this.innerModel().zones() == null) {
            this.innerModel().withZones(new ArrayList<String>());
        }
        this.innerModel().zones().add(zoneId.toString());
        return this;
    }

    @Override
    public PublicIpAddressImpl withSku(PublicIPSkuType skuType) {
        // Note: SKU is not updatable as of now, so this is available only during definition time.
        // Service return `SkuCannotBeChangedOnUpdate` upon attempt to change it.
        // Service default is PublicIPSkuType.BASIC
        //
        this.innerModel().withSku(skuType.sku());
        return this;
    }

    @Override
    public PublicIpAddressImpl withoutLeafDomainLabel() {
        this.innerModel().withDnsSettings(null);
        return this;
    }

    @Override
    public PublicIpAddressImpl withReverseFqdn(String reverseFqdn) {
        if (this.innerModel().dnsSettings() == null) {
            this.innerModel().withDnsSettings(new PublicIpAddressDnsSettings());
        }
        this
            .innerModel()
            .dnsSettings()
            .withReverseFqdn(reverseFqdn != null ? reverseFqdn.toLowerCase(Locale.ROOT) : null);
        return this;
    }

    @Override
    public PublicIpAddressImpl withoutReverseFqdn() {
        return this.withReverseFqdn(null);
    }

    // Getters

    @Override
    public int idleTimeoutInMinutes() {
        return ResourceManagerUtils.toPrimitiveInt(this.innerModel().idleTimeoutInMinutes());
    }

    @Override
    public IpAllocationMethod ipAllocationMethod() {
        return this.innerModel().publicIpAllocationMethod();
    }

    @Override
    public IpVersion version() {
        return this.innerModel().publicIpAddressVersion();
    }

    @Override
    public String fqdn() {
        if (this.innerModel().dnsSettings() != null) {
            return this.innerModel().dnsSettings().fqdn();
        } else {
            return null;
        }
    }

    @Override
    public String reverseFqdn() {
        if (this.innerModel().dnsSettings() != null) {
            return this.innerModel().dnsSettings().reverseFqdn();
        } else {
            return null;
        }
    }

    @Override
    public String ipAddress() {
        return this.innerModel().ipAddress();
    }

    @Override
    public String leafDomainLabel() {
        if (this.innerModel().dnsSettings() == null) {
            return null;
        } else {
            return this.innerModel().dnsSettings().domainNameLabel();
        }
    }

    @Override
    public Accepted<PublicIpAddress> beginCreate() {
        return AcceptedImpl
            .newAccepted(
                logger,
                this.manager().serviceClient().getHttpPipeline(),
                this.manager().serviceClient().getDefaultPollInterval(),
                () ->
                    this
                        .manager()
                        .serviceClient()
                        .getPublicIpAddresses()
                        .createOrUpdateWithResponseAsync(resourceGroupName(), name(), this.innerModel())
                        .block(),
                inner -> new PublicIpAddressImpl(inner.name(), inner, this.manager()),
                PublicIpAddressInner.class,
                () -> {
                    Flux<Indexable> dependencyTasksAsync =
                        taskGroup().invokeDependencyAsync(taskGroup().newInvocationContext());
                    dependencyTasksAsync.blockLast();

                    this.cleanupDnsSettings();
                },
                this::setInner,
                Context.NONE);
    }

    // CreateUpdateTaskGroup.ResourceCreator implementation
    @Override
    public Mono<PublicIpAddress> createResourceAsync() {
        this.cleanupDnsSettings();

        return this
            .manager()
            .serviceClient()
            .getPublicIpAddresses()
            .createOrUpdateAsync(this.resourceGroupName(), this.name(), this.innerModel())
            .map(innerToFluentMap(this));
    }

    private void cleanupDnsSettings() {
        // Clean up empty DNS settings
        final PublicIpAddressDnsSettings dnsSettings = this.innerModel().dnsSettings();
        if (dnsSettings != null) {
            if ((dnsSettings.domainNameLabel() == null || dnsSettings.domainNameLabel().isEmpty())
                && (dnsSettings.fqdn() == null || dnsSettings.fqdn().isEmpty())
                && (dnsSettings.reverseFqdn() == null || dnsSettings.reverseFqdn().isEmpty())) {
                this.innerModel().withDnsSettings(null);
            }
        }
    }

    private boolean equalsResourceType(String resourceType) {
        IpConfigurationInner ipConfig = this.innerModel().ipConfiguration();
        if (ipConfig == null || resourceType == null) {
            return false;
        } else {
            final String refId = this.innerModel().ipConfiguration().id();
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
            final String refId = this.innerModel().ipConfiguration().id();
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
        if (this.innerModel().zones() != null) {
            for (String zone : this.innerModel().zones()) {
                zones.add(AvailabilityZoneId.fromString(zone));
            }
        }
        return Collections.unmodifiableSet(zones);
    }

    @Override
    public PublicIPSkuType sku() {
        return PublicIPSkuType.fromSku(this.innerModel().sku());
    }

    @Override
    public List<IpTag> ipTags() {
        return Collections
            .unmodifiableList(innerModel().ipTags() == null ? new ArrayList<IpTag>() : innerModel().ipTags());
    }

    @Override
    public NicIpConfiguration getAssignedNetworkInterfaceIPConfiguration() {
        if (this.hasAssignedNetworkInterface()) {
            final String refId = this.innerModel().ipConfiguration().id();
            final String parentId = ResourceUtils.parentResourceIdFromResourceId(refId);
            final NetworkInterface nic = this.myManager.networkInterfaces().getById(parentId);
            final String childName = ResourceUtils.nameFromResourceId(refId);
            return nic.ipConfigurations().get(childName);
        } else {
            return null;
        }
    }

    @Override
    public PublicIpAddressImpl updateTags() {
        return this;
    }

    @Override
    public PublicIpAddress applyTags() {
        return applyTagsAsync().block();
    }

    @Override
    public Mono<PublicIpAddress> applyTagsAsync() {
        return this
            .manager()
            .serviceClient()
            .getPublicIpAddresses()
            .updateTagsAsync(resourceGroupName(), name(), innerModel().tags())
            .flatMap(
                inner -> {
                    setInner(inner);
                    return Mono.just((PublicIpAddress) PublicIpAddressImpl.this);
                });
    }

    @Override
    public PublicIpAddressImpl withIpTag(String tag) {
        if (innerModel().ipTags() == null) {
            innerModel().withIpTags(new ArrayList<IpTag>());
        }
        ipTags().add(new IpTag().withTag(tag));
        return this;
    }

    @Override
    public PublicIpAddressImpl withIpTag(String tag, String ipTagType) {
        if (innerModel().ipTags() == null) {
            innerModel().withIpTags(new ArrayList<IpTag>());
        }
        innerModel().ipTags().add(new IpTag().withTag(tag).withIpTagType(ipTagType));
        return this;
    }

    @Override
    public PublicIpAddressImpl withoutIpTag(String tag) {
        if (tag != null && innerModel().ipTags() != null) {
            for (IpTag ipTag : innerModel().ipTags()) {
                if (tag.equals(ipTag.tag())) {
                    innerModel().ipTags().remove(ipTag);
                    return this;
                }
            }
        }
        return this;
    }

    @Override
    public PublicIpAddressImpl withIpAddressVersion(IpVersion ipVersion) {
        this.innerModel().withPublicIpAddressVersion(ipVersion);
        return this;
    }
}
