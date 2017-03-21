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


    interface Definition extends
            ContainerService.DefinitionStages.Blank,
            ContainerService.DefinitionStages.WithGroup,
            ContainerService.DefinitionStages.WithMasterProfile /*required*/ ,
            ContainerService.DefinitionStages.WithLinuxProfile /*required*/ ,
            ContainerService.DefinitionStages.DefineAgentPoolProfiles /*required*/ ,
            ContainerService.DefinitionStages.WithCreate {
    }

    interface DefinitionStages {

        interface WithOrchestratorProfile {
            /**
             * Properties of the orchestrator.
             *
             * @return the next stage
             */
            Definition withOrchestratorProfile(ContainerServiceOchestratorTypes orchestratorType);
        }

        interface WithCustomProfile {
            /**
             * Properties for custom clusters.
             *
             * @return the next stage
             */
            Definition withCustomProfile(String orchestrator);
        }

        interface WithServicePrincipalProfile {
            /**
             * Properties for cluster service principals.
             *
             * @return the next stage
             */
            Definition withServicePrincipalProfile(String clientId,String secret);
        }

        interface WithMasterProfile {
            /**
             * Properties of master agents.
             *
             * @return the next stage
             */
            WithLinuxProfile withMasterProfile(int count,String dnsPrefix);
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
            Definition withWindowsProfile(String adminUsername,String adminPassword);
        }

        interface WithLinuxProfile {
            /**
             * Properties of Linux VMs.
             *
             * @return the next stage
             */
            DefineAgentPoolProfiles withLinuxProfile(String adminUsername, String sshKeyData);
        }

        interface WithDiagnosticsProfile {
            /**
             * Properties of the diagnostic agent.
             *
             * @return the next stage
             */
            Definition withDiagnosticsProfile(ContainerServiceDiagnosticsProfile vmDiagnostics);
        }

        interface WithCreate extends
                Creatable<ContainerService>,
                ContainerService.DefinitionStages.WithOrchestratorProfile,
                ContainerService.DefinitionStages.WithCustomProfile,
                ContainerService.DefinitionStages.WithServicePrincipalProfile,
                ContainerService.DefinitionStages.WithWindowsProfile,
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
            ContainerService.UpdateStages.WithCustomProfile,
            ContainerService.UpdateStages.WithServicePrincipalProfile,
            ContainerService.UpdateStages.WithMasterProfile,
            ContainerService.UpdateStages.DefineAgentPoolProfiles,
            ContainerService.UpdateStages.WithWindowsProfile,
            ContainerService.UpdateStages.WithLinuxProfile,
            ContainerService.UpdateStages.WithDiagnosticsProfile {
    }

    interface UpdateStages {

        interface WithOrchestratorProfile {
            /**
             * Properties of the orchestrator.
             *
             * @return the next stage
             */
            Update withOrchestratorProfile(ContainerServiceOchestratorTypes orchestratorType);
        }

        interface WithCustomProfile {
            /**
             * Properties for custom clusters.
             *
             * @return the next stage
             */
            Update withCustomProfile(String orchestrator);
        }

        interface WithServicePrincipalProfile {
            /**
             * Properties for cluster service principals.
             *
             * @return the next stage
             */
            Update withServicePrincipalProfile(String clientId,String secret);
        }

        interface WithMasterProfile {
            /**
             * Properties of master agents.
             *
             * @return the next stage
             */
            Update withMasterProfile(int count,String dnsPrefix);
        }

        interface DefineAgentPoolProfiles {
            /**
             * Properties of the agent pool.
             *
             * @param name
             * @return the next stage
             */
            CSAgentPoolProfile.Update<Update> updateContainerServiceAgentPoolProfile(String name);
        }

        interface WithWindowsProfile {
            /**
             * Properties of Windows VMs.
             *
             * @return the next stage
             */
            Update withWindowsProfile(String adminUsername,String adminPassword);
        }

        interface WithLinuxProfile {
            /**
             * Properties of Linux VMs.
             *
             * @return the next stage
             */
            Update withLinuxProfile(String adminUsername, String sshKeyData);
        }

        interface WithDiagnosticsProfile {
            /**
             * Properties of the diagnostic agent.
             *
             * @return the next stage
             */
            Update withDiagnosticsProfile(ContainerServiceDiagnosticsProfile vmDiagnostics);
        }
    }

}
