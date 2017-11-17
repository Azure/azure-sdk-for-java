/**
 * Copyright (c) Microsoft Corporation. All rights reserved.
 * Licensed under the MIT License. See License.txt in the project root for
 * license information.
 */

package com.microsoft.azure.management.samples;

import com.microsoft.azure.management.compute.samples.CreateVirtualMachineUsingCustomImageFromVHD;
import com.microsoft.azure.management.compute.samples.CreateVirtualMachineUsingCustomImageFromVM;
import com.microsoft.azure.management.compute.samples.CreateVirtualMachineUsingSpecializedDiskFromSnapshot;
import com.microsoft.azure.management.compute.samples.CreateVirtualMachineUsingSpecializedDiskFromVhd;
import com.microsoft.azure.management.compute.samples.CreateVirtualMachinesInParallel;
import com.microsoft.azure.management.compute.samples.CreateVirtualMachinesUsingCustomImageOrSpecializedVHD;
import com.microsoft.azure.management.compute.samples.DeployImageFromContainerRegistryToKubernetes;
import com.microsoft.azure.management.compute.samples.ListVirtualMachineExtensionImages;
import com.microsoft.azure.management.compute.samples.ListVirtualMachineImages;
import com.microsoft.azure.management.compute.samples.ManageAvailabilitySet;
import com.microsoft.azure.management.compute.samples.ManageManagedDisks;
import com.microsoft.azure.management.compute.samples.ManageContainerServiceUsingDockerSwarm;
import com.microsoft.azure.management.compute.samples.ManageContainerServiceUsingKubernetes;
import com.microsoft.azure.management.compute.samples.ManageResourceFromMSIEnabledVirtualMachineBelongsToAADGroup;
import com.microsoft.azure.management.compute.samples.ManageStorageFromMSIEnabledVirtualMachine;
import com.microsoft.azure.management.compute.samples.ManageVirtualMachine;
import com.microsoft.azure.management.compute.samples.ManageVirtualMachineAsync;
import com.microsoft.azure.management.compute.samples.ManageVirtualMachineExtension;
import com.microsoft.azure.management.compute.samples.ManageVirtualMachineScaleSet;
import com.microsoft.azure.management.compute.samples.ManageVirtualMachineScaleSetAsync;
import com.microsoft.azure.management.compute.samples.ManageVirtualMachineScaleSetWithUnmanagedDisks;
import com.microsoft.azure.management.compute.samples.ManageVirtualMachineWithDisk;
import com.microsoft.azure.management.compute.samples.ManageVirtualMachineWithUnmanagedDisks;
import com.microsoft.azure.management.compute.samples.ManageVirtualMachinesInParallel;
import com.microsoft.azure.management.compute.samples.ConvertVirtualMachineToManagedDisks;
import com.microsoft.azure.management.compute.samples.ManageZonalVirtualMachine;
import com.microsoft.azure.management.compute.samples.ManageZonalVirtualMachineScaleSet;
import org.junit.Assert;
import org.junit.Ignore;
import org.junit.Test;

public class ComputeSampleTests extends SamplesTestBase {

    @Test
    public void testCreateVirtualMachinesInParallel() {
        Assert.assertTrue(CreateVirtualMachinesInParallel.runSample(azure));
    }

    @Test
    public void testCreateVirtualMachinesUsingCustomImageOrSpecializedVHD() {
        Assert.assertTrue(CreateVirtualMachinesUsingCustomImageOrSpecializedVHD.runSample(azure));
    }

    @Test
    public void testCreateVirtualMachineUsingCustomImageFromVHD() {
        Assert.assertTrue(CreateVirtualMachineUsingCustomImageFromVHD.runSample(azure));
    }

    @Test
    public void testCreateVirtualMachineUsingCustomImageFromVM() {
        Assert.assertTrue(CreateVirtualMachineUsingCustomImageFromVM.runSample(azure));
    }

    @Test
    public void testCreateVirtualMachineUsingSpecializedDiskFromSnapshot() {
        Assert.assertTrue(CreateVirtualMachineUsingSpecializedDiskFromSnapshot.runSample(azure));
    }

    @Test
    public void testCreateVirtualMachineUsingSpecializedDiskFromVhd() {
        Assert.assertTrue(CreateVirtualMachineUsingSpecializedDiskFromVhd.runSample(azure));
    }

    @Test
    public void testListVirtualMachineExtensionImages() {
        Assert.assertTrue(ListVirtualMachineExtensionImages.runSample(azure));
    }

    @Test
    public void testListVirtualMachineImages() {
        Assert.assertTrue(ListVirtualMachineImages.runSample(azure));
    }

    @Test
    public void testManageAvailabilitySet() {
        Assert.assertTrue(ManageAvailabilitySet.runSample(azure));
    }

    @Test
    public void testManageVirtualMachineWithUnmanagedDisks() {
        Assert.assertTrue(ManageVirtualMachineWithUnmanagedDisks.runSample(azure));
    }

    @Test
    public void testManageVirtualMachine() {
        Assert.assertTrue(ManageVirtualMachine.runSample(azure));
    }

    @Test
    public void testManageVirtualMachineAsync() {
        Assert.assertTrue(ManageVirtualMachineAsync.runSample(azure));
    }

    @Test
    public void testManageVirtualMachineExtension() {
        Assert.assertTrue(ManageVirtualMachineExtension.runSample(azure));
    }

    @Test
    public void testManageVirtualMachineScaleSet() {
        Assert.assertTrue(ManageVirtualMachineScaleSet.runSample(azure));
    }

    @Test
    public void testManageVirtualMachineScaleSetAsync() {
        Assert.assertTrue(ManageVirtualMachineScaleSetAsync.runSample(azure));
    }

    @Test
    public void testManageVirtualMachineScaleSetWithUnmanagedDisks() {
        Assert.assertTrue(ManageVirtualMachineScaleSetWithUnmanagedDisks.runSample(azure));
    }

    @Test
    public void testManageVirtualMachinesInParallel() {
        Assert.assertTrue(ManageVirtualMachinesInParallel.runSample(azure));
    }

    @Test
    public void testManageVirtualMachineWithDisk() {
        Assert.assertTrue(ManageVirtualMachineWithDisk.runSample(azure));
    }

    @Test
    public void testConvertVirtualMachineToManagedDisks() {
        Assert.assertTrue(ConvertVirtualMachineToManagedDisks.runSample(azure));
    }

    @Test
    public void testManageManagedDisks() {
      Assert.assertTrue(ManageManagedDisks.runSample(azure));
    }

    @Test
    public void testManageContainerServiceUsingKubernetes() {
        if (isPlaybackMode()) {
            Assert.assertTrue(ManageContainerServiceUsingKubernetes.runSample(azure, "client id", "secret"));
        } else {
            Assert.assertTrue(ManageContainerServiceUsingKubernetes.runSample(azure, "", ""));
        }
    }

    @Test
    public void testManageContainerServiceUsingDockerSwarm() {
        Assert.assertTrue(ManageContainerServiceUsingDockerSwarm.runSample(azure));
    }

    @Test
    public void testDeployImageFromContainerRegistryToKubernetes() {
        if (!isPlaybackMode()) {
            Assert.assertTrue(DeployImageFromContainerRegistryToKubernetes.runSample(azure, "", ""));
        }
    }

    @Test
    @Ignore("The updated az cli with --msi option will be published on sept/12/2017, disabling this test for now")
    public void testManageStorageFromMSIEnabledVirtualMachine() {
        Assert.assertTrue(ManageStorageFromMSIEnabledVirtualMachine.runSample(azure));
    }

    @Test
    @Ignore("Mock service reporting that unexpected PUT request found, need investigation")
    public void testManageResourceFromMSIEnabledVirtualMachineBelongsToAADGroup() {
        Assert.assertTrue(ManageResourceFromMSIEnabledVirtualMachineBelongsToAADGroup.runSample(azure));
    }

    @Test
    public void testManageZonalVirtualMachine() {
        Assert.assertTrue(ManageZonalVirtualMachine.runSample(azure));
    }

    @Test
    public void testManageZonalVirtualMachineScaleSet() {
        Assert.assertTrue(ManageZonalVirtualMachineScaleSet.runSample(azure));
    }
}
