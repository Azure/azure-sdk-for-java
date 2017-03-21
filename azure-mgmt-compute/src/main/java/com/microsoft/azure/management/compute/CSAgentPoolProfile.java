/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;

import java.util.List;
import java.util.Map;

/**
 */
@Fluent
public interface CSAgentPoolProfile extends
    ChildResource<ContainerService>,
    HasInner<ContainerServiceAgentPoolProfile> {

    /**
     * Number of agents (VMs) to host docker containers. Allowed values must be in the range of 1 to 100 (inclusive). The default value is 1. 
     */
    int count();
    /**
     * Size of agent VMs.
     */
    ContainerServiceVMSizeTypes vmSize();
    /**
     * DNS prefix to be used to create the FQDN for the agent pool.
     */
    String dnsPrefix();
    /**
     * FDQN for the agent pool.
     */
    String fqdn();


    interface Definition<ParentT> extends
        DefinitionStages.WithAttach<ParentT>,
        DefinitionStages.Blank<ParentT>,
        DefinitionStages.WithVmSize<ParentT>,
        DefinitionStages.WithDnsPrefix<ParentT> {
    }

    interface DefinitionStages {

        interface WithAttach<ParentT> extends
            Attachable.InDefinition<ParentT> {
        }

        interface Blank<ParentT> {
            WithVmSize<ParentT> withCount(int param0);
        }

        interface WithVmSize<ParentT> {
            WithDnsPrefix<ParentT> withVmSize(ContainerServiceVMSizeTypes param0);
        }

        interface WithDnsPrefix<ParentT> {
            WithAttach<ParentT> withDnsPrefix(String param0);
        }
    }

    interface Update<ParentT> extends
        UpdateStages.WithAttach<ParentT>,
        UpdateStages.WithCount<ParentT>,
        UpdateStages.WithVmSize<ParentT>,
        UpdateStages.WithDnsPrefix<ParentT> {
    }

    interface UpdateStages {

        interface WithAttach<ParentT> extends
            Attachable.InUpdate<ParentT> {
        }

        interface WithCount<ParentT> {
            Update<ParentT> withCount(int param0);
        }

        interface WithVmSize<ParentT> {
            Update<ParentT> withVmSize(ContainerServiceVMSizeTypes param0);
        }

        interface WithDnsPrefix<ParentT> {
            WithAttach<ParentT> withDnsPrefix(String param0);
        }
    }

}
