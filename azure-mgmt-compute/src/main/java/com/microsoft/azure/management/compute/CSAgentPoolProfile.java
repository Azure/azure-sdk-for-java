/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.resources.fluentcore.arm.models.ChildResource;
import com.microsoft.azure.management.resources.fluentcore.model.Attachable;
import com.microsoft.azure.management.resources.fluentcore.model.HasInner;

/**
 * An client-side representation for a container service agent pool.
 */
@Fluent
public interface CSAgentPoolProfile extends
    ChildResource<ContainerService>,
    HasInner<ContainerServiceAgentPoolProfile> {

    /**
     * @return the number of agents (VMs) to host docker containers.
     * Allowed values must be in the range of 1 to 100 (inclusive).
     * The default value is 1.
     */
    int count();

    /**
     * @return size of agent VMs.
     */
    ContainerServiceVMSizeTypes vmSize();

    /**
     * @return DNS prefix to be used to create the FQDN for the agent pool.
     */
    String dnsLabel();

    /**
     * @return FDQN for the agent pool.
     */
    String fqdn();


    // Fluent interfaces

    /**
     * The entirety of a container service agent pool definition as a part of a parent definition.
     *
     * @param <ParentT> the return type of the final {@link Attachable#attach()}
     */
    interface Definition<ParentT> extends
        DefinitionStages.WithAttach<ParentT>,
        DefinitionStages.Blank<ParentT>,
        DefinitionStages.WithVmSize<ParentT>,
        DefinitionStages.WithDnsLabel<ParentT> {
    }

    /**
     * Grouping of container service agent pool definition stages as a part of parent container service definition.
     */
    interface DefinitionStages {

        /** The final stage of a container service agent pool definition.
         * <p>
         * At this stage, any remaining optional settings can be specified, or the container service agent pool definition
         * can be attached to the parent definition using {@link CSAgentPoolProfile.DefinitionStages.WithAttach#attach()}.
         * @param <ParentT> the return type of {@link CSAgentPoolProfile.DefinitionStages.WithAttach#attach()}
         */
        interface WithAttach<ParentT> extends
            Attachable.InDefinition<ParentT> {
        }

        /**
         * The first stage of a container service agent pool definition.
         *
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface Blank<ParentT> {
            /**
             * Specifies the number of agents (VMs) to host docker containers.
             * Allowed values must be in the range of 1 to 100 (inclusive).
             * The default value is 1.
             * @param count the count
             * @return the next stage of the definition
             */
            WithVmSize<ParentT> withCount(int count);
        }

        /**
         * The stage of a container service agent pool definition allowing to specify the agent VM size.
         *
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface WithVmSize<ParentT> {
            /**
             * Specify the size of the agents VMs.
             * @param vmSize the size of the VM.
             * @return the next stage of the definition
             */
            WithDnsLabel<ParentT> withVmSize(ContainerServiceVMSizeTypes vmSize);
        }

        /**
         * The stage of a container service agent pool definition allowing to specify the DNS label.
         *
         * @param <ParentT> the return type of the final {@link WithAttach#attach()}
         */
        interface WithDnsLabel<ParentT> {
            /**
             * Specify the DNS prefix to be used to create the FQDN for the agent pool.
             * @param dnsLabel the Dns label
             * @return the next stage of the definition
             */
            WithAttach<ParentT> withDnsLabel(String dnsLabel);
        }
    }
}
