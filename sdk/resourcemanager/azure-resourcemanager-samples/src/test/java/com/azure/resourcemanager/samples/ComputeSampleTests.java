// Copyright (c) Microsoft Corporation. All rights reserved.
// Licensed under the MIT License.

package com.azure.resourcemanager.samples;

import com.azure.resourcemanager.compute.samples.CreateVirtualMachineUsingCustomImageFromVHD;
import com.azure.resourcemanager.compute.samples.CreateVirtualMachineUsingCustomImageFromVM;
import com.azure.resourcemanager.compute.samples.CreateVirtualMachineUsingSpecializedDiskFromSnapshot;
import com.azure.resourcemanager.compute.samples.CreateVirtualMachineUsingSpecializedDiskFromVhd;
import com.azure.resourcemanager.compute.samples.CreateVirtualMachinesAsyncTrackingRelatedResources;
import com.azure.resourcemanager.compute.samples.CreateVirtualMachinesInParallel;
import com.azure.resourcemanager.compute.samples.CreateVirtualMachinesUsingCustomImageOrSpecializedVHD;
import com.azure.resourcemanager.compute.samples.ListComputeSkus;
import com.azure.resourcemanager.compute.samples.ListVirtualMachineExtensionImages;
import com.azure.resourcemanager.compute.samples.ListVirtualMachineImages;
import com.azure.resourcemanager.compute.samples.ManageAvailabilitySet;
import com.azure.resourcemanager.compute.samples.ManageManagedDisks;
import com.azure.resourcemanager.compute.samples.ManageResourceFromMSIEnabledVirtualMachineBelongsToAADGroup;
import com.azure.resourcemanager.compute.samples.ManageStorageFromMSIEnabledVirtualMachine;
import com.azure.resourcemanager.compute.samples.ManageUserAssignedMSIEnabledVirtualMachine;
import com.azure.resourcemanager.compute.samples.ManageVirtualMachine;
import com.azure.resourcemanager.compute.samples.ManageVirtualMachineAsync;
import com.azure.resourcemanager.compute.samples.ManageVirtualMachineExtension;
import com.azure.resourcemanager.compute.samples.ManageVirtualMachineScaleSet;
import com.azure.resourcemanager.compute.samples.ManageVirtualMachineScaleSetAsync;
import com.azure.resourcemanager.compute.samples.ManageVirtualMachineScaleSetWithUnmanagedDisks;
import com.azure.resourcemanager.compute.samples.ManageVirtualMachineWithDisk;
import com.azure.resourcemanager.compute.samples.ManageVirtualMachineWithUnmanagedDisks;
import com.azure.resourcemanager.compute.samples.ManageVirtualMachinesInParallel;
import com.azure.resourcemanager.compute.samples.ConvertVirtualMachineToManagedDisks;
import com.azure.resourcemanager.compute.samples.ManageZonalVirtualMachine;
import com.azure.resourcemanager.compute.samples.ManageZonalVirtualMachineScaleSet;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

public class ComputeSampleTests extends SamplesTestBase {

    @Test
    public void testCreateVirtualMachinesInParallel() {
        Assertions.assertTrue(CreateVirtualMachinesInParallel.runSample(azure));
    }

    @Test
    @Disabled("Sample leverages true parallelization, which cannot be recorded, until GenericResources support deleteByIds()")
    public void testCreateVirtualMachinesAsyncTrackingRelatedResources() {
        Assertions.assertTrue(CreateVirtualMachinesAsyncTrackingRelatedResources.runSample(azure));
    }

    @Test
    public void testCreateVirtualMachinesUsingCustomImageOrSpecializedVHD() {
        Assertions.assertTrue(CreateVirtualMachinesUsingCustomImageOrSpecializedVHD.runSample(azure));
    }

    @Test
    public void testCreateVirtualMachineUsingCustomImageFromVHD() {
        Assertions.assertTrue(CreateVirtualMachineUsingCustomImageFromVHD.runSample(azure));
    }

    @Test
    @Disabled("Need to investigate - 'Disks or snapshot cannot be resized down.'")
    public void testCreateVirtualMachineUsingCustomImageFromVM() {
        Assertions.assertTrue(CreateVirtualMachineUsingCustomImageFromVM.runSample(azure));
    }

    @Test
    public void testCreateVirtualMachineUsingSpecializedDiskFromSnapshot() {
        Assertions.assertTrue(CreateVirtualMachineUsingSpecializedDiskFromSnapshot.runSample(azure));
    }

    @Test
    public void testCreateVirtualMachineUsingSpecializedDiskFromVhd() {
        Assertions.assertTrue(CreateVirtualMachineUsingSpecializedDiskFromVhd.runSample(azure));
    }

    @Test
    public void testListVirtualMachineExtensionImages() {
        Assertions.assertTrue(ListVirtualMachineExtensionImages.runSample(azure));
    }

    @Test
    public void testListVirtualMachineImages() {
        Assertions.assertTrue(ListVirtualMachineImages.runSample(azure));
    }

    @Test
    public void testListListComputeSkus() {
        Assertions.assertTrue(ListComputeSkus.runSample(azure));
    }

    @Test
    public void testManageAvailabilitySet() {
        Assertions.assertTrue(ManageAvailabilitySet.runSample(azure));
    }

    @Test
    public void testManageVirtualMachineWithUnmanagedDisks() {
        Assertions.assertTrue(ManageVirtualMachineWithUnmanagedDisks.runSample(azure));
    }

    @Test
    public void testManageVirtualMachine() {
        Assertions.assertTrue(ManageVirtualMachine.runSample(azure));
    }

    @Test
    public void testManageVirtualMachineAsync() {
        Assertions.assertTrue(ManageVirtualMachineAsync.runSample(azure));
    }

    @Test
    public void testManageVirtualMachineExtension() {
        Assertions.assertTrue(ManageVirtualMachineExtension.runSample(azure));
    }

    @Test
    public void testManageVirtualMachineScaleSet() {
        Assertions.assertTrue(ManageVirtualMachineScaleSet.runSample(azure));
    }

    @Test
    public void testManageVirtualMachineScaleSetAsync() {
        Assertions.assertTrue(ManageVirtualMachineScaleSetAsync.runSample(azure));
    }

    @Test
    public void testManageVirtualMachineScaleSetWithUnmanagedDisks() {
        Assertions.assertTrue(ManageVirtualMachineScaleSetWithUnmanagedDisks.runSample(azure));
    }

    @Test
    public void testManageVirtualMachinesInParallel() {
        Assertions.assertTrue(ManageVirtualMachinesInParallel.runSample(azure));
    }

    @Test
    public void testManageVirtualMachineWithDisk() {
        Assertions.assertTrue(ManageVirtualMachineWithDisk.runSample(azure));
    }

    @Test
    public void testConvertVirtualMachineToManagedDisks() {
        Assertions.assertTrue(ConvertVirtualMachineToManagedDisks.runSample(azure));
    }

    @Test
    public void testManageManagedDisks() {
        Assertions.assertTrue(ManageManagedDisks.runSample(azure));
    }

    @Test
    @Disabled("Skipping for now - looks like a service side issue")
    public void testManageStorageFromMSIEnabledVirtualMachine() {
        Assertions.assertTrue(ManageStorageFromMSIEnabledVirtualMachine.runSample(azure));
    }

    @Test
    @Disabled("Skipping for now - looks like a service side issue")
    public void testManageResourceFromMSIEnabledVirtualMachineBelongsToAADGroup() {
        Assertions.assertTrue(ManageResourceFromMSIEnabledVirtualMachineBelongsToAADGroup.runSample(azure));
    }

    @Test
    public void testManageUserAssignedMSIEnabledVirtualMachine() {
        Assertions.assertTrue(ManageUserAssignedMSIEnabledVirtualMachine.runSample(azure));
    }

    @Test
    public void testManageZonalVirtualMachine() {
        Assertions.assertTrue(ManageZonalVirtualMachine.runSample(azure));
    }

    @Test
    public void testManageZonalVirtualMachineScaleSet() {
        Assertions.assertTrue(ManageZonalVirtualMachineScaleSet.runSample(azure));
    }
}
