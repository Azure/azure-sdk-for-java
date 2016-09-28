/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.Frontend;
import com.microsoft.azure.management.network.IPAllocationMethod;
import com.microsoft.azure.management.network.InboundNatPool;
import com.microsoft.azure.management.network.InboundNatRule;
import com.microsoft.azure.management.network.PrivateFrontend;
import com.microsoft.azure.management.network.PublicFrontend;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.LoadBalancingRule;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

/**
 *  Implementation for {@link PublicFrontend}.
 */
@LangDefinition
class FrontendImpl
    extends ChildResourceImpl<FrontendIPConfigurationInner, LoadBalancerImpl, LoadBalancer>
    implements
        Frontend,
        PrivateFrontend,
        PrivateFrontend.Definition<LoadBalancer.DefinitionStages.WithPrivateFrontendOrBackend>,
        PrivateFrontend.UpdateDefinition<LoadBalancer.Update>,
        PrivateFrontend.Update,
        PublicFrontend,
        PublicFrontend.Definition<LoadBalancer.DefinitionStages.WithPublicFrontendOrBackend>,
        PublicFrontend.UpdateDefinition<LoadBalancer.Update>,
        PublicFrontend.Update {

    FrontendImpl(FrontendIPConfigurationInner inner, LoadBalancerImpl parent) {
        super(inner, parent);
    }

    // Getters

    @Override
    public String networkId() {
        SubResource subnetRef = this.inner().subnet();
        if (subnetRef != null) {
            return ResourceUtils.parentResourcePathFromResourceId(subnetRef.id());
        } else {
            return null;
        }
    }

    @Override
    public String subnetName() {
        SubResource subnetRef = this.inner().subnet();
        if (subnetRef != null) {
            return ResourceUtils.nameFromResourceId(subnetRef.id());
        } else {
            return null;
        }
    }

    @Override
    public String privateIpAddress() {
        return this.inner().privateIPAddress();
    }

    @Override
    public IPAllocationMethod privateIpAllocationMethod() {
        return this.inner().privateIPAllocationMethod();
    }

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public String publicIpAddressId() {
        return this.inner().publicIPAddress().id();
    }

    @Override
    public boolean isPublic() {
        return (this.inner().publicIPAddress() != null);
    }

    @Override
    public Map<String, LoadBalancingRule> loadBalancingRules() {
        final Map<String, LoadBalancingRule> rules = new TreeMap<>();
        if (this.inner().loadBalancingRules() != null) {
            for (SubResource innerRef : this.inner().loadBalancingRules()) {
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
    public Map<String, InboundNatPool> inboundNatPools() {
        final Map<String, InboundNatPool> pools = new TreeMap<>();
        if (this.inner().inboundNatPools() != null) {
            for (SubResource innerRef : this.inner().inboundNatPools()) {
                String name = ResourceUtils.nameFromResourceId(innerRef.id());
                InboundNatPool pool = this.parent().inboundNatPools().get(name);
                if (pool != null) {
                    pools.put(name, pool);
                }
            }
        }

        return Collections.unmodifiableMap(pools);
    }

    @Override
    public Map<String, InboundNatRule> inboundNatRules() {
        final Map<String, InboundNatRule> rules = new TreeMap<>();
        if (this.inner().inboundNatRules() != null) {
            for (SubResource innerRef : this.inner().inboundNatRules()) {
                String name = ResourceUtils.nameFromResourceId(innerRef.id());
                InboundNatRule rule = this.parent().inboundNatRules().get(name);
                if (rule != null) {
                    rules.put(name, rule);
                }
            }
        }

        return Collections.unmodifiableMap(rules);
    }

    // Fluent setters

    @Override
    public FrontendImpl withExistingSubnet(Network network, String subnetName) {
        return this.withExistingSubnet(network.id(), subnetName);
    }

    @Override
    public FrontendImpl withExistingSubnet(String parentNetworkResourceId, String subnetName) {
        SubResource subnetRef = new SubResource()
                .withId(parentNetworkResourceId + "/subnets/" + subnetName);
        this.inner()
            .withSubnet(subnetRef)
            .withPublicIPAddress(null); // Ensure no conflicting public and private settings
        return this;
    }

    @Override
    public FrontendImpl withExistingPublicIpAddress(PublicIpAddress pip) {
        return this.withExistingPublicIpAddress(pip.id());
    }

    @Override
    public FrontendImpl withExistingPublicIpAddress(String resourceId) {
        SubResource pipRef = new SubResource().withId(resourceId);
        this.inner()
            .withPublicIPAddress(pipRef)

            // Ensure no conflicting public and private settings
            .withSubnet(null)
            .withPrivateIPAddress(null)
            .withPrivateIPAllocationMethod(null);
        return this;
    }

    @Override
    public FrontendImpl withoutPublicIpAddress() {
        this.inner().withPublicIPAddress(null);
        return this;
    }

    @Override
    public FrontendImpl withPrivateIpAddressDynamic() {
        this.inner()
            .withPrivateIPAddress(null)
            .withPrivateIPAllocationMethod(IPAllocationMethod.DYNAMIC)

            // Ensure no conflicting public and private settings
            .withPublicIPAddress(null);
        return this;
    }

    @Override
    public FrontendImpl withPrivateIpAddressStatic(String ipAddress) {
        this.inner()
            .withPrivateIPAddress(ipAddress)
            .withPrivateIPAllocationMethod(IPAllocationMethod.STATIC)

            // Ensure no conflicting public and private settings
            .withPublicIPAddress(null);
        return this;
    }

    // Verbs

    @Override
    public LoadBalancerImpl attach() {
        return this.parent().withFrontend(this);
    }

    @Override
    public PublicIpAddress getPublicIpAddress() {
        final String pipId = this.publicIpAddressId();
        if (pipId == null) {
            return null;
        } else {
            return this.parent().manager().publicIpAddresses().getById(pipId);
        }
    }
}
