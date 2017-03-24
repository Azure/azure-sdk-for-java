/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.CSAgentPoolProfile;
import com.microsoft.azure.management.compute.ContainerService;
import com.microsoft.azure.management.compute.ContainerServiceAgentPoolProfile;
import com.microsoft.azure.management.compute.ContainerServiceVMSizeTypes;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

/**
 * The implementation for {@link CSAgentPoolProfile} and its create and update interfaces.
 */
@LangDefinition
class CSAgentPoolProfileImpl
    extends ChildResourceImpl<ContainerServiceAgentPoolProfile,
        ContainerServiceImpl,
        ContainerService>
    implements
        CSAgentPoolProfile,
        CSAgentPoolProfile.Definition,
        CSAgentPoolProfile.Update {

    CSAgentPoolProfileImpl(ContainerServiceAgentPoolProfile inner, ContainerServiceImpl parent) {
        super(inner, parent);
    }

    @Override
    public String name() {
        return this.inner().name();
    }

    /**
     * Number of agents (VMs) to host docker containers. Allowed values must be in the range of 1 to 100 (inclusive). The default value is 1. 
     */
    @Override
    public int count() {
        return this.inner().count();
    }

    /**
     * Size of agent VMs.
     */
    @Override
    public ContainerServiceVMSizeTypes vmSize() {
        return this.inner().vmSize();
    }

    /**
     * DNS prefix to be used to create the FQDN for the agent pool.
     */
    @Override
    public String dnsLabel() {
        return this.inner().dnsPrefix();
    }

    /**
     * FDQN for the agent pool.
     */
    @Override
    public String fqdn() {
        return this.inner().fqdn();
    }

    @Override
    public CSAgentPoolProfileImpl withCount(int param0) {
        this.inner().withCount(param0);
        return this;        
    }

    @Override
    public CSAgentPoolProfileImpl withVmSize(ContainerServiceVMSizeTypes param0) {
        this.inner().withVmSize(param0);
        return this;        
    }

    @Override
    public CSAgentPoolProfileImpl withDnsLabel(String param0) {
        this.inner().withDnsPrefix(param0);
        return this;        
    }


    @Override
    public ContainerService.Definition attach() {
        this.parent().attachAgentPoolProfile(this);
        return this.parent();
    }
}
