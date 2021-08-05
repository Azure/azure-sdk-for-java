// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.management.SubResource;
import com.azure.resourcemanager.network.models.IpAllocationMethod;
import com.azure.resourcemanager.network.models.LoadBalancer;
import com.azure.resourcemanager.network.models.LoadBalancerFrontend;
import com.azure.resourcemanager.network.models.LoadBalancerInboundNatPool;
import com.azure.resourcemanager.network.models.LoadBalancerInboundNatRule;
import com.azure.resourcemanager.network.models.LoadBalancerPrivateFrontend;
import com.azure.resourcemanager.network.models.LoadBalancerPublicFrontend;
import com.azure.resourcemanager.network.models.LoadBalancingRule;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.network.models.Subnet;
import com.azure.resourcemanager.network.fluent.models.FrontendIpConfigurationInner;
import com.azure.resourcemanager.network.fluent.models.PublicIpAddressInner;
import com.azure.resourcemanager.network.fluent.models.SubnetInner;
import com.azure.resourcemanager.resources.fluentcore.arm.AvailabilityZoneId;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/** Implementation for LoadBalancerPublicFrontend. */
class LoadBalancerFrontendImpl extends ChildResourceImpl<FrontendIpConfigurationInner, LoadBalancerImpl, LoadBalancer>
    implements LoadBalancerFrontend,
        LoadBalancerPrivateFrontend,
        LoadBalancerPrivateFrontend.Definition<LoadBalancer.DefinitionStages.WithCreate>,
        LoadBalancerPrivateFrontend.UpdateDefinition<LoadBalancer.Update>,
        LoadBalancerPrivateFrontend.Update,
        LoadBalancerPublicFrontend,
        LoadBalancerPublicFrontend.Definition<LoadBalancer.DefinitionStages.WithCreate>,
        LoadBalancerPublicFrontend.UpdateDefinition<LoadBalancer.Update>,
        LoadBalancerPublicFrontend.Update {

    LoadBalancerFrontendImpl(FrontendIpConfigurationInner inner, LoadBalancerImpl parent) {
        super(inner, parent);
    }

    // Getters

    @Override
    public String networkId() {
        SubResource subnetRef = this.innerModel().subnet();
        if (subnetRef != null) {
            return ResourceUtils.parentResourceIdFromResourceId(subnetRef.id());
        } else {
            return null;
        }
    }

    @Override
    public String subnetName() {
        SubResource subnetRef = this.innerModel().subnet();
        if (subnetRef != null) {
            return ResourceUtils.nameFromResourceId(subnetRef.id());
        } else {
            return null;
        }
    }

    @Override
    public String privateIpAddress() {
        return this.innerModel().privateIpAddress();
    }

    @Override
    public IpAllocationMethod privateIpAllocationMethod() {
        return this.innerModel().privateIpAllocationMethod();
    }

    @Override
    public String name() {
        return this.innerModel().name();
    }

    @Override
    public String publicIpAddressId() {
        return this.innerModel().publicIpAddress().id();
    }

    @Override
    public boolean isPublic() {
        return (this.innerModel().publicIpAddress() != null);
    }

    @Override
    public Map<String, LoadBalancingRule> loadBalancingRules() {
        final Map<String, LoadBalancingRule> rules = new TreeMap<>();
        if (this.innerModel().loadBalancingRules() != null) {
            for (SubResource innerRef : this.innerModel().loadBalancingRules()) {
                String name = ResourceUtils.nameFromResourceId(innerRef.id());
                LoadBalancingRule rule = this.parent().loadBalancingRules().get(name);
                if (rule != null) {
                    rules.put(name, rule);
                }
            }
        }

        return Collections.unmodifiableMap(rules);
    }

    @Override
    public Map<String, LoadBalancerInboundNatPool> inboundNatPools() {
        final Map<String, LoadBalancerInboundNatPool> pools = new TreeMap<>();
        if (this.innerModel().inboundNatPools() != null) {
            for (SubResource innerRef : this.innerModel().inboundNatPools()) {
                String name = ResourceUtils.nameFromResourceId(innerRef.id());
                LoadBalancerInboundNatPool pool = this.parent().inboundNatPools().get(name);
                if (pool != null) {
                    pools.put(name, pool);
                }
            }
        }

        return Collections.unmodifiableMap(pools);
    }

    @Override
    public Map<String, LoadBalancerInboundNatRule> inboundNatRules() {
        final Map<String, LoadBalancerInboundNatRule> rules = new TreeMap<>();
        if (this.innerModel().inboundNatRules() != null) {
            for (SubResource innerRef : this.innerModel().inboundNatRules()) {
                String name = ResourceUtils.nameFromResourceId(innerRef.id());
                LoadBalancerInboundNatRule rule = this.parent().inboundNatRules().get(name);
                if (rule != null) {
                    rules.put(name, rule);
                }
            }
        }

        return Collections.unmodifiableMap(rules);
    }

    // Fluent setters

    @Override
    public LoadBalancerFrontendImpl withExistingSubnet(Network network, String subnetName) {
        return this.withExistingSubnet(network.id(), subnetName);
    }

    @Override
    public LoadBalancerFrontendImpl withExistingSubnet(String parentNetworkResourceId, String subnetName) {
        SubnetInner subnetRef = new SubnetInner();
        subnetRef.withId(parentNetworkResourceId + "/subnets/" + subnetName);
        this
            .innerModel()
            .withSubnet(subnetRef)
            .withPublicIpAddress(null); // Ensure no conflicting public and private settings
        return this;
    }

    @Override
    public LoadBalancerFrontendImpl withAvailabilityZone(AvailabilityZoneId zoneId) {
        // Note: Zone is not updatable as of now, so this is available only during definition time.
        // Service return `ResourceAvailabilityZonesCannotBeModified` upon attempt to append a new
        // zone or remove one. Trying to remove the last one means attempt to change resource from
        // zonal to regional, which is not supported.
        //
        // Zone is supported only for internal load balancer, hence exposed only for PrivateFrontEnd
        //
        if (this.innerModel().zones() == null) {
            this.innerModel().withZones(new ArrayList<String>());
        }
        this.innerModel().zones().add(zoneId.toString());
        return this;
    }

    @Override
    public LoadBalancerFrontendImpl withExistingPublicIpAddress(PublicIpAddress pip) {
        return this.withExistingPublicIpAddress(pip.id());
    }

    @Override
    public LoadBalancerFrontendImpl withExistingPublicIpAddress(String resourceId) {
        PublicIpAddressInner pipRef = new PublicIpAddressInner().withId(resourceId);
        this
            .innerModel()
            .withPublicIpAddress(pipRef)

            // Ensure no conflicting public and private settings
            .withSubnet(null)
            .withPrivateIpAddress(null)
            .withPrivateIpAllocationMethod(null);
        return this;
    }

    @Override
    public LoadBalancerFrontendImpl withoutPublicIpAddress() {
        this.innerModel().withPublicIpAddress(null);
        return this;
    }

    @Override
    public LoadBalancerFrontendImpl withPrivateIpAddressDynamic() {
        this
            .innerModel()
            .withPrivateIpAddress(null)
            .withPrivateIpAllocationMethod(IpAllocationMethod.DYNAMIC)

            // Ensure no conflicting public and private settings
            .withPublicIpAddress(null);
        return this;
    }

    @Override
    public LoadBalancerFrontendImpl withPrivateIpAddressStatic(String ipAddress) {
        this
            .innerModel()
            .withPrivateIpAddress(ipAddress)
            .withPrivateIpAllocationMethod(IpAllocationMethod.STATIC)

            // Ensure no conflicting public and private settings
            .withPublicIpAddress(null);
        return this;
    }

    @Override
    public LoadBalancerFrontendImpl withNewPublicIpAddress(String leafDnsLabel) {
        this.parent().withNewPublicIPAddress(leafDnsLabel, this.name());
        return this;
    }

    @Override
    public LoadBalancerFrontendImpl withNewPublicIpAddress(Creatable<PublicIpAddress> creatable) {
        this.parent().withNewPublicIPAddress(creatable, this.name());
        return this;
    }

    @Override
    public LoadBalancerFrontendImpl withNewPublicIpAddress() {
        String dnsLabel = this.parent().manager().resourceManager().internalContext().randomResourceName("fe", 20);
        return this.withNewPublicIpAddress(dnsLabel);
    }

    // Verbs

    @Override
    public LoadBalancerImpl attach() {
        return this.parent().withFrontend(this);
    }

    @Override
    public PublicIpAddress getPublicIpAddress() {
        return this.getPublicIpAddressAsync().block();
    }

    @Override
    public Mono<PublicIpAddress> getPublicIpAddressAsync() {
        String pipId = this.publicIpAddressId();
        return pipId == null ? Mono.empty() : this.parent().manager().publicIpAddresses().getByIdAsync(pipId);
    }

    @Override
    public Subnet getSubnet() {
        return Utils.getAssociatedSubnet(this.parent().manager(), this.innerModel().subnet());
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
}
