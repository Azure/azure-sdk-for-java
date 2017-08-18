/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.containerinstance.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.containerinstance.ContainerGroup;
import com.microsoft.azure.management.containerinstance.Volume;

/**
 * Implementation for container group's volume definition stages interface.
 */
@LangDefinition
public class VolumeImpl implements ContainerGroup.DefinitionStages.VolumeDefinitionStages.VolumeDefinition<ContainerGroupImpl> {
    private Volume innerVolume;
    private ContainerGroupImpl parent;

    VolumeImpl(ContainerGroupImpl parent) {
        this.parent = parent;
        this.innerVolume = new Volume();
    }

    @Override
    public ContainerGroupImpl attach() {
        return null;
//        return this.parent.withVolume(innerVolume);
    }

    @Override
    public VolumeImpl withExistingReadWriteAzureFileShare(String shareName) {
        return null;
    }

    @Override
    public VolumeImpl withExistingReadOnlyAzureFileShare(String shareName) {
        return null;
    }

    @Override
    public VolumeImpl withNewAzureFileShare(String shareName) {
        return null;
    }

    @Override
    public VolumeImpl withStorageAccountName(String storageAccountName) {
        return null;
    }

    @Override
    public VolumeImpl withStorageAccountKey(String storageAccountKey) {
        return null;
    }
}
