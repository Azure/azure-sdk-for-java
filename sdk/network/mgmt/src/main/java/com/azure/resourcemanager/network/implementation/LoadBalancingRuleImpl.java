// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.network.implementation;

import com.azure.core.management.SubResource;
import com.azure.resourcemanager.network.models.LoadBalancer;
import com.azure.resourcemanager.network.models.LoadBalancerBackend;
import com.azure.resourcemanager.network.models.LoadBalancerFrontend;
import com.azure.resourcemanager.network.models.LoadBalancerProbe;
import com.azure.resourcemanager.network.models.LoadBalancingRule;
import com.azure.resourcemanager.network.models.LoadDistribution;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.PublicIpAddress;
import com.azure.resourcemanager.network.models.Subnet;
import com.azure.resourcemanager.network.models.TransportProtocol;
import com.azure.resourcemanager.network.models.HasNetworkInterfaces;
import com.azure.resourcemanager.network.fluent.inner.LoadBalancingRuleInner;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import java.util.Arrays;
import java.util.Collection;

/** Implementation for LoadBalancingRule. */
class LoadBalancingRuleImpl extends ChildResourceImpl<LoadBalancingRuleInner, LoadBalancerImpl, LoadBalancer>
    implements LoadBalancingRule,
        LoadBalancingRule.Definition<LoadBalancer.DefinitionStages.WithLBRuleOrNatOrCreate>,
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
        return this.inner().enableFloatingIp();
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
        SubResource frontendRef = this.inner().frontendIpConfiguration();
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

    // Fluent withers

    @Override
    public LoadBalancingRuleImpl fromExistingPublicIPAddress(PublicIpAddress publicIPAddress) {
        return (publicIPAddress != null) ? this.fromExistingPublicIPAddress(publicIPAddress.id()) : this;
    }

    @Override
    public LoadBalancingRuleImpl fromExistingPublicIPAddress(String resourceId) {
        return (null != resourceId)
            ? this.fromFrontend(this.parent().ensurePublicFrontendWithPip(resourceId).name())
            : this;
    }

    @Override
    public LoadBalancingRuleImpl fromNewPublicIPAddress(String leafDnsLabel) {
        String frontendName = this.parent().manager().sdkContext().randomResourceName("fe", 20);
        this.parent().withNewPublicIPAddress(leafDnsLabel, frontendName);
        return fromFrontend(frontendName);
    }

    @Override
    public LoadBalancingRuleImpl fromNewPublicIPAddress(Creatable<PublicIpAddress> pipDefinition) {
        String frontendName = this.parent().manager().sdkContext().randomResourceName("fe", 20);
        this.parent().withNewPublicIPAddress(pipDefinition, frontendName);
        return fromFrontend(frontendName);
    }

    @Override
    public LoadBalancingRuleImpl fromNewPublicIPAddress() {
        String dnsLabel = this.parent().manager().sdkContext().randomResourceName("fe", 20);
        return this.fromNewPublicIPAddress(dnsLabel);
    }

    @Override
    public LoadBalancingRuleImpl fromExistingSubnet(String networkResourceId, String subnetName) {
        return (null != networkResourceId && null != subnetName)
            ? this.fromFrontend(this.parent().ensurePrivateFrontendWithSubnet(networkResourceId, subnetName).name())
            : this;
    }

    @Override
    public LoadBalancingRuleImpl fromExistingSubnet(Network network, String subnetName) {
        return (null != network && null != subnetName) ? this.fromExistingSubnet(network.id(), subnetName) : this;
    }

    @Override
    public LoadBalancingRuleImpl fromExistingSubnet(Subnet subnet) {
        return (null != subnet) ? this.fromExistingSubnet(subnet.parent().id(), subnet.name()) : this;
    }

    @Override
    public LoadBalancingRuleImpl withIdleTimeoutInMinutes(int minutes) {
        this.inner().withIdleTimeoutInMinutes(minutes);
        return this;
    }

    @Override
    public LoadBalancingRuleImpl withFloatingIP(boolean enable) {
        this.inner().withEnableFloatingIp(enable);
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
    public LoadBalancingRuleImpl fromFrontendPort(int port) {
        this.inner().withFrontendPort(port);

        // If backend port not specified earlier, make it the same as the frontend by default
        if (this.inner().backendPort() == null || this.inner().backendPort() == 0) {
            this.inner().withBackendPort(port);
        }

        return this;
    }

    @Override
    public LoadBalancingRuleImpl toBackendPort(int port) {
        this.inner().withBackendPort(port);
        return this;
    }

    @Override
    public LoadBalancingRuleImpl toExistingVirtualMachines(HasNetworkInterfaces... vms) {
        return (vms != null) ? this.toExistingVirtualMachines(Arrays.asList(vms)) : this;
    }

    @Override
    public LoadBalancingRuleImpl toExistingVirtualMachines(Collection<HasNetworkInterfaces> vms) {
        if (vms != null) {
            LoadBalancerBackendImpl backend = this.parent().ensureUniqueBackend().withExistingVirtualMachines(vms);
            this.toBackend(backend.name());
        }
        return this;
    }

    @Override
    public LoadBalancingRuleImpl withLoadDistribution(LoadDistribution loadDistribution) {
        this.inner().withLoadDistribution(loadDistribution);
        return this;
    }

    @Override
    public LoadBalancingRuleImpl fromFrontend(String frontendName) {
        SubResource frontendRef = this.parent().ensureFrontendRef(frontendName);
        if (frontendRef != null) {
            this.inner().withFrontendIpConfiguration(frontendRef);
        }
        return this;
    }

    @Override
    public LoadBalancingRuleImpl toBackend(String backendName) {
        // Ensure existence of backend, creating one if needed
        this.parent().defineBackend(backendName).attach();

        SubResource backendRef =
            new SubResource().withId(this.parent().futureResourceId() + "/backendAddressPools/" + backendName);
        this.inner().withBackendAddressPool(backendRef);
        return this;
    }

    @Override
    public LoadBalancingRuleImpl withProbe(String name) {
        SubResource probeRef = new SubResource().withId(this.parent().futureResourceId() + "/probes/" + name);
        this.inner().withProbe(probeRef);
        return this;
    }

    @Override
    public LoadBalancingRuleImpl withoutProbe() {
        this.inner().withProbe(null);
        return this;
    }

    // Verbs

    @Override
    public LoadBalancerImpl attach() {
        return this.parent().withLoadBalancingRule(this);
    }
}
