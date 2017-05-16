/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.compute.ContainerServiceAgentPool;
import com.microsoft.azure.management.compute.ContainerService;
import com.microsoft.azure.management.compute.ContainerServiceAgentPoolProfile;
import com.microsoft.azure.management.compute.ContainerServiceVMSizeTypes;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.ChildResourceImpl;

/**
 * The implementation for {@link ContainerServiceAgentPool} and its create and update interfaces.
 */
@LangDefinition
class ContainerServiceAgentPoolImpl
    extends ChildResourceImpl<ContainerServiceAgentPoolProfile,
        ContainerServiceImpl,
        ContainerService>
    implements
        ContainerServiceAgentPool,
        ContainerServiceAgentPool.Definition {

    ContainerServiceAgentPoolImpl(ContainerServiceAgentPoolProfile inner, ContainerServiceImpl parent) {
        super(inner, parent);
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
    public String dnsLabel() {
        return this.inner().dnsPrefix();
    }

    @Override
    public String fqdn() {
        return this.inner().fqdn();
    }

    @Override
    public ContainerServiceAgentPoolImpl withVMCount(int agentPoolCount) {
        if (agentPoolCount < 0 || agentPoolCount > 100) {
            throw new RuntimeException("Agent pool count  must be in the range of 1 to 100 (inclusive)");
        }

        this.inner().withCount(agentPoolCount);
        return this;        
    }

    @Override
    public ContainerServiceAgentPoolImpl withVMSize(ContainerServiceVMSizeTypes param0) {
        this.inner().withVmSize(param0);
        return this;        
    }

    @Override
    public ContainerServiceAgentPoolImpl withLeafDomainLabel(String param0) {
        this.inner().withDnsPrefix(param0);
        return this;        
    }


    @Override
    public ContainerService.Definition attach() {
        this.parent().attachAgentPoolProfile(this);
        return this.parent();
    }
}
