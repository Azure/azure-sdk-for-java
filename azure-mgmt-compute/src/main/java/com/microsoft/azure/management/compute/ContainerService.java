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
     * @return the properties of the orchestrator
     */
    ContainerServiceOrchestratorProfile orchestratorProfile();

    /**
     * @return the properties for custom clusters
     */
    ContainerServiceCustomProfile customProfile();

    /**
     * @return the properties for cluster service principals
     */
    ContainerServiceServicePrincipalProfile servicePrincipalProfile();

    /**
     * @return the properties for the master agent
     */
    ContainerServiceMasterProfile masterProfile();

    /**
     * @return current set of agent pool profiles for this container service
     */
    ContainerServiceAgentPoolProfile agentPoolProfile();

    /**
     * @return the properties for the Windows VMs
     */
    ContainerServiceWindowsProfile windowsProfile();

    /**
     * @return the properties for the Linux VMs
     */
    ContainerServiceLinuxProfile linuxProfile();

    /**
     * @return the properties for the diagnostic agent
     */
    ContainerServiceDiagnosticsProfile diagnosticsProfile();

    // Fluent interfaces

    /**
     * Container interface for all the definitions related to a container service.
     */
    interface Definition extends
            ContainerService.DefinitionStages.Blank,
            ContainerService.DefinitionStages.WithGroup,
            DefinitionStages.WithMasterNodeCount,
            DefinitionStages.WithMasterDnsLabel,
            ContainerService.DefinitionStages.WithLinuxProfile,
            ContainerService.DefinitionStages.WithLinuxProfileRootUsername,
            ContainerService.DefinitionStages.WithLinuxProfileSshKey,
            ContainerService.DefinitionStages.DefineAgentPoolProfiles,
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
                GroupableResource.DefinitionStages.WithGroup<WithMasterNodeCount> {
        }

        /**
         * The stage of the container service definition allowing to specify orchestration type.
         */
        interface WithOrchestratorProfile {
            /**
             * Specifies the Swarm orchestration type for the container service.
             * @return the next stage of the definition
             */
            WithCreate withSwarmOrchestration();

            /**
             * Specifies the DCOS orchestration type for the container service.
             * @return the next stage of the definition
             */
            WithCreate withDcosOrchestration();

            /**
             * Specifies the Kubernetes orchestration type for the container service.
             * @return the next stage of the definition
             */
            WithCreate withKubernetesOrchestration();
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
            WithMasterDnsLabel withMasterNodeCount(ContainerServiceMasterProfileCount count);
        }

        /**
         * The stage of the container service definition allowing to specify the master Dns label.
         */
        interface WithMasterDnsLabel {
            /**
             * Specifies the master node Dns label.
             * @param dnsLabel the Dns prefix
             * @return the next stage of the definition
             */
            WithLinuxProfile withMasterDnsLabel(String dnsLabel);
        }

        /**
         * The stage of the container service definition allowing to specify an agent pool profile.
         */
        interface DefineAgentPoolProfiles {
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
        interface WithLinuxProfile {
            /**
             * Begins the definition to specify Linux settings.
             * @return the stage representing configuration of Linux specific settings
             */
            WithLinuxProfileRootUsername withLinuxProfile();
        }

        /**
         * The stage of the container service definition allowing to specific the Linux root username.
         */
        interface WithLinuxProfileRootUsername {
            /**
             * Begins the definition to specify Linux root username.
             * @param rootUserName the root username
             * @return the next stage of the definition
             */
            WithLinuxProfileSshKey withRootUsername(String rootUserName);
        }

        /**
         * The stage of the container service definition allowing to specific the Linux SSH key.
         */
        interface WithLinuxProfileSshKey {
            /**
             * Begins the definition to specify Linux ssh key.
             * @param sshKeyData the SSH key data
             * @return the next stage of the definition
             */
            DefineAgentPoolProfiles withSshKey(String sshKeyData);
        }

        /**
         * The stage of the container service definition allowing to specific diagnostic settings.
         */
        interface WithDiagnosticsProfile {
            /**
             * Enable diagnostics.
             * @return the create stage of the definition
             */
            WithCreate withDiagnostics();

            /**
             * Disable diagnostics.
             * @return the create stage of the definition
             */
            WithCreate withoutDiagnostics();
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for
         * the resource to be created, but also allows for any other optional settings to
         * be specified.
         */
        interface WithCreate extends
                Creatable<ContainerService>,
                Resource.DefinitionWithTags<WithCreate>,
                ContainerService.DefinitionStages.WithOrchestratorProfile,
                ContainerService.DefinitionStages.WithDiagnosticsProfile {
        }
    }

    /**
     * The template for an update operation, containing all the settings that
     * can be modified.
     */
    interface Update extends
            Resource.UpdateWithTags<Update>,
            Appliable<ContainerService>,
            ContainerService.UpdateStages.WithUpdateAgentPoolCount,
            ContainerService.UpdateStages.WithDiagnosticsProfile {
    }

    /**
     * Grouping of container service update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the container service definition allowing to specific diagnostic settings.
         */
        interface WithDiagnosticsProfile {
            /**
             * Enables diagnostics.
             * @return the next stage of the update
             */
            Update withDiagnostics();

            /**
             * Disables diagnostics.
             * @return the next stage of the update
             */
            Update withoutDiagnostics();
        }

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
            Update withAgentCount(int agentCount);
        }
    }
}
