/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.network.Frontend;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.LoadBalancingRule;
import com.microsoft.azure.management.network.LoadDistribution;
import com.microsoft.azure.management.network.TransportProtocol;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

/**
 *  Implementation for {@link LoadBalancingRule}.
 */
class LoadBalancingRuleImpl
    extends ChildResourceImpl<LoadBalancingRuleInner, LoadBalancerImpl>
    implements
        LoadBalancingRule,
        LoadBalancingRule.Definition<LoadBalancer.DefinitionStages.WithCreateAndRule>,
        LoadBalancingRule.UpdateDefinition<LoadBalancer.Update>,
        LoadBalancingRule.Update {

    protected LoadBalancingRuleImpl(LoadBalancingRuleInner inner, LoadBalancerImpl parent) {
        super(inner, parent);
    }

    // Getters

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public TransportProtocol protocol() {
        return this.inner().protocol();
    }

    @Override
    public boolean floatingIp() {
        return this.inner().enableFloatingIP();
    }

    @Override
    public int idleTimeoutInMinutes() {
        return this.inner().idleTimeoutInMinutes();
    }

    @Override
    public int frontendPort() {
        return this.inner().frontendPort();
    }

    @Override
    public int backendPort() {
        if (this.inner().backendPort() == null) {
            return 0;
        } else {
            return this.inner().backendPort();
        }
    }

    @Override
    public LoadDistribution loadDistribution() {
        return this.inner().loadDistribution();
    }

    @Override
    public Frontend frontend() {
        SubResource frontendRef = this.inner().frontendIPConfiguration();
        if (frontendRef == null) {
            return null;
        } else {
            String frontendName = ResourceUtils.nameFromResourceId(frontendRef.id());
            return this.parent().frontends().get(frontendName);
        }
    }

    // Fluent setters

    @Override
    public LoadBalancingRuleImpl withIdleTimeoutInMinutes(int minutes) {
        this.inner().withIdleTimeoutInMinutes(minutes);
        return this;
    }

    @Override
    public LoadBalancingRuleImpl withFloatingIp(boolean enable) {
        this.inner().withEnableFloatingIP(enable);
        return this;
    }

    @Override
    public LoadBalancingRuleImpl withProtocol(TransportProtocol protocol) {
        this.inner().withProtocol(protocol);
        return this;
    }

    @Override
    public LoadBalancingRuleImpl withFrontendPort(int port) {
        this.inner().withFrontendPort(port);

        // If backend port not specified earlier, make it the same as the frontend by default
        if (this.inner().backendPort() == null || this.inner().backendPort() == 0) {
            this.inner().withBackendPort(port);
        }

        return this;
    }

    @Override
    public LoadBalancingRuleImpl withBackendPort(int port) {
        this.inner().withBackendPort(port);
        return this;
    }

    @Override
    public LoadBalancingRuleImpl withLoadDistribution(LoadDistribution loadDistribution) {
        this.inner().withLoadDistribution(loadDistribution);
        return this;
    }

    // Verbs

    @Override
    public LoadBalancerImpl attach() {
        return this.parent().withLoadBalancingRule(this);
    }
}
