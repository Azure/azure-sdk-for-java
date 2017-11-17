/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.samples;

import com.microsoft.azure.management.resources.samples.DeployUsingARMTemplate;
import com.microsoft.azure.management.resources.samples.DeployUsingARMTemplateWithProgress;
import com.microsoft.azure.management.resources.samples.DeployUsingARMTemplateWithTags;
import com.microsoft.azure.management.resources.samples.DeployVirtualMachineUsingARMTemplate;
import com.microsoft.azure.management.resources.samples.ManageResource;
import com.microsoft.azure.management.resources.samples.ManageResourceGroup;
import org.junit.Assert;
import org.junit.Test;

public class ResourceSampleTests extends SamplesTestBase {
    @Test
    public void testDeployUsingARMTemplate() {
        Assert.assertTrue(DeployUsingARMTemplate.runSample(azure));
    }

    @Test
    public void testDeployUsingARMTemplateWithProgress() {
        Assert.assertTrue(DeployUsingARMTemplateWithProgress.runSample(azure));
    }

    @Test
    public void testDeployUsingARMTemplateWithTags() {
        Assert.assertTrue(DeployUsingARMTemplateWithTags.runSample(azure));
    }

    @Test
    public void testManageResource() {
        Assert.assertTrue(ManageResource.runSample(azure));
    }

    @Test
    public void testManageResourceGroup() {
        Assert.assertTrue(ManageResourceGroup.runSample(azure));
    }

    @Test
    public void testDeployVirtualMachineUsingARMTemplate() {
        Assert.assertTrue(DeployVirtualMachineUsingARMTemplate.runSample(azure));
    }
}
