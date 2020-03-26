/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.containerservice.implementation;

import com.azure.management.containerservice.ContainerService;
import com.azure.management.containerservice.ContainerServiceAgentPool;
import com.azure.management.containerservice.ContainerServiceAgentPoolProfile;
import com.azure.management.containerservice.ContainerServiceStorageProfileTypes;
import com.azure.management.containerservice.ContainerServiceVMSizeTypes;
import com.azure.management.containerservice.OSType;
import com.azure.management.containerservice.OrchestratorServiceBase;
import com.azure.management.resources.fluentcore.arm.ResourceUtils;
import com.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

import java.util.ArrayList;
import java.util.List;

/**
 * The implementation for ContainerServiceAgentPool and its create and update interfaces.
 */
class ContainerServiceAgentPoolImpl extends
        ChildResourceImpl<ContainerServiceAgentPoolProfile,
                    ContainerServiceImpl,
                OrchestratorServiceBase>
    implements
        ContainerServiceAgentPool,
        ContainerServiceAgentPool.Definition {

    private String subnetName;

    ContainerServiceAgentPoolImpl(ContainerServiceAgentPoolProfile inner, ContainerServiceImpl parent) {
        super(inner, parent);
        String subnetId = (inner != null) ? this.inner().vnetSubnetID() : null;
        this.subnetName = ResourceUtils.nameFromResourceId(subnetId);
    }

    @Override
    public String name() {
        return this.inner().name();
    }

    @Override
    public int count() {
        return this.inner().count();
    }

    @Override
    public ContainerServiceVMSizeTypes vmSize() {
        return this.inner().vmSize();
    }

    @Override
    public String dnsPrefix() {
        return this.inner().dnsPrefix();
    }

    @Override
    public int osDiskSizeInGB() {
        return this.inner().osDiskSizeGB();
    }

    @Override
    public int[] ports() {
        List<Integer> portsList = this.inner().ports();
        if (portsList != null && portsList.size() > 0) {
            int[] ports = new int[portsList.size()];
            for (int i = 0; i < ports.length; i++) {
                ports[i] = portsList.get(i);
            }
            return ports;
        } else {
            return new int[0];
        }
    }

    @Override
    public OSType osType() {
        return this.inner().osType();
    }

    @Override
    public ContainerServiceStorageProfileTypes storageProfile() {
        return this.inner().storageProfile();
    }

    @Override
    public String subnetName() {
        if (this.subnetName != null) {
            return this.subnetName;
        } else {
            return ResourceUtils.nameFromResourceId(this.inner().vnetSubnetID());
        }
    }

    @Override
    public String networkId() {
        String subnetId = (this.inner() != null) ? this.inner().vnetSubnetID() : null;
        return (subnetId != null) ? ResourceUtils.parentResourceIdFromResourceId(subnetId) : null;
    }

    @Override
    public String fqdn() {
        return this.inner().fqdn();
    }

    @Override
    public ContainerServiceAgentPoolImpl withVirtualMachineCount(int agentPoolCount) {
        this.inner().withCount(agentPoolCount);
        return this;
    }

    @Override
    public ContainerServiceAgentPoolImpl withVirtualMachineSize(ContainerServiceVMSizeTypes param0) {
        this.inner().withVmSize(param0);
        return this;        
    }

    @Override
    public ContainerServiceAgentPoolImpl withDnsPrefix(String param0) {
        this.inner().withDnsPrefix(param0);
        return this;        
    }

    @Override
    public ContainerServiceAgentPoolImpl withPorts(int... ports) {
        if (ports != null && ports.length > 0) {
            this.inner().withPorts(new ArrayList<Integer>());
            for (int port : ports) {
                this.inner().ports().add(port);
            }
        }
        return this;
    }

    @Override
    public ContainerServiceAgentPoolImpl withOSType(OSType osType) {
        this.inner().withOsType(osType);
        return this;
    }

    @Override
    public ContainerServiceAgentPoolImpl withOSDiskSizeInGB(int osDiskSizeInGB) {
        this.inner().withOsDiskSizeGB(osDiskSizeInGB);
        return this;
    }

    @Override
    public ContainerServiceAgentPoolImpl withStorageProfile(ContainerServiceStorageProfileTypes storageProfile) {
        this.inner().withStorageProfile(storageProfile);
        return this;
    }

    @Override
    public ContainerServiceAgentPoolImpl withVirtualNetwork(String virtualNetworkId, String subnetName) {
        String vnetSubnetId = virtualNetworkId + "/subnets/" + subnetName;
        this.subnetName = subnetName;
        this.inner().withVnetSubnetID(vnetSubnetId);
        return this;
    }

    @Override
    public ContainerService.Definition attach() {
        this.parent().inner().agentPoolProfiles().add(this.inner());
        return this.parent();
    }

}
