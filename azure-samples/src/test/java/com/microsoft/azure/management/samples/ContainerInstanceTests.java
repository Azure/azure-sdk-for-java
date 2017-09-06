/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.samples;

import com.microsoft.azure.management.containerinstance.samples.ManageContainerInstanceWithAzureFileShareMount;
import com.microsoft.azure.management.containerinstance.samples.ManageContainerInstanceWithManualAzureFileShareMountCreation;
import com.microsoft.azure.management.containerinstance.samples.ManageContainerInstanceWithMultipleContainerImages;
import com.microsoft.azure.management.containerinstance.samples.ManageContainerInstanceZeroToOneAndOneToManyUsingKubernetesOrchestration;
import org.junit.Assert;
import org.junit.Test;

public class ContainerInstanceTests extends SamplesTestBase {

    @Test
    public void testManageContainerInstanceWithAzureFileShareMount() {
        Assert.assertTrue(ManageContainerInstanceWithAzureFileShareMount.runSample(azure));
    }

    @Test
    public void testManageContainerInstanceWithManualAzureFileShareMountCreation() {
        Assert.assertTrue(ManageContainerInstanceWithManualAzureFileShareMountCreation.runSample(azure));
    }

    @Test
    public void testManageContainerInstanceWithMultipleContainerImages() {
        Assert.assertTrue(ManageContainerInstanceWithMultipleContainerImages.runSample(azure));
    }

    @Test
    public void testManageContainerInstanceZeroToOneAndOneToManyUsingKubernetesOrchestration() {
        if (!isPlaybackMode()) {
            Assert.assertTrue(ManageContainerInstanceZeroToOneAndOneToManyUsingKubernetesOrchestration.runSample(azure, "", ""));
        }
    }

}
