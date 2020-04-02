/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.network.implementation;

import com.azure.core.management.CloudException;
import com.azure.core.management.SubResource;
import com.azure.management.network.LoadBalancer;
import com.azure.management.network.LoadBalancerBackend;
import com.azure.management.network.LoadBalancingRule;
import com.azure.management.network.NetworkInterface;
import com.azure.management.network.models.BackendAddressPoolInner;
import com.azure.management.network.models.HasNetworkInterfaces;
import com.azure.management.network.models.NetworkInterfaceIPConfigurationInner;
import com.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Implementation for LoadBalancerBackend.
 */
class LoadBalancerBackendImpl
        extends ChildResourceImpl<BackendAddressPoolInner, LoadBalancerImpl, LoadBalancer>
        implements
        LoadBalancerBackend,
        LoadBalancerBackend.Definition<LoadBalancer.DefinitionStages.WithCreate>,
        LoadBalancerBackend.UpdateDefinition<LoadBalancer.Update>,
        LoadBalancerBackend.Update {

    LoadBalancerBackendImpl(BackendAddressPoolInner inner, LoadBalancerImpl parent) {
        super(inner, parent);
    }

    // Getters

    @Override
    public Map<String, String> backendNicIPConfigurationNames() {
        // This assumes a NIC can only have one IP config associated with the backend of an LB,
        // which is correct at the time of this implementation and seems unlikely to ever change
        final Map<String, String> ipConfigNames = new TreeMap<>();
        if (this.inner().backendIPConfigurations() != null) {
            for (NetworkInterfaceIPConfigurationInner inner : this.inner().backendIPConfigurations()) {
                String nicId = ResourceUtils.parentResourceIdFromResourceId(inner.getId());
                String ipConfigName = ResourceUtils.nameFromResourceId(inner.getId());
                ipConfigNames.put(nicId, ipConfigName);
            }
        }

        return Collections.unmodifiableMap(ipConfigNames);
    }

    @Override
    public Map<String, LoadBalancingRule> loadBalancingRules() {
        final Map<String, LoadBalancingRule> rules = new TreeMap<>();
        if (this.inner().loadBalancingRules() != null) {
            for (SubResource inner : this.inner().loadBalancingRules()) {
                String name = ResourceUtils.nameFromResourceId(inner.getId());
                LoadBalancingRule rule = this.parent().loadBalancingRules().get(name);
                if (rule != null) {
                    rules.put(name, rule);
                }
            }
        }

        return Collections.unmodifiableMap(rules);
    }

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public Set<String> getVirtualMachineIds() {
        Set<String> vmIds = new HashSet<>();
        Map<String, String> nicConfigs = this.backendNicIPConfigurationNames();
        if (nicConfigs != null) {
            for (String nicId : nicConfigs.keySet()) {
                try {
                    NetworkInterface nic = this.parent().manager().networkInterfaces().getById(nicId);
                    if (nic == null || nic.virtualMachineId() == null) {
                        continue;
                    } else {
                        vmIds.add(nic.virtualMachineId());
                    }
                } catch (CloudException | IllegalArgumentException e) {
                    continue;
                }
            }
        }

        return vmIds;
    }

    // Verbs

    @Override
    public LoadBalancerImpl attach() {
        this.parent().withBackend(this);
        return this.parent();
    }

    // Withers
    @Override
    public LoadBalancerBackendImpl withExistingVirtualMachines(HasNetworkInterfaces... vms) {
        return (vms != null) ? this.withExistingVirtualMachines(Arrays.asList(vms)) : this;
    }

    @Override
    public LoadBalancerBackendImpl withExistingVirtualMachines(Collection<HasNetworkInterfaces> vms) {
        if (vms != null) {
            for (HasNetworkInterfaces vm : vms) {
                this.parent().withExistingVirtualMachine(vm, this.name());
            }
        }
        return this;
    }
}
