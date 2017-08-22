/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.containerinstance.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.containerinstance.ContainerGroup;
import com.microsoft.azure.management.resources.fluentcore.arm.models.implementation.GroupableResourceImpl;
import rx.Observable;

/**
 * Implementation for ContainerGroup and its create interfaces.
 */
@LangDefinition
public class ContainerGroupImpl
    extends
    GroupableResourceImpl<
            ContainerGroup,
            ContainerGroupInner,
            ContainerGroupImpl,
            ContainerInstanceManager>
    implements ContainerGroup,
    ContainerGroup.Definition {

    protected ContainerGroupImpl(String name, ContainerGroupInner innerObject, ContainerInstanceManager manager) {
        super(name, innerObject, manager);
    }

    @Override
    protected Observable<ContainerGroupInner> getInnerAsync() {
        return null;
    }

    @Override
    public ContainerGroupImpl withLinux() {
        return null;
    }

    @Override
    public ContainerGroupImpl withWindows() {
        return null;
    }

    @Override
    public Observable<ContainerGroup> createResourceAsync() {
        return null;
    }

    @Override
    public ContainerGroupImpl withPrivateImageRegistry(String server, String username, String password) {
        return null;
    }

    @Override
    public ContainerGroup.DefinitionStages.VolumeDefinitionStages.Blank<ContainerGroup.DefinitionStages.WithVolume> defineVolume(String name) {
        return null;
    }

    @Override
    public ContainerGroup.DefinitionStages.ContainerInstanceDefinitionStages.Blank<ContainerGroup.DefinitionStages.WithNextContainerInstance> defineContainerInstance(String name) {
        return null;
    }

    @Override
    public ContainerGroupImpl withContainerInstance(String imageName) {
        return null;
    }

    @Override
    public ContainerGroupImpl withContainerInstance(String imageName, int port) {
        return null;
    }

    @Override
    public String getLogContent(String resourceGroupName, String containerName, String containerGroupName) {
        return null;
    }

    @Override
    public String getLogContent(String resourceGroupName, String containerName, String containerGroupName, int tailLineCount) {
        return null;
    }

    @Override
    public Observable<String> getLogContentAsync(String resourceGroupName, String containerName, String containerGroupName) {
        return null;
    }

    @Override
    public Observable<String> getLogContentAsync(String resourceGroupName, String containerName, String containerGroupName, int tailLineCount) {
        return null;
    }
}
