/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */
package com.microsoft.azure.management.containerinstance.implementation;

import com.microsoft.azure.management.apigeneration.LangDefinition;
import com.microsoft.azure.management.containerinstance.AzureFileVolume;
import com.microsoft.azure.management.containerinstance.ContainerGroup;
import com.microsoft.azure.management.containerinstance.Volume;

import java.util.ArrayList;

/**
 * Implementation for container group's volume definition stages interface.
 */
@LangDefinition
class VolumeImpl implements ContainerGroup.DefinitionStages.VolumeDefinitionStages.VolumeDefinition<ContainerGroup.DefinitionStages.WithVolume> {
    private Volume innerVolume;
    private ContainerGroupImpl parent;

    VolumeImpl(ContainerGroupImpl parent, String volumeName) {
        this.parent = parent;
        this.innerVolume = new Volume().withName(volumeName);
    }

    @Override
    public ContainerGroupImpl attach() {
        if (parent.inner().volumes() == null) {
            parent.inner().withVolumes(new ArrayList<Volume>());
        }
        parent.inner().volumes().add(innerVolume);

        return parent;
    }

    @Override
    public VolumeImpl withExistingReadWriteAzureFileShare(String shareName) {
        ensureAzureFileVolume()
            .withReadOnly(false)
            .withShareName(shareName);

        return this;
    }

    @Override
    public VolumeImpl withExistingReadOnlyAzureFileShare(String shareName) {
        ensureAzureFileVolume()
            .withReadOnly(true)
            .withShareName(shareName);

        return this;
    }

    private AzureFileVolume ensureAzureFileVolume() {
        if (innerVolume.azureFile() == null) {
            innerVolume.withAzureFile(new AzureFileVolume());
        }

        return innerVolume.azureFile();
    }

    @Override
    public VolumeImpl withStorageAccountName(String storageAccountName) {
        ensureAzureFileVolume().withStorageAccountName(storageAccountName);

        return this;
    }

    @Override
    public VolumeImpl withStorageAccountKey(String storageAccountKey) {
        ensureAzureFileVolume().withStorageAccountKey(storageAccountKey);

        return this;
    }
}
