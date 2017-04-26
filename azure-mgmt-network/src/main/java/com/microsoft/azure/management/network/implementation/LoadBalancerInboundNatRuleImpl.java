/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.LoadBalancerFrontend;
import com.microsoft.azure.management.network.LoadBalancerInboundNatRule;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.TransportProtocol;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;

/**
 *  Implementation for {@link LoadBalancerInboundNatRule}.
 */
@LangDefinition
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
            return ResourceUtils.nameFromResourceId(this.inner().backendIPConfiguration().id());
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
            return ResourceUtils.parentResourceIdFromResourceId(this.inner().backendIPConfiguration().id());
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
                        this.inner().frontendIPConfiguration().id()));
    }

    @Override
    public int idleTimeoutInMinutes() {
        return Utils.toPrimitiveInt(this.inner().idleTimeoutInMinutes());
    }

    // Fluent setters

    @Override
    public LoadBalancerInboundNatRuleImpl withBackendPort(int port) {
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
    public LoadBalancerInboundNatRuleImpl withFrontendPort(int port) {
        this.inner().withFrontendPort(port);
        if (this.backendPort() == 0) {
            // By default, assume the same backend port
            return this.withBackendPort(port);
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
    public LoadBalancerInboundNatRuleImpl withFrontend(String frontendName) {
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
