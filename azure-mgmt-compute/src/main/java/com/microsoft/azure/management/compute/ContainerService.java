/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.apigeneration.Beta;
import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.apigeneration.Beta.SinceVersion;
import com.microsoft.azure.management.compute.implementation.ComputeManager;
import com.microsoft.azure.management.compute.implementation.ContainerServiceInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;

/**
 * An client-side representation for a container service.
 */
@Fluent
@Beta(SinceVersion.V1_1_0)
public interface ContainerService extends
        GroupableResource<ComputeManager, ContainerServiceInner>,
        Refreshable<ContainerService>,
        Updatable<ContainerService.Update> {

    /**
     * @return the master node count
     */
    int masterNodeCount();

    /**
     * @return the type of the orchestrator
     */
    ContainerServiceOchestratorTypes orchestratorType();

    /**
     * @return the master leaf domain label
     */
    String masterLeafDomainLabel();

    /**
     * @return the master FQDN
     */
    String masterFqdn();

    /**
     * @return the agent pool name
     */
    String agentPoolName();

    /**
     * @return the agent pool count
     */
    int agentPoolCount();

    /**
     * @return the agent pool leaf domain label
     */
    String agentPoolLeafDomainLabel();

    /**
     * @return the agent pool VM size
     */
    ContainerServiceVMSizeTypes agentPoolVMSize();

    /**
     * @return the agent pool FQDN
     */
    String agentPoolFqdn();

    /**
     * @return the linux root username
     */
    String linuxRootUsername();

    /**
     * @return the linux ssh key
     */
    String sshKey();

    /**
     * @return diagnostics enabled
     */
    boolean isDiagnosticsEnabled();

    /**
     * @return the service principal clientId
     */
    String servicePrincipalClientId();

    /**
     * @return the service principal secret
     */
    String servicePrincipalSecret();

    // Fluent interfaces

    /**
     * Container interface for all the definitions related to a container service.
     */
    interface Definition extends
            ContainerService.DefinitionStages.Blank,
            ContainerService.DefinitionStages.WithGroup,
            ContainerService.DefinitionStages.WithOrchestrator,
            DefinitionStages.WithMasterNodeCount,
            DefinitionStages.WithMasterLeafDomainLabel,
            DefinitionStages.WithLinux,
            DefinitionStages.WithLinuxRootUsername,
            DefinitionStages.WithLinuxSshKey,
            DefinitionStages.WithAgentPool,
            DefinitionStages.WithServicePrincipalProfile,
            DefinitionStages.WithDiagnostics,
            ContainerService.DefinitionStages.WithCreate {
    }

    /**
     * Grouping of container service definition stages.
     */
    interface DefinitionStages {
        /**
         * The first stage of a container service definition.
         */
        interface Blank extends
                GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /**
         * The stage of the container service definition allowing to specify the resource group.
         */
        interface WithGroup extends
                GroupableResource.DefinitionStages.WithGroup<WithOrchestrator> {
        }

        /**
         * The stage of the container service definition allowing to specify orchestration type.
         */
        interface WithOrchestrator {
            /**
             * Specifies the Swarm orchestration type for the container service.
             * @return the next stage of the definition
             */
            WithDiagnostics withSwarmOrchestration();

            /**
             * Specifies the DCOS orchestration type for the container service.
             * @return the next stage of the definition
             */
            WithDiagnostics withDcosOrchestration();

            /**
             * Specifies the Kubernetes orchestration type for the container service.
             * @return the next stage of the definition
             */
            WithServicePrincipalProfile withKubernetesOrchestration();
        }

        /**
         * The stage allowing properties for cluster service principals.
         */
        interface WithServicePrincipalProfile {
            /**
             * Properties for cluster service principals.
             * @param clientId The ID for the service principal.
             * @param secret The secret password associated with the service principal.
             * @return the next stage
             */
            WithLinux withServicePrincipal(String clientId, String secret);
        }

        /**
         * The stage of the container service definition allowing to specify the master node count.
         */
        interface WithMasterNodeCount {
            /**
             * Specifies the master node count.
             * @param count master profile count (1, 3, 5)
             * @return the next stage of the definition
             */
            WithMasterLeafDomainLabel withMasterNodeCount(ContainerServiceMasterProfileCount count);
        }

        /**
         * The stage of the container service definition allowing to specify the master Dns label.
         */
        interface WithMasterLeafDomainLabel {
            /**
             * Specifies the master node Dns label.
             * @param dnsLabel the Dns prefix
             * @return the next stage of the definition
             */
            WithAgentPool withMasterLeafDomainLabel(String dnsLabel);
        }

        /**
         * The stage of the container service definition allowing to specify an agent pool profile.
         */
        interface WithAgentPool {
            /**
             * Begins the definition of a agent pool profile to be attached to the container service.
             *
             * @param name the name for the agent pool profile
             * @return the stage representing configuration for the agent pool profile
             */
            ContainerServiceAgentPool.DefinitionStages.Blank<WithCreate> defineAgentPool(String name);
        }

        /**
         * The stage of the container service definition allowing the start of defining Linux specific settings.
         */
        interface WithLinux {
            /**
             * Begins the definition to specify Linux settings.
             * @return the stage representing configuration of Linux specific settings
             */
            WithLinuxRootUsername withLinux();
        }

        /**
         * The stage of the container service definition allowing to specific the Linux root username.
         */
        interface WithLinuxRootUsername {
            /**
             * Begins the definition to specify Linux root username.
             * @param rootUserName the root username
             * @return the next stage of the definition
             */
            WithLinuxSshKey withRootUsername(String rootUserName);
        }

        /**
         * The stage of the container service definition allowing to specific the Linux SSH key.
         */
        interface WithLinuxSshKey {
            /**
             * Begins the definition to specify Linux ssh key.
             * @param sshKeyData the SSH key data
             * @return the next stage of the definition
             */
            WithMasterNodeCount withSshKey(String sshKeyData);
        }

        /**
         * The stage of the container service definition allowing to specific diagnostic settings.
         */
        interface WithDiagnostics extends
                WithLinux {
            /**
             * Enable diagnostics.
             * @return the create stage of the definition
             */
            WithLinux withDiagnostics();
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for
         * the resource to be created, but also allows for any other optional settings to
         * be specified.
         */
        interface WithCreate extends
                Creatable<ContainerService>,
                Resource.DefinitionWithTags<WithCreate> {
        }
    }

    /**
     * The template for an update operation, containing all the settings that
     * can be modified.
     */
    interface Update extends
            Resource.UpdateWithTags<Update>,
            Appliable<ContainerService>,
            ContainerService.UpdateStages.WithUpdateAgentPoolCount {
    }

    /**
     * Grouping of container service update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the container service definition allowing to specific diagnostic settings.
         */
        interface WithUpdateAgentPoolCount {
            /**
             * Enables diagnostics.
             * @param agentCount the number of agents (VMs) to host docker containers.
             *                       Allowed values must be in the range of 1 to 100 (inclusive).
             *                       The default value is 1.
             * @return the next stage of the update
             */
            Update withAgentVMCount(int agentCount);
        }
    }
}
