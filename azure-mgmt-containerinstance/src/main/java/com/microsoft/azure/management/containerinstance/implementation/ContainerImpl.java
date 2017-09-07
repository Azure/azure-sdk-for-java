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
import com.microsoft.azure.management.containerinstance.ContainerPort;
import com.microsoft.azure.management.containerinstance.EnvironmentVariable;
import com.microsoft.azure.management.containerinstance.IpAddress;
import com.microsoft.azure.management.containerinstance.Port;
import com.microsoft.azure.management.containerinstance.ResourceRequests;
import com.microsoft.azure.management.containerinstance.ResourceRequirements;
import com.microsoft.azure.management.containerinstance.VolumeMount;

import java.util.ArrayList;
import java.util.Map;

/**
 * Implementation for container group's container instance definition stages interface.
 */
@LangDefinition
class ContainerImpl implements
    ContainerGroup.DefinitionStages.ContainerInstanceDefinitionStages.ContainerInstanceDefinition<ContainerGroup.DefinitionStages.WithNextContainerInstance> {
    private Container innerContainer;
    private ContainerGroupImpl parent;

    ContainerImpl(ContainerGroupImpl parent, String containerName) {
        this.parent = parent;
        this.innerContainer = new Container()
            .withName(containerName)
            .withResources(new ResourceRequirements()
                .withRequests(new ResourceRequests()
                    .withCpu(1)
                    .withMemoryInGB(1.5)));
    }

    @Override
    public ContainerGroupImpl attach() {
        if (parent.inner().containers() == null) {
            parent.inner().withContainers(new ArrayList<Container>());
        }
        parent.inner().containers().add(innerContainer);

        return parent;
    }

    @Override
    public ContainerImpl withImage(String imageName) {
        innerContainer.withImage(imageName);
        return this;
    }

    @Override
    public ContainerImpl withoutPorts() {
        innerContainer.withPorts(null);

        return this;
    }

    @Override
    public ContainerImpl withExternalTcpPorts(int... ports) {
        for (int port : ports) {
            this.withExternalTcpPort(port);
        }

        return this;
    }

    @Override
    public ContainerImpl withExternalTcpPort(int port) {
        ensureParentIpAddress().ports().add(new Port()
            .withPort(port)
            .withProtocol(ContainerGroupNetworkProtocol.TCP));
        this.withInternalPort(port);

        return this;
    }

    private IpAddress ensureParentIpAddress() {
        if (parent.inner().ipAddress() == null) {
            parent.inner().withIpAddress(new IpAddress()
                .withType("Public")
                .withPorts(new ArrayList<Port>()));
        }

        return parent.inner().ipAddress();
    }

    @Override
    public ContainerImpl withExternalUdpPorts(int... ports) {
        for (int port : ports) {
            this.withExternalUdpPort(port);
        }

        return this;
    }

    @Override
    public ContainerImpl withExternalUdpPort(int port) {
        ensureParentIpAddress().ports().add(new Port()
            .withPort(port)
            .withProtocol(ContainerGroupNetworkProtocol.UDP));
        this.withInternalPort(port);

        return this;
    }

    @Override
    public ContainerImpl withInternalPorts(int... ports) {
        for (int port : ports) {
            this.withInternalPort(port);
        }

        return this;
    }

    @Override
    public ContainerImpl withInternalPort(int port) {
        if (innerContainer.ports() == null) {
            innerContainer.withPorts(new ArrayList<ContainerPort>());
        }
        innerContainer.ports().add(new ContainerPort().withPort(port));

        return this;
    }

    @Override
    public ContainerImpl withCpuCoreCount(double cpuCoreCount) {
        innerContainer.resources().requests().withCpu(cpuCoreCount);

        return this;
    }

    @Override
    public ContainerImpl withMemorySizeInGB(double memorySize) {
        innerContainer.resources().requests().withMemoryInGB(memorySize);

        return this;
    }

    @Override
    public ContainerImpl withStartingCommandLines(String... commandLines) {
        for (String command : commandLines) {
            this.withStartingCommandLine(command);
        }

        return this;
    }

    @Override
    public ContainerImpl withStartingCommandLine(String commandLine) {
        if (innerContainer.command() == null) {
            innerContainer.withCommand(new ArrayList<String>());
        }
        innerContainer.command().add(commandLine);

        return this;
    }

    @Override
    public ContainerImpl withEnvironmentVariables(Map<String, String> environmentVariables) {
        for (Map.Entry<String, String> entry : environmentVariables.entrySet()) {
            this.withEnvironmentVariable(entry.getKey(), entry.getValue());
        }

        return this;
    }

    @Override
    public ContainerImpl withEnvironmentVariable(String envName, String envValue) {
        if (innerContainer.environmentVariables() == null) {
            innerContainer.withEnvironmentVariables(new ArrayList<EnvironmentVariable>());
        }

        innerContainer.environmentVariables().add(new EnvironmentVariable()
            .withName(envName)
            .withValue(envValue));

        return this;
    }

    @Override
    public ContainerImpl withVolumeMountSetting(String volumeName, String mountPath) {
        if (innerContainer.volumeMounts() == null) {
            innerContainer.withVolumeMounts(new ArrayList<VolumeMount>());
        }
        innerContainer.volumeMounts().add(new VolumeMount()
            .withName(volumeName)
            .withMountPath(mountPath)
            .withReadOnly(false));

        return this;
    }

    @Override
    public ContainerImpl withVolumeMountSetting(Map<String, String> volumeMountSetting) {
        for (Map.Entry<String, String> entry : volumeMountSetting.entrySet()) {
            this.withVolumeMountSetting(entry.getKey(), entry.getValue());
        }

        return this;
    }

    @Override
    public ContainerImpl withReadOnlyVolumeMountSetting(String volumeName, String mountPath) {
        if (innerContainer.volumeMounts() == null) {
            innerContainer.withVolumeMounts(new ArrayList<VolumeMount>());
        }
        innerContainer.volumeMounts().add(new VolumeMount()
            .withName(volumeName)
            .withMountPath(mountPath)
            .withReadOnly(true));

        return this;
    }

    @Override
    public ContainerImpl withReadOnlyVolumeMountSetting(Map<String, String> volumeMountSetting) {
        for (Map.Entry<String, String> entry : volumeMountSetting.entrySet()) {
            this.withReadOnlyVolumeMountSetting(entry.getKey(), entry.getValue());
        }

        return this;
    }
}
