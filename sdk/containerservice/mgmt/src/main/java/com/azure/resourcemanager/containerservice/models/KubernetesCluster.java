// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.
package com.azure.resourcemanager.containerservice.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.containerservice.ContainerServiceManager;
import com.azure.resourcemanager.containerservice.fluent.inner.ManagedClusterInner;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;

import java.util.List;
import java.util.Map;

/** A client-side representation for a managed Kubernetes cluster. */
@Fluent
public interface KubernetesCluster
    extends GroupableResource<ContainerServiceManager, ManagedClusterInner>,
        Refreshable<KubernetesCluster>,
        Updatable<KubernetesCluster.Update>,
        OrchestratorServiceBase {

    /** @return the provisioning state of the Kubernetes cluster */
    String provisioningState();

    /** @return the DNS prefix which was specified at creation time */
    String dnsPrefix();

    /** @return the FQDN for the master pool */
    String fqdn();

    /** @return the Kubernetes version */
    KubernetesVersion version();

    /** @return the Kubernetes configuration file content with administrative privileges to the cluster */
    byte[] adminKubeConfigContent();

    /** @return the Kubernetes configuration file content with user-level privileges to the cluster */
    byte[] userKubeConfigContent();

    /** @return the Kubernetes credentials with administrative privileges to the cluster */
    List<CredentialResult> adminKubeConfigs();

    /** @return the Kubernetes credentials with user-level privileges to the cluster */
    List<CredentialResult> userKubeConfigs();

    /** @return the service principal client ID */
    String servicePrincipalClientId();

    /** @return the service principal secret */
    String servicePrincipalSecret();

    /** @return the Linux root username */
    String linuxRootUsername();

    /** @return the Linux SSH key */
    String sshKey();

    /** @return the agent pools in the Kubernetes cluster */
    Map<String, KubernetesClusterAgentPool> agentPools();

    /** @return the network profile settings for the cluster */
    ContainerServiceNetworkProfile networkProfile();

    /** @return the cluster's add-on's profiles */
    Map<String, ManagedClusterAddonProfile> addonProfiles();

    /** @return the name of the resource group containing agent pool nodes */
    String nodeResourceGroup();

    /** @return true if Kubernetes Role-Based Access Control is enabled */
    boolean enableRBAC();

    // Fluent interfaces

    /** Interface for all the definitions related to a Kubernetes cluster. */
    interface Definition
        extends KubernetesCluster.DefinitionStages.Blank,
            KubernetesCluster.DefinitionStages.WithGroup,
            KubernetesCluster.DefinitionStages.WithVersion,
            DefinitionStages.WithLinuxRootUsername,
            DefinitionStages.WithLinuxSshKey,
            DefinitionStages.WithServicePrincipalClientId,
            DefinitionStages.WithServicePrincipalProfile,
            DefinitionStages.WithDnsPrefix,
            DefinitionStages.WithAgentPool,
            DefinitionStages.WithNetworkProfile,
            DefinitionStages.WithAddOnProfiles,
            KubernetesCluster.DefinitionStages.WithCreate {
    }

    /** Grouping of Kubernetes cluster definition stages. */
    interface DefinitionStages {
        /** The first stage of a container service definition. */
        interface Blank extends DefinitionWithRegion<WithGroup> {
        }

        /** The stage of the Kubernetes cluster definition allowing to specify the resource group. */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<WithVersion> {
        }

        /** The stage of the Kubernetes cluster definition allowing to specify orchestration type. */
        interface WithVersion {
            /**
             * Specifies the version for the Kubernetes cluster.
             *
             * @deprecated use {@link #withVersion(String)} or {@link #withLatestVersion}
             * @param kubernetesVersion the kubernetes version
             * @return the next stage of the definition
             */
            @Deprecated
            WithLinuxRootUsername withVersion(KubernetesVersion kubernetesVersion);

            /**
             * Specifies the version for the Kubernetes cluster.
             *
             * @param kubernetesVersion the kubernetes version
             * @return the next stage of the definition
             */
            WithLinuxRootUsername withVersion(String kubernetesVersion);

            /**
             * Uses the latest version for the Kubernetes cluster.
             *
             * @return the next stage of the definition
             */
            WithLinuxRootUsername withLatestVersion();
        }

        /** The stage of the Kubernetes cluster definition allowing to specific the Linux root username. */
        interface WithLinuxRootUsername {
            /**
             * Begins the definition to specify Linux root username.
             *
             * @param rootUserName the root username
             * @return the next stage of the definition
             */
            WithLinuxSshKey withRootUsername(String rootUserName);
        }

        /** The stage of the Kubernetes cluster definition allowing to specific the Linux SSH key. */
        interface WithLinuxSshKey {
            /**
             * Begins the definition to specify Linux ssh key.
             *
             * @param sshKeyData the SSH key data
             * @return the next stage of the definition
             */
            WithServicePrincipalClientId withSshKey(String sshKeyData);
        }

        /** The stage of the Kubernetes cluster definition allowing to specify the service principal client ID. */
        interface WithServicePrincipalClientId {
            /**
             * Properties for Kubernetes cluster service principal.
             *
             * @param clientId the ID for the service principal
             * @return the next stage
             */
            WithServicePrincipalProfile withServicePrincipalClientId(String clientId);
        }

        /** The stage of the Kubernetes cluster definition allowing to specify the service principal secret. */
        interface WithServicePrincipalProfile {
            /**
             * Properties for service principal.
             *
             * @param secret the secret password associated with the service principal
             * @return the next stage
             */
            WithAgentPool withServicePrincipalSecret(String secret);
        }

        /** The stage of the Kubernetes cluster definition allowing to specify an agent pool profile. */
        interface WithAgentPool {
            /**
             * Begins the definition of an agent pool profile to be attached to the Kubernetes cluster.
             *
             * @param name the name for the agent pool profile
             * @return the stage representing configuration for the agent pool profile
             */
            KubernetesClusterAgentPool.DefinitionStages.Blank<KubernetesCluster.DefinitionStages.WithCreate>
                defineAgentPool(String name);
        }

        /** The stage of the Kubernetes cluster definition allowing to specify a network profile. */
        interface WithNetworkProfile {
            /**
             * Begins the definition of a network profile to be attached to the Kubernetes cluster.
             *
             * @return the stage representing configuration for the network profile
             */
            NetworkProfileDefinitionStages.Blank<KubernetesCluster.DefinitionStages.WithCreate> defineNetworkProfile();
        }

        /** The Kubernetes cluster definition allowing to specify a network profile. */
        interface NetworkProfileDefinitionStages {
            /**
             * The first stage of a network profile definition.
             *
             * @param <ParentT> the stage of the Kubernetes cluster network profile definition to return to after
             *     attaching this definition
             */
            interface Blank<ParentT> extends WithAttach<ParentT> {
                /**
                 * Specifies the network plugin type to be used for building the Kubernetes network.
                 *
                 * @param networkPlugin the network plugin type to be used for building the Kubernetes network
                 * @return the next stage of the definition
                 */
                WithAttach<ParentT> withNetworkPlugin(NetworkPlugin networkPlugin);
            }

            /**
             * The stage of a network profile definition allowing to specify the network policy.
             *
             * @param <ParentT> the stage of the network profile definition to return to after attaching this definition
             */
            interface WithNetworkPolicy<ParentT> {
                /**
                 * Specifies the network policy to be used for building the Kubernetes network.
                 *
                 * @param networkPolicy the network policy to be used for building the Kubernetes network
                 * @return the next stage of the definition
                 */
                WithAttach<ParentT> withNetworkPolicy(NetworkPolicy networkPolicy);
            }

            /**
             * The stage of a network profile definition allowing to specify a CIDR notation IP range from which to
             * assign pod IPs when kubenet is used.
             *
             * @param <ParentT> the stage of the network profile definition to return to after attaching this definition
             */
            interface WithPodCidr<ParentT> {
                /**
                 * Specifies a CIDR notation IP range from which to assign pod IPs when kubenet is used.
                 *
                 * @param podCidr the CIDR notation IP range from which to assign pod IPs when kubenet is used
                 * @return the next stage of the definition
                 */
                WithAttach<ParentT> withPodCidr(String podCidr);
            }

            /**
             * The stage of a network profile definition allowing to specify a CIDR notation IP range from which to
             * assign service cluster IPs.
             *
             * @param <ParentT> the stage of the network profile definition to return to after attaching this definition
             */
            interface WithServiceCidr<ParentT> {
                /**
                 * Specifies a CIDR notation IP range from which to assign service cluster IPs; must not overlap with
                 * any subnet IP ranges.
                 *
                 * @param serviceCidr the CIDR notation IP range from which to assign service cluster IPs; it must not
                 *     overlap with any Subnet IP ranges
                 * @return the next stage of the definition
                 */
                WithAttach<ParentT> withServiceCidr(String serviceCidr);
            }

            /**
             * The stage of a network profile definition allowing to specify an IP address assigned to the Kubernetes
             * DNS service.
             *
             * @param <ParentT> the stage of the network profile definition to return to after attaching this definition
             */
            interface WithDnsServiceIP<ParentT> {
                /**
                 * Specifies an IP address assigned to the Kubernetes DNS service; it must be within the Kubernetes
                 * service address range specified in the service CIDR.
                 *
                 * @param dnsServiceIP the IP address assigned to the Kubernetes DNS service; it must be within the
                 *     Kubernetes service address range specified in the service CIDR
                 * @return the next stage of the definition
                 */
                WithAttach<ParentT> withDnsServiceIP(String dnsServiceIP);
            }

            /**
             * The stage of a network profile definition allowing to specify a CIDR notation IP range assigned to the
             * Docker bridge network.
             *
             * @param <ParentT> the stage of the network profile definition to return to after attaching this definition
             */
            interface WithDockerBridgeCidr<ParentT> {
                /**
                 * Specifies a CIDR notation IP range assigned to the Docker bridge network; it must not overlap with
                 * any subnet IP ranges or the Kubernetes service address range.
                 *
                 * @param dockerBridgeCidr the CIDR notation IP range assigned to the Docker bridge network; it must not
                 *     overlap with any subnet IP ranges or the Kubernetes service address range
                 * @return the next stage of the definition
                 */
                WithAttach<ParentT> withDockerBridgeCidr(String dockerBridgeCidr);
            }

            /**
             * The final stage of a network profile definition. At this stage, any remaining optional settings can be
             * specified, or the container service agent pool can be attached to the parent container service
             * definition.
             *
             * @param <ParentT> the stage of the container service definition to return to after attaching this
             *     definition
             */
            interface WithAttach<ParentT>
                extends NetworkProfileDefinitionStages.WithNetworkPolicy<ParentT>,
                    NetworkProfileDefinitionStages.WithPodCidr<ParentT>,
                    NetworkProfileDefinitionStages.WithServiceCidr<ParentT>,
                    NetworkProfileDefinitionStages.WithDnsServiceIP<ParentT>,
                    NetworkProfileDefinitionStages.WithDockerBridgeCidr<ParentT>,
                    Attachable.InDefinition<ParentT> {
            }
        }

        /**
         * The Kubernetes cluster network profile definition. The entirety of a Kubernetes cluster network profile
         * definition as a part of a parent definition.
         *
         * @param <ParentT> the stage of the container service definition to return to after attaching this definition
         */
        interface NetworkProfileDefinition<ParentT>
            extends NetworkProfileDefinitionStages.Blank<ParentT>,
                NetworkProfileDefinitionStages.WithNetworkPolicy<ParentT>,
                NetworkProfileDefinitionStages.WithPodCidr<ParentT>,
                NetworkProfileDefinitionStages.WithServiceCidr<ParentT>,
                NetworkProfileDefinitionStages.WithDnsServiceIP<ParentT>,
                NetworkProfileDefinitionStages.WithDockerBridgeCidr<ParentT>,
                NetworkProfileDefinitionStages.WithAttach<ParentT> {
        }

        /** The stage of the Kubernetes cluster definition allowing to specify the DNS prefix label. */
        interface WithDnsPrefix {
            /**
             * Specifies the DNS prefix to be used to create the FQDN for the master pool.
             *
             * @param dnsPrefix the DNS prefix to be used to create the FQDN for the master pool
             * @return the next stage of the definition
             */
            KubernetesCluster.DefinitionStages.WithCreate withDnsPrefix(String dnsPrefix);
        }

        /** The stage of the Kubernetes cluster definition allowing to specify the cluster's add-on's profiles. */
        interface WithAddOnProfiles {
            /**
             * Updates the cluster's add-on's profiles.
             *
             * @param addOnProfileMap the cluster's add-on's profiles
             * @return the next stage of the update
             */
            KubernetesCluster.Update withAddOnProfiles(Map<String, ManagedClusterAddonProfile> addOnProfileMap);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created,
         * but also allows for any other optional settings to be specified.
         */
        interface WithCreate
            extends Creatable<KubernetesCluster>,
                WithNetworkProfile,
                WithDnsPrefix,
                WithAddOnProfiles,
                Resource.DefinitionWithTags<WithCreate> {
        }
    }

    /** The template for an update operation, containing all the settings that can be modified. */
    interface Update
        extends KubernetesCluster.UpdateStages.WithUpdateAgentPoolCount,
            KubernetesCluster.UpdateStages.WithAddOnProfiles,
            KubernetesCluster.UpdateStages.WithNetworkProfile,
            KubernetesCluster.UpdateStages.WithRBAC,
            Resource.UpdateWithTags<KubernetesCluster.Update>,
            Appliable<KubernetesCluster> {
    }

    /** Grouping of the Kubernetes cluster update stages. */
    interface UpdateStages {
        /**
         * The stage of the Kubernetes cluster update definition allowing to specify the number of agents in the
         * specified pool.
         */
        interface WithUpdateAgentPoolCount {
            /**
             * Updates the agent pool virtual machine count.
             *
             * @param agentPoolName the name of the agent pool to be updated
             * @param agentCount the number of agents (virtual machines) to host docker containers.
             * @return the next stage of the update
             */
            KubernetesCluster.Update withAgentPoolVirtualMachineCount(String agentPoolName, int agentCount);

            /**
             * Updates all the agent pools virtual machine count.
             *
             * @param agentCount the number of agents (virtual machines) to host docker containers.
             * @return the next stage of the update
             */
            KubernetesCluster.Update withAgentPoolVirtualMachineCount(int agentCount);
        }

        /**
         * The stage of the Kubernetes cluster update definition allowing to specify the cluster's add-on's profiles.
         */
        interface WithAddOnProfiles {
            /**
             * Updates the cluster's add-on's profiles.
             *
             * @param addOnProfileMap the cluster's add-on's profiles
             * @return the next stage of the update
             */
            KubernetesCluster.Update withAddOnProfiles(Map<String, ManagedClusterAddonProfile> addOnProfileMap);
        }

        /** The stage of the Kubernetes cluster update definition allowing to specify the cluster's network profile. */
        interface WithNetworkProfile {
            /**
             * Updates the cluster's network profile.
             *
             * @param networkProfile the cluster's networkProfile
             * @return the next stage of the update
             */
            KubernetesCluster.Update withNetworkProfile(ContainerServiceNetworkProfile networkProfile);
        }

        /**
         * The stage of the Kubernetes cluster update definition allowing to specify if Kubernetes Role-Based Access
         * Control is enabled or disabled.
         */
        interface WithRBAC {
            /**
             * Updates the cluster to specify the Kubernetes Role-Based Access Control is enabled.
             *
             * @return the next stage of the update
             */
            KubernetesCluster.Update withRBACEnabled();

            /**
             * Updates the cluster to specify the Kubernetes Role-Based Access Control is disabled.
             *
             * @return the next stage of the update
             */
            KubernetesCluster.Update withRBACDisabled();
        }
    }
}
