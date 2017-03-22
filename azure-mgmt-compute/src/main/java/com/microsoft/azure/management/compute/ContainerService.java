/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.compute;

import com.microsoft.azure.management.apigeneration.Fluent;
import com.microsoft.azure.management.compute.implementation.ComputeManager;
import com.microsoft.azure.management.compute.implementation.ContainerServiceInner;
import com.microsoft.azure.management.resources.fluentcore.arm.models.GroupableResource;
import com.microsoft.azure.management.resources.fluentcore.arm.models.Resource;
import com.microsoft.azure.management.resources.fluentcore.model.Appliable;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.model.Refreshable;
import com.microsoft.azure.management.resources.fluentcore.model.Updatable;

import java.util.List;
import java.util.Map;

/**
 */
@Fluent
public interface ContainerService extends
        GroupableResource<ComputeManager, ContainerServiceInner>,
        Refreshable<ContainerService>,
        Updatable<ContainerService.Update> {

    /**
     * Properties of the orchestrator.
     */
    ContainerServiceOrchestratorProfile orchestratorProfile();
    /**
     * Properties for custom clusters.
     */
    ContainerServiceCustomProfile customProfile();
    /**
     * Properties for cluster service principals.
     */
    ContainerServiceServicePrincipalProfile servicePrincipalProfile();
    /**
     * Properties of master agents.
     */
    ContainerServiceMasterProfile masterProfile();
    /**
     * Properties of the agent pool.
     */
    Map<String, ContainerServiceAgentPoolProfile> agentPoolProfiles();
    /**
     * Properties of Windows VMs.
     */
    ContainerServiceWindowsProfile windowsProfile();
    /**
     * Properties of Linux VMs.
     */
    ContainerServiceLinuxProfile linuxProfile();
    /**
     * Properties of the diagnostic agent.
     */
    ContainerServiceDiagnosticsProfile diagnosticsProfile();


    interface DoneWith<T> {
        /**
         * Properties of Windows VMs.
         *
         * @return the next stage
         */
        T done();
    }

    interface Definition extends
            ContainerService.DefinitionStages.Blank,
            ContainerService.DefinitionStages.WithGroup,
            ContainerService.DefinitionStages.WithMasterProfile /*required*/ ,
            ContainerService.DefinitionStages.WithLinuxProfile /*required*/ ,
            ContainerService.DefinitionStages.WithLinuxProfileRootUsername,
            ContainerService.DefinitionStages.WithLinuxProfileSshKey,
            ContainerService.DoneWith,
            ContainerService.DefinitionStages.DefineAgentPoolProfiles /*required*/ ,
            ContainerService.DefinitionStages.WithWindowsProfile,
            ContainerService.DefinitionStages.WithWindowsAdminUsername,
            ContainerService.DefinitionStages.WithWindowsAdminPassword,
            ContainerService.DefinitionStages.WithCreate {
    }

    interface DefinitionStages {

        interface WithOrchestratorProfile {
            WithCreate withSwarmOrchestration();
            WithCreate withDCOSOrchestration();
            WithCreate withKubernetesOrchestration();
        }

        interface WithServicePrincipalProfile {
            /**
             * Properties for cluster service principals.
             *
             * @return the next stage
             */
            WithCreate withServicePrincipalProfile(String clientId, String secret);
        }

        interface WithMasterProfile {
            /**
             * Properties of master agents.
             *
             * @return the next stage
             */
            WithLinuxProfile withMasterProfile(ContainerServiceMasterProfileCount count,String dnsPrefix);
        }

        interface DefineAgentPoolProfiles {
            /**
             * Properties of the agent pool.
             *
             * @param name
             * @return the next stage
             */
            CSAgentPoolProfile.DefinitionStages.Blank<WithCreate> defineContainerServiceAgentPoolProfile(String name);
        }

        interface WithWindowsProfile {
            /**
             * Properties of Windows VMs.
             *
             * @return the next stage
             */
            WithWindowsAdminUsername withWindowsProfile();
        }

        interface WithWindowsAdminUsername {
            /**
             * Properties of Windows VMs.
             *
             * @return the next stage
             */
            WithWindowsAdminPassword withAdminUserName(String adminUsername);
        }

        interface WithWindowsAdminPassword {
            /**
             * Properties of Windows VMs.
             *
             * @return the next stage
             */
            DoneWith<WithCreate> withAdminPassword(String adminPassword);
        }

        interface WithLinuxProfile {
            /**
             * Properties of Linux VMs.
             *
             * @return the next stage
             */
            WithLinuxProfileRootUsername withLinuxProfile();
        }

        interface WithLinuxProfileRootUsername {
            WithLinuxProfileSshKey withRootUsername(String rootUserName);
        }

        interface WithLinuxProfileSshKey {
            DoneWith<DefineAgentPoolProfiles> withSshKey(String sshKeyData);
        }

        interface WithDiagnosticsProfile {
            /**
             * Properties of the diagnostic agent.
             *
             * @return the next stage
             */
            WithCreate withDiagnostics();
            WithCreate withoutDiagnostics();
        }

        interface WithCreate extends
                Creatable<ContainerService>,
                ContainerService.DefinitionStages.WithOrchestratorProfile,
                ContainerService.DefinitionStages.WithServicePrincipalProfile,
                ContainerService.DefinitionStages.WithWindowsProfile,
                ContainerService.DefinitionStages.DefineAgentPoolProfiles,
                ContainerService.DefinitionStages.WithDiagnosticsProfile {
        }

        interface Blank extends
                GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        interface WithGroup extends
                GroupableResource.DefinitionStages.WithGroup<WithMasterProfile> {
        }
    }

    interface Update extends
            Resource.UpdateWithTags<Update>,
            Appliable<ContainerService>,
            ContainerService.UpdateStages.WithOrchestratorProfile,
            ContainerService.UpdateStages.WithServicePrincipalProfile,
            ContainerService.UpdateStages.WithMasterProfile,
            ContainerService.UpdateStages.DefineAgentPoolProfiles,
            ContainerService.UpdateStages.WithWindowsProfile,
            ContainerService.UpdateStages.WithWindowsAdminUsername,
            ContainerService.UpdateStages.WithWindowsAdminPassword,
            ContainerService.UpdateStages.WithLinuxProfile,
            ContainerService.UpdateStages.WithLinuxProfileRootUsername,
            ContainerService.UpdateStages.WithLinuxProfileSshKey,
            ContainerService.UpdateStages.WithDiagnosticsProfile,
            ContainerService.UpdateStages.RemoveProfiles {
    }

    interface UpdateStages {

        interface WithOrchestratorProfile {
            Update withSwarmOrchestration();
            Update withDCOSOrchestration();
            Update withKubernetesOrchestration();
        }

        interface WithServicePrincipalProfile {
            /**
             * Properties for cluster service principals.
             *
             * @return the next stage
             */
            Update withServicePrincipalProfile(String clientId, String secret);
        }

        interface WithMasterProfile {
            /**
             * Properties of master agents.
             *
             * @return the next stage
             */
            WithLinuxProfile withMasterProfile(ContainerServiceMasterProfileCount count, String dnsPrefix);
        }

        interface DefineAgentPoolProfiles {
            /**
             * Properties of the agent pool.
             *
             * @param name
             * @return the next stage
             */
            CSAgentPoolProfile.DefinitionStages.Blank<Update> defineContainerServiceAgentPoolProfile(String name);
            CSAgentPoolProfile.Update updateContainerServiceAgentPoolProfile(String name);
        }

        interface WithWindowsProfile {
            /**
             * Properties of Windows VMs.
             *
             * @return the next stage
             */
            WithWindowsAdminUsername withWindowsProfile();
        }

        interface WithWindowsAdminUsername {
            /**
             * Properties of Windows VMs.
             *
             * @return the next stage
             */
            WithWindowsAdminPassword withAdminUserName(String adminUsername);
        }

        interface WithWindowsAdminPassword {
            /**
             * Properties of Windows VMs.
             *
             * @return the next stage
             */
            DoneWith<Update> withAdminPassword(String adminPassword);
        }

        interface WithLinuxProfile {
            /**
             * Properties of Linux VMs.
             *
             * @return the next stage
             */
            WithLinuxProfileRootUsername withLinuxProfile();
        }

        interface WithLinuxProfileRootUsername {
            WithLinuxProfileSshKey withRootUsername(String rootUserName);
        }

        interface WithLinuxProfileSshKey {
            DoneWith<Update> withSshKey(String sshKeyData);
        }

        interface WithDiagnosticsProfile {
            /**
             * Properties of the diagnostic agent.
             *
             * @return the next stage
             */
            Update withDiagnostics();
            Update withoutDiagnostics();
        }

        interface RemoveProfiles {
            Update removeAgentPoolProfile(CSAgentPoolProfile agentPoolProfile);
            Update removeWindowsProfile();
            Update removeOrchestration();
            Update removeServicePrincipalProfile();
        }
    }
}
