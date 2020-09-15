// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.samples;


import com.azure.resourcemanager.resources.samples.DeployUsingARMTemplate;
import com.azure.resourcemanager.resources.samples.DeployUsingARMTemplateAsync;
import com.azure.resourcemanager.resources.samples.DeployUsingARMTemplateWithDeploymentOperations;
import com.azure.resourcemanager.resources.samples.DeployUsingARMTemplateWithProgress;
import com.azure.resourcemanager.resources.samples.DeployUsingARMTemplateWithTags;
import com.azure.resourcemanager.resources.samples.DeployVirtualMachineUsingARMTemplate;
import com.azure.resourcemanager.resources.samples.ManageResource;
import com.azure.resourcemanager.resources.samples.ManageResourceGroup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.IOException;

public class ResourceSampleTests extends SamplesTestBase {
    @Test
    public void testDeployUsingARMTemplate() throws IOException, IllegalAccessException {
        if (isPlaybackMode()) {
            return;
        }
        Assertions.assertTrue(DeployUsingARMTemplate.runSample(azure));
    }

    @Test
    public void testDeployUsingARMTemplateWithProgress() throws IOException, IllegalAccessException {
        Assertions.assertTrue(DeployUsingARMTemplateWithProgress.runSample(azure));
    }

    @Test
    public void testDeployUsingARMTemplateAsync() throws InterruptedException {
        Assertions.assertTrue(DeployUsingARMTemplateAsync.runSample(azure));
    }

    @Test()
    public void testDeployUsingARMTemplateWithDeploymentOperations() throws InterruptedException {
        if (isPlaybackMode()) {
            Assertions.assertTrue(DeployUsingARMTemplateWithDeploymentOperations.runSample(azure, 0));
        } else {
            Assertions.assertTrue(DeployUsingARMTemplateWithDeploymentOperations.runSample(azure, -1));
        }
    }

    @Test
    public void testDeployUsingARMTemplateWithTags() throws IOException, IllegalAccessException {
        Assertions.assertTrue(DeployUsingARMTemplateWithTags.runSample(azure));
    }

    @Test
    public void testManageResource() {
        Assertions.assertTrue(ManageResource.runSample(azure));
    }

    @Test
    public void testManageResourceGroup() {
        Assertions.assertTrue(ManageResourceGroup.runSample(azure));
    }

    @Test
    public void testDeployVirtualMachineUsingARMTemplate() throws IOException, IllegalAccessException {
        Assertions.assertTrue(DeployVirtualMachineUsingARMTemplate.runSample(azure));
    }
}
