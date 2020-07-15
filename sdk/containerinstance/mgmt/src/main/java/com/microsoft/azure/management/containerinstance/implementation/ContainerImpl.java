/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.containerinstance.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.containerinstance.Container;
import com.microsoft.azure.management.containerinstance.ContainerGroup;
import com.microsoft.azure.management.containerinstance.ContainerGroupIpAddressType;
import com.microsoft.azure.management.containerinstance.ContainerGroupNetworkProtocol;
import com.microsoft.azure.management.containerinstance.ContainerNetworkProtocol;
import com.microsoft.azure.management.containerinstance.ContainerPort;
import com.microsoft.azure.management.containerinstance.EnvironmentVariable;
import com.microsoft.azure.management.containerinstance.GpuResource;
import com.microsoft.azure.management.containerinstance.GpuSku;
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
        this.withInternalTcpPort(port);

        return this;
    }

    private IpAddress ensureParentIpAddress() {
        if (parent.inner().ipAddress() == null) {
            parent.inner().withIpAddress(new IpAddress());
        }
        if (parent.inner().ipAddress().type() == null && parent.inner().ipAddress().dnsNameLabel() == null) {
            parent.inner().ipAddress().withType(ContainerGroupIpAddressType.PRIVATE);
        } else {
            parent.inner().ipAddress().withType(ContainerGroupIpAddressType.PUBLIC);
        }
        if (parent.inner().ipAddress().ports() == null) {
            parent.inner().ipAddress().withPorts(new ArrayList<Port>());
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
        this.withInternalUdpPort(port);

        return this;
    }

    @Override
    public ContainerImpl withInternalTcpPorts(int... ports) {
        for (int port : ports) {
            this.withInternalTcpPort(port);
        }

        return this;
    }

    @Override
    public ContainerImpl withInternalUdpPorts(int... ports) {
        for (int port : ports) {
            this.withInternalUdpPort(port);
        }

        return this;
    }

    @Override
    public ContainerImpl withInternalTcpPort(int port) {
        if (innerContainer.ports() == null) {
            innerContainer.withPorts(new ArrayList<ContainerPort>());
        }
        innerContainer.ports().add(new ContainerPort().withPort(port).withProtocol(ContainerNetworkProtocol.TCP));

        return this;
    }

    @Override
    public ContainerImpl withInternalUdpPort(int port) {
        if (innerContainer.ports() == null) {
            innerContainer.withPorts(new ArrayList<ContainerPort>());
        }
        innerContainer.ports().add(new ContainerPort().withPort(port).withProtocol(ContainerNetworkProtocol.UDP));

        return this;
    }

    @Override
    public ContainerImpl withCpuCoreCount(double cpuCoreCount) {
        innerContainer.resources().requests().withCpu(cpuCoreCount);
        return this;
    }

    @Override
    public ContainerImpl withGpuResource(int gpuCoreCount, GpuSku gpuSku) {
        innerContainer.resources().requests().withGpu(new GpuResource().withCount(gpuCoreCount).withSku(gpuSku));
        return this;
    }

    @Override
    public ContainerImpl withMemorySizeInGB(double memorySize) {
        innerContainer.resources().requests().withMemoryInGB(memorySize);

        return this;
    }

    @Override
    public ContainerImpl withStartingCommandLine(String executable, String... parameters) {
        this.withStartingCommandLine(executable);
        if (parameters != null) {
            for (String parameter : parameters) {
                this.withStartingCommandLine(parameter);
            }
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
    public ContainerImpl withEnvironmentVariableWithSecuredValue(Map<String, String> environmentVariables) {
        for (Map.Entry<String, String> entry : environmentVariables.entrySet()) {
            this.withEnvironmentVariableWithSecuredValue(entry.getKey(), entry.getValue());
        }

        return this;
    }

    @Override
    public ContainerImpl withEnvironmentVariableWithSecuredValue(String envName, String securedValue) {
        if (innerContainer.environmentVariables() == null) {
            innerContainer.withEnvironmentVariables(new ArrayList<EnvironmentVariable>());
        }

        innerContainer.environmentVariables().add(new EnvironmentVariable()
                .withName(envName)
                .withSecureValue(securedValue));

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
