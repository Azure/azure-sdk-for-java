/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network.implementation;

import com.azure.core.management.SubResource;
import com.azure.management.network.LoadBalancer;
import com.azure.management.network.LoadBalancerFrontend;
import com.azure.management.network.LoadBalancerInboundNatRule;
import com.azure.management.network.Network;
import com.azure.management.network.PublicIPAddress;
import com.azure.management.network.Subnet;
import com.azure.management.network.TransportProtocol;
import com.azure.management.network.models.InboundNatRuleInner;
import com.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import com.azure.management.resources.fluentcore.model.Creatable;
import com.azure.management.resources.fluentcore.utils.Utils;

/**
 * Implementation for LoadBalancerInboundNatRule.
 */
class LoadBalancerInboundNatRuleImpl
        extends ChildResourceImpl<InboundNatRuleInner, LoadBalancerImpl, LoadBalancer>
        implements
        LoadBalancerInboundNatRule,
        LoadBalancerInboundNatRule.Definition<LoadBalancer.DefinitionStages.WithCreateAndInboundNatRule>,
        LoadBalancerInboundNatRule.UpdateDefinition<LoadBalancer.Update>,
        LoadBalancerInboundNatRule.Update {

    LoadBalancerInboundNatRuleImpl(InboundNatRuleInner inner, LoadBalancerImpl parent) {
        super(inner, parent);
    }

    // Getters

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public String backendNicIPConfigurationName() {
        if (this.inner().backendIPConfiguration() == null) {
            return null;
        } else {
            return ResourceUtils.nameFromResourceId(this.inner().backendIPConfiguration().getId());
        }
    }

    @Override
    public int backendPort() {
        return Utils.toPrimitiveInt(this.inner().backendPort());
    }

    @Override
    public String backendNetworkInterfaceId() {
        if (this.inner().backendIPConfiguration() == null) {
            return null;
        } else {
            return ResourceUtils.parentResourceIdFromResourceId(this.inner().backendIPConfiguration().getId());
        }
    }

    @Override
    public TransportProtocol protocol() {
        return this.inner().protocol();
    }

    @Override
    public int frontendPort() {
        return Utils.toPrimitiveInt(this.inner().frontendPort());
    }

    @Override
    public boolean floatingIPEnabled() {
        return this.inner().enableFloatingIP().booleanValue();
    }

    @Override
    public LoadBalancerFrontend frontend() {
        return this.parent().frontends().get(
                ResourceUtils.nameFromResourceId(
                        this.inner().frontendIPConfiguration().getId()));
    }

    @Override
    public int idleTimeoutInMinutes() {
        return Utils.toPrimitiveInt(this.inner().idleTimeoutInMinutes());
    }

    // Fluent setters

    @Override
    public LoadBalancerInboundNatRuleImpl toBackendPort(int port) {
        this.inner().withBackendPort(port);
        return this;
    }

    @Override
    public LoadBalancerInboundNatRuleImpl withFloatingIPEnabled() {
        return withFloatingIP(true);
    }

    @Override
    public LoadBalancerInboundNatRuleImpl withFloatingIPDisabled() {
        return withFloatingIP(false);
    }

    @Override
    public LoadBalancerInboundNatRuleImpl withFloatingIP(boolean enabled) {
        this.inner().withEnableFloatingIP(enabled);
        return this;
    }

    @Override
    public LoadBalancerInboundNatRuleImpl fromFrontendPort(int port) {
        this.inner().withFrontendPort(port);
        if (this.backendPort() == 0) {
            // By default, assume the same backend port
            return this.toBackendPort(port);
        } else {
            return this;
        }
    }

    @Override
    public LoadBalancerInboundNatRuleImpl withIdleTimeoutInMinutes(int minutes) {
        this.inner().withIdleTimeoutInMinutes(minutes);
        return this;
    }

    @Override
    public LoadBalancerInboundNatRuleImpl withProtocol(TransportProtocol protocol) {
        this.inner().withProtocol(protocol);
        return this;
    }

    @Override
    public LoadBalancerInboundNatRuleImpl fromFrontend(String frontendName) {
        SubResource frontendRef = this.parent().ensureFrontendRef(frontendName);
        if (frontendRef != null) {
            this.inner().withFrontendIPConfiguration(frontendRef);
        }
        return this;
    }

    // Verbs

    @Override
    public LoadBalancerImpl attach() {
        return this.parent().withInboundNatRule(this);
    }

    @Override
    public LoadBalancerInboundNatRuleImpl fromExistingPublicIPAddress(PublicIPAddress publicIPAddress) {
        return (publicIPAddress != null) ? this.fromExistingPublicIPAddress(publicIPAddress.id()) : this;
    }

    @Override
    public LoadBalancerInboundNatRuleImpl fromExistingPublicIPAddress(String resourceId) {
        return (null != resourceId) ? this.fromFrontend(this.parent().ensurePublicFrontendWithPip(resourceId).name()) : this;
    }

    @Override
    public LoadBalancerInboundNatRuleImpl fromNewPublicIPAddress(String leafDnsLabel) {
        String frontendName = this.parent().manager().getSdkContext().randomResourceName("fe", 20);
        this.parent().withNewPublicIPAddress(leafDnsLabel, frontendName);
        this.fromFrontend(frontendName);
        return this;
    }

    @Override
    public LoadBalancerInboundNatRuleImpl fromNewPublicIPAddress(Creatable<PublicIPAddress> pipDefinition) {
        String frontendName = this.parent().manager().getSdkContext().randomResourceName("fe", 20);
        this.parent().withNewPublicIPAddress(pipDefinition, frontendName);
        this.fromFrontend(frontendName);
        return this;
    }

    @Override
    public LoadBalancerInboundNatRuleImpl fromNewPublicIPAddress() {
        String dnsLabel = this.parent().manager().getSdkContext().randomResourceName("fe", 20);
        return this.fromNewPublicIPAddress(dnsLabel);
    }

    @Override
    public LoadBalancerInboundNatRuleImpl fromExistingSubnet(String networkResourceId, String subnetName) {
        return (null != networkResourceId && null != subnetName)
                ? this.fromFrontend(this.parent().ensurePrivateFrontendWithSubnet(networkResourceId, subnetName).name())
                : this;
    }

    @Override
    public LoadBalancerInboundNatRuleImpl fromExistingSubnet(Network network, String subnetName) {
        return (null != network && null != subnetName)
                ? this.fromExistingSubnet(network.id(), subnetName)
                : this;
    }

    @Override
    public LoadBalancerInboundNatRuleImpl fromExistingSubnet(Subnet subnet) {
        return (null != subnet)
                ? this.fromExistingSubnet(subnet.parent().id(), subnet.name())
                : this;
    }
}
