/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.containerinstance.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.containerinstance.Container;
import com.microsoft.azure.management.containerinstance.ContainerGroup;

import java.util.Map;

/**
 * Implementation for container group's container instance definition stages interface.
 */
@LangDefinition
public class ContainerImpl implements ContainerGroup.DefinitionStages.ContainerInstanceDefinitionStages.ContainerInstanceDefinition<ContainerGroupImpl> {
    private Container innerContainer;
    private ContainerGroupImpl parent;

    ContainerImpl(ContainerGroupImpl parent) {
        this.parent = parent;
        this.innerContainer = new Container();
    }

    @Override
    public ContainerGroupImpl attach() {
        return null;
    }

    @Override
    public ContainerImpl withImage(String imageName) {
        return null;
    }

    @Override
    public ContainerImpl withCPUCoreCount(double cpuCoreCount) {
        return null;
    }

    @Override
    public ContainerImpl withMemorySizeInGB(double memorySize) {
        return null;
    }

    @Override
    public ContainerImpl withExternalTcpPorts(int... ports) {
        return null;
    }

    @Override
    public ContainerImpl withExternalTcpPort(int port) {
        return null;
    }

    @Override
    public ContainerImpl withExternalUdpPorts(int... ports) {
        return null;
    }

    @Override
    public ContainerImpl withExternalUdpPort(int port) {
        return null;
    }

    @Override
    public ContainerImpl withInternalTcpPorts(int... ports) {
        return null;
    }

    @Override
    public ContainerImpl withInternalTcpPort(int port) {
        return null;
    }

    @Override
    public ContainerImpl withInternalUdpPorts(int... ports) {
        return null;
    }

    @Override
    public ContainerImpl withInternalUdpPort(int port) {
        return null;
    }

    @Override
    public ContainerImpl withStartingCommandLines(String... commandLines) {
        return null;
    }

    @Override
    public ContainerImpl withStartingCommandLine(String commandLine) {
        return null;
    }

    @Override
    public ContainerImpl withEnvironmentVariables(Map<String, String> environmentVariables) {
        return null;
    }

    @Override
    public ContainerImpl withEnvironmentVariable(String envName, String envValue) {
        return null;
    }

    @Override
    public ContainerImpl withVolumeMountSetting(String volumeName, String mountPath) {
        return null;
    }

    @Override
    public ContainerImpl withVolumeMountSetting(Map<String, String> volumeMountSetting) {
        return null;
    }

    @Override
    public ContainerImpl withReadOnlyVolumeMountSetting(String volumeName, String mountPath) {
        return null;
    }

    @Override
    public ContainerImpl withReadOnlyVolumeMountSetting(Map<String, String> volumeMountSetting) {
        return null;
    }
}
