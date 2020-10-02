// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.samples;

import com.azure.resourcemanager.containerinstance.samples.ManageContainerInstanceWithAzureFileShareMount;
import com.azure.resourcemanager.containerinstance.samples.ManageContainerInstanceWithManualAzureFileShareMountCreation;
import com.azure.resourcemanager.containerinstance.samples.ManageContainerInstanceWithMultipleContainerImages;
import com.azure.resourcemanager.containerinstance.samples.ManageContainerInstanceZeroToOneAndOneToManyUsingContainerServiceOrchestrator;
import com.jcraft.jsch.JSchException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ContainerInstanceTests extends SamplesTestBase {

    @Test
    public void testManageContainerInstanceWithAzureFileShareMount() {
        // Skip test in "playback" mode due to HTTP calls made outside of the management plane which can not be recorded at this time
        if (!isPlaybackMode()) {
            Assertions.assertTrue(ManageContainerInstanceWithAzureFileShareMount.runSample(azureResourceManager));
        }
    }

    @Test
    public void testManageContainerInstanceWithManualAzureFileShareMountCreation() {
        // Skip test in "playback" mode due to HTTP calls made outside of the management plane which can not be recorded at this time
        if (!isPlaybackMode()) {
            Assertions.assertTrue(ManageContainerInstanceWithManualAzureFileShareMountCreation.runSample(azureResourceManager));
        }
    }

    @Test
    public void testManageContainerInstanceWithMultipleContainerImages() {
        // Skip test in "playback" mode due to HTTP calls made outside of the management plane which can not be recorded at this time
        if (!isPlaybackMode()) {
            Assertions.assertTrue(ManageContainerInstanceWithMultipleContainerImages.runSample(azureResourceManager));
        }
    }

    @Test
    public void testManageContainerInstanceZeroToOneAndOneToManyUsingContainerServiceOrchestrator() throws InterruptedException, JSchException, IOException {
        // Skip test in "playback" mode due to HTTP calls made outside of the management plane which can not be recorded at this time
        if (!isPlaybackMode()) {
            Assertions.assertTrue(ManageContainerInstanceZeroToOneAndOneToManyUsingContainerServiceOrchestrator.runSample(azureResourceManager, "", ""));
        }
    }

}
