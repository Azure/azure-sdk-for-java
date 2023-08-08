// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.containerinstance.implementation;

import com.azure.resourcemanager.containerinstance.models.AzureFileVolume;
import com.azure.resourcemanager.containerinstance.models.ContainerGroup;
import com.azure.resourcemanager.containerinstance.models.GitRepoVolume;
import com.azure.resourcemanager.containerinstance.models.Volume;
import java.util.ArrayList;
import java.util.Map;

/** Implementation for container group's volume definition stages interface. */
class VolumeImpl
    implements ContainerGroup.DefinitionStages.VolumeDefinitionStages.VolumeDefinition<
        ContainerGroup.DefinitionStages.WithVolume> {
    private Volume innerVolume;
    private ContainerGroupImpl parent;

    VolumeImpl(ContainerGroupImpl parent, String volumeName) {
        this.parent = parent;
        this.innerVolume = new Volume().withName(volumeName);
    }

    @Override
    public ContainerGroupImpl attach() {
        if (parent.innerModel().volumes() == null) {
            parent.innerModel().withVolumes(new ArrayList<Volume>());
        }
        parent.innerModel().volumes().add(innerVolume);

        return parent;
    }

    @Override
    public VolumeImpl withExistingReadWriteAzureFileShare(String shareName) {
        ensureAzureFileVolume().withReadOnly(false).withShareName(shareName);

        return this;
    }

    @Override
    public VolumeImpl withExistingReadOnlyAzureFileShare(String shareName) {
        ensureAzureFileVolume().withReadOnly(true).withShareName(shareName);

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

    @Override
    public VolumeImpl withSecrets(Map<String, String> secrets) {
        this.innerVolume.withSecret(secrets);

        return this;
    }

    @Override
    public VolumeImpl withGitUrl(String gitUrl) {
        this.innerVolume.withGitRepo(new GitRepoVolume());
        this.innerVolume.gitRepo().withRepository(gitUrl);

        return this;
    }

    @Override
    public VolumeImpl withGitDirectoryName(String gitDirectoryName) {
        if (this.innerVolume.gitRepo() == null) {
            this.innerVolume.withGitRepo(new GitRepoVolume());
        }
        this.innerVolume.gitRepo().withDirectory(gitDirectoryName);

        return this;
    }

    @Override
    public VolumeImpl withGitRevision(String gitRevision) {
        if (this.innerVolume.gitRepo() == null) {
            this.innerVolume.withGitRepo(new GitRepoVolume());
        }
        this.innerVolume.gitRepo().withRevision(gitRevision);

        return this;
    }
}
