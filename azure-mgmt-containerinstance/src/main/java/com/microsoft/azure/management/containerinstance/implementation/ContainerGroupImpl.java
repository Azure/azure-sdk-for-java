/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.containerinstance.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.containerinstance.Container;
import com.microsoft.azure.management.containerinstance.ContainerGroup;
import com.microsoft.azure.management.containerinstance.ContainerGroupNetworkProtocol;
import com.microsoft.azure.management.containerinstance.ImageRegistryCredential;
import com.microsoft.azure.management.containerinstance.OperatingSystemTypes;
import com.microsoft.azure.management.containerinstance.Port;
import com.microsoft.azure.management.containerinstance.Volume;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableParentResourceImpl;
import rx.Observable;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    ContainerGroup.Definition {

    private Map<String, Container> containers;
    private Map<String, Volume> volumes;
    private List<String> imageRegistryServers;
    private int[] externalTcpPorts;
    private int[] externalUdpPorts;

    protected ContainerGroupImpl(String name, ContainerGroupInner innerObject, ContainerInstanceManager manager) {
        super(name, innerObject, manager);
    }

    @Override
    protected void beforeCreating() {
    }

    @Override
    protected Observable<ContainerGroupInner> createInner() {
        return this.manager().inner().containerGroups().createOrUpdateAsync(this.resourceGroupName(), this.name(), this.inner());
    }

    @Override
    protected void afterCreating() {
        initializeChildrenFromInner();
    }

    @Override
    protected void initializeChildrenFromInner() {
        // Getting the container instances
        if (this.inner().containers() != null && this.inner().containers().size() > 0) {
            this.containers = new HashMap<>();
            for (Container containerInstance : this.inner().containers()) {
                this.containers.put(containerInstance.name(), containerInstance);
            }
        }

        // Getting the volumes
        if (this.inner().volumes() != null && this.inner().volumes().size() > 0) {
            this.volumes = new HashMap<>();
            for (Volume volume : this.inner().volumes()) {
                this.volumes.put(volume.name(), volume);
            }
        }

        // Getting the private image registry servers
        if (this.inner().imageRegistryCredentials() != null && this.inner().imageRegistryCredentials().size() > 0) {
            this.imageRegistryServers = new ArrayList<>();
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
            .withCPUCoreCount(1)
            .withMemorySizeInGB(1.5)
            .attach();
    }

    @Override
    public ContainerGroupImpl withContainerInstance(String imageName, int port) {
        return this.defineContainerInstance(this.name())
            .withImage(imageName)
            .withExternalTcpPort(port)
            .withCPUCoreCount(1)
            .withMemorySizeInGB(1.5)
            .attach();
    }

    @Override
    public Map<String, Container> containers() {
        return this.containers;
    }

    @Override
    public Collection<Port> externalPorts() {
        if (this.inner().ipAddress() != null && this.inner().ipAddress().ports() != null) {
            return this.inner().ipAddress().ports();
        } else {
            return null;
        }
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
        return this.volumes;
    }

    @Override
    public Collection<String> imageRegistryServers() {
        return this.imageRegistryServers;
    }

    @Override
    public String restartPolicy() {
        if (this.inner().restartPolicy() != null) {
            return this.inner().restartPolicy().toString();
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
    public String ipAddressType() {
        if (this.inner().ipAddress() != null) {
            return this.inner().ipAddress().type();
        } else {
            return null;
        }
    }

    @Override
    public String osType() {
        if (this.inner().osType() != null) {
            return this.inner().osType().toString();
        } else {
            return null;
        }
    }

    @Override
    public String state() {
        if (this.inner().state() != null) {
            return this.inner().state();
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
    public String getLogContent(String containerName) {
        return this.manager().containerGroups().getLogContent(this.resourceGroupName(), containerName, this.name());
    }

    @Override
    public String getLogContent(String containerName, int tailLineCount) {
        return this.manager().containerGroups().getLogContent(this.resourceGroupName(), containerName, this.name(), tailLineCount);
    }

    @Override
    public Observable<String> getLogContentAsync(String containerName) {
        return this.manager().containerGroups().getLogContentAsync(this.resourceGroupName(), containerName, this.name());
    }

    @Override
    public Observable<String> getLogContentAsync(String containerName, int tailLineCount) {
        return this.manager().containerGroups().getLogContentAsync(this.resourceGroupName(), containerName, this.name(), tailLineCount);
    }

}
