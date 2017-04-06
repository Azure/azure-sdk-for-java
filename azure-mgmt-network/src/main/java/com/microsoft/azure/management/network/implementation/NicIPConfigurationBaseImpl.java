/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.network.implementation;

import com.microsoft.azure.SubResource;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.network.IPAllocationMethod;
import com.microsoft.azure.management.network.IPVersion;
import com.microsoft.azure.management.network.LoadBalancer;
import com.microsoft.azure.management.network.LoadBalancerBackend;
import com.microsoft.azure.management.network.LoadBalancerInboundNatRule;
import com.microsoft.azure.management.network.Network;
import com.microsoft.azure.management.network.NicIPConfigurationBase;
import com.microsoft.azure.management.network.model.HasPrivateIPAddress;
import com.microsoft.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.microsoft.azure.management.resources.fluentcore.arm.models.HasSubnet;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Base class implementation for various network interface ip configurations.
 *
 * @param <ParentImplT> parent implementation
 * @param <IParentT> parent interface
 */
@LangDefinition
abstract class NicIPConfigurationBaseImpl<ParentImplT extends IParentT, IParentT>
    extends
        ChildResourceImpl<NetworkInterfaceIPConfigurationInner, ParentImplT, IParentT>
    implements
        NicIPConfigurationBase, HasSubnet, HasPrivateIPAddress {
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

    public String privateIPAddress() {
        return this.inner().privateIPAddress();
    }

    public IPAllocationMethod privateIPAllocationMethod() {
        return this.inner().privateIPAllocationMethod();
    }

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

    public String subnetName() {
        SubResource subnetRef = this.inner().subnet();
        if (subnetRef == null) {
            return null;
        }
        return ResourceUtils.nameFromResourceId(subnetRef.id());
    }

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
}
