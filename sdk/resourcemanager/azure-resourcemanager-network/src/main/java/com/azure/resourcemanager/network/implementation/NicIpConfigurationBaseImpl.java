// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.implementation;

import com.azure.core.management.SubResource;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.models.ApplicationGatewayBackend;
import com.azure.resourcemanager.network.models.IpAllocationMethod;
import com.azure.resourcemanager.network.models.IpVersion;
import com.azure.resourcemanager.network.models.LoadBalancer;
import com.azure.resourcemanager.network.models.LoadBalancerBackend;
import com.azure.resourcemanager.network.models.LoadBalancerInboundNatRule;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.network.models.NetworkSecurityGroup;
import com.azure.resourcemanager.network.models.NicIpConfigurationBase;
import com.azure.resourcemanager.network.models.Subnet;
import com.azure.resourcemanager.network.fluent.inner.BackendAddressPoolInner;
import com.azure.resourcemanager.network.fluent.inner.InboundNatRuleInner;
import com.azure.resourcemanager.network.fluent.inner.NetworkInterfaceIpConfigurationInner;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.HasManager;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Base class implementation for various network interface IP configurations.
 *
 * @param <ParentImplT> parent implementation
 * @param <ParentT> parent interface
 */
abstract class NicIpConfigurationBaseImpl<ParentImplT extends ParentT, ParentT extends HasManager<NetworkManager>>
    extends ChildResourceImpl<NetworkInterfaceIpConfigurationInner, ParentImplT, ParentT>
    implements NicIpConfigurationBase {
    /** the network client. */
    private final NetworkManager networkManager;

    protected NicIpConfigurationBaseImpl(
        NetworkInterfaceIpConfigurationInner inner, ParentImplT parent, NetworkManager networkManager) {
        super(inner, parent);
        this.networkManager = networkManager;
    }

    public String name() {
        return inner().name();
    }

    public boolean isPrimary() {
        return Utils.toPrimitiveBoolean(this.inner().primary());
    }

    @Override
    public NetworkSecurityGroup getNetworkSecurityGroup() {
        Network network = this.getNetwork();
        if (network == null) {
            return null;
        }

        String subnetName = this.subnetName();
        if (subnetName == null) {
            return null;
        }

        Subnet subnet = network.subnets().get(subnetName);
        if (subnet == null) {
            return null;
        }

        return subnet.getNetworkSecurityGroup();
    }

    @Override
    public String privateIpAddress() {
        return this.inner().privateIpAddress();
    }

    @Override
    public IpAllocationMethod privateIpAllocationMethod() {
        return this.inner().privateIpAllocationMethod();
    }

    @Override
    public IpVersion privateIpAddressVersion() {
        return this.inner().privateIpAddressVersion();
    }

    public String networkId() {
        SubResource subnetRef = this.inner().subnet();
        if (subnetRef == null) {
            return null;
        }
        return ResourceUtils.parentResourceIdFromResourceId(subnetRef.id());
    }

    public Network getNetwork() {
        String id = this.networkId();
        if (id == null) {
            return null;
        }
        return this.networkManager.networks().getById(id);
    }

    @Override
    public String subnetName() {
        SubResource subnetRef = this.inner().subnet();
        if (subnetRef == null) {
            return null;
        }
        return ResourceUtils.nameFromResourceId(subnetRef.id());
    }

    @Override
    public Collection<ApplicationGatewayBackend> listAssociatedApplicationGatewayBackends() {
        return com.azure.resourcemanager.network.implementation.Utils.listAssociatedApplicationGatewayBackends(
            this.parent().manager(), this.inner().applicationGatewayBackendAddressPools()
        );
    }

    @Override
    public List<LoadBalancerBackend> listAssociatedLoadBalancerBackends() {
        final List<BackendAddressPoolInner> backendRefs = this.inner().loadBalancerBackendAddressPools();
        if (backendRefs == null) {
            return Collections.unmodifiableList(new ArrayList<LoadBalancerBackend>());
        }
        final Map<String, LoadBalancer> loadBalancers = new HashMap<>();
        final List<LoadBalancerBackend> backends = new ArrayList<>();
        for (BackendAddressPoolInner backendRef : backendRefs) {
            String loadBalancerId = ResourceUtils.parentResourceIdFromResourceId(backendRef.id());
            LoadBalancer loadBalancer = loadBalancers.get(loadBalancerId.toLowerCase(Locale.ROOT));
            if (loadBalancer == null) {
                loadBalancer = this.networkManager.loadBalancers().getById(loadBalancerId);
                loadBalancers.put(loadBalancerId.toLowerCase(Locale.ROOT), loadBalancer);
            }
            String backendName = ResourceUtils.nameFromResourceId(backendRef.id());
            backends.add(loadBalancer.backends().get(backendName));
        }
        return Collections.unmodifiableList(backends);
    }

    @Override
    public List<LoadBalancerInboundNatRule> listAssociatedLoadBalancerInboundNatRules() {
        final List<InboundNatRuleInner> inboundNatPoolRefs = this.inner().loadBalancerInboundNatRules();
        if (inboundNatPoolRefs == null) {
            return Collections.unmodifiableList(new ArrayList<LoadBalancerInboundNatRule>());
        }
        final Map<String, LoadBalancer> loadBalancers = new HashMap<>();
        final List<LoadBalancerInboundNatRule> rules = new ArrayList<>();
        for (InboundNatRuleInner ref : inboundNatPoolRefs) {
            String loadBalancerId = ResourceUtils.parentResourceIdFromResourceId(ref.id());
            LoadBalancer loadBalancer = loadBalancers.get(loadBalancerId.toLowerCase(Locale.ROOT));
            if (loadBalancer == null) {
                loadBalancer = this.networkManager.loadBalancers().getById(loadBalancerId);
                loadBalancers.put(loadBalancerId.toLowerCase(Locale.ROOT), loadBalancer);
            }
            String ruleName = ResourceUtils.nameFromResourceId(ref.id());
            rules.add(loadBalancer.inboundNatRules().get(ruleName));
        }
        return Collections.unmodifiableList(rules);
    }
}
