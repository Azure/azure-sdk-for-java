/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.azure.management.containerservice;


import com.azure.core.annotation.Fluent;
import com.azure.management.containerservice.implementation.ContainerServiceManager;
import com.azure.management.containerservice.models.ContainerServiceInner;
import com.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.azure.management.resources.fluentcore.arm.models.Resource;
import com.azure.management.resources.fluentcore.model.Appliable;
import com.azure.management.resources.fluentcore.model.Creatable;
import com.azure.management.resources.fluentcore.model.Refreshable;
import com.azure.management.resources.fluentcore.model.Updatable;

import java.util.Map;

/**
 * A client-side representation for a container service.
 */
@Fluent
public interface ContainerService extends
        GroupableResource<ContainerServiceManager, ContainerServiceInner>,
        Refreshable<ContainerService>,
        Updatable<ContainerService.Update>,
    OrchestratorServiceBase {

    /**
     * @return the master node count
     */
    int masterNodeCount();

    /**
     * @return the type of the orchestrator
     */
    ContainerServiceOrchestratorTypes orchestratorType();

    /**
     * @return the master DNS prefix which was specified at creation time
     */
    String masterDnsPrefix();

    /**
     * @return the master FQDN
     */
    String masterFqdn();

    /**
     * @return the agent pools map
     */
    Map<String, ContainerServiceAgentPool> agentPools();

    /**
     * @return the Linux root username
     */
    String linuxRootUsername();

    /**
     * @return the Linux SSH key
     */
    String sshKey();

    /**
     * @return true if diagnostics are enabled
     */
    boolean isDiagnosticsEnabled();

    /**
     * @return the service principal client ID
     */
    String servicePrincipalClientId();

    /**
     * @return the service principal secret
     */
    String servicePrincipalSecret();

    /**
     * @return OS disk size in GB set for every machine in the master pool
     */
    int masterOSDiskSizeInGB();

    /**
     * @return the storage kind set for every machine in the master pool
     */
    ContainerServiceStorageProfileTypes masterStorageProfile();

    /**
     * @return the name of the subnet used by every machine in the master pool
     */
    String masterSubnetName();

    /**
     * @return the ID of the virtual network used by every machine in the master and agent pools
     */
    String networkId();


    // Fluent interfaces

    /**
     * Container interface for all the definitions related to a container service.
     */
    interface Definition extends
            ContainerService.DefinitionStages.Blank,
            ContainerService.DefinitionStages.WithGroup,
            ContainerService.DefinitionStages.WithOrchestrator,
            DefinitionStages.WithMasterNodeCount,
            DefinitionStages.WithLinux,
            DefinitionStages.WithLinuxRootUsername,
            DefinitionStages.WithLinuxSshKey,
            DefinitionStages.WithAgentPool,
            DefinitionStages.WithServicePrincipalProfile,
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
                DefinitionWithRegion<WithGroup> {
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
            WithLinux withSwarmOrchestration();

            /**
             * Specifies the DCOS orchestration type for the container service.
             * @return the next stage of the definition
             */
            WithLinux withDcosOrchestration();

            /**
             * Specifies the Kubernetes orchestration type for the container service.
             * @return the next stage of the definition
             */
            WithServicePrincipalProfile withKubernetesOrchestration();
        }

        /**
         * The stage allowing properties for cluster service principals to be specified.
         */
        interface WithServicePrincipalProfile {
            /**
             * Properties for cluster service principals.
             * @param clientId the ID for the service principal
             * @param secret the secret password associated with the service principal
             * @return the next stage
             */
            WithLinux withServicePrincipal(String clientId, String secret);
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
         * The stage of the container service definition allowing to specify the master node count.
         */
        interface WithMasterNodeCount {
            /**
             * Specifies the master node count.
             * @param count master profile count (1, 3, 5)
             * @return the next stage of the definition
             */
            WithAgentPool withMasterNodeCount(ContainerServiceMasterProfileCount count);
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
         * The stage of the container service definition allowing to specify the master DNS prefix label.
         */
        interface WithMasterDnsPrefix {
            /**
             * Specifies the DNS prefix to be used to create the FQDN for the master pool.
             *
             * @param dnsPrefix the DNS prefix to be used to create the FQDN for the master pool
             * @return the next stage of the definition
             */
            WithCreate withMasterDnsPrefix(String dnsPrefix);
        }

        /**
         * The stage of the container service definition allowing to enable diagnostics.
         */
        interface WithDiagnostics {
            /**
             * Enables diagnostics.
             * @return the next stage of the definition
             */
            WithCreate withDiagnostics();
        }

        /**
         * The stage of the container service definition allowing to specify the master VM size.
         */
        interface WithMasterVMSize {
            /**
             * Specifies the size of the master VMs, default set to "Standard_D2_v2".
             * @param vmSize the size of the VM
             * @return the next stage of the definition
             */
            WithCreate withMasterVMSize(ContainerServiceVMSizeTypes vmSize);
        }

        /**
         * The stage of a container service definition allowing to specify the master's virtual machine storage kind.
         */
        interface WithMasterStorageProfile {
            /**
             * Specifies the storage kind to be used for every machine in master pool.
             *
             * @param storageProfile the storage kind to be used for every machine in the master pool
             * @return the next stage of the definition
             */
            WithCreate withMasterStorageProfile(ContainerServiceStorageProfileTypes storageProfile);
        }

        /**
         * The stage of a container service definition allowing to specify the master pool OS disk size.
         *
         */
        interface WithMasterOSDiskSize {
            /**
             * OS Disk Size in GB to be used for every machine in the master pool.
             *
             * If you specify 0, the default osDisk size will be used according to the vmSize specified.
             * @param osDiskSizeInGB OS Disk Size in GB to be used for every machine in the master pool
             * @return the next stage of the definition
             */
            WithCreate withMasterOSDiskSizeInGB(int osDiskSizeInGB);
        }

        /**
         * The stage of the container service definition allowing to specify the virtual network and subnet for the machines.
         */
        interface WithSubnet {
            /**
             * Specifies the virtual network and subnet for the virtual machines in the master and agent pools.
             *
             * @param networkId the network ID to be used by the machines
             * @param subnetName the name of the subnet
             * @return the next stage of the definition
             */
            WithCreate withSubnet(String networkId, String subnetName);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created,
         *   but also allows for any other optional settings to be specified.
         */
        interface WithCreate extends
            WithMasterDnsPrefix,
            WithDiagnostics,
            WithMasterVMSize,
            WithMasterStorageProfile,
            WithMasterOSDiskSize,
            WithSubnet,
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
            ContainerService.UpdateStages.WithUpdateAgentPoolCount,
            ContainerService.UpdateStages.WithDiagnostics {
    }

    /**
     * Grouping of container service update stages.
     */
    interface UpdateStages {
        /**
         * The stage of the container service update allowing to specify the number of agents in the specified pool.
         */
        interface WithUpdateAgentPoolCount {
            /**
             * Updates the agent pool virtual machine count.
             *
             * @param agentCount the number of agents (virtual machines) to host docker containers.
             * @return the next stage of the update
             */
            Update withAgentVirtualMachineCount(int agentCount);
        }

        /**
         * The stage of the container service update allowing to enable or disable diagnostics.
         */
        interface WithDiagnostics {
            /**
             * Enables diagnostics.
             * @return the next stage of the definition
             */
            Update withDiagnostics();

            /**
             * Disables diagnostics.
             * @return the next stage of the definition
             */
            Update withoutDiagnostics();
        }
    }
}
