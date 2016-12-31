/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.VirtualMachineScaleSetNetworkInterface;
import com.microsoft.azure.management.network.VirtualMachineScaleSetNicIpConfiguration;
import com.microsoft.azure.management.network.IPAllocationMethod;
import com.microsoft.azure.management.network.IPVersion;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.LoadBalancerBackend;
import com.microsoft.azure.management.network.LoadBalancerInboundNatRule;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NicIpConfiguration;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *  Implementation for {@link NicIpConfiguration} for network interfaces associated
 *  with virtual machine scale set.
 */
@LangDefinition
class VirtualMachineScaleSetNicIpConfigurationImpl
        extends
        ChildResourceImpl<NetworkInterfaceIPConfigurationInner,
                VirtualMachineScaleSetNetworkInterfaceImpl,
                VirtualMachineScaleSetNetworkInterface>
        implements
        VirtualMachineScaleSetNicIpConfiguration {

    private final NetworkManager networkManager;

    VirtualMachineScaleSetNicIpConfigurationImpl(NetworkInterfaceIPConfigurationInner inner,
                                                           VirtualMachineScaleSetNetworkInterfaceImpl parent,
                                                           NetworkManager networkManager) {
        super(inner, parent);
        this.networkManager = networkManager;
    }

    @Override
    public String name() {
        return inner().name();
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
    public IPVersion privateIpAddressVersion() {
        return this.inner().privateIPAddressVersion();
    }

    @Override
    public String networkId() {
        SubResource subnetRef = this.inner().subnet();
        if (subnetRef == null) {
            return null;
        }
        return ResourceUtils.parentResourceIdFromResourceId(subnetRef.id());
    }

    @Override
    public Network getNetwork() {
        String id = this.networkId();
        return (id != null) ? this.networkManager.networks().getById(id) : null;
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
    public List<LoadBalancerBackend> listAssociatedLoadBalancerBackends() {
        final List<BackendAddressPoolInner> backendRefs = this.inner().loadBalancerBackendAddressPools();
        if (backendRefs == null) {
            return Collections.unmodifiableList(new ArrayList<LoadBalancerBackend>());
        }
        final Map<String, LoadBalancer> loadBalancers = new HashMap<>();
        final List<LoadBalancerBackend> backends = new ArrayList<>();
        for (BackendAddressPoolInner backendRef : backendRefs) {
            String loadBalancerId = ResourceUtils.parentResourceIdFromResourceId(backendRef.id());
            LoadBalancer loadBalancer = loadBalancers.get(loadBalancerId);
            if (loadBalancer == null) {
                loadBalancer = this.networkManager.loadBalancers().getById(loadBalancerId);
                loadBalancers.put(loadBalancerId, loadBalancer);
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
            LoadBalancer loadBalancer = loadBalancers.get(loadBalancerId);
            if (loadBalancer == null) {
                loadBalancer = this.networkManager.loadBalancers().getById(loadBalancerId);
                loadBalancers.put(loadBalancerId, loadBalancer);
            }
            String ruleName = ResourceUtils.nameFromResourceId(ref.id());
            rules.add(loadBalancer.inboundNatRules().get(ruleName));
        }
        return Collections.unmodifiableList(rules);
    }

    // Note: The inner ipConfig contains a property with name 'publicIPAddress'
    // which is valid only when the inner is explicitly created i.e. the one
    // associated with normal virtual machines. In VMSS case the inner ipConfig
    // is implicitly created for the scale set vm instances and 'publicIPAddress'
    // property is null.
    //
}