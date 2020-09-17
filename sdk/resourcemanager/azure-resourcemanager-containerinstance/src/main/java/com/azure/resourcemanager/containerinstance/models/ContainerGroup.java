// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerinstance.models;

import com.azure.core.annotation.Fluent;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.containerinstance.ContainerInstanceManager;
import com.azure.resourcemanager.containerinstance.fluent.inner.ContainerGroupInner;
import com.azure.resourcemanager.msi.models.Identity;
import com.azure.resourcemanager.resources.fluentcore.arm.models.GroupableResource;
import com.azure.resourcemanager.resources.fluentcore.arm.models.Resource;
import com.azure.resourcemanager.resources.fluentcore.model.Appliable;
import com.azure.resourcemanager.resources.fluentcore.model.Attachable;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.model.Refreshable;
import com.azure.resourcemanager.resources.fluentcore.model.Updatable;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import reactor.core.publisher.Mono;

/** An immutable client-side representation of an Azure Container Group. */
@Fluent
public interface ContainerGroup
    extends GroupableResource<ContainerInstanceManager, ContainerGroupInner>,
        Refreshable<ContainerGroup>,
        Updatable<ContainerGroup.Update> {

    /***********************************************************
     * Getters
     ***********************************************************/

    /** @return the container instances in this container group */
    Map<String, Container> containers();

    /** @return all the ports publicly exposed for this container group */
    Set<Port> externalPorts();

    /** @return the TCP ports publicly exposed for this container group */
    int[] externalTcpPorts();

    /** @return the UDP ports publicly exposed for this container group */
    int[] externalUdpPorts();

    /** @return the volumes for this container group */
    Map<String, Volume> volumes();

    /** @return the Docker image registry servers by which the container group is created from */
    Collection<String> imageRegistryServers();

    /** @return the container group restart policy */
    ContainerGroupRestartPolicy restartPolicy();

    /** @return the DNS prefix which was specified at creation time */
    String dnsPrefix();

    /** @return the FQDN for the container group */
    String fqdn();

    /** @return the IP address */
    String ipAddress();

    /** @return true if IP address is public */
    boolean isIPAddressPublic();

    /** @return true if IP address is private */
    boolean isIPAddressPrivate();

    /** @return the base level OS type required by the containers in the group */
    OperatingSystemTypes osType();

    /** @return the state of the container group; only valid in response */
    String state();

    /** @return the provisioningState of the container group */
    String provisioningState();

    /** @return the container group events */
    Set<Event> events();

    /** @return the DNS configuration for the container group */
    DnsConfiguration dnsConfig();

    /** @return the id of the network profile for the container group */
    String networkProfileId();

    /** @return whether managed service identity is enabled for the container group */
    boolean isManagedServiceIdentityEnabled();

    /**
     * @return the tenant id of the system assigned managed service identity. Null if managed service identity is not
     *     configured.
     */
    String systemAssignedManagedServiceIdentityTenantId();

    /**
     * @return the principal id of the system assigned managed service identity. Null if managed service identity is not
     *     configured.
     */
    String systemAssignedManagedServiceIdentityPrincipalId();

    /** @return whether managed service identity is system assigned, user assigned, both, or neither */
    ResourceIdentityType managedServiceIdentityType();

    /** @return the ids of the user assigned managed service identities. Returns an empty set if no MSIs are set. */
    Set<String> userAssignedManagedServiceIdentityIds();

    /** @return the log analytics information of the container group. */
    LogAnalytics logAnalytics();

    /***********************************************************
     * Actions
     ***********************************************************/

    /**
     * Restarts all containers in a container group in place. If container image has updates, new image will be
     * downloaded.
     */
    void restart();

    /**
     * Restarts all containers in a container group in place asynchronously. If container image has updates, new image
     * will be downloaded.
     *
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> restartAsync();

    /** Stops all containers in a container group. Compute resources will be de-allocated and billing will stop. */
    void stop();

    /**
     * Stops all containers in a container group asynchronously. Compute resources will be de-allocated and billing will
     * stop.
     *
     * @return a representation of the deferred computation of this call
     */
    Mono<Void> stopAsync();

    /**
     * Get the log content for the specified container instance within the container group.
     *
     * @param containerName the container instance name
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return all available log lines
     */
    String getLogContent(String containerName);

    /**
     * Get the log content for the specified container instance within the container group.
     *
     * @param containerName the container instance name
     * @param tailLineCount only get the last log lines up to this
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the log lines from the end, up to the number specified
     */
    String getLogContent(String containerName, int tailLineCount);

    /**
     * Get the log content for the specified container instance within the container group.
     *
     * @param containerName the container instance name
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return a representation of the future computation of this call
     */
    Mono<String> getLogContentAsync(String containerName);

    /**
     * Get the log content for the specified container instance within the container group.
     *
     * @param containerName the container instance name
     * @param tailLineCount only get the last log lines up to this
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return a representation of the future computation of this call
     */
    Mono<String> getLogContentAsync(String containerName, int tailLineCount);

    /**
     * Starts the exec command for a specific container instance.
     *
     * @param containerName the container instance name
     * @param command the command to be executed
     * @param row the row size of the terminal
     * @param column the column size of the terminal
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return the log lines from the end, up to the number specified
     */
    ContainerExecResponse executeCommand(String containerName, String command, int row, int column);

    /**
     * Starts the exec command for a specific container instance within the container group.
     *
     * @param containerName the container instance name
     * @param command the command to be executed
     * @param row the row size of the terminal
     * @param column the column size of the terminal
     * @throws IllegalArgumentException thrown if parameters fail the validation
     * @return a representation of the future computation of this call
     */
    Mono<ContainerExecResponse> executeCommandAsync(String containerName, String command, int row, int column);

    /** Starts the exec command for a specific container instance within the current group asynchronously. */
    interface Definition
        extends DefinitionStages.Blank,
            DefinitionStages.WithGroup,
            DefinitionStages.WithOsType,
            DefinitionStages.WithPublicOrPrivateImageRegistry,
            DefinitionStages.WithPrivateImageRegistryOrVolume,
            DefinitionStages.WithVolume,
            DefinitionStages.WithFirstContainerInstance,
            DefinitionStages.WithSystemAssignedManagedServiceIdentity,
            DefinitionStages.WithSystemAssignedIdentityBasedAccessOrCreate,
            DefinitionStages.WithNextContainerInstance,
            DefinitionStages.DnsConfigFork,
            DefinitionStages.WithCreate {
    }

    /** Grouping of the container group definition stages. */
    interface DefinitionStages {
        /** The first stage of the container group definition. */
        interface Blank extends GroupableResource.DefinitionWithRegion<WithGroup> {
        }

        /** The stage of the container group definition allowing to specify the resource group. */
        interface WithGroup extends GroupableResource.DefinitionStages.WithGroup<DefinitionStages.WithOsType> {
        }

        /** The stage of the container group definition allowing to specify the OS type. */
        interface WithOsType {
            /**
             * Specifies this is a Linux container group.
             *
             * @return the next stage of the definition
             */
            WithPublicOrPrivateImageRegistry withLinux();

            /**
             * Specifies this is a Windows container group.
             *
             * @return the next stage of the definition
             */
            WithPublicOrPrivateImageRegistry withWindows();
        }

        /** The stage of the container group definition allowing to specify a public only or private image registry. */
        interface WithPublicOrPrivateImageRegistry extends WithPublicImageRegistryOnly, WithPrivateImageRegistry {
        }

        /** The stage of the container group definition allowing to skip the private image registry. */
        interface WithPublicImageRegistryOnly {
            /**
             * Only public container image repository will be used to create the container instances in the container
             * group.
             *
             * @return the next stage of the definition
             */
            WithPrivateImageRegistryOrVolume withPublicImageRegistryOnly();
        }

        /** The stage of the container group definition allowing to specify a private image registry. */
        interface WithPrivateImageRegistry {
            /**
             * Specifies the private container image registry server login for the container group.
             *
             * @param server Docker image registry server, without protocol such as "http" and "https"
             * @param username the username for the private registry
             * @param password the password for the private registry
             * @return the next stage of the definition
             */
            WithPrivateImageRegistryOrVolume withPrivateImageRegistry(String server, String username, String password);
        }

        /** The stage of the container group definition allowing to specify a private image registry or a volume. */
        interface WithPrivateImageRegistryOrVolume extends WithPrivateImageRegistry {
            /**
             * Skips the definition of volumes to be shared by the container instances.
             *
             * <p>An IllegalArgumentException will be thrown if a container instance attempts to define a volume
             * mounting.
             *
             * @return the next stage of the definition
             */
            WithFirstContainerInstance withoutVolume();

            /**
             * Specifies a new Azure file share name to be created.
             *
             * @param volumeName the name of the volume
             * @param shareName the Azure file share name to be created
             * @return the next stage of the definition
             */
            WithFirstContainerInstance withNewAzureFileShareVolume(String volumeName, String shareName);

            /**
             * Specifies an empty directory volume that can be shared by the container instances in the container group.
             *
             * @param name the name of the empty directory volume
             * @return the next stage of the definition
             */
            WithFirstContainerInstance withEmptyDirectoryVolume(String name);

            /**
             * Begins the definition of a volume that can be shared by the container instances in the container group.
             *
             * <p>The definition must be completed with a call to {@link
             * VolumeDefinitionStages.WithVolumeAttach#attach()}
             *
             * @param name the name of the volume
             * @return the next stage of the definition
             */
            VolumeDefinitionStages.VolumeDefinitionBlank<WithVolume> defineVolume(String name);
        }

        /**
         * The stage of the container group definition allowing to specify a volume that can be mounted by a container
         * instance.
         */
        interface WithVolume extends WithFirstContainerInstance {
            /**
             * Begins the definition of a volume that can be shared by the container instances in the container group.
             *
             * <p>The definition must be completed with a call to {@link
             * VolumeDefinitionStages.WithVolumeAttach#attach()}
             *
             * @param name the name of the volume
             * @return the next stage of the definition
             */
            VolumeDefinitionStages.VolumeDefinitionBlank<WithVolume> defineVolume(String name);
        }

        /** Grouping of volume definition stages. */
        interface VolumeDefinitionStages {
            /**
             * The first stage of the volume definition.
             *
             * @param <ParentT> the stage of the parent definition to return to after attaching this definition
             */
            interface VolumeDefinitionBlank<ParentT> extends WithAzureFileShare<ParentT> {
            }

            /**
             * The stage of the volume definition allowing to specify a read only Azure File Share name.
             *
             * @param <ParentT> the stage of the parent definition to return to after attaching this definition
             */
            interface WithAzureFileShare<ParentT> {
                /**
                 * Specifies an existing Azure file share name.
                 *
                 * @param shareName an existing Azure file share name
                 * @return the next stage of the definition
                 */
                WithStorageAccountName<ParentT> withExistingReadWriteAzureFileShare(String shareName);

                /**
                 * Specifies an existing Azure file share name.
                 *
                 * @param shareName an existing Azure file share name
                 * @return the next stage of the definition
                 */
                WithStorageAccountName<ParentT> withExistingReadOnlyAzureFileShare(String shareName);
            }

            /**
             * The stage of the volume definition allowing to specify the storage account name to access to the Azure
             * file.
             *
             * @param <ParentT> the stage of the parent definition to return to after attaching this definition
             */
            interface WithStorageAccountName<ParentT> {
                /**
                 * Specifies the storage account name to access to the Azure file.
                 *
                 * @param storageAccountName the storage account name
                 * @return the next stage of the definition
                 */
                WithStorageAccountKey<ParentT> withStorageAccountName(String storageAccountName);
            }

            /**
             * The stage of the volume definition allowing to specify the storage account key to access to the Azure
             * file.
             *
             * @param <ParentT> the stage of the parent definition to return to after attaching this definition
             */
            interface WithStorageAccountKey<ParentT> {
                /**
                 * Specifies the storage account key to access to the Azure file.
                 *
                 * @param storageAccountKey the storage account key
                 * @return the next stage of the definition
                 */
                WithVolumeAttach<ParentT> withStorageAccountKey(String storageAccountKey);
            }

            /**
             * The stage of the volume definition allowing to specify the secrets map.
             *
             * @param <ParentT> the stage of the parent definition to return to after attaching this definition
             */
            interface WithSecretsMap<ParentT> {
                /**
                 * Specifies the secrets map.
                 *
                 * <p>The secret value must be specified in Base64 encoding
                 *
                 * @param secrets the new volume secrets map; value must be in Base64 encoding
                 * @return the next stage of the definition
                 */
                WithVolumeAttach<ParentT> withSecrets(Map<String, String> secrets);
            }

            /**
             * The stage of the volume definition allowing to specify the Git URL mappings.
             *
             * @param <ParentT> the stage of the parent definition to return to after attaching this definition
             */
            interface WithGitUrl<ParentT> {
                /**
                 * Specifies the Git URL for the new volume.
                 *
                 * @param gitUrl the Git URL for the new volume
                 * @return the next stage of the definition
                 */
                WithGitDirectoryName<ParentT> withGitUrl(String gitUrl);
            }

            /**
             * The stage of the volume definition allowing to specify the Git target directory name mappings.
             *
             * @param <ParentT> the stage of the parent definition to return to after attaching this definition
             */
            interface WithGitDirectoryName<ParentT> extends WithGitRevision<ParentT> {
                /**
                 * Specifies the Git target directory name for the new volume.
                 *
                 * <p>Must not contain or start with '..'. If '.' is supplied, the volume directory will be the git
                 * repository. Otherwise, if specified, the volume will contain the git repository in the subdirectory
                 * with the given name.
                 *
                 * @param gitDirectoryName the Git target directory name for the new volume
                 * @return the next stage of the definition
                 */
                WithGitRevision<ParentT> withGitDirectoryName(String gitDirectoryName);
            }

            /**
             * The stage of the volume definition allowing to specify the Git revision.
             *
             * @param <ParentT> the stage of the parent definition to return to after attaching this definition
             */
            interface WithGitRevision<ParentT> extends WithVolumeAttach<ParentT> {
                /**
                 * Specifies the Git revision for the new volume.
                 *
                 * @param gitRevision the Git revision for the new volume
                 * @return the next stage of the definition
                 */
                WithVolumeAttach<ParentT> withGitRevision(String gitRevision);
            }

            /**
             * The final stage of the volume definition.
             *
             * <p>At this stage, any remaining optional settings can be specified, or the subnet definition can be
             * attached to the parent virtual network definition.
             *
             * @param <ParentT> the stage of the parent definition to return to after attaching this definition
             */
            interface WithVolumeAttach<ParentT> extends Attachable.InDefinition<ParentT> {
            }

            /** Grouping of the container group's volume definition stages. */
            interface VolumeDefinition<ParentT>
                extends VolumeDefinitionBlank<ParentT>,
                    WithAzureFileShare<ParentT>,
                    WithStorageAccountName<ParentT>,
                    WithStorageAccountKey<ParentT>,
                    WithSecretsMap<ParentT>,
                    WithGitUrl<ParentT>,
                    WithGitDirectoryName<ParentT>,
                    WithGitRevision<ParentT>,
                    WithVolumeAttach<ParentT> {
            }
        }

        /** The stage of the container group definition allowing to specify first required container instance. */
        interface WithFirstContainerInstance {
            /**
             * Begins the definition of a container instance.
             *
             * @param name the name of the container instance
             * @return the next stage of the definition
             */
            ContainerInstanceDefinitionStages.ContainerInstanceDefinitionBlank<WithNextContainerInstance>
                defineContainerInstance(String name);

            /**
             * Defines one container instance for the specified image with one CPU count and 1.5 GB memory, with TCP
             * port 80 opened externally.
             *
             * @param imageName the name of the container image
             * @return the next stage of the definition
             */
            WithCreate withContainerInstance(String imageName);

            /**
             * Defines one container instance for the specified image with one CPU count and 1.5 GB memory, with a
             * custom TCP port opened externally.
             *
             * @param imageName the name of the container image
             * @param port the external port to be opened
             * @return the next stage of the definition
             */
            WithCreate withContainerInstance(String imageName, int port);
        }

        /** The stage of the container group definition allowing to specify a container instance. */
        interface WithNextContainerInstance extends WithCreate {
            /**
             * Begins the definition of a container instance.
             *
             * @param name the name of the volume
             * @return the next stage of the definition
             */
            ContainerInstanceDefinitionStages.ContainerInstanceDefinitionBlank<WithNextContainerInstance>
                defineContainerInstance(String name);
        }

        /** Grouping of volume definition stages. */
        interface ContainerInstanceDefinitionStages {
            /**
             * The first stage of the container instance definition.
             *
             * @param <ParentT> the stage of the parent definition to return to after attaching this definition
             */
            interface ContainerInstanceDefinitionBlank<ParentT> extends WithImage<ParentT> {
            }

            /**
             * The stage of the container instance definition allowing to specify the container image.
             *
             * @param <ParentT> the stage of the parent definition to return to after attaching this definition
             */
            interface WithImage<ParentT> {
                /**
                 * Specifies the container image to be used.
                 *
                 * @param imageName the container image
                 * @return the next stage of the definition
                 */
                WithOrWithoutPorts<ParentT> withImage(String imageName);
            }

            /**
             * The stage of the container instance definition allowing to specify (or not) the container ports.
             *
             * @param <ParentT> the stage of the parent definition to return to after attaching this definition
             */
            interface WithOrWithoutPorts<ParentT> extends WithPorts<ParentT>, WithoutPorts<ParentT> {
            }

            /**
             * The stage of the container instance definition allowing not to specify any container ports internal or
             * external.
             *
             * @param <ParentT> the stage of the parent definition to return to after attaching this definition
             */
            interface WithoutPorts<ParentT> {
                /**
                 * Specifies that not ports will be opened internally or externally for this container instance.
                 *
                 * @return the next stage of the definition
                 */
                WithContainerInstanceAttach<ParentT> withoutPorts();
            }

            /**
             * The stage of the container instance definition allowing to specify one or more container ports.
             *
             * @param <ParentT> the stage of the parent definition to return to after attaching this definition
             */
            interface WithPortsOrContainerInstanceAttach<ParentT>
                extends WithPorts<ParentT>, WithContainerInstanceAttach<ParentT> {
            }

            /**
             * The stage of the container instance definition allowing to specify the container ports.
             *
             * @param <ParentT> the stage of the parent definition to return to after attaching this definition
             */
            interface WithPorts<ParentT> {
                /**
                 * Specifies the container's TCP ports available to external clients.
                 *
                 * <p>A public IP address will be create to allow external clients to reach the containers within the
                 * group. To enable external clients to reach a container within the group, you must expose the port on
                 * the IP address and from the container. Because containers within the group share a port namespace,
                 * port mapping is not supported.
                 *
                 * @param ports array of TCP ports to be exposed externally
                 * @return the next stage of the definition
                 */
                WithPortsOrContainerInstanceAttach<ParentT> withExternalTcpPorts(int... ports);

                /**
                 * Specifies the container's TCP port available to external clients.
                 *
                 * <p>A public IP address will be create to allow external clients to reach the containers within the
                 * group. To enable external clients to reach a container within the group, you must expose the port on
                 * the IP address and from the container. Because containers within the group share a port namespace,
                 * port mapping is not supported.
                 *
                 * @param port TCP port to be exposed externally
                 * @return the next stage of the definition
                 */
                WithPortsOrContainerInstanceAttach<ParentT> withExternalTcpPort(int port);

                /**
                 * Specifies the container's UDP ports available to external clients.
                 *
                 * <p>A public IP address will be create to allow external clients to reach the containers within the
                 * group. To enable external clients to reach a container within the group, you must expose the port on
                 * the IP address and from the container. Because containers within the group share a port namespace,
                 * port mapping is not supported.
                 *
                 * @param ports array of UDP ports to be exposed externally
                 * @return the next stage of the definition
                 */
                WithPortsOrContainerInstanceAttach<ParentT> withExternalUdpPorts(int... ports);

                /**
                 * Specifies the container's UDP port available to external clients.
                 *
                 * <p>A public IP address will be create to allow external clients to reach the containers within the
                 * group. To enable external clients to reach a container within the group, you must expose the port on
                 * the IP address and from the container. Because containers within the group share a port namespace,
                 * port mapping is not supported.
                 *
                 * @param port UDP port to be exposed externally
                 * @return the next stage of the definition
                 */
                WithPortsOrContainerInstanceAttach<ParentT> withExternalUdpPort(int port);

                /**
                 * Specifies the container's TCP ports are available to internal clients only (other container instances
                 * within the container group).
                 *
                 * <p>Containers within a group can reach each other via localhost on the ports that they have exposed,
                 * even if those ports are not exposed externally on the group's IP address.
                 *
                 * @param ports array of TCP ports to be exposed internally
                 * @return the next stage of the definition
                 */
                WithPortsOrContainerInstanceAttach<ParentT> withInternalTcpPorts(int... ports);

                /**
                 * Specifies the container's Udp ports are available to internal clients only (other container instances
                 * within the container group).
                 *
                 * <p>Containers within a group can reach each other via localhost on the ports that they have exposed,
                 * even if those ports are not exposed externally on the group's IP address.
                 *
                 * @param ports array of UDP ports to be exposed internally
                 * @return the next stage of the definition
                 */
                WithPortsOrContainerInstanceAttach<ParentT> withInternalUdpPorts(int... ports);

                /**
                 * Specifies the container's TCP port is available to internal clients only (other container instances
                 * within the container group).
                 *
                 * <p>Containers within a group can reach each other via localhost on the ports that they have exposed,
                 * even if those ports are not exposed externally on the group's IP address.
                 *
                 * @param port TCP port to be exposed internally
                 * @return the next stage of the definition
                 */
                WithPortsOrContainerInstanceAttach<ParentT> withInternalTcpPort(int port);

                /**
                 * Specifies the container's UDP port is available to internal clients only (other container instances
                 * within the container group).
                 *
                 * <p>Containers within a group can reach each other via localhost on the ports that they have exposed,
                 * even if those ports are not exposed externally on the group's IP address.
                 *
                 * @param port UDP port to be exposed internally
                 * @return the next stage of the definition
                 */
                WithPortsOrContainerInstanceAttach<ParentT> withInternalUdpPort(int port);
            }

            /**
             * The stage of the container instance definition allowing to specify the number of CPU cores.
             *
             * <p>The CPU cores can be specified as a fraction, i.e. 1.5 represents one and a half atomic CPU cores will
             * be assigned to this container instance.
             *
             * @param <ParentT> the stage of the parent definition to return to after attaching this definition
             */
            interface WithCpuCoreCount<ParentT> {
                /**
                 * Specifies the number of CPU cores assigned to this container instance.
                 *
                 * @param cpuCoreCount the number of CPU cores
                 * @return the next stage of the definition
                 */
                WithContainerInstanceAttach<ParentT> withCpuCoreCount(double cpuCoreCount);
            }

            interface WithGpuResource<ParentT> {
                WithContainerInstanceAttach<ParentT> withGpuResource(int gpuCoreCount, GpuSku gpuSku);
            }

            /**
             * The stage of the container instance definition allowing to specify the memory size in GB.
             *
             * <p>The memory size can be specified as a fraction, i.e. 1.5 represents one and a half GB of memory.
             *
             * @param <ParentT> the stage of the parent definition to return to after attaching this definition
             */
            interface WithMemorySize<ParentT> {
                /**
                 * Specifies the memory size in GB assigned to this container instance.
                 *
                 * @param memorySize the memory size in GB
                 * @return the next stage of the definition
                 */
                WithContainerInstanceAttach<ParentT> withMemorySizeInGB(double memorySize);
            }

            /**
             * The stage of the container instance definition allowing to specify the starting command line.
             *
             * @param <ParentT> the stage of the parent definition to return to after attaching this definition
             */
            interface WithStartingCommandLine<ParentT> {
                /**
                 * Specifies the starting command lines.
                 *
                 * @param executable the executable which it will call after initializing the container
                 * @param parameters the parameter list for the executable to be called
                 * @return the next stage of the definition
                 */
                WithContainerInstanceAttach<ParentT> withStartingCommandLine(String executable, String... parameters);

                /**
                 * Specifies the starting command line.
                 *
                 * @param executable the executable or path to the executable that will be called after initializing the
                 *     container
                 * @return the next stage of the definition
                 */
                WithContainerInstanceAttach<ParentT> withStartingCommandLine(String executable);
            }

            /**
             * The stage of the container instance definition allowing to specify the environment variables.
             *
             * @param <ParentT> the stage of the parent definition to return to after attaching this definition
             */
            interface WithEnvironmentVariables<ParentT> {
                /**
                 * Specifies the environment variables.
                 *
                 * @param environmentVariables the environment variables in a name and value pair to be set after the
                 *     container gets initialized
                 * @return the next stage of the definition
                 */
                WithContainerInstanceAttach<ParentT> withEnvironmentVariables(Map<String, String> environmentVariables);

                /**
                 * Specifies the environment variable.
                 *
                 * @param envName the environment variable name
                 * @param envValue the environment variable value
                 * @return the next stage of the definition
                 */
                WithContainerInstanceAttach<ParentT> withEnvironmentVariable(String envName, String envValue);

                /**
                 * Specifies a collection of name and secure value pairs for the environment variables.
                 *
                 * @param environmentVariables the environment variables in a name and value pair to be set after the
                 *     container gets initialized
                 * @return the next stage of the definition
                 */
                WithContainerInstanceAttach<ParentT> withEnvironmentVariableWithSecuredValue(
                    Map<String, String> environmentVariables);

                /**
                 * Specifies the environment variable that has a secured value.
                 *
                 * @param envName the environment variable name
                 * @param securedValue the environment variable secured value
                 * @return the next stage of the definition
                 */
                WithContainerInstanceAttach<ParentT> withEnvironmentVariableWithSecuredValue(
                    String envName, String securedValue);
            }

            /**
             * The stage of the container instance definition allowing to specify volume mount setting.
             *
             * @param <ParentT> the stage of the parent definition to return to after attaching this definition
             */
            interface WithVolumeMountSetting<ParentT> {
                /**
                 * Specifies the container group's volume to be mounted by the container instance at a specified mount
                 * path.
                 *
                 * <p>Mounting an Azure file share as a volume in a container is a two-step process. First, you provide
                 * the details of the share as part of defining the container group, then you specify how you wan the
                 * volume mounted within one or more of the containers in the group.
                 *
                 * @param volumeName the volume name as defined in the volumes of the container group
                 * @param mountPath the local path the volume will be mounted at
                 * @return the next stage of the definition
                 * @throws IllegalArgumentException thrown if volumeName was not defined in the respective container
                 *     group definition stage.
                 */
                WithContainerInstanceAttach<ParentT> withVolumeMountSetting(String volumeName, String mountPath);

                /**
                 * Specifies the container group's volume to be mounted by the container instance at a specified mount
                 * path.
                 *
                 * <p>Mounting an Azure file share as a volume in a container is a two-step process. First, you provide
                 * the details of the share as part of defining the container group, then you specify how you wan the
                 * volume mounted within one or more of the containers in the group.
                 *
                 * @param volumeMountSetting the name and value pair representing volume names as defined in the volumes
                 *     of the container group and the local paths the volume will be mounted at
                 * @return the next stage of the definition
                 * @throws IllegalArgumentException thrown if volumeName was not defined in the respective container
                 *     group definition stage.
                 */
                WithContainerInstanceAttach<ParentT> withVolumeMountSetting(Map<String, String> volumeMountSetting);

                /**
                 * Specifies the container group's volume to be mounted by the container instance at a specified mount
                 * path.
                 *
                 * <p>Mounting an Azure file share as a volume in a container is a two-step process. First, you provide
                 * the details of the share as part of defining the container group, then you specify how you wan the
                 * volume mounted within one or more of the containers in the group.
                 *
                 * @param volumeName the volume name as defined in the volumes of the container group
                 * @param mountPath the local path the volume will be mounted at
                 * @return the next stage of the definition
                 * @throws IllegalArgumentException thrown if volumeName was not defined in the respective container
                 *     group definition stage.
                 */
                WithContainerInstanceAttach<ParentT> withReadOnlyVolumeMountSetting(
                    String volumeName, String mountPath);

                /**
                 * Specifies the container group's volume to be mounted by the container instance at a specified mount
                 * path.
                 *
                 * <p>Mounting an Azure file share as a volume in a container is a two-step process. First, you provide
                 * the details of the share as part of defining the container group, then you specify how you wan the
                 * volume mounted within one or more of the containers in the group.
                 *
                 * @param volumeMountSetting the name and value pair representing volume names as defined in the volumes
                 *     of the container group and the local paths the volume will be mounted at
                 * @return the next stage of the definition
                 * @throws IllegalArgumentException thrown if volumeName was not defined in the respective container
                 *     group definition stage.
                 */
                WithContainerInstanceAttach<ParentT> withReadOnlyVolumeMountSetting(
                    Map<String, String> volumeMountSetting);
            }

            /**
             * The final stage of the container instance definition.
             *
             * <p>At this stage, any remaining optional settings can be specified, or the subnet definition can be
             * attached to the parent virtual network definition.
             *
             * @param <ParentT> the stage of the parent definition to return to after attaching this definition
             */
            interface WithContainerInstanceAttach<ParentT>
                extends WithCpuCoreCount<ParentT>,
                    WithGpuResource<ParentT>,
                    WithMemorySize<ParentT>,
                    WithStartingCommandLine<ParentT>,
                    WithEnvironmentVariables<ParentT>,
                    WithVolumeMountSetting<ParentT>,
                    Attachable.InDefinition<ParentT> {
            }

            /** Grouping of the container group's volume definition stages. */
            interface ContainerInstanceDefinition<ParentT>
                extends ContainerInstanceDefinitionBlank<ParentT>,
                    WithImage<ParentT>,
                    WithOrWithoutPorts<ParentT>,
                    WithPortsOrContainerInstanceAttach<ParentT>,
                    WithContainerInstanceAttach<ParentT> {
            }
        }

        /**
         * The stage of the container instance definition allowing to specify having system assigned managed service
         * identity.
         */
        interface WithSystemAssignedManagedServiceIdentity {
            /**
             * Specifies a system assigned managed service identity for the container group.
             *
             * @return the next stage of the definition
             */
            WithSystemAssignedIdentityBasedAccessOrCreate withSystemAssignedManagedServiceIdentity();
        }

        /**
         * The stage of the container instance definition allowing to specify system assigned managed service identity
         * with specific role based access.
         */
        interface WithSystemAssignedIdentityBasedAccessOrCreate extends WithCreate {
            /**
             * Specifies a system assigned managed service identity with access to a specific resource with a specified
             * role.
             *
             * @param resourceId the id of the resource you are setting up access to
             * @param role access role to be assigned to the identity
             * @return the next stage of the definition
             */
            WithSystemAssignedIdentityBasedAccessOrCreate withSystemAssignedIdentityBasedAccessTo(
                String resourceId, BuiltInRole role);

            /**
             * Specifies a system assigned managed service identity with access to the current resource group and with
             * the specified role.
             *
             * @param role access role to be assigned to the identity
             * @return the next stage of the definition
             */
            WithSystemAssignedIdentityBasedAccessOrCreate withSystemAssignedIdentityBasedAccessToCurrentResourceGroup(
                BuiltInRole role);

            /**
             * Specifies a system assigned managed service identity with access to a specific resource with a specified
             * role from the id.
             *
             * @param resourceId the id of the resource you are setting up access to
             * @param roleDefinitionId id of the access role to be assigned to the identity
             * @return the next stage of the definition
             */
            WithSystemAssignedIdentityBasedAccessOrCreate withSystemAssignedIdentityBasedAccessTo(
                String resourceId, String roleDefinitionId);

            /**
             * Specifies a system assigned managed service identity with access to the current resource group and with
             * the specified role from the id.
             *
             * @param roleDefinitionId id of the access role to be assigned to the identity
             * @return the next stage of the definition
             */
            WithSystemAssignedIdentityBasedAccessOrCreate withSystemAssignedIdentityBasedAccessToCurrentResourceGroup(
                String roleDefinitionId);
        }

        /**
         * The stage of the container instance definition allowing to specify user assigned managed service identity.
         */
        interface WithUserAssignedManagedServiceIdentity {
            /**
             * Specifies the definition of a not-yet-created user assigned identity to be associated with the virtual
             * machine.
             *
             * @param creatableIdentity a creatable identity definition
             * @return the next stage of the definition
             */
            WithCreate withNewUserAssignedManagedServiceIdentity(Creatable<Identity> creatableIdentity);

            /**
             * Specifies an existing user assigned identity to be associate with the container group.
             *
             * @param identity the identity
             * @return the next stage of the definition
             */
            WithCreate withExistingUserAssignedManagedServiceIdentity(Identity identity);
        }

        /** The stage of the container group definition allowing to specify the container group restart policy. */
        interface WithRestartPolicy {
            /**
             * Specifies the restart policy for all the container instances within the container group.
             *
             * @param restartPolicy the restart policy for all the container instances within the container group
             * @return the next stage of the definition
             */
            WithCreate withRestartPolicy(ContainerGroupRestartPolicy restartPolicy);
        }

        /** The stage of the container group definition allowing to specify the DNS prefix label. */
        interface WithDnsPrefix {
            /**
             * Specifies the DNS prefix to be used to create the FQDN for the container group.
             *
             * @param dnsPrefix the DNS prefix to be used to create the FQDN for the container group
             * @return the next stage of the definition
             */
            WithCreate withDnsPrefix(String dnsPrefix);
        }

        /** The stage of the container group definition allowing to specify the network profile id. */
        interface WithNetworkProfile {
            /**
             * Specifies the network profile information for a container group.
             *
             * @param subscriptionId the ID of the subscription of the network profile
             * @param resourceGroupName the name of the resource group of the network profile
             * @param networkProfileName the name of the network profile
             * @return the next stage of the definition
             */
            DnsConfigFork withExistingNetworkProfile(
                String subscriptionId, String resourceGroupName, String networkProfileName);

            /**
             * Specifies the network profile information for a container group.
             *
             * @param networkProfileId the ID of the network profile
             * @return the next stage of the definition
             */
            DnsConfigFork withExistingNetworkProfile(String networkProfileId);

            /**
             * Specifies the virtual network in network profile for a container group.
             *
             * @param virtualNetworkId the ID of the virtual network
             * @param subnetName the name of the subnet within the virtual network.;
             *                   the subnet must have delegation for 'Microsoft.ContainerInstance/containerGroups'.
             * @return the next stage of the definition
             */
            DnsConfigFork withNewNetworkProfileOnExistingVirtualNetwork(String virtualNetworkId, String subnetName);

            /**
             * Creates a new virtual network to associate with network profile in a container group.
             *
             * @param addressSpace the address space for the virtual network
             * @return the next stage of the definition
             */
            DnsConfigFork withNewVirtualNetwork(String addressSpace);
        }

        interface DnsConfigFork extends WithDnsConfig, WithCreate {
        }

        /**
         * The stage of the container group definition allowing to specify the DNS configuration of the container group.
         */
        interface WithDnsConfig {
            /**
             * Specifies the DNS servers for the container group.
             *
             * @param dnsServerNames the names of the DNS servers
             * @return the next stage of the definition
             */
            WithCreate withDnsServerNames(List<String> dnsServerNames);

            /**
             * Specifies the DNS configuration for the container group.
             *
             * @param dnsServerNames the names of the DNS servers for the container group
             * @param dnsSearchDomains the DNS search domains for hostname lookup in the container group
             * @param dnsOptions the DNS options for the container group
             * @return the next stage of the definition
             */
            WithCreate withDnsConfiguration(List<String> dnsServerNames, String dnsSearchDomains, String dnsOptions);
        }

        /**
         * The stage of the container group definition allowing to specify the log analytics platform for the container
         * group.
         */
        interface WithLogAnalytics {
            /**
             * Specifies the log analytics workspace to use for the container group.
             *
             * @param workspaceId the id of the previously-created log analytics workspace
             * @param workspaceKey the key of the previously-created log analytics workspace
             * @return the next stage of the definition
             */
            WithCreate withLogAnalytics(String workspaceId, String workspaceKey);

            /**
             * Specifies the log analytics workspace with optional add-ons for the container group.
             *
             * @param workspaceId the id of the previously-created log analytics workspace
             * @param workspaceKey the key of the previously-created log analytics workspace
             * @param logType the log type to be used. Possible values include: 'ContainerInsights',
             *     'ContainerInstanceLogs'.
             * @param metadata the metadata for log analytics
             * @return the next stage of the definition
             */
            WithCreate withLogAnalytics(
                String workspaceId, String workspaceKey, LogAnalyticsLogType logType, Map<String, String> metadata);
        }

        /**
         * The stage of the definition which contains all the minimum required inputs for the resource to be created
         * (via {@link WithCreate#create()}), but also allows for any other optional settings to be specified.
         */
        interface WithCreate
            extends WithRestartPolicy,
                WithSystemAssignedManagedServiceIdentity,
                WithUserAssignedManagedServiceIdentity,
                WithDnsPrefix,
                WithNetworkProfile,
                WithLogAnalytics,
                Creatable<ContainerGroup>,
                Resource.DefinitionWithTags<WithCreate> {
        }
    }

    /** The template for an update operation, containing all the settings that can be modified. */
    interface Update extends Resource.UpdateWithTags<Update>, Appliable<ContainerGroup> {
    }
}
