/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.network.implementation;

import com.azure.management.network.IPAllocationMethod;
import com.azure.management.network.NetworkSecurityGroup;
import com.azure.management.network.VirtualMachineScaleSetNetworkInterface;
import com.azure.management.network.VirtualMachineScaleSetNicIPConfiguration;
import com.azure.management.network.models.NetworkInterfaceIPConfigurationInner;
import com.azure.management.network.models.NetworkInterfaceInner;
import com.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.azure.management.resources.fluentcore.arm.models.implementation.ResourceImpl;
import com.azure.management.resources.fluentcore.utils.Utils;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * The implementation for VirtualMachineScaleSetNetworkInterface.
 */
class VirtualMachineScaleSetNetworkInterfaceImpl
        extends
        ResourceImpl<VirtualMachineScaleSetNetworkInterface,
                NetworkInterfaceInner,
                VirtualMachineScaleSetNetworkInterfaceImpl>
        implements
        VirtualMachineScaleSetNetworkInterface {
    /**
     * the network client.
     */
    private final NetworkManager networkManager;
    /**
     * name of the parent scale set.
     */
    private final String scaleSetName;
    /**
     * resource group this nic belongs to.
     */
    private final String resourceGroupName;

    VirtualMachineScaleSetNetworkInterfaceImpl(String name,
                                               String scaleSetName,
                                               String resourceGroupName,
                                               NetworkInterfaceInner innerObject,
                                               NetworkManager networkManager) {
        super(name, innerObject);
        this.scaleSetName = scaleSetName;
        this.resourceGroupName = resourceGroupName;
        this.networkManager = networkManager;
    }

    @Override
    public boolean isIPForwardingEnabled() {
        return Utils.toPrimitiveBoolean(this.inner().enableIPForwarding());
    }

    @Override
    public String macAddress() {
        return this.inner().macAddress();
    }

    @Override
    public String internalDnsNameLabel() {
        if (this.inner().dnsSettings() == null) {
            return null;
        }
        return this.inner().dnsSettings().internalDnsNameLabel();
    }

    @Override
    public String internalFqdn() {
        if (this.inner().dnsSettings() == null) {
            return null;
        }
        return this.inner().dnsSettings().internalFqdn();
    }

    @Override
    public String internalDomainNameSuffix() {
        if (this.inner().dnsSettings() == null) {
            return null;
        }
        return this.inner().dnsSettings().internalDomainNameSuffix();
    }

    @Override
    public List<String> dnsServers() {
        if (this.inner().dnsSettings() == null || this.inner().dnsSettings().dnsServers() == null) {
            return Collections.unmodifiableList(new ArrayList<String>());
        }
        return Collections.unmodifiableList(this.inner().dnsSettings().dnsServers());
    }

    @Override
    public List<String> appliedDnsServers() {
        List<String> dnsServers = new ArrayList<>();
        if (this.inner().dnsSettings() == null || this.inner().dnsSettings().appliedDnsServers() == null) {
            return Collections.unmodifiableList(dnsServers);
        }
        return Collections.unmodifiableList(this.inner().dnsSettings().appliedDnsServers());
    }

    @Override
    public String primaryPrivateIP() {
        VirtualMachineScaleSetNicIPConfiguration primaryIPConfig = this.primaryIPConfiguration();
        if (primaryIPConfig == null) {
            return null;
        }
        return primaryIPConfig.privateIPAddress();
    }

    @Override
    public IPAllocationMethod primaryPrivateIPAllocationMethod() {
        VirtualMachineScaleSetNicIPConfiguration primaryIPConfig = this.primaryIPConfiguration();
        if (primaryIPConfig == null) {
            return null;
        }
        return primaryIPConfig.privateIPAllocationMethod();
    }

    @Override
    public Map<String, VirtualMachineScaleSetNicIPConfiguration> ipConfigurations() {
        List<NetworkInterfaceIPConfigurationInner> inners = this.inner().ipConfigurations();
        if (inners == null || inners.size() == 0) {
            return Collections.unmodifiableMap(new TreeMap<String, VirtualMachineScaleSetNicIPConfiguration>());
        }
        Map<String, VirtualMachineScaleSetNicIPConfiguration> nicIPConfigurations = new TreeMap<>();
        for (NetworkInterfaceIPConfigurationInner inner : inners) {
            VirtualMachineScaleSetNicIPConfigurationImpl nicIPConfiguration = new VirtualMachineScaleSetNicIPConfigurationImpl(inner, this, this.networkManager);
            nicIPConfigurations.put(nicIPConfiguration.name(), nicIPConfiguration);
        }
        return Collections.unmodifiableMap(nicIPConfigurations);
    }

    @Override
    public VirtualMachineScaleSetNicIPConfiguration primaryIPConfiguration() {
        for (VirtualMachineScaleSetNicIPConfiguration ipConfiguration : this.ipConfigurations().values()) {
            if (ipConfiguration.isPrimary()) {
                return ipConfiguration;
            }
        }
        return null;
    }

    @Override
    public String networkSecurityGroupId() {
        if (this.inner().networkSecurityGroup() == null) {
            return null;
        }
        return this.inner().networkSecurityGroup().getId();
    }

    @Override
    public NetworkSecurityGroup getNetworkSecurityGroup() {
        String nsgId = this.networkSecurityGroupId();
        if (nsgId == null) {
            return null;
        }
        return this.manager()
                .networkSecurityGroups()
                .getByResourceGroup(ResourceUtils.groupFromResourceId(nsgId),
                        ResourceUtils.nameFromResourceId(nsgId));
    }

    @Override
    public String virtualMachineId() {
        if (this.inner().virtualMachine() == null) {
            return null;
        }
        return this.inner().virtualMachine().getId();
    }

    @Override
    public Mono<VirtualMachineScaleSetNetworkInterface> createResourceAsync() {
        // VMSS NIC is a read-only resource hence this operation is not supported.
        throw new UnsupportedOperationException();
    }

    @Override
    protected Mono<NetworkInterfaceInner> getInnerAsync() {
        // FIXME: parameter - expand
        return this.manager().inner().networkInterfaces().getVirtualMachineScaleSetNetworkInterfaceAsync(
                this.resourceGroupName,
                this.scaleSetName,
                ResourceUtils.nameFromResourceId(this.virtualMachineId()),
                this.name(), null);
    }

    @Override
    public NetworkManager manager() {
        return this.networkManager;
    }

    @Override
    public boolean isAcceleratedNetworkingEnabled() {
        return Utils.toPrimitiveBoolean(this.inner().enableAcceleratedNetworking());
    }
}