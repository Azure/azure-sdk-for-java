// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerinstance.implementation;

import com.azure.core.management.Resource;
import com.azure.resourcemanager.authorization.utils.RoleAssignmentHelper;
import com.azure.resourcemanager.authorization.models.BuiltInRole;
import com.azure.resourcemanager.containerinstance.ContainerInstanceManager;
import com.azure.resourcemanager.containerinstance.fluent.models.ContainerGroupInner;
import com.azure.resourcemanager.containerinstance.models.Container;
import com.azure.resourcemanager.containerinstance.models.ContainerAttachResult;
import com.azure.resourcemanager.containerinstance.models.ContainerExecRequest;
import com.azure.resourcemanager.containerinstance.models.ContainerExecRequestTerminalSize;
import com.azure.resourcemanager.containerinstance.models.ContainerExecResponse;
import com.azure.resourcemanager.containerinstance.models.ContainerGroup;
import com.azure.resourcemanager.containerinstance.models.ContainerGroupDiagnostics;
import com.azure.resourcemanager.containerinstance.models.ContainerGroupIpAddressType;
import com.azure.resourcemanager.containerinstance.models.ContainerGroupNetworkProfile;
import com.azure.resourcemanager.containerinstance.models.ContainerGroupNetworkProtocol;
import com.azure.resourcemanager.containerinstance.models.ContainerGroupRestartPolicy;
import com.azure.resourcemanager.containerinstance.models.DnsConfiguration;
import com.azure.resourcemanager.containerinstance.models.Event;
import com.azure.resourcemanager.containerinstance.models.ImageRegistryCredential;
import com.azure.resourcemanager.containerinstance.models.IpAddress;
import com.azure.resourcemanager.containerinstance.models.LogAnalytics;
import com.azure.resourcemanager.containerinstance.models.LogAnalyticsLogType;
import com.azure.resourcemanager.containerinstance.models.OperatingSystemTypes;
import com.azure.resourcemanager.containerinstance.models.Port;
import com.azure.resourcemanager.containerinstance.models.ResourceIdentityType;
import com.azure.resourcemanager.containerinstance.models.Volume;
import com.azure.resourcemanager.msi.models.Identity;
import com.azure.resourcemanager.network.fluent.models.IpConfigurationProfileInner;
import com.azure.resourcemanager.network.fluent.models.NetworkProfileInner;
import com.azure.resourcemanager.network.fluent.models.SubnetInner;
import com.azure.resourcemanager.network.models.ContainerNetworkInterfaceConfiguration;
import com.azure.resourcemanager.network.models.Network;
import com.azure.resourcemanager.resources.fluentcore.arm.models.implementation.GroupableParentResourceImpl;
import com.azure.resourcemanager.resources.fluentcore.model.Creatable;
import com.azure.resourcemanager.resources.fluentcore.utils.ResourceManagerUtils;
import com.azure.resourcemanager.storage.models.StorageAccount;
import com.azure.storage.file.share.ShareServiceAsyncClient;
import com.azure.storage.file.share.ShareServiceClientBuilder;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/** Implementation for ContainerGroup and its create interfaces. */
public class ContainerGroupImpl
    extends GroupableParentResourceImpl<
        ContainerGroup, ContainerGroupInner, ContainerGroupImpl, ContainerInstanceManager>
    implements ContainerGroup, ContainerGroup.Definition, ContainerGroup.Update {

    private String creatableStorageAccountKey;
    private Creatable<Network> creatableVirtualNetwork;
    private NetworkProfileInner creatableNetworkProfileInner;
    private String creatableNetworkProfileName;
    private Map<String, String> newFileShares;

    private Map<String, Container> containers;
    private Map<String, Volume> volumes;
    private List<String> imageRegistryServers;
    private int[] externalTcpPorts;
    private int[] externalUdpPorts;
    private ContainerGroupMsiHandler containerGroupMsiHandler;

    protected ContainerGroupImpl(String name, ContainerGroupInner innerObject, ContainerInstanceManager manager) {
        super(name, innerObject, manager);
        this.containerGroupMsiHandler = new ContainerGroupMsiHandler(this);
        initializeChildrenFromInner();
    }

    private Mono<Void> beforeCreation() {
        Mono<Void> mono = Mono.empty();
        if (creatableVirtualNetwork != null) {
            mono =
                mono
                    .then(creatableVirtualNetwork.createAsync())
                    .flatMap(
                        network -> {
                            creatableVirtualNetwork = null;
                            return Mono.empty();
                        });
        }
        if (creatableNetworkProfileName != null && creatableNetworkProfileInner != null) {
            mono =
                mono
                    .then(
                        manager()
                            .networkManager()
                            .serviceClient()
                            .getNetworkProfiles()
                            .createOrUpdateAsync(
                                resourceGroupName(), creatableNetworkProfileName, creatableNetworkProfileInner)
                            .flatMap(
                                profile -> {
                                    creatableNetworkProfileName = null;
                                    creatableNetworkProfileInner = null;
                                    return Mono.empty();
                                }));
        }
        return mono;
    }

    @Override
    protected Mono<ContainerGroupInner> createInner() {
        this.containerGroupMsiHandler.processCreatedExternalIdentities();
        this.containerGroupMsiHandler.handleExternalIdentities();
        final ContainerGroupImpl self = this;

        if (!isInCreateMode()) {
            Resource resource = new Resource();
            resource.withLocation(self.regionName());
            resource.withTags(self.tags());
            return beforeCreation()
                .then(
                    manager()
                        .serviceClient()
                        .getContainerGroups()
                        .updateAsync(self.resourceGroupName(), self.name(), resource));
        } else if (newFileShares == null || creatableStorageAccountKey == null) {
            return beforeCreation()
                .then(manager().serviceClient().getContainerGroups()
                    .createOrUpdateAsync(resourceGroupName(), name(), innerModel()));
        } else {
            final StorageAccount storageAccount = this.taskResult(this.creatableStorageAccountKey);
            return beforeCreation()
                .thenMany(createFileShareAsync(storageAccount))
                .map(
                    volumeParameters ->
                        this
                            .defineVolume(volumeParameters.volumeName)
                            .withExistingReadWriteAzureFileShare(volumeParameters.fileShareName)
                            .withStorageAccountName(storageAccount.name())
                            .withStorageAccountKey(volumeParameters.storageAccountKey)
                            .attach())
                .then(
                    this
                        .manager()
                        .serviceClient()
                        .getContainerGroups()
                        .createOrUpdateAsync(resourceGroupName(), name(), innerModel()));
        }
    }

    private static class VolumeParameters {
        private String volumeName;
        private String fileShareName;
        private String storageAccountKey;

        VolumeParameters(String volumeName, String fileShareName, String storageAccountKey) {
            this.volumeName = volumeName;
            this.fileShareName = fileShareName;
            this.storageAccountKey = storageAccountKey;
        }
    }

    private Flux<VolumeParameters> createFileShareAsync(final StorageAccount storageAccount) {
        return storageAccount
            .getKeysAsync()
            .map(storageAccountKeys -> storageAccountKeys.get(0).value())
            .flatMapMany(
                key -> {
                    ShareServiceAsyncClient shareServiceAsyncClient =
                        new ShareServiceClientBuilder()
                            .connectionString(
                                ResourceManagerUtils.getStorageConnectionString(
                                    storageAccount.name(), key, manager().environment()))
                            .httpClient(manager().httpPipeline().getHttpClient())
                            .buildAsyncClient();

                    Objects.requireNonNull(newFileShares);
                    return Flux
                        .fromIterable(newFileShares.entrySet())
                        .flatMap(
                            fileShareEntry ->
                                createSingleFileShareAsync(
                                    shareServiceAsyncClient, fileShareEntry.getKey(), fileShareEntry.getValue(), key));
                });
    }

    private Mono<VolumeParameters> createSingleFileShareAsync(
        final ShareServiceAsyncClient client,
        final String volumeName,
        final String fileShareName,
        final String storageAccountKey) {
        return client
            .createShare(fileShareName)
            .then(Mono.just(new VolumeParameters(volumeName, fileShareName, storageAccountKey)));
    }

    @Override
    protected void afterCreating() {
        initializeChildrenFromInner();
    }

    @Override
    protected void initializeChildrenFromInner() {
        // Getting the container instances
        this.containers = new HashMap<>();
        if (this.innerModel().containers() != null && this.innerModel().containers().size() > 0) {
            for (Container containerInstance : this.innerModel().containers()) {
                this.containers.put(containerInstance.name(), containerInstance);
            }
        }

        // Getting the volumes
        this.volumes = new HashMap<>();
        if (this.innerModel().volumes() != null && this.innerModel().volumes().size() > 0) {
            for (Volume volume : this.innerModel().volumes()) {
                this.volumes.put(volume.name(), volume);
            }
        }

        // Getting the private image registry servers
        this.imageRegistryServers = new ArrayList<>();
        if (this.innerModel().imageRegistryCredentials() != null
            && this.innerModel().imageRegistryCredentials().size() > 0) {
            for (ImageRegistryCredential imageRegistry : this.innerModel().imageRegistryCredentials()) {
                this.imageRegistryServers.add(imageRegistry.server());
            }
        }

        // Splitting ports between TCP and UDP ports
        if (this.innerModel().ipAddress() != null && this.innerModel().ipAddress().ports() != null) {
            List<Port> tcpPorts = new ArrayList<>();
            List<Port> udpPorts = new ArrayList<>();
            for (Port port : this.innerModel().ipAddress().ports()) {
                if (port.protocol().equals(ContainerGroupNetworkProtocol.TCP)) {
                    tcpPorts.add(port);
                } else if (port.protocol().equals(ContainerGroupNetworkProtocol.UDP)) {
                    udpPorts.add(port);
                }
            }
            this.externalTcpPorts = new int[tcpPorts.size()];
            for (int i = 0; i < this.externalTcpPorts.length; i++) {
                this.externalTcpPorts[i] = tcpPorts.get(i).port();
            }
            this.externalUdpPorts = new int[udpPorts.size()];
            for (int i = 0; i < this.externalTcpPorts.length; i++) {
                this.externalTcpPorts[i] = tcpPorts.get(i).port();
            }
        } else {
            this.externalTcpPorts = new int[0];
            this.externalUdpPorts = new int[0];
        }
    }

    // Verbs

    @Override
    public Mono<ContainerGroup> refreshAsync() {
        return super
            .refreshAsync()
            .map(
                containerGroup -> {
                    ContainerGroupImpl impl = (ContainerGroupImpl) containerGroup;
                    impl.initializeChildrenFromInner();
                    return impl;
                });
    }

    @Override
    protected Mono<ContainerGroupInner> getInnerAsync() {
        return this
            .manager()
            .serviceClient()
            .getContainerGroups()
            .getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public ContainerGroupImpl withLinux() {
        this.innerModel().withOsType(OperatingSystemTypes.LINUX);

        return this;
    }

    @Override
    public ContainerGroupImpl withWindows() {
        this.innerModel().withOsType(OperatingSystemTypes.WINDOWS);

        return this;
    }

    @Override
    public ContainerGroupImpl withSystemAssignedManagedServiceIdentity() {
        this.containerGroupMsiHandler.withLocalManagedServiceIdentity();
        return this;
    }

    @Override
    public ContainerGroupImpl withSystemAssignedIdentityBasedAccessTo(String resourceId, BuiltInRole role) {
        this.containerGroupMsiHandler.withAccessTo(resourceId, role);
        return this;
    }

    @Override
    public ContainerGroupImpl withSystemAssignedIdentityBasedAccessToCurrentResourceGroup(BuiltInRole role) {
        this.containerGroupMsiHandler.withAccessToCurrentResourceGroup(role);
        return this;
    }

    @Override
    public ContainerGroupImpl withSystemAssignedIdentityBasedAccessTo(String resourceId, String roleDefinitionId) {
        this.containerGroupMsiHandler.withAccessTo(resourceId, roleDefinitionId);
        return this;
    }

    @Override
    public ContainerGroupImpl withSystemAssignedIdentityBasedAccessToCurrentResourceGroup(String roleDefinitionId) {
        this.containerGroupMsiHandler.withAccessToCurrentResourceGroup(roleDefinitionId);
        return this;
    }

    @Override
    public ContainerGroupImpl withNewUserAssignedManagedServiceIdentity(Creatable<Identity> creatableIdentity) {
        this.containerGroupMsiHandler.withNewExternalManagedServiceIdentity(creatableIdentity);
        return this;
    }

    @Override
    public ContainerGroupImpl withExistingUserAssignedManagedServiceIdentity(Identity identity) {
        this.containerGroupMsiHandler.withExistingExternalManagedServiceIdentity(identity);
        return this;
    }

    @Override
    public ContainerGroupImpl withPublicImageRegistryOnly() {
        this.innerModel().withImageRegistryCredentials(null);

        return this;
    }

    @Override
    public ContainerGroupImpl withPrivateImageRegistry(String server, String username, String password) {
        if (this.innerModel().imageRegistryCredentials() == null) {
            this.innerModel().withImageRegistryCredentials(new ArrayList<ImageRegistryCredential>());
        }
        this
            .innerModel()
            .imageRegistryCredentials()
            .add(new ImageRegistryCredential().withServer(server).withUsername(username).withPassword(password));

        return this;
    }

    @Override
    public ContainerGroupImpl withNewAzureFileShareVolume(String volumeName, String shareName) {
        if (this.newFileShares == null || this.creatableStorageAccountKey == null) {
            StorageAccount.DefinitionStages.WithGroup definitionWithGroup =
                manager()
                    .storageManager()
                    .storageAccounts()
                    .define(manager().resourceManager().internalContext().randomResourceName("fs", 24))
                    .withRegion(this.regionName());
            Creatable<StorageAccount> creatable;
            if (this.creatableGroup != null) {
                creatable = definitionWithGroup.withNewResourceGroup(this.creatableGroup);
            } else {
                creatable = definitionWithGroup.withExistingResourceGroup(this.resourceGroupName());
            }
            this.creatableStorageAccountKey = this.addDependency(creatable);
            this.newFileShares = new HashMap<>();
        }
        this.newFileShares.put(volumeName, shareName);

        return this;
    }

    @Override
    public ContainerGroupImpl withEmptyDirectoryVolume(String volumeName) {
        if (this.innerModel().volumes() == null) {
            this.innerModel().withVolumes(new ArrayList<Volume>());
        }
        this.innerModel().volumes().add(new Volume().withName(volumeName).withEmptyDir(new Object()));

        return this;
    }

    @Override
    public VolumeImpl defineVolume(String name) {
        return new VolumeImpl(this, name);
    }

    @Override
    public ContainerGroupImpl withoutVolume() {
        this.innerModel().withVolumes(null);

        return this;
    }

    @Override
    public ContainerImpl defineContainerInstance(String name) {
        return new ContainerImpl(this, name);
    }

    @Override
    public ContainerGroupImpl withContainerInstance(String imageName) {
        return this
            .defineContainerInstance(this.name())
            .withImage(imageName)
            .withoutPorts()
            .withCpuCoreCount(1)
            .withMemorySizeInGB(1.5)
            .attach();
    }

    @Override
    public ContainerGroupImpl withContainerInstance(String imageName, int port) {
        return this
            .defineContainerInstance(this.name())
            .withImage(imageName)
            .withExternalTcpPort(port)
            .withCpuCoreCount(1)
            .withMemorySizeInGB(1.5)
            .attach();
    }

    @Override
    public ContainerGroupImpl withRestartPolicy(ContainerGroupRestartPolicy restartPolicy) {
        this.innerModel().withRestartPolicy(restartPolicy);

        return this;
    }

    @Override
    public ContainerGroupImpl withDnsPrefix(String dnsPrefix) {
        if (this.innerModel().ipAddress() == null) {
            this.innerModel().withIpAddress(new IpAddress());
        }
        this.innerModel().ipAddress().withDnsNameLabel(dnsPrefix).withType(ContainerGroupIpAddressType.PUBLIC);

        return this;
    }

    @Override
    public ContainerGroupImpl withExistingNetworkProfile(
        String subscriptionId, String resourceGroupName, String networkProfileName) {
        String networkProfileId =
            "/subscriptions/"
                + subscriptionId
                + "/resourceGroups/"
                + resourceGroupName
                + "/providers/Microsoft.Network/networkProfiles/"
                + networkProfileName;
        return this.withExistingNetworkProfile(networkProfileId);
    }

    @Override
    public ContainerGroupImpl withExistingNetworkProfile(String networkProfileId) {
        this.innerModel().withNetworkProfile(new ContainerGroupNetworkProfile().withId(networkProfileId));
        if (this.innerModel().ipAddress() == null) {
            this.innerModel().withIpAddress(new IpAddress());
        }
        this.innerModel().ipAddress().withType(ContainerGroupIpAddressType.PRIVATE);
        return this;
    }

    @Override
    public ContainerGroupImpl withNewNetworkProfileOnExistingVirtualNetwork(
        String virtualNetworkId, String subnetName) {
        String randomNetworkProfileName = manager().resourceManager().internalContext()
            .randomResourceName("aci-profile-", 20);
        return this.withNewNetworkProfileOnExistingVirtualNetwork(randomNetworkProfileName,
            virtualNetworkId, subnetName);
    }

    @Override
    public ContainerGroupImpl withNewNetworkProfileOnExistingVirtualNetwork(
        String networkProfileName, String virtualNetworkId, String subnetName) {
        creatableNetworkProfileName = networkProfileName;
        String subnetId = String.format("%s/subnets/%s", virtualNetworkId, subnetName);
        SubnetInner subnetInner = new SubnetInner();
        subnetInner.withId(subnetId);
        creatableNetworkProfileInner =
            new NetworkProfileInner()
                .withContainerNetworkInterfaceConfigurations(
                    Collections
                        .singletonList(
                            new ContainerNetworkInterfaceConfiguration()
                                .withName("eth0")
                                .withIpConfigurations(
                                    Collections
                                        .singletonList(
                                            new IpConfigurationProfileInner()
                                                .withName("ipconfig0")
                                                .withSubnet(subnetInner)))));
        creatableNetworkProfileInner.withLocation(regionName());

        return this.withExistingNetworkProfile(
            manager().subscriptionId(), resourceGroupName(), creatableNetworkProfileName);
    }

    @Override
    public ContainerGroupImpl withNewVirtualNetwork(String addressSpace) {
        String virtualNetworkName = manager().resourceManager().internalContext().randomResourceName("net", 20);
        String subnetName = "subnet0";

        creatableVirtualNetwork =
            manager()
                .networkManager()
                .networks()
                .define(virtualNetworkName)
                .withRegion(region())
                .withExistingResourceGroup(resourceGroupName())
                .withAddressSpace(addressSpace)
                .defineSubnet(subnetName)
                .withAddressPrefix(addressSpace)
                .withDelegation("Microsoft.ContainerInstance/containerGroups")
                .attach();

        String virtualNetworkId =
            String
                .format(
                    "/subscriptions/%s/resourceGroups/%s/providers/Microsoft.Network/virtualNetworks/%s",
                    manager().subscriptionId(), resourceGroupName(), virtualNetworkName);
        return withNewNetworkProfileOnExistingVirtualNetwork(virtualNetworkId, subnetName);
    }

    @Override
    public ContainerGroupImpl withDnsServerNames(List<String> dnsServerNames) {
        this.innerModel().withDnsConfig(new DnsConfiguration().withNameServers(dnsServerNames));
        return this;
    }

    @Override
    public ContainerGroupImpl withDnsConfiguration(
        List<String> dnsServerNames, String dnsSearchDomains, String dnsOptions) {
        this
            .innerModel()
            .withDnsConfig(
                new DnsConfiguration()
                    .withNameServers(dnsServerNames)
                    .withSearchDomains(dnsSearchDomains)
                    .withOptions(dnsOptions));
        return this;
    }

    @Override
    public ContainerGroupImpl withLogAnalytics(String workspaceId, String workspaceKey) {
        this
            .innerModel()
            .withDiagnostics(
                new ContainerGroupDiagnostics()
                    .withLogAnalytics(new LogAnalytics().withWorkspaceId(workspaceId).withWorkspaceKey(workspaceKey)));
        return this;
    }

    @Override
    public ContainerGroupImpl withLogAnalytics(
        String workspaceId, String workspaceKey, LogAnalyticsLogType logType, Map<String, String> metadata) {
        this
            .innerModel()
            .withDiagnostics(
                new ContainerGroupDiagnostics()
                    .withLogAnalytics(
                        new LogAnalytics()
                            .withWorkspaceId(workspaceId)
                            .withWorkspaceKey(workspaceKey)
                            .withLogType(logType)
                            .withMetadata(metadata)));
        return this;
    }

    @Override
    public Map<String, Container> containers() {
        return Collections.unmodifiableMap(this.containers);
    }

    @Override
    public Set<Port> externalPorts() {
        return Collections
            .unmodifiableSet(
                this.innerModel().ipAddress() != null && this.innerModel().ipAddress().ports() != null
                    ? new HashSet<Port>(this.innerModel().ipAddress().ports())
                    : new HashSet<Port>());
    }

    @Override
    public int[] externalTcpPorts() {
        return this.externalTcpPorts.clone();
    }

    @Override
    public int[] externalUdpPorts() {
        return this.externalUdpPorts.clone();
    }

    @Override
    public Map<String, Volume> volumes() {
        return Collections.unmodifiableMap(this.volumes);
    }

    @Override
    public Collection<String> imageRegistryServers() {
        return Collections.unmodifiableCollection(this.imageRegistryServers);
    }

    @Override
    public ContainerGroupRestartPolicy restartPolicy() {
        return this.innerModel().restartPolicy();
    }

    @Override
    public String dnsPrefix() {
        if (this.innerModel().ipAddress() != null) {
            return this.innerModel().ipAddress().dnsNameLabel();
        } else {
            return null;
        }
    }

    @Override
    public String fqdn() {
        if (this.innerModel().ipAddress() != null) {
            return this.innerModel().ipAddress().fqdn();
        } else {
            return null;
        }
    }

    @Override
    public String ipAddress() {
        if (this.innerModel().ipAddress() != null) {
            return this.innerModel().ipAddress().ip();
        } else {
            return null;
        }
    }

    @Override
    public boolean isIPAddressPublic() {
        return this.innerModel().ipAddress() != null
            && this.innerModel().ipAddress().type() != null
            && this.innerModel().ipAddress().type() == ContainerGroupIpAddressType.PUBLIC;
    }

    @Override
    public boolean isIPAddressPrivate() {
        return this.innerModel().ipAddress() != null
            && this.innerModel().ipAddress().type() != null
            && this.innerModel().ipAddress().type() == ContainerGroupIpAddressType.PRIVATE;
    }

    @Override
    public OperatingSystemTypes osType() {
        return this.innerModel().osType();
    }

    @Override
    public String state() {
        if (this.innerModel().instanceView() != null && this.innerModel().instanceView().state() != null) {
            return this.innerModel().instanceView().state();
        } else {
            return null;
        }
    }

    @Override
    public String provisioningState() {
        if (this.innerModel().provisioningState() != null) {
            return this.innerModel().provisioningState();
        } else {
            return null;
        }
    }

    @Override
    public Set<Event> events() {
        return Collections
            .unmodifiableSet(
                this.innerModel().instanceView() != null && this.innerModel().instanceView().events() != null
                    ? new HashSet<Event>(this.innerModel().instanceView().events())
                    : new HashSet<Event>());
    }

    @Override
    public DnsConfiguration dnsConfig() {
        return this.innerModel().dnsConfig();
    }

    @Override
    public String networkProfileId() {
        return this.innerModel().networkProfile().id();
    }

    @Override
    public boolean isManagedServiceIdentityEnabled() {
        ResourceIdentityType type = this.managedServiceIdentityType();
        return type != null && !type.equals(ResourceIdentityType.NONE);
    }

    @Override
    public String systemAssignedManagedServiceIdentityTenantId() {
        if (this.innerModel().identity() != null) {
            return this.innerModel().identity().tenantId();
        }
        return null;
    }

    @Override
    public String systemAssignedManagedServiceIdentityPrincipalId() {
        if (this.innerModel().identity() != null) {
            return this.innerModel().identity().principalId();
        }
        return null;
    }

    @Override
    public ResourceIdentityType managedServiceIdentityType() {
        if (this.innerModel().identity() != null) {
            return this.innerModel().identity().type();
        }
        return null;
    }

    @Override
    public Set<String> userAssignedManagedServiceIdentityIds() {
        if (this.innerModel().identity() != null && this.innerModel().identity().userAssignedIdentities() != null) {
            return Collections
                .unmodifiableSet(new HashSet<String>(this.innerModel().identity().userAssignedIdentities().keySet()));
        }
        return Collections.unmodifiableSet(new HashSet<String>());
    }

    @Override
    public LogAnalytics logAnalytics() {
        return this.innerModel().diagnostics().logAnalytics();
    }

    @Override
    public void restart() {
        this.manager().serviceClient().getContainerGroups().restart(this.resourceGroupName(), this.name());
    }

    @Override
    public Mono<Void> restartAsync() {
        return this.manager().serviceClient().getContainerGroups().restartAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public void stop() {
        this.manager().serviceClient().getContainerGroups().stop(this.resourceGroupName(), this.name());
    }

    @Override
    public Mono<Void> stopAsync() {
        return this.manager().serviceClient().getContainerGroups().stopAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public String getLogContent(String containerName) {
        return this.manager().containerGroups().getLogContent(this.resourceGroupName(), this.name(), containerName);
    }

    @Override
    public String getLogContent(String containerName, int tailLineCount) {
        return this
            .manager()
            .containerGroups()
            .getLogContent(this.resourceGroupName(), this.name(), containerName, tailLineCount);
    }

    @Override
    public Mono<String> getLogContentAsync(String containerName) {
        return this
            .manager()
            .containerGroups()
            .getLogContentAsync(this.resourceGroupName(), this.name(), containerName);
    }

    @Override
    public Mono<String> getLogContentAsync(String containerName, int tailLineCount) {
        return this
            .manager()
            .containerGroups()
            .getLogContentAsync(this.resourceGroupName(), this.name(), containerName, tailLineCount);
    }

    @Override
    public ContainerExecResponse executeCommand(String containerName, String command, int row, int column) {
        return executeCommandAsync(containerName, command, row, column).block();
    }

    @Override
    public Mono<ContainerExecResponse> executeCommandAsync(String containerName, String command, int row, int column) {
        return this
            .manager()
            .serviceClient()
            .getContainers()
            .executeCommandAsync(
                this.resourceGroupName(),
                this.name(),
                containerName,
                new ContainerExecRequest()
                    .withCommand(command)
                    .withTerminalSize(new ContainerExecRequestTerminalSize().withRows(row).withCols(column)))
            .map(ContainerExecResponseImpl::new);
    }

    @Override
    public ContainerAttachResult attachOutputStream(String containerName) {
        return this.attachOutputStreamAsync(containerName).block();
    }

    @Override
    public Mono<ContainerAttachResult> attachOutputStreamAsync(String containerName) {
        return this.manager().containerGroups().attachOutputStreamAsync(
            this.resourceGroupName(), this.name(), containerName);
    }

    @Override
    public ContainerAttachResult attachOutputStream(Container container) {
        return this.attachOutputStreamAsync(container).block();
    }

    @Override
    public Mono<ContainerAttachResult> attachOutputStreamAsync(Container container) {
        return this.attachOutputStreamAsync(container.name());
    }

    RoleAssignmentHelper.IdProvider idProvider() {
        return new RoleAssignmentHelper.IdProvider() {
            @Override
            public String principalId() {
                if (innerModel() != null && innerModel().identity() != null) {
                    return innerModel().identity().principalId();
                } else {
                    return null;
                }
            }

            @Override
            public String resourceId() {
                if (innerModel() != null) {
                    return innerModel().id();
                } else {
                    return null;
                }
            }
        };
    }
}
