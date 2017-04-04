/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.LoadBalancerBackend;
import com.microsoft.azure.management.network.LoadBalancerFrontend;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.LoadBalancingRule;
import com.microsoft.azure.management.network.LoadDistribution;
import com.microsoft.azure.management.network.LoadBalancerProbe;
import com.microsoft.azure.management.network.TransportProtocol;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;

/**
 *  Implementation for {@link LoadBalancingRule}.
 */
@LangDefinition
class LoadBalancingRuleImpl
    extends ChildResourceImpl<LoadBalancingRuleInner, LoadBalancerImpl, LoadBalancer>
    implements
        LoadBalancingRule,
        LoadBalancingRule.Definition<LoadBalancer.DefinitionStages.WithLoadBalancingRuleOrCreate>,
        LoadBalancingRule.UpdateDefinition<LoadBalancer.Update>,
        LoadBalancingRule.Update {

    LoadBalancingRuleImpl(LoadBalancingRuleInner inner, LoadBalancerImpl parent) {
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
    public boolean floatingIPEnabled() {
        return this.inner().enableFloatingIP();
    }

    @Override
    public int idleTimeoutInMinutes() {
        return Utils.toPrimitiveInt(this.inner().idleTimeoutInMinutes());
    }

    @Override
    public int frontendPort() {
        return Utils.toPrimitiveInt(this.inner().frontendPort());
    }

    @Override
    public int backendPort() {
        return Utils.toPrimitiveInt(this.inner().backendPort());
    }

    @Override
    public LoadDistribution loadDistribution() {
        return this.inner().loadDistribution();
    }

    @Override
    public LoadBalancerFrontend frontend() {
        SubResource frontendRef = this.inner().frontendIPConfiguration();
        if (frontendRef == null) {
            return null;
        } else {
            String frontendName = ResourceUtils.nameFromResourceId(frontendRef.id());
            return this.parent().frontends().get(frontendName);
        }
    }

    @Override
    public LoadBalancerBackend backend() {
        SubResource backendRef = this.inner().backendAddressPool();
        if (backendRef == null) {
            return null;
        } else {
            String backendName = ResourceUtils.nameFromResourceId(backendRef.id());
            return this.parent().backends().get(backendName);
        }
    }

    @Override
    public LoadBalancerProbe probe() {
        SubResource probeRef = this.inner().probe();
        if (probeRef == null) {
            return null;
        } else {
            String probeName = ResourceUtils.nameFromResourceId(probeRef.id());
            if (this.parent().httpProbes().containsKey(probeName)) {
                return this.parent().httpProbes().get(probeName);
            } else if (this.parent().tcpProbes().containsKey(probeName)) {
                return this.parent().tcpProbes().get(probeName);
            } else {
                return null;
            }
        }
    }


    // Fluent setters

    @Override
    public LoadBalancingRuleImpl withIdleTimeoutInMinutes(int minutes) {
        this.inner().withIdleTimeoutInMinutes(minutes);
        return this;
    }

    @Override
    public LoadBalancingRuleImpl withFloatingIP(boolean enable) {
        this.inner().withEnableFloatingIP(enable);
        return this;
    }

    @Override
    public LoadBalancingRuleImpl withFloatingIPEnabled() {
        return withFloatingIP(true);
    }

    @Override
    public LoadBalancingRuleImpl withFloatingIPDisabled() {
        return withFloatingIP(false);
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

    @Override
    public LoadBalancingRuleImpl withFrontend(String frontendName) {
        SubResource frontendRef = new SubResource()
                .withId(this.parent().futureResourceId() + "/frontendIPConfigurations/" + frontendName);
        this.inner().withFrontendIPConfiguration(frontendRef);
        return this;
    }

    @Override
    public LoadBalancingRuleImpl withBackend(String backendName) {
        SubResource backendRef = new SubResource()
                .withId(this.parent().futureResourceId() + "/backendAddressPools/" + backendName);
        this.inner().withBackendAddressPool(backendRef);
        return this;
    }

    @Override
    public LoadBalancingRuleImpl withProbe(String name) {
        SubResource probeRef = new SubResource()
                .withId(this.parent().futureResourceId() + "/probes/" + name);
        this.inner().withProbe(probeRef);
        return this;
    }

    // Verbs

    @Override
    public LoadBalancerImpl attach() {
        return this.parent().withLoadBalancingRule(this);
    }
}
