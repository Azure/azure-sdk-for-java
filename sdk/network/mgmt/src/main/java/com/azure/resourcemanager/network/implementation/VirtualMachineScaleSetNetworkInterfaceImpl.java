// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.network.implementation;

import com.azure.core.util.logging.ClientLogger;
import com.azure.resourcemanager.network.NetworkManager;
import com.azure.resourcemanager.network.models.IpAllocationMethod;
import com.azure.resourcemanager.network.models.NetworkSecurityGroup;
import com.azure.resourcemanager.network.models.VirtualMachineScaleSetNetworkInterface;
import com.azure.resourcemanager.network.models.VirtualMachineScaleSetNicIpConfiguration;
import com.azure.resourcemanager.network.fluent.inner.NetworkInterfaceIpConfigurationInner;
import com.azure.resourcemanager.network.fluent.inner.NetworkInterfaceInner;
import com.azure.resourcemanager.resources.fluentcore.arm.ResourceUtils;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.ResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.utils.Utils;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import reactor.core.publisher.Mono;

/** The implementation for VirtualMachineScaleSetNetworkInterface. */
class VirtualMachineScaleSetNetworkInterfaceImpl
    extends ResourceImpl<
        VirtualMachineScaleSetNetworkInterface, NetworkInterfaceInner, VirtualMachineScaleSetNetworkInterfaceImpl>
    implements VirtualMachineScaleSetNetworkInterface {
    /** the network client. */
    private final NetworkManager networkManager;
    /** name of the parent scale set. */
    private final String scaleSetName;
    /** resource group this nic belongs to. */
    private final String resourceGroupName;

    private final ClientLogger logger = new ClientLogger(getClass());

    VirtualMachineScaleSetNetworkInterfaceImpl(
        String name,
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
        return Utils.toPrimitiveBoolean(this.inner().enableIpForwarding());
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
        VirtualMachineScaleSetNicIpConfiguration primaryIPConfig = this.primaryIPConfiguration();
        if (primaryIPConfig == null) {
            return null;
        }
        return primaryIPConfig.privateIpAddress();
    }

    @Override
    public IpAllocationMethod primaryPrivateIpAllocationMethod() {
        VirtualMachineScaleSetNicIpConfiguration primaryIPConfig = this.primaryIPConfiguration();
        if (primaryIPConfig == null) {
            return null;
        }
        return primaryIPConfig.privateIpAllocationMethod();
    }

    @Override
    public Map<String, VirtualMachineScaleSetNicIpConfiguration> ipConfigurations() {
        List<NetworkInterfaceIpConfigurationInner> inners = this.inner().ipConfigurations();
        if (inners == null || inners.size() == 0) {
            return Collections.unmodifiableMap(new TreeMap<String, VirtualMachineScaleSetNicIpConfiguration>());
        }
        Map<String, VirtualMachineScaleSetNicIpConfiguration> nicIPConfigurations = new TreeMap<>();
        for (NetworkInterfaceIpConfigurationInner inner : inners) {
            VirtualMachineScaleSetNicIpConfigurationImpl nicIPConfiguration =
                new VirtualMachineScaleSetNicIpConfigurationImpl(inner, this, this.networkManager);
            nicIPConfigurations.put(nicIPConfiguration.name(), nicIPConfiguration);
        }
        return Collections.unmodifiableMap(nicIPConfigurations);
    }

    @Override
    public VirtualMachineScaleSetNicIpConfiguration primaryIPConfiguration() {
        for (VirtualMachineScaleSetNicIpConfiguration ipConfiguration : this.ipConfigurations().values()) {
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
        return this.inner().networkSecurityGroup().id();
    }

    @Override
    public NetworkSecurityGroup getNetworkSecurityGroup() {
        String nsgId = this.networkSecurityGroupId();
        if (nsgId == null) {
            return null;
        }
        return this
            .manager()
            .networkSecurityGroups()
            .getByResourceGroup(ResourceUtils.groupFromResourceId(nsgId), ResourceUtils.nameFromResourceId(nsgId));
    }

    @Override
    public String virtualMachineId() {
        if (this.inner().virtualMachine() == null) {
            return null;
        }
        return this.inner().virtualMachine().id();
    }

    @Override
    public Mono<VirtualMachineScaleSetNetworkInterface> createResourceAsync() {
        // VMSS NIC is a read-only resource hence this operation is not supported.
        throw logger.logExceptionAsError(new UnsupportedOperationException());
    }

    @Override
    protected Mono<NetworkInterfaceInner> getInnerAsync() {
        return this
            .manager()
            .inner()
            .getNetworkInterfaces()
            .getVirtualMachineScaleSetNetworkInterfaceAsync(
                this.resourceGroupName,
                this.scaleSetName,
                ResourceUtils.nameFromResourceId(this.virtualMachineId()),
                this.name());
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
