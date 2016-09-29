/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.Frontend;
import com.microsoft.azure.management.network.InboundNatRule;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.TransportProtocol;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

/**
 *  Implementation for {@link InboundNatRule}.
 */
@LangDefinition
class InboundNatRuleImpl
    extends ChildResourceImpl<InboundNatRuleInner, LoadBalancerImpl, LoadBalancer>
    implements
        InboundNatRule,
        InboundNatRule.Definition<LoadBalancer.DefinitionStages.WithCreateAndInboundNatRule>,
        InboundNatRule.UpdateDefinition<LoadBalancer.Update>,
        InboundNatRule.Update {

    InboundNatRuleImpl(InboundNatRuleInner inner, LoadBalancerImpl parent) {
        super(inner, parent);
    }

    // Getters

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public String backendNicIpConfigurationName() {
        if (this.inner().backendIPConfiguration() == null) {
            return null;
        } else {
            return ResourceUtils.nameFromResourceId(this.inner().backendIPConfiguration().id());
        }
    }

    @Override
    public int backendPort() {
        if (this.inner().backendPort() == null) {
            return 0;
        } else {
            return this.inner().backendPort().intValue();
        }
    }

    @Override
    public String backendNetworkInterfaceId() {
        if (this.inner().backendIPConfiguration() == null) {
            return null;
        } else {
            return ResourceUtils.parentResourcePathFromResourceId(this.inner().backendIPConfiguration().id());
        }
    }

    @Override
    public TransportProtocol protocol() {
        return this.inner().protocol();
    }

    @Override
    public int frontendPort() {
        if (this.inner().frontendPort() == null) {
            return 0;
        } else {
            return this.inner().frontendPort().intValue();
        }
    }

    @Override
    public boolean floatingIpEnabled() {
        return this.inner().enableFloatingIP().booleanValue();
    }

    @Override
    public Frontend frontend() {
        return this.parent().frontends().get(
                ResourceUtils.nameFromResourceId(
                        this.inner().frontendIPConfiguration().id()));
    }

    @Override
    public int idleTimeoutInMinutes() {
        return this.inner().idleTimeoutInMinutes();
    }

    // Fluent setters

    @Override
    public InboundNatRuleImpl withBackendPort(int port) {
        this.inner().withBackendPort(port);
        return this;
    }

    @Override
    public InboundNatRuleImpl withFloatingIpEnabled() {
        return withFloatingIp(true);
    }

    @Override
    public InboundNatRuleImpl withFloatingIpDisabled() {
        return withFloatingIp(false);
    }

    @Override
    public InboundNatRuleImpl withFloatingIp(boolean enabled) {
        this.inner().withEnableFloatingIP(enabled);
        return this;
    }

    @Override
    public InboundNatRuleImpl withFrontendPort(int port) {
        this.inner().withFrontendPort(port);
        if (this.backendPort() == 0) {
            // By default, assume the same backend port
            return this.withBackendPort(port);
        } else {
            return this;
        }
    }

    @Override
    public InboundNatRuleImpl withIdleTimeoutInMinutes(int minutes) {
        this.inner().withIdleTimeoutInMinutes(minutes);
        return this;
    }

    @Override
    public InboundNatRuleImpl withProtocol(TransportProtocol protocol) {
        this.inner().withProtocol(protocol);
        return this;
    }

    @Override
    public InboundNatRuleImpl withFrontend(String frontendName) {
        SubResource frontendRef = new SubResource()
                .withId(this.parent().futureResourceId() + "/frontendIPConfigurations/" + frontendName);
        this.inner().withFrontendIPConfiguration(frontendRef);
        return this;
    }

    // Verbs

    @Override
    public LoadBalancerImpl attach() {
        return this.parent().withInboundNatRule(this);
    }
}
