/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.azure.management.samples;


import com.azure.management.resources.samples.DeployUsingARMTemplate;
import com.azure.management.resources.samples.DeployUsingARMTemplateAsync;
import com.azure.management.resources.samples.DeployUsingARMTemplateWithDeploymentOperations;
import com.azure.management.resources.samples.DeployUsingARMTemplateWithProgress;
import com.azure.management.resources.samples.DeployUsingARMTemplateWithTags;
import com.azure.management.resources.samples.DeployVirtualMachineUsingARMTemplate;
import com.azure.management.resources.samples.ManageResource;
import com.azure.management.resources.samples.ManageResourceGroup;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ResourceSampleTests extends SamplesTestBase {
    @Test
    public void testDeployUsingARMTemplate() {
        if (isPlaybackMode()) {
            return;
        }
        Assertions.assertTrue(DeployUsingARMTemplate.runSample(azure));
    }

    @Test
    public void testDeployUsingARMTemplateWithProgress() {
        Assertions.assertTrue(DeployUsingARMTemplateWithProgress.runSample(azure));
    }

    @Test
    public void testDeployUsingARMTemplateAsync() {
        Assertions.assertTrue(DeployUsingARMTemplateAsync.runSample(azure));
    }

    @Test()
    public void testDeployUsingARMTemplateWithDeploymentOperations() {
        if (isPlaybackMode()) {
            Assertions.assertTrue(DeployUsingARMTemplateWithDeploymentOperations.runSample(azure, 0));
        } else {
            Assertions.assertTrue(DeployUsingARMTemplateWithDeploymentOperations.runSample(azure, -1));
        }
    }

    @Test
    public void testDeployUsingARMTemplateWithTags() {
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
    public void testDeployVirtualMachineUsingARMTemplate() {
        Assertions.assertTrue(DeployVirtualMachineUsingARMTemplate.runSample(azure));
    }
}
