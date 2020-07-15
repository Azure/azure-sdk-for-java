/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.containerinstance.implementation;

import com.microsoft.azure.Resource;
import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.containerinstance.Container;
import com.microsoft.azure.management.containerinstance.ContainerExecRequest;
import com.microsoft.azure.management.containerinstance.ContainerExecRequestTerminalSize;
import com.microsoft.azure.management.containerinstance.ContainerExecResponse;
import com.microsoft.azure.management.containerinstance.ContainerGroup;
import com.microsoft.azure.management.containerinstance.ContainerGroupDiagnostics;
import com.microsoft.azure.management.containerinstance.ContainerGroupIpAddressType;
import com.microsoft.azure.management.containerinstance.ContainerGroupNetworkProfile;
import com.microsoft.azure.management.containerinstance.ContainerGroupNetworkProtocol;
import com.microsoft.azure.management.containerinstance.ContainerGroupRestartPolicy;
import com.microsoft.azure.management.containerinstance.DnsConfiguration;
import com.microsoft.azure.management.containerinstance.Event;
import com.microsoft.azure.management.containerinstance.ImageRegistryCredential;
import com.microsoft.azure.management.containerinstance.IpAddress;
import com.microsoft.azure.management.containerinstance.LogAnalytics;
import com.microsoft.azure.management.containerinstance.LogAnalyticsLogType;
import com.microsoft.azure.management.containerinstance.OperatingSystemTypes;
import com.microsoft.azure.management.containerinstance.Port;
import com.microsoft.azure.management.containerinstance.ResourceIdentityType;
import com.microsoft.azure.management.containerinstance.Volume;
import com.microsoft.azure.management.graphrbac.BuiltInRole;
import com.microsoft.azure.management.graphrbac.implementation.GraphRbacManager;
import com.microsoft.azure.management.graphrbac.implementation.RoleAssignmentHelper;
import com.microsoft.azure.management.msi.Identity;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableParentResourceImpl;
import com.microsoft.azure.management.resources.fluentcore.model.Creatable;
import com.microsoft.azure.management.resources.fluentcore.utils.SdkContext;
import com.microsoft.azure.management.resources.fluentcore.utils.Utils;
import com.microsoft.azure.management.storage.StorageAccount;
import com.microsoft.azure.management.storage.StorageAccountKey;
import com.microsoft.azure.management.storage.implementation.StorageManager;
import com.microsoft.azure.storage.CloudStorageAccount;
import com.microsoft.azure.storage.file.CloudFileClient;
import com.microsoft.azure.storage.file.CloudFileShare;
import org.apache.commons.lang3.tuple.Triple;
import rx.Completable;
import rx.Observable;
import rx.exceptions.Exceptions;
import rx.functions.Action2;
import rx.functions.Func0;
import rx.functions.Func1;

import java.net.URISyntaxException;
import java.security.InvalidKeyException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

/**
 * Implementation for ContainerGroup and its create interfaces.
 */
@LangDefinition
public class ContainerGroupImpl
        extends
        GroupableParentResourceImpl<
                ContainerGroup,
                ContainerGroupInner,
                ContainerGroupImpl,
                ContainerInstanceManager>
        implements ContainerGroup,
        ContainerGroup.Definition,
        ContainerGroup.Update {

    private final StorageManager storageManager;
    private String creatableStorageAccountKey;
    private Map<String, String> newFileShares;

    private Map<String, Container> containers;
    private Map<String, Volume> volumes;
    private List<String> imageRegistryServers;
    private int[] externalTcpPorts;
    private int[] externalUdpPorts;
    private ContainerGroupMsiHandler containerGroupMsiHandler;

    protected ContainerGroupImpl(String name, ContainerGroupInner innerObject, ContainerInstanceManager manager, final StorageManager storageManager, final GraphRbacManager rbacManager) {
        super(name, innerObject, manager);
        this.storageManager = storageManager;
        this.containerGroupMsiHandler = new ContainerGroupMsiHandler(rbacManager, this);
    }

    @Override
    protected void beforeCreating() {
    }

    @Override
    protected Observable<ContainerGroupInner> createInner() {
        this.containerGroupMsiHandler.processCreatedExternalIdentities();
        this.containerGroupMsiHandler.handleExternalIdentities();
        final ContainerGroupImpl self = this;

        if (!isInCreateMode()) {
            Resource resource = new Resource();
            resource.withLocation(self.regionName());
            resource.withTags(self.tags());
            return self.manager().inner().containerGroups()
                    .updateAsync(self.resourceGroupName(), self.name(), resource)
                    .flatMap(new Func1<ContainerGroupInner, Observable<ContainerGroupInner>>() {
                        @Override
                        public Observable<ContainerGroupInner> call(ContainerGroupInner containerGroupInner) {
                            return self.manager().inner().containerGroups()
                                    .getByResourceGroupAsync(self.resourceGroupName(), self.name());
                        }
                    });
        } else if (newFileShares == null || creatableStorageAccountKey == null) {
            return self.manager().inner().containerGroups().createOrUpdateAsync(self.resourceGroupName(), self.name(), self.inner());
        } else {
            final StorageAccount storageAccount = this.<StorageAccount>taskResult(this.creatableStorageAccountKey);
            return createFileShareAsync(storageAccount)
                    .collect(new Func0<List<Triple<String, String, String>>>() {
                        @Override
                        public List<Triple<String, String, String>> call() {
                            return new ArrayList<>();
                        }
                    }, new Action2<List<Triple<String, String, String>>, Triple<String, String, String>>() {
                        @Override
                        public void call(List<Triple<String, String, String>> cloudFileShares, Triple<String, String, String> fileShare) {
                            cloudFileShares.add(fileShare);
                        }
                    })
                    .flatMap(new Func1<List<Triple<String, String, String>>, Observable<? extends ContainerGroupInner>>() {
                        @Override
                        public Observable<? extends ContainerGroupInner> call(List<Triple<String, String, String>> fileShares) {
                            for (Triple<String, String, String> fileShareEntry : fileShares) {
                                self.defineVolume(fileShareEntry.getLeft())
                                        .withExistingReadWriteAzureFileShare(fileShareEntry.getMiddle())
                                        .withStorageAccountName(storageAccount.name())
                                        .withStorageAccountKey(fileShareEntry.getRight())
                                        .attach();
                            }
                            return self.manager().inner().containerGroups().createOrUpdateAsync(self.resourceGroupName(), self.name(), self.inner());
                        }
                    });
        }
    }

    private Observable<Triple<String, String, String>> createFileShareAsync(final StorageAccount storageAccount) {
        return storageAccount.getKeysAsync()
                .map(new Func1<List<StorageAccountKey>, String>() {
                    @Override
                    public String call(List<StorageAccountKey> storageAccountKeys) {
                        return storageAccountKeys.get(0).value();
                    }
                })
                .flatMap(new Func1<String, Observable<Triple<String, String, String>>>() {
                    CloudFileClient cloudFileClient;
                    @Override
                    public Observable<Triple<String, String, String>> call(final String storageAccountKey) {
                        try {
                            cloudFileClient = CloudStorageAccount.parse(Utils.getStorageConnectionString(
                                    storageAccount.name(),
                                    storageAccountKey,
                                    manager().inner().restClient()))
                                    .createCloudFileClient();
                        } catch (URISyntaxException syntaxException) {
                            throw Exceptions.propagate(syntaxException);
                        } catch (InvalidKeyException keyException) {
                            throw Exceptions.propagate(keyException);
                        }
                        return Observable.from(newFileShares.entrySet())
                                .flatMap(new Func1<Map.Entry<String, String>, Observable<Triple<String, String, String>>>() {
                                    @Override
                                    public Observable<Triple<String, String, String>> call(Map.Entry<String, String> fileShareEntry) {
                                        return createSingleFileShareAsync(cloudFileClient, fileShareEntry.getKey(), fileShareEntry.getValue(), storageAccountKey);
                                    }
                                });
                    }
                });
    }

    private Observable<Triple<String, String, String>> createSingleFileShareAsync(final CloudFileClient client, final String volumeName, final String fileShareName, final String storageAccountKey) {
        return Observable.fromCallable(new Callable<Triple<String, String, String>>() {
            @Override
            public Triple<String, String, String> call() throws Exception {
                CloudFileShare cloudFileShare = client.getShareReference(fileShareName);
                cloudFileShare.createIfNotExists();

                return Triple.of(volumeName, fileShareName, storageAccountKey);
            }
        });
    }

    @Override
    protected void afterCreating() {
        initializeChildrenFromInner();
    }

    @Override
    protected void initializeChildrenFromInner() {
        // Getting the container instances
        this.containers = new HashMap<>();
        if (this.inner().containers() != null && this.inner().containers().size() > 0) {
            for (Container containerInstance : this.inner().containers()) {
                this.containers.put(containerInstance.name(), containerInstance);
            }
        }

        // Getting the volumes
        this.volumes = new HashMap<>();
        if (this.inner().volumes() != null && this.inner().volumes().size() > 0) {
            for (Volume volume : this.inner().volumes()) {
                this.volumes.put(volume.name(), volume);
            }
        }

        // Getting the private image registry servers
        this.imageRegistryServers = new ArrayList<>();
        if (this.inner().imageRegistryCredentials() != null && this.inner().imageRegistryCredentials().size() > 0) {
            for (ImageRegistryCredential imageRegistry : this.inner().imageRegistryCredentials()) {
                this.imageRegistryServers.add(imageRegistry.server());
            }
        }

        // Splitting ports between TCP and UDP ports
        if (this.inner().ipAddress() != null && this.inner().ipAddress().ports() != null) {
            List<Port> tcpPorts = new ArrayList<>();
            List<Port> udpPorts = new ArrayList<>();
            for (Port port : this.inner().ipAddress().ports()) {
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
    public Observable<ContainerGroup> refreshAsync() {
        return super.refreshAsync().map(new Func1<ContainerGroup, ContainerGroup>() {
            @Override
            public ContainerGroup call(ContainerGroup containerGroup) {
                ContainerGroupImpl impl = (ContainerGroupImpl) containerGroup;
                impl.initializeChildrenFromInner();
                return impl;
            }
        });
    }

    @Override
    protected Observable<ContainerGroupInner> getInnerAsync() {
        return this.manager().inner().containerGroups().getByResourceGroupAsync(this.resourceGroupName(), this.name());
    }

    @Override
    public ContainerGroupImpl withLinux() {
        this.inner().withOsType(OperatingSystemTypes.LINUX);

        return this;
    }

    @Override
    public ContainerGroupImpl withWindows() {
        this.inner().withOsType(OperatingSystemTypes.WINDOWS);

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
        this.inner().withImageRegistryCredentials(null);

        return this;
    }

    @Override
    public ContainerGroupImpl withPrivateImageRegistry(String server, String username, String password) {
        if (this.inner().imageRegistryCredentials() == null) {
            this.inner().withImageRegistryCredentials(new ArrayList<ImageRegistryCredential>());
        }
        this.inner().imageRegistryCredentials().add(new ImageRegistryCredential()
                .withServer(server)
                .withUsername(username)
                .withPassword(password));

        return this;
    }

    @Override
    public ContainerGroupImpl withNewAzureFileShareVolume(String volumeName, String shareName) {
        if (this.newFileShares == null || this.creatableStorageAccountKey == null) {
            StorageAccount.DefinitionStages.WithGroup definitionWithGroup = this.storageManager
                    .storageAccounts()
                    .define(SdkContext.randomResourceName("fs", 24))
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
        if (this.inner().volumes() == null) {
            this.inner().withVolumes(new ArrayList<Volume>());
        }
        this.inner().volumes().add(new Volume()
                .withName(volumeName)
                .withEmptyDir(new Object()));

        return this;
    }

    @Override
    public VolumeImpl defineVolume(String name) {
        return new VolumeImpl(this, name);
    }

    @Override
    public ContainerGroupImpl withoutVolume() {
        this.inner().withVolumes(null);

        return this;
    }

    @Override
    public ContainerImpl defineContainerInstance(String name) {
        return new ContainerImpl(this, name);
    }

    @Override
    public ContainerGroupImpl withContainerInstance(String imageName) {
        return this.defineContainerInstance(this.name())
                .withImage(imageName)
                .withoutPorts()
                .withCpuCoreCount(1)
                .withMemorySizeInGB(1.5)
                .attach();
    }

    @Override
    public ContainerGroupImpl withContainerInstance(String imageName, int port) {
        return this.defineContainerInstance(this.name())
                .withImage(imageName)
                .withExternalTcpPort(port)
                .withCpuCoreCount(1)
                .withMemorySizeInGB(1.5)
                .attach();
    }

    @Override
    public ContainerGroupImpl withRestartPolicy(ContainerGroupRestartPolicy restartPolicy) {
        this.inner().withRestartPolicy(restartPolicy);

        return this;
    }

    @Override
    public ContainerGroupImpl withDnsPrefix(String dnsPrefix) {
        if (this.inner().ipAddress() == null) {
            this.inner().withIpAddress(new IpAddress());
        }
        this.inner().ipAddress().withDnsNameLabel(dnsPrefix).withType(ContainerGroupIpAddressType.PUBLIC);

        return this;
    }

    @Override
    public ContainerGroupImpl withNetworkProfileId(String subscriptionId, String resourceGroupName, String networkProfileName) {
        String networkProfileId = "/subscriptions/" + subscriptionId + "/resourceGroups/" + resourceGroupName + "/providers/Microsoft.Network/networkProfiles/" + networkProfileName;
        this.inner().withNetworkProfile(new ContainerGroupNetworkProfile().withId(networkProfileId));
        if (this.inner().ipAddress() == null) {
            this.inner().withIpAddress(new IpAddress());
        }
        this.inner().ipAddress().withType(ContainerGroupIpAddressType.PRIVATE);
        return this;
    }

    @Override
    public ContainerGroupImpl withDnsServerNames(List<String> dnsServerNames) {
        this.inner().withDnsConfig(new DnsConfiguration().withNameServers(dnsServerNames));
        return this;
    }

    @Override
    public ContainerGroupImpl withDnsConfiguration(List<String> dnsServerNames, String dnsSearchDomains, String dnsOptions) {
        this.inner().withDnsConfig(new DnsConfiguration().withNameServers(dnsServerNames).withSearchDomains(dnsSearchDomains).withOptions(dnsOptions));
        return this;
    }

    @Override
    public ContainerGroupImpl withLogAnalytics(String workspaceId, String workspaceKey) {
        this.inner().withDiagnostics(new ContainerGroupDiagnostics().withLogAnalytics(new LogAnalytics().withWorkspaceId(workspaceId).withWorkspaceKey(workspaceKey)));
        return this;
    }

    @Override
    public ContainerGroupImpl withLogAnalytics(String workspaceId, String workspaceKey, LogAnalyticsLogType logType, Map<String, String> metadata) {
        this.inner().withDiagnostics(new ContainerGroupDiagnostics().withLogAnalytics(new LogAnalytics().withWorkspaceId(workspaceId).withWorkspaceKey(workspaceKey).withLogType(logType).withMetadata(metadata)));
        return this;
    }

    @Override
    public Map<String, Container> containers() {
        return Collections.unmodifiableMap(this.containers);
    }

    @Override
    public Set<Port> externalPorts() {
        return Collections.unmodifiableSet(this.inner().ipAddress() != null && this.inner().ipAddress().ports() != null
                ? new HashSet<Port>(this.inner().ipAddress().ports())
                : new HashSet<Port>());
    }

    @Override
    public int[] externalTcpPorts() {
        return this.externalTcpPorts;
    }

    @Override
    public int[] externalUdpPorts() {
        return this.externalUdpPorts;
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
        return this.inner().restartPolicy();
    }

    @Override
    public String dnsPrefix() {
        if (this.inner().ipAddress() != null) {
            return this.inner().ipAddress().dnsNameLabel();
        } else {
            return null;
        }
    }

    @Override
    public String fqdn() {
        if (this.inner().ipAddress() != null) {
            return this.inner().ipAddress().fqdn();
        } else {
            return null;
        }
    }

    @Override
    public String ipAddress() {
        if (this.inner().ipAddress() != null) {
            return this.inner().ipAddress().ip();
        } else {
            return null;
        }
    }

    @Override
    public boolean isIPAddressPublic() {
        return this.inner().ipAddress() != null && this.inner().ipAddress().type() != null && this.inner().ipAddress().type() == ContainerGroupIpAddressType.PUBLIC;
    }

    @Override
    public boolean isIPAddressPrivate() {
        return this.inner().ipAddress() != null && this.inner().ipAddress().type() != null && this.inner().ipAddress().type() == ContainerGroupIpAddressType.PRIVATE;
    }

    @Override
    public OperatingSystemTypes osType() {
        return this.inner().osType();
    }

    @Override
    public String state() {
        if (this.inner().instanceView() != null && this.inner().instanceView().state() != null) {
            return this.inner().instanceView().state();
        } else {
            return null;
        }
    }

    @Override
    public String provisioningState() {
        if (this.inner().provisioningState() != null) {
            return this.inner().provisioningState();
        } else {
            return null;
        }
    }

    @Override
    public Set<Event> events() {
        return Collections.unmodifiableSet(this.inner().instanceView() != null && this.inner().instanceView().events() != null
                ? new HashSet<Event>(this.inner().instanceView().events())
                : new HashSet<Event>());
    }

    @Override
    public DnsConfiguration dnsConfig() {
        return this.inner().dnsConfig();
    }

    @Override
    public String networkProfileId() {
        return this.inner().networkProfile().id();
    }

    @Override
    public boolean isManagedServiceIdentityEnabled() {
        ResourceIdentityType type = this.managedServiceIdentityType();
        return type != null && !type.equals(ResourceIdentityType.NONE);
    }

    @Override
    public String systemAssignedManagedServiceIdentityTenantId() {
        if (this.inner().identity() != null) {
            return this.inner().identity().tenantId();
        }
        return null;
    }

    @Override
    public String systemAssignedManagedServiceIdentityPrincipalId() {
        if (this.inner().identity() != null) {
            return this.inner().identity().principalId();
        }
        return null;
    }

    @Override
    public ResourceIdentityType managedServiceIdentityType() {
        if (this.inner().identity() != null) {
            return this.inner().identity().type();
        }
        return null;
    }

    @Override
    public Set<String> userAssignedManagedServiceIdentityIds() {
        if (this.inner().identity() != null && this.inner().identity().userAssignedIdentities() != null) {
            return Collections.unmodifiableSet(new HashSet<String>(this.inner().identity().userAssignedIdentities().keySet()));
        }
        return Collections.unmodifiableSet(new HashSet<String>());
    }

    @Override
    public LogAnalytics logAnalytics() {
        return this.inner().diagnostics().logAnalytics();
    }

    @Override
    public void restart() {
        this.manager().inner().containerGroups().restart(this.resourceGroupName(), this.name());
    }

    @Override
    public Completable restartAsync() {
        return this.manager().inner().containerGroups().restartAsync(this.resourceGroupName(), this.name()).toCompletable();
    }

    @Override
    public void stop() {
        this.manager().inner().containerGroups().stop(this.resourceGroupName(), this.name());
    }

    @Override
    public Completable stopAsync() {
        return this.manager().inner().containerGroups().stopAsync(this.resourceGroupName(), this.name()).toCompletable();
    }

    @Override
    public String getLogContent(String containerName) {
        return this.manager().containerGroups().getLogContent(this.resourceGroupName(), this.name(), containerName);
    }

    @Override
    public String getLogContent(String containerName, int tailLineCount) {
        return this.manager().containerGroups().getLogContent(this.resourceGroupName(), this.name(), containerName, tailLineCount);
    }

    @Override
    public Observable<String> getLogContentAsync(String containerName) {
        return this.manager().containerGroups().getLogContentAsync(this.resourceGroupName(), this.name(), containerName);
    }

    @Override
    public Observable<String> getLogContentAsync(String containerName, int tailLineCount) {
        return this.manager().containerGroups().getLogContentAsync(this.resourceGroupName(), this.name(), containerName, tailLineCount);
    }

    @Override
    public ContainerExecResponse executeCommand(String containerName, String command, int row, int column) {
        return new ContainerExecResponseImpl(this.manager().inner().containers()
                .executeCommand(this.resourceGroupName(), this.name(), containerName,
                        new ContainerExecRequest()
                                .withCommand(command)
                                .withTerminalSize(new ContainerExecRequestTerminalSize()
                                        .withRows(row)
                                        .withCols(column))));
    }

    @Override
    public Observable<ContainerExecResponse> executeCommandAsync(String containerName, String command, int row, int column) {
        return this.manager().inner().containers()
                .executeCommandAsync(this.resourceGroupName(), this.name(), containerName,
                        new ContainerExecRequest()
                                .withCommand(command)
                                .withTerminalSize(new ContainerExecRequestTerminalSize()
                                        .withRows(row)
                                        .withCols(column)))
                .map(new Func1<ContainerExecResponseInner, ContainerExecResponse>() {
                    @Override
                    public ContainerExecResponse call(ContainerExecResponseInner containerExecResponseInner) {
                        return new ContainerExecResponseImpl(containerExecResponseInner);
                    }
                });
    }

    RoleAssignmentHelper.IdProvider idProvider() {
        return new RoleAssignmentHelper.IdProvider() {
            @Override
            public String principalId() {
                if (inner() != null && inner().identity() != null) {
                    return inner().identity().principalId();
                } else {
                    return null;
                }
            }

            @Override
            public String resourceId() {
                if (inner() != null) {
                    return inner().id();
                } else {
                    return null;
                }
            }
        };
    }
}
