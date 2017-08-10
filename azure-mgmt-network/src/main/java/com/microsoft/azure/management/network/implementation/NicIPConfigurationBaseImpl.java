/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.ApplicationGatewayBackend;
import com.microsoft.azure.management.network.IPAllocationMethod;
import com.microsoft.azure.management.network.IPVersion;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.LoadBalancerBackend;
import com.microsoft.azure.management.network.LoadBalancerInboundNatRule;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NetworkSecurityGroup;
import com.microsoft.azure.management.network.NicIPConfigurationBase;
import com.microsoft.azure.management.network.Subnet;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasManager;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class implementation for various network interface IP configurations.
 *
 * @param <ParentImplT> parent implementation
 * @param <ParentT> parent interface
 */
@LangDefinition
abstract class NicIPConfigurationBaseImpl<ParentImplT extends ParentT, ParentT extends HasManager<NetworkManager>>
    extends
        ChildResourceImpl<NetworkInterfaceIPConfigurationInner, ParentImplT, ParentT>
    implements
        NicIPConfigurationBase {
    /**
     * the network client.
     */
    private final NetworkManager networkManager;

    protected NicIPConfigurationBaseImpl(NetworkInterfaceIPConfigurationInner inner,
                                         ParentImplT parent,
                                         NetworkManager networkManager) {
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
    public String privateIPAddress() {
        return this.inner().privateIPAddress();
    }

    @Override
    public IPAllocationMethod privateIPAllocationMethod() {
        return this.inner().privateIPAllocationMethod();
    }

    @Override
    public IPVersion privateIPAddressVersion() {
        return this.inner().privateIPAddressVersion();
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
        return this.parent().manager().listAssociatedApplicationGatewayBackends(this.inner().applicationGatewayBackendAddressPools());
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
            LoadBalancer loadBalancer = loadBalancers.get(loadBalancerId.toLowerCase());
            if (loadBalancer == null) {
                loadBalancer = this.networkManager.loadBalancers().getById(loadBalancerId);
                loadBalancers.put(loadBalancerId.toLowerCase(), loadBalancer);
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
            LoadBalancer loadBalancer = loadBalancers.get(loadBalancerId.toLowerCase());
            if (loadBalancer == null) {
                loadBalancer = this.networkManager.loadBalancers().getById(loadBalancerId);
                loadBalancers.put(loadBalancerId.toLowerCase(), loadBalancer);
            }
            String ruleName = ResourceUtils.nameFromResourceId(ref.id());
            rules.add(loadBalancer.inboundNatRules().get(ruleName));
        }
        return Collections.unmodifiableList(rules);
    }
}
