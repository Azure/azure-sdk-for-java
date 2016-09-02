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
import com.microsoft.azure.management.network.Frontend;
import com.microsoft.azure.management.network.InboundNatPool;
import com.microsoft.azure.management.network.InboundNatRule;
import com.microsoft.azure.management.network.InternetFrontend;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.PublicIpAddress;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

/**
 *  Implementation for {@link InternetFrontend}.
 */
class FrontendImpl
    extends ChildResourceImpl<FrontendIPConfigurationInner, LoadBalancerImpl>
    implements
        Frontend,
        InternetFrontend,
        InternetFrontend.Definition<LoadBalancer.DefinitionStages.WithInternetFrontendOrBackend>,
        InternetFrontend.UpdateDefinition<LoadBalancer.Update>,
        InternetFrontend.Update {

    protected FrontendImpl(FrontendIPConfigurationInner inner, LoadBalancerImpl parent) {
        super(inner, parent);
    }

    // Getters

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public String publicIpAddressId() {
        return this.inner().publicIPAddress().id();
    }

    @Override
    public boolean isInternetFacing() {
        return (this.inner().publicIPAddress() != null);
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
    public FrontendImpl withExistingPublicIpAddress(PublicIpAddress pip) {
        return this.withExistingPublicIpAddress(pip.id());
    }

    @Override
    public FrontendImpl withExistingPublicIpAddress(String resourceId) {
        SubResource pipRef = new SubResource().withId(resourceId);
        this.inner().withPublicIPAddress(pipRef);
        return this;
    }

    // Verbs

    @Override
    public LoadBalancerImpl attach() {
        return this.parent().withFrontend(this);
    }
}
