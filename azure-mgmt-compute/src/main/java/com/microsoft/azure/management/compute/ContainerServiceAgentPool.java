/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.apigeneration.Beta.SinceVersion;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;

/**
 * A client-side representation for a container service agent pool.
 */
@Fluent
@Beta(SinceVersion.V1_1_0)
public interface ContainerServiceAgentPool extends
    ChildResource<ContainerService>,
    HasInner<ContainerServiceAgentPoolProfile> {

    /**
     * @return the number of agents (VMs) to host docker containers
     */
    int count();

    /**
     * @return size of agent VMs
     */
    ContainerServiceVMSizeTypes vmSize();

    /**
     * @return DNS prefix to be used to create the FQDN for the agent pool
     */
    String dnsLabel();

    /**
     * @return FDQN for the agent pool
     */
    String fqdn();


    // Fluent interfaces

    /**
     * The entirety of a container service agent pool definition as a part of a parent definition.
     * @param <ParentT>  the stage of the container service definition to return to after attaching this definition
     */
    interface Definition<ParentT> extends
        DefinitionStages.WithAttach<ParentT>,
        DefinitionStages.Blank<ParentT>,
            DefinitionStages.WithVMSize<ParentT>,
            DefinitionStages.WithLeafDomainLabel<ParentT> {
    }

    /**
     * Grouping of container service agent pool definition stages as a part of parent container service definition.
     */
    interface DefinitionStages {

        /** The final stage of a container service agent pool definition.
         * At this stage, any remaining optional settings can be specified, or the container service agent pool
         * can be attached to the parent container service definition.
         * @param <ParentT> the stage of the container service definition to return to after attaching this definition
         */
        interface WithAttach<ParentT> extends
            Attachable.InDefinition<ParentT> {
        }

        /**
         * The first stage of a container service agent pool definition.
         *
         * @param <ParentT>  the stage of the container service definition to return to after attaching this definition
         */
        interface Blank<ParentT> {
            /**
             * Specifies the number of agents (VMs) to host docker containers.
             * Allowed values must be in the range of 1 to 100 (inclusive).
             * @param count the count
             * @return the next stage of the definition
             */
            WithVMSize<ParentT> withVMCount(int count);
        }

        /**
         * The stage of a container service agent pool definition allowing to specify the agent VM size.
         *
         * @param <ParentT>  the stage of the container service definition to return to after attaching this definition
         */
        interface WithVMSize<ParentT> {
            /**
             * Specifies the size of the agents VMs.
             * @param vmSize the size of the VM
             * @return the next stage of the definition
             */
            WithLeafDomainLabel<ParentT> withVMSize(ContainerServiceVMSizeTypes vmSize);
        }

        /**
         * The stage of a container service agent pool definition allowing to specify the DNS label.
         *
         * @param <ParentT>  the stage of the container service definition to return to after attaching this definition
         */
        interface WithLeafDomainLabel<ParentT> {
            /**
             * Specify the DNS prefix to be used to create the FQDN for the agent pool.
             * @param dnsLabel the Dns label
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withLeafDomainLabel(String dnsLabel);
        }
    }
}
